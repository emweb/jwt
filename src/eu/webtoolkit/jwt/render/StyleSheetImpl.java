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

class StyleSheetImpl implements StyleSheet {
  private static Logger logger = LoggerFactory.getLogger(StyleSheetImpl.class);

  public StyleSheetImpl() {
    super();
    this.rulesetArray_ = new ArrayList<RulesetImpl>();
  }

  public int getRulesetSize() {
    return this.rulesetArray_.size();
  }

  public Ruleset rulesetAt(int i) {
    return this.rulesetArray_.get(i);
  }

  public List<RulesetImpl> rulesetArray_;
}
