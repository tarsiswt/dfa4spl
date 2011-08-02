package br.ufal.cideei.soot.analyses.reachingdefs;

import java.util.List;

import soot.Unit;

// TODO: Auto-generated Javadoc
/**
 * The Interface ReachedDefinitions.
 */
public interface IReachedDefinitions {
	
	/**
	 * Gets the reached uses.
	 *
	 * @param target the target
	 * @return the reached uses
	 */
	public List<Unit> getReachedUses(Unit target);
}
