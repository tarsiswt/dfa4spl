package br.ufal.cideei.soot.count;

import java.util.Map;

import profiling.ProfilingTag;

import soot.Body;
import soot.BodyTransformer;

public class FeatureSensitiveEstimative extends BodyTransformer {

	private static FeatureSensitiveEstimative instance = null;

	private long rdTotal = 0;
	private long uvTotal = 0;
	private long rdTotal2 = 0;
	private long uvTotal2 = 0;
	private long jimplificationTotal = 0;
	
	public static FeatureSensitiveEstimative v() {
		if (instance == null)
			instance = new FeatureSensitiveEstimative();
		return instance;
	}
	
	private FeatureSensitiveEstimative() {
	}

	@Override
	protected void internalTransform(Body body, String phase, Map map) {
		
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		
		long rdAnalysisTime     = profilingTag.getRdAnalysisTime();
		long uvAnalysisTime     = profilingTag.getUvAnalysisTime();
		long rdAnalysisTime2    = profilingTag.getRdAnalysisTime2();
		long uvAnalysisTime2    = profilingTag.getUvAnalysisTime2();
		long jimplificationTime = profilingTag.getJimplificationTime();
		
		this.rdTotal += rdAnalysisTime;
		this.uvTotal += uvAnalysisTime;
		this.rdTotal2 += rdAnalysisTime2;
		this.uvTotal2 += uvAnalysisTime2;
		this.jimplificationTotal += jimplificationTime;
	}

	public long getRdTotal() {
		return rdTotal;
	}
	
	public long getUvTotal() {
		return uvTotal;
	}
	
	public long getRdTotal2() {
		return rdTotal2;
	}
	
	public long getUvTotal2() {
		return uvTotal2;
	}
	
	public long getJimplificationTotal() {
		return jimplificationTotal;
	}

	public void reset() {
		this.rdTotal = 0;
		this.uvTotal = 0;
		this.rdTotal2 = 0;
		this.uvTotal2 = 0;
		this.jimplificationTotal = 0;
	}

}