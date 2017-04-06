package edu.ucsb.cs56.projects.games.pacman;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.Random;

public class AIPlayerRandom extends AIPlayer {

	Random random = new Random(System.currentTimeMillis());
	int numSteps = 1;

	public AIPlayerRandom() throws FileNotFoundException {
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
			// Press ESCAPE to quit the game and then stop
			pressKey(KeyEvent.VK_ESCAPE);
			stop();
			break;
		case KEY_PRESS:
		case KEY_RELEASE:
			break;

		default:
			numSteps--;
			if (numSteps == 0) {
				int randomKey = random.nextInt(4);
				numSteps = random.nextInt(150) + 1;

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
			}
		}
	}

	@Override
	protected void newModel(AIModel aiModel) {
		// Don't use trained models for random player
		
	}
}
