/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

class StdWidgetItemImpl extends StdLayoutItemImpl {
	public StdWidgetItemImpl(WWidgetItem item) {
		super();
		this.item_ = item;
	}

	public int getMinimumHeight() {
		return (int) this.item_.getWidget().getMinimumHeight().toPixels();
	}

	public void updateAddItem(WLayoutItem anon1) {
		assert false;
	}

	public void updateRemoveItem(WLayoutItem anon1) {
		assert false;
	}

	public void update(WLayoutItem anon1) {
		assert false;
	}

	public WLayoutItem getLayoutItem() {
		return this.item_;
	}

	void containerAddWidgets(WContainerWidget container) {
		if (container != null) {
			container.addWidget(this.item_.getWidget());
		} else {
			WContainerWidget wc = ((this.item_.getWidget().getParent()) instanceof WContainerWidget ? (WContainerWidget) (this.item_
					.getWidget().getParent())
					: null);
			if (wc != null) {
				wc.removeFromLayout(this.item_.getWidget());
			}
		}
	}

	public DomElement createDomElement(boolean fitWidth, boolean fitHeight,
			WApplication app) {
		WWidget w = this.item_.getWidget();
		DomElement d = w.createSDomElement(app);
		DomElement result = d;
		int marginRight = 0;
		int marginBottom = 0;
		boolean boxSizing = !app.getEnvironment().agentIsIElt(9);
		if (!boxSizing) {
			if (fitWidth) {
				marginRight = (w.boxPadding(Orientation.Horizontal) + w
						.boxBorder(Orientation.Horizontal)) * 2;
			}
			if (fitHeight) {
				marginBottom = (w.boxPadding(Orientation.Vertical) + w
						.boxBorder(Orientation.Vertical)) * 2;
			}
			boolean forceDiv = fitHeight
					&& d.getType() == DomElementType.DomElement_SELECT
					&& d.getAttribute("size").length() == 0;
			if (marginRight != 0 || marginBottom != 0 || forceDiv) {
				result = DomElement.createNew(DomElementType.DomElement_DIV);
				result.setProperty(Property.PropertyClass, "Wt-wrapdiv");
				StringWriter style = new StringWriter();
				if (app.getEnvironment().agentIsIElt(9) && !forceDiv) {
					style.append("margin-top:-1px;");
					marginBottom -= 1;
				}
				if (marginRight != 0) {
					style
							.append(
									app.getLayoutDirection() == LayoutDirection.LeftToRight ? "margin-right:"
											: "margin-left:").append(
									String.valueOf(marginRight)).append("px;");
				}
				if (marginBottom != 0) {
					style.append("margin-bottom:").append(
							String.valueOf(marginBottom)).append("px;");
				}
				result.setProperty(Property.PropertyStyle, style.toString());
			}
		}
		if (fitHeight
				&& d.getProperty(Property.PropertyStyleHeight).length() == 0) {
			if (d.getType() == DomElementType.DomElement_DIV
					&& !app.getEnvironment().agentIsWebKit()
					|| d.getType() == DomElementType.DomElement_UL
					|| d.getType() == DomElementType.DomElement_INPUT
					|| d.getType() == DomElementType.DomElement_TABLE
					|| d.getType() == DomElementType.DomElement_TEXTAREA) {
				d.setProperty(Property.PropertyStyleHeight, "100%");
			}
		}
		if (fitWidth
				&& d.getProperty(Property.PropertyStyleWidth).length() == 0) {
			if (d.getType() == DomElementType.DomElement_BUTTON
					|| d.getType() == DomElementType.DomElement_INPUT
					&& !d.getAttribute("type").equals("radio")
					&& !d.getAttribute("type").equals("checkbox")
					|| d.getType() == DomElementType.DomElement_SELECT
					&& !app.getEnvironment().agentIsIE()
					|| d.getType() == DomElementType.DomElement_TEXTAREA) {
				d.setProperty(Property.PropertyStyleWidth, "100%");
			}
		}
		if (result != d) {
			result.addChild(d);
		}
		return result;
	}

	public void setHint(String name, String value) {
		WApplication.getInstance().log("error").append(
				"WWidgetItem: unrecognized hint '").append(name).append("'");
	}

	private WWidgetItem item_;
}
