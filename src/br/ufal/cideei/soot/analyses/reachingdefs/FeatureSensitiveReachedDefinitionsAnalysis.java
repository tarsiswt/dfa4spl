package br.ufal.cideei.soot.analyses.reachingdefs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.ufal.cideei.algorithms.assignment.ReachedByDefinitionVisitor;
import br.ufal.cideei.soot.analyses.FowardFlowAnalysis;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NopStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

// TODO: Auto-generated Javadoc
/**
 * The Class FeatureSensitiveReachedDefinitionsAnalysis is an implementation for
 * the ReachedDefinitions analysis that is feature sensitive.
 */
public class FeatureSensitiveReachedDefinitionsAnalysis extends AbstractReachedDefinitions {

	/** The reaching definitions. */
	private FeatureSensitiveReachingDefinitions reachingDefinitions;

	/**
	 * Instantiates a new feature sensitive reached definitions analysis.
	 * 
	 * @param graph
	 *            the unit graph
	 * @param configuration
	 *            the configuration for which this analysis should be run
	 */
	public FeatureSensitiveReachedDefinitionsAnalysis(DirectedGraph<Unit> graph, Set<String> configuration,Map<Object,Object> options) {
		super(graph);
		this.reachingDefinitions = new FeatureSensitiveReachingDefinitions(graph, configuration, options);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ufal.cideei.soot.analyses.reachingdefs.ReachedDefinitions#getReachedUses
	 * (soot.Unit)
	 */
	@Override
	public List<Unit> getReachedUses(Unit target) {
		// Iterate over all Units in the graph and search for evey Unit that the
		// definition passed as a parameter reaches.
		Iterator<Unit> unitIterator = graph.iterator();
		List<Unit> reached = new ArrayList<Unit>();
		while (unitIterator.hasNext()) {
			Unit nextUnit = unitIterator.next();
			// Ignore nop statements
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