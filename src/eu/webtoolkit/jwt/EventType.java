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
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

/**
 * An enumeration describing an event&apos;s type.
 *
 * <p>
 *
 * @see WEvent#getEventType()
 */
public enum EventType {
  /** An event which is not user- or timer-initiated. */
  Other,
  /** A user-initiated event. */
  User,
  /** A timer-initiated event. */
  Timer,
  /** An event which is a resource request. */
  Resource;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
