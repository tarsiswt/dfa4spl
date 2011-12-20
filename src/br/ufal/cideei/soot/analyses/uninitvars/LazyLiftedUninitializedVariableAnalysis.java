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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

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
public class LazyLiftedUninitializedVariableAnalysis extends ForwardFlowAnalysis<Unit, MapLiftedFlowSet> {

	private MapLiftedFlowSet allLocals;
	private MapLiftedFlowSet emptySet;

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
	public LazyLiftedUninitializedVariableAnalysis(UnitGraph unitGraph, ILazyConfigRep configs) {
		super(unitGraph);
		this.allLocals = new MapLiftedFlowSet(configs);
		this.emptySet = new MapLiftedFlowSet(configs);

		Chain<Local> locals = unitGraph.getBody().getLocals();
		FlowSet flowSet = allLocals.getMapping().get(configs);
		for (Local local : locals) {
			flowSet.add(local);
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
		return this.allLocals.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected MapLiftedFlowSet newInitialFlow() {
		return this.emptySet.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
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
					kill(sourceFlowSet, unit, destFlowSet);
				} else {
					ILazyConfigRep second = split.getSecond();

					/*
					 * in this case, this lattice doesnt have a copy from the sourceFlowSet
					 */
					FlowSet destToBeAppliedLattice = new ArraySparseSet();

					// apply point-wise transfer function
					kill(sourceFlowSet, unit, destToBeAppliedLattice);

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
