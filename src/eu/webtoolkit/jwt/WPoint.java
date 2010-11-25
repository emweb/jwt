/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

/**
 * Utility class that defines a 2D point with integer coordinates.
 * <p>
 * 
 * @see WPolygonArea
 */
public class WPoint {
	/**
	 * Creates a point (0, 0).
	 */
	public WPoint() {
		this.x_ = 0;
		this.y_ = 0;
	}

	/**
	 * Creates a point (x, y).
	 */
	public WPoint(int x, int y) {
		this.x_ = x;
		this.y_ = y;
	}

	/**
	 * Sets the X coordinate.
	 */
	public void setX(int x) {
		this.x_ = x;
	}

	/**
	 * Sets the Y coordinate.
	 */
	public void setY(int y) {
		this.y_ = y;
	}

	/**
	 * Returns the X coordinate.
	 */
	public int getX() {
		return this.x_;
	}

	/**
	 * Returns the Y coordinate.
	 */
	public int getY() {
		return this.y_;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 */
	public boolean equals(WPoint other) {
		return this.x_ == other.x_ && this.y_ == other.y_;
	}

	WPoint add(WPoint other) {
		this.x_ += other.x_;
		this.y_ += other.y_;
		return this;
	}

	private int x_;
	private int y_;
}
