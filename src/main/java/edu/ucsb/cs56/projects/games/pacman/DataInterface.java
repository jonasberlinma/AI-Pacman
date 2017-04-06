package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;

public class DataInterface extends ArrayBlockingQueue<DataEvent> {

	private PrintStream eventOutputStream = null;
	private boolean doWrite = false;
	
	public DataInterface(boolean doWrite) throws FileNotFoundException {
		super(1000);
		this.doWrite = doWrite;
		eventOutputStream = new PrintStream(new FileOutputStream("EventStream.csv"));
	}

	public synchronized void setData(DataEvent dataEvent) {
		// If we have no player to pull off the events it fills up
		// Gross implementation should be fixed
		if (this.remainingCapacity() > 0) {
			this.add(dataEvent);
		}
		writeEvent(dataEvent);
	}

	public DataEvent getData() throws InterruptedException {
		return this.take();
	}

	private void writeEvent(DataEvent dataEvent) {
		if (doWrite)
			eventOutputStream.println(dataEvent.toCSV());
	}
}
