package edu.ucsb.cs56.projects.games.pacman;

import java.awt.Image;
import java.io.PrintStream;

/**
 * Class that every character inherits from, including players and AI-controlled
 * enemies.
 *
 * @author Dario Castellanos Anaya
 * @author Daniel Ly
 * @author Kelvin Yang
 * @version CS56, W15
 */
public abstract class Character {
	
	public enum PlayerType {PACMAN, MSPACMAN, GHOST1, GHOST2};
	public boolean alive;

	public PlayerType playerType;
	public int lives;
	public int deathTimer; // Used for invincibility after reviving
	public int startX, startY, speed;
	public int x, y; // x, y coordinates
	public int dx, dy; // change in x/y per move (eg left = -1, 0)
	public int reqdx, reqdy; // requested change input by keys, gets passed to
								// dx, dy when in grid

	protected DataInterface dataInterface;
	/**
	 * Constructor for Character class
	 *
	 * @param x
	 *            the starting x coordinate of pacman
	 * @param y
	 *            the starting y coordinate of pacman
	 */
	public Character(DataInterface dataInterface, int x, int y) {
		this.dataInterface = dataInterface;
		startX = x;
		startY = y;
		playerType = PlayerType.PACMAN;
		deathTimer = 1;
		alive = true;
		reset();
	}

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
		startX = x;
		startY = y;
		this.playerType = playerType;
		deathTimer = 0;
		alive = true;
		reset();
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
	 * Returns the image used for displaying remaining lives
	 *
	 * @return Image of character
	 */
	public abstract Image getLifeImage();

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
	public abstract void moveAI(Grid grid, Character[] c);

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

	public void writeState(PrintStream out, long gameID, long gameStep, int score, String characterID, boolean edible) {
		out.println("" + gameID + "," + gameStep + "," + characterID + "," + getCharacterType() + ","
				+ x / Board.BLOCKSIZE + "," + y / Board.BLOCKSIZE + "," + dx + "," + dy + "," + reqdx + "," + reqdy
				+ "," + speed + "," + edible + "," + score);
	}

	public abstract String getCharacterType();
}
