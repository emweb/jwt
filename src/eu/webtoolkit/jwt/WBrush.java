/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


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
	 * Default constructor.
	 * <p>
	 * Constructs a brush with a {@link WBrushStyle#NoBrush NoBrush} fill style.
	 */
	public WBrush() {
		this.style_ = WBrushStyle.NoBrush;
		this.color_ = WColor.black;
	}

	/**
	 * Construct a brush with a particular style.
	 * <p>
	 * Constructs a black brush with the indicated <code>style</code>.
	 */
	public WBrush(WBrushStyle style) {
		this.style_ = style;
		this.color_ = WColor.black;
	}

	/**
	 * Construct a solid brush of a particular color.
	 * <p>
	 * Constructs a solid brush with the indicated <code>color</code>.
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
	 * Comparison operator.
	 * <p>
	 * Returns <code>true</code> if the brushes are exactly the same.
	 */
	public boolean equals(WBrush other) {
		return this.color_.equals(other.color_) && this.style_ == other.style_;
	}

	/**
	 * Change the brush style.
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
	 * Change the brush color.
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
