/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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
  public WStandardChartProxyModel(final WAbstractItemModel sourceModel) {
    super();
    this.sourceModel_ = sourceModel;
    sourceModel
        .columnsInserted()
        .addListener(
            this,
            (WModelIndex e1, Integer e2, Integer e3) -> {
              WStandardChartProxyModel.this.sourceModelModified();
            });
    sourceModel
        .columnsRemoved()
        .addListener(
            this,
            (WModelIndex e1, Integer e2, Integer e3) -> {
              WStandardChartProxyModel.this.sourceModelModified();
            });
    sourceModel
        .rowsInserted()
        .addListener(
            this,
            (WModelIndex e1, Integer e2, Integer e3) -> {
              WStandardChartProxyModel.this.sourceModelModified();
            });
    sourceModel
        .rowsRemoved()
        .addListener(
            this,
            (WModelIndex e1, Integer e2, Integer e3) -> {
              WStandardChartProxyModel.this.sourceModelModified();
            });
    sourceModel
        .dataChanged()
        .addListener(
            this,
            (WModelIndex e1, WModelIndex e2) -> {
              WStandardChartProxyModel.this.sourceModelModified();
            });
    sourceModel
        .headerDataChanged()
        .addListener(
            this,
            (Orientation e1, Integer e2, Integer e3) -> {
              WStandardChartProxyModel.this.sourceModelModified();
            });
    sourceModel
        .layoutChanged()
        .addListener(
            this,
            () -> {
              WStandardChartProxyModel.this.sourceModelModified();
            });
    sourceModel
        .modelReset()
        .addListener(
            this,
            () -> {
              WStandardChartProxyModel.this.sourceModelModified();
            });
  }
  /**
   * Returns data at a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
   * role) WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * ItemDataRole::Display} as a double.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public double getData(int row, int column) {
    return StringUtils.asNumber(this.sourceModel_.getData(row, column, ItemDataRole.Display));
  }
  /**
   * Returns display data at a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
   * role) WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * ItemDataRole::Display} as a {@link WString}.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public WString getDisplayData(int row, int column) {
    return StringUtils.asString(this.sourceModel_.getData(row, column, ItemDataRole.Display));
  }
  /**
   * Returns the given column&apos;s header data.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getHeaderData(int section, Orientation
   * orientation, ItemDataRole role) WAbstractItemModel#getHeaderData()} for the given column with
   * the {@link ItemDataRole ItemDataRole::Display} as a {@link WString}.
   *
   * <p>
   *
   * @see WAbstractItemModel#getHeaderData(int section, Orientation orientation, ItemDataRole role)
   */
  public WString getHeaderData(int column) {
    return StringUtils.asString(
        this.sourceModel_.getHeaderData(column, Orientation.Horizontal, ItemDataRole.Display));
  }
  /**
   * Returns the tooltip text to use on a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
   * role) WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * ItemDataRole::ToolTip} as a {@link WString}.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public WString getToolTip(int row, int column) {
    return StringUtils.asString(this.sourceModel_.getData(row, column, ItemDataRole.ToolTip));
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
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
   * role) WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * LinkRole} as a {@link WLink}.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public WLink link(int row, int column) {
    Object result = this.sourceModel_.getData(row, column, ItemDataRole.Link);
    if (!(result != null)) {
      return null;
    } else {
      WLink c = ((WLink) result);
      return c;
    }
  }
  /**
   * Returns the marker pen color to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
   * role) WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * ItemDataRole::MarkerPenColor}, or null if no color is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public WColor getMarkerPenColor(int row, int column) {
    return this.color(row, column, ItemDataRole.MarkerPenColor);
  }
  /**
   * Returns the marker brush color to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
   * role) WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * ItemDataRole::MarkerBrushColor}, or null if no color is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public WColor getMarkerBrushColor(int row, int column) {
    return this.color(row, column, ItemDataRole.MarkerBrushColor);
  }
  /**
   * Returns the marker type to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
   * role) WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * MarkerTypeRole}, or null if no marker type is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public MarkerType markerType(int row, int column) {
    Object result = this.sourceModel_.getData(row, column, ItemDataRole.MarkerType);
    if (!(result != null)) {
      return null;
    } else {
      MarkerType t = ((MarkerType) result);
      return t;
    }
  }
  /**
   * Returns the bar pen color to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
   * role) WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * ItemDataRole::BarPenColor}, or null if no color is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public WColor getBarPenColor(int row, int column) {
    return this.color(row, column, ItemDataRole.BarPenColor);
  }
  /**
   * Returns the bar brush color to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
   * role) WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * ItemDataRole::BarBrushColor}, or null if no color is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public WColor getBarBrushColor(int row, int column) {
    return this.color(row, column, ItemDataRole.BarBrushColor);
  }
  /**
   * Returns the marker scale factor to use for a given row and column.
   *
   * <p>Returns the result of {@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole
   * role) WAbstractItemModel#getData()} for the given row and column with the {@link ItemDataRole
   * ItemDataRole::MarkerScaleFactor}, or null if no color is defined.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public Double getMarkerScaleFactor(int row, int column) {
    Object result = this.sourceModel_.getData(row, column, ItemDataRole.MarkerScaleFactor);
    if (!(result != null)) {
      return super.getMarkerScaleFactor(row, column);
    } else {
      double scale = StringUtils.asNumber(result);
      return scale;
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

  private WColor color(int row, int column, ItemDataRole colorDataRole) {
    Object result = this.sourceModel_.getData(row, column, colorDataRole);
    if (!(result != null)) {
      return null;
    } else {
      WColor c = ((WColor) result);
      return c;
    }
  }
}
