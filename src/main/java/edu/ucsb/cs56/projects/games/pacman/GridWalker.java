package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.function.BiFunction;

public class GridWalker {

	public enum Direction {
		LEFT, RIGHT, UP, DOWN;
		public boolean isSame(Direction other) {
			return this == other;
		}

		public boolean isOpposite(Direction other) {
			return this == other.opposite();
		}

		public Direction opposite() {
			Direction ret = null;
			switch (this) {
			case LEFT:
				ret = RIGHT;
				break;
			case RIGHT:
				ret = LEFT;
				break;
			case UP:
				ret = DOWN;
				break;
			case DOWN:
				ret = UP;
				break;
			}
			return ret;
		}

		static Direction parseDirection(String text) {
			Direction d = null;
			if (text != null) {
				switch (text) {
				case "LEFT":
				case "←":
				case "1":
					d = Direction.LEFT;
					break;
				case "RIGHT":
				case "→":
				case "2":
					d = Direction.RIGHT;
					break;
				case "DOWN":
				case "↓":
				case "3":
					d = Direction.DOWN;
					break;
				case "UP":
				case "↑":
				case "4":
					d = Direction.UP;
					break;
				case "0":
					break;
				default:
					System.err.println("Failed to parse direction " + text);
				}
			}
			return d;
		}
	};

	private short[][] grid = null;
	private short[][] screenData = null;
	private short[][] connectionCheck = null;

	private boolean walkerInitialized = false;

	private HashSet<Point> reachablePoints = new HashSet<Point>();
	private Hashtable<String, Point> allPoints = new Hashtable<String, Point>();
	private Hashtable<Point, HashSet<PathSection>> fromPathSectionHashtable = new Hashtable<Point, HashSet<PathSection>>();
	private Hashtable<Point, HashSet<PathSection>> toPathSectionHashtable = new Hashtable<Point, HashSet<PathSection>>();

	GridWalker(GridData level, short[][] screenData) {
		grid = level.get2DGridData();
		this.screenData = screenData;
		connectionCheck = new short[Board.NUMBLOCKS][Board.NUMBLOCKS];
		buildGraph();
		walkerInitialized = true;
		// printGraph("");
		// System.exit(0);
	}

	public void printGrid(PrintStream out) {
		for (int i = 0; i < Board.NUMBLOCKS; i++) {
			for (int j = 0; j < Board.NUMBLOCKS; j++) {
				short ch = (short) (grid[j][i] & (short) 15);
				String b = i + "-" + j + "->" + ch + ";";
				out.print(b);
				if ((ch & 8) == 0)
					out.print("Down ");
				if ((ch & 2) == 0)
					out.print("Up ");
				if ((ch & 4) == 0)
					out.print("Right ");
				if ((ch & 1) == 0)
					out.print("Left ");

			}
			System.out.println();
		}
	}

	/**
	 * Prints the board graph in a format suitable for Gephi
	 * 
	 * @param fileName
	 */
	public void printGraph(String fileName) {
		PrintStream nodeOut = null;
		PrintStream edgeOut = null;
		try {
			nodeOut = new PrintStream(new FileOutputStream(fileName + "_nodes.csv"));
			edgeOut = new PrintStream(new FileOutputStream(fileName + "_edges.csv"));
		} catch (FileNotFoundException e) {
			System.err.println(
					"Unable to open output files \"" + fileName + "_nodes.csv\" and \"" + fileName + "_edges.csv\"");
			e.printStackTrace();
		}

		nodeOut.println("Id,Label");
		edgeOut.println("Source,Target,Length");

		// Write the edges (PathSections)

		Enumeration<HashSet<PathSection>> i = fromPathSectionHashtable.elements();
		while (i.hasMoreElements()) {
			HashSet<PathSection> psh = i.nextElement();
			Iterator<PathSection> j = psh.iterator();
			while (j.hasNext()) {
				j.next().print(edgeOut);
			}
		}
		// Write the nodes (Points with path choices)
		Enumeration<Point> ip = fromPathSectionHashtable.keys();
		while (ip.hasMoreElements()) {
			ip.nextElement().print(nodeOut);
			nodeOut.println();
		}
		nodeOut.flush();
		edgeOut.flush();
		nodeOut.close();
		edgeOut.close();
	}

	class WalkInstance {
		private HashSet<Point> visitedPoints;
		private HashSet<Point> unvisitedPoints;
		private Hashtable<Point, Integer> distance;
		private int maxIterations = 0;

		WalkInstance() {
			visitedPoints = new HashSet<Point>();
			unvisitedPoints = new HashSet<Point>();
			distance = new Hashtable<Point, Integer>();

			maxIterations = 10000;
		}
	}

	private Point getPoint(int x, int y) {
		return allPoints.get(new Point(x, y).nodeNumber);
	}

	HashSet<PathSection> getPossiblePaths(Point point) {
		return fromPathSectionHashtable.get(point);
	}

	ArrayList<Direction> getPossibleDirections(int x, int y) {
		HashSet<PathSection> ps = getPossiblePaths(new Point(x, y));
		ArrayList<Direction> ret = new ArrayList<Direction>();
		for (PathSection p : ps) {
			ret.add(p.getDirection());
		}
		return ret;
	}

	private WalkInstance initDijkstra(Point startPoint) {
		WalkInstance wi = new WalkInstance();
		wi.unvisitedPoints.addAll(reachablePoints);
		wi.unvisitedPoints.forEach((x) -> wi.distance.put(x, Integer.MAX_VALUE));
		wi.distance.put(startPoint, 0);
		wi.visitedPoints.add(startPoint);
		wi.unvisitedPoints.remove(startPoint);
		return wi;
	}

	public Path getClosestPelletPath(int fromX, int fromY) {
		if (!walkerInitialized) {
			return null;
		}
		Point startPoint = getPoint(fromX, fromY);

		if (!startPoint.checkReachable("Start", reachablePoints)) {
			return null;
		}
		WalkInstance wi = initDijkstra(startPoint);
		Point currentPoint = startPoint;
		Path pelletPath = walkPath(wi, currentPoint, startPoint, null, (x, y) -> !x.hasPellet(screenData));
		if (pelletPath == null) {
			System.err.println("No pellet found from " + fromX + "," + fromY);
		}

		return pelletPath;
	}

	public Path getClosestFruitPath(int fromX, int fromY) {
		if (!walkerInitialized) {
			return null;
		}
		Point startPoint = getPoint(fromX, fromY);

		if (!startPoint.checkReachable("Start", reachablePoints)) {
			return null;
		}
		WalkInstance wi = initDijkstra(startPoint);
		Point currentPoint = startPoint;
		Path fruitPath = walkPath(wi, currentPoint, startPoint, null, (x, y) -> !x.hasFruit(screenData));

		return fruitPath;
	}

	public Path getClosestPillPath(int fromX, int fromY) {
		if (!walkerInitialized) {
			return null;
		}
		Point startPoint = getPoint(fromX, fromY);

		if (!startPoint.checkReachable("Start", reachablePoints)) {
			return null;
		}
		WalkInstance wi = initDijkstra(startPoint);
		Point currentPoint = startPoint;
		return walkPath(wi, currentPoint, startPoint, null, (x, y) -> !x.hasPill(screenData));
	}

	class DirectionDistance {
		Direction direction;
		int distance;
	}

	public DirectionDistance getShortestPathDirectionDistance(int fromX, int fromY, int toX, int toY) {
		DirectionDistance dd = null;
		Path shortest = getShortestPath(fromX, fromY, toX, toY);
		if (shortest != null && shortest.getFirstDirection() != null) {
			dd = new DirectionDistance();
			dd.direction = shortest.getFirstDirection();
			dd.distance = shortest.getDistance();
		}
		return dd;
	}

	public Path getShortestPath(int fromX, int fromY, int toX, int toY) {
		if (!walkerInitialized) {
			return null;
		}
		Point startPoint = getPoint(fromX, fromY);
		Point endPoint = getPoint(toX, toY);

		if (!startPoint.checkReachable("Start", reachablePoints)) {
			return null;
		}
		if (!endPoint.checkReachable("End", reachablePoints)) {
			return null;
		}
		WalkInstance wi = initDijkstra(startPoint);
		Point currentPoint = startPoint;
		return walkPath(wi, currentPoint, startPoint, endPoint, (x, y) -> !x.equals(y));
	}

	private Path walkPath(WalkInstance wi, Point currentPoint, Point startPoint, Point endPoint,
			BiFunction<Point, Point, Boolean> stoppingCondition) {
		while (currentPoint != null && stoppingCondition.apply(currentPoint, endPoint)) {
			wi.visitedPoints.add(currentPoint);
			wi.unvisitedPoints.remove(currentPoint);
			updateDistances(wi, currentPoint);
			currentPoint = findNext(wi, currentPoint);
		}
		Path path = null;
		if (currentPoint != null) {
			int shortestDistance = wi.distance.get(currentPoint);
			// Now walk backwards to find the shortest path
			ArrayList<PathSection> shortestPath = new ArrayList<PathSection>();
			while (!currentPoint.equals(startPoint) && wi.maxIterations-- > 0) {
				HashSet<PathSection> psh = toPathSectionHashtable.get(currentPoint);
				int minDistance = Integer.MAX_VALUE;
				PathSection minPathSection = null;
				for (PathSection ps : psh) {
					if (wi.distance.get(ps.getFromPoint()) < minDistance) {
						minDistance = wi.distance.get(ps.getFromPoint());
						minPathSection = ps;
					}
				}
				// Due to some bug some characters decide to step out of bounds
				if (minPathSection != null) {
					currentPoint = minPathSection.getFromPoint();
					shortestPath.add(minPathSection);
				}
			}
			// Reverse the order since we walked backwards
			Collections.reverse(shortestPath);
			path = new Path(shortestDistance, shortestPath);
		}
		return path;
	}

	private void updateDistances(WalkInstance wi, Point thisPoint) {
		HashSet<PathSection> p = fromPathSectionHashtable.get(thisPoint);
		for (PathSection i : p) {
			if (!wi.visitedPoints.contains(i.getToPoint())) {
				if (wi.distance.get(i.getToPoint()) > (wi.distance.get(thisPoint) + i.getLength())) {
					wi.distance.put(i.getToPoint(), wi.distance.get(thisPoint) + i.getLength());
				}
			}
		}
	}

	private Point findNext(WalkInstance wi, Point thisPoint) {
		Point nextPoint = null;
		int minDistance = Integer.MAX_VALUE;
		for (Point i : wi.unvisitedPoints) {
			if (wi.distance.get(i) < minDistance) {
				nextPoint = i;
				minDistance = wi.distance.get(i);
			}
		}
		return nextPoint;
	}

	private void buildGraph() {
		for (int i = 0; i < Board.NUMBLOCKS; i++) {
			for (int j = 0; j < Board.NUMBLOCKS; j++) {
				Point point = new Point(i, j);
				fromPathSectionHashtable.put(point, new HashSet<PathSection>());
				toPathSectionHashtable.put(point, new HashSet<PathSection>());
				allPoints.put(point.getNodeNumber(), point);
			}
		}
		for (int i = 0; i < Board.NUMBLOCKS; i++) {
			for (int j = 0; j < Board.NUMBLOCKS; j++) {

				Point fromPoint = allPoints.get(i + "-" + j);
				if (canWalkDown(fromPoint)) {
					Point downPoint = allPoints.get(fromPoint.stepDown().nodeNumber);
					reachablePoints.add(fromPoint);
					reachablePoints.add(downPoint);
					PathSection pathSection1 = new PathSection(fromPoint, downPoint, 1);
					PathSection pathSection2 = new PathSection(downPoint, fromPoint, 1);
					fromPathSectionHashtable.get(fromPoint).add(pathSection1);
					fromPathSectionHashtable.get(downPoint).add(pathSection2);
					toPathSectionHashtable.get(fromPoint).add(pathSection2);
					toPathSectionHashtable.get(downPoint).add(pathSection1);
				}
				if (canWalkRight(fromPoint)) {
					Point rightPoint = allPoints.get(fromPoint.stepRight().nodeNumber);
					reachablePoints.add(fromPoint);
					reachablePoints.add(rightPoint);
					PathSection pathSection1 = new PathSection(fromPoint, rightPoint, 1);
					PathSection pathSection2 = new PathSection(rightPoint, fromPoint, 1);
					fromPathSectionHashtable.get(fromPoint).add(pathSection1);
					fromPathSectionHashtable.get(rightPoint).add(pathSection2);
					toPathSectionHashtable.get(fromPoint).add(pathSection2);
					toPathSectionHashtable.get(rightPoint).add(pathSection1);
				}
			}
		}
		for (short i = 0; i < Board.NUMBLOCKS; i += 2) {
			for (short j = 0; j < Board.NUMBLOCKS; j += 2) {
				connectionCheck[j][i] = 0;
			}
		}
		// Starting point for Pacman
		connectionCheck[8][11] = 1;

		for (short iteration = 0; iteration < 15; iteration++) {
			for (short i = 0; i < Board.NUMBLOCKS; i++) {
				for (short j = 0; j < Board.NUMBLOCKS; j++) {
					Point point = allPoints.get(i + "-" + j);
					if (canWalkDown(point))
						connectionCheck[j][i] += connectionCheck[(j + 1) % Board.NUMBLOCKS][i];
					if (canWalkUp(point))
						connectionCheck[j][i] += connectionCheck[(j - 1 + Board.NUMBLOCKS) % Board.NUMBLOCKS][i];
					if (canWalkRight(point))
						connectionCheck[j][i] += connectionCheck[j][(i + 1) % Board.NUMBLOCKS];
					if (canWalkLeft(point))
						connectionCheck[j][i] += connectionCheck[j][(i - 1 + Board.NUMBLOCKS) % Board.NUMBLOCKS];
					connectionCheck[j][i] = (short) Math.min(connectionCheck[j][i], 1);
				}
			}
		}
		for (int i = 0; i < Board.NUMBLOCKS; i++) {
			for (int j = 0; j < Board.NUMBLOCKS; j++) {
				if (connectionCheck[j][i] == 0) {
					Point p = new Point(i, j);
					reachablePoints.remove(p);
					fromPathSectionHashtable.remove(p);
					toPathSectionHashtable.remove(p);
				}
			}
		}
	}

	private boolean canWalkRight(Point point) {
		// Current point
		short ch = (short) (grid[point.y][point.x] & (short) 15);
		// Right point from current
		short ch2 = (short) (grid[point.y][(point.x + 1) % Board.NUMBLOCKS] & (short) 15);
		// Check that we can walk right and back left to current
		return ((ch & 4) == 0) && ((ch2 & 1) == 0);
	}

	private boolean canWalkLeft(Point point) {
		// Current point
		short ch = (short) (grid[point.y][(point.x - 1 + Board.NUMBLOCKS) % Board.NUMBLOCKS] & (short) 15);
		// Right point from current
		short ch2 = (short) (grid[point.y][point.x] & (short) 15);
		// Check that we can walk right and back left to current
		return ((ch & 4) == 0) && ((ch2 & 1) == 0);
	}

	private boolean canWalkDown(Point point) {
		// Current point
		short ch = (short) (grid[point.y][point.x] & (short) 15);
		// Down point from current
		short ch2 = (short) (grid[(point.y + 1) % Board.NUMBLOCKS][point.x] & (short) 15);
		// Check that we can walk down and back up to current
		return ((ch & 8) == 0) && ((ch2 & 2) == 0);
	}

	private boolean canWalkUp(Point point) {
		// Current point
		short ch = (short) (grid[(point.y - 1 + Board.NUMBLOCKS) % Board.NUMBLOCKS][point.x] & (short) 15);
		// Down point from current
		short ch2 = (short) (grid[point.y][point.x] & (short) 15);
		// Check that we can walk up and back down to current
		return ((ch & 8) == 0) && ((ch2 & 2) == 0);
	}
}
