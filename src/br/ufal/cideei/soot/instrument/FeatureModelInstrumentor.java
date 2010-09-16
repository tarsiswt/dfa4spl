package br.ufal.cideei.soot.instrument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.util.SetUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class FeatureModelInstrumentor is a Soot transformation for transcribing
 * feature information to every Unit. This is done via Tag annonations. The
 * feature model and its format from which the information will be extracted is
 * still unkown.
 */
public class FeatureModelInstrumentor extends BodyTransformer {

	/** The instance. */
	private static FeatureModelInstrumentor instance = new FeatureModelInstrumentor();
	private static IFeatureExtracter extracter;
	private CompilationUnit currentCompilationUnit;

	/**
	 * Instantiates a new feature model instrumentor.
	 */
	private FeatureModelInstrumentor() {
	}

	/**
	 * V.
	 * 
	 * @param algorithm
	 * 
	 * @return the feature model instrumentor
	 */
	public static FeatureModelInstrumentor v(IFeatureExtracter extracter) {
		FeatureModelInstrumentor.extracter = extracter;
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String,
	 * java.util.Map)
	 */
	@Override
	protected void internalTransform(Body body, String phase, Map opt) {
		/*
		 * The feature set and its power set will be computed during the first
		 * iteration and after the first iteration respectivelly
		 */
		Set<String> featureSet = new HashSet<String>();
		Set<Set<String>> featurePowerSet = null;

		/*
		 * This is the first iteration over the units. We will find out what are
		 * the features present in this body, so that we can generate our power
		 * set. In order to optimize the code, we will also keep record of all
		 * the nodes and features of units that has at least one feature.
		 * 
		 * This triple is stored as and array of objects in this order: {Unit,
		 * ASTNodes, Features}
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
			Set<String> featuresOnUnit = new HashSet<String>();

			// flag used to improve code performance
			boolean alreadyAddedUnit = false;

			// TODO: treat exception correctly
			try {
				nodesTakenFromUnit = ASTNodeUnitBridge.getASTNodesFromUnit(nextUnit, this.currentCompilationUnit);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			Iterator<ASTNode> nodesIterator = nodesTakenFromUnit.iterator();
			while (nodesIterator.hasNext()) {
				ASTNode nextNode = nodesIterator.next();
				Set<String> features = extracter.getFeatures(nextNode);

				Iterator<String> nodesFeaturesIterator = features.iterator();
				while (nodesFeaturesIterator.hasNext()) {
					String feature = nodesFeaturesIterator.next();
					featuresOnUnit.add(feature);
					if (!featureSet.contains(feature)) {
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
		 
		/*
		 * Now, in this second iteration, the units will be tagged with their
		 * valid configurations
		 */

		/*
		 * Create a single FeatureTag that will be added to all units that have
		 * no color. Thus, this object should not be modified.
		 */
		FeatureTag<Set<String>> powerSetTag = new FeatureTag<Set<String>>();
		Iterator<Set<String>> featurePowerSetIterator = featurePowerSet.iterator();
		while (featurePowerSetIterator.hasNext()) {
			Set<String> set = (Set<String>) featurePowerSetIterator.next();
			powerSetTag.add(set);
		}

		/*
		 * All colorless units will have a full feature power set tag, meaning
		 * that all configurations are valid.
		 */
		Iterator<Unit> iterator = colorlessUnits.iterator();
		while (iterator.hasNext()) {
			Unit colorlessUnit = (Unit) iterator.next();
			colorlessUnit.addTag(powerSetTag);
		}

		/*
		 * Now iterate over the colored units and produce a valid configuration
		 * set for each of them
		 */
		Iterator<Object[]> uafIterator = uaf.iterator();
		while (uafIterator.hasNext()) {
			Object[] tripleObject = (Object[]) uafIterator.next();
			Unit unitInUaf = (Unit) tripleObject[0];
			Collection<ASTNode> ASTNodesInUaf = (Collection<ASTNode>) tripleObject[1];
			Set<String> featuresInUaf = (Set<String>) tripleObject[2];

			FeatureTag<Set<String>> validConfigurationsTag = new FeatureTag<Set<String>>();

			System.out.println(unitInUaf + " <feats> " + featuresInUaf);
			Set<Set<String>> validConfigurationsPowerSet = SetUtil.configurationSet(featurePowerSet, featuresInUaf);
			Iterator<Set<String>> validConfigurationsIterator = validConfigurationsPowerSet.iterator();
			while (validConfigurationsIterator.hasNext()) {
				Set<String> set = (Set<String>) validConfigurationsIterator.next();
				validConfigurationsTag.add(set);
			}
			
			unitInUaf.addTag(validConfigurationsTag);
			
		}
	}

	public void transform(Body body, CompilationUnit compilationUnit) {
		this.currentCompilationUnit = compilationUnit;
		this.transform(body);
	}
}
