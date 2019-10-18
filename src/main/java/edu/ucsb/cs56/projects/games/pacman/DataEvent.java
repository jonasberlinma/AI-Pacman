package edu.ucsb.cs56.projects.games.pacman;

import java.util.HashMap;
import java.util.Iterator;

public class DataEvent {
	public enum DataEventType {
		NEW_BOARD, INTRO, MOVE, PACMAN_DEATH, EAT_PELLET, EAT_GHOST, EAT_FRUIT, EAT_PILL, GAME_OVER, KEY_RELEASE,
		KEY_PRESS
	};

	DataEventType eventType;
	private long gameID = 0;
	private int gameStep = 0;
	private HashMap<String, String> keyValues = new HashMap<String, String>();

	public DataEvent(DataEventType eventType, EventTrackable board, EventTrackable trackable) {
		this.eventType = eventType;
		this.gameID = board.getGameID();
		this.gameStep = board.getGameStep();
		this.keyValues.putAll(trackable.getData(eventType));
	}

	public void setKeyValuePair(String key, String value) {
		keyValues.put(key, value);
	}

	public String toCSV() {
		StringBuffer out = new StringBuffer();

		out.append("gameID=" + gameID + ",gameStep=" + gameStep + ",time=" + System.currentTimeMillis() + ",eventType="
				+ eventType);

		if (keyValues.containsKey("playerType")) {
			out.append(",playerType=" + keyValues.get("playerType"));
		}

		Iterator<String> i = keyValues.keySet().iterator();

		while (i.hasNext()) {
			String key = i.next();
			if (key.compareTo("playerType") != 0) {
				out.append("," + key + "=" + keyValues.get(key));
			}
		}

		return out.toString();
	}

	public void setGameID(long gameID) {
		this.gameID = gameID;
	}

	public String getString(String key) {
		return keyValues.get(key);
	}

	public int getInt(String key) {
		return new Integer(keyValues.get(key)).intValue();
	}

	public boolean getBoolean(String key) {
		return keyValues.get(key).compareTo("true") == 0;
	}
}
