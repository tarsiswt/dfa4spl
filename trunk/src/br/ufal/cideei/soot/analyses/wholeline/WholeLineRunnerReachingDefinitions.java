/*
 * This is a prototype implementation of the concept of Feature-Sen
 * sitive Dataflow Analysis. More details in the AOSD'12 paper:
 * Dataflow Analysis for Software Product Lines
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package br.ufal.cideei.soot.analyses.wholeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


//#ifdef METRICS

//#ifdef OVERSEER
//@import ch.usi.overseer.OverHpc;
//@
//#endif

import br.ufal.cideei.util.count.AbstractMetricsSink;
import profiling.ProfilingTag;

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
	private static final String RD_RUNNER_L1_FLOWTHROUGH_COUNTER = "RD A2 L1 flowthrough counter";
	
	//#ifdef OVERSEER
//@	private static final String RD_RUNNER_CACHE_MISSES = "RD A2 cache misses";
//@	static OverHpc ohpc=OverHpc.getInstance();
//@	
	//#endif
	
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
		
		//#ifdef OVERSEER
//@		ohpc.initEvents("PERF_COUNT_HW_CACHE_MISSES");
//@		int threadId = ohpc.getThreadId();
//@		ohpc.bindEventsToThread(threadId);
//@		ohpc.stop();
//@		ohpc.start();
//@		
		//#endif
		
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

		//#ifdef OVERSEER
//@		long cacheMissesFromThread = ohpc.getEventFromThread(threadId, 0);
//@		ohpc.stop();
//@		
		//#endif
		
		this.sink.flow(body, RD_RUNNER_FLOWTHROUGH_TIME, flowThroughTime);
		Long max = Collections.max(memUnits);
		this.sink.flow(body, RD_RUNNER_FLOWSET_MEM, max);
		this.sink.flow(body, RD_RUNNER_FLOWTHROUGH_COUNTER, UnliftedReachingDefinitions.getFlowThroughCounter());
		this.sink.flow(body, RD_RUNNER_L1_FLOWTHROUGH_COUNTER, UnliftedReachingDefinitions.getL1flowThroughCounter());
		
		//#ifdef OVERSEER
//@		this.sink.flow(body, RD_RUNNER_CACHE_MISSES, cacheMissesFromThread);
//@		
		//#endif
		UnliftedReachingDefinitions.reset();

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setRdAnalysisTime(endAnalysis - startAnalysis);
		// #endif
	}
}
