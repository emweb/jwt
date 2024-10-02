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

class DeclarationBlockImpl implements DeclarationBlock {
  private static Logger logger = LoggerFactory.getLogger(DeclarationBlockImpl.class);

  public DeclarationBlockImpl() {
    super();
    this.properties_ = new HashMap<String, Term>();
    this.declarationString_ = "";
  }

  public Term value(final String property) {
    Term iter = this.properties_.get(property);
    return iter != null ? iter : new Term();
  }

  public String getDeclarationString() {
    return this.declarationString_;
  }

  public Map<String, Term> properties_;
  public String declarationString_;
}
