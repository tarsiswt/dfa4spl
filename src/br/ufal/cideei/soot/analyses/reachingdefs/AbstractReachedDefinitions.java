package br.ufal.cideei.soot.analyses.reachingdefs;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;

/**
 * The Class AbstractReachedDefinitions is representational class which both the
 * analysis that are feature sensitive and featuer insensitive should extend.
 * 
 * The difference between and ReachingDefinitions analysis and a
 * ReachedDefinions is that in the later one, we are interested in which s a
 * given definition reaches, while in the other we are interested in which definitions reaches a given Unit.
 */
public abstract class AbstractReachedDefinitions implements ReachedDefinitions {

	/** The graph. */
	DirectedGraph<Unit> graph;

	/**
	 * Instantiates a new simple reaching definitions analysis.
	 * 
	 * @param graph
	 *            the graph
	 */
	public AbstractReachedDefinitions(DirectedGraph<Unit> graph) {
		this.graph = graph;
	}
}
