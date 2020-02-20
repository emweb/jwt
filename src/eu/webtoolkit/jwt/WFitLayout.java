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
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A layout manager which spans a single widget to all available space.
 *
 * <p>This layout manager may manage only a single child widget, and sizes that widget so that it
 * uses all space available in the parent.
 */
public class WFitLayout extends WLayout {
  private static Logger logger = LoggerFactory.getLogger(WFitLayout.class);

  /** Creates a new fit layout. */
  public WFitLayout(WWidget parent) {
    super();
    this.grid_ = new Grid();
    this.grid_.columns_.add(new Grid.Section(0));
    this.grid_.rows_.add(new Grid.Section(0));
    List<Grid.Item> items = new ArrayList<Grid.Item>();
    items.add(new Grid.Item());
    this.grid_.items_.add(items);
    if (parent != null) {
      this.setLayoutInParent(parent);
    }
  }
  /**
   * Creates a new fit layout.
   *
   * <p>Calls {@link #WFitLayout(WWidget parent) this((WWidget)null)}
   */
  public WFitLayout() {
    this((WWidget) null);
  }

  public static void fitWidget(WContainerWidget container, WWidget widget) {
    WFitLayout l = new WFitLayout();
    container.setLayout(l);
    l.addWidget(widget);
  }

  public void addItem(WLayoutItem item) {
    if (this.grid_.items_.get(0).get(0).item_ != null) {
      logger.error(new StringWriter().append("addItem(): already have a widget").toString());
      return;
    }
    this.grid_.items_.get(0).get(0).item_ = item;
    this.updateAddItem(item);
  }

  public void removeItem(WLayoutItem item) {
    if (item == this.grid_.items_.get(0).get(0).item_) {
      this.grid_.items_.get(0).get(0).item_ = null;
      this.updateRemoveItem(item);
    }
  }

  public WLayoutItem getItemAt(int index) {
    return this.grid_.items_.get(0).get(0).item_;
  }

  public int indexOf(WLayoutItem item) {
    if (this.grid_.items_.get(0).get(0).item_ == item) {
      return 0;
    } else {
      return -1;
    }
  }

  public int getCount() {
    return this.grid_.items_.get(0).get(0).item_ != null ? 1 : 0;
  }

  public void clear() {
    this.clearLayoutItem(this.grid_.items_.get(0).get(0).item_);
    this.grid_.items_.get(0).get(0).item_ = null;
  }

  public Grid getGrid() {
    return this.grid_;
  }

  private Grid grid_;
}
