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
 * A widget that provides a line break between inline widgets.
 *
 * <p>This is an {@link WWidget#setInline(boolean inlined) inline } widget that provides a line
 * break inbetween its sibling widgets (such as {@link WText}).
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>The widget corresponds to the HTML <code>&lt;br /&gt;</code> tag and does not provide styling.
 * Styling through CSS is not applicable.
 */
public class WBreak extends WWebWidget {
  private static Logger logger = LoggerFactory.getLogger(WBreak.class);

  /** Construct a line break. */
  public WBreak(WContainerWidget parent) {
    super(parent);
    this.setInline(false);
  }
  /**
   * Construct a line break.
   *
   * <p>Calls {@link #WBreak(WContainerWidget parent) this((WContainerWidget)null)}
   */
  public WBreak() {
    this((WContainerWidget) null);
  }

  DomElementType getDomElementType() {
    return DomElementType.DomElement_BR;
  }
}
