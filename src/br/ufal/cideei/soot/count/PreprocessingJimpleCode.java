//#ifdef METRICS
package br.ufal.cideei.soot.count;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class PreprocessingJimpleCode extends BodyTransformer {

	private static PreprocessingJimpleCode instance = null;

	private long preprocessingTotal = 0;
	
	public static PreprocessingJimpleCode v() {
		if (instance == null)
			instance = new PreprocessingJimpleCode();
		return instance;
	}
	
	private PreprocessingJimpleCode() {
	}
	
	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		FeatureTag featureTag = (FeatureTag) body.getTag("FeatureTag");
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");

		if (featureTag.size() == 1) {
			profilingTag.setPreprocessingTime(0);
			return;
		}

		Random random = new Random();
		int nextInt = random.nextInt(featureTag.size());
		Object[] featuresArray = featureTag.getFeatures().toArray();
		Set<Set<String>> raffledConfiguration = (Set<Set<String>>) featuresArray[nextInt];
		
		long startPreprocessing = System.nanoTime();
		
		JimpleBody newBody = Jimple.v().newBody();
		PatchingChain<Unit> newUnits = newBody.getUnits(); 
		
		PatchingChain<Unit> units = body.getUnits();
		for (Unit unit : units) {
			FeatureTag unitTag = (FeatureTag) unit.getTag("FeatureTag");
			if (unitTag.belongsToConfiguration(raffledConfiguration)) {
				newUnits.add((Unit) unit.clone());
			}
		}
		
		long endPreprocessing = System.nanoTime();
		
		profilingTag.setPreprocessingTime(endPreprocessing - startPreprocessing);
	}

}
//#endif