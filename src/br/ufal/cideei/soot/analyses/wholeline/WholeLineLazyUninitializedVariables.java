//#ifdef LAZY
package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import br.ufal.cideei.soot.analyses.FlowSetUtils;
import br.ufal.cideei.soot.analyses.uninitvars.LazyLiftedInitializedVariablesAnalysis;
import br.ufal.cideei.soot.analyses.uninitvars.LazyLiftedUninitializedVariableAnalysis;
import br.ufal.cideei.soot.analyses.uninitvars.SimpleUninitializedVariableAnalysis;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.util.count.AbstractMetricsSink;

//TODO: can this class structure could be replaced by an abstract factory?
public class WholeLineLazyUninitializedVariables extends BodyTransformer {

	private static WholeLineLazyUninitializedVariables instance = new WholeLineLazyUninitializedVariables();

	private WholeLineLazyUninitializedVariables() {
	}

	public static WholeLineLazyUninitializedVariables v() {
		return instance;
	}

	// #ifdef METRICS
	private static final String UV_LAZY_FLOWTHROUGH_COUNTER = "UV LAZY flowthrough";
	private static final String UV_LAZY_SHARING_DEGREE = "UV LAZY sharing drg";
	private static final String UV_LAZY_MEM = "UV LAZY mem";
	private static final String UV_LAZY_FLOWTHROUGH_TIME = "UV LAZY flowthrough time";
	private AbstractMetricsSink sink;

	public WholeLineLazyUninitializedVariables setMetricsSink(AbstractMetricsSink sink) {
		this.sink = sink;
		return this;
	}

	// #endif

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		UnitGraph bodyGraph = new ExceptionalUnitGraph(body);
		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
		int size = configTag.getConfigReps().iterator().next().size();

		LazyLiftedUninitializedVariableAnalysis lazyUninitializedVariables = null;
		boolean wentHybrid = false;

		// #ifdef METRICS
		long startAnalysis = System.nanoTime();
		// #endif

		// #ifdef HYBRID
		// @ if (size == 1) {
		// @ SimpleUninitializedVariableAnalysis uninitializedVariables = new
//@		// SimpleUninitializedVariableAnalysis(bodyGraph);
		// @ wentHybrid = true;
		// @ } else {
		// #endif
		lazyUninitializedVariables = new LazyLiftedUninitializedVariableAnalysis(bodyGraph, (ILazyConfigRep) configTag.getConfigReps().iterator().next());
		// #ifdef HYBRID
		// @ }
		// #endif

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();

		if (!wentHybrid) {
			this.sink.flow(body, UV_LAZY_FLOWTHROUGH_TIME, lazyUninitializedVariables.getFlowThroughTime());
			this.sink.flow(body, UV_LAZY_MEM, FlowSetUtils.lazyMemoryUnits(body, lazyUninitializedVariables, true, 1, size));
			this.sink.flow(body, UV_LAZY_SHARING_DEGREE, FlowSetUtils.averageSharingDegree(body, lazyUninitializedVariables));
			this.sink.flow(body, UV_LAZY_FLOWTHROUGH_COUNTER, LazyLiftedUninitializedVariableAnalysis.getFlowThroughCounter());
		}
		LazyLiftedUninitializedVariableAnalysis.reset();

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setUvAnalysisTime2(endAnalysis - startAnalysis);
		// #endif
	}
}

// #endif
