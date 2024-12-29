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

/**
 * Enumeration that indiciates a standard icon.
 *
 * <p>
 *
 * @see WMessageBox
 */
public enum Icon {
  /** No icon. */
  None(0),
  /** An information icon. */
  Information(1),
  /** A warning icon. */
  Warning(2),
  /** A critical icon. */
  Critical(3),
  /** A question icon. */
  Question(4);

  private int value;

  Icon(int value) {
    this.value = value;
  }

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return value;
  }
}
