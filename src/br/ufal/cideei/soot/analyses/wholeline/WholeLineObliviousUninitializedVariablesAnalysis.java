package br.ufal.cideei.soot.analyses.wholeline;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//#ifdef METRICS
import profiling.ProfilingTag;
//#endif
import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.Transformer;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import br.ufal.cideei.soot.analyses.uninitvars.SimpleUninitializedVariableAnalysis;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.soot.instrument.FeatureTag;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.util.count.MetricsSink;

public class WholeLineObliviousUninitializedVariablesAnalysis extends BodyTransformer {

	private static WholeLineObliviousUninitializedVariablesAnalysis instance = new WholeLineObliviousUninitializedVariablesAnalysis();
	private MetricsSink sink;

	private WholeLineObliviousUninitializedVariablesAnalysis() {
	}

	public static WholeLineObliviousUninitializedVariablesAnalysis v() {
		return instance;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		long totalAnalysis = 0;

		long startAnalysis = 0;
		long endAnalysis = 0;

		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);

		int maximumBodySize = 0;
		int minimalBodySize = 0;

		SimpleUninitializedVariableAnalysis analysis = null; 

		if (configTag.size() > 1) {
			Set<IConfigRep> configs = configTag.getConfigReps();
			for (IConfigRep config : configs) {

				JimpleBody newBody = Jimple.v().newBody(body.getMethod());
				newBody.importBodyContentsFrom(body);

				PatchingChain<Unit> newBodyUnits = newBody.getUnits();
				Iterator<Unit> snapshotIterator = newBodyUnits.snapshotIterator();

				while (snapshotIterator.hasNext()) {
					Unit unit = (Unit) snapshotIterator.next();
					FeatureTag unitFeatureTag = (FeatureTag) unit.getTag("FeatureTag");
					if (!unitFeatureTag.getFeatureRep().belongsToConfiguration(config)) {
						newBodyUnits.remove(unit);
					}
				}

				/*
				 * If the body size is 0, then cannot continue. Store maximum
				 * and minimal size.
				 */
				int newBodySize = newBodyUnits.size();
				if (newBodySize == 0) {
					continue;
				} else {
					if (newBodySize > maximumBodySize) {
						maximumBodySize = newBodySize;
					}
					if (newBodySize < minimalBodySize) {
						minimalBodySize = newBodySize;
					}
				}

				UnitGraph newBodyGraph = new BriefUnitGraph(body);

				// #ifdef METRICS
				startAnalysis = System.nanoTime();
				// #endif
				analysis = new SimpleUninitializedVariableAnalysis(newBodyGraph);
				// #ifdef METRICS
				endAnalysis = System.nanoTime();
				totalAnalysis += (endAnalysis - startAnalysis);
				// #endif
			}
		} else {
			UnitGraph bodyGraph = new BriefUnitGraph(body);
			// #ifdef METRICS
			startAnalysis = System.nanoTime();
			// #endif
			analysis = new SimpleUninitializedVariableAnalysis(bodyGraph);
			// #ifdef METRICS
			endAnalysis = System.nanoTime();
			totalAnalysis = endAnalysis - startAnalysis;
			// #endif
		}

		// #ifdef METRICS
		this.sink.flow(body, "UV A1 flowthrough time", analysis.getFlowThroughTime());
		this.sink.flow(body, "UV A1 flowthrough", analysis.getFlowThroughCounter());
		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
		profilingTag.setUvAnalysisTime(totalAnalysis);
		profilingTag.setPreprocessingTime(0);
		
		// minimal = (minSize* maxTime)/maxSize
		if (minimalBodySize != 0 && maximumBodySize != 0) {
			double minimalProportionalJimplificationTime = (minimalBodySize * profilingTag.getJimplificationTime())/maximumBodySize;
			profilingTag.setJimplificationTime((profilingTag.getJimplificationTime() + Math.round(minimalProportionalJimplificationTime))/2);
		}
		// #endif
	}

	public Transformer setMetricsSink(MetricsSink sink) {
		this.sink = sink;
		return this;
	}
}
