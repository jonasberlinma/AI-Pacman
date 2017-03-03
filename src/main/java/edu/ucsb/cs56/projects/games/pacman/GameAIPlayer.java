package edu.ucsb.cs56.projects.games.pacman;

public class GameAIPlayer implements Runnable{
	
	private long delay = 100;

	@Override
	public void run() {
		
		while(true){
			try {
				Thread.sleep(delay);
				
				// Figure out next move
				// Deliver the next move
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
