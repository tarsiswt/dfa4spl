package br.ufal.cideei.soot.analyses.wholeline;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.toolkits.graph.BriefUnitGraph;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.util.WriterFacadeForAnalysingMM;

public class WholeLineRunnerUninitializedVariable extends BodyTransformer {
	

	private static WholeLineRunnerUninitializedVariable instance = new WholeLineRunnerUninitializedVariable();
	private long analysisTime = 0;

	private WholeLineRunnerUninitializedVariable() {
	}
	
	public static WholeLineRunnerUninitializedVariable v() {
		return instance;
	}
	
	public long getAnalysesTime() {
		long tmp = analysisTime;
		return tmp;
	}
	
	public void reset() {
		analysisTime = 0;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		BriefUnitGraph bodyGraph = new BriefUnitGraph(body);
		FeatureTag<Set<String>> featureTag = (FeatureTag<Set<String>>) body.getTag("FeatureTag");
//
//		FeatureSensitiveAnalysisRunner runner = new FeatureSensitiveAnalysisRunner(bodyGraph, featureTag.getFeatures(),
//				FeatureSensitiveUninitializedVariablesFactory.getInstance(), new HashMap<Object, Object>());
		try {
			//#ifdef METRICS
			long beforeRunner = System.nanoTime();
			//#endif
//			runner.execute2();
			//#ifdef METRICS
			long afterRunner = System.nanoTime();
			long delta = afterRunner - beforeRunner;
			this.analysisTime += delta;

			try {
				WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.UV_RUNNER_COLUMN, Double.toString(((double)delta)/1000000));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			System.out.println("[Runner]" + body.getMethod() + " with " + body.getTag("FeatureTag") + " took " + ((double)(afterRunner - beforeRunner)/1000000));
			//#endif
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
