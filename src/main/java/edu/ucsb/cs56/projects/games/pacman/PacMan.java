package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

/**
 * A Pac-Man arcade game remake
 * <p>
 * The version of the code by Jan Bodnar may be found at
 * http://zetcode.com/tutorials/javagamestutorial/pacman/
 *
 * @author Brian Postma
 * @author Jan Bodnar
 * @author Dario Castellanos
 * @author Deanna Hartsook
 * @author Kateryna Fomenko
 * @author Yuxiang Zhu
 * @author Ryan Tse
 * @author Chris Beser
 * @version CS56 W16
 */

public class PacMan {
	/**
	 * @throws InterruptedException 
	 * 
	 */

	public void runIt(String leaderBoard, int loopDelay, boolean headLess,  boolean autoPlay) throws InterruptedException {
		
		Board board = new Board();
		board.setLoopDelay(loopDelay);		

		if (!headLess) {
			BoardRenderer bg = new BoardRenderer(board, loopDelay);

			board.addBoardGraphics(bg);

			BoardFrame bf = new BoardFrame();

			bf.add(board.bg);
			bg.callLeaderboardMain(leaderBoard);

			bg.start();
		}

		Thread boardThread = new Thread(board);
		Thread aiPlayerThread = null;
		try {
			AIPlayer aiPlayer = new AIPlayerRandom();
			aiPlayer.setBoardAndDataInterface(board, board.getDataInterface());
			aiPlayerThread = new Thread(aiPlayer);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		boardThread.start();
		Thread.sleep(100);
		if(autoPlay){
			aiPlayerThread.start();
		}
		try {
			boardThread.join();
			if(aiPlayerThread != null){
				aiPlayerThread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Main function for PacMan Class that tests to see if there are command
	 * line arguments
	 *
	 * @param args
	 *            -- the command line arguments
	 */
	public static void main(String[] args) {

		Iterator<String> argi = new Vector<String>(Arrays.asList(args)).iterator();

		int loopDelay = 40;
		String leaderBoard = "";
		boolean headLess = false;
		boolean autoPlay = false;

		while (argi.hasNext()) {
			String theArg = argi.next();
			switch (theArg) {
			case "-loopDelay":
				loopDelay = new Integer(argi.next());
				break;
			case "-leaderBoard":
				leaderBoard = argi.next();
				break;
			case "-headLess":
				headLess = true;
				break;
			case "-autoPlay":
				autoPlay = true;
				break;
			default:
				System.out.println("Invalid command Line argument" + theArg);
				System.exit(1);
			}

		}
		PacMan pacman = new PacMan();
		try {
			pacman.runIt(leaderBoard, loopDelay, headLess, autoPlay);
		} catch (InterruptedException e) {
	
			e.printStackTrace();
		}

		return;

	}
}
