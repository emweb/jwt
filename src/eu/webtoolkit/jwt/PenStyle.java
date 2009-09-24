/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates a pen style.
 */
public enum PenStyle {
	/**
	 * Do not stroke.
	 */
	NoPen,
	/**
	 * Stroke with a solid line.
	 */
	SolidLine,
	/**
	 * Stroked with a dashed line.
	 */
	DashLine,
	/**
	 * Stroke with a dotted line.
	 */
	DotLine,
	/**
	 * Stroke with a dash dot line.
	 */
	DashDotLine,
	/**
	 * Stroke with a dash dot dot line.
	 */
	DashDotDotLine;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
