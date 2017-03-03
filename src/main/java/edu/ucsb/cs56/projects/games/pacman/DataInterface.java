package edu.ucsb.cs56.projects.games.pacman;

import java.util.concurrent.ArrayBlockingQueue;

public class DataInterface extends ArrayBlockingQueue<DataEvent> {

	private int foo = 0;

	public DataInterface() {
		super(1000);

	}

	public void setData(DataEvent dataEvent) {
		this.add(dataEvent);
	}
	public DataEvent getData() throws InterruptedException {
		return this.take();
	}
}
