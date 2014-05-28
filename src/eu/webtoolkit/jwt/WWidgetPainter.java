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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class WWidgetPainter {
	private static Logger logger = LoggerFactory
			.getLogger(WWidgetPainter.class);

	enum RenderType {
		InlineVml, InlineSvg, HtmlCanvas, PngImage;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	public abstract WPaintDevice createPaintDevice(boolean paintUpdate);

	public abstract WPaintDevice getPaintDevice(boolean paintUpdate);

	public abstract void createContents(DomElement element, WPaintDevice device);

	public abstract void updateContents(final List<DomElement> result,
			WPaintDevice device);

	public abstract WWidgetPainter.RenderType getRenderType();

	protected WWidgetPainter(WPaintedWidget widget) {
		this.widget_ = widget;
	}

	protected WPaintedWidget widget_;
}
