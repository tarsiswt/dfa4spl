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
import br.ufal.cideei.soot.SootUnitGraphSerializer;
import br.ufal.cideei.soot.UnitUtil;
import br.ufal.cideei.soot.analyses.FeatureSensitiveAnalysisRunner;
import br.ufal.cideei.soot.analyses.FeatureSensitiviteFowardFlowAnalysis;
import br.ufal.cideei.soot.analyses.LiftedFlowSet;
import br.ufal.cideei.soot.analyses.TestReachingDefinitions;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachedDefinitionsFactory;
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
		 * framework environment.They are:
		 * 
		 * - Which ASTNodes are in the text selection
		 * 
		 * - The casspath entry to the package root of the text selection
		 * 
		 * - The method name which contains the text selection
		 * 
		 * - The ColoredSourceFile object of the text selection
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
			String correspondentClasspath = MethodDeclarationSootMethodBridge.getCorrespondentClasspath(textSelectionFile);
			SootManager.configure(correspondentClasspath);
			MethodDeclaration methodDeclaration = MethodDeclarationSootMethodBridge.getParentMethod(selectionNodes.iterator().next());
			String declaringMethodClass = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
			MethodDeclarationSootMethodBridge mdsm = new MethodDeclarationSootMethodBridge(methodDeclaration);
			SootMethod sootMethod = SootManager.getMethodBySignature(declaringMethodClass, mdsm.getSootMethodSubSignature());
			Body body = sootMethod.retrieveActiveBody();

			/*
			 * Add transformation to pack. Will transform/instrument all
			 * classes.
			 */
			// if (!PackManager.v().hasPack("jtp.featmodelinst")) {
			// Transform featureModelTransform = new
			// Transform("jtp.featmodelinst",
			// FeatureModelInstrumentorTransformer.v(extracter));
			// PackManager.v().getPack("jtp").add(featureModelTransform);
			// }

			long instrStart = System.currentTimeMillis();
			FeatureModelInstrumentorTransformer instrumentorTransformer = FeatureModelInstrumentorTransformer.v(extracter, correspondentClasspath);
			instrumentorTransformer.transform2(body, correspondentClasspath);
			long instrEnd = System.currentTimeMillis();

			BriefUnitGraph bodyGraph = new BriefUnitGraph(body);

			/*
			 * Instantiate and execute a runner for the FSRD analysis.
			 */
			long runnerStart = System.currentTimeMillis();
			FeatureSensitiveAnalysisRunner runner = new FeatureSensitiveAnalysisRunner(bodyGraph, instrumentorTransformer.getPowerSet(),
					new FeatureSensitiveReachedDefinitionsFactory(), new HashMap<Object, Object>());
			runner.execute2();
			long runnerEnd = System.currentTimeMillis();
			Map<Set<String>, FeatureSensitiviteFowardFlowAnalysis> results = runner.getResults();

			System.out.println("RUNNER RESULTS:");
			System.out.println("instrumentation took: " + (instrEnd - instrStart));
			System.out.println("time: " + (runnerEnd - runnerStart));

			/*
			 * Bridges from the ASTNodes from the selection to Soot Units using
			 * line number as a parameter.
			 */
			Collection<Unit> unitsInSelection = ASTNodeUnitBridge.getUnitsFromLines(ASTNodeUnitBridge.getLinesFromASTNodes(selectionNodes, jdtCompilationUnit),
					body);

			StringBuilder messageBuilder = new StringBuilder();

			Iterator<Unit> unitsInSelectionIterator = unitsInSelection.iterator();
			while (unitsInSelectionIterator.hasNext()) {
				Unit unit = (Unit) unitsInSelectionIterator.next();
				messageBuilder.append("Provides " + unit + " to ");

				Set<Entry<Set<String>, FeatureSensitiviteFowardFlowAnalysis>> entrySet = results.entrySet();

				Iterator<Entry<Set<String>, FeatureSensitiviteFowardFlowAnalysis>> iterator = entrySet.iterator();
				Set<IFeature> tmpFeatureSet = new HashSet<IFeature>();
				while (iterator.hasNext()) {
					Map.Entry<Set<String>, FeatureSensitiviteFowardFlowAnalysis> entry = (Map.Entry<Set<String>, FeatureSensitiviteFowardFlowAnalysis>) iterator
							.next();

					FeatureSensitiviteFowardFlowAnalysis value = entry.getValue();
					List<Unit> reachedUsesUnits = ((FeatureSensitiveReachingDefinitions) value).getReachedUses(unit);

					for (Unit reachedUnit : reachedUsesUnits) {
						Set<IFeature> colorsOnReachedUnit = new HashSet<IFeature>();
						Collection<ASTNode> astNodesFromUnit = ASTNodeUnitBridge.getASTNodesFromUnit(reachedUnit, jdtCompilationUnit);
						for (ASTNode nodeFromUnit : astNodesFromUnit) {
							colorsOnReachedUnit.addAll(extracter.getFeatures(nodeFromUnit, textSelectionFile));
						}
						SourceLnPosTag lineTag = (SourceLnPosTag) reachedUnit.getTag("SourceLnPosTag");

						messageBuilder.append(colorsOnReachedUnit.toString() + "(line " + lineTag.startLn() + ")\n");
					}
				}
			}

			/*
			 * TODO: apenas para imprimir o resultado. Remover depois.
			 */
			Collection<Set<String>> keySet = results.keySet();
			String format = "|%1$-50s|%2$-50s|%3$-50s|\n";
			for (Set<String> key : keySet) {
				System.out.println(key);
				FeatureSensitiviteFowardFlowAnalysis featureSensitiviteFowardFlowAnalysis = results.get(key);

				Iterator<Unit> iterator = bodyGraph.iterator();
				while (iterator.hasNext()) {
					Unit unit = (Unit) iterator.next();
					System.out.format(format, unit, unit.getTag("FeatureTag"), featureSensitiviteFowardFlowAnalysis.getFlowAfter(unit));
				}

				System.out.println();
			}

			// System.out.println("==================");
			// System.out.println(entry.getKey());
			// System.out.println("------------------");
			// String format = "|%1$-50s|%2$-50s|\n";
			// System.out.format(format, unit, reachedUsesUnits);
			// System.out.println("==================");
			// System.out.println();

			InfoPopup.pop(shell, messageBuilder.toString());

			//
			// if (!PackManager.v().hasPack("wjtp.rd")) {
			// Transform t = new Transform("wjtp.rd", RD.v());
			// PackManager.v().getPack("wjtp").add(t);
			// }
			//
			// long instrumentationStart = System.currentTimeMillis();
			// PackManager.v().runPacks();
			// long instrumentationEnd = System.currentTimeMillis();
			// System.out.println("running packs took " + (instrumentationEnd -
			// instrumentationStart) + "ms");

			// Map<Body, FeatureSensitiveAnalysisRunner> bodyRunnerMap =
			// RD.v().getBodyRunnerMap();
			//
			// Set<Entry<Body, FeatureSensitiveAnalysisRunner>> entrySet =
			// bodyRunnerMap.entrySet();
			// Iterator<Entry<Body, FeatureSensitiveAnalysisRunner>> iterator =
			// entrySet.iterator();
			// while (iterator.hasNext()) {
			// Map.Entry<Body, FeatureSensitiveAnalysisRunner> entry =
			// (Map.Entry<Body, FeatureSensitiveAnalysisRunner>)
			// iterator.next();
			// System.out.println("========================");
			// System.out.println("Results for " +
			// entry.getKey().getMethod().getName());
			//				
			// FeatureSensitiveAnalysisRunner runner = entry.getValue();
			// Map<Set<Object>, FeatureSensitiviteFowardFlowAnalysis> results =
			// runner.getResults();
			// for (Entry<Set<Object>, FeatureSensitiviteFowardFlowAnalysis>
			// entry2 : results.entrySet()) {
			// System.out.println("configuration set:" + entry2.getKey());
			// FeatureSensitiviteFowardFlowAnalysis value = entry2.getValue();
			//
			// Iterator<Unit> iterator2 = body.getUnits().iterator();
			// while (iterator2.hasNext()) {
			// Unit unit = (Unit) iterator2.next();
			// System.out.println(unit + " " + ((FeatureTag)
			// unit.getTag("FeatureTag")).getFeatures());
			// System.out.println(value.getFlowAfter(unit));
			// System.out.println("---");
			// }
			// }
			// System.out.println("========================");
			// }

			/*
			 * TODO: For testing purposes only: manually define a set of valid
			 * configurations. This will probably be user input, so a parser
			 * will be needed.
			 */

			// UnitGraph graph = new BriefUnitGraph(body);

			/*
			 * The analyses are ran for every configuration in the
			 * configurations set automatically by the AnalysisRunner.
			 */
			// FeatureSensitiveAnalysisRunner runner = new
			// FeatureSensitiveAnalysisRunner(graph, SetUtil.tstconfig(),
			// FeatureSensitiveReachingDefinitions.class, new
			// HashMap<Object,Object>());
			// runner.execute();

			/*
			 * TODO: For testing purposes only: print the analysis output
			 */

			// Map<Set<Object>, FeatureSensitiviteFowardFlowAnalysis> results =
			// runner.getResults();
			// for (Entry<Set<Object>, FeatureSensitiviteFowardFlowAnalysis>
			// entry : results.entrySet()) {
			// System.out.println("configuration set:" + entry.getKey());
			// FeatureSensitiviteFowardFlowAnalysis value = entry.getValue();
			//
			// Iterator<Unit> iterator = body.getUnits().iterator();
			// while (iterator.hasNext()) {
			// Unit unit = (Unit) iterator.next();
			// System.out.println(unit + " " + ((FeatureTag)
			// unit.getTag("FeatureTag")).getFeatures());
			// System.out.println(value.getFlowAfter(unit));
			// System.out.println("---");
			// }
			// System.out.println("===");
			// }

			/*
			 * Reset SOOT states and free resources
			 */
			G.v().reset();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
}
