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
 * A default loading indicator.
 *
 * <p>The default loading indicator displays the text message Loading... in the right top corner of
 * the window.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>This widget does not provide styling, and can be styled using inline or external CSS as
 * appropriate.
 *
 * <p>
 *
 * <h3>i18n</h3>
 *
 * <p>The strings used in this class can be translated by overriding the default values for the
 * following localization keys:
 *
 * <ul>
 *   <li>Wt.WDefaultLoadingIndicator.Loading: Loading...
 * </ul>
 *
 * <p>
 *
 * @see WApplication#setLoadingIndicator(WLoadingIndicator indicator)
 */
public class WDefaultLoadingIndicator extends WLoadingIndicator {
  private static Logger logger = LoggerFactory.getLogger(WDefaultLoadingIndicator.class);

  /** Constructor. */
  public WDefaultLoadingIndicator(WContainerWidget parentContainer) {
    super();
    this.setImplementation(
        new WText(tr("Wt.WDefaultLoadingIndicator.Loading"), (WContainerWidget) null));
    this.setInline(false);
    this.setStyleClass("Wt-loading");
    WApplication app = WApplication.getInstance();
    app.getStyleSheet()
        .addRule(
            "div.Wt-loading",
            "background-color: red; color: white;font-family: Arial,Helvetica,sans-serif;font-size: small;position: absolute; right: 0px; top: 0px;");
    app.getStyleSheet().addRule("body div > div.Wt-loading", "position: fixed;");
    if (app.getEnvironment().getUserAgent().indexOf("MSIE 5.5") != -1
        || app.getEnvironment().getUserAgent().indexOf("MSIE 6") != -1) {
      app.getStyleSheet()
          .addRule(
              "div.Wt-loading",
              "right: expression(((ignoreMe2 = document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft )) + 'px' );top: expression(((ignoreMe = document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop)) + 'px' );");
    }
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #WDefaultLoadingIndicator(WContainerWidget parentContainer)
   * this((WContainerWidget)null)}
   */
  public WDefaultLoadingIndicator() {
    this((WContainerWidget) null);
  }

  public WWidget getWidget() {
    return this;
  }

  public void setMessage(final CharSequence text) {
    (ObjectUtils.cast(this.getImplementation(), WText.class)).setText(text);
  }
}
