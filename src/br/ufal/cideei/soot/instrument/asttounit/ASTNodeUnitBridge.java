package br.ufal.cideei.soot.instrument.asttounit;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import soot.Body;
import soot.Unit;
import soot.tagkit.Host;
import soot.tagkit.SourceLnPosTag;
import soot.util.Chain;

// TODO: Auto-generated Javadoc
/**
 * The Class ASTNodeUnitBridge is a utility class used to map between ASTNodes
 * and Units.
 */
public class ASTNodeUnitBridge {

	/**
	 * This is a utility class with only static methods. There's no need for a
	 * constructor.
	 */
	private ASTNodeUnitBridge() {
	}
	
	/**
	 * Gets the ASTNodes from a unit. This transition takes into consideration
	 * the position of unit in the source code. The SourceLnPosTag must be
	 * present in this unit or an {@link IllegalArgumentException} will be
	 * thrown.
	 * 
	 * @param unit
	 *            the unit
	 * @param compilationUnit
	 *            the compilation unit
	 * @return the aST nodes from unit
	 */
	public static Collection<ASTNode> getASTNodesFromUnit(Host host, CompilationUnit compilationUnit) {
		ASTNodesAtRangeFinder ASTNodeVisitor;
		if (!host.hasTag("SourceLnPosTag")) {
			throw new IllegalArgumentException("No SourceLnPosTag tag present on host");
		}
		try {
			ASTNodeVisitor = new ASTNodesAtRangeFinder((SourceLnPosTag)host.getTag("SourceLnPosTag"), compilationUnit);
		} catch (IllegalArgumentException ex) {
			// TODO: treat exception correctly
			return Collections.emptyList();
		} 
		compilationUnit.accept(ASTNodeVisitor);
		return ASTNodeVisitor.getNodes();
	}

	/**
	 * Gets the ASTNodes from units. This transition ONLY takes into
	 * consideration the starting line of the Units. If more precision is
	 * necessary, see
	 * {@link ASTNodeUnitBridge#getASTNodesFromUnit(Unit, CompilationUnit)}.
	 * 
	 * @param units
	 *            the units
	 * @param compilationUnit
	 *            the compilation unit
	 * @return the aST nodes from units
	 * 
	 * @see
	 */
	public static Collection<ASTNode> getASTNodesFromUnits(Collection<Unit> units, CompilationUnit compilationUnit) {
		return ASTNodeUnitBridge.getASTNodesFromLines(ASTNodeUnitBridge.getLinesFromUnits(units), compilationUnit);
	}

	/**
	 * Gets the ASTNodes from lines.
	 * 
	 * @param lines
	 *            the lines
	 * @param compilationUnit
	 *            the compilation unit
	 * @return the aST nodes from lines
	 */
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

	/**
	 * Gets the lines from units.
	 * 
	 * @param units
	 *            the units
	 * @return the lines from units
	 */
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
