package edu.ucsb.cs56.projects.games.pacman.model;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import edu.ucsb.cs56.projects.games.pacman.common.DataEvent;
import edu.ucsb.cs56.projects.games.pacman.common.DataObservation;

public class DataFlipper {

	private Hashtable<String, PivotField> pivotFields = new Hashtable<String, PivotField>();

	public void addPivotField(PivotField pivotField) {
		pivotFields.put(pivotField.getFieldName(), pivotField);
	}

	/**
	 * Read a file (fb) and split out the lines that make up each observation using
	 * the "gameStep" field This is used to read a eventlog.csv file
	 * 
	 * @param fb
	 * @param writeData
	 * @param writer
	 */
	public void findObservationsFromHistory(BufferedReader fb, PrintWriter writer) {
		try {

			String line;
			ArrayList<DataEvent> parsedLines = new ArrayList<DataEvent>();
			int level0Value = 0;
			ArrayList<DataObservation> observations = new ArrayList<DataObservation>();
			while ((line = fb.readLine()) != null) {

				// Parse the line
				DataEvent parsedLine = parseLine(line);
				// Check if we have a key break
				if (level0Value != parsedLine.getGameStep()) {
					// If so get the data values
					DataObservation observation = getObservation(parsedLines);
					// and write the output if we are supposed to
					if (level0Value != 0) {
						observations.add(observation);
					}
					parsedLines.clear();
					// And set the new value
					level0Value = parsedLine.getGameStep();
				}
				parsedLines.add(parsedLine);
			}
			writeObservations(observations, writer);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Find sets of DataEvents that make up the same observation This is used to
	 * parse the standard event stream from player
	 * 
	 * @param gameEventHistory
	 * @return
	 */
	public ArrayList<DataObservation> findObservationsFromHistory(ArrayList<DataEvent> gameEventHistory) {
		ArrayList<DataObservation> observations = new ArrayList<DataObservation>();
		int level0Value = 0;
		ArrayList<DataEvent> observationEvents = new ArrayList<DataEvent>();
		for (DataEvent dataEvent : gameEventHistory) {
			// Parse the line
			// Check if we have a key break
			if (level0Value != dataEvent.getGameStep()) {
				// If so get the data values
				// Pivot the events into a single observation
				observations.add(getObservation(observationEvents));
				// And set the new value
				level0Value = dataEvent.getGameStep();
				observationEvents.clear();
			}
			observationEvents.add(dataEvent);
		}
		return observations;
	}

	/**
	 * Take a set of data events that represent one observation in a played game in
	 * the format of hash tables and pivot them into a single hash table
	 * representing the observation
	 * 
	 * @param parsedLines
	 * @return
	 */
	public DataObservation getObservation(ArrayList<DataEvent> events) {

		String keyPrefix = "";
		// Through the file line by line

		// The top level pivot field value is not the same so we have found a new top
		// level group
		DataObservation observation = new DataObservation();
		for (DataEvent event : events) {
			// Loop through the key value pairs on the line. Remember the order matters
			for (String key : event.getKeys()) {
				String value = event.getValue(key);
				// Check if the key is a pivot key
				if (pivotFields.containsKey(key)) {
					PivotField pivotField = pivotFields.get(key);
					// If this is the top level pivot
					if (pivotField.getPivotLevel() == 0) {
						// We found the top pivot
						// Add it to the output
						observation.put(key, value);
						// If the pivot field is not the top level we have to start concatenating fields
						// to get the compound key. All fields after this one are expected to be this
						// pivot level until we hit the next pivot.
					} else if (pivotField.getPivotLevel() > 0) {
						keyPrefix = keyPrefix + value;
					}
					// If it is not a pivot field add it
				} else if (value != null) {
					try {
						observation.put(keyPrefix + key, standardizeValue(value));
					} catch (Exception e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}
			// Reset the pivot for next event
			keyPrefix = "";
		}
		return observation;
	}

	/**
	 * Parse a cvs line of key value pairs and create a hash table
	 * 
	 * @param line
	 * @return
	 */
	private DataEvent parseLine(String line) {
		String[] pairs = line.split(",");
		LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();
		for (String pair : pairs) {
			String[] keyValue = pair.split("=");
			String key = keyValue[0];
			String value = keyValue.length > 1 ? keyValue[1] : "";
			temp.put(key, value);
		}
		DataEvent parsedLine = new DataEvent(temp);

		for (String key : temp.keySet()) {
			parsedLine.setKeyValuePair(key, temp.get(key));
		}
		return parsedLine;
	}

	/**
	 * Write an observation in CSV format
	 * 
	 * @param stepDataValues
	 * @param write
	 * @throws Exception
	 */
	private void writeObservations(ArrayList<DataObservation> observations, PrintWriter write) throws Exception {
		LinkedHashSet<String> uniqueKeys = new LinkedHashSet<String>();
		for (LinkedHashMap<String, String> observation : observations) {
			uniqueKeys.addAll(observation.keySet());
		}
		for (String key : uniqueKeys) {
			write.print("" + key + ",");
		}
		write.println();
		for (LinkedHashMap<String, String> observation : observations) {
			for (String key : uniqueKeys) {
				String stringValue = observation.get(key) != null ? observation.get(key) : "";

				write.print(standardizeValue(stringValue) + ",");
			}
			write.println();
		}
	}

	String standardizeValue(String inputValue) {
		String value = "0";
		switch (inputValue) {
		case "True":
		case "true":
			value = "1";
			break;
		case "False":
		case "false":
			value = "2";
			break;
		case "":
			value = "0";
			break;
		// Key presses and directions
		case "←":
		case "LEFT":
			value = "1";
			break;

		case "→":
		case "RIGHT":
			value = "2";
			break;
		case "↓":
		case "DOWN":
			value = "3";
			break;
		case "↑":
		case "UP":
			value = "4";
			break;
		case "S":
			value = "5";
			break;
		// Character types
		case "PACMAN":
			value = "1";
			break;
		case "GHOST1":
			value = "2";
			break;
		case "GHOST2":
			value = "3";
			break;
		// Defaults
		default:
			try {
				Long.parseLong(inputValue);
				value = inputValue;
			} catch (NumberFormatException e) {
				//System.err.println("Unknown value " + inputValue);
				value = "0";
			}
		}
		return value;
	}
}
