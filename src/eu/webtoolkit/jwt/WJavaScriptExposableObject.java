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
 * A JavaScript exposable object.
 *
 * <p>A JavaScript bound object (as opposed to being mostly a simple value class) has an equivalent
 * representation in JavaScript. Its value can usually only be modified through a {@link
 * WJavaScriptHandle}. There are certain exceptions to this rule. Some methods, notably many {@link
 * WTransform} methods, will correctly apply these modifications also on the JavaScript
 * representation.
 *
 * <p>
 *
 * @see WJavaScriptHandle
 */
public abstract class WJavaScriptExposableObject {
  private static Logger logger = LoggerFactory.getLogger(WJavaScriptExposableObject.class);

  public WJavaScriptExposableObject() {
    this.clientBinding_ = null;
  }

  public WJavaScriptExposableObject(final WJavaScriptExposableObject other) {
    this.clientBinding_ = other.clientBinding_;
  }

  public abstract WJavaScriptExposableObject clone();
  /**
   * Returns whether this object is JavaScript bound.
   *
   * <p>An object is JavaScript bound if it is associated with a {@link WJavaScriptHandle}. It
   * should not be modified directly on the server side. {@link WJavaScriptHandle#setValue} should
   * be used instead.
   */
  public boolean isJavaScriptBound() {
    return this.clientBinding_ != null;
  }
  /**
   * Returns a JavaScript representation of the value of this object.
   *
   * <p>
   *
   * <p><i><b>Note: </b>The value returned will reflect the current server side value of the object.
   * If this object is JavaScript bound, this value may not reflect the actual client side value. If
   * you need access to the client side value, use {@link WJavaScriptExposableObject#getJsRef()
   * getJsRef()} intead. </i>
   */
  public abstract String getJsValue();
  /**
   * Returns a JavaScript reference to this object.
   *
   * <p>If this object is not JavaScript bound, it will return a JavaScript representation of the
   * value of the object, according to {@link WJavaScriptExposableObject#getJsValue() getJsValue()}.
   *
   * <p>
   *
   * <p><i><b>Warning: </b>This reference is intended as read-only. Attempts to modify it may have
   * unintended consequences. If you want a JavaScript reference that is modifiable, use the {@link
   * WJavaScriptHandle#getJsRef()} instead. </i>
   */
  public String getJsRef() {
    if (this.clientBinding_ != null) {
      return this.clientBinding_.jsRef_;
    } else {
      return this.getJsValue();
    }
  }

  public boolean closeTo(final WJavaScriptExposableObject other) {
    return false;
  }

  protected boolean sameBindingAs(final WJavaScriptExposableObject rhs) {
    if (!(this.clientBinding_ != null) && !(rhs.clientBinding_ != null)) {
      return true;
    } else {
      if (this.clientBinding_ != null && rhs.clientBinding_ != null) {
        return this.clientBinding_.equals(rhs.clientBinding_);
      } else {
        return false;
      }
    }
  }

  protected void assignBinding(final WJavaScriptExposableObject rhs) {
    assert rhs.clientBinding_ != null;
    if (rhs != this) {
      if (this.clientBinding_ != null) {}

      this.clientBinding_ = rhs.clientBinding_;
    }
  }

  protected void assignBinding(final WJavaScriptExposableObject rhs, final String jsRef) {
    assert rhs.clientBinding_ != null;
    if (rhs != this) {
      if (this.clientBinding_ != null) {}

      this.clientBinding_ = new WJavaScriptExposableObject.JSInfo(rhs.clientBinding_);
    }
    this.clientBinding_.jsRef_ = jsRef;
  }

  protected void checkModifiable() {
    if (this.isJavaScriptBound()) {
      throw new WException("Trying to modify a JavaScript bound object!");
    }
  }

  protected abstract void assignFromJSON(final com.google.gson.JsonElement value);

  static class JSInfo {
    private static Logger logger = LoggerFactory.getLogger(JSInfo.class);

    public JSInfo(WJavaScriptObjectStorage context, final String jsRef) {
      this.context_ = context;
      this.jsRef_ = jsRef;
    }

    public JSInfo(final WJavaScriptExposableObject.JSInfo other) {
      this.context_ = other.context_;
      this.jsRef_ = other.jsRef_;
    }

    public boolean equals(final WJavaScriptExposableObject.JSInfo rhs) {
      return this.context_ == rhs.context_ && this.jsRef_.equals(rhs.jsRef_);
    }

    public WJavaScriptObjectStorage context_;
    public String jsRef_;
  }

  WJavaScriptExposableObject.JSInfo clientBinding_;
}
