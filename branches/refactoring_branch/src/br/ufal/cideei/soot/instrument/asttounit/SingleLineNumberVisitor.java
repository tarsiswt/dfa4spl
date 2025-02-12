/*
 * This is a prototype implementation of the concept of Feature-Sen
 * sitive Dataflow Analysis. More details in the AOSD'12 paper:
 * Dataflow Analysis for Software Product Lines
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package br.ufal.cideei.soot.instrument.asttounit;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class SingleLineNumberVisitor extends ASTVisitor {

	private CompilationUnit compilationUnit;
	private int line;
	private Set<ASTNode> nodes = new HashSet<ASTNode>();
	
	public Set<ASTNode> getNodes(){
		return nodes;
	}

	public SingleLineNumberVisitor(int line, CompilationUnit compilationUnit) {
		super();
		this.line = line;
		this.compilationUnit = compilationUnit;
	}

	public boolean visit(ASTNode node) {
		if (compilationUnit.getLineNumber(node.getStartPosition()) == line) {
			nodes.add(node);
		}
		return true;
	}
}
