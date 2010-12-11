package br.ufal.cideei.soot.analyses.reachingdefs;

import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.analyses.AnalysisFactory;

public class FeatureSensitiveReachingDefinitionsFactory implements AnalysisFactory<FeatureSensitiveReachingDefinitions> {

	private static FeatureSensitiveReachingDefinitionsFactory instance;
	
	private FeatureSensitiveReachingDefinitionsFactory() {}

	@Override
	public FeatureSensitiveReachingDefinitions newAnalysis(DirectedGraph<Unit> graph, Set<String> configuration, Map<Object,Object> options) {
		return new FeatureSensitiveReachingDefinitions(graph,configuration,options);
	}

	public static FeatureSensitiveReachingDefinitionsFactory getInstance() {
		if (instance == null) {
			instance = new FeatureSensitiveReachingDefinitionsFactory();
		}
		return instance;
	}

}
