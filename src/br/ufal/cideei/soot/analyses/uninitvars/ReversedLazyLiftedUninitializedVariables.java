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

package br.ufal.cideei.soot.analyses.uninitvars;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;
import br.ufal.cideei.soot.analyses.MapLiftedFlowSet;
import br.ufal.cideei.soot.analyses.ReversedMapLiftedFlowSet;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.util.Pair;

/**
 * This implementation of the Initialized variable analysis uses a LiftedFlowSet
 * as a lattice element. The only major change is how it's KILL method is
 * implemented. Also, the gen method is empty. We fill the lattice with local
 * variables at the class constructor.
 */
public class ReversedLazyLiftedUninitializedVariables extends ForwardFlowAnalysis<Unit, ReversedMapLiftedFlowSet> {

	private ReversedMapLiftedFlowSet allLocals;
	private ReversedMapLiftedFlowSet emptySet;

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
	 * Instantiates a new TestReachingDefinitions.
	 * 
	 * @param graph
	 *            the graph
	 * @param configs
	 *            the configurations.
	 */
	public ReversedLazyLiftedUninitializedVariables(UnitGraph unitGraph, ILazyConfigRep configs) {
		super(unitGraph);
		this.allLocals = new ReversedMapLiftedFlowSet();
		this.emptySet = new ReversedMapLiftedFlowSet(configs);
		
		Map<FlowSet, IConfigRep> map = allLocals.getMapping();
		
		Chain<Local> locals = unitGraph.getBody().getLocals();
		FlowSet flowSet = new ArraySparseSet();
		for (Local local : locals)
			flowSet.add(local);
		
		map.put(flowSet, configs);
		super.doAnalysis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#copy(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	protected void copy(ReversedMapLiftedFlowSet source, ReversedMapLiftedFlowSet dest) {
		source.copy(dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#merge(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void merge(ReversedMapLiftedFlowSet source1, ReversedMapLiftedFlowSet source2, ReversedMapLiftedFlowSet dest) {
		source1.union(source2, dest);
	}

	/*ReversedMapLiftedFlowSet
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#entryInitialFlow()
	 */
	@Override
	protected ReversedMapLiftedFlowSet entryInitialFlow() {
		return this.allLocals.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected ReversedMapLiftedFlowSet newInitialFlow() {
		return this.emptySet.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
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
					kill(sourceFlowSet, unit, destFlowSet);
					
					// Add the mapping to the top level FlowSet (mapping)
					IConfigRep config = destMapping.get(destFlowSet);
					if (config == null)
						destMapping.put(destFlowSet, first);
					else 
						destMapping.put(destFlowSet, ((ILazyConfigRep)config).union(first));
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
						else 
							destMapping.put(destFlowSet, ((ILazyConfigRep)config).union(second));
					}

					/*
					 * This flowset will contain the result of the GEN/KILL operations,
					 * and is to be mapped from FIRST.
					 */
					FlowSet destToBeAppliedLattice = sourceFlowSet.clone();
					kill(sourceFlowSet, unit, destToBeAppliedLattice);
					destMapping.put(destToBeAppliedLattice, first);
				}
			} else {
				/*
				 *  There is nothing to be done in this case, thus we only copy the mapping
				 *  from the source.
				 */
				IConfigRep config = destMapping.get(sourceFlowSet);
				if (config == null)
					destMapping.put(sourceFlowSet, lazyConfig);
				else
					destMapping.put(sourceFlowSet, ((ILazyConfigRep)config).union(lazyConfig));
			}
			
		}
		// #ifdef METRICS
		timeSpentOnFlowThrough = System.nanoTime() - timeSpentOnFlowThrough;
		this.flowThroughTimeAccumulator += timeSpentOnFlowThrough;
		// #endif
	}

	private void kill(FlowSet source, Unit unit, FlowSet dest) {
		if (unit instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) unit;
			Value leftOp = assignStmt.getLeftOp();
			if (leftOp instanceof Local) {
				dest.remove(leftOp);
			}
		}
	}

}
