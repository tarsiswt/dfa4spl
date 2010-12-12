package br.ufal.cideei.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;

import br.ufal.cideei.features.IFeatureExtracter;

import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;

// TODO: Auto-generated Javadoc
/**
 * The Class SetUtil is a utility class to generate relevant sets to the
 * application.
 */
public class SetUtil {

	/**
	 * Instantiates a new sets the util.
	 */
	private SetUtil() {
	}

	/**
	 * Recursively generates a power set for a given set.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param originalSet
	 *            the original set
	 * @return the sets the
	 */
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
		for (Set<T> set : powerSet(rest)) {
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	/**
	 * Generates a valid configuration sets for given set of features and a
	 * power set.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param featurePowerSet
	 *            the feature power set
	 * @param featuresInUaf
	 *            the features in uaf
	 * @return the sets the
	 */
	public static <T> Set<Set<T>> configurationSet(Set<Set<T>> featurePowerSet, Set<IFeature> featuresInUaf) {
		Iterator<Set<T>> powerSetIterator = featurePowerSet.iterator();
		Set<Set<T>> resultingSet = new HashSet<Set<T>>();
		while (powerSetIterator.hasNext()) {
			Set<T> nextSubSet = (Set<T>) powerSetIterator.next();
			Iterator<T> subSetIterator = nextSubSet.iterator();
			boolean foundFeature = false;

			if (featuresInUaf.size() >= 1) {
				Iterator<IFeature> iterator = featuresInUaf.iterator();
				while (iterator.hasNext()) {
					IFeature feature = iterator.next();
					if (nextSubSet.contains(feature.getName())) {
						foundFeature = true;
					} else {
						foundFeature = false;
						break;
					}
				}
			}
			if (foundFeature) {
				resultingSet.add(nextSubSet);
			}
		}
		return resultingSet;
	}

	// TODO: FOR TESTING ONLY. REMOVE LATER.
	public static Collection<Set<String>> tstconfig() {
		// Set<Object> configuration1 = new HashSet<Object>();
		// configuration1.add("A");
		// Set<Object> configuration2 = new HashSet<Object>();
		// configuration2.add("B");
		// Set<Object> configuration3 = new HashSet<Object>();
		// configuration3.add("A");
		// configuration3.add("B");
		// Collection<Set<String>> configurations = new HashSet<Set<String>>();
		// configurations.add(configuration1);
		// configurations.add(configuration2);
		// configurations.add(configuration3);
		// return configurations;
		return null;
	}

	// public static Set<Set<IFeature>>
	// powerSetAgainstFeatureModel(Set<IFeature> originalSet, IFeatureExtracter
	// extracter, IFile file)
	// throws FeatureModelNotFoundException {
	// Set<Set<IFeature>> sets = new HashSet<Set<IFeature>>();
	// if (originalSet.isEmpty()) {
	// sets.add(new HashSet<IFeature>());
	// return sets;
	// }
	// List<IFeature> list = new ArrayList<IFeature>(originalSet);
	// IFeature head = list.get(0);
	// Set<IFeature> rest = new HashSet<IFeature>(list.subList(1, list.size()));
	// for (Set<IFeature> set : powerSetAgainstFeatureModel(rest, extracter,
	// file)) {
	// Set<IFeature> newSet = new HashSet<IFeature>();
	// newSet.add(head);
	// newSet.addAll(set);
	// if (extracter.isValid(newSet, file)) {
	// sets.add(newSet);
	// } else {
	// System.out.println("skipping invalid set: " + newSet);
	// }
	// if (extracter.isValid(set, file)) {
	// sets.add(set);
	// } else {
	// System.out.println("skipping invalid set: " + set);
	// }
	//
	// }
	// return sets;
	// }

	public static Set<Set<String>> ifToStr(Set<Set<IFeature>> configs) {
		Set<Set<String>> strConfigs = new HashSet<Set<String>>(configs.size());
		for (Set<IFeature> config : configs) {
			Set<String> strConfig = new HashSet<String>(config.size());
			strConfigs.add(strConfig);
			for (IFeature feat : config) {
				strConfig.add(feat.getName());
			}
		}
		return strConfigs;
	}
	
	/**
	    * Returns the power set from the given set by using a binary counter
	    * Example: S = {a,b,c}
	    * P(S) = {[], [c], [b], [b, c], [a], [a, c], [a, b], [a, b, c]}
	    * @param set String[]
	    * @return LinkedHashSet
	    */
	  private static Set<Set<String>> powerset(String[] set) {

	      //create the empty power set
	      LinkedHashSet<Set<String>> power = new LinkedHashSet();

	      //get the number of elements in the set
	      int elements = set.length;

	      //the number of members of a power set is 2^n
	      int powerElements = (int) Math.pow(2,elements);

	      //run a binary counter for the number of power elements
	      for (int i = 0; i < powerElements; i++) {

	          //convert the binary number to a string containing n digits
	          String binary = intToBinary(i, elements);

	          //create a new set
	          LinkedHashSet innerSet = new LinkedHashSet();

	          //convert each digit in the current binary number to the	corresponding element
	           //in the given set
	          for (int j = 0; j < binary.length(); j++) {
	              if (binary.charAt(j) == '1')
	                  innerSet.add(set[j]);
	          }

	          //add the new set to the power set
	          power.add(innerSet);

	      }

	      return power;
	  }

	  /**
	    * Converts the given integer to a String representing a binary number
	    * with the specified number of digits
	    * For example when using 4 digits the binary 1 is 0001
	    * @param binary int
	    * @param digits int
	    * @return String
	    */
	  private static String intToBinary(int binary, int digits) {

	      String temp = Integer.toBinaryString(binary);
	      int foundDigits = temp.length();
	      String returner = temp;
	      for (int i = foundDigits; i < digits; i++) {
	          returner = "0" + returner;
	      }

	      return returner;
	  }

}
