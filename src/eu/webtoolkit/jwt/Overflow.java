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

/** How to handle overflow of inner content. */
public enum Overflow {
  /** Show content that overflows. */
  Visible,
  /** Show scrollbars when needed. */
  Auto,
  /** Hide content that overflows. */
  Hidden,
  /** Always show scroll bars. */
  Scroll;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
