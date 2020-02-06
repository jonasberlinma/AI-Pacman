package edu.ucsb.cs56.projects.games.pacman.player;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Random;

import edu.ucsb.cs56.projects.games.pacman.Direction;
import edu.ucsb.cs56.projects.games.pacman.Grid;
import edu.ucsb.cs56.projects.games.pacman.GridWalker;
import edu.ucsb.cs56.projects.games.pacman.PathSection;
import edu.ucsb.cs56.projects.games.pacman.PivotField;
import edu.ucsb.cs56.projects.games.pacman.Point;
import edu.ucsb.cs56.projects.games.pacman.common.DataEvent;
import edu.ucsb.cs56.projects.games.pacman.common.DataObservation;
import edu.ucsb.cs56.projects.games.pacman.model.AIModel;
import edu.ucsb.cs56.projects.games.pacman.model.DataFlipper;

public class AIPlayerLearner extends AIPlayer {

	Random random = new Random(System.currentTimeMillis());

	private int lastScore;
	private RewardCalculator rc = null;
	private DataFlipper df = null;
	private ArrayList<DataEvent> eventHistory;
	public AIModel model = null;
	private int accept = 0;
	private int iterations = 0;
	private double lastBestReward = 0;

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
				rc = new RewardCalculator(dataEvent.getGameID(), 100, 0.99d);
				break;
			default:
			}
			break;

		case MOVE:
			// Move event something on the board changed
			String playerType = dataEvent.getString("playerType");

			if (playerType != null) {
				String playerTypeShort = playerType.substring(0, 5);
				switch (playerTypeShort) {

				case "GHOST":
					// Ghost moved, just keep an eye on it
					eventHistory.add(dataEvent);
					break;
				case "PACMA":
					// This is the main decision point where we use the model to figure out what to
					// do
					// Idea is:
					// 1. Figure out the possible directions we can go (just avoid running into
					// walls)
					// 2. If we have no model just pick a random direction of the possible ones
					// 3. If we have a model, the for each possible direction
					// - Use the current state and perturb it into each of the possible states we
					// can go to
					// - Score each possible next possible next state
					// - Find the highest scoring next possible state

					iterations++;

					// Collect the event so we have complete history
					eventHistory.add(dataEvent);

					// Get basic info
					int myX = dataEvent.getInt("x");
					int myY = dataEvent.getInt("y");
					// Find the possible directions we can go from where we are
					HashSet<PathSection> ps = gridWalker.getPossiblePaths(new Point(myX, myY));
					ArrayList<Direction> possibleDirections = new ArrayList<Direction>();
					for (PathSection p : ps) {
						possibleDirections.add(p.getDirection());
					}

					// Prep the data converting from game events to complete state observations
					DataObservation observation = df.getObservation(eventHistory);

					// Get a new suggested direction
					// Find the best possible direction
					Direction selectedDirection = null;
					double bestReward = -Double.MAX_VALUE;

					// Do we have a model or not
					if (model != null) {
						// Yes we have a model
						for (int i = 0; i < possibleDirections.size(); i++) {
							Direction proposedDirection = possibleDirections.get(i);

							// Add the proposed direction to the observation

							DataObservation proposedState = perturbState(observation, proposedDirection);

							// Score
							double predictedReward = model.score(proposedState);

							// Pick out the best one
							if (predictedReward > bestReward) {
								bestReward = predictedReward;
								selectedDirection = proposedDirection;
							}
						}
					} else {
						// No model just pick a direction
						selectedDirection = possibleDirections.get(random.nextInt(possibleDirections.size()));
					}

					// The score is normalized to 0 with a standard deviation of 1

					double gamma = 0.001;
					double alpha = (bestReward - lastBestReward) / gamma;
					lastBestReward = bestReward;
					double randomNumber = random.nextDouble();
					alpha = 0.9;
					if (alpha < randomNumber) {
						// If there is no big difference shake things up
						selectedDirection = possibleDirections.get(random.nextInt(possibleDirections.size()));
					} else {
						// We accepted the models decision
						accept++;
					}

					// Now press the key for the direction we selected
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

					rc.addScore(gameStep - 1, score - lastScore);
					lastScore = score;
					eventHistory.clear();
					break;
				default:
				} // Done with Pacman move
			} // Done with player type not null
		default:
		} // Done with event type
	}

	private DataObservation perturbState(DataObservation observation, Direction proposedDirection) {
		DataObservation perturbedState = observation.deepClone();
//		observation.put("KEY_PRESSkey", df.standardizeValue(proposedDirection.toString()));
		// Score the proposed change

		Direction ghost0Direction = Direction.parseDirection(observation.get("MOVE0direction"));
		int perturbation = 2;
		if (ghost0Direction != null) {
			int distance = Integer.parseInt(observation.get("MOVE0distance"));

			if (ghost0Direction.isSame(proposedDirection)) {
				perturbedState.put("MOVE0distance", "" + (distance - perturbation));
			} else {
				perturbedState.put("MOVE0distance", "" + (distance + perturbation));
			}
		}

		Direction ghost1Direction = Direction.parseDirection(observation.get("MOVE1direction"));
		if (ghost1Direction != null) {
			int distance = Integer.parseInt(observation.get("MOVE1distance"));

			if (ghost1Direction.isSame(proposedDirection)) {
				perturbedState.put("MOVE1distance", "" + (distance - perturbation));
			} else {
				perturbedState.put("MOVE1distance", "" + (distance + perturbation));
			}
		}
		
		Direction ghost2Direction = Direction.parseDirection(observation.get("MOVE2direction"));
		if (ghost2Direction != null) {
			int distance = Integer.parseInt(observation.get("MOVE2distance"));

			if (ghost2Direction.isSame(proposedDirection)) {
				perturbedState.put("MOVE2distance", "" + (distance - perturbation));
			} else {
				perturbedState.put("MOVE2distance", "" + (distance + perturbation));
			}
		}

		Direction pelletDirection = Direction.parseDirection(observation.get("MOVE99pelletDirection"));
		if (pelletDirection != null) {
			int distance = Integer.parseInt(observation.get("MOVE99pelletDistance"));

			if (pelletDirection.isSame(proposedDirection)) {
				perturbedState.put("MOVE99pelletDistance", "" + (distance - perturbation));
			} else {
				perturbedState.put("MOVE99pelletDistance", "" + (distance + perturbation));
			}
		}

		Direction fruitDirection = Direction.parseDirection(observation.get("MOVE99fruitDirection"));
		if (fruitDirection != null) {
			int distance = Integer.parseInt(observation.get("MOVE99fruitDistance"));

			if (fruitDirection.isSame(proposedDirection)) {
				perturbedState.put("MOVE99fruitDistance", "" + (distance - perturbation));
			} else {
				perturbedState.put("MOVE99fruitDistance", "" + (distance + perturbation));
			}
		}

		return perturbedState;
	}

	@Override
	public void newModel(AIModel aiModel) {
		model = aiModel;

	}

	@Override
	public LinkedHashMap<String, DataObservation> reportExperience() {
		return rc.getRewardHistory();
	}
}
