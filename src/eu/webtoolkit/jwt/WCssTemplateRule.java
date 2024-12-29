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
 * A CSS rule based on a template widget.
 *
 * <p>This is a CSS rule whose CSS style properties are defined based on properties of a template
 * widget. When modifying the template widget, these changes are reflected on the CSS rule and thus
 * all widgets that have this CSS rule.
 *
 * <p>
 *
 * @see WCssStyleSheet
 */
public class WCssTemplateRule extends WCssRule {
  private static Logger logger = LoggerFactory.getLogger(WCssTemplateRule.class);

  /**
   * Creates a CSS rule with a given selector.
   *
   * <p>The selector should be a valid CSS selector.
   *
   * <p>
   *
   * <p><i><b>Note: </b>If you want to update the rule, then the selector should be unique and not
   * contain commas, since this is not supported by Microsoft Internet Explorer. </i>
   */
  public WCssTemplateRule(final String selector) {
    super(selector);
    this.widget_ = null;
    this.widget_ = new WCssTemplateWidget(this, (WContainerWidget) null);
  }
  /**
   * Returns the widget that is used as a template.
   *
   * <p>Various properties of the widget are reflected in the CSS style:
   *
   * <ul>
   *   <li>size and dimensions: {@link WWidget#resize(WLength width, WLength height)
   *       WWidget#resize()}, {@link WWidget#setMinimumSize(WLength width, WLength height)
   *       WWidget#setMinimumSize()}, and {@link WWidget#setMaximumSize(WLength width, WLength
   *       height) WWidget#setMaximumSize()}
   *   <li>its position: {@link WWidget#setPositionScheme(PositionScheme scheme)
   *       WWidget#setPositionScheme()}, {@link WWidget#setOffsets(WLength offset, EnumSet sides)
   *       WWidget#setOffsets()}, {@link WWidget#setFloatSide(Side s) WWidget#setFloatSide()},
   *       {@link WWidget#setClearSides(EnumSet sides) WWidget#setClearSides()}
   *   <li>visibility: {@link WWidget#hide()}, {@link WWidget#show()} and {@link
   *       WWidget#setHidden(boolean hidden, WAnimation animation) WWidget#setHidden()}
   *   <li>margins: {@link WWidget#setMargin(WLength margin, EnumSet sides) WWidget#setMargin()}
   *   <li>line height: {@link WWidget#setLineHeight(WLength height) WWidget#setLineHeight()}
   *   <li>all decoration style properties: {@link WWidget#getDecorationStyle()}
   * </ul>
   *
   * <p>When modifying one of these properties of the returned widget, the rule will be updated
   * accordingly.
   */
  public WWidget getTemplateWidget() {
    return this.widget_;
  }

  public String getDeclarations() {
    DomElement e = new DomElement(DomElement.Mode.Update, this.widget_.getDomElementType());
    this.updateDomElement(e, true);
    return e.getCssStyle();
  }

  boolean updateDomElement(final DomElement element, boolean all) {
    this.widget_.updateDom(element, all);
    return true;
  }

  private WCssTemplateWidget widget_;
}
