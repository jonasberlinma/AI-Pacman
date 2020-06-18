package edu.ucsb.cs56.projects.games.pacman.common;

import java.util.LinkedHashMap;

import edu.ucsb.cs56.projects.games.pacman.common.DataEvent.DataEventType;

public interface EventTrackable {

	public long getGameID();
	public int getGameStep();
	public LinkedHashMap<String, String> getData(DataEvent.DataEventType dataEvent);
}
