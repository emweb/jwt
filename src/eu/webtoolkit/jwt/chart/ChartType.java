/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;


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
