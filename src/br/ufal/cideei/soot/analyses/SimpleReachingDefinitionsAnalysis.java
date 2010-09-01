package br.ufal.cideei.soot.analyses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Unit;
import soot.jimple.NopStmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleReachingDefinitionsAnalysis.
 */
public class SimpleReachingDefinitionsAnalysis {
	
	/** The reaching definitions. */
	private SimpleReachingDefinitions reachingDefinitions;
	
	/** The graph. */
	DirectedGraph<Unit> graph;

	/**
	 * Instantiates a new simple reaching definitions analysis.
	 *
	 * @param graph the graph
	 */
	public SimpleReachingDefinitionsAnalysis(DirectedGraph<Unit> graph) {
		this.graph = graph;
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
