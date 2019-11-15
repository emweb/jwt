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
 * <h3>JavaScript exposability</h3>
 * 
 * <p>
 * A WBrush is JavaScript exposable. If a WBrush
 * {@link WJavaScriptExposableObject#isJavaScriptBound() is JavaScript bound},
 * it can be accessed in your custom JavaScript code through
 * {@link WJavaScriptHandle#getJsRef() its handle&apos;s jsRef()}. At the
 * moment, only the {@link WBrush#getColor() getColor()} property is exposed,
 * e.g. a brush with the color WColor(10,20,30,255) will be represented in
 * JavaScript as:
 * 
 * <pre>
 * {@code
 *  {
 *    color: [10,20,30,255]
 *  }
 *  
 * }
 * </pre>
 * 
 * <p>
 * 
 * <p>
 * <i><b>Warning: </b>A WBrush that is JavaScript exposed should be modified
 * only through its {@link WJavaScriptHandle handle}. Any attempt at modifying
 * it will cause an exception to be thrown.</i>
 * </p>
 * 
 * @see WPainter#setBrush(WBrush b)
 * @see WPen
 * @see WPaintedWidget#createJSBrush()
 */
public class WBrush extends WJavaScriptExposableObject {
	private static Logger logger = LoggerFactory.getLogger(WBrush.class);

	/**
	 * Creates a brush.
	 * <p>
	 * 
	 * Creates a brush with a {@link BrushStyle#NoBrush} fill style.
	 */
	public WBrush() {
		super();
		this.style_ = BrushStyle.NoBrush;
		this.color_ = WColor.black;
		this.gradient_ = new WGradient();
	}

	/**
	 * Creates a brush with the given style.
	 */
	public WBrush(BrushStyle style) {
		super();
		this.style_ = style;
		this.color_ = WColor.black;
		this.gradient_ = new WGradient();
	}

	/**
	 * Creates a solid brush of a given color.
	 * <p>
	 * 
	 * Creates a solid brush with the indicated <code>color</code>.
	 */
	public WBrush(final WColor color) {
		super();
		this.style_ = BrushStyle.SolidPattern;
		this.color_ = color;
		this.gradient_ = new WGradient();
	}

	/**
	 * Creates a gradient brush.
	 */
	public WBrush(final WGradient gradient) {
		super();
		this.style_ = BrushStyle.GradientPattern;
		this.color_ = new WColor();
		this.gradient_ = gradient;
	}

	/**
	 * Clone method.
	 * <p>
	 * 
	 * Clones this brush.
	 */
	public WBrush clone() {
		WBrush result = new WBrush();
		if (this.isJavaScriptBound()) {
			result.assignBinding(this);
		}
		result.color_ = this.color_;
		result.gradient_ = this.gradient_;
		result.style_ = this.style_;
		return result;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <p>
	 * 
	 * Returns <code>true</code> if the brushes are exactly the same.
	 */
	public boolean equals(final WBrush other) {
		return this.sameBindingAs(other) && this.color_.equals(other.color_)
				&& this.style_ == other.style_
				&& this.gradient_.equals(other.gradient_);
	}

	/**
	 * Sets the brush style.
	 * <p>
	 * 
	 * @exception {@link WException} if the brush
	 *            {@link WJavaScriptExposableObject#isJavaScriptBound() is
	 *            JavaScript bound}
	 * @see WBrush#getStyle()
	 */
	public void setStyle(BrushStyle style) {
		this.checkModifiable();
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
	 * 
	 * If the current style is a gradient style, then it is reset to
	 * {@link BrushStyle#SolidPattern}.
	 * <p>
	 * 
	 * @exception {@link WException} if the brush
	 *            {@link WJavaScriptExposableObject#isJavaScriptBound() is
	 *            JavaScript bound}
	 * @see WBrush#getColor()
	 */
	public void setColor(final WColor color) {
		this.checkModifiable();
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
	 * 
	 * This also sets the style to {@link BrushStyle#GradientPattern}.
	 * <p>
	 * 
	 * @exception {@link WException} if the brush
	 *            {@link WJavaScriptExposableObject#isJavaScriptBound() is
	 *            JavaScript bound}
	 */
	public void setGradient(final WGradient gradient) {
		this.checkModifiable();
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

	public String getJsValue() {
		StringBuilder ss = new StringBuilder();
		ss.append("{\"color\":[").append(this.color_.getRed()).append(",")
				.append(this.color_.getGreen()).append(",")
				.append(this.color_.getBlue()).append(",")
				.append(this.color_.getAlpha()).append("]}");
		return ss.toString();
	}

	protected void assignFromJSON(final com.google.gson.JsonElement value) {
		try {
			final com.google.gson.JsonObject o = (com.google.gson.JsonObject) value;
			final com.google.gson.JsonElement color = o.get("color");
			final com.google.gson.JsonArray col = (com.google.gson.JsonArray) color;
			if (col.size() == 4
					&& !JsonUtils.isNull(JsonUtils.toNumber(col.get(0)))
					&& !JsonUtils.isNull(JsonUtils.toNumber(col.get(1)))
					&& !JsonUtils.isNull(JsonUtils.toNumber(col.get(2)))
					&& !JsonUtils.isNull(JsonUtils.toNumber(col.get(3)))) {
				this.color_ = new WColor(
						JsonUtils
								.orIfNullInt(JsonUtils.toNumber(col.get(0)), 0),
						JsonUtils.orIfNullInt(JsonUtils.toNumber(col.get(1)), 0),
						JsonUtils.orIfNullInt(JsonUtils.toNumber(col.get(2)), 0),
						JsonUtils.orIfNullInt(JsonUtils.toNumber(col.get(3)),
								255));
			} else {
				logger.error(new StringWriter().append(
						"Couldn't convert JSON to WBrush").toString());
			}
		} catch (final RuntimeException e) {
			logger.error(new StringWriter().append(
					"Couldn't convert JSON to WBrush: " + e.toString())
					.toString());
		}
	}

	private BrushStyle style_;
	private WColor color_;
	private WGradient gradient_;
}
