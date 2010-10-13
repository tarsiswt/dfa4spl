package br.ufal.cideei.features;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;

import cide.gast.IASTNode;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.language.jdt.ASTBridge;

// TODO: Auto-generated Javadoc
/**
 * A feature extracter implementation for CIDE.
 */
class CIDEFeatureExtracter implements IFeatureExtracter {

	/** The file from which colors from nodes are to be extracted. */
	private IFile file;
	
	public CIDEFeatureExtracter() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ufal.cideei.features.IFeatureExtracter#getFeatures(org.eclipse.jdt
	 * .core.dom.ASTNode)
	 */
	// TODO: treat exception correctly
	@Override
	public Set<String> getFeaturesNames(ASTNode node,IFile file) {
		Set<IFeature> cideFeatureSet = this.getFeatures(node, file);
		Set<String> stringFeatureSet = new HashSet<String>(cideFeatureSet.size());
		for (IFeature feature : cideFeatureSet) {
			String featureName = feature.getName();
			if (!stringFeatureSet.contains(featureName)) {
				stringFeatureSet.add(featureName);
			}
		}
		return stringFeatureSet;
	}
	
	@Override
	public Set<IFeature> getFeatures(ASTNode node,IFile file) {
		ColoredSourceFile coloredFile;
		try {
			coloredFile = ColoredSourceFile.getColoredSourceFile(file);
		} catch (FeatureModelNotFoundException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
		IASTNode iASTNode = ASTBridge.bridge(node);
		Set<IFeature> cideFeatureSet = coloredFile.getColorManager().getColors(iASTNode);
		return cideFeatureSet;
	}
	
	

}
