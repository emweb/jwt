/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;

/**
 * A interactive area in a widget, specified by a circle
 * <p>
 * 
 * The area may be added to a {@link WImage} or {@link WPaintedWidget} to
 * provide interactivity on a circular area of the image. The circle is
 * specified in pixel coordinates.
 * <p>
 * 
 * @see WImage#addArea(WAbstractArea area)
 * @see WPaintedWidget#addArea(WAbstractArea area)
 * @see WRectArea
 * @see WPolygonArea
 */
public class WCircleArea extends WAbstractArea {
	/**
	 * Default constructor.
	 * <p>
	 * Specifies a circular area with center (0, 0) and radius 0.
	 */
	public WCircleArea() {
		super();
		this.x_ = 0;
		this.y_ = 0;
		this.r_ = 0;
	}

	/**
	 * Construct a circular area with given geometry.
	 * <p>
	 * The arguments are in pixel units.
	 */
	public WCircleArea(int x, int y, int radius) {
		super();
		this.x_ = x;
		this.y_ = y;
		this.r_ = radius;
	}

	/**
	 * Set the center.
	 */
	public void setCenter(WPoint point) {
		this.setCenter(point.getX(), point.getY());
	}

	/**
	 * Set the center.
	 */
	public void setCenter(WPointF point) {
		this.setCenter((int) point.getX(), (int) point.getY());
	}

	/**
	 * Set the center.
	 */
	public void setCenter(int x, int y) {
		this.x_ = x;
		this.y_ = y;
		this.repaint();
	}

	/**
	 * Returns the center X coordinate.
	 */
	public int getCenterX() {
		return this.x_;
	}

	/**
	 * Returns the center Y coordinate.
	 */
	public int getCenterY() {
		return this.y_;
	}

	/**
	 * Set the radius.
	 */
	public void setRadius(int radius) {
		this.r_ = radius;
		this.repaint();
	}

	/**
	 * Returns the radius.
	 */
	public int getRadius() {
		return this.r_;
	}

	private int x_;
	private int y_;
	private int r_;

	protected void updateDom(DomElement element, boolean all) {
		element.setAttribute("shape", "circle");
		StringWriter coords = new StringWriter();
		coords.append(String.valueOf(this.x_)).append(',').append(
				String.valueOf(this.y_)).append(',').append(
				String.valueOf(this.r_));
		element.setAttribute("coords", coords.toString());
		super.updateDom(element, all);
	}
}
