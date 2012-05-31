package br.ufal.cideei.features;

import java.util.Set;

public interface FeatureSetChecker {

	public boolean check(Set<String> trueSet, Set<String> falseSet);

}
