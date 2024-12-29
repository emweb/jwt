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

class LayoutBox {
  private static Logger logger = LoggerFactory.getLogger(LayoutBox.class);

  public LayoutBox() {
    this.page = -1;
    this.x = 0;
    this.y = 0;
    this.width = 0;
    this.height = 0;
  }

  public boolean isNull() {
    return this.page == -1;
  }

  public int page;
  public double x;
  public double y;
  public double width;
  public double height;
}
