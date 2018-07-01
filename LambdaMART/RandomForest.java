import java.util.*;

public class RandomForest {
	public TrainingSample trainingSamples[];
	public TestSample testSamples[];
	public int leavesPerTree = 32;
	public int T = 500;
	public int minSamplesPerLeaves = 1;
	public int numOfFeatures = 46;
	public void bootstrap() {
		for (int kthCART = 0; kthCART < T; kthCART++) {
			int m = trainingSamples.length;
			TrainingSample randomSamples[] = new TrainingSample[m];
			for (int i = 0 ; i < m; i++) {
				int randomNum = (int)(Math.random() * trainingSamples.length);
				randomSamples[i] = trainingSamples[randomNum];
			}
			Node root = new Node();
			root.trainingSamples = trainingSamples;
			root.randomSamples = randomSamples;
			root.testSamples = testSamples;
			List<Node> nodes = new ArrayList<Node>();
			nodes.add(root);
			for (int i = 0, nextNode = 0; i < leavesPerTree - 1; i++, nextNode++) {
				if (nextNode >= nodes.size()) {
					System.out.println("We have problems here");
				} else if (nodes.get(nextNode).train(minSamplesPerLeaves, numOfFeatures) == true) {
					nodes.add(nodes.get(nextNode).left);
					nodes.get(nextNode).left = null;
					nodes.add(nodes.get(nextNode).right);
					nodes.get(nextNode).right = null;
				} else {
					i--;
				}
			}
			for (Node node : nodes) {
				if (node.feature == -1) {
					node.computeGamma();
					node.updateScore();
				}
			}
		}
		// for (TrainingSample sample : trainingSamples) {
		// 	sample.score /= T;
		// }
	}
}

class Node {
	public double threshold;
	public int feature = -1;
	public double gamma;
	public TrainingSample trainingSamples[];
	public TrainingSample randomSamples[];
	public TestSample testSamples[];
	public Node left;
	public Node right;
	public Boolean train(int minSamplesPerLeaves, int numOfFeatures) {
		int numOfSelectedFeatures = (int)(Math.log(numOfFeatures) / Math.log(2));
		double minimalSum = Double.MAX_VALUE;
		List<Integer> indices = new ArrayList<Integer>();
		int numOfLeft = 0, numOfRight = 0;
		//	randomly select features
		for (int i = 0; i < numOfSelectedFeatures; i++) {
			while (true) {
				int randomNum = (int)(Math.random() * numOfFeatures);
				if (indices.contains(randomNum)) {

				} else {
					indices.add(randomNum);
					break;
				}
			}
		}
		for (int i = 0; i < indices.size(); i++) {
			int f = indices.get(i);
			AuxiliaryNode[] auxiliaries = new AuxiliaryNode[randomSamples.length];
			for (int j = 0; j < randomSamples.length; j++) {
				auxiliaries[j] = new AuxiliaryNode();
				auxiliaries[j].value = randomSamples[j].feature[f];
				auxiliaries[j].label = randomSamples[j].label;
				//auxiliaries[j].label = j + 1;
			}
			Arrays.sort(auxiliaries, new MyComparator4());
			// for (int j = 0; j < auxiliaries.length; j++) {
			// 	System.out.println(auxiliaries[j].label);
			// }
			double sumOfLeft = 0, squareSumOfLeft = 0;
			double sumOfRight = 0, squareSumOfRight = 0;
			for (int j = 0; j < randomSamples.length; j++) {
				sumOfRight += auxiliaries[j].label;
				squareSumOfRight += auxiliaries[j].label * auxiliaries[j].label;
			}
			for (int j = 0; j < minSamplesPerLeaves - 1; j++) {
				sumOfLeft += auxiliaries[j].label;
				squareSumOfLeft += auxiliaries[j].label * auxiliaries[j].label;
			}
			for (int j = minSamplesPerLeaves - 1; j < randomSamples.length - minSamplesPerLeaves; j++) {
				if (auxiliaries[j].value == auxiliaries[j+1].value) {
					sumOfLeft += auxiliaries[j].label;
					sumOfRight -= auxiliaries[j].label;
					squareSumOfLeft += auxiliaries[j].label * auxiliaries[j].label;
					squareSumOfRight -= auxiliaries[j].label * auxiliaries[j].label;
					continue;
				}
				sumOfLeft += auxiliaries[j].label;
				sumOfRight -= auxiliaries[j].label;
				squareSumOfLeft += auxiliaries[j].label * auxiliaries[j].label;
				squareSumOfRight -= auxiliaries[j].label * auxiliaries[j].label;
				double varLeft = squareSumOfLeft - sumOfLeft * sumOfLeft / (j + 1);
				//System.out.println(varLeft);
				double varRight = squareSumOfRight - sumOfRight * sumOfRight / (randomSamples.length - j - 1);
				//System.out.println(varRight);

				if (varLeft + varRight < minimalSum) {
					minimalSum = varLeft + varRight;
					feature = f;
					threshold = auxiliaries[j].value;
					numOfLeft = j + 1;
					numOfRight = randomSamples.length - j - 1;
				}
			}
		}

		if (feature == -1) {
			//System.out.println("No feature can be used to split");
			return false;
		}

		//	distribute the trainingSamples, randomSamples and testSamples to two leaves
		left = new Node();
		right = new Node();
		left.randomSamples = new TrainingSample[numOfLeft];
		right.randomSamples = new TrainingSample[numOfRight];
		for (int i = 0, i1 = 0, i2 = 0; i < randomSamples.length; i++) {
			if (randomSamples[i].feature[feature] <= threshold) {
				left.randomSamples[i1] = randomSamples[i];
				i1++;
			} else {
				right.randomSamples[i2] = randomSamples[i];
				i2++;
			}
		}
		numOfLeft = 0;
		for (int i = 0; i < trainingSamples.length; i++) {
			if (trainingSamples[i].feature[feature] <= threshold) {
				numOfLeft++;
			}
		}
		numOfRight = trainingSamples.length - numOfLeft;
		left.trainingSamples = new TrainingSample[numOfLeft];
		right.trainingSamples = new TrainingSample[numOfRight];
		for (int i = 0, i1 = 0, i2 = 0; i < trainingSamples.length; i++) {
			if (trainingSamples[i].feature[feature] <= threshold) {
				left.trainingSamples[i1] = trainingSamples[i];
				i1++;
			} else {
				right.trainingSamples[i2] = trainingSamples[i];
				i2++;
			}
		}
		numOfLeft = 0;
		for (int i = 0; i < testSamples.length; i++) {
			if (testSamples[i].feature[feature] <= threshold) {
				numOfLeft++;
			}
		}
		numOfRight = testSamples.length	- numOfLeft;
		left.testSamples = new TestSample[numOfLeft];
		right.testSamples = new TestSample[numOfRight];
		for (int i = 0, i1 = 0, i2 = 0; i < testSamples.length; i++) {
			if (testSamples[i].feature[feature] <= threshold) {
				left.testSamples[i1] = testSamples[i];
				i1++;
			} else {
				right.testSamples[i2] = testSamples[i];
				i2++;
			}
		}
		return true;
	}

	public void computeGamma() {
		//System.out.println("computing gamma in RF");
		double sum = 0;
		for (TrainingSample sample : trainingSamples) {
			sum += sample.label;
		}
		gamma = sum / trainingSamples.length;
		gamma = (gamma > 0.5) ? 1 : 0;
		//System.out.println(gamma);
	}

	public void updateScore() {
		// System.out.println("updating score in RF");
		// System.out.println("gamma:" + gamma);
		for (TrainingSample sample : trainingSamples) {
			sample.score += gamma;
			//System.out.println(sample.score);
		}
		for (TestSample sample : testSamples) {
			sample.score += gamma;
			//System.out.println(sample.score);
		}
	}
}

class AuxiliaryNode {
	public double value;
	public double label;
}

class MyComparator4 implements Comparator<AuxiliaryNode> {
	@Override
	public int compare(AuxiliaryNode o1, AuxiliaryNode o2) {
		if (o1.value > o2.value) return 1;
		if (o1.value < o2.value) return -1;
		return 0;
	}
}
