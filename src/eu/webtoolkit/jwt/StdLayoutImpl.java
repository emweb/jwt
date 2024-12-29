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

abstract class StdLayoutImpl extends StdLayoutItemImpl implements WLayoutImpl {
  private static Logger logger = LoggerFactory.getLogger(StdLayoutImpl.class);

  public StdLayoutImpl(WLayout layout) {
    super();
    this.layout_ = layout;
  }

  public abstract void updateDom(final DomElement parent);

  public abstract boolean itemResized(WLayoutItem item);

  public abstract boolean isParentResized();

  public WLayoutItem getLayoutItem() {
    return this.layout_;
  }

  protected WLayout getLayout() {
    return this.layout_;
  }

  protected static StdLayoutItemImpl getImpl(WLayoutItem item) {
    return ObjectUtils.cast(item.getImpl(), StdLayoutItemImpl.class);
  }

  private WLayout layout_;
}
