package br.ufal.cideei.soot.analyses;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import br.ufal.cideei.soot.instrument.FeatureTag;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

// A -> FlowSet
// N -> Unit
// T -> must be the same used to annotate the FeatureTag
public abstract class FeatureSensitiviteFowardFlowAnalysis<A extends Unit, N extends FlowSet,T extends Collection> extends ForwardFlowAnalysis<Unit,FlowSet>{

	private final Set<?> configuration;

	public FeatureSensitiviteFowardFlowAnalysis(DirectedGraph graph, Set<?> configuration) {
		super(graph);
		this.configuration = configuration;
	}
	
	protected void flowThrough(FlowSet a, Unit b, FlowSet c) {
		
	}

	protected boolean beforeFilter(N arg0, A arg1, N arg2) {
		if (arg1.hasTag("FeatureTag")) {
			FeatureTag<T> tag = (FeatureTag<T>) arg1.getTag("FeatureTag");
			
			List<T> annotatedConfigs = tag.getFeatures();
			Iterator<T> annotatedconfigsIterator = annotatedConfigs.iterator();
			while (annotatedconfigsIterator.hasNext()) {
				T annotatedconfig = (T) annotatedconfigsIterator.next();
				if (configuration.containsAll(annotatedconfig)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected abstract void filteredFlowThrough(N arg0, A arg1, N arg2);
	
}
