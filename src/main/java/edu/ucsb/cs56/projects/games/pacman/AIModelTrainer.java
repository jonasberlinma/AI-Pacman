package edu.ucsb.cs56.projects.games.pacman;

import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class AIModelTrainer implements Runnable {

	private Thread trainingThread = null;
	private boolean doRun = false;
	private ArrayBlockingQueue<Vector<DataEvent>> eventQueue = null;
	private GameController gameController = null;

	protected AIModelTrainer() {
		trainingThread = new Thread(this, "Training thread");
	}

	protected void setController(GameController gameController, ArrayBlockingQueue<Vector<DataEvent>> eventQueue) {
		this.eventQueue = eventQueue;
		this.gameController = gameController;
	}

	protected void start() {
		doRun = true;
		trainingThread.start();
	}

	@Override
	public void run() {
		while (doRun) {
			try {
				// Pull the next completed game off the queue and hand it to the
				// trainer
				gameCompleteEvent(eventQueue.take());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void stop() {
		doRun = false;
		try {
			trainingThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Different types of model trainer implementations should implement this
	 * method to get data from completed games
	 * 
	 * 
	 */
	protected abstract void gameCompleteEvent(Vector<DataEvent> gameEventLog);

	/**
	 * Called by the model trainer implementations to report that a new model is
	 * completed so the controller can start using it
	 * 
	 * @param newModel
	 */
	protected void setNewModel(AIModel newModel) {
		gameController.setNewModel(newModel);
	}

}
