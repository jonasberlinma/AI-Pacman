package edu.ucsb.cs56.projects.games.pacman;

import java.util.Vector;

public class AIModelTrainerNull extends AIModelTrainer {

	private int nGames = 0;
	@Override
	protected void gameCompleteEvent(Vector<DataEvent> gameEventLog) {
		nGames++;
		
		if(nGames % 10 == 0){
			this.setNewModel(new AIModelRandom(System.currentTimeMillis()));
			System.out.println("Sending new model");
		} 
		System.out.println("Training on " + gameEventLog.size() + " events");
	}
}
