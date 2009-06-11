package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates how line end points are rendered.
 */
public enum PenCapStyle {
	/**
	 * Flat ends.
	 */
	FlatCap,
	/**
	 * Square ends (prolongs line with half width).
	 */
	SquareCap,
	/**
	 * Round ends (terminates with a half circle).
	 */
	RoundCap;

	public int getValue() {
		return ordinal();
	}
}
