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

/**
 * An abstract base class for items that can participate in a layout.
 *
 * <p>
 *
 * @see WLayout
 */
public interface WLayoutItem {
  /**
   * Finds the widget item corresponding to the given <i>widget</i>
   *
   * <p>The widget is searched for recursively inside nested layouts.
   */
  public WWidgetItem findWidgetItem(WWidget widget);
  /**
   * Returns the layout that implements this {@link WLayoutItem}.
   *
   * <p>This implements a type-safe upcasting mechanism to a {@link WLayout}.
   */
  public WLayout getLayout();
  /**
   * Returns the widget that is held by this {@link WLayoutItem}.
   *
   * <p>This implements a type-safe upcasting mechanism to a {@link WWidgetItem}.
   */
  public WWidget getWidget();
  /** Returns the layout in which this item is contained. */
  public WLayout getParentLayout();

  public WWidget getParentWidget();

  public WLayoutItemImpl getImpl();

  public void iterateWidgets(final HandleWidgetMethod method);
  /** Internal method. */
  public void setParentWidget(WWidget parent);
  /** Internal method. */
  public void setParentLayout(WLayout parentLayout);
}
