package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Map;


//#ifdef METRICS

//#ifdef OVERSEER
//@import ch.usi.overseer.OverHpc;
//@
//#endif
import profiling.ProfilingTag;
import br.ufal.cideei.util.count.AbstractMetricsSink;

//#endif

//#ifdef LAZY
//@import br.ufal.cideei.soot.analyses.reachingdefs.LazyLiftedReachingDefinitions;
//@
//#endif

import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import br.ufal.cideei.soot.analyses.FlowSetUtils;
import br.ufal.cideei.soot.analyses.reachingdefs.LiftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.reachingdefs.SimpleReachingDefinitions;

import br.ufal.cideei.soot.instrument.ConfigTag;

//TODO: can this class structure could be replaced by an abstract factory? 
public class WholeLineLiftedReachingDefinitions extends BodyTransformer {

	public WholeLineLiftedReachingDefinitions() {
	}

	// #ifdef METRICS
	private static final String RD_LIFTED_FLOWTHROUGH_COUNTER = "RD A3 flowthrough";
	private static final String RD_LIFTED_FLOWSET_MEM = "RD A3 mem";
	private static final String RD_LIFTED_FLOWTHROUGH_TIME = "RD A3 flowthrough time";
	private static final String RD_LIFTED_L1_FLOWTHROUGH_COUNTER = "RD A3 L1 flowthrough counter";
	
	//#ifdef OVERSEER
//@	private static final String RD_LIFTED_CACHE_MISSES = "RD A3 cache misses";
//@	static OverHpc ohpc=OverHpc.getInstance();
//@	
	//#endif
	
	private AbstractMetricsSink sink;

	public WholeLineLiftedReachingDefinitions setMetricsSink(AbstractMetricsSink sink) {
		this.sink = sink;
		return this;
	}

	// #endif

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		UnitGraph bodyGraph = new BriefUnitGraph(body);
		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);

		boolean wentHybrid = false;
		LiftedReachingDefinitions liftedReachingDefinitions = null;
		SimpleReachingDefinitions simpleReachingDefinitions = null;

		// #ifdef METRICS
		
		//#ifdef OVERSEER
//@		ohpc.initEvents("PERF_COUNT_HW_CACHE_MISSES");
//@		int threadId = ohpc.getThreadId();
//@		ohpc.bindEventsToThread(threadId);
//@		ohpc.stop();
//@		ohpc.start();
//@		
		//#endif
		
		long startAnalysis = System.nanoTime();

		// #endif

		// #ifdef HYBRID
//@		if (configTag.size() == 1) {
//@			wentHybrid = true;
//@			simpleReachingDefinitions = new SimpleReachingDefinitions(bodyGraph);
//@		} else {
			// #endif
			liftedReachingDefinitions = new LiftedReachingDefinitions(bodyGraph, configTag.getConfigReps());
			liftedReachingDefinitions.execute();
			// #ifdef HYBRID
//@		}
//@
		// #endif

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();
		
		//#ifdef OVERSEER
//@		long cacheMissesFromThread = ohpc.getEventFromThread(threadId, 0);
//@		ohpc.stop();
//@		
		//#endif
		
		if (!wentHybrid) {
			this.sink.flow(body, RD_LIFTED_FLOWTHROUGH_TIME, liftedReachingDefinitions.getFlowThroughTime());
			this.sink.flow(body, RD_LIFTED_FLOWSET_MEM, FlowSetUtils.liftedMemoryUnits(body, liftedReachingDefinitions, false, 1));
			this.sink.flow(body, RD_LIFTED_FLOWTHROUGH_COUNTER, LiftedReachingDefinitions.getFlowThroughCounter());
			this.sink.flow(body, RD_LIFTED_L1_FLOWTHROUGH_COUNTER, liftedReachingDefinitions.getL1flowThroughCounter());
			//#ifdef OVERSEER
//@			this.sink.flow(body, RD_LIFTED_CACHE_MISSES, cacheMissesFromThread);
//@			
			//#endif
		}

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setRdAnalysisTime2(endAnalysis - startAnalysis);
		// #endif
	}
}
