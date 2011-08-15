package br.ufal.cideei.soot.instrument.bddrep;

import java.util.Collection;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import org.apache.commons.collections.bidimap.UnmodifiableBidiMap;

import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.util.Pair;

public class BDDConfigRep implements ILazyConfigRep {

	private UnmodifiableBidiMap atoms;
	private BDD configs;

	private BDDConfigRep(BDD configsAsBDD, UnmodifiableBidiMap atoms) {
		this.configs = configsAsBDD;
		this.atoms = atoms;
	}

	public static BDDConfigRep localConfigurations(UnmodifiableBidiMap atoms, BDDFactory factory) {
		return new BDDConfigRep(factory.one(), atoms);
	}

	@Override
	public ILazyConfigRep intersection(ILazyConfigRep aOther) {
		if (aOther instanceof BDDConfigRep) {
			BDDConfigRep other = (BDDConfigRep) aOther;
			return new BDDConfigRep(other.configs.and(this.configs), atoms);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Pair<ILazyConfigRep, ILazyConfigRep> split(IFeatureRep aRep) {
		if (aRep instanceof BDDFeatureRep) {
			BDDFeatureRep rep = (BDDFeatureRep) aRep;
			BDD repBDD = rep.getBDD();
			return new Pair<ILazyConfigRep, ILazyConfigRep>(new BDDConfigRep(this.configs.and(repBDD), atoms), new BDDConfigRep(this.configs.and(repBDD.not()), atoms));
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Pair<ILazyConfigRep, ILazyConfigRep> split(Collection<IConfigRep> belongedConfigs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILazyConfigRep union(ILazyConfigRep aOther) {
		if (aOther instanceof BDDConfigRep) {
			BDDConfigRep other = (BDDConfigRep) aOther;
			return new BDDConfigRep(other.configs.or(this.configs), atoms);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public int size() {
		return configs.nodeCount();
	}

	@Override
	public boolean belongsToConfiguration(IFeatureRep rep) {
		throw new UnsupportedOperationException();
	}

}
