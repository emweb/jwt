package eu.webtoolkit.jwt.chart;


/**
 * Enumeration that indicates a chart axis.
 * 
 * @see WCartesianChart#getAxis(Axis axis)
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
	 * Second Y Axis.
	 */
	Y2Axis(2),
	/**
	 * Ordinate axis (== Y1Axis for a 2D plot).
	 */
	OrdinateAxis(Axis.YAxis.getValue());

	static Axis Y1Axis = YAxis;

	private int value;

	Axis(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
