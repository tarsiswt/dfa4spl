/*
 * 
 */
package br.ufal.cideei.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;

// TODO: Auto-generated Javadoc
/**
 * This class is used to topologically sort a given directed graph.
 *
 * @param <E> the element type
 * @param <V> the value type
 */
public class TopologicalSort<E, V> {
	
	/** The sorted. */
	private List<V> sorted = new ArrayList<V>();
	
	/** The marked. */
	private List<V> marked = new ArrayList<V>();
	
	/** The graph. */
	private DirectedGraph<V, E> graph;

	/**
	 * Instantiates a new topological sort.
	 *
	 * @param graph the graph
	 */
	public TopologicalSort(DirectedGraph<V, E> graph) {
		this.graph = graph;
	}

	/**
	 * Sort.
	 *
	 * @return the list
	 */
	public List<V> sort() {
		Set<V> vertexes = graph.vertexSet();
		for (V vertex : vertexes){
			this.visit(vertex);
		}
		return sorted;
	}

	/**
	 * Visit.
	 *
	 * @param node the node
	 */
	private void visit(V node) {
		if (!marked.contains(node)) {
			marked.add(node);
			Set<E> edgesOf = graph.edgesOf(node);
			for (E edge : edgesOf) {
				if (graph.getEdgeSource(edge).equals(node)) {
					visit(graph.getEdgeTarget(edge));
				}
			}
			sorted.add(node);
		}
	}
}
