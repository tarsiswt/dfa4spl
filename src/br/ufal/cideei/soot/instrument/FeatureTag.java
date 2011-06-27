package br.ufal.cideei.soot.instrument;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.BidiMap;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * The Class FeatureTag is used to store feature-sensitive metadata. The
 * metadata is simply stored in a Set collection.
 * 
 * @param <E>
 *            the element type
 */
public class FeatureTag<E> extends AbstractSet<E> implements Tag {

	/** The Constant FEAT_TAG_NAME. */
	public static final String FEAT_TAG_NAME = "FeatureTag";

	/** The features are kept in this list */
	private Set<E> features = new HashSet<E>();

	private Integer Id = -1;

	private BidiMap atoms;

	public int getId() {
		return Id;
	}

	private static final FeatureTag emptyTag = new FeatureTag();

	static {
//		emptyTag = new FeatureTag();
		emptyTag.features = Collections.unmodifiableSet(Collections.emptySet());
		emptyTag.Id = 0;
	}

	public static <E> FeatureTag<E> emptyFeatureTag() {
		return emptyTag;
	}

	/**
	 * Adds a feature to the list.
	 * 
	 * @param ft
	 *            the feature to be added.
	 */
	public boolean add(E ft) {
		return features.add(ft);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return features.addAll(c);
	}

	@Override
	public boolean contains(Object o) {
		return features.contains(o);
	}

	/**
	 * Removes a given feature from the list.
	 * 
	 * @param ft
	 *            the feature to be removed.
	 * @return
	 */
	public boolean remove(Object ft) {
		return features.remove(ft);
	}

	/**
	 * Gets the features as an unmodifiable List.
	 * 
	 * @return the features
	 */
	public Collection<E> getFeatures() {
		return Collections.unmodifiableSet(this.features);
	}

	public void setFeatures(Set<E> features) {
		this.features = features;
	}

	@Override
	public boolean isEmpty() {
		return this.features.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.tagkit.Tag#getName()
	 */
	@Override
	public String getName() {
		return FeatureTag.FEAT_TAG_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.tagkit.Tag#getValue()
	 */
	@Override
	public byte[] getValue() throws AttributeValueException {
		return null;
	}

	@Override
	public String toString() {
		return features.toString();
	}

	@Override
	public Iterator<E> iterator() {
		return features.iterator();
	}

	@Override
	public int size() {
		return features.size();
	}

	public boolean belongsToConfiguration(Collection<E> configuration) {
		return configuration.containsAll(this);
	}

	/**
	 * Gera o identificador para esta Tag baseado no superconjunto. Não utilizar
	 * quando a esta Tag diz respeito a um Body.
	 */
	public void generateId(Map<E, Integer> atoms) {
		this.Id = 0;
		for (E element : this.features) {
			Integer featId = atoms.get(element);
			if (featId != null) {
				this.Id += featId;
			}
		}
	}

	public void setFeatureIdMap(BidiMap atoms) {
		this.atoms = atoms;
	}
	
	public Set<E> getAtoms(){
		return this.atoms.keySet();
	}

	/**
	 * Para um dado ID, retorna o conjunto de Features.
	 * 
	 * @param id
	 * @return
	 */
	public Set<E> getConfigurationForId(Integer id) {
		Set<E> configuration = new HashSet<E>();
		int highestOneBit = Integer.highestOneBit(id);
		configuration.add((E) atoms.getKey(highestOneBit));
		int tmp = id - highestOneBit;
		while (tmp >= 1) {
			highestOneBit = Integer.highestOneBit(tmp);
			tmp -= highestOneBit;
			configuration.add((E) atoms.getKey(highestOneBit));
		}
		return configuration;
	}
	
	public Integer getIdForConfiguration(Set<E> configuration) {
		Iterator<E> iterator = configuration.iterator();
		int accumulator = 0;
		while (iterator.hasNext()) {
			E e = (E) iterator.next();
			accumulator += (Integer) atoms.get(e);			
		}
		return accumulator;
	}
}