package br.ufal.cideei.visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.text.ITextSelection;

public class SelectionNodesVisitor extends ASTVisitor {

	private ITextSelection textSelection;
	private Set<ASTNode> nodes = new HashSet<ASTNode>();

	private SelectionNodesVisitor() {
	}

	public SelectionNodesVisitor(ITextSelection textSelection) {
		this.textSelection = textSelection;
	}
	
	public Set<ASTNode> getNodes(){
		return nodes;
	}

	public void preVisit(ASTNode node) {
		super.preVisit(node);
		if (node.getStartPosition() >= textSelection.getOffset() && 
				(node.getStartPosition() + node.getLength()) <= textSelection.getOffset() + textSelection.getLength()) {
			nodes.add(node);
		}
	}
}
