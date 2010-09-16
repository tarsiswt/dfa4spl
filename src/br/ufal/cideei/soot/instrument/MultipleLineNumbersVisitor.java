package br.ufal.cideei.soot.instrument;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class MultipleLineNumbersVisitor extends ASTVisitor {
	private CompilationUnit compilationUnit;
	private Collection<Integer> lines;
	private Set<ASTNode> nodes = new HashSet<ASTNode>();
	
	public Set<ASTNode> getNodes(){
		return nodes;
	}

	public MultipleLineNumbersVisitor(Collection<Integer> lines, CompilationUnit compilationUnit) {
		super();
		this.lines = lines;
		this.compilationUnit = compilationUnit;
	}

	public boolean visit(ASTNode node) {
		if (lines.contains(compilationUnit.getLineNumber(node.getStartPosition()))) {
			nodes.add(node);
		}
		return true;
	}
}
