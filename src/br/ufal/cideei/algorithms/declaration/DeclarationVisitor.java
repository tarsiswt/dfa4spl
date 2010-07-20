package br.ufal.cideei.algorithms.declaration;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

// TODO: Auto-generated Javadoc
/**
 * The Class DeclarationVisitor is used to visit an ASTNode in search for VariableDeclarationFragments.
 * The methods {@link #found()} and {@link #reset()} are provided to allow the object reuse.
 * 
 * Perhaps a visitor the receives a set of ASTNodes instead of only one would be better. For an example of
 * such implementation check the {@link DeclarationVisitor } class. 
 * 
 */
public class DeclarationVisitor extends ASTVisitor {
	
	/** 
	 * After visiting an ASTNode, simpleName will be null of no VariableDeclarationFragment was found or the SimpleName instance otherwise.
	 */
	private SimpleName simpleName = null;
	
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public SimpleName getName(){
		return this.simpleName;
	}
	
	/**
	 * This method will null the {@link #simpleName} name attribute. This is a convenience method to reuse this object on loops.
	 */
	public void reset(){
		this.simpleName = null;
	}
	
	/**
	 * Returns true if simpleName is not nulll, false otherwise.
	 *
	 * @return true, if simpleName is not null
	 * @return false otherwise.
	 */
	public boolean found(){
		return simpleName == null ? false : true;
	}
	
	/** 
	 * This visitor will simply collect the {@link VariableDeclarationFragment#getName()}.
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment declaration){
		this.simpleName = declaration.getName();
		return true;
	}

}
