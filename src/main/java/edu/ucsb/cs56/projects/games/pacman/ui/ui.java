package edu.ucsb.cs56.projects.games.pacman.ui;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;

import edu.ucsb.cs56.projects.games.pacman.GameClient;

public class ui {

	public static void main(String[] args) {
		int port = 8081;
		boolean verbose = false;
		Iterator<String> argi = new Vector<String>(Arrays.asList(args)).iterator();

		while (argi.hasNext()) {
			String theArg = argi.next();
			switch (theArg) {
			case "-port":
				port = Integer.parseInt(argi.next());
				break;
			case "-verbose":
				verbose = true;
				break;
			default:
				System.out.println("Invalid command Line argument" + theArg);
				System.exit(1);
			}
		}

		GameClient gameClient = new GameClient(port, verbose);
		BoardRenderer boardRenderer = new BoardRenderer(gameClient);
		System.out.println("Started renderer");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				boardRenderer.createUI();
				boardRenderer.start();
			}
		});
	}
}
