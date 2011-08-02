package br.ufal.cideei.soot.analyses.uninitvars;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;
import br.ufal.cideei.soot.analyses.MapLiftedFlowSet;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;

/**
 * This implementation of the Initialized variable analysis uses a LiftedFlowSet as a lattice element. The only major
 * change is how it's KILL method is implemented. Also, the gen method is empty. We fill the lattice with local
 * variables at the class constructor.
 */
public class LiftedUninitializedVariableAnalysis extends ForwardFlowAnalysis<Unit, MapLiftedFlowSet> {

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
	public LiftedUninitializedVariableAnalysis(UnitGraph unitGraph, Collection<IConfigRep> configs) {
		super(unitGraph);
		this.allLocals = new MapLiftedFlowSet(configs);
		this.emptySet = new MapLiftedFlowSet(configs);

		Chain<Local> locals = unitGraph.getBody().getLocals();
		for (Local local : locals) {
			if (!local.getName().contains("$")) {
				HashMap<IConfigRep, FlowSet> mapping = this.allLocals.getMapping();
				Set<Entry<IConfigRep, FlowSet>> entrySet = mapping.entrySet();
				for (Entry<IConfigRep, FlowSet> entry : entrySet) {
					entry.getValue().add(local);
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
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(MapLiftedFlowSet source, Unit unit, MapLiftedFlowSet dest) {
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
				kill(sourceFlowSet, unit, destFlowSet);
			} else {
				sourceFlowSet.copy(destFlowSet);
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
