package edu.ucsb.cs56.projects.games.pacman.model;

import edu.ucsb.cs56.projects.games.pacman.common.DataGameResult;

public class AIModelTrainerNull extends AIModelTrainer {

	private int nGames = 0;
	@Override
	protected void gameCompleteEvent(DataGameResult gameEventLog) {
		nGames++;
		
		if(nGames % 10 == 0){
			this.setNewModel(new AIModelRandom(System.currentTimeMillis()));
			System.out.println("Sending new model");
		} 
		System.out.println("Training on " + gameEventLog.events.size() + " events");
	}
	@Override
	public void doTrain() {
		
	}
}
