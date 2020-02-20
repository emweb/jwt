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
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Enumeration that indicates a role for a data item.
 *
 * <p>A single data item can have data associated with it corresponding to different roles. Each
 * role may be used by the corresponding view class in a different way.
 *
 * <p>
 *
 * @see WModelIndex#getData(int role)
 */
public class ItemDataRole {
  /** Role for textual representation. */
  public static final int DisplayRole = 0;
  /** Role for the url of an icon. */
  public static final int DecorationRole = 1;
  /** Role for the edited value. */
  public static final int EditRole = 2;
  /** Role for the style class. */
  public static final int StyleClassRole = 3;
  /**
   * Role that indicates the check state.
   *
   * <p>Data for this role should be a <code>bool</code>. When the {@link ItemFlag#ItemIsTristate}
   * flag is set for the item, data for this role should be of type {@link CheckState}.
   */
  public static final int CheckStateRole = 4;
  /** Role for a (plain) tooltip. */
  public static final int ToolTipRole = 5;
  /** Role for a link. */
  public static final int LinkRole = 6;
  /** Role for mime type information. */
  public static final int MimeTypeRole = 7;
  /** Level in aggregation, for header data. */
  public static final int LevelRole = 8;
  /** Marker pen color (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) */
  public static final int MarkerPenColorRole = 16;
  /** Marker brush color (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) */
  public static final int MarkerBrushColorRole = 17;
  /** Marker size (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) */
  public static final int MarkerScaleFactorRole = 20;
  /** Marker type (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) */
  public static final int MarkerTypeRole = 21;
  /** Bar pen color (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) */
  public static final int BarPenColorRole = 18;
  /** Bar brush color (for {@link eu.webtoolkit.jwt.chart.WCartesianChart}) */
  public static final int BarBrushColorRole = 19;
  /** First role reserved for user purposes. */
  public static final int UserRole = 32;
}
