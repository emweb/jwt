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
 * Enumeration for a cursor style.
 *
 * <p>
 *
 * @see WCssDecorationStyle#setCursor(Cursor c)
 * @see WAbstractArea#setCursor(Cursor cursor)
 */
public enum Cursor {
  /** Arrow, CSS &apos;default&apos; cursor. */
  ArrowCursor,
  /** Cursor chosen by the browser, CSS &apos;auto&apos; cursor. */
  AutoCursor,
  /** Crosshair, CSS &apos;cross&apos; cursor. */
  CrossCursor,
  /** Pointing hand, CSS &apos;pointer&apos; cursor. */
  PointingHandCursor,
  /** Open hand, CSS &apos;move&apos; cursor. */
  OpenHandCursor,
  /** Wait, CSS &apos;wait&apos; cursor. */
  WaitCursor,
  /** Text edit, CSS &apos;text&apos; cursor. */
  IBeamCursor,
  /** Help, CSS &apos;help&apos; cursor. */
  WhatsThisCursor;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
