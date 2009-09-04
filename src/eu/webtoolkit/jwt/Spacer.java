/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


class Spacer extends WWebWidget {
	public Spacer() {
		super();
		this.setInline(false);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_DIV;
	}
}
