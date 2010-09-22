package br.ufal.cideei.soot.instrument.asttounit;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class SingleLineNumberVisitor extends ASTVisitor {

	private CompilationUnit compilationUnit;
	private int line;
	private Set<ASTNode> nodes = new HashSet<ASTNode>();
	
	public Set<ASTNode> getNodes(){
		return nodes;
	}

	public SingleLineNumberVisitor(int line, CompilationUnit compilationUnit) {
		super();
		this.line = line;
		this.compilationUnit = compilationUnit;
	}

	public boolean visit(ASTNode node) {
		if (compilationUnit.getLineNumber(node.getStartPosition()) == line) {
			nodes.add(node);
		}
		return true;
	}
}
