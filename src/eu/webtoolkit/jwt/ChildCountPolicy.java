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

/** An enumeration for the policy to display the child count. */
public enum ChildCountPolicy {
  /** Do not display a child count. */
  Disabled,
  /** Always display a child count. */
  Enabled,
  /** Lazy loading: on first use. */
  Lazy;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
