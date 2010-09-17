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

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleReachingDefinitionsAnalysis.
 */
public class SimpleReachedDefinitionsAnalysis extends AbstractReachedDefinitions{
	
	/** The reaching definitions. */
	private SimpleReachingDefinitions reachingDefinitions;

	/**
	 * Instantiates a new simple reaching definitions analysis.
	 *
	 * @param graph the graph
	 */
	public SimpleReachedDefinitionsAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		this.reachingDefinitions = new SimpleReachingDefinitions(graph);
	}
	
	/**
	 * Gets the reached uses.
	 *
	 * @param target the target
	 * @return the reached uses
	 */
	public List<Unit> getReachedUses(Unit target){
		Iterator<Unit> unitIterator = graph.iterator();
		List<Unit> reached = new ArrayList<Unit>();
		while(unitIterator.hasNext()){
			Unit nextUnit = unitIterator.next();
			if (nextUnit instanceof NopStmt){
				continue;
			}
			FlowSet reachingDefSet = this.reachingDefinitions.getFlowAfter(nextUnit);
			Iterator<? extends Unit> flowIterator = reachingDefSet.toList().iterator();
			while(flowIterator.hasNext()){
				Unit nextUnitInFlow = flowIterator.next();
				if (nextUnitInFlow instanceof NopStmt){
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
	 * Creates a KILL set for a given Unit and it to the FlowSet dest. In this
	 * case, our KILL set are the Assignments made to the same Value that this
	 * Unit assigns to.
	 * 
	 * @param src
	 *            the src
	 * @param unit
	 *            the unit
	 * @param dest
	 *            the dest
	 */
	private void kill(FlowSet source, Unit unit, FlowSet dest) {
		FlowSet kills = emptySet.clone();
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
	 */
	// TODO: MUST ITERATOR THROUGH ALL DEFBOXES!!!
	private void gen(FlowSet dest, Unit unit) {
		if (unit instanceof AssignStmt) {
			dest.add(unit);
		}
	}
}
