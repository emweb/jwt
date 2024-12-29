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
 * Class representing grid-based data for on a 3D chart.
 *
 * <p>General information can be found at {@link WAbstractDataSeries3D}. The model for this
 * dataseries does not contain an x- and y-axis. Instead the class derives the x- and y-values from
 * the minimum and delta provided in the constructor. The size of the model determines the size of
 * the grid. The model itself is structured as a table, where every value represents the z-value of
 * a data-point. The corresponding x- and y-values are calculated by adding delta times the
 * row/column-index to the axis-minimum.
 */
public class WEquidistantGridData extends WAbstractGridData {
  private static Logger logger = LoggerFactory.getLogger(WEquidistantGridData.class);

  /** Constructor. */
  public WEquidistantGridData(
      WAbstractItemModel model, double XMin, double deltaX, double YMin, double deltaY) {
    super(model);
    this.XMinimum_ = XMin;
    this.deltaX_ = deltaX;
    this.YMinimum_ = YMin;
    this.deltaY_ = deltaY;
  }

  public double minimum(Axis axis) {
    if (axis == Axis.X3D) {
      return this.XMinimum_;
    } else {
      if (axis == Axis.Y3D) {
        return this.YMinimum_;
      } else {
        if (axis == Axis.Z3D) {
          if (!this.rangeCached_) {
            this.findRange();
          }
          return this.zMin_;
        } else {
          throw new WException("WEquidistantGridData.C: unknown Axis-type");
        }
      }
    }
  }

  public double maximum(Axis axis) {
    int Nx;
    int Ny;
    if (axis == Axis.X3D) {
      Nx = this.model_.getRowCount();
      return this.XMinimum_ + (Nx - 1) * this.deltaX_;
    } else {
      if (axis == Axis.Y3D) {
        Ny = this.model_.getColumnCount();
        return this.YMinimum_ + (Ny - 1) * this.deltaY_;
      } else {
        if (axis == Axis.Z3D) {
          if (!this.rangeCached_) {
            this.findRange();
          }
          return this.zMax_;
        } else {
          throw new WException("WEquidistantGridData.C: unknown Axis-type");
        }
      }
    }
  }
  /** Sets the minimum and delta for the X-axis. */
  public void setXAbscis(double XMinimum, double deltaX) {
    this.XMinimum_ = XMinimum;
    this.deltaX_ = deltaX;
    if (this.chart_ != null) {
      this.chart_.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
    }
  }
  /**
   * Returns the minimum value of the X-axis.
   *
   * <p>
   *
   * @see WEquidistantGridData#setXAbscis(double XMinimum, double deltaX)
   */
  public double XMinimum() {
    return this.XMinimum_;
  }
  /**
   * Returns the delta value of the X-axis.
   *
   * <p>The delta is the interval between subsequent values on the axis.
   *
   * <p>
   *
   * @see WEquidistantGridData#setXAbscis(double XMinimum, double deltaX)
   */
  public double getDeltaX() {
    return this.deltaX_;
  }
  /** Sets the minimum and delta for the Y-axis. */
  public void setYAbscis(double YMinimum, double deltaY) {
    this.YMinimum_ = YMinimum;
    this.deltaY_ = deltaY;
    if (this.chart_ != null) {
      this.chart_.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
    }
  }
  /**
   * Returns the minimum value of the Y-axis.
   *
   * <p>
   *
   * @see WEquidistantGridData#setYAbscis(double YMinimum, double deltaY)
   */
  public double YMinimum() {
    return this.YMinimum_;
  }
  /**
   * Returns the delta value of the Y-axis.
   *
   * <p>The delta is the interval between subsequent values on the axis.
   *
   * <p>
   *
   * @see WEquidistantGridData#setYAbscis(double YMinimum, double deltaY)
   */
  public double getDeltaY() {
    return this.deltaY_;
  }

  public int getNbXPoints() {
    return this.model_.getRowCount();
  }

  public int getNbYPoints() {
    return this.model_.getColumnCount();
  }

  public WString axisLabel(int u, Axis axis) {
    if (axis == Axis.X3D) {
      return new WString("{1}").arg(this.XMinimum_ + u * this.deltaX_);
    } else {
      if (axis == Axis.Y3D) {
        return new WString("{1}").arg(this.YMinimum_ + u * this.deltaY_);
      } else {
        throw new WException("WEquidistantGridData: don't know this type of axis");
      }
    }
  }

  Object data(int i, int j) {
    return this.model_.getData(i, j);
  }

  int getCountSimpleData() {
    int result;
    result = 0;
    int nbModelRows = this.model_.getRowCount();
    int nbModelCols = this.model_.getColumnCount();
    for (int i = 0; i < nbModelRows; i++) {
      for (int j = 0; j < nbModelCols; j++) {
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
    int Nx = this.model_.getRowCount();
    int Ny = this.model_.getColumnCount();
    java.nio.ByteBuffer scaledXAxis = WebGLUtils.newByteBuffer(4 * (Nx));
    java.nio.ByteBuffer scaledYAxis = WebGLUtils.newByteBuffer(4 * (Ny));
    double xMin = this.chart_.axis(Axis.X3D).getMinimum();
    double xMax = this.chart_.axis(Axis.X3D).getMaximum();
    double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
    double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
    double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
    double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
    for (int i = 0; i < Nx; i++) {
      scaledXAxis.putFloat((float) ((this.XMinimum_ + i * this.deltaX_ - xMin) / (xMax - xMin)));
    }
    for (int j = 0; j < Ny; j++) {
      scaledYAxis.putFloat((float) ((this.YMinimum_ + j * this.deltaY_ - yMin) / (yMax - yMin)));
    }
    for (int i = 0; i < Nx; i++) {
      for (int j = 0; j < Ny; j++) {
        if (!(this.model_.getData(i, j, ItemDataRole.MarkerBrushColor) != null)) {
          simplePtsArray.putFloat(scaledXAxis.getFloat(4 * (i)));
          simplePtsArray.putFloat(scaledYAxis.getFloat(4 * (j)));
          simplePtsArray.putFloat(
              (float) ((StringUtils.asNumber(this.model_.getData(i, j)) - zMin) / (zMax - zMin)));
          if ((this.model_.getData(i, j, ItemDataRole.MarkerScaleFactor) != null)) {
            simplePtsSize.putFloat(
                (float)
                    StringUtils.asNumber(
                        this.model_.getData(i, j, ItemDataRole.MarkerScaleFactor)));
          } else {
            simplePtsSize.putFloat((float) this.getPointSize());
          }
        } else {
          coloredPtsArray.putFloat(scaledXAxis.getFloat(4 * (i)));
          coloredPtsArray.putFloat(scaledYAxis.getFloat(4 * (j)));
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
    int Nx = this.model_.getRowCount();
    int Ny = this.model_.getColumnCount();
    java.nio.ByteBuffer scaledXAxis = WebGLUtils.newByteBuffer(4 * (Nx));
    java.nio.ByteBuffer scaledYAxis = WebGLUtils.newByteBuffer(4 * (Ny));
    double xMin = this.chart_.axis(Axis.X3D).getMinimum();
    double xMax = this.chart_.axis(Axis.X3D).getMaximum();
    double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
    double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
    double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
    double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
    for (int i = 0; i < Nx; i++) {
      scaledXAxis.putFloat((float) ((this.XMinimum_ + i * this.deltaX_ - xMin) / (xMax - xMin)));
    }
    for (int j = 0; j < Ny; j++) {
      scaledYAxis.putFloat((float) ((this.YMinimum_ + j * this.deltaY_ - yMin) / (yMax - yMin)));
    }
    int nbXaxisBuffers = this.getNbXPoints() / (SURFACE_SIDE_LIMIT - 1);
    int nbYaxisBuffers = this.getNbYPoints() / (SURFACE_SIDE_LIMIT - 1);
    if (this.getNbXPoints() % (SURFACE_SIDE_LIMIT - 1) != 0) {
      nbXaxisBuffers++;
    }
    if (this.getNbYPoints() % (SURFACE_SIDE_LIMIT - 1) != 0) {
      nbYaxisBuffers++;
    }
    int SURFACE_SIDE_LIMIT = WAbstractGridData.SURFACE_SIDE_LIMIT - 1;
    int bufferIndex = 0;
    for (int k = 0; k < nbXaxisBuffers - 1; k++) {
      for (int l = 0; l < nbYaxisBuffers - 1; l++) {
        bufferIndex = k * nbYaxisBuffers + l;
        int cnt1 = 0;
        int i = k * SURFACE_SIDE_LIMIT;
        for (; i < (k + 1) * SURFACE_SIDE_LIMIT + 1; i++) {
          int cnt2 = 0;
          int j = l * SURFACE_SIDE_LIMIT;
          for (; j < (l + 1) * SURFACE_SIDE_LIMIT + 1; j++) {
            simplePtsArrays.get(bufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
            simplePtsArrays.get(bufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
            simplePtsArrays
                .get(bufferIndex)
                .putFloat(
                    (float)
                        ((StringUtils.asNumber(this.model_.getData(i, j)) - zMin) / (zMax - zMin)));
            cnt2++;
          }
          cnt1++;
        }
      }
      bufferIndex = k * nbYaxisBuffers + nbYaxisBuffers - 1;
      int cnt1 = 0;
      int i = k * SURFACE_SIDE_LIMIT;
      for (; i < (k + 1) * SURFACE_SIDE_LIMIT + 1; i++) {
        int j = (nbYaxisBuffers - 1) * SURFACE_SIDE_LIMIT;
        for (; j < Ny; j++) {
          simplePtsArrays.get(bufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
          simplePtsArrays.get(bufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
          simplePtsArrays
              .get(bufferIndex)
              .putFloat(
                  (float)
                      ((StringUtils.asNumber(this.model_.getData(i, j)) - zMin) / (zMax - zMin)));
        }
        cnt1++;
      }
    }
    for (int l = 0; l < nbYaxisBuffers - 1; l++) {
      bufferIndex = (nbXaxisBuffers - 1) * nbYaxisBuffers + l;
      int i = (nbXaxisBuffers - 1) * SURFACE_SIDE_LIMIT;
      for (; i < Nx; i++) {
        int cnt2 = 0;
        int j = l * SURFACE_SIDE_LIMIT;
        for (; j < (l + 1) * SURFACE_SIDE_LIMIT + 1; j++) {
          simplePtsArrays.get(bufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
          simplePtsArrays.get(bufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
          simplePtsArrays
              .get(bufferIndex)
              .putFloat(
                  (float)
                      ((StringUtils.asNumber(this.model_.getData(i, j)) - zMin) / (zMax - zMin)));
          cnt2++;
        }
      }
    }
    bufferIndex = (nbXaxisBuffers - 1) * nbYaxisBuffers + (nbYaxisBuffers - 1);
    int i = (nbXaxisBuffers - 1) * SURFACE_SIDE_LIMIT;
    for (; i < Nx; i++) {
      int j = (nbYaxisBuffers - 1) * SURFACE_SIDE_LIMIT;
      for (; j < Ny; j++) {
        simplePtsArrays.get(bufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
        simplePtsArrays.get(bufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
        simplePtsArrays
            .get(bufferIndex)
            .putFloat(
                (float) ((StringUtils.asNumber(this.model_.getData(i, j)) - zMin) / (zMax - zMin)));
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
      throw new WException(
          "WEquidistantGridData.C: Dimensions of multiple bar-series data do not match");
    }
    int Nx = this.model_.getRowCount();
    int Ny = this.model_.getColumnCount();
    java.nio.ByteBuffer scaledXAxis = WebGLUtils.newByteBuffer(4 * (Nx));
    java.nio.ByteBuffer scaledYAxis = WebGLUtils.newByteBuffer(4 * (Ny));
    double xMin = this.chart_.axis(Axis.X3D).getMinimum();
    double xMax = this.chart_.axis(Axis.X3D).getMaximum();
    double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
    double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
    double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
    double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
    for (int i = 0; i < Nx; i++) {
      scaledXAxis.putFloat((float) ((xMin + 0.5 + i - xMin) / (xMax - xMin)));
    }
    for (int j = 0; j < Ny; j++) {
      scaledYAxis.putFloat((float) ((yMin + 0.5 + j - yMin) / (yMax - yMin)));
    }
    int simpleBufferIndex = 0;
    int simpleCount = 0;
    for (int i = 0; i < Nx; i++) {
      for (int j = 0; j < Ny; j++) {
        float z0 = this.stackAllValues(prevDataseries, i, j);
        if (simpleCount == BAR_BUFFER_LIMIT) {
          simpleBufferIndex++;
          simpleCount = 0;
        }
        simplePtsArrays.get(simpleBufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
        simplePtsArrays.get(simpleBufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
        simplePtsArrays.get(simpleBufferIndex).putFloat(z0);
        double modelVal = StringUtils.asNumber(this.model_.getData(i, j));
        if (modelVal <= 0) {
          modelVal = 0.00001;
        }
        simplePtsArrays
            .get(simpleBufferIndex)
            .putFloat((float) ((modelVal - zMin) / (zMax - zMin)));
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
      throw new WException(
          "WEquidistantGridData.C: Dimensions of multiple bar-series data do not match");
    }
    int Nx = this.model_.getRowCount();
    int Ny = this.model_.getColumnCount();
    java.nio.ByteBuffer scaledXAxis = WebGLUtils.newByteBuffer(4 * (Nx));
    java.nio.ByteBuffer scaledYAxis = WebGLUtils.newByteBuffer(4 * (Ny));
    double xMin = this.chart_.axis(Axis.X3D).getMinimum();
    double xMax = this.chart_.axis(Axis.X3D).getMaximum();
    double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
    double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
    double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
    double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
    for (int i = 0; i < Nx; i++) {
      scaledXAxis.putFloat((float) ((xMin + 0.5 + i - xMin) / (xMax - xMin)));
    }
    for (int j = 0; j < Ny; j++) {
      scaledYAxis.putFloat((float) ((yMin + 0.5 + j - yMin) / (yMax - yMin)));
    }
    int simpleBufferIndex = 0;
    int coloredBufferIndex = 0;
    int simpleCount = 0;
    int coloredCount = 0;
    for (int i = 0; i < Nx; i++) {
      for (int j = 0; j < Ny; j++) {
        float z0 = this.stackAllValues(prevDataseries, i, j);
        if (!(this.model_.getData(i, j, ItemDataRole.MarkerBrushColor) != null)) {
          if (simpleCount == BAR_BUFFER_LIMIT) {
            simpleBufferIndex++;
            simpleCount = 0;
          }
          simplePtsArrays.get(simpleBufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
          simplePtsArrays.get(simpleBufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
          simplePtsArrays.get(simpleBufferIndex).putFloat(z0);
          double modelVal = StringUtils.asNumber(this.model_.getData(i, j));
          if (modelVal <= 0) {
            modelVal = 0.00001;
          }
          simplePtsArrays
              .get(simpleBufferIndex)
              .putFloat((float) ((modelVal - zMin) / (zMax - zMin)));
          simpleCount++;
        } else {
          if (coloredCount == BAR_BUFFER_LIMIT) {
            coloredBufferIndex++;
            coloredCount = 0;
          }
          coloredPtsArrays.get(coloredBufferIndex).putFloat(scaledXAxis.getFloat(4 * (i)));
          coloredPtsArrays.get(coloredBufferIndex).putFloat(scaledYAxis.getFloat(4 * (j)));
          coloredPtsArrays.get(coloredBufferIndex).putFloat(z0);
          double modelVal = StringUtils.asNumber(this.model_.getData(i, j));
          if (modelVal <= 0) {
            modelVal = 0.00001;
          }
          coloredPtsArrays
              .get(coloredBufferIndex)
              .putFloat((float) ((modelVal - zMin) / (zMax - zMin)));
          WColor color = ((WColor) this.model_.getData(i, j, ItemDataRole.MarkerBrushColor));
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
    double maxSoFar = Double.MIN_VALUE;
    double zVal;
    int nbModelRows = this.model_.getRowCount();
    int nbModelCols = this.model_.getColumnCount();
    for (int i = 0; i < nbModelRows; i++) {
      for (int j = 0; j < nbModelCols; j++) {
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

  private double XMinimum_;
  private double deltaX_;
  private double YMinimum_;
  private double deltaY_;
}
