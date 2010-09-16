package br.ufal.cideei.soot.instrument;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import soot.Body;
import soot.Unit;
import soot.tagkit.SourceLnPosTag;
import soot.util.Chain;

public class ASTNodeUnitBridge {

	private ASTNodeUnitBridge() {
	}
	
	public static Collection<ASTNode> getASTNodesFromUnit(Unit unit, CompilationUnit compilationUnit) {
		ASTNodesAtRangeFinder ASTNodeVisitor = new ASTNodesAtRangeFinder(unit, compilationUnit);
		compilationUnit.accept(ASTNodeVisitor);
		return ASTNodeVisitor.getNodes();
	}

	public static Collection<ASTNode> getASTNodesFromUnits(Collection<Unit> units, CompilationUnit compilationUnit) {
		return ASTNodeUnitBridge.getASTNodesFromLines(ASTNodeUnitBridge.getLinesFromUnits(units), compilationUnit);
	}

	public static Collection<ASTNode> getASTNodesFromLines(Collection<Integer> lines, CompilationUnit compilationUnit) {
		MultipleLineNumbersVisitor linesVisitor = new MultipleLineNumbersVisitor(lines, compilationUnit);
		compilationUnit.accept(linesVisitor);
		return linesVisitor.getNodes();
	}

	/**
	 * Gets the lines from the AST nodes.
	 * 
	 * @param nodes
	 *            the nodes
	 * @param compilationUnit
	 *            the compilation unit
	 * @return the lines from ast nodes
	 */
	public static Collection<Integer> getLinesFromASTNodes(Collection<ASTNode> nodes, CompilationUnit compilationUnit) {
		Set<Integer> lineSet = new HashSet<Integer>();
		for (ASTNode node : nodes) {
			lineSet.add(compilationUnit.getLineNumber(node.getStartPosition()));
		}
		return lineSet;
	}

	/**
	 * Gets the line from unit.
	 * 
	 * @param unit
	 *            the unit
	 * @return the line from unit
	 */
	public static Integer getLineFromUnit(Unit unit) {
		if (unit.hasTag("SourceLnPosTag")) {
			SourceLnPosTag lineTag = (SourceLnPosTag) unit.getTag("SourceLnPosTag");
			return lineTag.startLn();
		}
		return null;
	}

	public static Collection<Integer> getLinesFromUnits(Collection<Unit> units) {
		Set<Integer> lines = new HashSet<Integer>(units.size());
		Iterator<Unit> iterator = units.iterator();
		while (iterator.hasNext()) {
			Unit unit = iterator.next();
			if (unit.hasTag("SourceLnPosTag")) {
				SourceLnPosTag lineTag = (SourceLnPosTag) unit.getTag("SourceLnPosTag");
				lines.add(lineTag.startLn());
			}
		}
		return lines;
	}

	/**
	 * Gets the units from lines.
	 * 
	 * @param lines
	 *            the lines
	 * @param body
	 *            the body
	 * @return the units from lines
	 */
	public static Collection<Unit> getUnitsFromLines(Collection<Integer> lines, Body body) {
		Set<Unit> unitSet = new HashSet<Unit>();
		for (Integer line : lines) {
			Chain<Unit> units = body.getUnits();
			for (Unit unit : units) {
				if (unit.hasTag("SourceLnPosTag")) {
					SourceLnPosTag lineTag = (SourceLnPosTag) unit.getTag("SourceLnPosTag");
					if (lineTag != null) {
						if (lineTag.startLn() == line.intValue()) {
							unitSet.add(unit);
						}
					}
				}
			}
		}
		return unitSet;
	}
}
