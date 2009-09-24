/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
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
