//#ifdef LAZY
//@package br.ufal.cideei.soot.analyses.wholeline;
//@
//@import java.io.File;
//@import java.util.Map;
//@
//@import profiling.ProfilingTag;
//@import soot.Body;
//@import soot.BodyTransformer;
//@import soot.Unit;
//@import soot.toolkits.graph.ExceptionalUnitGraph;
//@import soot.toolkits.graph.UnitGraph;
//@import br.ufal.cideei.soot.analyses.FlowSetUtils;
//@import br.ufal.cideei.soot.analyses.reachingdefs.LazyLiftedReachingDefinitions;
//@import br.ufal.cideei.soot.analyses.reachingdefs.SimpleReachingDefinitions;
//@import br.ufal.cideei.soot.analyses.uninitvars.SimpleUninitializedVariableAnalysis;
//@import br.ufal.cideei.soot.instrument.ConfigTag;
//@import br.ufal.cideei.soot.instrument.FeatureTag;
//@import br.ufal.cideei.soot.instrument.ILazyConfigRep;
//@import br.ufal.cideei.util.count.AbstractMetricsSink;
//@
//@//XXX: can this class structure could be replaced by an abstract factory?
//@public class WholeLineLazyReachingDefinitions extends BodyTransformer {
//@
//@	private static WholeLineLazyReachingDefinitions instance = new WholeLineLazyReachingDefinitions();
//@
//@	private WholeLineLazyReachingDefinitions() {
//@	}
//@
//@	public static WholeLineLazyReachingDefinitions v() {
//@		return instance;
//@	}
//@
	// #ifdef METRICS
//@	private static final String RD_LAZY_FLOWTHROUGH_COUNTER = "RD LAZY flowthrough";
//@	private static final String RD_LAZY_SHARING_DEGREE = "RD LAZY sharing drg";
//@	private static final String RD_LAZY_MEM = "RD LAZY mem";
//@	private static final String RD_LAZY_FLOWTHROUGH_TIME = "RD LAZY flowthrough time";
//@	private AbstractMetricsSink sink;
//@
//@	public WholeLineLazyReachingDefinitions setMetricsSink(AbstractMetricsSink sink) {
//@		this.sink = sink;
//@		return this;
//@	}
//@
	// #endif
//@
//@	@Override
//@	protected void internalTransform(Body body, String phase, Map options) {
//@		UnitGraph bodyGraph = new ExceptionalUnitGraph(body);
//@		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
//@		int size = configTag.getConfigReps().iterator().next().size();
//@		boolean wentHybrid = false;
//@
//@		LazyLiftedReachingDefinitions lazyReachingDefinitions = null;
//@
		// #ifdef METRICS
//@		long startAnalysis = System.nanoTime();
		// #endif
//@
		// #ifdef HYBRID
//@		if (size == 1) {
//@			wentHybrid = true;
//@			 SimpleReachingDefinitions simpleReachingDefinitions = new SimpleReachingDefinitions(bodyGraph);
//@		} else {
			// #endif
//@			lazyReachingDefinitions = new LazyLiftedReachingDefinitions(bodyGraph, (ILazyConfigRep) configTag.getConfigReps().iterator().next());
//@			lazyReachingDefinitions.execute();
			// #ifdef HYBRID
//@		}
		// #endif
//@
		// #ifdef METRICS
//@
		// #ifdef LAZY
//@//		 if (body.getMethod().getSignature().contains("f(")) {
//@//		 FlowSetUtils.pbm(body, lazyReachingDefinitions, System.getProperty("user.home") + File.separator +
//@//		 "lazy-pix.pbm");
//@//									
//@//		 System.out.println(body.getTag(ConfigTag.CONFIG_TAG_NAME));
//@//		 for (Unit unit : body.getUnits()) {
//@//		 System.out.println(unit + " [[" + unit.getTag(FeatureTag.FEAT_TAG_NAME) + "]]");
//@//		 System.out.println(lazyReachingDefinitions.getFlowAfter(unit));
//@//		 }
//@//		 }
		// #endif
//@
//@		long endAnalysis = System.nanoTime();
//@
//@		if (!wentHybrid) {
//@			this.sink.flow(body, RD_LAZY_FLOWTHROUGH_TIME, lazyReachingDefinitions.getFlowThroughTime());
//@			this.sink.flow(body, RD_LAZY_MEM, FlowSetUtils.lazyMemoryUnits(body, lazyReachingDefinitions, true, 1, configTag.getConfigReps().iterator().next().size()));
//@			this.sink.flow(body, RD_LAZY_SHARING_DEGREE, FlowSetUtils.averageSharingDegree(body, lazyReachingDefinitions));
//@			this.sink.flow(body, RD_LAZY_FLOWTHROUGH_COUNTER, LazyLiftedReachingDefinitions.getFlowThroughCounter());
//@			LazyLiftedReachingDefinitions.reset();
//@		}
//@
//@		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
//@		profilingTag.setRdAnalysisTime2(endAnalysis - startAnalysis);
		// #endif
//@	}
//@}
//@
// #endif
