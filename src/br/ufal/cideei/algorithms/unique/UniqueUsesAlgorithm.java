package br.ufal.cideei.algorithms.unique;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import br.ufal.cideei.algorithms.BaseAlgorithm;

public class UniqueUsesAlgorithm extends BaseAlgorithm {

	private Set<ASTNode> nodes;
	private CompilationUnit compilationUnit;
	private String message = "";

	public UniqueUsesAlgorithm(Set<ASTNode> nodes, CompilationUnit compilationUnit, Object file) {
		this.nodes = nodes;
		this.compilationUnit = compilationUnit;
	}

	@Override
	public void execute() {
		MethodDeclaration methodDeclaration = this.getParentMethod(this.nodes.iterator().next());
		SimpleNameUsesVisitor simpleNameVisitor = new SimpleNameUsesVisitor(null);

		StringBuilder stringBuilder = new StringBuilder();
		for (ASTNode eachNode : nodes) {
			if (eachNode instanceof SimpleName) {
				simpleNameVisitor.reset((SimpleName)eachNode);
				methodDeclaration.accept(simpleNameVisitor);
				Set<ASTNode> usesOfSimpleName = simpleNameVisitor.getUsesNodes();
				for (ASTNode useNode : usesOfSimpleName){
					stringBuilder.append(eachNode + " is also used in line " + compilationUnit.getLineNumber(useNode.getStartPosition()) + "\n");
				}
			}
		}
		this.message = stringBuilder.toString();

	}

	@Override
	public String getMessage() {
		return this.message;
	}

}
