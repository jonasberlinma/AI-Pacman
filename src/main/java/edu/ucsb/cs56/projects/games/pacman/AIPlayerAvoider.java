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
					if (numSteps % 4 == 0) {
						int ghostX = dataEvent.getInt("x");
						int ghostY = dataEvent.getInt("y");
						Path path = gridWalker.getShortestPath(myX, myY, ghostX, ghostY);
						if (path != null && closestGhostDistance > path.distance) {
							closestGhostDistance = Math.min(closestGhostDistance, path.distance);
							closestGhostPath = path;
						}
					}
					break;
				case "PACMA":
					if (numSteps-- == 0 && closestGhostPath != null) {
						GridWalker.Direction newDirection = null;
						if (closestGhostPath.pathSections.size() != 0) {
							GridWalker.PathSection p = closestGhostPath.pathSections.elementAt(0);
							// This has to be fixed so the user doesn't have to
							// worry about BLOCKSIZE
							HashSet<PathSection> ps = grid.getGridWalker().getPossiblePaths(
									gridWalker.new Point(myX, myY));

							if (ps != null) {
								Vector<Direction> possibleDirections = new Vector<Direction>();
								for (PathSection psi : ps) {
									if (psi.getDirection() != p.getDirection()) {
										possibleDirections.add(psi.getDirection());
									}
								}
								if (possibleDirections.size() > 0) {
									newDirection = possibleDirections
											.get(new Random().nextInt(possibleDirections.size()));
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
}
