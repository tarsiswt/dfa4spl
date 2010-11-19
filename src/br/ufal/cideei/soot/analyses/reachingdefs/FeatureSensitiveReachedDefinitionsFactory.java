package br.ufal.cideei.soot.analyses.reachingdefs;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.analyses.AnalysisFactory;

public class FeatureSensitiveReachedDefinitionsFactory implements AnalysisFactory<FeatureSensitiveReachingDefinitions> {

	@Override
	public FeatureSensitiveReachingDefinitions newAnalysis(DirectedGraph<Unit> graph, Set<String> configurations, Map<Object,Object> options) {
		return new FeatureSensitiveReachingDefinitions(graph,configurations,options);
	}

}
