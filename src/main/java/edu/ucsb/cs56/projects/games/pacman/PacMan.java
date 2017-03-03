package edu.ucsb.cs56.projects.games.pacman;

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
	 * 
	 */

	public void runIt(String leaderBoard, int loopDelay, boolean headLess, boolean oneTime, boolean autoPlay) {
		
		Board board = new Board();
		board.setLoopDelay(loopDelay);		
		board.setOneTime(oneTime);

		if (!headLess) {
			BoardRenderer bg = new BoardRenderer(board, loopDelay);

			board.addBoardGraphics(bg);

			BoardFrame bf = new BoardFrame();

			bf.add(board.bg);
			bg.callLeaderboardMain(leaderBoard);

			bg.start();
		}

		Thread boardThread = new Thread(board);
		Thread aiPlayerThread = new Thread(new AIPlayerRandom(board, 40, board.getDataInterface()));
		if(autoPlay){
			aiPlayerThread.start();
		}
		boardThread.start();
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
		boolean oneTime = false;
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
			case "-oneTime":
				oneTime = true;
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
		pacman.runIt(leaderBoard, loopDelay, headLess, oneTime, autoPlay);

		return;

	}
}
