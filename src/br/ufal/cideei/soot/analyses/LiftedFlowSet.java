package br.ufal.cideei.soot.analyses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import soot.toolkits.scalar.AbstractFlowSet;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

/**
 * The Class LiftedFlowSet.
 * 
 * TODO: Explicar próposito e função desta classe.
 * 
 * @param <T>
 *            the generic type
 */
public class LiftedFlowSet<T> extends AbstractFlowSet {

	private List<Set<String>> configurations;
	
	private List<FlowSet> lattices;
	
	public List<Set<String>> getConfigurations() {
		return Collections.unmodifiableList(configurations);
	}
	
	public List<FlowSet> getLattices() {
		return Collections.unmodifiableList(lattices);
	}

	/**
	 * Instantiates a new LiftedFlowSet.
	 */
	public LiftedFlowSet(Collection<Set<String>> configs) {
		
		//Both lists have the same size...
		//Configurations = [ {}, {A}, {B}, {A, B} ]
		//Lattices       = [ l1, l2, l3, l4 ]
		
		this.configurations = new ArrayList<Set<String>>();
		this.lattices = new ArrayList<FlowSet>();
		
		for (Set<String> configuration : configs) {
			this.configurations.add(configuration);
			this.lattices.add(new ArraySparseSet());
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
		
		List<Set<String>> otherConfigurations = other.getConfigurations();
		List<FlowSet> otherLattices = other.getLattices();
		
		this.configurations = new ArrayList<Set<String>>();
		this.lattices = new ArrayList<FlowSet>();
		
		this.configurations.addAll(otherConfigurations);
		this.lattices.addAll(otherLattices);
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
		
		if (other.configurations.equals(this.configurations) && other.lattices.equals(this.lattices)) {
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
		this.configurations.clear();
		this.lattices.clear();
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
		return configurations.isEmpty() && lattices.isEmpty();
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#size()
	 */
	@Override
	public int size() {
		return this.configurations.size();
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

		for (int i = 0; i < this.size(); i++) {
			FlowSet otherNormal = (FlowSet) destLifted.lattices.get(i);
			FlowSet thisNormal = (FlowSet) lattices.get(i);
			
			thisNormal.copy(otherNormal);
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
//	@Override
//	public void difference(FlowSet other, FlowSet dest) {
//		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
//		LiftedFlowSet destLifted = (LiftedFlowSet) dest;
//
//		destLifted.clearFlowSets();
//
//		/*
//		 * If they are the same object, or equal, then the resulting difference
//		 * must be empty.
//		 */
//		if (this.equals(other)) {
//			return;
//		}
//
//		for (int i = 0; i < this.size(); i++) {
//			FlowSet otherNormal = (FlowSet) otherLifted.lattices.get(i);
//			FlowSet thisNormal = (FlowSet) lattices.get(i);
//			
//			FlowSet destNewFlowSet = new ArraySparseSet();
//			destLifted.lattices.add(i, destNewFlowSet);
//			thisNormal.difference(otherNormal, destNewFlowSet);
//		}
//	}

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
//	@Override
//	public void intersection(FlowSet other, FlowSet dest) {
//		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
//		LiftedFlowSet destLifted = (LiftedFlowSet) dest;
//
//		destLifted.clearFlowSets();
//
//		for (int i = 0; i < this.size(); i++) {
//			FlowSet otherNormal = (FlowSet) otherLifted.lattices.get(i);
//			FlowSet thisNormal = (FlowSet) lattices.get(i);
//			
//			FlowSet destNewFlowSet = new ArraySparseSet();
//			destLifted.lattices.add(i, destNewFlowSet);
//			thisNormal.intersection(otherNormal, destNewFlowSet);
//		}
//	}

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

		for (int i = 0; i < this.size(); i++) {
			FlowSet otherNormal = (FlowSet) otherLifted.lattices.get(i);
			FlowSet thisNormal = (FlowSet) lattices.get(i);
			
			FlowSet destNewFlowSet = new ArraySparseSet();
			destLifted.lattices.add(i, destNewFlowSet);
			thisNormal.union(otherNormal, destNewFlowSet);
		}
	}

	/**
	 * Clear only the FlowSets inside the map, the keys remain.
	 */
	private void clearFlowSets() {
		//TODO isso est‡ errado... o limpar dele Ž colocar o ArraySparseSet...
		this.lattices.clear();
	}

	/**
	 * Returns a String representation of this object.
	 * 
	 * @return String representation
	 * 
	 */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < this.size(); i++) {
			result.append(this.configurations.get(i).toString());
			result.append("=");
			result.append(this.lattices.get(i).toString());
			result.append("; ");
		}
		return result.toString();
	}

}