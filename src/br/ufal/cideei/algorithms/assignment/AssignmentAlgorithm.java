package br.ufal.cideei.algorithms.assignment;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import br.ufal.cideei.algorithms.BaseAlgorithm;
import br.ufal.cideei.soot.SootManager;
import br.ufal.cideei.soot.instrument.ASTNodeUnitBridge;
import br.ufal.cideei.util.MethodDeclarationSootMethodBridge;
import dk.itu.smartemf.ofbiz.analysis.ReachingDefinition;

/**
 * 
 * This class perform the Assignment algorithm. It will check for uses of a
 * declared variable in a selection and compute nodes the assignment reaches.
 * 
 * To run the algorithm use the {@link #execute()} method to perform the
 * operation and then call {@link #getMessage()} to retrive the output message.
 * 
 * @author Társis
 * 
 */
public class AssignmentAlgorithm extends BaseAlgorithm {

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
	 * @param file
	 *            the file
	 */
	public AssignmentAlgorithm(Set<ASTNode> nodes, CompilationUnit compilationUnit) {
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
		 * 
		 * The parameter are null ass we will reuse this object in the loop and
		 * reset its state before visiting nodes again.
		 */
		ReachedByDefinitionVisitor reachedByDefinitionVisitor2 = new ReachedByDefinitionVisitor(null, null);

		/*
		 * Now for the main loop, we'll simply look for the reached definitions
		 * of every selected definition, and then store it in the message.
		 */

		StringBuilder stringBuilder = new StringBuilder();
		try {
			for (ASTNode node : nodes) {
				reachedByDefinitionVisitor2.reset(reachingDefinition, node);
				methodDeclaration.accept(reachedByDefinitionVisitor2);
				Set<ASTNode> reachedNodes = reachedByDefinitionVisitor2.getReachedNodes();
				for (ASTNode reachedNode : reachedNodes) {
					stringBuilder.append("Provides " + node + " to " + reachedNode + " (line " + compilationUnit.getLineNumber(reachedNode.getStartPosition())
							+ ")\n");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		this.message = stringBuilder.toString();
	}

	/**
	 * An alternate execution for this algorithm. this implementation uses Soot
	 * and a few built-in analysis.
	 * 
	 * @param textSelectionFile
	 *            the text selection file
	 * @throws ExecutionException
	 *             the execution exception
	 * @throws ExecutionException
	 *             the execution exception
	 */
	public void sootExecute(IFile textSelectionFile) throws ExecutionException {
		/*
		 * The following loc are the phase one of the algorithm. We are only
		 * gathering up the necessary information in order to perform the
		 * algorithm itself.
		 */
		SootManager.reset();
		SootManager.configure(this.getCorrespondentClasspath(textSelectionFile));

		MethodDeclaration methodDeclaration = getParentMethod(nodes.iterator().next());
		String declaringMethodClass = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
		MethodDeclarationSootMethodBridge mdsm = new MethodDeclarationSootMethodBridge(methodDeclaration);
		SootMethod sootMethod = SootManager.getMethodBySignature(declaringMethodClass, mdsm.getSootMethodSubSignature());

		Body body = sootMethod.retrieveActiveBody();
		ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);

		/*
		 * We'll use Soot built-in LocalUses and LocalDefs anaylses to compute
		 * which units a given assignments reaches. There is no need for a
		 * ReachingDefinition analysis, as the LocalUses will give us the
		 * information we want.
		 */
		SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(graph);
		SimpleLocalUses simpleLocalUses = new SimpleLocalUses(graph, simpleLocalDefs);

		/*
		 * The input is gathered as ASTNode, so we use the line number from the
		 * source code to convert the nodes into Units.
		 */
		Collection<Integer> lines = ASTNodeUnitBridge.getLinesFromASTNodes(nodes, compilationUnit);
		Collection<Unit> units = ASTNodeUnitBridge.getUnitsFromLines(lines, body);

		/*
		 * Now for the main loop. All we need to do is iterate over the
		 * selection and store in the Map which lines has uses of the given
		 * definition.
		 */
		Map<String, Set<Integer>> variableLineMap = new HashMap<String, Set<Integer>>();
		for (Unit eachUnit : units) {
			for (Object unitBoxPairObj : simpleLocalUses.getUsesOf(eachUnit)) {
				UnitValueBoxPair unitBoxPair = (UnitValueBoxPair) unitBoxPairObj;
				ValueBox valueBox = unitBoxPair.getValueBox();
				Value valueFromBox = valueBox.getValue();
				String variableString = valueFromBox.toString();
				/*
				 * TODO: Is there a better way to check if a Value has its name
				 * created by the Jimple IR or if it is the original name?
				 */
				if (variableString.contains("$") || (!(valueFromBox instanceof Local))) {
					continue;
				}
				
				if (!variableLineMap.containsKey(variableString)) {
					variableLineMap.put(variableString, new HashSet<Integer>());
					variableLineMap.get(variableString).add(ASTNodeUnitBridge.getLineFromUnit(unitBoxPair.getUnit()));
				} else {
					variableLineMap.get(variableString).add(ASTNodeUnitBridge.getLineFromUnit(unitBoxPair.getUnit()));
				}
			}
		}

		/*
		 * Builds the message.
		 */
		StringBuilder stringBuilder = new StringBuilder();
		for (Map.Entry<String, Set<Integer>> varToLines : variableLineMap.entrySet()) {
			String varName = varToLines.getKey();
			for (Integer line : varToLines.getValue()) {
				stringBuilder.append("Provides " + varName + " to line " + line + "\n");
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
