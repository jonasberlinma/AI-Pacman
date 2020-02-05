package edu.ucsb.cs56.projects.games.pacman;

import java.io.PrintStream;
import java.util.HashSet;

public class Point {

	public int x, y;
	String nodeNumber = "";
	// private int distance;

	Point(int x, int y) {
		this.x = x;
		this.y = y;
		nodeNumber = x + "-" + y;
	}
	
	Point(){
		
	}

	Point stepRight() {
		Point newPoint = new Point((x + 1) % Board.NUMBLOCKS, y);
		return newPoint;
	}

	Point stepDown() {
		Point newPoint = new Point(x, (y + 1) % Board.NUMBLOCKS);
		return newPoint;
	}

	public String getNodeNumber() {
		return nodeNumber;
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

	public boolean checkReachable(String type, HashSet<Point> reachablePoints) {
		boolean reachable = false;
		if (!reachablePoints.contains(this)) {
			System.err.println(type + " point not found " + x + "-" + y);
		} else {
			reachable = true;
		}
		return reachable;
	}

	boolean hasPellet(short[][] screenData) {
		// return (screenData[y][x] & 16) != 0 || (screenData[y][x] & 32) !=
		// 0 || (screenData[y][x] & 64) != 0;
		return (screenData[y][x] & 16) != 0;
	}

	boolean hasPill(short[][] screenData) {
		return (screenData[y][x] & 64) != 0;
	}

	boolean hasFruit(short[][] screenData) {
		return (screenData[y][x] & 32) != 0;
	}

}
