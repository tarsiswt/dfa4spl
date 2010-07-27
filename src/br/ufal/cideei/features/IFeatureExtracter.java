package br.ufal.cideei.features;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;

public interface IFeatureExtracter {
	
	public Set<String> getFeatures(ASTNode node);

}
