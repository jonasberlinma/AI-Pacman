package edu.ucsb.cs56.projects.games.pacman;

import java.util.ArrayList;
import java.util.Random;

public class AIModelRandom extends AIModel {

	private Random random = null;

	AIModelRandom(long seed) {
		random = new Random(seed);
	}

	@Override
	double score(DataObservation observation) {
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

}
