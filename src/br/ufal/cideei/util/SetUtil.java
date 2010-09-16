package br.ufal.cideei.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SetUtil {

	private SetUtil() {
	}

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

	public static <T> Set<Set<T>> configurationSet(Set<Set<T>> featurePowerSet, Set<T> featuresInUaf) {
		Iterator<Set<T>> powerSetIterator = featurePowerSet.iterator();
		Set<Set<T>> resultingSet = new HashSet<Set<T>>();
		while (powerSetIterator.hasNext()) {
			Set<T> nextSubSet = (Set<T>) powerSetIterator.next();
			Iterator<T> subSetIterator = nextSubSet.iterator();
			boolean foundFeature = false;

			if (featuresInUaf.size() >=2) {
				Iterator<T> iterator = featuresInUaf.iterator();
				while (iterator.hasNext()) {
					T t = (T) iterator.next();
					if (nextSubSet.contains(t)) {
						foundFeature = true;
					} else {
						foundFeature = false;
						break;
					}
				}
			} else if (featuresInUaf.size() == 1) {
				while (subSetIterator.hasNext()) {
					T element = (T) subSetIterator.next();
					if (featuresInUaf.contains(element)) {
						// System.out.println(featuresInUaf + " contains " +
						// element);
						foundFeature = true;
					}
				}
			}

			if (foundFeature) {
				resultingSet.add(nextSubSet);
			}
		}
		return resultingSet;
	}
}
