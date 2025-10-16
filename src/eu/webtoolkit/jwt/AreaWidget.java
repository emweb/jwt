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

class AreaWidget extends WInteractWidget {
  private static Logger logger = LoggerFactory.getLogger(AreaWidget.class);

  public AreaWidget(WAbstractArea facade) {
    super();
    this.facade_ = facade;
  }

  public void remove() {
    super.remove();
  }

  public WAbstractArea getFacade() {
    return this.facade_;
  }

  WAbstractArea facade_;

  void updateDom(final DomElement element, boolean all) {
    boolean needsUrlResolution = this.facade_.updateDom(element, all);
    super.updateDom(element, all);
    if (element.getAttribute("href").length() == 0) {
      if (!WApplication.getInstance().getEnvironment().agentIsGecko()) {
        element.setAttribute("href", "#");
      }
      element.addPropertyWord(Property.Class, WInteractWidget.noDefault);
      List<String> styleClassesVector = Utils.getWidgetStyleClasses(this);
      for (String className : styleClassesVector) {
        element.addPropertyWord(Property.Class, className);
      }
    }
    if (needsUrlResolution) {
      WAnchor.renderUrlResolution(this, element, all);
    }
  }

  DomElementType getDomElementType() {
    return DomElementType.AREA;
  }
}
