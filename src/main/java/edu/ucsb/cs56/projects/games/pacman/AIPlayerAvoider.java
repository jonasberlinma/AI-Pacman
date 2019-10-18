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

	private static final long PILL_DURATION_MOVES = 125;
	Random random = new Random(System.currentTimeMillis());
	int numSteps = 1;

	private int myX, myY;
	private int moveCount = 0;

	private long pillEatMove = 0;

	Comparator<Path> distanceComparator = new Comparator<Path>() {
		@Override
		public int compare(Path path1, Path path2) {
			int ret = 0;
			if (path1.getDistance() < path2.getDistance()) {
				ret = -1;
			} else if (path1.getDistance() > path2.getDistance()) {
				ret = 1;
			}
			return ret;
		}
	};

	private TreeMap<String, Path> paths = new TreeMap<String, Path>();

	void addPath(String characterID, Path path) {
		if (path != null) {
			if (paths.containsKey(characterID)) {
				paths.remove(characterID);
			}
			paths.put(characterID, path);
		}
	}

	Path getNthClosestPath(int order) {
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
					if (path != null) {
						path.setEdible(dataEvent.getBoolean("edible"));
						addPath(characterID, path);
					}

					break;
				case "PACMA":
					moveCount++;
					// Can't move too fast or we end up with an unstable
					// feedback loop
					myX = dataEvent.getInt("x");
					myY = dataEvent.getInt("y");
					if (moveCount % 2 == 0) {
						Path closestGhostPath = getNthClosestPath(0);
						int closestGhostDistance = closestGhostPath.getDistance();
						Path closestPillPath = gridWalker.getClosestPelletPath(myX, myY);
						int closestPillDistance = closestPillPath != null ?closestPillPath.getDistance():30;
						if (closestGhostPath != null) {
							Direction newDirection = null;
							if (closestGhostPath.getEdible() && closestGhostDistance < 12
									&& (movesSincePillEat() < PILL_DURATION_MOVES)) {
								// If the ghosts are edible run to the closes
								// one if it it not more than 12 steps away
								newDirection = closestGhostPath.getFirstDirection();
							} else if (closestGhostDistance > 5 && closestGhostDistance < 12 && closestPillDistance < 12
									&& !closestGhostPath.equals(closestPillPath)) {
								// If there is no ghosts within 4 steps and both
								// a ghost and a pill within 12 steps go eat the
								// pill
								newDirection = getForwardDirection(closestPillPath);
							} else if (closestGhostDistance > 4) {
								// If there is no ghost closer than 4 run to the
								// closes pellet
								newDirection = getForwardDirection(gridWalker.getClosestPelletPath(myX, myY));
							} else {
								// If not run away from the closest ghosts
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

						closestGhostDistance = Integer.MAX_VALUE;
						break;
					}
				default:
				}
			}
			break;
		case EAT_PILL:
			pillEatMove = this.moveCount;
			break;
		default:
		}
	}

	private long movesSincePillEat() {
		return moveCount - pillEatMove;
	}

	private Direction getRandomAwayDirection(GridWalker gridWalker) {

		Direction newDirection = null;
		HashSet<PathSection> ps = gridWalker.getPossiblePaths(gridWalker.new Point(myX, myY));

		if (ps != null) {
			Vector<Direction> possibleDirections = new Vector<Direction>();
			Path closestGhostPath = getNthClosestPath(0);
			Path secondClosestGhostPath = getNthClosestPath(1);
			Direction closestGhostDirection = null;
			Direction secondClosestGhostDirection = null;
			closestGhostDirection = closestGhostPath.getFirstDirection();
			if (secondClosestGhostPath != null) {
				secondClosestGhostDirection = secondClosestGhostPath.getFirstDirection();
			}
			for (PathSection psi : ps) {
				// Can't walk down the given path any other direction is
				// fair game
				if (closestGhostDirection != null && secondClosestGhostDirection != null
						&& psi.getDirection() != closestGhostDirection) {
					possibleDirections.add(psi.getDirection());
				}
			}
			// Of the possible directions pick a random one
			if (possibleDirections.size() > 0) {
				newDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
			}
		}
		return newDirection;
	}

	private Direction getForwardDirection(Path walkPath) {
		Direction newDirection = null;
		if (walkPath != null) {
			newDirection = walkPath.getFirstDirection();
		}
		return newDirection;
	}

	@Override
	protected void newModel(AIModel aiModel) {
		// Don't use trained models for avoider
	}
}
