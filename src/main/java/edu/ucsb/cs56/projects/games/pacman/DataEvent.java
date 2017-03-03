package edu.ucsb.cs56.projects.games.pacman;

public class DataEvent {
	public enum DataEventType {
		INTRO, MOVE, PACMAN_DEATH, EAT_PELLET, EAT_GHOST, EAT_FRUIT, EAT_PILL, GAME_OVER, KEY_RELEASE, KEY_PRESS
	};

	DataEventType eventType;

	public DataEvent(DataEventType eventType) {
		this.eventType = eventType;
	}
}
