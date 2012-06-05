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

//#ifdef LAZY
package br.ufal.cideei.soot.analyses.wholeline;

import java.io.File;
import java.util.Map;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import br.ufal.cideei.soot.analyses.FlowSetUtils;
import br.ufal.cideei.soot.analyses.reachingdefs.ReversedLazyLiftedReachingDefinitions;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.util.count.AbstractMetricsSink;

//XXX: can this class structure could be replaced by an abstract factory?
public class WholeLineReversedLazyReachingDefinitions extends BodyTransformer {

	private static WholeLineReversedLazyReachingDefinitions instance = new WholeLineReversedLazyReachingDefinitions();

	private WholeLineReversedLazyReachingDefinitions() {
	}

	public static WholeLineReversedLazyReachingDefinitions v() {
		return instance;
	}

	// #ifdef METRICS
	private static final String RD_REVERSED_LAZY_FLOWTHROUGH_COUNTER = "RD REVERSED LAZY flowthrough";
	private static final String RD_REVERSED_LAZY_SHARING_DEGREE = "RD REVERSED LAZY sharing drg";
	private static final String RD_REVERSED_LAZY_MEM = "RD REVERSED LAZY mem";
	private static final String RD_REVERSED_LAZY_FLOWTHROUGH_TIME = "RD REVERSED LAZY flowthrough time";
	private AbstractMetricsSink sink;

	public WholeLineReversedLazyReachingDefinitions setMetricsSink(AbstractMetricsSink sink) {
		this.sink = sink;
		return this;
	}

	// #endif

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		UnitGraph bodyGraph = new ExceptionalUnitGraph(body);
		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
		int size = configTag.getConfigReps().iterator().next().size();
		boolean wentHybrid = false;

		ReversedLazyLiftedReachingDefinitions reversedLazyReachingDefinitions = null;

		// #ifdef METRICS
		long startAnalysis = System.nanoTime();
		// #endif

		// #ifdef HYBRID
//@		if (size == 1) {
//@			wentHybrid = true;
//@			 SimpleReachingDefinitions simpleReachingDefinitions = new SimpleReachingDefinitions(bodyGraph);
//@		} else {
			// #endif
			reversedLazyReachingDefinitions = new ReversedLazyLiftedReachingDefinitions(bodyGraph, (ILazyConfigRep) configTag.getConfigReps().iterator().next());
			reversedLazyReachingDefinitions.execute();
			// #ifdef HYBRID
//@		}
		// #endif

		// #ifdef METRICS
			
		long endAnalysis = System.nanoTime();

		if (!wentHybrid) {
			this.sink.flow(body, RD_REVERSED_LAZY_FLOWTHROUGH_TIME, reversedLazyReachingDefinitions.getFlowThroughTime());
//			this.sink.flow(body, RD_REVERSED_LAZY_MEM, FlowSetUtils.lazyMemoryUnits(body, lazyReachingDefinitions, true, 1, configTag.getConfigReps().iterator().next().size()));
			this.sink.flow(body, RD_REVERSED_LAZY_SHARING_DEGREE, FlowSetUtils.averageSharingDegree(body, reversedLazyReachingDefinitions));
			this.sink.flow(body, RD_REVERSED_LAZY_FLOWTHROUGH_COUNTER, ReversedLazyLiftedReachingDefinitions.getFlowThroughCounter());
			ReversedLazyLiftedReachingDefinitions.reset();
		}

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setRdAnalysisTime2(endAnalysis - startAnalysis);
		// #endif
	}
}

// #endif
