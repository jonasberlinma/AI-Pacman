package edu.ucsb.cs56.projects.games.pacman;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Class representing the map layout
 *
 * @author Yuxiang Zhu
 * @author Joseph Kompella
 * @author Kekoa Sato
 * @version CS56 F16
 */

public class Grid implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int fruitCounter = 0;
	private int x;
	private int y;

	/*
	 * check this link to implement the ghost AI movement at intersection. Revise
	 * the level 1 data to classic pacman for intersection detection
	 * http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-
	 * behavior
	 */

	private short[][] screenData;
	private short[][][] levelsData;
	private GridWalker[] gridWalkers = null;

	private int currentLevel;

	class Pair {
		int from;
		int to;
	}

	/**
	 * Constructor for Board object
	 */
	public Grid() {

	}
	public void load() {
		screenData = new short[Board.getNumblocksStatic()][Board.getNumblocksStatic()];

		String[] loadableLevels = { "level1.data", "level2.data", "level3.data", "level4.data", "level5.data" };
		this.levelsData = new short[loadableLevels.length][1][1];
		gridWalkers = new GridWalker[loadableLevels.length];
		for (int i = 0; i < loadableLevels.length; i++) {
			GridData level = loadLevel("/assets/levels/" + loadableLevels[i]);
			gridWalkers[i] = new GridWalker(level, getScreenData());
			levelsData[i] = level.get2DGridData();
		}
	}

	public GridData loadLevel(String asset_path) {
		try {
			InputStream input_stream = getClass().getResourceAsStream(asset_path);
			// System.out.println(input_stream);
			ObjectInputStream object_input_stream = new ObjectInputStream(input_stream);
			GridData data = (GridData) object_input_stream.readObject();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			System.out.println("Failed to load level data assets: " + asset_path);
			System.exit(2);
		}
		return null;
	}

	/**
	 * Checks if there are any pellets left for Pacman to eat, and restarts the game
	 * on the next board in a higher difficulty if finished
	 *
	 * @return A boolean indicating whether or not the maze is finished
	 */
	public boolean checkMaze() {
		for (int i = 0; i < Board.getNumblocksStatic(); i++) {
			for (int j = 0; j < Board.getNumblocksStatic(); j++) {
				if ((getScreenData()[i][j] & (GridData.GRID_CELL_PELLET ^ GridData.GRID_CELL_POWER_PILL)) != 0)
					return false;
			}
		}
		return true;
	}

	/**
	 * Count the number of pellets left for Pacman to eat
	 *
	 * @return An int indicating how many pellets are left
	 */
	public int getPelletNum() {
		int numOfPellet = 0;
		for (int i = 0; i < Board.getNumblocksStatic(); i++) {
			for (int j = 0; j < Board.getNumblocksStatic(); j++) {
				if ((getScreenData()[i][j] & GridData.GRID_CELL_PELLET) != 0)
					numOfPellet++;
			}
		}
		return numOfPellet;
	}

	/**
	 * Count the number of power pills left for Pacman to eat
	 *
	 * @return An int indicating how many pills are left
	 */
	public int getPillNum() {
		int numOfPill = 0;
		for (int i = 0; i < Board.getNumblocksStatic(); i++) {
			for (int j = 0; j < Board.getNumblocksStatic(); j++) {
				if ((getScreenData()[i][j] & GridData.GRID_CELL_POWER_PILL) != 0)
					numOfPill++;
			}
		}
		return numOfPill;
	}

	/**
	 * Count the number of pellets in each map
	 *
	 * @param numBoardsCleared the number of levels that have been cleared
	 * @return An int indicating how many pellets are left
	 */
	public int getPelletNumForMap(int numBoardsCleared) {
		int numOfPellet = 0;
		for (int i = 0; i < Board.getNumblocksStatic(); i++) {
			for (int j = 0; j < Board.getNumblocksStatic(); j++) {
				if ((this.levelsData[numBoardsCleared % this.levelsData.length][i][j]
						& GridData.GRID_CELL_PELLET) != 0) {
					numOfPellet++;
				}
			}
		}
		return numOfPellet;
	}

	/**
	 * Initialize level
	 * 
	 * @param numBoardsCleared the number of levels that have been cleared
	 */
	public void levelInit(int numBoardsCleared) {
		this.currentLevel = numBoardsCleared;
		for (int i = 0; i < Board.getNumblocksStatic(); i++) {
			getScreenData()[i] = Arrays.copyOf(this.levelsData[numBoardsCleared % this.levelsData.length][i],
					Board.getNumblocksStatic());
		}
	}

	/*
	 * Better Implementation Idea You could have an ArrayList that holds Point
	 * Objects of locations that the Pacman has eaten pellets. - Create
	 * ArrayList<Point> in Grid.java & initialize - Add new Point every time Pacman
	 * eats pellet/fruit in PacPlayer.java
	 * 
	 * When it is time to spawn a fruit, choose a random Point from this ArrayList
	 * (int) (Math.random() * list.size()), then remove the point where you spawned
	 * the fruit.
	 * 
	 * When a level is complete, make sure you clear this ArrayList using
	 * list.clear()
	 ** 
	 * If you want to fix this too, you can try: To prevent the fruit from spawning
	 * on Pacman, just cross-check that the spawning location != any of the Pacman
	 * location (use a loop on pacmen array for this so you handle multi-player
	 * mode)
	 * 
	 * I think without having a defined search space, and just searching randomly,
	 * you cannot easily fix the bug where the program is looking forever to find an
	 * open space. That's why having this list of possible spaces is the best way I
	 * can think of.
	 */

	public void randomBlock() {
		this.x = (int) (Math.random() * Board.getNumblocksStatic());
		this.y = (int) (Math.random() * Board.getNumblocksStatic());
	}

	/**
	 * Increment fruit as the pacman is alive
	 *
	 * @param numBoardsCleared number of levels cleared
	 */
	public void incrementFruit(int numBoardsCleared) {
		if (this.getPelletNum() < this.getPelletNumForMap(numBoardsCleared)) {
			if (fruitCounter > 100) {
				fruitCounter = 0;
				this.randomBlock();
				while (true) {
					if (((getScreenData()[this.x][this.y] & GridData.GRID_CELL_PELLET) == 0)
							&& (this.levelsData[numBoardsCleared % this.levelsData.length][this.x][this.y]
									& GridData.GRID_CELL_PELLET) != 0) {
						getScreenData()[this.x][this.y] = (short) (getScreenData()[this.x][this.y] | GridData.GRID_CELL_FRUIT);
						break;
					}
					this.randomBlock();
				}
			} else {
				fruitCounter++;
			}
		} else {
			return;
		}
	}

	public void writeGrid(PrintStream gridOut) {
		for (int i = 0; i < Board.getNumblocksStatic(); i++) {
			for (int j = 0; j < Board.getNumblocksStatic(); j++) {
				gridOut.print("" + getScreenData()[i][j]);
				if (j < Board.getNumblocksStatic() - 1) {
					gridOut.print(",");
				}
			}
			gridOut.println("");
		}
	}

	public GridWalker getGridWalker() {
		return gridWalkers[currentLevel];
	}

	public short[][] getScreenData() {
		return screenData;
	}
}
