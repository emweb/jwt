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
 * Enumeration that specifies how an area should be filled.
 * <p>
 * Data series of type LineSeries or CurveSerie may be filled under or above the
 * line or curve. This enumeration specifies the other limit of this fill. Data
 * series of type BarSeries can use this setting to configure the bottom of the
 * chart.
 * <p>
 * 
 * @see WDataSeries#setFillRange(FillRangeType fillRange)
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
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

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
