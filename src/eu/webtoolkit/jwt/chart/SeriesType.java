/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;


/**
 * Enumeration that specifies the type of a chart series.
 * <p>
 * 
 * @see WDataSeries#setType(SeriesType type)
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
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

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
