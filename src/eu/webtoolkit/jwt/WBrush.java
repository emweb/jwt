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

/**
 * A value class that defines the style for filling a path.
 * <p>
 * 
 * A brush defines the properties of how areas (the interior of shapes) are
 * filled. A brush is defined either as a solid color or a gradient.
 * <p>
 * 
 * @see WPainter#setBrush(WBrush b)
 * @see WPen
 */
public class WBrush {
	private static Logger logger = LoggerFactory.getLogger(WBrush.class);

	/**
	 * Creates a brush.
	 * <p>
	 * Creates a brush with a {@link BrushStyle#NoBrush NoBrush} fill style.
	 */
	public WBrush() {
		this.style_ = BrushStyle.NoBrush;
		this.color_ = WColor.black;
		this.gradient_ = new WGradient();
	}

	/**
	 * Creates a brush with the given style.
	 */
	public WBrush(BrushStyle style) {
		this.style_ = style;
		this.color_ = WColor.black;
		this.gradient_ = new WGradient();
	}

	/**
	 * Creates a solid brush of a given color.
	 * <p>
	 * Creates a solid brush with the indicated <code>color</code>.
	 */
	public WBrush(final WColor color) {
		this.style_ = BrushStyle.SolidPattern;
		this.color_ = color;
		this.gradient_ = new WGradient();
	}

	/**
	 * Creates a gradient brush.
	 */
	public WBrush(final WGradient gradient) {
		this.style_ = BrushStyle.GradientPattern;
		this.color_ = new WColor();
		this.gradient_ = gradient;
	}

	/**
	 * Clone method.
	 * <p>
	 * Clones this brush.
	 */
	public WBrush clone() {
		WBrush result = new WBrush();
		result.color_ = this.color_;
		result.gradient_ = this.gradient_;
		result.style_ = this.style_;
		return result;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <p>
	 * Returns <code>true</code> if the brushes are exactly the same.
	 */
	public boolean equals(final WBrush other) {
		return this.color_.equals(other.color_) && this.style_ == other.style_
				&& this.gradient_.equals(other.gradient_);
	}

	/**
	 * Sets the brush style.
	 * <p>
	 * 
	 * @see WBrush#getStyle()
	 */
	public void setStyle(BrushStyle style) {
		this.style_ = style;
	}

	/**
	 * Returns the fill style.
	 * <p>
	 * 
	 * @see WBrush#setStyle(BrushStyle style)
	 */
	public BrushStyle getStyle() {
		return this.style_;
	}

	/**
	 * Sets the brush color.
	 * <p>
	 * If the current style is a gradient style, then it is reset to
	 * {@link BrushStyle#SolidPattern SolidPattern}.
	 * <p>
	 * 
	 * @see WBrush#getColor()
	 */
	public void setColor(final WColor color) {
		this.color_ = color;
		if (this.style_ == BrushStyle.GradientPattern) {
			this.style_ = BrushStyle.SolidPattern;
		}
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

	/**
	 * Sets the brush gradient.
	 * <p>
	 * This also sets the style to {@link BrushStyle#GradientPattern
	 * GradientPattern}.
	 */
	public void setGradient(final WGradient gradient) {
		if (!this.gradient_.isEmpty()) {
			this.gradient_ = gradient;
			this.style_ = BrushStyle.GradientPattern;
		}
	}

	/**
	 * Returns the brush gradient.
	 */
	public WGradient getGradient() {
		return this.gradient_;
	}

	private BrushStyle style_;
	private WColor color_;
	private WGradient gradient_;
}
