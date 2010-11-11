package br.ufal.cideei.soot.analyses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.Unit;
import soot.toolkits.scalar.AbstractFlowSet;
import soot.toolkits.scalar.ArraySparseSet;
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

		Set<String> ASet = new HashSet<String>();
		Set<String> BSet = new HashSet<String>();
		Set<String> ABSet = new HashSet<String>();
		ASet.add("A");
		BSet.add("B");
		ABSet.add("A");
		ABSet.add("B");

		this.map.put(ASet, new ArraySparseSet());
		this.map.put(BSet, new ArraySparseSet());
		this.map.put(ABSet, new ArraySparseSet());

	}

	public LiftedFlowSet(LiftedFlowSet other) {
		// this.map = (HashMap<Set<String>, FlowSet>) other.getMap().clone();
		HashMap<Set<String>, FlowSet> otherMap = other.getMap();

		this.map = new HashMap<Set<String>, FlowSet>();
		this.map.putAll(otherMap);
	}

	@Override
	public LiftedFlowSet clone() {
		return new LiftedFlowSet(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LiftedFlowSet))
			return false;
		if (obj == this)
			return true;
		LiftedFlowSet other = (LiftedFlowSet) obj;
		if (other.map.equals(this.map)) {
			return true;
		}
		return false;
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	// TODO implementar o add/remove(Object, FlowSet)?

	public void add(Set<String> config, Object obj) {
		if (map.containsKey(config)) {
			map.get(config).add(obj);
		}
	}

	@Override
	// FIXME
	public void add(Object object) {
		FeatureTag<T> tag = getFeatureTag(object);

		if (tag != null) {
			List<Set<String>> taggedFeatures = (List<Set<String>>) tag.getFeatures();
			for (Set<String> taggedFeatureSet : taggedFeatures) {
				if (map.containsKey(taggedFeatureSet)) {
					map.get(taggedFeatureSet).add(object);
				}
			}

			// TODO tomar cuidado com esse else... acho que vem "" e isso n‹o Ž
			// igual a null!
		} else {
			// In this case, the unit is mandatory, so that it should be
			// included in all configurations.
			for (Set<String> featureSet : map.keySet()) {
				map.get(featureSet).add(object);
			}
		}
	}

	@Override
	// FIXME: lança exceção quando o actual configuration não está setado
	public boolean contains(Object object) {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.contains(object);
	}

	@Override
	// FIXME: lança exceção quando o actual configuration não está setado
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
	// FIXME: lança exceção quando o actual configuration não está setado
	public int size() {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.size();
	}

	@Override
	// FIXME: lança exceção quando o actual configuration não está setado
	public List toList() {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.toList();
	}

	// TODO implementar nosso iterator pra evitar que os clientes conhecam nossa
	// map???
	// public Iterator iterator() {
	// return map.
	// }

	@Override
	public void copy(FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) dest;

		Set<Set<String>> configurations = map.keySet();

		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;

			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);

			thisNormal.copy(otherNormal);
		}
	}

	@Override
	public void difference(FlowSet other) {
		difference(other, this);
	}

	@Override
	public void difference(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;
		
		// If they are the same object, or equal, then the difference must be empty
		if (destLifted == this && destLifted == other || other.equals(destLifted)) {
			destLifted.clearFlowSets();
			return;
		}

		Set<Set<String>> configurations = map.keySet();

		for (Set<String> configuration : configurations) {
			if (otherLifted.map.containsKey(configuration)) {
				FlowSet thisNormal = (FlowSet) map.get(configuration);
				FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
				
				FlowSet destNewFlowSet = new ArraySparseSet();
				destLifted.map.put(configuration, destNewFlowSet);
				thisNormal.difference(otherNormal, destNewFlowSet);
			}
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
	
	private void clearFlowSets() {
		Set<Set<String>> keySet = this.map.keySet();
		for (Set<String> set : keySet) {
			map.put(set, new ArraySparseSet());
		}
	}

	@Override
	public String toString() {
		return this.map.toString();
	}
}