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

import java.util.Map;

//#ifdef METRICS
import profiling.ProfilingTag;
import br.ufal.cideei.util.count.AbstractMetricsSink;

//#endif

import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import br.ufal.cideei.soot.analyses.FlowSetUtils;
import br.ufal.cideei.soot.analyses.uninitvars.LiftedUninitializedVariableAnalysis;
import br.ufal.cideei.soot.analyses.uninitvars.SimpleUninitializedVariableAnalysis;
import br.ufal.cideei.soot.instrument.ConfigTag;

public class WholeLineLiftedUninitializedVariableAnalysis extends BodyTransformer {

	// #ifdef METRICS
	private static final String UV_LIFTED_FLOWTHROUGH_COUNTER = "UV A3 flowthrough";
	private static final String UV_LIFTED_FLOWSET_MEM = "UV A3 mem";
	private static final String UV_LIFTED_FLOWTHROUGH_TIME = "UV A3 flowthrough time";
	private static final String UV_LIFTED_L1_FLOWTHROUGH_COUNTER = "UV A3 L1 flowthrough counter";
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
		boolean wentHybrid = false;
		LiftedUninitializedVariableAnalysis liftedUninitializedVariableAnalysis = null;

		// #ifdef METRICS
		long startAnalysis = System.nanoTime();
		// #endif

		// #ifdef HYBRID
//@		if (configTag.size() == 1) {
//@			wentHybrid = true;
//@			SimpleUninitializedVariableAnalysis simpleUninitializedVariables = new SimpleUninitializedVariableAnalysis(bodyGraph);
//@		} else {
			// #endif
			liftedUninitializedVariableAnalysis = new LiftedUninitializedVariableAnalysis(bodyGraph, configTag.getConfigReps());
			// #ifdef HYBRID
//@		}
		// #endif

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();

		if (!wentHybrid) {
			this.sink.flow(body, UV_LIFTED_FLOWTHROUGH_TIME, liftedUninitializedVariableAnalysis.getFlowThroughTime());
			this.sink.flow(body, UV_LIFTED_FLOWSET_MEM, FlowSetUtils.liftedMemoryUnits(body, liftedUninitializedVariableAnalysis, false, 1));
			this.sink.flow(body, UV_LIFTED_FLOWTHROUGH_COUNTER, LiftedUninitializedVariableAnalysis.getFlowThroughCounter());
			this.sink.flow(body, UV_LIFTED_L1_FLOWTHROUGH_COUNTER, liftedUninitializedVariableAnalysis.getL1flowThroughCounter());
			LiftedUninitializedVariableAnalysis.reset();
		}

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setUvAnalysisTime2(endAnalysis - startAnalysis);
		// #endif
	}

}
