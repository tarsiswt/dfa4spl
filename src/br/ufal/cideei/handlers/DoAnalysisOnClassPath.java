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

//#ifdef LAZY
//@import br.ufal.cideei.soot.analyses.wholeline.WholeLineLazyReachingDefinitions;
//@import br.ufal.cideei.soot.analyses.wholeline.WholeLineLazyUninitializedVariables;
//@
//#else

import br.ufal.cideei.soot.analyses.wholeline.WholeLineLiftedReachingDefinitions;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineLiftedUninitializedVariableAnalysis;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineRunnerReachingDefinitions;
import br.ufal.cideei.soot.analyses.wholeline.WholeLineRunnerUninitializedVariable;

//#endif

//#ifdef METRICS
import br.ufal.cideei.soot.count.AssignmentsCounter;
import br.ufal.cideei.soot.count.FeatureSensitiveEstimative;
import br.ufal.cideei.soot.count.LocalCounter;
import br.ufal.cideei.util.count.MetricsSink;
import br.ufal.cideei.util.count.MetricsTable;

//#endif

import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;

//TODO: this class is very similar to DoFeatureObliviousAnalysisOnClassPath. Check for common funcionality and for code reuse opportunities.
/**
 * Invokes feature-sensitive analyses on a Eclipse project. Mainly for collecting data/metrics.
 * 
 * @author T�rsis
 */
public class DoAnalysisOnClassPath extends AbstractHandler {
	// #ifdef METRICS
	private static MetricsSink sink;

	// #endif

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//#ifdef CACHEPURGE
		br.Main.randomLong();
		//#endif
		
		// TODO: exteriorize this number as a configuration parameter. Abstract away the looping.
		int times = 10;
		try {
			for (int i = 0; i < times; i++) {
				// #ifdef METRICS
				sink = new MetricsSink(new MetricsTable(new File(System.getProperty("user.home") + File.separator + "fs.xls")));
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
					 * To build the path string variable that will represent Soot's classpath we will first iterate
					 * through all libs (.jars) files, then through all source classpaths.
					 * 
					 * FIXME: WARNING: A bug was found on Soot, in which the FileSourceTag would contain incorrect
					 * information regarding the absolute location of the source file. In this workaround, the classpath
					 * must be injected into the FeatureModelInstrumentorTransformer class (done through its
					 * constructor).
					 * 
					 * As a consequence, we CANNOT build an string with all classpaths that contains source code for the
					 * project and thus one only source code classpath can be analysed at a given time.
					 * 
					 * This seriously restricts the range of projects that can be analysed with this tool.
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
				// Resets SOOT
				SootManager.reset();

				/*
				 * terminate the Metrics Facade. This dumps all the in-memory information.
				 */
				// #ifdef METRICS
				sink.terminate();
				sink = null;
				// #endif
				System.out.println("=============" + (i + 1) + "/" + times + "=============");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			SootManager.reset();
			// #ifdef METRICS
			sink.terminate();
			// #endif
		}

		return null;
	}

	/**
	 * Configures the classpath, sets up the transformers, load (jimplify) classes and run the packs.
	 * 
	 * @param javaProject
	 * @param entry
	 * @param libs
	 */
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
		
		System.out.println(classPath);

		IFeatureExtracter extracter = CIDEFeatureExtracterFactory.getInstance().getExtracter();
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
					SootManager.loadAndSupport(qualifiedNameStrBuilder.toString());
				}
			}
		}

		Scene.v().loadNecessaryClasses();

		addPacks(classPath, extracter);

		SootManager.runPacks(extracter);
	}

	private void addPacks(String classPath, IFeatureExtracter extracter) {
		Transform instrumentation = new Transform("jtp.fminst", new FeatureModelInstrumentorTransformer(extracter, classPath)
		// #ifdef METRICS
				.setMetricsSink(sink)
		// #endif
		);
		PackManager.v().getPack("jtp").add(instrumentation);

		// #ifdef LAZY
		// @ Transform reachingDefLazy = new Transform("jap.rdlazy", WholeLineLazyReachingDefinitions.v()
		// #ifdef METRICS
		// @ .setMetricsSink(sink)
		// #endif
		// @ );
		// @ PackManager.v().getPack("jap").add(reachingDefLazy);
		// @
		// @ Transform uninitVarsLazy = new Transform("jap.uninitvarlazy", WholeLineLazyUninitializedVariables.v()
		// #ifdef METRICS
		// @ .setMetricsSink(sink)
		// #endif
		// @ );
		// @ PackManager.v().getPack("jap").add(uninitVarsLazy);
		// @
		// #else

		Transform reachingDefRunner = new Transform("jap.rdrunner", WholeLineRunnerReachingDefinitions.v()
		// #ifdef METRICS
				.setMetricsSink(sink)
		// #endif
		);
		PackManager.v().getPack("jap").add(reachingDefRunner);

		Transform reachingDefLifted = new Transform("jap.rdlifted", new WholeLineLiftedReachingDefinitions()
		// #ifdef METRICS
				.setMetricsSink(sink)
		// #endif
		);
		PackManager.v().getPack("jap").add(reachingDefLifted);

		Transform uninitVarsLifted = new Transform("jap.uninitvarlifted", new WholeLineLiftedUninitializedVariableAnalysis()
		// #ifdef METRICS
				.setMetricsSink(sink)
		// #endif
		);
		PackManager.v().getPack("jap").add(uninitVarsLifted);

		Transform uninitVarsRunner = new Transform("jap.uninitvarrunner", WholeLineRunnerUninitializedVariable.v()
		// #ifdef METRICS
				.setMetricsSink(sink)
		// #endif
		);
		PackManager.v().getPack("jap").add(uninitVarsRunner);

		// #endif

		// #ifdef METRICS
		Transform assignmentsCounter = new Transform("jap.counter.assgnmt", new AssignmentsCounter(sink, true));
		PackManager.v().getPack("jap").add(assignmentsCounter);

		Transform localCounter = new Transform("jap.counter.local", new LocalCounter(sink, true));
		PackManager.v().getPack("jap").add(localCounter);

		Transform estimativeCounter = new Transform("jap.counter.estimative", new FeatureSensitiveEstimative(sink));
		PackManager.v().getPack("jap").add(estimativeCounter);
		// #endif
	}
}
