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

/**
 * An abstract base class for layout managers.
 * <p>
 * 
 * This class is the abstract base class for any layout manager. A layout
 * manager is associated with a container widget, and manages the layout of
 * children inside the whole space available to the container widget.
 * <p>
 * The implementation of the layout manager depends on the container widget to
 * which it is set, and is therefore deferred to WLayoutImpl.
 * <p>
 * A layout never assumes ownership of contained items, instead these are owned
 * by the parent widget to which the layout is applied.
 * <p>
 * <p>
 * <i><b>Note: </b>When applying a layout manager to a {@link WContainerWidget},
 * you may not define any padding for the container widget. Instead, use
 * {@link WLayout#setContentsMargins(int left, int top, int right, int bottom)
 * setContentsMargins()}. </i>
 * </p>
 */
public abstract class WLayout extends WObject implements WLayoutItem {
	/**
	 * Adds a layout <i>item</i>.
	 * <p>
	 * The item may be a widget or nested layout.
	 * <p>
	 * How the item is layed out with respect to siblings is implementation
	 * specific to the layout manager. In some cases, a layout manager will
	 * overload this method with extra arguments that specify layout options.
	 * <p>
	 * 
	 * @see WLayout#removeItem(WLayoutItem item)
	 * @see WLayout#addWidget(WWidget w)
	 */
	public abstract void addItem(WLayoutItem item);

	/**
	 * Adds the given <i>widget</i> to the layout.
	 * <p>
	 * This method wraps the widget in a {@link WWidgetItem} and calls
	 * {@link WLayout#addItem(WLayoutItem item) addItem()}.
	 * <p>
	 * How the widget is layed out with respect to siblings is implementation
	 * specific to the layout manager. In some cases, a layout manager will
	 * overload this method with extra arguments that specify layout options.
	 * <p>
	 * 
	 * @see WLayout#removeWidget(WWidget w)
	 * @see WLayout#addItem(WLayoutItem item)
	 */
	public void addWidget(WWidget w) {
		this.addItem(new WWidgetItem(w));
	}

	/**
	 * Removes a layout <i>item</i> (widget or nested layout).
	 * <p>
	 * 
	 * @see WLayout#addItem(WLayoutItem item)
	 * @see WLayout#removeWidget(WWidget w)
	 */
	public abstract void removeItem(WLayoutItem item);

	/**
	 * Removes the given <i>widget</i> from the layout.
	 * <p>
	 * This method finds the corresponding {@link WWidgetItem} and calls
	 * {@link WLayout#removeItem(WLayoutItem item) removeItem()}. The widget
	 * itself is not destroyed.
	 * <p>
	 * Returns <code>true</code> if succesful.
	 * <p>
	 * 
	 * @see WLayout#addWidget(WWidget w)
	 * @see WLayout#removeItem(WLayoutItem item)
	 */
	public boolean removeWidget(WWidget w) {
		WWidgetItem widgetItem = this.findWidgetItem(w);
		if (widgetItem != null) {
			widgetItem.getParentLayout().removeItem(widgetItem);
			;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the number of items in this layout.
	 * <p>
	 * This may be a theoretical number, which is greater than the actual number
	 * of items. It can be used to iterate over the items in the layout, in
	 * conjunction with {@link WLayout#getItemAt(int index) getItemAt()}.
	 */
	public abstract int getCount();

	/**
	 * Returns the layout item at a specific <i>index</i>.
	 * <p>
	 * If there is no item at the <code>index</code>, <code>null</code> is
	 * returned.
	 * <p>
	 * 
	 * @see WLayout#indexOf(WLayoutItem item)
	 * @see WLayout#getCount()
	 */
	public abstract WLayoutItem getItemAt(int index);

	/**
	 * Returns the index of a given <i>item</i>.
	 * <p>
	 * The default implementation loops over all items, and returns the index
	 * for which itemAt(index) equals <code>item</code>.
	 * <p>
	 * 
	 * @see WLayout#getItemAt(int index)
	 */
	public int indexOf(WLayoutItem item) {
		int c = this.getCount();
		for (int i = 0; i < c; ++i) {
			if (this.getItemAt(i) == item) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Finds the widget item associated with the given <i>widget</i>.
	 */
	public WWidgetItem findWidgetItem(WWidget widget) {
		int c = this.getCount();
		for (int i = 0; i < c; ++i) {
			WLayoutItem item = this.getItemAt(i);
			if (item != null) {
				WWidgetItem result = item.findWidgetItem(widget);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * Provides a hint to the layout implementation.
	 * <p>
	 * In some cases, a layout implementation may require some hints for
	 * rendering its contents. Possible hints are indicated in the reference
	 * documentation for each layout manager.
	 */
	public void setLayoutHint(String name, String value) {
		if (this.impl_ != null) {
			this.impl_.setHint(name, value);
		} else {
			if (!(this.hints_ != null)) {
				this.hints_ = new ArrayList<WLayout.Hint>();
			}
			this.hints_.add(new WLayout.Hint(name, value));
		}
	}

	public WWidget getWidget() {
		return null;
	}

	public WLayout getLayout() {
		return this;
	}

	public WLayout getParentLayout() {
		return ((this.getParent()) instanceof WLayout ? (WLayout) (this
				.getParent()) : null);
	}

	public WLayoutItemImpl getImpl() {
		return this.impl_;
	}

	/**
	 * Set contents margins (in pixels).
	 * <p>
	 * The default contents margins are 9 pixels in all directions.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Only used when the layout manager is applied to a
	 * {@link WContainerWidget}.</i>
	 * </p>
	 * 
	 * @see WLayout#setContentsMargins(int left, int top, int right, int bottom)
	 */
	public void setContentsMargins(int left, int top, int right, int bottom) {
		if (!(this.margins_ != null)) {
			this.margins_ = new int[4];
		}
		this.margins_[0] = left;
		this.margins_[1] = top;
		this.margins_[2] = right;
		this.margins_[3] = bottom;
	}

	/**
	 * Returns a contents margin.
	 * <p>
	 * 
	 * @see WLayout#setContentsMargins(int left, int top, int right, int bottom)
	 */
	public int getContentsMargin(Side side) {
		if (!(this.margins_ != null)) {
			return 9;
		}
		switch (side) {
		case Left:
			return this.margins_[0];
		case Top:
			return this.margins_[1];
		case Right:
			return this.margins_[2];
		case Bottom:
			return this.margins_[3];
		default:
			return 9;
		}
	}

	/**
	 * Create a layout.
	 */
	protected WLayout() {
		super();
		this.margins_ = null;
		this.impl_ = null;
		this.hints_ = null;
	}

	/**
	 * Update the layout.
	 * <p>
	 * Must be called whenever some properties of the layout have changed.
	 */
	protected void update(WLayoutItem item) {
		if (this.impl_ != null) {
			this.impl_.update(item);
		}
	}

	/**
	 * Update the layout.
	 * <p>
	 * Calls {@link #update(WLayoutItem item) update((WLayoutItem)null)}
	 */
	protected final void update() {
		update((WLayoutItem) null);
	}

	/**
	 * Update the layout, adding the given layout <i>item</i>.
	 * <p>
	 * Must be called from the implementation of
	 * {@link WLayout#addItem(WLayoutItem item) addItem()}
	 */
	protected void updateAddItem(WLayoutItem item) {
		if (item.getParentLayout() != null) {
			throw new WtException("Cannot add item to two Layouts");
		}
		item.setParentLayout(this);
		if (this.impl_ != null) {
			item.setParentWidget(this.impl_.getParentWidget());
			this.impl_.updateAddItem(item);
		}
	}

	/**
	 * Update the layout, remove the given layout <i>item</i>.
	 * <p>
	 * Must be called from the implementation of
	 * {@link WLayout#removeItem(WLayoutItem item) removeItem()}
	 */
	protected void updateRemoveItem(WLayoutItem item) {
		if (this.impl_ != null) {
			this.impl_.updateRemoveItem(item);
		}
		item.setParentLayout((WLayout) null);
	}

	/**
	 * Set the layout in the <i>parent</i>.
	 * <p>
	 * Must be called from the constructor after the layout has been fully
	 * created (since it will call virtual methods {@link WLayout#getCount()
	 * getCount()} and {@link WLayout#getItemAt(int index) getItemAt()}).
	 */
	protected void setLayoutInParent(WWidget parent) {
		parent.setLayout(this);
	}

	static class Hint {
		public Hint(String aName, String aValue) {
			this.name = aName;
			this.value = aValue;
		}

		public String name;
		public String value;
	}

	private int[] margins_;
	private WLayoutItemImpl impl_;
	private List<WLayout.Hint> hints_;

	public void setParentWidget(WWidget parent) {
		if (!(this.getParent() != null)) {
			this.setParent(parent);
		}
		assert !(this.impl_ != null);
		int c = this.getCount();
		for (int i = 0; i < c; ++i) {
			WLayoutItem item = this.getItemAt(i);
			if (item != null) {
				item.setParentWidget(parent);
			}
		}
		this.impl_ = parent.createLayoutItemImpl(this);
		if (this.hints_ != null) {
			for (int i = 0; i < this.hints_.size(); ++i) {
				this.impl_.setHint(this.hints_.get(i).name,
						this.hints_.get(i).value);
			}
			;
			this.hints_ = null;
		}
	}

	public void setParentLayout(WLayout layout) {
		if (layout != null) {
			layout.addChild(this);
		} else {
			this.setParent((WObject) null);
		}
	}
}
