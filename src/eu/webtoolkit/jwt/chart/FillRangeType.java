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
 * Enumeration that specifies how an area should be filled.
 * 
 * Data series of type LineSeries or CurveSerie may be filled under or above the
 * line or curve. This enumeration specifies the other limit of this fill.
 * <p>
 * 
 * @see WDataSeries#setFillRange(FillRangeType fillRange)
 * @see WCartesianChart
 */
public enum FillRangeType {
	/**
	 * Do not fill under the curve.
	 */
	NoFill,
	/**
	 * Fill from the curve to the chart bottom (min).
	 */
	MinimumValueFill,
	/**
	 * Fill from the curve to the chart top.
	 */
	MaximumValueFill,
	/**
	 * Fill from the curve to the zero Y value.
	 */
	ZeroValueFill;

	public int getValue() {
		return ordinal();
	}
}
