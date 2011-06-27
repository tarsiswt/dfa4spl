//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;

import profiling.ProfilingTag;

import soot.Body;
import br.ufal.cideei.util.count.AbstractMetricsSink;

public class FeatureSensitiveEstimative extends FeatureObliviousEstimative {

	protected static final String REACHING_DEFINITIONS = "rd (a2)";
	protected static final String UNINITIALIZED_VARIABLES = "uv (a2)";
	protected static final String REACHING_DEFINITIONS_SIMULATENEOUS = "rd (a3)";
	protected static final String UNINITIALIZED_VARIABLES_SIMULATENEOUS = "uv (a3)";
	protected static final String PREPROCESSING = "preprocessing";

	public FeatureSensitiveEstimative(AbstractMetricsSink sink) {
		super(sink);
	}

	@Override
	protected void internalTransform(Body body, String phase, Map map) {
		super.internalTransform(body, phase, map);
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		sink.flow(body, REACHING_DEFINITIONS_SIMULATENEOUS, Long.toString(profilingTag.getRdAnalysisTime2()));
		sink.flow(body, UNINITIALIZED_VARIABLES_SIMULATENEOUS, Long.toString(profilingTag.getUvAnalysisTime2()));
		sink.flow(body, PREPROCESSING, Long.toString(profilingTag.getPreprocessingTime()));
	}

}
// #endif
