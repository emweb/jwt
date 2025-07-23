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
 * Enumeration that describes different autocomplete modes.
 *
 * <p>The autocomplete mode tells the browser what type of information is required by the field.
 * This helps the browser to complete the field for the user.
 *
 * <p>
 */
public enum AutoCompleteMode {
  /**
   * Forbid the browser to automatically enter or select values.
   *
   * <p>
   *
   * <p><i><b>Note: </b>In most modern browsers, this will not stop password manager to do it. </i>
   */
  Off,
  /** The browser will &quot;guess&quot; what type of data is required. */
  On,
  /**
   * A new password. This should be used with field for entering a new password or confirming the
   * new password.
   */
  NewPassword,
  /** The current password of the user. */
  CurrentPassword,
  /** An accout name or username. */
  Username;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
