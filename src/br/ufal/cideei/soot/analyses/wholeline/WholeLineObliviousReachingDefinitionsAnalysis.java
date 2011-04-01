package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import profiling.ProfilingTag;
import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.tagkit.Tag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.analyses.reachingdefs.SimpleReachedDefinitionsAnalysis;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class WholeLineObliviousReachingDefinitionsAnalysis extends
		BodyTransformer {

	private static WholeLineObliviousReachingDefinitionsAnalysis instance = new WholeLineObliviousReachingDefinitionsAnalysis();

	private WholeLineObliviousReachingDefinitionsAnalysis() {
	}

	public static WholeLineObliviousReachingDefinitionsAnalysis v() {
		return instance;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {

		// #ifdef METRICS
		long startAnalysis = System.nanoTime();
		// #endif

		FeatureTag featureTag = (FeatureTag) body.getTag("FeatureTag");
		if (featureTag.size() > 1) {
			Collection configs = featureTag.getFeatures();
			for (Object object : configs) {
				Set<String> config = (Set<String>) object;
				JimpleBody newBody = Jimple.v().newBody(body.getMethod());
				newBody.importBodyContentsFrom(body);

				PatchingChain<Unit> newBodyUnits = newBody.getUnits();
				Iterator<Unit> snapshotIterator = newBodyUnits
						.snapshotIterator();

				while (snapshotIterator.hasNext()) {
					Unit unit = (Unit) snapshotIterator.next();
					FeatureTag unitFeatureTag = (FeatureTag) unit
							.getTag("FeatureTag");
					if (!unitFeatureTag.belongsToConfiguration(config)) {
						newBodyUnits.remove(unit);
					}
				}

				if (newBodyUnits.size() == 0){
					continue;
				}
				BriefUnitGraph newBodyGraph = new BriefUnitGraph(newBody);
				new SimpleReachedDefinitionsAnalysis(newBodyGraph);

			}
		} else {
			BriefUnitGraph bodyGraph = new BriefUnitGraph(body);
			new SimpleReachedDefinitionsAnalysis(bodyGraph);
		}

		// #ifdef METRICS
		long endAnalysis = System.nanoTime();

		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setRdAnalysisTime(endAnalysis - startAnalysis);
		// #endif
	}

}