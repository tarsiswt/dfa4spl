/*
 * 
 */
package br.ufal.cideei.features;

/**
 * A factory for creating CIDEFeatureExtracter objects. But as CIDEFeatureExtracter objects are immutable classes and
 * contains no state, this class simply provides a way to retrieve the single CIDEFeatureExtracter instance needed.
 */
public class CIDEFeatureExtracterFactory {

	/** The instance. */
	static CIDEFeatureExtracterFactory instance = new CIDEFeatureExtracterFactory();

	static CIDEFeatureExtracter extracterInstance = new CIDEFeatureExtracter();

	/**
	 * Defeats instantiation.
	 */
	private CIDEFeatureExtracterFactory() {
	}

	/**
	 * Gets the single instance of CIDEFeatureExtracterFactory.
	 * 
	 * @return single instance of CIDEFeatureExtracterFactory
	 */
	public static CIDEFeatureExtracterFactory getInstance() {
		return instance;
	}

	/**
	 * Returns an object that implements the IFeatureExtracter interface.
	 * 
	 * @return the feature extracter
	 */
	public IFeatureExtracter getExtracter() {
		return extracterInstance;
	}
}
