package br.ufal.cideei.soot.analyses;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.toolkits.graph.DirectedGraph;

import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachedDefinitionsAnalysis;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachingDefinitions;

public class FeatureSensitiveAnalysisRunner {

	private Set<Set<Object>> configurations;
	private DirectedGraph graph;
	private Class analysis;
	private Map<Set<Object>,FeatureSensitiviteFowardFlowAnalysis> configurationAnalysisMap;

	public FeatureSensitiveAnalysisRunner(DirectedGraph graph,Set<Set<Object>> configurations, Class analysis) {
		this.graph = graph;
		this.configurations = configurations;
		this.analysis = analysis;
		this.configurationAnalysisMap = new HashMap<Set<Object>, FeatureSensitiviteFowardFlowAnalysis>(configurations.size());
	}
	
	public void execute() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Iterator<Set<Object>> iterator = configurations.iterator();
		while(iterator.hasNext()) {
			Set<Object> config = iterator.next();
			Constructor<? extends FeatureSensitiviteFowardFlowAnalysis> constructor = analysis.getConstructor(DirectedGraph.class,Set.class);
			FeatureSensitiviteFowardFlowAnalysis instance = constructor.newInstance(graph,config);			
			configurationAnalysisMap.put(config, instance);
		}
	}
	
	public Map<Set<Object>,FeatureSensitiviteFowardFlowAnalysis> getResults(){
		return configurationAnalysisMap;
	}
}