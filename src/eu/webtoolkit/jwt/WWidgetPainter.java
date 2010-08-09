/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.List;

abstract class WWidgetPainter {
	enum RenderType {
		InlineVml, InlineSvg, HtmlCanvas, PngImage;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	public abstract WPaintDevice getPaintDevice();

	public abstract void createContents(DomElement element, WPaintDevice device);

	public abstract void updateContents(List<DomElement> result,
			WPaintDevice device);

	public abstract WWidgetPainter.RenderType getRenderType();

	protected WWidgetPainter(WPaintedWidget widget) {
		this.widget_ = widget;
	}

	protected WPaintedWidget widget_;
}
