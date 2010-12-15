package br.ufal.cideei.soot.analyses;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import br.ufal.cideei.soot.instrument.FeatureTag;

// TODO: Auto-generated Javadoc
/**
 * The Class FeatureSensitiviteFowardFlowAnalysis is the base implementation for
 * forward flow analysis that are feature-sensitive. Implementing an
 * feature-sensitive analysis extending this class is very similar to extending
 * the ForwardFlowAnalysis class.
 * 
 * @param <A>
 *            the generic type
 * @param <N>
 *            the number type
 * @param <T>
 *            the generic type
 */
public abstract class FeatureSensitiveFowardFlowAnalysis<A extends Unit, N extends FlowSet, T extends Collection> extends ForwardFlowAnalysis<Unit, FlowSet> {

	/** The configuration. */
	protected final Set<?> configuration;
	private Set<Unit> unitBin = new HashSet<Unit>();

	// #ifdef METRICS
	private static long flowThroughCounter = 0;
	private static long jumpOverCounter = 0;

	public static long getFlowThroughCounter() {
		return flowThroughCounter;
	}
	
	public static long getJumpOverCounter() {
		return jumpOverCounter;
	}
	
	public static void reset() {
		flowThroughCounter = 0;
		jumpOverCounter = 0;
	}

	// #endif

	/**
	 * Instantiates a new feature sensitivite foward flow analysis.
	 * 
	 * @param graph
	 *            the graph
	 * @param configuration
	 *            the configuration
	 */
	public FeatureSensitiveFowardFlowAnalysis(DirectedGraph graph, Set<?> configuration) {
		super(graph);
		this.configuration = configuration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.FlowAnalysis#flowThrough(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	protected final void flowThrough(FlowSet source, Unit unit, FlowSet dest) {
		if (beforeFilter(source, unit, dest)) {
			// #ifdef METRICS
			flowThroughCounter++;
			// #endif
			filteredFlowThrough(source, unit, dest);
		} else {
			// #ifdef METRICS
			jumpOverCounter++;
			// #endif
			source.copy(dest);
		}
	}

	/**
	 * Before filter.
	 * 
	 * @param source
	 *            the source
	 * @param unit
	 *            the unit
	 * @param dest
	 *            the dest
	 * @return true, if successful
	 */
	protected boolean beforeFilter(FlowSet source, Unit unit, FlowSet dest) {
		if (unit.hasTag("FeatureTag")) {
			FeatureTag<T> tag = (FeatureTag<T>) unit.getTag("FeatureTag");
			Collection<T> annotatedConfigs = (Collection<T>) tag.getFeatures();

			Iterator<T> annotatedconfigsIterator = annotatedConfigs.iterator();
			while (annotatedconfigsIterator.hasNext()) {
				T annotatedconfig = (T) annotatedconfigsIterator.next();
				if (configuration.containsAll(annotatedconfig)) {
					return true;
				}
			}
		}
		this.unitBin.add(unit);
		return false;
	}

	public FlowSet getFlowAfter(Unit unit) {
		if (this.unitBin.contains(unit)) {
			return new ArraySparseSet();
		} else {
			return super.getFlowAfter(unit);
		}
	}

	/**
	 * Filtered flow through.
	 * 
	 * @param source
	 *            the source
	 * @param unit
	 *            the unit
	 * @param dest
	 *            the dest
	 */
	protected abstract void filteredFlowThrough(FlowSet source, Unit unit, FlowSet dest);
}
