/*
 * 
 */
package br.ufal.cideei.features;

/**
 * A factory for creating CIDEFeatureExtracter objects.
 */
public class CIDEFeatureExtracterFactory {

	/** The instance. */
	static CIDEFeatureExtracterFactory instance = new CIDEFeatureExtracterFactory();

	/**
	 * Instantiates a new cIDE feature extracter factory.
	 */
	private CIDEFeatureExtracterFactory() {
		super();
	}

	/**
	 * Gets the single instance of CIDEFeatureExtracterFactory.
	 * 
	 * @return single instance of CIDEFeatureExtracterFactory
	 */
	public static CIDEFeatureExtracterFactory getInstance() {
		return instance;
	}

	// XXX: check for unecessary duplicate methods
	/**
	 * New extracter.
	 * 
	 * @return the i feature extracter
	 */
	public IFeatureExtracter newExtracter() {
		return new CIDEFeatureExtracter();
	}
}
