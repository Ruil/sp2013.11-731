package type;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class LM {
	// // A language model scores sequences of English words, and must account
	// // for both beginning and end of each sequence. Example API usage:
	// lm = models.LM(filename)
	// sentence = "This is a test ."
	// lm_state = lm.begin() // initial state is always <s>
	// logprob = 0.0
	// for word in sentence.split():
	//     (lm_state, word_logprob) = lm.score(lm_state, word)
	//     logprob += word_logprob
	// logprob += lm.end(lm_state) // transition to </s>, can also use lm.score(lm_state, "</s>")[1]
	
	HashMap<String, double[]> table=new HashMap<String, double[]>();
	public LM(String filename){
    	System.out.println("Reading language model from "+filename+"...\n");

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(filename));
			String line = br.readLine();
			while (line != null) {
				if(line.trim().equals("")){
					line = br.readLine();
					continue;
				}
				String[] entry=line.trim().split("\t");
				if (entry.length > 1 && !entry[0].equals("ngram")){
					double logprob=Double.parseDouble(entry[0]);
					String ngram=entry[1];
					double backoff=0;
					if(entry.length>2){
						backoff=Double.parseDouble(entry[2]);					
					}
					double[] prob={logprob, backoff};
					
					table.put(ngram, prob);
				}
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
		}
	}
	
	public String begin(){
		return "<s>";
	}
	
	public LMItem score(String state, String word){
		String ngram=(state+" "+word).trim();
		String[] ngrams=ngram.split(" ");
		String temp=ngrams[ngrams.length-2]+" "+ngrams[ngrams.length-1];
		return new LMItem(temp, score(ngram));
	}
	
	public double backoffScore(String ngram){
		if(table.containsKey(ngram)){
			return table.get(ngram)[1];
		}else{
			return 0;
		}
	}
	
	public double score(String ngram){
		ngram=ngram.trim();
		if(ngram.equals("")){
			return 0;
		}
		String[] ngrams=ngram.split(" ");
		if(table.containsKey(ngram)){
			return table.get(ngram)[0];
		}else{
			if(ngrams.length==1){
				return table.get("<unk>")[0];
			}else {
				String a="";
				for(int i=0;i<ngrams.length-1;i++){
					a+=ngrams[i]+" ";
				}
				String b="";
				for(int i=1;i<ngrams.length;i++){
					b+=ngrams[i]+" ";
				}
				return backoffScore(a.trim())+score(b.trim());
			}
		}
	}
	
	public double end(String state){
		return score(state, "</s>").logprob;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
