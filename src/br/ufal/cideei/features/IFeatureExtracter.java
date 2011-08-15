package br.ufal.cideei.features;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;

import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;

// TODO: Auto-generated Javadoc
/**
 * The Interface IFeatureExtracter defines the contract for retrieving feature information from ASTNodes.
 * 
 * TODO: needs some flexibility if this is to used with different components other than CIDE.
 */
public interface IFeatureExtracter {

	/**
	 * Gets the features.
	 * 
	 * @param node
	 *            the node
	 * @return the features
	 */
	public Set<String> getFeaturesNames(ASTNode node, IFile file);

	public Set<IFeature> getFeatures(ASTNode node, IFile file);

	boolean isValid(Set<IFeature> config) throws FeatureModelNotFoundException;

}
