/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
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
