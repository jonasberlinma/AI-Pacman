package edu.ucsb.cs56.projects.games.pacman;

import java.util.HashMap;
import java.util.Iterator;

public class DataEvent {
	public enum DataEventType {
		NEW_BOARD, INTRO, MOVE, PACMAN_DEATH, EAT_PELLET, EAT_GHOST, EAT_FRUIT, EAT_PILL, GAME_OVER, KEY_RELEASE, KEY_PRESS
	};

	DataEventType eventType;
	private long gameID = 0;
	private int gameStep = 0;
	HashMap<String, String> keyValues = new HashMap<String, String>(); 

	public DataEvent(DataEventType eventType, EventTrackable trackable) {
		this.eventType = eventType;
		this.gameID = trackable.getGameID();
		this.gameStep = trackable.getGameStep();
	}
	public void setKeyValuePair(String key, String value){
		keyValues.put(key, value);
	}
	public String toCSV(){
		StringBuffer out = new StringBuffer();
		
//		out.append("" + gameID + "," + gameStep + "," + eventType);
		out.append(gameStep + "," + eventType);
		
		Iterator<String> i = keyValues.keySet().iterator();
		
		while(i.hasNext()){
			String key = i.next();
			out.append(key + "," + keyValues.get(key));
		}
		
		return out.toString();
	}
}
