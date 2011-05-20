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
import br.ufal.cideei.soot.analyses.FeatureSensitiveFowardFlowAnalysis;
import br.ufal.cideei.soot.analyses.reachingdefs.LiftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineLiftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineLiftedUninitializedVariableAnalysis;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineRunnerReachingDefinitions;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineRunnerUninitializedVariable;
import br.ufal.cideei.soot.count.AssignmentsCounter;
import br.ufal.cideei.soot.count.ColoredBodyCounter;
import br.ufal.cideei.soot.count.FeatureSensitiveEstimative;
import br.ufal.cideei.soot.count.LocalCounter;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;
import br.ufal.cideei.soot.instrument.LineNumberColorMapper;
import br.ufal.cideei.util.ExecutionResultWrapper;

public class DoAnalysisOnClassPath extends AbstractHandler {
	private static double totalRDRunnerTime;
	private static double totalRDLiftedTime;
	private static double totalUVRunnerTime;
	private static double totalUVLiftedTime;
	private static ExecutionResultWrapper<Double> rdRunnerResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> uvRunnerResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> rdLiftedResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> uvLiftedResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> instrumentationResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> parsingTimeResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> colorLookupTableBuildingTimeResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> CIDEExtractingTime = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Double> jimplificationResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Long> coloredBodyResults = new ExecutionResultWrapper<Long>();

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
					 * regarding the absolute location of the source file. In
					 * this workaround, the classpath must be injected
					 * into the FeatureModelInstrumentorTransformer class (done though its constructor).
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
				System.out.println("=============" + (i+1) + "/" + times + "=============");
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
			System.out.format(format, "[RD-LIFTED] results: ", rdLiftedResults.toString());
			System.out.format(format, "[RD-RUNNER] results: ", rdRunnerResults.toString());
			System.out.format(format, "[UV-LIFTED] results: ", uvLiftedResults.toString());
			System.out.format(format, "[UV-RUNNER] results: ", uvRunnerResults.toString());
			System.out.format(format, "[INSTRUMNT] results: ", instrumentationResults.toString());
			System.out.format(format, "[COLORTABL] results: ", colorLookupTableBuildingTimeResults.toString());
			System.out.format(format, "[CIDEEXTRC] results: ", CIDEExtractingTime.toString());
			System.out.format(format, "[JIMPLFCTN] results: ", jimplificationResults.toString());

			rdLiftedResults = new ExecutionResultWrapper<Double>();
			rdRunnerResults = new ExecutionResultWrapper<Double>();
			uvLiftedResults = new ExecutionResultWrapper<Double>();
			uvRunnerResults = new ExecutionResultWrapper<Double>();
			instrumentationResults = new ExecutionResultWrapper<Double>();
			jimplificationResults = new ExecutionResultWrapper<Double>();
			coloredBodyResults = new ExecutionResultWrapper<Long>();
			parsingTimeResults = new ExecutionResultWrapper<Double>();
			colorLookupTableBuildingTimeResults = new ExecutionResultWrapper<Double>();
			CIDEExtractingTime = new ExecutionResultWrapper<Double>();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FeatureSensitiveEstimative.v().closeMetricsFile();
		
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

		Transform reachingDefRunner = new Transform("jap.rdrunner", WholeLineRunnerReachingDefinitions.v());
		PackManager.v().getPack("jap").add(reachingDefRunner);

		Transform reachingDefLifted = new Transform("jap.rdlifted", WholeLineLiftedReachingDefinitions.v());
		PackManager.v().getPack("jap").add(reachingDefLifted);

		Transform uninitVarsLifted = new Transform("jap.uninitvarlifted", WholeLineLiftedUninitializedVariableAnalysis.v());
		PackManager.v().getPack("jap").add(uninitVarsLifted);

		Transform uninitVarsRunner = new Transform("jap.uninitvarrunner", WholeLineRunnerUninitializedVariable.v());
		PackManager.v().getPack("jap").add(uninitVarsRunner);

		// #ifdef METRICS
		Transform assignmentsCounter = new Transform("jap.counter.assgnmt", AssignmentsCounter.v());
		PackManager.v().getPack("jap").add(assignmentsCounter);

		Transform cBodyCounter = new Transform("jap.counter.coloredbody", ColoredBodyCounter.v());
		PackManager.v().getPack("jap").add(cBodyCounter);

		Transform localCounter = new Transform("jap.counter.local", LocalCounter.v());
		PackManager.v().getPack("jap").add(localCounter);
		
		Transform estimativeCounter = new Transform("jap.counter.estimative", FeatureSensitiveEstimative.v());
		PackManager.v().getPack("jap").add(estimativeCounter);
		// #endif

		SootManager.runPacks(extracter);

		// #ifdef METRICS
		String format = "|%1$-50s|%2$-50s|\n";
		
		double rdLiftedTime = ((double) FeatureSensitiveEstimative.v().getRdTotal2()) / 1000000;
		double rdRunnerTime = ((double) FeatureSensitiveEstimative.v().getRdTotal()) / 1000000;
		double uvLiftedTime = ((double) FeatureSensitiveEstimative.v().getUvTotal2()) / 1000000;
		double uvRunnerTime = ((double) FeatureSensitiveEstimative.v().getUvTotal()) / 1000000;
		double jimplificationTime = ((double) FeatureSensitiveEstimative.v().getJimplificationTotal()) / 1000000;
		
		double instrumentationTime = ((double) FeatureModelInstrumentorTransformer.getTransformationTime()) / 1000000;
		double parsingTime = ((double) FeatureModelInstrumentorTransformer.getParsingTime()) / 1000000;
		double colorLookupTableBuildingTime = ((double) FeatureModelInstrumentorTransformer.getColorLookupTableBuildingTime()) / 1000000;
		double CIDEExtractingTime = ((double) LineNumberColorMapper.getExtractTime()) / 1000000;
		
		DoAnalysisOnClassPath.rdLiftedResults.add(rdLiftedTime);
		DoAnalysisOnClassPath.rdRunnerResults.add(rdRunnerTime);
		DoAnalysisOnClassPath.uvLiftedResults.add(uvLiftedTime);
		DoAnalysisOnClassPath.uvRunnerResults.add(uvRunnerTime);
		DoAnalysisOnClassPath.instrumentationResults.add(instrumentationTime);
		DoAnalysisOnClassPath.jimplificationResults.add(jimplificationTime);
		DoAnalysisOnClassPath.coloredBodyResults.add(ColoredBodyCounter.v().getCount());
		DoAnalysisOnClassPath.parsingTimeResults.add(parsingTime);
		DoAnalysisOnClassPath.colorLookupTableBuildingTimeResults.add(colorLookupTableBuildingTime);
		DoAnalysisOnClassPath.CIDEExtractingTime.add(CIDEExtractingTime);
		DoAnalysisOnClassPath.totalRDRunnerTime += rdRunnerTime;
		DoAnalysisOnClassPath.totalRDLiftedTime += rdLiftedTime;
		DoAnalysisOnClassPath.totalUVRunnerTime += uvRunnerTime;
		DoAnalysisOnClassPath.totalUVLiftedTime += uvLiftedTime;

		FeatureSensitiveEstimative.v().reset();
		FeatureModelInstrumentorTransformer.v().reset();
		AssignmentsCounter.v().reset();
		LocalCounter.v().reset();
		FeatureSensitiveFowardFlowAnalysis.reset();
		LiftedReachingDefinitions.reset();
		ColoredBodyCounter.v().reset();
		LineNumberColorMapper.reset();
		// #endif
	}
}
