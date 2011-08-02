package br.ufal.cideei.soot.analyses.reachingdefs;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;

// TODO: Find a better name!
/**
 */
public class UnliftedReachingDefinitions extends ForwardFlowAnalysis<Unit, FlowSet> {

	/** The empty set. */
	private FlowSet emptySet = new ArraySparseSet();

	private IConfigRep configuration;

	// #ifdef METRICS
	private long flowThroughTimeAccumulator = 0;

	public long getFlowThroughTime() {
		return this.flowThroughTimeAccumulator;
	}

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
	public UnliftedReachingDefinitions(DirectedGraph<Unit> graph, IConfigRep configuration) {
		super(graph);
		this.configuration = configuration;
		super.doAnalysis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#copy(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void copy(FlowSet source, FlowSet dest) {
		source.copy(dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#merge(java.lang.Object, java.lang.Object, java.lang.Object)
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
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(FlowSet source, Unit unit, FlowSet dest) {
		// #ifdef METRICS
		flowThroughCounter++;
		long timeSpentOnFlowThrough = System.nanoTime();
		// #endif

		FeatureTag tag = (FeatureTag) unit.getTag(FeatureTag.FEAT_TAG_NAME);
		IFeatureRep featureRep = tag.getFeatureRep();

		if (featureRep.belongsToConfiguration(configuration)) {
			kill(source, unit, dest);
			gen(dest, unit);
		} else {
			source.copy(dest);
		}
		// #ifdef METRICS
		timeSpentOnFlowThrough = System.nanoTime() - timeSpentOnFlowThrough;
		this.flowThroughTimeAccumulator += timeSpentOnFlowThrough;
		// #endif
	}

	private void kill(FlowSet source, Unit unit, FlowSet dest) {
		FlowSet kills = new ArraySparseSet();
		if (unit instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) unit;
			for (Object earlierAssignment : source.toList()) {
				if (earlierAssignment instanceof AssignStmt) {
					AssignStmt stmt = (AssignStmt) earlierAssignment;
					if (stmt.getLeftOp().equivTo(assignStmt.getLeftOp())) {
						kills.add(earlierAssignment);
					}
				}
			}
		}
		source.difference(kills, dest);
	}

	/**
	 * Creates a GEN set for a given Unit and it to the FlowSet dest. In this case, our GEN set are all the definitions
	 * present in the unit.
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
