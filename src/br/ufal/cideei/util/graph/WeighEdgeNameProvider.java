package br.ufal.cideei.util.graph;

import org.jgrapht.WeightedGraph;
import org.jgrapht.ext.EdgeNameProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class WeighEdgeNameProvider is a utility class used to provide a label
 * for edges when transforming it to a serializes format like .DOT. It simple
 * adds the double corresponding to its weight.
 * 
 * @param <E>
 *            the element type
 */
public class WeighEdgeNameProvider<E> implements EdgeNameProvider<E> {

	/** The graph. */
	private WeightedGraph<?, E> graph;

	/**
	 * Instantiates a new weigh edge name provider.
	 * 
	 * @param graph
	 *            the graph
	 */
	public WeighEdgeNameProvider(WeightedGraph<?, E> graph) {
		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgrapht.ext.EdgeNameProvider#getEdgeName(java.lang.Object)
	 */
	@Override
	public String getEdgeName(E edge) {
		return graph.getEdgeWeight(edge) + "";
	}

}
