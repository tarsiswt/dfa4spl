package br.ufal.cideei.soot.instrument;

import java.util.Set;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

public class ConfigTag implements Tag {

	/** The Constant CONFIG_TAG_NAME. */
	public static final String CONFIG_TAG_NAME = "ConfigTag";
	private Set<IConfigRep> reps;

	public ConfigTag(Set<IConfigRep> localConfigs) {
		this.reps = localConfigs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.tagkit.Tag#getName()
	 */
	@Override
	public String getName() {
		return ConfigTag.CONFIG_TAG_NAME;
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

	public Set<IConfigRep> getConfigReps() {
		return this.reps;
	}
	
	public int size(){
		return reps.size();
	}

}
