/*
 * Copyright (C) 2019 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import eu.webtoolkit.jwt.WWidget;

public class WidgetUtils {
	public static <W extends WWidget> W remove(WWidget c, W w) {
		if (w != null && c != null && w.getParent() == c) {
			c.removeWidget(w);
			return w;
		} else {
			return null;
		}
	}
}
