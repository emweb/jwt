/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base class for layout managers.
 *
 * <p>This class is the abstract base class for any layout manager. A layout manager is associated
 * with a container widget, and manages the layout of children inside the whole space available to
 * the container widget.
 *
 * <p>The implementation of the layout manager depends on the container widget to which it is set,
 * and is therefore deferred to {@link WLayoutImpl}.
 *
 * <p>
 *
 * <p><i><b>Note: </b>When applying a layout manager to a {@link WContainerWidget}, you may not
 * define any padding for the container widget. Instead, use {@link WLayout#setContentsMargins(int
 * left, int top, int right, int bottom) setContentsMargins()}. </i>
 */
public abstract class WLayout extends WObject implements WLayoutItem {
  private static Logger logger = LoggerFactory.getLogger(WLayout.class);

  /**
   * Set the preferred layout implementation.
   *
   * <p>The default implementation for box layouts and fit layouts is {@link
   * LayoutImplementation#Flex} (if supported by the browser). Otherwise a fallback to {@link
   * LayoutImplementation#JavaScript} is used.
   *
   * <p>
   *
   * @see WLayout#setDefaultImplementation(LayoutImplementation implementation)
   */
  public void setPreferredImplementation(LayoutImplementation implementation) {
    if (this.preferredImplementation_ != implementation) {
      this.preferredImplementation_ = implementation;
      if (this.impl_ != null && this.getImplementation() != this.getPreferredImplementation()) {
        this.updateImplementation();
      }
    }
  }
  /**
   * Sets the preferred layout implementation globally.
   *
   * <p>The default implementation for box layouts and fit layouts is {@link
   * LayoutImplementation#Flex} (if supported by the browser). Otherwise a fallback to {@link
   * LayoutImplementation#JavaScript} is used.
   *
   * <p>Because there are cases where {@link LayoutImplementation#Flex} does not work properly, this
   * method can be used to set the global preferred implementation to instead. Since this is a
   * system-wide setting, and not a per-session setting, you should call this function before any
   * session is created, e.g. in main() before calling WRun(). setPreferredImplementation()
   */
  public static void setDefaultImplementation(LayoutImplementation implementation) {
    defaultImplementation_ = implementation;
  }
  /**
   * Adds a layout <i>item</i>.
   *
   * <p>The item may be a widget or nested layout.
   *
   * <p>How the item is layed out with respect to siblings is implementation specific to the layout
   * manager. In some cases, a layout manager will overload this method with extra arguments that
   * specify layout options.
   *
   * <p>
   *
   * @see WLayout#removeItem(WLayoutItem item)
   */
  public abstract void addItem(WLayoutItem item);
  /**
   * Adds the given <i>widget</i> to the layout.
   *
   * <p>This method wraps the widget in a {@link WWidgetItem} and calls addItem(WLayoutItem *).
   *
   * <p>How the widget is layed out with respect to siblings is implementation specific to the
   * layout manager. In some cases, a layout manager will overload this method with extra arguments
   * that specify layout options.
   *
   * <p>
   *
   * @see WLayout#removeWidget(WWidget w)
   */
  public void addWidget(WWidget w) {
    this.addItem(new WWidgetItem(w));
  }
  /**
   * Removes a layout <i>item</i> (widget or nested layout).
   *
   * <p>
   *
   * @see WLayout#removeWidget(WWidget w)
   */
  public abstract WLayoutItem removeItem(WLayoutItem item);
  /**
   * Removes the given <i>widget</i> from the layout.
   *
   * <p>This method finds the corresponding {@link WWidgetItem} and calls {@link
   * WLayout#removeItem(WLayoutItem item) removeItem()}, and returns the widget.
   *
   * <p>
   *
   * @see WLayout#removeItem(WLayoutItem item)
   */
  public WWidget removeWidget(WWidget w) {
    WWidgetItem widgetItem = this.findWidgetItem(w);
    if (widgetItem != null) {
      WLayoutItem wi = widgetItem.getParentLayout().removeItem(widgetItem);
      return widgetItem.getTakeWidget();
    } else {
      return null;
    }
  }
  /**
   * Returns the number of items in this layout.
   *
   * <p>This may be a theoretical number, which is greater than the actual number of items. It can
   * be used to iterate over the items in the layout, in conjunction with {@link
   * WLayout#getItemAt(int index) getItemAt()}.
   */
  public abstract int getCount();
  /**
   * Returns the layout item at a specific <i>index</i>.
   *
   * <p>If there is no item at the <code>index</code>, <code>null</code> is returned.
   *
   * <p>
   *
   * @see WLayout#indexOf(WLayoutItem item)
   * @see WLayout#getCount()
   */
  public abstract WLayoutItem getItemAt(int index);
  /**
   * Returns the index of a given <i>item</i>.
   *
   * <p>The default implementation loops over all items, and returns the index for which
   * itemAt(index) equals <code>item</code>.
   *
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
  /** Finds the widget item associated with the given <i>widget</i>. */
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

  public WWidget getWidget() {
    return null;
  }

  public WLayout getLayout() {
    return this;
  }

  public WLayout getParentLayout() {
    return this.parentLayout_;
  }

  public WWidget getParentWidget() {
    if (this.parentWidget_ != null) {
      return this.parentWidget_;
    } else {
      if (this.parentLayout_ != null) {
        return this.parentLayout_.getParentWidget();
      } else {
        return null;
      }
    }
  }

  public WLayoutImpl getImpl() {
    return this.impl_;
  }
  /**
   * Set contents margins (in pixels).
   *
   * <p>The default contents margins are 9 pixels in all directions.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Only used when the layout manager is applied to a {@link WContainerWidget}.
   * </i>
   *
   * @see WLayout#setContentsMargins(int left, int top, int right, int bottom)
   */
  public void setContentsMargins(int left, int top, int right, int bottom) {
    this.margins_[0] = left;
    this.margins_[1] = top;
    this.margins_[2] = right;
    this.margins_[3] = bottom;
  }
  /**
   * Returns a contents margin.
   *
   * <p>
   *
   * @see WLayout#setContentsMargins(int left, int top, int right, int bottom)
   */
  public int getContentsMargin(Side side) {
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

  public boolean isImplementationIsFlexLayout() {
    return false;
  }
  /** Create a layout. */
  protected WLayout() {
    super();
    this.parentLayout_ = null;
    this.parentWidget_ = null;
    this.impl_ = null;
    this.preferredImplementation_ = defaultImplementation_;
    this.margins_[0] = this.margins_[1] = this.margins_[2] = this.margins_[3] = 9;
  }
  /**
   * Update the layout.
   *
   * <p>Must be called whenever some properties of the layout have changed.
   */
  protected void update(WLayoutItem item) {
    if (this.impl_ != null) {
      this.impl_.update();
    }
  }
  /**
   * Update the layout.
   *
   * <p>Calls {@link #update(WLayoutItem item) update((WLayoutItem)null)}
   */
  protected final void update() {
    update((WLayoutItem) null);
  }

  protected void itemAdded(WLayoutItem item) {
    item.setParentLayout(this);
    WWidget w = this.getParentWidget();
    if (w != null) {
      item.setParentWidget(w);
    }
    if (this.impl_ != null) {
      this.impl_.itemAdded(item);
    }
  }

  protected void itemRemoved(WLayoutItem item) {
    if (this.impl_ != null) {
      this.impl_.itemRemoved(item);
    }
    item.setParentWidget((WWidget) null);
    item.setParentLayout((WLayout) null);
  }

  public void setParentWidget(WWidget parent) {
    this.parentWidget_ = parent;
    int c = this.getCount();
    for (int i = 0; i < c; ++i) {
      WLayoutItem item = this.getItemAt(i);
      if (item != null) {
        item.setParentWidget(parent);
      }
    }
    if (!(parent != null)) {
      this.impl_ = null;
    }
  }

  protected void setImpl(WLayoutImpl impl) {
    this.impl_ = impl;
  }

  protected LayoutImplementation getImplementation() {
    if (ObjectUtils.cast(this.impl_, StdGridLayoutImpl2.class) != null) {
      return LayoutImplementation.JavaScript;
    }
    if (ObjectUtils.cast(this.impl_, FlexLayoutImpl.class) != null) {
      return LayoutImplementation.Flex;
    }
    return this.preferredImplementation_;
  }

  protected LayoutImplementation getPreferredImplementation() {
    return this.preferredImplementation_;
  }

  protected void updateImplementation() {}

  private WLayout parentLayout_;
  private WWidget parentWidget_;
  private int[] margins_ = new int[4];
  private WLayoutImpl impl_;
  private LayoutImplementation preferredImplementation_;
  private static LayoutImplementation defaultImplementation_ = LayoutImplementation.Flex;

  public void setParentLayout(WLayout layout) {
    this.parentLayout_ = layout;
  }
}
