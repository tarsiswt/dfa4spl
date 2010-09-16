package br.ufal.cideei.soot.instrument;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import soot.Unit;
import soot.tagkit.SourceLnPosTag;

public class ASTNodesAtRangeFinder extends ASTVisitor {

	private CompilationUnit compilationUnit;
	private Collection<ASTNode> foundNodes = new HashSet<ASTNode>();
	private int startLine;
	private int startPos;
	private int endLine;
	private int endPos;

	public ASTNodesAtRangeFinder(int startLine, int startPos, int endLine, int endPos, CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		this.startLine = startLine;
		this.startPos = startPos;
		this.endLine = endLine;
		this.endPos = endPos;
	}

	public ASTNodesAtRangeFinder(Unit unit, CompilationUnit compilationUnit) {
		if (unit.hasTag("SourceLnPosTag")) {
			SourceLnPosTag lineTag = (SourceLnPosTag) unit.getTag("SourceLnPosTag");
			this.startLine = lineTag.startLn();
			this.startPos = lineTag.startPos();
			this.endLine = lineTag.endLn();
			this.endPos = lineTag.endPos();
			this.compilationUnit = compilationUnit;
		} else {
			throw new IllegalArgumentException("No SourceLnPosTag found in this unit.");
		}
	}

	@Override
	public void preVisit(ASTNode node) {
		int nodeStartPosition = node.getStartPosition();
		int nodeEndPosition = nodeStartPosition + node.getLength();

		if (compilationUnit.getLineNumber(nodeStartPosition) == startLine && compilationUnit.getColumnNumber(nodeStartPosition) + 1 >= startPos
				&& compilationUnit.getLineNumber(nodeEndPosition) == endLine && compilationUnit.getColumnNumber(nodeEndPosition) - 1 <= endPos) {
			
			this.foundNodes.add(node);
		}
	}

	public Collection<ASTNode> getNodes() {
		return this.foundNodes;
	}
}
