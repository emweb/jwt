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
 * An external CSS style sheet.
 *
 * <p>
 *
 * @see WApplication#useStyleSheet(WLink link, String media)
 */
public class WLinkedCssStyleSheet {
  private static Logger logger = LoggerFactory.getLogger(WLinkedCssStyleSheet.class);

  /** Creates a new (external) style sheet reference. */
  public WLinkedCssStyleSheet(final WLink link, final String media) {
    this.link_ = link;
    this.media_ = media;
  }
  /**
   * Creates a new (external) style sheet reference.
   *
   * <p>Calls {@link #WLinkedCssStyleSheet(WLink link, String media) this(link, "all")}
   */
  public WLinkedCssStyleSheet(final WLink link) {
    this(link, "all");
  }

  public WLink getLink() {
    return this.link_;
  }

  public String getMedia() {
    return this.media_;
  }

  public void cssText(final StringBuilder out) {
    WApplication app = WApplication.getInstance();
    out.append("@import url(\"").append(this.link_.resolveUrl(app)).append("\")");
    if (this.media_.length() != 0 && !this.media_.equals("all")) {
      out.append(" ").append(this.media_);
    }
    out.append(";\n");
  }

  private WLink link_;
  private String media_;
}
