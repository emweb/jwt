package eu.webtoolkit.jwt.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

}
