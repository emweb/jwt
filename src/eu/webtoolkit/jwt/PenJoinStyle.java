/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
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

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
