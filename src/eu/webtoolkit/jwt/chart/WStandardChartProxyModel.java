/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link WAbstractChartModel} implementation that wraps a {@link WAbstractItemModel}.
 *
 * <p>This model delegates all functions to an underlying {@link WAbstractItemModel}, using the
 * appropriate roles.
 *
 * <p>This model also triggers the {@link WAbstractChartModel#changed()} signal whenever the
 * underlying {@link WAbstractItemModel} is changed.
 */
public class WStandardChartProxyModel extends WAbstractChartModel {
  private static Logger logger = LoggerFactory.getLogger(WStandardChartProxyModel.class);

  /** Creates a new {@link WStandardChartProxyModel} that wraps the given source model. */
  public WStandardChartProxyModel(WAbstractItemModel sourceModel, WObject parent) {
    super(parent);
    this.sourceModel_ = sourceModel;
    sourceModel
        .columnsInserted()
        .addListener(
            this,
            new Signal3.Listener<WModelIndex, Integer, Integer>() {
              public void trigger(WModelIndex e1, Integer e2, Integer e3) {
                WStandardChartProxyModel.this.sourceModelModified();
              }
            });
    sourceModel
        .columnsRemoved()
        .addListener(
            this,
            new Signal3.Listener<WModelIndex, Integer, Integer>() {
              public void trigger(WModelIndex e1, Integer e2, Integer e3) {
                WStandardChartProxyModel.this.sourceModelModified();
              }
            });
    sourceModel
        .rowsInserted()
        .addListener(
            this,
            new Signal3.Listener<WModelIndex, Integer, Integer>() {
              public void trigger(WModelIndex e1, Integer e2, Integer e3) {
                WStandardChartProxyModel.this.sourceModelModified();
              }
            });
    sourceModel
        .rowsRemoved()
        .addListener(
            this,
            new Signal3.Listener<WModelIndex, Integer, Integer>() {
              public void trigger(WModelIndex e1, Integer e2, Integer e3) {
                WStandardChartProxyModel.this.sourceModelModified();
              }
            });
    sourceModel
        .dataChanged()
        .addListener(
            this,
            new Signal2.Listener<WModelIndex, WModelIndex>() {
              public void trigger(WModelIndex e1, WModelIndex e2) {
                WStandardChartProxyModel.this.sourceModelModified();
              }
            });
    sourceModel
        .headerDataChanged()
        .addListener(
            this,
            new Signal3.Listener<Orientation, Integer, Integer>() {
              public void trigger(Orientation e1, Integer e2, Integer e3) {
                WStandardChartProxyModel.this.sourceModelModified();
              }
            });
    sourceModel
        .layoutChanged()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                WStandardChartProxyModel.this.sourceModelModified();
              }
            });
    sourceModel
        .modelReset()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                WStandardChartProxyModel.this.sourceModelModified();
              }
            });
  }
  /**
   * Creates a new {@link WStandardChartProxyModel} that wraps the given source model.
   *
   * <p>Calls {@link #WStandardChartProxyModel(WAbstractItemModel sourceModel, WObject parent)
   * this(sourceModel, (WObject)null)}
   */
  public WStandardChartProxyModel(WAbstractItemModel sourceModel) {
    this(sourceModel, (WObject) null);
  }
  /**
   * Returns data at a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, int role)
   * WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * DisplayRole} as a double.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, int role)
   */
  public double getData(int row, int column) {
    return StringUtils.asNumber(this.sourceModel_.getData(row, column, ItemDataRole.DisplayRole));
  }
  /**
   * Returns display data at a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, int role)
   * WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * DisplayRole} as a {@link WString}.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, int role)
   */
  public WString getDisplayData(int row, int column) {
    return StringUtils.asString(this.sourceModel_.getData(row, column, ItemDataRole.DisplayRole));
  }
  /**
   * Returns the given column&apos;s header data.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getHeaderData(int section, Orientation
   * orientation, int role) WAbstractItemModel#getHeaderData()} for the given column with the {@link
   * ItemDataRole DisplayRole} as a {@link WString}.
   *
   * <p>
   *
   * @see WAbstractItemModel#getHeaderData(int section, Orientation orientation, int role)
   */
  public WString getHeaderData(int column) {
    return StringUtils.asString(
        this.sourceModel_.getHeaderData(column, Orientation.Horizontal, ItemDataRole.DisplayRole));
  }
  /**
   * Returns the tooltip text to use on a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, int role)
   * WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * ToolTipRole} as a {@link WString}.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, int role)
   */
  public WString getToolTip(int row, int column) {
    return StringUtils.asString(this.sourceModel_.getData(row, column, ItemDataRole.ToolTipRole));
  }
  /**
   * Returns the item flags for the given row and column.
   *
   * <p>Returns the result of WAbstractItemModel::index(row, column).{@link
   * WStandardChartProxyModel#flags(int row, int column) flags()} for the given row and column.
   *
   * <p>
   *
   * @see WModelIndex#getFlags()
   */
  public EnumSet<ItemFlag> flags(int row, int column) {
    return this.sourceModel_.getIndex(row, column).getFlags();
  }
  /**
   * Returns the link to use on a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, int role)
   * WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * LinkRole} as a {@link WLink}.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, int role)
   */
  public WLink link(int row, int column) {
    Object result = this.sourceModel_.getData(row, column, ItemDataRole.LinkRole);
    if ((result == null)) {
      return null;
    } else {
      WLink c = ((WLink) result);
      return c;
    }
  }
  /**
   * Returns the marker pen color to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, int role)
   * WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * MarkerPenColorRole}, or null if no color is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, int role)
   */
  public WColor getMarkerPenColor(int row, int column) {
    return this.color(row, column, ItemDataRole.MarkerPenColorRole);
  }
  /**
   * Returns the marker brush color to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, int role)
   * WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * MarkerBrushColorRole}, or null if no color is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, int role)
   */
  public WColor getMarkerBrushColor(int row, int column) {
    return this.color(row, column, ItemDataRole.MarkerBrushColorRole);
  }
  /**
   * Returns the marker type to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, int role)
   * WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * MarkerTypeRole}, or null if no marker type is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, int role)
   */
  public MarkerType markerType(int row, int column) {
    Object result = this.sourceModel_.getData(row, column, ItemDataRole.MarkerTypeRole);
    if ((result == null)) {
      return null;
    } else {
      MarkerType t = ((MarkerType) result);
      return t;
    }
  }
  /**
   * Returns the bar pen color to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, int role)
   * WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * BarPenColorRole}, or null if no color is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, int role)
   */
  public WColor getBarPenColor(int row, int column) {
    return this.color(row, column, ItemDataRole.BarPenColorRole);
  }
  /**
   * Returns the bar brush color to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, int role)
   * WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * BarBrushColorRole}, or null if no color is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, int role)
   */
  public WColor getBarBrushColor(int row, int column) {
    return this.color(row, column, ItemDataRole.BarBrushColorRole);
  }
  /**
   * Returns the marker scale factor to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, int role)
   * WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * MarkerScaleFactorRole}, or null if no color is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, int role)
   */
  public Double getMarkerScaleFactor(int row, int column) {
    Object result = this.sourceModel_.getData(row, column, ItemDataRole.MarkerScaleFactorRole);
    if ((result == null)) {
      return super.getMarkerScaleFactor(row, column);
    } else {
      double tmp = StringUtils.asNumber(result);
      return tmp;
    }
  }
  /**
   * Returns the number of columns.
   *
   * <p>
   *
   * @see WAbstractItemModel#getColumnCount(WModelIndex parent)
   */
  public int getColumnCount() {
    return this.sourceModel_.getColumnCount();
  }
  /**
   * Returns the number of rows.
   *
   * <p>
   *
   * @see WAbstractItemModel#getRowCount(WModelIndex parent)
   */
  public int getRowCount() {
    return this.sourceModel_.getRowCount();
  }
  /** Returns the wrapped source model. */
  public WAbstractItemModel getSourceModel() {
    return this.sourceModel_;
  }

  private WAbstractItemModel sourceModel_;

  private void sourceModelModified() {
    this.changed().trigger();
  }

  private WColor color(int row, int column, int colorDataRole) {
    Object result = this.sourceModel_.getData(row, column, colorDataRole);
    if ((result == null)) {
      return null;
    } else {
      WColor c = ((WColor) result);
      return c;
    }
  }
}
