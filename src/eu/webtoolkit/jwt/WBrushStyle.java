package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates a fill style.
 */
public enum WBrushStyle {
	/**
	 * Do not fill.
	 */
	NoBrush(0),
	/**
	 * Fill with a solid color.
	 */
	SolidPattern(1);

	private int value;

	WBrushStyle(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
