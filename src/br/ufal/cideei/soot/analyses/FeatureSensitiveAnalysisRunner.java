package br.ufal.cideei.soot.analyses;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachedDefinitionsAnalysis;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachingDefinitionsFactory;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachingDefinitions;
import br.ufal.cideei.soot.analyses.uninitvars.FeatureSensitiveUninitializedVariablesFactory;

public class FeatureSensitiveAnalysisRunner {

	private Collection<Set<String>> configurations;
	private DirectedGraph graph;
	private Map<Set<String>, FowardFlowAnalysis> configurationAnalysisMap;
	private Map options;
	private AnalysisFactory<? extends FowardFlowAnalysis> factory;

	public FeatureSensitiveAnalysisRunner(DirectedGraph graph, Collection<Set<String>> configurations, AnalysisFactory<? extends FowardFlowAnalysis> factory , Map options) {
		this.graph = graph;
		this.configurations = configurations;
		this.options = options;
		this.factory = factory;
		this.configurationAnalysisMap = new HashMap<Set<String>, FowardFlowAnalysis>(configurations.size());
	}

	public void execute2() {
		Iterator<Set<String>> iterator = configurations.iterator();
		while (iterator.hasNext()) {
			Set<String> config = iterator.next();
			FowardFlowAnalysis newAnalysis = factory.newAnalysis(graph, config, options);
			configurationAnalysisMap.put(config, newAnalysis);
		}
	}

	public Map<Set<String>, FowardFlowAnalysis> getResults() {
		return configurationAnalysisMap;
	}
}