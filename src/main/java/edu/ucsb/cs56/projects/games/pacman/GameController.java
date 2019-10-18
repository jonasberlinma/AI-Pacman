package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * The GameController controls all game execution, both foreground real-time
 * games and background data collection games
 * 
 * @author jonas
 *
 */
public class GameController implements Runnable {

	private int nThreads = 0;
	private Thread controllerThread;
	private Vector<AIGame> gameList = new Vector<AIGame>();
	private int nCompletedGames = 0;
	private PrintWriter out = null;
	private Properties prop = null;

	// These are all related to the foreground game and the rendeting of the
	// forground game
	AIGame foregroundAIGame = null;
	BoardRenderer boardRenderer = null;
	BoardFrame bf = null;

	// Training
	private AIModelTrainer aiModelTrainer = null;
	private ArrayBlockingQueue<Vector<DataEvent>> gameResultQueue;
	private AIModel currentModel;
	private int nTrainedModels = 0;

	GameController(Properties prop) {

		gameResultQueue = new ArrayBlockingQueue<Vector<DataEvent>>(1000);

		loadTrainer(prop);
		aiModelTrainer.start();
		this.prop = prop;
		this.nThreads = Integer.parseInt(prop.getProperty("nBackgroundPlayers", "0"));

		controllerThread = new Thread(this, "BackgroundGameController");
		try {
			out = new PrintWriter(new FileOutputStream("eventlog.csv"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void start() {
		controllerThread.start();
	}

	public void stop() {
		// Turn off the renderer if there is one
		if (boardRenderer != null) {
			boardRenderer.stop();
			bf.dispose();
		}
	}

	public int getNCompletedGames() {
		return nCompletedGames;
	}

	@Override
	public void run() {

		try {
			startForegroundGame();
			Thread.sleep(100);

			while (foregroundAIGame.isRunning()) {

				Thread.sleep(10);
				Iterator<AIGame> i = gameList.iterator();
				while (i.hasNext()) {
					AIGame aiGame = i.next();
					if (!aiGame.isRunning()) {
						aiGame.join();
						i.remove();
						nCompletedGames++;
						aiGame.report(out);
						gameResultQueue.put(aiGame.getEventLog());
					}
				}

				if (gameList.size() < nThreads) {
					AIGame aiGame = new AIGame(prop, 5, true);
					aiGame.setModel(currentModel);
					gameList.addElement(aiGame);
					aiGame.start();
				}
			}
			stop();
		} catch (NumberFormatException e1) {
			System.err.println("Failed to parse property");
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			System.err.println("Failed to open event log");
			e1.printStackTrace();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e1) {
			System.err.println("Failed to load player class");
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startForegroundGame() throws NumberFormatException, FileNotFoundException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		foregroundAIGame = new AIGame(prop, Integer.parseInt(prop.getProperty("loopDelay", "40")), true);

		if (!Boolean.getBoolean(prop.getProperty("headLess"))) {
			// This circular dependency can be removed by removing the the
			// leaderboard call in Board
			boardRenderer = new BoardRenderer(foregroundAIGame.getBoard(), this);
			foregroundAIGame.addBoardRendered(boardRenderer, prop.getProperty("leaderBoard"));

			bf = new BoardFrame();

			bf.add(boardRenderer);
			boardRenderer.callLeaderboardMain(prop.getProperty("leaderBoard"));

			boardRenderer.start();
			foregroundAIGame.start();
		}
	}

	public void join() {
		try {
			controllerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setNewModel(AIModel newModel) {
		nTrainedModels++;
		foregroundAIGame.setNTrainedModels(nTrainedModels);
		foregroundAIGame.setModel(newModel);
		currentModel = newModel;
	}

	private void loadTrainer(Properties prop) {
		System.out.println("Loading trainer");
		try {
			Class<?> theClass = Class.forName(prop.getProperty("aiModelTrainerClassName"));
			aiModelTrainer = (AIModelTrainer) theClass.newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			System.err.println("Failed to load trainer");
			e.printStackTrace();
		}
		aiModelTrainer.setController(this, gameResultQueue);
	}
}
