package edu.ucsb.cs56.projects.games.pacman;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
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

	public static final int BLOCKSIZE = 24;
	public static final int NUMBLOCKS = 17;
	public static final int SCRSIZE = NUMBLOCKS * BLOCKSIZE;

	private final int MAX_GHOSTS = 12;
	private final int MAX_SPEED = 6;

	private long gameID = 0;
	private int gameStep = 0;
	private int nTrainedModels;

	private int score;

	private Grid grid;
	private GameType gt = GameType.INTRO;
	PacPlayer pacman;
	PacPlayer msPacman;
	Ghost ghost1, ghost2;
	Vector<Character> pacmen;
	Vector<Ghost> ghosts;
	private int startNumGhosts = 0;
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

		pacman = new PacPlayer(dataInterface, 8 * BLOCKSIZE, 11 * BLOCKSIZE, PlayerType.PACMAN, grid);
		// msPacman = new PacPlayer(dataInterface, 7 * BLOCKSIZE, 11 *
		// BLOCKSIZE, PacPlayer.MSPACMAN, grid);
		ghost1 = new Ghost(0, dataInterface, 8 * BLOCKSIZE, 7 * BLOCKSIZE, 3, PlayerType.GHOST1, grid);
		ghost2 = new Ghost(1, dataInterface, 9 * BLOCKSIZE, 7 * BLOCKSIZE, 3, PlayerType.GHOST2, grid);

		ghosts = new Vector<Ghost>();
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
				if (pacman.alive) {
					pacman.move(grid, this);
					DataEvent de = new DataEvent(DataEventType.MOVE, this, pacman);
					Vector<Direction> dirs = grid.getGridWalker().getPossibleDirections(pacman.x / BLOCKSIZE,
							pacman.y / BLOCKSIZE);
					de.setKeyValuePair("UP", dirs.contains(Direction.UP)?"True":"False");
					de.setKeyValuePair("DOWN", dirs.contains(Direction.DOWN)?"True":"False");
					de.setKeyValuePair("LEFT", dirs.contains(Direction.LEFT)?"True":"False");
					de.setKeyValuePair("RIGHT", dirs.contains(Direction.RIGHT)?"True":"False");
					de.setKeyValuePair("score", Integer.valueOf(score).toString());
					
					dataInterface.setData(de);
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
					DirectionDistance dd = grid.getGridWalker().getShortestPathDirectionDistance(pacman.x / BLOCKSIZE,
							pacman.y / BLOCKSIZE, g.x / BLOCKSIZE, g.y / BLOCKSIZE);
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
				detectCollision(ghosts);
				break;
			case COOPERATIVE:
				if (msPacman.alive) {
					msPacman.move(grid, this);
					dataInterface.setData(new DataEvent(DataEventType.MOVE, this, msPacman));
				}
				for (Ghost g : ghosts) {
					g.moveAI(grid, pacmen);
					dataInterface.setData(new DataEvent(DataEventType.MOVE, this, g));
				}
				grid.incrementFruit(numBoardsCleared);
				detectCollision(ghosts);
				break;
			case VERSUS:
				for (Ghost g : ghosts) {
					g.move(grid, this);
					dataInterface.setData(new DataEvent(DataEventType.MOVE, this, g));
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
			pacmen.add(pacman);
			pacman.reset();
			break;
		case COOPERATIVE:
			pacmen.add(pacman);
			pacmen.add(msPacman);
			pacman.reset();
			msPacman.reset();
			break;
		case VERSUS:
			pacmen.add(pacman);
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
				int the_type = i % 2;
				switch (the_type) {
				case 0:
					ghosts.add(
							new Ghost(i, dataInterface, (i + 6) * BLOCKSIZE, 2 * BLOCKSIZE, random, PlayerType.GHOST1));
					break;
				case 1:
					ghosts.add(
							new Ghost(i, dataInterface, (i + 6) * BLOCKSIZE, 2 * BLOCKSIZE, random, PlayerType.GHOST2));
				}

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
				DataEvent dataEvent = new DataEvent(DataEventType.KEY_PRESS, this, this);
				dataEvent.setKeyValuePair("key", KeyEvent.getKeyText(key));
				dataInterface.setData(dataEvent);
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
		DataEvent dataEvent = new DataEvent(DataEventType.KEY_RELEASE, this, this);
		dataEvent.setKeyValuePair("key", KeyEvent.getKeyText(key));
		dataInterface.setData(dataEvent);
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
			// Required due to heisenbug. Somehow the game type changes to
			// single, versus, or cooperative before the characters are
			// fully initialized
			System.out.println("Initial game type " + gt);
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

	public int getNTrainedModels() {
		return this.nTrainedModels;
	}

	public void setNTrainedModels(int nTrainedModels) {
		this.nTrainedModels = nTrainedModels;
	}
}
