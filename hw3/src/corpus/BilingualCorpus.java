package corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import type.CountBag;
import type.SentencePair;

public class BilingualCorpus {
	static final String CORPUS_FILE_NAME_PATH = "D:\\temp\\dev-test-train.de-en";

	HashMap<String, HashMap<String, CountBag>> table;
	HashMap<String, Double> total_f; 

	int index;
	ArrayList<String[]> frenchSentenceLs;
	ArrayList<String[]> englishSentenceLs;

	public BilingualCorpus() {
		this.table = new HashMap<String, HashMap<String, CountBag>>();
		this.total_f=new HashMap<String, Double>();
		this.frenchSentenceLs = new ArrayList<String[]>();
		this.englishSentenceLs = new ArrayList<String[]>();
	}

	public void buildTables() {
		try {
			Scanner in;
			in = new Scanner(new File(CORPUS_FILE_NAME_PATH));

			while (in.hasNext()) {
				String input = in.nextLine();
				int sentencePairSegmentionPos = input.indexOf(" ||| ");
				if (input.trim().equals("") || sentencePairSegmentionPos < 0) {
					continue;
				}

				String frenchSentence = input.substring(0,
						sentencePairSegmentionPos);
				String englishSentence = input
						.substring(sentencePairSegmentionPos + 5);
				addTableItems(frenchSentence, englishSentence);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<String, HashMap<String, CountBag>> getTable(){
		return this.table;
	}
	
	public HashMap<String, Double> getTotalFrenchItems(){
		return this.total_f; 
	}

	public void initializeSentencePairIterator() {
		this.index = 0;
	}

	public SentencePair next() {
		SentencePair pair = new SentencePair(this.frenchSentenceLs.get(index),
				this.englishSentenceLs.get(index));
		this.index++;
		return pair;
	}

	public boolean hasNext() {
		if (this.index < this.frenchSentenceLs.size()) {
			return true;
		}
		return false;
	}

	private void addTableItems(String frenchSentence, String englishSentence) {
		String[] frenchItems = refine(frenchSentence.split(" "));
		
		this.frenchSentenceLs.add(frenchItems);
		String[] englishItems = refine(englishSentence.split(" "));
		this.englishSentenceLs.add(englishItems);
		
		for (int i = 0; i < frenchItems.length; i++) {
			this.total_f.put(frenchItems[i], new Double(0));
			for (int j = 0; j < englishItems.length; j++) {
				if (this.table.containsKey(englishItems[j])) {
					if (!this.table.get(englishItems[j]).containsKey(frenchItems[i])) {
						this.table.get(englishItems[j]).put(frenchItems[i],
								new CountBag());
					}
				} else {
					HashMap<String, CountBag> value = new HashMap<String, CountBag>();
					value.put(frenchItems[i], new CountBag());
					this.table.put(englishItems[j], value);
				}
			}
		}
	}

	String[] refine(String[] items){
		ArrayList<String> newItems=new ArrayList<String>();
		newItems.add("");
		for(String item:items){
			if(!item.trim().equals("")){
				newItems.add(item.toLowerCase());
			}
		}
		String[] refined=new String[newItems.size()];
		for(int i=0;i<newItems.size();i++){
			refined[i]=	newItems.get(i);		
		}
		return refined;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BilingualCorpus corpus = new BilingualCorpus();
		corpus.buildTables();
		System.out.println(corpus.table.size());
	}

}
