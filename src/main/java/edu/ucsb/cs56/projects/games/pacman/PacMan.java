package edu.ucsb.cs56.projects.games.pacman;

import java.lang.management.ThreadInfo;
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

	/**
	 * Main function for PacMan Class that tests to see if there are command line
	 * arguments
	 *
	 * @param args -- the command line arguments
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
			case "-aiModelTrainerClassName":
				prop.setProperty("aiModelTrainerClassName", argi.next());
				break;
			case "-numGhosts":
				prop.setProperty("numGhosts", argi.next());
				break;

			default:
				System.out.println("Invalid command Line argument" + theArg);
				System.exit(1);
			}

		}
		if (prop.getProperty("autoPlay") == null) {
			prop.setProperty("aiPlayerClassName", "edu.ucsb.cs56.projects.games.pacman.AIPlayerNull");
			prop.setProperty("aiModelTrainerClassName", "edu.ucsb.cs56.projects.games.pacman.AIModelTrainerNull");

		} else if (prop.getProperty("aiPlayerClassName") == null) {
			System.err.println("In auto play a player class name has to be specified");
			System.exit(1);
		}
		if (prop.getProperty("aiModelTrainerClassName") == null) {

			prop.setProperty("aiModelTrainerClassName", "edu.ucsb.cs56.projects.games.pacman.AIModelTrainerNull");
		}
		if (prop.getProperty("headLess") == null) {
			prop.setProperty("headLess", Boolean.toString(false));
		}
		System.out.println("Using model trainer " + prop.getProperty("aiModelTrainerClassName"));

		ThreadWarningSystem tws = new ThreadWarningSystem();
		tws.addListener(new ThreadWarningSystem.Listener() {
			public void deadlockDetected(ThreadInfo inf) {
				System.out.println("Deadlocked Thread:");
				System.out.println("------------------");
				System.out.println(inf);
				for (StackTraceElement ste : inf.getStackTrace()) {
					System.out.println("\t" + ste);
				}
			}

			public void thresholdExceeded(ThreadInfo[] threads) {
			}
		});

		GameController gc = new GameController(prop);

		gc.start();

		gc.join();

		System.exit(0);

		return;

	}
}
