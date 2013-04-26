package type;

public class SentencePair {

	private String[] french;
	private String[] english;

	public SentencePair(String[] french, String[] english) {
		this.french = french;
		this.english = english;
	}

	public String[] getFrench() {
		return this.french;
	}

	public String[] getEnglish() {
		return this.english;
	}
}
