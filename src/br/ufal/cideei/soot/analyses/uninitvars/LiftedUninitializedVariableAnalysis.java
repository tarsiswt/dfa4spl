package br.ufal.cideei.soot.analyses.uninitvars;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;
import br.ufal.cideei.soot.analyses.LiftedFlowSet;
import br.ufal.cideei.soot.instrument.FeatureTag;

/**
 * This implementation of the Initialized variable analysis uses a LiftedFlowSet
 * as a lattice element. The only major change is how it's KILL method is
 * implemented. Also, the gen method is empty. We fill the lattice with local
 * variables at the class constructor.
 */
public class LiftedUninitializedVariableAnalysis extends ForwardFlowAnalysis<Unit, LiftedFlowSet> {

	/** The empty set. */
	/*
	 * FIXME: the clone method of LiftedFlowSet is not working properly right
	 * now.
	 */
	private LiftedFlowSet allLocals;
	private LiftedFlowSet emptySet;
	private Collection<Set<String>> configurations;

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
	 * Instantiates a new TestReachingDefinitions.
	 * 
	 * @param graph
	 *            the graph
	 * @param configs
	 *            the configurations.
	 */
	public LiftedUninitializedVariableAnalysis(DirectedGraph<Unit> graph, Collection<Set<String>> configs) {
		super(graph);
		this.configurations = configs;
		this.allLocals = new LiftedFlowSet(configs);
		this.emptySet = new LiftedFlowSet(configs);

		if (graph instanceof UnitGraph) {
			UnitGraph ug = (UnitGraph) graph;

			Chain<Local> locals = ug.getBody().getLocals();

			for (Object object : locals) {
				Local local = (Local) object;

				if (!local.getName().contains("$")) {

					FlowSet[] lattices = this.allLocals.getLattices();

					for (int i = 0; i < lattices.length; i++) {
						FlowSet flowSet = lattices[i];
						flowSet.add(local);
					}
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
	protected void copy(LiftedFlowSet source, LiftedFlowSet dest) {
		source.copy(dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#merge(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void merge(LiftedFlowSet source1, LiftedFlowSet source2, LiftedFlowSet dest) {
		source1.union(source2, dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#entryInitialFlow()
	 */
	@Override
	protected LiftedFlowSet entryInitialFlow() {
		// return new LiftedFlowSet<Collection<Set<String>>>(configurations);
		// List<FlowSet> lattices = this.emptySet.getLattices();
		// LiftedFlowSet<Collection<Set<String>>> liftedFlowSet = new
		// LiftedFlowSet<Collection<Set<String>>>(configurations);
		// liftedFlowSet.
		return this.allLocals.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected LiftedFlowSet newInitialFlow() {
		// return new LiftedFlowSet<Collection<Set<String>>>(configurations);
		return this.allLocals.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(LiftedFlowSet source, Unit unit, LiftedFlowSet dest) {
		// #ifdef METRICS
		flowThroughCounter++;
		// #endif

		FeatureTag<String> tag = (FeatureTag<String>) unit.getTag("FeatureTag");
		Collection<String> features = tag.getFeatures();

		Set<String>[] configurations = source.getConfigurations();

		FlowSet[] sourceLattices = source.getLattices();
		FlowSet[] destLattices = dest.getLattices();

		for (int i = 0; i < configurations.length; i++) {

			Set<String> configuration = configurations[i];
			FlowSet sourceFlowSet = sourceLattices[i];
			FlowSet destFlowSet = destLattices[i];

			if (configuration.containsAll(features)) {
				kill(sourceFlowSet, unit, destFlowSet);
			} else {
				sourceFlowSet.copy(destFlowSet);
			}
		}
	}

	private void kill(FlowSet source, Unit unit, FlowSet dest) {
		FlowSet kills = new ArraySparseSet();
		if (unit instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) unit;
			for (Object declaredVariable : source.toList()) {
				if (declaredVariable instanceof Local) {
					Local local = (Local) declaredVariable;
					if (local.equivTo(assignStmt.getLeftOp())) {
						kills.add(local);
					}
				}
			}
		}
		source.difference(kills, dest);
	}

}