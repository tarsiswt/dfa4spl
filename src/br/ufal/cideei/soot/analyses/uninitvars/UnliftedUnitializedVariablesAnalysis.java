package br.ufal.cideei.soot.analyses.uninitvars;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;
import br.ufal.cideei.soot.instrument.FeatureTag;

// TODO: Auto-generated Javadoc
/**
 */
public class UnliftedUnitializedVariablesAnalysis extends ForwardFlowAnalysis<Unit, FlowSet> {

	private FlowSet allLocals;

	/** The empty set. */
	private FlowSet emptySet;

	private final int configurationId;

	// #ifdef METRICS
	private static long flowThroughCounter = 0;

	public static long getFlowThroughCounter() {
		return flowThroughCounter;
	}

	public static void reset() {
		flowThroughCounter = 0;
	}

	// #endif

	/**
	 */
	public UnliftedUnitializedVariablesAnalysis(DirectedGraph<Unit> graph, final int configurationId) {
		super(graph);
		this.configurationId = configurationId;
		this.emptySet = new ArraySparseSet();
		this.allLocals = new ArraySparseSet();
		if (graph instanceof UnitGraph) {
			UnitGraph ug = (UnitGraph) graph;

			Chain<Local> locals = ug.getBody().getLocals();
			for (Object object : locals) {
				Local local = (Local) object;
				if (!local.getName().contains("$")) {
					allLocals.add(local);
				}
			}
		}
		super.doAnalysis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#copy(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	protected void copy(FlowSet source, FlowSet dest) {
		source.copy(dest);
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
		return this.allLocals.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected FlowSet newInitialFlow() {
		return this.emptySet.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(FlowSet source, Unit unit, FlowSet dest) {
		// #ifdef METRICS
		flowThroughCounter++;
		// #endif

		FeatureTag<String> tag = (FeatureTag<String>) unit.getTag("FeatureTag");
		int featureTagId = tag.getId();

		if ((featureTagId & configurationId) == featureTagId) {
			kill(source, unit, dest);
		} else {
			source.copy(dest);
		}
	}

	private void kill(FlowSet src, Unit unit, FlowSet dest) {
		FlowSet kills = new ArraySparseSet();
		if (unit instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) unit;
			Value leftOp = assignStmt.getLeftOp();
			if (leftOp instanceof Local) {
				kills.add(leftOp);
			}
		}
		src.difference(kills, dest);
	}

}