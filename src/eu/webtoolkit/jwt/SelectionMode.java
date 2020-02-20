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
 * Enumeration that indicates how items may be selected.
 *
 * <p>
 *
 * @see WAbstractItemView#setSelectionMode(SelectionMode mode)
 */
public enum SelectionMode {
  /** No selections. */
  NoSelection(0),
  /** Single selection only. */
  SingleSelection(1),
  /** Multiple selection. */
  ExtendedSelection(3);

  private int value;

  SelectionMode(int value) {
    this.value = value;
  }

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return value;
  }
}
