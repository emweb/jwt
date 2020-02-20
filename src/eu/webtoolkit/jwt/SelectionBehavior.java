/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Enumeration that indicates what is being selected.
 *
 * <p>
 *
 * @see WAbstractItemView#setSelectionBehavior(SelectionBehavior behavior)
 */
public enum SelectionBehavior {
  /** Select single items. */
  SelectItems(0),
  /** Select only rows. */
  SelectRows(1);

  private int value;

  SelectionBehavior(int value) {
    this.value = value;
  }

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return value;
  }
}
