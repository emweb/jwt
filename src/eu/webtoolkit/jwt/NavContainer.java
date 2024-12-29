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

final class NavContainer extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(NavContainer.class);

  public NavContainer(WContainerWidget parentContainer) {
    super();
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public NavContainer() {
    this((WContainerWidget) null);
  }

  public boolean isBootstrap2Responsive() {
    return this.getStyleClass().indexOf("nav-collapse") != -1;
  }

  public void setHidden(boolean hidden, final WAnimation animation) {
    if (this.isBootstrap2Responsive()) {
      if (animation.isEmpty()) {
        if (hidden) {
          this.setHeight(new WLength(0));
        } else {
          this.setHeight(WLength.Auto);
        }
      }
    }
    super.setHidden(hidden, animation);
  }
}
