package edu.ucsb.cs56.projects.games.pacman.player;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import edu.ucsb.cs56.projects.games.pacman.common.DataObservation;

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

	private ArrayList<Measure> measures = new ArrayList<Measure>();
	private ArrayList<Measure> rewardHistory = new ArrayList<Measure>();

	public RewardCalculator(long gameID, int size, double discountRate) {
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
		double reward = 0;
		for (int i = 0; i < measures.size(); i++) {
			reward = reward + measures.get(i).score * Math.pow(discountRate, i);
			measures.get(0).reward = reward;
		}
		measures.add(new Measure(gameStep, score));
	}

	private void finalizeRewards() {
		for (int j = 0; j < measures.size(); j++) {
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

	public LinkedHashMap<String, DataObservation> getRewardHistory() {
		finalizeRewards();
		LinkedHashMap<String, DataObservation> result = new LinkedHashMap<String, DataObservation>();
		for (Measure measure : rewardHistory) {
			DataObservation obs = new DataObservation();
			String gameStepTmp = Long.valueOf(measure.gameStep).toString();
			obs.put("gameStep", gameStepTmp);
			obs.put("score", Integer.valueOf(measure.score).toString());
			obs.put("reward", Double.valueOf(measure.reward).toString());
			result.put(gameStepTmp, obs);
		}
		return result;
	}
}
