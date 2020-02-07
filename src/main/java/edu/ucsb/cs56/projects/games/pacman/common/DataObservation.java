package edu.ucsb.cs56.projects.games.pacman.common;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class DataObservation extends LinkedHashMap<String, String> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Create deep copy of observation

	public DataObservation deepClone() {
		DataObservation theObservation = new DataObservation();
		this.forEach((x, y)-> theObservation.put(new String(x), new String(y)));
		return theObservation;
	}
	public void dumpComparison(DataObservation otherObservation) {
		this.forEach((x, y)->System.out.print(x + "=" + y + ";" + otherObservation.get(x) + ", "));
		System.out.println();
	}
}
