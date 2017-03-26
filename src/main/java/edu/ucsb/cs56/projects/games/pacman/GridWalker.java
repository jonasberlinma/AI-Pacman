package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class GridWalker {

	private short[][] grid = null;
	private short[][] connectionCheck = null;

	private HashSet<Point> reachablePoints = new HashSet<Point>();
	private Hashtable<String, Point> allPoints = new Hashtable<String, Point>();
	private Hashtable<Point, HashSet<PathSection>> pathSectionHashtable = new Hashtable<Point, HashSet<PathSection>>();

	GridWalker(GridData level) {
		grid = level.get2DGridData();
		connectionCheck = new short[Board.NUMBLOCKS][Board.NUMBLOCKS];
	}

	protected class PathSection {
		Point fromPoint, toPoint;
		int length;

		PathSection(Point fromPoint, Point toPoint, int length) {
			this.fromPoint = fromPoint;
			this.toPoint = toPoint;
			this.length = length;
		}

		@Override
		public boolean equals(Object other) {
			PathSection ps = (PathSection) other;
			return this.fromPoint.equals(ps.fromPoint) && this.toPoint.equals(ps.toPoint);
		}

		@Override
		public int hashCode() {
			return fromPoint.hashCode() * 31 * toPoint.hashCode();
		}

		void print(PrintStream out) {
			out.println("" + fromPoint.nodeNumber + "," + toPoint.nodeNumber + "," + length);
		}
	}

	protected class Point {

		int x, y;
		String nodeNumber = "";
		private int distance;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
			nodeNumber = getNodeNumber();
		}

		Point stepRight() {
			Point newPoint = new Point((x + 1) % Board.NUMBLOCKS, y );
			return newPoint;
		}

		Point stepDown() {
			Point newPoint = new Point(x , (y+ 1) % Board.NUMBLOCKS);
			return newPoint;
		}

		private String getNodeNumber() {
			return x + "-" + y;
		}

		@Override
		public boolean equals(Object point) {
			Point p = (Point) point;
			return (this.x == p.x && this.y == p.y);
		}

		@Override
		public int hashCode() {
			int hash = 1;
			hash = hash * 31 + nodeNumber.hashCode() * 4 + x * 8 + y * 16;
			return hash;
		}

		void print(PrintStream out) {
			out.print("" + nodeNumber + "," + nodeNumber);
		}

		private void setDistance(int distance) {
			this.distance = distance;
		}
	}

	public void computeDistanceMap() {

		// printGrid(System.out);
		// Pick a starting point
		// Walk the maze
		//printGrid(System.out);
		buildGraph();
		printGraph();
		
		getShortestPath(2, 0, 13, 15);
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

	private void printGraph() {
		PrintStream nodeOut = null;
		PrintStream edgeOut = null;
		try {
			nodeOut = new PrintStream(new FileOutputStream("nodes.csv"));
			edgeOut = new PrintStream(new FileOutputStream("edges.csv"));
		} catch (FileNotFoundException e) {
			System.err.println("Unable to open output files \"nodes.csv\" and \"edges.csv\"");
			e.printStackTrace();
		}

		nodeOut.println("Id,Label");
		edgeOut.println("Source,Target,Length");

		// Write the edges (PathSections)

		Enumeration<HashSet<PathSection>> i = pathSectionHashtable.elements();
		while (i.hasMoreElements()) {
			HashSet<PathSection> psh = i.nextElement();
			Iterator<PathSection> j = psh.iterator();
			while (j.hasNext()) {
				j.next().print(edgeOut);
			}
		}
		// Write the nodes (Points with path choices)
		Enumeration<Point> ip = pathSectionHashtable.keys();
		while (ip.hasMoreElements()) {
			ip.nextElement().print(nodeOut);
			nodeOut.println();
		}
		nodeOut.flush();
		edgeOut.flush();
		nodeOut.close();
		edgeOut.close();
	}

	private HashSet<Point> visitedPoints;
	private HashSet<Point> unvisitedPoints;

	public int getShortestPath(int fromX, int fromY, int toX, int toY) {
		int startX = fromX / Board.BLOCKSIZE;
		int startY = fromY / Board.BLOCKSIZE;
		int endX = toX / Board.BLOCKSIZE;
		int endY = toY / Board.BLOCKSIZE;
		visitedPoints = new HashSet<Point>();
		unvisitedPoints = new HashSet<Point>();
		Point startPoint = allPoints.get(new Point(startX, startY).nodeNumber);
		Point endPoint = allPoints.get(new Point(endX, endY).nodeNumber);

		if (!reachablePoints.contains(startPoint)) {
			System.out.println("Start point not found " + startX + "-" + startY);
			return 0;
		}
		if (!reachablePoints.contains(endPoint)) {
			System.out.println("End point not found " + endX + "-" + endY);
			return 0;
		}
		visitedPoints.clear();
		unvisitedPoints.clear();
		Enumeration<Point> p = pathSectionHashtable.keys();
		while (p.hasMoreElements()) {
			Point point = p.nextElement();
			if (reachablePoints.contains(point)) {
				unvisitedPoints.add(point);
			}
		}
		unvisitedPoints.forEach((x) -> x.setDistance(Integer.MAX_VALUE));
		startPoint.setDistance(0);

		visitedPoints.add(startPoint);
		unvisitedPoints.remove(startPoint);
		Point currentPoint = startPoint;

		while (currentPoint != null && !currentPoint.equals(endPoint)) {
			visitedPoints.add(currentPoint);
			unvisitedPoints.remove(currentPoint);
			updateDistances(currentPoint);
			Point nextPoint = findNext(currentPoint);
			if (nextPoint == null) {
				System.out.print("Puking ");
				currentPoint.print(System.out);
				System.out.print(" Working from ");
				startPoint.print(System.out);
				System.out.print(" to ");
				endPoint.print(System.out);
				System.out.println("Size of unvisited " + unvisitedPoints.size());
			}
			currentPoint = nextPoint;
		}
		return (currentPoint == null) ? 0 : currentPoint.distance;
	}

	private void updateDistances(Point thisPoint) {
		HashSet<PathSection> p = pathSectionHashtable.get(thisPoint);
		for (PathSection i : p) {
			if (!visitedPoints.contains(i.toPoint)) {
				if (i.toPoint.distance > (thisPoint.distance + i.length)) {
					i.toPoint.distance = thisPoint.distance + i.length;
				}
			}
		}
	}

	private Point findNext(Point thisPoint) {
		Point nextPoint = null;
		int minDistance = Integer.MAX_VALUE;
		for (Point i : unvisitedPoints) {
			if (i.distance < minDistance) {
				nextPoint = i;
				minDistance = i.distance;
			}
		}
		return nextPoint;
	}

	private void buildGraph() {
		for (int i = 0; i < Board.NUMBLOCKS; i++) {
			for (int j = 0; j < Board.NUMBLOCKS; j++) {
				Point point = new Point(i, j);
				pathSectionHashtable.put(point, new HashSet<PathSection>());
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
					pathSectionHashtable.get(fromPoint).add(pathSection1);
					pathSectionHashtable.get(downPoint).add(pathSection2);
				}
				if (canWalkRight(fromPoint)) {
					Point rightPoint = allPoints.get(fromPoint.stepRight().nodeNumber);
					reachablePoints.add(fromPoint);
					reachablePoints.add(rightPoint);
					PathSection pathSection1 = new PathSection(fromPoint, rightPoint, 1);
					PathSection pathSection2 = new PathSection(rightPoint, fromPoint, 1);
					pathSectionHashtable.get(fromPoint).add(pathSection1);
					pathSectionHashtable.get(rightPoint).add(pathSection2);
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
					reachablePoints.remove(new Point(i, j));
				}
			}
		}
		System.out.println("Have " + reachablePoints.size() + " reachabe");
		System.out.println("and " + allPoints.size() + " total.");

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
