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
 * Enumeration that describes different input modes.
 *
 * <p>The input mode tells the browser what layout should be used for a virtual keybord when editing
 * this field. This mainly impacts phone users.
 *
 * <p>
 */
public enum InputMode {
  /** Does not specify any input mode to the browser. */
  Off,
  /** No virtual keyboard should be displayed. */
  None,
  /** The locale-specific standard virtual keyboard. */
  Text,
  /** A numeric virtual keyboard wich also have &quot;#&quot;&quot; and &quot;*". */
  Tel,
  /** Ensure that the virtual keyboard has &quot;/&quot;. */
  Url,
  /** Ensure that the virtual keyboard has &quot;@&quot;. */
  Email,
  /**
   * Ensure that the virtual keyboard has the digit from 0 to 9. Does usually show only the numbers
   * with maybe also &quot;-&quot; .
   */
  Numeric,
  /** Like Numeric + ensure that the virtual keyboard has the decimal separator. */
  Decimal,
  /** A virtual keyboard convenient for search. */
  Search;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
