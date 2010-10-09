package br.ufal.cideei.features;

public abstract class AbstractFeatureExtracterFactory {
	protected AbstractFeatureExtracterFactory() {
	}
	
	public abstract IFeatureExtracter newExtracter();

}
