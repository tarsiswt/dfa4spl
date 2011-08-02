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

		sink.flow(body, PREPROCESSING, profilingTag.getPreprocessingTime());
		sink.flow(body, FeatureObliviousEstimative.CLASS_PROPERTY, body.getMethod().getDeclaringClass().getName());
		sink.flow(body, FeatureObliviousEstimative.CONFIGURATIONS_SIZE, noOfConfigurations);
		sink.flow(body, FeatureObliviousEstimative.NUMBER_OF_UNITS, body.getUnits().size());
		sink.flow(body, FeatureObliviousEstimative.REACHING_DEFINITIONS, rdAnalysisTime);
		sink.flow(body, FeatureObliviousEstimative.UNINITIALIZED_VARIABLES, uvAnalysisTime);
		if (noOfConfigurations > 1) {
			sink.flow(body, FeatureObliviousEstimative.JIMPLIFICATION, noOfConfigurations * jimplificationTime);
		} else {
			sink.flow(body, FeatureObliviousEstimative.JIMPLIFICATION, jimplificationTime);
		}
	}
}
// #endif
