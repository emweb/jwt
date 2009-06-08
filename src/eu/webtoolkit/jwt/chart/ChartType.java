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

	public int getValue() {
		return ordinal();
	}
}
