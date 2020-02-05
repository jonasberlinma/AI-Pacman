package edu.ucsb.cs56.projects.games.pacman;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

public interface GameInterface {

	// Get the basic grid the characters are running around
	public Grid getGrid();

	// Get character locations and state
	public PacPlayer getMsPacman();

	public PacPlayer getPacman();

	public Vector<Ghost> getGhosts();

	// Get the game state (type). This tells us to show different screens
	public GameType getGameType();

	// Current number of pellets and score
	public int getNumPellet();

	public int getScore();

	// Press and release keys (from user keyboard)
	public void keyPressed(int key);

	public void keyReleased(int key);

	// Check if we are supposed to play audio (set by game events)
	public int getAudioClipID();

	// Overall status of games

	public int getNCompletedGames();

	public int getNTrainedModels();

	// Methods in this section are primarily used to study and manipulate the board
	// in "Local" mode
	// Shortest point to point path
	public ArrayList<PathSection> getShortestPath(int x1, int y1, int x2, int y2);

	public ArrayList<Direction> getPossibleDirections(int x, int y);

	public void putGhost(int x, int y);

	public void clear(int x, int y);

	public LinkedHashMap<String, ArrayList<PathSection>> analyze(int x, int y);

}
