/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


enum VectorFormat {
	SvgFormat, VmlFormat;

	public int getValue() {
		return ordinal();
	}
}
