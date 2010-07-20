package br.ufal.cideei.algorithms.declaration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;

import de.ovgu.cide.features.source.ColoredSourceFile;

/**
 * 
 * This class perform the Declaration algorithms. It will check for uses of a declared variable in a selection and will
 * display the lines in which the variable is referenced.
 * 
 * This is only a preliminary version, and the class hierarchy hasn't been defined yet.
 * 
 * To run the algorithm use the {@link #execute()} method to perform the operation and then call {@link #getMessage()} to
 * retrive the output message.
 * 
 * @author Társis
 *
 */
public class Declaration {

	/** Will be set at the end of the execute method. */
	private String message = null;
	
	/** The nodes in which we will perform the analysis. */
	private Set<ASTNode> nodes = null;	
	
	/** Not used. */
	private ColoredSourceFile file = null;
	
	/** The compilation unit which we retrieve the lines from the ASTNodes. */
	private CompilationUnit compilationUnit = null;

	
	/**
	 * Disables default constructor
	 */
	private Declaration() {
	}

	
	/**
	 * Instantiates a new declaration.
	 *
	 * @param nodes the nodes
	 * @param compilationUnit the compilation unit
	 * @param file the file
	 */
	public Declaration(Set<ASTNode> nodes, CompilationUnit compilationUnit, ColoredSourceFile file) {
		this.file = file;
		this.nodes = nodes;
		this.compilationUnit = compilationUnit;
	}
	
	
	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage(){
		return this.message;
	}

	/**
	 * Execute the algorithm. Use {@link #getMessage()} to see the results
	 */
	public void execute() {
		if (nodes.isEmpty()) {
			return;
		}
		DeclarationVisitor declarationVisitor = new DeclarationVisitor();
		Set<SimpleName> simpleNameDeclarations = new HashSet<SimpleName>();

		Iterator<ASTNode> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			ASTNode node = iterator.next();
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
