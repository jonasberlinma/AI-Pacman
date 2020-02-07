package edu.ucsb.cs56.projects.games.pacman.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import edu.ucsb.cs56.projects.games.pacman.common.DataGameResult;

public abstract class AIModelTrainer {

	private Channel channel;
	private String modelQueueName;
	private String gameQueueName;
	
	private long lastTrainTime = 0;

	private String eventConsumerTag;

	byte[] eventHistory;

	protected AIModelTrainer() {
	}

	public void setController(Channel channel, String gameQueueName, String modelQueueName) {
		this.channel = channel;
		this.gameQueueName = gameQueueName;
		this.modelQueueName = modelQueueName;
	}

	public void start() {
		// Connect to the event queue 
		DeliverCallback processGame = (consumerTag, delivery) -> {
			eventHistory = delivery.getBody();
			processMessage(eventHistory);
		};
		CancelCallback cancelCallback = (consumerTag) -> {

		};
		try {
			// Auto ack messages
			eventConsumerTag = channel.basicConsume(gameQueueName, true, processGame, cancelCallback);
		} catch (IOException e) {
			System.err.println("Faled to consume event queue");
			e.printStackTrace();
		}
		
		// Connect to the model queue
		
	}

	private void processMessage(byte[] eventHistory) {
		ByteArrayInputStream bis = new ByteArrayInputStream(eventHistory);

		ObjectInputStream ois;
		DataGameResult gameResult = null;
		try {
			ois = new ObjectInputStream(bis);
			gameResult = (DataGameResult) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} 

		gameCompleteEvent(gameResult);
		if(System.currentTimeMillis() - lastTrainTime > 1000) {
			doTrain();
			lastTrainTime = System.currentTimeMillis();
		}
	}

	protected void stop() {
		try {
			channel.basicCancel(eventConsumerTag);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Different types of model trainer implementations should implement this method
	 * to get data from completed games
	 * 
	 * 
	 */
	protected abstract void gameCompleteEvent(DataGameResult gameEventLog);

	/**
	 * Train model
	 */
	protected abstract void doTrain();

	/**
	 * Called by the model trainer implementations to report that a new model is
	 * completed so the controller can start using it
	 * 
	 * @param newModel
	 */
	protected void setNewModel(byte[] newModelSerial) {
        try {
			channel.basicPublish("", modelQueueName, null, newModelSerial);
		} catch (IOException e) {
			System.err.println("Failed to send new model to queue");
			e.printStackTrace();
		}
	}

}
