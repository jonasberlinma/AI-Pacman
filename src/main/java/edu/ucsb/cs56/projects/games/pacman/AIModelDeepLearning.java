package edu.ucsb.cs56.projects.games.pacman;

import java.util.Random;
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

public class AIModelDeepLearning extends AIModel {
	private Long modelID = 0l;
	private int numInputs = 10;
	private int numHiddenNodes = 10;
	private int numOutputs = 4;
	private double learningRate = 0.01;
	private Random random = new Random();
	Vector<DataObservation> observations = null;
	AIModelDeepLearning() {
		modelID = System.currentTimeMillis();
		// Commented out since the library is broken right now
		
		//MultiLayerNetwork network = null;
		//MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(123).weightInit(WeightInit.XAVIER)
		//		.updater(new Nesterovs(learningRate, 0.9)).list()
		//		.layer(new DropoutLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).activation(Activation.TANH)
		//				.build())
		//		.layer(new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes).activation(Activation.TANH)
		//				.build())
		//		.layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY)
		//				.nIn(numHiddenNodes).nOut(numOutputs).build())
		//		.build();
		//network = new MultiLayerNetwork(conf);
		//network.init();
	}

	public long getModelID() {
		return this.modelID;
	}
	@Override
	double score(DataObservation observation) {
		// This will call the NN for scoring
		// TODO: Remember to do the same data prep as in training
		return random.nextDouble();
	}

	@Override
	void setDataObservations(Vector<DataObservation> observations) {
		this.observations = observations;
		// If this model requires some specific data processing/normalization it can be put here
	}

	@Override
	void train() {
		System.out.println("Training Deep Learning model");
		// Call NN training
	}
}
