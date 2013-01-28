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
 * A interactive area in a widget, specified by a circle.
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
	private static Logger logger = LoggerFactory.getLogger(WCircleArea.class);

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
	 * Creates a circular area with given geometry.
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
	 * Sets the center.
	 */
	public void setCenter(WPoint point) {
		this.setCenter(point.getX(), point.getY());
	}

	/**
	 * Sets the center.
	 */
	public void setCenter(WPointF point) {
		this.setCenter((int) point.getX(), (int) point.getY());
	}

	/**
	 * Sets the center.
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
	 * Sets the radius.
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

	protected boolean updateDom(DomElement element, boolean all) {
		element.setAttribute("shape", "circle");
		StringWriter coords = new StringWriter();
		coords.append(String.valueOf(this.x_)).append(',').append(
				String.valueOf(this.y_)).append(',').append(
				String.valueOf(this.r_));
		element.setAttribute("coords", coords.toString());
		return super.updateDom(element, all);
	}
}
