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
 * A layout manager which arranges widgets horizontally.
 *
 * <p>This convenience class creates a horizontal box layout, laying contained widgets out from left
 * to right.
 *
 * <p>See the {@link WBoxLayout} documentation for available member methods and more information.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * WContainerWidget w = new WContainerWidget(this);
 *
 * WHBoxLayout layout = new WHBoxLayout();
 * layout.addWidget(new WText("One"));
 * layout.addWidget(new WText("Two"));
 * layout.addWidget(new WText("Three"));
 * layout.addWidget(new WText("Four"));
 *
 * w.setLayout(layout);
 *
 * }</pre>
 *
 * <p>
 *
 * <p><i><b>Note: </b>First consider if you can achieve your layout using CSS ! </i>
 *
 * @see WVBoxLayout
 */
public class WHBoxLayout extends WBoxLayout {
  private static Logger logger = LoggerFactory.getLogger(WHBoxLayout.class);

  /** Creates a new horizontal box layout. */
  public WHBoxLayout() {
    super(LayoutDirection.LeftToRight);
  }
}
