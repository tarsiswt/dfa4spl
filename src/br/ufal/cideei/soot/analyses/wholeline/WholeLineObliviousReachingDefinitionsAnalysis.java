package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.analyses.reachingdefs.SimpleReachedDefinitionsAnalysis;

public class WholeLineObliviousReachingDefinitionsAnalysis extends BodyTransformer {

	private static WholeLineObliviousReachingDefinitionsAnalysis instance = new WholeLineObliviousReachingDefinitionsAnalysis();

	private WholeLineObliviousReachingDefinitionsAnalysis() {
	}

	public static WholeLineObliviousReachingDefinitionsAnalysis v() {
		return instance;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		DirectedGraph<Unit> bodyGraph = new BriefUnitGraph(body);

		// #ifdef METRICS
		long startAnalysis = System.nanoTime();
		// #endif
		
		new SimpleReachedDefinitionsAnalysis(bodyGraph);
		
		// #ifdef METRICS
		long endAnalysis = System.nanoTime();
		
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setRdAnalysisTime(endAnalysis - startAnalysis);
		// #endif
	}

}