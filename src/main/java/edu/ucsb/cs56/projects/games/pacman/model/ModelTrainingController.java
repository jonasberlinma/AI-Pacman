package edu.ucsb.cs56.projects.games.pacman.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ModelTrainingController {

	private static final String MODEL_QUEUE = "model";
	private static final String GAME_QUEUE = "game";
	private static Connection connection;
	private static Channel channel;
	private static String aiModelTrainerClassName;
	private static AIModelTrainer aiModelTrainer;

	public static void main(String[] args) {
		String host = "localhost";
		String username = null;
		String password = null;
		boolean verbose = false;

		Iterator<String> argi = new Vector<String>(Arrays.asList(args)).iterator();

		while (argi.hasNext()) {
			String theArg = argi.next();
			switch (theArg) {
			case "-host":
				host = argi.next();
				break;
			case "-verbose":
				verbose = true;
				break;
			case "-aiModelTrainerClassName":
				aiModelTrainerClassName = argi.next();
				break;
			case "-username":
				username = argi.next();
				break;
			case "-password":
				password = argi.next();
				break;
			default:
				System.out.println("Invalid command Line argument" + theArg);
				System.exit(1);
			}
		}

		// First create RabbitMQ connection, channel, and queues
		//
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setUsername(username);
		factory.setPassword(password);
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(MODEL_QUEUE, false, false, false, null);
			channel.queueDeclare(GAME_QUEUE, false, false, false, null);
		} catch (IOException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		// Now create create the AIModelTrainer of the specified kind and hand the queue handles
		aiModelTrainer = loadTrainer();
		// Finally start the model trainer
		aiModelTrainer.start();
		
	}
	private static AIModelTrainer loadTrainer() {
		System.out.println("Loading trainer");
		AIModelTrainer aiModelTrainer = null;
		try {
			Class<?> theClass = Class.forName(aiModelTrainerClassName);
			aiModelTrainer = (AIModelTrainer) theClass.newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			System.err.println("Failed to load trainer");
			e.printStackTrace();
		}
		aiModelTrainer.setController(channel, GAME_QUEUE, MODEL_QUEUE);
		return aiModelTrainer;
	}
}
