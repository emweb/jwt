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
 * An abstract class for implementing layout managers.
 *
 * <p>
 *
 * @see WLayoutItem
 */
public abstract class StdLayoutItemImpl extends WObject implements WLayoutItemImpl {
  private static Logger logger = LoggerFactory.getLogger(StdLayoutItemImpl.class);

  /** Constructor. */
  public StdLayoutItemImpl() {
    super();
  }
  /** Returns the container of the of the parent layout. */
  public WContainerWidget getContainer() {
    return ObjectUtils.cast(this.getLayoutItem().getParentWidget(), WContainerWidget.class);
  }
  /** Returns the actual {@link WLayoutItem}. */
  public abstract WLayoutItem getLayoutItem();
  /** Returns the minimum width of the item. */
  public abstract int getMinimumWidth();
  /** Returns the minimum height of the item. */
  public abstract int getMinimumHeight();
  /** Returns the maximum width of the item. */
  public abstract int getMaximumWidth();
  /** Returns the maximum height of the item. */
  public abstract int getMaximumHeight();
  /**
   * Returns the parent layout of the item.
   *
   * <p>Returns the parent layout of the item as a {@link StdLayoutImpl} if the layout is a subclass
   * of {@link StdLayoutImpl}. Otherwise returns nullptr;
   */
  public StdLayoutImpl getParentLayoutImpl() {
    WLayoutItem i = this.getLayoutItem();
    if (i.getParentLayout() != null) {
      return ObjectUtils.cast(i.getParentLayout().getImpl(), StdLayoutImpl.class);
    } else {
      return null;
    }
  }

  public abstract DomElement createDomElement(
      DomElement parent, boolean fitWidth, boolean fitHeight, WApplication app);
}
