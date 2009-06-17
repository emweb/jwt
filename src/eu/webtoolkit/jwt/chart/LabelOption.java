package eu.webtoolkit.jwt.chart;


/**
 * Enumeration that specifies options for the labels.
 * <p>
 * 
 * @see WPieChart#setDisplayLabels(EnumSet options)
 */
public enum LabelOption {
	/**
	 * Do not display labels (default).
	 */
	NoLabels,
	/**
	 * Display labels inside each segment.
	 */
	Inside,
	/**
	 * Display labels outside each segment.
	 */
	Outside,
	/**
	 * Display the label text.
	 */
	TextLabel,
	/**
	 * Display the value (as percentage).
	 */
	TextPercentage;

	public int getValue() {
		return ordinal();
	}
}
