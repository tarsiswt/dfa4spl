package br.ufal.cideei.features;

import org.eclipse.jdt.core.IJavaProject;

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

	public IFeatureExtracter newExtracter(IJavaProject javaProject) {
		return new CIDEFeatureExtracter(javaProject);
	}

}
