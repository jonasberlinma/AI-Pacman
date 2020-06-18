package edu.ucsb.cs56.projects.games.pacman;

import java.io.PrintStream;

public class PathSection {

	private Point fromPoint, toPoint;
	private int length;

	PathSection(Point fromPoint, Point toPoint, int length) {
		this.fromPoint = fromPoint;
		this.toPoint = toPoint;
		this.length = length;
	}
	
	PathSection(){
		
	}
	
	public Point getFromPoint() {
		return this.fromPoint;
	}
	public Point getToPoint() {
		return this.toPoint;
	}
	public int getLength() {
		return this.length;
	}

	public Direction getDirection() {
		Direction d = null;
		// This is for the periodic boundary conditions
		int xDistance = Math.abs(fromPoint.x - toPoint.x);
		int yDistance = Math.abs(fromPoint.y - toPoint.y);
		if (fromPoint.x < toPoint.x) {
			if (xDistance < 2) {
				d = Direction.RIGHT;
			} else {
				d = Direction.LEFT;
			}
		} else if (fromPoint.x > toPoint.x) {
			if (xDistance < 2) {
				d = Direction.LEFT;
			} else {
				d = Direction.RIGHT;
			}
		} else if (fromPoint.y < toPoint.y) {
			if (yDistance < 2) {
				d = Direction.DOWN;
			} else {
				d = Direction.UP;
			}
		} else if (fromPoint.y > toPoint.y) {
			if (yDistance < 2) {
				d = Direction.UP;
			} else {
				d = Direction.DOWN;
			}
		}
		return d;
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
