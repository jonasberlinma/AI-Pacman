package edu.ucsb.cs56.projects.games.pacman;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import edu.ucsb.cs56.projects.games.pacman.model.AIModel;
import edu.ucsb.cs56.projects.games.pacman.ui.BoardFrame;
import edu.ucsb.cs56.projects.games.pacman.ui.BoardRenderer;

/**
 * The GameController controls all game execution, both foreground real-time
 * games and background data collection games
 * 
 * @author jonas
 *
 */
public class GameController implements Runnable {

	private int nThreads = 0;
	private boolean noStop = false;
	private boolean keepRunning = true;
	private Thread controllerThread;
	private Vector<AIGame> gameList = new Vector<AIGame>();
	private int nCompletedGames = 0;
	private PrintWriter out = null;
	private Properties prop = null;

	// These are all related to the foreground game and the rendering of the
	// foreground game
	AIGame foregroundAIGame = null;
	BoardRenderer boardRenderer = null;
	BoardFrame abf = null;

	// Training
	private Connection connection;
	private Channel channel;
	private final String MODEL_QUEUE = "model";
	private final String GAME_QUEUE = "game";
	private AIModel currentModel;
	private int nTrainedModels = 0;

	GameController(Properties prop) {

		// First create RabbitMQ connection, channel, and queues
		//
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(prop.getProperty("host"));
		factory.setUsername(prop.getProperty("username"));
		factory.setPassword(prop.getProperty("password"));
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(MODEL_QUEUE, false, false, false, null);
			channel.queueDeclare(GAME_QUEUE, false, false, false, null);
		} catch (IOException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Rabbit MQ delivery callback for 
		DeliverCallback processNewModel = (consumerTag, delivery) -> {
			Class<?> theClass;
			try {
				theClass = Class.forName(prop.getProperty("aiModelClassName"));
				currentModel = (AIModel) theClass.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				System.err.println("Unable to find or load " + prop.getProperty("aiModelClassName"));
				e.printStackTrace();
			} 
			System.out.println("Got model data of size " + delivery.getBody().length);
			currentModel.loadModel(delivery.getBody());
			nTrainedModels++;
		};
		
		try {
			channel.basicConsume(MODEL_QUEUE, true, processNewModel, consumerTag->{});
		} catch (IOException e1) {
			System.err.println("Unable to start model consumer in GameController");
			e1.printStackTrace();
		}

		this.prop = prop;
		this.nThreads = Integer.parseInt(prop.getProperty("nBackgroundPlayers", "0"));
		this.noStop = Boolean.parseBoolean(prop.getProperty("noStop", "false"));

		controllerThread = new Thread(this, "BackgroundGameController");
		try {
			out = new PrintWriter(new FileOutputStream("eventlog.csv"));
			// This is a bad location for this code. It should be moved
			PrintWriter rewardsOut = new PrintWriter(new FileOutputStream("rewards.dat"));
			rewardsOut.flush();
			rewardsOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		controllerThread.start();
	}

	public void stop() {
		foregroundAIGame.stop();
		// Turn off the renderer if there is one
		if (boardRenderer != null) {
			boardRenderer.stop();
		}
	}

	public int getNCompletedGames() {
		return nCompletedGames;
	}

	@Override
	public void run() {

		try {
			while (keepRunning) {
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
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(baos);
							oos.writeObject(aiGame.getDataGameResult());
							oos.flush();
							channel.basicPublish("", GAME_QUEUE, null,
									baos.toByteArray());
							oos.close();
							baos.close();
						}
					}

					if (gameList.size() < nThreads) {
						AIGame aiGame = new AIGame(prop, 5, true);
						aiGame.setModel(currentModel);
						gameList.addElement(aiGame);
						aiGame.start();
					}
				}
				if (!noStop) {
					keepRunning = false;
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
		} catch (IOException e) {
			System.err.println("Failed to publish game results");
			e.printStackTrace();
		}
	}

	private void startForegroundGame() throws NumberFormatException, FileNotFoundException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		foregroundAIGame = new AIGame(prop, Integer.parseInt(prop.getProperty("loopDelay", "40")), true);
		foregroundAIGame.start();

		if (!Boolean.getBoolean(prop.getProperty("headLess"))) {
			// This circular dependency can be removed by removing the the
			// leaderboard call in Board
			int port = Integer.parseInt(prop.getProperty("port", "8081"));
			boolean verbose = Boolean.parseBoolean(prop.getProperty("verbose", "false"));
			GameServer gameServer = new GameServer(foregroundAIGame.getBoard(), this, port, verbose);
			gameServer.Start();
		}
	}

	public void join() {
		try {
			controllerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public void setNewModel(AIModel newModel) {
		nTrainedModels++;
		foregroundAIGame.setModel(newModel);
		currentModel = newModel;
	}

	public int getNTrainedModels() {
		return this.nTrainedModels;
	}

}
