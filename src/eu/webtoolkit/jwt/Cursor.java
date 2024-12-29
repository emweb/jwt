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
 * Enumeration for a cursor style.
 *
 * <p>
 *
 * @see WCssDecorationStyle#setCursor(Cursor c)
 * @see WAbstractArea#setCursor(Cursor cursor)
 */
public enum Cursor {
  /** Arrow, CSS &apos;default&apos; cursor. */
  Arrow,
  /** Cursor chosen by the browser, CSS &apos;auto&apos; cursor. */
  Auto,
  /** Crosshair, CSS &apos;cross&apos; cursor. */
  Cross,
  /** Pointing hand, CSS &apos;pointer&apos; cursor. */
  PointingHand,
  /** Open hand, CSS &apos;move&apos; cursor. */
  OpenHand,
  /** Wait, CSS &apos;wait&apos; cursor. */
  Wait,
  /** Text edit, CSS &apos;text&apos; cursor. */
  IBeam,
  /** Help, CSS &apos;help&apos; cursor. */
  WhatsThis;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
