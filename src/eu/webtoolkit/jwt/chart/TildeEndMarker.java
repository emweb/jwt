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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TildeEndMarker extends WPainterPath {
  private static Logger logger = LoggerFactory.getLogger(TildeEndMarker.class);

  public TildeEndMarker(int segmentMargin) {
    super();
    this.moveTo(0, 0);
    this.lineTo(0, -(segmentMargin - 25));
    this.moveTo(-15, -(segmentMargin - 20));
    this.lineTo(15, -(segmentMargin - 10));
  }
}
