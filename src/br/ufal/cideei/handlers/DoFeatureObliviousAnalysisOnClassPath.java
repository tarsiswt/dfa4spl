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
import br.ufal.cideei.soot.analyses.wholeline.WholeLineObliviousReachingDefinitionsAnalysis;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineObliviousUninitializedVariablesAnalysis;
import br.ufal.cideei.soot.count.AssignmentsCounter;
import br.ufal.cideei.soot.count.BodyCounter;
import br.ufal.cideei.soot.count.FeatureObliviousEstimative;
import br.ufal.cideei.soot.count.LocalCounter;
import br.ufal.cideei.soot.count.PreprocessingJimpleCode;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;
import br.ufal.cideei.util.ExecutionResultWrapper;

public class DoFeatureObliviousAnalysisOnClassPath extends AbstractHandler {
	private static ExecutionResultWrapper<Double> jimplificationResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> simpleRDResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> simpleUVResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> preprocessingResults = new ExecutionResultWrapper<Double>();

	private Long assignmentsCount;
	private Long bodyCount;
	private Long localCount;

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
					 * To build the path string variable that will represent Soot's
					 * classpath we will first iterate through all libs (.jars)
					 * files, then through all source classpaths.
					 * 
					 * FIXME: WARNING: A bug was found on Soot, in which the
					 * FileSourceTag would contain incorrect information regarding
					 * the absolute localtion of the source file. In order to
					 * workaround this, the classpath must be injected into the
					 * FeatureModelInstrumentorTransformer class (it is done though
					 * its constructor).
					 * 
					 * As a consequence, we CANNOT build an string with all
					 * classpaths that contains source code for the project and thus
					 * one only source code classpath can be analysed at a given
					 * time.
					 * 
					 * This seriously restricts the range of projects that can be
					 * analysed with this tool.
					 */
					StringBuilder libsPaths = new StringBuilder();
					for (IClasspathEntry entry : classPathEntries) {
						if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
							File file = entry.getPath().makeAbsolute().toFile();
							if (file.isAbsolute()) {
								libsPaths.append(file.getAbsolutePath() + File.pathSeparator);
							} else {
								libsPaths.append(ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath()).getLocation().toOSString() + File.pathSeparator);
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
				System.out.println("=============" + (i+1) + "/" + times + "=============");
			}
		} catch (Throwable e) {
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
			System.out.format(format, "[PREPROCES] results: ", preprocessingResults.toString());

			System.out.format(format, "[ASSGNMNT-COUNT] results: ", assignmentsCount);
			System.out.format(format, "[BODY-COUNT] results: ", bodyCount);
			System.out.format(format, "[LOCAL-COUNT] results: ", localCount);

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

		Transform instrumentation = new Transform("jtp.fminst", FeatureModelInstrumentorTransformer.v(extracter, classPath));
		PackManager.v().getPack("jtp").add(instrumentation);

		Transform reachingDef = new Transform("jap.simplerd", WholeLineObliviousReachingDefinitionsAnalysis.v());
		PackManager.v().getPack("jap").add(reachingDef);

		Transform uninitVars = new Transform("jap.simpleuv", WholeLineObliviousUninitializedVariablesAnalysis.v());
		PackManager.v().getPack("jap").add(uninitVars);

		//#ifdef METRICS
		Transform assignmentsCounter = new Transform("jap.counter.assgnmt", AssignmentsCounter.v());
		PackManager.v().getPack("jap").add(assignmentsCounter);

		Transform bodyCounter = new Transform("jap.counter.body", BodyCounter.v());
		PackManager.v().getPack("jap").add(bodyCounter);

		Transform localCounter = new Transform("jap.counter.local", LocalCounter.v());
		PackManager.v().getPack("jap").add(localCounter);

		Transform preprocessingCounter = new Transform("jap.counter.preprocessing", PreprocessingJimpleCode.v());
		PackManager.v().getPack("jap").add(preprocessingCounter);
		
		Transform estimativeCounter = new Transform("jap.counter.estimative", FeatureObliviousEstimative.v());
		PackManager.v().getPack("jap").add(estimativeCounter);
		//#endif
		
		SootManager.runPacks(extracter);

		//#ifdef METRICS
		double simpleRDTime = ((double) FeatureObliviousEstimative.v().getRdTotal()) / 1000000;
		double simpleUVTime = ((double) FeatureObliviousEstimative.v().getUvTotal()) / 1000000;
		double jimplificationTime = ((double) FeatureObliviousEstimative.v().getJimplificationTotal()) / 1000000;
		double preprocessingTime = ((double) FeatureObliviousEstimative.v().getPreprocessingTotal()) / 1000000;
		
		DoFeatureObliviousAnalysisOnClassPath.simpleRDResults.add(simpleRDTime);
		DoFeatureObliviousAnalysisOnClassPath.simpleUVResults.add(simpleUVTime);
		DoFeatureObliviousAnalysisOnClassPath.jimplificationResults.add(jimplificationTime);
		DoFeatureObliviousAnalysisOnClassPath.preprocessingResults.add(preprocessingTime);
		
		assignmentsCount = AssignmentsCounter.v().getCount();
		bodyCount = BodyCounter.v().getCount();
		localCount = LocalCounter.v().getCount();

		AssignmentsCounter.v().reset();
		BodyCounter.v().reset();
		LocalCounter.v().reset();
		FeatureObliviousEstimative.v().reset();
		//#endif
	}

}