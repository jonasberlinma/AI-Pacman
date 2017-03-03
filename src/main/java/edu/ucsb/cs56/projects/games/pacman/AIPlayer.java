package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public abstract class AIPlayer implements Runnable {

	private Board board = null;
	private int lastKey;
	private DataInterface dataInterface = null;
	private PrintStream eventOutputStream = null;

	AIPlayer() throws FileNotFoundException {
		eventOutputStream = new PrintStream(new FileOutputStream("EventStream.csv"));
	}
	public void setBoardAndDataInterface(Board board, DataInterface dataInterface){
		this.board = board;
		this.dataInterface = dataInterface;
	}
	@Override
	public void run() {

		while (true) {
			try {
				DataEvent dataEvent = dataInterface.getData();
				dataEvent(dataEvent);
				writeEvent(dataEvent);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public abstract void dataEvent(DataEvent dataEvent);

	protected void pressKey(int key) {
		if (lastKey != key) {
			board.keyReleased(lastKey);
		}
		lastKey = key;
		board.keyPressed(key);
	}

	private void writeEvent(DataEvent dataEvent) {
		eventOutputStream.println(dataEvent.toCSV());
	}
}
