package br.ufal.cideei.soot.analyses.reachingdefs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Unit;
import soot.jimple.NopStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleReachingDefinitionsAnalysis.
 */
public class SimpleReachedDefinitionsAnalysis extends AbstractReachedDefinitions {

	/** The reaching definitions. */
	private final SimpleReachingDefinitions reachingDefinitions;

	/**
	 * Instantiates a new simple reaching definitions analysis.
	 * 
	 * @param graph
	 *            the graph
	 */
	public SimpleReachedDefinitionsAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		this.reachingDefinitions = new SimpleReachingDefinitions(graph);
	}

	/**
	 * Gets the reached uses.
	 * 
	 * @param target
	 *            the target
	 * @return the reached uses
	 */
	public List<Unit> getReachedUses(Unit target) {
		Iterator<Unit> unitIterator = graph.iterator();
		List<Unit> reached = new ArrayList<Unit>();
		while (unitIterator.hasNext()) {
			Unit nextUnit = unitIterator.next();
			if (nextUnit instanceof NopStmt) {
				continue;
			}
			FlowSet reachingDefSet = this.reachingDefinitions.getFlowAfter(nextUnit);
			Iterator<? extends Unit> flowIterator = reachingDefSet.toList().iterator();
			while (flowIterator.hasNext()) {
				Unit nextUnitInFlow = flowIterator.next();
				if (nextUnitInFlow instanceof NopStmt) {
					continue;
				}
				if (nextUnitInFlow.equals(target)) {
					reached.add(nextUnit);
				}
			}
		}
		return reached;
	}

	public FlowSet getFlowAfter(Unit unit) {
		return this.reachingDefinitions.getFlowAfter(unit);
	}

}