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
 * Abstract base class for themes in JWt.
 *
 * <p>
 *
 * @see WApplication#setTheme(WTheme theme)
 */
public abstract class WTheme extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WTheme.class);

  /** Constructor. */
  public WTheme() {
    super();
  }
  /**
   * Returns a theme name.
   *
   * <p>Returns a unique name for the theme. This name is used by the default implementation of
   * {@link WTheme#getResourcesUrl() getResourcesUrl()} to compute a location for the theme&apos;s
   * resources.
   */
  public abstract String getName();
  /**
   * Returns the URL where theme-related resources are stored.
   *
   * <p>The default implementation considers a folder within JWt&apos;s resource directory, based on
   * the theme {@link WTheme#getName() getName()}.
   */
  public String getResourcesUrl() {
    return WApplication.getRelativeResourcesUrl() + "themes/" + this.getName() + "/";
  }
  /**
   * Serves the CSS for the theme.
   *
   * <p>This must serve CSS declarations for the theme.
   *
   * <p>The default implementation serves all the {@link WTheme#getStyleSheets() getStyleSheets()}.
   */
  public void serveCss(final StringBuilder out) {
    List<WLinkedCssStyleSheet> sheets = this.getStyleSheets();
    for (int i = 0; i < sheets.size(); ++i) {
      sheets.get(i).cssText(out);
    }
  }
  /**
   * Returns a vector with stylesheets for the theme.
   *
   * <p>This should return a vector with stylesheets that implement the theme. This list may be
   * tailored to the current user agent, which is read from the application environment.
   */
  public abstract List<WLinkedCssStyleSheet> getStyleSheets();
  /**
   * Called when the theme is assigned to a {@link WApplication}.
   *
   * <p>This allows the theme to do things like load resource bundles when it is added to an
   * application using {@link WApplication#setTheme(WTheme theme) WApplication#setTheme()}.
   *
   * <p>The default implementation does nothing.
   */
  public void init(WApplication app) {}
  /**
   * Applies the theme to a child of a composite widget.
   *
   * <p>The <code>widgetRole</code> indicates the role that <code>child</code> has within the
   * implementation of the <code>widget</code>.
   *
   * <p>
   *
   * @see WWidget#setThemeStyleEnabled(boolean enabled)
   */
  public void apply(WWidget widget, WWidget child, int widgetRole) {
    this.applyFunctionalStyling(widget, child, widgetRole);
    if (widget.isThemeStyleEnabled()) {
      this.applyOptionalStyling(widget, child, widgetRole);
    }
  }
  /**
   * Applies the theme to a DOM element that renders a widget.
   *
   * <p>The <code>element</code> is a rendered representation of the <code>widget</code>, and may be
   * further customized to reflect the theme.
   */
  public abstract void apply(WWidget widget, final DomElement element, int elementRole);
  /** Returns a generic CSS class name for a disabled element. */
  public abstract String getDisabledClass();
  /** Returns a generic CSS class name for an active element. */
  public abstract String getActiveClass();
  /** Returns a generic CSS class name for the chosen role. */
  public abstract String utilityCssClass(int utilityCssClassRole);
  /** Returns whether the theme allows for an anchor to be styled as a button. */
  public abstract boolean isCanStyleAnchorAsButton();
  /**
   * Load the required content for validation.
   *
   * <p>The styling, and scripts used for validation are separated. Loading these is not done on
   * theme initialization.
   */
  public void loadValidationStyling(WApplication app) {
    logger.warn(
        new StringWriter()
            .append(
                "loadValidationStyling(): Using the default (empty) call. Override it if you make use of custom validation (using DOM.validate() or DOM.wtValdiate()).")
            .toString());
  }
  /** Applies a style that indicates the result of validation. */
  public abstract void applyValidationStyle(
      WWidget widget, final WValidator.Result validation, EnumSet<ValidationStyleFlag> flags);
  /**
   * Applies a style that indicates the result of validation.
   *
   * <p>Calls {@link #applyValidationStyle(WWidget widget, WValidator.Result validation, EnumSet
   * flags) applyValidationStyle(widget, validation, EnumSet.of(flag, flags))}
   */
  public final void applyValidationStyle(
      WWidget widget,
      final WValidator.Result validation,
      ValidationStyleFlag flag,
      ValidationStyleFlag... flags) {
    applyValidationStyle(widget, validation, EnumSet.of(flag, flags));
  }

  public abstract boolean canBorderBoxElement(final DomElement element);
  /**
   * Returns which side the {@link WPanel} collapse icon should be added on.
   *
   * <p>Side is assumed to be {@link Side#Left} or {@link Side#Right}, other sides are not
   * supported.
   */
  public Side getPanelCollapseIconSide() {
    return Side.Left;
  }
  /**
   * Applies the functional part of the theme to a widget&apos;s child.
   *
   * <p>Only applies the functional part of the theme. This means that only things that are
   * mandatory for the widget to function properly but depend on the theme are applied.
   *
   * <p>
   *
   * @see WTheme#apply(WWidget widget, WWidget child, int widgetRole)
   * @see WTheme#applyOptionalStyling(WWidget widget, WWidget child, int widgetRole)
   */
  protected abstract void applyFunctionalStyling(WWidget widget, WWidget child, int widgetRole);
  /**
   * Applies the optional part of the theme to a widget&apos;s child.
   *
   * <p>Only applies the optional part of the theme. This means that only things that are purely
   * cosmetic and do not affect the functionality of the widget are applied.
   *
   * <p>This should only be called for widgets that have theme styling enabled.
   *
   * <p>
   *
   * @see WTheme#apply(WWidget widget, WWidget child, int widgetRole)
   * @see WTheme#applyFunctionalStyling(WWidget widget, WWidget child, int widgetRole)
   * @see WWidget#setThemeStyleEnabled(boolean enabled)
   */
  protected abstract void applyOptionalStyling(WWidget widget, WWidget child, int widgetRole);
}
