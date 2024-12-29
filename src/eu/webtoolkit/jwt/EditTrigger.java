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

/** Enumeration that specifies the user action that triggers editing. */
public enum EditTrigger {
  /** Do not allow user to initiate editing. */
  None,
  /** Edit an item when clicked. */
  SingleClicked,
  /** Edit an item when double clicked. */
  DoubleClicked,
  /** Edit a selected item that is clicked again. */
  SelectedClicked;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
