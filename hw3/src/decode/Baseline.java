package decode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import type.Hypothesis;
import type.LM;
import type.LMItem;
import type.Phrase;
import type.TM;
import util.FileUtil;

public class Baseline {
	List<String> input_sents;
	LM lm;
	TM tm;
	String output;

	public Baseline(String inputfilepathname, String lmfilepathname,
			String tmfilepathname, String output) {
		input_sents = FileUtil.readFile(inputfilepathname);
		lm = new LM(lmfilepathname);
		tm = new TM(tmfilepathname, Integer.MAX_VALUE);
		this.output=output;
	}

	void decode() {
		StringBuffer buffer=new StringBuffer();
		for (String f : input_sents) {
			// The following code implements a DP monotone decoding
			// algorithm (one that doesn't permute the target phrases).
			// Hence all hypotheses in stacks[i] represent translations of
			// the first i words of the input sentence.
			// HINT: Generalize this so that stacks[i] contains translations
			// of any i words (remember to keep track of which words those
			// are, and to estimate future costs)
			String[] ngrams = f.trim().split(" ");
			Hypothesis initial_hypothesis = new Hypothesis(0.0, lm.begin(),
					null, null);
			ArrayList<ArrayList<Hypothesis>> stacks = new ArrayList<ArrayList<Hypothesis>>();
			for (int i = 0; i < ngrams.length; i++) {
				ArrayList<Hypothesis> current = new ArrayList<Hypothesis>();
				stacks.add(current);
			}
			stacks.get(0).add(initial_hypothesis);
			for (int i = 0; i < stacks.size(); i++) {

				// extend the top s hypotheses in the current stack
				ArrayList<Hypothesis> currentStack = stacks.get(i);
				Collections.sort(currentStack);
				for (Hypothesis h : currentStack) {
					int len=Math.min(i+4,  ngrams.length);
					for (int j = i + 1; j <len; j++) {
						String next = "";
						for (int k = i + 1; k <= j; k++) {
							next += ngrams[k] + " ";
						}
						next = next.trim();

						if (tm.contains(next)) {
							ArrayList<Phrase> nextTransPhraseLs = tm
									.getPhrases(next);
							for (Phrase phrase : nextTransPhraseLs) {
								double logprob = h.logprob + phrase.logprob;
								String lm_state = h.lm_state;
								String phraseStr = phrase.text;
								String[] temp = phraseStr.split(" ");
								for (String word : temp) {
									LMItem item = lm.score(lm_state, word);
									logprob += item.logprob;
									lm_state = item.text;
								}
								
								if(j==ngrams.length-1){
									logprob += lm.end(lm_state);
								}
								
								Hypothesis new_hypothesis = new Hypothesis(
										logprob, lm_state, h, phrase);
								boolean find = false;
								ArrayList<Hypothesis> currentLs = stacks.get(j);
								for (int l = 0; l < currentLs.size(); l++) {
									if (currentLs.get(l).lm_state
											.equals(lm_state)) {
										find = true;
										if (currentLs.get(l).logprob < logprob) {
											currentLs.set(l, new_hypothesis);
										}
									}
								}
								if (!find) {
									currentLs.add(new_hypothesis);
								}
							}
						}
					}
				}
			}

			// find best translation by looking at the best scoring hypothesis
			// on the last stack
			ArrayList<Hypothesis> lastStack = stacks.get(stacks.size() - 1);
			Hypothesis winner = lastStack.get(0);
			double max = lastStack.get(0).logprob;

			for (int i = 1; i < lastStack.size(); i++) {
				if (max < lastStack.get(i).logprob) {
					max = lastStack.get(i).logprob;
					winner = lastStack.get(i);
				}
			}
			buffer.append(extract_english_recursive(winner).toString().trim()+"\n");
		}
		FileUtil.writeToFile(output, buffer);
	}

	StringBuffer extract_english_recursive(Hypothesis winner) {
		if (winner.phrase == null) {
			return new StringBuffer();
		}
		StringBuffer buffer = new StringBuffer(" "+winner.phrase.text);
		return buffer.insert(0, extract_english_recursive(winner.predecessor));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String inputfilepathname = "D:\\Workspace\\GitHub\\sp2013.11-731\\hw3\\data\\input";
		String lmfilepathname = "D:\\Workspace\\GitHub\\sp2013.11-731\\hw3\\data\\lm";
		String tmfilepathname = "D:\\Workspace\\GitHub\\sp2013.11-731\\hw3\\data\\tm";
		String output="D:\\Workspace\\GitHub\\sp2013.11-731\\hw3\\output\\output.txt";
		Baseline baseline=new Baseline(inputfilepathname, lmfilepathname, tmfilepathname, output);
		baseline.decode();
	}

}
