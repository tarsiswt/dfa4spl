package br.ufal.cideei.soot.analyses.reachingdefs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NopStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import br.ufal.cideei.soot.analyses.FeatureSensitiviteFowardFlowAnalysis;

/**
 * The Class FeatureSensitiveReachingDefinitions.
 */
public class FeatureSensitiveReachingDefinitions extends FeatureSensitiviteFowardFlowAnalysis<Unit, FlowSet, Collection> {

	/** The empty set. */
	private FlowSet emptySet = new ArraySparseSet();

	private FlowSet newInitialFlowSet = new ArraySparseSet();

	/**
	 * Instantiates a new feature sensitive reaching definitions.
	 * 
	 * @param graph
	 *            the graph
	 * @param configuration
	 *            the configuration
	 */
	public FeatureSensitiveReachingDefinitions(DirectedGraph graph, Set<Object> configuration, Map options) {
		super(graph, configuration);
		if (options.containsKey("initialFlow")) {
			Collection<? extends Unit> initialFlow = (Collection<? extends Unit>) options.get("initialFlow");
			Iterator<? extends Unit> iterator = initialFlow.iterator();
			while (iterator.hasNext()) {
				Unit unit = (Unit) iterator.next();
				newInitialFlowSet.add(unit);
			}
		}
		// Initiate the analysis framework algorithms
		super.doAnalysis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ufal.cideei.soot.analyses.FeatureSensitiviteFowardFlowAnalysis#
	 * filteredFlowThrough(soot.toolkits.scalar.FlowSet, soot.Unit,
	 * soot.toolkits.scalar.FlowSet)
	 */
	@Override
	protected void filteredFlowThrough(FlowSet source, Unit unit, FlowSet dest) {
		kill(source, unit, dest);
		gen(dest, unit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#merge(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void merge(FlowSet source1, FlowSet source2, FlowSet dest) {
		source1.union(source2, dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#entryInitialFlow()
	 */
	@Override
	protected FlowSet entryInitialFlow() {
		return this.emptySet.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected FlowSet newInitialFlow() {
		if (newInitialFlowSet.isEmpty()) {
			return this.emptySet.clone();
		} else {
			return newInitialFlowSet.clone();
		}
	}

	/**
	 * Creates a KILL set for a given Unit and it to the FlowSet dest. In this
	 * case, our KILL set are the Assignments made to the same Value that this
	 * Unit assigns to.
	 * 
	 * @param src
	 *            the src
	 * @param unit
	 *            the unit
	 * @param dest
	 *            the dest
	 */
	private void kill(FlowSet src, Unit unit, FlowSet dest) {
		FlowSet kills = emptySet.clone();
		if (unit instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) unit;
			for (Object earlierAssignment : src.toList()) {
				if (earlierAssignment instanceof AssignStmt) {
					AssignStmt stmt = (AssignStmt) earlierAssignment;
					if (stmt.getLeftOp().equivTo(assignStmt.getLeftOp())) {
						kills.add(earlierAssignment);
					}
				}
			}
		}
		src.difference(kills, dest);
	}

	/**
	 * Creates a GEN set for a given Unit and it to the FlowSet dest. In this
	 * case, our GEN set are all the definitions present in the unit.
	 * 
	 * @param dest
	 *            the dest
	 * @param unit
	 *            the unit
	 */
	// TODO: MUST ITERATOR THROUGH ALL DEFBOXES!!!
	private void gen(FlowSet dest, Unit unit) {
		if (unit instanceof AssignStmt) {
			dest.add(unit);
		}
	}

	@Override
	protected void copy(FlowSet source, FlowSet dest) {
		source.copy(dest);
	}
	
	public List<Unit> getReachedDefinitions(Unit target) {
		// Iterate over all Units in the graph and search for evey Unit that the
		// definition passed as a parameter reaches.
		Iterator<Unit> unitIterator = this.graph.iterator();
		List<Unit> reached = new ArrayList<Unit>();
		while (unitIterator.hasNext()) {
			Unit nextUnit = unitIterator.next();
			// Ignore nop statements
			if (nextUnit instanceof NopStmt) {
				continue;
			}

			FlowSet reachingDefSet = this.getFlowAfter(nextUnit);
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
	
	

}
