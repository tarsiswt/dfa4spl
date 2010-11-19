package br.ufal.cideei.soot.analyses.wholeline;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import br.ufal.cideei.soot.analyses.FeatureSensitiveAnalysisRunner;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachedDefinitionsFactory;
import br.ufal.cideei.soot.analyses.reachingdefs.FeatureSensitiveReachingDefinitions;
import br.ufal.cideei.soot.instrument.FeatureTag;

import soot.Body;
import soot.BodyTransformer;
import soot.tagkit.Tag;
import soot.toolkits.graph.BriefUnitGraph;

public class WholeLineRunnerReachingDefinitions extends BodyTransformer {

	private static WholeLineRunnerReachingDefinitions instance = new WholeLineRunnerReachingDefinitions();
	private long analysisTime = 0;

	private WholeLineRunnerReachingDefinitions() {
	}
	
	public static WholeLineRunnerReachingDefinitions v() {
		return instance;
	}
	
	public long getAnalysesTime() {
		return analysisTime;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
//		System.out.println("[While Runner] analysing " + body.getMethod() + " with " + body.getTag("FeatureTag"));
		BriefUnitGraph bodyGraph = new BriefUnitGraph(body);
		FeatureTag<Set<String>> featureTag = (FeatureTag<Set<String>>) body.getTag("FeatureTag");

		FeatureSensitiveAnalysisRunner runner = new FeatureSensitiveAnalysisRunner(bodyGraph, featureTag.getFeatures(),
				new FeatureSensitiveReachedDefinitionsFactory(), new HashMap<Object, Object>());
		try {
			long beforeRunner = System.currentTimeMillis();
			runner.execute2();
			long afterRunner = System.currentTimeMillis();
			this.analysisTime += afterRunner - beforeRunner;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
