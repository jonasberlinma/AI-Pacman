package edu.ucsb.cs56.projects.games.pacman;

import java.util.Vector;

public class AIModelTrainerDeepLearning extends AIModelTrainer {

	DataFlipper dataFlipper = null;

	AIModelTrainerDeepLearning() {
		dataFlipper = new DataFlipper();
		dataFlipper.addPivotField(new PivotField("gameStep", 0));
		dataFlipper.addPivotField(new PivotField("eventType", 1));
		dataFlipper.addPivotField(new PivotField("ghostNum", 2));

	}

	@Override
	protected void gameCompleteEvent(DataGameResult gameEventLog) {

		
		System.out.println("Training on " + gameEventLog.events.size() + " events and " + gameEventLog.experience.size()
				+ " experience points");
		// Prep the data
		Vector<DataObservation> observations = dataFlipper.findObservationsFromHistory(gameEventLog.events);

		int matchCount = 0;
		for (DataObservation dataObservation : observations) {
			String thisGameStep = dataObservation.get("gameStep");
			DataObservation experienceObs = gameEventLog.experience.get(thisGameStep);
			matchCount = experienceObs != null ? ++matchCount : matchCount;
		}
		System.out.println("Observations " + observations.size() + " experience count " + gameEventLog.experience.size()
				+ " match count " + matchCount);

		// Train a new model

		AIModelDeepLearning model = new AIModelDeepLearning();
		model.setDataObservations(observations);

		model.train();
		// Make the new model available to the players
		this.setNewModel(model);
		System.out.println("Sending new model " + model.getModelID());
	}
}
