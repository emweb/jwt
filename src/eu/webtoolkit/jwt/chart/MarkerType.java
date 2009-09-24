/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;


/**
 * Enumeration that specifies a type of point marker.
 * <p>
 * 
 * @see WDataSeries#setMarker(MarkerType marker)
 * @see WCartesianChart
 */
public enum MarkerType {
	/**
	 * Do not draw point markers.
	 */
	NoMarker,
	/**
	 * Mark points using a square.
	 */
	SquareMarker,
	/**
	 * Mark points using a circle.
	 */
	CircleMarker,
	/**
	 * Mark points using a cross (+).
	 */
	CrossMarker,
	/**
	 * Mark points using a cross (x).
	 */
	XCrossMarker,
	/**
	 * Mark points using a triangle.
	 */
	TriangleMarker;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
