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
 * A localized string resolver that bundles multiple string resolvers.
 *
 * <p>This class implements the localized strings interface and delegates {@link WString#tr(String
 * key) WString#tr()} string resolution to one or more string resolvers. You will typically use this
 * class if you want to combine different methods of string resolution (e.g. some from files, and
 * other strings using a database).
 *
 * <p>
 *
 * @see WApplication#setLocalizedStrings(WLocalizedStrings translator)
 */
public class WCombinedLocalizedStrings extends WLocalizedStrings {
  private static Logger logger = LoggerFactory.getLogger(WCombinedLocalizedStrings.class);

  /** Constructor. */
  public WCombinedLocalizedStrings() {
    super();
    this.localizedStrings_ = new ArrayList<WLocalizedStrings>();
  }
  /**
   * Adds a string resolver.
   *
   * <p>The order in which string resolvers are added is significant: {@link
   * WCombinedLocalizedStrings#resolveKey(Locale locale, String key) resolveKey()} will consult each
   * string resolver in the order they have been added, until a match is found.
   */
  public void add(final WLocalizedStrings resolver) {
    this.insert(this.localizedStrings_.size(), resolver);
  }
  /**
   * Inserts a string resolver.
   *
   * <p>
   *
   * @see WCombinedLocalizedStrings#add(WLocalizedStrings resolver)
   */
  public void insert(int index, final WLocalizedStrings resolver) {
    this.localizedStrings_.add(0 + index, resolver);
  }
  /**
   * Removes a string resolver.
   *
   * <p>
   *
   * @see WCombinedLocalizedStrings#add(WLocalizedStrings resolver)
   */
  public void remove(final WLocalizedStrings resolver) {
    this.localizedStrings_.remove(resolver);
  }

  public List<WLocalizedStrings> getItems() {
    return this.localizedStrings_;
  }

  public void hibernate() {
    for (int i = 0; i < this.localizedStrings_.size(); ++i) {
      this.localizedStrings_.get(i).hibernate();
    }
  }

  public LocalizedString resolveKey(final Locale locale, final String key) {
    for (int i = 0; i < this.localizedStrings_.size(); ++i) {
      LocalizedString result = this.localizedStrings_.get(i).resolveKey(locale, key);
      if (result.success) {
        return result;
      }
    }
    return new LocalizedString();
  }

  public LocalizedString resolvePluralKey(final Locale locale, final String key, long amount) {
    for (int i = 0; i < this.localizedStrings_.size(); ++i) {
      LocalizedString result = this.localizedStrings_.get(i).resolvePluralKey(locale, key, amount);
      if (result.success) {
        return result;
      }
    }
    return new LocalizedString();
  }

  private List<WLocalizedStrings> localizedStrings_;
}
