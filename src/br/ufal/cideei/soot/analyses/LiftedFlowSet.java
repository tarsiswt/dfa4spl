package br.ufal.cideei.soot.analyses;

import java.util.Collection;
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

/**
 * The Class LiftedFlowSet.
 * 
 * TODO: Explicar próposito e função desta classe.
 * 
 * @param <T>
 *            the generic type
 */
public class LiftedFlowSet<T> extends AbstractFlowSet {

	/**
	 * The map attribute maps Configurations (Set<String>) to an average Soot
	 * FlowSet (tipically an ArraySparseSet will suffice)
	 */
	private HashMap<Set<String>, FlowSet> map;

	/**
	 * Getter for the map attribute.
	 * 
	 * @return the map
	 */
	public HashMap<Set<String>, FlowSet> getMap() {
		return map;
	}

	/**
	 * Instantiates a new LiftedFlowSet.
	 */
	public LiftedFlowSet(Collection<Set<String>> configs) {
		this.map = new HashMap<Set<String>, FlowSet>();
		for (Set<String> config : configs) {
			this.map.put(config, new ArraySparseSet());
		}
	}

	/**
	 * Instantiates a new LiftedFlowSet, but copy the contents of the other into
	 * this. A pseudo copy-constructor.
	 * 
	 * @param other
	 *            the other
	 */
	public LiftedFlowSet(LiftedFlowSet other) {
		HashMap<Set<String>, FlowSet> otherMap = other.getMap();

		this.map = new HashMap<Set<String>, FlowSet>();
		this.map.putAll(otherMap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#clone()
	 */
	// FIXME: não está funcionando como esperado. É preciso rever esta
	// implementação.
	@Override
	public LiftedFlowSet clone() {
		return new LiftedFlowSet(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// A LiftedFlowSet can only be equal to another LiftedFlowSet
		if (!(obj instanceof LiftedFlowSet))
			return false;
		// Test for self-equality
		if (obj == this)
			return true;
		// If their maps are equals, then the objects are considered equal.
		LiftedFlowSet other = (LiftedFlowSet) obj;
		if (other.map.equals(this.map)) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#clear()
	 */
	@Override
	public void clear() {
		this.map.clear();
	}

	/**
	 * Adds obj to it's respective configuration FlowSet only if such config exists.
	 * 
	 * @param config
	 *            the config
	 * @param obj
	 *            the obj
	 */
	public void add(Set<String> config, Object obj) {
		if (map.containsKey(config)) {
			map.get(config).add(obj);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#add(java.lang.Object)
	 */
	@Override
	/*
	 * FIXME: SUGESTÃO: Este método deve adicionar o object à todos os FlowSets
	 * contidos no map, sem checar pelo FeatureTag. Um outro método que cheque
	 * pelo conteúdo do FeatureTag deve ser implementado. Desta maneira teremos
	 * uma api completa para adicionar elementos de diferentes maneiras ao
	 * LiftedFlowSet.
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#contains(java.lang.Object)
	 */
	@Override
	/*
	 * TODO: SUGESTÃO: mudar a semântica deste método. Retornar true se existe o
	 * objecto existe em QUALQUER um dos FlowSets, false caso contrário. Criar
	 * um novo método com a seguinte assinatura:
	 * LiftedFlowSet#contains(Set<String>,Object). A semântica deste novo método
	 * deve ser óbvia.
	 */
	public boolean contains(Object object) {
		throw new UnsupportedOperationException("This method is not defined for a LiftedFlowSet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#isEmpty()
	 */
	@Override
	/*
	 * TODO: SUGESTÃO: retornar true caso o map esteja vazio, false caso
	 * contrário. Criar um novo método com a seguinte assinatura:
	 * LiftedFlowSet#isEmpty(Set<String>). A semântica deste novo método deve
	 * ser óbvia.
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#remove(java.lang.Object)
	 */
	@Override
	/*
	 * TODO: SUGESTÃO: Vide os comentários, TO-DO e FIX-ME do método add.
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#size()
	 */
	@Override
	public int size() {
		return map.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#toList()
	 */
	@Override
	public List toList() {
		throw new UnsupportedOperationException("This method is not defined for a LiftedFlowSet");
	}

	/*
	 * TODO: implementar nosso iterator pra evitar que os clientes conhecam
	 * nossa map???
	 * 
	 * 
	 * public Iterator iterator() { return map. }
	 */

	/**
	 * Copies this Config-FlowSet mapping into dest.
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#copy(soot.toolkits.scalar.FlowSet)
	 */
	@Override
	/*
	 * TODO: O copy como implementado no AbstracFlowSet limpa o dest antes de
	 * realizar a cópia. Talvez devessémos fazer o mesmo, só que no nosso nível.
	 * Ou seja, limpar o map do dest, antes de realizar a cópia? Deve ser o
	 * clear() ou o clearFlowSets()?
	 */
	public void copy(FlowSet dest) {
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;

		Set<Set<String>> configurations = map.keySet();

		for (Set<String> configuration : configurations) {
			if (destLifted.map.containsKey(configuration)) {
				FlowSet otherNormal = (FlowSet) destLifted.map.get(configuration);
				FlowSet thisNormal = (FlowSet) map.get(configuration);

				thisNormal.copy(otherNormal);
			}
		}
	}

	/**
	 * The difference between LiftedFlowSets is defined as the difference
	 * between every FlowSets in @code{this} and the FlowSets in @code{other}
	 * with the same configuration.
	 * 
	 * The result is placed on @code{dest}. It`s keys are preserverd, but its
	 * flowsets are cleared.
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#difference(soot.toolkits.scalar.
	 *      FlowSet, soot.toolkits.scalar.FlowSet)
	 */
	@Override
	public void difference(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;

		destLifted.clearFlowSets();

		/*
		 * If they are the same object, or equal, then the resulting difference
		 * must be empty.
		 */
		if (this.equals(other)) {
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

	/**
	 * The intersection between LiftedFlowSets is defined as the intersection
	 * between every FlowSets in @code{this} and the FlowSets in @code{other}
	 * with the same configuration.
	 * 
	 * The result is placed on @code{dest}. It`s keys are preserverd, but its
	 * flowsets are cleared.
	 * 
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#intersection(soot.toolkits.scalar
	 *      .FlowSet, soot.toolkits.scalar.FlowSet)
	 */
	@Override
	public void intersection(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;

		destLifted.clearFlowSets();

		Set<Set<String>> configurations = map.keySet();

		for (Set<String> configuration : configurations) {
			if (otherLifted.map.containsKey(configuration)) {
				FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
				FlowSet thisNormal = (FlowSet) map.get(configuration);

				FlowSet destNewFlowSet = new ArraySparseSet();
				destLifted.map.put(configuration, destNewFlowSet);
				thisNormal.intersection(otherNormal, destNewFlowSet);
			}
		}
	}

	/**
	 * The union between LiftedFlowSets is defined as the union between every
	 * FlowSets in @code{this} and the FlowSets in @code{other} with the same
	 * configuration.
	 * 
	 * The result is placed on @code{dest}. It`s keys are preserverd, but its
	 * flowsets are cleared.
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#union(soot.toolkits.scalar.FlowSet,
	 *      soot.toolkits.scalar.FlowSet)
	 */
	@Override
	public void union(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;

		destLifted.clearFlowSets();

		Set<Set<String>> configurations = map.keySet();

		for (Set<String> configuration : configurations) {
			if (otherLifted.map.containsKey(configuration)) {
				FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
				FlowSet thisNormal = (FlowSet) map.get(configuration);

				FlowSet destNewFlowSet = new ArraySparseSet();
				destLifted.map.put(configuration, destNewFlowSet);
				thisNormal.union(otherNormal, destNewFlowSet);
			}
		}
	}

	/**
	 * Gets the FeatureTag of the Object if it is an instance of a Unit.
	 * 
	 * @param object
	 *            the object
	 * @return the feature tag, null otherwise
	 */
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

	/**
	 * Clear only the FlowSets inside the map, the keys remain.
	 */
	private void clearFlowSets() {
		Set<Set<String>> keySet = this.map.keySet();
		for (Set<String> set : keySet) {
			map.put(set, new ArraySparseSet());
		}
	}

	/**
	 * Returns a String representation of this object.
	 * 
	 * @return String representation
	 * 
	 */
	@Override
	public String toString() {
		return this.map.toString();
	}
}