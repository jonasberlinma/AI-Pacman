package edu.ucsb.cs56.projects.games.pacman;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
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

	private final String[] theVariables = { "MOVE99x", "MOVE99y", "MOVE99dx", "MOVE99dy", "MOVE99reqdx", "MOVE99reqdy",
			"MOVE99speed", "MOVE99edible", "MOVE99UP", "MOVE99DOWN", "MOVE99LEFT", "MOVE99RIGHT", "KEY_RELEASEkey",
			"KEY_PRESSkey", "MOVE0x", "MOVE0y", "MOVE0dx", "MOVE0dy", "MOVE0reqdx", "MOVE0reqdy", "MOVE0speed",
			"MOVE0edible", "MOVE0distance", "MOVE0direction", "MOVE1x",

			"MOVE1y", "MOVE1dx", "MOVE1dy", "MOVE1reqdx", "MOVE1reqdy", "MOVE1speed", "MOVE1edible", "MOVE1distance",
			"MOVE1direction", "MOVE2x", "MOVE2y", "MOVE2dx", "MOVE2dy", "MOVE2reqdx", "MOVE2reqdy", "MOVE2speed",
			"MOVE2edible", "MOVE2distance", "MOVE2direction" };

	private Long modelID = 0l;
	private int numInputs = theVariables.length;
	private int numHiddenNodes = 50;
	private int numOutputs = 1;
	private double learningRate = 0.01;
	private int nEpochs = 20;
	private Vector<DataObservation> observations = null;
	private MultiLayerNetwork network = null;

	AIModelDeepLearning() {
		modelID = System.currentTimeMillis();

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(123).weightInit(WeightInit.XAVIER)
				.updater(new Nesterovs(learningRate, 0.9)).list()
				.layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).activation(Activation.TANH).build())
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
		double[][] indep = new double[1][theVariables.length];
		indep[0] = getObservation(observation);
		double theScore = network.output(Nd4j.create(indep), false).getDouble(0,0);
		//System.out.println("Score " + theScore);
		return theScore;
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
		network.setListeners(new ScoreIterationListener(1));
		DataSetIterator iterator = getTrainingData(observations);
		for (int i = 0; i < nEpochs; i++) {
			iterator.reset();
			network.fit(iterator);
			iterator.reset();
			INDArray output = network.output(iterator);
			System.out.println("Out " + output.toString());
			
		}
		System.out.println("Iteration count " + network.getIterationCount());
	}

	private DataSetIterator getTrainingData(Vector<DataObservation> observations) {
		// Create the data frame

		// Pull out the values

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

		//ds.normalize();
		ds.save(new File("trainingdata.txt"));

		List<DataSet> list = ds.asList();
		return new ListDataSetIterator<>(list, 200);
	}

	// Pick out the desired independent variables from the list and create a double
	// vector
	private double[] getObservation(DataObservation dataObservation) {

		double[] theRow = new double[theVariables.length];
		for (int i = 0; i < theVariables.length; i++) {
			String stringValue = dataObservation.get(theVariables[i]);
			//System.out.println("" + theVariables[i] + "="+stringValue);
			if (stringValue != null) {
				theRow[i] = Double.parseDouble(stringValue);
			} else {
				theRow[i] = 0;
			}
		}
		return theRow;
	}
}
