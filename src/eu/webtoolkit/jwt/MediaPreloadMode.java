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

/** Enumeration for preload strategy. */
public enum MediaPreloadMode {
  /** Hints that the user will probably not play the video. */
  None,
  /** Hints that it is ok to download the entire resource. */
  Auto,
  /** Hints that retrieving metadata is a good option. */
  Metadata;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
