package edu.ucsb.cs56.projects.games.pacman.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class FlipData {

	public static void main(String[] args) {

		Iterator<String> argi = new ArrayList<String>(Arrays.asList(args)).iterator();

		String fileName = null;
		
		System.out.println(System.getProperty("java.version"));

		while (argi.hasNext()) {
			String theArg = argi.next();
			switch (theArg) {
			case "-fileName":
				fileName = argi.next();
				break;

			default:
				System.out.println("Invalid command Line argument" + theArg);
				System.exit(1);
			}
		}
		DataFlipper flipper = new DataFlipper();
		flipper.addPivotField(new PivotField("gameStep", 0));
		flipper.addPivotField(new PivotField("eventType", 1));
		flipper.addPivotField(new PivotField("ghostNum", 2));
		try {
			PrintWriter writer = new PrintWriter(new File("foo.csv"));

			BufferedReader fb = new BufferedReader(new FileReader(fileName));
			flipper.findObservationsFromHistory(fb, writer);
			fb.close();

			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
