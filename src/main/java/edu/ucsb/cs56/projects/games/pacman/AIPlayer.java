package edu.ucsb.cs56.projects.games.pacman;

public abstract class AIPlayer implements Runnable {

	private Board board = null;
	private int lastKey;

	private boolean doRun = false;
	Thread aiPlayerThread;

	public AIPlayer(){
		aiPlayerThread = new Thread(this, "AI Player");
	}
	public void start(){
		aiPlayerThread.start();
	}
	public void join() throws InterruptedException{
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

		}
		while (doRun) {
			try {
				DataEvent dataEvent = board.getDataInterface().getData();
				dataEvent(dataEvent);


			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected void stop() {
		// Figure out how to cleanly stop the thread
		doRun = false;
	}

	protected abstract void dataEvent(DataEvent dataEvent);

	protected void pressKey(int key) {
		if (lastKey != key) {
			board.keyReleased(lastKey);
		}
		lastKey = key;
		board.keyPressed(key);
	}


}
