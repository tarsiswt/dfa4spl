package br.ufal.cideei.algorithms.assignment;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import br.ufal.cideei.algorithms.BaseAlgorithm;
import br.ufal.cideei.algorithms.BaseSootAlgorithm;
import br.ufal.cideei.algorithms.IAlgorithm;
import de.ovgu.cide.features.source.ColoredSourceFile;
import dk.itu.smartemf.ofbiz.analysis.ReachingDefinition;

/**
 * 
 * This class perform the Assignment algorithm. It will check for uses of a
 * declared variable in a selection and compute nodes the assignment reaches.
 * 
 * 
 * To run the algorithm use the {@link #execute()} method to perform the
 * operation and then call {@link #getMessage()} to retrive the output message.
 * 
 * @author Társis
 * 
 */
public class AssignmentAlgorithm extends BaseAlgorithm {

	/** The file. */
	private ColoredSourceFile file;

	/** The nodes. */
	private Set<ASTNode> nodes;

	/** The compilation unit. */
	private CompilationUnit compilationUnit;

	/** The message. */
	private String message = "";

	/**
	 * Instantiates a new assignment algorithm.
	 * 
	 * @param nodes
	 *            the nodes
	 * @param compilationUnit
	 *            the compilation unit
	 * @param file
	 *            the file
	 */
	public AssignmentAlgorithm(Set<ASTNode> nodes, CompilationUnit compilationUnit, ColoredSourceFile file) {
		this.file = file;
		this.nodes = nodes;
		this.compilationUnit = compilationUnit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ufal.cideei.algorithms.IAlgorithm#execute()
	 */
	@Override
	public void execute() {

		/*
		 * We start off by computing the method in which we wish to perform our
		 * computation
		 */
		MethodDeclaration methodDeclaration = getParentMethod(nodes.iterator().next());

		/*
		 * This class ReachingDefinition class wraps around the the actual
		 * Reaching Definition analysis, enabling us to query for the
		 * definitions that reaches a give Statement
		 */
		ReachingDefinition reachingDefinition = new ReachingDefinition(methodDeclaration);

		/*
		 * ReachedByDefinitionVisitor is a ASTVisitor that will perform as if an
		 * "inverted" Reaching Definition. That is, it will visit the subtree of
		 * an ASTNode (in this case the method) and compute which nodes are
		 * reached by a given reaching definition.
		 */
		ReachedByDefinitionVisitor reachedByDefinitionVisitor = new ReachedByDefinitionVisitor(null, null);

		/*
		 * Now for the main loop, we'll simply look for the reached definitions
		 * of every selected definition, and then store it in the message.
		 */
		StringBuilder stringBuilder = new StringBuilder();
		for (ASTNode node : nodes) {
			if (node instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement declarationNode = (VariableDeclarationStatement) node;
				reachedByDefinitionVisitor.reset(reachingDefinition, declarationNode);
				methodDeclaration.accept(reachedByDefinitionVisitor);
				Set<ASTNode> reachedNodes = reachedByDefinitionVisitor.getReachedNodes();
				for (ASTNode reachedNode : reachedNodes) {
					stringBuilder.append("Provides " + declarationNode + " to line " + compilationUnit.getLineNumber(reachedNode.getStartPosition()) + "\n");
				}
			}
		}
		this.message = stringBuilder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ufal.cideei.algorithms.IAlgorithm#getMessage()
	 */
	@Override
	public String getMessage() {
		return message;
	}

}
