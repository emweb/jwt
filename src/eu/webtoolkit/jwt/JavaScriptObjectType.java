/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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

/**
 * Enumeration for a JavaScript object type.
 *
 * <p>This is an internal JWt type.
 */
public enum JavaScriptObjectType {
  JavaScriptFunction,
  JavaScriptConstructor,
  JavaScriptObject,
  JavaScriptPrototype;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
