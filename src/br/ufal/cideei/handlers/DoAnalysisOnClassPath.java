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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.soot.SootManager;
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

				IFeatureExtracter extracter = CIDEFeatureExtracterFactory.getInstance().newExtracter();
				for (IClasspathEntry entry : classPathEntries) {
					if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
						continue;
					}

					String classPath = ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath()).getLocation().toOSString();
					SootManager.configure(classPath);

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
								System.out.println("Compilation unit:" + classPath + File.separator + qualifiedNameStrBuilder.toString() + " RLEVEL: "
										+ sootClass.resolvingLevel());
							}
						}
					}
					Scene.v().loadNecessaryClasses();
					Transform t = new Transform("tag.fminst", FeatureModelInstrumentorTransformer.v(extracter));
					PackManager.v().getPack("tag").add(t);
					SootManager.runPacks(extracter);
					break;
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
}
