package br.ufal.cideei.soot.instrument;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * The Class FeatureTag is used to store feature-sensitive metadata. The metadata is simply stored in a Set collection.
 * 
 * @param <IFeatureRep>
 *            the element type
 */
public class FeatureTag implements Tag {

	/** The Constant FEAT_TAG_NAME. */
	public static final String FEAT_TAG_NAME = "FeatureTag";

	IFeatureRep rep;

	public FeatureTag(IFeatureRep rep) {
		this.rep = rep;
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

	public IFeatureRep getFeatureRep() {
		return this.rep;
	}

	@Override
	public String toString() {
		return this.rep.toString();
	}
}