package br.ufal.cideei.features;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;

import cide.gast.IASTNode;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.language.jdt.ASTBridge;

/**
 * A feature extracter implementation for CIDE.
 */
class CIDEFeatureExtracter implements IFeatureExtracter {

	/** The file from which colors from nodes are to be extracted. */
	private IFile file;
	private IJavaProject javaProject;

	public CIDEFeatureExtracter() {
		// TODO Auto-generated constructor stub
	}

	public CIDEFeatureExtracter(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ufal.cideei.features.IFeatureExtracter#getFeatures(org.eclipse.jdt
	 * .core.dom.ASTNode)
	 */
	@Override
	public Set<String> getFeaturesNames(ASTNode node, IFile file) {
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
	public Set<IFeature> getFeatures(ASTNode node, IFile file) {
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

	@Override
	public boolean isValid(Set<IFeature> config) throws FeatureModelNotFoundException {
		/*
		 * FIXME: simply not working. WHY?
		 */
//		IFeatureModel featureModel = FeatureModelManager.getInstance().getActiveFeatureModelProvider().getFeatureModel(javaProject.getProject());
//		return FeatureModelManager.getInstance().getActiveFeatureModelProvider().getFeatureModel(javaProject.getProject()).isValidSelection(featureModel.getVisibleFeatures());
		return true;
	}

}
