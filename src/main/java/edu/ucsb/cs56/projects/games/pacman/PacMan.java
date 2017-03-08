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
	 * @throws FileNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * 
	 */

	public void runIt(String aiPlayerClassName, String leaderBoard, int loopDelay, boolean headLess, boolean autoPlay,
			int nBackgroundPlayers) throws InterruptedException, FileNotFoundException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		BoardRenderer boardRenderer = null;
		BoardFrame bf = null;

		AIGame aiGame = new AIGame(aiPlayerClassName, loopDelay, false);

		BackgroundGameController bgc = new BackgroundGameController(aiPlayerClassName, nBackgroundPlayers);

		if (!headLess) {
			// This circular dependency can be removed by removing the the
			// leaderboard call in Board
			boardRenderer = new BoardRenderer(aiGame.getBoard(), bgc);
			aiGame.addBoardRendered(boardRenderer, leaderBoard);

			bf = new BoardFrame();

			bf.add(boardRenderer);
			boardRenderer.callLeaderboardMain(leaderBoard);

			boardRenderer.start();
		}

		bgc.start();
		aiGame.start();
		aiGame.join();
		// Turn off the renderer if there is one
		if (boardRenderer != null) {
			boardRenderer.stop();
			bf.dispose();
		}
		System.exit(0);
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
		String aiPlayerClassName = null;
		int nBackgroundPlayers = 0;

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
			case "-nBackgroundPlayers":
				nBackgroundPlayers = new Integer(argi.next()).intValue();
				break;
			case "-aiPlayerClassName":
				aiPlayerClassName = argi.next();
				break;

			default:
				System.out.println("Invalid command Line argument" + theArg);
				System.exit(1);
			}

		}
		if (!autoPlay) {
			aiPlayerClassName = "edu.ucsb.cs56.projects.games.pacman.AIPlayerNull";

		} else if (aiPlayerClassName == null) {
			System.err.println("In auto play a player class name has to be specified");
			System.exit(1);
		}
		PacMan pacman = new PacMan();
		try {
			pacman.runIt(aiPlayerClassName, leaderBoard, loopDelay, headLess, autoPlay, nBackgroundPlayers);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;

	}
}
