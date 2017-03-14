package edu.ucsb.cs56.projects.games.pacman;

import java.util.Map;

public interface EventTrackable {

	public long getGameID();
	public int getGameStep();
	public Map<String, String> getData();
}
