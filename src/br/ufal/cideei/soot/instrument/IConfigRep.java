package br.ufal.cideei.soot.instrument;

public interface IConfigRep {

	boolean belongsToConfiguration(IFeatureRep rep);

	int size();
	
}
