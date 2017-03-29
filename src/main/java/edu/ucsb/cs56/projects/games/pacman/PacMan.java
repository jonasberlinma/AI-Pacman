package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
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

	public void runIt(Properties prop) throws InterruptedException, FileNotFoundException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		BoardRenderer boardRenderer = null;
		BoardFrame bf = null;

		AIGame aiGame = new AIGame(prop, Integer.parseInt(prop.getProperty("loopDelay")), false);

		BackgroundGameController bgc = new BackgroundGameController(prop);

		if (!Boolean.getBoolean(prop.getProperty("headLess"))) {
			// This circular dependency can be removed by removing the the
			// leaderboard call in Board
			boardRenderer = new BoardRenderer(aiGame.getBoard(), bgc);
			aiGame.addBoardRendered(boardRenderer, prop.getProperty("leaderBoard"));

			bf = new BoardFrame();

			bf.add(boardRenderer);
			boardRenderer.callLeaderboardMain(prop.getProperty("leaderBoard"));

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

		Properties prop = new Properties();

		while (argi.hasNext()) {
			String theArg = argi.next();
			switch (theArg) {
			case "-loopDelay":
				prop.setProperty("loopDelay", argi.next());
				break;
			case "-leaderBoard":
				prop.setProperty("leaderBoard", argi.next());
				break;
			case "-headLess":
				prop.setProperty("headLess", Boolean.toString(true));
				break;
			case "-autoPlay":
				prop.setProperty("autoPlay", Boolean.toString(true));
				break;
			case "-nBackgroundPlayers":
				prop.setProperty("nBackgroundPlayers", argi.next());
				break;
			case "-aiPlayerClassName":
				prop.setProperty("aiPlayerClassName", argi.next());
				break;

			default:
				System.out.println("Invalid command Line argument" + theArg);
				System.exit(1);
			}

		}
		if (prop.getProperty("autoPlay") == null) {
			prop.setProperty("aiPlayerClassName", "edu.ucsb.cs56.projects.games.pacman.AIPlayerNull");

		} else if (prop.getProperty("aiPlayerClassName") == null) {
			System.err.println("In auto play a player class name has to be specified");
			System.exit(1);
		}
		PacMan pacman = new PacMan();
		try {
			pacman.runIt(prop);
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
