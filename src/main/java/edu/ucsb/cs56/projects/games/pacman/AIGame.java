package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import edu.ucsb.cs56.projects.games.pacman.ui.BoardRenderer;

public class AIGame implements Runnable {

	private AIPlayer aiPlayer = null;
	private Board board = null;
	private BoardRenderer boardRenderer;
	private Thread aiGameThread;
	private boolean isRunning = false;
	private AIModel currentModel = null;

	public AIGame(Properties prop, int loopDelay, boolean background)
			throws FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		board = new Board(prop, !background);
		board.setLoopDelay(loopDelay);
		
		Class<?> theClass = Class.forName(prop.getProperty("aiPlayerClassName"));

		aiPlayer = (AIPlayer) theClass.newInstance();
		aiPlayer.setBoard(board);
		aiPlayer.setAIModel(currentModel);
		aiGameThread = new Thread(this, "AI Game Player 1");
	}

	public void addBoardRendered(BoardRenderer boardRenderer) {
		this.boardRenderer = boardRenderer;
	}

	public Board getBoard() {
		return this.board;
	}

	public void start() {
		aiGameThread.start();
	}

	public void join() throws InterruptedException {
		aiGameThread.join();
	}

	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public void run() {
		isRunning = true;
		if (boardRenderer != null) {
			boardRenderer.callLeaderboardMain();
		}
		aiPlayer.start();
		board.start();
		try {
			aiPlayer.join();
			board.stop();
			board.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		isRunning = false;
	}

	protected void report(PrintWriter out) {
		ArrayList<DataEvent> events = aiPlayer.getEventLog();
		Iterator<DataEvent> i = events.iterator();
		while (i.hasNext()) {
			out.println(i.next().toCSV());
		}
	}

	public DataGameResult getDataGameResult() {
		DataGameResult dgr = new DataGameResult(aiPlayer.getEventLog(), aiPlayer.reportExperience());
		return dgr;
	}

	public void setModel(AIModel newModel) {
		aiPlayer.newModel(newModel);

	}
}
