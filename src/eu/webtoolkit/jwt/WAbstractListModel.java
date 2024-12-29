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

/**
 * An abstract list model for use with JWt&apos;s view classes.
 *
 * <p>An abstract list model specializes {@link WAbstractItemModel} for one-dimensional lists (i.e.
 * a model with 1 column and no children).
 *
 * <p>It cannot be used directly but must be subclassed. Subclassed models must at least reimplement
 * {@link WAbstractItemModel#getRowCount(WModelIndex parent) WAbstractItemModel#getRowCount()} to
 * return the number of rows, and {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
 * role) WAbstractItemModel#getData()} to return data.
 */
public abstract class WAbstractListModel extends WAbstractItemModel {
  private static Logger logger = LoggerFactory.getLogger(WAbstractListModel.class);

  /** Create a new abstract list model. */
  public WAbstractListModel() {
    super();
  }

  public WModelIndex getParent(final WModelIndex index) {
    return null;
  }

  public WModelIndex getIndex(int row, int column, final WModelIndex parent) {
    return this.createIndex(row, column, null);
  }

  public int getColumnCount(final WModelIndex parent) {
    return (parent != null) ? 0 : 1;
  }
}
