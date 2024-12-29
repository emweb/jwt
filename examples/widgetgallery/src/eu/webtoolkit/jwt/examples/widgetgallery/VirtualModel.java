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

class VirtualModel extends WAbstractTableModel {
  private static Logger logger = LoggerFactory.getLogger(VirtualModel.class);

  public VirtualModel(int rows, int columns) {
    super();
    this.rows_ = rows;
    this.columns_ = columns;
  }

  public int getRowCount(final WModelIndex parent) {
    if (!(parent != null)) {
      return this.rows_;
    } else {
      return 0;
    }
  }

  public int getColumnCount(final WModelIndex parent) {
    if (!(parent != null)) {
      return this.columns_;
    } else {
      return 0;
    }
  }

  public Object getData(final WModelIndex index, ItemDataRole role) {
    if (role.equals(ItemDataRole.Display)) {
      if (index.getColumn() == 0) {
        return new WString("Row {1}").arg(index.getRow());
      } else {
        return new WString("Item row {1}, col {2}").arg(index.getRow()).arg(index.getColumn());
      }
    } else {
      return null;
    }
  }

  public Object getHeaderData(int section, Orientation orientation, ItemDataRole role) {
    if (orientation == Orientation.Horizontal) {
      if (role.equals(ItemDataRole.Display)) {
        return new WString("Column {1}").arg(section);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  private int rows_;
  private int columns_;
}
