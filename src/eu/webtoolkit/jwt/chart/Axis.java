/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;


/**
 * Enumeration that indicates a chart axis.
 * 
 * @see WCartesianChart#getAxis(Axis axis)
 */
public enum Axis {
	/**
	 * X axis.
	 */
	X(0),
	/**
	 * First Y axis (== Y1).
	 */
	Y(1),
	/**
	 * Second Y Axis.
	 */
	Y2(2),
	/**
	 * Ordinate axis (== Y1 for a 2D plot).
	 */
	Ordinate(Axis.Y.getValue()),
	
	Y3D(3);
	
	public static Axis X3D = Axis.X;
	
	public static Axis Z3D = Axis.Y;
	
	static Axis Y1 = Y;
	

	private int value;

	Axis(int value) {
		this.value = value;
	}

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return value;
	}
}
