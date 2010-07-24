package br.ufal.cideei.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import soot.Body;
import soot.Unit;
import soot.tagkit.SourceLnPosTag;
import soot.util.Chain;

public abstract class BaseSootAlgorithm extends BaseAlgorithm {
	
	public abstract void executeWithSoot(IFile file) throws ExecutionException;

	protected Collection<Integer> getLinesFromASTNodes(Collection<ASTNode> nodes, CompilationUnit compilationUnit) {
		Set<Integer> lineSet = new HashSet<Integer>();
		for (ASTNode node : nodes) {
			lineSet.add(compilationUnit.getLineNumber(node.getStartPosition()));
		}
		return lineSet;
	}

	protected Integer getLineFromUnit(Unit unit) {
		if (unit.hasTag("SourceLnPosTag")) {
			SourceLnPosTag lineTag = (SourceLnPosTag) unit.getTag("SourceLnPosTag");
			return lineTag.startLn();
		}
		return null;
	}

	protected Collection<Unit> getUnitsFromLines(Collection<Integer> lines, Body body) {
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
	
	protected String getCorrespondentClasspath(IFile file) throws ExecutionException{
		/*
		 * used to find out what the classpath entry related to the IFile of the
		 * text selection. this is necessary for some algorithms that might use
		 * the Soot framework
		 */
		IProject project = file.getProject();
		IJavaProject javaProject = null;

		try {
			if (file.getProject().isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				javaProject = JavaCore.create(project);
			}
		} catch (CoreException e) {
			e.printStackTrace();
			throw new ExecutionException("Not a Java Project");
		}

		/*
		 * When using the Soot framework, we need the path to the package root
		 * in which the file is located. There may be other ways to acomplish
		 * this. TODO look for optimal way of finding it.
		 */

		String pathToSourceClasspathEntry = null;

		IClasspathEntry[] classPathEntries = null;
		try {
			classPathEntries = javaProject.getResolvedClasspath(true);
			for (IClasspathEntry entry : classPathEntries) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					pathToSourceClasspathEntry = ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath()).getLocation().toOSString();
					break;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException("No source classpath identified");
		}
		return pathToSourceClasspathEntry;
	}
}
