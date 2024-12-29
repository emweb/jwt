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
 * Enumeration that indicates a drop action.
 *
 * <p>
 *
 * @see WAbstractItemModel#dropEvent(WDropEvent e, DropAction action, int row, int column,
 *     WModelIndex parent)
 */
public enum DropAction {
  /** Copy the selection. */
  Copy,
  /** Move the selection (deleting originals) */
  Move;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
