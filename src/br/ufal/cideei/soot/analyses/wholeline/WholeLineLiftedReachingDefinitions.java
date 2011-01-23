package br.ufal.cideei.soot.analyses.wholeline;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import br.ufal.cideei.soot.analyses.reachingdefs.LiftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.reachingdefs.SimpleReachedDefinitionsAnalysis;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.util.WriterFacadeForAnalysingMM;

public class WholeLineLiftedReachingDefinitions extends BodyTransformer {

	private static WholeLineLiftedReachingDefinitions instance = new WholeLineLiftedReachingDefinitions();
	private long analysisTime = 0;

	private WholeLineLiftedReachingDefinitions() {
	}

	public static WholeLineLiftedReachingDefinitions v() {
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

		LiftedReachingDefinitions liftedReachingDefinitions = new LiftedReachingDefinitions(bodyGraph, featureTag.getFeatures());
		liftedReachingDefinitions.execute();
		// #ifdef METRICS
		long afterRunner = System.nanoTime();
		long delta = afterRunner - beforeRunner;
		this.analysisTime += delta;

		try {
			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.RD_LIFTED_COLUMN, Double.toString(((double) delta) / 1000000));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("[Lifted Lattice]" + body.getMethod() + " with " +
		// body.getTag("FeatureTag") + " took "
		// + ((double) (afterRunner - beforeRunner) / 1000000));
		// #endif
	}

}
