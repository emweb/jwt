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

	public int getValue() {
		return ordinal();
	}
}
