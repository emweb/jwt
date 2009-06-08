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
 * Enumeration that indicates a scale for an axis.
 * 
 * The scale determines how values are mapped onto an axis.
 * <p>
 * 
 * @see WAxis#setScale(AxisScale scale)
 */
public enum AxisScale {
	/**
	 * A category scale is set as the scale for the X axis in a
	 * {@link ChartType#CategoryChart CategoryChart}, and is only applicable
	 * there. It lists all values, evenly spaced, and consecutively in the order
	 * of the model.
	 */
	CategoryScale(0),
	/**
	 * A linear scale is the default scale for all axes, except for the X scale
	 * in a CategoryScale. It maps values in a linear fashion on the axis.
	 */
	LinearScale(1),
	/**
	 * A logarithmic scale is useful for plotting values with of a large range,
	 * but only works for positive values.
	 */
	LogScale(2),
	/**
	 * A date scale is a special linear scale, which is useful for the X axis in
	 * a ScatterPlot, when the X series contain dates (of type {@link WDate}).
	 */
	DateScale(3);

	private int value;

	AxisScale(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
