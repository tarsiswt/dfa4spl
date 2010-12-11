package br.ufal.cideei.soot.instrument;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

// TODO: Auto-generated Javadoc
/**
 * The Class FeatureTag is used to store feature-sensitive metadata. The
 * metadata is simply stored in a List collection.
 * 
 * @param <E>
 *            the element type
 */
public class FeatureTag<E> extends AbstractCollection<E> implements Tag {

	/** The Constant FEAT_TAG_NAME. */
	private static final String FEAT_TAG_NAME = "FeatureTag";

	/** The features are kept in this list */
	private Set<E> features = new HashSet<E>();
	
	public static <E> FeatureTag<E> emptyFeatureTag(){
		FeatureTag emptyTag = new FeatureTag<E>();
		emptyTag.features = Collections.<E>emptySet();
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

}
