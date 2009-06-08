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
 * Enumeration that indicates a logical location for an axis.
 * 
 * The location is dependent on the values of the other axis.
 * <p>
 * 
 * @see WAxis#setLocation(AxisLocation location)
 */
public enum AxisLocation {
	/**
	 * At the minimum value.
	 */
	MinimumValue,
	/**
	 * At the maximum value.
	 */
	MaximumValue,
	/**
	 * At the zero value (if displayed).
	 */
	ZeroValue;

	public int getValue() {
		return ordinal();
	}
}
