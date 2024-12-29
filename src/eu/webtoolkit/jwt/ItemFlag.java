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
 * Flags that indicate data item options.
 *
 * <p>
 *
 * @see WModelIndex#getFlags()
 */
public enum ItemFlag {
  /** Item can be selected. */
  Selectable,
  /** Item can be edited. */
  Editable,
  /** Item can be checked (checkbox is enabled) */
  UserCheckable,
  /** Item can be dragged. */
  DragEnabled,
  /** Item can be a drop target. */
  DropEnabled,
  /**
   * Item has tree states.
   *
   * <p>When set, {@link ItemDataRole#Checked} data is of type {@link CheckState}
   */
  Tristate,
  /** Item&apos;s text ({@link ItemDataRole#Display}, {@link ItemDataRole#ToolTip}) is HTML. */
  XHTMLText,
  /** Item&apos;s value has been modified. */
  Dirty,
  /** Item&apos;s tooltip is deferred. */
  DeferredToolTip;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
