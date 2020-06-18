package edu.ucsb.cs56.projects.games.pacman.model;

import java.util.ArrayList;
import java.util.Random;

import edu.ucsb.cs56.projects.games.pacman.common.DataObservation;

public class AIModelRandom extends AIModel {

	private Random random = null;

	AIModelRandom(long seed) {
		random = new Random(seed);
	}

	@Override
	public double score(DataObservation observation) {
		return random.nextDouble();
	}

	@Override
	void setDataObservations(ArrayList<DataObservation> observations) {

	}

	@Override
	void train() {

	}

	@Override
	void test() {

	}
	
	@Override
	byte[] toBytes() {
		return "".getBytes();
	}

	@Override
	public void loadModel(byte[] modelBytes) {
		
	}

	@Override
	public void initialize() {
		
	}

}
