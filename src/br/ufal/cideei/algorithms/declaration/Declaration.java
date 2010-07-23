package br.ufal.cideei.algorithms.declaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.ValueBox;
import soot.jimple.Jimple;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.LocalUses;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.util.Chain;
import br.ufal.cideei.algorithms.BaseSootAlgorithm;
import br.ufal.cideei.algorithms.IAlgorithm;
import br.ufal.cideei.soot.SootManager;
import de.ovgu.cide.features.source.ColoredSourceFile;

/**
 * 
 * This class perform the Declaration algorithms. It will check for uses of a
 * declared variable in a selection and will display the lines in which the
 * variable is referenced.
 * 
 * This is only a preliminary version, and the class hierarchy hasn't been
 * defined yet.
 * 
 * To run the algorithm use the {@link #execute()} method to perform the
 * operation and then call {@link #getMessage()} to retrive the output message.
 * 
 * @author Társis
 * 
 */
public class Declaration extends BaseSootAlgorithm {

	/** Will be set at the end of the execute method. */
	private String message = null;

	/** The nodes in which we will perform the analysis. */
	private Set<ASTNode> nodes = null;

	/** Not used. */
	private ColoredSourceFile file = null;

	/** The compilation unit which we retrieve the lines from the ASTNodes. */
	private CompilationUnit compilationUnit = null;

	/**
	 * Disables default constructor
	 */
	private Declaration() {
	}

	/**
	 * Instantiates a new declaration.
	 * 
	 * @param nodes
	 *            the nodes
	 * @param compilationUnit
	 *            the compilation unit
	 * @param file
	 *            the file
	 */
	public Declaration(Set<ASTNode> nodes, CompilationUnit compilationUnit, ColoredSourceFile file) {
		this.file = file;
		this.nodes = nodes;
		this.compilationUnit = compilationUnit;
	}

	/**
	 * Gets the message.
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Execute the algorithm. Use {@link #getMessage()} to see the results.
	 */
	public void execute() {
		if (nodes.isEmpty()) {
			return;
		}

		/*
		 * This visitor will search for VariableDeclarationFragment in the
		 * acceptant node. Every ASTNode from the selection must be visited by
		 * this visitor.
		 */
		DeclarationVisitor declarationVisitor = new DeclarationVisitor();
		Set<SimpleName> simpleNameDeclarations = new HashSet<SimpleName>();

		Iterator<ASTNode> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			ASTNode node = iterator.next();
			node.accept(declarationVisitor);
			if (declarationVisitor.found()) {
				simpleNameDeclarations.add(declarationVisitor.getName());
			}
			declarationVisitor.reset();
		}

		/*
		 * To compute the method in which the selection was made we only take
		 * into consideration the first one. TODO: check this later
		 */
		MethodDeclaration methodDeclarationNode = getParentMethod(nodes.iterator().next());

		/*
		 * This visitor will look for uses of the SimpleNames from the
		 * VariableDeclarationFragment we retrieve from the last visitor.
		 * There's no need to iterate on the nodes because the Visitor is
		 * implemented to iterate over the Set.
		 */
		DeclarationSimpleNameVisitor declarationSimpleNameVisitor = new DeclarationSimpleNameVisitor(simpleNameDeclarations);
		methodDeclarationNode.accept(declarationSimpleNameVisitor);
		Map<SimpleName, Set<SimpleName>> declarationMap = declarationSimpleNameVisitor.getDeclarationMaps();
		StringBuilder stringBuilder = new StringBuilder();

		/*
		 * Now for each declaration we found, we get every corresponding use (in
		 * which line)
		 */
		for (SimpleName simpleName : simpleNameDeclarations) {
			Set<SimpleName> simpleNameReferences = declarationMap.get(simpleName);
			for (SimpleName simpleNameReference : simpleNameReferences) {
				stringBuilder.append("provides " + simpleName.getIdentifier() + " to line "
						+ compilationUnit.getLineNumber(simpleNameReference.getStartPosition()) + "\n");
			}
		}

		this.message = stringBuilder.toString();
	}

	public void executeWithSoot(IFile textSelectionFile) throws ExecutionException {
		if (nodes.isEmpty()) {
			return;
		}
		
		/*
		 * used to find out what the classpath entry related to the IFile of the
		 * text selection. this is necessary for some algorithms that might use
		 * the Soot framework
		 */
		String pathToSourceClasspathEntry = this.getCorrespondentClasspath(textSelectionFile);

		/*
		 * To compute the method in which the selection was made we only take
		 * into consideration the first one. TODO: check this later
		 */
		MethodDeclaration methodDeclaration = getParentMethod(nodes.iterator().next());

		/*
		 * We need both the method name and it's declaring class in order to query
		 * for information withing Soot.
		 */
		String methodDeclarationName = methodDeclaration.getName().getIdentifier();
		String declaringMethodClass = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();

		SootManager.configure(pathToSourceClasspathEntry);
		SootMethod sootMethod = SootManager.getMethod(declaringMethodClass, methodDeclarationName);
		Body activeBody = sootMethod.retrieveActiveBody();

		/*
		 * This is the core of the algorithm implementation with Soot.
		 * We need to relate the ASTNodes that were computed earlier from text selection
		 * to Units. To do that we used the lines from the source code.
		 * 
		 * Then for every Unit that was selected we need to find out what they do declare.
		 * 
		 * Now, knowing which Locals where declared on those units, we need to search in all the
		 * Units in the method for uses of that declaration.
		 * 
		 * For every use found, we print the message.
		 * 
		 * This approach has several drawbacks compared to the other implementation:
		 * * It requires a lot less code
		 * * It is reasonably slower(TODO: needs some testing on this)
		 * * The following code seems to capture the variable declarations that are temporary
		 *   and were created by the compilation of the source.
		 * * It does not take into account declarations like: String str;
		 *   Probably something related to the underlying IR.
		 *   
		 * Thus, this implementation, as is, is not recommended for use in the tool.
		 *   
		 */
		UnitGraph unitGraph = new BriefUnitGraph(activeBody);
		LocalDefs localDefs = new SimpleLocalDefs(unitGraph);
		SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);

		Collection<Integer> lines = getLinesFromASTNodes(nodes, compilationUnit);
		Collection<Unit> selectUnits = getUnitsFromLines(lines, activeBody);

		UnitGraph unitGraph2 = new ExceptionalUnitGraph(activeBody);
		for (Unit eachSelectedUnit : selectUnits) {
			for (ValueBox vbox : eachSelectedUnit.getDefBoxes()) {
				Local localDefinition = (Local) vbox.getValue();
				for (Unit eachUnitInBody : activeBody.getUnits()) {
					List usesInEachUnit = localUses.getUsesOf(eachUnitInBody);
					for (Object unitValuePairObj : usesInEachUnit) {
						UnitValueBoxPair unitValuePair = (UnitValueBoxPair) unitValuePairObj;
						if (((Local) (unitValuePair.getValueBox().getValue())).equivTo(localDefinition)) {
							System.out.println("Provides " + localDefinition.getName() + " to line " + getLineFromUnit(unitValuePair.getUnit()).toString());
						}
					}
				}
			}
		}
	}
}
