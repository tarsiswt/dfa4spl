package br.ufal.cideei.soot.analyses;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleReachingDefinitions.
 */
class SimpleReachingDefinitions extends ForwardFlowAnalysis<Unit, FlowSet> {

	/** The empty set. */
	private FlowSet emptySet;

	/**
	 * Instantiates a new simple reaching definitions.
	 *
	 * @param graph the graph
	 */
	public SimpleReachingDefinitions(DirectedGraph<Unit> graph) {
		super(graph);
		this.emptySet = new ArraySparseSet();
		super.doAnalysis();
	}

	/* (non-Javadoc)
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#copy(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void copy(FlowSet source, FlowSet dest) {
		source.copy(dest);
	}

	/* (non-Javadoc)
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#merge(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void merge(FlowSet source1, FlowSet source2, FlowSet dest) {
		source1.union(source2, dest);
	}

	/* (non-Javadoc)
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#entryInitialFlow()
	 */
	@Override
	protected FlowSet entryInitialFlow() {
		return this.emptySet.clone();
	}

	/* (non-Javadoc)
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected FlowSet newInitialFlow() {
		return this.emptySet.clone();
	}

	/* (non-Javadoc)
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(FlowSet source, Unit unit, FlowSet dest) {
		kill(source, unit, dest);
		gen(dest, unit);
	}

	/**
	 * Kill.
	 *
	 * @param src the src
	 * @param unit the unit
	 * @param dest the dest
	 */
	private void kill(FlowSet src, Unit unit, FlowSet dest) {
		FlowSet kills = emptySet.clone();
		if (unit instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) unit;
			for (Object earlierAssignment : src.toList()) {
				if (earlierAssignment instanceof AssignStmt) {
					AssignStmt stmt = (AssignStmt) earlierAssignment;
					if (stmt.getLeftOp().equivTo(assignStmt.getLeftOp())) {
						kills.add(earlierAssignment);
					}
				}
			}
		}
		src.difference(kills, dest);
	}

	/**
	 * Gen.
	 *
	 * @param dest the dest
	 * @param unit the unit
	 */
	private void gen(FlowSet dest, Unit unit) {
		if (unit instanceof AssignStmt) {
			dest.add(unit);
		}
	}
}
