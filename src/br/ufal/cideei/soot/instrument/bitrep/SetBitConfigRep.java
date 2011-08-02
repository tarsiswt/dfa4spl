package br.ufal.cideei.soot.instrument.bitrep;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.UnmodifiableBidiMap;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.util.Pair;

public class SetBitConfigRep implements ILazyConfigRep {

	private UnmodifiableBidiMap atoms;
	private Set<IConfigRep> configs;
	private final int highestId;

	public SetBitConfigRep(Collection<IConfigRep> configs, BidiMap atoms, int highestId) {
		this.atoms = (UnmodifiableBidiMap) UnmodifiableBidiMap.decorate(atoms);
		this.configs = Collections.unmodifiableSet(new HashSet<IConfigRep>(configs));
		this.highestId = highestId;
	}

	@Override
	public boolean belongsToConfiguration(IFeatureRep rep) {
		for (IConfigRep config : configs) {
			if (config.belongsToConfiguration(rep)) {
				return true;
			}
		}
		return false;
	}
	
	public Set<IConfigRep> getConfigs() {
		return configs;
	}

	public Set<IConfigRep> belongsToConfigurations(IFeatureRep rep) {
		Set<IConfigRep> foundConfigs = new HashSet<IConfigRep>();
		for (IConfigRep config : configs) {
			if (config.belongsToConfiguration(rep)) {
				foundConfigs.add(config);
			}
		}
		return foundConfigs;
	}

	public Pair<ILazyConfigRep, ILazyConfigRep> split(IFeatureRep rep) {
		Set<IConfigRep> belongsToConfigurations = belongsToConfigurations(rep);

		Set<IConfigRep> leftSplit = new HashSet<IConfigRep>(configs);
		leftSplit.removeAll(belongsToConfigurations);

		return new Pair<ILazyConfigRep, ILazyConfigRep>(new SetBitConfigRep(leftSplit, atoms, highestId), new SetBitConfigRep(belongsToConfigurations, atoms,
				highestId));
	}

	@Override
	public Pair<ILazyConfigRep, ILazyConfigRep> split(Collection<IConfigRep> belongedConfigs) {
		Set<IConfigRep> leftSplit = new HashSet<IConfigRep>(configs);
		leftSplit.removeAll(belongedConfigs);

		return new Pair<ILazyConfigRep, ILazyConfigRep>(new SetBitConfigRep(leftSplit, atoms, highestId),
				new SetBitConfigRep(belongedConfigs, atoms, highestId));
	}

	@Override
	public int size() {
		return this.configs.size();
	}

	@Override
	public String toString() {
		return configs.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (this == o)
			return true;
		if (!(o instanceof SetBitConfigRep))
			return false;
		SetBitConfigRep that = (SetBitConfigRep) o;
		return new EqualsBuilder().append(this.configs, that.configs).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.configs).toHashCode();
	}

	@Override
	public BitVectorConfigRep intersection(ILazyConfigRep aOther) {
		throw new UnsupportedOperationException();
	}

	@Override
	public BitVectorConfigRep union(ILazyConfigRep otherKey) {
		throw new UnsupportedOperationException();
	}
}
