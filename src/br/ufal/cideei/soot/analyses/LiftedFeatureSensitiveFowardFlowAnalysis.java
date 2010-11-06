package br.ufal.cideei.soot.analyses;

import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public abstract class LiftedFeatureSensitiveFowardFlowAnalysis<N, A> extends ForwardFlowAnalysis<N, A>{

	public LiftedFeatureSensitiveFowardFlowAnalysis(DirectedGraph<N> graph) {
		super(graph);
		// TODO Auto-generated constructor stub
	}
	
}
