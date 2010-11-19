package br.ufal.cideei.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.soot.SootManager;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineLiftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineRunnerReachingDefinitions;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;
import br.ufal.cideei.util.MethodDeclarationSootMethodBridge;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;

public class DoAnalysisOnClassPath extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IJavaProject) {
				IJavaProject javaProject = (IJavaProject) firstElement;

				IClasspathEntry[] classPathEntries = null;
				try {
					classPathEntries = javaProject.getResolvedClasspath(true);
				} catch (JavaModelException e) {
					e.printStackTrace();
					throw new ExecutionException("No source classpath identified");
				}

				StringBuilder libsPaths = new StringBuilder();
				for (IClasspathEntry entry : classPathEntries) {
					if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						File file = entry.getPath().makeAbsolute().toFile();
						if (file.isAbsolute()) {
//							System.out.println(file.getAbsolutePath());
							libsPaths.append(file.getAbsolutePath() + File.pathSeparator);
						} else {
//							System.out.println(ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath()).getLocation().toOSString());
							libsPaths.append(ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath()).getLocation().toOSString() + File.pathSeparator);
						}
					}
				}
				System.out.println(libsPaths.toString());
				for (IClasspathEntry entry : classPathEntries) {
					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						this.addPacks(javaProject, entry, libsPaths.toString());
					}
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			G.v().reset();
		}
	}

	private void addPacks(IJavaProject javaProject, IClasspathEntry entry, String libs) {
		/*
		 * if the classpath entry is "", then JDT will complain about it.
		 */
		String classPath;
		if (entry.getPath().toOSString().equals(File.separator + javaProject.getElementName())) {
			classPath = javaProject.getResource().getLocation().toFile().getAbsolutePath();
		} else {
			classPath = ResourcesPlugin.getWorkspace().getRoot().getFolder(entry.getPath()).getLocation().toOSString();
		}

		SootManager.configure(classPath + File.pathSeparator + libs);

		IFeatureExtracter extracter = CIDEFeatureExtracterFactory.getInstance().newExtracter();
		IPackageFragmentRoot[] packageFragmentRoots = javaProject.findPackageFragmentRoots(entry);
		for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
			IJavaElement[] children = null;
			try {
				children = packageFragmentRoot.getChildren();
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (IJavaElement child : children) {
				IPackageFragment packageFragment = (IPackageFragment) child;
				ICompilationUnit[] compilationUnits = null;
				try {
					compilationUnits = packageFragment.getCompilationUnits();
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				for (ICompilationUnit compilationUnit : compilationUnits) {
					// CompilationUnit a = compilationUnit.
					String fragmentName = packageFragment.getElementName();
					String compilationName = compilationUnit.getElementName();
					StringBuilder qualifiedNameStrBuilder = new StringBuilder(fragmentName);
					// If it's the default package:
					if (qualifiedNameStrBuilder.length() == 0) {
						// Remove ".java" suffix
						qualifiedNameStrBuilder.append(compilationName.substring(0, compilationName.length() - 5));
					} else {
						// Remove ".java" suffix
						qualifiedNameStrBuilder.append(".").append(compilationName.substring(0, compilationName.length() - 5));
					}

					// This goes into Soot loadAndSupport
					SootClass sootClass = SootManager.loadAndSupport(qualifiedNameStrBuilder.toString());
					// System.out.println("Compilation unit:" + classPath +
					// File.separator + qualifiedNameStrBuilder.toString() +
					// " RLEVEL: "
					// + sootClass.resolvingLevel());
				}
			}
		}
		Scene.v().loadNecessaryClasses();

		Transform t = new Transform("jap.fminst", FeatureModelInstrumentorTransformer.v(extracter, classPath));
		PackManager.v().getPack("jap").add(t);

		Transform t2 = new Transform("jap.rdrunner", WholeLineRunnerReachingDefinitions.v());
		PackManager.v().getPack("jap").add(t2);

		Transform t3 = new Transform("jap.rdlifted", WholeLineLiftedReachingDefinitions.v());
		PackManager.v().getPack("jap").add(t3);

		SootManager.runPacks(extracter);

		System.out.println("runner took:" + WholeLineRunnerReachingDefinitions.v().getAnalysesTime());
		System.out.println("lifted took:" + WholeLineLiftedReachingDefinitions.v().getAnalysesTime());
	}
}
