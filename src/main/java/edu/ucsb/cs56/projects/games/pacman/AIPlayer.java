package edu.ucsb.cs56.projects.games.pacman;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public abstract class AIPlayer implements Runnable {

	private Board board = null;
	private int lastKey;

	private boolean doRun = false;
	private Thread aiPlayerThread;
	protected int playerID = 0;
	private static int nextPlayerID = 0;

	private ArrayList<DataEvent> eventCollection;

	public AIPlayer() {
		aiPlayerThread = new Thread(this, "AI Player");
		eventCollection = new ArrayList<DataEvent>(10000);
		playerID = nextPlayerID++;
	}

	protected void setAIModel(AIModel aiModel) {
		// Tell the subclass there is a new model
		newModel(aiModel);
	}

	public void start() {
		aiPlayerThread.start();
	}

	public void join() throws InterruptedException {
		aiPlayerThread.join();
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	@Override
	public void run() {
		doRun = true;
		try {
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (doRun) {
			try {
				DataEvent dataEvent = board.getDataInterface().getData();
				// Log the raw event
				logEvent(dataEvent);
				// Pass data event to subclass
				dataEvent(board.getGrid(), dataEvent);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected void stop() {
		// Tell the thread to exit at its earliest convenience
		doRun = false;
	}

	/**
	 * Subclasses implement this method to deal with game events whatever way they
	 * desire
	 * 
	 * @param dataEvent
	 */
	protected abstract void dataEvent(Grid grid, DataEvent dataEvent);

	/**
	 * Player implementations should implement this method to get new models as they
	 * are trained
	 * 
	 * @param aiModel
	 */
	protected abstract void newModel(AIModel aiModel);

	/**
	 * Player implements should implement this method to report game experience at
	 * game end
	 * 
	 * @return
	 */
	public abstract LinkedHashMap<String, DataObservation> reportExperience();

	protected void pressKey(int key) {
		if (lastKey != key) {
			board.keyReleased(lastKey);
		}
		lastKey = key;
		board.keyPressed(key);
	}

	private void logEvent(DataEvent dataEvent) {
		eventCollection.add(dataEvent);
	}

	protected ArrayList<DataEvent> getEventLog() {
		return eventCollection;
	}

}
