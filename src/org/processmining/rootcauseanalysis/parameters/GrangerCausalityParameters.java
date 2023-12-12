package org.processmining.rootcauseanalysis.parameters;

public class GrangerCausalityParameters {

	public static int DEFAULT_MINIMALSAMPLESIZE;
	public static int DEFAULT_MAXH;
	public static int DEFAULT_BETA;

	static {
		DEFAULT_MINIMALSAMPLESIZE = 100;
		DEFAULT_MAXH = Integer.MAX_VALUE;
		DEFAULT_BETA = 10;
	}

	// FIELDS

	private int minimumSampleSize;
	private int maxH;
	private int beta;

	// CONSTRUCTORS

	public GrangerCausalityParameters() {
		setMinimumSampleSize(DEFAULT_MINIMALSAMPLESIZE);
		setMaxH(DEFAULT_MAXH);
		setBeta(DEFAULT_BETA);
	}

	// GETTERS AND SETTERS

	public int getMinimumSampleSize() {
		return minimumSampleSize;
	}

	public void setMinimumSampleSize(int minimumSampleSize) {
		this.minimumSampleSize = minimumSampleSize;
	}

	public int getMaxH() {
		return maxH;
	}

	public void setMaxH(int maxH) {
		this.maxH = maxH;
	}

	public int getBeta() {
		return beta;
	}

	public void setBeta(int beta) {
		this.beta = beta;
	}

}
