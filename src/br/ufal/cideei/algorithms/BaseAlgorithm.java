package br.ufal.cideei.algorithms;

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
import org.eclipse.jdt.core.dom.MethodDeclaration;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseAlgorithm.
 */
public abstract class BaseAlgorithm implements IAlgorithm {

	/**
	 * Gets the parent method.
	 * 
	 * @param node
	 *            the node
	 * @return the parent method
	 */
	protected MethodDeclaration getParentMethod(ASTNode node) {
		if (node == null) {
			return null;
		} else {
			if (node.getNodeType() == ASTNode.METHOD_DECLARATION) {
				return (MethodDeclaration) node;
			} else {
				return getParentMethod(node.getParent());
			}
		}
	}


	/**
	 * Gets the correspondent classpath.
	 * 
	 * @param file
	 *            the file
	 * @return the correspondent classpath
	 * @throws ExecutionException
	 *             the execution exception
	 */
	protected String getCorrespondentClasspath(IFile file) throws ExecutionException {
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
