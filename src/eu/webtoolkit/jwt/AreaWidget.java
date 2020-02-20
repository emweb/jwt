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

class AreaWidget extends WInteractWidget {
  private static Logger logger = LoggerFactory.getLogger(AreaWidget.class);

  public AreaWidget(WAbstractArea facade) {
    super();
    this.facade_ = facade;
  }

  public void remove() {
    if (this.facade_ != null) {
      this.facade_.impl_ = null;
      if (this.facade_ != null) this.facade_.remove();
    }
    super.remove();
  }

  public WAbstractArea getFacade() {
    return this.facade_;
  }

  WAbstractArea facade_;

  void updateDom(final DomElement element, boolean all) {
    boolean needsUrlResolution = this.facade_.updateDom(element, all);
    super.updateDom(element, all);
    if (element.getProperty(Property.PropertyStyleCursor).length() != 0
        && !WApplication.getInstance().getEnvironment().agentIsGecko()
        && element.getAttribute("href").length() == 0) {
      element.setAttribute("href", "javascript:void(0);");
    }
    if (needsUrlResolution) {
      WAnchor.renderUrlResolution(this, element, all);
    }
  }

  DomElementType getDomElementType() {
    return DomElementType.DomElement_AREA;
  }
}
