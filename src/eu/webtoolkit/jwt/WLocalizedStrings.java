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
 * An abstract class that provides support for localized strings.
 *
 * <p>This abstract class provides the content to localized WStrings, by resolving the key to a
 * string using the current application locale.
 *
 * <p>
 *
 * @see WString#tr(String key)
 * @see WApplication#setLocalizedStrings(WLocalizedStrings translator)
 */
public abstract class WLocalizedStrings {
  private static Logger logger = LoggerFactory.getLogger(WLocalizedStrings.class);

  /**
   * Purges memory resources, if possible.
   *
   * <p>This is called afer event handling, and is an opportunity to conserve memory inbetween
   * events, by freeing memory used for cached key/value bindings, if applicable.
   *
   * <p>The default implementation does nothing.
   */
  public void hibernate() {}
  /**
   * Resolves a key in the given locale.
   *
   * <p>This method is used by {@link WString} to obtain the UTF-8 value corresponding to a key in
   * the given locale.
   *
   * <p>Returns a successful {@link LocalizedString} if the key could be resolved.
   *
   * <p>
   *
   * @see WString#tr(String key)
   */
  public abstract LocalizedString resolveKey(final Locale locale, final String key);
  /**
   * Resolves the plural form of a key in the given locale.
   *
   * <p>This method is used by {@link WString} to obtain the UTF-8 value corresponding to a key in
   * the current locale, taking into account the possibility of multiple plural forms, and chosing
   * the right plural form based on the <code>amount</code> passed.
   *
   * <p>Throws a std::logic_error if the underlying implementation does not provide support for
   * plural internationalized strings.
   *
   * <p>Returns a successful {@link LocalizedString} if the key could be resolved.
   *
   * <p>
   */
  public LocalizedString resolvePluralKey(final Locale locale, final String key, long amount) {
    throw new WException("WLocalizedStrings::resolvePluralKey is not supported");
  }
  /**
   * Utility method to evaluate a plural expression.
   *
   * <p>This evaluates C expressions such as used by ngettext for a particular value, which can be
   * useful to implement plural key resolution.
   *
   * <p>
   *
   * @see WLocalizedStrings#resolvePluralKey(Locale locale, String key, long amount)
   */
  public static int evaluatePluralExpression(final String expression, long n) {
    return PluralExpression.evalPluralCase(expression, n);
  }
}
