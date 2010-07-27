package br.ufal.cideei.algorithms.unique;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

public class SimpleNameUsesVisitor extends ASTVisitor {

	private SimpleName lookForUsesOf;
	private Set<ASTNode> foundUses = new HashSet<ASTNode>();

	public SimpleNameUsesVisitor(SimpleName lookForUsesOf) {
		this.lookForUsesOf = lookForUsesOf;
	}
	
	public void reset(SimpleName lookForUsesOf){
		this.lookForUsesOf = lookForUsesOf;
		this.foundUses.clear();
	}
	
	public Set<ASTNode> getUsesNodes(){
		return this.foundUses;
	}
	
	public boolean visit(SimpleName visitedName){
//		if (visitedName.equals(lookForUsesOf)){
//			foundUses.add(visitedName);
//		}
		if (visitedName.getIdentifier().equals(lookForUsesOf.getIdentifier())){
			foundUses.add(visitedName);
		}
		return true;
	}

}
