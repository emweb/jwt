/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

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
