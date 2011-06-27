/*
 * 
 */
package br.ufal.cideei.features;

import org.eclipse.jdt.core.IJavaProject;

// TODO: Auto-generated Javadoc
//FIXME: terrible class structre. Think Abstract Factory.
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

	/**
	 * New extracter.
	 * 
	 * @param javaProject
	 *            the java project
	 * @return the i feature extracter
	 */
	public IFeatureExtracter newExtracter(IJavaProject javaProject) {
		return new CIDEFeatureExtracter();
	}

}
