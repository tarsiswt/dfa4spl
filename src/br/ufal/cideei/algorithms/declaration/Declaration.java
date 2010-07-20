package br.ufal.cideei.algorithms.declaration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;

import de.ovgu.cide.features.source.ColoredSourceFile;

public class Declaration {

	private String message = null;
	private Set<ASTNode> nodes = null;
	private ColoredSourceFile file = null;
	private CompilationUnit compilationUnit = null;

	private Declaration() {
	}

	public Declaration(Set<ASTNode> nodes, CompilationUnit compilationUnit, ColoredSourceFile file) {
		this.file = file;
		this.nodes = nodes;
		this.compilationUnit = compilationUnit;
	}
	
	public String getMessage(){
		return this.message;
	}

	public void execute() {
		if (nodes.isEmpty()) {
			return;
		}
		DeclarationVisitor declarationVisitor = new DeclarationVisitor(null);
		Set<SimpleName> simpleNameDeclarations = new HashSet<SimpleName>();

		Iterator<ASTNode> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			ASTNode node = iterator.next();
			declarationVisitor.setNode(node);
			node.accept(declarationVisitor);
			if (declarationVisitor.found()) {
				simpleNameDeclarations.add(declarationVisitor.getName());
			}
			declarationVisitor.reset();
		}

		ASTNode rootNode = nodes.iterator().next().getRoot();

		DeclarationSimpleNameVisitor declarationSimpleNameVisitor = new DeclarationSimpleNameVisitor(simpleNameDeclarations);
		rootNode.accept(declarationSimpleNameVisitor);
		Map<SimpleName, Set<SimpleName>> declarationMap = declarationSimpleNameVisitor.getDeclarationMaps();
		StringBuilder stringBuilder = new StringBuilder();

		for (SimpleName simpleName : simpleNameDeclarations) {
			Set<SimpleName> simpleNameReferences = declarationMap.get(simpleName);
			for (SimpleName simpleNameReference : simpleNameReferences) {
				stringBuilder.append("provides " + simpleName.getIdentifier() + " to line " + compilationUnit.getLineNumber(simpleNameReference.getStartPosition()) + "\n");
			}
		}
		
		this.message = stringBuilder.toString();

	}

}
