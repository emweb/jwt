package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

class StdWidgetItemImpl extends StdLayoutItemImpl {
	public StdWidgetItemImpl(WWidgetItem item) {
		super();
		this.item_ = item;
	}

	public void destroy() {
	}

	public int getMinimumHeight() {
		return (int) this.item_.getWidget().getMinimumHeight().getToPixels();
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

	public void containerAddWidgets(WContainerWidget container) {
		container.addWidget(this.item_.getWidget());
	}

	public DomElement createDomElement(boolean fitWidth, boolean fitHeight,
			WApplication app) {
		DomElement d = this.item_.getWidget().createSDomElement(app);
		DomElement result = d;
		if (!app.getEnvironment().agentIsIE()
				&& !app.getEnvironment().agentIsOpera()
				&& (isTextArea(this.item_.getWidget())
						&& (fitWidth || fitHeight)
						|| d.getType() == DomElementType.DomElement_INPUT
						&& fitWidth || d.getType() == DomElementType.DomElement_BUTTON
						&& fitWidth
						&& app.getEnvironment().getAgent() == WEnvironment.UserAgent.Konqueror)) {
			result = DomElement.createNew(DomElementType.DomElement_DIV);
			String style = "height:100%;";
			if (fitWidth) {
				style += "margin-right:8px;";
			}
			if (fitHeight && d.getType() == DomElementType.DomElement_TEXTAREA) {
				style += "height:100%;";
			}
			result.setAttribute("style", style);
		}
		if (fitHeight
				&& d.getProperty(Property.PropertyStyleHeight).length() == 0) {
			if (d.getType() == DomElementType.DomElement_DIV
					|| d.getType() == DomElementType.DomElement_UL
					|| d.getType() == DomElementType.DomElement_TABLE
					|| d.getType() == DomElementType.DomElement_TEXTAREA) {
				d.setProperty(Property.PropertyStyleHeight, "100%");
			}
		}
		if (fitWidth
				&& d.getProperty(Property.PropertyStyleWidth).length() == 0) {
			if (d.getType() == DomElementType.DomElement_BUTTON
					|| d.getType() == DomElementType.DomElement_INPUT
					|| d.getType() == DomElementType.DomElement_TEXTAREA) {
				d.setProperty(Property.PropertyStyleWidth, "100%");
			}
		}
		if (result != d) {
			result.addChild(d);
		}
		return result;
	}

	public int getAdditionalVerticalPadding(boolean fitWidth, boolean fitHeight) {
		WApplication app = WApplication.instance();
		if (!app.getEnvironment().agentIsIE()
				&& !app.getEnvironment().agentIsOpera() && fitHeight
				&& isTextArea(this.item_.getWidget())) {
			return 5;
		} else {
			return 0;
		}
	}

	public void setHint(String name, String value) {
		WApplication.instance().log("error").append(
				"WWidgetItem: unrecognized hint '").append(name).append("'");
	}

	private WWidgetItem item_;

	static boolean isTextArea(WWidget w) {
		return ((w) instanceof WTextArea ? (WTextArea) (w) : null) != null
				&& !(((w) instanceof WTextEdit ? (WTextEdit) (w) : null) != null);
	}
}
