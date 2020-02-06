package edu.ucsb.cs56.projects.games.pacman.model;

import java.util.ArrayList;

import edu.ucsb.cs56.projects.games.pacman.common.DataObservation;

public abstract class AIModel {

	/**
	 * Override to score an observation
	 * 
	 * @return
	 */
	public abstract double score(DataObservation observation);

	abstract void setDataObservations(ArrayList<DataObservation> observations);

	abstract void train();
	
	abstract void test();
}
