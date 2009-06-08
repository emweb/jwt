package eu.webtoolkit.jwt.chart;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * Enumeration that specifies a type of point marker.
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

	public int getValue() {
		return ordinal();
	}
}
