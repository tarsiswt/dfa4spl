//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.util.count.AbstractMetricsSink;

public class FeatureObliviousEstimative extends BodyTransformer {

	protected static final String CLASS_PROPERTY = "class";
	protected static final String CONFIGURATIONS_SIZE = "configurations";
	protected static final String NUMBER_OF_UNITS = "units";
	protected static final String REACHING_DEFINITIONS = "rd";
	protected static final String UNINITIALIZED_VARIABLES = "uv";
	protected static final String JIMPLIFICATION = "jimplification";

	protected AbstractMetricsSink sink;

	public FeatureObliviousEstimative(AbstractMetricsSink sink) {
		this.sink = sink;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		FeatureTag featureTag = (FeatureTag) body.getTag("FeatureTag");
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");

		int featureTagSize = featureTag.size();
		long rdAnalysisTime = profilingTag.getRdAnalysisTime();
		long uvAnalysisTime = profilingTag.getUvAnalysisTime();
		long jimplificationTime = profilingTag.getJimplificationTime();

		sink.flow(body, FeatureObliviousEstimative.CLASS_PROPERTY, body.getMethod().getDeclaringClass().getName());
		sink.flow(body, FeatureObliviousEstimative.CONFIGURATIONS_SIZE, featureTagSize);
		sink.flow(body, FeatureObliviousEstimative.NUMBER_OF_UNITS, body.getUnits().size());
		sink.flow(body, FeatureObliviousEstimative.REACHING_DEFINITIONS, rdAnalysisTime);
		sink.flow(body, FeatureObliviousEstimative.UNINITIALIZED_VARIABLES, uvAnalysisTime);
		if (featureTagSize > 1) {
			sink.flow(body, FeatureObliviousEstimative.JIMPLIFICATION, featureTagSize * jimplificationTime);
		} else {
			sink.flow(body, FeatureObliviousEstimative.JIMPLIFICATION, jimplificationTime);
		}
	}
}
// #endif