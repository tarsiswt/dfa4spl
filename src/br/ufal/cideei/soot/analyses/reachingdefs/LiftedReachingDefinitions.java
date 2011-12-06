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
import br.ufal.cideei.soot.analyses.MapLiftedFlowSet;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;

/**
 * This implementation of the Reaching Definitions analysis uses a LiftedFlowSet
 * as a lattice element. The only major change is how its KILL method is
 * implemented. Everything else is quite similar to a 'regular' FlowSet-based
 * analysis.
 */
public class LiftedReachingDefinitions extends
		ForwardFlowAnalysis<Unit, MapLiftedFlowSet> {

	private Collection<IConfigRep> configurations;

	// #ifdef METRICS
	private long flowThroughTimeAccumulator = 0;

	public long getFlowThroughTime() {
		return this.flowThroughTimeAccumulator;
	}

	protected static long flowThroughCounter = 0;

	public static long getFlowThroughCounter() {
		return flowThroughCounter;
	}

	private long L1flowThroughCounter = 0;

	public long getL1flowThroughCounter() {
		return L1flowThroughCounter;
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
	 */
	public LiftedReachingDefinitions(DirectedGraph<Unit> graph,
			Collection<IConfigRep> configs) {
		super(graph);
		this.configurations = configs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#copy(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	protected void copy(MapLiftedFlowSet source, MapLiftedFlowSet dest) {
		source.copy(dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#merge(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void merge(MapLiftedFlowSet source1, MapLiftedFlowSet source2,
			MapLiftedFlowSet dest) {
		source1.union(source2, dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#entryInitialFlow()
	 */
	@Override
	protected MapLiftedFlowSet entryInitialFlow() {
		return new MapLiftedFlowSet(configurations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected MapLiftedFlowSet newInitialFlow() {
		return new MapLiftedFlowSet(configurations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(MapLiftedFlowSet source, Unit unit, MapLiftedFlowSet dest) {
		//#ifdef CACHEPURGE
		br.Main.waste();
		//#endif

		// #ifdef METRICS
		flowThroughCounter++;
		long timeSpentOnFlowThrough = System.nanoTime();
		// #endif

		FeatureTag tag = (FeatureTag) unit.getTag(FeatureTag.FEAT_TAG_NAME);
		IFeatureRep featureRep = tag.getFeatureRep();

		Collection<IConfigRep> configs = source.getConfigurations();	
		for (IConfigRep config : configs) {
			FlowSet sourceFlowSet = source.getLattice(config);
			FlowSet destFlowSet = dest.getLattice(config);
			if (config.belongsToConfiguration(featureRep)) {
				L1flowThroughCounter++;
				kill(sourceFlowSet, unit, destFlowSet, null);
				gen(sourceFlowSet, unit, destFlowSet, null);
			} else {
				sourceFlowSet.copy(destFlowSet);
			}
		}
		// #ifdef METRICS
		timeSpentOnFlowThrough = System.nanoTime() - timeSpentOnFlowThrough;
		this.flowThroughTimeAccumulator += timeSpentOnFlowThrough;
		// #endif
	}

	/**
	 * Creates a KILL set for the given unit and remove the elements that are in
	 * KILL from the destination FlowSet.
	 * 
	 * @param source
	 * @param unit
	 * @param dest
	 * @param configuration
	 */
	protected void kill(FlowSet source, Unit unit, FlowSet dest,
			Set<String> configuration) {
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
	 * Creates a GEN set for a given Unit and add it to the FlowSet dest. In
	 * this case, our GEN set are all the definitions present in the unit.
	 * 
	 * @param dest
	 *            the dest
	 * @param unit
	 *            the unit
	 * @param configuration
	 */
	protected void gen(FlowSet source, Unit unit, FlowSet dest,
			Set<String> configuration) {
		if (unit instanceof AssignStmt) {
			dest.add(unit);
		}
	}

	public List<Unit> getReachedUses(Unit target, Set<String> configuration) {
		// int index = 0;
		// for (Set<String> configuration1 : configurations) {
		// if (configuration.equals(configuration1)) {
		// Iterator<Unit> unitIterator = graph.iterator();
		// List<Unit> reached = new ArrayList<Unit>();
		// while (unitIterator.hasNext()) {
		// Unit nextUnit = unitIterator.next();
		//
		// LiftedFlowSet reachingDefSet = this.getFlowAfter(nextUnit);
		// FlowSet flowSet = reachingDefSet.getLattices()[index];
		// Iterator<? extends Unit> flowIterator = flowSet.toList().iterator();
		// if (flowSet.contains(target)) {
		// reached.add(nextUnit);
		// }
		// }
		// return reached;
		// }
		// }
		// index++;
		return null;
	}

	public void execute() {
		this.doAnalysis();
	}

}
