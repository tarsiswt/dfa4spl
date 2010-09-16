package br.ufal.cideei.features;

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
 * The Class CIDEFeatureExtracter.
 */
public class CIDEFeatureExtracter implements IFeatureExtracter {

	/** The file. */
	private IFile file;

	/**
	 * Instantiates a new CIDE feature extracter.
	 * 
	 * @param file
	 *            the file
	 */
	public CIDEFeatureExtracter(IFile file) {
		this.file = file;
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
	public Set<String> getFeatures(ASTNode node) {
		ColoredSourceFile coloredFile;
		try {
			coloredFile = ColoredSourceFile.getColoredSourceFile(file);
		} catch (FeatureModelNotFoundException e) {
			e.printStackTrace();
			return new HashSet<String>();
		}
		IASTNode iASTNode = ASTBridge.bridge(node);
		Set<IFeature> cideFeatureSet = coloredFile.getColorManager().getColors(iASTNode);
		Set<String> stringFeatureSet = new HashSet<String>(cideFeatureSet.size());
		for (IFeature feature : cideFeatureSet) {
			String featureName = feature.getName();
			if (!stringFeatureSet.contains(featureName)) {
				stringFeatureSet.add(featureName);
			}
		}
		return stringFeatureSet;
	}

}
