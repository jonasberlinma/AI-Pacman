package edu.ucsb.cs56.projects.games.pacman;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.DropoutLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class AIModelDeepLearning extends AIModel {
	
	private final String[] theVariables = { "gameStep", "time" };
	
	private Long modelID = 0l;
	private int numInputs = theVariables.length;
	private int numHiddenNodes = 10;
	private int numOutputs = 1;
	private double learningRate = 0.01;
	private int nEpochs = 200;
	private Random random = new Random();
	private Vector<DataObservation> observations = null;
	private MultiLayerNetwork network = null;
	
	
	AIModelDeepLearning() {
		modelID = System.currentTimeMillis();

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(123).weightInit(WeightInit.XAVIER)
				.updater(new Nesterovs(learningRate, 0.9)).list()
				.layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).activation(Activation.TANH)
						.build())
				.layer(new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes).activation(Activation.TANH)
						.build())
				.layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY)
						.nIn(numHiddenNodes).nOut(numOutputs).build())
				.build();
		network = new MultiLayerNetwork(conf);
		network.init();
		network.setListeners(new ScoreIterationListener(1));
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
		// If this model requires some specific data processing/normalization it can be
		// put here
	}

	@Override
	void train() {
		System.out.println("Training Deep Learning model");

		// Call NN training
		DataSetIterator iterator = getTrainingData(observations);
		for (int i = 0; i < nEpochs; i++) {
			iterator.reset();
			network.fit(iterator);
		}
	}

	private DataSetIterator getTrainingData(Vector<DataObservation> observations) {
		// Create the data frame

		// Pull out the values
		
		System.out.println("Data " + observations.toString());
		System.exit(1);
		double[][] theData = new double[observations.size()][];
		for (int i = 0; i < observations.size(); i++) {

			double[] tmpRow = getObservation(observations.get(i));
			theData[i] = tmpRow;
		}

		INDArray gameInfo = Nd4j.create(theData);

		double[][] theTarget = new double[observations.size()][1];

		for (int i = 0; i < observations.size(); i++) {
			String rewardString = observations.get(i).get("reward");
			if (rewardString != null) {
				theTarget[i][0] = Double.parseDouble(rewardString);
			} else {
				theTarget[i][0] = 0;
			}
		}

		INDArray scores = Nd4j.create(theTarget);

		System.out.println("Independent variables" + gameInfo.rows() + "x" + gameInfo.columns());
		System.out.println("Target variables" + scores.rows() + "x" + scores.columns());

		DataSet ds = new DataSet(gameInfo, scores);

		System.out.println("Examples " + ds.numExamples());
		System.out.println("Outcomes " + ds.numOutcomes());
		System.out.println("Inputs " + ds.numInputs());

		List<DataSet> list = ds.asList();
		return new ListDataSetIterator<>(list, 100);
	}

	private double[] getObservation(DataObservation dataObservation) {


		double[] theRow = new double[theVariables.length];
		for (int i = 0; i < theVariables.length; i++) {
			theRow[i] = Double.parseDouble(dataObservation.get(theVariables[i]));
		}
		return theRow;
	}
}
