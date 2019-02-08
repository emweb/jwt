/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MapUtils {
	private static final Logger logger = LoggerFactory.getLogger(MapUtils.class);
	
	@SuppressWarnings("unchecked")
	public static <K, V> V access(Map<K, V> map, K key, Class valueClass) {
		V value = map.get(key);

		if (value == null) {
			try {
				value = (V) valueClass.newInstance();
			} catch (InstantiationException e) {
				logger.error("InstantiationException for {}", valueClass.getName(), e);
			} catch (IllegalAccessException e) {
				logger.error("IllegalAccessException for {}", valueClass.getName(), e);
			}
			map.put(key, value);
		}

		return value;
	}
}
