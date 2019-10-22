package edu.ucsb.cs56.projects.games.pacman;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Random;

public class AIPlayerLearner extends AIPlayer {

	Random random = new Random(System.currentTimeMillis());

	private int lastScore;
	private RewardCalculator rc = null;

	public AIPlayerLearner() throws FileNotFoundException {
		lastScore = 0;
	}

	@Override
	public void dataEvent(Grid grid, DataEvent dataEvent) {

		switch (dataEvent.eventType) {

		case INTRO:
			// Let's press the start key
			pressKey(KeyEvent.VK_S);
			pressKey(KeyEvent.VK_DOWN);
			break;
		case GAME_OVER:
			try {
				PrintWriter out = new PrintWriter(new FileOutputStream("rewards.dat", true));
				rc.reportRewards(out);
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
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

				case "PACMA":

					int randomKey = random.nextInt(4);

					switch (randomKey) {
					case 0:
						pressKey(KeyEvent.VK_LEFT);
						break;
					case 1:
						pressKey(KeyEvent.VK_DOWN);
						break;
					case 2:
						pressKey(KeyEvent.VK_RIGHT);
						break;
					case 3:
						pressKey(KeyEvent.VK_UP);
						break;
					default:
					}

					int gameStep = dataEvent.getGameStep();
					int score = dataEvent.getInt("score");

					rc.addScore(gameStep, score - lastScore);
					lastScore = score;
					break;
				default:
				}
			}
		default:

		}
	}

	@Override
	protected void newModel(AIModel aiModel) {
		// Don't use trained models for random player

	}
}
