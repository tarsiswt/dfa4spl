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
public class Declaration {

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
		IProject project = textSelectionFile.getProject();
		IJavaProject javaProject = null;

		try {
			if (textSelectionFile.getProject().isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				javaProject = JavaCore.create(project);
			}
		} catch (CoreException e) {
			e.printStackTrace();
			throw new ExecutionException("Not a Java Project");
		}

		/*
		 * When using the Soot framework, we need the path to the package root
		 * in which the file is located. There may be other ways to acomplish
		 * this. TODO look for optimal way of finding it.
		 */

		String pathToSourceClasspathEntry = null;
		String pathToSourceFile = ResourcesPlugin.getWorkspace().getRoot().getFile(textSelectionFile.getFullPath()).getLocation().toOSString();

		IClasspathEntry[] classPathEntries = null;
		try {
			classPathEntries = javaProject.getResolvedClasspath(true);
			for (IClasspathEntry entry : classPathEntries) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					pathToSourceClasspathEntry = ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath()).getLocation().toOSString();
					break;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException("No source classpath identified");
		}

		/*
		 * To compute the method in which the selection was made we only take
		 * into consideration the first one. TODO: check this later
		 */
		MethodDeclaration methodDeclaration = getParentMethod(nodes.iterator().next());

		try {
			String methodDeclarationName = methodDeclaration.getName().getIdentifier();
			String declaringMethodClass = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();

			System.out.println(pathToSourceClasspathEntry);
			SootManager.configure(pathToSourceClasspathEntry);
			SootMethod sootMethod = SootManager.getMethod(declaringMethodClass, methodDeclarationName);
			Body activeBody = sootMethod.retrieveActiveBody();

			UnitGraph unitGraph = new BriefUnitGraph(activeBody);
			LocalDefs localDefs = new SimpleLocalDefs(unitGraph);
			SimpleLocalUses localUses = new SimpleLocalUses(unitGraph,localDefs); 

			Collection<Integer> lines = getLinesFromASTNodes(nodes, compilationUnit);
			Collection<Unit> selectUnits = getUnitsFromLines(lines, activeBody);
			
			System.out.println("-----------------------");
			UnitGraph unitGraph2 = new ExceptionalUnitGraph(activeBody);
			for (Unit eachSelectedUnit : selectUnits) {
				for (ValueBox vbox : eachSelectedUnit.getDefBoxes()){
					Local localDefinition = (Local)vbox.getValue();
					for (Unit eachUnitInBody : activeBody.getUnits()){
						List usesInEachUnit = localUses.getUsesOf(eachUnitInBody);
						for (Object unitValuePairObj : usesInEachUnit){
							UnitValueBoxPair unitValuePair = (UnitValueBoxPair)unitValuePairObj;
							if (((Local)(unitValuePair.getValueBox().getValue())).equivTo(localDefinition)){
								System.out.println("Provides " + localDefinition.getName() + " to line " + getLineFromUnit(unitValuePair.getUnit()).toString());
							}
						}
					}
				}
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private Collection<Integer> getLinesFromASTNodes(Collection<ASTNode> nodes, CompilationUnit compilationUnit) {
		Set<Integer> lineSet = new HashSet<Integer>();
		for (ASTNode node : nodes) {
			lineSet.add(compilationUnit.getLineNumber(node.getStartPosition()));
		}
		return lineSet;
	}
	
	private Integer getLineFromUnit(Unit unit){
		if (unit.hasTag("SourceLnPosTag")) {
			SourceLnPosTag lineTag = (SourceLnPosTag) unit.getTag("SourceLnPosTag");
			return lineTag.startLn();
		}
		return null;
	}

	private Collection<Unit> getUnitsFromLines(Collection<Integer> lines, Body body) {
		Set<Unit> unitSet = new HashSet<Unit>();
		for (Integer line : lines) {
			Chain<Unit> units = body.getUnits();
			for (Unit unit : units) {
				if (unit.hasTag("SourceLnPosTag")) {
					SourceLnPosTag lineTag = (SourceLnPosTag) unit.getTag("SourceLnPosTag");
					if (lineTag != null) {
						System.out.println(line + ":" + lineTag.startLn() + ":" + unit.toString());
						if (lineTag.startLn() == line.intValue()) {
							unitSet.add(unit);
						}
					}
				} else {
					System.out.println(line + ":nl:" + unit.toString());
				}
			}
		}
		return unitSet;
	}

	private MethodDeclaration getParentMethod(ASTNode node) {
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
