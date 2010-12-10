package br.ufal.cideei.soot.instrument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;

import soot.Body;
import soot.BodyTransformer;
import soot.SootClass;
import soot.SourceLocator;
import soot.Unit;
import soot.javaToJimple.InitialResolver;
import soot.options.Options;
import soot.tagkit.SourceFileTag;
import soot.tagkit.Tag;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.soot.UnitUtil;
import br.ufal.cideei.soot.instrument.asttounit.ASTNodeUnitBridge;
import br.ufal.cideei.util.CachedICompilationUnitParser;
import br.ufal.cideei.util.SetUtil;
import br.ufal.cideei.util.WriterFacadeForAnalysingMM;

/**
 * The Class FeatureModelInstrumentor is a Soot transformation for transcribing
 * feature information to every Unit. This is done via Tag annonations. The
 * feature model and its format from which the information will be extracted is
 * still unkown.
 */
public class FeatureModelInstrumentorTransformer extends BodyTransformer {

	/** The singleton instance. */
	private static FeatureModelInstrumentorTransformer instance = new FeatureModelInstrumentorTransformer();
	/** Feature extracter. */
	private static IFeatureExtracter extracter;
	/** Current compilation unit the transformation is working on */
	private CompilationUnit currentCompilationUnit;
	private IFile file;
	/*
	 * XXX: Workaround for the preTransform method. See comments on
	 * #preTransform method.
	 */
	private static String classPath;
	private CachedICompilationUnitParser cachedParser = new CachedICompilationUnitParser();

	// #ifdef METRICS
	private static long totalBodies = 0;
	private static long totalColoredBodies = 0;
	private static long transformationTime = 0;

	// #endif

	/**
	 * Disable default constructor. This class is a singleton.
	 */
	private FeatureModelInstrumentorTransformer() {
	}

	/**
	 * V.
	 * 
	 * @param algorithm
	 * 
	 * @return the feature model instrumentor
	 */
	public static FeatureModelInstrumentorTransformer v(IFeatureExtracter extracter, String classPath) {
		FeatureModelInstrumentorTransformer.classPath = classPath;
		FeatureModelInstrumentorTransformer.extracter = extracter;
		return instance;
	}

	public static FeatureModelInstrumentorTransformer v() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String,
	 * java.util.Map)
	 */
	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		long startTransform = System.nanoTime();
		try {
			preTransform(body);
		} catch (IllegalStateException ex) {
			System.out.println("Skipping " + body.getMethod().getName() + " :" + ex.getMessage());
			return;
		}

		// #ifdef METRICS
		try {
			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.METHOD_COLUMN, body.getMethod().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// #endif
		
		/*
		 * XXX: variable used to compute how much time some portions of this
		 * code takes. Looking for bottlenecks
		 */
		// #ifdef METRICS_INSTRUMENTATION_BOTTLENECK
		long unitToASTNodeDelta = 0;
		// #endif

		/*
		 * The feature set and its power set will be computed during the first
		 * iteration and after the second iteration respectivelly
		 */
		Set<String> featureNameSet = new HashSet<String>();
		Set<IFeature> featureSet = new HashSet<IFeature>();
		Set<Set<String>> featureNamePowerSet = null;
		Set<Set<IFeature>> featurePowerSet = null;

		/*
		 * This is the first iteration over the units. We will find out what are
		 * the features present in this body, so that we can generate our power
		 * set. In order to optimize the code, we will also keep record of all
		 * the nodes and features of units that has at least one feature.
		 * 
		 * This triple is stored as an array of objects in this order: {Unit,
		 * ASTNodes, Features}.
		 * 
		 * The first object can be seen as the "key" of the triple. The second
		 * one is the ASTNodes reated to the Unit. The third is the features
		 * they have been marked with
		 */
		Iterator<Unit> unitIt = body.getUnits().snapshotIterator();

		/*
		 * A list of triples, in the form {Unit, ASTNodes, Features}, named UAF.
		 * Units that have at least one color attached to it will be stored in
		 * this array.
		 */
		List<Object[]> uaf = new ArrayList<Object[]>();

		/*
		 * Units that have no colors are stored in this list;
		 */
		List<Unit> colorlessUnits = new ArrayList<Unit>();

		while (unitIt.hasNext()) {
			Unit nextUnit = unitIt.next();
			Collection<ASTNode> nodesTakenFromUnit = null;
			Set<IFeature> featuresOnUnit = new HashSet<IFeature>();

			// flag used to improve code performance
			boolean alreadyAddedUnit = false;

			// TODO: how to treat this exception?
			try {
				// #ifdef METRICS_INSTRUMENTATION_BOTTLENECK
				long startUnitToASTNodeDelta = System.nanoTime();
				// #endif

				nodesTakenFromUnit = ASTNodeUnitBridge.getASTNodesFromUnit(nextUnit, this.currentCompilationUnit);

				// #ifdef METRICS_INSTRUMENTATION_BOTTLENECK
				long endUnitToASTNodeDelta = System.nanoTime();
				unitToASTNodeDelta += endUnitToASTNodeDelta - startUnitToASTNodeDelta;
				// #endif
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			Iterator<ASTNode> nodesIterator = nodesTakenFromUnit.iterator();
			while (nodesIterator.hasNext()) {
				ASTNode nextNode = nodesIterator.next();
				Set<IFeature> features = extracter.getFeatures(nextNode, this.file);

				Iterator<IFeature> nodesFeaturesIterator = features.iterator();
				while (nodesFeaturesIterator.hasNext()) {
					IFeature feature = nodesFeaturesIterator.next();
					featuresOnUnit.add(feature);
					if (!featureNameSet.contains(feature.getName())) {
						featureNameSet.add(feature.getName());
						featureSet.add(feature);
					}
				}
				if (!alreadyAddedUnit && !featuresOnUnit.isEmpty()) {
					uaf.add(new Object[] { nextUnit, nodesTakenFromUnit, featuresOnUnit });
					alreadyAddedUnit = true;
				}
			}

			/*
			 * If the unit has not been added to the uaf, then we'll keep track
			 * of them for later use
			 */
			if (!alreadyAddedUnit) {
				colorlessUnits.add(nextUnit);
			}
		}

		featurePowerSet = SetUtil.powerSet(featureSet);
		featureNamePowerSet = new HashSet<Set<String>>(featurePowerSet.size());
		for (Set<IFeature> config : featurePowerSet) {
			boolean valid = false;
			try {
				valid = extracter.isValid(config);
			} catch (FeatureModelNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (valid) {
				Set<String> strConfig = new HashSet<String>(config.size());
				for (IFeature feat : config) {
					strConfig.add(feat.getName());
				}
				featureNamePowerSet.add(strConfig);
			}
		}

		/*
		 * Now, in this second iteration, the units will be tagged with their
		 * valid configurations. Since we have separated the Units that have
		 * colors and the ones that don't, we'll iterate each of them
		 * separetely.
		 */

		/*
		 * Create a single FeatureTag that will be added to all units that have
		 * no color and the Body. This object should not be modified.
		 */
		FeatureTag<Set<String>> powerSetTag = new FeatureTag<Set<String>>();
		powerSetTag.addAll(featureNamePowerSet);

		// #ifdef METRICS
		FeatureModelInstrumentorTransformer.totalBodies++;
		// if the body has more than one color
		if (featureNamePowerSet.size() > 1) {
			FeatureModelInstrumentorTransformer.totalColoredBodies++;
		}
		// #endif

		/*
		 * All colorless units will have a full feature power set tag, meaning
		 * that all configurations are valid. Additionally, the Body will have
		 * this same object, so that the powerset of a given body is easily
		 * retrieved.
		 */
		body.addTag(powerSetTag);
		Iterator<Unit> iterator = colorlessUnits.iterator();
		while (iterator.hasNext()) {
			Unit colorlessUnit = (Unit) iterator.next();
			colorlessUnit.addTag(powerSetTag);
		}

		/*
		 * Now iterate over the colored units and produce a valid configuration
		 * set for each of them
		 */

		/*
		 * FIXME: possible bottleneck, measure it (BDDS!)
		 * 
		 */
		Iterator<Object[]> uafIterator = uaf.iterator();
		while (uafIterator.hasNext()) {
			Object[] tripleObject = (Object[]) uafIterator.next();
			Unit unitInUaf = (Unit) tripleObject[0];
			Collection<ASTNode> ASTNodesInUaf = (Collection<ASTNode>) tripleObject[1];
			Set<IFeature> featuresInUaf = (Set<IFeature>) tripleObject[2];

			FeatureTag<Set<String>> validConfigurationsTag = new FeatureTag<Set<String>>();

			Set<Set<String>> validConfigurationsPowerSet = SetUtil.configurationSet(featureNamePowerSet, featuresInUaf);
			System.out.println(featureNamePowerSet + "::" + featuresInUaf + " = " + validConfigurationsPowerSet);
			Iterator<Set<String>> validConfigurationsIterator = validConfigurationsPowerSet.iterator();
			while (validConfigurationsIterator.hasNext()) {
				Set<String> set = (Set<String>) validConfigurationsIterator.next();
				validConfigurationsTag.add(set);
			}

			unitInUaf.addTag(validConfigurationsTag);

		}

		long endTransform = System.nanoTime();
		long delta = endTransform - startTransform;
		FeatureModelInstrumentorTransformer.transformationTime += delta;

		// #ifdef METRICS
		try {
			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.INSTRUMENTATION_COLUMN, Double.toString(((double) delta) / 1000000));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// #endif
		// #ifdef METRICS_INSTRUMENTATION_BOTTLENECK
		try {
			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.INSTRUMENTATION_UNITTOASTNODE_COLUMN, Double.toString(((double) unitToASTNodeDelta) / 1000000));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// #endif
	}

	/**
	 * Do the transformation on the body. To accomplish this, the class that
	 * declares this SootMethod, it needs to be tagged with the SourceFileTag.
	 * 
	 * @param body
	 *            the body
	 * @param compilationUnit
	 *            the compilation unit
	 */
	public void transform2(Body body, String classPath) {
		FeatureModelInstrumentorTransformer.classPath = classPath;
		preTransform(body);
		this.transform(body);
	}

	private void preTransform(Body body) {
		SootClass sootClass = body.getMethod().getDeclaringClass();
		if (!sootClass.hasTag("SourceFileTag")) {
			throw new IllegalArgumentException("the body cannot be traced to its source file");
		}
		/*
		 * XXX: WARNING! tag.getAbsolutePath() returns an INCORRECT value for
		 * the absolute path AFTER the first body transformation. In order to
		 * work around this, we must inject the classpath we are working on
		 * through a parameter in this method. We will use tag.getSourceFile()
		 * in order to resolve the file name.
		 * 
		 * Yes, this is ugly.
		 */
		SourceFileTag tag = (SourceFileTag) body.getMethod().getDeclaringClass().getTag("SourceFileTag");

		/*
		 * package name
		 */
		String absolutePath = sootClass.getName();
		int lastIndexOf = absolutePath.lastIndexOf(".");
		if (lastIndexOf != -1) {
			absolutePath = absolutePath.substring(0, lastIndexOf);
		} else {
			absolutePath = "";
		}

		/*
		 * XXX String#replaceAll bugs when replacing "special" chars like
		 * File.separator. The Matcher and Patter composes a workaround for
		 * that.
		 */
		absolutePath = absolutePath.replaceAll(Pattern.quote("."), Matcher.quoteReplacement(File.separator));
		absolutePath = classPath + File.separator + absolutePath + File.separator + tag.getSourceFile();
		tag.setAbsolutePath(absolutePath);

		IPath path = new Path(tag.getAbsolutePath());
		this.file = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

		CompilationUnit jdtCompilationUnit = cachedParser.parse(file);

		/*
		 * The CIDE feature extractor depends on this object.
		 */
		this.currentCompilationUnit = jdtCompilationUnit;
	}

	// #ifdef METRICS
	public static long getTransformationTime() {
		return FeatureModelInstrumentorTransformer.transformationTime;
	}

	public static long getTotalBodies() {
		return FeatureModelInstrumentorTransformer.totalBodies;
	}

	public static long getTotalColoredBodies() {
		return FeatureModelInstrumentorTransformer.totalColoredBodies;
	}

	public void reset() {
		FeatureModelInstrumentorTransformer.totalColoredBodies = 0;
		FeatureModelInstrumentorTransformer.totalBodies = 0;
		FeatureModelInstrumentorTransformer.transformationTime = 0;
	}
	// #endif
}
