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
 * A value class that defines a rectangle.
 * <p>
 * 
 * The rectangle is defined by a top-left point and a width and height.
 */
public class WRectF {
	/**
	 * Default constructor.
	 * <p>
	 * Constructs a rectangle from top left point (<i>x=0</i>, <code>y=0</code>)
	 * and size <i>width=0</i> x <code>height=0</code>.
	 */
	public WRectF() {
		this.x_ = 0;
		this.y_ = 0;
		this.width_ = 0;
		this.height_ = 0;
	}

	/**
	 * Creates a rectangle.
	 * <p>
	 * Constructs a rectangle with top left point (<i>x</i>, <code>y</code>) and
	 * size <i>width</i> x <code>height</code>.
	 */
	public WRectF(double x, double y, double width, double height) {
		this.x_ = x;
		this.y_ = y;
		this.width_ = width;
		this.height_ = height;
	}

	/**
	 * Creates a rectangle.
	 * <p>
	 * Constructs a rectangle from the two points <code>topLeft</code> and
	 * <code>bottomRight</code>.
	 * <p>
	 * If you want to create a rectangle from two arbitrary corner points, you
	 * can use this constructor too, but should call
	 * {@link WRectF#getNormalized() getNormalized()} afterwords.
	 */
	public WRectF(WPointF topLeft, WPointF bottomRight) {
		this.x_ = topLeft.getX();
		this.y_ = topLeft.getY();
		this.width_ = bottomRight.getX() - topLeft.getX();
		this.height_ = bottomRight.getY() - topLeft.getY();
	}

	/**
	 * Internal assign method.
	 */
	public WRectF assign(WRectF rhs) {
		this.x_ = rhs.x_;
		this.y_ = rhs.y_;
		this.width_ = rhs.width_;
		this.height_ = rhs.height_;
		return this;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 */
	public boolean equals(WRectF rhs) {
		return this.x_ == rhs.x_ && this.y_ == rhs.y_
				&& this.width_ == rhs.width_ && this.height_ == rhs.height_;
	}

	// public boolean isNull() ;
	/**
	 * Determines whether or not this rectangle is empty.
	 * <p>
	 * A rectangle is empty if its width or its height is less than or equal to
	 * zero.
	 */
	public boolean isEmpty() {
		return this.width_ <= 0 || this.height_ <= 0;
	}

	/**
	 * Sets the X-position of the left side.
	 * <p>
	 * The right side of the rectangle does not move, and as a result, the
	 * rectangle may be resized.
	 */
	public void setX(double x) {
		this.width_ += this.x_ - x;
		this.x_ = x;
	}

	/**
	 * Sets the Y-position of the top side.
	 * <p>
	 * The bottom side of the rectangle does not move, and as a result, the
	 * rectangle may be resized.
	 */
	public void setY(double y) {
		this.height_ += this.y_ - y;
		this.y_ = y;
	}

	/**
	 * Sets the width.
	 * <p>
	 * The right side of the rectangle may move, but does not change the X
	 * position of the left side.
	 */
	public void setWidth(double width) {
		this.width_ = width;
	}

	/**
	 * Sets the Y-position of the top side.
	 * <p>
	 * The bottom side of the rectangle may move, but does not change the Y
	 * position of the top side.
	 */
	public void setHeight(double height) {
		this.height_ = height;
	}

	/**
	 * Returns the X-position of the left side.
	 * <p>
	 * This is equivalent to {@link WRectF#getLeft() getLeft()}.
	 * <p>
	 * 
	 * @see WRectF#getY()
	 * @see WRectF#getLeft()
	 */
	public double getX() {
		return this.x_;
	}

	/**
	 * Returns the Y-position of the top side.
	 * <p>
	 * This is equivalent to {@link WRectF#getTop() getTop()}.
	 * <p>
	 * 
	 * @see WRectF#getX()
	 * @see WRectF#getTop()
	 */
	public double getY() {
		return this.y_;
	}

	/**
	 * Returns the width.
	 * <p>
	 * 
	 * @see WRectF#getHeight()
	 */
	public double getWidth() {
		return this.width_;
	}

	/**
	 * Returns the height.
	 * <p>
	 * 
	 * @see WRectF#getWidth()
	 */
	public double getHeight() {
		return this.height_;
	}

	/**
	 * Returns the X position of the left side.
	 * <p>
	 * 
	 * @see WRectF#getX()
	 * @see WRectF#getRight()
	 */
	public double getLeft() {
		return this.x_;
	}

	/**
	 * Returns the Y position of the top side.
	 * <p>
	 * 
	 * @see WRectF#getY()
	 * @see WRectF#getBottom()
	 */
	public double getTop() {
		return this.y_;
	}

	/**
	 * Returns the X position of the right side.
	 * <p>
	 * 
	 * @see WRectF#getLeft()
	 */
	public double getRight() {
		return this.x_ + this.width_;
	}

	/**
	 * Returns the Y position of the bottom side.
	 * <p>
	 * 
	 * @see WRectF#getTop()
	 */
	public double getBottom() {
		return this.y_ + this.height_;
	}

	/**
	 * Returns the top left point.
	 * <p>
	 * 
	 * @see WRectF#getLeft()
	 * @see WRectF#getTop()
	 */
	public WPointF getTopLeft() {
		return new WPointF(this.x_, this.y_);
	}

	/**
	 * Returns the top right point.
	 * <p>
	 * 
	 * @see WRectF#getRight()
	 * @see WRectF#getTop()
	 */
	public WPointF getTopRight() {
		return new WPointF(this.x_ + this.width_, this.y_);
	}

	/**
	 * Returns the center point.
	 */
	public WPointF getCenter() {
		return new WPointF(this.x_ + this.width_ / 2, this.y_ + this.height_
				/ 2);
	}

	/**
	 * Returns the bottom left point.
	 * <p>
	 * 
	 * @see WRectF#getLeft()
	 * @see WRectF#getBottom()
	 */
	public WPointF getBottomLeft() {
		return new WPointF(this.x_, this.y_ + this.height_);
	}

	/**
	 * Returns the bottom right point.
	 * <p>
	 * 
	 * @see WRectF#getRight()
	 * @see WRectF#getBottom()
	 */
	public WPointF getBottomRight() {
		return new WPointF(this.x_ + this.width_, this.y_ + this.height_);
	}

	/**
	 * Tests if a rectangle contains a point.
	 */
	public boolean contains(WPointF p) {
		return this.contains(p.getX(), p.getY());
	}

	/**
	 * Tests if a rectangle contains a point.
	 */
	public boolean contains(double x, double y) {
		return x >= this.x_ && x <= this.x_ + this.width_ && y >= this.y_
				&& y <= this.y_ + this.height_;
	}

	/**
	 * Tests if two rectangles intersect.
	 */
	public boolean intersects(WRectF other) {
		if (this.isEmpty() || other.isEmpty()) {
			return false;
		} else {
			WRectF r1 = this.getNormalized();
			WRectF r2 = other.getNormalized();
			boolean intersectX = r2.getLeft() >= r1.getLeft()
					&& r2.getLeft() <= r1.getRight()
					|| r2.getRight() >= r1.getLeft()
					&& r2.getRight() <= r1.getRight();
			boolean intersectY = r2.getTop() >= r1.getTop()
					&& r2.getTop() <= r1.getBottom()
					|| r2.getBottom() >= r1.getTop()
					&& r2.getBottom() <= r1.getBottom();
			return intersectX && intersectY;
		}
	}

	/**
	 * Makes the union of to rectangles.
	 */
	public WRectF united(WRectF other) {
		if (this.isEmpty()) {
			return other;
		} else {
			if (other.isEmpty()) {
				return this;
			} else {
				WRectF r1 = this.getNormalized();
				WRectF r2 = other.getNormalized();
				double l = Math.min(r1.getLeft(), r2.getLeft());
				double r = Math.max(r1.getRight(), r2.getRight());
				double t = Math.min(r1.getTop(), r2.getTop());
				double b = Math.max(r1.getBottom(), r2.getBottom());
				return new WRectF(l, t, r - l, b - t);
			}
		}
	}

	/**
	 * Returns a normalized rectangle.
	 * <p>
	 * A normalized rectangle has a positive width and height.
	 */
	public WRectF getNormalized() {
		double x;
		double y;
		double w;
		double h;
		if (this.width_ > 0) {
			x = this.x_;
			w = this.width_;
		} else {
			x = this.x_ + this.width_;
			w = -this.width_;
		}
		if (this.height_ > 0) {
			y = this.y_;
			h = this.height_;
		} else {
			y = this.y_ + this.height_;
			h = -this.height_;
		}
		return new WRectF(x, y, w, h);
	}

	private double x_;
	private double y_;
	private double width_;
	private double height_;
}
