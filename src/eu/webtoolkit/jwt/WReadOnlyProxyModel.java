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
 * A read-only wrapper for a source model.
 *
 * <p>This is a simple proxy model which provides a read-only view on a source model. This is
 * convenient for situations where you want to share a common read-only source model between
 * different sessions.
 */
public class WReadOnlyProxyModel extends WAbstractProxyModel {
  private static Logger logger = LoggerFactory.getLogger(WReadOnlyProxyModel.class);

  /** Constructor. */
  public WReadOnlyProxyModel() {
    super();
  }
  /**
   * Maps a source model index to the proxy model.
   *
   * <p>Returns the sourceIndex unmodified.
   */
  public WModelIndex mapFromSource(final WModelIndex sourceIndex) {
    return sourceIndex;
  }
  /**
   * Maps a proxy model index to the source model.
   *
   * <p>Returns the proxyIndex unmodified.
   */
  public WModelIndex mapToSource(final WModelIndex proxyIndex) {
    return proxyIndex;
  }
  /**
   * Returns the number of columns.
   *
   * <p>This returns the column count of the source model.
   */
  public int getColumnCount(final WModelIndex parent) {
    return this.getSourceModel().getColumnCount(parent);
  }
  /**
   * Returns the number of rows.
   *
   * <p>This returns the row count of the source model.
   */
  public int getRowCount(final WModelIndex parent) {
    return this.getSourceModel().getRowCount(parent);
  }
  /**
   * Returns the parent for a model index.
   *
   * <p>Returns the parent of the given index in the source model.
   */
  public WModelIndex getParent(final WModelIndex index) {
    return this.getSourceModel().getParent(index);
  }
  /**
   * Returns the child index for the given row and column.
   *
   * <p>Returns the index in the source model.
   */
  public WModelIndex getIndex(int row, int column, final WModelIndex parent) {
    return this.getSourceModel().getIndex(row, column, parent);
  }
  /** Always returns <code>false</code> and has no effect. */
  public boolean setData(final WModelIndex index, final Object value, ItemDataRole role) {
    return false;
  }
  /** Always returns <code>false</code> and has no effect. */
  public boolean setItemData(
      final WModelIndex index, final SortedMap<ItemDataRole, Object> values) {
    return false;
  }
  /** Always returns <code>false</code> and has no effect. */
  public boolean setHeaderData(
      int section, Orientation orientation, final Object value, ItemDataRole role) {
    return false;
  }
  /** Always returns <code>false</code> and has no effect. */
  public boolean insertColumns(int column, int count, final WModelIndex parent) {
    return false;
  }
  /** Always returns <code>false</code> and has no effect. */
  public boolean removeColumns(int column, int count, final WModelIndex parent) {
    return false;
  }
  /** Has no effect. */
  public void dropEvent(
      final WDropEvent e, DropAction action, int row, int column, final WModelIndex parent) {}
}
