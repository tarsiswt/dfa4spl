package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;
import java.util.Set;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import br.ufal.cideei.soot.analyses.uninitvars.LiftedUninitializedVariableAnalysis;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class WholeLineLiftedUninitializedVariableAnalysis extends BodyTransformer {

	private static WholeLineLiftedUninitializedVariableAnalysis instance = new WholeLineLiftedUninitializedVariableAnalysis();

	private WholeLineLiftedUninitializedVariableAnalysis() {
	}

	public static WholeLineLiftedUninitializedVariableAnalysis v() {
		return instance;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		BriefUnitGraph bodyGraph = new BriefUnitGraph(body);
		FeatureTag<Set<String>> featureTag = (FeatureTag<Set<String>>) body.getTag("FeatureTag");

		// #ifdef METRICS
		long startAnalysis = System.nanoTime();
		// #endif

		new LiftedUninitializedVariableAnalysis(bodyGraph, featureTag.getFeatures());

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();
		
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setUvAnalysisTime2(endAnalysis - startAnalysis);
		//#endif
	}

}
