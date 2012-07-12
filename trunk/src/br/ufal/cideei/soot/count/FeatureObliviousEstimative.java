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

//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.util.count.AbstractMetricsSink;

public class FeatureObliviousEstimative extends BodyTransformer {

	protected static final String CLASS_PROPERTY = "class";
	protected static final String CONFIGURATIONS_SIZE = "configurations";
	protected static final String NUMBER_OF_UNITS = "units";
	protected static final String REACHING_DEFINITIONS = "rd";
	protected static final String UNINITIALIZED_VARIABLES = "uv";
	protected static final String JIMPLIFICATION = "jimplification";
	protected static final String PREPROCESSING = "preprocessing";
	

	protected AbstractMetricsSink sink;

	public FeatureObliviousEstimative(AbstractMetricsSink sink) {
		this.sink = sink;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
		int noOfConfigurations;
		// #ifdef LAZY
		noOfConfigurations = configTag.getConfigReps().iterator().next().size();
		// #else
//@		noOfConfigurations = configTag.size();
		// #endif

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");

		long rdAnalysisTime = profilingTag.getRdAnalysisTime();
		long uvAnalysisTime = profilingTag.getUvAnalysisTime();
		long jimplificationTime = profilingTag.getJimplificationTime();
		
		if (noOfConfigurations < 1)
			throw new IllegalStateException("#configurations < 1 for method " + body.getMethod().getName());

		sink.flow(body, PREPROCESSING, profilingTag.getPreprocessingTime());
		sink.flow(body, FeatureObliviousEstimative.CLASS_PROPERTY, body.getMethod().getDeclaringClass().getName());
		sink.flow(body, FeatureObliviousEstimative.CONFIGURATIONS_SIZE, noOfConfigurations);
		sink.flow(body, FeatureObliviousEstimative.NUMBER_OF_UNITS, body.getUnits().size());
		sink.flow(body, FeatureObliviousEstimative.REACHING_DEFINITIONS, rdAnalysisTime * noOfConfigurations);
		sink.flow(body, FeatureObliviousEstimative.UNINITIALIZED_VARIABLES, uvAnalysisTime * noOfConfigurations);
		sink.flow(body, FeatureObliviousEstimative.JIMPLIFICATION, noOfConfigurations * jimplificationTime);
	}
}
// #endif
