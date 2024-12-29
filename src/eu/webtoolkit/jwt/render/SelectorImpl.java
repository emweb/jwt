/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

import eu.webtoolkit.jwt.*;
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

final class SelectorImpl implements Selector {
  private static Logger logger = LoggerFactory.getLogger(SelectorImpl.class);

  public SelectorImpl() {
    super();
    this.simpleSelectors_ = new ArrayList<SimpleSelectorImpl>();
  }

  public int getSize() {
    return this.simpleSelectors_.size();
  }

  public SimpleSelector at(int i) {
    return this.simpleSelectors_.get(i);
  }

  public Specificity getSpecificity() {
    int a = 0;
    int b = 0;
    int c = 0;
    int d = 0;
    for (int i = 0; i < this.simpleSelectors_.size(); ++i) {
      if (this.simpleSelectors_.get(i).hashid_.length() != 0) {
        ++b;
      }
      c += this.simpleSelectors_.get(i).classes_.size();
      if (this.simpleSelectors_.get(i).elementName_.length() != 0
          && !this.simpleSelectors_.get(i).elementName_.equals("*")) {
        ++d;
      }
    }
    return new Specificity(a, b, c, d);
  }

  public void addSimpleSelector(final SimpleSelectorImpl s) {
    this.simpleSelectors_.add(s);
  }

  public List<SimpleSelectorImpl> simpleSelectors_;
}
