/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/** Enumeration that indicates the palette flavour. */
public enum PaletteFlavour {
  /** Neutral palette. */
  Neutral(0),
  /** Bold palette. */
  Bold(1),
  /** Muted palette. */
  Muted(2),
  /** Grayscale palette. */
  GrayScale(255);

  private int value;

  PaletteFlavour(int value) {
    this.value = value;
  }

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return value;
  }
}
