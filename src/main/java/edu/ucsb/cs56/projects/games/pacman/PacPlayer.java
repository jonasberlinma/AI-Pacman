package edu.ucsb.cs56.projects.games.pacman;

import java.awt.Image;
import java.awt.event.KeyEvent;

import edu.ucsb.cs56.projects.games.pacman.DataEvent.DataEventType;

/**
 * Player controlled pacman character.
 *
 * @author Dario Castellanos
 * @author Daniel Ly
 * @author Kelvin Yang
 * @author Joseph Kompella
 * @author Kekoa Sato
 * @version CS56 F16
 */
public class PacPlayer extends Character {
	

	final int pacanimdelay = 2;
	final int pacmananimcount = 4;
	private final int pacmanspeed = 4;
	int pacanimcount = pacanimdelay;
	int pacanimdir = 1;
	int pacmananimpos = 0;

	// need these so that when pacman collides with wall and stops moving
	// he keeps facing wall instead of facing default position
	public int direction;





	/**
	 * Constructor for PacPlayer class
	 *
	 * @param x
	 *            the starting x coordinate of pacman
	 * @param y
	 *            the starting y coordinate of pacman
	 */
	public PacPlayer(DataInterface dataInterface, int x, int y, PlayerType playerType) {
		super(dataInterface, x, y, playerType);
		speed = pacmanspeed;
		lives = 3;
		direction = 3;


	}

	/**
	 * Constructor for PacPlayer class
	 *
	 * @param x
	 *            the starting x coordinate of pacman
	 * @param y
	 *            the starting y coordinate of pacman
	 * @param playerNum
	 *            int representing who the player is controlling
	 * @param grid
	 *            the grid in which PacPlayer is part of.
	 */
	public PacPlayer(DataInterface dataInterface, int x, int y, PlayerType playerNum, Grid grid) {
		super(dataInterface, x, y, playerNum);
		speed = pacmanspeed;
		lives = 3;
		direction = 3;
	}
	public void resetPos() {
		super.resetPos();
		direction = 3;
	}

	public void death() {
		if (deathTimer <= 0) {
			lives--;
			deathTimer = 40;
			resetPos();
		}
		if (lives <= 0) {
			alive = false;
		}
	}

	/**
	 * Moves character's current position with the board's collision
	 *
	 * @param grid
	 *            The Grid to be used for collision
	 */
	public void move(Grid grid, Board board) {
		if (deathTimer > 0)
			deathTimer--;
		short ch;

		// allows you to switch directions even when you are not in a grid
		if (reqdx == -dx && reqdy == -dy) {
			dx = reqdx;
			dy = reqdy;
			if (dx != 0 || dy != 0)
				direction = ((direction + 1) % 4) + 1;
		}

		if (x % Board.BLOCKSIZE == 0 && y % Board.BLOCKSIZE == 0) {

			// Tunnel effect
			x = ((x / Board.BLOCKSIZE + Board.NUMBLOCKS) % Board.NUMBLOCKS) * Board.BLOCKSIZE;
			y = ((y / Board.BLOCKSIZE + Board.NUMBLOCKS) % Board.NUMBLOCKS) * Board.BLOCKSIZE;

			ch = grid.screenData[y / Board.BLOCKSIZE][x / Board.BLOCKSIZE];

			// if pellet, eat and increase score
			if ((ch & 16) != 0) {
				// Toggles pellet bit
				DataEvent dataEvent = new DataEvent(DataEventType.EAT_PELLET, board, board);
				dataEvent.setKeyValuePair("score", "" + board.getScore());
				dataInterface.setData(dataEvent);
				grid.screenData[y / Board.BLOCKSIZE][x / Board.BLOCKSIZE] = (short) (ch ^ 16);
				board.playAudio(0);
				board.addScore(1);
				speed = 3;
			}
			// if fruit, eat and increase score
			else if ((ch & 32) != 0) {
				// Toggles fruit bit
				DataEvent dataEvent = new DataEvent(DataEventType.EAT_FRUIT, board, board);
				dataEvent.setKeyValuePair("score", "" + board.getScore());
				dataInterface.setData(dataEvent);
				grid.screenData[y / Board.BLOCKSIZE][x / Board.BLOCKSIZE] = (short) (ch ^ 32);
				board.addScore(10);
				board.playAudio(1);
				speed = 3;
			} else if ((ch & 64) != 0) {
				// Toggles pill bit
				DataEvent dataEvent = new DataEvent(DataEventType.EAT_PILL, board, board);
				dataEvent.setKeyValuePair("score", "" + board.getScore());
				dataInterface.setData(dataEvent);
				grid.screenData[y / Board.BLOCKSIZE][x / Board.BLOCKSIZE] = (short) (ch ^ 64);
				board.playAudio(1);
				board.addScore(5);
				speed = 3;
			} else
				speed = 4;

			// passes key commands to movement
			if (reqdx != 0 || reqdy != 0) {
				if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0) || (reqdx == 1 && reqdy == 0 && (ch & 4) != 0)
						|| (reqdx == 0 && reqdy == -1 && (ch & 2) != 0)
						|| (reqdx == 0 && reqdy == 1 && (ch & 8) != 0))) {
					dx = reqdx;
					dy = reqdy;
					if (reqdx == -1 && reqdy == 0 && (ch & 1) == 0)
						direction = 1;
					if (reqdx == 0 && reqdy == -1 && (ch & 2) == 0)
						direction = 2;
					if (reqdx == 1 && reqdy == 0 && (ch & 4) == 0)
						direction = 3;
					if (reqdx == 0 && reqdy == 1 && (ch & 8) == 0)
						direction = 4;
				}
			}

			// Check for standstill, stop movement if hit wall
			if ((dx == -1 && dy == 0 && (ch & 1) != 0) || (dx == 1 && dy == 0 && (ch & 4) != 0)
					|| (dx == 0 && dy == -1 && (ch & 2) != 0) || (dx == 0 && dy == 1 && (ch & 8) != 0)) {
				dx = 0;
				dy = 0;
			}
		}
		move();
	}

	
	/**
	 * Moves character's current position with the board's collision
	 *
	 * @param grid
	 *            The Grid to be used for collision
	 */
	@Override
	public void moveAI(Grid grid, Character[] c) {

	}

	/**
	 * Handles key presses for game controls
	 *
	 * @param key
	 *            Integer representing the key pressed
	 */
	public void keyPressed(int key) {
		if (playerType == PlayerType.PACMAN) {
			switch (key) {
			case KeyEvent.VK_LEFT:
				reqdx = -1;
				reqdy = 0;
				break;
			case KeyEvent.VK_RIGHT:
				reqdx = 1;
				reqdy = 0;
				break;
			case KeyEvent.VK_UP:
				reqdx = 0;
				reqdy = -1;
				break;
			case KeyEvent.VK_DOWN:
				reqdx = 0;
				reqdy = 1;
				break;
			default:
				break;
			}
		} else if (playerType == PlayerType.MSPACMAN) {
			switch (key) {
			case KeyEvent.VK_A:
				reqdx = -1;
				reqdy = 0;
				break;
			case KeyEvent.VK_D:
				reqdx = 1;
				reqdy = 0;
				break;
			case KeyEvent.VK_W:
				reqdx = 0;
				reqdy = -1;
				break;
			case KeyEvent.VK_S:
				reqdx = 0;
				reqdy = 1;
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void keyReleased(int key) {
		// move(this.grid);
		if (playerType == PlayerType.PACMAN) {
			switch (key) {
			case KeyEvent.VK_LEFT:
				reqdx = 0;
				break;
			case KeyEvent.VK_RIGHT:
				reqdx = 0;
				break;
			case KeyEvent.VK_UP:
				reqdy = 0;
				break;
			case KeyEvent.VK_DOWN:
				reqdy = 0;
				break;
			default:
				break;
			}
		} else if (playerType == PlayerType.MSPACMAN) {
			switch (key) {
			case KeyEvent.VK_A:
				reqdx = 0;
				break;
			case KeyEvent.VK_D:
				reqdx = 0;
				break;
			case KeyEvent.VK_W:
				reqdy = 0;
				break;
			case KeyEvent.VK_S:
				reqdy = 0;
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Returns the image used for displaying remaining lives
	 *
	 * @return image of pacman facing left
	 */
	@Override
	public Image getLifeImage() {
		return AssetController.getInstance().getLifeImage(playerType);
	}
}
