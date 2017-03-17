package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import edu.ucsb.cs56.projects.games.pacman.Grid.Pair;

public class GridWalker {

	short[][] grid = null;
	short[][] visited = null;
	GridData level = null;

	// Point lastPoint = null;

	private int stepNumber = 0;
	private int lastStepNumber = 0;

	Hashtable<Point, Vector<PathSection>> pathSectionHashtable = new Hashtable<Point, Vector<PathSection>>();
	Vector<PathSection> pathSections = new Vector<PathSection>();

	GridWalker(GridData level) {
		this.level = level;
		grid = level.get2DGridData();
		visited = level.get2DGridData();

		for (int i = 0; i < Board.NUMBLOCKS; i++) {
			for (int j = 0; j < Board.NUMBLOCKS; j++) {
				visited[i][j] = 0;
			}
		}
	}
	public Vector<PathSection> getPathSections(){
		return pathSections;
	}
	protected class PathSection {
		Point fromPoint, toPoint;
		int length;

		PathSection(Point fromPoint, Point toPoint, int length) {
			this.fromPoint = fromPoint;
			this.toPoint = toPoint;
			this.length = length;
		}

		void print(PrintStream out) {
			out.println("" + fromPoint.nodeNumber + "," + toPoint.nodeNumber + "," + length);
		}
	}

	protected class Point {
		int x, y;
		int nodeNumber = 0;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
			nodeNumber = getNodeNumber();
		}
		private int getNodeNumber(){
			return x * 1000 + y;
		}
		@Override
		public boolean equals(Object point) {
			Point p = (Point) point;
			return (this.x == p.x && this.y == p.y && this.nodeNumber == p.nodeNumber);
		}

		@Override
		public int hashCode() {
			int hash = 1;
			hash = hash * 31 + nodeNumber * 4 + x * 8 + y * 16;
			return hash;
		}

		void step(Direction direction) {
			x = (x + direction.dx + Board.NUMBLOCKS) % Board.NUMBLOCKS;
			y = (y + direction.dy + Board.NUMBLOCKS) % Board.NUMBLOCKS;
			nodeNumber = getNodeNumber();
		}

		Point copy() {
			return new Point(this.x, this.y);
		}

		void print(PrintStream out) {
			out.print("" + nodeNumber + "," + nodeNumber);
		}

		private void setInspecting() {
			visited[x][y] = -1;
		}

		private void setVisited() {
			if(visited[x][y] == 0)
				visited[x][y] = 1;
		}

		private void setIsChoice() {
			visited[x][y] = 2;
		}

		private boolean isChoice() {
			return visited[x][y] == 2;
		}
	}

	class Direction {
		int dx, dy;

		Direction(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}
	}

	public Hashtable<Pair, Integer> computeDistanceMap() {

		// printGrid();
		// Pick a starting point
		Point point = new Point(0, 3);
		point.setIsChoice();
		// Walk the maze
		walk(point, point);
		// printWalkOrder();
		printGraph();
		return assemblePaths();
	}

	public void printGrid(PrintStream out) {
		for (int i = 0; i < Board.NUMBLOCKS; i++) {
			for (int j = 0; j < Board.NUMBLOCKS; j++) {
				short ch = (short) (grid[i][j] & (short) 15);
				String b = "   " + ch + "-" + getWalkableDirections(new Point(i, j)).size();
				b = b.substring(b.length() - 5, b.length());
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		nodeOut.println("Id,Label");
		edgeOut.println("Source,Target,Length");

		// Write the edges (PathSections)
		Iterator<PathSection> i = pathSections.iterator();
		while (i.hasNext()) {
			PathSection ps = i.next();
			ps.print(edgeOut);
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

	private Hashtable<Pair, Integer> assemblePaths() {
		// This is a brute force algorithm. It should use Yen's algorithm:
		// https://en.wikipedia.org/wiki/Yen%27s_algorithm

		Hashtable<Pair, Integer> distanceMap = new Hashtable<Pair, Integer>();

		System.out.println("Assembling paths");
		System.out.println("Using " + pathSectionHashtable.size() + " points");
		System.out.println("and " + pathSections.size() + " path sections");

		for (int i = 0; i < Board.NUMBLOCKS; i++) {
			for (int j = 0; j < Board.NUMBLOCKS; j++) {
				Point fromPoint = new Point(i, j);
				if (pathSectionHashtable.containsKey(fromPoint)) {
					for (int k = 0; k < Board.NUMBLOCKS; k++) {
						for (int l = 0; l < Board.NUMBLOCKS; l++) {
							Point toPoint = new Point(k, l);
							if (!(i == k && j == l)) {
								if (pathSectionHashtable.containsKey(toPoint)) {

									Vector<PathSection> fromPaths = pathSectionHashtable.get(fromPoint);
									Vector<PathSection> toPaths = pathSectionHashtable.get(toPoint);
									System.out.println("Options from " + fromPaths.size());
									System.out.println("Options to   " + toPaths.size());

								}
							}
						}
					}
				}
			}
		}
		return distanceMap;
	}

	private PathSection addPathSection(Point point, Point lastPoint) {
		PathSection p1 = null;
		if (lastPoint != null && !point.equals(lastPoint)) {
			p1 = new PathSection(lastPoint, point, stepNumber - lastStepNumber);
			if (!pathSectionHashtable.containsKey(lastPoint)) {
				pathSectionHashtable.put(lastPoint, new Vector<PathSection>());
			}
			pathSectionHashtable.get(lastPoint).add(p1);
			PathSection p2 = new PathSection(point, lastPoint, stepNumber - lastStepNumber);
			if (!pathSectionHashtable.containsKey(point)) {
				pathSectionHashtable.put(point, new Vector<PathSection>());
			}
			pathSectionHashtable.get(point).add(p2);
			pathSections.add(p1);
			pathSections.add(p2);
		}
		lastStepNumber = stepNumber;
		return p1;
	}

	private void walk(Point point, Point lastPoint) {
		// First figure out what directions we can walk
		stepNumber++;
		point.setInspecting();
		Point newLastPoint = lastPoint;
		Vector<Direction> walkableDirections = getWalkableDirections(point);
		if (getWalkableDirectionCount(point) > 2 && !point.isChoice()) {
			// We hit a choice point add the section if we haven't been here already
			addPathSection(point, lastPoint);
			newLastPoint = point;
			point.setIsChoice();
		}

		Iterator<Direction> i = walkableDirections.iterator();
		// Just to make sure we don't step on our tail

		while (i.hasNext()) {
			Point newPoint = point.copy();
			Direction dir = i.next();
			newPoint.step(dir);
			
			if (newPoint.isChoice()) {
				// We ran into our tail we have been here before
				addPathSection(newPoint, newLastPoint);
			} else {
				walk(newPoint, newLastPoint);
			}
		}
		point.setVisited();
	}

	private Vector<Direction> getWalkableDirections(Point point) {
		short ch = (short) (grid[point.x][point.y] & (short) 15);
		Vector<Direction> directions = new Vector<Direction>();

		// Check down
		if ((ch & 8) == 0 && (visited[(point.x + 1) % Board.NUMBLOCKS][point.y] >= 0))
			directions.add(new Direction(1, 0));
		// Check up
		if ((ch & 2) == 0 && (visited[(point.x - 1 + Board.NUMBLOCKS) % Board.NUMBLOCKS][point.y] >= 0))
			directions.add(new Direction(-1, 0));
		// Check right
		if ((ch & 4) == 0 && (visited[point.x][(point.y + 1) % Board.NUMBLOCKS] >= 0))
			directions.add(new Direction(0, 1));
		// Check left
		if ((ch & 1) == 0 && (visited[point.x][(point.y - 1 + Board.NUMBLOCKS) % Board.NUMBLOCKS] >= 0))
			directions.add(new Direction(0, -1));
		return directions;
	}

	private int getWalkableDirectionCount(Point point) {
		short ch = (short) (grid[point.x][point.y] & (short) 15);
		int directionCount = 0;

		// Check down
		if ((ch & 8) == 0)
			directionCount++;
		// Check up
		if ((ch & 2) == 0)
			directionCount++;
		// Check right
		if ((ch & 4) == 0)
			directionCount++;
		// Check left
		if ((ch & 1) == 0)
			directionCount++;
		;
		return directionCount;
	}

}
