package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;
import java.util.Set;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import br.ufal.cideei.soot.analyses.reachingdefs.LiftedReachingDefinitions;
import br.ufal.cideei.soot.instrument.FeatureTag;
//TODO: can this class structure could be replaced by an abstract factory?
public class WholeLineLiftedReachingDefinitions extends BodyTransformer {

	private static WholeLineLiftedReachingDefinitions instance = new WholeLineLiftedReachingDefinitions();

	private WholeLineLiftedReachingDefinitions() {
	}

	public static WholeLineLiftedReachingDefinitions v() {
		return instance;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		BriefUnitGraph bodyGraph = new BriefUnitGraph(body);
		FeatureTag<Set<String>> featureTag = (FeatureTag<Set<String>>) body.getTag("FeatureTag");

		//#ifdef METRICS
		long startAnalysis = System.nanoTime();
		// #endif

		LiftedReachingDefinitions liftedReachingDefinitions = new LiftedReachingDefinitions(bodyGraph, featureTag.getFeatures());
		liftedReachingDefinitions.execute();

		//#ifdef METRICS
		long endAnalysis = System.nanoTime();

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setRdAnalysisTime2(endAnalysis - startAnalysis);
		//#endif
	}

}
