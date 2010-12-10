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
import br.ufal.cideei.soot.analyses.FeatureSensitiviteFowardFlowAnalysis;
import br.ufal.cideei.soot.analyses.reachingdefs.LiftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineLiftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineLiftedUninitializedVariableAnalysis;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineRunnerReachingDefinitions;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineRunnerUninitializedVariable;
import br.ufal.cideei.soot.count.AssignmentsCounter;
import br.ufal.cideei.soot.count.ColoredBodyCounter;
import br.ufal.cideei.soot.count.LocalCounter;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;
import br.ufal.cideei.util.ExecutionResultWrapper;
import br.ufal.cideei.util.MethodDeclarationSootMethodBridge;
import br.ufal.cideei.util.WriterFacadeForAnalysingMM;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;

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
	private static ExecutionResultWrapper<Double> jimplificationResults = new ExecutionResultWrapper<Double>();
	private static ExecutionResultWrapper<Long> coloredBodyResults = new ExecutionResultWrapper<Long>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		int times = 10;
		try {
			for (int i = 0; i < times; i++) {
				// #ifdef METRICS
				try {
					WriterFacadeForAnalysingMM.renew();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// #endif

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
			// #ifdef METRICS
			try {
				WriterFacadeForAnalysingMM.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// #endif
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
			System.out.format(format, "[JIMPLFCTN] results: ", jimplificationResults.toString());
			System.out.format(format, "[COLORBODY] results: ", ColoredBodyCounter.v().toString());

			rdLiftedResults = new ExecutionResultWrapper<Double>();
			rdRunnerResults = new ExecutionResultWrapper<Double>();
			uvLiftedResults = new ExecutionResultWrapper<Double>();
			uvRunnerResults = new ExecutionResultWrapper<Double>();
			instrumentationResults = new ExecutionResultWrapper<Double>();
			jimplificationResults = new ExecutionResultWrapper<Double>();
			coloredBodyResults = new ExecutionResultWrapper<Long>();

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

		// #endif

		SootManager.runPacks(extracter);

		// #ifdef METRICS
		String format = "|%1$-50s|%2$-50s|\n";
		double rdRunnerTime = ((double) WholeLineRunnerReachingDefinitions.v().getAnalysesTime()) / 1000000;
		double rdLiftedTime = ((double) WholeLineLiftedReachingDefinitions.v().getAnalysesTime()) / 1000000;

		double uvRunnerTime = ((double) WholeLineRunnerUninitializedVariable.v().getAnalysesTime()) / 1000000;
		double uvLiftedTime = ((double) WholeLineLiftedUninitializedVariableAnalysis.v().getAnalysesTime()) / 1000000;

		double instrumentationTime = ((double) FeatureModelInstrumentorTransformer.getTransformationTime()) / 1000000;

		DoAnalysisOnClassPath.rdLiftedResults.add(rdLiftedTime);
		DoAnalysisOnClassPath.rdRunnerResults.add(rdRunnerTime);
		DoAnalysisOnClassPath.uvLiftedResults.add(uvLiftedTime);
		DoAnalysisOnClassPath.uvRunnerResults.add(uvRunnerTime);
		DoAnalysisOnClassPath.instrumentationResults.add(instrumentationTime);
		DoAnalysisOnClassPath.jimplificationResults.add(((double) (endJimplification - startJimplification)) / 1000000);
		coloredBodyResults.add(ColoredBodyCounter.v().getCount());

		DoAnalysisOnClassPath.totalRDRunnerTime += rdRunnerTime;
		DoAnalysisOnClassPath.totalRDLiftedTime += rdLiftedTime;
		DoAnalysisOnClassPath.totalUVRunnerTime += uvRunnerTime;
		DoAnalysisOnClassPath.totalUVLiftedTime += uvLiftedTime;

		// System.out.format(format, "[RD]runner took:", rdRunnerTime + "ms");
		// System.out.format(format, "[RD]lifted took:", rdLiftedTime + "ms");
		// System.out.format(format, "[RD]runner/lifted:",
		// DoAnalysisOnClassPath.totalRDRunnerTime /
		// DoAnalysisOnClassPath.totalRDLiftedTime);

		long runnerFlowThroughCounter = FeatureSensitiviteFowardFlowAnalysis.getFlowThroughCounter();
		// System.out.format(format, "Runner no. of flowThroughs called: ",
		// runnerFlowThroughCounter);
		long liftedFlowThroughCounter = LiftedReachingDefinitions.getFlowThroughCounter();
		// System.out.format(format, "Lifted no. of flowThroughs called: ",
		// liftedFlowThroughCounter);

		long totalBodies = FeatureModelInstrumentorTransformer.getTotalBodies();
		long coloredBodies = FeatureModelInstrumentorTransformer.getTotalColoredBodies();

		// System.out.format(format, "Total bodies: ", totalBodies);
		// System.out.format(format, "Bodies with at least 1 ft.: ",
		// coloredBodies);
		// System.out.format(format, "Percentage: ", ((((double) coloredBodies)
		// / ((double) (totalBodies))) * 100) + "%");
		// System.out.format(format, "Total of assignments: ",
		// AssignmentsCounter.v().getCounter());
		// System.out.format(format, "Average assignments/bodies: ",
		// AssignmentsCounter.v().getCounter() / totalBodies);

		WholeLineLiftedReachingDefinitions.v().reset();
		WholeLineRunnerReachingDefinitions.v().reset();
		WholeLineRunnerUninitializedVariable.v().reset();
		WholeLineLiftedUninitializedVariableAnalysis.v().reset();
		FeatureModelInstrumentorTransformer.v().reset();
		AssignmentsCounter.v().reset();
		LocalCounter.v().reset();
		FeatureSensitiviteFowardFlowAnalysis.reset();
		LiftedReachingDefinitions.reset();
		ColoredBodyCounter.v().reset();
		// #endif
	}
}
