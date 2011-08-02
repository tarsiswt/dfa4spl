package br.ufal.cideei.soot.analyses;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.soot.instrument.bitrep.BitVectorConfigRep;

public class LazyMapLiftedFlowSet extends MapLiftedFlowSet{

	public LazyMapLiftedFlowSet(Collection<IConfigRep> configs) {
		super(configs);
	}
	
	public LazyMapLiftedFlowSet(IConfigRep seed) {
		super(seed);
		map = new HashMap<IConfigRep, FlowSet>();
		map.put(seed, new ArraySparseSet());
	}
	
	protected LazyMapLiftedFlowSet(Map<IConfigRep, FlowSet> newMap) {
		super(newMap);
	}

	@Override
	public LazyMapLiftedFlowSet clone() {
		Set<Entry<IConfigRep, FlowSet>> entrySet = map.entrySet();
		Map<IConfigRep, FlowSet> newMap = new HashMap<IConfigRep, FlowSet>();
		for (Entry<IConfigRep, FlowSet> entry : entrySet) {
			newMap.put(entry.getKey(), entry.getValue().clone());
		}
		return new LazyMapLiftedFlowSet(newMap);
	}
	
	@Override
	public void intersection(FlowSet aOther, FlowSet aDest) {
		LazyMapLiftedFlowSet other = (LazyMapLiftedFlowSet) aOther;
		LazyMapLiftedFlowSet dest = (LazyMapLiftedFlowSet) aDest;
		
		Set<Entry<IConfigRep, FlowSet>> entrySet = this.map.entrySet();
		Set<Entry<IConfigRep, FlowSet>> otherEntrySet = other.map.entrySet();
		
		HashMap<IConfigRep, FlowSet> destMap = new HashMap<IConfigRep, FlowSet>();
		for (Entry<IConfigRep, FlowSet> entry : entrySet) {
			for (Entry<IConfigRep, FlowSet> otherEntry : otherEntrySet){
				ILazyConfigRep key = (ILazyConfigRep) entry.getKey();
				ILazyConfigRep otherKey = (ILazyConfigRep) otherEntry.getKey();
				
				ILazyConfigRep union = key.union(otherKey);
				if (union.size() != 0) {
					FlowSet otherFlowSet = other.map.get(otherKey);
					ArraySparseSet destFlowSet = new ArraySparseSet();
					this.map.get(key).intersection(otherFlowSet, destFlowSet);
					destMap.put(union, destFlowSet);
				}
			}
		}
		dest.map = destMap;
	}
	
	@Override
	public void union(FlowSet aOther, FlowSet aDest) {
		LazyMapLiftedFlowSet other = (LazyMapLiftedFlowSet) aOther;
		LazyMapLiftedFlowSet dest = (LazyMapLiftedFlowSet) aDest;
		
		Set<Entry<IConfigRep, FlowSet>> entrySet = this.map.entrySet();
		Set<Entry<IConfigRep, FlowSet>> otherEntrySet = other.map.entrySet();
		
		HashMap<IConfigRep, FlowSet> destMap = new HashMap<IConfigRep, FlowSet>();
		for (Entry<IConfigRep, FlowSet> entry : entrySet) {
			for (Entry<IConfigRep, FlowSet> otherEntry : otherEntrySet){
				ILazyConfigRep key = (ILazyConfigRep) entry.getKey();
				ILazyConfigRep otherKey = (ILazyConfigRep) otherEntry.getKey();
				
				ILazyConfigRep union = key.union(otherKey);
				if (union.size() != 0) {
					FlowSet otherFlowSet = other.map.get(otherKey);
					ArraySparseSet destFlowSet = new ArraySparseSet();
					this.map.get(key).union(otherFlowSet, destFlowSet);
					destMap.put(union, destFlowSet);
				}
			}
		}
		dest.map = destMap;
	}
	
}
