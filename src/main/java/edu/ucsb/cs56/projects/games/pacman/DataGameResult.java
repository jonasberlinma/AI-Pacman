package edu.ucsb.cs56.projects.games.pacman;

import java.util.LinkedHashMap;
import java.util.Vector;

public class DataGameResult {

	public Vector<DataEvent> events;
	public LinkedHashMap<String, DataObservation> experience;

	public DataGameResult(Vector<DataEvent> events, LinkedHashMap<String, DataObservation> experience) {
		this.events = events;
		this.experience = experience;
	}
	@Override
	public String toString() {
		return "{" + events.toString() + "},{" + experience.toString() + "}";
	}
}
