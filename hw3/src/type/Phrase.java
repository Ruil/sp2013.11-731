package type;

public class Phrase implements Comparable<Phrase>{
	public String text;
	public double logprob;
	public Phrase(String text, double logprob) {
		this.text=text;
		this.logprob=logprob;
	}
	@Override
	public int compareTo(Phrase arg0) {
		if(arg0.logprob>this.logprob){
			return 1;
		}else if(arg0.logprob<this.logprob){
			return -1;
		}
		return 0;
	}

}
