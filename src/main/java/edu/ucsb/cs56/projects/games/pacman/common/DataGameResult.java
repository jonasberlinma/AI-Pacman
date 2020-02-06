package edu.ucsb.cs56.projects.games.pacman.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DataGameResult {

	public ArrayList<DataEvent> events;
	public LinkedHashMap<String, DataObservation> experience;

	public DataGameResult(ArrayList<DataEvent> events, LinkedHashMap<String, DataObservation> experience) {
		this.events = events;
		this.experience = experience;
	}

	@Override
	public String toString() {
		return "{" + events.toString() + "},{" + experience.toString() + "}";
	}
}
