package edu.ucsb.cs56.projects.games.pacman;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Random;

import edu.ucsb.cs56.projects.games.pacman.GridWalker.Direction;
import edu.ucsb.cs56.projects.games.pacman.GridWalker.PathSection;

public class AIPlayerLearner extends AIPlayer {

	Random random = new Random(System.currentTimeMillis());

	private int lastScore;
	private RewardCalculator rc = null;
	private DataFlipper df = null;
	private ArrayList<DataEvent> eventHistory;
	public AIModel model = null;
	private int accept = 0;
	private int iterations = 0;

	public AIPlayerLearner() throws FileNotFoundException {
		lastScore = 0;
		df = new DataFlipper();
		df.addPivotField(new PivotField("gameStep", 0));
		df.addPivotField(new PivotField("eventType", 1));
		df.addPivotField(new PivotField("ghostNum", 2));

		eventHistory = new ArrayList<DataEvent>();
	}

	@Override
	public synchronized void dataEvent(Grid grid, DataEvent dataEvent) {
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
				rc = new RewardCalculator(dataEvent.getGameID(), 5, 0.4d);
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
					if (iterations++ % 8 == 0)
						return;
					// Get basic info
					int myX = dataEvent.getInt("x");
					int myY = dataEvent.getInt("y");
					HashSet<PathSection> ps = gridWalker.getPossiblePaths(gridWalker.new Point(myX, myY));
					ArrayList<Direction> possibleDirections = new ArrayList<Direction>();
					for (PathSection p : ps) {
						possibleDirections.add(p.getDirection());
					}

					eventHistory.add(dataEvent);

					// Prep the data
					DataObservation observation = df.getObservation(eventHistory);
					// Where are we
					// Get a new suggested direction
					// Find the best possible direction
					Direction selectedDirection = null;
					double bestReward = -Double.MAX_VALUE;
					//if (playerID == 0)
					//	System.out.println("Checking " + possibleDirections.size() + " directions");
					if (model != null) {
						for (int i = 0; i < possibleDirections.size(); i++) {
							Direction proposedDirection = possibleDirections.get(i);

							// Add the proposed direction to the observation

							DataObservation proposedState = perturbState(observation, proposedDirection);

							double predictedReward = model.score(proposedState);

					//		if (playerID == 0) {
					//			observation.dumpComparison(proposedState);
					//			System.out.println("Score for " + playerID + " " + proposedDirection.toString() + " "
					//					+ predictedReward);
					//		}
							if (predictedReward > bestReward) {
								bestReward = predictedReward;
								selectedDirection = proposedDirection;
							}
						}
					//	if (playerID == 0)
					//		System.out.println("Model for " + playerID + " predicts " + selectedDirection);
					} else {
						selectedDirection = possibleDirections.get(random.nextInt(possibleDirections.size()));
						if (playerID == 0)
							System.out.println("No model picked " + selectedDirection + " randomly");
					}
//					double alpha = lastPredictedReward != 0 ? predictedReward / lastPredictedReward : 1.0;
					// System.out.println("Alpha " + alpha + " predicted reward " + predictedReward
					// + " last predicted reward " + lastPredictedReward);

//					lastPredictedReward = predictedReward;
//					double randomNumber = random.nextDouble();

					double alpha = 1;
					double randomNumber = 0;

					if (alpha > randomNumber) {
						accept++;
						switch (selectedDirection) {
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

	private DataObservation perturbState(DataObservation observation, Direction proposedDirection) {
		DataObservation perturbedState = observation.deepClone();
		observation.put("KEY_PRESSkey", df.standardizeValue(proposedDirection.toString()));
		// Score the proposed change
		String ghostDirection = observation.get("MOVE0direction");
		if (ghostDirection != null
				&& ghostDirection.compareTo(df.standardizeValue(proposedDirection.toString())) == 0) {

			int distance = Integer.parseInt(observation.get("MOVE0distance")) - 1;
			perturbedState.put("MOVE0distance", "" + distance);
		}
		String pelletDirection = observation.get("MOVE99pelletDirection");
		if (pelletDirection != null
				&& pelletDirection.compareTo(df.standardizeValue(proposedDirection.toString())) == 0) {
			int distance = Integer.parseInt(observation.get("MOVE99pelletDistance")) - 1;

			perturbedState.put("MOVE99pelletDistance", "" + distance);
		}
		return perturbedState;
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
