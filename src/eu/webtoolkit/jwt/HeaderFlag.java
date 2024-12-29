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
 * Flags that indicate table header options.
 *
 * <p>
 *
 * @see WAbstractItemModel#getHeaderFlags(int section, Orientation orientation)
 */
public enum HeaderFlag {
  /**
   * Flag that indicates that the column can be expanded.
   *
   * <p>
   *
   * @see WAbstractItemModel#expandColumn(int column)
   */
  ColumnIsCollapsed,
  /**
   * Flag that indicates that the column was expanded to the left.
   *
   * <p>
   *
   * @see WAbstractItemModel#collapseColumn(int column)
   */
  ColumnIsExpandedLeft,
  /**
   * Flag that indicates that the column was expanded to the right.
   *
   * <p>
   *
   * @see WAbstractItemModel#collapseColumn(int column)
   */
  ColumnIsExpandedRight,
  /** Flag that indicates that the header can be checked. */
  UserCheckable,
  /**
   * Flag that indicates that the item has three states.
   *
   * <p>When set, {@link ItemDataRole#Checked} data is of type {@link CheckState}
   */
  Tristate,
  /**
   * Flag that indicates that the item text ({@link ItemDataRole#Display}, {@link
   * ItemDataRole#ToolTip}) is HTML.
   */
  XHTMLText;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
