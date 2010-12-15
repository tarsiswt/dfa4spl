package br.ufal.cideei.soot.analyses.uninitvars;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.util.Chain;
import br.ufal.cideei.soot.analyses.FeatureSensitiveFowardFlowAnalysis;

/**
 * The Class FeatureSensitiveUninitializedVariables.
 */
public class FeatureSensitiveUninitializedVariables extends FeatureSensitiveFowardFlowAnalysis<Unit, FlowSet, Collection> {

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
	public FeatureSensitiveUninitializedVariables(DirectedGraph graph, Set<String> configuration, Map options) {
		super(graph, configuration);
		if (graph instanceof UnitGraph) {
			UnitGraph ug = (UnitGraph) graph;

			Chain locals = ug.getBody().getLocals();

			for (Object object : locals) {
				Local local = (Local) object;

				if (!local.getName().contains("$")) {
					emptySet.add(local);
				}
			}
		}
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
		/*
		 * GEN = {}
		 */
//		gen(dest, unit);
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
			Value leftOp = assignStmt.getLeftOp();
			if (leftOp instanceof Local) {
				kills.add(leftOp);
			}
		}
		src.difference(kills, dest);
	}

	/**
	 * Creates a GEN set for a given Unit and it to the FlowSet dest. In this
	 * case, all information needed is loaded on construction, right before the
	 * analysis take place, so no GEN set is needed.
	 * 
	 * @param dest
	 *            the dest
	 * @param unit
	 *            the unit
	 */
	// TODO: MUST ITERATE THROUGH ALL DEFBOXES!!!
	private void gen(FlowSet dest, Unit unit) {
	}

	@Override
	protected void copy(FlowSet source, FlowSet dest) {
		source.copy(dest);
	}
}
