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
 * Class that defines the style for filling areas.
 * <p>
 * 
 * A brush defines the properties of how areas (the interior of shapes) are
 * filled. A brush is defined using a color and a fill type (currently only
 * solid fills are supported).
 * <p>
 * 
 * @see WPainter#setBrush(WBrush b)
 * @see WPen
 */
public class WBrush {
	/**
	 * Creates a brush.
	 * <p>
	 * Creates a brush with a {@link WBrushStyle#NoBrush NoBrush} fill style.
	 */
	public WBrush() {
		this.style_ = WBrushStyle.NoBrush;
		this.color_ = WColor.black;
	}

	/**
	 * Creates a black brush with given style.
	 * <p>
	 * Creates a black brush with the indicated <code>style</code>.
	 */
	public WBrush(WBrushStyle style) {
		this.style_ = style;
		this.color_ = WColor.black;
	}

	/**
	 * Creates a solid brush of a given color.
	 * <p>
	 * Creates a solid brush with the indicated <code>color</code>.
	 */
	public WBrush(WColor color) {
		this.style_ = WBrushStyle.SolidPattern;
		this.color_ = color;
	}

	/**
	 * Clone method.
	 * <p>
	 * Clones this {@link WBrush} object.
	 */
	public WBrush clone() {
		WBrush result = new WBrush();
		result.color_ = this.color_;
		result.style_ = this.style_;
		return result;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <p>
	 * Returns <code>true</code> if the brushes are exactly the same.
	 */
	public boolean equals(WBrush other) {
		return this.color_.equals(other.color_) && this.style_ == other.style_;
	}

	/**
	 * Sets the brush style.
	 * <p>
	 * 
	 * @see WBrush#getStyle()
	 */
	public void setStyle(WBrushStyle style) {
		this.style_ = style;
	}

	/**
	 * Returns the fill style.
	 * <p>
	 * 
	 * @see WBrush#setStyle(WBrushStyle style)
	 */
	public WBrushStyle getStyle() {
		return this.style_;
	}

	/**
	 * Sets the brush color.
	 * <p>
	 * 
	 * @see WBrush#getColor()
	 */
	public void setColor(WColor color) {
		this.color_ = color;
	}

	/**
	 * Returns the brush color.
	 * <p>
	 * 
	 * @see WBrush#getColor()
	 */
	public WColor getColor() {
		return this.color_;
	}

	private WBrushStyle style_;
	private WColor color_;
}
