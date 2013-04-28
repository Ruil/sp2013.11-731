package rerank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import type.Translation;
import util.FileUtil;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class PairwiseLearning {

	/*
	 * 1. Rank according to bleu 2. Calculate the differences 3. Generating the
	 * instances 4. Learning the weights
	 */
	static Instances generateInstances(HashMap<String, ArrayList<Translation>> bestHash,
			HashMap<String, String> refHash) {
		FastVector attVals = new FastVector();
		attVals.addElement("-1");
		attVals.addElement("1");
		// 1. score 100 translations
		Iterator<String> iter = bestHash.keySet().iterator();
		ArrayList<Instance> instanceLs = new ArrayList<Instance>();
		while (iter.hasNext()) {
			String key = iter.next();
			String ref = refHash.get(key);
			ArrayList<Translation> tranLs = bestHash.get(key);
			for (Translation tran : tranLs) {
				tran.score = score(tran, ref);
			}
			Collections.sort(tranLs);

			// 2. group into two sets: 1-50, 51-100
			int mid = 50;
			for (int i = 0; i < mid; i++) {
				double[] vals = new double[4];
				vals[0] = tranLs.get(i).features[0]
						- tranLs.get(i + mid).features[0];
				vals[1] = tranLs.get(i).features[1]
						- tranLs.get(i + mid).features[1];
				vals[2] = tranLs.get(i).features[2]
						- tranLs.get(i + mid).features[2];
				vals[3] = attVals.indexOf("1");
				Instance instance = new Instance(1.0, vals);
				instanceLs.add(instance);
			}

			// 3. pairwise (label, i,i+49) and reverse (-label, i+49,i)
			for (int i = 0; i < mid; i++) {
				double[] vals = new double[4];
				vals[0] = -tranLs.get(i).features[0]
						+ tranLs.get(i + mid).features[0];
				vals[1] = -tranLs.get(i).features[1]
						+ tranLs.get(i + mid).features[1];
				vals[2] = -tranLs.get(i).features[2]
						+ tranLs.get(i + mid).features[2];
				vals[3] = attVals.indexOf("-1");
				Instance instance = new Instance(1.0, vals);
				instanceLs.add(instance);
			}
		}
		return createInstances(new String[] { "p(e|f)", "p(e)", "p_lex(f|e)" },
				instanceLs);
	}

	static private Instances createInstances(String[] featureNames,
			ArrayList<Instance> instanceLs) {
		FastVector atts = new FastVector(); // setup Attributes
		for (String featureName : featureNames) {
			atts.addElement(new Attribute(featureName));
		}

		FastVector attVals = new FastVector();
		attVals.addElement("-1");
		attVals.addElement("1");

		Attribute classAtt = new Attribute("class", attVals);
		atts.addElement(classAtt);

		Instances data = new Instances("TestData", atts, 0);

		data.setClass(classAtt);

		for (Instance instance : instanceLs) {
			data.add(instance);
		}
		return data;
	}

	static double score(Translation tran, String ref) {
		String[] gswords = ref.split(" ");
		HashSet<String> set = new HashSet<String>();
		for (String word : gswords) {
			set.add(word);
		}
		String[] tranwords = tran.text.split(" ");

		double prec = 0;
		double recall = 0;
		HashSet<String> tranSet = new HashSet<String>();
		for (String word : tranwords) {
			if (set.contains(word)) {
				prec++;
				tranSet.add(word);
			}
		}

		prec = prec / (tranwords.length * 1.0);
		recall = (tranSet.size() * 1.0) / (set.size() * 1.0);

		return HarmonicMean(prec, recall);
	}

	static void learning(Instances instances) {
		train(instances);
	}

	static double HarmonicMean(double prec, double recall) {
		return 10 * prec * recall / (9 * prec + recall);
	}

	static public void train(Instances data) {
		// setting class attribute
		data.setClassIndex(data.numAttributes() - 1);

		Logistic classifier = new Logistic();
		try {
			classifier.buildClassifier(data);
			
			double[][] weights=classifier.featureWeights();
			for(int i=0;i<weights.length;i++){
				for(int j=0;j<weights[i].length;j++){
					System.out.println(""+i+" "+j+":"+weights[i][j]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		for (int i = 0; i < data.numInstances(); i++) {

			try {
				double[] probDist = classifier.distributionForInstance(data.instance(i));

				double positiveProb = probDist[1];
				//double clsLabel = classifier.classifyInstance(data.instance(i));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String root = "D:\\Workspace\\GitHub\\sp2013.11-731\\hw4\\data\\";
		String dep100best = root + "dev.100best";
		String depref = root + "dev.ref";

		HashMap<String, ArrayList<Translation>> bestHash = new HashMap<String, ArrayList<Translation>>();
		List<String> lines = FileUtil.readFile(dep100best);
		for (String line : lines) {
			String[] temp = line.split(" \\|\\|\\| ");
			String id = temp[0];

			String text = temp[1];
			String featureStr = temp[2];
			double[] features = new double[3];
			features[0] = Double.parseDouble(featureStr.substring(7,
					featureStr.indexOf(" p(e)=")));
			features[1] = Double.parseDouble(featureStr.substring(
					featureStr.indexOf(" p(e)=") + 6,
					featureStr.indexOf(" p_lex(f|e)=")));
			features[2] = Double.parseDouble(featureStr.substring(featureStr
					.indexOf(" p_lex(f|e)=") + 12));

			Translation tran = new Translation(text, features);
			if (!bestHash.containsKey(id)) {
				bestHash.put(id, new ArrayList<Translation>());
			}
			bestHash.get(id).add(tran);
		}
		
		lines = FileUtil.readFile(depref);
		HashMap<String, String> refHash=new HashMap<String, String>();
		for(int i=0;i<lines.size();i++){
			String key=""+(i+100001);
			refHash.put(key, lines.get(i));
		}
		learning(generateInstances(bestHash, refHash));
	}

}
