package br.ufal.cideei.soot.analyses.reachingdefs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NopStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import br.ufal.cideei.soot.analyses.LiftedFlowSet;
import br.ufal.cideei.soot.instrument.FeatureTag;

// TODO: Auto-generated Javadoc
/**
 * This implementation of the Reaching Definitions analysis uses a LiftedFlowSet
 * as a lattice element. The only major change is how its KILL method is
 * implemented. Everything else is quite similar to a 'regular' FlowSet-based
 * analysis.
 */
public class LiftedReachingDefinitions extends ForwardFlowAnalysis<Unit, LiftedFlowSet<Collection<Set<Object>>>> {

	private Collection<Set<String>> configurations;

	// #ifdef METRICS
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
	public LiftedReachingDefinitions(DirectedGraph<Unit> graph, Collection<Set<String>> configs) {
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
		// #ifdef METRICS
		flowThroughCounter++;
		// #endif

		FeatureTag<String> tag = (FeatureTag<String>) unit.getTag("FeatureTag");
		Collection<String> features = tag.getFeatures();
		int id = tag.getId();

		Set<String>[] configurations = source.getConfigurations();

		FlowSet[] sourceLattices = source.getLattices();
		FlowSet[] destLattices = dest.getLattices();

		for (int index = 0; index < sourceLattices.length; index++) {
			FlowSet sourceFlowSet = sourceLattices[index];
			FlowSet destFlowSet = destLattices[index];

			if ((id & index) == id) {
				/*
				 * TODO: a configuração não é mais a correta aqui. Estamos
				 * usando de 0 .. (2^n)-1 para iterar sobre as possíveis
				 * configurações, como pode ser visto no if acima.
				 */
				kill(sourceFlowSet, unit, destFlowSet, configurations[index]);
				gen(sourceFlowSet, unit, destFlowSet, configurations[index]);
			} else {
				sourceFlowSet.copy(destFlowSet);
			}
		}
	}

	protected void kill(FlowSet source, Unit unit, FlowSet dest, Set<String> configuration) {
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
	 * Creates a GEN set for a given Unit and it to the FlowSet dest. In this
	 * case, our GEN set are all the definitions present in the unit.
	 * 
	 * @param dest
	 *            the dest
	 * @param unit
	 *            the unit
	 * @param configuration
	 */
	// TODO: MUST ITERATE THROUGH ALL DEFBOXES!!!
	protected void gen(FlowSet source, Unit unit, FlowSet dest, Set<String> configuration) {
		if (unit instanceof AssignStmt) {
			dest.add(unit);
		}
	}

	public List<Unit> getReachedUses(Unit target, Set<String> configuration) {
		int index = 0;
		for (Set<String> configuration1 : configurations) {
			if (configuration.equals(configuration1)) {
				Iterator<Unit> unitIterator = graph.iterator();
				List<Unit> reached = new ArrayList<Unit>();
				while (unitIterator.hasNext()) {
					Unit nextUnit = unitIterator.next();

					LiftedFlowSet reachingDefSet = this.getFlowAfter(nextUnit);
					FlowSet flowSet = reachingDefSet.getLattices()[index];
					Iterator<? extends Unit> flowIterator = flowSet.toList().iterator();
					if (flowSet.contains(target)) {
						reached.add(nextUnit);
					}
				}
				return reached;
			}
		}
		index++;
		return null;
	}

	public void execute() {
		this.doAnalysis();
	}

}