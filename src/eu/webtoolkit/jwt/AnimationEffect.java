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
 * An enumeration describing an animation effect.
 *
 * <p>An animation effect can be the combination of a motion and an optional fade effect, e.g:
 *
 * <pre>{@code
 * AnimationEffect::SlideInFromRight
 * AnimationEffect::SlideInFromTop | AnimationEffect::Fade
 *
 * }</pre>
 *
 * <p>You can specify only one motion effect.
 *
 * <p>
 */
public enum AnimationEffect {
  /** Slides right to show, left to hide. */
  SlideInFromLeft,
  /** Slides left to show, right to hide. */
  SlideInFromRight,
  /** Slides up to show, down to hide. */
  SlideInFromBottom,
  /** Slides down to show, up to hide. */
  SlideInFromTop,
  /** Pops up to show, pops away to hide. */
  Pop,
  /** Fade effect. */
  Fade;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
