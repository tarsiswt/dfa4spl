package br.ufal.cideei.soot.analyses.uninitvars;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;
import br.ufal.cideei.soot.analyses.LiftedFlowSet;
import br.ufal.cideei.soot.instrument.FeatureTag;

/**
 * This implementation of the Initialized variable analysis uses a LiftedFlowSet
 * as a lattice element. The only major change is how it's KILL method is
 * implemented. Also, the gen method is empty. We fill the lattice with local
 * variables at the class constructor.
 */
public class LiftedUninitializedVariableAnalysis extends ForwardFlowAnalysis<Unit, LiftedFlowSet> {

	/** The empty set. */
	/*
	 * FIXME: the clone method of LiftedFlowSet is not working properly right
	 * now.
	 */
	private LiftedFlowSet emptySet;
	private Collection<Set<String>> configurations;

	/**
	 * Instantiates a new TestReachingDefinitions.
	 * 
	 * @param graph
	 *            the graph
	 * @param configs
	 *            the configurations.
	 */
	public LiftedUninitializedVariableAnalysis(DirectedGraph<Unit> graph, Collection<Set<String>> configs) {
		super(graph);
		this.configurations = configs;
		this.emptySet = new LiftedFlowSet(configs);

		if (graph instanceof UnitGraph) {
			UnitGraph ug = (UnitGraph) graph;

			Chain locals = ug.getBody().getLocals();

			for (Object object : locals) {
				Local local = (Local) object;

				if (!local.getName().contains("$")) {
					emptySet.add(local);
				}
			}
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
	 * case, if the Unit has an assignment to a variable contained in the
	 * lattice, we remove that variable from there, since it was definitely
	 * initialized.
	 * 
	 * @param source
	 *            the source
	 * @param unit
	 *            the unit
	 * @param dest
	 *            the dest
	 */
	private void kill(LiftedFlowSet source, Unit unit, LiftedFlowSet dest) {

		LiftedFlowSet kills = new LiftedFlowSet(this.configurations);
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
					for (Object declaredVariable : flowSetList) {
						if (declaredVariable instanceof Local) {
							Local local = (Local) declaredVariable;
							if (local.equivTo(assignStmt.getLeftOp())) {
								kills.add(validConfig, local);
							}
						}
					}
				}
			}
		}
		source.difference(kills, dest);
	}

	/**
	 * @param dest
	 *            the dest
	 * @param unit
	 *            the unit
	 */
	private void gen(LiftedFlowSet dest, Unit unit) {
		// do nothing
	}

}
