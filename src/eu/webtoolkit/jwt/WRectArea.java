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
 * A interactive area in a widget, specified by a rectangle.
 * <p>
 * 
 * The area may be added to a {@link WImage} or {@link WPaintedWidget} to
 * provide interactivity on a rectangular area of the image. The rectangle is
 * specified in pixel coordinates.
 * <p>
 * 
 * @see WImage#addArea(WAbstractArea area)
 * @see WPaintedWidget#addArea(WAbstractArea area)
 * @see WCircleArea
 * @see WPolygonArea
 */
public class WRectArea extends WAbstractArea {
	private static Logger logger = LoggerFactory.getLogger(WRectArea.class);

	/**
	 * Default constructor.
	 * <p>
	 * The default constructor creates a rectangular area spans the whole
	 * widget.
	 */
	public WRectArea() {
		super();
		this.x_ = 0;
		this.y_ = 0;
		this.width_ = 0;
		this.height_ = 0;
	}

	/**
	 * Creates a rectangular area with given geometry.
	 * <p>
	 * The arguments are in pixel units.
	 */
	public WRectArea(int x, int y, int width, int height) {
		super();
		this.x_ = x;
		this.y_ = y;
		this.width_ = width;
		this.height_ = height;
	}

	/**
	 * Creates a rectangular area with given geometry.
	 * <p>
	 * The arguments are in pixel units.
	 */
	public WRectArea(double x, double y, double width, double height) {
		super();
		this.x_ = (int) x;
		this.y_ = (int) y;
		this.width_ = (int) width;
		this.height_ = (int) height;
	}

	/**
	 * Creates a rectangular area with given geometry.
	 * <p>
	 * The <code>rect</code> argument is in pixel units.
	 */
	public WRectArea(WRectF rect) {
		super();
		this.x_ = (int) rect.getX();
		this.y_ = (int) rect.getY();
		this.width_ = (int) rect.getWidth();
		this.height_ = (int) rect.getHeight();
	}

	/**
	 * Sets the top-left X coordinate.
	 */
	public void setX(int x) {
		this.x_ = x;
		this.repaint();
	}

	/**
	 * Returns the top-left X coordinate.
	 */
	public int getX() {
		return this.x_;
	}

	/**
	 * Sets the top-left Y coordinate.
	 */
	public void setY(int y) {
		this.y_ = y;
		this.repaint();
	}

	/**
	 * Returns the top-left Y coordinate.
	 */
	public int getY() {
		return this.y_;
	}

	/**
	 * Sets the width.
	 */
	public void setWidth(int width) {
		this.width_ = width;
		this.repaint();
	}

	/**
	 * Returns the width.
	 */
	public int getWidth() {
		return this.width_;
	}

	/**
	 * Sets the height.
	 */
	public void setHeight(int height) {
		this.height_ = height;
		this.repaint();
	}

	/**
	 * Returns the height.
	 */
	public int getHeight() {
		return this.height_;
	}

	private int x_;
	private int y_;
	private int width_;
	private int height_;

	protected boolean updateDom(DomElement element, boolean all) {
		element.setAttribute("shape", "rect");
		StringWriter coords = new StringWriter();
		if (this.x_ == 0 && this.y_ == 0 && this.width_ == 0
				&& this.height_ == 0) {
			coords.append("0%,0%,100%,100%");
		} else {
			coords.append(String.valueOf(this.x_)).append(',').append(
					String.valueOf(this.y_)).append(',').append(
					String.valueOf(this.x_ + this.width_)).append(',').append(
					String.valueOf(this.y_ + this.height_));
		}
		element.setAttribute("coords", coords.toString());
		return super.updateDom(element, all);
	}
}
