package edu.ucsb.cs56.projects.games.pacman;

import java.util.Vector;

public class AIModelTrainerDeepLearning extends AIModelTrainer {
	@Override
	protected void gameCompleteEvent(Vector<DataEvent> gameEventLog) {

		System.out.println("Training on " + gameEventLog.size() + " events");
		// Prep the data
		// Train a new model
		// Make the new model available to the players
		this.setNewModel(null);
		System.out.println("Sending new model");
	}
}
