/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import java.util.Comparator;

public class ReverseOrder<T extends Comparable<T>> implements Comparator<T> {

	public int compare(T o1, T o2) {
		return -o1.compareTo(o2);
	}

}
