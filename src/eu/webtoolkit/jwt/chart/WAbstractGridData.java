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
 * <p>General information can be found at {@link WAbstractDataSeries3D}. Information on how the
 * model is structured is provided in the subclasses. GridData can be represented in three ways.
 * This is indicated by {@link Series3DType} and can be either {@link Series3DType#Point}, {@link
 * Series3DType#Surface} or {@link Series3DType#Bar}. Note that points and surfaces can only be
 * added to charts of type {@link ChartType#Scatter}, while bars can only be added to charts of type
 * {@link ChartType#Category}.
 *
 * <p>When the data is shown as a surface, a mesh can be added to the surface. This draws lines over
 * the surface at the positions of the x- and y-values. For bar-series data, it is possible to
 * adjust the width of the bars in both directions.
 *
 * <p>The three types of data-representation are illustrated below.
 *
 * <p><div align="center"> <img src="doc-files/gridDataTypes.png">
 *
 * <p><strong>The three representation types of grid-based data</strong> </div>
 */
public abstract class WAbstractGridData extends WAbstractDataSeries3D {
  private static Logger logger = LoggerFactory.getLogger(WAbstractGridData.class);

  /** Constructor. */
  public WAbstractGridData(WAbstractItemModel model) {
    super(model);
    this.seriesType_ = Series3DType.Point;
    this.surfaceMeshEnabled_ = false;
    this.colorRoleEnabled_ = false;
    this.barWidthX_ = 0.5f;
    this.barWidthY_ = 0.5f;
    this.meshPen_ = new WPen();
    this.vertexPosBufferSizes_ = new ArrayList<Integer>();
    this.vertexPosBuffer2Sizes_ = new ArrayList<Integer>();
    this.indexBufferSizes_ = new ArrayList<Integer>();
    this.lineBufferSizes_ = new ArrayList<Integer>();
    this.indexBufferSizes2_ = new ArrayList<Integer>();
    this.lineBufferSizes2_ = new ArrayList<Integer>();
    this.isoLineBufferSizes_ = new ArrayList<Integer>();
    this.isoLineHeights_ = new ArrayList<Double>();
    this.isoLineColorMap_ = (WAbstractColorMap) null;
    this.vertexPosBuffers_ = new ArrayList<WGLWidget.Buffer>();
    this.vertexPosBuffers2_ = new ArrayList<WGLWidget.Buffer>();
    this.vertexSizeBuffers_ = new ArrayList<WGLWidget.Buffer>();
    this.vertexSizeBuffers2_ = new ArrayList<WGLWidget.Buffer>();
    this.vertexColorBuffers2_ = new ArrayList<WGLWidget.Buffer>();
    this.indexBuffers_ = new ArrayList<WGLWidget.Buffer>();
    this.indexBuffers2_ = new ArrayList<WGLWidget.Buffer>();
    this.overlayLinesBuffers_ = new ArrayList<WGLWidget.Buffer>();
    this.overlayLinesBuffers2_ = new ArrayList<WGLWidget.Buffer>();
    this.colormapTexBuffers_ = new ArrayList<WGLWidget.Buffer>();
    this.isoLineBuffers_ = new ArrayList<WGLWidget.Buffer>();
    this.binaryResources_ = new ArrayList<WMemoryResource>();
    this.fragShader_ = new WGLWidget.Shader();
    this.colFragShader_ = new WGLWidget.Shader();
    this.meshFragShader_ = new WGLWidget.Shader();
    this.singleColorFragShader_ = new WGLWidget.Shader();
    this.positionFragShader_ = new WGLWidget.Shader();
    this.isoLineFragShader_ = new WGLWidget.Shader();
    this.vertShader_ = new WGLWidget.Shader();
    this.colVertShader_ = new WGLWidget.Shader();
    this.meshVertShader_ = new WGLWidget.Shader();
    this.isoLineVertexShader_ = new WGLWidget.Shader();
    this.seriesProgram_ = new WGLWidget.Program();
    this.colSeriesProgram_ = new WGLWidget.Program();
    this.meshProgram_ = new WGLWidget.Program();
    this.singleColorProgram_ = new WGLWidget.Program();
    this.positionProgram_ = new WGLWidget.Program();
    this.isoLineProgram_ = new WGLWidget.Program();
    this.vertexPosAttr_ = new WGLWidget.AttribLocation();
    this.vertexPosAttr2_ = new WGLWidget.AttribLocation();
    this.singleColor_vertexPosAttr_ = new WGLWidget.AttribLocation();
    this.position_vertexPosAttr_ = new WGLWidget.AttribLocation();
    this.meshVertexPosAttr_ = new WGLWidget.AttribLocation();
    this.isoLineVertexPosAttr_ = new WGLWidget.AttribLocation();
    this.vertexSizeAttr_ = new WGLWidget.AttribLocation();
    this.vertexSizeAttr2_ = new WGLWidget.AttribLocation();
    this.vertexColAttr2_ = new WGLWidget.AttribLocation();
    this.barTexCoordAttr_ = new WGLWidget.AttribLocation();
    this.mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.mvMatrixUniform2_ = new WGLWidget.UniformLocation();
    this.singleColor_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.position_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.mesh_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.isoLine_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.pMatrix_ = new WGLWidget.UniformLocation();
    this.pMatrix2_ = new WGLWidget.UniformLocation();
    this.singleColor_pMatrix_ = new WGLWidget.UniformLocation();
    this.position_pMatrix_ = new WGLWidget.UniformLocation();
    this.mesh_pMatrix_ = new WGLWidget.UniformLocation();
    this.isoLine_pMatrix_ = new WGLWidget.UniformLocation();
    this.cMatrix_ = new WGLWidget.UniformLocation();
    this.cMatrix2_ = new WGLWidget.UniformLocation();
    this.singleColor_cMatrix_ = new WGLWidget.UniformLocation();
    this.position_cMatrix_ = new WGLWidget.UniformLocation();
    this.mesh_cMatrix_ = new WGLWidget.UniformLocation();
    this.isoLine_cMatrix_ = new WGLWidget.UniformLocation();
    this.TexSampler_ = new WGLWidget.UniformLocation();
    this.isoLine_TexSampler_ = new WGLWidget.UniformLocation();
    this.mesh_colorUniform_ = new WGLWidget.UniformLocation();
    this.singleColorUniform_ = new WGLWidget.UniformLocation();
    this.pointSpriteUniform_ = new WGLWidget.UniformLocation();
    this.pointSpriteUniform2_ = new WGLWidget.UniformLocation();
    this.vpHeightUniform_ = new WGLWidget.UniformLocation();
    this.vpHeightUniform2_ = new WGLWidget.UniformLocation();
    this.offset_ = new WGLWidget.UniformLocation();
    this.isoLine_offset_ = new WGLWidget.UniformLocation();
    this.scaleFactor_ = new WGLWidget.UniformLocation();
    this.isoLine_scaleFactor_ = new WGLWidget.UniformLocation();
    this.minPtUniform_ = new WGLWidget.UniformLocation();
    this.mesh_minPtUniform_ = new WGLWidget.UniformLocation();
    this.singleColor_minPtUniform_ = new WGLWidget.UniformLocation();
    this.position_minPtUniform_ = new WGLWidget.UniformLocation();
    this.maxPtUniform_ = new WGLWidget.UniformLocation();
    this.mesh_maxPtUniform_ = new WGLWidget.UniformLocation();
    this.singleColor_maxPtUniform_ = new WGLWidget.UniformLocation();
    this.position_maxPtUniform_ = new WGLWidget.UniformLocation();
    this.dataMinPtUniform_ = new WGLWidget.UniformLocation();
    this.mesh_dataMinPtUniform_ = new WGLWidget.UniformLocation();
    this.singleColor_dataMinPtUniform_ = new WGLWidget.UniformLocation();
    this.position_dataMinPtUniform_ = new WGLWidget.UniformLocation();
    this.dataMaxPtUniform_ = new WGLWidget.UniformLocation();
    this.mesh_dataMaxPtUniform_ = new WGLWidget.UniformLocation();
    this.singleColor_dataMaxPtUniform_ = new WGLWidget.UniformLocation();
    this.position_dataMaxPtUniform_ = new WGLWidget.UniformLocation();
    this.singleColor_marginUniform_ = new WGLWidget.UniformLocation();
    this.position_marginUniform_ = new WGLWidget.UniformLocation();
    this.colormapTexture_ = new WGLWidget.Texture();
    this.isoLineColorMapTexture_ = new WGLWidget.Texture();
    this.pointSpriteTexture_ = new WGLWidget.Texture();
    this.minPt_ = new ArrayList<Float>();
    this.maxPt_ = new ArrayList<Float>();
    this.jsMinPt_ = new WGLWidget.JavaScriptVector(3);
    this.jsMaxPt_ = new WGLWidget.JavaScriptVector(3);
    this.minPtChanged_ = true;
    this.maxPtChanged_ = true;
    this.changeClippingMinX_ = new JSlot();
    this.changeClippingMaxX_ = new JSlot();
    this.changeClippingMinY_ = new JSlot();
    this.changeClippingMaxY_ = new JSlot();
    this.changeClippingMinZ_ = new JSlot();
    this.changeClippingMaxZ_ = new JSlot();
    this.clippingLinesEnabled_ = false;
    this.clippingLinesColor_ = new WColor(0, 0, 0);
    for (int i = 0; i < 3; ++i) {
      this.minPt_.add(-Float.POSITIVE_INFINITY);
      this.maxPt_.add(Float.POSITIVE_INFINITY);
    }
  }

  public abstract double minimum(Axis axis);

  public abstract double maximum(Axis axis);
  /**
   * Sets the type of representation that will be used for the data.
   *
   * <p>All representations in {@link Series3DType} are possible for the data. Note that {@link
   * Series3DType#Point} and {@link Series3DType#Surface} can only be used on a chart that is
   * configured as a {@link ChartType#Scatter} and {@link Series3DType#Bar} can only be used on a
   * chart that is configured to be a {@link ChartType#Category}.
   *
   * <p>The default value is {@link Series3DType#Point}.
   */
  public void setType(Series3DType type) {
    if (this.seriesType_ != type) {
      this.seriesType_ = type;
      if (this.chart_ != null) {
        this.chart_.updateChart(
            EnumUtils.or(EnumSet.of(ChartUpdates.GLContext), ChartUpdates.GLTextures));
      }
    }
  }
  /**
   * Returns the type of representation that will be used for the data.
   *
   * <p>
   *
   * @see WAbstractGridData#setType(Series3DType type)
   */
  public Series3DType getType() {
    return this.seriesType_;
  }
  /**
   * Enables or disables a mesh for when a surface is drawn.
   *
   * <p>The default value is false. This option only takes effect when the type of this {@link
   * WGridData} is {@link Series3DType#Surface}. The mesh is drawn at the position of the x-axis and
   * y-axis values.
   */
  public void setSurfaceMeshEnabled(boolean enabled) {
    if (enabled != this.surfaceMeshEnabled_) {
      this.surfaceMeshEnabled_ = enabled;
      if (this.seriesType_ == Series3DType.Surface) {
        if (this.chart_ != null) {
          this.chart_.updateChart(
              EnumUtils.or(EnumSet.of(ChartUpdates.GLContext), ChartUpdates.GLTextures));
        }
      }
    }
  }
  /**
   * Enables or disables a mesh for when a surface is drawn.
   *
   * <p>Calls {@link #setSurfaceMeshEnabled(boolean enabled) setSurfaceMeshEnabled(true)}
   */
  public final void setSurfaceMeshEnabled() {
    setSurfaceMeshEnabled(true);
  }
  /**
   * Returns whether the surface-mesh is enabled for this dataseries.
   *
   * <p>
   *
   * @see WAbstractGridData#setSurfaceMeshEnabled(boolean enabled)
   */
  public boolean isSurfaceMeshEnabled() {
    return this.surfaceMeshEnabled_;
  }
  /**
   * Sets the bar-width.
   *
   * <p>This option only takes effect when the type of this {@link WGridData} is {@link
   * Series3DType#Bar}. The values provided should be between 0 and 1, where 1 lets the bars each
   * take up 1/(nb of x/y-values) of the axis.
   *
   * <p>The default bar-width is 0.5 in both directions.
   */
  public void setBarWidth(double xWidth, double yWidth) {
    if (xWidth != this.barWidthX_ || yWidth != this.barWidthY_) {
      this.barWidthX_ = xWidth;
      this.barWidthY_ = yWidth;
      if (this.chart_ != null) {
        this.chart_.updateChart(
            EnumUtils.or(EnumSet.of(ChartUpdates.GLContext), ChartUpdates.GLTextures));
      }
    }
  }
  /**
   * Returns the bar-width in the X-axis direction.
   *
   * <p>
   *
   * @see WAbstractGridData#setBarWidth(double xWidth, double yWidth)
   */
  public double getBarWidthX() {
    return this.barWidthX_;
  }
  /**
   * Returns the bar-width in the Y-axis direction.
   *
   * <p>
   *
   * @see WAbstractGridData#setBarWidth(double xWidth, double yWidth)
   */
  public double getBarWidthY() {
    return this.barWidthY_;
  }
  /**
   * Sets the {@link WPen} that is used for drawing the mesh.
   *
   * <p>Used when drawing the mesh on a surface or the lines around bars. The default is a default
   * constructed {@link WPen} (black and one pixel wide).
   *
   * <p>Note: only the width and color of this {@link WPen} are used.
   *
   * <p>
   *
   * @see WAbstractGridData#setSurfaceMeshEnabled(boolean enabled)
   */
  public void setPen(final WPen pen) {
    this.meshPen_ = pen;
    if (this.chart_ != null) {
      this.chart_.updateChart(
          EnumUtils.or(EnumSet.of(ChartUpdates.GLContext), ChartUpdates.GLTextures));
    }
  }
  /**
   * Returns the pen that is used for drawing the mesh.
   *
   * <p>
   *
   * @see WAbstractGridData#setPen(WPen pen)
   */
  public WPen getPen() {
    return this.meshPen_;
  }
  /**
   * Find all points on the surface that are projected to the given pixel.
   *
   * <p>A ray is cast from the given pixel&apos;s x,y position (from the top left of the chart, in
   * screen coordinates) and every intersection with the surface is returned, along with its
   * distance from the look point. Note that the coordinates of the intersection points are
   * interpolated between the data points that make up the surface.
   */
  public List<WSurfaceSelection> pickSurface(int x, int y) {
    javax.vecmath.GVector re = new javax.vecmath.GVector(new double[] {0, 0, 0});
    javax.vecmath.GVector rd = new javax.vecmath.GVector(new double[] {0, 0, 0});
    this.chart_.createRay(x, y, re, rd);
    javax.vecmath.Matrix4f invTransform =
        WebGLUtils.multiply(this.chart_.getCameraMatrix(), this.mvMatrix_);
    invTransform.invert();
    javax.vecmath.GVector camera = new javax.vecmath.GVector(new double[] {0.0, 0.0, 0.0, 1.0});
    camera = WebGLUtils.multiply(invTransform, camera);
    javax.vecmath.GVector camera3 = new javax.vecmath.GVector(camera);
    int Nx = this.getNbXPoints();
    int Ny = this.getNbYPoints();
    List<java.nio.ByteBuffer> simplePtsArrays = new ArrayList<java.nio.ByteBuffer>();
    int nbXaxisBuffers;
    int nbYaxisBuffers;
    nbXaxisBuffers = Nx / (SURFACE_SIDE_LIMIT - 1);
    nbYaxisBuffers = Ny / (SURFACE_SIDE_LIMIT - 1);
    if (Nx % (SURFACE_SIDE_LIMIT - 1) != 0) {
      nbXaxisBuffers++;
    }
    if (Ny % (SURFACE_SIDE_LIMIT - 1) != 0) {
      nbYaxisBuffers++;
    }
    for (int i = 0; i < nbXaxisBuffers - 1; i++) {
      for (int j = 0; j < nbYaxisBuffers - 1; j++) {
        simplePtsArrays.add(
            WebGLUtils.newByteBuffer(4 * (3 * SURFACE_SIDE_LIMIT * SURFACE_SIDE_LIMIT)));
      }
      ;
      simplePtsArrays.add(
          WebGLUtils.newByteBuffer(
              4
                  * (3
                      * SURFACE_SIDE_LIMIT
                      * (Ny - (nbYaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1)))));
    }
    for (int j = 0; j < nbYaxisBuffers - 1; j++) {
      simplePtsArrays.add(
          WebGLUtils.newByteBuffer(
              4
                  * (3
                      * (Nx - (nbXaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1))
                      * SURFACE_SIDE_LIMIT)));
    }
    simplePtsArrays.add(
        WebGLUtils.newByteBuffer(
            4
                * (3
                    * (Nx - (nbXaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1))
                    * (Ny - (nbYaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1)))));
    this.surfaceDataFromModel(simplePtsArrays);
    List<WSurfaceSelection> result = new ArrayList<WSurfaceSelection>();
    for (int i = 0; i < simplePtsArrays.size(); i++) {
      int Nx_patch = SURFACE_SIDE_LIMIT;
      int Ny_patch = SURFACE_SIDE_LIMIT;
      if ((i + 1) % nbYaxisBuffers == 0) {
        Ny_patch = Ny - (nbYaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1);
      }
      if ((int) i >= (nbXaxisBuffers - 1) * nbYaxisBuffers) {
        Nx_patch = Nx - (nbXaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1);
      }
      java.nio.IntBuffer vertexIndices =
          java.nio.IntBuffer.allocate((Nx_patch - 1) * (Ny_patch + 1) * 2);
      this.generateVertexIndices(vertexIndices, Nx_patch, Ny_patch);
      for (int j = 0; j < vertexIndices.capacity() - 2; ++j) {
        if (vertexIndices.get(j) == vertexIndices.get(j + 1)
            || vertexIndices.get(j + 1) == vertexIndices.get(j + 2)
            || vertexIndices.get(j) == vertexIndices.get(j + 2)) {
          continue;
        }
        javax.vecmath.GVector point = new javax.vecmath.GVector(new double[] {0, 0, 0});
        double distance =
            this.rayTriangleIntersect(
                re,
                rd,
                camera3,
                new javax.vecmath.GVector(
                    new double[] {
                      simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j) * 3)),
                      simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j) * 3 + 1)),
                      simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j) * 3 + 2))
                    }),
                new javax.vecmath.GVector(
                    new double[] {
                      simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 1) * 3)),
                      simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 1) * 3 + 1)),
                      simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 1) * 3 + 2))
                    }),
                new javax.vecmath.GVector(
                    new double[] {
                      simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 2) * 3)),
                      simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 2) * 3 + 1)),
                      simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 2) * 3 + 2))
                    }),
                point);
        if (distance != Double.POSITIVE_INFINITY) {
          double resX =
              point.getElement(0)
                      * (this.chart_.axis(Axis.X3D).getMaximum()
                          - this.chart_.axis(Axis.X3D).getMinimum())
                  + this.chart_.axis(Axis.X3D).getMinimum();
          double resY =
              point.getElement(1)
                      * (this.chart_.axis(Axis.Y3D).getMaximum()
                          - this.chart_.axis(Axis.Y3D).getMinimum())
                  + this.chart_.axis(Axis.Y3D).getMinimum();
          double resZ =
              point.getElement(2)
                      * (this.chart_.axis(Axis.Z3D).getMaximum()
                          - this.chart_.axis(Axis.Z3D).getMinimum())
                  + this.chart_.axis(Axis.Z3D).getMinimum();
          result.add(new WSurfaceSelection(distance, resX, resY, resZ));
        }
      }
    }
    return result;
  }
  /**
   * Return the bar that is closest to the look point at the given pixel.
   *
   * <p>A ray is cast from the given pixel&apos;s x,y position (from the top left of the chart, in
   * screen coordinates), and the closest bar on this {@link WAbstractGridData} is returned, along
   * with its distance from the look point.
   *
   * <p>Note that if this {@link WAbstractGridData} is hidden, this method still returns the closest
   * bar as if it was visible. Also, if multiple bars are on the same bar chart, the bar that is
   * returned may be behind another data series. Use the distance field of the returned {@link
   * WBarSelection} to determine which data series is in front from the given angle.
   *
   * <p>If there is no bar at the given pixel, then a selection with an invalid {@link WModelIndex}
   * is returned. The distance is then set to positive infinity.
   */
  public WBarSelection pickBar(int x, int y) {
    javax.vecmath.GVector re = new javax.vecmath.GVector(new double[] {0, 0, 0});
    javax.vecmath.GVector rd = new javax.vecmath.GVector(new double[] {0, 0, 0});
    this.chart_.createRay(x, y, re, rd);
    double closestDistance = Double.POSITIVE_INFINITY;
    int closestI = 0;
    int closestJ = 0;
    javax.vecmath.Matrix4f invTransform =
        WebGLUtils.multiply(this.chart_.getCameraMatrix(), this.mvMatrix_);
    invTransform.invert();
    javax.vecmath.GVector camera = new javax.vecmath.GVector(new double[] {0.0, 0.0, 0.0, 1.0});
    camera = WebGLUtils.multiply(invTransform, camera);
    int Nx = this.getNbXPoints();
    int Ny = this.getNbYPoints();
    int cnt = Nx * Ny;
    List<java.nio.ByteBuffer> simplePtsArrays = new ArrayList<java.nio.ByteBuffer>();
    List<java.nio.ByteBuffer> barVertexArrays = new ArrayList<java.nio.ByteBuffer>();
    int nbSimpleBarBuffers = cnt / BAR_BUFFER_LIMIT + 1;
    int PT_INFO_SIZE = 4;
    int PTS_PER_BAR = 8;
    for (int i = 0; i < nbSimpleBarBuffers - 1; ++i) {
      simplePtsArrays.add(WebGLUtils.newByteBuffer(4 * (PT_INFO_SIZE * BAR_BUFFER_LIMIT)));
      barVertexArrays.add(WebGLUtils.newByteBuffer(4 * (PTS_PER_BAR * 3 * BAR_BUFFER_LIMIT)));
    }
    simplePtsArrays.add(
        WebGLUtils.newByteBuffer(
            4 * (PT_INFO_SIZE * (cnt - BAR_BUFFER_LIMIT * (nbSimpleBarBuffers - 1)))));
    barVertexArrays.add(
        WebGLUtils.newByteBuffer(
            4 * (PTS_PER_BAR * 3 * (cnt - BAR_BUFFER_LIMIT * (nbSimpleBarBuffers - 1)))));
    this.barDataFromModel(simplePtsArrays);
    for (int i = 0; i < simplePtsArrays.size(); ++i) {
      this.barSeriesVertexData(simplePtsArrays.get(i), barVertexArrays.get(i));
    }
    for (int i = 0; i < simplePtsArrays.size(); ++i) {
      java.nio.IntBuffer vertexIndices =
          java.nio.IntBuffer.allocate(
              12 * 3 * (simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE));
      this.generateVertexIndices(
          vertexIndices, 0, 0, simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE);
      for (int j = 0; j < vertexIndices.capacity(); j += 3) {
        javax.vecmath.GVector point = new javax.vecmath.GVector(new double[] {0, 0, 0});
        double distance =
            this.rayTriangleIntersect(
                re,
                rd,
                new javax.vecmath.GVector(camera),
                new javax.vecmath.GVector(
                    new double[] {
                      barVertexArrays.get(i).getFloat(4 * (vertexIndices.get(j) * 3)),
                      barVertexArrays.get(i).getFloat(4 * (vertexIndices.get(j) * 3 + 1)),
                      barVertexArrays.get(i).getFloat(4 * (vertexIndices.get(j) * 3 + 2))
                    }),
                new javax.vecmath.GVector(
                    new double[] {
                      barVertexArrays.get(i).getFloat(4 * (vertexIndices.get(j + 1) * 3)),
                      barVertexArrays.get(i).getFloat(4 * (vertexIndices.get(j + 1) * 3 + 1)),
                      barVertexArrays.get(i).getFloat(4 * (vertexIndices.get(j + 1) * 3 + 2))
                    }),
                new javax.vecmath.GVector(
                    new double[] {
                      barVertexArrays.get(i).getFloat(4 * (vertexIndices.get(j + 2) * 3)),
                      barVertexArrays.get(i).getFloat(4 * (vertexIndices.get(j + 2) * 3 + 1)),
                      barVertexArrays.get(i).getFloat(4 * (vertexIndices.get(j + 2) * 3 + 2))
                    }),
                point);
        if (distance < closestDistance) {
          closestDistance = distance;
          closestI = i;
          closestJ = j;
        }
      }
    }
    if (closestDistance != Double.POSITIVE_INFINITY) {
      int tmp = BAR_BUFFER_LIMIT * closestI + closestJ / (12 * 3);
      return new WBarSelection(closestDistance, this.model_.getIndex(tmp / Ny + 1, tmp % Ny + 1));
    } else {
      return new WBarSelection(Double.POSITIVE_INFINITY, (WModelIndex) null);
    }
  }
  /**
   * Set isoline levels.
   *
   * <p>Isolines are drawn on the top or ground plane of the chart. Only applies if the type is
   * {@link Series3DType#Surface}.
   *
   * <p>The isoline levels are set in the coordinate system of the item model.
   */
  public void setIsoLevels(final List<Double> isoLevels) {
    Utils.copyList(isoLevels, this.isoLineHeights_);
    if (this.chart_ != null) {
      this.chart_.updateChart(
          EnumUtils.or(EnumSet.of(ChartUpdates.GLContext), ChartUpdates.GLTextures));
    }
  }
  /** Get all of the isoline levels. */
  public List<Double> getIsoLevels() {
    return this.isoLineHeights_;
  }
  /**
   * Set the color map for the isolines.
   *
   * <p>When no color map is defined for the isolines, i.e. {@link
   * WAbstractGridData#getIsoColorMap() getIsoColorMap()} is set to NULL, the color map of this
   * {@link WAbstractGridData} will be used.
   *
   * <p>The isolines are only drawn if the type is {@link Series3DType#Surface}.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setColorMap(WAbstractColorMap colormap)
   */
  public void setIsoColorMap(final WAbstractColorMap colormap) {
    this.isoLineColorMap_ = colormap;
    if (this.chart_ != null) {
      this.chart_.updateChart(
          EnumUtils.or(EnumSet.of(ChartUpdates.GLContext), ChartUpdates.GLTextures));
    }
  }
  /**
   * Get the color map for the isolines.
   *
   * <p>
   *
   * @see WAbstractGridData#setIsoColorMap(WAbstractColorMap colormap)
   */
  public WAbstractColorMap getIsoColorMap() {
    return this.isoLineColorMap_;
  }
  /**
   * Set the value below which the data series will be clipped on the given axis.
   *
   * <p>This only affects data series whose type is {@link Series3DType#Surface}.
   */
  public void setClippingMin(Axis axis, float v) {
    this.minPtChanged_ = true;
    if (this.jsMinPt_.isInitialized()) {
      Utils.copyList(this.jsMinPt_.getValue(), this.minPt_);
    }
    this.minPt_.set(axisToIndex(axis), v);
    if (this.chart_ != null) {
      this.chart_.updateChart(
          EnumUtils.or(EnumSet.of(ChartUpdates.GLContext), ChartUpdates.GLTextures));
    }
  }
  /**
   * Gets the value below which the data series will be clipped on the given axis.
   *
   * <p>
   *
   * @see WAbstractGridData#setClippingMin(Axis axis, float v)
   */
  public float getClippingMin(Axis axis) {
    int idx = axisToIndex(axis);
    return this.jsMinPt_.isInitialized() ? this.jsMinPt_.getValue().get(idx) : this.minPt_.get(idx);
  }
  /**
   * {@link JSlot} to change the value below which the data series will be clipped on the given
   * axis.
   *
   * <p>The {@link JSlot} takes one extra argument: the value to clip below.
   *
   * <p>The jsRef() of this {@link JSlot} is only valid when this {@link WAbstractGridData} has been
   * added to a {@link WCartesian3DChart}. If this {@link WAbstractGridData} moves to another {@link
   * WCartesian3DChart}, the jsRef() of the {@link JSlot} changes.
   *
   * <p>
   *
   * @see WAbstractGridData#setClippingMin(Axis axis, float v)
   */
  public JSlot changeClippingMin(Axis axis) {
    if (axis == Axis.X3D) {
      return this.changeClippingMinX_;
    } else {
      if (axis == Axis.Y3D) {
        return this.changeClippingMinY_;
      } else {
        if (axis == Axis.Z3D) {
          return this.changeClippingMinZ_;
        } else {
          throw new WException("Invalid axis for 3D chart");
        }
      }
    }
  }
  /**
   * Set the value above which the data series will be clipped on the given axis.
   *
   * <p>This only affects data series whose type is {@link Series3DType#Surface}.
   */
  public void setClippingMax(Axis axis, float v) {
    this.maxPtChanged_ = true;
    if (this.jsMaxPt_.isInitialized()) {
      Utils.copyList(this.jsMaxPt_.getValue(), this.maxPt_);
    }
    this.maxPt_.set(axisToIndex(axis), v);
    if (this.chart_ != null) {
      this.chart_.updateChart(
          EnumUtils.or(EnumSet.of(ChartUpdates.GLContext), ChartUpdates.GLTextures));
    }
  }
  /**
   * Gets the value above which the data series will be clipped on the given axis.
   *
   * <p>
   *
   * @see WAbstractGridData#setClippingMax(Axis axis, float v)
   */
  public float getClippingMax(Axis axis) {
    int idx = axisToIndex(axis);
    return this.jsMaxPt_.isInitialized() ? this.jsMaxPt_.getValue().get(idx) : this.maxPt_.get(idx);
  }
  /**
   * {@link JSlot} to change the value above which the data series will be clipped on the given
   * axis.
   *
   * <p>The {@link JSlot} takes one extra argument: the value to clip below.
   *
   * <p>The jsRef() of this {@link JSlot} is only valid when this {@link WAbstractGridData} has been
   * added to a {@link WCartesian3DChart}. If this {@link WAbstractGridData} moves to another {@link
   * WCartesian3DChart}, the jsRef() of the {@link JSlot} changes.
   *
   * <p>
   *
   * @see WAbstractGridData#setClippingMax(Axis axis, float v)
   */
  public JSlot changeClippingMax(Axis axis) {
    if (axis == Axis.X3D) {
      return this.changeClippingMaxX_;
    } else {
      if (axis == Axis.Y3D) {
        return this.changeClippingMaxY_;
      } else {
        if (axis == Axis.Z3D) {
          return this.changeClippingMaxZ_;
        } else {
          throw new WException("Invalid axis for 3D chart");
        }
      }
    }
  }
  /**
   * Sets whether clipping lines should be drawn where a surface is clipped.
   *
   * <p>Clipping lines are disabled by default. Note that rendering will be significantly slower
   * when enabled.
   */
  public void setClippingLinesEnabled(boolean clippingLinesEnabled) {
    this.clippingLinesEnabled_ = clippingLinesEnabled;
    if (this.chart_ != null) {
      this.chart_.updateChart(
          EnumUtils.or(EnumSet.of(ChartUpdates.GLContext), ChartUpdates.GLTextures));
    }
  }
  /**
   * Returns whether clipping lines are enabled.
   *
   * <p>
   *
   * @see WAbstractGridData#setClippingLinesEnabled(boolean clippingLinesEnabled)
   */
  public boolean isClippingLinesEnabled() {
    return this.clippingLinesEnabled_;
  }
  /**
   * Sets the color of the clipping lines.
   *
   * <p>
   *
   * @see WAbstractGridData#setClippingLinesEnabled(boolean clippingLinesEnabled)
   */
  public void setClippingLinesColor(WColor clippingLinesColor) {
    this.clippingLinesColor_ = clippingLinesColor;
    if (this.chart_ != null) {
      this.chart_.updateChart(
          EnumUtils.or(EnumSet.of(ChartUpdates.GLContext), ChartUpdates.GLTextures));
    }
  }
  /**
   * Gets the color of the clipping lines.
   *
   * <p>
   *
   * @see WAbstractGridData#setClippingLinesColor(WColor clippingLinesColor)
   * @see WAbstractGridData#setClippingLinesEnabled(boolean clippingLinesEnabled)
   */
  public WColor getClippingLinesColor() {
    return this.clippingLinesColor_;
  }

  public static final int SURFACE_SIDE_LIMIT = 256;
  public static final int BAR_BUFFER_LIMIT = 8190;

  public abstract int getNbXPoints();

  public abstract int getNbYPoints();

  public abstract WString axisLabel(int u, Axis axis);

  abstract Object data(int i, int j);

  public void setChart(WCartesian3DChart chart) {
    super.setChart(chart);
    this.jsMinPt_ = new WGLWidget.JavaScriptVector(3);
    this.jsMaxPt_ = new WGLWidget.JavaScriptVector(3);
    chart.addJavaScriptVector(this.jsMinPt_);
    chart.addJavaScriptVector(this.jsMaxPt_);
    this.minPtChanged_ = true;
    this.maxPtChanged_ = true;
    this.changeClippingMinX_.setJavaScript(
        "function(o,e,pos) {var obj = "
            + this.chart_.getJsRef()
            + ".wtObj;"
            + this.jsMinPt_.getJsRef()
            + "[0] = pos;"
            + chart.getRepaintSlot().execJs()
            + " }",
        1);
    this.changeClippingMaxX_.setJavaScript(
        "function(o,e,pos) {var obj = "
            + this.chart_.getJsRef()
            + ".wtObj;"
            + this.jsMaxPt_.getJsRef()
            + "[0] = pos;"
            + chart.getRepaintSlot().execJs()
            + " }",
        1);
    this.changeClippingMinY_.setJavaScript(
        "function(o,e,pos) {var obj = "
            + this.chart_.getJsRef()
            + ".wtObj;"
            + this.jsMinPt_.getJsRef()
            + "[1] = pos;"
            + chart.getRepaintSlot().execJs()
            + " }",
        1);
    this.changeClippingMaxY_.setJavaScript(
        "function(o,e,pos) {var obj = "
            + this.chart_.getJsRef()
            + ".wtObj;"
            + this.jsMaxPt_.getJsRef()
            + "[1] = pos;"
            + chart.getRepaintSlot().execJs()
            + " }",
        1);
    this.changeClippingMinZ_.setJavaScript(
        "function(o,e,pos) {var obj = "
            + this.chart_.getJsRef()
            + ".wtObj;"
            + this.jsMinPt_.getJsRef()
            + "[2] = pos;"
            + chart.getRepaintSlot().execJs()
            + " }",
        1);
    this.changeClippingMaxZ_.setJavaScript(
        "function(o,e,pos) {var obj = "
            + this.chart_.getJsRef()
            + ".wtObj;"
            + this.jsMaxPt_.getJsRef()
            + "[2] = pos;"
            + chart.getRepaintSlot().execJs()
            + " }",
        1);
  }

  public List<Object> getGlObjects() {
    List<Object> res = new ArrayList<Object>();
    List<List<WGLWidget.Buffer>> buffers = new ArrayList<List<WGLWidget.Buffer>>();
    buffers.add(this.vertexPosBuffers_);
    buffers.add(this.vertexPosBuffers2_);
    buffers.add(this.vertexSizeBuffers_);
    buffers.add(this.vertexSizeBuffers2_);
    buffers.add(this.vertexColorBuffers2_);
    buffers.add(this.indexBuffers_);
    buffers.add(this.indexBuffers2_);
    buffers.add(this.overlayLinesBuffers_);
    buffers.add(this.overlayLinesBuffers2_);
    buffers.add(this.colormapTexBuffers_);
    buffers.add(this.isoLineBuffers_);
    for (int i = 0; i < buffers.size(); ++i) {
      for (int j = 0; j < buffers.get(i).size(); ++j) {
        res.add(buffers.get(i).get(j));
      }
    }
    res.add(this.fragShader_);
    res.add(this.colFragShader_);
    res.add(this.meshFragShader_);
    res.add(this.singleColorFragShader_);
    res.add(this.positionFragShader_);
    res.add(this.isoLineFragShader_);
    res.add(this.vertShader_);
    res.add(this.colVertShader_);
    res.add(this.meshVertShader_);
    res.add(this.isoLineVertexShader_);
    res.add(this.seriesProgram_);
    res.add(this.colSeriesProgram_);
    res.add(this.meshProgram_);
    res.add(this.singleColorProgram_);
    res.add(this.isoLineProgram_);
    res.add(this.colormapTexture_);
    res.add(this.isoLineColorMapTexture_);
    res.add(this.pointSpriteTexture_);
    return res;
  }

  public void initializeGL() {
    this.chart_.initJavaScriptVector(this.jsMinPt_);
    this.chart_.initJavaScriptVector(this.jsMaxPt_);
  }

  public void paintGL() {
    if (this.hidden_) {
      return;
    }
    this.loadPointSpriteTexture(this.pointSpriteTexture_);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.NEAREST);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.NEAREST);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    switch (this.seriesType_) {
      case Point:
        if (this.chart_.getType() != ChartType.Scatter) {
          return;
        }
        break;
      case Surface:
        if (this.chart_.getType() != ChartType.Scatter) {
          return;
        }
        break;
      case Bar:
        if (this.chart_.getType() != ChartType.Category) {
          return;
        }
        break;
    }
    this.chart_.disable(WGLWidget.GLenum.CULL_FACE);
    this.chart_.enable(WGLWidget.GLenum.DEPTH_TEST);
    for (int i = 0; i < this.vertexPosBuffers_.size(); i++) {
      this.chart_.useProgram(this.seriesProgram_);
      this.chart_.uniformMatrix4(this.cMatrix_, this.chart_.getJsMatrix());
      this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.vertexPosBuffers_.get(i));
      this.chart_.vertexAttribPointer(this.vertexPosAttr_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.chart_.enableVertexAttribArray(this.vertexPosAttr_);
      if (this.seriesType_ == Series3DType.Bar) {
        this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.colormapTexBuffers_.get(i));
        this.chart_.vertexAttribPointer(
            this.barTexCoordAttr_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
        this.chart_.enableVertexAttribArray(this.barTexCoordAttr_);
      }
      double xMin = this.chart_.axis(Axis.X3D).getMinimum();
      double xMax = this.chart_.axis(Axis.X3D).getMaximum();
      double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
      double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
      double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
      double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
      this.chart_.activeTexture(WGLWidget.GLenum.TEXTURE0);
      this.chart_.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.colormapTexture_);
      this.chart_.uniform1i(this.TexSampler_, 0);
      if (!this.minPtUniform_.isNull()) {
        this.chart_.uniform3fv(this.minPtUniform_, this.jsMinPt_);
        this.chart_.uniform3fv(this.maxPtUniform_, this.jsMaxPt_);
        this.chart_.uniform3f(this.dataMinPtUniform_, xMin, yMin, zMin);
        this.chart_.uniform3f(this.dataMaxPtUniform_, xMax, yMax, zMax);
      }
      if (this.seriesType_ == Series3DType.Bar) {
        this.chart_.enable(WGLWidget.GLenum.POLYGON_OFFSET_FILL);
        float unitOffset =
            (float)
                (Math.pow(5, this.meshPen_.getWidth().getValue()) < 10000
                    ? Math.pow(5, this.meshPen_.getWidth().getValue())
                    : 10000);
        this.chart_.polygonOffset(1, unitOffset);
        this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.indexBuffers_.get(i));
        this.chart_.drawElements(
            WGLWidget.GLenum.TRIANGLES,
            this.indexBufferSizes_.get(i),
            WGLWidget.GLenum.UNSIGNED_SHORT,
            0);
        this.chart_.disableVertexAttribArray(this.vertexPosAttr_);
        this.chart_.disableVertexAttribArray(this.barTexCoordAttr_);
      } else {
        if (this.seriesType_ == Series3DType.Surface) {
          this.chart_.enable(WGLWidget.GLenum.POLYGON_OFFSET_FILL);
          float unitOffset =
              (float)
                  (Math.pow(5, this.meshPen_.getWidth().getValue()) < 10000
                      ? Math.pow(5, this.meshPen_.getWidth().getValue())
                      : 10000);
          this.chart_.polygonOffset(1, unitOffset);
          this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.indexBuffers_.get(i));
          this.chart_.drawElements(
              WGLWidget.GLenum.TRIANGLE_STRIP,
              this.indexBufferSizes_.get(i),
              WGLWidget.GLenum.UNSIGNED_SHORT,
              0);
          this.chart_.disable(WGLWidget.GLenum.POLYGON_OFFSET_FILL);
          this.chart_.disableVertexAttribArray(this.vertexPosAttr_);
        } else {
          this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.vertexSizeBuffers_.get(i));
          this.chart_.vertexAttribPointer(
              this.vertexSizeAttr_, 1, WGLWidget.GLenum.FLOAT, false, 0, 0);
          this.chart_.enableVertexAttribArray(this.vertexSizeAttr_);
          this.chart_.activeTexture(WGLWidget.GLenum.TEXTURE1);
          this.chart_.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.pointSpriteTexture_);
          this.chart_.uniform1i(this.pointSpriteUniform_, 1);
          this.chart_.uniform1f(this.vpHeightUniform_, this.chart_.getHeight().getValue());
          this.chart_.drawArrays(WGLWidget.GLenum.POINTS, 0, this.vertexPosBufferSizes_.get(i) / 3);
          this.chart_.disableVertexAttribArray(this.vertexPosAttr_);
          this.chart_.disableVertexAttribArray(this.vertexSizeAttr_);
        }
      }
      if (this.seriesType_ == Series3DType.Bar
          || this.seriesType_ == Series3DType.Surface && this.surfaceMeshEnabled_) {
        this.chart_.useProgram(this.meshProgram_);
        this.chart_.depthFunc(WGLWidget.GLenum.LEQUAL);
        this.chart_.uniformMatrix4(this.mesh_cMatrix_, this.chart_.getJsMatrix());
        this.chart_.uniform3fv(this.mesh_minPtUniform_, this.jsMinPt_);
        this.chart_.uniform3fv(this.mesh_maxPtUniform_, this.jsMaxPt_);
        this.chart_.uniform3f(this.mesh_dataMinPtUniform_, xMin, yMin, zMin);
        this.chart_.uniform3f(this.mesh_dataMaxPtUniform_, xMax, yMax, zMax);
        this.chart_.uniform4f(
            this.mesh_colorUniform_,
            (float) this.meshPen_.getColor().getRed(),
            (float) this.meshPen_.getColor().getGreen(),
            (float) this.meshPen_.getColor().getBlue(),
            (float) this.meshPen_.getColor().getAlpha());
        this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.vertexPosBuffers_.get(i));
        this.chart_.vertexAttribPointer(
            this.meshVertexPosAttr_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
        this.chart_.enableVertexAttribArray(this.meshVertexPosAttr_);
        this.chart_.bindBuffer(
            WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.overlayLinesBuffers_.get(i));
        if (this.seriesType_ == Series3DType.Surface) {
          this.chart_.lineWidth(
              this.meshPen_.getWidth().getValue() == 0 ? 1.0 : this.meshPen_.getWidth().getValue());
          this.chart_.drawElements(
              WGLWidget.GLenum.LINE_STRIP,
              this.lineBufferSizes_.get(i),
              WGLWidget.GLenum.UNSIGNED_SHORT,
              0);
        } else {
          if (this.seriesType_ == Series3DType.Bar) {
            this.chart_.lineWidth(
                this.meshPen_.getWidth().getValue() == 0
                    ? 1.0
                    : this.meshPen_.getWidth().getValue());
            this.chart_.drawElements(
                WGLWidget.GLenum.LINES,
                this.lineBufferSizes_.get(i),
                WGLWidget.GLenum.UNSIGNED_SHORT,
                0);
          }
        }
        this.chart_.disableVertexAttribArray(this.meshVertexPosAttr_);
      }
    }
    for (int i = 0; i < this.vertexPosBuffers2_.size(); i++) {
      this.chart_.useProgram(this.colSeriesProgram_);
      this.chart_.uniformMatrix4(this.cMatrix2_, this.chart_.getJsMatrix());
      this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.vertexPosBuffers2_.get(i));
      this.chart_.vertexAttribPointer(this.vertexPosAttr2_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.chart_.enableVertexAttribArray(this.vertexPosAttr2_);
      this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.vertexColorBuffers2_.get(i));
      this.chart_.vertexAttribPointer(this.vertexColAttr2_, 4, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.chart_.enableVertexAttribArray(this.vertexColAttr2_);
      if (this.seriesType_ == Series3DType.Point) {
        this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.vertexSizeBuffers2_.get(i));
        this.chart_.vertexAttribPointer(
            this.vertexSizeAttr2_, 1, WGLWidget.GLenum.FLOAT, false, 0, 0);
        this.chart_.enableVertexAttribArray(this.vertexSizeAttr2_);
        this.chart_.activeTexture(WGLWidget.GLenum.TEXTURE0);
        this.chart_.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.pointSpriteTexture_);
        this.chart_.uniform1i(this.pointSpriteUniform2_, 0);
        this.chart_.uniform1f(this.vpHeightUniform2_, this.chart_.getHeight().getValue());
        this.chart_.drawArrays(WGLWidget.GLenum.POINTS, 0, this.vertexPosBuffer2Sizes_.get(i) / 3);
        this.chart_.disableVertexAttribArray(this.vertexSizeAttr2_);
      } else {
        if (this.seriesType_ == Series3DType.Bar) {
          this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.indexBuffers2_.get(i));
          this.chart_.drawElements(
              WGLWidget.GLenum.TRIANGLES,
              this.indexBufferSizes2_.get(i),
              WGLWidget.GLenum.UNSIGNED_SHORT,
              0);
        }
      }
      this.chart_.disableVertexAttribArray(this.vertexPosAttr2_);
      this.chart_.disableVertexAttribArray(this.vertexColAttr2_);
      if (this.seriesType_ == Series3DType.Bar) {
        this.chart_.useProgram(this.meshProgram_);
        this.chart_.depthFunc(WGLWidget.GLenum.LEQUAL);
        this.chart_.enable(WGLWidget.GLenum.POLYGON_OFFSET_FILL);
        this.chart_.polygonOffset(1, 0.001);
        this.chart_.uniformMatrix4(this.mesh_cMatrix_, this.chart_.getJsMatrix());
        this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.vertexPosBuffers2_.get(i));
        this.chart_.vertexAttribPointer(
            this.meshVertexPosAttr_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
        this.chart_.enableVertexAttribArray(this.meshVertexPosAttr_);
        this.chart_.bindBuffer(
            WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.overlayLinesBuffers2_.get(i));
        this.chart_.drawElements(
            WGLWidget.GLenum.LINES,
            this.lineBufferSizes2_.get(i),
            WGLWidget.GLenum.UNSIGNED_SHORT,
            0);
        this.chart_.disableVertexAttribArray(this.meshVertexPosAttr_);
      }
    }
    if (this.seriesType_ == Series3DType.Surface && this.isoLineHeights_.size() > 0) {
      this.drawIsoLines();
    }
    this.chart_.enable(WGLWidget.GLenum.CULL_FACE);
    this.chart_.disable(WGLWidget.GLenum.DEPTH_TEST);
  }

  public void updateGL() {
    switch (this.seriesType_) {
      case Point:
        if (this.chart_.getType() != ChartType.Scatter) {
          return;
        }
        this.initializePointSeriesBuffers();
        break;
      case Surface:
        if (this.chart_.getType() != ChartType.Scatter) {
          return;
        }
        this.initializeSurfaceSeriesBuffers();
        break;
      case Bar:
        if (this.chart_.getType() != ChartType.Category) {
          return;
        }
        this.initializeBarSeriesBuffers();
        break;
    }
    this.colormapTexture_ = this.getColorTexture();
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.NEAREST);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.NEAREST);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.isoLineColorMapTexture_ = this.getIsoLineColorMapTexture();
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.NEAREST);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.NEAREST);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.pointSpriteTexture_ = this.getPointSpriteTexture();
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.NEAREST);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.NEAREST);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.chart_.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.initShaders();
    if (!this.jsMinPt_.isInitialized()) {
      this.chart_.initJavaScriptVector(this.jsMinPt_);
    }
    if (!this.jsMaxPt_.isInitialized()) {
      this.chart_.initJavaScriptVector(this.jsMaxPt_);
    }
    if (this.minPtChanged_) {
      this.chart_.setJavaScriptVector(this.jsMinPt_, this.minPt_);
      this.minPtChanged_ = false;
    }
    if (this.maxPtChanged_) {
      this.chart_.setJavaScriptVector(this.jsMaxPt_, this.maxPt_);
      this.maxPtChanged_ = false;
    }
    double min;
    double max;
    switch (this.seriesType_) {
      case Bar:
        break;
      case Point:
      case Surface:
        this.chart_.useProgram(this.seriesProgram_);
        if (this.colormap_ != null) {
          min = this.chart_.toPlotCubeCoords(this.colormap_.getMinimum(), Axis.Z3D);
          max = this.chart_.toPlotCubeCoords(this.colormap_.getMaximum(), Axis.Z3D);
          this.chart_.uniform1f(this.offset_, min);
          this.chart_.uniform1f(this.scaleFactor_, 1.0 / (max - min));
        } else {
          this.chart_.uniform1f(this.offset_, 0.0);
          this.chart_.uniform1f(this.scaleFactor_, 1.0);
        }
        if (this.isoLineHeights_.size() > 0) {
          this.chart_.useProgram(this.isoLineProgram_);
          if (this.isoLineColorMap_ != null) {
            min = this.chart_.toPlotCubeCoords(this.isoLineColorMap_.getMinimum(), Axis.Z3D);
            max = this.chart_.toPlotCubeCoords(this.isoLineColorMap_.getMaximum(), Axis.Z3D);
            this.chart_.uniform1f(this.isoLine_offset_, min);
            this.chart_.uniform1f(this.isoLine_scaleFactor_, 1.0 / (max - min));
          } else {
            if (this.colormap_ != null) {
              min = this.chart_.toPlotCubeCoords(this.colormap_.getMinimum(), Axis.Z3D);
              max = this.chart_.toPlotCubeCoords(this.colormap_.getMaximum(), Axis.Z3D);
              this.chart_.uniform1f(this.isoLine_offset_, min);
              this.chart_.uniform1f(this.isoLine_scaleFactor_, 1.0 / (max - min));
            } else {
              this.chart_.uniform1f(this.isoLine_offset_, 0.0);
              this.chart_.uniform1f(this.isoLine_scaleFactor_, 1.0);
            }
          }
        }
        break;
    }
    ;
    this.chart_.useProgram(this.seriesProgram_);
    this.chart_.uniformMatrix4(this.mvMatrixUniform_, this.mvMatrix_);
    this.chart_.uniformMatrix4(this.pMatrix_, this.chart_.getPMatrix());
    switch (this.seriesType_) {
      case Bar:
        this.chart_.useProgram(this.meshProgram_);
        this.chart_.uniformMatrix4(this.mesh_mvMatrixUniform_, this.mvMatrix_);
        this.chart_.uniformMatrix4(this.mesh_pMatrix_, this.chart_.getPMatrix());
      case Point:
        this.chart_.useProgram(this.colSeriesProgram_);
        this.chart_.uniformMatrix4(this.mvMatrixUniform2_, this.mvMatrix_);
        this.chart_.uniformMatrix4(this.pMatrix2_, this.chart_.getPMatrix());
        break;
      case Surface:
        if (this.surfaceMeshEnabled_) {
          this.chart_.useProgram(this.meshProgram_);
          this.chart_.uniformMatrix4(this.mesh_mvMatrixUniform_, this.mvMatrix_);
          this.chart_.uniformMatrix4(this.mesh_pMatrix_, this.chart_.getPMatrix());
        }
        if (this.isoLineHeights_.size() > 0) {
          this.chart_.useProgram(this.isoLineProgram_);
          this.chart_.uniformMatrix4(this.isoLine_mvMatrixUniform_, this.mvMatrix_);
          this.chart_.uniformMatrix4(this.isoLine_pMatrix_, this.chart_.getPMatrix());
        }
        if (this.chart_.isIntersectionLinesEnabled()
            || this.isClippingLinesEnabled()
            || !this.chart_.intersectionPlanes_.isEmpty()) {
          this.chart_.useProgram(this.singleColorProgram_);
          this.chart_.uniformMatrix4(this.singleColor_mvMatrixUniform_, this.mvMatrix_);
          this.chart_.uniformMatrix4(this.singleColor_pMatrix_, this.chart_.getPMatrix());
          this.chart_.useProgram(this.positionProgram_);
          this.chart_.uniformMatrix4(this.position_mvMatrixUniform_, this.mvMatrix_);
          this.chart_.uniformMatrix4(this.position_pMatrix_, this.chart_.getPMatrix());
        }
        break;
    }
    ;
  }

  public void resizeGL() {
    if (!this.seriesProgram_.isNull()) {
      this.chart_.useProgram(this.seriesProgram_);
      this.chart_.uniformMatrix4(this.pMatrix_, this.chart_.getPMatrix());
    }
    if (!this.colSeriesProgram_.isNull()) {
      this.chart_.useProgram(this.colSeriesProgram_);
      this.chart_.uniformMatrix4(this.pMatrix2_, this.chart_.getPMatrix());
    }
    if (!this.singleColorProgram_.isNull()) {
      this.chart_.useProgram(this.singleColorProgram_);
      this.chart_.uniformMatrix4(this.singleColor_pMatrix_, this.chart_.getPMatrix());
    }
    if (!this.positionProgram_.isNull()) {
      this.chart_.useProgram(this.positionProgram_);
      this.chart_.uniformMatrix4(this.position_pMatrix_, this.chart_.getPMatrix());
    }
    if (!this.meshProgram_.isNull()) {
      this.chart_.useProgram(this.meshProgram_);
      this.chart_.uniformMatrix4(this.mesh_pMatrix_, this.chart_.getPMatrix());
    }
    if (!this.isoLineProgram_.isNull()) {
      this.chart_.useProgram(this.isoLineProgram_);
      this.chart_.uniformMatrix4(this.isoLine_pMatrix_, this.chart_.getPMatrix());
    }
  }

  public void deleteAllGLResources() {
    if (this.seriesProgram_.isNull()) {
      return;
    }
    if (!this.singleColorProgram_.isNull()) {
      this.chart_.detachShader(this.singleColorProgram_, this.singleColorFragShader_);
      this.chart_.detachShader(this.singleColorProgram_, this.vertShader_);
      this.chart_.deleteShader(this.singleColorFragShader_);
      this.chart_.deleteProgram(this.singleColorProgram_);
      this.singleColorProgram_.clear();
    }
    if (!this.positionProgram_.isNull()) {
      this.chart_.detachShader(this.positionProgram_, this.positionFragShader_);
      this.chart_.detachShader(this.positionProgram_, this.vertShader_);
      this.chart_.deleteShader(this.positionFragShader_);
      this.chart_.deleteProgram(this.positionProgram_);
      this.positionProgram_.clear();
    }
    if (!this.seriesProgram_.isNull()) {
      this.chart_.detachShader(this.seriesProgram_, this.fragShader_);
      this.chart_.detachShader(this.seriesProgram_, this.vertShader_);
    }
    this.chart_.deleteShader(this.fragShader_);
    this.chart_.deleteShader(this.vertShader_);
    this.chart_.deleteProgram(this.seriesProgram_);
    this.seriesProgram_.clear();
    if (!this.colSeriesProgram_.isNull()) {
      this.chart_.detachShader(this.colSeriesProgram_, this.colFragShader_);
      this.chart_.detachShader(this.colSeriesProgram_, this.colVertShader_);
      this.chart_.deleteShader(this.colFragShader_);
      this.chart_.deleteShader(this.colVertShader_);
      this.chart_.deleteProgram(this.colSeriesProgram_);
      this.colSeriesProgram_.clear();
    }
    if (!this.meshProgram_.isNull()) {
      this.chart_.detachShader(this.meshProgram_, this.meshFragShader_);
      this.chart_.detachShader(this.meshProgram_, this.meshVertShader_);
      this.chart_.deleteShader(this.meshFragShader_);
      this.chart_.deleteShader(this.meshVertShader_);
      this.chart_.deleteProgram(this.meshProgram_);
      this.meshProgram_.clear();
    }
    if (!this.isoLineProgram_.isNull()) {
      this.chart_.detachShader(this.isoLineProgram_, this.isoLineFragShader_);
      this.chart_.detachShader(this.isoLineProgram_, this.isoLineVertexShader_);
      this.chart_.deleteShader(this.isoLineFragShader_);
      this.chart_.deleteShader(this.isoLineVertexShader_);
      this.chart_.deleteProgram(this.isoLineProgram_);
      this.isoLineProgram_.clear();
    }
    for (int i = 0; i < this.vertexPosBuffers_.size(); i++) {
      if (!this.vertexPosBuffers_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.vertexPosBuffers_.get(i));
        this.vertexPosBuffers_.get(i).clear();
      }
    }
    for (int i = 0; i < this.vertexSizeBuffers_.size(); i++) {
      if (!this.vertexSizeBuffers_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.vertexSizeBuffers_.get(i));
        this.vertexSizeBuffers_.get(i).clear();
      }
    }
    for (int i = 0; i < this.vertexPosBuffers2_.size(); i++) {
      if (!this.vertexPosBuffers2_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.vertexPosBuffers2_.get(i));
        this.vertexPosBuffers2_.get(i).clear();
      }
    }
    for (int i = 0; i < this.vertexColorBuffers2_.size(); i++) {
      if (!this.vertexColorBuffers2_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.vertexColorBuffers2_.get(i));
        this.vertexColorBuffers2_.get(i).clear();
      }
    }
    for (int i = 0; i < this.vertexSizeBuffers2_.size(); i++) {
      if (!this.vertexSizeBuffers2_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.vertexSizeBuffers2_.get(i));
        this.vertexSizeBuffers2_.get(i).clear();
      }
    }
    for (int i = 0; i < this.isoLineBuffers_.size(); ++i) {
      if (!this.isoLineBuffers_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.isoLineBuffers_.get(i));
        this.isoLineBuffers_.get(i).clear();
      }
    }
    for (int i = 0; i < this.indexBuffers_.size(); i++) {
      if (!this.indexBuffers_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.indexBuffers_.get(i));
        this.indexBuffers_.get(i).clear();
      }
    }
    for (int i = 0; i < this.indexBuffers2_.size(); i++) {
      if (!this.indexBuffers2_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.indexBuffers2_.get(i));
        this.indexBuffers2_.get(i).clear();
      }
    }
    for (int i = 0; i < this.overlayLinesBuffers_.size(); i++) {
      if (!this.overlayLinesBuffers_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.overlayLinesBuffers_.get(i));
        this.overlayLinesBuffers_.get(i).clear();
      }
    }
    for (int i = 0; i < this.overlayLinesBuffers2_.size(); i++) {
      if (!this.overlayLinesBuffers2_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.overlayLinesBuffers2_.get(i));
        this.overlayLinesBuffers2_.get(i).clear();
      }
    }
    for (int i = 0; i < this.colormapTexBuffers_.size(); i++) {
      if (!this.colormapTexBuffers_.get(i).isNull()) {
        this.chart_.deleteBuffer(this.colormapTexBuffers_.get(i));
        this.colormapTexBuffers_.get(i).clear();
      }
    }
    this.vertexPosBuffers_.clear();
    this.vertexPosBufferSizes_.clear();
    this.vertexSizeBuffers_.clear();
    this.vertexPosBuffers2_.clear();
    this.vertexPosBuffer2Sizes_.clear();
    this.vertexSizeBuffers2_.clear();
    this.vertexColorBuffers2_.clear();
    this.indexBuffers_.clear();
    this.indexBuffers2_.clear();
    this.indexBufferSizes_.clear();
    this.indexBufferSizes2_.clear();
    this.overlayLinesBuffers_.clear();
    this.overlayLinesBuffers2_.clear();
    this.lineBufferSizes_.clear();
    this.lineBufferSizes2_.clear();
    this.colormapTexBuffers_.clear();
    this.isoLineBuffers_.clear();
    this.isoLineBufferSizes_.clear();
    if (!this.colormapTexture_.isNull()) {
      this.chart_.deleteTexture(this.colormapTexture_);
      this.colormapTexture_.clear();
    }
    if (!this.isoLineColorMapTexture_.isNull()) {
      this.chart_.deleteTexture(this.isoLineColorMapTexture_);
      this.isoLineColorMapTexture_.clear();
    }
    if (!this.pointSpriteTexture_.isNull()) {
      this.chart_.deleteTexture(this.pointSpriteTexture_);
      this.pointSpriteTexture_.clear();
    }
  }

  abstract void pointDataFromModel(
      final java.nio.ByteBuffer simplePtsArray,
      final java.nio.ByteBuffer simplePtsSize,
      final java.nio.ByteBuffer coloredPtsArray,
      final java.nio.ByteBuffer coloredPtsSize,
      final java.nio.ByteBuffer coloredPtsColor);

  abstract void surfaceDataFromModel(final List<java.nio.ByteBuffer> simplePtsArrays);

  protected abstract void barDataFromModel(final List<java.nio.ByteBuffer> simplePtsArrays);

  protected abstract void barDataFromModel(
      final List<java.nio.ByteBuffer> simplePtsArrays,
      final List<java.nio.ByteBuffer> coloredPtsArrays,
      final List<java.nio.ByteBuffer> coloredPtsColors);

  abstract int getCountSimpleData();

  float stackAllValues(List<WAbstractGridData> dataseries, int i, int j) {
    float value = 0;
    for (int k = 0; k < dataseries.size(); k++) {
      float plotCubeVal =
          (float)
              this.chart_.toPlotCubeCoords(
                  StringUtils.asNumber(dataseries.get(k).data(i, j)), Axis.Z3D);
      if (plotCubeVal <= 0) {
        plotCubeVal = zeroBarCompensation;
      }
      value += plotCubeVal;
    }
    return value;
  }

  Series3DType seriesType_;
  boolean surfaceMeshEnabled_;
  boolean colorRoleEnabled_;
  double barWidthX_;
  double barWidthY_;
  WPen meshPen_;
  protected static final float zeroBarCompensation = 0.001f;

  private void initShaders() {
    switch (this.seriesType_) {
      case Bar:
        this.fragShader_ = this.chart_.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
        this.chart_.shaderSource(this.fragShader_, barFragShaderSrc);
        this.chart_.compileShader(this.fragShader_);
        this.vertShader_ = this.chart_.createShader(WGLWidget.GLenum.VERTEX_SHADER);
        this.chart_.shaderSource(this.vertShader_, barVertexShaderSrc);
        this.chart_.compileShader(this.vertShader_);
        this.seriesProgram_ = this.chart_.createProgram();
        this.chart_.attachShader(this.seriesProgram_, this.vertShader_);
        this.chart_.attachShader(this.seriesProgram_, this.fragShader_);
        this.chart_.linkProgram(this.seriesProgram_);
        this.vertexPosAttr_ = this.chart_.getAttribLocation(this.seriesProgram_, "aVertexPosition");
        this.barTexCoordAttr_ = this.chart_.getAttribLocation(this.seriesProgram_, "aTextureCoord");
        this.mvMatrixUniform_ = this.chart_.getUniformLocation(this.seriesProgram_, "uMVMatrix");
        this.pMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_, "uPMatrix");
        this.cMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_, "uCMatrix");
        this.TexSampler_ = this.chart_.getUniformLocation(this.seriesProgram_, "uSampler");
        this.colFragShader_ = this.chart_.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
        this.chart_.shaderSource(this.colFragShader_, colBarFragShaderSrc);
        this.chart_.compileShader(this.colFragShader_);
        this.colVertShader_ = this.chart_.createShader(WGLWidget.GLenum.VERTEX_SHADER);
        this.chart_.shaderSource(this.colVertShader_, colBarVertexShaderSrc);
        this.chart_.compileShader(this.colVertShader_);
        this.colSeriesProgram_ = this.chart_.createProgram();
        this.chart_.attachShader(this.colSeriesProgram_, this.colFragShader_);
        this.chart_.attachShader(this.colSeriesProgram_, this.colVertShader_);
        this.chart_.linkProgram(this.colSeriesProgram_);
        this.vertexPosAttr2_ =
            this.chart_.getAttribLocation(this.colSeriesProgram_, "aVertexPosition");
        this.vertexColAttr2_ =
            this.chart_.getAttribLocation(this.colSeriesProgram_, "aVertexColor");
        this.mvMatrixUniform2_ =
            this.chart_.getUniformLocation(this.colSeriesProgram_, "uMVMatrix");
        this.pMatrix2_ = this.chart_.getUniformLocation(this.colSeriesProgram_, "uPMatrix");
        this.cMatrix2_ = this.chart_.getUniformLocation(this.colSeriesProgram_, "uCMatrix");
        break;
      case Point:
        this.fragShader_ = this.chart_.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
        this.chart_.shaderSource(this.fragShader_, ptFragShaderSrc);
        this.chart_.compileShader(this.fragShader_);
        this.vertShader_ = this.chart_.createShader(WGLWidget.GLenum.VERTEX_SHADER);
        this.chart_.shaderSource(this.vertShader_, ptVertexShaderSrc);
        this.chart_.compileShader(this.vertShader_);
        this.seriesProgram_ = this.chart_.createProgram();
        this.chart_.attachShader(this.seriesProgram_, this.vertShader_);
        this.chart_.attachShader(this.seriesProgram_, this.fragShader_);
        this.chart_.linkProgram(this.seriesProgram_);
        this.vertexPosAttr_ = this.chart_.getAttribLocation(this.seriesProgram_, "aVertexPosition");
        this.vertexSizeAttr_ = this.chart_.getAttribLocation(this.seriesProgram_, "aPointSize");
        this.mvMatrixUniform_ = this.chart_.getUniformLocation(this.seriesProgram_, "uMVMatrix");
        this.pMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_, "uPMatrix");
        this.cMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_, "uCMatrix");
        this.TexSampler_ = this.chart_.getUniformLocation(this.seriesProgram_, "uSampler");
        this.pointSpriteUniform_ =
            this.chart_.getUniformLocation(this.seriesProgram_, "uPointSprite");
        this.vpHeightUniform_ = this.chart_.getUniformLocation(this.seriesProgram_, "uVPHeight");
        this.offset_ = this.chart_.getUniformLocation(this.seriesProgram_, "uOffset");
        this.scaleFactor_ = this.chart_.getUniformLocation(this.seriesProgram_, "uScaleFactor");
        this.colFragShader_ = this.chart_.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
        this.chart_.shaderSource(this.colFragShader_, colPtFragShaderSrc);
        this.chart_.compileShader(this.colFragShader_);
        this.colVertShader_ = this.chart_.createShader(WGLWidget.GLenum.VERTEX_SHADER);
        this.chart_.shaderSource(this.colVertShader_, colPtVertexShaderSrc);
        this.chart_.compileShader(this.colVertShader_);
        this.colSeriesProgram_ = this.chart_.createProgram();
        this.chart_.attachShader(this.colSeriesProgram_, this.colVertShader_);
        this.chart_.attachShader(this.colSeriesProgram_, this.colFragShader_);
        this.chart_.linkProgram(this.colSeriesProgram_);
        this.vertexPosAttr2_ =
            this.chart_.getAttribLocation(this.colSeriesProgram_, "aVertexPosition");
        this.vertexSizeAttr2_ = this.chart_.getAttribLocation(this.colSeriesProgram_, "aPointSize");
        this.vertexColAttr2_ = this.chart_.getAttribLocation(this.colSeriesProgram_, "aColor");
        this.mvMatrixUniform2_ =
            this.chart_.getUniformLocation(this.colSeriesProgram_, "uMVMatrix");
        this.pMatrix2_ = this.chart_.getUniformLocation(this.colSeriesProgram_, "uPMatrix");
        this.cMatrix2_ = this.chart_.getUniformLocation(this.colSeriesProgram_, "uCMatrix");
        this.pointSpriteUniform2_ =
            this.chart_.getUniformLocation(this.colSeriesProgram_, "uPointSprite");
        this.vpHeightUniform2_ =
            this.chart_.getUniformLocation(this.colSeriesProgram_, "uVPHeight");
        break;
      case Surface:
        this.fragShader_ = this.chart_.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
        this.chart_.shaderSource(this.fragShader_, surfFragShaderSrc);
        this.chart_.compileShader(this.fragShader_);
        this.vertShader_ = this.chart_.createShader(WGLWidget.GLenum.VERTEX_SHADER);
        this.chart_.shaderSource(this.vertShader_, surfVertexShaderSrc);
        this.chart_.compileShader(this.vertShader_);
        this.seriesProgram_ = this.chart_.createProgram();
        this.chart_.attachShader(this.seriesProgram_, this.vertShader_);
        this.chart_.attachShader(this.seriesProgram_, this.fragShader_);
        this.chart_.linkProgram(this.seriesProgram_);
        this.vertexPosAttr_ = this.chart_.getAttribLocation(this.seriesProgram_, "aVertexPosition");
        this.mvMatrixUniform_ = this.chart_.getUniformLocation(this.seriesProgram_, "uMVMatrix");
        this.pMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_, "uPMatrix");
        this.cMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_, "uCMatrix");
        this.TexSampler_ = this.chart_.getUniformLocation(this.seriesProgram_, "uSampler");
        this.offset_ = this.chart_.getUniformLocation(this.seriesProgram_, "uOffset");
        this.scaleFactor_ = this.chart_.getUniformLocation(this.seriesProgram_, "uScaleFactor");
        this.minPtUniform_ = this.chart_.getUniformLocation(this.seriesProgram_, "uMinPt");
        this.maxPtUniform_ = this.chart_.getUniformLocation(this.seriesProgram_, "uMaxPt");
        this.dataMinPtUniform_ = this.chart_.getUniformLocation(this.seriesProgram_, "uDataMinPt");
        this.dataMaxPtUniform_ = this.chart_.getUniformLocation(this.seriesProgram_, "uDataMaxPt");
        if (this.chart_.isIntersectionLinesEnabled()
            || this.isClippingLinesEnabled()
            || !this.chart_.intersectionPlanes_.isEmpty()) {
          this.singleColorFragShader_ = this.chart_.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
          this.chart_.shaderSource(this.singleColorFragShader_, surfFragSingleColorShaderSrc);
          this.chart_.compileShader(this.singleColorFragShader_);
          this.singleColorProgram_ = this.chart_.createProgram();
          this.chart_.attachShader(this.singleColorProgram_, this.vertShader_);
          this.chart_.attachShader(this.singleColorProgram_, this.singleColorFragShader_);
          this.chart_.linkProgram(this.singleColorProgram_);
          this.chart_.useProgram(this.singleColorProgram_);
          this.singleColor_vertexPosAttr_ =
              this.chart_.getAttribLocation(this.singleColorProgram_, "aVertexPosition");
          this.singleColor_mvMatrixUniform_ =
              this.chart_.getUniformLocation(this.singleColorProgram_, "uMVMatrix");
          this.singleColor_pMatrix_ =
              this.chart_.getUniformLocation(this.singleColorProgram_, "uPMatrix");
          this.singleColor_cMatrix_ =
              this.chart_.getUniformLocation(this.singleColorProgram_, "uCMatrix");
          this.singleColorUniform_ =
              this.chart_.getUniformLocation(this.singleColorProgram_, "uColor");
          this.singleColor_minPtUniform_ =
              this.chart_.getUniformLocation(this.singleColorProgram_, "uMinPt");
          this.singleColor_maxPtUniform_ =
              this.chart_.getUniformLocation(this.singleColorProgram_, "uMaxPt");
          this.singleColor_dataMinPtUniform_ =
              this.chart_.getUniformLocation(this.singleColorProgram_, "uDataMinPt");
          this.singleColor_dataMaxPtUniform_ =
              this.chart_.getUniformLocation(this.singleColorProgram_, "uDataMaxPt");
          this.singleColor_marginUniform_ =
              this.chart_.getUniformLocation(this.singleColorProgram_, "uMargin");
          this.positionFragShader_ = this.chart_.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
          this.chart_.shaderSource(this.positionFragShader_, surfFragPosShaderSrc);
          this.chart_.compileShader(this.positionFragShader_);
          this.positionProgram_ = this.chart_.createProgram();
          this.chart_.attachShader(this.positionProgram_, this.vertShader_);
          this.chart_.attachShader(this.positionProgram_, this.positionFragShader_);
          this.chart_.linkProgram(this.positionProgram_);
          this.chart_.useProgram(this.positionProgram_);
          this.position_vertexPosAttr_ =
              this.chart_.getAttribLocation(this.positionProgram_, "aVertexPosition");
          this.position_mvMatrixUniform_ =
              this.chart_.getUniformLocation(this.positionProgram_, "uMVMatrix");
          this.position_pMatrix_ =
              this.chart_.getUniformLocation(this.positionProgram_, "uPMatrix");
          this.position_cMatrix_ =
              this.chart_.getUniformLocation(this.positionProgram_, "uCMatrix");
          this.position_marginUniform_ =
              this.chart_.getUniformLocation(this.positionProgram_, "uMargin");
          this.position_minPtUniform_ =
              this.chart_.getUniformLocation(this.positionProgram_, "uMinPt");
          this.position_maxPtUniform_ =
              this.chart_.getUniformLocation(this.positionProgram_, "uMaxPt");
          this.position_dataMinPtUniform_ =
              this.chart_.getUniformLocation(this.positionProgram_, "uDataMinPt");
          this.position_dataMaxPtUniform_ =
              this.chart_.getUniformLocation(this.positionProgram_, "uDataMaxPt");
        }
        break;
    }
    ;
    if (this.seriesType_ == Series3DType.Bar
        || this.seriesType_ == Series3DType.Surface && this.surfaceMeshEnabled_) {
      this.meshFragShader_ = this.chart_.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
      this.chart_.shaderSource(this.meshFragShader_, meshFragShaderSrc);
      this.chart_.compileShader(this.meshFragShader_);
      this.meshVertShader_ = this.chart_.createShader(WGLWidget.GLenum.VERTEX_SHADER);
      this.chart_.shaderSource(this.meshVertShader_, meshVertexShaderSrc);
      this.chart_.compileShader(this.meshVertShader_);
      this.meshProgram_ = this.chart_.createProgram();
      this.chart_.attachShader(this.meshProgram_, this.meshVertShader_);
      this.chart_.attachShader(this.meshProgram_, this.meshFragShader_);
      this.chart_.linkProgram(this.meshProgram_);
      this.meshVertexPosAttr_ = this.chart_.getAttribLocation(this.meshProgram_, "aVertexPosition");
      this.mesh_mvMatrixUniform_ = this.chart_.getUniformLocation(this.meshProgram_, "uMVMatrix");
      this.mesh_pMatrix_ = this.chart_.getUniformLocation(this.meshProgram_, "uPMatrix");
      this.mesh_cMatrix_ = this.chart_.getUniformLocation(this.meshProgram_, "uCMatrix");
      this.mesh_colorUniform_ = this.chart_.getUniformLocation(this.meshProgram_, "uColor");
      this.mesh_minPtUniform_ = this.chart_.getUniformLocation(this.meshProgram_, "uMinPt");
      this.mesh_maxPtUniform_ = this.chart_.getUniformLocation(this.meshProgram_, "uMaxPt");
      this.mesh_dataMinPtUniform_ = this.chart_.getUniformLocation(this.meshProgram_, "uDataMinPt");
      this.mesh_dataMaxPtUniform_ = this.chart_.getUniformLocation(this.meshProgram_, "uDataMaxPt");
    }
    if (this.seriesType_ == Series3DType.Surface && this.isoLineHeights_.size() > 0) {
      this.isoLineFragShader_ = this.chart_.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
      this.chart_.shaderSource(this.isoLineFragShader_, isoLineFragShaderSrc);
      this.chart_.compileShader(this.isoLineFragShader_);
      this.isoLineVertexShader_ = this.chart_.createShader(WGLWidget.GLenum.VERTEX_SHADER);
      this.chart_.shaderSource(this.isoLineVertexShader_, isoLineVertexShaderSrc);
      this.chart_.compileShader(this.isoLineVertexShader_);
      this.isoLineProgram_ = this.chart_.createProgram();
      this.chart_.attachShader(this.isoLineProgram_, this.isoLineVertexShader_);
      this.chart_.attachShader(this.isoLineProgram_, this.isoLineFragShader_);
      this.chart_.linkProgram(this.isoLineProgram_);
      this.isoLineVertexPosAttr_ =
          this.chart_.getAttribLocation(this.isoLineProgram_, "aVertexPosition");
      this.isoLine_mvMatrixUniform_ =
          this.chart_.getUniformLocation(this.isoLineProgram_, "uMVMatrix");
      this.isoLine_pMatrix_ = this.chart_.getUniformLocation(this.isoLineProgram_, "uPMatrix");
      this.isoLine_cMatrix_ = this.chart_.getUniformLocation(this.isoLineProgram_, "uCMatrix");
      this.isoLine_TexSampler_ = this.chart_.getUniformLocation(this.isoLineProgram_, "uSampler");
      this.isoLine_offset_ = this.chart_.getUniformLocation(this.isoLineProgram_, "uOffset");
      this.isoLine_scaleFactor_ =
          this.chart_.getUniformLocation(this.isoLineProgram_, "uScaleFactor");
    }
  }

  private void initializePointSeriesBuffers() {
    int Nx = this.getNbXPoints();
    int Ny = this.getNbYPoints();
    int cnt = this.getCountSimpleData();
    List<java.nio.ByteBuffer> simplePtsArrays = new ArrayList<java.nio.ByteBuffer>();
    List<java.nio.ByteBuffer> simplePtsSizes = new ArrayList<java.nio.ByteBuffer>();
    List<java.nio.ByteBuffer> coloredPtsArrays = new ArrayList<java.nio.ByteBuffer>();
    List<java.nio.ByteBuffer> coloredPtsSizes = new ArrayList<java.nio.ByteBuffer>();
    List<java.nio.ByteBuffer> coloredPtsColors = new ArrayList<java.nio.ByteBuffer>();
    simplePtsArrays.add(WebGLUtils.newByteBuffer(4 * (3 * cnt)));
    simplePtsSizes.add(WebGLUtils.newByteBuffer(4 * (cnt)));
    coloredPtsArrays.add(WebGLUtils.newByteBuffer(4 * (3 * (Nx * Ny - cnt))));
    coloredPtsSizes.add(WebGLUtils.newByteBuffer(4 * (Nx * Ny - cnt)));
    coloredPtsColors.add(WebGLUtils.newByteBuffer(4 * (4 * (Nx * Ny - cnt))));
    this.pointDataFromModel(
        simplePtsArrays.get(0),
        simplePtsSizes.get(0),
        coloredPtsArrays.get(0),
        coloredPtsSizes.get(0),
        coloredPtsColors.get(0));
    for (int i = 0; i < simplePtsArrays.size(); i++) {
      if (simplePtsArrays.get(i).capacity() / 4 != 0) {
        this.loadBinaryResource(simplePtsArrays.get(i), this.vertexPosBuffers_);
        this.vertexPosBufferSizes_.add(simplePtsArrays.get(i).capacity() / 4);
        this.loadBinaryResource(simplePtsSizes.get(i), this.vertexSizeBuffers_);
      }
      if (coloredPtsArrays.get(i).capacity() / 4 != 0) {
        this.loadBinaryResource(coloredPtsArrays.get(i), this.vertexPosBuffers2_);
        this.vertexPosBuffer2Sizes_.add(coloredPtsArrays.get(i).capacity() / 4);
        this.loadBinaryResource(coloredPtsSizes.get(i), this.vertexSizeBuffers2_);
        this.loadBinaryResource(coloredPtsColors.get(i), this.vertexColorBuffers2_);
      }
    }
  }

  private void initializeSurfaceSeriesBuffers() {
    int Nx = this.getNbXPoints();
    int Ny = this.getNbYPoints();
    List<java.nio.ByteBuffer> simplePtsArrays = new ArrayList<java.nio.ByteBuffer>();
    int nbXaxisBuffers;
    int nbYaxisBuffers;
    nbXaxisBuffers = Nx / (SURFACE_SIDE_LIMIT - 1);
    nbYaxisBuffers = Ny / (SURFACE_SIDE_LIMIT - 1);
    if (Nx % (SURFACE_SIDE_LIMIT - 1) != 0) {
      nbXaxisBuffers++;
    }
    if (Ny % (SURFACE_SIDE_LIMIT - 1) != 0) {
      nbYaxisBuffers++;
    }
    for (int i = 0; i < nbXaxisBuffers - 1; i++) {
      for (int j = 0; j < nbYaxisBuffers - 1; j++) {
        simplePtsArrays.add(
            WebGLUtils.newByteBuffer(4 * (3 * SURFACE_SIDE_LIMIT * SURFACE_SIDE_LIMIT)));
      }
      simplePtsArrays.add(
          WebGLUtils.newByteBuffer(
              4
                  * (3
                      * SURFACE_SIDE_LIMIT
                      * (Ny - (nbYaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1)))));
    }
    for (int j = 0; j < nbYaxisBuffers - 1; j++) {
      simplePtsArrays.add(
          WebGLUtils.newByteBuffer(
              4
                  * (3
                      * (Nx - (nbXaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1))
                      * SURFACE_SIDE_LIMIT)));
    }
    simplePtsArrays.add(
        WebGLUtils.newByteBuffer(
            4
                * (3
                    * (Nx - (nbXaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1))
                    * (Ny - (nbYaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1)))));
    this.surfaceDataFromModel(simplePtsArrays);
    for (int i = 0; i < simplePtsArrays.size(); i++) {
      this.loadBinaryResource(simplePtsArrays.get(i), this.vertexPosBuffers_);
      this.vertexPosBufferSizes_.add(simplePtsArrays.get(i).capacity() / 4);
    }
    for (int i = 0; i < this.isoLineHeights_.size(); ++i) {
      List<Float> lines = new ArrayList<Float>();
      this.linesForIsoLevel(this.isoLineHeights_.get(i), lines);
      java.nio.ByteBuffer buff = WebGLUtils.newByteBuffer(4 * (lines.size()));
      for (int j = 0; j < lines.size(); ++j) {
        buff.putFloat(lines.get(j));
      }
      this.loadBinaryResource(buff, this.isoLineBuffers_);
      this.isoLineBufferSizes_.add(lines.size());
    }
    for (int i = 0; i < simplePtsArrays.size(); i++) {
      this.indexBuffers_.add(this.chart_.createBuffer());
      int Nx_patch = SURFACE_SIDE_LIMIT;
      int Ny_patch = SURFACE_SIDE_LIMIT;
      if ((i + 1) % nbYaxisBuffers == 0) {
        Ny_patch = Ny - (nbYaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1);
      }
      if ((int) i >= (nbXaxisBuffers - 1) * nbYaxisBuffers) {
        Nx_patch = Nx - (nbXaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1);
      }
      java.nio.IntBuffer vertexIndices =
          java.nio.IntBuffer.allocate((Nx_patch - 1) * (Ny_patch + 1) * 2);
      this.generateVertexIndices(vertexIndices, Nx_patch, Ny_patch);
      this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.indexBuffers_.get(i));
      this.chart_.bufferDataiv(
          WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
          vertexIndices,
          WGLWidget.GLenum.STATIC_DRAW,
          WGLWidget.GLenum.UNSIGNED_SHORT);
      this.indexBufferSizes_.add(vertexIndices.capacity());
      this.overlayLinesBuffers_.add(this.chart_.createBuffer());
      java.nio.IntBuffer lineIndices = java.nio.IntBuffer.allocate(2 * Nx_patch * Ny_patch);
      this.generateMeshIndices(lineIndices, Nx_patch, Ny_patch);
      this.chart_.bindBuffer(
          WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.overlayLinesBuffers_.get(i));
      this.chart_.bufferDataiv(
          WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
          lineIndices,
          WGLWidget.GLenum.STATIC_DRAW,
          WGLWidget.GLenum.UNSIGNED_SHORT);
      this.lineBufferSizes_.add(lineIndices.capacity());
    }
  }

  private void initializeBarSeriesBuffers() {
    int Nx = this.getNbXPoints();
    int Ny = this.getNbYPoints();
    int cnt = this.getCountSimpleData();
    List<java.nio.ByteBuffer> simplePtsArrays = new ArrayList<java.nio.ByteBuffer>();
    List<java.nio.ByteBuffer> coloredPtsArrays = new ArrayList<java.nio.ByteBuffer>();
    List<java.nio.ByteBuffer> coloredPtsColors = new ArrayList<java.nio.ByteBuffer>();
    List<java.nio.ByteBuffer> barVertexArrays = new ArrayList<java.nio.ByteBuffer>();
    List<java.nio.ByteBuffer> coloredBarVertexArrays = new ArrayList<java.nio.ByteBuffer>();
    int nbSimpleBarBuffers = cnt / BAR_BUFFER_LIMIT + 1;
    int nbColoredBarBuffers = (Nx * Ny - cnt) / BAR_BUFFER_LIMIT + 1;
    int PT_INFO_SIZE = 4;
    int PTS_PER_BAR = 8;
    for (int i = 0; i < nbSimpleBarBuffers - 1; i++) {
      simplePtsArrays.add(WebGLUtils.newByteBuffer(4 * (PT_INFO_SIZE * BAR_BUFFER_LIMIT)));
      barVertexArrays.add(WebGLUtils.newByteBuffer(4 * (PTS_PER_BAR * 3 * BAR_BUFFER_LIMIT)));
    }
    simplePtsArrays.add(
        WebGLUtils.newByteBuffer(
            4 * (PT_INFO_SIZE * (cnt - BAR_BUFFER_LIMIT * (nbSimpleBarBuffers - 1)))));
    barVertexArrays.add(
        WebGLUtils.newByteBuffer(
            4 * (PTS_PER_BAR * 3 * (cnt - BAR_BUFFER_LIMIT * (nbSimpleBarBuffers - 1)))));
    for (int i = 0; i < nbColoredBarBuffers - 1; i++) {
      coloredPtsArrays.add(WebGLUtils.newByteBuffer(4 * (PT_INFO_SIZE * BAR_BUFFER_LIMIT)));
      coloredPtsColors.add(
          WebGLUtils.newByteBuffer(4 * (PTS_PER_BAR * PT_INFO_SIZE * BAR_BUFFER_LIMIT)));
      coloredBarVertexArrays.add(
          WebGLUtils.newByteBuffer(4 * (PTS_PER_BAR * 3 * BAR_BUFFER_LIMIT)));
    }
    coloredPtsArrays.add(
        WebGLUtils.newByteBuffer(
            4 * (PT_INFO_SIZE * (Nx * Ny - cnt - BAR_BUFFER_LIMIT * (nbColoredBarBuffers - 1)))));
    coloredPtsColors.add(
        WebGLUtils.newByteBuffer(
            4
                * (PTS_PER_BAR
                    * PT_INFO_SIZE
                    * (Nx * Ny - cnt - BAR_BUFFER_LIMIT * (nbColoredBarBuffers - 1)))));
    coloredBarVertexArrays.add(
        WebGLUtils.newByteBuffer(
            4
                * (PTS_PER_BAR
                    * 3
                    * (Nx * Ny - cnt - BAR_BUFFER_LIMIT * (nbColoredBarBuffers - 1)))));
    this.barDataFromModel(simplePtsArrays, coloredPtsArrays, coloredPtsColors);
    for (int i = 0; i < simplePtsArrays.size(); i++) {
      this.barSeriesVertexData(simplePtsArrays.get(i), barVertexArrays.get(i));
    }
    for (int i = 0; i < coloredPtsArrays.size(); i++) {
      this.barSeriesVertexData(coloredPtsArrays.get(i), coloredBarVertexArrays.get(i));
    }
    for (int i = 0; i < barVertexArrays.size(); i++) {
      this.loadBinaryResource(barVertexArrays.get(i), this.vertexPosBuffers_);
      this.vertexPosBufferSizes_.add(barVertexArrays.get(i).capacity() / 4);
    }
    for (int i = 0; i < coloredBarVertexArrays.size(); i++) {
      this.loadBinaryResource(coloredBarVertexArrays.get(i), this.vertexPosBuffers2_);
      this.vertexPosBuffer2Sizes_.add(coloredBarVertexArrays.get(i).capacity() / 4);
      this.loadBinaryResource(coloredPtsColors.get(i), this.vertexColorBuffers2_);
    }
    for (int i = 0; i < simplePtsArrays.size(); i++) {
      this.indexBuffers_.add(this.chart_.createBuffer());
      java.nio.IntBuffer vertexIndices =
          java.nio.IntBuffer.allocate(
              12 * 3 * (simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE));
      this.generateVertexIndices(
          vertexIndices, 0, 0, simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE);
      this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.indexBuffers_.get(i));
      this.chart_.bufferDataiv(
          WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
          vertexIndices,
          WGLWidget.GLenum.STATIC_DRAW,
          WGLWidget.GLenum.UNSIGNED_SHORT);
      this.indexBufferSizes_.add(vertexIndices.capacity());
      this.overlayLinesBuffers_.add(this.chart_.createBuffer());
      java.nio.IntBuffer lineIndices =
          java.nio.IntBuffer.allocate(24 * (simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE));
      this.generateMeshIndices(
          lineIndices, 0, 0, simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE);
      this.chart_.bindBuffer(
          WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.overlayLinesBuffers_.get(i));
      this.chart_.bufferDataiv(
          WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
          lineIndices,
          WGLWidget.GLenum.STATIC_DRAW,
          WGLWidget.GLenum.UNSIGNED_SHORT);
      this.lineBufferSizes_.add(lineIndices.capacity());
      java.nio.ByteBuffer texCoordArray =
          WebGLUtils.newByteBuffer(
              4 * (PTS_PER_BAR * 2 * (simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE)));
      this.generateTextureCoords(
          texCoordArray,
          simplePtsArrays.get(i),
          simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE);
      this.colormapTexBuffers_.add(this.chart_.createBuffer());
      this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.colormapTexBuffers_.get(i));
      this.chart_.bufferDatafv(
          WGLWidget.GLenum.ARRAY_BUFFER, texCoordArray, WGLWidget.GLenum.STATIC_DRAW);
    }
    for (int i = 0; i < coloredPtsArrays.size(); i++) {
      this.indexBuffers2_.add(this.chart_.createBuffer());
      java.nio.IntBuffer vertexIndices =
          java.nio.IntBuffer.allocate(
              12 * 3 * (coloredPtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE));
      this.generateVertexIndices(
          vertexIndices, 0, 0, coloredPtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE);
      this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.indexBuffers2_.get(i));
      this.chart_.bufferDataiv(
          WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
          vertexIndices,
          WGLWidget.GLenum.STATIC_DRAW,
          WGLWidget.GLenum.UNSIGNED_SHORT);
      this.indexBufferSizes2_.add(vertexIndices.capacity());
      this.overlayLinesBuffers2_.add(this.chart_.createBuffer());
      java.nio.IntBuffer lineIndices =
          java.nio.IntBuffer.allocate(24 * (coloredPtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE));
      this.generateMeshIndices(
          lineIndices, 0, 0, coloredPtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE);
      this.chart_.bindBuffer(
          WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.overlayLinesBuffers2_.get(i));
      this.chart_.bufferDataiv(
          WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
          lineIndices,
          WGLWidget.GLenum.STATIC_DRAW,
          WGLWidget.GLenum.UNSIGNED_SHORT);
      this.lineBufferSizes2_.add(lineIndices.capacity());
    }
  }

  private void loadBinaryResource(
      final java.nio.ByteBuffer data, final List<WGLWidget.Buffer> buffers) {
    buffers.add(this.chart_.createBuffer());
    this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, buffers.get(buffers.size() - 1));
    this.chart_.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, data, WGLWidget.GLenum.STATIC_DRAW, true);
  }

  private void barSeriesVertexData(
      final java.nio.ByteBuffer verticesIN, final java.nio.ByteBuffer verticesOUT) {
    float x;
    float y;
    float z0;
    float z;
    for (int i = 0; i < verticesIN.capacity() / 4 / 4; i++) {
      int index = i * 4;
      x = verticesIN.getFloat(4 * (index));
      y = verticesIN.getFloat(4 * (index + 1));
      z0 = verticesIN.getFloat(4 * (index + 2));
      z = verticesIN.getFloat(4 * (index + 2)) + verticesIN.getFloat(4 * (index + 3));
      float barWidthX_ = (float) (this.barWidthX_ / this.getNbXPoints());
      float barWidthY_ = (float) (this.barWidthY_ / this.getNbYPoints());
      push(verticesOUT, x - barWidthX_ / 2, y + barWidthY_ / 2, z0);
      push(verticesOUT, x - barWidthX_ / 2, y + barWidthY_ / 2, z);
      push(verticesOUT, x + barWidthX_ / 2, y + barWidthY_ / 2, z);
      push(verticesOUT, x + barWidthX_ / 2, y + barWidthY_ / 2, z0);
      push(verticesOUT, x - barWidthX_ / 2, y - barWidthY_ / 2, z0);
      push(verticesOUT, x - barWidthX_ / 2, y - barWidthY_ / 2, z);
      push(verticesOUT, x + barWidthX_ / 2, y - barWidthY_ / 2, z);
      push(verticesOUT, x + barWidthX_ / 2, y - barWidthY_ / 2, z0);
    }
  }

  private void generateVertexIndices(
      final java.nio.IntBuffer indicesOUT, int Nx, int Ny, int size) {
    boolean forward = true;
    switch (this.seriesType_) {
      case Point:
        break;
      case Surface:
        for (int i = 0; i < Nx - 1; i++) {
          if (forward) {
            for (int j = 0; j < Ny; j++) {
              indicesOUT.put(i * Ny + j);
              indicesOUT.put((i + 1) * Ny + j);
            }
            indicesOUT.put((i + 1) * Ny + (Ny - 1));
            forward = false;
          } else {
            for (int j = Ny - 1; j >= 0; j--) {
              indicesOUT.put(i * Ny + j);
              indicesOUT.put((i + 1) * Ny + j);
            }
            indicesOUT.put((i + 1) * Ny);
            forward = true;
          }
        }
        break;
      case Bar:
        for (int i = 0; i < size; i++) {
          int index = i * 8;
          push(indicesOUT, index + 0, index + 1, index + 2);
          push(indicesOUT, index + 0, index + 2, index + 3);
          push(indicesOUT, index + 4, index + 5, index + 1);
          push(indicesOUT, index + 4, index + 1, index + 0);
          push(indicesOUT, index + 3, index + 2, index + 6);
          push(indicesOUT, index + 3, index + 6, index + 7);
          push(indicesOUT, index + 7, index + 6, index + 5);
          push(indicesOUT, index + 7, index + 5, index + 4);
          push(indicesOUT, index + 4, index + 0, index + 3);
          push(indicesOUT, index + 4, index + 3, index + 7);
          push(indicesOUT, index + 1, index + 5, index + 6);
          push(indicesOUT, index + 1, index + 6, index + 2);
        }
        break;
    }
    ;
  }

  private final void generateVertexIndices(final java.nio.IntBuffer indicesOUT, int Nx, int Ny) {
    generateVertexIndices(indicesOUT, Nx, Ny, 0);
  }

  private void generateMeshIndices(final java.nio.IntBuffer indicesOUT, int Nx, int Ny, int size) {
    boolean forward = true;
    switch (this.seriesType_) {
      case Point:
        break;
      case Surface:
        for (int i = 0; i < Nx; i++) {
          if (forward) {
            for (int j = 0; j < Ny; j++) {
              indicesOUT.put(i * Ny + j);
            }
            forward = false;
          } else {
            for (int j = Ny - 1; j >= 0; j--) {
              indicesOUT.put(i * Ny + j);
            }
            forward = true;
          }
        }
        if (forward == true) {
          forward = false;
          for (int i = 0; i < Ny; i++) {
            if (forward) {
              for (int j = 0; j < Nx; j++) {
                indicesOUT.put(j * Ny + i);
              }
              forward = false;
            } else {
              for (int j = Nx - 1; j >= 0; j--) {
                indicesOUT.put(j * Ny + i);
              }
              forward = true;
            }
          }
        } else {
          forward = false;
          for (int i = Ny - 1; i >= 0; i--) {
            if (forward) {
              for (int j = 0; j < Nx; j++) {
                indicesOUT.put(j * Ny + i);
              }
              forward = false;
            } else {
              for (int j = Nx - 1; j >= 0; j--) {
                indicesOUT.put(j * Ny + i);
              }
              forward = true;
            }
          }
        }
        break;
      case Bar:
        for (int i = 0; i < size; i++) {
          int index = i * 8;
          indicesOUT.put(index + 0);
          indicesOUT.put(index + 1);
          indicesOUT.put(index + 1);
          indicesOUT.put(index + 2);
          indicesOUT.put(index + 2);
          indicesOUT.put(index + 3);
          indicesOUT.put(index + 3);
          indicesOUT.put(index + 0);
          indicesOUT.put(index + 4);
          indicesOUT.put(index + 5);
          indicesOUT.put(index + 5);
          indicesOUT.put(index + 6);
          indicesOUT.put(index + 6);
          indicesOUT.put(index + 7);
          indicesOUT.put(index + 7);
          indicesOUT.put(index + 4);
          indicesOUT.put(index + 0);
          indicesOUT.put(index + 4);
          indicesOUT.put(index + 1);
          indicesOUT.put(index + 5);
          indicesOUT.put(index + 2);
          indicesOUT.put(index + 6);
          indicesOUT.put(index + 3);
          indicesOUT.put(index + 7);
        }
        break;
    }
    ;
  }

  private final void generateMeshIndices(final java.nio.IntBuffer indicesOUT, int Nx, int Ny) {
    generateMeshIndices(indicesOUT, Nx, Ny, 0);
  }

  private void generateTextureCoords(
      final java.nio.ByteBuffer coordsOUT, final java.nio.ByteBuffer dataArray, int size) {
    switch (this.seriesType_) {
      case Point:
      case Surface:
        break;
      case Bar:
        if (this.colormap_ == null) {
          for (int i = 0; i < size; i++) {
            for (int k = 0; k < 16; k++) {
              coordsOUT.putFloat(0.0f);
            }
          }
        } else {
          float min = (float) this.chart_.toPlotCubeCoords(this.colormap_.getMinimum(), Axis.Z3D);
          float max = (float) this.chart_.toPlotCubeCoords(this.colormap_.getMaximum(), Axis.Z3D);
          for (int i = 0; i < size; i++) {
            float zNorm = (dataArray.getFloat(4 * (i * 4 + 3)) - min) / (max - min);
            for (int k = 0; k < 8; k++) {
              coordsOUT.putFloat(0.0f);
              coordsOUT.putFloat(zNorm);
            }
          }
        }
        break;
    }
    ;
  }

  void paintGLIndex(int index) {
    this.paintGLIndex(index, 0.0, 0.0, 0.0);
  }

  void paintGLIndex(int index, double marginX, double marginY, double marginZ) {
    if (this.hidden_) {
      return;
    }
    if (this.seriesType_ != Series3DType.Surface || this.chart_.getType() != ChartType.Scatter) {
      return;
    }
    double xMin = this.chart_.axis(Axis.X3D).getMinimum();
    double xMax = this.chart_.axis(Axis.X3D).getMaximum();
    double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
    double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
    double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
    double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
    this.chart_.disable(WGLWidget.GLenum.CULL_FACE);
    this.chart_.enable(WGLWidget.GLenum.DEPTH_TEST);
    for (int i = 0; i < this.vertexPosBuffers_.size(); i++) {
      this.chart_.useProgram(this.singleColorProgram_);
      this.chart_.uniformMatrix4(this.singleColor_cMatrix_, this.chart_.getJsMatrix());
      this.chart_.uniform3fv(this.singleColor_minPtUniform_, this.jsMinPt_);
      this.chart_.uniform3fv(this.singleColor_maxPtUniform_, this.jsMaxPt_);
      this.chart_.uniform3f(this.singleColor_dataMinPtUniform_, xMin, yMin, zMin);
      this.chart_.uniform3f(this.singleColor_dataMaxPtUniform_, xMax, yMax, zMax);
      this.chart_.uniform3f(this.singleColor_marginUniform_, marginX, marginY, marginZ);
      this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.vertexPosBuffers_.get(i));
      this.chart_.vertexAttribPointer(
          this.singleColor_vertexPosAttr_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.chart_.enableVertexAttribArray(this.singleColor_vertexPosAttr_);
      float r = (index >> 16 & 0xff) / 255.0f;
      float g = (index >> 8 & 0xff) / 255.0f;
      float b = (index & 0xff) / 255.0f;
      this.chart_.uniform3f(this.singleColorUniform_, r, g, b);
      this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.indexBuffers_.get(i));
      this.chart_.drawElements(
          WGLWidget.GLenum.TRIANGLE_STRIP,
          this.indexBufferSizes_.get(i),
          WGLWidget.GLenum.UNSIGNED_SHORT,
          0);
      this.chart_.disableVertexAttribArray(this.singleColor_vertexPosAttr_);
    }
    this.chart_.enable(WGLWidget.GLenum.CULL_FACE);
    this.chart_.disable(WGLWidget.GLenum.DEPTH_TEST);
  }

  void paintGLPositions() {
    this.paintGLPositions(0.0, 0.0, 0.0);
  }

  void paintGLPositions(double marginX, double marginY, double marginZ) {
    if (this.hidden_) {
      return;
    }
    if (this.seriesType_ != Series3DType.Surface || this.chart_.getType() != ChartType.Scatter) {
      return;
    }
    double xMin = this.chart_.axis(Axis.X3D).getMinimum();
    double xMax = this.chart_.axis(Axis.X3D).getMaximum();
    double yMin = this.chart_.axis(Axis.Y3D).getMinimum();
    double yMax = this.chart_.axis(Axis.Y3D).getMaximum();
    double zMin = this.chart_.axis(Axis.Z3D).getMinimum();
    double zMax = this.chart_.axis(Axis.Z3D).getMaximum();
    this.chart_.disable(WGLWidget.GLenum.CULL_FACE);
    this.chart_.enable(WGLWidget.GLenum.DEPTH_TEST);
    for (int i = 0; i < this.vertexPosBuffers_.size(); i++) {
      this.chart_.useProgram(this.positionProgram_);
      this.chart_.uniformMatrix4(this.position_cMatrix_, this.chart_.getJsMatrix());
      this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.vertexPosBuffers_.get(i));
      this.chart_.uniform3fv(this.position_minPtUniform_, this.jsMinPt_);
      this.chart_.uniform3fv(this.position_maxPtUniform_, this.jsMaxPt_);
      this.chart_.uniform3f(this.position_dataMinPtUniform_, xMin, yMin, zMin);
      this.chart_.uniform3f(this.position_dataMaxPtUniform_, xMax, yMax, zMax);
      this.chart_.uniform3f(this.position_marginUniform_, marginX, marginY, marginZ);
      this.chart_.vertexAttribPointer(
          this.position_vertexPosAttr_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.chart_.enableVertexAttribArray(this.position_vertexPosAttr_);
      this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.indexBuffers_.get(i));
      this.chart_.drawElements(
          WGLWidget.GLenum.TRIANGLE_STRIP,
          this.indexBufferSizes_.get(i),
          WGLWidget.GLenum.UNSIGNED_SHORT,
          0);
      this.chart_.disableVertexAttribArray(this.position_vertexPosAttr_);
    }
    this.chart_.enable(WGLWidget.GLenum.CULL_FACE);
    this.chart_.disable(WGLWidget.GLenum.DEPTH_TEST);
  }

  private double rayTriangleIntersect(
      final javax.vecmath.GVector re,
      final javax.vecmath.GVector rd,
      final javax.vecmath.GVector camera,
      final javax.vecmath.GVector v0,
      final javax.vecmath.GVector v1,
      final javax.vecmath.GVector v2,
      final javax.vecmath.GVector point) {
    javax.vecmath.GVector e1 = new javax.vecmath.GVector(WebGLUtils.subtract(v1, v0));
    javax.vecmath.GVector e2 = new javax.vecmath.GVector(WebGLUtils.subtract(v2, v0));
    javax.vecmath.GVector tr = new javax.vecmath.GVector(WebGLUtils.subtract(re, v0));
    javax.vecmath.GVector P = WebGLUtils.cross(rd, e2);
    javax.vecmath.GVector Q = WebGLUtils.cross(tr, e1);
    double m = P.dot(e1);
    double t = Q.dot(e2) / m;
    if (t < 0) {
      return Double.POSITIVE_INFINITY;
    }
    double gamma = Q.dot(rd) / m;
    if (gamma < 0 || gamma > 1) {
      return Double.POSITIVE_INFINITY;
    }
    double beta = P.dot(tr) / m;
    if (beta < 0 || beta > 1 - gamma) {
      return Double.POSITIVE_INFINITY;
    }
    point.setElement(0, re.getElement(0) + rd.getElement(0) * t);
    point.setElement(1, re.getElement(1) + rd.getElement(1) * t);
    point.setElement(2, re.getElement(2) + rd.getElement(2) * t);
    double distance = new javax.vecmath.GVector(WebGLUtils.subtract(point, camera)).norm();
    return distance;
  }

  private void drawIsoLines() {
    this.chart_.useProgram(this.isoLineProgram_);
    this.chart_.depthFunc(WGLWidget.GLenum.LEQUAL);
    this.chart_.uniformMatrix4(this.isoLine_cMatrix_, this.chart_.getJsMatrix());
    this.chart_.lineWidth(1.0);
    this.chart_.activeTexture(WGLWidget.GLenum.TEXTURE0);
    this.chart_.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.isoLineColorMapTexture_);
    this.chart_.uniform1i(this.isoLine_TexSampler_, 0);
    for (int i = 0; i < this.isoLineHeights_.size(); ++i) {
      if (this.isoLineBufferSizes_.get(i) == 0) {
        continue;
      }
      this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.isoLineBuffers_.get(i));
      this.chart_.vertexAttribPointer(
          this.isoLineVertexPosAttr_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.chart_.enableVertexAttribArray(this.isoLineVertexPosAttr_);
      this.chart_.drawArrays(WGLWidget.GLenum.LINES, 0, this.isoLineBufferSizes_.get(i) / 3);
      this.chart_.disableVertexAttribArray(this.isoLineVertexPosAttr_);
    }
  }

  private void linesForIsoLevel(double z, final List<Float> result) {
    int Nx = this.getNbXPoints();
    int Ny = this.getNbYPoints();
    double minZ = this.chart_.axis(Axis.Z3D).getMinimum();
    double maxZ = this.chart_.axis(Axis.Z3D).getMaximum();
    double scaledZ = (z - minZ) / (maxZ - minZ);
    List<java.nio.ByteBuffer> simplePtsArrays = new ArrayList<java.nio.ByteBuffer>();
    int nbXaxisBuffers;
    int nbYaxisBuffers;
    nbXaxisBuffers = Nx / (SURFACE_SIDE_LIMIT - 1);
    nbYaxisBuffers = Ny / (SURFACE_SIDE_LIMIT - 1);
    if (Nx % (SURFACE_SIDE_LIMIT - 1) != 0) {
      nbXaxisBuffers++;
    }
    if (Ny % (SURFACE_SIDE_LIMIT - 1) != 0) {
      nbYaxisBuffers++;
    }
    for (int i = 0; i < nbXaxisBuffers - 1; i++) {
      for (int j = 0; j < nbYaxisBuffers - 1; j++) {
        simplePtsArrays.add(
            WebGLUtils.newByteBuffer(4 * (3 * SURFACE_SIDE_LIMIT * SURFACE_SIDE_LIMIT)));
      }
      ;
      simplePtsArrays.add(
          WebGLUtils.newByteBuffer(
              4
                  * (3
                      * SURFACE_SIDE_LIMIT
                      * (Ny - (nbYaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1)))));
    }
    for (int j = 0; j < nbYaxisBuffers - 1; j++) {
      simplePtsArrays.add(
          WebGLUtils.newByteBuffer(
              4
                  * (3
                      * (Nx - (nbXaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1))
                      * SURFACE_SIDE_LIMIT)));
    }
    simplePtsArrays.add(
        WebGLUtils.newByteBuffer(
            4
                * (3
                    * (Nx - (nbXaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1))
                    * (Ny - (nbYaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1)))));
    this.surfaceDataFromModel(simplePtsArrays);
    for (int i = 0; i < simplePtsArrays.size(); i++) {
      int Nx_patch = SURFACE_SIDE_LIMIT;
      int Ny_patch = SURFACE_SIDE_LIMIT;
      if ((i + 1) % nbYaxisBuffers == 0) {
        Ny_patch = Ny - (nbYaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1);
      }
      if ((int) i >= (nbXaxisBuffers - 1) * nbYaxisBuffers) {
        Nx_patch = Nx - (nbXaxisBuffers - 1) * (SURFACE_SIDE_LIMIT - 1);
      }
      java.nio.IntBuffer vertexIndices =
          java.nio.IntBuffer.allocate((Nx_patch - 1) * (Ny_patch + 1) * 2);
      this.generateVertexIndices(vertexIndices, Nx_patch, Ny_patch);
      for (int j = 0; j < vertexIndices.capacity() - 2; ++j) {
        if (vertexIndices.get(j) == vertexIndices.get(j + 1)
            || vertexIndices.get(j + 1) == vertexIndices.get(j + 2)
            || vertexIndices.get(j) == vertexIndices.get(j + 2)) {
          continue;
        }
        javax.vecmath.GVector a =
            new javax.vecmath.GVector(
                new double[] {
                  simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j) * 3)),
                  simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j) * 3 + 1)),
                  simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j) * 3 + 2))
                });
        javax.vecmath.GVector b =
            new javax.vecmath.GVector(
                new double[] {
                  simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 1) * 3)),
                  simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 1) * 3 + 1)),
                  simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 1) * 3 + 2))
                });
        javax.vecmath.GVector c =
            new javax.vecmath.GVector(
                new double[] {
                  simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 2) * 3)),
                  simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 2) * 3 + 1)),
                  simplePtsArrays.get(i).getFloat(4 * (vertexIndices.get(j + 2) * 3 + 2))
                });
        List<javax.vecmath.GVector> intersections = new ArrayList<javax.vecmath.GVector>();
        if (a.getElement(2) >= scaledZ && b.getElement(2) < scaledZ
            || a.getElement(2) < scaledZ && b.getElement(2) >= scaledZ) {
          double factor = (scaledZ - a.getElement(2)) / (b.getElement(2) - a.getElement(2));
          javax.vecmath.GVector d = new javax.vecmath.GVector(WebGLUtils.subtract(b, a));
          javax.vecmath.GVector e = new javax.vecmath.GVector(WebGLUtils.multiply(d, factor));
          javax.vecmath.GVector f = new javax.vecmath.GVector(WebGLUtils.add(a, e));
          intersections.add(f);
        }
        if (b.getElement(2) >= scaledZ && c.getElement(2) < scaledZ
            || b.getElement(2) < scaledZ && c.getElement(2) >= scaledZ) {
          double factor = (scaledZ - b.getElement(2)) / (c.getElement(2) - b.getElement(2));
          javax.vecmath.GVector d = new javax.vecmath.GVector(WebGLUtils.subtract(c, b));
          javax.vecmath.GVector e = new javax.vecmath.GVector(WebGLUtils.multiply(d, factor));
          javax.vecmath.GVector f = new javax.vecmath.GVector(WebGLUtils.add(b, e));
          intersections.add(f);
        }
        if (intersections.size() < 2
            && (c.getElement(2) >= scaledZ && a.getElement(2) < scaledZ
                || c.getElement(2) < scaledZ && a.getElement(2) >= scaledZ)) {
          double factor = (scaledZ - c.getElement(2)) / (a.getElement(2) - c.getElement(2));
          javax.vecmath.GVector d = new javax.vecmath.GVector(WebGLUtils.subtract(a, c));
          javax.vecmath.GVector e = new javax.vecmath.GVector(WebGLUtils.multiply(d, factor));
          javax.vecmath.GVector f = new javax.vecmath.GVector(WebGLUtils.add(c, e));
          intersections.add(f);
        }
        if (intersections.size() == 2) {
          result.add((float) intersections.get(0).getElement(0));
          result.add((float) intersections.get(0).getElement(1));
          result.add((float) intersections.get(0).getElement(2));
          result.add((float) intersections.get(1).getElement(0));
          result.add((float) intersections.get(1).getElement(1));
          result.add((float) intersections.get(1).getElement(2));
        }
      }
    }
  }

  private WGLWidget.Texture getIsoLineColorMapTexture() {
    WPaintDevice cpd = null;
    if (this.isoLineColorMap_ == null) {
      return this.getColorTexture();
    } else {
      cpd = this.chart_.createPaintDevice(new WLength(1), new WLength(1024));
      WPainter painter = new WPainter(cpd);
      this.isoLineColorMap_.createStrip(painter);
      painter.end();
    }
    WGLWidget.Texture tex = this.chart_.createTexture();
    this.chart_.bindTexture(WGLWidget.GLenum.TEXTURE_2D, tex);
    this.chart_.pixelStorei(WGLWidget.GLenum.UNPACK_FLIP_Y_WEBGL, 1);
    this.chart_.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D,
        0,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.UNSIGNED_BYTE,
        cpd);
    return tex;
  }

  private List<Integer> vertexPosBufferSizes_;
  private List<Integer> vertexPosBuffer2Sizes_;
  private List<Integer> indexBufferSizes_;
  private List<Integer> lineBufferSizes_;
  private List<Integer> indexBufferSizes2_;
  private List<Integer> lineBufferSizes2_;
  private List<Integer> isoLineBufferSizes_;
  private List<Double> isoLineHeights_;
  private WAbstractColorMap isoLineColorMap_;
  private List<WGLWidget.Buffer> vertexPosBuffers_;
  private List<WGLWidget.Buffer> vertexPosBuffers2_;
  private List<WGLWidget.Buffer> vertexSizeBuffers_;
  private List<WGLWidget.Buffer> vertexSizeBuffers2_;
  private List<WGLWidget.Buffer> vertexColorBuffers2_;
  private List<WGLWidget.Buffer> indexBuffers_;
  private List<WGLWidget.Buffer> indexBuffers2_;
  private List<WGLWidget.Buffer> overlayLinesBuffers_;
  private List<WGLWidget.Buffer> overlayLinesBuffers2_;
  private List<WGLWidget.Buffer> colormapTexBuffers_;
  private List<WGLWidget.Buffer> isoLineBuffers_;
  private List<WMemoryResource> binaryResources_;
  private WGLWidget.Shader fragShader_;
  private WGLWidget.Shader colFragShader_;
  private WGLWidget.Shader meshFragShader_;
  private WGLWidget.Shader singleColorFragShader_;
  private WGLWidget.Shader positionFragShader_;
  private WGLWidget.Shader isoLineFragShader_;
  private WGLWidget.Shader vertShader_;
  private WGLWidget.Shader colVertShader_;
  private WGLWidget.Shader meshVertShader_;
  private WGLWidget.Shader isoLineVertexShader_;
  private WGLWidget.Program seriesProgram_;
  private WGLWidget.Program colSeriesProgram_;
  private WGLWidget.Program meshProgram_;
  private WGLWidget.Program singleColorProgram_;
  private WGLWidget.Program positionProgram_;
  private WGLWidget.Program isoLineProgram_;
  private WGLWidget.AttribLocation vertexPosAttr_;
  private WGLWidget.AttribLocation vertexPosAttr2_;
  private WGLWidget.AttribLocation singleColor_vertexPosAttr_;
  private WGLWidget.AttribLocation position_vertexPosAttr_;
  private WGLWidget.AttribLocation meshVertexPosAttr_;
  private WGLWidget.AttribLocation isoLineVertexPosAttr_;
  private WGLWidget.AttribLocation vertexSizeAttr_;
  private WGLWidget.AttribLocation vertexSizeAttr2_;
  private WGLWidget.AttribLocation vertexColAttr2_;
  private WGLWidget.AttribLocation barTexCoordAttr_;
  private WGLWidget.UniformLocation mvMatrixUniform_;
  private WGLWidget.UniformLocation mvMatrixUniform2_;
  private WGLWidget.UniformLocation singleColor_mvMatrixUniform_;
  private WGLWidget.UniformLocation position_mvMatrixUniform_;
  private WGLWidget.UniformLocation mesh_mvMatrixUniform_;
  private WGLWidget.UniformLocation isoLine_mvMatrixUniform_;
  private WGLWidget.UniformLocation pMatrix_;
  private WGLWidget.UniformLocation pMatrix2_;
  private WGLWidget.UniformLocation singleColor_pMatrix_;
  private WGLWidget.UniformLocation position_pMatrix_;
  private WGLWidget.UniformLocation mesh_pMatrix_;
  private WGLWidget.UniformLocation isoLine_pMatrix_;
  private WGLWidget.UniformLocation cMatrix_;
  private WGLWidget.UniformLocation cMatrix2_;
  private WGLWidget.UniformLocation singleColor_cMatrix_;
  private WGLWidget.UniformLocation position_cMatrix_;
  private WGLWidget.UniformLocation mesh_cMatrix_;
  private WGLWidget.UniformLocation isoLine_cMatrix_;
  private WGLWidget.UniformLocation TexSampler_;
  private WGLWidget.UniformLocation isoLine_TexSampler_;
  private WGLWidget.UniformLocation mesh_colorUniform_;
  private WGLWidget.UniformLocation singleColorUniform_;
  private WGLWidget.UniformLocation pointSpriteUniform_;
  private WGLWidget.UniformLocation pointSpriteUniform2_;
  private WGLWidget.UniformLocation vpHeightUniform_;
  private WGLWidget.UniformLocation vpHeightUniform2_;
  private WGLWidget.UniformLocation offset_;
  private WGLWidget.UniformLocation isoLine_offset_;
  private WGLWidget.UniformLocation scaleFactor_;
  private WGLWidget.UniformLocation isoLine_scaleFactor_;
  private WGLWidget.UniformLocation minPtUniform_;
  private WGLWidget.UniformLocation mesh_minPtUniform_;
  private WGLWidget.UniformLocation singleColor_minPtUniform_;
  private WGLWidget.UniformLocation position_minPtUniform_;
  private WGLWidget.UniformLocation maxPtUniform_;
  private WGLWidget.UniformLocation mesh_maxPtUniform_;
  private WGLWidget.UniformLocation singleColor_maxPtUniform_;
  private WGLWidget.UniformLocation position_maxPtUniform_;
  private WGLWidget.UniformLocation dataMinPtUniform_;
  private WGLWidget.UniformLocation mesh_dataMinPtUniform_;
  private WGLWidget.UniformLocation singleColor_dataMinPtUniform_;
  private WGLWidget.UniformLocation position_dataMinPtUniform_;
  private WGLWidget.UniformLocation dataMaxPtUniform_;
  private WGLWidget.UniformLocation mesh_dataMaxPtUniform_;
  private WGLWidget.UniformLocation singleColor_dataMaxPtUniform_;
  private WGLWidget.UniformLocation position_dataMaxPtUniform_;
  private WGLWidget.UniformLocation singleColor_marginUniform_;
  private WGLWidget.UniformLocation position_marginUniform_;
  private WGLWidget.Texture colormapTexture_;
  private WGLWidget.Texture isoLineColorMapTexture_;
  private WGLWidget.Texture pointSpriteTexture_;
  private List<Float> minPt_;
  private List<Float> maxPt_;
  WGLWidget.JavaScriptVector jsMinPt_;
  WGLWidget.JavaScriptVector jsMaxPt_;
  private boolean minPtChanged_;
  private boolean maxPtChanged_;
  private JSlot changeClippingMinX_;
  private JSlot changeClippingMaxX_;
  private JSlot changeClippingMinY_;
  private JSlot changeClippingMaxY_;
  private JSlot changeClippingMinZ_;
  private JSlot changeClippingMaxZ_;
  private boolean clippingLinesEnabled_;
  private WColor clippingLinesColor_;
  private static final String barFragShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\nvarying vec2 vTextureCoord;\nvarying vec3 vPos;\n\nuniform sampler2D uSampler;\n\nvoid main(void) {\n  if (any(lessThan(vPos, vec3(0.0, 0.0, 0.0))) ||      any(greaterThan(vPos, vec3(1.0, 1.0, 1.0)))) {\n    discard;\n  }\n  gl_FragColor = texture2D(uSampler, vec2(vTextureCoord.s, vTextureCoord.t) );\n}\n";
  private static final String barVertexShaderSrc =
      "attribute vec3 aVertexPosition;\nattribute vec2 aTextureCoord;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec2 vTextureCoord;\nvarying vec3 vPos;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vTextureCoord = aTextureCoord;\n  vPos = aVertexPosition;\n}\n";
  private static final String colBarFragShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\nvarying vec3 vPos;\nvarying vec4 vColor;\n\nvoid main(void) {\n  if (any(lessThan(vPos, vec3(0.0, 0.0, 0.0))) ||      any(greaterThan(vPos, vec3(1.0, 1.0, 1.0)))) {\n    discard;\n  }\n  gl_FragColor = vColor;\n}\n";
  private static final String colBarVertexShaderSrc =
      "attribute vec3 aVertexPosition;\nattribute vec4 aVertexColor;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec4 vColor;\nvarying vec3 vPos;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vColor = aVertexColor/255.0;\n  vPos = aVertexPosition;\n}\n";
  private static final String ptFragShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec3 vPos;\n\nuniform sampler2D uSampler;\nuniform sampler2D uPointSprite;\nuniform float uOffset;\nuniform float uScaleFactor;\nuniform float uVPHeight;\n\nvoid main(void) {\n  if (any(lessThan(vPos, vec3(0.0, 0.0, 0.0))) ||      any(greaterThan(vPos, vec3(1.0, 1.0, 1.0)))) {\n    discard;\n  }\n  vec2 texCoord = gl_PointCoord - vec2(0.0, 1.0 / uVPHeight) * 0.50;\n  texCoord.y = 1.0 - texCoord.y;\n  if (texture2D(uPointSprite, texCoord).w < 0.5) {\n    discard;\n  }\n  gl_FragColor = texture2D(uSampler, vec2(0.0, uScaleFactor * (vPos.z - uOffset) ) );\n}\n";
  private static final String ptVertexShaderSrc =
      "attribute vec3 aVertexPosition;\nattribute float aPointSize;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec3 vPos;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vPos = aVertexPosition;\n  gl_PointSize = aPointSize;\n}\n";
  private static final String colPtFragShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\nuniform sampler2D uPointSprite;\nvarying vec4 vColor;\nvarying vec3 vPos;\nuniform float uVPHeight;\n\nvoid main(void) {\n  if (any(lessThan(vPos, vec3(0.0, 0.0, 0.0))) ||      any(greaterThan(vPos, vec3(1.0, 1.0, 1.0)))) {\n    discard;\n  }\n  vec2 texCoord = gl_PointCoord - vec2(0.0, 1.0 / uVPHeight) * 0.25;\n  texCoord.y = 1.0 - texCoord.y;\n  if (texture2D(uPointSprite, texCoord).w < 0.5) {\n    discard;\n  }\n  gl_FragColor = vColor;\n}\n";
  private static final String colPtVertexShaderSrc =
      "attribute vec3 aVertexPosition;\nattribute float aPointSize;\nattribute vec4 aColor;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec4 vColor;\nvarying vec3 vPos;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vColor = aColor/255.0;\n  vPos = aVertexPosition;\n  gl_PointSize = aPointSize;\n}\n";
  private static final String surfFragShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec3 vPos;\n\nuniform sampler2D uSampler;\nuniform float uOffset;\nuniform float uScaleFactor;\nuniform vec3 uMinPt;\nuniform vec3 uMaxPt;\nuniform vec3 uDataMinPt;\nuniform vec3 uDataMaxPt;\n\nvoid main(void) {\n  vec3 minPt = max((uMinPt - uDataMinPt) / (uDataMaxPt - uDataMinPt), vec3(0.0));\n  vec3 maxPt = min((uMaxPt - uDataMinPt) / (uDataMaxPt - uDataMinPt), vec3(1.0));\n  if (any(lessThan(vPos, minPt)) ||      any(greaterThan(vPos, maxPt))) {\n    discard;\n  }\n  gl_FragColor = texture2D(uSampler, vec2(0.0, uScaleFactor * (vPos.z - uOffset) ) );\n}\n";
  private static final String surfFragSingleColorShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec3 vPos;\n\nuniform vec3 uMargin;\nuniform vec3 uColor;\nuniform vec3 uMinPt;\nuniform vec3 uMaxPt;\nuniform vec3 uDataMinPt;\nuniform vec3 uDataMaxPt;\n\nvoid main(void) {\n  vec3 minPt = max((uMinPt - uDataMinPt) / (uDataMaxPt - uDataMinPt), vec3(0.0));\n  vec3 maxPt = min((uMaxPt - uDataMinPt) / (uDataMaxPt - uDataMinPt), vec3(1.0));\n  minPt = minPt - uMargin;\n  maxPt = maxPt + uMargin;\n  if (any(lessThan(vPos, minPt)) ||      any(greaterThan(vPos, maxPt))) {\n    discard;\n  }\n  if (any(lessThan(vPos, minPt + uMargin / 2.0)) ||        any(greaterThan(vPos, maxPt - uMargin / 2.0))) {\n      gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);\n  } else {\n    gl_FragColor = vec4(uColor, 1.0);\n  }\n}\n";
  private static final String surfFragPosShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec3 vPos;\n\nuniform vec3 uMargin;\nuniform vec3 uMinPt;\nuniform vec3 uMaxPt;\nuniform vec3 uDataMinPt;\nuniform vec3 uDataMaxPt;\n\nvoid main(void) {\n  vec3 minPt = max((uMinPt - uDataMinPt) / (uDataMaxPt - uDataMinPt), vec3(0.0));\n  vec3 maxPt = min((uMaxPt - uDataMinPt) / (uDataMaxPt - uDataMinPt), vec3(1.0));\n  minPt = minPt - uMargin;\n  maxPt = maxPt + uMargin;\n  if (any(lessThan(vPos, minPt)) ||      any(greaterThan(vPos, maxPt))) {\n    discard;\n  }\n  gl_FragColor = vec4(vPos, 1.0);\n}\n";
  private static final String surfVertexShaderSrc =
      "attribute vec3 aVertexPosition;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec3 vPos;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vPos = aVertexPosition;\n}\n";
  private static final String meshFragShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec3 vPos;\n\nuniform vec4 uColor;\nuniform vec3 uMinPt;\nuniform vec3 uMaxPt;\nuniform vec3 uDataMinPt;\nuniform vec3 uDataMaxPt;\n\nvoid main(void) {\n  vec3 minPt = max((uMinPt - uDataMinPt) / (uDataMaxPt - uDataMinPt), vec3(0.0));\n  vec3 maxPt = min((uMaxPt - uDataMinPt) / (uDataMaxPt - uDataMinPt), vec3(1.0));\n  if (any(lessThan(vPos, minPt)) ||      any(greaterThan(vPos, maxPt))) {\n    discard;\n  }\n  gl_FragColor = uColor/255.0;\n}\n";
  private static final String meshVertexShaderSrc =
      "attribute vec3 aVertexPosition;\n\nvarying vec3 vPos;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vPos = aVertexPosition;\n}\n";
  private static final String isoLineFragShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nuniform sampler2D uSampler;\nuniform float uOffset;\nuniform float uScaleFactor;\n\nvarying vec3 vPos;\n\nvoid main(void) {\n  if (any(lessThan(vPos.xy, vec2(0.0, 0.0))) ||      any(greaterThan(vPos.xy, vec2(1.0, 1.0)))) {\n    discard;\n  }\n  gl_FragColor = texture2D(uSampler, vec2(0.0, uScaleFactor * (vPos.z - uOffset) ) );\n}\n";
  private static final String isoLineVertexShaderSrc =
      "attribute vec3 aVertexPosition;\n\nvarying vec3 vPos;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvoid main(void) {\n  float z = (uCMatrix[1][2] > 0.0) ? 0.0 : 1.0;\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition.xy, z, 1.0);\n  vPos = aVertexPosition;\n}\n";

  static void push(final java.nio.ByteBuffer vec, float x, float y, float z) {
    vec.putFloat(x);
    vec.putFloat(y);
    vec.putFloat(z);
  }

  static void push(final java.nio.IntBuffer vec, int x, int y, int z) {
    vec.put(x);
    vec.put(y);
    vec.put(z);
  }

  static int axisToIndex(Axis axis) {
    if (axis == Axis.X3D) {
      return 0;
    } else {
      if (axis == Axis.Y3D) {
        return 1;
      } else {
        if (axis == Axis.Z3D) {
          return 2;
        } else {
          throw new WException("Invalid axis for 3D chart");
        }
      }
    }
  }
}
