/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;


/**
 * Enumeration that indicates a logical location for an axis.
 * <p>
 * The location is dependent on the values of the other axis.
 * <p>
 * 
 * @see WAxis#setLocation(AxisValue location)
 */
public enum AxisValue {
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

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
