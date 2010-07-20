package br.ufal.cideei.algorithms.declaration;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class DeclarationVisitor extends ASTVisitor {
	private ASTNode node;
	private SimpleName simpleName = null;
	
	public DeclarationVisitor(ASTNode node) {
		this.node = node;
	}
	
	public void setNode(ASTNode node) {
		this.node = node;
	}
	
	public SimpleName getName(){
		return this.simpleName;
	}
	
	public void reset(){
		this.simpleName = null;
		this.node = null;
	}
	
	public boolean found(){
		return simpleName == null ? false : true;
	}
	
	public boolean visit(VariableDeclarationFragment declaration){
		this.simpleName = declaration.getName();
		return true;
	}

}
