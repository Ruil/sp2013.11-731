package type;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class TM {
	// A translation model is a dictionary where keys are tuples of French words
	// and values are lists of (english, logprob) named tuples. For instance,
	// the French phrase "que se est" has two translations, represented like so:
	// tm[('que', 'se', 'est')] = [
	// phrase(english='what has', logprob=-0.301030009985),
	// phrase(english='what has been', logprob=-0.301030009985)]
	// k is a pruning parameter: only the top k translations are kept for each
	// f.

	HashMap<String, ArrayList<Phrase>> tmhash;

	public TM(String filename, int k) {
		System.out.println("Reading translation model from " + filename
				+ "...\n");
		tmhash = new HashMap<String, ArrayList<Phrase>>();

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(filename));
			String line = br.readLine();
			while (line != null && !line.trim().equals("")) {
				String[] temp = line.trim().split(" \\|\\|\\| ");
				String f = temp[0];
				String e = temp[1];
				String logprob = temp[2];

				Phrase phrase = new Phrase(e, Double.parseDouble(logprob));

				if (tmhash.containsKey(f)) {
					tmhash.get(f).add(phrase);
				} else {
					ArrayList<Phrase> phraseLs = new ArrayList<Phrase>();
					phraseLs.add(phrase);
					tmhash.put(f, phraseLs);
				}

				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
		}

		Iterator<String> iter = tmhash.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			ArrayList<Phrase> value = tmhash.get(key);
			Collections.sort(value);

			ArrayList<Phrase> newLs = new ArrayList<Phrase>();
			int len = Math.min(k, value.size());
			for (int i = 0; i < len; i++) {
				newLs.add(value.get(i));
			}

			tmhash.put(key, newLs);
		}

	}
	
	public boolean contains(String ngram){
		return tmhash.containsKey(ngram);
	}
	
	public ArrayList<Phrase> getPhrases(String ngram){
		return tmhash.get(ngram);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String filename = "D:\\Workspace\\GitHub\\sp2013.11-731\\hw3\\data\\tm";
		TM tm = new TM(filename, Integer.MAX_VALUE);
		boolean find = tm.contains("sectarias");
		ArrayList<Phrase> ls=tm.getPhrases("sectarias");
		ls.size();
	}
}
