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

class MapWidget extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(MapWidget.class);

  public MapWidget() {
    super();
    this.areas_ = new ArrayList<WAbstractArea>();
  }

  public void insertArea(int index, WAbstractArea area) {
    this.insertWidget(index, area.takeWidget());
    this.areas_.add(0, area);
  }

  public WAbstractArea removeArea(WAbstractArea area) {
    int index = this.getIndexOf(area.getWidget());
    if (index != -1) {
      area.returnWidget(WidgetUtils.remove(this, area.getWidget()));
      return CollectionUtils.take(this.areas_, area);
    } else {
      return null;
    }
  }

  public WAbstractArea area(int index) {
    return this.areas_.get(index);
  }

  protected void render(EnumSet<RenderFlag> flags) {
    super.render(flags);
    WImage parent_img = ObjectUtils.cast(this.getParent(), WImage.class);
    if (parent_img != null) {
      if (parent_img.targetJS_.length() != 0) {
        parent_img.doJavaScript(parent_img.getSetAreaCoordsJS());
      }
    }
  }

  void updateDom(final DomElement element, boolean all) {
    if (all) {
      element.setAttribute("name", this.getId());
    }
    super.updateDom(element, all);
  }

  DomElementType getDomElementType() {
    return DomElementType.MAP;
  }

  private List<WAbstractArea> areas_;
}
