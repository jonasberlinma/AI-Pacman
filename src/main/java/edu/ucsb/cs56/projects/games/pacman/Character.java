package edu.ucsb.cs56.projects.games.pacman;

//import java.awt.Image;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Vector;

import edu.ucsb.cs56.projects.games.pacman.common.DataEvent;
import edu.ucsb.cs56.projects.games.pacman.common.DataInterface;
import edu.ucsb.cs56.projects.games.pacman.common.EventTrackable;

/**
 * Class that every character inherits from, including players and AI-controlled
 * enemies.
 *
 * @author Dario Castellanos Anaya
 * @author Daniel Ly
 * @author Kelvin Yang
 * @version CS56, W15
 */
public abstract class Character implements EventTrackable {
	
	public enum PlayerType {PACMAN, MSPACMAN, GHOST1, GHOST2};
	public boolean alive;

	PlayerType playerType;
	public int lives;
	public int deathTimer; // Used for invincibility after reviving
	public int startX, startY, speed;
	public int x, y; // x, y coordinates
	public int dx, dy; // change in x/y per move (eg left = -1, 0)
	public int reqdx, reqdy; // requested change input by keys, gets passed to
								// dx, dy when in grid
	public int ghostNum = 99;
	private static int nextCharacterID = 0;
	private int characterID;

	protected DataInterface dataInterface;


	/**
	 * Constructor for Character class
	 *
	 * @param x
	 *            the starting x coordinate of pacman
	 * @param y
	 *            the starting y coordinate of pacman
	 * @param playerNum
	 *            int representing who the player is controlling
	 */
	public Character(DataInterface dataInterface, int x, int y, PlayerType playerType) {
		this.dataInterface = dataInterface;
		characterID = nextCharacterID();
		startX = x;
		startY = y;
		this.playerType = playerType;
		deathTimer = 0;
		alive = true;
		reset();
	}
	/**
	 * Default constructor for data access
	 */
	public Character() {
		
	}

	/**
	 * Restores the character's default values.
	 */
	public void reset() {
		resetPos();
		deathTimer = 0;
		lives = 3;
		alive = true;
	}

	/**
	 * Restores the character's default position
	 */
	public void resetPos() {
		this.x = startX;
		this.y = startY;
		dx = 0;
		dy = 0;
		reqdx = 0;
		reqdy = 0;
	}

	/**
	 * draws the character onto the screen
	 *
	 * @param g
	 *            a Graphics2D object
	 * @param canvas
	 *            A JComponent object to be drawn on
	 */
	// public abstract void draw(Graphics2D g, JComponent canvas);

	/**
	 * Handles character's death
	 */
	public abstract void death();

	/**
	 * Handles key presses for game controls
	 *
	 * @param key
	 *            Integer representing the key pressed
	 */
	public abstract void keyPressed(int key);

	/**
	 * Handles key releases for game controls
	 *
	 * @param key
	 *            Integer representing the key released
	 */
	public abstract void keyReleased(int key);

	/**
	 * Moves character's current position with the board's collision
	 *
	 * @param grid
	 *            The Grid to be used for collision
	 */
	public abstract void move(Grid grid, Board board);

	/**
	 * Moves character's current position with the board's collision
	 *
	 * @param grid
	 *            The Grid to be used for collision
	 * @param c
	 *            Array of target Characters
	 */
	public abstract void moveAI(Grid grid, Vector<Character> c);

	/**
	 * Moves character's current position
	 */
	public void move() {
		x = x + speed * dx;
		y = y + speed * dy;

		if (x % Board.BLOCKSIZE == 0 && y % Board.BLOCKSIZE == 0) {
			x = ((x / Board.BLOCKSIZE + Board.NUMBLOCKS) % Board.NUMBLOCKS) * Board.BLOCKSIZE;
			y = ((y / Board.BLOCKSIZE + Board.NUMBLOCKS) % Board.NUMBLOCKS) * Board.BLOCKSIZE;
		}
	}

	public static void writeHeader(PrintStream out) {
		out.println("GameID,GameStep,CharacterID,CharacterType,x,y,dx,dy,reqdx,reqdy,speed,edible,score");
	}
	
	@Override
	public long getGameID(){
		return 0;
	}
	@Override
	public int getGameStep(){
		return 0;
	}
	public LinkedHashMap<String, String> getData(DataEvent.DataEventType eventType){
		LinkedHashMap<String, String> hashtable = new LinkedHashMap<String,String>();
		hashtable.put("ghostNum", "" + ghostNum);
		hashtable.put("playerType", playerType.name());
		hashtable.put("characterID", "" + characterID);
		hashtable.put("x", "" + x / Board.BLOCKSIZE);
		hashtable.put("y", "" + y / Board.BLOCKSIZE);
		hashtable.put("dx", "" + dx);
		hashtable.put("dy", "" + dy);
		hashtable.put("reqdx", "" + reqdx);
		hashtable.put("reqdy", "" + reqdy);
		hashtable.put("speed", "" + speed);
		hashtable.put("edible", "" + getEdible());

		return hashtable;
	}
	public PlayerType getPlayerType(){
		return playerType;
	}
	public abstract boolean getEdible();
	public static synchronized int nextCharacterID(){
		return ++nextCharacterID;
	}
}
