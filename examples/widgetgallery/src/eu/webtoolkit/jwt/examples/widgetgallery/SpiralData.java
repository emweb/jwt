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

class SpiralData extends WStandardItemModel {
  private static Logger logger = LoggerFactory.getLogger(SpiralData.class);

  public SpiralData(int nbPts) {
    super(nbPts, 3);
    this.nbPts_ = nbPts;
  }

  public Object getData(final WModelIndex index, ItemDataRole role) {
    if (!role.equals(ItemDataRole.Display)) {
      return super.getData(index, role);
    }
    final double pi = 3.141592;
    double XYangle = index.getRow() * (8 * pi / this.nbPts_);
    double heightRatio = (float) index.getRow() / this.getRowCount();
    double radius = 1.0 + heightRatio * 5.0;
    if (index.getColumn() == 0) {
      return radius * Math.cos(XYangle);
    } else {
      if (index.getColumn() == 1) {
        return radius * Math.sin(XYangle);
      } else {
        if (index.getColumn() == 2) {
          return 5.0 - index.getRow() * (10.0 / this.nbPts_);
        } else {
          return null;
        }
      }
    }
  }

  private int nbPts_;
}
