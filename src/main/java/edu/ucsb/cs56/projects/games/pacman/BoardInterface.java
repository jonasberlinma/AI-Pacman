package edu.ucsb.cs56.projects.games.pacman;

import java.util.Vector;

public interface BoardInterface {

	// This is basic board size and should not be here
	public int getBlocksize();

	public int getNumblocks();

	public int getScrsize();

	// Get the basic grid the characters are running around
	public Grid getGrid();

	// Get character locations and state
	public PacPlayer getMsPacman();

	public PacPlayer getPacman();

	public Vector<Ghost> getGhosts();

	// Get and set the game state (type). This tells us to show different screens
	public GameType getGameType();

	public void setGameType(GameType gameType);

	// Reset game. Should probably not be here
	public void resetGame(); // This one is problematic and should be removed in favor of using the escape
								// key correctly

	// Current number of pellets and score
	public int getNumPellet();

	public int getScore();

	// Press and release keys (from user keyboard)
	public void keyPressed(int key);

	public void keyReleased(int key);

	// Check if we are supposed to play audio (set by game events)
	public boolean doPlayAudio();

	public int getAudioClipID();
}
