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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StringListModelCompare implements Comparator<Integer> {
  private static Logger logger = LoggerFactory.getLogger(StringListModelCompare.class);

  public WStringListModel model_;
  public SortOrder order_;

  public StringListModelCompare(WStringListModel model, SortOrder order) {
    super();
    this.model_ = model;
    this.order_ = order;
  }

  public int compare(Integer r1, Integer r2) {
    int result = this.model_.getStringList().get(r1).compareTo(this.model_.getStringList().get(r2));
    if (this.order_ == SortOrder.Descending) {
      result = -result;
    }
    return result;
  }
}
