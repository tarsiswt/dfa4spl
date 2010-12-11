package br.ufal.cideei.soot.instrument;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import de.ovgu.cide.features.IFeature;

import br.ufal.cideei.features.IFeatureExtracter;

// TODO: Auto-generated Javadoc
/**
 * The Class LineNumberColorMapper.
 */
public class LineNumberColorMapper extends ASTVisitor {

	/** The compilation unit. */
	private CompilationUnit compilationUnit;

	/** The file. */
	private IFile file;

	/** The extracter. */
	private IFeatureExtracter extracter;

	/** The line to colors. */
	private Map<Integer, Set<String>> lineToColors = new HashMap<Integer, Set<String>>();

	/**
	 * Gets the line to colors.
	 * 
	 * @return the line to colors
	 */
	public Map<Integer, Set<String>> getLineToColors() {
		return lineToColors;
	}

	/**
	 * Instantiates a new line number color mapper.
	 * 
	 * @param compilationUnit
	 *            the compilation unit
	 * @param file
	 *            the file
	 * @param extracter
	 *            the extracter
	 */
	LineNumberColorMapper(CompilationUnit compilationUnit, IFile file, IFeatureExtracter extracter) {
		// dependencies
		this.compilationUnit = compilationUnit;
		this.file = file;
		this.extracter = extracter;
	}

	/**
	 * Defeats instantiation.
	 */
	private LineNumberColorMapper() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom
	 * .ASTNode)
	 */
	public void preVisit(ASTNode node) {
		int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		Set<String> mappedFeatureSet = lineToColors.get(lineNumber);
		if (mappedFeatureSet == null) {
			Set<String> extractedFeatures = extracter.getFeaturesNames(node, file);
			if (!extractedFeatures.isEmpty()) {
				lineToColors.put(lineNumber, extractedFeatures);
			}
		}
	}
}
