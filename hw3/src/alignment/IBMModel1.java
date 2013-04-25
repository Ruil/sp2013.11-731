package alignment;

import java.util.HashMap;
import java.util.Iterator;

import corpus.BilingualCorpus;

import type.CountBag;
import type.SentencePair;
import util.FileUtil;

public class IBMModel1 {
	HashMap<String, HashMap<String, CountBag>> table;
	HashMap<String, Double> total_f;
	BilingualCorpus corpus;
	static final int MAX_ITER = 12;

	public IBMModel1() {
		this.corpus = new BilingualCorpus();
		this.corpus.buildTables();
		this.table = this.corpus.getTable();
		this.total_f = this.corpus.getTotalFrenchItems();
	}

	void EM() {
		// initialize t(e|f) uniformly
		initializeTranslationTable();
		int iter = 0;
		while (!isConverged(iter)) {
			System.out.println("Start iteration " + iter
					+ " ....................");
			initialize();
			Estep();
			Mstep();
			System.out.println("End iteration " + iter
					+ " ....................");
			iter++;
		}
	}

	boolean isConverged(int iter) {
		if (MAX_ITER < iter) {
			return true;
		}
		return false;
	}

	void initialize() {
		this.corpus.initializeSentencePairIterator();

		// count(e,f)=0;
		initializeCounts();

		// total(f)=0;
		initializeTotalF();
	}

	void Estep() {
		while (this.corpus.hasNext()) {
			SentencePair pair = this.corpus.next();
			// compute normalization
			String[] englishItems = pair.getEnglish();
			String[] frenchItems = pair.getFrench();
			HashMap<String, Double> s_total = new HashMap<String, Double>();

			for (String englishItem : englishItems) {
				// s-total(e)=0
				for (String frenchItem : frenchItems) {
					CountBag bag = this.table.get(englishItem).get(frenchItem);

					// s-total(e)+=t(e|f)
					if (s_total.containsKey(englishItem)) {
						s_total.put(englishItem, s_total.get(englishItem)
								+ bag.counts[1]);
					} else {
						s_total.put(englishItem, bag.counts[1]);
					}
				}
			}

			// collect counts
			for (String englishItem : englishItems) {
				for (String frenchItem : frenchItems) {
					CountBag bag = this.table.get(englishItem).get(frenchItem);

					// count(e|f)+=t(e|f)/s-total(e)
					double prob = bag.counts[1] / s_total.get(englishItem);
					bag.counts[0] += prob;

					// total(f)+=t(e|f)/s-total(e)
					this.total_f.put(frenchItem, this.total_f.get(frenchItem)
							+ prob);
				}
			}
		}
	}

	void Mstep() {
		// estimate probabilities
		Iterator<String> e_iterator = this.table.keySet().iterator();
		while (e_iterator.hasNext()) {
			String ekey = e_iterator.next();
			HashMap<String, CountBag> hash = this.table.get(ekey);
			Iterator<String> f_iterator = hash.keySet().iterator();
			while (f_iterator.hasNext()) {
				String fkey = f_iterator.next();
				CountBag bag = hash.get(fkey);

				// t(e|f)=count(e|f)/total(f)
				double count_ef = bag.counts[0];
				bag.counts[1] = count_ef / this.total_f.get(fkey);
				// System.out.println(ekey+" | "+fkey+": "+bag.counts[1]);
			}
		}
	}

	void initializeCounts() {
		Iterator<String> e_iterator = this.table.keySet().iterator();
		while (e_iterator.hasNext()) {
			String key = e_iterator.next();
			HashMap<String, CountBag> hash = this.table.get(key);
			Iterator<String> f_iterator = hash.keySet().iterator();
			while (f_iterator.hasNext()) {
				key = f_iterator.next();
				CountBag bag = hash.get(key);
				bag.counts[0] = 0;
			}
		}
	}

	void initializeTotalF() {
		this.total_f = this.corpus.getTotalFrenchItems();
		Iterator<String> iterator = this.total_f.keySet().iterator();
		while (iterator.hasNext()) {
			String next = iterator.next();
			this.total_f.put(next, new Double(0));
		}
	}

	void initializeTranslationTable() {
		Iterator<String> e_iterator = this.table.keySet().iterator();
		while (e_iterator.hasNext()) {
			String key = e_iterator.next();
			HashMap<String, CountBag> hash = this.table.get(key);
			Iterator<String> f_iterator = hash.keySet().iterator();
			double f_size = hash.size();
			while (f_iterator.hasNext()) {
				key = f_iterator.next();
				CountBag bag = hash.get(key);
				bag.counts[1] = 1.0 / f_size;
			}
		}
	}

	StringBuffer align() {
		StringBuffer buffer = new StringBuffer();
		this.corpus.initializeSentencePairIterator();
		while (this.corpus.hasNext()) {
			SentencePair pair = this.corpus.next();
			String[] englishItems = pair.getEnglish();
			String[] frenchItems = pair.getFrench();
			for (int i = 0; i < englishItems.length; i++) {
				double max = 0;
				int index = -1;
				for (int j = 0; j < frenchItems.length; j++) {
					double t_ef = this.table.get(englishItems[i]).get(
							frenchItems[j]).counts[1];
					if (t_ef > max) {
						max = t_ef;
						index = j;
					}
				}
				if (!((index - 1) < 0 || (i - 1) < 0)) {
					buffer.append((index - 1) + "-" + (i - 1) + " ");
				}
			}
			buffer.append("\n");
		}
		return buffer;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IBMModel1 model = new IBMModel1();
		model.EM();
		FileUtil.writeToFile("./alignment", model.align());
	}

}
