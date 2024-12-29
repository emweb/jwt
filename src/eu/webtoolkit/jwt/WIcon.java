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

class WIcon extends WInteractWidget {
  private static Logger logger = LoggerFactory.getLogger(WIcon.class);

  public WIcon(WContainerWidget parentContainer) {
    super();
    this.name_ = "";
    this.iconChanged_ = false;
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public WIcon() {
    this((WContainerWidget) null);
  }

  public WIcon(final String name, WContainerWidget parentContainer) {
    super();
    this.name_ = "";
    this.iconChanged_ = false;
    this.setName(name);
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public WIcon(final String name) {
    this(name, (WContainerWidget) null);
  }

  public void setName(final String name) {
    if (!this.name_.equals(name)) {
      this.name_ = name;
      this.iconChanged_ = true;
      this.repaint();
      if (this.name_.length() != 0) {
        loadIconFont();
      }
    }
  }

  public String getName() {
    return this.name_;
  }

  public void setSize(double factor) {
    this.getDecorationStyle().getFont().setSize(new WLength(factor, LengthUnit.FontEm));
  }

  public double getSize() {
    final WFont f = this.getDecorationStyle().getFont();
    if (f.getSizeLength().getUnit() == LengthUnit.FontEm) {
      return f.getSizeLength().getValue();
    } else {
      return 1;
    }
  }

  public static void loadIconFont() {
    WApplication app = WApplication.getInstance();
    String fontDir = WApplication.getRelativeResourcesUrl() + "font-awesome/";
    app.useStyleSheet(new WLink(fontDir + "css/font-awesome.min.css"));
  }

  void updateDom(final DomElement element, boolean all) {
    if (this.iconChanged_ || all) {
      String sc = "";
      if (!all) {
        sc = this.getStyleClass();
      }
      if (this.name_.length() != 0) {
        sc = StringUtils.addWord(sc, "fa fa-" + this.name_);
      }
      element.setProperty(Property.Class, sc);
      this.iconChanged_ = false;
    }
    super.updateDom(element, all);
  }

  DomElementType getDomElementType() {
    return DomElementType.I;
  }

  void propagateRenderOk(boolean deep) {
    this.iconChanged_ = false;
    super.propagateRenderOk(deep);
  }

  private String name_;
  private boolean iconChanged_;
}
