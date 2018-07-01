import java.util.*;
import java.io.*;

public class Preprocessor {
	public static void main(String[] args) {
		final int numOfTrainingSamples = 9442;
		String lastLabel = null, lastQid = null;
		Boolean isUseful = false;
		List<String> rankList = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("./MQ2008/Fold5/train.txt"));
			//BufferedReader br = new BufferedReader(new FileReader("../simple.txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("./MQ2008/Fold5/newtrain.txt", false));			
			for (int i = 0; i < numOfTrainingSamples; i++) {
				String data = br.readLine();
				String[] buff = data.split(" ");
				String label = buff[0];
				String qid = buff[1];
				if (lastLabel == null) {
					lastLabel = label;
					lastQid = qid;
				}
				if (!isUseful && label.indexOf(lastLabel) == -1 && qid.indexOf(lastQid) != -1) {
					isUseful = true;
				}
				if (qid.indexOf(lastQid) == -1 && isUseful) {
					for (String record : rankList) {
						bw.write(record);
						bw.newLine();
					}
					isUseful = false;
					lastLabel = label;
					lastQid = qid;
					rankList.clear();
				} else if (qid.indexOf(lastQid) == -1 && !isUseful) {
					lastLabel = label;
					lastQid = qid;
					rankList.clear();
				}		
				rankList.add(data);
			}
			if (isUseful) {
				for (String record : rankList) {
					bw.write(record);
					bw.newLine();
				}
			}
			br.close();
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}



