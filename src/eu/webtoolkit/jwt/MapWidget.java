/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


class MapWidget extends WContainerWidget {
	public MapWidget() {
		super();
	}

	protected void updateDom(DomElement element, boolean all) {
		if (all) {
			element.setAttribute("name", this.getFormName());
		}
		super.updateDom(element, all);
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_MAP;
	}
}
