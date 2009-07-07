/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.Map;

class MapUtils {

	@SuppressWarnings("unchecked")
	public static <K, V> V access(Map<K, V> map, K key, Class valueClass) {
		V value = map.get(key);

		if (value == null) {
			try {
				value = (V) valueClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			map.put(key, value);
		}

		return value;
	}
}
