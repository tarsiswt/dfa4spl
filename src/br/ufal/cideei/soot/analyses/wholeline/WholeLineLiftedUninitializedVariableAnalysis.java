package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;
import java.util.Set;

import br.ufal.cideei.soot.analyses.reachingdefs.LiftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.uninitvars.LiftedUninitializedVariableAnalysis;
import br.ufal.cideei.soot.instrument.FeatureTag;

import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;

public class WholeLineLiftedUninitializedVariableAnalysis extends BodyTransformer {


	private static WholeLineLiftedUninitializedVariableAnalysis instance = new WholeLineLiftedUninitializedVariableAnalysis();
	private long analysisTime = 0;

	private WholeLineLiftedUninitializedVariableAnalysis() {
	}

	public static WholeLineLiftedUninitializedVariableAnalysis v() {
		return instance;
	}

	public long getAnalysesTime() {
		long tmp = analysisTime;
		return tmp;
	}

	public void reset() {
		analysisTime = 0;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		BriefUnitGraph bodyGraph = new BriefUnitGraph(body);
		FeatureTag<Set<String>> featureTag = (FeatureTag<Set<String>>) body.getTag("FeatureTag");

		// #ifdef METRICS
		long beforeRunner = System.nanoTime();
		// #endif
		new LiftedUninitializedVariableAnalysis(bodyGraph, featureTag.getFeatures());
		// #ifdef METRICS
		long afterRunner = System.nanoTime();
		this.analysisTime += afterRunner - beforeRunner;
//		System.out.println("[Lifted Lattice]" + body.getMethod() + " with " + body.getTag("FeatureTag") + " took "
//				+ ((double) (afterRunner - beforeRunner) / 1000000));
		// #endif
	}


}
