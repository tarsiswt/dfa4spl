package br.ufal.cideei.algorithms.assignment;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.infoflow.FakeJimpleLocal;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SmartLocalDefs;
import br.ufal.cideei.algorithms.BaseSootAlgorithm;
import br.ufal.cideei.soot.SootManager;
import br.ufal.cideei.soot.analyses.Definition;
import br.ufal.cideei.soot.analyses.SimpleReachingDefinitions;
import br.ufal.cideei.soot.analyses.SimpleReachingDefinitionsAnalysis;
import br.ufal.cideei.soot.analyses.SimpleReachingDefinitionsRmk;
import de.ovgu.cide.features.source.ColoredSourceFile;
import dk.itu.smartemf.ofbiz.analysis.*;

public class Assignment extends BaseSootAlgorithm {

	private ColoredSourceFile file;
	private Set<ASTNode> nodes;
	private CompilationUnit compilationUnit;
	private StringBuilder messageBuilder = new StringBuilder();

	public Assignment(Set<ASTNode> nodes, CompilationUnit compilationUnit, ColoredSourceFile file) {
		this.file = file;
		this.nodes = nodes;
		this.compilationUnit = compilationUnit;
	}

	@Override
	public void execute() {
		MethodDeclaration methodDeclaration = getParentMethod(nodes.iterator().next());
		ReachingDefinition rda = new ReachingDefinition(methodDeclaration);
		for (ASTNode node : nodes) {
			if (node instanceof Statement) {
				Set<Pair<String,ASTNode>> reachingSet =  rda.getReachingDefsAt((Statement) node);
				for (Pair pair : reachingSet){
					System.out.println(pair.toString());
				}
			}
		}
	}

	public void executeWithSoot(IFile textSelectionFile) throws ExecutionException {
		SootManager.reset();
		SootManager.configure(this.getCorrespondentClasspath(textSelectionFile));

		MethodDeclaration methodDeclaration = getParentMethod(nodes.iterator().next());
		String methodDeclarationName = methodDeclaration.getName().getIdentifier();
		String declaringMethodClass = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
		SootMethod sootMethod = SootManager.getMethod(declaringMethodClass, methodDeclarationName);

		Body body = sootMethod.retrieveActiveBody();
		SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(new ExceptionalUnitGraph(body));

		Collection<Integer> lines = this.getLinesFromASTNodes(nodes, compilationUnit);
		Collection<Unit> units = this.getUnitsFromLines(lines, body);

		UnitGraph unitGraph = new BriefUnitGraph(sootMethod.retrieveActiveBody());
		System.out.println(body.getLocals());

		/*
		 * SimpleReachingDefinitionsRmk analysis = new
		 * SimpleReachingDefinitionsRmk(unitGraph); for (Unit eachSelectedUnit :
		 * units) { FlowSet after = analysis.getFlowBefore(eachSelectedUnit);
		 * System.out.println("-----unit-----");
		 * System.out.println(eachSelectedUnit);
		 * System.out.println("---reachings--"); for (Object unitObj :
		 * after.toList()) { if (unitObj instanceof AssignStmt) { //
		 * System.out.println("reaching def @ line " + ((SourceLnPosTag)
		 * eachSelectedUnit.getTag("SourceLnPosTag")).startLn() + ":" +
		 * ((AssignStmt)unitObj)); System.out.println(unitObj); } }
		 * System.out.println(); }
		 */

		/*
		 * SimpleReachingDefinitions simpleReachingDefinitions = new
		 * SimpleReachingDefinitions(unitGraph); for (Unit eachSelectedUnit :
		 * units){ List<Definition> reachingDefinitions =
		 * simpleReachingDefinitions
		 * .getReachingDefinitionsBefore(eachSelectedUnit); for (Definition
		 * definition : reachingDefinitions){ messageBuilder.append("\n" +
		 * definition.getLocal() + "[ reaches ]" +
		 * ((SourceLnPosTag)definition.getDefStatement
		 * ().getTag("SourceLnPosTag")).startLn()); } }
		 */

		/*
		 * SimpleLiveLocals lives = new SimpleLiveLocals(unitGraph);
		 * SmartLocalDefs smartDefs = new SmartLocalDefs(unitGraph, lives);
		 * 
		 * Iterator bodyIterator = body.getUnits().iterator(); while
		 * (bodyIterator.hasNext()){ Stmt bodyStmt = (Stmt)bodyIterator.next();
		 * //System.out.println("stmt: "+s); Iterator useBoxesIterator =
		 * bodyStmt.getUseBoxes().iterator(); while
		 * (useBoxesIterator.hasNext()){ ValueBox valueBox =
		 * (ValueBox)useBoxesIterator.next(); if (valueBox.getValue() instanceof
		 * Local) { Local usedLocal = (Local)valueBox.getValue(); if (usedLocal
		 * instanceof FakeJimpleLocal){ System.out.println("fake!"); }
		 * //System.out.println("local: "+l); Iterator rDefsIt =
		 * simpleLocalDefs.getDefsOfAt(usedLocal, bodyStmt).iterator(); while
		 * (rDefsIt.hasNext()){ Stmt localDefOnUnit = (Stmt)rDefsIt.next(); if
		 * (localDefOnUnit.hasTag("SourceLnPosTag")) { SourceLnPosTag lineTag =
		 * (SourceLnPosTag) localDefOnUnit.getTag("SourceLnPosTag"); if (lineTag
		 * != null) { String info = usedLocal+" reaches " + lineTag.startLn();
		 * messageBuilder.append("\n" + info); } } } } } }
		 */
	}

	@Override
	public String getMessage() {
		return messageBuilder.toString();
	}

}
