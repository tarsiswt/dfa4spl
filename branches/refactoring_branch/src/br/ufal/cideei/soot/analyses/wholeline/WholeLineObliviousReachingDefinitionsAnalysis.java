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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//#ifdef METRICS
import profiling.ProfilingTag;
import br.ufal.cideei.util.count.MetricsSink;

//#endif

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.Transformer;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import br.ufal.cideei.soot.analyses.reachingdefs.SimpleReachingDefinitions;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.soot.instrument.IConfigRep;

public class WholeLineObliviousReachingDefinitionsAnalysis extends BodyTransformer {

	private static WholeLineObliviousReachingDefinitionsAnalysis instance = new WholeLineObliviousReachingDefinitionsAnalysis();
	private MetricsSink sink;

	private WholeLineObliviousReachingDefinitionsAnalysis() {
	}

	public static WholeLineObliviousReachingDefinitionsAnalysis v() {
		return instance;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setPreprocessingTime(0);

		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
		// #ifdef LAZY
//@		if (true)
//@			throw new IllegalStateException("Feature oblivious analysis in lazy mode not supported");
		// #endif

		long totalAnalysis = 0;

		if (configTag.size() > 1) {
			int maximumBodySize = 0;
			Body maximumBody = null;
			int minimalBodySize = 0;
			Body minimalBody = null;
			
			Set<IConfigRep> configs = configTag.getConfigReps();
			for (IConfigRep config : configs) {

				JimpleBody newBody = Jimple.v().newBody(body.getMethod());
				newBody.importBodyContentsFrom(body);

				PatchingChain<Unit> newBodyUnits = newBody.getUnits();
				Iterator<Unit> snapshotIterator = newBodyUnits.snapshotIterator();

				while (snapshotIterator.hasNext()) {
					Unit unit = (Unit) snapshotIterator.next();
					FeatureTag unitFeatureTag = (FeatureTag) unit.getTag("FeatureTag");
					if (!unitFeatureTag.getFeatureRep().belongsToConfiguration(config)) {
						newBodyUnits.remove(unit);
					}
				}

				/*
				 * If the body size is 0, then cannot continue. Store maximum and minimal size.
				 */
				int newBodySize = newBodyUnits.size();

				if (newBodySize > maximumBodySize) {
					maximumBodySize = newBodySize;
					maximumBody = newBody;
				}
				if (newBodySize < minimalBodySize && newBodySize > 0) {
					minimalBodySize = newBodySize;
					minimalBody = newBody;
				}
			}
			
			if (minimalBody == null) {
				minimalBody = body;
				minimalBodySize = body.getUnits().size();
			}
			
			// #ifdef METRICS
			long maximumAnalysis;
			{
				UnitGraph maximumBodyGraph = new BriefUnitGraph(maximumBody);
				long startAnalysis = System.nanoTime();
				new SimpleReachingDefinitions(maximumBodyGraph);
				long endAnalysis = System.nanoTime();
				maximumAnalysis = (endAnalysis - startAnalysis);
			}
			
			long minimumAnalysis;
			{
				UnitGraph minimalBodyGraph = new BriefUnitGraph(minimalBody);
				long startAnalysis = System.nanoTime();
				new SimpleReachingDefinitions(minimalBodyGraph);
				long endAnalysis = System.nanoTime();
				minimumAnalysis = (endAnalysis - startAnalysis);
			}
			
			totalAnalysis = (maximumAnalysis + minimumAnalysis) / 2;
			
			double minimalProportionalJimplificationTime = (minimalBodySize * profilingTag.getJimplificationTime()) / maximumBodySize;
			profilingTag.setJimplificationTime((profilingTag.getJimplificationTime() + Math.round(minimalProportionalJimplificationTime)) / 2);
			// #endif
		} else {
			UnitGraph bodyGraph = new BriefUnitGraph(body);
			// #ifdef METRICS
			long startAnalysis = System.nanoTime();
			// #endif
			SimpleReachingDefinitions simpleReachingDefinitions = new SimpleReachingDefinitions(bodyGraph);
			// #ifdef METRICS
			long endAnalysis = System.nanoTime();
			totalAnalysis = endAnalysis - startAnalysis;
			
			this.sink.flow(body, "RD A1 flowthrough time", simpleReachingDefinitions.getFlowThroughTime());
			this.sink.flow(body, "RD A1 flowthrough", simpleReachingDefinitions.getFlowThroughCounter());
			// #endif
		}

		profilingTag.setRdAnalysisTime(totalAnalysis);
	}

	public Transformer setMetricsSink(MetricsSink sink) {
		this.sink = sink;
		return this;
	}
}
