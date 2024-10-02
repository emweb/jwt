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

class Specificity {
  private static Logger logger = LoggerFactory.getLogger(Specificity.class);

  public Specificity(boolean valid) {
    this.value_ = 0;
    this.valid_ = valid;
  }

  public Specificity() {
    this(true);
  }

  public Specificity(int a, int b, int c, int d) {
    this.value_ = 0;
    this.valid_ = true;
    this.setA(a);
    this.setB(b);
    this.setC(c);
    this.setD(d);
  }

  public boolean isValid() {
    return this.valid_;
  }

  public boolean equals(final Specificity other) {
    return this.valid_ == other.valid_ && this.value_ == other.value_;
  }

  public void setA(int a) {
    this.value_ = this.value_ & ~0xFF000000 | (a & 0xFF) << 24;
  }

  public void setB(int b) {
    this.value_ = this.value_ & ~0x00FF0000 | (b & 0xFF) << 16;
  }

  public void setC(int c) {
    this.value_ = this.value_ & ~0x0000FF00 | (c & 0xFF) << 8;
  }

  public void setD(int d) {
    this.value_ = this.value_ & ~0x000000FF | d & 0xFF;
  }

  public boolean isSmallerThen(final Specificity other) {
    return !this.valid_ ? true : this.value_ < other.value_;
  }

  public boolean isGreaterThen(final Specificity other) {
    return !other.valid_ ? true : this.value_ > other.value_;
  }

  public boolean isSmallerOrEqualThen(final Specificity other) {
    return !this.isGreaterThen(other);
  }

  public boolean isGreaterOrEqualThen(final Specificity other) {
    return !this.isSmallerThen(other);
  }

  private long value_;
  private boolean valid_;
}
