package br.ufal.cideei.soot.analyses;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import br.ufal.cideei.soot.instrument.FeatureTag;

// TODO: Auto-generated Javadoc
/**
 * This implementation of the Reaching Definitions analysis uses a LiftedFlowSet
 * as a lattice element. The only major change is how it's KILL method is
 * implemented. Everything else is quite similar to a 'regular' FlowSet-based
 * analysis.
 */
public class TestReachingDefinitions extends ForwardFlowAnalysis<Unit, LiftedFlowSet<Collection<Set<Object>>>> {

	/** The empty set. */
	/*
	 * FIXME: the clone method of LiftedFlowSet is not working properly right
	 * now.
	 */
	private LiftedFlowSet<Collection<Set<String>>> emptySet;
	private Collection<Set<String>> configurations;

	/**
	 * Instantiates a new TestReachingDefinitions.
	 * 
	 * @param graph
	 *            the graph
	 */
	public TestReachingDefinitions(DirectedGraph<Unit> graph, Collection<Set<String>> configs) {
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
		return this.emptySet.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected LiftedFlowSet newInitialFlow() {
		return this.emptySet.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(LiftedFlowSet source, Unit unit, LiftedFlowSet dest) {
		kill(source, unit, dest);
		gen(dest, unit);
	}

	/**
	 * Creates a KILL set for a given Unit and it to the FlowSet dest. In this
	 * case, our KILL set are the Assignments made to the same Value that this
	 * Unit assigns to.
	 * 
	 * @param source
	 *            the source
	 * @param unit
	 *            the unit
	 * @param dest
	 *            the dest
	 */
	private void kill(LiftedFlowSet source, Unit unit, LiftedFlowSet dest) {

		LiftedFlowSet<Collection<Set<String>>> kills = new LiftedFlowSet(this.configurations);
		// FIXME: clone not working correctly!
		// LiftedFlowSet kills = this.emptySet.clone();

		/*
		 * For the kill set, we are only interested on Assignments.
		 */
		if (unit instanceof AssignStmt) {
			Map<Set<String>, FlowSet> liftedMap = source.getMap();
			Set<Set<String>> configurations = liftedMap.keySet();

			FeatureTag<Set<String>> tag = (FeatureTag<Set<String>>) unit.getTag("FeatureTag");
			Collection<Set<String>> features = tag.getFeatures();

			AssignStmt assignStmt = (AssignStmt) unit;

			/*
			 * Iterate through all valid configurations as intrumented in the
			 * FeatureTag. If for every configuration C, C contains an earlier
			 * assignment made to the same Local we are looking at now (unit),
			 * then it must be killed.
			 */
			for (Set<String> validConfig : features) {
				if (configurations.contains(validConfig)) {
					List flowSetList = liftedMap.get(validConfig).toList();
					for (Object earlierAssignment : flowSetList) {
						if (earlierAssignment instanceof AssignStmt) {
							AssignStmt stmt = (AssignStmt) earlierAssignment;
							if (stmt.getLeftOp().equivTo(assignStmt.getLeftOp())) {
								kills.add(validConfig, earlierAssignment);
							}
						}
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
	private void gen(LiftedFlowSet dest, Unit unit) {
		if (unit instanceof AssignStmt) {
			dest.add(unit);
		}
	}
}