package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.analyses.uninitvars.SimpleUninitializedVariableAnalysis;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class CopyOfWholeLineSimpleUninitializedVariablesAnalysis extends BodyTransformer {

	private static CopyOfWholeLineSimpleUninitializedVariablesAnalysis instance = new CopyOfWholeLineSimpleUninitializedVariablesAnalysis();

	private CopyOfWholeLineSimpleUninitializedVariablesAnalysis() {
	}

	public static CopyOfWholeLineSimpleUninitializedVariablesAnalysis v() {
		return instance;
	}

	// #ifdef METRICS
	private long analysisTime = 0;

	public long getAnalysesTime() {
		return analysisTime;
	}

	public void reset() {
		analysisTime = 0;
	}

	// #ifdef METRICS

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		DirectedGraph<Unit> bodyGraph = new BriefUnitGraph(body);

		// #ifdef METRICS
		long beforeRunner = System.nanoTime();
		// #endif
		new SimpleUninitializedVariableAnalysis(bodyGraph);
		// #ifdef METRICS
		long afterRunner = System.nanoTime();
		long delta = afterRunner - beforeRunner;
		FeatureTag tag = (FeatureTag) body.getTag("FeatureTag");
		int size = tag.size();
		// if contains color
		if (size > 1) {
			System.out.println(body.getMethod() + " " + tag + " " + size);
			System.out.println("delta:" + delta);
			long factor = (long) (Math.log(size)/Math.log(2));
			delta = delta * factor;
			System.out.println("fator: " + factor);
			System.out.println("newdelta: " + delta);
			this.analysisTime += delta;
		} else {
			this.analysisTime += delta;
		}
		// #endif
	}
}
