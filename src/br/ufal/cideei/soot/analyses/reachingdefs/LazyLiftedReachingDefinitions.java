package br.ufal.cideei.soot.analyses.reachingdefs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

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
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.util.Pair;

/**
 * This implementation of the Reaching Definitions analysis uses a LiftedFlowSet as a lattice element. The only major
 * change is how its KILL method is implemented. Everything else is quite similar to a 'regular' FlowSet-based analysis.
 */
public class LazyLiftedReachingDefinitions extends ForwardFlowAnalysis<Unit, MapLiftedFlowSet> {

	private ILazyConfigRep configurations;

	// #ifdef METRICS
	private long flowThroughTimeAccumulator = 0;

	public long getFlowThroughTime() {
		return this.flowThroughTimeAccumulator;
	}

	protected static long flowThroughCounter = 0;

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
	 */
	public LazyLiftedReachingDefinitions(DirectedGraph<Unit> graph, ILazyConfigRep configs) {
		super(graph);
		this.configurations = configs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#copy(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void copy(MapLiftedFlowSet source, MapLiftedFlowSet dest) {
		source.copy(dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#merge(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void merge(MapLiftedFlowSet source1, MapLiftedFlowSet source2, MapLiftedFlowSet dest) {
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
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(MapLiftedFlowSet source, Unit unit, MapLiftedFlowSet dest) {
		// #ifdef METRICS
		flowThroughCounter++;
		long timeSpentOnFlowThrough = System.nanoTime();
		// #endif

		// pre-copy the information from source to dest
		source.copy(dest);

		// get feature instrumentation for this unit
		FeatureTag tag = (FeatureTag) unit.getTag(FeatureTag.FEAT_TAG_NAME);
		IFeatureRep featureRep = tag.getFeatureRep();

		HashMap<IConfigRep, FlowSet> destMapping = dest.getMapping();

		// iterate over all entries of the lazy flowset (source)
		HashMap<IConfigRep, FlowSet> sourceMapping = source.getMapping();
		Iterator<Entry<IConfigRep, FlowSet>> iterator = sourceMapping.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<IConfigRep, FlowSet> entry = iterator.next();
			ILazyConfigRep lazyConfig = (ILazyConfigRep) entry.getKey();

			FlowSet sourceFlowSet = entry.getValue();
			FlowSet destFlowSet = dest.getLattice(lazyConfig);

			/*
			 * gets the set of configurations whose lattices should be passed to the transfer function.
			 * 
			 * applyToConfigurations = 0 => copy the lattice to dest
			 * 
			 * applyToConfigurations != 0 && applyToConfigurations == lazyConfig => apply the transfer function
			 * 
			 * applyToConfigurations != 0 && applyToConfigurations != lazyConfig => split and apply the transfer
			 * function (on who?)
			 * 
			 * the 1st case has already been addressed with the pre-copy
			 */
			Pair<ILazyConfigRep, ILazyConfigRep> split = lazyConfig.split(featureRep);
			ILazyConfigRep first = split.getFirst();

			if (first.size() != 0) {
				if (first.size() == lazyConfig.size()) {
					kill(sourceFlowSet, unit, destFlowSet, null);
					gen(sourceFlowSet, unit, destFlowSet, null);
				} else {
					ILazyConfigRep second = split.getSecond();

					/*
					 * in this case, this lattice doesnt have a copy from the sourceFlowSet
					 */
					FlowSet destToBeAppliedLattice = new ArraySparseSet();

					// apply point-wise transfer function
					kill(sourceFlowSet, unit, destToBeAppliedLattice, null);
					gen(sourceFlowSet, unit, destToBeAppliedLattice, null);

					/*
					 * make sure an empty config rep doesnt get into the lattice, or it will propagate garbage
					 */
					if (second.size() != 0) {
						destMapping.put(second, destFlowSet);
					}

					// add the new lattice
					destMapping.put(first, destToBeAppliedLattice);

					// remove config rep that has been split
					destMapping.remove(lazyConfig);
				}
			}
		}
		// #ifdef METRICS
		timeSpentOnFlowThrough = System.nanoTime() - timeSpentOnFlowThrough;
		this.flowThroughTimeAccumulator += timeSpentOnFlowThrough;
		// #endif
	}

	protected void kill(FlowSet source, Unit unit, FlowSet dest, IConfigRep configuration) {
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
	 * @param configuration
	 */
	protected void gen(FlowSet source, Unit unit, FlowSet dest, IConfigRep configuration) {
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
