/*
 * 
 */
package br.ufal.cideei.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;

// TODO: Auto-generated Javadoc
/**
 * The Class DirectedAcyclicGraphLongestPath is used to compute the longest path between vertexes in a graph.
 *
 * @param <V> the value type
 * @param <E> the element type
 */
public class DirectedAcyclicGraphLongestPath<V, E> {
	
	/** The graph. */
	private DirectedGraph<V, E> graph;
	
	/** The length to. */
	private Double[] lengthTo;

	/**
	 * Instantiates a new directed acyclic graph longest path.
	 *
	 * @param graph the graph
	 */
	public DirectedAcyclicGraphLongestPath(DirectedGraph<V,E> graph) {
		this.graph = graph;		
		Map<V,Integer> lengthMap = new HashMap<V, Integer>();
		List<V> topSorted = new TopologicalSort<E, V>(graph).sort();
		Double[] lengthTo = new Double[topSorted.size()];
		Set<E> edgeSet = graph.edgeSet();
		for (V vertex : topSorted){
			for (E edge : edgeSet){
				int v =topSorted.indexOf(graph.getEdgeSource(edge)), w = topSorted.indexOf(graph.getEdgeTarget(edge));
				if (lengthTo[w] <= lengthTo[v] + graph.getEdgeWeight(edge)){
					lengthTo[w] = lengthTo[v] + graph.getEdgeWeight(edge);
				}
			}
		}
		this.lengthTo = lengthTo;
	}
	
	/**
	 * Gets the longest path.
	 *
	 * @return the longest path
	 */
	public double getLongestPath(){
		List<Double> list = Arrays.asList(this.lengthTo);
		return Collections.max(list);
	}
	
}
