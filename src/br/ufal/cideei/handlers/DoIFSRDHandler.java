package br.ufal.cideei.handlers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import polyglot.util.CollectionUtil;

import soot.Body;
import soot.G;
import soot.PackManager;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import br.ufal.cideei.algorithms.assignment.AssignmentAlgorithm;
import br.ufal.cideei.algorithms.coa.ChainOfAssignmentAlgorithm;
import br.ufal.cideei.algorithms.declaration.DeclarationAlgorithm;
import br.ufal.cideei.algorithms.unique.UniqueUsesAlgorithm;
import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.soot.SootManager;
import br.ufal.cideei.soot.UnitUtil;
import br.ufal.cideei.soot.analyses.FeatureSensitiveAnalysisRunner;
import br.ufal.cideei.soot.analyses.FeatureSensitiviteFowardFlowAnalysis;
import br.ufal.cideei.soot.analyses.LiftedFlowSet;
import br.ufal.cideei.soot.analyses.TestReachingDefinitions;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachingDefinitions;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.soot.instrument.asttounit.ASTNodeUnitBridge;
import br.ufal.cideei.soot.inter.RD;
import br.ufal.cideei.ui.InfoPopup;
import br.ufal.cideei.util.MethodDeclarationSootMethodBridge;
import br.ufal.cideei.util.SetUtil;
import br.ufal.cideei.visitors.SelectionNodesVisitor;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.ColoredSourceFile;

/**
 * Handler for the br.ufal.cideei.commands.DoCompute extension command.
 * 
 * @author Társis
 * 
 */
public class DoIFSRDHandler extends AbstractHandler implements IHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		/*
		 * In order to perform analyses on the selected code, there are few
		 * thing we need to collect first in order to configure the Soot
		 * framework environment. They are: - Which ASTNodes are in the text
		 * selection - The casspath entry to the package root of the text
		 * selection - The method name which contains the text selection - The
		 * ColoredSourceFile object of the text selection
		 */
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		Shell shell = HandlerUtil.getActiveShellChecked(event);

		if (!(selection instanceof ITextSelection))
			throw new ExecutionException("Not a text selection");

		/*
		 * used to find out the project name and later to create a compilation
		 * unit from it
		 */
		IFile textSelectionFile = (IFile) HandlerUtil.getActiveEditorChecked(event).getEditorInput().getAdapter(IFile.class);
		System.out.println("from handler: " + textSelectionFile);

		// used to compute the ASTNodes corresponding to the text selection
		ITextSelection textSelection = (ITextSelection) selection;

		// this visitor will compute the ASTNodes that were selected by the user
		SelectionNodesVisitor selectionNodesVisitor = new SelectionNodesVisitor(textSelection);
		/*
		 * Now we need to create a compilation unit for the file, and then parse
		 * it to generate an AST in which we will perform our analyses.
		 */
		ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(textSelectionFile);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(compilationUnit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		CompilationUnit jdtCompilationUnit = (CompilationUnit) parser.createAST(null);
		jdtCompilationUnit.accept(selectionNodesVisitor);

		Set<ASTNode> selectionNodes = selectionNodesVisitor.getNodes();

		/*
		 * Not yet used. We'll need to query for the colors(features) associated
		 * with the ASTNodes we are analysing. For now, we are only checking for
		 * the lice number.
		 */
		ColoredSourceFile coloredSourceFile = null;

		/*
		 * Some algorithms might need to compare some features related to
		 * ASTNodes. This is the only current implementation and it provides a
		 * way to query for features from CIDE.
		 */
		IFeatureExtracter extracter = CIDEFeatureExtracterFactory.getInstance().newExtracter();
		// TODO: this wrapping try is for debug only. remove later.
		try {
			/*
			 * Initialize and configure Soot's options and find out which method
			 * contains the selection
			 */
			SootManager.configure(MethodDeclarationSootMethodBridge.getCorrespondentClasspath(textSelectionFile));
			MethodDeclaration methodDeclaration = MethodDeclarationSootMethodBridge.getParentMethod(selectionNodes.iterator().next());
			String declaringMethodClass = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
			MethodDeclarationSootMethodBridge mdsm = new MethodDeclarationSootMethodBridge(methodDeclaration);
			SootMethod sootMethod = SootManager.getMethodBySignature(declaringMethodClass, mdsm.getSootMethodSubSignature());
			Body body = sootMethod.retrieveActiveBody();

			/*
			 * Do Jimple code instrumentation with feature model on the single
			 * body.
			 * 
			 * TODO: only used to test TestReachingDef. Remove later.
			 */
			System.out.println("LIFTED RESULTS:");
			
			long instrStart = System.currentTimeMillis();
			FeatureModelInstrumentorTransformer instrumentorTransformer = FeatureModelInstrumentorTransformer.v(extracter);
			instrumentorTransformer.transform2(body);
			long instrEnd = System.currentTimeMillis();
			System.out.println("instrumentation took: " + (instrEnd - instrStart));

			BriefUnitGraph bodyGraph = new BriefUnitGraph(body);

			this.runTestReachingDefs(bodyGraph, instrumentorTransformer.getPowerSet());

			G.v().reset();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public void runTestReachingDefs(BriefUnitGraph bodyGraph, Collection<Set<String>> configs) {
		long liftedStart = System.currentTimeMillis();
		TestReachingDefinitions tst = new TestReachingDefinitions(bodyGraph, configs);
		long liftedEnd = System.currentTimeMillis();
		System.out.println("Lifted time: " + (liftedEnd - liftedStart));
		Iterator<Unit> iterator = bodyGraph.iterator();
		String format = "|%1$-35s|%2$-30s|%3$-40s|\n";
		while (iterator.hasNext()) {
			Unit unit = (Unit) iterator.next();
			LiftedFlowSet flowAfter = tst.getFlowAfter(unit);

			System.out.format(format, unit, unit.getTag("FeatureTag"), flowAfter);

		}
		/*
		 * UnitUtil.serializeGraph(bodyGraph.getBody(), null);
		 */
	}
}
