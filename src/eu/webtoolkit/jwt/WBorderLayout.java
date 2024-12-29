/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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
 * A layout manager which divides the container region in five regions.
 *
 * <p>The five regions are composed of:
 *
 * <pre>
 * ------------------------------------
 * |              North               |
 * ------------------------------------
 * |      |                    |      |
 * | West |       Center       | East |
 * |      |                    |      |
 * ------------------------------------
 * |              South               |
 * ------------------------------------
 * </pre>
 *
 * <p>Each region may hold no more than one widget, and for all but the Center region, the widget is
 * optional.
 *
 * <p>The North, West, East, and South widgets will take their preferred sizes, while the Center
 * widget takes all available remaining space.
 */
public class WBorderLayout extends WLayout {
  private static Logger logger = LoggerFactory.getLogger(WBorderLayout.class);

  /** Creates a new border layout. */
  public WBorderLayout() {
    super();
    this.grid_ = new Grid();
    {
      int insertPos = 0;
      for (int ii = 0; ii < (3); ++ii) this.grid_.columns_.add(insertPos + ii, new Grid.Section(0));
    }
    ;
    this.grid_.columns_.get(1).stretch_ = 1;
    {
      int insertPos = 0;
      for (int ii = 0; ii < (3); ++ii) this.grid_.rows_.add(insertPos + ii, new Grid.Section(0));
    }
    ;
    this.grid_.rows_.get(1).stretch_ = 1;
    for (int j = 0; j < 3; ++j) {
      this.grid_.items_.add(new ArrayList<Grid.Item>());
      for (int i = 0; i < 3; ++i) {
        final List<Grid.Item> items = this.grid_.items_.get(this.grid_.items_.size() - 1);
        items.add(new Grid.Item());
      }
    }
    this.grid_.items_.get(0).get(0).colSpan_ = 3;
    this.grid_.items_.get(2).get(0).colSpan_ = 3;
  }
  /**
   * Sets spacing between each item.
   *
   * <p>The default spacing is 6 pixels.
   */
  public void setSpacing(int size) {
    this.grid_.horizontalSpacing_ = size;
    this.grid_.verticalSpacing_ = size;
  }
  /**
   * Returns the spacing between each item.
   *
   * <p>
   *
   * @see WBorderLayout#setSpacing(int size)
   */
  public int getSpacing() {
    return this.grid_.horizontalSpacing_;
  }

  public void addItem(WLayoutItem item) {
    this.add(item, LayoutPosition.Center);
  }

  public WLayoutItem removeItem(WLayoutItem item) {
    WLayoutItem result = null;
    for (int i = 0; i < 5; ++i) {
      final Grid.Item gridItem = this.itemAtPosition(LayoutPosition.values()[i]);
      if (gridItem.item_ == item) {
        result = gridItem.item_;
        this.itemRemoved(item);
        break;
      }
    }
    return result;
  }

  public WLayoutItem getItemAt(int index) {
    int j = 0;
    for (int i = 0; i < 5; ++i) {
      WLayoutItem it = this.itemAtPosition(LayoutPosition.values()[i]).item_;
      if (it != null) {
        if (j == index) {
          return it;
        } else {
          ++j;
        }
      }
    }
    return null;
  }

  public int getCount() {
    int j = 0;
    for (int i = 0; i < 5; ++i) {
      if (this.itemAtPosition(LayoutPosition.values()[i]).item_ != null) {
        ++j;
      }
    }
    return j;
  }
  /**
   * Adds a widget to the given position.
   *
   * <p>Only one widget per position is supported.
   *
   * <p>
   */
  public void addWidget(WWidget w, LayoutPosition position) {
    this.add(new WWidgetItem(w), position);
  }
  // public Widget  addWidget(<Woow... some pseudoinstantiation type!> widget, LayoutPosition
  // position) ;
  /**
   * Adds a layout item to the given position.
   *
   * <p>Only one widget per position is supported.
   */
  public void add(WLayoutItem item, LayoutPosition position) {
    final Grid.Item it = this.itemAtPosition(position);
    if (it.item_ != null) {
      logger.error(new StringWriter().append("supports only one widget per position").toString());
      return;
    }
    it.item_ = item;
    this.itemAdded(it.item_);
  }
  /**
   * Returns the widget at a position.
   *
   * <p>Returns <code>null</code> if no widget was set for that position.
   */
  public WWidget widgetAt(LayoutPosition position) {
    WWidgetItem item = ObjectUtils.cast(this.getItemAt(position), WWidgetItem.class);
    if (item != null) {
      return item.getWidget();
    } else {
      return null;
    }
  }
  /**
   * Returns the item at a position.
   *
   * <p>Returns <code>null</code> if no item was set for that position.
   */
  public WLayoutItem getItemAt(LayoutPosition position) {
    final Grid.Item gridItem = this.itemAtPosition(position);
    return gridItem.item_;
  }
  /** Returns the position at which the given layout item is set. */
  public LayoutPosition getPosition(WLayoutItem item) {
    for (int i = 0; i < 5; ++i) {
      if (this.itemAtPosition(LayoutPosition.values()[i]).item_ == item) {
        return LayoutPosition.values()[i];
      }
    }
    logger.error(new StringWriter().append("position(): item not found").toString());
    return LayoutPosition.Center;
  }

  public void iterateWidgets(final HandleWidgetMethod method) {
    for (int r = 0; r < this.grid_.rows_.size(); ++r) {
      for (int c = 0; c < this.grid_.columns_.size(); ++c) {
        WLayoutItem item = this.grid_.items_.get(r).get(c).item_;
        if (item != null) {
          item.iterateWidgets(method);
        }
      }
    }
  }

  private Grid grid_;

  private Grid.Item itemAtPosition(LayoutPosition position) {
    switch (position) {
      case North:
        return this.grid_.items_.get(0).get(0);
      case East:
        return this.grid_.items_.get(1).get(2);
      case South:
        return this.grid_.items_.get(2).get(0);
      case West:
        return this.grid_.items_.get(1).get(0);
      case Center:
        return this.grid_.items_.get(1).get(1);
      default:
        logger.error(
            new StringWriter()
                .append("itemAtPosition(): invalid position:")
                .append(String.valueOf((int) position.getValue()))
                .toString());
        return this.grid_.items_.get(1).get(1);
    }
  }

  public void setParentWidget(WWidget parent) {
    super.setParentWidget(parent);
    if (parent != null) {
      this.setImpl(new StdGridLayoutImpl2(this, this.grid_));
    }
  }
}
