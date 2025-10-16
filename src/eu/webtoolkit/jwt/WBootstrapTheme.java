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
 * Theme based on the Twitter Bootstrap CSS framework.
 *
 * <p>
 *
 * <p>This theme implements support for building a JWt web application that uses Twitter Bootstrap
 * as a theme for its (layout and) styling. The theme comes with CSS from Bootstrap version 2.2.2 or
 * version 3.1. Only the CSS components of twitter bootstrap are used, but not the JavaScript (i.e.
 * the functional parts), since the functionality is already built-in to the widgets from the
 * library.
 *
 * <p>Using this theme, various widgets provided by the library are rendered using markup that is
 * compatible with Twitter Bootstrap. The bootstrap theme is also extended with a proper
 * (compatible) styling of widgets for which bootstrap does not provide styling (table views, tree
 * views, sliders, etc...).
 *
 * <p>By default, the theme will use CSS resources that are shipped together with the JWt
 * distribution, but since the Twitter Bootstrap CSS API is a popular API for custom themes, you can
 * easily replace the CSS with custom-built CSS (by reimplementing {@link
 * WBootstrapTheme#getStyleSheets() getStyleSheets()}).
 *
 * <p>Although this theme facilitates the use of Twitter Bootstrap with JWt, it is still important
 * to understand how Bootstrap expects markup to be, especially related to layout using its grid
 * system, for which we refer to the official bootstrap documentation, see <a
 * href="http://getbootstrap.com">http://getbootstrap.com</a>
 *
 * <p>
 *
 * @see WApplication#setTheme(WTheme theme)
 * @deprecated {@link WBootstrapTheme} is deprecated. Use one of the concrete versioned Bootstrap
 *     theme classes instead, like {@link WBootstrap2Theme} and {@link WBootstrap3Theme}.
 */
public class WBootstrapTheme extends WTheme {
  private static Logger logger = LoggerFactory.getLogger(WBootstrapTheme.class);

  /** Constructor. */
  public WBootstrapTheme() {
    super();
    this.impl_ = new WBootstrap2Theme();
    this.version_ = BootstrapVersion.v2;
    this.formControlStyle_ = true;
  }
  /**
   * Enables responsive features.
   *
   * <p>Responsive features can be enabled only at application startup. For bootstrap 3, you need to
   * use the progressive bootstrap feature of JWt as it requires setting HTML meta flags.
   *
   * <p>Responsive features are disabled by default.
   */
  public void setResponsive(boolean enabled) {
    if (this.getVersion() == BootstrapVersion.v2) {
      WBootstrap2Theme bootstrap2 = ObjectUtils.cast(this.impl_, WBootstrap2Theme.class);
      assert bootstrap2 != null;
      bootstrap2.setResponsive(enabled);
    } else {
      WBootstrap3Theme bootstrap3 = ObjectUtils.cast(this.impl_, WBootstrap3Theme.class);
      assert bootstrap3 != null;
      bootstrap3.setResponsive(enabled);
    }
  }
  /**
   * Returns whether responsive features are enabled.
   *
   * <p>
   *
   * @see WBootstrapTheme#setResponsive(boolean enabled)
   */
  public boolean isResponsive() {
    if (this.getVersion() == BootstrapVersion.v2) {
      WBootstrap2Theme bootstrap2 = ObjectUtils.cast(this.impl_, WBootstrap2Theme.class);
      assert bootstrap2 != null;
      return bootstrap2.isResponsive();
    } else {
      WBootstrap3Theme bootstrap3 = ObjectUtils.cast(this.impl_, WBootstrap3Theme.class);
      assert bootstrap3 != null;
      return bootstrap3.isResponsive();
    }
  }
  /**
   * Sets the bootstrap version.
   *
   * <p>The default bootstrap version is 2 (but this may change in the future and thus we recommend
   * setting the version).
   *
   * <p>Since Twitter Bootstrap breaks its API with a major version change, the version has a big
   * impact on how how the markup is done for various widgets.
   *
   * <p>Note that the two Bootstrap versions have a different license: Apache 2.0 for Bootstrap
   * version 2.2.2, and MIT for version 3.1. See these licenses for details.
   */
  public void setVersion(BootstrapVersion version) {
    if (this.version_ == version) {
      return;
    }
    if (version == BootstrapVersion.v2) {
      WBootstrap2Theme bootstrap2 = new WBootstrap2Theme();
      bootstrap2.setResponsive(this.isResponsive());
      this.impl_ = bootstrap2;
    } else {
      WBootstrap3Theme bootstrap3 = new WBootstrap3Theme();
      bootstrap3.setResponsive(this.isResponsive());
      bootstrap3.setFormControlStyleEnabled(this.formControlStyle_);
      this.impl_ = bootstrap3;
    }
    this.version_ = version;
  }
  /**
   * Returns the bootstrap version.
   *
   * <p>
   *
   * @see WBootstrapTheme#setVersion(BootstrapVersion version)
   */
  public BootstrapVersion getVersion() {
    return this.version_;
  }
  /**
   * Enables form-control on all applicable form widgets.
   *
   * <p>This is relevant only for bootstrap 3.
   *
   * <p>By applying &quot;form-control&quot; on form widgets, they will become block level elements
   * that take the size of the parent (which is in bootstrap&apos;s philosphy a grid layout).
   *
   * <p>The default value is <code>true</code>.
   */
  public void setFormControlStyleEnabled(boolean enabled) {
    this.formControlStyle_ = enabled;
    if (this.getVersion() == BootstrapVersion.v3) {
      WBootstrap3Theme bootstrap3 = ObjectUtils.cast(this.impl_, WBootstrap3Theme.class);
      assert bootstrap3 != null;
      bootstrap3.setFormControlStyleEnabled(enabled);
    }
  }

  public String getName() {
    return this.impl_.getName();
  }

  public String getResourcesUrl() {
    return this.impl_.getResourcesUrl();
  }

  public List<WLinkedCssStyleSheet> getStyleSheets() {
    return this.impl_.getStyleSheets();
  }

  public void init(WApplication app) {
    this.impl_.init(app);
  }

  public void apply(WWidget widget, WWidget child, int widgetRole) {
    this.impl_.apply(widget, child, widgetRole);
  }

  public void apply(WWidget widget, final DomElement element, int elementRole) {
    this.impl_.apply(widget, element, elementRole);
  }

  public String getDisabledClass() {
    return this.impl_.getDisabledClass();
  }

  public String getActiveClass() {
    return this.impl_.getActiveClass();
  }

  public String utilityCssClass(int utilityCssClassRole) {
    return this.impl_.utilityCssClass(utilityCssClassRole);
  }

  public boolean isCanStyleAnchorAsButton() {
    return this.impl_.isCanStyleAnchorAsButton();
  }

  public void loadValidationStyling(WApplication app) {
    this.impl_.loadValidationStyling(app);
  }

  public void applyValidationStyle(
      WWidget widget, final WValidator.Result validation, EnumSet<ValidationStyleFlag> styles) {
    this.impl_.applyValidationStyle(widget, validation, styles);
  }

  public boolean canBorderBoxElement(final DomElement element) {
    return this.impl_.canBorderBoxElement(element);
  }

  protected void applyFunctionalStyling(WWidget widget, WWidget child, int widgetRole) {
    this.impl_.applyFunctionalStyling(widget, child, widgetRole);
  }

  protected void applyOptionalStyling(WWidget widget, WWidget child, int widgetRole) {
    this.impl_.applyOptionalStyling(widget, child, widgetRole);
  }

  private WTheme impl_;
  private BootstrapVersion version_;
  private boolean formControlStyle_;
}
