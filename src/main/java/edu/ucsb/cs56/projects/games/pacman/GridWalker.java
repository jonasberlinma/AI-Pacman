package edu.ucsb.cs56.projects.games.pacman;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import edu.ucsb.cs56.projects.games.pacman.Grid.Pair;

public class GridWalker {

	short[][] grid = null;
	short[][] visited = null;
	GridData level = null;
	short walkOrder = 1;
	
	Point lastPoint = null;
	
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

	class PathSection {
		Point fromPoint, toPoint;
		PathSection(Point fromPoint, Point toPoint){
			this.fromPoint = fromPoint;
			this.toPoint = toPoint;
		}
		void print(){
			System.out.println("Section");
			fromPoint.print();
			toPoint.print();
		}
	}

	class Point {
		int x, y;
		Vector<Point> steps = new Vector<Point>();

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		void step(Direction direction) {
			x = (x + direction.dx + Board.NUMBLOCKS) % Board.NUMBLOCKS;
			y = (y + direction.dy + Board.NUMBLOCKS) % Board.NUMBLOCKS;
		}
		Point copy() {
			return new Point(this.x, this.y);
		}
		void print() {
			System.out.println("x=" + x + "y=" + y);
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

		//printGrid();
		// Pick a starting point
		Point point = new Point(0, 0);
		// Walk the maze
		walk(point);
		//printWalkOrder();
		return new Hashtable<Pair, Integer>();

	}
	void printGrid(){
		for (int i = 0; i < Board.NUMBLOCKS; i++) {
			for (int j = 0; j < Board.NUMBLOCKS; j++) {
				short ch = (short) (grid[i][j] & (short) 15);
				String b = "   " + ch + "-" + getWalkableDirections(new Point(i, j)).size();
				b = b.substring(b.length() - 5, b.length());
				System.out.print(b);
				if ((ch & 8) == 0)
					System.out.print("Down ");
				if ((ch & 2) == 0)
					System.out.print("Up ");
				if ((ch & 4) == 0)
					System.out.print("Right ");
				if ((ch & 1) == 0)
					System.out.print("Left ");

			}
			System.out.println();
		}
	}

	void printWalkOrder() {
		for (int i = 0; i < Board.NUMBLOCKS; i++) {
			for (int j = 0; j < Board.NUMBLOCKS; j++) {
				String visit = "   " + visited[i][j];
				visit = visit.substring(visit.length() - 4, visit.length());
				System.out.print(visit);
			}
			System.out.println();
		}
	}

	private void setVisited(Point point) {
		visited[point.x][point.y] = walkOrder;
		walkOrder++;
	}
	private void setInspecting(Point point){
		visited[point.x][point.y] = -1;
	}

	private void addPathSection(Point point){
		if(lastPoint != null){
			PathSection p = new PathSection(lastPoint, point);
			pathSections.add(p);
		}
		lastPoint = point;
	}
	private void walk(Point point) {
		// First figure out what directions we can walk
		Vector<Direction> walkableDirections = getWalkableDirections(point);
		
		if(walkableDirections.size() > 1){
			addPathSection(point);
		}
		Iterator<Direction> i = walkableDirections.iterator();
		// Just to make sure we don't step on our tail
		setInspecting(point);
		while (i.hasNext()) {
			Point newPoint = point.copy();
			newPoint.step(i.next());
			walk(newPoint);
		}
		setVisited(point);

	}

	private Vector<Direction> getWalkableDirections(Point point) {
		short ch = (short) (grid[point.x][point.y] & (short) 15);
		Vector<Direction> directions = new Vector<Direction>();

		// Check down
		if ((ch & 8) == 0 && (visited[(point.x + 1) % Board.NUMBLOCKS][point.y] == 0))
			directions.add(new Direction(1, 0));
		// Check up
		if ((ch & 2) == 0 && (visited[(point.x - 1 + Board.NUMBLOCKS) % Board.NUMBLOCKS][point.y] == 0))
			directions.add(new Direction(-1, 0));
		// Check right
		if ((ch & 4) == 0 && (visited[point.x][(point.y + 1) % Board.NUMBLOCKS] == 0))
			directions.add(new Direction(0, 1));
		// Check left
		if ((ch & 1) == 0 && (visited[point.x][(point.y - 1 + Board.NUMBLOCKS) % Board.NUMBLOCKS] == 0))
			directions.add(new Direction(0, -1));
		return directions;
	}

}
