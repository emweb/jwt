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
 * Enumeration that specifies where the target of an anchor should be displayed.
 *
 * <p>
 *
 * @see WAnchor#setTarget(AnchorTarget target)
 */
public enum AnchorTarget {
  /** Show Instead of the application. */
  TargetSelf,
  /** Show in the top level frame of the application window. */
  TargetThisWindow,
  /** Show in a separate new tab or window. */
  TargetNewWindow,
  /** Useful only for a downloadable resource. */
  TargetDownload;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
