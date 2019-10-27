package edu.ucsb.cs56.projects.games.pacman;

import java.util.Random;
import java.util.Vector;

public class AIModelRandom extends AIModel {

	private Random random =null;
	
	AIModelRandom(long seed){
		random = new Random(seed);
	}
	@Override
	double score(DataObservation observation) {
		return random.nextDouble();
	}

	@Override
	void setDataObservations(Vector<DataObservation> observations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void train() {
		// TODO Auto-generated method stub
		
	}

}
