package edu.ucsb.cs56.projects.games.pacman.ui;

import javax.swing.SwingUtilities;

import edu.ucsb.cs56.projects.games.pacman.GameClient;

public class ui {

	public static void main(String[] args) {
		GameClient gameClient = new GameClient(8081);
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
