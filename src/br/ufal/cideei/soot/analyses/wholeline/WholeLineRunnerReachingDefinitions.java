package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;
import java.util.Set;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import br.ufal.cideei.soot.analyses.reachingdefs.UnliftedReachingDefinitions;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class WholeLineRunnerReachingDefinitions extends BodyTransformer {

	private static WholeLineRunnerReachingDefinitions instance = new WholeLineRunnerReachingDefinitions();

	private WholeLineRunnerReachingDefinitions() {
	}

	public static WholeLineRunnerReachingDefinitions v() {
		return instance;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		BriefUnitGraph bodyGraph = new BriefUnitGraph(body);
		FeatureTag<Set<String>> featureTag = (FeatureTag<Set<String>>) body.getTag("FeatureTag");

		// #ifdef METRICS
		long startAnalysis = System.nanoTime();
		// #endif

		for (Set<String> configuration : featureTag) {
			new UnliftedReachingDefinitions(bodyGraph, configuration);
		}

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setRdAnalysisTime(endAnalysis - startAnalysis);
		//#endif
	}

}