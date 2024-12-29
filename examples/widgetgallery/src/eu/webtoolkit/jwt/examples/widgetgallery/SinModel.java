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

class SinModel extends WAbstractChartModel {
  private static Logger logger = LoggerFactory.getLogger(SinModel.class);

  public SinModel(double minimum, double maximum, int rows) {
    super();
    this.minimum_ = minimum;
    this.maximum_ = maximum;
    this.rows_ = rows;
  }

  public double getData(int row, int column) {
    double x = this.minimum_ + row * (this.maximum_ - this.minimum_) / (this.getRowCount() - 1);
    if (column == 0) {
      return x;
    } else {
      return Math.sin(x) + Math.sin(x * 100.0) / 40.0;
    }
  }

  public int getColumnCount() {
    return 2;
  }

  public int getRowCount() {
    return this.rows_;
  }

  public double getMinimum() {
    return this.minimum_;
  }

  public double getMaximum() {
    return this.maximum_;
  }

  private double minimum_;
  private double maximum_;
  private int rows_;
}
