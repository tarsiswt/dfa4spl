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
	 * TODO: Deveria retornar um UnmodifiableMap?
	 * 
	 * @return the map
	 */
	public HashMap<Set<String>, FlowSet> getMap() {
		return map;
	}

	/** The actual configuration. */
	// TODO: Este atributo provavelmente será removido mais tarde.
	private Set<String> actualConfiguration;

	/**
	 * Instantiates a new LiftedFlowSet.
	 */
	/*
	 * TODO: esta classe necessita que um keySet para o map interno seja
	 * injetado. Ela não funcionará corretamente sem isso. KeySet que está
	 * hardcoded abaixo é o seguinte: [{A},{B} e {A,B}].
	 */
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
	 * TODO: explicar semântica da função
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
	 * TODO: explicar semântica da função
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
	 * Adds obj to it's respective configuration FlowSet if such config exists.
	 * Does nothing otherwise.
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
	// FIXME: lança exceção quando o actual configuration não está setado.
	/*
	 * TODO: SUGESTÃO: mudar a semântica deste método. Retornar true se existe o
	 * objecto existe em QUALQUER um dos FlowSets, false caso contrário. Criar
	 * um novo método com a seguinte assinatura:
	 * LiftedFlowSet#contains(Set<String>,Object). A semântica deste novo método
	 * deve ser óbvia.
	 */
	public boolean contains(Object object) {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.contains(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#isEmpty()
	 */
	@Override
	// FIXME: lança exceção quando o actual configuration não está setado
	/*
	 * TODO: SUGESTÃO: retornar true caso o map esteja vazio, false caso
	 * contrário. Criar um novo método com a seguinte assinatura:
	 * LiftedFlowSet#isEmpty(Set<String>). A semântica deste novo método deve
	 * ser óbvia.
	 */
	public boolean isEmpty() {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.isEmpty();
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
	// FIXME: lança exceção quando o actual configuration não está setado
	/*
	 * TODO: SUGESTÃO: retornar o size do map.
	 */
	public int size() {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#toList()
	 */
	@Override
	// FIXME: lança exceção quando o actual configuration não está setado
	/*
	 * TODO: Não tenho nenhuma sugestão para o refatoramento deste método.
	 * 
	 * Lançar um UnsupportedOperationException?
	 */
	public List toList() {
		FlowSet configurationFlowSet = map.get(actualConfiguration);
		return configurationFlowSet.toList();
	}

	/*
	 * TODO: implementar nosso iterator pra evitar que os clientes conhecam
	 * nossa map???
	 * 
	 * 
	 * public Iterator iterator() { return map. }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see
	 * soot.toolkits.scalar.AbstractFlowSet#copy(soot.toolkits.scalar.FlowSet)
	 */
	@Override
	/*
	 * NOTA: A semântica deste método permanece.
	 */
	public void copy(FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) dest;

		Set<Set<String>> configurations = map.keySet();

		/*
		 * TODO: Checar se o otherLifted realmente contém configuration antes de
		 * realizar um get para evitar nullpointerexceptions
		 */
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;

			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);

			thisNormal.copy(otherNormal);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.toolkits.scalar.AbstractFlowSet#difference(soot.toolkits.scalar.
	 * FlowSet)
	 */
	// @Override
	// public void difference(FlowSet other) {
	// difference(other, this);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see
	 * soot.toolkits.scalar.AbstractFlowSet#difference(soot.toolkits.scalar.
	 * FlowSet, soot.toolkits.scalar.FlowSet)
	 */
	@Override
	public void difference(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;

		/*
		 * If they are the same object, or equal, then the resulting difference
		 * must be empty.
		 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see
	 * soot.toolkits.scalar.AbstractFlowSet#intersection(soot.toolkits.scalar
	 * .FlowSet)
	 */
	// @Override
	// public void intersection(FlowSet other) {
	// LiftedFlowSet otherLifted = (LiftedFlowSet) other;
	//
	// Set<Set<String>> configurations = map.keySet();
	//
	// /*
	// * TODO: Checar se o otherLifted realmente contém configuration antes de
	// * realizar um get para evitar nullpointerexceptions
	// */
	// for (Set<String> configuration : configurations) {
	// actualConfiguration = configuration;
	//
	// FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
	// FlowSet thisNormal = (FlowSet) map.get(configuration);
	//
	// thisNormal.intersection(otherNormal);
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see
	 * soot.toolkits.scalar.AbstractFlowSet#intersection(soot.toolkits.scalar
	 * .FlowSet, soot.toolkits.scalar.FlowSet)
	 */
	@Override
	public void intersection(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;

		Set<Set<String>> configurations = map.keySet();

		/*
		 * TODO: Checar se o otherLifted realmente contém configuration antes de
		 * realizar um get para evitar nullpointerexceptions
		 */
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;

			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);
			FlowSet destNormal = (FlowSet) destLifted.map.get(configuration);

			thisNormal.intersection(otherNormal, destNormal);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see
	 * soot.toolkits.scalar.AbstractFlowSet#union(soot.toolkits.scalar.FlowSet)
	 */
	// @Override
	// public void union(FlowSet other) {
	// LiftedFlowSet otherLifted = (LiftedFlowSet) other;
	//
	// Set<Set<String>> configurations = map.keySet();
	//
	//		
	// for (Set<String> configuration : configurations) {
	// actualConfiguration = configuration;
	//
	// FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
	// FlowSet thisNormal = (FlowSet) map.get(configuration);
	//
	// thisNormal.union(otherNormal);
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * TODO: explicar semântica da função
	 * 
	 * @see
	 * soot.toolkits.scalar.AbstractFlowSet#union(soot.toolkits.scalar.FlowSet,
	 * soot.toolkits.scalar.FlowSet)
	 */
	@Override
	public void union(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;

		Set<Set<String>> configurations = map.keySet();

		/*
		 * TODO: Checar se o otherLifted realmente contém configuration antes de
		 * realizar um get para evitar nullpointerexceptions
		 */
		for (Set<String> configuration : configurations) {
			actualConfiguration = configuration;

			FlowSet otherNormal = (FlowSet) otherLifted.map.get(configuration);
			FlowSet thisNormal = (FlowSet) map.get(configuration);
			FlowSet destNormal = (FlowSet) destLifted.map.get(configuration);

			thisNormal.union(otherNormal, destNormal);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#toString()
	 */
	@Override
	public String toString() {
		return this.map.toString();
	}
}