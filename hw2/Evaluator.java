package eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import util.FileUtil;

public class Evaluator {

	public Evaluator() {
		// TODO Auto-generated constructor stub
	}

	int eval(String h1, String h2, String ref){ 
		String[] gswords=ref.split(" ");
		HashSet<String> set=new HashSet<String>();
		for(String word:gswords){
			set.add(word);
		}
		String[] h1words=h1.split(" ");
		String[] h2words=h2.split(" ");
		
		double prec1=0, prec2=0;
		double recall1=0, recall2=0;
		HashSet<String> h1Set=new HashSet<String>();
		HashSet<String> h2Set=new HashSet<String>();
		for(String word:h1words){
			if(set.contains(word)){
				prec1++;
				h1Set.add(word);
			}
		}
		
		prec1=prec1/(h1words.length*1.0);
		recall1=(h1Set.size()*1.0)/(set.size()*1.0);
		
		double score1=HarmonicMean(prec1, recall1);
		
		for(String word:h2words){
			if(set.contains(word)){
				prec2++;
				h2Set.add(word);
			}
		}
		
		prec2=prec2/(h2words.length*1.0);
		recall2=(h2Set.size()*1.0)/(set.size()*1.0);
		
		double score2=HarmonicMean(prec2, recall2);
		if(score1>score2){
			return 1;
		}else if(score1==score2){
			return 0;
		}
		return -1;
	}
	
	double HarmonicMean(double prec, double recall){
		return 10*prec*recall/(9*prec+recall);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Evaluator eval=new Evaluator();
		String filePathName="E:\\Dropbox\\Wei\\11731\\test.hyp1-hyp2-ref";
		String outputPathName="E:\\Dropbox\\Wei\\11731\\output.txt";
		List<String> lines=FileUtil.readFile(filePathName);
		StringBuffer buffer=new StringBuffer();
		for(String line: lines){
			int index=line.indexOf("||| ");
			String h1=line.substring(0, index);
			line=line.substring(index+4);
			index=line.indexOf("||| ");
			String h2=line.substring(0, index);
			String ref=line.substring(index+4);
			buffer.append(eval.eval(h1,h2,ref)+"\n");
		}
		FileUtil.writeToFile(outputPathName, buffer);
	}

}
