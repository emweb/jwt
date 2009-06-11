package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates a direction.
 */
public enum Orientation {
	/**
	 * Horizontal.
	 */
	Horizontal,
	/**
	 * Vertical.
	 */
	Vertical;

	public int getValue() {
		return ordinal();
	}
}
