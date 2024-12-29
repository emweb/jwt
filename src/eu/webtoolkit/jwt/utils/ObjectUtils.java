/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.StringUtils;

public class ObjectUtils {
	@SuppressWarnings("unchecked")
	public static int compare(Object d1, Object d2) {
		if (d1 == null && d2 == null)
			return 0;
		else if (d1 == null)
			return -1;
		else if (d2 == null)
			return 1;

		if (d1.getClass() == d2.getClass()) {
			Comparable c1 = (Comparable) d1;
			Comparable c2 = (Comparable) d2;

			return c1.compareTo(c2);
		} else {
			WString s1 = StringUtils.asString(d1);
			WString s2 = StringUtils.asString(d2);

			return s1.compareTo(s2);
		}

	}

    public static <T> T cast(Object object, Class<T> clazz) {
      return clazz.isInstance(object) ? clazz.cast(object) : null;
    }
}
