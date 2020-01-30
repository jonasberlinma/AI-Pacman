package edu.ucsb.cs56.projects.games.pacman;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Vector;

import edu.ucsb.cs56.projects.games.pacman.Character.PlayerType;
import edu.ucsb.cs56.projects.games.pacman.DataEvent.DataEventType;
import edu.ucsb.cs56.projects.games.pacman.GridWalker.Direction;
import edu.ucsb.cs56.projects.games.pacman.GridWalker.DirectionDistance;
import edu.ucsb.cs56.projects.games.pacman.GridWalker.Path;
import edu.ucsb.cs56.projects.games.pacman.ui.BoardRenderer;

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

	private static final int BLOCKSIZE = 24;
	private static final int NUMBLOCKS = 17;
	private static final int SCRSIZE = getNumblocks() * getBlocksize();

	private final int MAX_GHOSTS = 12;
	private final int MAX_SPEED = 6;

	private long gameID = 0;
	private int gameStep = 0;

	private int score;

	private Grid grid;
	private GameType gt = GameType.INTRO;
	private PacPlayer pacman;
	private PacPlayer msPacman;
	Ghost ghost1, ghost2;
	Vector<Character> pacmen;
	private Vector<Ghost> ghosts;
	private int startNumGhosts = 0;
	private int numGhosts = 6;
	int numBoardsCleared = 0;
	private int curSpeed = 3;
	private int numPellet;
	private int numPills;
	private int loopDelay;

	public BoardRenderer boardRenderer = null;
	private DataInterface dataInterface;
	private Thread boardThread;

	private boolean doRun = false;
	private int audioClipID = -1;

	/**
	 * Constructor for Board object
	 * 
	 * @throws FileNotFoundException
	 */
	public Board(Properties prop, boolean doWrite) throws FileNotFoundException {

		dataInterface = new DataInterface(doWrite);
		startNumGhosts = Integer.parseInt(prop.getProperty("numGhosts", "6"));
		numGhosts = startNumGhosts;

		grid = new Grid();

		gt = GameType.INTRO;
		grid.levelInit(0);

		// grid.writeGrid(gridOut);

		pacmen = new Vector<Character>();

		setPacman(new PacPlayer(dataInterface, 8 * getBlocksize(), 11 * getBlocksize(), PlayerType.PACMAN, grid));
		// msPacman = new PacPlayer(dataInterface, 7 * BLOCKSIZE, 11 *
		// BLOCKSIZE, PacPlayer.MSPACMAN, grid);
		ghost1 = new Ghost(0, dataInterface, 8 * getBlocksize(), 7 * getBlocksize(), 3, PlayerType.GHOST1, grid);
		ghost2 = new Ghost(1, dataInterface, 9 * getBlocksize(), 7 * getBlocksize(), 3, PlayerType.GHOST2, grid);

		setGhosts(new Vector<Ghost>());
		numPills = 4;

		dataInterface.setData(new DataEvent(DataEventType.INTRO, this, this));

		boardThread = new Thread(this, "Game Board");

	}

	/**
	 * Start the board
	 */
	public void start() {
		boardThread.start();
	}

	public void stop() {
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
	 * @param g2d a Graphics 2D object
	 */
	private void playGame() {
		if (gt == GameType.SINGLEPLAYER || gt == GameType.VERSUS || gt == GameType.COOPERATIVE) {
			if (!checkAlive()) {
				gameOver();
			} else {
				if (getPacman().alive) {
					getPacman().move(grid, this);
					DataEvent de = new DataEvent(DataEventType.MOVE, this, getPacman());
					ArrayList<Direction> dirs = grid.getGridWalker().getPossibleDirections(getPacman().x / getBlocksize(),
							getPacman().y / getBlocksize());
					Path pelletPath = grid.getGridWalker().getClosestPelletPath(getPacman().x / getBlocksize(),
							getPacman().y / getBlocksize());
					if (pelletPath != null) {
						de.setKeyValuePair("pelletDirection", "" + pelletPath.getFirstDirection());
						de.setKeyValuePair("pelletDistance" , "" + pelletPath.getDistance());
					}
					Path fruitPath = grid.getGridWalker().getClosestFruitPath(getPacman().x/getBlocksize(), getPacman().y/getBlocksize());
					// Fruit can be scarce
					if(fruitPath != null) {
						de.setKeyValuePair("fruitDirection", "" + fruitPath.getFirstDirection());
						de.setKeyValuePair("fruitDistance", "" + fruitPath.getDistance());
					} else {
						// Just say it is far away
						de.setKeyValuePair("fruitDirection", "" + 1);
						de.setKeyValuePair("fruitDistance", "" + 99);
					}
					de.setKeyValuePair("UP", dirs.contains(Direction.UP) ? "True" : "False");
					de.setKeyValuePair("DOWN", dirs.contains(Direction.DOWN) ? "True" : "False");
					de.setKeyValuePair("LEFT", dirs.contains(Direction.LEFT) ? "True" : "False");
					de.setKeyValuePair("RIGHT", dirs.contains(Direction.RIGHT) ? "True" : "False");
					de.setKeyValuePair("score", Integer.valueOf(score).toString());

					dataInterface.setData(de);
					if (grid.getPillNum() != numPills) {
						for (Ghost g : getGhosts()) {
							g.edible = true;
							g.edibleTimer = 200;
						}
						numPills = grid.getPillNum();
					}
				}
			}
			switch (gt) {
			case SINGLEPLAYER:
				for (Ghost g : getGhosts()) {
					g.moveAI(grid, pacmen);
					DirectionDistance dd = grid.getGridWalker().getShortestPathDirectionDistance(getPacman().x / getBlocksize(),
							getPacman().y / getBlocksize(), g.x / getBlocksize(), g.y / getBlocksize());
					DataEvent de = new DataEvent(DataEventType.MOVE, this, g);
					de.setKeyValuePair("ghostNum", Integer.valueOf(g.ghostNum).toString());
					if (dd != null) {
						String distanceString = Integer.valueOf(dd.distance).toString();
						de.setKeyValuePair("distance", distanceString);
						de.setKeyValuePair("direction", dd.direction.toString());
					}
					dataInterface.setData(de);
				}
				grid.incrementFruit(numBoardsCleared);
				detectCollision(getGhosts());
				break;
			case COOPERATIVE:
				if (getMsPacman().alive) {
					getMsPacman().move(grid, this);
					dataInterface.setData(new DataEvent(DataEventType.MOVE, this, getMsPacman()));
				}
				for (Ghost g : getGhosts()) {
					g.moveAI(grid, pacmen);
					dataInterface.setData(new DataEvent(DataEventType.MOVE, this, g));
				}
				grid.incrementFruit(numBoardsCleared);
				detectCollision(getGhosts());
				break;
			case VERSUS:
				for (Ghost g : getGhosts()) {
					g.move(grid, this);
					dataInterface.setData(new DataEvent(DataEventType.MOVE, this, g));
				}

				if (score >= getNumPellet()) {
					score = 0;
					numBoardsCleared++;
					grid.levelInit(numBoardsCleared);
					levelContinue();

				}
				grid.incrementFruit(numBoardsCleared);
				detectCollision(getGhosts());
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
		DataEvent de = new DataEvent(DataEventType.GAME_OVER, this, this);
		de.setKeyValuePair("score", "" + score);
		dataInterface.setData(de);
		if (boardRenderer != null)
			boardRenderer.drawGameOver();

		gt = GameType.INTRO;

		numBoardsCleared = 0;
		grid.levelInit(0);
	}

	/**
	 * Detects when ghosts and pacman collide
	 *
	 * @param ghosts An array of Ghost
	 */
	public void detectCollision(Vector<Ghost> ghosts) {
		for (Character pacman : pacmen) {
			for (Ghost ghost : ghosts) {
				if ((Math.abs(pacman.x - ghost.x) < 20 && Math.abs(pacman.y - ghost.y) < 20) && ghost.edible == false) {
					if (pacman.deathTimer <= 0) {
						score -= 100;
						dataInterface.setData(new DataEvent(DataEventType.PACMAN_DEATH, this, this));
					}
					;
					pacman.death();
				}

				if ((Math.abs(pacman.x - ghost.x) < 20 && Math.abs(pacman.y - ghost.y) < 20) && ghost.edible == true) {
					ghost.death();
					score += 40;
					DataEvent dataEvent = new DataEvent(DataEventType.EAT_GHOST, this, this);
					dataEvent.setKeyValuePair("score", "" + score);
					dataInterface.setData(dataEvent);

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
		return pacmen.stream().anyMatch(x -> x.alive);
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

		numGhosts = startNumGhosts;
		curSpeed = 3;
		numPills = 4;

		switch (gt) {
		case SINGLEPLAYER:
			pacmen.add(getPacman());
			getPacman().reset();
			break;
		case COOPERATIVE:
			pacmen.add(getPacman());
			pacmen.add(getMsPacman());
			getPacman().reset();
			getMsPacman().reset();
			break;
		case VERSUS:
			pacmen.add(getPacman());
			getPacman().reset();
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
		setNumPellet(grid.getPelletNum() + grid.getPillNum());
		numPills = grid.getPillNum();
		getGhosts().clear();
		if (gt == GameType.VERSUS) {
			getGhosts().add(ghost1);
			getGhosts().add(ghost2);
		} else {
			for (int i = 0; i < numGhosts; i++) {
				int random = (int) (Math.random() * curSpeed) + 1;
				int the_type = i % 2;
				switch (the_type) {
				case 0:
					getGhosts().add(
							new Ghost(i, dataInterface, (i + 6) * getBlocksize(), 2 * getBlocksize(), random, PlayerType.GHOST1));
					break;
				case 1:
					getGhosts().add(
							new Ghost(i, dataInterface, (i + 6) * getBlocksize(), 2 * getBlocksize(), random, PlayerType.GHOST2));
				}

			}
		}
		switch (gt) {
		case SINGLEPLAYER:
			getPacman().resetPos();
			break;
		case COOPERATIVE:
			getPacman().resetPos();
			getMsPacman().resetPos();
			break;
		case VERSUS:
			getPacman().resetPos();
			for (Character ghost : getGhosts()) {
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
				DataEvent dataEvent = new DataEvent(DataEventType.KEY_PRESS, this, this);
				dataEvent.setKeyValuePair("key", KeyEvent.getKeyText(key));
				dataInterface.setData(dataEvent);
				switch (gt) {
				case SINGLEPLAYER:
					getPacman().keyPressed(key);
					break;
				case COOPERATIVE:
					getPacman().keyPressed(key);
					getMsPacman().keyPressed(key);
					break;
				case VERSUS:
					getPacman().keyPressed(key);
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
		DataEvent dataEvent = new DataEvent(DataEventType.KEY_RELEASE, this, this);
		dataEvent.setKeyValuePair("key", KeyEvent.getKeyText(key));
		dataInterface.setData(dataEvent);
		switch (gt) {
		case SINGLEPLAYER:
			getPacman().keyReleased(key);
			break;
		case COOPERATIVE:
			getPacman().keyReleased(key);
			getMsPacman().keyReleased(key);
			break;
		case VERSUS:
			getPacman().keyReleased(key);
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
			// Required due to heisenbug. Somehow the game type changes to
			// single, versus, or cooperative before the characters are
			// fully initialized
			Thread.sleep(1000);
			while (doRun) {
				gameStep++;
				Thread.sleep(loopDelay);
				if (gt == GameType.SINGLEPLAYER || gt == GameType.VERSUS || gt == GameType.COOPERATIVE) {

					this.playGame();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public long getGameID() {
		return gameID;

	}

	@Override
	public int getGameStep() {
		return gameStep;
	}

	/**
	 * Called by Board to signal that a sound should be played if there is a
	 * renderer
	 * 
	 * @param audioClipID
	 */
	public void playAudio(int audioClipID) {
		this.audioClipID = audioClipID;
	}

	/**
	 * Called by renderer to figure if there is a game sound to play
	 * 
	 * @return
	 */
	public boolean doPlayAudio() {
		return audioClipID != -1;
	}

	/**
	 * Called by renderer to figure out what game sound to play
	 * 
	 * @return
	 */
	public int getAudioClipID() {
		int clipID = this.audioClipID;
		this.audioClipID = -1;
		return clipID;
	}

	@Override
	public LinkedHashMap<String, String> getData(DataEvent.DataEventType dataEvent) {
		LinkedHashMap<String, String> hashtable = new LinkedHashMap<String, String>();

		switch (dataEvent) {
		case MOVE:
			break;
		default:
		}
		return hashtable;

	}

	public PacPlayer getMsPacman() {
		return msPacman;
	}

	public void setMsPacman(PacPlayer msPacman) {
		this.msPacman = msPacman;
	}

	public PacPlayer getPacman() {
		return pacman;
	}

	public void setPacman(PacPlayer pacman) {
		this.pacman = pacman;
	}

	public Vector<Ghost> getGhosts() {
		return ghosts;
	}

	public void setGhosts(Vector<Ghost> ghosts) {
		this.ghosts = ghosts;
	}

	public int getNumPellet() {
		return numPellet;
	}

	public void setNumPellet(int numPellet) {
		this.numPellet = numPellet;
	}

	public static int getBlocksize() {
		return BLOCKSIZE;
	}

	public static int getNumblocks() {
		return NUMBLOCKS;
	}

	public static int getScrsize() {
		return SCRSIZE;
	}
}
