package edu.ucsb.cs56.projects.games.pacman;

import java.util.ArrayList;

public class AIModelTrainerDeepLearning extends AIModelTrainer {

	DataFlipper dataFlipper = null;
	ArrayList<DataObservation> observationHistory = new ArrayList<DataObservation>();

	AIModelTrainerDeepLearning() {
		dataFlipper = new DataFlipper();
		dataFlipper.addPivotField(new PivotField("gameStep", 0));
		dataFlipper.addPivotField(new PivotField("eventType", 1));
		dataFlipper.addPivotField(new PivotField("ghostNum", 2));

	}

	@Override
	protected void gameCompleteEvent(DataGameResult gameEventLog) {

		System.out.println("Adding " + gameEventLog.events.size() + " events and " + gameEventLog.experience.size()
				+ " experience points");
		// Prep the data
		ArrayList<DataObservation> observations = dataFlipper.findObservationsFromHistory(gameEventLog.events);

		int matchCount = 0;
		// Match observations to experience
		for (DataObservation dataObservation : observations) {
			String thisGameStep = dataObservation.get("gameStep");
			DataObservation experienceObs = gameEventLog.experience.get(thisGameStep);
			matchCount = experienceObs != null ? ++matchCount : matchCount;
			if (experienceObs != null) {
				dataObservation.put("reward", experienceObs.get("reward"));
			}

		}

		observationHistory.addAll(observations);

	}

	@Override
	public void doTrain() {
		System.out.println("Total observations " + observationHistory.size());
		// Train a new model

		AIModelDeepLearning model = new AIModelDeepLearning();
		model.setDataObservations(observationHistory);

		model.train();
		// Make the new model available to the players
		System.out.println("Starting test");
		// model.test();
		model.printData("theData.csv");
		System.out.println("Test done");

		this.setNewModel(model);
		System.out.println("Sending new model " + model.getModelID());
	}
}
