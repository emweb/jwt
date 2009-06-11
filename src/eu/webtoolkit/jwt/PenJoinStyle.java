package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates how line joins are rendered.
 */
public enum PenJoinStyle {
	/**
	 * Pointy joins.
	 */
	MiterJoin,
	/**
	 * Squared-off joins.
	 */
	BevelJoin,
	/**
	 * Rounded joins.
	 */
	RoundJoin;

	public int getValue() {
		return ordinal();
	}
}
