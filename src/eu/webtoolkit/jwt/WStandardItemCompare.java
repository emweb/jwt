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

class WStandardItemCompare implements Comparator<Integer> {
  private static Logger logger = LoggerFactory.getLogger(WStandardItemCompare.class);

  public WStandardItemCompare(WStandardItem anItem, int aColumn, SortOrder anOrder) {
    super();
    this.item = anItem;
    this.column = aColumn;
    this.order = anOrder;
  }

  public int compare(Integer r1, Integer r2) {
    WStandardItem item1 = this.item.getChild(r1, this.column);
    WStandardItem item2 = this.item.getChild(r2, this.column);
    int result;
    if (item1 != null) {
      if (item2 != null) {
        result = item1.compare(item2);
      } else {
        result = 1;
      }
    } else {
      if (item2 != null) {
        result = -1;
      } else {
        result = 0;
      }
    }
    if (this.order == SortOrder.Descending) {
      result = -result;
    }
    return result;
  }

  public WStandardItem item;
  public int column;
  public SortOrder order;
}
