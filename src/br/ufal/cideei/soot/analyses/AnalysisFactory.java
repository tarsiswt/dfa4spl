package br.ufal.cideei.soot.analyses;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;

public abstract interface AnalysisFactory<T> {
	public abstract T newAnalysis(DirectedGraph<Unit> graph, Set<String> configurations, Map<Object,Object> options);
}
