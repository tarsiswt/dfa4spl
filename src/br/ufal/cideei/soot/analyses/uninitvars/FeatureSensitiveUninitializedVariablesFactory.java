package br.ufal.cideei.soot.analyses.uninitvars;

import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.analyses.AnalysisFactory;

public class FeatureSensitiveUninitializedVariablesFactory implements AnalysisFactory<FeatureSensitiveUninitializedVariables> {

	private static FeatureSensitiveUninitializedVariablesFactory instance;

	@Override
	public FeatureSensitiveUninitializedVariables newAnalysis(DirectedGraph<Unit> graph, Set<String> configuration, Map<Object, Object> options) {
		return new FeatureSensitiveUninitializedVariables(graph, configuration, options);
	}

	public static FeatureSensitiveUninitializedVariablesFactory getInstance() {
		if (instance == null) {
			instance = new FeatureSensitiveUninitializedVariablesFactory();
		}
		return instance;
	}

}
