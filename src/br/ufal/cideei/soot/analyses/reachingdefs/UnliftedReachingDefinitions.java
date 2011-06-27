package br.ufal.cideei.soot.analyses.reachingdefs;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import br.ufal.cideei.soot.instrument.FeatureTag;

// TODO: Find a better name!
/**
 */
public class UnliftedReachingDefinitions extends ForwardFlowAnalysis<Unit, FlowSet> {

	protected final int configurationId;

	/** The empty set. */
	private FlowSet emptySet = new ArraySparseSet();

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
	public UnliftedReachingDefinitions(DirectedGraph<Unit> graph, int configurationId) {
		super(graph);
		this.configurationId = configurationId;
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
		return this.emptySet.clone();
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

		FeatureTag<String> tag = (FeatureTag<String>) unit.getTag(FeatureTag.FEAT_TAG_NAME);
		int tagFeaturesId = tag.getId();

		if ((tagFeaturesId & this.configurationId) == tagFeaturesId) {
			kill(source, unit, dest);
			gen(dest, unit);
		} else {
			source.copy(dest);
		}
	}

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
	private void gen(FlowSet dest, Unit unit) {
		if (unit instanceof AssignStmt) {
			dest.add(unit);
		}
	}

}
