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
 * Enumeration that specifies an option for rendering a view item.
 *
 * <p>
 *
 * @see WAbstractItemDelegate#update(WWidget widget, WModelIndex index, EnumSet flags)
 */
public enum ViewItemRenderFlag {
  /** Render as selected */
  Selected,
  /** Render in editing mode */
  Editing,
  /** Render (the editor) focused */
  Focused,
  /** Render as invalid */
  Invalid;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
