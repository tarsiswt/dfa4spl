package br.ufal.cideei.soot.instrument.bddrep;

import java.util.Set;

import net.sf.javabdd.BDD;

import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;

public class BDDFeatureRep implements IFeatureRep {
	
	BDD configs;
	
	public BDDFeatureRep clone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IFeatureRep addAll(IFeatureRep rep) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean belongsToConfiguration(IConfigRep config) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getFeatures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public BDD getBDD() {
		return null;
	}
}
