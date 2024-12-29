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

class NumericItem extends WStandardItem {
  private static Logger logger = LoggerFactory.getLogger(NumericItem.class);

  public WStandardItem clone() {
    return new NumericItem();
  }

  public void setData(final Object data, ItemDataRole role) {
    if (role.equals(ItemDataRole.Edit)) {
      Object dt = new Object();
      double d = StringUtils.asNumber(data);
      if (d != d) {
        dt = data;
      } else {
        dt = d;
      }
      super.setData(dt, role);
    } else {
      super.setData(data, role);
    }
  }
}
