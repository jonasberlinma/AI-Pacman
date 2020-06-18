package edu.ucsb.cs56.projects.games.pacman.common;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class DataEvent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum DataEventType  {
		NEW_BOARD, INTRO, MOVE, PACMAN_DEATH, EAT_PELLET, EAT_GHOST, EAT_FRUIT, EAT_PILL, GAME_OVER, KEY_RELEASE,
		KEY_PRESS
	};

	private LinkedHashMap<String, String> keyValues = new LinkedHashMap<String, String>();

	public DataEvent(DataEventType eventType, EventTrackable board, EventTrackable trackable) {
		this.keyValues.put("gameID", Long.valueOf(board.getGameID()).toString());
		this.keyValues.put("gameStep", Integer.valueOf(board.getGameStep()).toString());
		this.keyValues.put("time", "" + System.currentTimeMillis());
		this.keyValues.put("eventType", eventType.toString());
		this.keyValues.putAll(trackable.getData(eventType));
	}

	@Override
	public String toString() {
		return keyValues.toString();
	}
	public DataEvent(LinkedHashMap<String, String> keyValues) {
		this.keyValues.putAll(keyValues);
	}

	public Set<String> getKeys() {
		return keyValues.keySet();
	}

	public String getValue(String key) {
		return keyValues.get(key);
	}
	public DataEventType getEventType() {
		return DataEventType.valueOf(this.keyValues.get("eventType"));
	}
	public void setKeyValuePair(String key, String value) {
		keyValues.put(key, value);
	}

	public String toCSV() {
		StringBuffer out = new StringBuffer();

		Iterator<String> i = keyValues.keySet().iterator();

		while (i.hasNext()) {
			String key = i.next();
				out.append("," + key + "=" + keyValues.get(key));
		}

		return out.toString();
	}

	public void setGameID(long gameID) {
		this.keyValues.put("gameID", Long.valueOf(gameID).toString());
	}

	public long getGameID() {
		return Long.valueOf(this.keyValues.get("gameID"));
	}

	public int getGameStep() {
		return Integer.valueOf(this.keyValues.get("gameStep"));
	}

	public String getString(String key) {
		return keyValues.get(key);
	}

	public int getInt(String key) {
		return Integer.valueOf(keyValues.get(key));
	}

	public long getLong(String key) {
		return Long.valueOf(keyValues.get(key));
	}

	public boolean getBoolean(String key) {
		return keyValues.get(key).compareTo("true") == 0;
	}
}
