package br.ufal.cideei.util.count;


public abstract class Benchmark {
	protected final boolean featureModel;
	protected final boolean lazy;
	protected final boolean oblivious;
	
	public Benchmark(boolean featureModel) {
		this.featureModel = featureModel;
		this.lazy = false;
		this.oblivious = true;
	}
	
	public Benchmark(boolean featureModel, boolean lazy) {
		this.oblivious = false;
		this.featureModel = featureModel;
		this.lazy = lazy;
	}
	
	public abstract int avgFooterRow();
	
	public abstract int sumFooterRow();
	
	public boolean lazy() { return lazy; }
	
	public boolean featureModel() { return featureModel; }

	public abstract String file();

	public boolean oblivious() { return oblivious; }
		
}
