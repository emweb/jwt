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
 * A widget that represents a Font-Aweswome icon.
 *
 * <p>By default, JWt will load the default Font-Awesome included with it. This is version 4.3.0.
 * For a list of all icons, visit: <a
 * href="https://fontawesome.com/v4/icons/">https://fontawesome.com/v4/icons/</a>
 *
 * <p>
 *
 * @see WIcon#setName(String name)
 */
public class WIcon extends WInteractWidget {
  private static Logger logger = LoggerFactory.getLogger(WIcon.class);

  /** Creates an empty icon. */
  public WIcon(WContainerWidget parentContainer) {
    super();
    this.name_ = "";
    this.iconChanged_ = false;
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an empty icon.
   *
   * <p>Calls {@link #WIcon(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WIcon() {
    this((WContainerWidget) null);
  }
  /**
   * Creates an icon with the given name.
   *
   * <p>
   *
   * @see WIcon#setName(String name)
   */
  public WIcon(final String name, WContainerWidget parentContainer) {
    super();
    this.name_ = "";
    this.iconChanged_ = false;
    this.setName(name);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an icon with the given name.
   *
   * <p>Calls {@link #WIcon(String name, WContainerWidget parentContainer) this(name,
   * (WContainerWidget)null)}
   */
  public WIcon(final String name) {
    this(name, (WContainerWidget) null);
  }
  /**
   * Set the icon name.
   *
   * <p>This sets the name of the icon. The name should be a the name of a Font-Aweswome icon,
   * without the <code>fa-</code> prefix.
   *
   * <p>Usage example: The &quot;play&quot; icon: <a
   * href="https://fontawesome.com/v4/icon/play">https://fontawesome.com/v4/icon/play</a> can be
   * included with:
   *
   * <pre>{@code
   * WApplication app = WApplication.getInstance();
   * app.getRoot().addWidget(new WIcon("play"));
   *
   * }</pre>
   *
   * <p>
   *
   * <p><i><b>Note: </b>The name can be followed by sizing information separated by a space if the
   * Font Aweswome version used allows it. E.g. <code>&quot;play fa-4&quot;</code> </i>
   */
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
  /**
   * Returns the icon name.
   *
   * <p>
   *
   * @see WIcon#setName(String name)
   */
  public String getName() {
    return this.name_;
  }
  /**
   * Changes the icon&apos;s size.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This is done in CSS, not using the <code>fa-{size}</code> method. </i>
   */
  public void setSize(double factor) {
    this.getDecorationStyle().getFont().setSize(new WLength(factor, LengthUnit.FontEm));
  }
  /**
   * Returns the icon size.
   *
   * <p>
   *
   * @see WIcon#setSize(double factor)
   */
  public double getSize() {
    final WFont f = this.getDecorationStyle().getFont();
    if (f.getSizeLength().getUnit() == LengthUnit.FontEm) {
      return f.getSizeLength().getValue();
    } else {
      return 1;
    }
  }
  /**
   * Loads the Font-Aweswome css style sheet.
   *
   * <p>This is a convenience function that adds Font-Aweswome&apos;s CSS style sheet to the list of
   * used style sheets.
   *
   * <p>By default this will load the stylesheet present at: <code>
   * resources/font-awesome/css/font-awesome.min.css</code> The <code>resources</code> directory can
   * be set with a command-line option, namely <code>&ndash;resources-dir</code>, see <a
   * href="https://www.webtoolkit.eu/wt/doc/reference/html/overview.html#config_wthttpd">Wt&apos;s
   * configuration options</a>.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This is automatically called when needed by {@link WIcon}. </i>
   */
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
