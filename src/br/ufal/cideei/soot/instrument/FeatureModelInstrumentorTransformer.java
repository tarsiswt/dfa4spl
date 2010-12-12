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
import soot.tagkit.SourceLnPosTag;
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
	private CachedLineNumberMapper cachedLineColorMapper = new CachedLineNumberMapper();
	private Map<Integer, Set<String>> currentColorMap;

	// #ifdef METRICS
	private static long totalBodies = 0;
	private static long totalColoredBodies = 0;
	private static long transformationTime = 0;
	private static long parsingTime = 0;
	private static long colorLookupTableBuildingTime = 0;

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
		preTransform(body);

		// #ifdef METRICS
		try {
			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.METHOD_COLUMN, body.getMethod().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long startTransform = System.nanoTime();
		// #endif
		Iterator<Unit> unitIt = body.getUnits().iterator();
		Set<String> allPresentFeatures = new HashSet<String>();
		FeatureTag<Set<String>> emptyFeatureTag = FeatureTag.<Set<String>> emptyFeatureTag();
		
		while (unitIt.hasNext()) {
			Unit nextUnit = unitIt.next();
			SourceLnPosTag lineTag = (SourceLnPosTag) nextUnit.getTag("SourceLnPosTag");
			if (lineTag == null) {
				nextUnit.addTag(emptyFeatureTag);
			} else {
				int unitLine = lineTag.startLn();
				Set<String> nextUnitColors = currentColorMap.get(unitLine);
				if (nextUnitColors != null) {
					allPresentFeatures.addAll(nextUnitColors);
					FeatureTag<String> featureTag = new FeatureTag<String>();
					featureTag.setFeatures(nextUnitColors);
					nextUnit.addTag(featureTag);
				} else {
					nextUnit.addTag(emptyFeatureTag);
				}
			}
		}
		

		// #ifdef METRICS
		long endTransform = System.nanoTime();
		long delta = endTransform - startTransform;
		FeatureModelInstrumentorTransformer.transformationTime += delta;

		try {
			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.INSTRUMENTATION_COLUMN, Double.toString(((double) delta) / 1000000));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// #endif

		Set<Set<String>> localPowerSet = SetUtil.powerSet(allPresentFeatures);
		FeatureTag<Set<String>> powerSetTag = new FeatureTag<Set<String>>();
		powerSetTag.addAll(localPowerSet);
		body.addTag(powerSetTag);
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
		SourceFileTag sourceFileTag = (SourceFileTag) body.getMethod().getDeclaringClass().getTag("SourceFileTag");

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
		absolutePath = classPath + File.separator + absolutePath + File.separator + sourceFileTag.getSourceFile();
		sourceFileTag.setAbsolutePath(absolutePath);

		IPath path = new Path(sourceFileTag.getAbsolutePath());
		this.file = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

		long startCompilationUnitParser = System.nanoTime();
		CompilationUnit compilationUnit = cachedParser.parse(file);
		long endCompilationUnitParser = System.nanoTime();
		FeatureModelInstrumentorTransformer.parsingTime += endCompilationUnitParser - startCompilationUnitParser;
		
		long startBuilderColorLookUpTable = System.nanoTime();
		this.currentColorMap = cachedLineColorMapper.makeAccept(compilationUnit, file, extracter, compilationUnit);
		long endBuilderColorLookUpTable = System.nanoTime();
		FeatureModelInstrumentorTransformer.colorLookupTableBuildingTime += endBuilderColorLookUpTable - startBuilderColorLookUpTable;
		
		// #ifdef METRICS
		try {
			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.INSTRUMENTATION_UNITTOASTNODE_COLUMN,    Double.toString(((double) (endBuilderColorLookUpTable  - startBuilderColorLookUpTable )/ 1000000)));
			WriterFacadeForAnalysingMM.write(WriterFacadeForAnalysingMM.INSTRUMENTATION_COMPILATIONUNIT_PARSING, Double.toString(((double) (endCompilationUnitParser  - startCompilationUnitParser )    / 1000000)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// #endif

		/*
		 * The CIDE feature extractor depends on this object.
		 */
		this.currentCompilationUnit = compilationUnit;
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
	
	public static double getParsingTime() {
		return FeatureModelInstrumentorTransformer.parsingTime;
	}
	
	public static double getColorLookupTableBuildingTime() {
		return FeatureModelInstrumentorTransformer.colorLookupTableBuildingTime;
	}

	public void reset() {
		FeatureModelInstrumentorTransformer.totalColoredBodies = 0;
		FeatureModelInstrumentorTransformer.totalBodies = 0;
		FeatureModelInstrumentorTransformer.transformationTime = 0;
		FeatureModelInstrumentorTransformer.parsingTime = 0;
		FeatureModelInstrumentorTransformer.colorLookupTableBuildingTime = 0;
	}
	// #endif

	
}
