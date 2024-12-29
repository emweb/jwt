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
 * An abstract base class for implementing layout managers.
 *
 * <p>
 *
 * @see WLayoutItem
 * @see WLayout
 */
public interface WLayoutImpl extends WLayoutItemImpl {
  /**
   * Adds a layout <i>item</i>.
   *
   * <p>The <code>item</code> already has an implementation set.
   */
  public void itemAdded(WLayoutItem item);
  /** Removes a layout <i>item</i>. */
  public void itemRemoved(WLayoutItem item);
  /** Updates the layout. */
  public void update();
}
