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

class MapWidget extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(MapWidget.class);

  public MapWidget() {
    super();
  }

  protected void render(EnumSet<RenderFlag> flags) {
    super.render(flags);
    WImage parent_img = ((this.getParent()) instanceof WImage ? (WImage) (this.getParent()) : null);
    if (parent_img.targetJS_.length() != 0) {
      parent_img.doJavaScript(parent_img.getSetAreaCoordsJS());
    }
  }

  void updateDom(final DomElement element, boolean all) {
    if (all) {
      element.setAttribute("name", this.getId());
    }
    super.updateDom(element, all);
  }

  DomElementType getDomElementType() {
    return DomElementType.DomElement_MAP;
  }
}
