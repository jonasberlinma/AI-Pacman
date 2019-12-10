package edu.ucsb.cs56.projects.games.pacman;

import java.io.File;
import java.io.PrintStream;
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
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class AIModelDeepLearning extends AIModel {

//	private final String[] theVariables = { "MOVE99x", "MOVE99y", "MOVE99dx", "MOVE99dy", "MOVE99reqdx", "MOVE99reqdy",
//			"MOVE99speed", "MOVE99edible", "MOVE99UP", "MOVE99DOWN", "MOVE99LEFT", "MOVE99RIGHT", "KEY_RELEASEkey",
//			"KEY_PRESSkey", "MOVE0x", "MOVE0y", "MOVE0dx", "MOVE0dy", "MOVE0reqdx", "MOVE0reqdy", "MOVE0speed",
//			"MOVE0edible", "MOVE0distance", "MOVE0direction", "MOVE1x",
//
//			"MOVE1y", "MOVE1dx", "MOVE1dy", "MOVE1reqdx", "MOVE1reqdy", "MOVE1speed", "MOVE1edible", "MOVE1distance",
//			"MOVE1direction", "MOVE2x", "MOVE2y", "MOVE2dx", "MOVE2dy", "MOVE2reqdx", "MOVE2reqdy", "MOVE2speed",
//			"MOVE2edible", "MOVE2distance", "MOVE2direction" };

//	private final String[] theVariables = { "MOVE0distance", "MOVE0direction", "MOVE1distance", "MOVE1direction",
//			"MOVE2distance", "MOVE2direction", "KEY_PRESSkey" };

	private final String[] theVariables = { "MOVE0distance", "MOVE0direction", "MOVE99pelletDirection", "MOVE99pelletDistance", "KEY_PRESSkey" };

	private Long modelID = 0l;
	private int numInputs = theVariables.length;
	private int numHiddenNodes = 20;
	private int numOutputs = 1;
	private double learningRate = 0.02;
	private int nEpochs = 20;
	private Vector<DataObservation> observations = null;
	private MultiLayerNetwork network = null;
	private NormalizerStandardize ns = null;

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
		double[][] dep =new double[1][1];
		DataSet ds = new DataSet(Nd4j.create(indep), Nd4j.create(dep));
		ns.transform(ds);
		double theScore = network.output(ds.getFeatures(), false).getDouble(0, 0);
		// System.out.println("Score " + theScore);
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
		test();
		System.out.println("Training Deep Learning model");

		// Call NN training

		DataSetIterator iterator = getTrainingData();
		for (int i = 0; i < nEpochs; i++) {
			iterator.reset();
			network.fit(iterator);
			iterator.reset();
		}
		System.out.println("Iteration count " + network.getIterationCount());
	}

	@Override
	void test() {

		INDArray independentVariables = getIndependentVariables();
		INDArray dependentVariables = getDependentVariables();
		
		
		INDArray output = network.output(independentVariables);

		double error = dependentVariables.distance2(output.castTo(DataType.DOUBLE))
				/ Math.sqrt((double) observations.size());

		PrintStream out = null;
		try {
			out = new PrintStream(new File("theData.csv"));
			printData(out, independentVariables, dependentVariables, output);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
		System.out.println("RMS error: " + error);

	}

	private void printData(PrintStream out, INDArray independentVariables, INDArray dependentVariables,
			INDArray output) {
		for (int i = 0; i < theVariables.length; i++) {
			out.print(theVariables[i] + ",");
		}
		out.println("Actual,Predicted");
		for (int i = 0; i < output.rows(); i++) {
			for (int j = 0; j < independentVariables.columns(); j++) {
				out.print(independentVariables.getDouble(i, j) + ",");
			}
			out.println(dependentVariables.getDouble(i, 0) + "," + output.getDouble(i, 0));
		}
	}

	private DataSetIterator getTrainingData() {

		INDArray gameInfo = getIndependentVariables();

		INDArray scores = getDependentVariables();

		System.out.println("Independent variables" + gameInfo.rows() + "x" + gameInfo.columns());
		System.out.println("Target variables" + scores.rows() + "x" + scores.columns());

		DataSet ds = new DataSet(gameInfo, scores);
		
		ns = new NormalizerStandardize();
		ns.fitLabel(true);

		ns.fit(ds);
		
		ns.transform(ds);
		
		ds.save(new File("trainingdata.txt"));

		List<DataSet> list = ds.asList();
		return new ListDataSetIterator<>(list, 200);
	}

	private INDArray getIndependentVariables() {
		// Pull out the values

		double[][] theData = new double[observations.size()][];
		for (int i = 0; i < observations.size(); i++) {

			double[] tmpRow = getObservation(observations.get(i));
			theData[i] = tmpRow;
		}

		INDArray gameInfo = Nd4j.create(theData);
		return gameInfo;
	}

	private INDArray getDependentVariables() {
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
		return scores;
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
