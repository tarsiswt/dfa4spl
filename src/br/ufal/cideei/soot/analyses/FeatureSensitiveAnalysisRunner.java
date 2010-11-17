package br.ufal.cideei.soot.analyses;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.toolkits.graph.DirectedGraph;

import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachedDefinitionsAnalysis;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachingDefinitions;

public class FeatureSensitiveAnalysisRunner {

	private Collection<Set<String>> configurations;
	private DirectedGraph graph;
	private Class analysis;
	private Map<Set<String>, FeatureSensitiviteFowardFlowAnalysis> configurationAnalysisMap;
	private Map options;

	public FeatureSensitiveAnalysisRunner(DirectedGraph graph, Collection<Set<String>> configurations, Class analysis, Map options) {
		this.graph = graph;
		this.configurations = configurations;
		this.analysis = analysis;
		this.options = options;
		this.configurationAnalysisMap = new HashMap<Set<String>, FeatureSensitiviteFowardFlowAnalysis>(configurations.size());
	}

	public void execute() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Iterator<Set<String>> iterator = configurations.iterator();
		while (iterator.hasNext()) {
			Set<String> config = iterator.next();
			Constructor<? extends FeatureSensitiviteFowardFlowAnalysis> constructor = analysis.getConstructor(DirectedGraph.class, Set.class, Map.class);
//			long start = System.currentTimeMillis();
			FeatureSensitiviteFowardFlowAnalysis instance = constructor.newInstance(graph, config, options);
//			long end = System.currentTimeMillis();
//			System.out.println("reaching definitions analysis for " + config + ": " + (end - start) + "ms");
			configurationAnalysisMap.put(config, instance);
		}
	}

	public Map<Set<String>, FeatureSensitiviteFowardFlowAnalysis> getResults() {
		return configurationAnalysisMap;
	}
}