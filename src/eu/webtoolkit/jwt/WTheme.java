/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for themes in JWt.
 * <p>
 * 
 * @see WApplication#setTheme(WTheme theme)
 */
public abstract class WTheme extends WObject {
	private static Logger logger = LoggerFactory.getLogger(WTheme.class);

	/**
	 * Constructor.
	 */
	public WTheme(WObject parent) {
		super(parent);
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WTheme(WObject parent) this((WObject)null)}
	 */
	public WTheme() {
		this((WObject) null);
	}

	/**
	 * Returns a theme name.
	 * <p>
	 * 
	 * Returns a unique name for the theme. This name is used by the default
	 * implementation of {@link WTheme#getResourcesUrl() getResourcesUrl()} to
	 * compute a location for the theme&apos;s resources.
	 */
	public abstract String getName();

	/**
	 * Returns the URL where theme-related resources are stored.
	 * <p>
	 * 
	 * The default implementation considers a folder within JWt&apos;s resource
	 * directory, based on the theme {@link WTheme#getName() getName()}.
	 */
	public String getResourcesUrl() {
		return WApplication.getRelativeResourcesUrl() + "themes/"
				+ this.getName() + "/";
	}

	/**
	 * Serves the CSS for the theme.
	 * <p>
	 * 
	 * This must serve CSS declarations for the theme.
	 * <p>
	 * The default implementation serves all the {@link WTheme#getStyleSheets()
	 * getStyleSheets()}.
	 */
	public void serveCss(final StringBuilder out) {
		List<WCssStyleSheet> sheets = this.getStyleSheets();
		for (int i = 0; i < sheets.size(); ++i) {
			sheets.get(i).cssText(out, true);
		}
	}

	/**
	 * Returns a vector with stylesheets for the theme.
	 * <p>
	 * 
	 * This should return a vector with stylesheets that implement the theme.
	 * This list may be tailored to the current user agent, which is read from
	 * the application environment.
	 */
	public abstract List<WCssStyleSheet> getStyleSheets();

	/**
	 * Applies the theme to a child of a composite widget.
	 * <p>
	 * 
	 * The <code>widgetRole</code> indicates the role that <code>child</code>
	 * has within the implementation of the <code>widget</code>.
	 */
	public abstract void apply(WWidget widget, WWidget child, int widgetRole);

	/**
	 * Applies the theme to a DOM element that renders a widget.
	 * <p>
	 * 
	 * The <code>element</code> is a rendered representation of the
	 * <code>widget</code>, and may be further customized to reflect the theme.
	 */
	public abstract void apply(WWidget widget, final DomElement element,
			int elementRole);

	/**
	 * Returns a generic CSS class name for a disabled element.
	 */
	public abstract String getDisabledClass();

	/**
	 * Returns a generic CSS class name for an active element.
	 */
	public abstract String getActiveClass();

	/**
	 * Returns a generic CSS class name for the chosen role.
	 */
	public abstract String utilityCssClass(int utilityCssClassRole);

	/**
	 * Returns whether the theme allows for an anchor to be styled as a button.
	 */
	public abstract boolean isCanStyleAnchorAsButton();

	/**
	 * Applies a style that indicates the result of validation.
	 */
	public abstract void applyValidationStyle(WWidget widget,
			final WValidator.Result validation,
			EnumSet<ValidationStyleFlag> flags);

	/**
	 * Applies a style that indicates the result of validation.
	 * <p>
	 * Calls
	 * {@link #applyValidationStyle(WWidget widget, WValidator.Result validation, EnumSet flags)
	 * applyValidationStyle(widget, validation, EnumSet.of(flag, flags))}
	 */
	public final void applyValidationStyle(WWidget widget,
			final WValidator.Result validation, ValidationStyleFlag flag,
			ValidationStyleFlag... flags) {
		applyValidationStyle(widget, validation, EnumSet.of(flag, flags));
	}

	public abstract boolean canBorderBoxElement(final DomElement element);
}
