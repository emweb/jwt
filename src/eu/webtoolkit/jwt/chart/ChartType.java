/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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
 * Enumeration type that indicates a chart type for a cartesian chart.
 */
public enum ChartType {
	/**
	 * The X series are categories.
	 */
	CategoryChart,
	/**
	 * The X series must be interpreted as numerical data.
	 */
	ScatterPlot;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
