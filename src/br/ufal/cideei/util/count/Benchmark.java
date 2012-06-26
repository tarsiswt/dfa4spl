package br.ufal.cideei.util.count;


public abstract class Benchmark {
	protected final boolean featureModel;
	protected final boolean lazy;
	
	public Benchmark(boolean featureModel, boolean lazy) {
		this.featureModel = featureModel;
		this.lazy = lazy;
	}
	
	public abstract int avgFooterRow();
	
	public abstract int sumFooterRow();
	
	public boolean lazy() { return lazy; }
	
	public boolean featureModel() { return featureModel; }

	public abstract String file();
		
}
