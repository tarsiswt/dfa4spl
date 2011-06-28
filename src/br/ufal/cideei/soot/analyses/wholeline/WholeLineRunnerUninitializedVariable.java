package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;
import java.util.Set;

//#ifdef METRICS
import profiling.ProfilingTag;
//#endif
import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import br.ufal.cideei.soot.analyses.uninitvars.UnliftedUnitializedVariablesAnalysis;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class WholeLineRunnerUninitializedVariable extends BodyTransformer {

	private static WholeLineRunnerUninitializedVariable instance = new WholeLineRunnerUninitializedVariable();

	private WholeLineRunnerUninitializedVariable() {
	}

	public static WholeLineRunnerUninitializedVariable v() {
		return instance;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		BriefUnitGraph bodyGraph = new BriefUnitGraph(body);
		FeatureTag<Set<String>> featureTag = (FeatureTag<Set<String>>) body.getTag("FeatureTag");

		// #ifdef METRICS
		long startAnalysis = System.nanoTime();
		// #endif

		int numOfConfigurations = featureTag.size();
		for (int index = 0;index < numOfConfigurations; index++) {
			new UnliftedUnitializedVariablesAnalysis(bodyGraph, index);
		}

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();
		
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setUvAnalysisTime(endAnalysis - startAnalysis);
		// #endif
	}

}
