package edu.ucsb.cs56.projects.games.pacman;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class DataEvent {
	public enum DataEventType {
		NEW_BOARD, INTRO, MOVE, PACMAN_DEATH, EAT_PELLET, EAT_GHOST, EAT_FRUIT, EAT_PILL, GAME_OVER, KEY_RELEASE,
		KEY_PRESS
	};

	private LinkedHashMap<String, String> keyValues = new LinkedHashMap<String, String>();

	public DataEvent(DataEventType eventType, EventTrackable board, EventTrackable trackable) {
		this.keyValues.put("eventType", eventType.toString());
		this.keyValues.put("gameID", new Long(board.getGameID()).toString());
		this.keyValues.put("gameStep", new Integer(board.getGameStep()).toString());
		this.keyValues.putAll(trackable.getData(eventType));
	}

	public DataEvent(LinkedHashMap<String, String> keyValues) {
		this.keyValues.putAll(keyValues);
	}

	protected Set<String> getKeys() {
		return keyValues.keySet();
	}

	protected String getValue(String key) {
		return keyValues.get(key);
	}
	protected DataEventType getEventType() {
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
		this.keyValues.put("gameID", new Long(gameID).toString());
	}

	public long getGameID() {
		return Long.parseLong(this.keyValues.get("gameID"));
	}

	public int getGameStep() {
		return Integer.parseInt(this.keyValues.get("gameStep"));
		
	}

	public String getString(String key) {
		return keyValues.get(key);
	}

	public int getInt(String key) {
		return new Integer(keyValues.get(key)).intValue();
	}

	public long getLong(String key) {
		return new Long(keyValues.get(key)).longValue();
	}

	public boolean getBoolean(String key) {
		return keyValues.get(key).compareTo("true") == 0;
	}
}
