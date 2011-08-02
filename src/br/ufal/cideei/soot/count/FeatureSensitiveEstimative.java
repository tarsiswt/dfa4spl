//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;

import profiling.ProfilingTag;

import soot.Body;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.util.count.AbstractMetricsSink;

public class FeatureSensitiveEstimative extends FeatureObliviousEstimative {

	protected static final String REACHING_DEFINITIONS = "rd (a2)";
	protected static final String UNINITIALIZED_VARIABLES = "uv (a2)";
	protected static final String REACHING_DEFINITIONS_SIMULTANEOUS = "rd (a3)";
	protected static final String UNINITIALIZED_VARIABLES_SIMULTANEOUS = "uv (a3)";
	

	public FeatureSensitiveEstimative(AbstractMetricsSink sink) {
		super(sink);
	}

	@Override
	protected void internalTransform(Body body, String phase, Map map) {
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

		sink.flow(body, PREPROCESSING, profilingTag.getPreprocessingTime());
		sink.flow(body, FeatureObliviousEstimative.CLASS_PROPERTY, body.getMethod().getDeclaringClass().getName());
		sink.flow(body, FeatureObliviousEstimative.CONFIGURATIONS_SIZE, noOfConfigurations);
		sink.flow(body, FeatureObliviousEstimative.NUMBER_OF_UNITS, body.getUnits().size());
		sink.flow(body, FeatureObliviousEstimative.REACHING_DEFINITIONS, rdAnalysisTime);
		sink.flow(body, FeatureObliviousEstimative.UNINITIALIZED_VARIABLES, uvAnalysisTime);
		sink.flow(body, FeatureObliviousEstimative.JIMPLIFICATION, jimplificationTime);

		sink.flow(body, REACHING_DEFINITIONS_SIMULTANEOUS, profilingTag.getRdAnalysisTime2());
		sink.flow(body, UNINITIALIZED_VARIABLES_SIMULTANEOUS, profilingTag.getUvAnalysisTime2());
	}

}
// #endif
