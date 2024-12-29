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
 * Class representing grid-based data for a 3D chart.
 *
 * <p>General information can be found at {@link WAbstractDataSeries3D}. The model for this
 * dataseries is structured as a table. One of the columns (by default index 0) contains the x-axis
 * values, one of the rows (by default index 0) contains the y-axis values. All other values in the
 * table contain the z-value corresponding to the x- and y-values with the same column- and
 * row-index.
 */
public class WGridData extends WAbstractGridData {
  private static Logger logger = LoggerFactory.getLogger(WGridData.class);

  /** Constructor. */
  public WGridData(WAbstractItemModel model) {
    super(model);
    this.XAbscisColumn_ = 0;
    this.YAbscisRow_ = 0;
  }

  public double minimum(Axis axis) {
    if (axis == Axis.X3D) {
      if (this.YAbscisRow_ != 0) {
        return StringUtils.asNumber(this.model_.getData(0, this.XAbscisColumn_));
      } else {
        return StringUtils.asNumber(this.model_.getData(1, this.XAbscisColumn_));
      }
    } else {
      if (axis == Axis.Y3D) {
        if (this.XAbscisColumn_ != 0) {
          return StringUtils.asNumber(this.model_.getData(this.YAbscisRow_, 0));
        } else {
          return StringUtils.asNumber(this.model_.getData(this.YAbscisRow_, 1));
        }
      } else {
        if (axis == Axis.Z3D) {
          if (!this.rangeCached_) {
            this.findRange();
          }
          return this.zMin_;
        } else {
          throw new WException("WAbstractGridData.C: unknown Axis-type");
        }
      }
    }
  }

  public double maximum(Axis axis) {
    if (axis == Axis.X3D) {
      if (this.seriesType_ == Series3DType.Bar) {
        return this.model_.getRowCount() - 1 - 0.5;
      }
      if (this.YAbscisRow_ != this.model_.getRowCount()) {
        return StringUtils.asNumber(
            this.model_.getData(this.model_.getRowCount() - 1, this.XAbscisColumn_));
      } else {
        return StringUtils.asNumber(
            this.model_.getData(this.model_.getRowCount() - 2, this.XAbscisColumn_));
      }
    } else {
      if (axis == Axis.Y3D) {
        if (this.seriesType_ == Series3DType.Bar) {
          return this.model_.getColumnCount() - 1 - 0.5;
        }
        if (this.XAbscisColumn_ != this.model_.getColumnCount()) {
          return StringUtils.asNumber(
              this.model_.getData(this.YAbscisRow_, this.model_.getColumnCount() - 1));
        } else {
          return StringUtils.asNumber(
              this.model_.getData(this.YAbscisRow_, this.model_.getColumnCount() - 2));
        }
      } else {
        if (axis == Axis.Z3D) {
          if (!this.rangeCached_) {
            this.findRange();
          }
          return this.zMax_;
        } else {
          throw new WException("WAbstractGridData.C: unknown Axis-type");
        }
      }
    }
  }
  /**
   * Set which column in the model is used as x-axis.
   *
   * <p>The default column that is used has index 0.
   */
  public void setXSeriesColumn(int modelColumn) {
    this.XAbscisColumn_ = modelColumn;
    this.rangeCached_ = false;
  }
  /**
   * Returns which column in the model is used as x-axis.
   *
   * <p>
   *
   * @see WGridData#setXSeriesColumn(int modelColumn)
   */
  public int XSeriesColumn() {
    return this.XAbscisColumn_;
  }
  /**
   * Set which row in the model is used as y-axis.
   *
   * <p>The default row that is used has index 0.
   */
  public void setYSeriesRow(int modelRow) {
    this.YAbscisRow_ = modelRow;
    this.rangeCached_ = false;
  }
  /**
   * Returns which row in the model is used as y-axis.
   *
   * <p>
   *
   * @see WGridData#setYSeriesRow(int modelRow)
   */
  public int YSeriesRow() {
    return this.YAbscisRow_;
  }

  public int getNbXPoints() {
    return this.model_.getRowCount() - 1;
  }

  public int getNbYPoints() {
    return this.model_.getColumnCount() - 1;
  }

  public WString axisLabel(int u, Axis axis) {
    if (axis == Axis.X3D) {
      if (u >= this.YAbscisRow_) {
        u++;
      }
      return StringUtils.asString(this.model_.getData(u, this.XAbscisColumn_));
    } else {
      if (axis == Axis.Y3D) {
        if (u >= this.XAbscisColumn_) {
          u++;
        }
        return StringUtils.asString(this.model_.getData(this.YAbscisRow_, u));
      } else {
        throw new WException("WGridData: don't know this type of axis");
      }
    }
  }

  Object data(int i, int j) {
    if (i >= this.XAbscisColumn_) {
      i++;
    }
    if (j >= this.YAbscisRow_) {
      j++;
    }
    return this.model_.getData(i, j);
  }

  int getCountSimpleData() {
    int result = 0;
    int nbModelRows = this.model_.getRowCount();
    int nbModelCols = this.model_.getColumnCount();
    for (int i = 0; i < nbModelRows; i++) {
      if (i == this.YAbscisRow_) {
        continue;
      }
      for (int j = 0; j < nbModelCols; j++) {
        if (j == this.XAbscisColumn_) {
          continue;
        }
        if (!(this.model_.getData(i, j, ItemDataRole.MarkerBrushColor) != null)) {
          result++;
        }
      }
    }
    return result;
  }

  void pointDataFromModel(
      final java.nio.ByteBuffer simplePtsArray,
      final java.nio.ByteBuffer simplePtsSize,
      final java.nio.ByteBuffer coloredPtsArray,
      final java.nio.ByteBuffer coloredPtsSize,
      final java.nio.ByteBuffer coloredPtsColor) {
    int nbModelRows = this.model_.getRowCount();
    int nbModelCols = this.model_.getColumnCount();
    java.nio.ByteBuffer scaledXAxis = WebGLUtils.newByteBuffer(4 * (nbModelRows - 1));
    java.nio.ByteBuffer scaledYAxis = WebGLUtils.newByteBuffer(4 * (nbModelCols - 1));
    double xMin = this.chart_.axis(Axis.X3D).getMinimum();
    double xMax = this.chart_.axis(Axis.X3D).getMaximum();
    double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
    double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
    double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
    double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
    for (int i = 0; i < nbModelRows; i++) {
      if (i == this.YAbscisRow_) {
        continue;
      }
      scaledXAxis.putFloat(
          (float)
              ((StringUtils.asNumber(this.model_.getData(i, this.XAbscisColumn_)) - xMin)
                  / (xMax - xMin)));
    }
    for (int j = 0; j < nbModelCols; j++) {
      if (j == this.XAbscisColumn_) {
        continue;
      }
      scaledYAxis.putFloat(
          (float)
              ((StringUtils.asNumber(this.model_.getData(this.YAbscisRow_, j)) - yMin)
                  / (yMax - yMin)));
    }
    int rowOffset = 0;
    int colOffset = 0;
    for (int i = 0; i < nbModelRows; i++) {
      if (i == this.YAbscisRow_) {
        rowOffset = 1;
        continue;
      }
      colOffset = 0;
      for (int j = 0; j < nbModelCols; j++) {
        if (j == this.XAbscisColumn_) {
          colOffset = 1;
          continue;
        }
        if (!(this.model_.getData(i, j, ItemDataRole.MarkerBrushColor) != null)) {
          simplePtsArray.putFloat(scaledXAxis.getFloat(4 * (i - rowOffset)));
          simplePtsArray.putFloat(scaledYAxis.getFloat(4 * (j - colOffset)));
          simplePtsArray.putFloat(
              (float)
                  ((StringUtils.asNumber(this.model_.getData(i, j, ItemDataRole.Display)) - zMin)
                      / (zMax - zMin)));
          if ((this.model_.getData(i, j, ItemDataRole.MarkerScaleFactor) != null)) {
            simplePtsSize.putFloat(
                (float)
                    StringUtils.asNumber(
                        this.model_.getData(i, j, ItemDataRole.MarkerScaleFactor)));
          } else {
            simplePtsSize.putFloat((float) this.getPointSize());
          }
        } else {
          coloredPtsArray.putFloat(scaledXAxis.getFloat(4 * (i - rowOffset)));
          coloredPtsArray.putFloat(scaledYAxis.getFloat(4 * (j - colOffset)));
          coloredPtsArray.putFloat(
              (float) ((StringUtils.asNumber(this.model_.getData(i, j)) - zMin) / (zMax - zMin)));
          WColor color = ((WColor) this.model_.getData(i, j, ItemDataRole.MarkerBrushColor));
          coloredPtsColor.putFloat((float) color.getRed());
          coloredPtsColor.putFloat((float) color.getGreen());
          coloredPtsColor.putFloat((float) color.getBlue());
          coloredPtsColor.putFloat((float) color.getAlpha());
          if ((this.model_.getData(i, j, ItemDataRole.MarkerScaleFactor) != null)) {
            coloredPtsSize.putFloat(
                (float)
                    StringUtils.asNumber(
                        this.model_.getData(i, j, ItemDataRole.MarkerScaleFactor)));
          } else {
            coloredPtsSize.putFloat((float) this.getPointSize());
          }
        }
      }
    }
  }

  void surfaceDataFromModel(final List<java.nio.ByteBuffer> simplePtsArrays) {
    int nbModelRows = this.model_.getRowCount();
    int nbModelCols = this.model_.getColumnCount();
    java.nio.ByteBuffer scaledXAxis = WebGLUtils.newByteBuffer(4 * (nbModelRows - 1));
    java.nio.ByteBuffer scaledYAxis = WebGLUtils.newByteBuffer(4 * (nbModelCols - 1));
    double xMin = this.chart_.axis(Axis.X3D).getMinimum();
    double xMax = this.chart_.axis(Axis.X3D).getMaximum();
    double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
    double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
    double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
    double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
    for (int i = 0; i < nbModelRows; i++) {
      if (i == this.YAbscisRow_) {
        continue;
      }
      scaledXAxis.putFloat(
          (float)
              ((StringUtils.asNumber(this.model_.getData(i, this.XAbscisColumn_)) - xMin)
                  / (xMax - xMin)));
    }
    for (int j = 0; j < nbModelCols; j++) {
      if (j == this.XAbscisColumn_) {
        continue;
      }
      scaledYAxis.putFloat(
          (float)
              ((StringUtils.asNumber(this.model_.getData(this.YAbscisRow_, j)) - yMin)
                  / (yMax - yMin)));
    }
    int nbXaxisBuffers = this.getNbXPoints() / (SURFACE_SIDE_LIMIT - 1);
    int nbYaxisBuffers = this.getNbYPoints() / (SURFACE_SIDE_LIMIT - 1);
    if (this.getNbXPoints() % (SURFACE_SIDE_LIMIT - 1) != 0) {
      nbXaxisBuffers++;
    }
    if (this.getNbYPoints() % (SURFACE_SIDE_LIMIT - 1) != 0) {
      nbYaxisBuffers++;
    }
    int SURFACE_SIDE_LIMIT = WGridData.SURFACE_SIDE_LIMIT - 1;
    int rowOffset = 0;
    int colOffset = 0;
    int bufferIndex = 0;
    for (int k = 0; k < nbXaxisBuffers - 1; k++) {
      for (int l = 0; l < nbYaxisBuffers - 1; l++) {
        bufferIndex = k * nbYaxisBuffers + l;
        int cnt1 = 0;
        int i = k * SURFACE_SIDE_LIMIT;
        rowOffset = 0;
        for (; i < (k + 1) * SURFACE_SIDE_LIMIT + 1; i++) {
          if (i >= this.YAbscisRow_) {
            rowOffset = 1;
          }
          int cnt2 = 0;
          int j = l * SURFACE_SIDE_LIMIT;
          colOffset = 0;
          for (; j < (l + 1) * SURFACE_SIDE_LIMIT + 1; j++) {
            if (j >= this.XAbscisColumn_) {
              colOffset = 1;
            }
            simplePtsArrays.get(bufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
            simplePtsArrays.get(bufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
            simplePtsArrays
                .get(bufferIndex)
                .putFloat(
                    (float)
                        ((StringUtils.asNumber(this.model_.getData(i + rowOffset, j + colOffset))
                                - zMin)
                            / (zMax - zMin)));
            cnt2++;
          }
          cnt1++;
        }
      }
      bufferIndex = k * nbYaxisBuffers + nbYaxisBuffers - 1;
      int cnt1 = 0;
      int i = k * SURFACE_SIDE_LIMIT;
      rowOffset = 0;
      for (; i < (k + 1) * SURFACE_SIDE_LIMIT + 1; i++) {
        if (i >= this.YAbscisRow_) {
          rowOffset = 1;
        }
        int j = (nbYaxisBuffers - 1) * SURFACE_SIDE_LIMIT;
        colOffset = 0;
        for (; j < nbModelCols - 1; j++) {
          if (j >= this.XAbscisColumn_) {
            colOffset = 1;
          }
          simplePtsArrays.get(bufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
          simplePtsArrays.get(bufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
          simplePtsArrays
              .get(bufferIndex)
              .putFloat(
                  (float)
                      ((StringUtils.asNumber(this.model_.getData(i + rowOffset, j + colOffset))
                              - zMin)
                          / (zMax - zMin)));
        }
        cnt1++;
      }
    }
    for (int l = 0; l < nbYaxisBuffers - 1; l++) {
      bufferIndex = (nbXaxisBuffers - 1) * nbYaxisBuffers + l;
      int i = (nbXaxisBuffers - 1) * SURFACE_SIDE_LIMIT;
      rowOffset = 0;
      for (; i < nbModelRows - 1; i++) {
        if (i >= this.YAbscisRow_) {
          rowOffset = 1;
        }
        int cnt2 = 0;
        int j = l * SURFACE_SIDE_LIMIT;
        colOffset = 0;
        for (; j < (l + 1) * SURFACE_SIDE_LIMIT + 1; j++) {
          if (j >= this.XAbscisColumn_) {
            colOffset = 1;
          }
          simplePtsArrays.get(bufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
          simplePtsArrays.get(bufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
          simplePtsArrays
              .get(bufferIndex)
              .putFloat(
                  (float)
                      ((StringUtils.asNumber(this.model_.getData(i + rowOffset, j + colOffset))
                              - zMin)
                          / (zMax - zMin)));
          cnt2++;
        }
      }
    }
    bufferIndex = (nbXaxisBuffers - 1) * nbYaxisBuffers + (nbYaxisBuffers - 1);
    int i = (nbXaxisBuffers - 1) * SURFACE_SIDE_LIMIT;
    rowOffset = 0;
    for (; i < nbModelRows - 1; i++) {
      if (i >= this.YAbscisRow_) {
        rowOffset = 1;
      }
      int j = (nbYaxisBuffers - 1) * SURFACE_SIDE_LIMIT;
      colOffset = 0;
      for (; j < nbModelCols - 1; j++) {
        if (j >= this.XAbscisColumn_) {
          colOffset = 1;
        }
        simplePtsArrays.get(bufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
        simplePtsArrays.get(bufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
        simplePtsArrays
            .get(bufferIndex)
            .putFloat(
                (float)
                    ((StringUtils.asNumber(this.model_.getData(i + rowOffset, j + colOffset))
                            - zMin)
                        / (zMax - zMin)));
      }
    }
  }

  protected void barDataFromModel(final List<java.nio.ByteBuffer> simplePtsArrays) {
    final List<WAbstractDataSeries3D> dataseries = this.chart_.getDataSeries();
    List<WAbstractGridData> prevDataseries = new ArrayList<WAbstractGridData>();
    boolean first = true;
    int xDim = 0;
    int yDim = 0;
    for (int i = 0; i < dataseries.size(); i++) {
      if (ObjectUtils.cast(dataseries.get(i), WAbstractGridData.class) != null) {
        WAbstractGridData griddata = ObjectUtils.cast(dataseries.get(i), WAbstractGridData.class);
        if (griddata == this || griddata.getType() != Series3DType.Bar) {
          break;
        }
        if (first) {
          xDim = griddata.getNbXPoints();
          yDim = griddata.getNbYPoints();
          first = false;
        }
        if (griddata.getNbXPoints() != xDim
            || griddata.getNbYPoints() != yDim
            || griddata.isHidden()) {
          continue;
        }
        prevDataseries.add(griddata);
      }
    }
    if (!prevDataseries.isEmpty() && (xDim != this.getNbXPoints() || yDim != this.getNbYPoints())) {
      throw new WException("WGridData.C: Dimensions of multiple bar-series data do not match");
    }
    int nbModelRows = this.model_.getRowCount();
    int nbModelCols = this.model_.getColumnCount();
    java.nio.ByteBuffer scaledXAxis = WebGLUtils.newByteBuffer(4 * (nbModelRows - 1));
    java.nio.ByteBuffer scaledYAxis = WebGLUtils.newByteBuffer(4 * (nbModelCols - 1));
    double xMin = this.chart_.axis(Axis.X3D).getMinimum();
    double xMax = this.chart_.axis(Axis.X3D).getMaximum();
    double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
    double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
    double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
    double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
    for (int i = 0; i < nbModelRows - 1; i++) {
      scaledXAxis.putFloat((float) ((xMin + 0.5 + i - xMin) / (xMax - xMin)));
    }
    for (int j = 0; j < nbModelCols - 1; j++) {
      scaledYAxis.putFloat((float) ((yMin + 0.5 + j - yMin) / (yMax - yMin)));
    }
    int rowOffset = 0;
    int colOffset = 0;
    int simpleBufferIndex = 0;
    int simpleCount = 0;
    for (int i = 0; i < nbModelRows - 1; i++) {
      if (i >= this.YAbscisRow_) {
        rowOffset = 1;
      }
      colOffset = 0;
      for (int j = 0; j < nbModelCols - 1; j++) {
        if (j >= this.XAbscisColumn_) {
          colOffset = 1;
        }
        float z0 = this.stackAllValues(prevDataseries, i, j);
        if (simpleCount == BAR_BUFFER_LIMIT) {
          simpleBufferIndex++;
          simpleCount = 0;
        }
        simplePtsArrays.get(simpleBufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
        simplePtsArrays.get(simpleBufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
        simplePtsArrays.get(simpleBufferIndex).putFloat(z0);
        double modelVal = StringUtils.asNumber(this.model_.getData(i + rowOffset, j + colOffset));
        float delta = modelVal <= 0 ? zeroBarCompensation : 0;
        simplePtsArrays
            .get(simpleBufferIndex)
            .putFloat((float) ((modelVal - zMin) / (zMax - zMin)) + delta);
        simpleCount++;
      }
    }
  }

  protected void barDataFromModel(
      final List<java.nio.ByteBuffer> simplePtsArrays,
      final List<java.nio.ByteBuffer> coloredPtsArrays,
      final List<java.nio.ByteBuffer> coloredPtsColors) {
    final List<WAbstractDataSeries3D> dataseries = this.chart_.getDataSeries();
    List<WAbstractGridData> prevDataseries = new ArrayList<WAbstractGridData>();
    boolean first = true;
    int xDim = 0;
    int yDim = 0;
    for (int i = 0; i < dataseries.size(); i++) {
      if (ObjectUtils.cast(dataseries.get(i), WAbstractGridData.class) != null) {
        WAbstractGridData griddata = ObjectUtils.cast(dataseries.get(i), WAbstractGridData.class);
        if (griddata == this || griddata.getType() != Series3DType.Bar) {
          break;
        }
        if (first) {
          xDim = griddata.getNbXPoints();
          yDim = griddata.getNbYPoints();
          first = false;
        }
        if (griddata.getNbXPoints() != xDim
            || griddata.getNbYPoints() != yDim
            || griddata.isHidden()) {
          continue;
        }
        prevDataseries.add(griddata);
      }
    }
    if (!prevDataseries.isEmpty() && (xDim != this.getNbXPoints() || yDim != this.getNbYPoints())) {
      throw new WException("WGridData.C: Dimensions of multiple bar-series data do not match");
    }
    int nbModelRows = this.model_.getRowCount();
    int nbModelCols = this.model_.getColumnCount();
    java.nio.ByteBuffer scaledXAxis = WebGLUtils.newByteBuffer(4 * (nbModelRows - 1));
    java.nio.ByteBuffer scaledYAxis = WebGLUtils.newByteBuffer(4 * (nbModelCols - 1));
    double xMin = this.chart_.axis(Axis.X3D).getMinimum();
    double xMax = this.chart_.axis(Axis.X3D).getMaximum();
    double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
    double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
    double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
    double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
    for (int i = 0; i < nbModelRows - 1; i++) {
      scaledXAxis.putFloat((float) ((xMin + 0.5 + i - xMin) / (xMax - xMin)));
    }
    for (int j = 0; j < nbModelCols - 1; j++) {
      scaledYAxis.putFloat((float) ((yMin + 0.5 + j - yMin) / (yMax - yMin)));
    }
    int rowOffset = 0;
    int colOffset = 0;
    int simpleBufferIndex = 0;
    int coloredBufferIndex = 0;
    int simpleCount = 0;
    int coloredCount = 0;
    for (int i = 0; i < nbModelRows - 1; i++) {
      if (i >= this.YAbscisRow_) {
        rowOffset = 1;
      }
      colOffset = 0;
      for (int j = 0; j < nbModelCols - 1; j++) {
        if (j >= this.XAbscisColumn_) {
          colOffset = 1;
        }
        float z0 = this.stackAllValues(prevDataseries, i, j);
        if (!(this.model_.getData(i + rowOffset, j + colOffset, ItemDataRole.MarkerBrushColor)
            != null)) {
          if (simpleCount == BAR_BUFFER_LIMIT) {
            simpleBufferIndex++;
            simpleCount = 0;
          }
          simplePtsArrays.get(simpleBufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
          simplePtsArrays.get(simpleBufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
          simplePtsArrays.get(simpleBufferIndex).putFloat(z0);
          double modelVal = StringUtils.asNumber(this.model_.getData(i + rowOffset, j + colOffset));
          float delta = modelVal <= 0 ? zeroBarCompensation : 0;
          simplePtsArrays
              .get(simpleBufferIndex)
              .putFloat((float) ((modelVal - zMin) / (zMax - zMin)) + delta);
          simpleCount++;
        } else {
          if (coloredCount == BAR_BUFFER_LIMIT) {
            coloredBufferIndex++;
            coloredCount = 0;
          }
          coloredPtsArrays.get(coloredBufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
          coloredPtsArrays.get(coloredBufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
          coloredPtsArrays.get(coloredBufferIndex).putFloat(z0);
          double modelVal = StringUtils.asNumber(this.model_.getData(i + rowOffset, j + colOffset));
          float delta = modelVal <= 0 ? zeroBarCompensation : 0;
          coloredPtsArrays
              .get(coloredBufferIndex)
              .putFloat((float) ((modelVal - zMin) / (zMax - zMin)) + delta);
          WColor color =
              ((WColor)
                  this.model_.getData(i + rowOffset, j + colOffset, ItemDataRole.MarkerBrushColor));
          for (int k = 0; k < 8; k++) {
            coloredPtsColors.get(coloredBufferIndex).putFloat((float) color.getRed());
            coloredPtsColors.get(coloredBufferIndex).putFloat((float) color.getGreen());
            coloredPtsColors.get(coloredBufferIndex).putFloat((float) color.getBlue());
            coloredPtsColors.get(coloredBufferIndex).putFloat((float) color.getAlpha());
          }
          coloredCount++;
        }
      }
    }
  }

  private void findRange() {
    double minSoFar = Double.MAX_VALUE;
    double maxSoFar = -Double.MAX_VALUE;
    double zVal;
    int nbModelRows = this.model_.getRowCount();
    int nbModelCols = this.model_.getColumnCount();
    for (int i = 0; i < nbModelRows; i++) {
      if (i == this.YAbscisRow_) {
        continue;
      }
      for (int j = 0; j < nbModelCols; j++) {
        if (j == this.XAbscisColumn_) {
          continue;
        }
        zVal = StringUtils.asNumber(this.model_.getData(i, j));
        if (zVal < minSoFar) {
          minSoFar = zVal;
        }
        if (zVal > maxSoFar) {
          maxSoFar = zVal;
        }
      }
    }
    this.zMin_ = minSoFar;
    this.zMax_ = maxSoFar;
    this.rangeCached_ = true;
  }

  private int XAbscisColumn_;
  private int YAbscisRow_;
}
