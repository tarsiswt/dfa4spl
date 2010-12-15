package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.analyses.uninitvars.SimpleUninitializedVariableAnalysis;

public class WholeLineSimpleUninitializedVariablesAnalysis extends BodyTransformer {

	private static WholeLineSimpleUninitializedVariablesAnalysis instance = new WholeLineSimpleUninitializedVariablesAnalysis();

	private WholeLineSimpleUninitializedVariablesAnalysis() {
	}

	public static WholeLineSimpleUninitializedVariablesAnalysis v() {
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
		this.analysisTime += afterRunner - beforeRunner;
		// #endif
	}
}
