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

class HeaderProxyModel extends WAbstractTableModel {
  private static Logger logger = LoggerFactory.getLogger(HeaderProxyModel.class);

  public HeaderProxyModel(final WAbstractItemModel model) {
    super();
    this.model_ = model;
  }

  public int getColumnCount(final WModelIndex parent) {
    return this.model_.getColumnCount();
  }

  public int getRowCount(final WModelIndex parent) {
    return 1;
  }

  public Object getData(final WModelIndex index, ItemDataRole role) {
    return this.model_.getHeaderData(index.getColumn(), Orientation.Horizontal, role);
  }

  public boolean setData(final WModelIndex index, final Object value, ItemDataRole role) {
    return this.model_.setHeaderData(index.getColumn(), Orientation.Horizontal, value, role);
  }

  public EnumSet<ItemFlag> getFlags(final WModelIndex index) {
    EnumSet<HeaderFlag> headerFlags =
        this.model_.getHeaderFlags(index.getColumn(), Orientation.Horizontal);
    EnumSet<ItemFlag> result = EnumSet.noneOf(ItemFlag.class);
    if (headerFlags.contains(HeaderFlag.UserCheckable)) {
      result.add(ItemFlag.UserCheckable);
    }
    if (headerFlags.contains(HeaderFlag.Tristate)) {
      result.add(ItemFlag.Tristate);
    }
    if (headerFlags.contains(HeaderFlag.XHTMLText)) {
      result.add(ItemFlag.XHTMLText);
    }
    return result;
  }

  private WAbstractItemModel model_;
}
