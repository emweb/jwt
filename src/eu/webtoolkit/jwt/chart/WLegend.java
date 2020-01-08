/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

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

class WLegend {
	private static Logger logger = LoggerFactory.getLogger(WLegend.class);

	public WLegend() {
		this.legendEnabled_ = false;
		this.legendLocation_ = LegendLocation.LegendOutside;
		this.legendSide_ = Side.Right;
		this.legendAlignment_ = AlignmentFlag.AlignMiddle;
		this.legendColumns_ = 1;
		this.legendColumnWidth_ = new WLength(100);
		this.legendFont_ = new WFont();
		this.legendBorder_ = new WPen(PenStyle.NoPen);
		this.legendBackground_ = new WBrush(BrushStyle.NoBrush);
		this.legendFont_.setFamily(WFont.GenericFamily.SansSerif, "Arial");
		this.legendFont_.setSize(WFont.Size.FixedSize, new WLength(10,
				WLength.Unit.Point));
	}

	public void setLegendEnabled(boolean enabled) {
		if (this.legendEnabled_ != enabled) {
			this.legendEnabled_ = enabled;
		}
	}

	public boolean isLegendEnabled() {
		return this.legendEnabled_;
	}

	public void setLegendLocation(LegendLocation location, Side side,
			AlignmentFlag alignment) {
		this.legendLocation_ = location;
		this.legendSide_ = side;
		this.legendAlignment_ = alignment;
	}

	public void setLegendStyle(final WFont font, final WPen border,
			final WBrush background) {
		this.legendFont_ = font;
		this.legendBorder_ = border;
		this.legendBackground_ = background;
	}

	public LegendLocation getLegendLocation() {
		return this.legendLocation_;
	}

	public Side getLegendSide() {
		return this.legendSide_;
	}

	public AlignmentFlag getLegendAlignment() {
		return this.legendAlignment_;
	}

	public int getLegendColumns() {
		return this.legendColumns_;
	}

	public WLength getLegendColumnWidth() {
		return this.legendColumnWidth_;
	}

	public WFont getLegendFont() {
		return this.legendFont_;
	}

	public WPen getLegendBorder() {
		return this.legendBorder_;
	}

	public WBrush getLegendBackground() {
		return this.legendBackground_;
	}

	public void setLegendColumns(int columns) {
		this.legendColumns_ = columns;
	}

	public void setLegendColumnWidth(final WLength columnWidth) {
		this.legendColumnWidth_ = columnWidth;
	}

	protected boolean legendEnabled_;
	protected LegendLocation legendLocation_;
	protected Side legendSide_;
	protected AlignmentFlag legendAlignment_;
	protected int legendColumns_;
	protected WLength legendColumnWidth_;
	protected WFont legendFont_;
	protected WPen legendBorder_;
	protected WBrush legendBackground_;
}
