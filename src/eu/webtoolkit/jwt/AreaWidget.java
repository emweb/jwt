/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;

class AreaWidget extends WInteractWidget {
	public AreaWidget(WAbstractArea facade) {
		super();
		this.facade_ = facade;
	}

	void repaint(EnumSet<RepaintFlag> flags) {
		super.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public WAbstractArea getFacade() {
		return this.facade_;
	}

	private WAbstractArea facade_;

	void updateDom(DomElement element, boolean all) {
		this.facade_.updateDom(element, all);
		super.updateDom(element, all);
		if (element.getProperty(Property.PropertyStyleCursor).length() != 0
				&& !WApplication.getInstance().getEnvironment().agentIsGecko()
				&& element.getAttribute("href").length() == 0) {
			element.setAttribute("href", "#");
		}
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_AREA;
	}
}
