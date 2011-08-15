package br.ufal.cideei.soot.analyses.reachingdefs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NopStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class SimpleReachingDefinitions extends ForwardFlowAnalysis<Unit, FlowSet> implements IReachedDefinitions {

	// #ifdef METRICS
	protected long flowThroughTimeAccumulator = 0;

	public long getFlowThroughTime() {
		return this.flowThroughTimeAccumulator;
	}

	protected long flowThroughCounter = 0;

	public long getFlowThroughCounter() {
		return flowThroughCounter;
	}

	// #endif
	
	/**
	 * Instantiates a new simple reaching definitions.
	 * 
	 * @param graph
	 *            the graph
	 */
	public SimpleReachingDefinitions(DirectedGraph<Unit> graph) {
		super(graph);
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
		return new ArraySparseSet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowAnalysis#newInitialFlow()
	 */
	@Override
	protected FlowSet newInitialFlow() {
		return new ArraySparseSet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void flowThrough(FlowSet source, Unit unit, FlowSet dest) {
		// #ifdef METRICS
		flowThroughCounter++;
		long timeSpentOnFlowThrough = System.nanoTime();
		// #endif
		
		kill(source, unit, dest);
		gen(dest, unit);
		
		// #ifdef METRICS
		timeSpentOnFlowThrough = System.nanoTime() - timeSpentOnFlowThrough;
		this.flowThroughTimeAccumulator += timeSpentOnFlowThrough;
		// #endif
	}

	/**
	 * Creates a KILL set for a given Unit and it to the FlowSet dest. In this case, our KILL set are the Assignments
	 * made to the same Value that this Unit assigns to.
	 * 
	 * @param src
	 *            the src
	 * @param unit
	 *            the unit
	 * @param dest
	 *            the dest
	 */
	private void kill(FlowSet source, Unit unit, FlowSet dest) {
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
	 */
	private void gen(FlowSet dest, Unit unit) {
		if (unit instanceof AssignStmt) {
			dest.add(unit);
		}
	}

	@Override
	public List<Unit> getReachedUses(Unit target) {
		Iterator<Unit> unitIterator = graph.iterator();
		List<Unit> reached = new ArrayList<Unit>();
		while (unitIterator.hasNext()) {
			Unit nextUnit = unitIterator.next();
			// Ignore nop statements
			if (nextUnit instanceof NopStmt) {
				continue;
			}

			FlowSet reachingDefSet = this.getFlowAfter(nextUnit);
			Iterator<? extends Unit> flowIterator = reachingDefSet.toList().iterator();
			while (flowIterator.hasNext()) {
				Unit nextUnitInFlow = flowIterator.next();
				if (nextUnitInFlow instanceof NopStmt) {
					continue;
				}
				if (nextUnitInFlow.equals(target)) {
					reached.add(nextUnit);
				}
			}
		}
		return reached;
	}
}
