package edu.ucsb.cs56.projects.games.pacman;

import java.util.LinkedHashMap;

public interface EventTrackable {

	public long getGameID();
	public int getGameStep();
	public LinkedHashMap<String, String> getData(DataEvent.DataEventType dataEvent);
}
