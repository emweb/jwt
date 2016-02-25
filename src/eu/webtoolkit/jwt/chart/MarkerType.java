/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

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
 * Enumeration that specifies a type of point marker.
 * <p>
 * 
 * @see WDataSeries#setMarker(MarkerType marker)
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
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
	TriangleMarker,
	/**
	 * Mark points using a custom marker.
	 */
	CustomMarker,
	/**
	 * Mark points using a star.
	 */
	StarMarker,
	/**
	 * Mark points using an inverted (upside-down) triangle.
	 */
	InvertedTriangleMarker,
	/**
	 * Mark points using an asterisk (*).
	 */
	AsteriskMarker,
	/**
	 * Mark points using a diamond.
	 */
	DiamondMarker;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
