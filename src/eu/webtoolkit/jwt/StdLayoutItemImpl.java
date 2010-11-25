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

abstract class StdLayoutItemImpl extends WObject implements WLayoutItemImpl {
	public StdLayoutItemImpl() {
		super();
	}

	public WContainerWidget getContainer() {
		StdLayoutImpl p = this.getParentLayoutImpl();
		if (p != null) {
			return p.getContainer();
		} else {
			return null;
		}
	}

	public abstract WLayoutItem getLayoutItem();

	public WWidget getParentWidget() {
		return this.getContainer();
	}

	public abstract int getMinimumHeight();

	public StdLayoutImpl getParentLayoutImpl() {
		WLayoutItem i = this.getLayoutItem();
		if (i.getParentLayout() != null) {
			return ((i.getParentLayout().getImpl()) instanceof StdLayoutImpl ? (StdLayoutImpl) (i
					.getParentLayout().getImpl())
					: null);
		} else {
			return null;
		}
	}

	abstract void containerAddWidgets(WContainerWidget container);

	public abstract DomElement createDomElement(boolean fitWidth,
			boolean fitHeight, WApplication app);
}
