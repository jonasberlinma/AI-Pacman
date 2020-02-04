package edu.ucsb.cs56.projects.games.pacman.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import edu.ucsb.cs56.projects.games.pacman.GameInterface;
import edu.ucsb.cs56.projects.games.pacman.Character.PlayerType;
import edu.ucsb.cs56.projects.games.pacman.GameType;
import edu.ucsb.cs56.projects.games.pacman.Ghost;
import edu.ucsb.cs56.projects.games.pacman.Grid;
import edu.ucsb.cs56.projects.games.pacman.GridData;
import edu.ucsb.cs56.projects.games.pacman.PacPlayer;

public class BoardRenderer extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7383414799194976684L;

	// These constants exist in both Board and BoardRenderer
	private static final int BLOCKSIZE = 24;
	private static final int NUMBLOCKS = 17;
	private static final int SCRSIZE = BLOCKSIZE * NUMBLOCKS;

	private Color mazeColor = new Color(5, 100, 5);
	private Color dotColor = new Color(192, 192, 0);
	private Color fruitColor = new Color(255, 0, 0);

	private static String[] files = { "pacmanleaderboardsingle.ser", "pacmanleaderboardcoop.ser",
			"pacmanleaderboardversus.ser" };

	private GameInterface board = null;
	private BoardFrame bf = null;

	private Font smallFont = new Font("Helvetica", Font.BOLD, 14);

	private ScoreLoader sl = new ScoreLoader("highScores.txt");
	private LeaderboardGUI leaderBoardGui = new LeaderboardGUI();

	private Timer timer = null;
	private boolean introAudioPlayed = false;

	public void stop() {
		bf.dispose();
		System.out.println("Stop");
		timer.stop();
	}

	public BoardRenderer(GameInterface board) {
		this.board = board;

	}

	protected void createUI() {
		addKeyListener(new TAdapter());
		setFocusable(true);
		setBackground(Color.black);
		setDoubleBuffered(false);
		AssetController.getInstance();

		leaderBoardGui.setLeaderBoardFileName(files);

		bf = new BoardFrame();
		bf.add(this);
		bf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void start() {
		System.out.println("Start");
		timer = new Timer(100, this);
		timer.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.validate();
		this.repaint();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;

		drawMaze(g2d);
		GameType gt = board.getGameType();
		drawScore(g, board);
		switch (gt) {
		case INTRO:
			showIntroScreen(g);
			if (!introAudioPlayed) {
				AssetController.getInstance().playIntroAudio();
				introAudioPlayed = true;
			}
			break;
		case HELP:
			showHelpScreen(g);
			break;
		case GAME_OVER:
			drawGameOver();
			break;
		default:
			introAudioPlayed = false;
			drawPacman(g2d, this, board.getPacman());

			if (gt == GameType.COOPERATIVE)
				drawPacman(g2d, this, board.getMsPacman());
			for (Ghost ghost : board.getGhosts()) {
				drawGhost(g2d, this, ghost);
			}
		}

		if (!timer.isRunning())
			showPauseScreen(g);

		if (board.doPlayAudio()) {
			AssetController.getInstance().playAudio(board.getAudioClipID());
		}

		Toolkit.getDefaultToolkit().sync();
		g.dispose();
	}

	/**
	 * Draw a message box with the text "Press s to start." in the center of the
	 * screen
	 *
	 * @param g a Graphics object
	 */
	private void showIntroScreen(Graphics g) {
		g.setColor(new Color(0, 32, 48));
		g.fillRect(50, SCRSIZE / 2 - 50, SCRSIZE - 100, 90);
		g.setColor(Color.white);
		g.drawRect(50, SCRSIZE / 2 - 50, SCRSIZE - 100, 90);

		String s = "Press s for single player";
		String d = "Press d for Co-Op";
		String f = "Press f for Versus";
		String h = "Press h for help";
		Font small = new Font("Helvetica", Font.BOLD, 14);
		FontMetrics metr = getFontMetrics(small);

		g.setColor(Color.white);
		g.setFont(small);
		g.drawString(s, (SCRSIZE - metr.stringWidth(s)) / 2, SCRSIZE / 2 - metr.getHeight() * 3 / 2);
		g.drawString(d, (SCRSIZE - metr.stringWidth(d)) / 2, SCRSIZE / 2 - metr.getHeight() / 2);
		g.drawString(f, (SCRSIZE - metr.stringWidth(f)) / 2, SCRSIZE / 2 + metr.getHeight() / 2);
		g.drawString(h, (SCRSIZE - metr.stringWidth(h)) / 2, SCRSIZE / 2 + metr.getHeight() * 3 / 2);
		drawHighScores(g);
	}

	/**
	 * Displays a list of scores on the bottom of the screen
	 *
	 * @param g a Graphics object
	 */
	private void drawHighScores(Graphics g) {
		ArrayList<Integer> scores = sl.loadScores();
		g.setFont(smallFont);
		FontMetrics fm = getFontMetrics(smallFont);

		g.setColor(new Color(0, 32, 48));
		g.fillRect(SCRSIZE / 4, SCRSIZE - (SCRSIZE / 3) - fm.getAscent(), SCRSIZE / 2, BLOCKSIZE * 4);
		g.setColor(Color.white);
		g.drawRect(SCRSIZE / 4, SCRSIZE - (SCRSIZE / 3) - fm.getAscent(), SCRSIZE / 2, BLOCKSIZE * 4);

		g.setColor(new Color(96, 128, 255));
		for (int i = 0; i < scores.size(); i++) {
			if (i < 5)
				g.drawString((i + 1) + ": " + scores.get(i), SCRSIZE / 4 + BLOCKSIZE,
						SCRSIZE - (SCRSIZE / 3) + (i * fm.getHeight()));
			else if (i < 10)
				g.drawString((i + 1) + ": " + scores.get(i), SCRSIZE / 2 + BLOCKSIZE,
						SCRSIZE - (SCRSIZE / 3) + ((i - 5) * fm.getHeight()));
		}
	}

	/**
	 * Draw a message box telling the player the game is paused Also tells player to
	 * press 'p' to continue the game
	 *
	 * @param g a Graphics object
	 */
	private void showPauseScreen(Graphics g) {
		g.setColor(new Color(0, 32, 48));
		g.fillRect(50, SCRSIZE / 2 - 50, SCRSIZE - 100, 90);
		g.setColor(Color.white);
		g.drawRect(50, SCRSIZE / 2 - 50, SCRSIZE - 100, 90);

		String a = "Game Paused...";
		String b = "Press 'p' or 'Pause' to continue";
		Font big = new Font("Helvetica", Font.BOLD, 20);
		Font small = new Font("Helvetica", Font.BOLD, 12);
		FontMetrics metr1 = getFontMetrics(big);
		FontMetrics metr2 = getFontMetrics(small);

		g.setColor(Color.white);
		g.setFont(big);
		g.drawString(a, (SCRSIZE - metr1.stringWidth(a)) / 2, SCRSIZE / 2 - metr1.getHeight() / 2);
		g.setFont(small);
		g.drawString(b, (SCRSIZE - metr2.stringWidth(b)) / 2, SCRSIZE / 2 + metr2.getHeight() / 2);
	}

	/**
	 * Class that handles key presses for game controls
	 */
	private class TAdapter extends KeyAdapter {

		/**
		 * Detects when a key is pressed.
		 * <p>
		 * In-game: Changes Pacman's direction of movement with the arrow keys. Quit
		 * game by pressing the escape key.
		 * <p>
		 * Not in-game: Press the 'S' key to begin the game.
		 *
		 * @param e a KeyEvent
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			board.keyPressed(key);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			int key = e.getKeyCode();
			board.keyReleased(key);
		}
	}

	/**
	 * Shows help
	 *
	 * @param g a Graphics object
	 */
	private void showHelpScreen(Graphics g) {
		g.setColor(new Color(0, 32, 48));
		g.fillRect(10, 10, SCRSIZE - 15, SCRSIZE - 15);
		g.setColor(Color.white);
		g.drawRect(10, 10, SCRSIZE - 15, SCRSIZE - 15);

		int bx = 25, by = 60;

		String a = "Help";

		Font big = new Font("Helvetica", Font.BOLD, 18);
		Font medium = new Font("Helvetica", Font.BOLD, 14);
		Font small = new Font("Helvetica", Font.PLAIN, 12);
		FontMetrics metr1 = getFontMetrics(big);

		g.setColor(Color.white);
		g.setFont(big);
		g.drawString(a, (SCRSIZE - metr1.stringWidth(a)) / 2, 40);
		g.setFont(medium);
		g.drawString("Title Screen", bx, by);
		g.drawString("In Game", bx + 200, by);
		g.drawString("Controls", bx, by + 90);
		g.setFont(small);
		g.drawString("S - Start Single Player", bx + 10, by + 20);
		g.drawString("D - Start Co-op", bx + 10, by + 40);
		g.drawString("F - Start Versus", bx + 10, by + 60);
		g.drawString("Esc - Quit Game", bx + 210, by + 20);
		g.drawString("P - Pause Game", bx + 210, by + 40);

		g.drawString("Pacman:", bx + 10, by + 110);
		g.drawString("Up Arrow - Move Up", bx + 30, by + 130);
		g.drawString("Left Arrow - Move Left", bx + 30, by + 150);
		g.drawString("Down Arrow - Move Down", bx + 30, by + 170);
		g.drawString("Right Arrow - Move Right", bx + 30, by + 190);

		g.drawString("Mrs. Pacman:", bx + 220, by + 110);
		g.drawString("W - Move Up", bx + 240, by + 130);
		g.drawString("A - Move Left", bx + 240, by + 150);
		g.drawString("S - Move Down", bx + 240, by + 170);
		g.drawString("D - Move Right", bx + 240, by + 190);

		g.drawString("Ghost 1", bx + 10, by + 220);
		g.drawString("W - Move Up", bx + 30, by + 240);
		g.drawString("A - Move Left", bx + 30, by + 260);
		g.drawString("S - Move Down", bx + 30, by + 280);
		g.drawString("D - Move Right", bx + 30, by + 300);

		g.drawString("Ghost 2", bx + 220, by + 220);
		g.drawString("I - Move Up", bx + 240, by + 240);
		g.drawString("J - Move Left", bx + 240, by + 260);
		g.drawString("K - Move Down", bx + 240, by + 280);
		g.drawString("L - Move Right", bx + 240, by + 300);

		g.drawString("Press 'h' to return...", bx + 245, by + 330);
	}

	/**
	 * Display the current score on the bottom right of the screen
	 *
	 * @param g a Graphics object
	 */
	private void drawScore(Graphics g, GameInterface board) {
		g.setFont(smallFont);
		g.setColor(new Color(96, 128, 255));
		int score = board.getScore();
		GameType gt = board.getGameType();
		if (gt == GameType.VERSUS) {
			String p = "Pellets left: " + (board.getNumPellet() - score);
			g.drawString(p, SCRSIZE / 2 + 96, SCRSIZE + 16);
		} else {
			String s = "Score: " + score;
			g.drawString(s, SCRSIZE / 2 + 136, SCRSIZE + 16);
			g.drawString("BG Games: " + board.getNCompletedGames(), SCRSIZE / 2 - 100, SCRSIZE + 16);
			g.drawString("Models: " + board.getNTrainedModels(), SCRSIZE / 2 + 20, SCRSIZE + 16);
		}
		int pacmanLives = board.getPacman().lives;
		for (int i = 0; i < pacmanLives; i++) {
			g.drawImage(AssetController.getInstance().getLifeImage(PlayerType.PACMAN), i * 28 + 8, SCRSIZE + 1, this);
		}

		if (gt == GameType.COOPERATIVE) {
			int msPacmanLives = board.getMsPacman().lives;
			for (int i = 0; i < msPacmanLives; i++) {
				g.drawImage(AssetController.getInstance().getLifeImage(PlayerType.MSPACMAN), i * 28 + 108, SCRSIZE + 1,
						this);
			}
		}
	}

	/**
	 * Draws the ghost
	 *
	 * @param g      a Graphics2D object
	 * @param canvas A Jcomponent object to be drawn on
	 */
	private void drawGhost(Graphics2D g, JComponent canvas, Ghost ghost) {
		AssetController ac = AssetController.getInstance();
		if (ghost.edible)
			g.drawImage(ac.getScaredGhostImage(), ghost.x + 4, ghost.y + 4, canvas);
		else
			g.drawImage(ac.getGhostImage(ghost.getPlayerType()), ghost.x + 4, ghost.y + 4, canvas);
	}

	/**
	 * Calls the appropriate draw method for the direction Pacman is facing
	 *
	 * @param g2d    a Graphics2D object
	 * @param canvas A Jcomponent object to be drawn on
	 */
	private void drawPacman(Graphics2D g2d, JComponent canvas, PacPlayer pacman) {
		if (pacman.deathTimer % 5 > 3)
			return; // Flicker while invincibile

		AssetController ac = AssetController.getInstance();
		if (pacman.direction == 1)
			g2d.drawImage(ac.pacmanLeft[pacman.getPlayerType().ordinal()][pacman.getPacmananimpos()], pacman.x + 4,
					pacman.y + 4, canvas);
		else if (pacman.direction == 2)
			g2d.drawImage(ac.pacmanUp[pacman.getPlayerType().ordinal()][pacman.getPacmananimpos()], pacman.x + 4,
					pacman.y + 4, canvas);
		else if (pacman.direction == 4)
			g2d.drawImage(ac.pacmanDown[pacman.getPlayerType().ordinal()][pacman.getPacmananimpos()], pacman.x + 4,
					pacman.y + 4, canvas);
		else
			g2d.drawImage(ac.pacmanRight[pacman.getPlayerType().ordinal()][pacman.getPacmananimpos()], pacman.x + 4,
					pacman.y + 4, canvas);
	}

	public void drawGameOver() {
		GameType gt = board.getGameType();
		int score = board.getScore();
		if (gt != GameType.VERSUS) {
			if (score > 1)
				sl.writeScore(score);
		}
		Date d = new Date();
		if (gt == GameType.SINGLEPLAYER)
			leaderBoardGui.showEndGameScreen(score, d, 1);
		else if (gt == GameType.COOPERATIVE)
			leaderBoardGui.showEndGameScreen(score, d, 2);
		else if (gt == GameType.VERSUS)
			leaderBoardGui.showEndGameScreen(score, d, 3);
		// gt = GameType.INTRO;
		timer.stop();

	}

	/**
	 * Draws the maze that serves as a playing field.
	 *
	 * @param g2d a Graphics2D object
	 */
	private void drawMaze(Graphics2D g2d) {
		int x, y;
		g2d.setStroke(new BasicStroke(2));
		Grid grid = board.getGrid();
		for (int i = 0; i < NUMBLOCKS; i++) {
			for (int j = 0; j < NUMBLOCKS; j++) {
				y = i * BLOCKSIZE + 3;
				x = j * BLOCKSIZE + 3;


				g2d.setColor(mazeColor);

				if ((grid.getScreenData()[i][j] & GridData.GRID_CELL_BORDER_LEFT) != 0) // draws
					// left
					g2d.drawLine(x, y, x, y + BLOCKSIZE - 1);
				if ((grid.getScreenData()[i][j] & GridData.GRID_CELL_BORDER_TOP) != 0) // draws
																						// top
					g2d.drawLine(x, y, x + BLOCKSIZE - 1, y);
				if ((grid.getScreenData()[i][j] & GridData.GRID_CELL_BORDER_RIGHT) != 0) // draws
					// right
					g2d.drawLine(x + BLOCKSIZE - 1, y, x + BLOCKSIZE - 1, y + BLOCKSIZE - 1);
				if ((grid.getScreenData()[i][j] & GridData.GRID_CELL_BORDER_BOTTOM) != 0) // draws
																							// bottom
					g2d.drawLine(x, y + BLOCKSIZE - 1, x + BLOCKSIZE - 1, y + BLOCKSIZE - 1);

				g2d.setColor(dotColor);
				if ((grid.getScreenData()[i][j] & GridData.GRID_CELL_PELLET) != 0) // draws
																					// pellet
					g2d.fillRect(x + 11, y + 11, 2, 2);

				if ((grid.getScreenData()[i][j] & GridData.GRID_CELL_POWER_PILL) != 0) // draws
																						// power
																						// pill
					g2d.fillOval(x + 6, y + 6, 12, 12);
				g2d.setColor(fruitColor);
				if ((grid.getScreenData()[i][j] & GridData.GRID_CELL_FRUIT) != 0) // draws
																					// fruit
					g2d.fillRect(x + 10, y + 10, 4, 4);
			}
		}
	}
}
