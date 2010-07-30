package br.ufal.cideei.algorithms.coa;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import de.ovgu.cide.features.source.ColoredSourceFile;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.LocalUses;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import br.ufal.cideei.algorithms.BaseAlgorithm;
import br.ufal.cideei.soot.SootManager;
import br.ufal.cideei.util.VertexNameFilterProvider;
import br.ufal.cideei.util.WeighEdgeNameProvider;

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
	public ChainOfAssignmentAlgorithm(Set<ASTNode> nodes, CompilationUnit compilationUnit, ColoredSourceFile file) {
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
		// TODO Auto-generated method stub
	}

	/**
	 * Soot execute.
	 * 
	 * @param textSelectionFile
	 *            the text selection file
	 * @throws ExecutionException
	 *             the execution exception
	 */
	// TODO: tratar exceções corretamente
	public void sootExecute(IFile textSelectionFile) throws ExecutionException {

		/*
		 * The following loc are the phase one of the algorithm. We are only
		 * gathering up the necessary information in order to perform the
		 * algorithm itself.
		 */
		SootManager.reset();
		SootManager.configure(this.getCorrespondentClasspath(textSelectionFile));

		MethodDeclaration methodDeclaration = getParentMethod(nodes.iterator().next());
		String methodDeclarationName = methodDeclaration.getName().getIdentifier();
		String declaringMethodClass = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
		SootMethod sootMethod = SootManager.getMethod(declaringMethodClass, methodDeclarationName);

		Body body = sootMethod.retrieveActiveBody();
		ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);

		/*
		 * We'll use Soot built-in LocalUses and LocalDefs anaylses to compute
		 * which units a given assignments reaches.
		 */
		SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(graph);
		SimpleLocalUses simpleLocalUses = new SimpleLocalUses(graph, simpleLocalDefs);

		/*
		 * The input is gathered as ASTNode, so we use the line number from the
		 * source code to convert the nodes into Units.
		 */
		Collection<Integer> lines = this.getLinesFromASTNodes(nodes, compilationUnit);
		Collection<Unit> units = this.getUnitsFromLines(lines, body);

		/*
		 * Initiate the chain contribution graph that will be populated in the
		 * following loop recursively.
		 */
		SimpleDirectedWeightedGraph<Unit, DefaultWeightedEdge> chainGraph = new SimpleDirectedWeightedGraph<Unit, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);

		/*
		 * For every unit in the selection, we`ll recursively compute the chain
		 * contribution graph.
		 */
		for (Unit eachUnit : units) {
			recursiveGraphBuilder(eachUnit, simpleLocalUses, chainGraph);
		}

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
				 * Making sure that we are not searching for path of lenght 0.
				 */
				if (nextUnitInGraph.equals(eachUnit)) {
					continue;
				}

				List<GraphPath<Unit, DefaultWeightedEdge>> paths = shortestPaths.getPaths(nextUnitInGraph);
				if (paths != null) {
					GraphPath<Unit, DefaultWeightedEdge> graphPath = paths.get(paths.size() - 1);
					if (!unitChainsToMap.containsKey(eachUnit)) {
						unitChainsToMap.put(eachUnit, new HashSet<Integer>());
					}
					unitChainsToMap.get(eachUnit).add(this.getLineFromUnit(graphPath.getEndVertex()));
					// stringBuilder.append(this.getLineFromUnit(eachUnit) +
					// " chains to " +
					// this.getLineFromUnit(graphPath.getEndVertex()) + "\n");
				}
			}
		}

		/*
		 * Builds the message
		 */
		StringBuilder stringBuilder = new StringBuilder();
		for (Entry<Unit, Set<Integer>> entry : unitChainsToMap.entrySet()) {
			Unit entryUnit = entry.getKey();
			Integer entryUnitLine = this.getLineFromUnit(entryUnit);
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
		 * The .DOT will be saved on the user home and will be named wat.dot
		 * 
		 * TODO: treat exceptions correctly
		 */
		String userHomeDir = System.getProperty("user.home");
		DOTExporter<Unit, DefaultWeightedEdge> exporter = new DOTExporter<Unit, DefaultWeightedEdge>(new VertexNameFilterProvider<Unit>(this.compilationUnit),
				null, new WeighEdgeNameProvider<DefaultWeightedEdge>(chainGraph));

		try {
			FileWriter fileWriter = new FileWriter(new File(userHomeDir + File.separator + "wat.dot"));
			exporter.export(fileWriter, chainGraph);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recursive graph builder.
	 * 
	 * @param unit
	 *            the each unit
	 * @param localUses
	 *            the local uses
	 * @param chainGraph
	 *            the chain graph
	 */
	private void recursiveGraphBuilder(Unit unit, LocalUses localUses, SimpleDirectedWeightedGraph<Unit, DefaultWeightedEdge> chainGraph) {
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
				chainGraph.setEdgeWeight(weightedEdge, 1);
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
