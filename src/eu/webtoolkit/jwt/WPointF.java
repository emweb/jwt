/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * Utility class that defines a 2D point.
 */
public class WPointF {
	/**
	 * Default constructor.
	 * <p>
	 * Constructs a point (<i>x=0</i>, <code>y=0</code>).
	 */
	public WPointF() {
		this.x_ = 0;
		this.y_ = 0;
	}

	/**
	 * Construct a point.
	 * <p>
	 * Constructs a point (<i>x</i>, <code>y</code>).
	 */
	public WPointF(double x, double y) {
		this.x_ = x;
		this.y_ = y;
	}

	/**
	 * Copy constructor.
	 */
	public WPointF(WPointF other) {
		this.x_ = other.getX();
		this.y_ = other.getY();
	}

	/**
	 * Construct a point from mouse coordinates.
	 */
	public WPointF(WMouseEvent.Coordinates other) {
		this.x_ = other.x;
		this.y_ = other.y;
	}

	/**
	 * Changes the X coordinate.
	 */
	public void setX(double x) {
		this.x_ = x;
	}

	/**
	 * Changes the Y coordinate.
	 */
	public void setY(double y) {
		this.y_ = y;
	}

	/**
	 * Returns the X coordinate.
	 */
	public double getX() {
		return this.x_;
	}

	/**
	 * Returns the Y coordinate.
	 */
	public double getY() {
		return this.y_;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 */
	public boolean equals(WPointF other) {
		return this.x_ == other.x_ && this.y_ == other.y_;
	}

	WPointF add(WPointF other) {
		this.x_ += other.x_;
		this.y_ += other.y_;
		return this;
	}

	private double x_;
	private double y_;
}
