package br.ufal.cideei.soot.instrument;

import java.util.Collection;

import br.ufal.cideei.util.Pair;

public interface ILazyConfigRep extends IConfigRep {
	public Pair<ILazyConfigRep, ILazyConfigRep> split(IFeatureRep rep);

	public Pair<ILazyConfigRep, ILazyConfigRep> split(Collection<IConfigRep> belongedConfigs);
	
	public ILazyConfigRep intersection(ILazyConfigRep aOther);

	public ILazyConfigRep union(ILazyConfigRep otherKey);
}
