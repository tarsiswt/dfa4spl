package br.ufal.cideei.algorithms.declaration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

public class DeclarationSimpleNameVisitor extends ASTVisitor {
	private Set<SimpleName> cachedKeySet = null;
	private HashMap<SimpleName, Set<SimpleName>> declarationMaps = new HashMap<SimpleName, Set<SimpleName>>();

	public DeclarationSimpleNameVisitor(Set<SimpleName> targetNames) {
		 for (SimpleName name : targetNames){
			 declarationMaps.put(name, new HashSet<SimpleName>());
		 }
		 this.cachedKeySet = declarationMaps.keySet();
	}

	public HashMap<SimpleName, Set<SimpleName>> getDeclarationMaps() {
		return declarationMaps;
	}
	
	public boolean visit(SimpleName visitedSimpleName) {
		SimpleName innerSimpleName = hasSimpleName(visitedSimpleName);
		if (hasSimpleName(visitedSimpleName) != null){
			declarationMaps.get(innerSimpleName).add(visitedSimpleName);
		}
		return true;
	}
	
	private SimpleName hasSimpleName(SimpleName visitedSimpleName){
		for (SimpleName name : cachedKeySet){
			if (name.getIdentifier().equals(visitedSimpleName.getIdentifier()) &&
					name.getStartPosition() != visitedSimpleName.getStartPosition()){
				return name;
			}
		}
		return null;
	}

}
