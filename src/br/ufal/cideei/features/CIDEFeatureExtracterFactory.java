package br.ufal.cideei.features;

public class CIDEFeatureExtracterFactory extends AbstractFeatureExtracterFactory {
	
	static CIDEFeatureExtracterFactory instance = new CIDEFeatureExtracterFactory();
	private CIDEFeatureExtracterFactory() {
		super();
	}
	
	public static CIDEFeatureExtracterFactory getInstance() {
		return instance;
	}

	@Override
	public IFeatureExtracter newExtracter() {
		return new CIDEFeatureExtracter();
	}

}
