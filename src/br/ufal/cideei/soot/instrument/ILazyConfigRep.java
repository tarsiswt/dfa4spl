package br.ufal.cideei.soot.instrument;

import java.util.Collection;

import br.ufal.cideei.soot.instrument.bitrep.BitVectorConfigRep;
import br.ufal.cideei.util.Pair;

public interface ILazyConfigRep extends IConfigRep {
	public Collection<IConfigRep> belongsToConfigurations(IFeatureRep rep);

	public Pair<ILazyConfigRep, ILazyConfigRep> split(IFeatureRep rep);

	public Pair<ILazyConfigRep, ILazyConfigRep> split(Collection<IConfigRep> belongedConfigs);
	
	public ILazyConfigRep intersection(ILazyConfigRep aOther);

	public ILazyConfigRep union(ILazyConfigRep otherKey);
}
