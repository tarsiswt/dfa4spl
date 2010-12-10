package br.ufal.cideei.soot.analyses.reachingdefs;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import br.ufal.cideei.soot.analyses.LiftedFlowSet;
import br.ufal.cideei.soot.instrument.FeatureTag;

// TODO: Auto-generated Javadoc
/**
 * This implementation of the Reaching Definitions analysis uses a LiftedFlowSet
 * as a lattice element. The only major change is how it's KILL method is
 * implemented. Everything else is quite similar to a 'regular' FlowSet-based
 * analysis.
 */
public class LiftedReachingDefinitions extends ForwardFlowAnalysis<Unit, LiftedFlowSet<Collection<Set<Object>>>> {

	/** The empty set. */
	/*
	 * FIXME: the clone method of LiftedFlowSet is currently not working
	 * properly.
	 */
	private LiftedFlowSet<Collection<Set<String>>> emptySet;
	private Collection<Set<String>> configurations;

	//#ifdef METRICS
	private static long flowThroughCounter = 0;

	public static long getFlowThroughCounter() {
		return flowThroughCounter;		
	}
	
	public static void reset() {
		flowThroughCounter = 0;
	}
	//#endif

	/**
	 * Instantiates a new TestReachingDefinitions.
	 * 
	 * @param graph
	 *            the graph
	 */
	public LiftedReachingDefinitions(DirectedGraph<Unit> graph, Collection<Set<String>> configs) {
		super(graph);
		this.configurations = configs;
		this.emptySet = new LiftedFlowSet<Collection<Set<String>>>(configs);
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
		return new LiftedFlowSet<Collection<Set<String>>>(configurations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected LiftedFlowSet newInitialFlow() {
		return new LiftedFlowSet<Collection<Set<String>>>(configurations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(LiftedFlowSet source, Unit unit, LiftedFlowSet dest) {
		//#ifdef METRICS
		flowThroughCounter++;
		//#endif
		
		FeatureTag<Set<String>> tag = (FeatureTag<Set<String>>) unit.getTag("FeatureTag");
		Collection<Set<String>> features = tag.getFeatures();
		
		List<Set<String>> configurations = source.getConfigurations();
		
		List<FlowSet> sourceLattices = source.getLattices();
		List<FlowSet> destLattices = dest.getLattices();
		
		for (int i = 0; i < configurations.size(); i++) {
			
			Set<String> configuration = configurations.get(i);
			FlowSet sourceFlowSet = sourceLattices.get(i);
			FlowSet destFlowSet = destLattices.get(i);
			
			if (features.contains(configuration)) {
				kill(sourceFlowSet, unit, destFlowSet);
				gen(destFlowSet, unit);
			} else {
				sourceFlowSet.copy(destFlowSet);
			}
		}
	}

	private void kill(FlowSet source, Unit unit, FlowSet dest) {
		FlowSet kills = new ArraySparseSet();//emptySet.clone();
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
	 * Creates a GEN set for a given Unit and it to the FlowSet dest. In this
	 * case, our GEN set are all the definitions present in the unit.
	 * 
	 * @param dest
	 *            the dest
	 * @param unit
	 *            the unit
	 */
	// TODO: MUST ITERATE THROUGH ALL DEFBOXES!!!
	private void gen(FlowSet dest, Unit unit) {
		if (unit instanceof AssignStmt) {
			dest.add(unit);
		}
	}

}