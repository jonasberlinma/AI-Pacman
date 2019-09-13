package edu.ucsb.cs56.projects.games.pacman;

import javax.swing.JFrame;

public class BoardFrame extends JFrame	 {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8544832190499769476L;

	public BoardFrame(){
		setTitle("Pacman");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(420, 465);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}
}
