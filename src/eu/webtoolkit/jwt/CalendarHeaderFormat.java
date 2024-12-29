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

/** The calendar header format. */
public enum CalendarHeaderFormat {
  /** First letter of a day (e.g. &apos;M&apos; for Monday) */
  SingleLetterDayNames,
  /** First 3 letters of a day (e.g. &apos;Mon&apos; for Monday) */
  ShortDayNames,
  /** Full day name. */
  LongDayNames;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
