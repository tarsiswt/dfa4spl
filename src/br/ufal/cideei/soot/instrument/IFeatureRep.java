package br.ufal.cideei.soot.instrument;

import java.util.Set;

public interface IFeatureRep {
	int size();

	Set<String> getFeatures();

	boolean belongsToConfiguration(IConfigRep config);
	
	public IFeatureRep addAll(IFeatureRep rep);

	IFeatureRep clone();

}
