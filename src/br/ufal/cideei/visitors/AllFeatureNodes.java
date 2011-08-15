package br.ufal.cideei.visitors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.text.ITextSelection;

import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
import br.ufal.cideei.features.IFeatureExtracter;

public class AllFeatureNodes extends ASTVisitor {

	private ITextSelection textSelection;
	private IFile file;
	private IFeatureExtracter extracter;

	/** The nodes will be added to this Set as the visitor visits the nodes */
	private Set<ASTNode> nodes = new HashSet<ASTNode>();
	private Set<String> selectionFeatures;

	/**
	 * Instantiates a new selection nodes visitor.
	 */
	@SuppressWarnings("unused")
	private AllFeatureNodes() {
	}

	/**
	 * Instantiates a new selection nodes visitor.
	 * 
	 * @param textSelection
	 *            the text selection
	 */
	public AllFeatureNodes(ITextSelection textSelection, IFile file, Set<String> features) {
		extracter = CIDEFeatureExtracterFactory.getInstance().getExtracter();
		selectionFeatures = features;
		this.textSelection = textSelection;
		this.file = file;
	}

	/**
	 * Gets the nodes.
	 * 
	 * @return the nodes
	 */
	public Set<ASTNode> getNodes() {
		return nodes;
	}

	/**
	 * Populates the {@link #nodes} Set with the ASTNodes. Use
	 * {@link #getNodes()} to retrieve the nodes after accepting this visitor to
	 * an ASTNode
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
	 */
	public void preVisit(ASTNode node) {
		super.preVisit(node);
		Set<String> featuresNode = extracter.getFeaturesNames(node, file);
		if (!featuresNode.isEmpty() && selectionFeatures.containsAll(featuresNode)) {
			nodes.add(node);
		}
	}

}
