package type;

public class LMItem {

	public String text;
	public double logprob;

	public LMItem(String text, double logprob) {
		this.text = text;
		this.logprob = logprob;
	}

}
