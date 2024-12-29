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
 * Enumeration that indicates how to change a selection.
 *
 * <p>
 *
 * @see WAbstractItemView#select(WModelIndex index, SelectionFlag option)
 */
public enum SelectionFlag {
  /** Add to selection. */
  Select(1),
  /** Remove from selection. */
  Deselect(2),
  /** Toggle in selection. */
  ToggleSelect(3),
  /** Clear selection and add single item. */
  ClearAndSelect(4);

  private int value;

  SelectionFlag(int value) {
    this.value = value;
  }

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return value;
  }
}
