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

/** An enumeration for a media encoding. */
public enum MediaEncoding {
  /** The poster image (e.g. JPG/PNG) for a video. */
  PosterImage,
  /** Audio: MP3 encoding (<b>essential audio</b> format) */
  MP3,
  /** Audio: MP4 encoding (<b>essential audio</b> format) */
  M4A,
  /** Audio: OGG encoding. */
  OGA,
  /** Audio: WAV (uncompressed) format. */
  WAV,
  /** Audio: WebM encoding. */
  WEBMA,
  /** Audio: Flash format. */
  FLA,
  /** Video: MP4 encoding (<b>essential video</b> format) */
  M4V,
  /** Video: OGG encoding. */
  OGV,
  /** Video: WebM encoding. */
  WEBMV,
  /** Video: Flash format. */
  FLV;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
