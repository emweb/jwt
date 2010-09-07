/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


abstract class StdLayoutImpl extends StdLayoutItemImpl {
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

	public void update(WLayoutItem item) {
		WContainerWidget c = this.getContainer();
		if (c != null) {
			c.layoutChanged();
		}
	}

	public abstract void updateDom();

	public abstract boolean itemResized(WLayoutItem item);

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
				.getImpl())
				: null);
	}

	private WLayout layout_;
	private WContainerWidget container_;

	void setContainer(WContainerWidget c) {
		if (c.getCount() != 0) {
			while (c.getCount() != 0) {
				c.removeWidget(c.getWidget(0));
			}
		}
		this.container_ = c;
		this.containerAddWidgets(this.container_);
	}
}
