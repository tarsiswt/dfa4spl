package br.ufal.cideei.algorithms.declaration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

// TODO: Auto-generated Javadoc
/**
 * The Class DeclarationSimpleNameVisitor will visit SimpleName nodes 
 * and associate a Set of SimpleNames to it containing all the references to the SimpleName.
 * 
 * 
 * 
 */
public class DeclarationSimpleNameVisitor extends ASTVisitor {
		
	/** The declaration map. */
	private Map<SimpleName, Set<SimpleName>> declarationMaps = new HashMap<SimpleName, Set<SimpleName>>();

	/**
	 * Instantiates a new declaration simple name visitor. 
	 * The contents of parameters will be stored as the Key Set.
	 *
	 * @param targetNames the target names
	 */
	public DeclarationSimpleNameVisitor(Set<SimpleName> targetNames) {
		 for (SimpleName name : targetNames){
			 declarationMaps.put(name, new HashSet<SimpleName>());
		 }
	}

	/**
	 * Gets the declaration maps.
	 *
	 * @return the declaration maps
	 */
	public Map<SimpleName, Set<SimpleName>> getDeclarationMaps() {
		return declarationMaps;
	}
	
	/**
	 * This visit will map SimpleName uses with the SimpleNames contained in the {@link #declarationMaps} Key Set.
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleName)
	 */
	public boolean visit(SimpleName visitedSimpleName) {
		SimpleName innerSimpleName = hasSimpleName(visitedSimpleName);
		if (hasSimpleName(visitedSimpleName) != null){
			declarationMaps.get(innerSimpleName).add(visitedSimpleName);
		}
		return true;
	}
	
	/**
	 * Checks for a SimpleName which has the same identifer and return it.
	 *
	 * @param visitedSimpleName the visited simple name
	 * @return the SimpleName instance that was found in the {@link #declarationMaps} Key Set.
	 */
	private SimpleName hasSimpleName(SimpleName visitedSimpleName){
		for (SimpleName name : declarationMaps.keySet()){
			if (name.getIdentifier().equals(visitedSimpleName.getIdentifier()) &&
					name.getStartPosition() != visitedSimpleName.getStartPosition()){
				return name;
			}
		}
		return null;
	}

}
