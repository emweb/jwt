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

abstract class StdLayoutImpl extends StdLayoutItemImpl {
	public StdLayoutImpl(WLayout layout) {
		super();
		this.layout_ = layout;
		this.container_ = null;
	}

	public void destroy() {
		if (this.container_ != null) {
			this.container_.layoutChanged(true);
		}
	}

	public void updateAddItem(WLayoutItem item) {
		WContainerWidget c = this.getContainer();
		if (c != null) {
			getImpl(item).containerAddWidgets(c);
			this.update(item);
		}
	}

	public void updateRemoveItem(WLayoutItem item) {
		this.update(item);
	}

	public void update(WLayoutItem item) {
		WContainerWidget c = this.getContainer();
		if (c != null) {
			c.layoutChanged();
		}
	}

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

	protected void containerAddWidgets(WContainerWidget container) {
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
				.getImpl())
				: null);
	}

	private WLayout layout_;
	private WContainerWidget container_;

	void setContainer(WContainerWidget c) {
		if (c.getCount() != 0) {
			WApplication
					.getInstance()
					.log("warn")
					.append(
							"WContainerWidget: applying Layout manager to non-empty WContainerWidget. Container is cleared.");
			while (c.getCount() != 0) {
				c.removeWidget(c.getWidget(0));
			}
		}
		this.container_ = c;
		this.containerAddWidgets(this.container_);
	}
}
