/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


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

	public WWidget getParent() {
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
