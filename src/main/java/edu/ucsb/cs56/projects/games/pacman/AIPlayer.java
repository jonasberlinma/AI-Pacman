package edu.ucsb.cs56.projects.games.pacman;

import java.awt.event.KeyEvent;

public abstract class AIPlayer implements Runnable {

	
	private int delay;
	private Board board = null;
	private int lastKey;
	private DataInterface dataInterface = null;
	AIPlayer(Board board, int delay, DataInterface dataInterface){
		this.dataInterface = dataInterface;
		this.board = board;
	}
	
	@Override
	public void run(){
		
	
		
		while(true){
			try {
				Thread.sleep(delay);
				
				dataEvent(dataInterface.getData());
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	public abstract void dataEvent(DataEvent dataEvent);
	
	protected void pressKey(int key){
		if(lastKey != key){
			board.keyReleased(lastKey);
		}
		lastKey = key;
		board.keyPressed(key);
	}
}
