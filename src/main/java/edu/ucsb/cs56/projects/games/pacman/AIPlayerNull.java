package edu.ucsb.cs56.projects.games.pacman;

import java.util.LinkedHashMap;

public class AIPlayerNull extends AIPlayer {

	@Override
	protected void dataEvent(Grid grid, DataEvent dataEvent) {

	}

	@Override
	protected void newModel(AIModel aiModel) {
		// Doesn't use model training

	}

	@Override
	public LinkedHashMap<String, DataObservation> reportExperience() {
		// Since we don't do anything we don't have any experience
		return null;
	}

}
