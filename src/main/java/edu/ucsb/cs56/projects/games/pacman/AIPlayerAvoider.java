package edu.ucsb.cs56.projects.games.pacman;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import edu.ucsb.cs56.projects.games.pacman.GridWalker.Direction;
import edu.ucsb.cs56.projects.games.pacman.GridWalker.Path;
import edu.ucsb.cs56.projects.games.pacman.GridWalker.PathSection;

/**
 * This one tries to avoid all the ghosts by running away from the closest
 * ghost. It seems to work at shorter distance but is not a very good overall
 * strategy.
 * 
 * @author jonas
 *
 */
public class AIPlayerAvoider extends AIPlayer {

	Random random = new Random(System.currentTimeMillis());
	int numSteps = 1;

	private int myX, myY;
	private int closestGhostDistance;
	private Path closestGhostPath;

	@Override
	public void dataEvent(Grid grid, DataEvent dataEvent) {
		GridWalker gridWalker = grid.getGridWalker();
		switch (dataEvent.eventType) {

		case INTRO:
			// Let's press the start key
			pressKey(KeyEvent.VK_S);
			pressKey(KeyEvent.VK_DOWN);
			break;
		case GAME_OVER:
			// Press ESCAPE to quit the game and then stop
			pressKey(KeyEvent.VK_ESCAPE);
			stop();
			break;
		case KEY_PRESS:
		case KEY_RELEASE:
			break;

		case MOVE:
			String playerType = dataEvent.getString("playerType");
			String characterID = dataEvent.getString("characterID");
			if (playerType != null) {
				String playerTypeShort = playerType.substring(0, 5);
				switch (playerTypeShort) {
				case "GHOST":
					// Ghost move record where it is and figure out the shortest path to the ghost
					int ghostX = dataEvent.getInt("x");
					int ghostY = dataEvent.getInt("y");
					Path path = gridWalker.getShortestPath(myX, myY, ghostX, ghostY);
					// Figure out the closest ghost
					if (path != null && closestGhostDistance > path.distance) {
						closestGhostDistance = Math.min(closestGhostDistance, path.distance);
						closestGhostPath = path;
					}
					break;
				case "PACMA":
					if (closestGhostPath != null) {
						Direction newDirection = null;
						Path walkPath = null;
						if (closestGhostDistance > 4) {
							// If there is a ghost closer than 6 away run
							walkPath = gridWalker.getClosestGoodiesPath(myX, myY);
							newDirection = getForwardDirection(walkPath);
						} else {
							// If not run to the closest pellet
							walkPath = closestGhostPath;
							newDirection = getRandomReverseDirection(gridWalker, walkPath);
						}
						if (newDirection != null) {
							numSteps = random.nextInt(10) + 1;
							switch (newDirection) {
							case LEFT:
								pressKey(KeyEvent.VK_LEFT);
								break;
							case RIGHT:
								pressKey(KeyEvent.VK_RIGHT);
								break;
							case DOWN:
								pressKey(KeyEvent.VK_DOWN);
								break;
							case UP:
								pressKey(KeyEvent.VK_UP);
								break;
							default:

							}
						}
					}
					myX = dataEvent.getInt("x");
					myY = dataEvent.getInt("y");
					closestGhostDistance = Integer.MAX_VALUE;
					break;
				default:
				}

			}
			break;

		default:
		}
	}

	private Direction getRandomReverseDirection(GridWalker gridWalker, Path walkPath) {

		Direction newDirection = null;
		if (walkPath.pathSections.size() != 0) {
			Direction directionFollowingThePath  = walkPath.pathSections.elementAt(0).getDirection();
			HashSet<PathSection> ps = gridWalker.getPossiblePaths(gridWalker.new Point(myX, myY));

			if (ps != null) {
				Vector<Direction> possibleDirections = new Vector<Direction>();
				for (PathSection psi : ps) {
					// Can't walk down the given path any other direction is fair game
					if (psi.getDirection() != directionFollowingThePath) {
						possibleDirections.add(psi.getDirection());
					}
				}
				// Of the possible direction pick a random one
				if (possibleDirections.size() > 0) {
					newDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
				}
			}
		}
		return newDirection;
	}

	private Direction getForwardDirection(Path walkPath) {
		Direction newDirection = null;
		if (walkPath != null && walkPath.pathSections.size() > 0) {
			newDirection = walkPath.pathSections.elementAt(0).getDirection();
		}
		return newDirection;
	}
}
