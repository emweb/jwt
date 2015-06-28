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
 * A value class that defines the style for pen strokes.
 * <p>
 * 
 * A pen defines the properties of how lines (that may surround shapes) are
 * rendered.
 * <p>
 * A pen with width 0 is a <i>cosmetic</i> pen, and is always rendered as 1
 * pixel width, regardless of transformations. Otherwized, the pen width is
 * modified by the {@link WPainter#getWorldTransform() transformation} set on
 * the painter.
 * <p>
 * 
 * @see WPainter#setPen(WPen p)
 * @see WBrush
 */
public class WPen {
	private static Logger logger = LoggerFactory.getLogger(WPen.class);

	/**
	 * Creates a black cosmetic pen.
	 * <p>
	 * Constructs a black solid pen of 0 width (i.e. cosmetic single pixel
	 * width), with {@link PenCapStyle#SquareCap SquareCap} line ends and
	 * {@link PenJoinStyle#BevelJoin BevelJoin} line join style.
	 */
	public WPen() {
		this.penStyle_ = PenStyle.SolidLine;
		this.penCapStyle_ = PenCapStyle.SquareCap;
		this.penJoinStyle_ = PenJoinStyle.BevelJoin;
		this.width_ = new WLength(0);
		this.color_ = WColor.black;
		this.gradient_ = new WGradient();
	}

	/**
	 * Creates a black pen with a particular style.
	 * <p>
	 * Constructs a black pen of 0 width (i.e. cosmetic single pixel width),
	 * with {@link PenCapStyle#SquareCap SquareCap} line ends and
	 * {@link PenJoinStyle#BevelJoin BevelJoin} line join style.
	 * <p>
	 * The line style is set to <code>style</code>.
	 */
	public WPen(PenStyle style) {
		this.penStyle_ = style;
		this.penCapStyle_ = PenCapStyle.SquareCap;
		this.penJoinStyle_ = PenJoinStyle.BevelJoin;
		this.width_ = new WLength(0);
		this.color_ = WColor.black;
		this.gradient_ = new WGradient();
	}

	/**
	 * Creates a solid pen of a particular color.
	 * <p>
	 * Constructs a solid pen of 0 width (i.e. cosmetic single pixel width),
	 * with {@link PenCapStyle#SquareCap SquareCap} line ends and
	 * {@link PenJoinStyle#BevelJoin BevelJoin} line join style.
	 * <p>
	 * The pen color is set to <code>color</code>.
	 */
	public WPen(final WColor color) {
		this.penStyle_ = PenStyle.SolidLine;
		this.penCapStyle_ = PenCapStyle.SquareCap;
		this.penJoinStyle_ = PenJoinStyle.BevelJoin;
		this.width_ = new WLength(0);
		this.color_ = color;
		this.gradient_ = new WGradient();
	}

	/**
	 * Creates a solid pen with a gradient color.
	 * <p>
	 * Constructs a solid pen of 0 width (i.e. cosmetic single pixel width),
	 * with {@link PenCapStyle#SquareCap SquareCap} line ends and
	 * {@link PenJoinStyle#BevelJoin BevelJoin} line join style.
	 * <p>
	 * The pen&apos;s color is defined by the gradient <code>color</code>.
	 */
	public WPen(final WGradient gradient) {
		this.penStyle_ = PenStyle.SolidLine;
		this.penCapStyle_ = PenCapStyle.SquareCap;
		this.penJoinStyle_ = PenJoinStyle.BevelJoin;
		this.width_ = new WLength(0);
		this.color_ = WColor.black;
		this.gradient_ = gradient;
	}

	/**
	 * Clone method.
	 * <p>
	 * Clones this pen.
	 */
	public WPen clone() {
		WPen result = new WPen();
		result.penStyle_ = this.penStyle_;
		result.penCapStyle_ = this.penCapStyle_;
		result.penJoinStyle_ = this.penJoinStyle_;
		result.width_ = this.width_;
		result.color_ = this.color_;
		return result;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <p>
	 * Returns <code>true</code> if the pens are exactly the same.
	 */
	public boolean equals(final WPen other) {
		return this.penStyle_ == other.penStyle_
				&& this.penCapStyle_ == other.penCapStyle_
				&& this.penJoinStyle_ == other.penJoinStyle_
				&& this.width_.equals(other.width_)
				&& this.color_.equals(other.color_)
				&& this.gradient_.equals(other.gradient_);
	}

	/**
	 * Sets the pen style.
	 * <p>
	 * The pen style determines the pattern with which the pen is rendered.
	 */
	public void setStyle(PenStyle style) {
		this.penStyle_ = style;
	}

	/**
	 * Returns the pen style.
	 * <p>
	 * 
	 * @see WPen#setStyle(PenStyle style)
	 */
	public PenStyle getStyle() {
		return this.penStyle_;
	}

	/**
	 * Sets the style for rendering line ends.
	 * <p>
	 * The cap style configures how line ends are rendered.
	 */
	public void setCapStyle(PenCapStyle style) {
		this.penCapStyle_ = style;
	}

	/**
	 * Returns the style for rendering line ends.
	 * <p>
	 * 
	 * @see WPen#setCapStyle(PenCapStyle style)
	 */
	public PenCapStyle getCapStyle() {
		return this.penCapStyle_;
	}

	/**
	 * Sets the style for rendering line joins.
	 * <p>
	 * The join style configures how corners are rendered between different
	 * segments of a poly-line, rectange or painter path.
	 */
	public void setJoinStyle(PenJoinStyle style) {
		this.penJoinStyle_ = style;
	}

	/**
	 * Returns the style for rendering line joins.
	 * <p>
	 * 
	 * @see WPen#setJoinStyle(PenJoinStyle style)
	 */
	public PenJoinStyle getJoinStyle() {
		return this.penJoinStyle_;
	}

	/**
	 * Sets the pen width.
	 * <p>
	 * A pen width <code>must</code> be specified using
	 * {@link WLength.Unit#Pixel} units.
	 */
	public void setWidth(final WLength width) {
		this.width_ = width;
	}

	/**
	 * Returns the pen width.
	 * <p>
	 * 
	 * @see WPen#setWidth(WLength width)
	 */
	public WLength getWidth() {
		return this.width_;
	}

	/**
	 * Sets the pen color.
	 * <p>
	 * <i>{@link WPen#setGradient(WGradient gradient) setGradient()}</i>
	 */
	public void setColor(final WColor color) {
		this.color_ = color;
		this.gradient_ = new WGradient();
	}

	/**
	 * Returns the pen color.
	 * <p>
	 * 
	 * @see WPen#setColor(WColor color)
	 */
	public WColor getColor() {
		return this.color_;
	}

	/**
	 * Sets the pen color as a gradient.
	 * <p>
	 * 
	 * @see WPen#setColor(WColor color)
	 */
	public void setGradient(final WGradient gradient) {
		this.gradient_ = gradient;
	}

	/**
	 * Returns the pen color gradient.
	 * <p>
	 * 
	 * @see WPen#setGradient(WGradient gradient)
	 */
	public WGradient getGradient() {
		return this.gradient_;
	}

	private PenStyle penStyle_;
	private PenCapStyle penCapStyle_;
	private PenJoinStyle penJoinStyle_;
	private WLength width_;
	private WColor color_;
	private WGradient gradient_;
}
