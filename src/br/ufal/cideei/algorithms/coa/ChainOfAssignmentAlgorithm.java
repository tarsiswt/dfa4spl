package br.ufal.cideei.algorithms.coa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import soot.Body;
import soot.G;
import soot.PatchingChain;
import soot.Printer;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.LocalUses;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.tools.CFGViewer;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;
import br.ufal.cideei.algorithms.BaseAlgorithm;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.soot.SootManager;
import br.ufal.cideei.soot.SootUnitGraphSerializer;
import br.ufal.cideei.soot.UnitUtil;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachedDefinitionsAnalysis;
import br.ufal.cideei.soot.analyses.reachingdefs.SimpleReachedDefinitionsAnalysis;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;
import br.ufal.cideei.soot.instrument.asttounit.ASTNodeUnitBridge;
import br.ufal.cideei.util.MethodDeclarationSootMethodBridge;
import br.ufal.cideei.util.graph.VertexLineNameProvider;
import br.ufal.cideei.util.graph.VertexNameFilterProvider;
import br.ufal.cideei.util.graph.WeighEdgeNameProvider;
import de.ovgu.cide.features.source.ColoredSourceFile;

// TODO: Auto-generated Javadoc
/**
 * The Class ChainOfAssignmentAlgorithm.
 */
public class ChainOfAssignmentAlgorithm extends BaseAlgorithm {

	/** The file. */
	private ColoredSourceFile file;

	/** The nodes. */
	private Set<ASTNode> nodes;

	/** The compilation unit. */
	private CompilationUnit compilationUnit;

	/** The message. */
	private String message = "";

	private IFeatureExtracter extracter;

	/**
	 * Instantiates a new chain of assignment algorithm. The current
	 * implementation of this algorith will follow these steps: 1) compute the
	 * initial set of definitions (from the selection) and insert them in the
	 * chain of contribution graph; 2) or every use of that definition, insert
	 * the defition that uses the later in the graph. 3) repeat (2) for every
	 * insertion in the graph.
	 * 
	 * 
	 * 
	 * @param nodes
	 *            the nodes
	 * @param compilationUnit
	 *            the compilation unit
	 * @param file
	 *            the file
	 */
	public ChainOfAssignmentAlgorithm(Set<ASTNode> nodes, CompilationUnit compilationUnit, ColoredSourceFile file, IFeatureExtracter extracter) {
		this.file = file;
		this.nodes = nodes;
		this.compilationUnit = compilationUnit;
		this.extracter = extracter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ufal.cideei.algorithms.IAlgorithm#execute()
	 */
	@Override
	public void execute() {
	}

	/**
	 * Soot execute.
	 * 
	 * @param textSelectionFile
	 *            the text selection file
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
		SootManager.runPacks(extracter);

		ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);
		System.out.println("graph size: " + graph.size());

		/*
		 * We'll use our Soot reaching definition analysis wrapper to compute
		 * which units a given assignments reaches.
		 */
		SimpleReachedDefinitionsAnalysis reachingDefinitions = new SimpleReachedDefinitionsAnalysis(graph);

		/*
		 * The input is gathered as ASTNode, so we use the line number from the
		 * source code to convert the nodes into Units.
		 */
		Collection<Integer> lines = ASTNodeUnitBridge.getLinesFromASTNodes(nodes, compilationUnit);
		Collection<Unit> units = ASTNodeUnitBridge.getUnitsFromLines(lines, body);

		/*
		 * Initiate the chain contribution graph that will be populated in the
		 * following loop recursively.
		 */
		DefaultDirectedWeightedGraph<Unit, DefaultWeightedEdge> chainGraph = new DefaultDirectedWeightedGraph<Unit, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);

		/*
		 * For every unit in the selection, we`ll recursively compute the chain
		 * contribution graph.
		 */
		for (Unit eachUnit : units) {
			recursiveGraphBuilder(eachUnit, reachingDefinitions, chainGraph);
		}

		System.out.println("chain graph size: " + chainGraph.vertexSet().size());

		/*
		 * Now we must find the longest path from every node from the selection
		 * that is in the graph to every other node on the path. To do this,
		 * we`ll use an variant of the Bellman-Ford algorithm that is provided
		 * in the KShortestPath class. It computes the k shortest paths between
		 * 2 different vertexes. The current implementation asks the algorithm
		 * to compute a threshold of paths and we choose the longest one.
		 * 
		 * TODO: implement with a smarter longest path algorithm
		 */
		final int KSHORTEST_PATHS_THRESHOLD = 10;
		DepthFirstIterator<Unit, DefaultWeightedEdge> graphIterator = new DepthFirstIterator<Unit, DefaultWeightedEdge>(chainGraph);

		Map<Unit, Set<Integer>> unitChainsToMap = new HashMap<Unit, Set<Integer>>();

		for (Unit eachUnit : units) {
			/*
			 * If the unit from selection did not end up in the graph, then it
			 * is of no interest to us in this point.
			 */
			if (!chainGraph.containsVertex(eachUnit)) {
				continue;
			}
			KShortestPaths<Unit, DefaultWeightedEdge> shortestPaths = new KShortestPaths<Unit, DefaultWeightedEdge>(chainGraph, eachUnit,
					KSHORTEST_PATHS_THRESHOLD);
			while (graphIterator.hasNext()) {
				Unit nextUnitInGraph = graphIterator.next();
				/*
				 * Making sure that we are not searching for path in which the
				 * start vertex = end vertex
				 */
				if (nextUnitInGraph.equals(eachUnit)) {
					continue;
				}

				List<GraphPath<Unit, DefaultWeightedEdge>> paths = shortestPaths.getPaths(nextUnitInGraph);
				if (paths != null) {
					// GraphPath<Unit, DefaultWeightedEdge> graphPath =
					// paths.get(paths.size() - 1);
					GraphPath<Unit, DefaultWeightedEdge> graphPath = paths.get(0);

					/*
					 * Since every edge on the graph have weight 1, we`ll ignore
					 * paths that have a total weight of 1, that is, those
					 * graphs with size equals to 1 or less.
					 */
					if (graphPath.getWeight() <= 1) {
						continue;
					}

					if (!unitChainsToMap.containsKey(eachUnit)) {
						unitChainsToMap.put(eachUnit, new HashSet<Integer>());
					}
					unitChainsToMap.get(eachUnit).add(ASTNodeUnitBridge.getLineFromUnit(graphPath.getEndVertex()));
				}
			}
		}

		/*
		 * Builds the message
		 */
		StringBuilder stringBuilder = new StringBuilder();
		for (Entry<Unit, Set<Integer>> entry : unitChainsToMap.entrySet()) {
			Unit entryUnit = entry.getKey();
			Integer entryUnitLine = ASTNodeUnitBridge.getLineFromUnit(entryUnit);
			Set<Integer> entryUnitLines = entry.getValue();

			for (Integer line : entryUnitLines) {
				stringBuilder.append("Line " + entryUnitLine + " chains to " + line + "\n");
			}
		}
		this.message = stringBuilder.toString();

		/*
		 * For debug only: this will export the generated graph as a .DOT, that
		 * aids in the visualisation of the generated graph.
		 * 
		 * The .DOT will be saved on the user home and will be named comp.dot
		 * 
		 * Additionally, another .DOT file will be created from the same graph,
		 * but in a simplified manner. Nodes will be the lines from source
		 * codes, and no weight on the edges
		 * 
		 * TODO: treat exceptions correctly
		 * 
		 * TODO: move this functionality to a different location, perhaps in a
		 * different class, by exposing the generated graph through a get
		 * method?
		 */
		String userHomeDir = System.getProperty("user.home");

		try {
			DOTExporter<Unit, DefaultWeightedEdge> completeExporter = new DOTExporter<Unit, DefaultWeightedEdge>(new VertexNameFilterProvider<Unit>(
					this.compilationUnit), null, new WeighEdgeNameProvider<DefaultWeightedEdge>(chainGraph));

			FileWriter fileWriterForCompleteExporter = new FileWriter(new File(userHomeDir + File.separator + "comp.dot"));
			completeExporter.export(fileWriterForCompleteExporter, chainGraph);
			fileWriterForCompleteExporter.close();

			DOTExporter<Unit, DefaultWeightedEdge> simplifiedExporter = new DOTExporter<Unit, DefaultWeightedEdge>(new VertexLineNameProvider<Unit>(
					this.compilationUnit), null, null);

			FileWriter fileWriterForSimplifiedExporter = new FileWriter(new File(userHomeDir + File.separator + "simp.dot"));
			simplifiedExporter.export(fileWriterForSimplifiedExporter, chainGraph);
			fileWriterForSimplifiedExporter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recursively build the chain contribution graph. For every local use of a
	 * unit, create a vertex for that unit, and then an edge between then. If
	 * they belong to the same line, then the edge weight is 0, otherwise it is
	 * 1.
	 * 
	 * Repeat for every created vertex.
	 * 
	 * @param unit
	 *            the each unit
	 * @param localUses
	 *            the local uses
	 * @param chainGraph
	 *            the chain graph
	 */
	private void recursiveGraphBuilder(Unit unit, SimpleReachedDefinitionsAnalysis reachingDef,
			DefaultDirectedWeightedGraph<Unit, DefaultWeightedEdge> chainGraph) {
		System.out.println(unit);
		List<Unit> reachedUses = reachingDef.getReachedUses(unit);
		for (Unit reachedUse : reachedUses) {
			List<ValueBox> defBoxes = unit.getDefBoxes();
			for (ValueBox defBox : defBoxes) {
				Value defValueInBox = defBox.getValue();
				List<ValueBox> useBoxes = reachedUse.getUseBoxes();
				for (ValueBox useBox : useBoxes) {
					if (defValueInBox.equals(useBox.getValue())) {

						if (!chainGraph.containsVertex(unit)) {
							chainGraph.addVertex(unit);
						}
						if (!chainGraph.containsVertex(reachedUse)) {
							chainGraph.addVertex(reachedUse);
						}
						if (chainGraph.containsEdge(unit, reachedUse)) {
							continue;
						}

						DefaultWeightedEdge weightedEdge = chainGraph.addEdge(unit, reachedUse);
						if (weightedEdge != null) {
							if (ASTNodeUnitBridge.getLineFromUnit(reachedUse).equals(ASTNodeUnitBridge.getLineFromUnit(unit))) {
								chainGraph.setEdgeWeight(weightedEdge, 0);
							} else {
								chainGraph.setEdgeWeight(weightedEdge, 1);
							}
						}
						recursiveGraphBuilder(reachedUse, reachingDef, chainGraph);
					}
				}
			}
		}
	}

	/**
	 * Recursively build the chain contribution graph. For every local use of a
	 * unit, create a vertex for that unit, and then an edge between then. If
	 * they belong to the same line, then the edge weight is 0, otherwise it is
	 * 1.
	 * 
	 * Repeat for every created vertex.
	 * 
	 * @param unit
	 *            the each unit
	 * @param localUses
	 *            the local uses
	 * @param chainGraph
	 *            the chain graph
	 * @deprecated
	 */
	private void recursiveGraphBuilder(Unit unit, LocalUses localUses, DefaultDirectedWeightedGraph<Unit, DefaultWeightedEdge> chainGraph) {
		for (Object unitBoxPairObj : localUses.getUsesOf(unit)) {
			UnitValueBoxPair unitBoxPair = (UnitValueBoxPair) unitBoxPairObj;
			Unit unitFromPair = unitBoxPair.getUnit();

			if (!chainGraph.containsVertex(unit)) {
				chainGraph.addVertex(unit);
			}
			if (!chainGraph.containsVertex(unitFromPair)) {
				chainGraph.addVertex(unitFromPair);
			}
			DefaultWeightedEdge weightedEdge = chainGraph.addEdge(unit, unitFromPair);
			if (weightedEdge != null) {
				if (ASTNodeUnitBridge.getLineFromUnit(unitFromPair).equals(ASTNodeUnitBridge.getLineFromUnit(unit))) {
					chainGraph.setEdgeWeight(weightedEdge, 0);
				} else {
					chainGraph.setEdgeWeight(weightedEdge, 1);
				}
			}
			recursiveGraphBuilder(unitFromPair, localUses, chainGraph);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ufal.cideei.algorithms.IAlgorithm#getMessage()
	 */
	@Override
	public String getMessage() {
		return this.message;
	}

}
