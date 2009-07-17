/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.List;

abstract class WWidgetPainter {
	public void destroy() {
	}

	public abstract WPaintDevice getCreatePaintDevice();

	public abstract void createContents(DomElement element, WPaintDevice device);

	public abstract void updateContents(List<DomElement> result,
			WPaintDevice device);

	protected WWidgetPainter(WPaintedWidget widget) {
		this.widget_ = widget;
	}

	protected WPaintedWidget widget_;
}
