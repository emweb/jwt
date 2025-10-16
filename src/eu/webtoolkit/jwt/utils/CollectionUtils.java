/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CollectionUtils {

	public static <V> void resize(List<V> list, int size) {
		while (list.size() > size)
			list.remove(list.size() - 1);
		while (list.size() < size)
			list.add(null);
	}

	public static <V, Compare extends Comparator<V>> int insertionPoint(List<V> list, V item, Compare compare) {
		int i = Collections.binarySearch(list, item, compare);
		if (i < 0) {
			return -1 - i;
		} else
			return i;
	}

	/*
	 * Returns index of first value in list which is equal to, or greater than value.
	 */
	public static int lowerBound(List<Integer> list, int value) {
		int i = Collections.binarySearch(list, value);
		if (i < 0) {
			return -1 - i;
		} else
			return i;
	}

	/*
	 * Returns index of first value in list which is greater than value. 
	 */
	public static int upperBound(List<Integer> list, int value) {
		return lowerBound(list, value + 1);
	}
	
	public static <K, V> void findInMultimap(Map<K,List<V>> map, K key, List<V> result) {
		List<V> tmp = map.get(key);
		if (tmp != null)
			result.addAll(tmp);
	}

	public static <V> void splice(LinkedList<V> list, Iterator<V> iterator, LinkedList<V> list2, V v) {
		list2.remove(v);
		assert(iterator.equals(list.iterator()));

		list.addFirst(v);
	}

	public static <V> boolean add(List<V> list, V v) {
		if (list.indexOf(v) == -1) {
			list.add(v);
			return true;
		} else
			return false;
	}

	public static <V> V take(List<V> list, V v) {
		int index = list.indexOf(v);
		if (index == -1)
			return null;
		return list.remove(index);
	}

	public static <K,V> K keyForValue(Map<K,V> map, V value) {
		return map.entrySet().stream()
				.filter(e -> Objects.equals(e.getValue(), value))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null);
	}
}
