/*
 * This is a prototype implementation of the concept of Feature-Sen
 * sitive Dataflow Analysis. More details in the AOSD'12 paper:
 * Dataflow Analysis for Software Product Lines
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package br.ufal.cideei.soot.analyses.reachingdefs;

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
import br.ufal.cideei.soot.analyses.ReversedMapLiftedFlowSet;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.util.Pair;

/**
 * This implementation of the Reaching Definitions analysis uses a LiftedFlowSet as a lattice element. The only major
 * change is how its KILL method is implemented. Everything else is quite similar to a 'regular' FlowSet-based analysis.
 */
public class ReversedLazyLiftedReachingDefinitions extends ForwardFlowAnalysis<Unit, ReversedMapLiftedFlowSet> {

	private ILazyConfigRep configurations;

	// #ifdef METRICS
	private long flowThroughTimeAccumulator = 0;

	protected static long mergeCounter = 0;

	public static long getMergeCounter() {
		return mergeCounter;
	}
	
	public long getFlowThroughTime() {
		return this.flowThroughTimeAccumulator;
	}

	protected static long flowThroughCounter = 0;

	public static long getFlowThroughCounter() {
		return flowThroughCounter;
	}

	public static void reset() {
		flowThroughCounter = 0;
		mergeCounter = 0;
	}

	// #endif

	/**
	 * Instantiates a new TestReachingDefinitions.
	 * 
	 * @param graph
	 *            the graph
	 */
	public ReversedLazyLiftedReachingDefinitions(DirectedGraph<Unit> graph, ILazyConfigRep configs) {
		super(graph);
		this.configurations = configs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#copy(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void copy(ReversedMapLiftedFlowSet source, ReversedMapLiftedFlowSet dest) {
		source.copy(dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#merge(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void merge(ReversedMapLiftedFlowSet source1, ReversedMapLiftedFlowSet source2, ReversedMapLiftedFlowSet dest) {
		source1.union(source2, dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#entryInitialFlow()
	 */
	@Override
	protected ReversedMapLiftedFlowSet entryInitialFlow() {
		return new ReversedMapLiftedFlowSet(configurations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected ReversedMapLiftedFlowSet newInitialFlow() {
		return new ReversedMapLiftedFlowSet(configurations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(ReversedMapLiftedFlowSet source, Unit unit, ReversedMapLiftedFlowSet dest) {
		// #ifdef METRICS
		flowThroughCounter++;
		long timeSpentOnFlowThrough = System.nanoTime();
		// #endif

		// clear the destination lattice to insert new ones
		dest.clear();

		// get feature information for the unit which this transfer function is being applied to
		FeatureTag tag = (FeatureTag) unit.getTag(FeatureTag.FEAT_TAG_NAME);
		IFeatureRep featureRep = tag.getFeatureRep();

		HashMap<FlowSet, IConfigRep> destMapping = dest.getMapping();

		// iterate over all entries of the lazy flowset (source)
		HashMap<FlowSet, IConfigRep> sourceMapping = source.getMapping();
		Iterator<Entry<FlowSet, IConfigRep>> iterator = sourceMapping.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<FlowSet, IConfigRep> entry = iterator.next();
			ILazyConfigRep lazyConfig = (ILazyConfigRep) entry.getValue();

			FlowSet sourceFlowSet = entry.getKey();

			/*
			 *  The split of a lazy configuration L by another lazy configuration O
			 *  gives rise to two other lazy configurations, the FIRST one contains
			 *  the set of configuration that both L and O "have in common", and
			 *  the SECOND contains the set of the "rest".
			 *  
			 *  Thus, if the size of FIRST is 0, there are no configurations in common.
			 *  If the size of FIRST is the same as the size of O, than their corresponding
			 *  sets are the same as well. 
			 */
			Pair<ILazyConfigRep, ILazyConfigRep> split = lazyConfig.split(featureRep);
			ILazyConfigRep first = split.getFirst();

			if (first.size() != 0) {
				FlowSet destFlowSet = sourceFlowSet.clone();
				if (first.size() == lazyConfig.size()) {
					/*
					 *  This mutates the destFlowSet to contain the result of the GEN/KILL
					 *  operations.
					 */
					kill(sourceFlowSet, unit, destFlowSet, null);
					gen(sourceFlowSet, unit, destFlowSet, null);
					
					// Add the mapping to the top level FlowSet (mapping)
					IConfigRep config = destMapping.get(destFlowSet);
					if (config == null)
						destMapping.put(destFlowSet, first);
					else {
						destMapping.put(destFlowSet, ((ILazyConfigRep)config).union(first));
						mergeCounter++;
					}
				} else {
					/*
					 * This lazy configuration must map the same value that L
					 * mapped to.
					 */
					ILazyConfigRep second = split.getSecond();
					if (second.size() != 0) {
						IConfigRep config = destMapping.get(destFlowSet);
						if (config == null)
							destMapping.put(destFlowSet, second);
						else {
							destMapping.put(destFlowSet, ((ILazyConfigRep)config).union(second));
							mergeCounter++;
						}
					}
					/*
					 * This flowset will contain the result of the GEN/KILL operations,
					 * and is to be mapped from FIRST.
					 */
					FlowSet destToBeAppliedLattice = new ArraySparseSet();
					kill(sourceFlowSet, unit, destToBeAppliedLattice, null);
					gen(sourceFlowSet, unit, destToBeAppliedLattice, null);
					IConfigRep config = destMapping.get(destToBeAppliedLattice);
					if (config == null)
						destMapping.put(destToBeAppliedLattice, first);
					else {
						destMapping.put(destToBeAppliedLattice, ((ILazyConfigRep)config).union(first));
						mergeCounter++;
					}
				}
			} else {
				/*
				 *  There is nothing to be done in this case, thus we only copy the mapping
				 *  from the source.
				 */
				IConfigRep config = destMapping.get(sourceFlowSet);
				if (config == null)
					destMapping.put(sourceFlowSet, lazyConfig);
				else {
					destMapping.put(sourceFlowSet, ((ILazyConfigRep)config).union(lazyConfig));
					mergeCounter++;
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
