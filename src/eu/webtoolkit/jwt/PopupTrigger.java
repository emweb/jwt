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

/** Enumeration that defines a trigger for showing the popup. */
public enum PopupTrigger {
  /**
   * Shows popup when the user starts editing.
   *
   * <p>The popup is shown when the currently edited text has a length longer than the filter
   * length.
   */
  Editing,
  /**
   * Shows popup when user clicks a drop down icon.
   *
   * <p>The lineedit is modified to show a drop down icon, and clicking the icon shows the
   * suggestions, very much like a {@link WComboBox}.
   */
  DropDownIcon;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
