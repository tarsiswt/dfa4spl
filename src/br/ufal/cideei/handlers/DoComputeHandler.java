package br.ufal.cideei.handlers;

import java.util.HashSet;
import java.util.Iterator;
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

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;

import br.ufal.cideei.algorithms.assignment.AssignmentAlgorithm;
import br.ufal.cideei.algorithms.coa.ChainOfAssignmentAlgorithm;
import br.ufal.cideei.algorithms.declaration.DeclarationAlgorithm;
import br.ufal.cideei.algorithms.unique.UniqueUsesAlgorithm;
import br.ufal.cideei.features.CIDEFeatureExtracter;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.soot.SootManager;
import br.ufal.cideei.soot.analyses.FeatureSensitiveAnalysisRunner;
import br.ufal.cideei.soot.analyses.FeatureSensitiviteFowardFlowAnalysis;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachingDefinitions;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.ui.InfoPopup;
import br.ufal.cideei.util.MethodDeclarationSootMethodBridge;
import br.ufal.cideei.visitors.SelectionNodesVisitor;
import de.ovgu.cide.features.source.ColoredSourceFile;

/**
 * Handler for the br.ufal.cideei.commands.DoCompute extension command.
 * 
 * @author Társis
 * 
 */
public class DoComputeHandler extends AbstractHandler implements IHandler {

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
		IFeatureExtracter extracter = new CIDEFeatureExtracter(textSelectionFile);
		// TODO: this wrapping try is for debug only. remove later.
		try {

			SootManager.configure(MethodDeclarationSootMethodBridge.getCorrespondentClasspath(textSelectionFile));
			MethodDeclaration methodDeclaration = MethodDeclarationSootMethodBridge.getParentMethod(selectionNodes.iterator().next());
			String declaringMethodClass = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
			MethodDeclarationSootMethodBridge mdsm = new MethodDeclarationSootMethodBridge(methodDeclaration);
			SootMethod sootMethod = SootManager.getMethodBySignature(declaringMethodClass, mdsm.getSootMethodSubSignature());
			Body body = sootMethod.retrieveActiveBody();

			FeatureModelInstrumentorTransformer.v(extracter).transform2(body);
			/*
			 * TODO: For testing purposes only: manually define a set of valid
			 * configurations. This will probably be user input, so a parser
			 * will be needed.
			 */
			Set<Object> configuration1 = new HashSet<Object>();
			configuration1.add("A");
			Set<Object> configuration2 = new HashSet<Object>();
			configuration2.add("B");
			Set<Set<Object>> configurations = new HashSet<Set<Object>>();
			configurations.add(configuration1);
			configurations.add(configuration2);

			/*
			 * The analysis are ran for every configuration in the
			 * configurations set automatically by the AnalysisRunner.
			 */
			long start = System.currentTimeMillis();
			FeatureSensitiveAnalysisRunner runner = new FeatureSensitiveAnalysisRunner(new BriefUnitGraph(body), configurations,
					FeatureSensitiveReachingDefinitions.class);
			runner.execute();
			long end = System.currentTimeMillis();
			System.out.println("Execution time for the FeatureSensitiveReachingDefinitions analysis is " + (end-start) + "ms");

			/*
			 * TODO: For testing purposes only: print the analysis output
			 */
			/*Map<Set<Object>, FeatureSensitiviteFowardFlowAnalysis> results = runner.getResults();
			for (Entry<Set<Object>, FeatureSensitiviteFowardFlowAnalysis> entry : results.entrySet()) {
				System.out.println("configuration set:" + entry.getKey());
				FeatureSensitiviteFowardFlowAnalysis value = entry.getValue();

				Iterator<Unit> iterator = body.getUnits().iterator();
				while (iterator.hasNext()) {
					Unit unit = (Unit) iterator.next();
					System.out.println(unit + " " + ((FeatureTag) unit.getTag("FeatureTag")).getFeatures());
					System.out.println(value.getFlowAfter(unit));
					System.out.println("---");
				}
				System.out.println("===");
			}*/

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
}
