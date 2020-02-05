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
	// These constants exist in both Board and BoardRenderer
	public static final int BLOCKSIZE = 24; 
	public static final int NUMBLOCKS = 17;

	private final int MAX_GHOSTS = 12;
	private final int MAX_SPEED = 6;

	private long gameID = 0;
	private int gameStep = 0;

	private int score;

	private Grid grid;
	private GameType gt = GameType.INTRO;
	private PacPlayer pacman;
	private PacPlayer msPacman;
	private Ghost ghost1, ghost2;
	private Vector<Character> pacmen;
	private Vector<Ghost> ghosts;
	private int startNumGhosts = 0;
	private int numGhosts = 6;
	private int numBoardsCleared = 0;
	private int curSpeed = 3;
	private int numPellet;
	private int numPills;
	private int loopDelay;

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
		grid.load();

		gt = GameType.INTRO;
		grid.levelInit(0);

		// grid.writeGrid(gridOut);

		pacmen = new Vector<Character>();

		setPacman(new PacPlayer(dataInterface, 8 * BLOCKSIZE, 11 * BLOCKSIZE, PlayerType.PACMAN, grid));
		// msPacman = new PacPlayer(dataInterface, 7 * BLOCKSIZE, 11 *
		// BLOCKSIZE, PacPlayer.MSPACMAN, grid);
		ghost1 = new Ghost(0, dataInterface, 8 * BLOCKSIZE, 7 * BLOCKSIZE, 3, PlayerType.GHOST1, grid);
		ghost2 = new Ghost(1, dataInterface, 9 * BLOCKSIZE, 7 * BLOCKSIZE, 3, PlayerType.GHOST2, grid);

		setGhosts(new Vector<Ghost>());
		numPills = 4;

		dataInterface.setData(new DataEvent(DataEventType.INTRO, this, this));

		boardThread = new Thread(this, "Game Board");

	}

	/**
	 * Start the board
	 */

	protected void start() {
		boardThread.start();
	}

	protected void stop() {
		doRun = false;
	}

	/**
	 * Wait for the board thread to exit an join it. Called by IAGame
	 * 
	 * @throws InterruptedException
	 */
	protected void join() throws InterruptedException {
		boardThread.join();
	}

	protected void setLoopDelay(int loopDelay) {
		this.loopDelay = loopDelay;
	}

	protected DataInterface getDataInterface() {
		return dataInterface;
	}

	protected void addScore(int addScore) {
		score = score + addScore;
	}

	/**
	 * Main game logic loop
	 *
	 * @param g2d a Graphics 2D object
	 */
	private void playGame() {
		doAnimPacman(pacman);
		if (gt == GameType.SINGLEPLAYER || gt == GameType.VERSUS || gt == GameType.COOPERATIVE) {
			if (!checkAlive()) {
				gameOver();
			} else {
				if (getPacman().alive) {
					getPacman().move(grid, this);
					DataEvent de = new DataEvent(DataEventType.MOVE, this, getPacman());
					ArrayList<Direction> dirs = grid.getGridWalker().getPossibleDirections(getPacman().x / BLOCKSIZE,
							getPacman().y / BLOCKSIZE);
					Path pelletPath = grid.getGridWalker().getClosestPelletPath(getPacman().x / BLOCKSIZE,
							getPacman().y / BLOCKSIZE);
					if (pelletPath != null) {
						de.setKeyValuePair("pelletDirection", "" + pelletPath.getFirstDirection());
						de.setKeyValuePair("pelletDistance", "" + pelletPath.getDistance());
					}
					Path fruitPath = grid.getGridWalker().getClosestFruitPath(getPacman().x / BLOCKSIZE,
							getPacman().y / BLOCKSIZE);
					// Fruit can be scarce
					if (fruitPath != null) {
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
					DirectionDistance dd = grid.getGridWalker().getShortestPathDirectionDistance(
							getPacman().x / BLOCKSIZE, getPacman().y / BLOCKSIZE, g.x / BLOCKSIZE, g.y / BLOCKSIZE);
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
	 * Animates the Pacman sprite's direction as well as mouth opening and closing
	 */
	private void doAnimPacman(PacPlayer pacman) {
		pacman.setPacanimcount(pacman.getPacanimcount() - 1);
		if (pacman.getPacanimcount() <= 0) {
			pacman.setPacanimcount(pacman.getPacanimdelay());
			pacman.setPacmananimpos(pacman.getPacmananimpos() + pacman.getPacanimdir());
			if (pacman.getPacmananimpos() == (pacman.getPacanimcount() - 1) || pacman.getPacmananimpos() == 0)
				pacman.setPacanimdir(-pacman.getPacanimdir());
		}
	}

	/**
	 * End the game if remaining lives reaches 0.
	 */
	private void gameOver() {
		System.out.println("GameOver called in Board");
		gt = GameType.GAME_OVER;
		DataEvent de = new DataEvent(DataEventType.GAME_OVER, this, this);
		de.setKeyValuePair("score", "" + score);
		dataInterface.setData(de);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//numBoardsCleared = 0;
		//grid.levelInit(0);
	}

	/**
	 * Detects when ghosts and pacman collide
	 *
	 * @param ghosts An array of Ghost
	 */
	private void detectCollision(Vector<Ghost> ghosts) {
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
	private boolean checkAlive() {
		boolean isAlive = pacmen.stream().anyMatch(x -> x.alive);
		return isAlive;
	}

	private void resetGame() {
		gt = GameType.INTRO;
		numBoardsCleared = 0;
		grid.levelInit(0);
	}

	/**
	 * Initialize game variables
	 */
	private void gameInit() {
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
	private void levelContinue() {
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
							new Ghost(i, dataInterface, (i + 6) * BLOCKSIZE, 2 * BLOCKSIZE, random, PlayerType.GHOST1));
					break;
				case 1:
					getGhosts().add(
							new Ghost(i, dataInterface, (i + 6) * BLOCKSIZE, 2 * BLOCKSIZE, random, PlayerType.GHOST2));
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
				resetGame();
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
	protected void playAudio(int audioClipID) {
		this.audioClipID = audioClipID;
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

		return hashtable;
	}

	public PacPlayer getMsPacman() {
		return msPacman;
	}

	protected void setMsPacman(PacPlayer msPacman) {
		this.msPacman = msPacman;
	}

	public PacPlayer getPacman() {
		return pacman;
	}

	protected void setPacman(PacPlayer pacman) {
		this.pacman = pacman;
	}

	public Vector<Ghost> getGhosts() {
		return ghosts;
	}

	protected void setGhosts(Vector<Ghost> ghosts) {
		this.ghosts = ghosts;
	}

	public int getNumPellet() {
		return numPellet;
	}

	private void setNumPellet(int numPellet) {
		this.numPellet = numPellet;
	}

	public GameType getGameType() {
		return gt;
	}

	public Grid getGrid() {
		return grid;
	}

	public int getScore() {
		return score;
	}
	public Path getShortestPath(int x1, int y1, int x2, int y2) {
		Path path = getGrid().getGridWalker().getShortestPath(x1,  y1,  x2,  y2);
		return path;
	}
}
