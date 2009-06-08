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

class AreaWidget extends WInteractWidget {
	public AreaWidget(WAbstractArea facade) {
		super();
		this.facade_ = facade;
	}

	public void repaint(EnumSet<RepaintFlag> flags) {
		super.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public WAbstractArea getFacade() {
		return this.facade_;
	}

	private WAbstractArea facade_;

	protected void updateDom(DomElement element, boolean all) {
		this.facade_.updateDom(element, all);
		super.updateDom(element, all);
		if (element.getProperty(Property.PropertyStyleCursor).length() != 0
				&& !WApplication.instance().getEnvironment().agentIsGecko()
				&& element.getAttribute("href").length() == 0) {
			element.setAttribute("href", "#");
		}
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_AREA;
	}
}
