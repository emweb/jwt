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

final class SimpleSelectorImpl implements SimpleSelector {
  private static Logger logger = LoggerFactory.getLogger(SimpleSelectorImpl.class);

  public SimpleSelectorImpl() {
    super();
    this.elementName_ = "";
    this.elementType_ = DomElementType.UNKNOWN;
    this.classes_ = new ArrayList<String>();
    this.hashid_ = "";
  }

  public String getElementName() {
    return this.elementName_;
  }

  public DomElementType getElementType() {
    return this.elementType_;
  }

  public String getHashId() {
    return this.hashid_;
  }

  public List<String> getClasses() {
    return this.classes_;
  }

  public void setElementName(final String name) {
    this.elementName_ = name;
    this.elementType_ = DomElement.parseTagName(this.elementName_);
  }

  public void addClass(final String id) {
    this.classes_.add(id);
  }

  public void setHash(final String id) {
    if (!(this.hashid_.length() != 0)) {
      this.hashid_ = id;
    }
  }

  public String elementName_;
  public DomElementType elementType_;
  public List<String> classes_;
  public String hashid_;
}
