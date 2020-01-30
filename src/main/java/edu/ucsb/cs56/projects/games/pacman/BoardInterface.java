package edu.ucsb.cs56.projects.games.pacman;

import java.util.Vector;

public interface BoardInterface {

	public PacPlayer getMsPacman();

	public void setMsPacman(PacPlayer msPacman);
	public PacPlayer getPacman();
	public void setPacman(PacPlayer pacman);

	public Vector<Ghost> getGhosts();
	public void setGhosts(Vector<Ghost> ghosts);

	public int getNumPellet();

	public void setNumPellet(int numPellet);
	
	public int getBlocksize();
	public int getNumblocks();
	public int getScrsize();
}
