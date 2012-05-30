package br.ufal.cideei.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapUtil {
	private MapUtil() { }
	
	public static <K, V> Collection<V> getValues(Collection<K> keys, Map<K, V> map) {
		Collection<V> ret = new ArrayList<V>();
		Set<Entry<K, V>> entrySet = map.entrySet();
		for (Entry<K, V> entry : entrySet){
			ret.add(map.get(entry.getKey()));
		}
		return ret;
	}
}
