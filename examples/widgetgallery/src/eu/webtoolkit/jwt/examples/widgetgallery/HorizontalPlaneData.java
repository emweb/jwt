/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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

class HorizontalPlaneData extends WStandardItemModel {
  private static Logger logger = LoggerFactory.getLogger(HorizontalPlaneData.class);

  public HorizontalPlaneData(int nbXpts, int nbYpts) {
    super(nbXpts + 1, nbYpts + 1);
    this.xStart_ = -10.0;
    this.xEnd_ = 10.0;
    this.yStart_ = -10.0;
    this.yEnd_ = 10.0;
  }

  public Object getData(final WModelIndex index, ItemDataRole role) {
    if (!role.equals(ItemDataRole.Display)) {
      return super.getData(index, role);
    }
    return 0.0;
  }

  private final double xStart_;
  private final double xEnd_;
  private final double yStart_;
  private final double yEnd_;
}
