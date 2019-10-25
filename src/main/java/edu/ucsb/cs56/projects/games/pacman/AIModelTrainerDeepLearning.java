package edu.ucsb.cs56.projects.games.pacman;

import java.util.LinkedHashMap;
import java.util.Vector;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.DropoutLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class AIModelTrainerDeepLearning extends AIModelTrainer {

	DataFlipper dataFlipper = null;


	AIModelTrainerDeepLearning() {
		dataFlipper = new DataFlipper();
		dataFlipper.addPivotField(new PivotField("gameStep", 0));
		dataFlipper.addPivotField(new PivotField("eventType", 1));
		dataFlipper.addPivotField(new PivotField("ghostNum", 2));

	}

	@Override
	protected void gameCompleteEvent(Vector<DataEvent> gameEventLog) {

		System.out.println("Training on " + gameEventLog.size() + " events");
		// Prep the data

		Vector<DataObservation> observations = dataFlipper.findObservationsFromHistory(gameEventLog);

		// Train a new model

		AIModel model = new AIModelDeepLearning();
		// Make the new model available to the players
		this.setNewModel(model);
		System.out.println("Sending new model");
	}
}
