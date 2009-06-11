package eu.webtoolkit.jwt.chart;


/**
 * Enumeration that specifies a type of point marker.
 * 
 * @see WDataSeries#setMarker(MarkerType marker)
 * @see WCartesianChart
 */
public enum MarkerType {
	/**
	 * Do not draw point markers.
	 */
	NoMarker,
	/**
	 * Mark points using a square.
	 */
	SquareMarker,
	/**
	 * Mark points using a circle.
	 */
	CircleMarker,
	/**
	 * Mark points using a cross (+).
	 */
	CrossMarker,
	/**
	 * Mark points using a cross (x).
	 */
	XCrossMarker,
	/**
	 * Mark points using a triangle.
	 */
	TriangleMarker;

	public int getValue() {
		return ordinal();
	}
}
