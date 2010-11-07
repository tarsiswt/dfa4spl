package br.ufal.cideei.soot.analyses;

import java.util.List;
import java.util.Map;
import java.util.Set;

import br.ufal.cideei.soot.instrument.FeatureTag;

import soot.Unit;
import soot.toolkits.scalar.AbstractFlowSet;
import soot.toolkits.scalar.FlowSet;

public class LiftedFlowSet<T> extends AbstractFlowSet {
	
	Map<Set<String>, FlowSet> map;
	
	Set<String> actualConfiguration;
	
	public LiftedFlowSet() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void add(Object object) {
		FeatureTag<T> tag = getFeatureTag(object);
		if (tag != null) {
			List<Set<String>> taggedFeatures = (List<Set<String>>) tag.getFeatures();
			for (Set<String> taggedFeatureSet : taggedFeatures) {
				if (map.containsKey(taggedFeatureSet)) {
					map.get(taggedFeatureSet).add(object);
				}				
			}
		}
	}
	
	@Override
	public boolean contains(Object object) {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.contains(object);
	}

	@Override
	public boolean isEmpty() {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.isEmpty();
	}

	@Override
	public void remove(Object object) {
		// TODO Auto-generated method stub
		
		// eh pra remover somente o atual ou de todas as configs???
		// ou eu pego as configs da unit (object) e removo somente dessas configs?
		
//		FeatureTag<T> tag = getFeatureTag(object);
//		if (tag != null) {
//			List<Set<String>> taggedFeatures = (List<Set<String>>) tag.getFeatures();
//			for (Set<String> taggedFeatureSet : taggedFeatures) {
//				if (map.containsKey(taggedFeatureSet)) {
//					map.get(taggedFeatureSet).add(object);
//				}				
//			}
//		}
	}

	@Override
	public int size() {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.size();
	}

	@Override
	public List toList() {
		// TODO Auto-generated method stub
		return null;
	}

	public LiftedFlowSet clone() {
		return null;
	}
	
	@Override
	public void difference(FlowSet other, FlowSet dest) {
		// a analise vai chamar esse metodo passando LiftedFlowSet...
		
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;
		
		Set<Set<String>> configurations = map.keySet();
		
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;
			
			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);
			FlowSet destNormal = (FlowSet) destLifted.map.get(configuration);
			
			//tarsis: e assim mesmo? dest.metodo(this, outro)??? Nunca sei essa ordem!
			//olhei isso na API do SOOT, mas queria confirmacao
			destNormal.difference(thisNormal, otherNormal);
		}
	}
	
	private FeatureTag getFeatureTag(Object object) {
		if (object instanceof Unit) {
			Unit unit = (Unit) object;
			if (unit.hasTag("FeatureTag")) {
				FeatureTag<T> tag = (FeatureTag<T>) unit.getTag("FeatureTag");
				return tag;
			}
		}
		return null;
	}
}