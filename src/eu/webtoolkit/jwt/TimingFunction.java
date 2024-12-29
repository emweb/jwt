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
 * A timing function.
 *
 * <p>The timing function defines how the animation effects are animated during the total duration
 * of the animation.
 */
public enum TimingFunction {
  /** Slow start and slow finish. */
  Ease,
  /** Linear throughout. */
  Linear,
  /** Slow start. */
  EaseIn,
  /** Slow finish. */
  EaseOut,
  /** Slow start and even slower finish. */
  EaseInOut,
  /** (Currently unsupported) */
  CubicBezier;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
