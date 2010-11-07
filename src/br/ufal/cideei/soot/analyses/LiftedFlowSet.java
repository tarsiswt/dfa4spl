package br.ufal.cideei.soot.analyses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.toolkits.scalar.AbstractFlowSet;
import soot.toolkits.scalar.FlowSet;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class LiftedFlowSet<T> extends AbstractFlowSet {

	private HashMap<Set<String>, FlowSet> map;

	public HashMap<Set<String>, FlowSet> getMap() {
		return map;
	}

	private Set<String> actualConfiguration;

	public LiftedFlowSet() {
		 this.map = new HashMap<Set<String>, FlowSet>();
	}
	
	public LiftedFlowSet(LiftedFlowSet other) {
		this.map = (HashMap<Set<String>, FlowSet>) other.getMap().clone();
	}
	
	@Override
	public LiftedFlowSet clone() {
		return new LiftedFlowSet(this);
	}
	
	@Override
	public void clear() {
		this.map.clear();
	}

	//TODO implementar o add/remove(Object, FlowSet)?

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
		FeatureTag<T> tag = getFeatureTag(object);
		if (tag != null) {
			List<Set<String>> taggedFeatures = (List<Set<String>>) tag.getFeatures();
			for (Set<String> taggedFeatureSet : taggedFeatures) {
				if (map.containsKey(taggedFeatureSet)) {
					map.get(taggedFeatureSet).remove(object);
				}
			}
		}
	}

	@Override
	public int size() {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.size();
	}

	@Override
	public List toList() {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.toList();
	}

	@Override
	public void copy(FlowSet dest) {
		// TODO Auto-generated method stub
		LiftedFlowSet otherLifted = (LiftedFlowSet) dest;
		
		Set<Set<String>> configurations = map.keySet();
		
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;
			
			FlowSet thisNormal = (FlowSet) map.get(configuration);
			
			//copy();
		}
	}
	
	@Override
	public void difference(FlowSet other) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		
		Set<Set<String>> configurations = map.keySet();
		
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;
			
			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);
			
			thisNormal.difference(otherNormal);
		}
	}
	
	@Override
	public void difference(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;
		
		Set<Set<String>> configurations = map.keySet();
		
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;
			
			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);
			FlowSet destNormal = (FlowSet) destLifted.map.get(configuration);
			
			thisNormal.difference(otherNormal, destNormal);
		}
	}

	@Override
	public void intersection(FlowSet other) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		
		Set<Set<String>> configurations = map.keySet();
		
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;
			
			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);
			
			thisNormal.intersection(otherNormal);
		}
	}
	
	@Override
	public void intersection(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;
		
		Set<Set<String>> configurations = map.keySet();
		
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;
			
			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);
			FlowSet destNormal = (FlowSet) destLifted.map.get(configuration);
			
			thisNormal.intersection(otherNormal, destNormal);
		}
	}
	
	@Override
	public void union(FlowSet other) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		
		Set<Set<String>> configurations = map.keySet();
		
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;
			
			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);
			
			thisNormal.union(otherNormal);
		}
	}
	
	@Override
	public void union(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;
		
		Set<Set<String>> configurations = map.keySet();
		
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;
			
			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);
			FlowSet destNormal = (FlowSet) destLifted.map.get(configuration);
			
			thisNormal.union(otherNormal, destNormal);
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