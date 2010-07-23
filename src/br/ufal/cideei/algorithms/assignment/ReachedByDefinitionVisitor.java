package br.ufal.cideei.algorithms.assignment;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import dk.itu.smartemf.ofbiz.analysis.Pair;
import dk.itu.smartemf.ofbiz.analysis.ReachingDefinition;

public class ReachedByDefinitionVisitor extends ASTVisitor {
	private ReachingDefinition rda;
	private VariableDeclarationStatement declarationNode;
	private Set<ASTNode> reachedNodes = new HashSet<ASTNode>();

	public Set<ASTNode> getReachedNodes() {
		return reachedNodes;
	}

	public void reset(ReachingDefinition rda, VariableDeclarationStatement declarationNode) {
		this.rda = rda;
		this.declarationNode = declarationNode;
		reachedNodes.clear();
	}

	public ReachedByDefinitionVisitor(ReachingDefinition rda, VariableDeclarationStatement declarationNode) {
		this.declarationNode = declarationNode;
		this.rda = rda;
	}

	public boolean preVisit2(ASTNode node) {
		if (!(node instanceof VariableDeclarationStatement)) {
			return true;
		}
		Set<Pair<String, ASTNode>> reachingDefsPairs = rda.getReachingDefsAt((VariableDeclarationStatement) node);
		for (Pair pair : reachingDefsPairs) {
			ASTNode reachingDef = (ASTNode) pair.second;
			if (reachingDef.equals(declarationNode)) {
				reachedNodes.add(node);
			}
		}
		return true;
	}
}
