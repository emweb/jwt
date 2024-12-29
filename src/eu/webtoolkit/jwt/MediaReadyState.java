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

/** Enumeration that indicates how much of (a media) is loaded. */
public enum MediaReadyState {
  /** No information available. */
  HaveNothing(0),
  /** Metadata loaded: duration, width, height. */
  HaveMetaData(1),
  /** Data at playback position is available. */
  HaveCurrentData(2),
  /** Have data to play for a while. */
  HaveFutureData(3),
  /** Enough to reach the end without stalling. */
  HaveEnoughData(4);

  private int value;

  MediaReadyState(int value) {
    this.value = value;
  }

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return value;
  }
}
