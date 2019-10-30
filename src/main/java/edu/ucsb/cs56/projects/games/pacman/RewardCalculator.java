package edu.ucsb.cs56.projects.games.pacman;

import java.io.PrintWriter;
import java.util.Vector;

public class RewardCalculator {

	private long gameID;
	private int size;
	private double discountRate;

	class Measure {
		long gameStep;
		int score;
		double reward;

		protected Measure(long gameStep, int score) {
			this.gameStep = gameStep;
			this.score = score;
		}
	}

	private Vector<Measure> measures = new Vector<Measure>();
	private Vector<Measure> rewardHistory = new Vector<Measure>();

	RewardCalculator(long gameID, int size, double discountRate) {
		this.gameID = gameID;
		this.size = size;
		this.discountRate = discountRate;
	}

	public void addScore(long gameStep, int score) {
		if (measures.size() > 1) {
			rewardHistory.add(measures.get(0));
		}
		if (size == measures.size()) {
			measures.remove(0);
		}
		measures.add(new Measure(gameStep, score));

		double reward = 0;
		for (int i = 0; i < measures.size(); i++) {
			reward = reward + measures.get(i).score * Math.pow(discountRate, i);
			measures.get(0).reward = reward;
		}
	}
	private void finalizeRewards() {
		for(int j = 0; j < measures.size(); j++) {
			double reward = 0;
			for (int i = j; i < measures.size(); i++) {
				reward = reward + measures.get(i).score * Math.pow(discountRate, i - j);
				measures.get(j).reward = reward;
			}
			rewardHistory.add(measures.get(j));
		}
	}
	public void reportRewards(PrintWriter out) {
		finalizeRewards();
		for (Measure measure : rewardHistory) {
			out.println("" + gameID + "," + measure.gameStep + "," + measure.score + "," + measure.reward);
		}
	}
	public Vector<Measure> getRewardHistory(){
		return this.rewardHistory;
	}
}
