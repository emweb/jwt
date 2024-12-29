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

class WCssTemplateWidget extends WWebWidget {
  private static Logger logger = LoggerFactory.getLogger(WCssTemplateWidget.class);

  public WCssTemplateWidget(WCssTemplateRule rule, WContainerWidget parentContainer) {
    super();
    this.rule_ = rule;
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public WCssTemplateWidget(WCssTemplateRule rule) {
    this(rule, (WContainerWidget) null);
  }

  public void setPositionScheme(PositionScheme scheme) {
    super.setPositionScheme(scheme);
    this.rule_.modified();
  }

  public void setOffsets(final WLength offset, EnumSet<Side> sides) {
    super.setOffsets(offset, sides);
    this.rule_.modified();
  }

  public void resize(final WLength width, final WLength height) {
    super.resize(width, height);
    this.rule_.modified();
  }

  public void setMinimumSize(final WLength width, final WLength height) {
    super.setMinimumSize(width, height);
    this.rule_.modified();
  }

  public void setMaximumSize(final WLength width, final WLength height) {
    super.setMaximumSize(width, height);
    this.rule_.modified();
  }

  public void setLineHeight(final WLength height) {
    super.setLineHeight(height);
    this.rule_.modified();
  }

  public void setFloatSide(Side s) {
    super.setFloatSide(s);
    this.rule_.modified();
  }

  public void setClearSides(EnumSet<Side> sides) {
    super.setClearSides(sides);
    this.rule_.modified();
  }

  public void setMargin(final WLength margin, EnumSet<Side> sides) {
    super.setMargin(margin, sides);
    this.rule_.modified();
  }

  public void setHidden(boolean hidden, final WAnimation animation) {
    super.setHidden(hidden, animation);
    this.rule_.modified();
  }

  public void setPopup(boolean popup) {
    super.setPopup(popup);
    this.rule_.modified();
  }

  public void setInline(boolean isinline) {
    super.setInline(isinline);
    this.rule_.modified();
  }

  public WCssDecorationStyle getDecorationStyle() {
    this.rule_.modified();
    return super.getDecorationStyle();
  }

  public void setVerticalAlignment(AlignmentFlag alignment, final WLength length) {
    super.setVerticalAlignment(alignment, length);
    this.rule_.modified();
  }

  DomElementType getDomElementType() {
    return DomElementType.SPAN;
  }

  private WCssTemplateRule rule_;
}
