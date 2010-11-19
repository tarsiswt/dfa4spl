package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;
import java.util.Set;

import br.ufal.cideei.soot.analyses.TestReachingDefinitions;
import br.ufal.cideei.soot.instrument.FeatureTag;

import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;

public class WholeLineLiftedReachingDefinitions extends BodyTransformer {

	private static WholeLineLiftedReachingDefinitions instance = new WholeLineLiftedReachingDefinitions();
	private long analysisTime = 0;

	private WholeLineLiftedReachingDefinitions() {
	}

	public static WholeLineLiftedReachingDefinitions v() {
		return instance;
	}

	public long getAnalysesTime() {
		return analysisTime;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
//		System.out.println("[While Lifted] analysing " + body.getMethod() + " with " + body.getTag("FeatureTag"));
		BriefUnitGraph bodyGraph = new BriefUnitGraph(body);
		FeatureTag<Set<String>> featureTag = (FeatureTag<Set<String>>) body.getTag("FeatureTag");

		long beforeRunner = System.currentTimeMillis();
		TestReachingDefinitions tst = new TestReachingDefinitions(bodyGraph, featureTag.getFeatures());
		long afterRunner = System.currentTimeMillis();
		this.analysisTime += afterRunner - beforeRunner;

	}

}
