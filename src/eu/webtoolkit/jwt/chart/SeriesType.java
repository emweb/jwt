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
 * Enumeration that specifies the type of a chart series.
 * 
 * @see WDataSeries#setType(SeriesType type)
 * @see WCartesianChart
 */
public enum SeriesType {
	/**
	 * Series rendered solely as point markers.
	 */
	PointSeries,
	/**
	 * Series rendered as points connected by straight lines.
	 */
	LineSeries,
	/**
	 * Series rendered as points connected by curves.
	 */
	CurveSeries,
	/**
	 * Series rendered as bars.
	 */
	BarSeries;

	public int getValue() {
		return ordinal();
	}
}
