package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates a sort order.
 */
public enum SortOrder {
	/**
	 * Ascending sort order.
	 */
	AscendingOrder,
	/**
	 * Descending sort order.
	 */
	DescendingOrder;

	public int getValue() {
		return ordinal();
	}
}
