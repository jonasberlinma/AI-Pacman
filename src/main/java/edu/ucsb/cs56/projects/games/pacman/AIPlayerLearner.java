package edu.ucsb.cs56.projects.games.pacman;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Vector;

import edu.ucsb.cs56.projects.games.pacman.GridWalker.Direction;
import edu.ucsb.cs56.projects.games.pacman.GridWalker.PathSection;

public class AIPlayerLearner extends AIPlayer {

	Random random = new Random(System.currentTimeMillis());

	private int lastScore;
	private RewardCalculator rc = null;
	private DataFlipper df = null;
	private Vector<DataEvent> eventHistory;
	public AIModel model = null;
	private double lastPredictedReward;
	private int accept = 0;
	private int iterations = 0;

	public AIPlayerLearner() throws FileNotFoundException {
		lastScore = 0;
		df = new DataFlipper();
		df.addPivotField(new PivotField("gameStep", 0));
		df.addPivotField(new PivotField("eventType", 1));
		df.addPivotField(new PivotField("ghostNum", 2));

		eventHistory = new Vector<DataEvent>();
	}

	@Override
	public void dataEvent(Grid grid, DataEvent dataEvent) {
		GridWalker gridWalker = grid.getGridWalker();

		switch (dataEvent.getEventType()) {

		case INTRO:
			// Let's press the start key
			pressKey(KeyEvent.VK_S);
			pressKey(KeyEvent.VK_DOWN);
			break;
		case GAME_OVER:
			// Have to add the last score
			int gameStep = dataEvent.getGameStep();
			int score = dataEvent.getInt("score");

			rc.addScore(gameStep, score - lastScore);
			try {
				PrintWriter out = new PrintWriter(new FileOutputStream("rewards.dat", true));
				rc.reportRewards(out);
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Acceptance rate " + (double) accept / iterations);
			// Press ESCAPE to quit the game and then stop
			pressKey(KeyEvent.VK_ESCAPE);
			stop();
			break;
		case KEY_PRESS:
			break;
		case KEY_RELEASE:
			String key = dataEvent.getString("key");
			switch (key) {
			case "S":
				// Have to put this one here since the gameID is not set until the game starts
				rc = new RewardCalculator(dataEvent.getGameID(), 20, 0.8d);
				break;
			default:
			}
			break;

		case MOVE:

			String playerType = dataEvent.getString("playerType");

			if (playerType != null) {
				String playerTypeShort = playerType.substring(0, 5);
				switch (playerTypeShort) {

				case "GHOST":

					eventHistory.add(dataEvent);
					break;
				case "PACMA":
					// Get basic info
					int myX = dataEvent.getInt("x");
					int myY = dataEvent.getInt("y");
					HashSet<PathSection> ps = gridWalker.getPossiblePaths(gridWalker.new Point(myX, myY));
					Vector<Direction> possibleDirections = new Vector<Direction>();
					for (PathSection p : ps) {
						possibleDirections.add(p.getDirection());
					}

					eventHistory.add(dataEvent);

					// Prep the data
					DataObservation observation = df.getObservation(eventHistory);
					int randomKey = random.nextInt(possibleDirections.size());
					// TODO: Add the proposed direction

					// Score the proposed change
					double predictedReward = 0;

					if (model != null) {
						predictedReward = model.score(observation);
					}

					double alpha = lastPredictedReward != 0 ? predictedReward / lastPredictedReward : 1.0;
					//System.out.println("Alpha " + alpha + " predicted reward " + predictedReward
					//		+ " last predicted reward " + lastPredictedReward);

					lastPredictedReward = predictedReward;
					double randomNumber = random.nextDouble();

					iterations++;
					if (alpha > randomNumber) {
						accept++;
						switch (possibleDirections.get(randomKey)) {
						case LEFT:
							pressKey(KeyEvent.VK_LEFT);
							break;
						case DOWN:
							pressKey(KeyEvent.VK_DOWN);
							break;
						case RIGHT:
							pressKey(KeyEvent.VK_RIGHT);
							break;
						case UP:
							pressKey(KeyEvent.VK_UP);
							break;
						default:
						}

						gameStep = dataEvent.getGameStep();
						score = dataEvent.getInt("score");

						rc.addScore(gameStep, score - lastScore);
						lastScore = score;
					}
					eventHistory.clear();
					break;
				default:
				}
			}
		default:

		}
	}

	@Override
	protected void newModel(AIModel aiModel) {
		model = aiModel;

	}

	@Override
	public LinkedHashMap<String, DataObservation> reportExperience() {
		return rc.getRewardHistory();
	}
}
