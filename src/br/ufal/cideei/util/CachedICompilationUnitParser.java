package br.ufal.cideei.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class CachedICompilationUnitParser {

	// Caching key
	private IFile file = null;

	// Caching target
	private CompilationUnit cu = null;

	// Expensive to create, reuse it.
	private ASTParser parser = null;

	public CompilationUnit parse(IFile aFile) {
		// MISS
		if (!aFile.equals(this.file)) {
			/*
			 * Lazy initializes the member ASTParser
			 */
			if (parser == null) {
				parser = ASTParser.newParser(AST.JLS3);
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				parser.setResolveBindings(true);
			}

			/*
			 * Create a ASTNode (a CompilationUnit) by reusing the parser;
			 */
			ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(aFile);
			parser.setSource(compilationUnit);
			CompilationUnit jdtCompilationUnit = (CompilationUnit) parser.createAST(null);

			/*
			 * Caches the result
			 */
			this.file = aFile;
			return this.cu = jdtCompilationUnit;
		} // HIT
		else {
			return this.cu;
		}
	}

}
