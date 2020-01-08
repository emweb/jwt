/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class StdLayoutImpl extends StdLayoutItemImpl {
	private static Logger logger = LoggerFactory.getLogger(StdLayoutImpl.class);

	public StdLayoutImpl(WLayout layout) {
		super();
		this.layout_ = layout;
		this.container_ = null;
	}

	public void updateAddItem(WLayoutItem item) {
		WContainerWidget c = this.getContainer();
		if (c != null) {
			getImpl(item).containerAddWidgets(c);
			this.update(item);
		}
	}

	public void updateRemoveItem(WLayoutItem item) {
		WContainerWidget c = this.getContainer();
		if (c != null) {
			this.update(item);
			getImpl(item).containerAddWidgets((WContainerWidget) null);
		}
	}

	public abstract void update(WLayoutItem anon1);

	public abstract void updateDom(final DomElement parent);

	public abstract boolean itemResized(WLayoutItem item);

	public abstract boolean isParentResized();

	public WContainerWidget getContainer() {
		if (this.container_ != null) {
			return this.container_;
		} else {
			return super.getContainer();
		}
	}

	public WLayoutItem getLayoutItem() {
		return this.layout_;
	}

	void containerAddWidgets(WContainerWidget container) {
		int c = this.layout_.getCount();
		for (int i = 0; i < c; ++i) {
			WLayoutItem item = this.layout_.getItemAt(i);
			if (item != null) {
				getImpl(item).containerAddWidgets(container);
			}
		}
	}

	protected WLayout getLayout() {
		return this.layout_;
	}

	protected static StdLayoutItemImpl getImpl(WLayoutItem item) {
		return ((item.getImpl()) instanceof StdLayoutItemImpl ? (StdLayoutItemImpl) (item
				.getImpl()) : null);
	}

	private WLayout layout_;
	private WContainerWidget container_;

	void setContainer(WContainerWidget c) {
		for (int i = c.getCount(); i > 0; --i) {
			WWidget w = c.getWidget(i - 1);
			if (!(this.layout_.findWidgetItem(w) != null)) {
				c.removeWidget(w);
			}
		}
		this.container_ = c;
		this.containerAddWidgets(this.container_);
	}
}
