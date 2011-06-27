package br.ufal.cideei.soot.instrument;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.dom.CompilationUnit;

import soot.Body;
import soot.BodyTransformer;
import soot.SootClass;
import soot.Unit;
import soot.tagkit.SourceFileTag;
import soot.tagkit.SourceLnPosTag;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.util.CachedICompilationUnitParser;
import br.ufal.cideei.util.SetUtil;
import br.ufal.cideei.util.count.AbstractMetricsSink;

/**
 * The Class FeatureModelInstrumentor is a Soot transformation for transcribing
 * feature information to every Unit. This is done via Tag annonations. The
 * feature model and its format from which the information will be extracted is
 * still unkown.
 */
// TODO: change class name to something more appropriate, like
// "FeatureInstrumentorTransformer"
public class FeatureModelInstrumentorTransformer extends BodyTransformer {

	/** Feature extracter. */
	private static IFeatureExtracter extracter;
	/** Current compilation unit the transformation is working on */
	private IFile file;

	/**
	 * XXX: Workaround for the preTransform method. See comments on
	 * FeatureModelInstrumentorTransformer#preTransform() method.
	 */
	private static String classPath;
	private CachedICompilationUnitParser cachedParser = new CachedICompilationUnitParser();
	private CachedLineNumberMapper cachedLineColorMapper = new CachedLineNumberMapper();
	private Map<Integer, Set<String>> currentColorMap;

	// #ifdef METRICS
	private static long transformationTime = 0;
	private static long parsingTime = 0;
	private static long colorLookupTableBuildingTime = 0;
	private AbstractMetricsSink sink;
	private static String COLOR_LOOKUP = "color table";
	private static String PARSING = "parsing";
	private static String INSTRUMENTATION = "instrumentation";

	// #endif
	/*
	 * TODO: maybe injecting the sink depency in a different way could make this
	 * funcionality less intrusive.
	 */
	public FeatureModelInstrumentorTransformer(AbstractMetricsSink sink, IFeatureExtracter extracter, String classPath) {
		FeatureModelInstrumentorTransformer.classPath = classPath;
		FeatureModelInstrumentorTransformer.extracter = extracter;
		// #ifdef METRICS
		this.sink = sink;
		// #endif
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String,
	 * java.util.Map)
	 */
	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		preTransform(body);

		// #ifdef METRICS
		long startTransform = System.nanoTime();
		// #endif

		/*
		 * Iterate over all units, look up for their colors and add a new
		 * FeatureTag to each of them, and also compute all the colors found in
		 * the whole body. Units with no colors receive an empty FeatureTag.
		 */
		Iterator<Unit> unitIt = body.getUnits().iterator();

		/*
		 * After the following loop, allPresentFeatures will hold all the colors
		 * found in the body. Used to calculate a "local" power set.
		 */
		Set<String> allPresentFeatures = new HashSet<String>();

		/*
		 * The set of features are represented as bits, for a more compact
		 * representation. The mapping between a feature and it's ID is stored
		 * in the FeatureTag of the body.
		 * 
		 * This is necessary so that clients, such as r.d. analysis, can safely
		 * iterate over all configurations without explicitly invoking Set
		 * operations like containsAll();
		 * 
		 * TODO: check redundancy between allPresentFeatures &
		 * allPresentFeaturesId
		 */
		Map<String, Integer> allPresentFeaturesId = new HashMap<String, Integer>();
		FeatureTag<Set<String>> emptyFeatureTag = FeatureTag.<Set<String>> emptyFeatureTag();

		int idGen = 1;
		while (unitIt.hasNext()) {
			Unit nextUnit = unitIt.next();
			SourceLnPosTag lineTag = (SourceLnPosTag) nextUnit.getTag("SourceLnPosTag");
			if (lineTag == null) {
				nextUnit.addTag(emptyFeatureTag);
			} else {
				int unitLine = lineTag.startLn();
				Set<String> nextUnitColors = currentColorMap.get(unitLine);
				if (nextUnitColors != null) {

					for (String color : nextUnitColors) {
						if (!allPresentFeaturesId.containsKey(color)) {
							allPresentFeaturesId.put(color, idGen);
							idGen = idGen << 1;
						}
					}
					/*
					 * increment local powerset with new found colors.
					 */
					allPresentFeatures.addAll(nextUnitColors);

					/*
					 * creates a FeatureTag, generate it's ID, and assign it to
					 * the corresponding Unit.
					 */
					FeatureTag<String> featureTag = new FeatureTag<String>();
					featureTag.setFeatures(nextUnitColors);
					featureTag.generateId(allPresentFeaturesId);
					nextUnit.addTag(featureTag);
				} else {
					nextUnit.addTag(emptyFeatureTag);
				}
			}
		}
		
		// #ifdef METRICS
		long transformationDelta = System.nanoTime() - startTransform;
		if (sink != null) {
			sink.flow(body, FeatureModelInstrumentorTransformer.INSTRUMENTATION, transformationDelta);
		}
		FeatureModelInstrumentorTransformer.transformationTime += transformationDelta;
		// #endif

		Set<Set<String>> localPowerSet = SetUtil.powerSet(allPresentFeatures);
		FeatureTag<Set<String>> powerSetTag = new FeatureTag<Set<String>>();
		powerSetTag.addAll(localPowerSet);
		DualHashBidiMap dualHashBidiMap = new DualHashBidiMap(allPresentFeaturesId);
		powerSetTag.setFeatureIdMap(new DualHashBidiMap(dualHashBidiMap));
		body.addTag(powerSetTag);
	}

	/**
	 * Do the transformation on the body. To accomplish this, the class that
	 * declares this SootMethod needs to be tagged with the SourceFileTag.
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
		 * the absolute path AFTER the first body transformation. In this
		 * workaround, since this method depends on the classpath , it is
		 * injected on this class constructor. We will use tag.getSourceFile()
		 * in order to resolve the file name.
		 * 
		 * Yes, this is ugly.
		 */
		SourceFileTag sourceFileTag = (SourceFileTag) body.getMethod().getDeclaringClass().getTag("SourceFileTag");

		/*
		 * The String absolutePath will be transformed to the absolute path to
		 * the Class which body belongs to. See the XXX above for the
		 * explanation.
		 */
		String absolutePath = sootClass.getName();
		int lastIndexOf = absolutePath.lastIndexOf(".");
		if (lastIndexOf != -1) {
			absolutePath = absolutePath.substring(0, lastIndexOf);
		} else {
			absolutePath = "";
		}

		/*
		 * XXX String#replaceAll does not work properly when replacing "special"
		 * chars like File.separator. The Matcher and Pattern composes a
		 * workaround for that.
		 */
		absolutePath = absolutePath.replaceAll(Pattern.quote("."), Matcher.quoteReplacement(File.separator));
		absolutePath = classPath + File.separator + absolutePath + File.separator + sourceFileTag.getSourceFile();
		sourceFileTag.setAbsolutePath(absolutePath);

		IPath path = new Path(sourceFileTag.getAbsolutePath());
		this.file = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

		//#ifdef METRICS
		long startCompilationUnitParser = System.nanoTime();
		//#endif
		CompilationUnit compilationUnit = cachedParser.parse(file);
		//#ifdef METRICS
		long parsingDelta = System.nanoTime() - startCompilationUnitParser;
		if (sink != null)
			sink.flow(body, FeatureModelInstrumentorTransformer.PARSING, parsingDelta);
		FeatureModelInstrumentorTransformer.parsingTime += parsingDelta;

		long startBuilderColorLookUpTable = System.nanoTime();
		//#endif
		this.currentColorMap = cachedLineColorMapper.makeAccept(compilationUnit, file, extracter, compilationUnit);
		//#ifdef METRICS
		long builderColorLookUpTableDelta = System.nanoTime() - startBuilderColorLookUpTable;
		if (sink != null)
			sink.flow(body, FeatureModelInstrumentorTransformer.COLOR_LOOKUP, builderColorLookUpTableDelta);
		FeatureModelInstrumentorTransformer.colorLookupTableBuildingTime += builderColorLookUpTableDelta;
		//#endif
	}
}
