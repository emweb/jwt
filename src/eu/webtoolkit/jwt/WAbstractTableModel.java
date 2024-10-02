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
 * An abstract table model for use with JWt&apos;s view classes.
 *
 * <p>An abstract table model specializes {@link WAbstractItemModel} for two-dimensional tables (but
 * no hierarchical models).
 *
 * <p>It cannot be used directly but must be subclassed. Subclassed models must at least reimplement
 * {@link WAbstractItemModel#getColumnCount(WModelIndex parent)
 * WAbstractItemModel#getColumnCount()}, {@link WAbstractItemModel#getRowCount(WModelIndex parent)
 * WAbstractItemModel#getRowCount()} and {@link WAbstractItemModel#getData(WModelIndex index,
 * ItemDataRole role) WAbstractItemModel#getData()}.
 */
public abstract class WAbstractTableModel extends WAbstractItemModel {
  private static Logger logger = LoggerFactory.getLogger(WAbstractTableModel.class);

  /** Creates a new abstract list model. */
  public WAbstractTableModel() {
    super();
  }

  public WModelIndex getParent(final WModelIndex parent) {
    return null;
  }

  public WModelIndex getIndex(int row, int column, final WModelIndex index) {
    return this.createIndex(row, column, null);
  }
}
