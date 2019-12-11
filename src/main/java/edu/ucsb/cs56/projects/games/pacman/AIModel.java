package edu.ucsb.cs56.projects.games.pacman;

import java.util.ArrayList;

public abstract class AIModel {

	/**
	 * Override to score an observation
	 * 
	 * @return
	 */
	abstract double score(DataObservation observation);

	abstract void setDataObservations(ArrayList<DataObservation> observations);

	abstract void train();
	
	abstract void test();
}
