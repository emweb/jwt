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
 * Enumeration that indicates a chart axis.
 * 
 * @see WCartesianChart#axis(Axis axis)
 */
public enum Axis {
	/**
	 * X axis.
	 */
	XAxis(0),
	/**
	 * First Y axis (== Y1Axis).
	 */
	YAxis(1),
	/**
	 * First Y axis (== YAxis).
	 */
	Y1Axis(Axis.YAxis.getValue()),
	/**
	 * Second Y Axis.
	 */
	Y2Axis(2),
	/**
	 * Ordinate axis (== Y1Axis for a 2D plot).
	 */
	OrdinateAxis(Axis.YAxis.getValue());

	private int value;

	Axis(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
