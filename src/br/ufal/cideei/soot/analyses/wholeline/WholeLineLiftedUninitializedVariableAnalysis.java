package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;

//#ifdef METRICS
import profiling.ProfilingTag;

//#endif

import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import br.ufal.cideei.soot.analyses.FlowSetUtils;
import br.ufal.cideei.soot.analyses.uninitvars.LiftedUninitializedVariableAnalysis;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.util.count.AbstractMetricsSink;

public class WholeLineLiftedUninitializedVariableAnalysis extends BodyTransformer {

	private static WholeLineLiftedUninitializedVariableAnalysis instance = new WholeLineLiftedUninitializedVariableAnalysis();

	private WholeLineLiftedUninitializedVariableAnalysis() {
	}

	public static WholeLineLiftedUninitializedVariableAnalysis v() {
		return instance;
	}

	// #ifdef METRICS
	private static final String UV_LIFTED_FLOWTHROUGH_COUNTER = "UV A3 flowthrough";
	private static final String UV_LIFTED_FLOWSET_MEM = "UV A3 mem";
	private static final String UV_LIFTED_FLOWTHROUGH_TIME = "UV A3 flowthrough time";
	private AbstractMetricsSink sink;

	public WholeLineLiftedUninitializedVariableAnalysis setMetricsSink(AbstractMetricsSink sink) {
		this.sink = sink;
		return this;
	}

	// #endif

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		UnitGraph bodyGraph = new BriefUnitGraph(body);
		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);

		// #ifdef METRICS
		long startAnalysis = System.nanoTime();
		// #endif

		LiftedUninitializedVariableAnalysis liftedUninitializedVariableAnalysis = new LiftedUninitializedVariableAnalysis(bodyGraph, configTag.getConfigReps());

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();

		this.sink.flow(body, UV_LIFTED_FLOWTHROUGH_TIME, liftedUninitializedVariableAnalysis.getFlowThroughTime());
		this.sink.flow(body, UV_LIFTED_FLOWSET_MEM, FlowSetUtils.liftedMemoryUnits(body, liftedUninitializedVariableAnalysis, false, 1));
		this.sink.flow(body, UV_LIFTED_FLOWTHROUGH_COUNTER, LiftedUninitializedVariableAnalysis.getFlowThroughCounter());
		LiftedUninitializedVariableAnalysis.reset();

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setUvAnalysisTime2(endAnalysis - startAnalysis);
		// #endif
	}

}
