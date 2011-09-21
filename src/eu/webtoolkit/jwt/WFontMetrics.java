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

/**
 * Font metrics class.
 * <p>
 * 
 * This class provides font metrics for a given font. It is returned by an
 * implementation of {@link WPaintDevice#getFontMetrics()
 * WPaintDevice#getFontMetrics()}, and may differ between devices.
 * <p>
 * All methods return pixel dimensions.
 * <p>
 * 
 * @see WPaintDevice
 */
public class WFontMetrics {
	public WFontMetrics(WFont font, double leading, double ascent,
			double descent) {
		this.font_ = font;
		this.leading_ = leading;
		this.ascent_ = ascent;
		this.descent_ = descent;
	}

	public WFont getFont() {
		return this.font_;
	}

	public double getSize() {
		return this.ascent_ + this.descent_;
	}

	public double getHeight() {
		return this.leading_ + this.ascent_ + this.descent_;
	}

	public double getLeading() {
		return this.leading_;
	}

	public double getAscent() {
		return this.ascent_;
	}

	public double getDescent() {
		return this.descent_;
	}

	private WFont font_;
	private double leading_;
	private double ascent_;
	private double descent_;
}
