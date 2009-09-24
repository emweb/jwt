/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;


/**
 * Enumeration that specifies how an area should be filled.
 * <p>
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

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
