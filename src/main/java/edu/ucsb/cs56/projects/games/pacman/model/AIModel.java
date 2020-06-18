package edu.ucsb.cs56.projects.games.pacman.model;

import java.util.ArrayList;

import edu.ucsb.cs56.projects.games.pacman.common.DataObservation;

public abstract class AIModel {

	public abstract void initialize();
	/**
	 * Override to score an observation
	 * 
	 * @return
	 */
	public abstract double score(DataObservation observation);

	/**
	 * Called by the model trainer to report a new DataObservations from a completed
	 * game
	 * 
	 * @param observations
	 */
	abstract void setDataObservations(ArrayList<DataObservation> observations);

	/**
	 * Called by the model trainer to tell the model to train itself
	 */
	abstract void train();

	/**
	 * Called by the model trainer to tell the model to test itself
	 */
	abstract void test();

	/**
	 * Called by the model trainer to tell the model to package itself into a byte
	 * array for shipping
	 * 
	 * @return
	 */
	abstract byte[] toBytes();
	
	/**
	 * Called by the CameController to rehydrate a model
	 * 
	 */
	
	public abstract void loadModel(byte[] modelBytes);
	
}
