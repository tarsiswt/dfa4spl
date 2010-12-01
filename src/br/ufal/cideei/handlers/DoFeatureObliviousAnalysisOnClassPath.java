package br.ufal.cideei.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.soot.SootManager;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineSimpleReachingDefinitionsAnalysis;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineSimpleUninitializedVariablesAnalysis;
import br.ufal.cideei.util.ExecutionResultWrapper;

public class DoFeatureObliviousAnalysisOnClassPath extends AbstractHandler {
	private static ExecutionResultWrapper<Double> jimplificationResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> simpleRDResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> simpleUVResults = new ExecutionResultWrapper<Double>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		int times = 10;
		try {
			for (int i = 0; i < times; i++) {
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

					/*
					 * To build the path string variable that will represent
					 * Soot's classpath we will first iterate through all libs
					 * (.jars) files, then through all source classpaths.
					 * 
					 * FIXME: WARNING: A bug was found on Soot, in which the
					 * FileSourceTag would contain incorrect information
					 * regarding the absolute localtion of the source file. In
					 * order to workaround this, the classpath must be injected
					 * into the FeatureModelInstrumentorTransformer class (it is
					 * done though its constructor).
					 * 
					 * As a consequence, we CANNOT build an string with all
					 * classpaths that contains source code for the project and
					 * thus one only source code classpath can be analysed at a
					 * given time.
					 * 
					 * This seriously restricts the range of projects that can
					 * be analysed with this tool.
					 */
					StringBuilder libsPaths = new StringBuilder();
					for (IClasspathEntry entry : classPathEntries) {
						if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
							File file = entry.getPath().makeAbsolute().toFile();
							if (file.isAbsolute()) {
								libsPaths.append(file.getAbsolutePath() + File.pathSeparator);
							} else {
								libsPaths.append(ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath()).getLocation().toOSString()
										+ File.pathSeparator);
							}
						}
					}
					for (IClasspathEntry entry : classPathEntries) {
						if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
							this.addPacks(javaProject, entry, libsPaths.toString());
						}
					}
				}
				G.reset();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			G.reset();
		}
		String format = "|%1$-50s|%2$-80s|\n";
		// System.out.format(format, "TOTAL/" + times +": Lifted"
		// ,DoAnalysisOnClassPath.totalRDLiftedTime);
		// System.out.format(format, "TOTAL/" + times +": Runner"
		// ,DoAnalysisOnClassPath.totalRDRunnerTime);
		// System.out.format(format, "TOTAL: Runner/Lifted"
		// ,DoAnalysisOnClassPath.totalRDRunnerTime/DoAnalysisOnClassPath.totalRDLiftedTime);
		try {

			System.out.format(format, "[JIMPLFCTN] results: ", jimplificationResults.toString());
			System.out.format(format, "[RD-SIMPLE] results: ", simpleRDResults.toString());
			System.out.format(format, "[UV-SIMPLE] results: ", simpleUVResults.toString());
			
			jimplificationResults = new ExecutionResultWrapper<Double>();
			simpleRDResults = new ExecutionResultWrapper<Double>();
			simpleUVResults = new ExecutionResultWrapper<Double>();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

		// #ifdef METRICS
		long startJimplification = System.nanoTime();
		// #endif

		SootManager.configure(classPath + File.pathSeparator + libs);

		IFeatureExtracter extracter = CIDEFeatureExtracterFactory.getInstance().newExtracter(javaProject);
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
					SootManager.loadAndSupport(qualifiedNameStrBuilder.toString());
				}
			}
		}
		Scene.v().loadNecessaryClasses();

		// #ifdef METRICS
		long endJimplification = System.nanoTime();
		// #endif

		Transform reachingDef = new Transform("jap.simplerd", WholeLineSimpleReachingDefinitionsAnalysis.v());
		PackManager.v().getPack("jap").add(reachingDef);

		Transform uninitVars = new Transform("jap.simpleuv", WholeLineSimpleUninitializedVariablesAnalysis.v());
		PackManager.v().getPack("jap").add(uninitVars);
		
		SootManager.runPacks(extracter);	
		
		double simpleRDTime = ((double) WholeLineSimpleReachingDefinitionsAnalysis.v().getAnalysesTime()) / 1000000;
		double simpleUVTime = ((double) WholeLineSimpleUninitializedVariablesAnalysis.v().getAnalysesTime()) / 1000000;
		
		DoFeatureObliviousAnalysisOnClassPath.jimplificationResults.add(((double) (endJimplification - startJimplification)) / 1000000);
		DoFeatureObliviousAnalysisOnClassPath.simpleRDResults.add(simpleRDTime);
		DoFeatureObliviousAnalysisOnClassPath.simpleUVResults.add(simpleUVTime);
		
		WholeLineSimpleReachingDefinitionsAnalysis.v().reset();
		WholeLineSimpleUninitializedVariablesAnalysis.v().reset();
	}
}
