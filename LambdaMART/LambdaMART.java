import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LambdaMART {
	public static void main(String[] args) {
		final int numOfTrainingSamples = 36192;
		final int numofTestSamples = 40;
		final int truncationLevel = 10;
		final int numOfFeatures = 46;
		final int numOfTrees = 1;
		final int numOfLeaves = 16;
		final double learningRate = 0.5;
		final int minSamplesPerLeaves = 1;
		List<Integer> trainingFirstIndex = new ArrayList<Integer>();
		List<Integer> trainingLastIndex = new ArrayList<Integer>();
		List<Integer> testFirstIndex = new ArrayList<Integer>();
		List<Integer> testLastIndex = new ArrayList<Integer>();
		int[] trainingLabel = new int[numOfTrainingSamples];
		int[] testLabel = new int[numofTestSamples];
		String lastqid = "qid:-1";
		int firstQidIndex = 0;
		TrainingSample trainingSamples[] = new TrainingSample[numOfTrainingSamples];
		TestSample testSamples[] = new TestSample[numofTestSamples];
		//	training sample
		try {
			BufferedReader br = new BufferedReader(new FileReader("./MQ2007/Fold2/newtrain.txt"));
			//BufferedReader br = new BufferedReader(new FileReader("../simple.txt"));
			String data = null;
			for (int i = 0; i < numOfTrainingSamples; i++) {
				trainingSamples[i] = new TrainingSample();
				trainingSamples[i].feature = new double[numOfFeatures];
				trainingSamples[i].score = 0;
				data = br.readLine();
				String[] buff = data.split(" ");
				trainingLabel[i] = Integer.parseInt(buff[0]);
				trainingSamples[i].label = trainingLabel[i];
				trainingSamples[i].qid = Integer.parseInt(buff[1].substring(buff[1].indexOf(":") + 1));
				if (buff[1].indexOf(lastqid) == -1) {
	 				trainingFirstIndex.add(firstQidIndex);
	 				lastqid = buff[1];
				}
				firstQidIndex++;
				for (int j = 0; j < numOfFeatures; j++) {
					trainingSamples[i].feature[j] = Double.parseDouble(buff[j + 2].substring(buff[j + 2].indexOf(":") + 1));
				}
			}
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		for (int i = 1; i < trainingFirstIndex.size(); i++) {
			trainingLastIndex.add(trainingFirstIndex.get(i) - 1);
		}
		trainingLastIndex.add(numOfTrainingSamples - 1);

		//	training_ideal_DCG
		final int maxLabel = 5;
		List<Double> training_ideal_DCG = new ArrayList<Double>();
		List<Double> training_original_NDCG = new ArrayList<Double>();
		for (int athListWithSameqid = 0; athListWithSameqid < trainingLastIndex.size(); athListWithSameqid++) {
			int firstIndex = trainingFirstIndex.get(athListWithSameqid);
			int lastIndex = trainingLastIndex.get(athListWithSameqid);
			int numOfSameqid = lastIndex - firstIndex + 1;
			int[] count = new int[maxLabel];
			Arrays.fill(count, 0);
			for (int i = 0; i < numOfSameqid; i++) {
				count[trainingLabel[i + trainingFirstIndex.get(athListWithSameqid)]]++;
			}
			double idealDcg = 0;
			int[] ideal_sequence = new int[numOfSameqid];
			for (int i = count.length - 1, k = 0; i > -1; i--) {
				for (int j = 0; j < count[i]; j++) {
					ideal_sequence[k] = i;
					k++;
				}
			}
			double originalDCG = 0;
			for (int i = 0; i < numOfSameqid; i++) {
				double denominator = Math.log(i + 2)/ Math.log(2);
				double numerator1 = Math.pow(2, trainingLabel[i + firstIndex]) - 1;
				double numerator2 = Math.pow(2, ideal_sequence[i]) - 1;
				originalDCG += numerator1 / denominator;
				idealDcg += numerator2 / denominator;
			}
			training_original_NDCG.add(originalDCG / idealDcg);
			training_ideal_DCG.add(idealDcg);
		}

		//	test sample
		lastqid = "qid:-1";
		firstQidIndex = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader("./MQ2007/Fold2/newvali.txt"));
			//BufferedReader br = new BufferedReader(new FileReader("../simple.txt"));
			String data = null;
			for (int i = 0; i < numofTestSamples; i++) {
				testSamples[i] = new TestSample();
				testSamples[i].feature = new double[numOfFeatures];
				testSamples[i].score = 0;
				data = br.readLine();
				String[] buff = data.split(" ");
				testLabel[i] = Integer.parseInt(buff[0]);
				testSamples[i].label = Integer.parseInt(buff[0]);
				testSamples[i].qid = Integer.parseInt(buff[1].substring(buff[1].indexOf(":") + 1));
				if (buff[1].indexOf(lastqid) == -1) {
	 				testFirstIndex.add(firstQidIndex);
	 				lastqid = buff[1];
				}
				firstQidIndex++;
				for (int j = 0; j < numOfFeatures; j++) {
					testSamples[i].feature[j] = Double.parseDouble(buff[j + 2].substring(buff[j + 2].indexOf(":") + 1));
				}
			}
			br.close();
		} catch(IOException e) {
			e.printStackTrace();			
		}
		for (int i = 1; i < testFirstIndex.size(); i++) {
			testLastIndex.add(testFirstIndex.get(i) - 1);
		}
		testLastIndex.add(numofTestSamples - 1);

		//	test_ideal_DCG
		List<Double> test_ideal_DCG = new ArrayList<Double>();
		List<Double> test_original_NDCG = new ArrayList<Double>();
		for (int athListWithSameqid = 0; athListWithSameqid < testLastIndex.size(); athListWithSameqid++) {
			int firstIndex = testFirstIndex.get(athListWithSameqid);
			int lastIndex = testLastIndex.get(athListWithSameqid);
			int numOfSameqid = lastIndex - firstIndex + 1;
			int[] count = new int[maxLabel];
			Arrays.fill(count, 0);
			for (int i = 0; i < numOfSameqid; i++) {
				count[testLabel[i + testFirstIndex.get(athListWithSameqid)]]++;
			}
			double idealDcg = 0;
			int[] ideal_sequence = new int[numOfSameqid];
			for (int i = count.length - 1, k = 0; i > -1; i--) {
				for (int j = 0; j < count[i]; j++) {
					ideal_sequence[k] = i;
					k++;
				}
			}
			double originalDCG = 0;
			for (int i = 0; i < numOfSameqid; i++) {
				double denominator = Math.log(i + 2)/ Math.log(2);
				double numerator1 = Math.pow(2, testLabel[i + firstIndex]) - 1;
				double numerator2 = Math.pow(2, ideal_sequence[i]) - 1;
				originalDCG += numerator1 / denominator;
				idealDcg += numerator2 / denominator;
			}
			test_original_NDCG.add(originalDCG / idealDcg);
			test_ideal_DCG.add(idealDcg);
			//System.out.println(idealDcg);
		}

		//	compute the original NDCG for training and test set
		double original_average_NDCG = 0;
		for (int i = 0; i < training_original_NDCG.size(); i++) {
			original_average_NDCG += training_original_NDCG.get(i);
		}
		original_average_NDCG = original_average_NDCG / training_original_NDCG.size();
		System.out.println("The original average for training set is " + original_average_NDCG);
		original_average_NDCG = 0;
		for (int i = 0; i < test_original_NDCG.size(); i++) {
			original_average_NDCG += test_original_NDCG.get(i);
		}
		original_average_NDCG = original_average_NDCG / test_original_NDCG.size();
		System.out.println("The original average for test set is " + original_average_NDCG);

		//	regression trees
		for (int kthtree = 0; kthtree < numOfTrees; kthtree++) {
			for (int i = 0; i < numOfTrainingSamples; i++) {
				trainingSamples[i].lambda = 0;
				trainingSamples[i].weight = 0;
			}
			for (int i = 0; i < trainingFirstIndex.size(); i++) {
				for (int j = trainingFirstIndex.get(i); j < trainingLastIndex.get(i) + 1; j++) {
					for (int k = trainingFirstIndex.get(i); k < trainingLastIndex.get(i) + 1; k++) {
						if (trainingLabel[j] > trainingLabel[k]) {
							double denominator_j = Math.log(j - trainingFirstIndex.get(i) + 2) / Math.log(2);
							double DCG_j = (Math.pow(2, trainingLabel[j]) - 1) / denominator_j;
							double DCG_j_swapped_k = (Math.pow(2, trainingLabel[k]) - 1) / denominator_j;
							double denominator_k = Math.log(k - trainingFirstIndex.get(i) + 2) / Math.log(2);
							double DCG_k = (Math.pow(2, trainingLabel[k]) - 1) / denominator_k;
							double DCG_k_swapped_j = (Math.pow(2, trainingLabel[j]) - 1) / denominator_k;
							double positiveDeltaNDCG_j_k = Math.abs((DCG_j - DCG_j_swapped_k + DCG_k - DCG_k_swapped_j) / training_ideal_DCG.get(i));
							double rho_j_k = 1 / (1 + Math.pow(Math.E, trainingSamples[j].score - trainingSamples[k].score));
							//System.out.println((1 + j) + ":" + rho_j_k * (1 - rho_j_k) * positiveDeltaNDCG_j_k);
							trainingSamples[j].lambda -= rho_j_k * positiveDeltaNDCG_j_k;
							trainingSamples[k].lambda += rho_j_k * positiveDeltaNDCG_j_k;
							trainingSamples[j].weight += rho_j_k * (1 - rho_j_k) * positiveDeltaNDCG_j_k;
							trainingSamples[k].weight += rho_j_k * (1 - rho_j_k) * positiveDeltaNDCG_j_k;
						}
					}
				}
			}
			/*for (int i = 0; i < numOfTrainingSamples; i++) {
				String result = "";
				result += trainingSamples[i].label + " ";
				result += "lambda:" + trainingSamples[i].lambda + " ";
				result += "weight:" + trainingSamples[i].weight;
				System.out.println(result);
			}
			System.out.println("--------------------------");*/


			List<Split> splits = new ArrayList<Split>();
			Split root = new Split();
			root.trainingSamples = trainingSamples;
			root.testSamples = testSamples;
			splits.add(root);

			for (int i = 0, nextSplit = 0; i < numOfLeaves - 1; i++, nextSplit++) {
				if (nextSplit >= splits.size()) {
					System.out.println("We have problems here");
				} else if (splits.get(nextSplit).train(minSamplesPerLeaves, numOfFeatures) == true) {
					System.out.println(kthtree + "th tree split feature:" + splits.get(nextSplit).feature + " split value:" + splits.get(nextSplit).threshold);
					splits.add(splits.get(nextSplit).left);
					splits.get(nextSplit).left = null;
					splits.add(splits.get(nextSplit).right);
					splits.get(nextSplit).right = null;
				} else {
					i--;
				}
			}

			for (Split split : splits) {
				if (split.feature == -1) {
					split.computeGamma();
					split.updateScore(learningRate);
				}
			}

			/*System.out.println("sort before:");
			for (int i = 0; i < numOfTrainingSamples; i++) {
				String result = trainingSamples[i].label + " " + trainingSamples[i].score;
				System.out.println(result);
			}
			System.out.println("--------------------------");*/

			
			//	sort the trainingSamples by new score
			for (int athListWithSameqid = 0; athListWithSameqid < trainingLastIndex.size(); athListWithSameqid++) {
				int firstIndex = trainingFirstIndex.get(athListWithSameqid);
				int lastIndex = trainingLastIndex.get(athListWithSameqid);
				Arrays.sort(trainingSamples, firstIndex, lastIndex + 1, new MyComparator1());
			}

			//	update the training label array
			for (int i = 0; i < numOfTrainingSamples; i++) {
				trainingLabel[i] = trainingSamples[i].label;
			}

			/*System.out.println("sort after:");
			for (int i = 0; i < numOfTrainingSamples; i++) {
				String result = trainingLabel[i] + " " + trainingSamples[i].score;
				System.out.println(result);
			}
			System.out.println("--------------------------");*/


			//	sort the testSamples by new score
			for (int athListWithSameqid = 0; athListWithSameqid < testLastIndex.size(); athListWithSameqid++) {
				int firstIndex = testFirstIndex.get(athListWithSameqid);
				int lastIndex = testLastIndex.get(athListWithSameqid);
				Arrays.sort(testSamples, firstIndex, lastIndex + 1, new MyComparator3());
			}
			//	update the test label array
			for (int i = 0; i < numofTestSamples; i++) {
				testLabel[i] = testSamples[i].label;
			}
		
		}

		//	the final average training NDCG
		double average_NDCG = 0;
		for (int athListWithSameqid = 0; athListWithSameqid < trainingLastIndex.size(); athListWithSameqid++) {
			int firstIndex = trainingFirstIndex.get(athListWithSameqid);
			int lastIndex = trainingLastIndex.get(athListWithSameqid);
			int numOfSameqid = lastIndex - firstIndex + 1;

			double final_DCG = 0;
			for (int i = 0; i < numOfSameqid; i++) {
				double denominator = Math.log(i + 2) / Math.log(2);
				double numerator = Math.pow(2, trainingLabel[i + firstIndex]) - 1;
				final_DCG += numerator / denominator;
			}

			//	final NDCG
			double final_NDCG = final_DCG / training_ideal_DCG.get(athListWithSameqid);
			average_NDCG += final_NDCG;

			if (Double.isNaN(final_NDCG)) {
				System.out.println(training_ideal_DCG.get(athListWithSameqid) + " " + firstIndex);
			}

			//System.out.println(athListWithSameqid + "th training final_NDCG:" + final_NDCG);
		}
		average_NDCG = average_NDCG / trainingFirstIndex.size();
		System.out.println("final average training NDCG:" + average_NDCG);

		// for (int i = 0; i < numOfTrainingSamples; i++) {
		// 	String result = "";
		// 	result += "" + trainingSamples[i].label + " ";
		// 	for (int j = 0; j < 5; j++) {
		// 		result += "" + j + ":" + trainingSamples[i].feature[j] + " ";
		// 	}
		// 	result += trainingSamples[i].score;
		// 	System.out.println(result);
		// }

		//	the final average test NDCG
		average_NDCG = 0;
		for (int athListWithSameqid = 0; athListWithSameqid < testLastIndex.size(); athListWithSameqid++) {
			int firstIndex = testFirstIndex.get(athListWithSameqid);
			int lastIndex = testLastIndex.get(athListWithSameqid);
			int numOfSameqid = lastIndex - firstIndex + 1;

			double final_DCG = 0;
			for (int i = 0; i < numOfSameqid; i++) {
				double denominator = Math.log(i + 2) / Math.log(2);
				double numerator = Math.pow(2, testLabel[i + firstIndex]) - 1;
				final_DCG += numerator / denominator;
			}

			//	final NDCG
			double final_NDCG = final_DCG / test_ideal_DCG.get(athListWithSameqid);
			average_NDCG += final_NDCG;

			if (Double.isNaN(final_NDCG) || Double.isInfinite(final_NDCG)) {
				System.out.println(test_ideal_DCG.get(athListWithSameqid) + " " + firstIndex);
			}

			//System.out.println(athListWithSameqid + "th test final_NDCG:" + final_NDCG);
		}
		average_NDCG = average_NDCG / testFirstIndex.size();
		System.out.println("final average test NDCG:" + average_NDCG);

		System.out.println("The number of trees:" + numOfTrees);
		System.out.println("The number of leaves:" + numOfLeaves);
		// for (int i = 0; i < numofTestSamples; i++) {
		// 	String result = "";
		// 	result += "" + testSamples[i].label + " ";
		// 	for (int j = 0; j < 5; j++) {
		// 		result += "" + j + ":" + testSamples[i].feature[j] + " ";
		// 	}
		// 	result += testSamples[i].score;
		// 	System.out.println(result);
		// }
	}
}

class Split {
	public double threshold;
	public int feature = -1;
	public double gamma;
	public TrainingSample trainingSamples[];
	public TestSample testSamples[];
	public Split left;
	public Split right;
	public Boolean train(int minSamplesPerLeaves, int numOfFeatures) {
		double minimalSum = Double.MAX_VALUE;
		int numOfLeft = 0, numOfRight = 0;
		for (int i = 0; i < numOfFeatures; i++) {
			AuxiliarySplit[] auxiliaries = new AuxiliarySplit[trainingSamples.length];
			for (int j = 0; j < trainingSamples.length; j++) {
				auxiliaries[j] = new AuxiliarySplit();
				auxiliaries[j].value = trainingSamples[j].feature[i];
				auxiliaries[j].lambda = trainingSamples[j].lambda;
				//auxiliaries[j].lambda = j + 1;
			}
			Arrays.sort(auxiliaries, new MyComparator2());
			// for (int j = 0; j < auxiliaries.length; j++) {
			// 	System.out.println(auxiliaries[j].lambda);
			// }
			double sumOfLeft = 0, squareSumOfLeft = 0;
			double sumOfRight = 0, squareSumOfRight = 0;
			for (int j = 0; j < trainingSamples.length; j++) {
				sumOfRight += auxiliaries[j].lambda;
				squareSumOfRight += auxiliaries[j].lambda * auxiliaries[j].lambda;
			}
			for (int j = 0; j < minSamplesPerLeaves - 1; j++) {
				sumOfLeft += auxiliaries[j].lambda;
				squareSumOfLeft += auxiliaries[j].lambda * auxiliaries[j].lambda;
			}
			for (int j = minSamplesPerLeaves - 1; j < trainingSamples.length - minSamplesPerLeaves; j++) {
				if (auxiliaries[j].value == auxiliaries[j+1].value) continue;
				sumOfLeft += auxiliaries[j].lambda;
				sumOfRight -= auxiliaries[j].lambda;
				squareSumOfLeft += auxiliaries[j].lambda * auxiliaries[j].lambda;
				squareSumOfRight -= auxiliaries[j].lambda * auxiliaries[j].lambda;
				double varLeft = squareSumOfLeft - sumOfLeft * sumOfLeft / (j + 1);
				//System.out.println(varLeft);
				double varRight = squareSumOfRight - sumOfRight * sumOfRight / (trainingSamples.length - j - 1);
				//System.out.println(varRight);

				//System.out.println("s" + i + "," + j + ":" + (varLeft + varRight) + " featureValue:" + auxiliaries[j].value);
				if (varLeft + varRight < minimalSum) {
					minimalSum = varLeft + varRight;
					feature = i;
					threshold = auxiliaries[j].value;
					numOfLeft = j + 1;
					numOfRight = trainingSamples.length - numOfLeft;
				}
			}
		}

		if (feature == -1) {
			System.out.println("No feature can be used to split");
			return false;
		}

		//	distribute the trainingSamples and testSamples to two leaves
		left = new Split();
		right = new Split();
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
		numOfRight = 0;
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
		double sum = 0;
		double numerator = 0;
		double denominator = 0;
		for (TrainingSample sample : trainingSamples) {
			numerator += sample.lambda;
			denominator += sample.weight;
		}
		gamma = numerator / denominator;
		//System.out.println(gamma);
	}
	public void updateScore(double learningRate) {
		for (TrainingSample sample : trainingSamples) {
			sample.score -= gamma * learningRate;
		}
		for (TestSample sample : testSamples) {
			sample.score -= gamma * learningRate;
		}
	}
	public void saveSplit() {}
}

class TrainingSample {
	public int label;
	public double score = 0;
	public double[] feature;
	public int qid;
	public double lambda;
	public double weight;
}

class TestSample {
	public int label;
	public double score = 0;
	public double[] feature;
	public int qid;
}

class AuxiliarySplit {
	public double value;
	public double lambda;
}


class MyComparator1 implements Comparator<TrainingSample> {
	@Override
	public int compare(TrainingSample o1, TrainingSample o2) {
		if (o1.score < o2.score) return 1;
		if (o1.score > o2.score) return -1;
		return 0;
	}
}

class MyComparator2 implements Comparator<AuxiliarySplit> {
	@Override
	public int compare(AuxiliarySplit o1, AuxiliarySplit o2) {
		if (o1.value > o2.value) return 1;
		if (o1.value < o2.value) return -1;
		return 0;
	}
}

class MyComparator3 implements Comparator<TestSample> {
	@Override
	public int compare(TestSample o1, TestSample o2) {
		if (o1.score < o2.score) return 1;
		if (o1.score > o2.score) return -1;
		return 0;
	}
}





