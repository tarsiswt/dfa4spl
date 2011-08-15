package br.ufal.cideei.soot.analyses.wholeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

//#ifdef METRICS
import profiling.ProfilingTag;
import br.ufal.cideei.util.count.AbstractMetricsSink;

//#endif

import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import br.ufal.cideei.soot.analyses.FlowSetUtils;
import br.ufal.cideei.soot.analyses.reachingdefs.UnliftedReachingDefinitions;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.soot.instrument.IConfigRep;

public class WholeLineRunnerReachingDefinitions extends BodyTransformer {

	private static WholeLineRunnerReachingDefinitions instance = new WholeLineRunnerReachingDefinitions();

	private WholeLineRunnerReachingDefinitions() {
	}

	public static WholeLineRunnerReachingDefinitions v() {
		return instance;
	}

	// #ifdef METRICS
	private static final String RD_RUNNER_FLOWTHROUGH_COUNTER = "RD A2 flowthrough";
	private static final String RD_RUNNER_FLOWSET_MEM = "RD A2 mem";
	private static final String RD_RUNNER_FLOWTHROUGH_TIME = "RD A2 flowthrough time";
	private AbstractMetricsSink sink;

	public WholeLineRunnerReachingDefinitions setMetricsSink(AbstractMetricsSink sink) {
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
			UnliftedReachingDefinitions unliftedReachingDefinitions = new UnliftedReachingDefinitions(bodyGraph, config);

			// #ifdef METRICS
			memUnits.add(FlowSetUtils.unliftedMemoryUnits(body, unliftedReachingDefinitions, 1));
			flowThroughTime += unliftedReachingDefinitions.getFlowThroughTime();

			// #endif
		}

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();

		this.sink.flow(body, RD_RUNNER_FLOWTHROUGH_TIME, flowThroughTime);
		Long max = Collections.max(memUnits);
		this.sink.flow(body, RD_RUNNER_FLOWSET_MEM, max);
		this.sink.flow(body, RD_RUNNER_FLOWTHROUGH_COUNTER, UnliftedReachingDefinitions.getFlowThroughCounter());
		UnliftedReachingDefinitions.reset();

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setRdAnalysisTime(endAnalysis - startAnalysis);
		// #endif
	}
}
