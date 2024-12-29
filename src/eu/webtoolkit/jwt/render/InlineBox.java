/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

import eu.webtoolkit.jwt.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InlineBox extends LayoutBox {
  private static Logger logger = LoggerFactory.getLogger(InlineBox.class);

  public InlineBox() {
    super();
    this.utf8Pos = 0;
    this.utf8Count = 0;
    this.whitespaceWidth = 0;
    this.whitespaceCount = 0;
    this.baseline = 0;
  }

  public int utf8Pos;
  public int utf8Count;
  public double whitespaceWidth;
  public int whitespaceCount;
  public double baseline;
}
