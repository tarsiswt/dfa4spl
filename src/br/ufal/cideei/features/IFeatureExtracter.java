package br.ufal.cideei.features;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

// TODO: Auto-generated Javadoc
/**
 * The Interface IFeatureExtracter.
 */
public interface IFeatureExtracter {
	
	/**
	 * Gets the features.
	 *
	 * @param node the node
	 * @return the features
	 */
	public Set<String> getFeatures(ASTNode node);

}
