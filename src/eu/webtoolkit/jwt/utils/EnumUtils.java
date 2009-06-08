/**
 * 
 */
package eu.webtoolkit.jwt.utils;

import java.util.EnumSet;

import eu.webtoolkit.jwt.Key;

/**
 * @author wim
 * 
 */
public class EnumUtils {

	public static <E extends Enum<E>> EnumSet<E> mask(EnumSet<E> theSet, E mask) {
		EnumSet<E> retval = EnumSet.copyOf(theSet);
		EnumSet<E> theMask = EnumSet.of(mask);
		retval.retainAll(theMask);
		return retval;
	}

	public static <E extends Enum<E>> EnumSet<E> mask(EnumSet<E> theSet, EnumSet<E> mask) {
		EnumSet<E> retval = EnumSet.copyOf(theSet);
		retval.retainAll(mask);
		return retval;
	}

	public static <E extends Enum<E>> E enumFromSet(EnumSet<E> theSet) {
		if (theSet.isEmpty()) {
			return null;
		} else {
			return theSet.iterator().next();
		}
	}

	public static <E extends Enum<E>> EnumSet<E> setOnly(EnumSet<E> theSet, E flag) {
		theSet.clear();
		theSet.add(flag);
		return theSet;
	}

	public static Key keyFromValue(int key) {
		for (Key k : Key.values()) {
			if (k.getValue() == key)
				return k;
		}

		return Key.Key_unknown;
	}

	public static <E extends Enum<E>> EnumSet<E> or(EnumSet<E> theSet, E flag) {
		EnumSet<E> retval = EnumSet.copyOf(theSet);
		retval.add(flag);
		return retval;
	}

	public static <E extends Enum<E>> E max(E e1, E e2) {
		return e1.ordinal() > e2.ordinal() ? e1 : e2;
	}
}
