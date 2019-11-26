package edu.ucsb.cs56.projects.games.pacman;

import javax.swing.JFrame;

public class BoardFrame extends JFrame	 {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8544832190499769476L;

	public BoardFrame(){
		this.setTitle("Pacman");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(420, 465);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.toFront();
		this.setVisible(true);
	}
}
