package eu.webtoolkit.jwt.utils;

import java.util.Comparator;

import eu.webtoolkit.jwt.WString;

public class ReverseOrder<T extends Comparable<T>> implements Comparator<T> {

	public int compare(T o1, T o2) {
		return -o1.compareTo(o2);
	}

}
