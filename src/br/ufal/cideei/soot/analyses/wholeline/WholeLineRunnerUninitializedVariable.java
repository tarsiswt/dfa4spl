package br.ufal.cideei.soot.analyses.wholeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

//#ifdef METRICS
import profiling.ProfilingTag;
import br.ufal.cideei.soot.count.AssignmentsCounter;

//#endif
import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import br.ufal.cideei.soot.analyses.FlowSetUtils;
import br.ufal.cideei.soot.analyses.reachingdefs.UnliftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.uninitvars.UnliftedUnitializedVariablesAnalysis;

import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.util.count.AbstractMetricsSink;

public class WholeLineRunnerUninitializedVariable extends BodyTransformer {

	private static WholeLineRunnerUninitializedVariable instance = new WholeLineRunnerUninitializedVariable();

	private WholeLineRunnerUninitializedVariable() {
	}

	public static WholeLineRunnerUninitializedVariable v() {
		return instance;
	}

	// #ifdef METRICS
	private static final String UV_RUNNER_FLOWTHROUGH_COUNTER = "UV A2 flowthrough";
	private static final String UV_RUNNER_FLOWSET_MEM = "UV A2 mem";
	private static final String UV_RUNNER_FLOWTHROUGH_TIME = "UV A2 flowthrough time";
	private static final String UV_RUNNER_L1_FLOWTHROUGH_COUNTER = "UV A2 L1 flowthough counter";
	private AbstractMetricsSink sink;

	public WholeLineRunnerUninitializedVariable setMetricsSink(AbstractMetricsSink sink) {
		this.sink = sink;
		return this;
	}

	// #endif

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		UnitGraph bodyGraph = new BriefUnitGraph(body);
		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);

		List<Long> memUnits = new ArrayList<Long>();

		// #ifdef METRICS
		long startAnalysis = System.nanoTime();
		long flowThroughTime = 0;
		// #endif

		Set<IConfigRep> configReps = configTag.getConfigReps();
		for (IConfigRep config : configReps) {
			UnliftedUnitializedVariablesAnalysis unliftedUnitializedVariablesAnalysis = new UnliftedUnitializedVariablesAnalysis(bodyGraph, config);

			// #ifdef METRICS
			memUnits.add(FlowSetUtils.unliftedMemoryUnits(body, unliftedUnitializedVariablesAnalysis, 1));
			flowThroughTime += unliftedUnitializedVariablesAnalysis.getFlowThroughTime();
			// #endif
		}

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();

		this.sink.flow(body, UV_RUNNER_FLOWTHROUGH_TIME, flowThroughTime);
		Long max = Collections.max(memUnits);
		this.sink.flow(body, UV_RUNNER_FLOWSET_MEM, max);
		this.sink.flow(body, UV_RUNNER_FLOWTHROUGH_COUNTER, UnliftedUnitializedVariablesAnalysis.getFlowThroughCounter());
		this.sink.flow(body, UV_RUNNER_L1_FLOWTHROUGH_COUNTER, UnliftedUnitializedVariablesAnalysis.getL1flowThroughCounter());
		UnliftedUnitializedVariablesAnalysis.reset();

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setUvAnalysisTime(endAnalysis - startAnalysis);
		// #endif
	}

}
