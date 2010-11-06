package br.ufal.cideei.soot.analyses;

import java.util.List;
import java.util.Map;
import java.util.Set;

import br.ufal.cideei.soot.instrument.FeatureTag;

import soot.Unit;
import soot.toolkits.scalar.AbstractFlowSet;
import soot.toolkits.scalar.FlowSet;

public class LiftedFlowSet<T> extends AbstractFlowSet {
	
//	@Override
//	public void difference(FlowSet arg0, FlowSet arg1) {
//		// TODO Auto-generated method stub
//		pegar cada par do meu map...
//		para cada par
//			arg1.difference(do cara do map, arg0);
//	}
	
	Map<Set<String>, FlowSet> map;
	
	public LiftedFlowSet() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void add(Object object) {
		// TODO Auto-generated method stub
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
	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void remove(Object arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public List toList() {
		// TODO Auto-generated method stub
		return null;
	}

	public LiftedFlowSet clone() {
		return null;
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