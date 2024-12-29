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

/** The state in which validated input can exist. */
public enum ValidationState {
  /** The input is invalid. */
  Invalid,
  /** The input is invalid (empty and mandatory). */
  InvalidEmpty,
  /** The input is valid. */
  Valid;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
