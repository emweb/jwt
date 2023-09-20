/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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
 * A proxy model that proxies its source model unmodified.
 *
 * <p>A {@link WIdentityProxyModel} simply forwards the structure of the source model, without any
 * transformation. {@link WIdentityProxyModel} can be used as a base class for implementing proxy
 * models that reimplement data(), but retain all other characteristics of the source model.
 */
public class WIdentityProxyModel extends WAbstractProxyModel {
  private static Logger logger = LoggerFactory.getLogger(WIdentityProxyModel.class);

  /** Constructor. */
  public WIdentityProxyModel() {
    super();
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
  }
  /**
   * Returns the number of columns.
   *
   * <p>Translates the parent index to the source model, and returns the number of columns of the
   * source model.
   */
  public int getColumnCount(final WModelIndex parent) {
    return this.getSourceModel().getColumnCount(this.mapToSource(parent));
  }
  /**
   * Returns the number of rows.
   *
   * <p>Translates the parent index to the source model, and returns the number of rows of the
   * source model.
   */
  public int getRowCount(final WModelIndex parent) {
    return this.getSourceModel().getRowCount(this.mapToSource(parent));
  }
  /**
   * Returns the parent for a model index.
   *
   * <p>Translates the child index to the source model, and translates its parent back to this proxy
   * model.
   */
  public WModelIndex getParent(final WModelIndex child) {
    final WModelIndex sourceIndex = this.mapToSource(child);
    final WModelIndex sourceParent = sourceIndex.getParent();
    return this.mapFromSource(sourceParent);
  }
  /** Returns the child index for the given row and column. */
  public WModelIndex getIndex(int row, int column, final WModelIndex parent) {
    if (!this.hasIndex(row, column, parent)) {
      return null;
    }
    final WModelIndex sourceParent = this.mapToSource(parent);
    final WModelIndex sourceIndex = this.getSourceModel().getIndex(row, column, sourceParent);
    return this.mapFromSource(sourceIndex);
  }
  /**
   * Maps a proxy model index to the source model.
   *
   * <p>Returns a model index with the same row and column as the source index. The parent index is
   * mapped recursively.
   */
  public WModelIndex mapFromSource(final WModelIndex sourceIndex) {
    if (!(sourceIndex != null)) {
      return null;
    }
    return this.createIndex(
        sourceIndex.getRow(), sourceIndex.getColumn(), sourceIndex.getInternalPointer());
  }
  /**
   * Maps a source model index to the proxy model.
   *
   * <p>Returns a model index with the same row and column as the proxy index. The parent index is
   * mapped recursively.
   */
  public WModelIndex mapToSource(final WModelIndex proxyIndex) {
    if (!(this.getSourceModel() != null) || !(proxyIndex != null)) {
      return null;
    }
    return this.createSourceIndex(
        proxyIndex.getRow(), proxyIndex.getColumn(), proxyIndex.getInternalPointer());
  }
  /**
   * Sets the source model.
   *
   * <p>The source model provides the actual data for the proxy model.
   *
   * <p>Ownership of the source model is <i>not</i> transferred.
   *
   * <p>All signals of the source model are forwarded to the proxy model.
   */
  public void setSourceModel(final WAbstractItemModel newSourceModel) {
    if (this.getSourceModel() != null) {
      for (int i = 0; i < this.modelConnections_.size(); ++i) {
        this.modelConnections_.get(i).disconnect();
      }
      this.modelConnections_.clear();
    }
    super.setSourceModel(newSourceModel);
    if (newSourceModel != null) {
      this.modelConnections_.add(
          newSourceModel
              .rowsAboutToBeInserted()
              .addListener(
                  this,
                  (WModelIndex e1, Integer e2, Integer e3) -> {
                    WIdentityProxyModel.this.sourceRowsAboutToBeInserted(e1, e2, e3);
                  }));
      this.modelConnections_.add(
          newSourceModel
              .rowsInserted()
              .addListener(
                  this,
                  (WModelIndex e1, Integer e2, Integer e3) -> {
                    WIdentityProxyModel.this.sourceRowsInserted(e1, e2, e3);
                  }));
      this.modelConnections_.add(
          newSourceModel
              .rowsAboutToBeRemoved()
              .addListener(
                  this,
                  (WModelIndex e1, Integer e2, Integer e3) -> {
                    WIdentityProxyModel.this.sourceRowsAboutToBeRemoved(e1, e2, e3);
                  }));
      this.modelConnections_.add(
          newSourceModel
              .rowsRemoved()
              .addListener(
                  this,
                  (WModelIndex e1, Integer e2, Integer e3) -> {
                    WIdentityProxyModel.this.sourceRowsRemoved(e1, e2, e3);
                  }));
      this.modelConnections_.add(
          newSourceModel
              .columnsAboutToBeInserted()
              .addListener(
                  this,
                  (WModelIndex e1, Integer e2, Integer e3) -> {
                    WIdentityProxyModel.this.sourceColumnsAboutToBeInserted(e1, e2, e3);
                  }));
      this.modelConnections_.add(
          newSourceModel
              .columnsInserted()
              .addListener(
                  this,
                  (WModelIndex e1, Integer e2, Integer e3) -> {
                    WIdentityProxyModel.this.sourceColumnsInserted(e1, e2, e3);
                  }));
      this.modelConnections_.add(
          newSourceModel
              .columnsAboutToBeRemoved()
              .addListener(
                  this,
                  (WModelIndex e1, Integer e2, Integer e3) -> {
                    WIdentityProxyModel.this.sourceColumnsAboutToBeRemoved(e1, e2, e3);
                  }));
      this.modelConnections_.add(
          newSourceModel
              .columnsRemoved()
              .addListener(
                  this,
                  (WModelIndex e1, Integer e2, Integer e3) -> {
                    WIdentityProxyModel.this.sourceColumnsRemoved(e1, e2, e3);
                  }));
      this.modelConnections_.add(
          newSourceModel
              .modelReset()
              .addListener(
                  this,
                  () -> {
                    WIdentityProxyModel.this.sourceModelReset();
                  }));
      this.modelConnections_.add(
          newSourceModel
              .dataChanged()
              .addListener(
                  this,
                  (WModelIndex e1, WModelIndex e2) -> {
                    WIdentityProxyModel.this.sourceDataChanged(e1, e2);
                  }));
      this.modelConnections_.add(
          newSourceModel
              .headerDataChanged()
              .addListener(
                  this,
                  (Orientation e1, Integer e2, Integer e3) -> {
                    WIdentityProxyModel.this.sourceHeaderDataChanged(e1, e2, e3);
                  }));
      this.modelConnections_.add(
          newSourceModel
              .layoutAboutToBeChanged()
              .addListener(
                  this,
                  () -> {
                    WIdentityProxyModel.this.sourceLayoutAboutToBeChanged();
                  }));
      this.modelConnections_.add(
          newSourceModel
              .layoutChanged()
              .addListener(
                  this,
                  () -> {
                    WIdentityProxyModel.this.sourceLayoutChanged();
                  }));
    }
  }
  /**
   * Inserts one or more columns.
   *
   * <p>Inserts <code>count</code> columns at column <code>column</code> in the source model.
   *
   * <p>Forwards the result indicating success from the source model.
   */
  public boolean insertColumns(int column, int count, final WModelIndex parent) {
    return this.getSourceModel().insertColumns(column, count, this.mapToSource(parent));
  }
  /**
   * Inserts one or more rows.
   *
   * <p>Inserts <code>count</code> rows at row <code>row</code> in the source model.
   *
   * <p>Forwards the result indicating success from the source model.
   */
  public boolean insertRows(int row, int count, final WModelIndex parent) {
    return this.getSourceModel().insertRows(row, count, this.mapToSource(parent));
  }
  /**
   * Removes columns.
   *
   * <p>Removes <code>count</code> columns at column <code>column</code> in the source model.
   *
   * <p>Forwards the result indicating success from the source model.
   */
  public boolean removeColumns(int column, int count, final WModelIndex parent) {
    return this.getSourceModel().removeColumns(column, count, this.mapToSource(parent));
  }
  /**
   * Removes rows.
   *
   * <p>Removes <code>count</code> rows at row <code>row</code> in the source model.
   *
   * <p>Forwards the result indicating success from the source model.
   */
  public boolean removeRows(int row, int count, final WModelIndex parent) {
    return this.getSourceModel().removeRows(row, count, this.mapToSource(parent));
  }
  /**
   * Set header data for a column or row.
   *
   * <p>Sets the header data for a column or row in the source model.
   *
   * <p>Forwards the result indicating success from the source model.
   */
  public boolean setHeaderData(
      int section, Orientation orientation, final Object value, ItemDataRole role) {
    return this.getSourceModel().setHeaderData(section, orientation, value, role);
  }

  private List<AbstractSignal.Connection> modelConnections_;

  private void sourceColumnsAboutToBeInserted(final WModelIndex parent, int start, int end) {
    this.beginInsertColumns(this.mapFromSource(parent), start, end);
  }

  private void sourceColumnsAboutToBeRemoved(final WModelIndex parent, int start, int end) {
    this.beginRemoveColumns(this.mapFromSource(parent), start, end);
  }

  private void sourceColumnsInserted(final WModelIndex parent, int start, int end) {
    this.endInsertColumns();
  }

  private void sourceColumnsRemoved(final WModelIndex parent, int start, int end) {
    this.endRemoveColumns();
  }

  private void sourceRowsAboutToBeInserted(final WModelIndex parent, int start, int end) {
    this.beginInsertRows(this.mapFromSource(parent), start, end);
  }

  private void sourceRowsAboutToBeRemoved(final WModelIndex parent, int start, int end) {
    this.beginRemoveRows(this.mapFromSource(parent), start, end);
  }

  private void sourceRowsInserted(final WModelIndex parent, int start, int end) {
    this.endInsertRows();
  }

  private void sourceRowsRemoved(final WModelIndex parent, int start, int end) {
    this.endRemoveRows();
  }

  private void sourceDataChanged(final WModelIndex topLeft, final WModelIndex bottomRight) {
    this.dataChanged().trigger(this.mapFromSource(topLeft), this.mapFromSource(bottomRight));
  }

  private void sourceHeaderDataChanged(Orientation orientation, int start, int end) {
    this.headerDataChanged().trigger(orientation, start, end);
  }

  private void sourceLayoutAboutToBeChanged() {
    this.layoutAboutToBeChanged().trigger();
  }

  private void sourceLayoutChanged() {
    this.layoutChanged().trigger();
  }

  private void sourceModelReset() {
    this.modelReset().trigger();
  }
}
