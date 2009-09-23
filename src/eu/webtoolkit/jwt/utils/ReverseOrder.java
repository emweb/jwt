/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import java.util.Comparator;

/**
 * ReverseOrder implements a reverse order {@link Comparator}.
 *
 * @param <T>
 */
public class ReverseOrder<T extends Comparable<T>> implements Comparator<T> {

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(T o1, T o2) {
		return -o1.compareTo(o2);
	}
}
