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

final class CombinedStyleSheet implements StyleSheet {
  private static Logger logger = LoggerFactory.getLogger(CombinedStyleSheet.class);

  public CombinedStyleSheet() {
    super();
    this.sheets_ = new ArrayList<StyleSheet>();
    this.sheets_owned_ = new ArrayList<StyleSheet>();
  }

  public void use(StyleSheet sh) {
    this.sheets_.add(sh);
  }

  public int getRulesetSize() {
    int result = 0;
    for (int i = 0; i < this.sheets_.size(); ++i) {
      result += this.sheets_.get(i).getRulesetSize();
    }
    return result;
  }

  public Ruleset rulesetAt(int j) {
    for (int i = 0; i < this.sheets_.size(); ++i) {
      if ((int) j < this.sheets_.get(i).getRulesetSize()) {
        return this.sheets_.get(i).rulesetAt(j);
      }
      j -= this.sheets_.get(i).getRulesetSize();
    }
    return this.sheets_.get(0).rulesetAt(0);
  }

  private List<StyleSheet> sheets_;
  private List<StyleSheet> sheets_owned_;
}
