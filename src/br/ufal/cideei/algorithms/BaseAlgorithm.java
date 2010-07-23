package br.ufal.cideei.algorithms;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public abstract class BaseAlgorithm implements IAlgorithm {
	protected MethodDeclaration getParentMethod(ASTNode node) {
		if (node == null) {
			return null;
		} else {
			if (node.getNodeType() == ASTNode.METHOD_DECLARATION) {
				return (MethodDeclaration) node;
			} else {
				return getParentMethod(node.getParent());
			}
		}
	}

	
}
