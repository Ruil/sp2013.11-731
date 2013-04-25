package type;


public class Hypothesis implements Comparable<Hypothesis>{
	public double logprob;
	public String lm_state;
	public Hypothesis predecessor;
	public Phrase phrase;
	public Hypothesis(double logprob, String lm_state, Hypothesis predecessor, Phrase phrase) {
		this.logprob=logprob;
		this.lm_state=lm_state;
		this.predecessor=predecessor;
		this.phrase=phrase;
	}

	@Override
	public int compareTo(Hypothesis arg0) {
		if(arg0.logprob>this.logprob){
			return 1;
		}else if(arg0.logprob<this.logprob){
			return -1;
		}
		return 0;
	}


}
