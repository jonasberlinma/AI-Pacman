package edu.ucsb.cs56.projects.games.pacman;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;
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
	private int moveCount = 0;

	Comparator<Path> distanceComparator = new Comparator<Path>() {
		@Override
		public int compare(Path path1, Path path2) {
			int ret = 0;
			if (path1.distance < path2.distance) {
				ret = -1;
			} else if (path1.distance > path2.distance) {
				ret = 1;
			}
			return ret;
		}
	};

	TreeMap<String, Path> paths = new TreeMap<String, Path>();

	void addPath(String characterID, Path path) {
		if (path != null) {
			if (paths.containsKey(characterID)) {
				paths.remove(characterID);
			}
			paths.put(characterID, path);
		}
	}

	Path getNClosestPath(int order) {
		Path path = null;
		if (paths.size() > order) {
			Vector<Path> v = new Vector<Path>(paths.values());
			Collections.sort(v, distanceComparator);
			path = v.elementAt(order);
		}
		return path;
	}

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
					// Ghost move record where it is and figure out the
					// shortest path to the ghost
					int ghostX = dataEvent.getInt("x");
					int ghostY = dataEvent.getInt("y");
					Path path = gridWalker.getShortestPath(myX, myY, ghostX, ghostY);
					addPath(characterID, path);

					break;
				case "PACMA":
					moveCount++;
					// Can't move too fast or we end up with an unstable
					// feedback loop
					if (moveCount % 2 == 0) {
						Path closestGhostPath = getNClosestPath(0);
						int closestGhostDistance = closestGhostPath.distance;
						if (closestGhostPath != null) {
							Direction newDirection = null;

							if (closestGhostDistance > 4) {
								// If there is no ghost closer than 4 run to the
								// closes pellet
								newDirection = getForwardDirection(gridWalker.getClosestPelletPath(myX, myY));
							} else {
								// If not run away from the two closest ghosts
								newDirection = getRandomAwayDirection(gridWalker);
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
					}
				default:
				}
			}
			break;
		default:
		}
	}

	private Direction getRandomAwayDirection(GridWalker gridWalker) {

		Direction newDirection = null;
		HashSet<PathSection> ps = gridWalker.getPossiblePaths(gridWalker.new Point(myX, myY));

		if (ps != null) {
			Vector<Direction> possibleDirections = new Vector<Direction>();
			Path closestGhostPath = getNClosestPath(0);
			Path secondClosestGhostPath = getNClosestPath(1);
			Direction closestGhostDirection = null;
			Direction secondClosestGhostDirection = null;
			if (closestGhostPath.pathSections.size() > 0) {
				closestGhostDirection = closestGhostPath.pathSections.elementAt(0).getDirection();
			}
			if (secondClosestGhostPath != null && secondClosestGhostPath.pathSections.size() > 0) {
				secondClosestGhostDirection = secondClosestGhostPath.pathSections.elementAt(0).getDirection();
			}
			for (PathSection psi : ps) {
				// Can't walk down the given path any other direction is
				// fair game
				if (closestGhostDirection != null && secondClosestGhostDirection != null
						&& psi.getDirection() != closestGhostDirection
						//&& psi.getDirection() != secondClosestGhostDirection
						) {
					possibleDirections.add(psi.getDirection());
				}
			}
			// Of the possible direction pick a random one
			if (possibleDirections.size() > 0) {
				newDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
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

	@Override
	protected void newModel(AIModel aiModel) {
		// Don't use trained models for avoider
	}
}
