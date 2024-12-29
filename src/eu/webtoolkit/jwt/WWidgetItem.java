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

/** A layout item that holds a single widget. */
public class WWidgetItem implements WLayoutItem {
  private static Logger logger = LoggerFactory.getLogger(WWidgetItem.class);

  /** Creates a new item for the given <i>widget</i>. */
  public WWidgetItem(WWidget widget) {
    super();
    this.widget_ = widget;
    this.parentLayout_ = null;
    this.impl_ = null;
  }

  public WWidget getWidget() {
    return this.widget_;
  }

  public WLayout getLayout() {
    return null;
  }

  public WLayout getParentLayout() {
    return this.parentLayout_;
  }

  public WWidget getParentWidget() {
    if (this.parentLayout_ != null) {
      return this.parentLayout_.getParentWidget();
    } else {
      return null;
    }
  }

  public WWidgetItem findWidgetItem(WWidget widget) {
    if (this.widget_ == widget) {
      return this;
    } else {
      return null;
    }
  }

  public WWidgetItemImpl getImpl() {
    return this.impl_;
  }

  public WWidget getTakeWidget() {
    WWidget result = this.widget_;
    this.impl_ = null;
    return result;
  }

  public void iterateWidgets(final HandleWidgetMethod method) {
    if (this.widget_ != null) {
      method.handle(this.widget_);
    }
  }

  private WWidget widget_;
  private WLayout parentLayout_;
  private WWidgetItemImpl impl_;

  public void setParentWidget(WWidget parent) {
    if (!(this.widget_ != null)) {
      return;
    }
    if (parent != null) {
      WContainerWidget pc = ObjectUtils.cast(parent, WContainerWidget.class);
      if (this.widget_.getParent() != null) {
        if (this.widget_.getParent() != pc) {
          throw new WException("Cannot move a WWidgetItem to another container");
        }
      } else {
        pc.widgetAdded(this.widget_);
      }
      boolean flexLayout = this.parentLayout_.isImplementationIsFlexLayout();
      if (flexLayout) {
        this.impl_ = new FlexItemImpl(this);
      } else {
        this.impl_ = new StdWidgetItemImpl(this);
      }
    } else {
      WContainerWidget pc = ObjectUtils.cast(this.widget_.getParent(), WContainerWidget.class);
      if (pc != null) {
        assert this.impl_ != null;
        boolean flex = ObjectUtils.cast(this.getImpl(), FlexItemImpl.class) != null;
        pc.widgetRemoved(this.widget_, flex);
      }
      this.impl_ = null;
    }
  }

  public void setParentLayout(WLayout parentLayout) {
    this.parentLayout_ = parentLayout;
  }
}
