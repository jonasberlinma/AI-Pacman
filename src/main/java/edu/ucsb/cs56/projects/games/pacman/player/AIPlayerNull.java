package edu.ucsb.cs56.projects.games.pacman.player;

import java.util.LinkedHashMap;

import edu.ucsb.cs56.projects.games.pacman.Grid;
import edu.ucsb.cs56.projects.games.pacman.common.DataEvent;
import edu.ucsb.cs56.projects.games.pacman.common.DataObservation;
import edu.ucsb.cs56.projects.games.pacman.model.AIModel;

public class AIPlayerNull extends AIPlayer {

	@Override
	protected void dataEvent(Grid grid, DataEvent dataEvent) {

	}

	@Override
	public void newModel(AIModel aiModel) {
		// Doesn't use model training

	}

	@Override
	public LinkedHashMap<String, DataObservation> reportExperience() {
		// Since we don't do anything we don't have any experience
		return null;
	}

}
