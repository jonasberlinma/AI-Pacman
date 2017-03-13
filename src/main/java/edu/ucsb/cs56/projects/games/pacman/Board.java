package edu.ucsb.cs56.projects.games.pacman;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import edu.ucsb.cs56.projects.games.pacman.Character.PlayerType;
import edu.ucsb.cs56.projects.games.pacman.DataEvent.DataEventType;

/**
 * Playing field for a Pac-Man arcade game remake that keeps track of all
 * relevant data and handles game logic.
 * <p>
 * The version of the code by Jan Bodnar may be found at
 * http://zetcode.com/tutorials/javagamestutorial/pacman/
 *
 * @author Brian Postma
 * @author Jan Bodnar
 * @author Dario Castellanos
 * @author Brandon Newman
 * @author Daniel Ly
 * @author Deanna Hartsook
 * @author Kateryna Fomenko
 * @author Yuxiang Zhu
 * @author Kelvin Yang
 * @author Joseph Kompella
 * @author Kekoa Sato
 * @version CS56 F16
 */
public class Board implements Runnable, EventTrackable {
	/**
	 * 
	 */

	public static final int BLOCKSIZE = 24;
	public static final int NUMBLOCKS = 17;
	public static final int SCRSIZE = NUMBLOCKS * BLOCKSIZE;

	private final int MAX_GHOSTS = 12;
	private final int MAX_SPEED = 6;

	private long gameID = 0;
	private int gameStep = 0;

	private int score;
	private Grid grid;
	private GameType gt;
	PacPlayer pacman;
	PacPlayer msPacman;
	Ghost ghost1, ghost2;
	Character[] pacmen;
	ArrayList<Ghost> ghosts;
	private int numGhosts = 6;
	int numBoardsCleared = 0;
	private int curSpeed = 3;
	int numPellet;
	private int numPills;
	private int loopDelay;


	public BoardRenderer boardRenderer = null;
	private DataInterface dataInterface;
	private Thread boardThread;

	private boolean doRun = false;

	/**
	 * Constructor for Board object
	 * 
	 * @throws FileNotFoundException
	 */
	public Board(boolean doWrite) throws FileNotFoundException {

		dataInterface = new DataInterface(doWrite);

		// openOutputs();

		grid = new Grid();

		gt = GameType.INTRO;
		grid.levelInit(0);

		// grid.writeGrid(gridOut);

		pacman = new PacPlayer(dataInterface, 8 * BLOCKSIZE, 11 * BLOCKSIZE, PlayerType.PACMAN, grid);
		//msPacman = new PacPlayer(dataInterface, 7 * BLOCKSIZE, 11 * BLOCKSIZE, PacPlayer.MSPACMAN, grid);
		ghost1 = new Ghost(dataInterface, 8 * BLOCKSIZE, 7 * BLOCKSIZE, 3, PlayerType.GHOST1, grid);
		ghost2 = new Ghost(dataInterface, 9 * BLOCKSIZE, 7 * BLOCKSIZE, 3, PlayerType.GHOST2, grid);

		ghosts = new ArrayList<Ghost>();
		numPills = 4;

	
		dataInterface.setData(new DataEvent(DataEventType.INTRO, this));

		boardThread = new Thread(this, "Game Board");

	}

	/**
	 * Start the board
	 */
	public void start() {
		boardThread.start();
	}
	
	public void stop(){
		doRun = false;
	}

	/**
	 * Wait for the board thread to exit an join it. Called by IAGame
	 * 
	 * @throws InterruptedException
	 */
	public void join() throws InterruptedException {
		boardThread.join();
	}

	public void setLoopDelay(int loopDelay) {
		this.loopDelay = loopDelay;
	}

	public DataInterface getDataInterface() {
		return dataInterface;
	}

	public Grid getGrid() {
		return grid;
	}

	public GameType getGameType() {
		return gt;
	}

	public int getScore() {
		return score;
	}

	public void addScore(int addScore) {
		score = score + addScore;
	}

	public void addBoardRenderer(BoardRenderer boardRenderer) {
		this.boardRenderer = boardRenderer;
	}

	/**
	 * Main game logic loop
	 *
	 * @param g2d
	 *            a Graphics 2D object
	 */
	private void playGame() {
		if (gt == GameType.SINGLEPLAYER || gt == GameType.VERSUS || gt == GameType.COOPERATIVE) {

			if (!checkAlive()) {
				gameOver();
			} else {
				if (pacman.alive) {
					pacman.move(grid, this);
					if (grid.getPillNum() != numPills) {
						for (Ghost g : ghosts) {
							g.edible = true;
							g.edibleTimer = 200;
						}
						numPills = grid.getPillNum();
					}
				}
			}
			switch (gt) {
			case SINGLEPLAYER:
				for (Ghost g : ghosts) {
					g.moveAI(grid, pacmen);
				}
				grid.incrementFruit(numBoardsCleared);
				detectCollision(ghosts);
				break;
			case COOPERATIVE:
				if (msPacman.alive) {
					msPacman.move(grid, this);
				}
				for (Ghost g : ghosts) {
					g.moveAI(grid, pacmen);
				}
				grid.incrementFruit(numBoardsCleared);
				detectCollision(ghosts);
				break;
			case VERSUS:
				for (Ghost g : ghosts) {
					g.move(grid, this);
				}

				if (score >= numPellet) {
					score = 0;
					numBoardsCleared++;
					grid.levelInit(numBoardsCleared);
					levelContinue();

				}
				grid.incrementFruit(numBoardsCleared);
				detectCollision(ghosts);
				break;
			case HELP:
				break;
			case INTRO:
				break;
			case LEADERBOARD:
				break;
			default:
				break;
			}
			if (grid.checkMaze()) {
				score += 50;
				numBoardsCleared++;

				numGhosts = (numGhosts + 1) % MAX_GHOSTS;
				curSpeed = (curSpeed + 1) % MAX_SPEED;
				grid.levelInit(numBoardsCleared);
				levelContinue();
			}
		}
	}

	/**
	 * End the game if remaining lives reaches 0.
	 */
	public void gameOver() {

		dataInterface.setData(new DataEvent(DataEventType.GAME_OVER, this));
		if (boardRenderer != null)
			boardRenderer.drawGameOver();

		gt = GameType.INTRO;

		numBoardsCleared = 0;
		grid.levelInit(0);
	}

	/**
	 * Detects when ghosts and pacman collide
	 *
	 * @param ghosts
	 *            An array of Ghost
	 */
	public void detectCollision(ArrayList<Ghost> ghosts) {
		for (Character pacman : pacmen) {
			for (Ghost ghost : ghosts) {
				if ((Math.abs(pacman.x - ghost.x) < 20 && Math.abs(pacman.y - ghost.y) < 20) && ghost.edible == false) {
					if (pacman.deathTimer <= 0) {
						dataInterface.setData(new DataEvent(DataEventType.PACMAN_DEATH, this));
					}
					;
					pacman.death();
				}

				if ((Math.abs(pacman.x - ghost.x) < 20 && Math.abs(pacman.y - ghost.y) < 20) && ghost.edible == true) {
					dataInterface.setData(new DataEvent(DataEventType.EAT_GHOST, this));
					ghost.death();
					score += 40;
				}
			}
		}
	}

	/**
	 * Returns true if any pacman is alive, returns false if they are all dead
	 *
	 * @return true if any surviving, false if all dead
	 */
	public boolean checkAlive() {
		for (Character pacman : pacmen)
			if (pacman.alive)
				return true;
		return false;
	}

	public void resetGame() {
		gt = GameType.INTRO;
		numBoardsCleared = 0;
		grid.levelInit(0);
	}

	/**
	 * Initialize game variables
	 */
	public void gameInit() {
		grid.levelInit(numBoardsCleared);
		levelContinue();
		score = 0;
		numGhosts = 6;
		curSpeed = 3;
		numPills = 4;



		switch (gt) {
		case SINGLEPLAYER:
			pacmen = new Character[1];
			pacmen[0] = pacman;
			pacman.reset();
			break;
		case COOPERATIVE:
			pacmen = new Character[2];
			pacmen[0] = pacman;
			pacmen[1] = msPacman;
			pacman.reset();
			msPacman.reset();
			break;
		case VERSUS:
			pacmen = new Character[1];
			pacmen[0] = pacman;
			pacman.reset();
			break;
		default:
			break;
		}
		gameID = System.currentTimeMillis();
		gameStep = 0;
	}

	/**
	 * Initialize Pacman and ghost position/direction
	 */
	public void levelContinue() {
		numPellet = grid.getPelletNum() + grid.getPillNum();
		numPills = grid.getPillNum();
		ghosts.clear();
		if (gt == GameType.VERSUS) {
			ghosts.add(ghost1);
			ghosts.add(ghost2);
		} else {
			for (int i = 0; i < numGhosts; i++) {
				int random = (int) (Math.random() * curSpeed) + 1;
				ghosts.add(new Ghost(dataInterface, (i + 6) * BLOCKSIZE, 2 * BLOCKSIZE, random, i % 2));
			}
		}
		switch (gt) {
		case SINGLEPLAYER:
			pacman.resetPos();
			break;
		case COOPERATIVE:
			pacman.resetPos();
			msPacman.resetPos();
			break;
		case VERSUS:
			pacman.resetPos();
			for (Character ghost : ghosts) {
				ghost.resetPos();
				if (numBoardsCleared == 3)
					ghost.speed = MAX_SPEED;
			}
			break;
		default:
			break;
		}
	}

	public void keyPressed(int key) {

		if (gt == GameType.INTRO) {
			switch (key) {
			case KeyEvent.VK_S:
				gt = GameType.SINGLEPLAYER;
				gameInit();
				break;
			case KeyEvent.VK_D:
				gt = GameType.COOPERATIVE;
				gameInit();
				break;
			case KeyEvent.VK_F:
				gt = GameType.VERSUS;
				gameInit();
				break;
			case KeyEvent.VK_H:
				gt = GameType.HELP;
				break;
			}
		} else if (gt == GameType.HELP) {
			switch (key) {
			case KeyEvent.VK_H:
				gt = GameType.INTRO;
				break;
			}
		} else {
			switch (key) {
			case KeyEvent.VK_ESCAPE:
				doRun = false;
				break;
			default:
				// Normal play mode
				dataInterface.setData(new DataEvent(DataEventType.KEY_PRESS, this));
				switch (gt) {
				case SINGLEPLAYER:
					pacman.keyPressed(key);
					break;
				case COOPERATIVE:
					pacman.keyPressed(key);
					msPacman.keyPressed(key);
					break;
				case VERSUS:
					pacman.keyPressed(key);
					ghost1.keyPressed(key);
					ghost2.keyPressed(key);
					break;
				default:
					break;
				}
			}
		}
	}

	public void keyReleased(int key) {
		dataInterface.setData(new DataEvent(DataEventType.KEY_RELEASE, this));
		switch (gt) {
		case SINGLEPLAYER:
			pacman.keyReleased(key);
			break;
		case COOPERATIVE:
			pacman.keyReleased(key);
			msPacman.keyReleased(key);
			break;
		case VERSUS:
			pacman.keyReleased(key);
			ghost1.keyReleased(key);
			ghost2.keyReleased(key);
			break;
		default:
			break;
		}
	}

	@Override
	public void run() {
		try {
			doRun = true;
			Thread.sleep(1000);
			while (doRun) {
				gameStep++;
				Thread.sleep(loopDelay);
				if (gt == GameType.SINGLEPLAYER || gt == GameType.VERSUS || gt == GameType.COOPERATIVE) {
					dataInterface.setData(new DataEvent(DataEventType.MOVE, this));
					this.playGame();
				}
			}
		} catch (InterruptedException e) {
		}
	}

	/*
	 * void openOutputs() { try { characterStateOut = new PrintStream(new
	 * FileOutputStream("character.dat"));
	 * Character.writeHeader(characterStateOut); eventOut = new PrintStream(new
	 * FileOutputStream("event.dat")); writeEventHeader(); gridOut = new
	 * PrintStream(new FileOutputStream("grid.dat")); } catch
	 * (FileNotFoundException e1) { e1.printStackTrace(); System.exit(1); } }
	 */
	/*
	 * void writeState() { if (characterStateOut != null) {
	 * pacman.writeState(characterStateOut, gameID, gameStep, score, "P" + 1,
	 * false); int ghostID = 1; for (Ghost ghost : ghosts) {
	 * ghost.writeState(characterStateOut, gameID, gameStep, score, "G" +
	 * ghostID, ghost.edible); ghostID++; } } }
	 */
	/*
	 * public void writeEventHeader() { eventOut.println(
	 * "GameID, GameStep, EventType"); }
	 */
	/*
	 * void writeEventfoo(String evenType) {
	 * 
	 * eventOut.println(gameID + "," + gameStep + "," + evenType); }
	 */
	@Override
	public long getGameID() {
		return gameID;

	}

	@Override
	public int getGameStep() {
		return gameStep;
	}
}
