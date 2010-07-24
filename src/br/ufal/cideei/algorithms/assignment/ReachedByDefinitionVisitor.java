package br.ufal.cideei.algorithms.assignment;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import dk.itu.smartemf.ofbiz.analysis.Pair;
import dk.itu.smartemf.ofbiz.analysis.ReachingDefinition;

// TODO: Auto-generated Javadoc
/**
 * The Class ReachedByDefinitionVisitor.
 */
public class ReachedByDefinitionVisitor extends ASTVisitor {

	/** The Wrapper class the for the Reaching Definition analysis. */
	private ReachingDefinition reachingDefinition;

	/**
	 * The definition node. We are looking for the nodes that this definition
	 * reaches.
	 */
	private ASTNode definitionNode;

	/**
	 * The reached nodes. The nodes that can be reached by the provided
	 * definition should be placed here.
	 */
	private Set<ASTNode> reachedNodes = new HashSet<ASTNode>();

	/**
	 * Gets the reached nodes.
	 * 
	 * @return the reached nodes
	 */
	public Set<ASTNode> getReachedNodes() {
		return reachedNodes;
	}

	/**
	 * Resets the state of the object. This includes substiting the Reaching
	 * Definition, the definition node and clearing the reached nodes set,
	 * 
	 * @param rda
	 *            the rda
	 * @param definitionNode
	 *            the definition node
	 */
	public void reset(ReachingDefinition rda, ASTNode definitionNode) {
		this.reachingDefinition = rda;
		this.definitionNode = definitionNode;
		reachedNodes.clear();
	}

	/**
	 * Instantiates a new reached by definition visitor.
	 * 
	 * @param rda
	 *            the rda
	 * @param declarationNode
	 *            the declaration node
	 */
	public ReachedByDefinitionVisitor(ReachingDefinition rda, ASTNode declarationNode) {
		this.definitionNode = declarationNode;
		this.reachingDefinition = rda;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#preVisit2(org.eclipse.jdt.core.dom
	 * .ASTNode)
	 */
	public boolean preVisit2(ASTNode node) {
		/*
		 * Filter out unwanted nodes.
		 */
		if (!((node instanceof Expression) || (node instanceof Statement) || (node instanceof VariableDeclaration))) {
			return true;
		}
		if (node instanceof Block) {
			return true;
		}
		
		/*
		 * For every visited node, we must compute which nodes this definition reaches.
		 */
		Set<Pair<String, ASTNode>> reachingDefsPairs = reachingDefinition.getReachingDefsAt(node);
		for (Pair pair : reachingDefsPairs) {
			ASTNode reachingDef = (ASTNode) pair.second;
			if (reachingDef.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
				Expression expr = ((ExpressionStatement) reachingDef).getExpression();
				if (expr.equals(definitionNode)) {
					if (!(expr instanceof Assignment)) {
						return true;
					}
					reachedNodes.add(node);
				}
			} else {
				if (reachingDef.equals(definitionNode)) {
					reachedNodes.add(node);
				}
			}
		}
		return true;
	}
}
