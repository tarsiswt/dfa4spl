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
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;

// TODO: Auto-generated Javadoc
/**
 */
public class UnliftedUnitializedVariablesAnalysis extends ForwardFlowAnalysis<Unit, FlowSet> {

	private FlowSet allLocals;

	/** The empty set. */
	private FlowSet emptySet;

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
	
	private static long L1flowThroughCounter = 0;

	public static long getL1flowThroughCounter() {
		return L1flowThroughCounter;
	}

	public static void reset() {
		flowThroughCounter = 0;
		L1flowThroughCounter = 0;
	}

	// #endif

	/**
	 */
	public UnliftedUnitializedVariablesAnalysis(DirectedGraph<Unit> graph, IConfigRep configuration) {
		super(graph);
		this.configuration = configuration;
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
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(FlowSet source, Unit unit, FlowSet dest) {
		//#ifdef CACHEPURGE
		br.Main.waste();
		//#endif
		
		// #ifdef METRICS
		flowThroughCounter++;
		long timeSpentOnFlowThrough = System.nanoTime();
		// #endif

		FeatureTag tag = (FeatureTag) unit.getTag(FeatureTag.FEAT_TAG_NAME);
		IFeatureRep featureRep = tag.getFeatureRep();

		if (featureRep.belongsToConfiguration(configuration)) {
			L1flowThroughCounter++;
			kill(source, unit, dest);
		} else {
			source.copy(dest);
		}

		// #ifdef METRICS
		timeSpentOnFlowThrough = System.nanoTime() - timeSpentOnFlowThrough;
		this.flowThroughTimeAccumulator += timeSpentOnFlowThrough;
		// #endif
	}

	private void kill(FlowSet src, Unit unit, FlowSet dest) {
		if (unit instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) unit;
			Value leftOp = assignStmt.getLeftOp();
			if (leftOp instanceof Local) {
				dest.remove(leftOp);
			}
		}
	}

}
