package br.ufal.cideei.soot.count;

import java.util.Map;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class FeatureObliviousEstimative extends BodyTransformer {

	private static FeatureObliviousEstimative instance = null;

	private long rdTotal = 0;
	private long uvTotal = 0;
	private long jimplificationTotal = 0;
	private long preprocessingTotal = 0;
	
	public static FeatureObliviousEstimative v() {
		if (instance == null)
			instance = new FeatureObliviousEstimative();
		return instance;
	}
	
	private FeatureObliviousEstimative() {
	}
	
	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		
		FeatureTag featureTag = (FeatureTag) body.getTag("FeatureTag");
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");

		int size = featureTag.size();
		long rdAnalysisTime     = profilingTag.getRdAnalysisTime();
		long uvAnalysisTime     = profilingTag.getUvAnalysisTime();
		long jimplificationTime = profilingTag.getJimplificationTime();
		long preprocessingTime = profilingTag.getPreprocessingTime();
		
		// if contains color
		if (size > 1) {
			long numberOfConfigurations = (long) (Math.log(size)/Math.log(2));
			
			this.rdTotal += (numberOfConfigurations * rdAnalysisTime);
			this.uvTotal += (numberOfConfigurations * uvAnalysisTime);
			this.jimplificationTotal += (numberOfConfigurations * jimplificationTime);
			this.preprocessingTotal += (numberOfConfigurations * preprocessingTime);
			
		} else {
			this.rdTotal += rdAnalysisTime;
			this.uvTotal += uvAnalysisTime;
			this.jimplificationTotal += jimplificationTime;
			this.preprocessingTotal += preprocessingTime;
		}
	}

	public long getRdTotal() {
		return rdTotal;
	}

	public long getUvTotal() {
		return uvTotal;
	}

	public long getJimplificationTotal() {
		return jimplificationTotal;
	}

	public long getPreprocessingTotal() {
		return preprocessingTotal;
	}

	public void reset() {
		this.rdTotal = 0;
		this.uvTotal = 0;
		this.jimplificationTotal = 0;
		this.preprocessingTotal = 0;
	}

}