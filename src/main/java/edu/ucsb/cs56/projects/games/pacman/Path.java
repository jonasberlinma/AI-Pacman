package edu.ucsb.cs56.projects.games.pacman;

import java.util.ArrayList;

import edu.ucsb.cs56.projects.games.pacman.GridWalker.Direction;

public class Path {
	private int distance;
	private boolean edible;
	ArrayList<PathSection> pathSections = null;

	Path(int distance, ArrayList<PathSection> pathSections) {
		this.distance = distance;
		this.pathSections = pathSections;
	}

	Path() {

	}

	public ArrayList<PathSection> getPathSections(){
		return pathSections;
	}
	
	public Direction getFirstDirection() {
		Direction ret = null;
		if (this.pathSections.size() > 0) {
			ret = this.pathSections.get(0).getDirection();
		}
		return ret;
	}

	public boolean isSameDirection(Path otherPath) {
		return this.getFirstDirection().equals(otherPath.getFirstDirection());
	}

	public int getDistance() {
		return distance;
	}

	public boolean getEdible() {
		return edible;
	}

	public void setEdible(boolean edible) {
		this.edible = edible;
	}
}

