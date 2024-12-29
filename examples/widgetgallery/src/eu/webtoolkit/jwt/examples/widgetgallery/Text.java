/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
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

class Text extends WText {
  private static Logger logger = LoggerFactory.getLogger(Text.class);

  public Text(WContainerWidget parentContainer) {
    super();
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public Text() {
    this((WContainerWidget) null);
  }

  public WString getCalculateToolTip() {
    return new WString("Deferred tooltip");
  }

  public WString getToolTip() {
    return this.getCalculateToolTip();
  }
}
