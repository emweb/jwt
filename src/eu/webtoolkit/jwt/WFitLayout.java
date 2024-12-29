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
 * A layout manager which spans a single widget to all available space.
 *
 * <p>This layout manager may manage only a single child widget, and sizes that widget so that it
 * uses all space available in the parent.
 */
public class WFitLayout extends WLayout {
  private static Logger logger = LoggerFactory.getLogger(WFitLayout.class);

  /** Creates a new fit layout. */
  public WFitLayout() {
    super();
    this.grid_ = new Grid();
    this.grid_.columns_.add(new Grid.Section(0));
    this.grid_.rows_.add(new Grid.Section(0));
    List<Grid.Item> items = new ArrayList<Grid.Item>();
    items.add(new Grid.Item());
    this.grid_.items_.add(items);
  }

  public static void fitWidget(WContainerWidget container, WWidget widget) {
    WFitLayout l = new WFitLayout();
    l.addWidget(widget);
    container.setLayout(l);
  }

  public void addItem(WLayoutItem item) {
    if (this.grid_.items_.get(0).get(0).item_ != null) {
      logger.error(new StringWriter().append("addItem(): already have a widget").toString());
      return;
    }
    WLayoutItem it = item;
    this.grid_.items_.get(0).get(0).item_ = item;
    this.itemAdded(it);
  }

  public WLayoutItem removeItem(WLayoutItem item) {
    if (item == this.grid_.items_.get(0).get(0).item_) {
      final WLayoutItem result = this.grid_.items_.get(0).get(0).item_;
      this.itemRemoved(item);
      return result;
    } else {
      return null;
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

  public void iterateWidgets(final HandleWidgetMethod method) {
    if (this.grid_.items_.get(0).get(0).item_ != null) {
      this.grid_.items_.get(0).get(0).item_.iterateWidgets(method);
    }
  }

  public boolean isImplementationIsFlexLayout() {
    final WEnvironment env = WApplication.getInstance().getEnvironment();
    return this.getPreferredImplementation() == LayoutImplementation.Flex && !env.agentIsIElt(10);
  }

  protected void updateImplementation() {
    if (!(this.getParentWidget() != null)) {
      return;
    }
    boolean isFlexLayout = this.isImplementationIsFlexLayout();
    if (isFlexLayout) {
      this.setImpl(new FlexLayoutImpl(this, this.grid_));
    } else {
      this.setImpl(new StdGridLayoutImpl2(this, this.grid_));
    }
  }

  private Grid grid_;

  public void setParentWidget(WWidget parent) {
    super.setParentWidget(parent);
    if (parent != null) {
      this.updateImplementation();
    }
  }
}
