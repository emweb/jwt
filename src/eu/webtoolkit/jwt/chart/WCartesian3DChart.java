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
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A 3D Cartesian chart.
 *
 * <p>The chart consists of a plotcube, which is always open on the front, and adapts to the data
 * which is shown on the chart. The plotcube has three axes of type {@link WAxis}. Each of these can
 * be manually configured as in the 2D case. The chart can be either a {@link ChartType#Scatter} or
 * a {@link ChartType#Category}. This influences how the data is positioned in relation to the
 * x/y-axis. Gridlines can also be drawn on each of the plotcube-planes. The chart has a
 * mouse-handler which allows rotation of the chart around the center of the plotcube. Zooming in
 * and out is possible by scrolling.
 *
 * <p>Data that can be shown on the chart derives from {@link WAbstractDataSeries3D}. Multiple
 * dataseries can be added to the chart using {@link
 * WCartesian3DChart#addDataSeries(WAbstractDataSeries3D dataseries_) addDataSeries()}. The color of
 * the dataseries is by default determined by the colors of the {@link WChartPalette}. This way a
 * separate color is assigned to each new dataseries. All rendering logic of the data is contained
 * in the dataseries-classes and further styling is often possible there. For example, a {@link
 * WAbstractColorMap} can be added to a dataseries, which will assign a color to datapoints based on
 * their z-value. More information on this is found in the documentation of {@link
 * WAbstractDataSeries3D}.
 *
 * <p>It is possible to assign a title to the chart. A legend can also be shown that lists the
 * titles of all dataseries (unless disabled in the dataseries itself). The legend position and
 * style can be configured. In addition to title and legend, a colormap-legend is shown for every
 * dataseries which has a colormap enabled and indicates that it should be displayed on the chart.
 *
 * <p><div align="center"> <img src="doc-files/Chart3DCombo.png">
 *
 * <p><strong>A scatterplot on the left, a category-chart on the right.</strong> </div>
 */
public class WCartesian3DChart extends WGLWidget {
  private static Logger logger = LoggerFactory.getLogger(WCartesian3DChart.class);

  /**
   * An invisible intersection plane.
   *
   * <p>Describes an invisible intersection plane, with the axis it is perpendicular to, its
   * position and the color of the intersection.
   */
  public static class IntersectionPlane {
    private static Logger logger = LoggerFactory.getLogger(IntersectionPlane.class);

    public Axis axis;
    public double position;
    public WColor color;
    /**
     * Constructor.
     *
     * <p>Create an intersection plane perpendicular to the given axis, at the given position on
     * that axis, and the color that the intersection lines should have.
     *
     * <p>
     *
     * @see WCartesian3DChart#setIntersectionPlanes(List intersectionPlanes)
     */
    public IntersectionPlane(Axis axis, double position, WColor col) {
      this.axis = axis;
      this.position = position;
      this.color = col;
    }
  }
  /**
   * Constructor.
   *
   * <p>Constructs a cartesian 3D chart, with the type set to {@link ChartType#Scatter}, a
   * transparent background, a {@link PaletteFlavour#Muted} palette and no gridlines.
   */
  public WCartesian3DChart(WContainerWidget parentContainer) {
    super();
    this.worldTransform_ =
        new javax.vecmath.Matrix4f(
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f);
    this.isViewSet_ = false;
    this.dataSeriesVector_ = new ArrayList<WAbstractDataSeries3D>();
    this.xAxis_ = new WAxis();
    this.yAxis_ = new WAxis();
    this.zAxis_ = new WAxis();
    this.chartType_ = ChartType.Scatter;
    this.cubeLinesPen_ = new WPen();
    this.gridLinesPen_ = new WPen();
    this.background_ = new WColor(StandardColor.White);
    this.chartPalette_ = new WStandardPalette(PaletteFlavour.Muted);
    this.title_ = new WString();
    this.titleFont_ = new WFont();
    this.legend_ = new WLegend3D();
    this.interface_ = new WChart3DImplementation(this);
    this.axisRenderWidth_ = 1024;
    this.axisRenderHeight_ = 256;
    this.gridRenderWidth_ = 512;
    this.textureScaling_ = 0;
    this.seriesCounter_ = 0;
    this.currentTopOffset_ = 0;
    this.currentBottomOffset_ = 0;
    this.currentLeftOffset_ = 0;
    this.currentRightOffset_ = 0;
    this.updates_ = EnumSet.noneOf(ChartUpdates.class);
    this.intersectionLinesEnabled_ = false;
    this.intersectionLinesColor_ = new WColor();
    this.intersectionPlanes_ = new ArrayList<WCartesian3DChart.IntersectionPlane>();
    this.fragmentShader_ = new WGLWidget.Shader();
    this.vertexShader_ = new WGLWidget.Shader();
    this.fragmentShader2_ = new WGLWidget.Shader();
    this.vertexShader2_ = new WGLWidget.Shader();
    this.cubeLineFragShader_ = new WGLWidget.Shader();
    this.cubeLineVertShader_ = new WGLWidget.Shader();
    this.vertexShader2D_ = new WGLWidget.Shader();
    this.fragmentShader2D_ = new WGLWidget.Shader();
    this.intersectionLinesFragmentShader_ = new WGLWidget.Shader();
    this.clippingPlaneFragShader_ = new WGLWidget.Shader();
    this.clippingPlaneVertexShader_ = new WGLWidget.Shader();
    this.cubeProgram_ = new WGLWidget.Program();
    this.cubeLineProgram_ = new WGLWidget.Program();
    this.axisProgram_ = new WGLWidget.Program();
    this.textureProgram_ = new WGLWidget.Program();
    this.intersectionLinesProgram_ = new WGLWidget.Program();
    this.clippingPlaneProgram_ = new WGLWidget.Program();
    this.cube_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.cube_planeNormalAttribute_ = new WGLWidget.AttribLocation();
    this.cube_textureCoordAttribute_ = new WGLWidget.AttribLocation();
    this.cubeLine_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.cubeLine_normalAttribute_ = new WGLWidget.AttribLocation();
    this.cube_pMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cube_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cube_cMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cube_texSampler1Uniform_ = new WGLWidget.UniformLocation();
    this.cube_texSampler2Uniform_ = new WGLWidget.UniformLocation();
    this.cube_texSampler3Uniform_ = new WGLWidget.UniformLocation();
    this.cubeLine_pMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cubeLine_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cubeLine_cMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cubeLine_nMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cubeLine_colorUniform_ = new WGLWidget.UniformLocation();
    this.axis_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.axis_textureCoordAttribute_ = new WGLWidget.AttribLocation();
    this.axis_inPlaneAttribute_ = new WGLWidget.AttribLocation();
    this.axis_planeNormalAttribute_ = new WGLWidget.AttribLocation();
    this.axis_outOfPlaneNormalAttribute_ = new WGLWidget.AttribLocation();
    this.axis_pMatrixUniform_ = new WGLWidget.UniformLocation();
    this.axis_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.axis_cMatrixUniform_ = new WGLWidget.UniformLocation();
    this.axis_nMatrixUniform_ = new WGLWidget.UniformLocation();
    this.axis_normalAngleTextureUniform_ = new WGLWidget.UniformLocation();
    this.axis_texSamplerUniform_ = new WGLWidget.UniformLocation();
    this.texture_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.texture_vertexTextureCoAttribute_ = new WGLWidget.AttribLocation();
    this.texture_texSamplerUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.intersectionLines_vertexTextureCoAttribute_ = new WGLWidget.AttribLocation();
    this.intersectionLines_cameraUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_viewportWidthUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_viewportHeightUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_positionSamplerUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_colorUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_meshIndexSamplerUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.clippingPlane_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_pMatrixUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_cMatrixUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_clipPtUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_dataMinPtUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_dataMaxPtUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_clippingAxis_ = new WGLWidget.UniformLocation();
    this.clippingPlane_drawPositionUniform_ = new WGLWidget.UniformLocation();
    this.pMatrix_ =
        new javax.vecmath.Matrix4f(
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f);
    this.jsMatrix_ = new WGLWidget.JavaScriptMatrix4x4();
    this.cubeData_ = WebGLUtils.newByteBuffer(0);
    this.cubeNormalsData_ = WebGLUtils.newByteBuffer(0);
    this.cubeIndices_ = java.nio.IntBuffer.allocate(0);
    this.cubeTexCo_ = WebGLUtils.newByteBuffer(0);
    this.cubeLineIndices_ = java.nio.IntBuffer.allocate(0);
    this.axisSlabData_ = WebGLUtils.newByteBuffer(0);
    this.axisSlabIndices_ = java.nio.IntBuffer.allocate(0);
    this.axisInPlaneBools_ = WebGLUtils.newByteBuffer(0);
    this.axisPlaneNormal_ = WebGLUtils.newByteBuffer(0);
    this.axisOutOfPlaneNormal_ = WebGLUtils.newByteBuffer(0);
    this.axisTexCo_ = WebGLUtils.newByteBuffer(0);
    this.axisSlabDataVert_ = WebGLUtils.newByteBuffer(0);
    this.axisSlabIndicesVert_ = java.nio.IntBuffer.allocate(0);
    this.axisInPlaneBoolsVert_ = WebGLUtils.newByteBuffer(0);
    this.axisPlaneNormalVert_ = WebGLUtils.newByteBuffer(0);
    this.axisOutOfPlaneNormalVert_ = WebGLUtils.newByteBuffer(0);
    this.axisTexCoVert_ = WebGLUtils.newByteBuffer(0);
    this.cubeBuffer_ = new WGLWidget.Buffer();
    this.cubeNormalsBuffer_ = new WGLWidget.Buffer();
    this.cubeIndicesBuffer_ = new WGLWidget.Buffer();
    this.cubeLineNormalsBuffer_ = new WGLWidget.Buffer();
    this.cubeLineIndicesBuffer_ = new WGLWidget.Buffer();
    this.cubeTexCoords_ = new WGLWidget.Buffer();
    this.axisBuffer_ = new WGLWidget.Buffer();
    this.axisIndicesBuffer_ = new WGLWidget.Buffer();
    this.axisInPlaneBuffer_ = new WGLWidget.Buffer();
    this.axisPlaneNormalBuffer_ = new WGLWidget.Buffer();
    this.axisOutOfPlaneNormalBuffer_ = new WGLWidget.Buffer();
    this.axisVertBuffer_ = new WGLWidget.Buffer();
    this.axisIndicesVertBuffer_ = new WGLWidget.Buffer();
    this.axisInPlaneVertBuffer_ = new WGLWidget.Buffer();
    this.axisPlaneNormalVertBuffer_ = new WGLWidget.Buffer();
    this.axisOutOfPlaneNormalVertBuffer_ = new WGLWidget.Buffer();
    this.axisTexCoordsHoriz_ = new WGLWidget.Buffer();
    this.axisTexCoordsVert_ = new WGLWidget.Buffer();
    this.overlayPosBuffer_ = new WGLWidget.Buffer();
    this.overlayTexCoBuffer_ = new WGLWidget.Buffer();
    this.clippingPlaneVertBuffer_ = new WGLWidget.Buffer();
    this.horizAxisTexture_ = new WGLWidget.Texture();
    this.horizAxisTexture2_ = new WGLWidget.Texture();
    this.vertAxisTexture_ = new WGLWidget.Texture();
    this.cubeTextureXY_ = new WGLWidget.Texture();
    this.cubeTextureXZ_ = new WGLWidget.Texture();
    this.cubeTextureYZ_ = new WGLWidget.Texture();
    this.titleTexture_ = new WGLWidget.Texture();
    this.legendTexture_ = new WGLWidget.Texture();
    this.colorMapTexture_ = new WGLWidget.Texture();
    this.meshIndexTexture_ = new WGLWidget.Texture();
    this.positionTexture_ = new WGLWidget.Texture();
    this.intersectionLinesTexture_ = new WGLWidget.Texture();
    this.meshIndexFramebuffer_ = new WGLWidget.Framebuffer();
    this.positionFramebuffer_ = new WGLWidget.Framebuffer();
    this.intersectionLinesFramebuffer_ = new WGLWidget.Framebuffer();
    this.offscreenDepthbuffer_ = new WGLWidget.Renderbuffer();
    this.objectsToDelete = new ArrayList<Object>();
    this.XYGridEnabled_[0] = false;
    this.XYGridEnabled_[1] = false;
    this.XZGridEnabled_[0] = false;
    this.XZGridEnabled_[1] = false;
    this.YZGridEnabled_[0] = false;
    this.YZGridEnabled_[1] = false;
    this.xAxis_.init(this.interface_, Axis.X3D);
    this.yAxis_.init(this.interface_, Axis.Y3D);
    this.zAxis_.init(this.interface_, Axis.Z3D);
    this.titleFont_.setFamily(FontFamily.SansSerif);
    this.titleFont_.setSize(new WLength(15, LengthUnit.Point));
    this.addJavaScriptMatrix4(this.jsMatrix_);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #WCartesian3DChart(WContainerWidget parentContainer)
   * this((WContainerWidget)null)}
   */
  public WCartesian3DChart() {
    this((WContainerWidget) null);
  }
  /**
   * Constructor.
   *
   * <p>Construct a cartesian 3D chart with the specified type, a transparent background, a {@link
   * PaletteFlavour#Muted} palette and no gridlines.
   */
  public WCartesian3DChart(ChartType type, WContainerWidget parentContainer) {
    super();
    this.worldTransform_ =
        new javax.vecmath.Matrix4f(
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f);
    this.isViewSet_ = false;
    this.dataSeriesVector_ = new ArrayList<WAbstractDataSeries3D>();
    this.xAxis_ = new WAxis();
    this.yAxis_ = new WAxis();
    this.zAxis_ = new WAxis();
    this.chartType_ = type;
    this.cubeLinesPen_ = new WPen();
    this.gridLinesPen_ = new WPen();
    this.background_ = new WColor(StandardColor.White);
    this.chartPalette_ = new WStandardPalette(PaletteFlavour.Muted);
    this.title_ = new WString();
    this.titleFont_ = new WFont();
    this.legend_ = new WLegend3D();
    this.interface_ = new WChart3DImplementation(this);
    this.axisRenderWidth_ = 1024;
    this.axisRenderHeight_ = 256;
    this.gridRenderWidth_ = 512;
    this.textureScaling_ = 0;
    this.seriesCounter_ = 0;
    this.currentTopOffset_ = 0;
    this.currentBottomOffset_ = 0;
    this.currentLeftOffset_ = 0;
    this.currentRightOffset_ = 0;
    this.updates_ = EnumSet.noneOf(ChartUpdates.class);
    this.intersectionLinesEnabled_ = false;
    this.intersectionLinesColor_ = new WColor();
    this.intersectionPlanes_ = new ArrayList<WCartesian3DChart.IntersectionPlane>();
    this.fragmentShader_ = new WGLWidget.Shader();
    this.vertexShader_ = new WGLWidget.Shader();
    this.fragmentShader2_ = new WGLWidget.Shader();
    this.vertexShader2_ = new WGLWidget.Shader();
    this.cubeLineFragShader_ = new WGLWidget.Shader();
    this.cubeLineVertShader_ = new WGLWidget.Shader();
    this.vertexShader2D_ = new WGLWidget.Shader();
    this.fragmentShader2D_ = new WGLWidget.Shader();
    this.intersectionLinesFragmentShader_ = new WGLWidget.Shader();
    this.clippingPlaneFragShader_ = new WGLWidget.Shader();
    this.clippingPlaneVertexShader_ = new WGLWidget.Shader();
    this.cubeProgram_ = new WGLWidget.Program();
    this.cubeLineProgram_ = new WGLWidget.Program();
    this.axisProgram_ = new WGLWidget.Program();
    this.textureProgram_ = new WGLWidget.Program();
    this.intersectionLinesProgram_ = new WGLWidget.Program();
    this.clippingPlaneProgram_ = new WGLWidget.Program();
    this.cube_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.cube_planeNormalAttribute_ = new WGLWidget.AttribLocation();
    this.cube_textureCoordAttribute_ = new WGLWidget.AttribLocation();
    this.cubeLine_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.cubeLine_normalAttribute_ = new WGLWidget.AttribLocation();
    this.cube_pMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cube_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cube_cMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cube_texSampler1Uniform_ = new WGLWidget.UniformLocation();
    this.cube_texSampler2Uniform_ = new WGLWidget.UniformLocation();
    this.cube_texSampler3Uniform_ = new WGLWidget.UniformLocation();
    this.cubeLine_pMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cubeLine_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cubeLine_cMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cubeLine_nMatrixUniform_ = new WGLWidget.UniformLocation();
    this.cubeLine_colorUniform_ = new WGLWidget.UniformLocation();
    this.axis_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.axis_textureCoordAttribute_ = new WGLWidget.AttribLocation();
    this.axis_inPlaneAttribute_ = new WGLWidget.AttribLocation();
    this.axis_planeNormalAttribute_ = new WGLWidget.AttribLocation();
    this.axis_outOfPlaneNormalAttribute_ = new WGLWidget.AttribLocation();
    this.axis_pMatrixUniform_ = new WGLWidget.UniformLocation();
    this.axis_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.axis_cMatrixUniform_ = new WGLWidget.UniformLocation();
    this.axis_nMatrixUniform_ = new WGLWidget.UniformLocation();
    this.axis_normalAngleTextureUniform_ = new WGLWidget.UniformLocation();
    this.axis_texSamplerUniform_ = new WGLWidget.UniformLocation();
    this.texture_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.texture_vertexTextureCoAttribute_ = new WGLWidget.AttribLocation();
    this.texture_texSamplerUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.intersectionLines_vertexTextureCoAttribute_ = new WGLWidget.AttribLocation();
    this.intersectionLines_cameraUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_viewportWidthUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_viewportHeightUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_positionSamplerUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_colorUniform_ = new WGLWidget.UniformLocation();
    this.intersectionLines_meshIndexSamplerUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_vertexPositionAttribute_ = new WGLWidget.AttribLocation();
    this.clippingPlane_mvMatrixUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_pMatrixUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_cMatrixUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_clipPtUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_dataMinPtUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_dataMaxPtUniform_ = new WGLWidget.UniformLocation();
    this.clippingPlane_clippingAxis_ = new WGLWidget.UniformLocation();
    this.clippingPlane_drawPositionUniform_ = new WGLWidget.UniformLocation();
    this.pMatrix_ =
        new javax.vecmath.Matrix4f(
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f);
    this.jsMatrix_ = new WGLWidget.JavaScriptMatrix4x4();
    this.cubeData_ = WebGLUtils.newByteBuffer(0);
    this.cubeNormalsData_ = WebGLUtils.newByteBuffer(0);
    this.cubeIndices_ = java.nio.IntBuffer.allocate(0);
    this.cubeTexCo_ = WebGLUtils.newByteBuffer(0);
    this.cubeLineIndices_ = java.nio.IntBuffer.allocate(0);
    this.axisSlabData_ = WebGLUtils.newByteBuffer(0);
    this.axisSlabIndices_ = java.nio.IntBuffer.allocate(0);
    this.axisInPlaneBools_ = WebGLUtils.newByteBuffer(0);
    this.axisPlaneNormal_ = WebGLUtils.newByteBuffer(0);
    this.axisOutOfPlaneNormal_ = WebGLUtils.newByteBuffer(0);
    this.axisTexCo_ = WebGLUtils.newByteBuffer(0);
    this.axisSlabDataVert_ = WebGLUtils.newByteBuffer(0);
    this.axisSlabIndicesVert_ = java.nio.IntBuffer.allocate(0);
    this.axisInPlaneBoolsVert_ = WebGLUtils.newByteBuffer(0);
    this.axisPlaneNormalVert_ = WebGLUtils.newByteBuffer(0);
    this.axisOutOfPlaneNormalVert_ = WebGLUtils.newByteBuffer(0);
    this.axisTexCoVert_ = WebGLUtils.newByteBuffer(0);
    this.cubeBuffer_ = new WGLWidget.Buffer();
    this.cubeNormalsBuffer_ = new WGLWidget.Buffer();
    this.cubeIndicesBuffer_ = new WGLWidget.Buffer();
    this.cubeLineNormalsBuffer_ = new WGLWidget.Buffer();
    this.cubeLineIndicesBuffer_ = new WGLWidget.Buffer();
    this.cubeTexCoords_ = new WGLWidget.Buffer();
    this.axisBuffer_ = new WGLWidget.Buffer();
    this.axisIndicesBuffer_ = new WGLWidget.Buffer();
    this.axisInPlaneBuffer_ = new WGLWidget.Buffer();
    this.axisPlaneNormalBuffer_ = new WGLWidget.Buffer();
    this.axisOutOfPlaneNormalBuffer_ = new WGLWidget.Buffer();
    this.axisVertBuffer_ = new WGLWidget.Buffer();
    this.axisIndicesVertBuffer_ = new WGLWidget.Buffer();
    this.axisInPlaneVertBuffer_ = new WGLWidget.Buffer();
    this.axisPlaneNormalVertBuffer_ = new WGLWidget.Buffer();
    this.axisOutOfPlaneNormalVertBuffer_ = new WGLWidget.Buffer();
    this.axisTexCoordsHoriz_ = new WGLWidget.Buffer();
    this.axisTexCoordsVert_ = new WGLWidget.Buffer();
    this.overlayPosBuffer_ = new WGLWidget.Buffer();
    this.overlayTexCoBuffer_ = new WGLWidget.Buffer();
    this.clippingPlaneVertBuffer_ = new WGLWidget.Buffer();
    this.horizAxisTexture_ = new WGLWidget.Texture();
    this.horizAxisTexture2_ = new WGLWidget.Texture();
    this.vertAxisTexture_ = new WGLWidget.Texture();
    this.cubeTextureXY_ = new WGLWidget.Texture();
    this.cubeTextureXZ_ = new WGLWidget.Texture();
    this.cubeTextureYZ_ = new WGLWidget.Texture();
    this.titleTexture_ = new WGLWidget.Texture();
    this.legendTexture_ = new WGLWidget.Texture();
    this.colorMapTexture_ = new WGLWidget.Texture();
    this.meshIndexTexture_ = new WGLWidget.Texture();
    this.positionTexture_ = new WGLWidget.Texture();
    this.intersectionLinesTexture_ = new WGLWidget.Texture();
    this.meshIndexFramebuffer_ = new WGLWidget.Framebuffer();
    this.positionFramebuffer_ = new WGLWidget.Framebuffer();
    this.intersectionLinesFramebuffer_ = new WGLWidget.Framebuffer();
    this.offscreenDepthbuffer_ = new WGLWidget.Renderbuffer();
    this.objectsToDelete = new ArrayList<Object>();
    this.XYGridEnabled_[0] = false;
    this.XYGridEnabled_[1] = false;
    this.XZGridEnabled_[0] = false;
    this.XZGridEnabled_[1] = false;
    this.YZGridEnabled_[0] = false;
    this.YZGridEnabled_[1] = false;
    this.xAxis_.init(this.interface_, Axis.X3D);
    this.yAxis_.init(this.interface_, Axis.Y3D);
    this.zAxis_.init(this.interface_, Axis.Z3D);
    this.titleFont_.setFamily(FontFamily.SansSerif);
    this.titleFont_.setSize(new WLength(15, LengthUnit.Point));
    this.addJavaScriptMatrix4(this.jsMatrix_);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #WCartesian3DChart(ChartType type, WContainerWidget parentContainer) this(type,
   * (WContainerWidget)null)}
   */
  public WCartesian3DChart(ChartType type) {
    this(type, (WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {
    super.remove();
  }
  /**
   * Add a dataseries to the chart.
   *
   * <p>If the chart is of type {@link ChartType#Scatter} only numerical dataseries should be added
   * and if it is of type {@link ChartType#Category} only categorical dataseries should be added. If
   * multiple categorical datasets are added, the axis-labels of the first dataseries will be used
   * on the chart.
   *
   * <p>
   *
   * @see WCartesian3DChart#removeDataSeries(WAbstractDataSeries3D dataseries)
   */
  public void addDataSeries(WAbstractDataSeries3D dataseries_) {
    WAbstractDataSeries3D dataseries = dataseries_;
    this.dataSeriesVector_.add(dataseries_);
    dataseries.setChart(this);
    if ((dataseries.getTitle().length() == 0)) {
      dataseries.setDefaultTitle(++this.seriesCounter_);
    }
    this.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
  }
  /**
   * Removes a dataseries from a chart.
   *
   * <p>
   *
   * @see WCartesian3DChart#addDataSeries(WAbstractDataSeries3D dataseries_)
   */
  public WAbstractDataSeries3D removeDataSeries(WAbstractDataSeries3D dataseries) {
    WAbstractDataSeries3D result = CollectionUtils.take(this.dataSeriesVector_, dataseries);
    List<Object> glObjects = dataseries.getGlObjects();
    ;

    for (int i = 0; i < glObjects.size(); ++i) {
      this.objectsToDelete.add(glObjects.get(i));
    }
    this.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
    return result;
  }
  /**
   * Returns all dataseries that were added to this chart.
   *
   * <p>
   *
   * @see WCartesian3DChart#addDataSeries(WAbstractDataSeries3D dataseries_)
   * @see WCartesian3DChart#removeDataSeries(WAbstractDataSeries3D dataseries)
   */
  public List<WAbstractDataSeries3D> getDataSeries() {
    List<WAbstractDataSeries3D> result = new ArrayList<WAbstractDataSeries3D>();
    ;

    for (WAbstractDataSeries3D d : this.dataSeriesVector_) {
      result.add(d);
    }
    return result;
  }
  /** Returns the specified axis belonging to the chart. */
  public WAxis axis(Axis axis) {
    if (axis == Axis.X3D) {
      return this.xAxis_;
    } else {
      if (axis == Axis.Y3D) {
        return this.yAxis_;
      } else {
        if (axis == Axis.Z3D) {
          return this.zAxis_;
        } else {
          throw new WException("WCartesian3DChart: don't know this type of axis");
        }
      }
    }
  }
  /**
   * Sets an axis.
   *
   * <p>
   *
   * @see WCartesian3DChart#axis(Axis axis)
   */
  public void setAxis(WAxis waxis, Axis axis) {
    if (axis == Axis.X3D) {
      this.xAxis_ = waxis;
      this.xAxis_.init(this.interface_, Axis.X3D);
    } else {
      if (axis == Axis.Y3D) {
        this.yAxis_ = waxis;
        this.yAxis_.init(this.interface_, Axis.Y3D);
      } else {
        if (axis == Axis.Z3D) {
          this.zAxis_ = waxis;
          this.zAxis_.init(this.interface_, Axis.Z3D);
        } else {
          throw new WException("WCartesian3DChart: don't know this type of axis");
        }
      }
    }
  }
  /**
   * Enable/disable gridlines.
   *
   * <p>Enables or disables gridlines in the given plane, along the given axis. All gridlines are by
   * default disabled.
   */
  public void setGridEnabled(Plane plane, Axis axis, boolean enabled) {
    switch (plane) {
      case XY:
        if (axis == Axis.X3D) {
          this.XYGridEnabled_[0] = enabled;
        }
        if (axis == Axis.Y3D) {
          this.XYGridEnabled_[1] = enabled;
        }
        break;
      case XZ:
        if (axis == Axis.X3D) {
          this.XZGridEnabled_[0] = enabled;
        }
        if (axis == Axis.Z3D) {
          this.XZGridEnabled_[1] = enabled;
        }
        break;
      case YZ:
        if (axis == Axis.Y3D) {
          this.YZGridEnabled_[0] = enabled;
        }
        if (axis == Axis.Z3D) {
          this.YZGridEnabled_[1] = enabled;
        }
        break;
    }
    this.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
  }
  /**
   * Enable/disable gridlines.
   *
   * <p>Calls {@link #setGridEnabled(Plane plane, Axis axis, boolean enabled) setGridEnabled(plane,
   * axis, true)}
   */
  public final void setGridEnabled(Plane plane, Axis axis) {
    setGridEnabled(plane, axis, true);
  }
  // public boolean isGridEnabled(Plane plane, Axis axis) ;
  /**
   * Set whether intersection lines are shown between surface charts.
   *
   * <p>This is disabled by default.
   */
  public void setIntersectionLinesEnabled(boolean enabled) {
    this.intersectionLinesEnabled_ = enabled;
    this.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
  }
  /**
   * Set whether intersection lines are shown between surface charts.
   *
   * <p>Calls {@link #setIntersectionLinesEnabled(boolean enabled)
   * setIntersectionLinesEnabled(true)}
   */
  public final void setIntersectionLinesEnabled() {
    setIntersectionLinesEnabled(true);
  }
  /**
   * Returns whether intersection lines are shown between surface charts.
   *
   * <p>
   *
   * @see WCartesian3DChart#setIntersectionLinesEnabled(boolean enabled)
   */
  public boolean isIntersectionLinesEnabled() {
    return this.intersectionLinesEnabled_;
  }
  /** Sets the color of the intersection lines between surface charts. */
  public void setIntersectionLinesColor(WColor color) {
    this.intersectionLinesColor_ = color;
    this.repaintGL(EnumSet.of(GLClientSideRenderer.PAINT_GL));
  }
  /**
   * Gets the color of the intersection lines between surface charts.
   *
   * <p>
   *
   * @see WCartesian3DChart#setIntersectionLinesColor(WColor color)
   */
  public WColor getIntersectionLinesColor() {
    return this.intersectionLinesColor_;
  }
  /**
   * Set the invisible planes with which intersections are drawn.
   *
   * <p>This plane is perpendicular to the given axis, and the intersection is shown in the given
   * color.
   *
   * <p>Note that render times will take increasingly longer as you add more intersection planes.
   */
  public void setIntersectionPlanes(
      final List<WCartesian3DChart.IntersectionPlane> intersectionPlanes) {
    Utils.copyList(intersectionPlanes, this.intersectionPlanes_);
    this.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
  }
  /**
   * Get the invisible planes with which intersections are drawn.
   *
   * <p>
   *
   * @see WCartesian3DChart#setIntersectionPlanes(List intersectionPlanes)
   */
  public List<WCartesian3DChart.IntersectionPlane> getIntersectionPlanes() {
    return this.intersectionPlanes_;
  }
  /**
   * Sets the pen used for drawing the gridlines.
   *
   * <p>The default pen for drawing gridlines is a {@link StandardColor#Black} pen of width 0.
   *
   * <p>
   *
   * @see WCartesian3DChart#getGridLinesPen()
   */
  public void setGridLinesPen(final WPen pen) {
    this.gridLinesPen_ = pen;
    this.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
  }
  /**
   * Returns the pen used for drawing the gridlines.
   *
   * <p>
   *
   * @see WCartesian3DChart#setGridLinesPen(WPen pen)
   */
  public WPen getGridLinesPen() {
    return this.gridLinesPen_;
  }
  /**
   * Sets the pen used to draw the edges of the plotcube.
   *
   * <p>The default pen for drawing cubelines is a {@link StandardColor#Black} pen of width 0.
   *
   * <p>Note: Only width and color of the pen are used, all other styling is ignored.
   */
  public void setCubeLinesPen(final WPen pen) {
    this.cubeLinesPen_ = pen;
    this.updateChart(EnumSet.of(ChartUpdates.GLContext));
  }
  /**
   * Returns a reference to the pen used for drawing the edges of the plotcube.
   *
   * <p>The width and color of the pen are used when drawing the edges of the plotcube
   *
   * <p>
   *
   * @see WCartesian3DChart#setCubeLinesPen(WPen pen)
   */
  public WPen getCubeLinesPen() {
    return this.cubeLinesPen_;
  }
  /**
   * Sets the type of this chart.
   *
   * <p>Sets the type of this chart to either {@link ChartType#Scatter} (for drawing numerical data)
   * or to {@link ChartType#Category} (for drawing categorical data).
   */
  public void setType(ChartType type) {
    this.chartType_ = type;
    this.xAxis_.init(this.interface_, Axis.X3D);
    this.yAxis_.init(this.interface_, Axis.Y3D);
    this.zAxis_.init(this.interface_, Axis.Z3D);
  }
  /**
   * Returns the type of this chart.
   *
   * <p>
   *
   * @see WCartesian3DChart#setType(ChartType type)
   */
  public ChartType getType() {
    return this.chartType_;
  }
  /**
   * Sets the palette for this chart.
   *
   * <p>Ownership of the {@link WChartPalette} is transferred to the chart.
   *
   * <p>The given palette determines which color subsequent dataseries will have. If a dataseries
   * has a colormap set, then the palette is not used for this data.
   */
  public void setPalette(final WChartPalette palette) {
    this.chartPalette_ = palette;
    this.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
  }
  /**
   * Returns the palette used for this chart.
   *
   * <p>
   *
   * @see WCartesian3DChart#setPalette(WChartPalette palette)
   */
  public WChartPalette getPalette() {
    return this.chartPalette_;
  }
  /**
   * Sets the background color for this chart.
   *
   * <p>This sets the GL-clearcolor. The default is transparant, which will cause the background to
   * have the color set in css.
   */
  public void setBackground(final WColor background) {
    this.background_ = background;
    this.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
  }
  /**
   * Returns the background color used for this chart.
   *
   * <p>
   *
   * @see WCartesian3DChart#setBackground(WColor background)
   */
  public WColor getBackground() {
    return this.background_;
  }
  /**
   * Sets the title that is put on the chart.
   *
   * <p>The title is always put at the top of the chart and in the center.
   *
   * <p>
   *
   * @see WCartesian3DChart#setTitleFont(WFont titleFont)
   */
  public void setTitle(final CharSequence title) {
    this.title_ = WString.toWString(title);
    this.updateChart(EnumSet.of(ChartUpdates.GLTextures));
  }
  /**
   * Returns the title that is put at the top of this chart.
   *
   * <p>
   *
   * @see WCartesian3DChart#setTitle(CharSequence title)
   * @see WCartesian3DChart#setTitleFont(WFont titleFont)
   */
  public WString getTitle() {
    return this.title_;
  }
  /**
   * Sets the font that is used to draw the title.
   *
   * <p>The default font is the default constructed {@link WFont}.
   *
   * <p>
   *
   * @see WCartesian3DChart#setTitle(CharSequence title)
   */
  public void setTitleFont(final WFont titleFont) {
    this.titleFont_ = titleFont;
    this.updateChart(EnumSet.of(ChartUpdates.GLTextures));
  }
  /**
   * Returns the font used to draw the title.
   *
   * <p>
   *
   * @see WCartesian3DChart#setTitle(CharSequence title)
   * @see WCartesian3DChart#setTitleFont(WFont titleFont)
   */
  public WFont getTitleFont() {
    return this.titleFont_;
  }
  /**
   * Enables the legend.
   *
   * <p>The location of the legend can be configured using {@link
   * WCartesian3DChart#setLegendLocation(Side side, AlignmentFlag alignment) setLegendLocation()}.
   * Only series for which the legend is enabled are included in this legend.
   *
   * <p>The default value is <code>false</code>.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendLocation(Side side, AlignmentFlag alignment)
   */
  public void setLegendEnabled(boolean enabled) {
    this.legend_.setLegendEnabled(enabled);
    this.updateChart(EnumSet.of(ChartUpdates.GLTextures));
  }
  /**
   * Returns whether the legend is enabled.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendEnabled(boolean enabled)
   */
  public boolean isLegendEnabled() {
    return this.legend_.isLegendEnabled();
  }
  /**
   * Configures the location of the legend.
   *
   * <p>The provided <code>side</code> can either be {@link Side#Left}, {@link Side#Right}, {@link
   * Side#Top}, {@link Side#Bottom} and configures the side of the chart at which the legend is
   * displayed.
   *
   * <p>The <code>alignment</code> specifies how the legend is aligned. This can be a horizontal
   * alignment flag ({@link AlignmentFlag#Left}, {@link AlignmentFlag#Center}, or {@link
   * AlignmentFlag#Right}), when the <code>side</code> is {@link Side#Bottom} or {@link Side#Top},
   * or a vertical alignment flag ({@link AlignmentFlag#Top}, {@link AlignmentFlag#Middle}, or
   * {@link AlignmentFlag#Bottom}) when the <code>side</code> is {@link Side#Left} or {@link
   * Side#Right}.
   *
   * <p>The default location is {@link Side#Right} and {@link AlignmentFlag#Middle}.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendEnabled(boolean enabled)
   */
  public void setLegendLocation(Side side, AlignmentFlag alignment) {
    this.legend_.setLegendLocation(LegendLocation.Outside, side, alignment);
    this.updateChart(EnumSet.of(ChartUpdates.GLTextures));
  }
  /**
   * Configures the legend decoration.
   *
   * <p>This configures the font, border and background for the legend.
   *
   * <p>The default font is a 10pt sans serif font (the same as the default axis label font), the
   * default <code>border</code> is PenStyle::None and the default <code>background</code> is
   * BrushStyle::None.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendEnabled(boolean enabled)
   */
  public void setLegendStyle(final WFont font, final WPen border, final WBrush background) {
    this.legend_.setLegendStyle(font, border, background);
    this.updateChart(EnumSet.of(ChartUpdates.GLTextures));
  }
  /**
   * Returns the legend side.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendLocation(Side side, AlignmentFlag alignment)
   */
  public Side getLegendSide() {
    return this.legend_.getLegendSide();
  }
  /**
   * Returns the legend alignment.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendLocation(Side side, AlignmentFlag alignment)
   */
  public AlignmentFlag getLegendAlignment() {
    return this.legend_.getLegendAlignment();
  }
  /**
   * Returns the number of legend columns.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendColumns(int columns, WLength columnWidth)
   */
  public int getLegendColumns() {
    return this.legend_.getLegendColumns();
  }
  /**
   * Returns the legend column width.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendColumns(int columns, WLength columnWidth)
   */
  public WLength getLegendColumnWidth() {
    return this.legend_.getLegendColumnWidth();
  }
  /**
   * Returns the legend font.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendStyle(WFont font, WPen border, WBrush background)
   */
  public WFont getLegendFont() {
    return this.legend_.getLegendFont();
  }
  /**
   * Returns the legend border pen.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendStyle(WFont font, WPen border, WBrush background)
   */
  public WPen getLegendBorder() {
    return this.legend_.getLegendBorder();
  }
  /**
   * Returns the legend background brush.
   *
   * <p>
   *
   * @see WCartesian3DChart#setLegendStyle(WFont font, WPen border, WBrush background)
   */
  public WBrush getLegendBackground() {
    return this.legend_.getLegendBackground();
  }
  /**
   * Configures the number of columns and columnwidth of the legend.
   *
   * <p>The default value is a single column, 100 pixels wide.
   */
  public void setLegendColumns(int columns, final WLength columnWidth) {
    this.legend_.setLegendColumns(columns);
    this.legend_.setLegendColumnWidth(columnWidth);
    this.updateChart(EnumSet.of(ChartUpdates.GLTextures));
  }
  /**
   * Initializes the chart layout.
   *
   * <p>This method must be called before any methods relating to the layout of the chart are called
   * (eg. calling minimum() or maximum() on one of the axes). The method is also automatically
   * called when the chart is rendered.
   */
  public void initLayout() {
    this.textureScaling_ = 1;
    int widgetWidth = (int) this.getWidth().getValue();
    while (widgetWidth > 0 && widgetWidth < 1024) {
      this.textureScaling_ *= 2;
      widgetWidth *= 2;
    }
    int axisOffset = (int) (this.axisRenderWidth_ / this.textureScaling_ / 1.6 * 0.3);
    int axisWidth = this.axisRenderWidth_ / this.textureScaling_;
    this.xAxis_.prepareRender(Orientation.Horizontal, axisWidth - 2 * axisOffset);
    this.yAxis_.prepareRender(Orientation.Horizontal, axisWidth - 2 * axisOffset);
    this.zAxis_.prepareRender(Orientation.Vertical, axisWidth - 2 * axisOffset);
  }
  /**
   * Set the camera-matrix.
   *
   * <p>The viewpoint can be set with the camera-matrix. The chart is defined in the world
   * coordinate system as a cube with axes from 0 to 1 in all three directions. Therefore the center
   * of the cube is positioned at (0.5, 0.5, 0.5). The camera can be most easily position with the
   * lookAt method of WMatrix4x4. A common use-case when manipulating the matrix is to translate the
   * center to the origin and then rotate.
   */
  public void setCameraMatrix(final javax.vecmath.Matrix4f matrix) {
    this.worldTransform_ = matrix;
    this.updateChart(EnumSet.of(ChartUpdates.CameraMatrix));
  }
  /**
   * Get the current camera-matrix.
   *
   * <p>The matrix represents the current view on the scene. It corresponds to a coordinate system
   * where the chart&apos;s axes run from 0 to 1 in all three directions.
   *
   * <p>
   *
   * @see WCartesian3DChart#setCameraMatrix(javax.vecmath.Matrix4f matrix)
   */
  public javax.vecmath.Matrix4f getCameraMatrix() {
    return this.jsMatrix_.getValue();
  }
  /**
   * Get the current camera matrix as a JavaScriptMatrix4x4.
   *
   * <p>This JavaScriptMatrix4x4 can be used to implement a custom mouse handler using {@link
   * WGLWidget#setClientSideMouseHandler(String handlerCode) WGLWidget#setClientSideMouseHandler()}.
   *
   * <p>
   *
   * @see WCartesian3DChart#setCameraMatrix(javax.vecmath.Matrix4f matrix)
   */
  public WGLWidget.JavaScriptMatrix4x4 getJsMatrix() {
    return this.jsMatrix_;
  }

  public javax.vecmath.Matrix4f getPMatrix() {
    return this.pMatrix_;
  }

  public double toPlotCubeCoords(double value, Axis axis) {
    double min = 0.0;
    double max = 1.0;
    if (axis == Axis.X3D) {
      min = this.xAxis_.getMinimum();
      max = this.xAxis_.getMaximum();
    } else {
      if (axis == Axis.Y3D) {
        min = this.yAxis_.getMinimum();
        max = this.yAxis_.getMaximum();
      } else {
        if (axis == Axis.Z3D) {
          min = this.zAxis_.getMinimum();
          max = this.zAxis_.getMaximum();
        } else {
          throw new WException("WCartesian3DChart: don't know this type of axis");
        }
      }
    }
    return (value - min) / (max - min);
  }
  /**
   * Initialize the WebGL state when the widget is first shown.
   *
   * <p>Specialized for chart rendering.
   */
  protected void initializeGL() {
    if (!this.isViewSet_) {
      WebGLUtils.lookAt(this.worldTransform_, 0.5, 0.5, 5, 0.5, 0.5, 0.5, 0, 1, 0);
      WebGLUtils.translate(this.worldTransform_, 0.5, 0.5, 0.5);
      WebGLUtils.rotate(this.worldTransform_, 45.0, 0.0, 1.0, 0.0);
      WebGLUtils.rotate(this.worldTransform_, 20.0, 1.0, 0.0, 1.0);
      WebGLUtils.rotate(this.worldTransform_, 5.0, 0.0, 1.0, 0.0);
      WebGLUtils.scale(this.worldTransform_, (float) 1.8);
      WebGLUtils.translate(this.worldTransform_, -0.5, -0.5, -0.5);
      this.isViewSet_ = true;
    }
    if (!this.isRestoringContext()) {
      this.initJavaScriptMatrix4(this.jsMatrix_);
      this.setClientSideLookAtHandler(this.jsMatrix_, 0.5, 0.5, 0.5, 0, 1, 0, 0.005, 0.005);
    }
    double ratio = this.getHeight().getValue() / this.getWidth().getValue();
    WebGLUtils.ortho(this.pMatrix_, -2, 2, -2 * ratio, 2 * ratio, -100, 100);
    this.disable(WGLWidget.GLenum.DEPTH_TEST);
    this.cullFace(WGLWidget.GLenum.BACK);
    this.enable(WGLWidget.GLenum.CULL_FACE);
    int w = (int) this.getWidth().getValue();
    int h = (int) this.getHeight().getValue();
    this.viewport(0, 0, w < 0 ? 0 : w, h < 0 ? 0 : h);
    this.init2DShaders();
    float[] vertexPos = {-1, 1, 0, 1, 1, 0, -1, -1, 0, 1, -1, 0};
    float[] texCo = {0, 1, 1, 1, 0, 0, 1, 0};
    this.overlayPosBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.overlayPosBuffer_);
    int size = vertexPos.length;
    java.nio.ByteBuffer vertexPosBuf = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      vertexPosBuf.putFloat(vertexPos[i]);
    }
    this.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER, vertexPosBuf, WGLWidget.GLenum.STATIC_DRAW);
    this.overlayTexCoBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.overlayTexCoBuffer_);
    size = texCo.length;
    java.nio.ByteBuffer texCoBuf = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      texCoBuf.putFloat(texCo[i]);
    }
    this.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER, texCoBuf, WGLWidget.GLenum.STATIC_DRAW);
    this.clippingPlaneVertBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.clippingPlaneVertBuffer_);
    java.nio.ByteBuffer clippingPlaneBuf = WebGLUtils.newByteBuffer(4 * (8));
    clippingPlaneBuf.putFloat(-1.0f);
    clippingPlaneBuf.putFloat(-1.0f);
    clippingPlaneBuf.putFloat(-1.0f);
    clippingPlaneBuf.putFloat(2.0f);
    clippingPlaneBuf.putFloat(2.0f);
    clippingPlaneBuf.putFloat(-1.0f);
    clippingPlaneBuf.putFloat(2.0f);
    clippingPlaneBuf.putFloat(2.0f);
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, clippingPlaneBuf, WGLWidget.GLenum.STATIC_DRAW);
    for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
      this.dataSeriesVector_.get(i).initializeGL();
    }
    if (this.isRestoringContext()) {
      this.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
    } else {
      this.updateChart(
          EnumUtils.or(
              EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures),
              ChartUpdates.CameraMatrix));
    }
  }
  /**
   * Update the client-side painting function.
   *
   * <p>Specialized for chart rendering.
   */
  protected void paintGL() {
    this.clearColor(
        this.background_.getRed() / 255.0,
        this.background_.getGreen() / 255.0,
        this.background_.getBlue() / 255.0,
        this.background_.getAlpha() / 255.0);
    this.clear(EnumSet.of(WGLWidget.GLenum.COLOR_BUFFER_BIT, WGLWidget.GLenum.DEPTH_BUFFER_BIT));
    this.enable(WGLWidget.GLenum.BLEND);
    this.blendFunc(WGLWidget.GLenum.SRC_ALPHA, WGLWidget.GLenum.ONE_MINUS_SRC_ALPHA);
    this.enable(WGLWidget.GLenum.CULL_FACE);
    javax.vecmath.Matrix4f mvMatrix =
        new javax.vecmath.Matrix4f(
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f);
    this.useProgram(this.cubeProgram_);
    this.uniformMatrix4(this.cube_mvMatrixUniform_, mvMatrix);
    this.uniformMatrix4(this.cube_cMatrixUniform_, this.jsMatrix_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeBuffer_);
    this.vertexAttribPointer(
        this.cube_vertexPositionAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.cube_vertexPositionAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeNormalsBuffer_);
    this.vertexAttribPointer(
        this.cube_planeNormalAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.cube_planeNormalAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeTexCoords_);
    this.vertexAttribPointer(
        this.cube_textureCoordAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.cube_textureCoordAttribute_);
    this.activeTexture(WGLWidget.GLenum.TEXTURE0);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.cubeTextureXY_);
    this.uniform1i(this.cube_texSampler1Uniform_, 0);
    this.activeTexture(WGLWidget.GLenum.TEXTURE1);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.cubeTextureXZ_);
    this.uniform1i(this.cube_texSampler2Uniform_, 1);
    this.activeTexture(WGLWidget.GLenum.TEXTURE2);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.cubeTextureYZ_);
    this.uniform1i(this.cube_texSampler3Uniform_, 2);
    this.enable(WGLWidget.GLenum.POLYGON_OFFSET_FILL);
    float unitOffset =
        (float)
            (Math.pow(5, this.gridLinesPen_.getWidth().getValue()) < 10000
                ? Math.pow(5, this.gridLinesPen_.getWidth().getValue())
                : 10000);
    this.polygonOffset(1, unitOffset);
    this.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.cubeIndicesBuffer_);
    this.drawElements(
        WGLWidget.GLenum.TRIANGLES,
        this.cubeIndices_.capacity(),
        WGLWidget.GLenum.UNSIGNED_SHORT,
        0);
    this.disableVertexAttribArray(this.cube_vertexPositionAttribute_);
    this.disableVertexAttribArray(this.cube_textureCoordAttribute_);
    this.disable(WGLWidget.GLenum.POLYGON_OFFSET_FILL);
    this.useProgram(this.cubeLineProgram_);
    this.uniformMatrix4(this.cubeLine_mvMatrixUniform_, mvMatrix);
    this.uniformMatrix4(this.cubeLine_cMatrixUniform_, this.jsMatrix_);
    this.uniformMatrix4(this.cubeLine_nMatrixUniform_, this.jsMatrix_.multiply(mvMatrix));
    this.uniform4f(
        this.cubeLine_colorUniform_,
        (float) this.cubeLinesPen_.getColor().getRed(),
        (float) this.cubeLinesPen_.getColor().getGreen(),
        (float) this.cubeLinesPen_.getColor().getBlue(),
        (float) this.cubeLinesPen_.getColor().getAlpha());
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeBuffer_);
    this.vertexAttribPointer(
        this.cubeLine_vertexPositionAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.cubeLine_vertexPositionAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeLineNormalsBuffer_);
    this.vertexAttribPointer(
        this.cubeLine_normalAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.cubeLine_normalAttribute_);
    this.lineWidth(
        this.cubeLinesPen_.getWidth().getValue() == 0
            ? 1.0
            : this.cubeLinesPen_.getWidth().getValue());
    this.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.cubeLineIndicesBuffer_);
    this.drawElements(
        WGLWidget.GLenum.LINES,
        this.cubeLineIndices_.capacity(),
        WGLWidget.GLenum.UNSIGNED_SHORT,
        0);
    this.disableVertexAttribArray(this.cubeLine_vertexPositionAttribute_);
    this.disableVertexAttribArray(this.cubeLine_normalAttribute_);
    this.useProgram(this.axisProgram_);
    this.uniformMatrix4(this.axis_mvMatrixUniform_, mvMatrix);
    this.uniformMatrix4(this.axis_cMatrixUniform_, this.jsMatrix_);
    this.uniformMatrix4(this.axis_nMatrixUniform_, this.jsMatrix_.multiply(mvMatrix));
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisBuffer_);
    this.vertexAttribPointer(
        this.axis_vertexPositionAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.axis_vertexPositionAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisTexCoordsHoriz_);
    this.vertexAttribPointer(
        this.axis_textureCoordAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.axis_textureCoordAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisInPlaneBuffer_);
    this.vertexAttribPointer(this.axis_inPlaneAttribute_, 1, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.axis_inPlaneAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisPlaneNormalBuffer_);
    this.vertexAttribPointer(
        this.axis_planeNormalAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.axis_planeNormalAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisOutOfPlaneNormalBuffer_);
    this.vertexAttribPointer(
        this.axis_outOfPlaneNormalAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.axis_outOfPlaneNormalAttribute_);
    this.uniform1i(this.axis_normalAngleTextureUniform_, 1);
    this.activeTexture(WGLWidget.GLenum.TEXTURE1);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.horizAxisTexture_);
    this.uniform1i(this.axis_texSamplerUniform_, 1);
    this.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.axisIndicesBuffer_);
    this.drawElements(
        WGLWidget.GLenum.TRIANGLES,
        this.axisSlabIndices_.capacity(),
        WGLWidget.GLenum.UNSIGNED_SHORT,
        0);
    this.uniform1i(this.axis_normalAngleTextureUniform_, 0);
    this.activeTexture(WGLWidget.GLenum.TEXTURE1);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.horizAxisTexture2_);
    this.uniform1i(this.axis_texSamplerUniform_, 1);
    this.drawElements(
        WGLWidget.GLenum.TRIANGLES,
        this.axisSlabIndices_.capacity(),
        WGLWidget.GLenum.UNSIGNED_SHORT,
        0);
    this.disableVertexAttribArray(this.axis_vertexPositionAttribute_);
    this.disableVertexAttribArray(this.axis_textureCoordAttribute_);
    this.disableVertexAttribArray(this.axis_inPlaneAttribute_);
    this.disableVertexAttribArray(this.axis_planeNormalAttribute_);
    this.disableVertexAttribArray(this.axis_outOfPlaneNormalAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisVertBuffer_);
    this.vertexAttribPointer(
        this.axis_vertexPositionAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.axis_vertexPositionAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisTexCoordsVert_);
    this.vertexAttribPointer(
        this.axis_textureCoordAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.axis_textureCoordAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisInPlaneVertBuffer_);
    this.vertexAttribPointer(this.axis_inPlaneAttribute_, 1, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.axis_inPlaneAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisPlaneNormalVertBuffer_);
    this.vertexAttribPointer(
        this.axis_planeNormalAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.axis_planeNormalAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisOutOfPlaneNormalVertBuffer_);
    this.vertexAttribPointer(
        this.axis_outOfPlaneNormalAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.axis_outOfPlaneNormalAttribute_);
    this.activeTexture(WGLWidget.GLenum.TEXTURE1);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.vertAxisTexture_);
    this.uniform1i(this.axis_texSamplerUniform_, 1);
    this.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.axisIndicesVertBuffer_);
    this.drawElements(
        WGLWidget.GLenum.TRIANGLES,
        this.axisSlabIndicesVert_.capacity(),
        WGLWidget.GLenum.UNSIGNED_SHORT,
        0);
    this.uniform1i(this.axis_normalAngleTextureUniform_, 1);
    this.drawElements(
        WGLWidget.GLenum.TRIANGLES,
        this.axisSlabIndicesVert_.capacity(),
        WGLWidget.GLenum.UNSIGNED_SHORT,
        0);
    this.disableVertexAttribArray(this.axis_vertexPositionAttribute_);
    this.disableVertexAttribArray(this.axis_textureCoordAttribute_);
    this.disableVertexAttribArray(this.axis_inPlaneAttribute_);
    this.disableVertexAttribArray(this.axis_planeNormalAttribute_);
    this.disableVertexAttribArray(this.axis_outOfPlaneNormalAttribute_);
    this.disable(WGLWidget.GLenum.BLEND);
    for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
      WAbstractGridData gridData =
          ObjectUtils.cast(this.dataSeriesVector_.get(i), WAbstractGridData.class);
      if (gridData != null
          && gridData.getType() == Series3DType.Surface
          && gridData.isClippingLinesEnabled()) {
        gridData.paintGL();
        this.renderClippingLines(gridData);
        this.enable(WGLWidget.GLenum.BLEND);
        this.disable(WGLWidget.GLenum.CULL_FACE);
        this.disable(WGLWidget.GLenum.DEPTH_TEST);
        this.depthMask(false);
        if (!this.intersectionLinesTexture_.isNull()) {
          this.paintPeripheralTexture(
              this.overlayPosBuffer_, this.overlayTexCoBuffer_, this.intersectionLinesTexture_);
        }
        this.depthMask(true);
        this.disable(WGLWidget.GLenum.BLEND);
        this.enable(WGLWidget.GLenum.CULL_FACE);
        this.enable(WGLWidget.GLenum.DEPTH_TEST);
      }
    }
    for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
      WAbstractGridData gridData =
          ObjectUtils.cast(this.dataSeriesVector_.get(i), WAbstractGridData.class);
      if (gridData != null
          && gridData.getType() == Series3DType.Surface
          && !gridData.isClippingLinesEnabled()) {
        gridData.paintGL();
      }
    }
    if (!this.intersectionLinesEnabled_ && this.intersectionPlanes_.isEmpty()) {
      for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
        WAbstractGridData gridData =
            ObjectUtils.cast(this.dataSeriesVector_.get(i), WAbstractGridData.class);
        if (!(gridData != null && gridData.getType() == Series3DType.Surface)) {
          this.dataSeriesVector_.get(i).paintGL();
        }
      }
    }
    if (this.intersectionLinesEnabled_ || !this.intersectionPlanes_.isEmpty()) {
      this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.intersectionLinesFramebuffer_);
      this.clearColor(0.0, 0.0, 0.0, 0.0);
      this.clear(EnumSet.of(WGLWidget.GLenum.COLOR_BUFFER_BIT, WGLWidget.GLenum.DEPTH_BUFFER_BIT));
      this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, new WGLWidget.Framebuffer());
      if (this.intersectionLinesEnabled_) {
        this.renderIntersectionLines();
      }
      if (!this.intersectionPlanes_.isEmpty()) {
        this.renderIntersectionLinesWithInvisiblePlanes();
      }
      this.enable(WGLWidget.GLenum.BLEND);
      this.disable(WGLWidget.GLenum.CULL_FACE);
      this.disable(WGLWidget.GLenum.DEPTH_TEST);
      this.depthMask(false);
      if (!this.intersectionLinesTexture_.isNull()) {
        this.paintPeripheralTexture(
            this.overlayPosBuffer_, this.overlayTexCoBuffer_, this.intersectionLinesTexture_);
      }
      this.depthMask(true);
      this.disable(WGLWidget.GLenum.BLEND);
      this.enable(WGLWidget.GLenum.CULL_FACE);
      this.enable(WGLWidget.GLenum.DEPTH_TEST);
      for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
        WAbstractGridData gridData =
            ObjectUtils.cast(this.dataSeriesVector_.get(i), WAbstractGridData.class);
        if (!(gridData != null) || gridData.getType() != Series3DType.Surface) {
          this.dataSeriesVector_.get(i).paintGL();
        }
      }
    }
    this.enable(WGLWidget.GLenum.BLEND);
    this.disable(WGLWidget.GLenum.CULL_FACE);
    if (!this.titleTexture_.isNull()) {
      this.paintPeripheralTexture(
          this.overlayPosBuffer_, this.overlayTexCoBuffer_, this.titleTexture_);
    }
    if (!this.legendTexture_.isNull()) {
      this.paintPeripheralTexture(
          this.overlayPosBuffer_, this.overlayTexCoBuffer_, this.legendTexture_);
    }
    if (!this.colorMapTexture_.isNull()) {
      this.paintPeripheralTexture(
          this.overlayPosBuffer_, this.overlayTexCoBuffer_, this.colorMapTexture_);
    }
    this.disable(WGLWidget.GLenum.BLEND);
  }
  /**
   * Update state set in {@link WCartesian3DChart#initializeGL() initializeGL()}
   *
   * <p>Specialized for chart rendering.
   */
  protected void updateGL() {
    if (this.updates_.contains(ChartUpdates.GLContext)) {
      this.deleteAllGLResources();
      for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
        this.dataSeriesVector_.get(i).deleteAllGLResources();
      }
      this.clearColor(
          this.background_.getRed() / 255.0,
          this.background_.getGreen() / 255.0,
          this.background_.getBlue() / 255.0,
          this.background_.getAlpha() / 255.0);
      this.initializePlotCube();
      if (this.intersectionLinesEnabled_ || !this.intersectionPlanes_.isEmpty()) {
        this.initializeIntersectionLinesProgram();
        this.initOffscreenBuffer();
      }
      for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
        WAbstractGridData data =
            ObjectUtils.cast(this.dataSeriesVector_.get(i), WAbstractGridData.class);
        if (data != null) {
          this.initializeClippingPlaneProgram();
          if (!this.intersectionLinesEnabled_ && this.intersectionPlanes_.isEmpty()) {
            this.initializeIntersectionLinesProgram();
            this.initOffscreenBuffer();
          }
          break;
        }
      }
      for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
        this.dataSeriesVector_.get(i).updateGL();
      }
      this.repaintGL(EnumSet.of(GLClientSideRenderer.RESIZE_GL));
      this.repaintGL(EnumSet.of(GLClientSideRenderer.PAINT_GL));
    }
    if (this.updates_.contains(ChartUpdates.CameraMatrix)) {
      this.setJavaScriptMatrix4(this.jsMatrix_, this.worldTransform_);
      this.repaintGL(EnumSet.of(GLClientSideRenderer.PAINT_GL));
    }
    if (this.updates_.contains(ChartUpdates.GLTextures)) {
      this.deleteGLTextures();
      this.loadCubeTextures();
      this.currentTopOffset_ = 0;
      this.currentBottomOffset_ = 0;
      this.currentLeftOffset_ = 0;
      this.currentRightOffset_ = 0;
      this.initTitle();
      if (this.legend_.getLegendSide() == Side.Left) {
        this.initColorMaps();
        this.initLegend();
      } else {
        if (this.legend_.getLegendSide() == Side.Right) {
          this.initLegend();
          this.initColorMaps();
        } else {
          this.initLegend();
          this.initColorMaps();
        }
      }
      this.repaintGL(EnumSet.of(GLClientSideRenderer.PAINT_GL));
    }
    this.updates_ = EnumSet.noneOf(ChartUpdates.class);
  }
  /**
   * Act on resize events.
   *
   * <p>Specialized for chart rendering.
   */
  protected void resizeGL(int width, int height) {
    this.viewport(0, 0, width, height);
    double ratio = (double) height / width;
    this.pMatrix_ =
        new javax.vecmath.Matrix4f(
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f);
    WebGLUtils.ortho(this.pMatrix_, -2, 2, -2 * ratio, 2 * ratio, -100, 100);
    this.useProgram(this.cubeProgram_);
    this.uniformMatrix4(this.cube_pMatrixUniform_, this.pMatrix_);
    this.useProgram(this.cubeLineProgram_);
    this.uniformMatrix4(this.cubeLine_pMatrixUniform_, this.pMatrix_);
    this.useProgram(this.axisProgram_);
    this.uniformMatrix4(this.axis_pMatrixUniform_, this.pMatrix_);
    boolean clippingLinesEnabled = false;
    for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
      WAbstractGridData data =
          ObjectUtils.cast(this.dataSeriesVector_.get(i), WAbstractGridData.class);
      if (data != null && data.isClippingLinesEnabled()) {
        clippingLinesEnabled = true;
        break;
      }
    }
    if (this.intersectionLinesEnabled_
        || clippingLinesEnabled
        || !this.intersectionPlanes_.isEmpty()) {
      this.resizeOffscreenBuffer();
    }
    for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
      this.dataSeriesVector_.get(i).resizeGL();
    }
  }
  /** Update the chart. */
  public void updateChart(EnumSet<ChartUpdates> flags) {
    this.updates_.addAll(flags);
    this.repaintGL(EnumSet.of(GLClientSideRenderer.UPDATE_GL));
  }
  /**
   * Update the chart.
   *
   * <p>Calls {@link #updateChart(EnumSet flags) updateChart(EnumSet.of(flag, flags))}
   */
  public final void updateChart(ChartUpdates flag, ChartUpdates... flags) {
    updateChart(EnumSet.of(flag, flags));
  }

  public void resize(final WLength width, final WLength height) {
    this.updateChart(EnumSet.of(ChartUpdates.GLTextures));
    super.resize(width, height);
  }

  public void createRay(
      double x, double y, final javax.vecmath.GVector eye, final javax.vecmath.GVector direction) {
    javax.vecmath.Matrix4f transform = WebGLUtils.multiply(this.pMatrix_, this.getCameraMatrix());
    javax.vecmath.Matrix4f invTransform = transform;
    invTransform.invert();
    javax.vecmath.GVector near_ =
        new javax.vecmath.GVector(
            new double[] {
              x / this.getWidth().getValue() * 2 - 1,
              y / this.getHeight().getValue() * -2 + 1,
              -1.0,
              1.0
            });
    javax.vecmath.GVector far_ =
        new javax.vecmath.GVector(
            new double[] {near_.getElement(0), near_.getElement(1), 1.0, 1.0});
    near_ = WebGLUtils.multiply(invTransform, near_);
    far_ = WebGLUtils.multiply(invTransform, far_);
    near_ = WebGLUtils.multiply(near_, 1.0 / near_.getElement(3));
    far_ = WebGLUtils.multiply(far_, 1.0 / far_.getElement(3));
    javax.vecmath.GVector ray = new javax.vecmath.GVector(WebGLUtils.subtract(far_, near_));
    WebGLUtils.normalize(ray);
    direction.setElement(0, ray.getElement(0));
    direction.setElement(1, ray.getElement(2));
    direction.setElement(2, ray.getElement(1));
    eye.setElement(0, near_.getElement(0));
    eye.setElement(1, near_.getElement(2));
    eye.setElement(2, near_.getElement(1));
  }

  private void initializePlotCube() {
    this.cubeBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeBuffer_);
    int size = cubeData.length;
    this.cubeData_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.cubeData_.putFloat(cubeData[i]);
    }
    this.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeData_, WGLWidget.GLenum.STATIC_DRAW);
    this.cubeNormalsBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeNormalsBuffer_);
    size = cubePlaneNormals.length;
    this.cubeNormalsData_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.cubeNormalsData_.putFloat(cubePlaneNormals[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, this.cubeNormalsData_, WGLWidget.GLenum.STATIC_DRAW);
    this.cubeIndicesBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.cubeIndicesBuffer_);
    size = cubeIndices.length;
    this.cubeIndices_ = java.nio.IntBuffer.allocate(size);
    for (int i = 0; i < size; i++) {
      this.cubeIndices_.put(cubeIndices[i]);
    }
    this.bufferDataiv(
        WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
        this.cubeIndices_,
        WGLWidget.GLenum.STATIC_DRAW,
        WGLWidget.GLenum.UNSIGNED_SHORT);
    this.cubeLineNormalsBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeLineNormalsBuffer_);
    size = cubeLineNormals.length;
    java.nio.ByteBuffer cubeLineNormalsArray = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      cubeLineNormalsArray.putFloat(cubeLineNormals[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, cubeLineNormalsArray, WGLWidget.GLenum.STATIC_DRAW);
    this.cubeLineIndicesBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.cubeLineIndicesBuffer_);
    size = cubeLineIndices.length;
    this.cubeLineIndices_ = java.nio.IntBuffer.allocate(size);
    for (int i = 0; i < size; i++) {
      this.cubeLineIndices_.put(cubeLineIndices[i]);
    }
    this.bufferDataiv(
        WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
        this.cubeLineIndices_,
        WGLWidget.GLenum.STATIC_DRAW,
        WGLWidget.GLenum.UNSIGNED_SHORT);
    this.axisBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisBuffer_);
    size = axisSlabData.length;
    this.axisSlabData_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.axisSlabData_.putFloat(axisSlabData[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, this.axisSlabData_, WGLWidget.GLenum.STATIC_DRAW);
    this.axisIndicesBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.axisIndicesBuffer_);
    size = axisSlabIndices.length;
    this.axisSlabIndices_ = java.nio.IntBuffer.allocate(size);
    for (int i = 0; i < size; i++) {
      this.axisSlabIndices_.put(axisSlabIndices[i]);
    }
    this.bufferDataiv(
        WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
        this.axisSlabIndices_,
        WGLWidget.GLenum.STATIC_DRAW,
        WGLWidget.GLenum.UNSIGNED_SHORT);
    this.axisInPlaneBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisInPlaneBuffer_);
    size = axisInPlaneBools.length;
    this.axisInPlaneBools_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.axisInPlaneBools_.putFloat(axisInPlaneBools[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, this.axisInPlaneBools_, WGLWidget.GLenum.STATIC_DRAW);
    this.axisPlaneNormalBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisPlaneNormalBuffer_);
    size = axisPlaneNormal.length;
    this.axisPlaneNormal_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.axisPlaneNormal_.putFloat(axisPlaneNormal[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, this.axisPlaneNormal_, WGLWidget.GLenum.STATIC_DRAW);
    this.axisOutOfPlaneNormalBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisOutOfPlaneNormalBuffer_);
    size = axisOutOfPlaneNormal.length;
    this.axisOutOfPlaneNormal_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.axisOutOfPlaneNormal_.putFloat(axisOutOfPlaneNormal[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, this.axisOutOfPlaneNormal_, WGLWidget.GLenum.STATIC_DRAW);
    this.axisVertBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisVertBuffer_);
    size = axisSlabDataVertical.length;
    this.axisSlabDataVert_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.axisSlabDataVert_.putFloat(axisSlabDataVertical[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, this.axisSlabDataVert_, WGLWidget.GLenum.STATIC_DRAW);
    this.axisIndicesVertBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER, this.axisIndicesVertBuffer_);
    size = axisSlabIndicesVertical.length;
    this.axisSlabIndicesVert_ = java.nio.IntBuffer.allocate(size);
    for (int i = 0; i < size; i++) {
      this.axisSlabIndicesVert_.put(axisSlabIndicesVertical[i]);
    }
    this.bufferDataiv(
        WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
        this.axisSlabIndicesVert_,
        WGLWidget.GLenum.STATIC_DRAW,
        WGLWidget.GLenum.UNSIGNED_SHORT);
    this.axisInPlaneVertBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisInPlaneVertBuffer_);
    size = axisInPlaneBoolsVertical.length;
    this.axisInPlaneBoolsVert_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.axisInPlaneBoolsVert_.putFloat(axisInPlaneBoolsVertical[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, this.axisInPlaneBoolsVert_, WGLWidget.GLenum.STATIC_DRAW);
    this.axisPlaneNormalVertBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisPlaneNormalVertBuffer_);
    size = axisPlaneNormalVertical.length;
    this.axisPlaneNormalVert_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.axisPlaneNormalVert_.putFloat(axisPlaneNormalVertical[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, this.axisPlaneNormalVert_, WGLWidget.GLenum.STATIC_DRAW);
    this.axisOutOfPlaneNormalVertBuffer_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisOutOfPlaneNormalVertBuffer_);
    size = axisOutOfPlaneNormalVertical.length;
    this.axisOutOfPlaneNormalVert_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.axisOutOfPlaneNormalVert_.putFloat(axisOutOfPlaneNormalVertical[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER,
        this.axisOutOfPlaneNormalVert_,
        WGLWidget.GLenum.STATIC_DRAW);
    this.fragmentShader_ = this.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
    this.shaderSource(this.fragmentShader_, cubeFragmentShaderSrc);
    this.compileShader(this.fragmentShader_);
    this.vertexShader_ = this.createShader(WGLWidget.GLenum.VERTEX_SHADER);
    this.shaderSource(this.vertexShader_, cubeVertexShaderSrc);
    this.compileShader(this.vertexShader_);
    this.fragmentShader2_ = this.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
    this.shaderSource(this.fragmentShader2_, axisFragmentShaderSrc);
    this.compileShader(this.fragmentShader2_);
    this.vertexShader2_ = this.createShader(WGLWidget.GLenum.VERTEX_SHADER);
    this.shaderSource(this.vertexShader2_, axisVertexShaderSrc);
    this.compileShader(this.vertexShader2_);
    this.cubeLineFragShader_ = this.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
    this.shaderSource(this.cubeLineFragShader_, cubeLineFragmentShaderSrc);
    ;
    this.compileShader(this.cubeLineFragShader_);
    this.cubeLineVertShader_ = this.createShader(WGLWidget.GLenum.VERTEX_SHADER);
    this.shaderSource(this.cubeLineVertShader_, cubeLineVertexShaderSrc);
    this.compileShader(this.cubeLineVertShader_);
    this.cubeProgram_ = this.createProgram();
    this.cubeLineProgram_ = this.createProgram();
    this.axisProgram_ = this.createProgram();
    this.attachShader(this.cubeProgram_, this.vertexShader_);
    this.attachShader(this.cubeProgram_, this.fragmentShader_);
    this.attachShader(this.cubeLineProgram_, this.cubeLineVertShader_);
    this.attachShader(this.cubeLineProgram_, this.cubeLineFragShader_);
    this.attachShader(this.axisProgram_, this.vertexShader2_);
    this.attachShader(this.axisProgram_, this.fragmentShader2_);
    this.linkProgram(this.cubeProgram_);
    this.linkProgram(this.cubeLineProgram_);
    this.linkProgram(this.axisProgram_);
    this.cube_vertexPositionAttribute_ =
        this.getAttribLocation(this.cubeProgram_, "aVertexPosition");
    this.cube_planeNormalAttribute_ = this.getAttribLocation(this.cubeProgram_, "aPlaneNormal");
    this.cube_textureCoordAttribute_ = this.getAttribLocation(this.cubeProgram_, "aTextureCo");
    this.cubeLine_vertexPositionAttribute_ =
        this.getAttribLocation(this.cubeLineProgram_, "aVertexPosition");
    this.cubeLine_normalAttribute_ = this.getAttribLocation(this.cubeLineProgram_, "aNormal");
    this.axis_vertexPositionAttribute_ =
        this.getAttribLocation(this.axisProgram_, "aVertexPosition");
    this.axis_textureCoordAttribute_ = this.getAttribLocation(this.axisProgram_, "aTextureCo");
    this.axis_inPlaneAttribute_ = this.getAttribLocation(this.axisProgram_, "aInPlane");
    this.axis_planeNormalAttribute_ = this.getAttribLocation(this.axisProgram_, "aPlaneNormal");
    this.axis_outOfPlaneNormalAttribute_ =
        this.getAttribLocation(this.axisProgram_, "aOutOfPlaneNormal");
    this.cube_pMatrixUniform_ = this.getUniformLocation(this.cubeProgram_, "uPMatrix");
    this.cube_mvMatrixUniform_ = this.getUniformLocation(this.cubeProgram_, "uMVMatrix");
    this.cube_cMatrixUniform_ = this.getUniformLocation(this.cubeProgram_, "uCMatrix");
    this.cube_texSampler1Uniform_ = this.getUniformLocation(this.cubeProgram_, "uSampler1");
    this.cube_texSampler2Uniform_ = this.getUniformLocation(this.cubeProgram_, "uSampler2");
    this.cube_texSampler3Uniform_ = this.getUniformLocation(this.cubeProgram_, "uSampler3");
    this.cubeLine_pMatrixUniform_ = this.getUniformLocation(this.cubeLineProgram_, "uPMatrix");
    this.cubeLine_mvMatrixUniform_ = this.getUniformLocation(this.cubeLineProgram_, "uMVMatrix");
    this.cubeLine_cMatrixUniform_ = this.getUniformLocation(this.cubeLineProgram_, "uCMatrix");
    this.cubeLine_nMatrixUniform_ = this.getUniformLocation(this.cubeLineProgram_, "uNMatrix");
    this.cubeLine_colorUniform_ = this.getUniformLocation(this.cubeLineProgram_, "uColor");
    this.axis_pMatrixUniform_ = this.getUniformLocation(this.axisProgram_, "uPMatrix");
    this.axis_mvMatrixUniform_ = this.getUniformLocation(this.axisProgram_, "uMVMatrix");
    this.axis_cMatrixUniform_ = this.getUniformLocation(this.axisProgram_, "uCMatrix");
    this.axis_nMatrixUniform_ = this.getUniformLocation(this.axisProgram_, "uNMatrix");
    this.axis_normalAngleTextureUniform_ =
        this.getUniformLocation(this.axisProgram_, "uNormalAngleTexture");
    this.axis_texSamplerUniform_ = this.getUniformLocation(this.axisProgram_, "uSampler");
    this.loadCubeTextures();
    this.axisTexCoordsHoriz_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisTexCoordsHoriz_);
    size = axisTexCo.length;
    this.axisTexCo_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.axisTexCo_.putFloat(axisTexCo[i]);
    }
    this.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER, this.axisTexCo_, WGLWidget.GLenum.STATIC_DRAW);
    this.axisTexCoordsVert_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.axisTexCoordsVert_);
    size = axisTexCoVertical.length;
    this.axisTexCoVert_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.axisTexCoVert_.putFloat(axisTexCoVertical[i]);
    }
    this.bufferDatafv(
        WGLWidget.GLenum.ARRAY_BUFFER, this.axisTexCoVert_, WGLWidget.GLenum.STATIC_DRAW);
    this.cubeTexCoords_ = this.createBuffer();
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeTexCoords_);
    size = cubeTexCo.length;
    this.cubeTexCo_ = WebGLUtils.newByteBuffer(4 * (size));
    for (int i = 0; i < size; i++) {
      this.cubeTexCo_.putFloat(cubeTexCo[i]);
    }
    this.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER, this.cubeTexCo_, WGLWidget.GLenum.STATIC_DRAW);
    this.useProgram(this.cubeProgram_);
    this.uniformMatrix4(this.cube_pMatrixUniform_, this.pMatrix_);
    this.useProgram(this.cubeLineProgram_);
    this.uniformMatrix4(this.cubeLine_pMatrixUniform_, this.pMatrix_);
    this.useProgram(this.axisProgram_);
    this.uniformMatrix4(this.axis_pMatrixUniform_, this.pMatrix_);
  }

  private void deleteAllGLResources() {
    if (this.cubeProgram_.isNull()) {
      return;
    }
    if (!this.intersectionLinesProgram_.isNull()) {
      this.detachShader(this.intersectionLinesProgram_, this.intersectionLinesFragmentShader_);
      this.detachShader(this.intersectionLinesProgram_, this.vertexShader2D_);
      this.deleteShader(this.intersectionLinesFragmentShader_);
      this.deleteProgram(this.intersectionLinesProgram_);
      this.intersectionLinesProgram_.clear();
    }
    if (!this.clippingPlaneProgram_.isNull()) {
      this.detachShader(this.clippingPlaneProgram_, this.clippingPlaneFragShader_);
      this.detachShader(this.clippingPlaneProgram_, this.clippingPlaneVertexShader_);
      this.deleteShader(this.clippingPlaneFragShader_);
      this.deleteShader(this.clippingPlaneVertexShader_);
      this.deleteProgram(this.clippingPlaneProgram_);
      this.clippingPlaneProgram_.clear();
    }
    this.deleteBuffer(this.cubeBuffer_);
    this.deleteBuffer(this.cubeNormalsBuffer_);
    this.deleteBuffer(this.cubeIndicesBuffer_);
    this.deleteBuffer(this.cubeLineNormalsBuffer_);
    this.deleteBuffer(this.cubeLineIndicesBuffer_);
    this.deleteBuffer(this.axisBuffer_);
    this.deleteBuffer(this.axisIndicesBuffer_);
    this.deleteBuffer(this.axisInPlaneBuffer_);
    this.deleteBuffer(this.axisPlaneNormalBuffer_);
    this.deleteBuffer(this.axisOutOfPlaneNormalBuffer_);
    this.deleteBuffer(this.axisVertBuffer_);
    this.deleteBuffer(this.axisIndicesVertBuffer_);
    this.deleteBuffer(this.axisInPlaneVertBuffer_);
    this.deleteBuffer(this.axisPlaneNormalVertBuffer_);
    this.deleteBuffer(this.axisOutOfPlaneNormalVertBuffer_);
    this.deleteGLTextures();
    this.deleteBuffer(this.cubeTexCoords_);
    this.cubeTexCoords_.clear();
    this.deleteBuffer(this.axisTexCoordsHoriz_);
    this.axisTexCoordsHoriz_.clear();
    this.deleteBuffer(this.axisTexCoordsVert_);
    this.axisTexCoordsVert_.clear();
    if (!this.cubeProgram_.isNull()) {
      this.detachShader(this.cubeProgram_, this.fragmentShader_);
      this.detachShader(this.cubeProgram_, this.vertexShader_);
    }
    if (!this.axisProgram_.isNull()) {
      this.detachShader(this.axisProgram_, this.fragmentShader2_);
      this.detachShader(this.axisProgram_, this.vertexShader2_);
    }
    this.deleteShader(this.fragmentShader_);
    this.deleteShader(this.vertexShader_);
    this.deleteShader(this.fragmentShader2_);
    this.deleteShader(this.vertexShader2_);
    this.deleteShader(this.cubeLineFragShader_);
    this.deleteShader(this.cubeLineVertShader_);
    this.deleteProgram(this.cubeProgram_);
    this.cubeProgram_.clear();
    this.deleteProgram(this.axisProgram_);
    this.axisProgram_.clear();
    this.deleteProgram(this.cubeLineProgram_);
    this.cubeLineProgram_.clear();
    this.deleteOffscreenBuffer();
    this.clearBinaryResources();
  }

  private void deleteGLTextures() {
    if (this.cubeProgram_.isNull()) {
      return;
    }
    this.deleteTexture(this.horizAxisTexture_);
    this.horizAxisTexture_.clear();
    this.deleteTexture(this.horizAxisTexture2_);
    this.horizAxisTexture2_.clear();
    this.deleteTexture(this.vertAxisTexture_);
    this.vertAxisTexture_.clear();
    this.deleteTexture(this.cubeTextureXY_);
    this.cubeTextureXY_.clear();
    this.deleteTexture(this.cubeTextureXZ_);
    this.cubeTextureXZ_.clear();
    this.deleteTexture(this.cubeTextureYZ_);
    this.cubeTextureYZ_.clear();
    if (!this.titleTexture_.isNull()) {
      this.deleteTexture(this.titleTexture_);
    }
    this.titleTexture_.clear();
    if (!this.legendTexture_.isNull()) {
      this.deleteTexture(this.legendTexture_);
    }
    this.legendTexture_.clear();
    if (!this.colorMapTexture_.isNull()) {
      this.deleteTexture(this.colorMapTexture_);
    }
    this.colorMapTexture_.clear();
    for (int i = 0; i < this.objectsToDelete.size(); ++i) {
      Object o = this.objectsToDelete.get(i);
      if (o.getClass().equals(WGLWidget.Buffer.class)) {
        WGLWidget.Buffer buf = ((WGLWidget.Buffer) this.objectsToDelete.get(i));
        if (!buf.isNull()) {
          this.deleteBuffer(buf);
        }
      } else {
        if (o.getClass().equals(WGLWidget.Texture.class)) {
          WGLWidget.Texture tex = ((WGLWidget.Texture) this.objectsToDelete.get(i));
          if (!tex.isNull()) {
            this.deleteTexture(tex);
          }
        } else {
          if (o.getClass().equals(WGLWidget.Shader.class)) {
            WGLWidget.Shader shader = ((WGLWidget.Shader) this.objectsToDelete.get(i));
            if (!shader.isNull()) {
              this.deleteShader(shader);
            }
          } else {
            if (o.getClass().equals(WGLWidget.Program.class)) {
              WGLWidget.Program prog = ((WGLWidget.Program) this.objectsToDelete.get(i));
              if (!prog.isNull()) {
                this.deleteProgram(prog);
              }
            } else {
              assert false;
            }
          }
        }
      }
    }
    this.objectsToDelete.clear();
  }

  private void initializeIntersectionLinesProgram() {
    this.intersectionLinesFragmentShader_ = this.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
    this.shaderSource(this.intersectionLinesFragmentShader_, intersectionLinesFragmentShaderSrc);
    this.compileShader(this.intersectionLinesFragmentShader_);
    this.intersectionLinesProgram_ = this.createProgram();
    this.attachShader(this.intersectionLinesProgram_, this.vertexShader2D_);
    this.attachShader(this.intersectionLinesProgram_, this.intersectionLinesFragmentShader_);
    this.linkProgram(this.intersectionLinesProgram_);
    this.useProgram(this.intersectionLinesProgram_);
    this.intersectionLines_vertexPositionAttribute_ =
        this.getAttribLocation(this.intersectionLinesProgram_, "aVertexPosition");
    this.intersectionLines_vertexTextureCoAttribute_ =
        this.getAttribLocation(this.intersectionLinesProgram_, "aTextureCo");
    this.intersectionLines_viewportWidthUniform_ =
        this.getUniformLocation(this.intersectionLinesProgram_, "uVPwidth");
    this.intersectionLines_viewportHeightUniform_ =
        this.getUniformLocation(this.intersectionLinesProgram_, "uVPheight");
    this.intersectionLines_cameraUniform_ =
        this.getUniformLocation(this.intersectionLinesProgram_, "uCamera");
    this.intersectionLines_colorUniform_ =
        this.getUniformLocation(this.intersectionLinesProgram_, "uColor");
    this.intersectionLines_positionSamplerUniform_ =
        this.getUniformLocation(this.intersectionLinesProgram_, "uPositionSampler");
    this.intersectionLines_meshIndexSamplerUniform_ =
        this.getUniformLocation(this.intersectionLinesProgram_, "uMeshIndexSampler");
  }

  private void initializeClippingPlaneProgram() {
    this.clippingPlaneFragShader_ = this.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
    this.shaderSource(this.clippingPlaneFragShader_, clippingPlaneFragShaderSrc);
    this.compileShader(this.clippingPlaneFragShader_);
    this.clippingPlaneVertexShader_ = this.createShader(WGLWidget.GLenum.VERTEX_SHADER);
    this.shaderSource(this.clippingPlaneVertexShader_, clippingPlaneVertexShaderSrc);
    this.compileShader(this.clippingPlaneVertexShader_);
    this.clippingPlaneProgram_ = this.createProgram();
    this.attachShader(this.clippingPlaneProgram_, this.clippingPlaneVertexShader_);
    this.attachShader(this.clippingPlaneProgram_, this.clippingPlaneFragShader_);
    this.linkProgram(this.clippingPlaneProgram_);
    this.useProgram(this.clippingPlaneProgram_);
    this.clippingPlane_vertexPositionAttribute_ =
        this.getAttribLocation(this.clippingPlaneProgram_, "aVertexPosition");
    this.clippingPlane_mvMatrixUniform_ =
        this.getUniformLocation(this.clippingPlaneProgram_, "uMVMatrix");
    this.clippingPlane_pMatrixUniform_ =
        this.getUniformLocation(this.clippingPlaneProgram_, "uPMatrix");
    this.clippingPlane_cMatrixUniform_ =
        this.getUniformLocation(this.clippingPlaneProgram_, "uCMatrix");
    this.clippingPlane_clipPtUniform_ =
        this.getUniformLocation(this.clippingPlaneProgram_, "uClipPt");
    this.clippingPlane_dataMinPtUniform_ =
        this.getUniformLocation(this.clippingPlaneProgram_, "uDataMinPt");
    this.clippingPlane_dataMaxPtUniform_ =
        this.getUniformLocation(this.clippingPlaneProgram_, "uDataMaxPt");
    this.clippingPlane_clippingAxis_ =
        this.getUniformLocation(this.clippingPlaneProgram_, "uClippingAxis");
    this.clippingPlane_drawPositionUniform_ =
        this.getUniformLocation(this.clippingPlaneProgram_, "uDrawPosition");
  }

  private void initOffscreenBuffer() {
    this.offscreenDepthbuffer_ = this.getCreateRenderbuffer();
    this.intersectionLinesFramebuffer_ = this.getCreateFramebuffer();
    this.intersectionLinesTexture_ = this.createTexture();
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.intersectionLinesTexture_);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.NEAREST);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.NEAREST);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.meshIndexFramebuffer_ = this.getCreateFramebuffer();
    this.meshIndexTexture_ = this.createTexture();
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.meshIndexTexture_);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.NEAREST);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.NEAREST);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.positionFramebuffer_ = this.getCreateFramebuffer();
    this.positionTexture_ = this.createTexture();
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.positionTexture_);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.NEAREST);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.NEAREST);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.resizeOffscreenBuffer();
  }

  private void resizeOffscreenBuffer() {
    int w = (int) this.getWidth().getValue();
    int h = (int) this.getHeight().getValue();
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.intersectionLinesTexture_);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D, 0, WGLWidget.GLenum.RGBA, w, h, 0, WGLWidget.GLenum.RGBA);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.meshIndexTexture_);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D, 0, WGLWidget.GLenum.RGB, w, h, 0, WGLWidget.GLenum.RGB);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.positionTexture_);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D, 0, WGLWidget.GLenum.RGB, w, h, 0, WGLWidget.GLenum.RGB);
    this.bindRenderbuffer(WGLWidget.GLenum.RENDERBUFFER, this.offscreenDepthbuffer_);
    this.renderbufferStorage(
        WGLWidget.GLenum.RENDERBUFFER, WGLWidget.GLenum.DEPTH_COMPONENT16, w, h);
    this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.intersectionLinesFramebuffer_);
    this.framebufferTexture2D(
        WGLWidget.GLenum.FRAMEBUFFER,
        WGLWidget.GLenum.COLOR_ATTACHMENT0,
        WGLWidget.GLenum.TEXTURE_2D,
        this.intersectionLinesTexture_,
        0);
    this.framebufferRenderbuffer(
        WGLWidget.GLenum.FRAMEBUFFER,
        WGLWidget.GLenum.DEPTH_ATTACHMENT,
        WGLWidget.GLenum.RENDERBUFFER,
        this.offscreenDepthbuffer_);
    this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.meshIndexFramebuffer_);
    this.framebufferTexture2D(
        WGLWidget.GLenum.FRAMEBUFFER,
        WGLWidget.GLenum.COLOR_ATTACHMENT0,
        WGLWidget.GLenum.TEXTURE_2D,
        this.meshIndexTexture_,
        0);
    this.framebufferRenderbuffer(
        WGLWidget.GLenum.FRAMEBUFFER,
        WGLWidget.GLenum.DEPTH_ATTACHMENT,
        WGLWidget.GLenum.RENDERBUFFER,
        this.offscreenDepthbuffer_);
    this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.positionFramebuffer_);
    this.framebufferTexture2D(
        WGLWidget.GLenum.FRAMEBUFFER,
        WGLWidget.GLenum.COLOR_ATTACHMENT0,
        WGLWidget.GLenum.TEXTURE_2D,
        this.positionTexture_,
        0);
    this.framebufferRenderbuffer(
        WGLWidget.GLenum.FRAMEBUFFER,
        WGLWidget.GLenum.DEPTH_ATTACHMENT,
        WGLWidget.GLenum.RENDERBUFFER,
        this.offscreenDepthbuffer_);
    this.bindRenderbuffer(WGLWidget.GLenum.RENDERBUFFER, new WGLWidget.Renderbuffer());
    this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, new WGLWidget.Framebuffer());
  }

  private void deleteOffscreenBuffer() {
    if (!this.offscreenDepthbuffer_.isNull()) {
      this.deleteRenderbuffer(this.offscreenDepthbuffer_);
      this.offscreenDepthbuffer_.clear();
    }
    if (!this.intersectionLinesFramebuffer_.isNull()) {
      this.deleteFramebuffer(this.intersectionLinesFramebuffer_);
      this.intersectionLinesFramebuffer_.clear();
    }
    if (!this.positionFramebuffer_.isNull()) {
      this.deleteFramebuffer(this.positionFramebuffer_);
      this.positionFramebuffer_.clear();
    }
    if (!this.meshIndexFramebuffer_.isNull()) {
      this.deleteFramebuffer(this.meshIndexFramebuffer_);
      this.meshIndexFramebuffer_.clear();
    }
    if (!this.intersectionLinesTexture_.isNull()) {
      this.deleteTexture(this.intersectionLinesTexture_);
      this.intersectionLinesTexture_.clear();
    }
    if (!this.meshIndexTexture_.isNull()) {
      this.deleteTexture(this.meshIndexTexture_);
      this.meshIndexTexture_.clear();
    }
    if (!this.positionTexture_.isNull()) {
      this.deleteTexture(this.positionTexture_);
      this.positionTexture_.clear();
    }
  }

  private void renderIntersectionLines() {
    this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.meshIndexFramebuffer_);
    this.clearColor(1.0, 1.0, 1.0, 1.0);
    this.clear(EnumSet.of(WGLWidget.GLenum.COLOR_BUFFER_BIT, WGLWidget.GLenum.DEPTH_BUFFER_BIT));
    for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
      WAbstractGridData gridData =
          ObjectUtils.cast(this.dataSeriesVector_.get(i), WAbstractGridData.class);
      if (gridData != null && gridData.getType() == Series3DType.Surface) {
        gridData.paintGLIndex(i);
      }
    }
    this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.positionFramebuffer_);
    this.clear(EnumSet.of(WGLWidget.GLenum.COLOR_BUFFER_BIT, WGLWidget.GLenum.DEPTH_BUFFER_BIT));
    for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
      WAbstractGridData gridData =
          ObjectUtils.cast(this.dataSeriesVector_.get(i), WAbstractGridData.class);
      if (gridData != null && gridData.getType() == Series3DType.Surface) {
        gridData.paintGLPositions();
      }
    }
    this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.intersectionLinesFramebuffer_);
    this.disable(WGLWidget.GLenum.CULL_FACE);
    this.disable(WGLWidget.GLenum.DEPTH_TEST);
    this.enable(WGLWidget.GLenum.BLEND);
    this.useProgram(this.intersectionLinesProgram_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.overlayPosBuffer_);
    this.vertexAttribPointer(
        this.intersectionLines_vertexPositionAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.intersectionLines_vertexPositionAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.overlayTexCoBuffer_);
    this.vertexAttribPointer(
        this.intersectionLines_vertexTextureCoAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.intersectionLines_vertexTextureCoAttribute_);
    this.uniformMatrix4(this.intersectionLines_cameraUniform_, this.jsMatrix_);
    this.uniform1f(this.intersectionLines_viewportWidthUniform_, this.getWidth().getValue());
    this.uniform1f(this.intersectionLines_viewportHeightUniform_, this.getHeight().getValue());
    this.uniform4f(
        this.intersectionLines_colorUniform_,
        this.intersectionLinesColor_.getRed() / 255.0,
        this.intersectionLinesColor_.getGreen() / 255.0,
        this.intersectionLinesColor_.getBlue() / 255.0,
        this.intersectionLinesColor_.getAlpha() / 255.0);
    this.activeTexture(WGLWidget.GLenum.TEXTURE0);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.positionTexture_);
    this.uniform1i(this.intersectionLines_positionSamplerUniform_, 0);
    this.activeTexture(WGLWidget.GLenum.TEXTURE1);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.meshIndexTexture_);
    this.uniform1i(this.intersectionLines_meshIndexSamplerUniform_, 1);
    this.drawArrays(WGLWidget.GLenum.TRIANGLE_STRIP, 0, 4);
    this.disableVertexAttribArray(this.intersectionLines_vertexPositionAttribute_);
    this.disableVertexAttribArray(this.intersectionLines_vertexTextureCoAttribute_);
    this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, new WGLWidget.Framebuffer());
    this.enable(WGLWidget.GLenum.CULL_FACE);
    this.enable(WGLWidget.GLenum.DEPTH_TEST);
    this.disable(WGLWidget.GLenum.BLEND);
  }

  private void renderIntersectionLinesWithInvisiblePlanes() {
    for (int i = 0; i < this.intersectionPlanes_.size(); ++i) {
      this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.meshIndexFramebuffer_);
      this.clearColor(1.0, 1.0, 1.0, 1.0);
      this.clear(EnumSet.of(WGLWidget.GLenum.COLOR_BUFFER_BIT, WGLWidget.GLenum.DEPTH_BUFFER_BIT));
      for (int j = 0; j < this.dataSeriesVector_.size(); j++) {
        WAbstractGridData gridData =
            ObjectUtils.cast(this.dataSeriesVector_.get(j), WAbstractGridData.class);
        if (gridData != null && gridData.getType() == Series3DType.Surface) {
          gridData.paintGLIndex(1);
        }
      }
      this.disable(WGLWidget.GLenum.CULL_FACE);
      this.enable(WGLWidget.GLenum.DEPTH_TEST);
      this.useProgram(this.clippingPlaneProgram_);
      double minX = this.xAxis_.getMinimum();
      double maxX = this.xAxis_.getMaximum();
      double minY = this.yAxis_.getMinimum();
      double maxY = this.yAxis_.getMaximum();
      double minZ = this.zAxis_.getMinimum();
      double maxZ = this.zAxis_.getMaximum();
      WCartesian3DChart.IntersectionPlane plane = this.intersectionPlanes_.get(i);
      if (plane.axis == Axis.X3D) {
        this.uniform1i(this.clippingPlane_clippingAxis_, 0);
      } else {
        if (plane.axis == Axis.Y3D) {
          this.uniform1i(this.clippingPlane_clippingAxis_, 1);
        } else {
          this.uniform1i(this.clippingPlane_clippingAxis_, 2);
        }
      }
      this.uniform3f(
          this.clippingPlane_clipPtUniform_, plane.position, plane.position, plane.position);
      this.uniform3f(this.clippingPlane_dataMinPtUniform_, minX, minY, minZ);
      this.uniform3f(this.clippingPlane_dataMaxPtUniform_, maxX, maxY, maxZ);
      this.uniform1i(this.clippingPlane_drawPositionUniform_, 0);
      javax.vecmath.Matrix4f mvMatrix =
          new javax.vecmath.Matrix4f(
              1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
              0.0f, 1.0f);
      this.uniformMatrix4(this.clippingPlane_pMatrixUniform_, this.pMatrix_);
      this.uniformMatrix4(this.clippingPlane_mvMatrixUniform_, mvMatrix);
      this.uniformMatrix4(this.clippingPlane_cMatrixUniform_, this.jsMatrix_);
      this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.clippingPlaneVertBuffer_);
      this.vertexAttribPointer(
          this.clippingPlane_vertexPositionAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.enableVertexAttribArray(this.clippingPlane_vertexPositionAttribute_);
      this.drawArrays(WGLWidget.GLenum.TRIANGLE_STRIP, 0, 4);
      this.enable(WGLWidget.GLenum.CULL_FACE);
      this.disable(WGLWidget.GLenum.DEPTH_TEST);
      this.disableVertexAttribArray(this.clippingPlane_vertexPositionAttribute_);
      this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.positionFramebuffer_);
      this.clear(EnumSet.of(WGLWidget.GLenum.COLOR_BUFFER_BIT, WGLWidget.GLenum.DEPTH_BUFFER_BIT));
      for (int j = 0; j < this.dataSeriesVector_.size(); j++) {
        WAbstractGridData gridData =
            ObjectUtils.cast(this.dataSeriesVector_.get(j), WAbstractGridData.class);
        if (gridData != null && gridData.getType() == Series3DType.Surface) {
          gridData.paintGLPositions();
        }
      }
      this.disable(WGLWidget.GLenum.CULL_FACE);
      this.enable(WGLWidget.GLenum.DEPTH_TEST);
      this.useProgram(this.clippingPlaneProgram_);
      this.uniform1i(this.clippingPlane_drawPositionUniform_, 1);
      this.uniformMatrix4(this.clippingPlane_pMatrixUniform_, this.pMatrix_);
      this.uniformMatrix4(this.clippingPlane_mvMatrixUniform_, mvMatrix);
      this.uniformMatrix4(this.clippingPlane_cMatrixUniform_, this.jsMatrix_);
      this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.clippingPlaneVertBuffer_);
      this.vertexAttribPointer(
          this.clippingPlane_vertexPositionAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.enableVertexAttribArray(this.clippingPlane_vertexPositionAttribute_);
      this.drawArrays(WGLWidget.GLenum.TRIANGLE_STRIP, 0, 4);
      this.enable(WGLWidget.GLenum.CULL_FACE);
      this.disable(WGLWidget.GLenum.DEPTH_TEST);
      this.disableVertexAttribArray(this.clippingPlane_vertexPositionAttribute_);
      this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.intersectionLinesFramebuffer_);
      this.disable(WGLWidget.GLenum.CULL_FACE);
      this.disable(WGLWidget.GLenum.DEPTH_TEST);
      this.enable(WGLWidget.GLenum.BLEND);
      this.useProgram(this.intersectionLinesProgram_);
      this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.overlayPosBuffer_);
      this.vertexAttribPointer(
          this.intersectionLines_vertexPositionAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.enableVertexAttribArray(this.intersectionLines_vertexPositionAttribute_);
      this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.overlayTexCoBuffer_);
      this.vertexAttribPointer(
          this.intersectionLines_vertexTextureCoAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.enableVertexAttribArray(this.intersectionLines_vertexTextureCoAttribute_);
      this.uniformMatrix4(this.intersectionLines_cameraUniform_, this.jsMatrix_);
      this.uniform1f(this.intersectionLines_viewportWidthUniform_, this.getWidth().getValue());
      this.uniform1f(this.intersectionLines_viewportHeightUniform_, this.getHeight().getValue());
      this.uniform4f(
          this.intersectionLines_colorUniform_,
          plane.color.getRed() / 255.0,
          plane.color.getGreen() / 255.0,
          plane.color.getBlue() / 255.0,
          plane.color.getAlpha() / 255.0);
      this.activeTexture(WGLWidget.GLenum.TEXTURE0);
      this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.positionTexture_);
      this.uniform1i(this.intersectionLines_positionSamplerUniform_, 0);
      this.activeTexture(WGLWidget.GLenum.TEXTURE1);
      this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.meshIndexTexture_);
      this.uniform1i(this.intersectionLines_meshIndexSamplerUniform_, 1);
      this.drawArrays(WGLWidget.GLenum.TRIANGLE_STRIP, 0, 4);
      this.disableVertexAttribArray(this.intersectionLines_vertexPositionAttribute_);
      this.disableVertexAttribArray(this.intersectionLines_vertexTextureCoAttribute_);
      this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, new WGLWidget.Framebuffer());
      this.enable(WGLWidget.GLenum.CULL_FACE);
      this.enable(WGLWidget.GLenum.DEPTH_TEST);
      this.disable(WGLWidget.GLenum.BLEND);
    }
  }

  private void renderClippingLines(WAbstractGridData data) {
    this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.intersectionLinesFramebuffer_);
    this.clearColor(0.0, 0.0, 0.0, 0.0);
    this.clear(EnumSet.of(WGLWidget.GLenum.COLOR_BUFFER_BIT, WGLWidget.GLenum.DEPTH_BUFFER_BIT));
    this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, new WGLWidget.Framebuffer());
    for (int i = 0; i < 6; ++i) {
      this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.meshIndexFramebuffer_);
      this.clearColor(1.0, 1.0, 1.0, 1.0);
      this.clear(EnumSet.of(WGLWidget.GLenum.COLOR_BUFFER_BIT, WGLWidget.GLenum.DEPTH_BUFFER_BIT));
      this.disable(WGLWidget.GLenum.CULL_FACE);
      this.enable(WGLWidget.GLenum.DEPTH_TEST);
      double minX = this.xAxis_.getMinimum();
      double maxX = this.xAxis_.getMaximum();
      double minY = this.yAxis_.getMinimum();
      double maxY = this.yAxis_.getMaximum();
      double minZ = this.zAxis_.getMinimum();
      double maxZ = this.zAxis_.getMaximum();
      this.useProgram(this.clippingPlaneProgram_);
      int clippingAxis = i / 2;
      this.uniform1i(this.clippingPlane_clippingAxis_, clippingAxis);
      if (i % 2 == 0) {
        this.uniform3fv(this.clippingPlane_clipPtUniform_, data.jsMaxPt_);
      } else {
        this.uniform3fv(this.clippingPlane_clipPtUniform_, data.jsMinPt_);
      }
      this.uniform3f(this.clippingPlane_dataMinPtUniform_, minX, minY, minZ);
      this.uniform3f(this.clippingPlane_dataMaxPtUniform_, maxX, maxY, maxZ);
      this.uniform1i(this.clippingPlane_drawPositionUniform_, 0);
      javax.vecmath.Matrix4f mvMatrix =
          new javax.vecmath.Matrix4f(
              1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
              0.0f, 1.0f);
      this.uniformMatrix4(this.clippingPlane_pMatrixUniform_, this.pMatrix_);
      this.uniformMatrix4(this.clippingPlane_mvMatrixUniform_, mvMatrix);
      this.uniformMatrix4(this.clippingPlane_cMatrixUniform_, this.jsMatrix_);
      this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.clippingPlaneVertBuffer_);
      this.vertexAttribPointer(
          this.clippingPlane_vertexPositionAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.enableVertexAttribArray(this.clippingPlane_vertexPositionAttribute_);
      this.drawArrays(WGLWidget.GLenum.TRIANGLE_STRIP, 0, 4);
      this.disableVertexAttribArray(this.clippingPlane_vertexPositionAttribute_);
      data.paintGLIndex(
          1,
          clippingAxis == 0 ? 0.01 : 0.0,
          clippingAxis == 1 ? 0.01 : 0.0,
          clippingAxis == 2 ? 0.01 : 0.0);
      for (int j = 0; j < this.dataSeriesVector_.size(); ++j) {
        if (this.dataSeriesVector_.get(j) != data) {
          WAbstractGridData gridData =
              ObjectUtils.cast(this.dataSeriesVector_.get(j), WAbstractGridData.class);
          if (gridData != null
              && gridData.getType() == Series3DType.Surface
              && gridData.isClippingLinesEnabled()) {
            gridData.paintGLIndex(0xffffff);
          }
        }
      }
      this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.positionFramebuffer_);
      this.clear(EnumSet.of(WGLWidget.GLenum.COLOR_BUFFER_BIT, WGLWidget.GLenum.DEPTH_BUFFER_BIT));
      this.disable(WGLWidget.GLenum.CULL_FACE);
      this.enable(WGLWidget.GLenum.DEPTH_TEST);
      this.useProgram(this.clippingPlaneProgram_);
      this.uniform1i(this.clippingPlane_drawPositionUniform_, 1);
      this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.clippingPlaneVertBuffer_);
      this.vertexAttribPointer(
          this.clippingPlane_vertexPositionAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.enableVertexAttribArray(this.clippingPlane_vertexPositionAttribute_);
      this.drawArrays(WGLWidget.GLenum.TRIANGLE_STRIP, 0, 4);
      this.disableVertexAttribArray(this.clippingPlane_vertexPositionAttribute_);
      data.paintGLPositions(
          clippingAxis == 0 ? 0.01 : 0.0,
          clippingAxis == 1 ? 0.01 : 0.0,
          clippingAxis == 2 ? 0.01 : 0.0);
      for (int j = 0; j < this.dataSeriesVector_.size(); ++j) {
        if (this.dataSeriesVector_.get(j) != data) {
          WAbstractGridData gridData =
              ObjectUtils.cast(this.dataSeriesVector_.get(j), WAbstractGridData.class);
          if (gridData != null
              && gridData.getType() == Series3DType.Surface
              && gridData.isClippingLinesEnabled()) {
            gridData.paintGLPositions();
          }
        }
      }
      this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, this.intersectionLinesFramebuffer_);
      this.disable(WGLWidget.GLenum.CULL_FACE);
      this.disable(WGLWidget.GLenum.DEPTH_TEST);
      this.enable(WGLWidget.GLenum.BLEND);
      this.useProgram(this.intersectionLinesProgram_);
      this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.overlayPosBuffer_);
      this.vertexAttribPointer(
          this.intersectionLines_vertexPositionAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.enableVertexAttribArray(this.intersectionLines_vertexPositionAttribute_);
      this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, this.overlayTexCoBuffer_);
      this.vertexAttribPointer(
          this.intersectionLines_vertexTextureCoAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
      this.enableVertexAttribArray(this.intersectionLines_vertexTextureCoAttribute_);
      this.uniformMatrix4(this.intersectionLines_cameraUniform_, this.jsMatrix_);
      this.uniform1f(this.intersectionLines_viewportWidthUniform_, this.getWidth().getValue());
      this.uniform1f(this.intersectionLines_viewportHeightUniform_, this.getHeight().getValue());
      this.uniform4f(
          this.intersectionLines_colorUniform_,
          data.getClippingLinesColor().getRed() / 255.0,
          data.getClippingLinesColor().getGreen() / 255.0,
          data.getClippingLinesColor().getBlue() / 255.0,
          data.getClippingLinesColor().getAlpha() / 255.0);
      this.activeTexture(WGLWidget.GLenum.TEXTURE0);
      this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.positionTexture_);
      this.uniform1i(this.intersectionLines_positionSamplerUniform_, 0);
      this.activeTexture(WGLWidget.GLenum.TEXTURE1);
      this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.meshIndexTexture_);
      this.uniform1i(this.intersectionLines_meshIndexSamplerUniform_, 1);
      this.drawArrays(WGLWidget.GLenum.TRIANGLE_STRIP, 0, 4);
      this.disableVertexAttribArray(this.intersectionLines_vertexPositionAttribute_);
      this.disableVertexAttribArray(this.intersectionLines_vertexTextureCoAttribute_);
      this.bindFramebuffer(WGLWidget.GLenum.FRAMEBUFFER, new WGLWidget.Framebuffer());
      this.enable(WGLWidget.GLenum.CULL_FACE);
      this.enable(WGLWidget.GLenum.DEPTH_TEST);
      this.disable(WGLWidget.GLenum.BLEND);
    }
  }

  private void paintHorizAxisTextures(WPaintDevice paintDevice, boolean labelAngleMirrored) {
    if (this.textureScaling_ == 0) {
      throw new WException("WCartesian3DChart: axes not initialized properly");
    }
    double oldLabelAngleX = 0.0;
    double oldLabelAngleY = 0.0;
    if (labelAngleMirrored) {
      this.xAxis_.setRenderMirror(true);
      this.yAxis_.setRenderMirror(true);
      oldLabelAngleX = this.xAxis_.getLabelAngle();
      this.xAxis_.setLabelAngle(-oldLabelAngleX);
      oldLabelAngleY = this.yAxis_.getLabelAngle();
      this.yAxis_.setLabelAngle(-oldLabelAngleY);
    }
    WPainter painter = new WPainter(paintDevice);
    painter.begin(paintDevice);
    int axisOffset = (int) (this.axisRenderWidth_ / this.textureScaling_ / 1.6 * 0.3);
    int axisWidth = this.axisRenderWidth_ / this.textureScaling_;
    int axisHeight = this.axisRenderHeight_ / this.textureScaling_;
    WPointF axisStart = new WPointF();
    WPointF axisEnd = new WPointF();
    double tickStart;
    double tickEnd;
    double labelPos;
    AlignmentFlag labelHFlag;
    AlignmentFlag labelVFlag;
    WPainterPath clippy = new WPainterPath();
    clippy.addRect(new WRectF(0, 0, this.axisRenderWidth_, this.axisRenderHeight_));
    painter.setClipPath(clippy);
    painter.setClipping(true);
    painter.scale(this.textureScaling_, this.textureScaling_);
    axisStart = new WPointF(axisOffset, 0.0);
    axisEnd = new WPointF(axisWidth - axisOffset, 0.0);
    tickStart = 0.0;
    tickEnd = TICKLENGTH;
    labelPos = tickEnd;
    labelHFlag = AlignmentFlag.Center;
    labelVFlag = AlignmentFlag.Top;
    if (this.xAxis_.getLabelAngle() > ANGLE1) {
      labelHFlag = labelPos > 0 ? AlignmentFlag.Right : AlignmentFlag.Left;
      if (this.xAxis_.getLabelAngle() > ANGLE2) {
        labelVFlag = AlignmentFlag.Middle;
      }
    } else {
      if (this.xAxis_.getLabelAngle() < -ANGLE1) {
        labelHFlag = labelPos > 0 ? AlignmentFlag.Left : AlignmentFlag.Right;
        if (this.xAxis_.getLabelAngle() < -ANGLE2) {
          labelVFlag = AlignmentFlag.Middle;
        }
      }
    }
    this.xAxis_.render(
        painter,
        EnumSet.of(AxisProperty.Line, AxisProperty.Labels),
        axisStart,
        axisEnd,
        tickStart,
        tickEnd,
        labelPos,
        EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
    double addOffset = this.xAxis_.getTitleOffset();
    WFont oldFont = painter.getFont();
    painter.setFont(this.xAxis_.getTitleFont());
    painter.drawText(
        new WRectF(0, TITLEOFFSET + addOffset, axisWidth, axisHeight - TITLEOFFSET - addOffset),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Top),
        this.xAxis_.getTitle());
    painter.scale(1.0 / this.textureScaling_, 1.0 / this.textureScaling_);
    painter.translate(0, this.axisRenderHeight_);
    painter.setClipPath(clippy);
    painter.scale(this.textureScaling_, this.textureScaling_);
    axisEnd = new WPointF(axisOffset, 0.0);
    axisStart = new WPointF(axisWidth - axisOffset, 0.0);
    tickStart = 0.0;
    tickEnd = TICKLENGTH;
    labelPos = tickEnd;
    this.xAxis_.render(
        painter,
        EnumSet.of(AxisProperty.Line, AxisProperty.Labels),
        axisStart,
        axisEnd,
        tickStart,
        tickEnd,
        labelPos,
        EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
    painter.drawText(
        new WRectF(0, TITLEOFFSET + addOffset, axisWidth, axisHeight - TITLEOFFSET - addOffset),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Top),
        this.xAxis_.getTitle());
    painter.scale(1.0 / this.textureScaling_, 1.0 / this.textureScaling_);
    painter.translate(0, this.axisRenderHeight_);
    painter.setClipPath(clippy);
    painter.scale(this.textureScaling_, this.textureScaling_);
    axisStart = new WPointF(axisOffset, axisHeight);
    axisEnd = new WPointF(axisWidth - axisOffset, axisHeight);
    tickStart = -TICKLENGTH;
    tickEnd = 0.0;
    labelPos = tickEnd - 4;
    labelHFlag = AlignmentFlag.Center;
    labelVFlag = AlignmentFlag.Bottom;
    if (this.xAxis_.getLabelAngle() > ANGLE1) {
      labelHFlag = labelPos > 0 ? AlignmentFlag.Right : AlignmentFlag.Left;
      if (this.xAxis_.getLabelAngle() > ANGLE2) {
        labelVFlag = AlignmentFlag.Middle;
      }
    } else {
      if (this.xAxis_.getLabelAngle() < -ANGLE1) {
        labelHFlag = labelPos > 0 ? AlignmentFlag.Left : AlignmentFlag.Right;
        if (this.xAxis_.getLabelAngle() < -ANGLE2) {
          labelVFlag = AlignmentFlag.Middle;
        }
      }
    }
    this.xAxis_.render(
        painter,
        EnumSet.of(AxisProperty.Line, AxisProperty.Labels),
        axisStart,
        axisEnd,
        tickStart,
        tickEnd,
        labelPos,
        EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
    painter.drawText(
        new WRectF(0, 0, axisWidth, axisHeight - TITLEOFFSET - addOffset),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Bottom),
        this.xAxis_.getTitle());
    painter.scale(1.0 / this.textureScaling_, 1.0 / this.textureScaling_);
    painter.translate(0, this.axisRenderHeight_);
    painter.setClipPath(clippy);
    painter.scale(this.textureScaling_, this.textureScaling_);
    axisEnd = new WPointF(axisOffset, axisHeight);
    axisStart = new WPointF(axisWidth - axisOffset, axisHeight);
    tickStart = -TICKLENGTH;
    tickEnd = 0.0;
    labelPos = tickEnd - 4;
    this.xAxis_.render(
        painter,
        EnumSet.of(AxisProperty.Line, AxisProperty.Labels),
        axisStart,
        axisEnd,
        tickStart,
        tickEnd,
        labelPos,
        EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
    painter.drawText(
        new WRectF(0, 0, axisWidth, axisHeight - TITLEOFFSET - addOffset),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Bottom),
        this.xAxis_.getTitle());
    painter.setFont(oldFont);
    painter.scale(1.0 / this.textureScaling_, 1.0 / this.textureScaling_);
    painter.translate(0, this.axisRenderHeight_);
    painter.setClipPath(clippy);
    painter.scale(this.textureScaling_, this.textureScaling_);
    axisStart = new WPointF(axisOffset, 0.0);
    axisEnd = new WPointF(axisWidth - axisOffset, 0.0);
    tickStart = 0.0;
    tickEnd = TICKLENGTH;
    labelPos = tickEnd;
    labelHFlag = AlignmentFlag.Center;
    labelVFlag = AlignmentFlag.Top;
    if (this.yAxis_.getLabelAngle() > ANGLE1) {
      labelHFlag = labelPos > 0 ? AlignmentFlag.Right : AlignmentFlag.Left;
      if (this.yAxis_.getLabelAngle() > ANGLE2) {
        labelVFlag = AlignmentFlag.Middle;
      }
    } else {
      if (this.yAxis_.getLabelAngle() < -ANGLE1) {
        labelHFlag = labelPos > 0 ? AlignmentFlag.Left : AlignmentFlag.Right;
        if (this.yAxis_.getLabelAngle() < -ANGLE2) {
          labelVFlag = AlignmentFlag.Middle;
        }
      }
    }
    this.yAxis_.render(
        painter,
        EnumSet.of(AxisProperty.Line, AxisProperty.Labels),
        axisStart,
        axisEnd,
        tickStart,
        tickEnd,
        labelPos,
        EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
    addOffset = this.yAxis_.getTitleOffset();
    painter.setFont(this.yAxis_.getTitleFont());
    painter.drawText(
        new WRectF(0, TITLEOFFSET + addOffset, axisWidth, axisHeight - TITLEOFFSET - addOffset),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Top),
        this.yAxis_.getTitle());
    painter.scale(1.0 / this.textureScaling_, 1.0 / this.textureScaling_);
    painter.translate(0, this.axisRenderHeight_);
    painter.setClipPath(clippy);
    painter.scale(this.textureScaling_, this.textureScaling_);
    axisEnd = new WPointF(axisOffset, 0.0);
    axisStart = new WPointF(axisWidth - axisOffset, 0.0);
    tickStart = 0.0;
    tickEnd = TICKLENGTH;
    labelPos = tickEnd;
    this.yAxis_.render(
        painter,
        EnumSet.of(AxisProperty.Line, AxisProperty.Labels),
        axisStart,
        axisEnd,
        tickStart,
        tickEnd,
        labelPos,
        EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
    painter.drawText(
        new WRectF(0, TITLEOFFSET + addOffset, axisWidth, axisHeight - TITLEOFFSET - addOffset),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Top),
        this.yAxis_.getTitle());
    painter.scale(1.0 / this.textureScaling_, 1.0 / this.textureScaling_);
    painter.translate(0, this.axisRenderHeight_);
    painter.setClipPath(clippy);
    painter.scale(this.textureScaling_, this.textureScaling_);
    axisStart = new WPointF(axisOffset, axisHeight);
    axisEnd = new WPointF(axisWidth - axisOffset, axisHeight);
    tickStart = -TICKLENGTH;
    tickEnd = 0.0;
    labelPos = tickEnd - 4;
    labelHFlag = AlignmentFlag.Center;
    labelVFlag = AlignmentFlag.Bottom;
    if (this.yAxis_.getLabelAngle() > ANGLE1) {
      labelHFlag = labelPos > 0 ? AlignmentFlag.Right : AlignmentFlag.Left;
      if (this.yAxis_.getLabelAngle() > ANGLE2) {
        labelVFlag = AlignmentFlag.Middle;
      }
    } else {
      if (this.yAxis_.getLabelAngle() < -ANGLE1) {
        labelHFlag = labelPos > 0 ? AlignmentFlag.Left : AlignmentFlag.Right;
        if (this.yAxis_.getLabelAngle() < -ANGLE2) {
          labelVFlag = AlignmentFlag.Middle;
        }
      }
    }
    this.yAxis_.render(
        painter,
        EnumSet.of(AxisProperty.Line, AxisProperty.Labels),
        axisStart,
        axisEnd,
        tickStart,
        tickEnd,
        labelPos,
        EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
    painter.drawText(
        new WRectF(0, 0, axisWidth, axisHeight - TITLEOFFSET - addOffset),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Bottom),
        this.yAxis_.getTitle());
    painter.scale(1.0 / this.textureScaling_, 1.0 / this.textureScaling_);
    painter.translate(0, this.axisRenderHeight_);
    painter.setClipPath(clippy);
    painter.scale(this.textureScaling_, this.textureScaling_);
    axisEnd = new WPointF(axisOffset, axisHeight);
    axisStart = new WPointF(axisWidth - axisOffset, axisHeight);
    tickStart = -TICKLENGTH;
    tickEnd = 0.0;
    labelPos = tickEnd - 4;
    this.yAxis_.render(
        painter,
        EnumSet.of(AxisProperty.Line, AxisProperty.Labels),
        axisStart,
        axisEnd,
        tickStart,
        tickEnd,
        labelPos,
        EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
    painter.drawText(
        new WRectF(0, 0, axisWidth, axisHeight - TITLEOFFSET - addOffset),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Bottom),
        this.yAxis_.getTitle());
    painter.setFont(oldFont);
    if (labelAngleMirrored) {
      this.xAxis_.setLabelAngle(oldLabelAngleX);
      this.yAxis_.setLabelAngle(oldLabelAngleY);
      this.xAxis_.setRenderMirror(false);
      this.yAxis_.setRenderMirror(false);
    }
    painter.end();
  }

  private final void paintHorizAxisTextures(WPaintDevice paintDevice) {
    paintHorizAxisTextures(paintDevice, false);
  }

  private void paintVertAxisTextures(WPaintDevice paintDevice) {
    int axisOffset = (int) (this.axisRenderWidth_ / this.textureScaling_ / 1.6 * 0.3);
    int axisWidth = this.axisRenderWidth_ / this.textureScaling_;
    int axisHeight = this.axisRenderHeight_ / this.textureScaling_;
    WPainter painter = new WPainter(paintDevice);
    painter.scale(this.textureScaling_, this.textureScaling_);
    WPointF axisStart = new WPointF(axisHeight, axisWidth - axisOffset);
    WPointF axisEnd = new WPointF(axisHeight, axisOffset);
    double tickStart = -TICKLENGTH;
    double tickEnd = 0.0;
    double labelPos = tickEnd - 4;
    AlignmentFlag labelHFlag = AlignmentFlag.Right;
    AlignmentFlag labelVFlag = AlignmentFlag.Middle;
    if (this.zAxis_.getLabelAngle() > ANGLE1) {
      labelVFlag = labelPos < 0 ? AlignmentFlag.Bottom : AlignmentFlag.Top;
      if (this.zAxis_.getLabelAngle() > ANGLE2) {
        labelHFlag = AlignmentFlag.Center;
      }
    } else {
      if (this.zAxis_.getLabelAngle() < -ANGLE1) {
        labelVFlag = labelPos < 0 ? AlignmentFlag.Top : AlignmentFlag.Bottom;
        if (this.zAxis_.getLabelAngle() < -ANGLE2) {
          labelHFlag = AlignmentFlag.Center;
        }
      }
    }
    this.zAxis_.render(
        painter,
        EnumSet.of(AxisProperty.Line, AxisProperty.Labels),
        axisStart,
        axisEnd,
        tickStart,
        tickEnd,
        labelPos,
        EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
    double addOffset = this.zAxis_.getTitleOffset();
    painter.rotate(-90);
    WFont oldFont = this.zAxis_.getTitleFont();
    painter.setFont(this.zAxis_.getTitleFont());
    painter.drawText(
        new WRectF(-axisWidth, 0, axisWidth, axisHeight - TITLEOFFSET - addOffset),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Bottom),
        this.zAxis_.getTitle());
    painter.rotate(90);
    axisStart = new WPointF(axisHeight, axisWidth - axisOffset);
    axisEnd = new WPointF(axisHeight, axisOffset);
    tickStart = 0.0;
    tickEnd = TICKLENGTH;
    labelPos = tickEnd;
    labelHFlag = AlignmentFlag.Left;
    labelVFlag = AlignmentFlag.Middle;
    if (this.zAxis_.getLabelAngle() > ANGLE1) {
      labelVFlag = labelPos < 0 ? AlignmentFlag.Bottom : AlignmentFlag.Top;
      if (this.zAxis_.getLabelAngle() > ANGLE2) {
        labelHFlag = AlignmentFlag.Center;
      }
    } else {
      if (this.zAxis_.getLabelAngle() < -ANGLE1) {
        labelVFlag = labelPos < 0 ? AlignmentFlag.Top : AlignmentFlag.Bottom;
        if (this.zAxis_.getLabelAngle() < -ANGLE2) {
          labelHFlag = AlignmentFlag.Center;
        }
      }
    }
    this.zAxis_.render(
        painter,
        EnumSet.of(AxisProperty.Line, AxisProperty.Labels),
        axisStart,
        axisEnd,
        tickStart,
        tickEnd,
        labelPos,
        EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
    painter.rotate(-90);
    painter.drawText(
        new WRectF(
            -axisWidth,
            axisHeight + TITLEOFFSET + addOffset,
            axisWidth,
            axisHeight - TITLEOFFSET - addOffset),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Top),
        this.zAxis_.getTitle());
    painter.setFont(oldFont);
    painter.rotate(90);
    painter.end();
  }

  private void paintGridLines(WPaintDevice paintDevice, Plane plane) {
    int axisOffset =
        (int) ((double) this.axisRenderWidth_ / (double) this.textureScaling_ / 1.6 * 0.3);
    int axisWidth = this.axisRenderWidth_ / this.textureScaling_;
    int renderLength = axisWidth - 2 * axisOffset;
    WPainter painter = new WPainter(paintDevice);
    painter.scale(this.textureScaling_, this.textureScaling_);
    if (this.gridLinesPen_.getWidth().getValue() == 0) {
      this.gridLinesPen_.setWidth(new WLength(1));
    }
    painter.setPen(this.gridLinesPen_);
    switch (plane) {
      case XY:
        if (this.XYGridEnabled_[0]) {
          List<Double> pos = this.xAxis_.gridLinePositions(new AxisConfig());
          for (int i = 0; i < pos.size(); i++) {
            if (pos.get(i) == 0 || pos.get(i) == this.gridRenderWidth_) {
              continue;
            }
            int texPos =
                (int) (pos.get(i) / renderLength * (this.gridRenderWidth_ / this.textureScaling_));
            painter.drawLine(
                texPos + 0.5,
                0.5,
                texPos + 0.5,
                this.gridRenderWidth_ / this.textureScaling_ - 0.5);
          }
        }
        if (this.XYGridEnabled_[1]) {
          List<Double> pos = this.yAxis_.gridLinePositions(new AxisConfig());
          for (int i = 0; i < pos.size(); i++) {
            if (pos.get(i) == 0 || pos.get(i) == this.gridRenderWidth_) {
              continue;
            }
            int texPos =
                (int) (pos.get(i) / renderLength * (this.gridRenderWidth_ / this.textureScaling_));
            painter.drawLine(
                0.5,
                texPos + 0.5,
                this.gridRenderWidth_ / this.textureScaling_ - 0.5,
                texPos + 0.5);
          }
        }
        break;
      case XZ:
        if (this.XZGridEnabled_[0]) {
          List<Double> pos = this.xAxis_.gridLinePositions(new AxisConfig());
          for (int i = 0; i < pos.size(); i++) {
            if (pos.get(i) == 0 || pos.get(i) == this.gridRenderWidth_) {
              continue;
            }
            int texPos =
                (int) (pos.get(i) / renderLength * (this.gridRenderWidth_ / this.textureScaling_));
            painter.drawLine(
                texPos + 0.5,
                0.5,
                texPos + 0.5,
                this.gridRenderWidth_ / this.textureScaling_ - 0.5);
          }
        }
        if (this.XZGridEnabled_[1]) {
          List<Double> pos = this.zAxis_.gridLinePositions(new AxisConfig());
          for (int i = 0; i < pos.size(); i++) {
            if (pos.get(i) == 0 || pos.get(i) == this.gridRenderWidth_) {
              continue;
            }
            int texPos =
                (int) (pos.get(i) / renderLength * (this.gridRenderWidth_ / this.textureScaling_));
            painter.drawLine(
                0.5,
                texPos + 0.5,
                this.gridRenderWidth_ / this.textureScaling_ - 0.5,
                texPos + 0.5);
          }
        }
        break;
      case YZ:
        if (this.YZGridEnabled_[0]) {
          List<Double> pos = this.yAxis_.gridLinePositions(new AxisConfig());
          for (int i = 0; i < pos.size(); i++) {
            if (pos.get(i) == 0 || pos.get(i) == this.gridRenderWidth_) {
              continue;
            }
            int texPos =
                (int) (pos.get(i) / renderLength * (this.gridRenderWidth_ / this.textureScaling_));
            painter.drawLine(
                texPos + 0.5,
                0.5,
                texPos + 0.5,
                this.gridRenderWidth_ / this.textureScaling_ - 0.5);
          }
        }
        if (this.YZGridEnabled_[1]) {
          List<Double> pos = this.zAxis_.gridLinePositions(new AxisConfig());
          for (int i = 0; i < pos.size(); i++) {
            if (pos.get(i) == 0 || pos.get(i) == this.gridRenderWidth_) {
              continue;
            }
            int texPos =
                (int) (pos.get(i) / renderLength * (this.gridRenderWidth_ / this.textureScaling_));
            painter.drawLine(
                0.5,
                texPos + 0.5,
                this.gridRenderWidth_ / this.textureScaling_ - 0.5,
                texPos + 0.5);
          }
        }
        break;
    }
    painter.end();
  }

  private void loadCubeTextures() {
    this.initLayout();
    WPaintDevice cpdHoriz0 = this.createPaintDevice(new WLength(1024), new WLength(8 * 256));
    WPaintDevice cpdHoriz1 = this.createPaintDevice(new WLength(1024), new WLength(8 * 256));
    WPaintDevice cpdVert = this.createPaintDevice(new WLength(2 * 256), new WLength(1024));
    this.paintHorizAxisTextures(cpdHoriz0);
    this.paintHorizAxisTextures(cpdHoriz1, true);
    this.paintVertAxisTextures(cpdVert);
    if (this.horizAxisTexture_.isNull()) {
      this.horizAxisTexture_ = this.createTexture();
    }
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.horizAxisTexture_);
    this.pixelStorei(WGLWidget.GLenum.UNPACK_FLIP_Y_WEBGL, 1);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D,
        0,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.UNSIGNED_BYTE,
        cpdHoriz0);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_MIN_FILTER,
        WGLWidget.GLenum.LINEAR_MIPMAP_LINEAR);
    this.generateMipmap(WGLWidget.GLenum.TEXTURE_2D);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    if (this.horizAxisTexture2_.isNull()) {
      this.horizAxisTexture2_ = this.createTexture();
    }
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.horizAxisTexture2_);
    this.pixelStorei(WGLWidget.GLenum.UNPACK_FLIP_Y_WEBGL, 1);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D,
        0,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.UNSIGNED_BYTE,
        cpdHoriz1);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_MIN_FILTER,
        WGLWidget.GLenum.LINEAR_MIPMAP_LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.generateMipmap(WGLWidget.GLenum.TEXTURE_2D);
    if (this.vertAxisTexture_.isNull()) {
      this.vertAxisTexture_ = this.createTexture();
    }
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.vertAxisTexture_);
    this.pixelStorei(WGLWidget.GLenum.UNPACK_FLIP_Y_WEBGL, 1);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D,
        0,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.UNSIGNED_BYTE,
        cpdVert);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_MIN_FILTER,
        WGLWidget.GLenum.LINEAR_MIPMAP_LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.generateMipmap(WGLWidget.GLenum.TEXTURE_2D);
    WPaintDevice pd1 = this.createPaintDevice(new WLength(512), new WLength(512));
    this.paintGridLines(pd1, Plane.XY);
    this.cubeTextureXY_ = this.createTexture();
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.cubeTextureXY_);
    this.pixelStorei(WGLWidget.GLenum.UNPACK_FLIP_Y_WEBGL, 1);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D,
        0,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.UNSIGNED_BYTE,
        pd1);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_MIN_FILTER,
        WGLWidget.GLenum.LINEAR_MIPMAP_LINEAR);
    this.generateMipmap(WGLWidget.GLenum.TEXTURE_2D);
    WPaintDevice pd2 = this.createPaintDevice(new WLength(512), new WLength(512));
    this.paintGridLines(pd2, Plane.XZ);
    this.cubeTextureXZ_ = this.createTexture();
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.cubeTextureXZ_);
    this.pixelStorei(WGLWidget.GLenum.UNPACK_FLIP_Y_WEBGL, 1);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D,
        0,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.UNSIGNED_BYTE,
        pd2);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_MIN_FILTER,
        WGLWidget.GLenum.LINEAR_MIPMAP_LINEAR);
    this.generateMipmap(WGLWidget.GLenum.TEXTURE_2D);
    WPaintDevice pd3 = this.createPaintDevice(new WLength(512), new WLength(512));
    this.paintGridLines(pd3, Plane.YZ);
    this.cubeTextureYZ_ = this.createTexture();
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.cubeTextureYZ_);
    this.pixelStorei(WGLWidget.GLenum.UNPACK_FLIP_Y_WEBGL, 1);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D,
        0,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.UNSIGNED_BYTE,
        pd3);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_MIN_FILTER,
        WGLWidget.GLenum.LINEAR_MIPMAP_LINEAR);
    this.generateMipmap(WGLWidget.GLenum.TEXTURE_2D);
  }

  private void init2DShaders() {
    this.fragmentShader2D_ = this.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
    this.shaderSource(this.fragmentShader2D_, fragmentShaderSrc2D);
    this.compileShader(this.fragmentShader2D_);
    this.vertexShader2D_ = this.createShader(WGLWidget.GLenum.VERTEX_SHADER);
    this.shaderSource(this.vertexShader2D_, vertexShaderSrc2D);
    this.compileShader(this.vertexShader2D_);
    this.textureProgram_ = this.createProgram();
    this.attachShader(this.textureProgram_, this.vertexShader2D_);
    this.attachShader(this.textureProgram_, this.fragmentShader2D_);
    this.linkProgram(this.textureProgram_);
    this.texture_vertexPositionAttribute_ =
        this.getAttribLocation(this.textureProgram_, "aVertexPosition");
    this.texture_vertexTextureCoAttribute_ =
        this.getAttribLocation(this.textureProgram_, "aTextureCo");
    this.texture_texSamplerUniform_ = this.getUniformLocation(this.textureProgram_, "uSampler");
  }

  private void initTitle() {
    if ((this.title_.length() == 0)) {
      return;
    }
    float pixelHeight = (float) (this.titleFont_.getSizeLength().toPixels() * 1.5);
    WPaintDevice titlePaintDev = this.createPaintDevice(this.getWidth(), this.getHeight());
    WPainter painter = new WPainter(titlePaintDev);
    painter.setFont(this.titleFont_);
    painter.drawText(
        new WRectF(0, 0, this.getWidth().getValue(), pixelHeight),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Middle),
        this.title_);
    painter.end();
    this.titleTexture_ = this.createTexture();
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.titleTexture_);
    this.pixelStorei(WGLWidget.GLenum.UNPACK_FLIP_Y_WEBGL, 1);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D,
        0,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.UNSIGNED_BYTE,
        titlePaintDev);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.currentTopOffset_ += pixelHeight;
  }

  private void initColorMaps() {
    WPaintDevice colorMapPaintDev = this.createPaintDevice(this.getWidth(), this.getHeight());
    WPainter painter = new WPainter(colorMapPaintDev);
    painter.translate(0, this.currentTopOffset_);
    final int WIDTH = 100;
    int space =
        (int) this.getHeight().getValue() - this.currentTopOffset_ - this.currentBottomOffset_;
    final int VERT_MARGIN = (int) (space * 0.1);
    final int LEFT_OFFSET = 4;
    for (int i = 0; i < this.dataSeriesVector_.size(); i++) {
      if (this.dataSeriesVector_.get(i).getColorMap() == null
          || this.dataSeriesVector_.get(i).isHidden()) {
        continue;
      }
      painter.save();
      if (this.dataSeriesVector_.get(i).isColorMapVisible()) {
        switch (this.dataSeriesVector_.get(i).getColorMapSide()) {
          case Left:
            painter.translate(this.currentLeftOffset_, 0);
            this.dataSeriesVector_
                .get(i)
                .getColorMap()
                .paintLegend(
                    painter, new WRectF(LEFT_OFFSET, VERT_MARGIN, WIDTH, space - 2 * VERT_MARGIN));
            this.currentLeftOffset_ += WIDTH;
            break;
          case Right:
            painter.translate(this.getWidth().getValue() - this.currentRightOffset_ - WIDTH, 0);
            this.dataSeriesVector_
                .get(i)
                .getColorMap()
                .paintLegend(
                    painter, new WRectF(LEFT_OFFSET, VERT_MARGIN, WIDTH, space - 2 * VERT_MARGIN));
            this.currentRightOffset_ += WIDTH;
            break;
          default:
            throw new WException(
                "WCartesian3DChart: colormaps can only be put left or right of the chart");
        }
      }
      painter.restore();
    }
    painter.end();
    this.colorMapTexture_ = this.createTexture();
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.colorMapTexture_);
    this.pixelStorei(WGLWidget.GLenum.UNPACK_FLIP_Y_WEBGL, 1);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D,
        0,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.UNSIGNED_BYTE,
        colorMapPaintDev);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
  }

  private void initLegend() {
    if (!this.legend_.isLegendEnabled()) {
      return;
    }
    int MARGIN = 4;
    int legendWidth = this.legend_.getWidth();
    int legendHeight = this.legend_.height(this.dataSeriesVector_);
    WPaintDevice legendPaintDev =
        this.createPaintDevice(
            new WLength(this.getWidth().getValue()), new WLength(this.getHeight().getValue()));
    WPainter painter = new WPainter(legendPaintDev);
    int space = 0;
    if (this.legend_.getLegendSide() == Side.Left) {
      space =
          (int) this.getHeight().getValue() - this.currentTopOffset_ - this.currentBottomOffset_;
      painter.translate(this.currentLeftOffset_ + MARGIN, 0);
      this.currentLeftOffset_ += legendWidth + MARGIN;
    } else {
      if (this.legend_.getLegendSide() == Side.Right) {
        space =
            (int) this.getHeight().getValue() - this.currentTopOffset_ - this.currentBottomOffset_;
        painter.translate(
            this.getWidth().getValue() - this.currentRightOffset_ - legendWidth - MARGIN, 0);
        this.currentRightOffset_ += legendWidth + MARGIN;
      } else {
        if (this.legend_.getLegendSide() == Side.Top) {
          space =
              (int) this.getWidth().getValue() - this.currentLeftOffset_ - this.currentRightOffset_;
          painter.translate(0, this.currentTopOffset_ + MARGIN);
          this.currentTopOffset_ += legendHeight + MARGIN;
        } else {
          if (this.legend_.getLegendSide() == Side.Bottom) {
            space =
                (int) this.getWidth().getValue()
                    - this.currentLeftOffset_
                    - this.currentRightOffset_;
            painter.translate(
                0, this.getHeight().getValue() - this.currentBottomOffset_ - legendHeight - MARGIN);
            this.currentBottomOffset_ += legendHeight + MARGIN;
          }
        }
      }
    }
    if (this.legend_.getLegendSide() == Side.Left || this.legend_.getLegendSide() == Side.Right) {
      painter.translate(0, this.currentTopOffset_);
      switch (this.legend_.getLegendAlignment()) {
        case Top:
          painter.translate(0, MARGIN);
          this.currentTopOffset_ += MARGIN;
          break;
        case Middle:
          painter.translate(0, (space - legendHeight) / 2);
          break;
        case Bottom:
          painter.translate(0, space - legendHeight - MARGIN);
          this.currentBottomOffset_ += MARGIN;
          break;
        default:
          break;
      }
    }
    if (this.legend_.getLegendSide() == Side.Top || this.legend_.getLegendSide() == Side.Bottom) {
      painter.translate(this.currentLeftOffset_, 0);
      space = (int) this.getWidth().getValue() - this.currentLeftOffset_ - this.currentRightOffset_;
      switch (this.legend_.getLegendAlignment()) {
        case Left:
          painter.translate(MARGIN, 0);
          this.currentLeftOffset_ += MARGIN;
          break;
        case Center:
          painter.translate((space - legendWidth) / 2, 0);
          break;
        case Right:
          painter.translate(space - legendWidth - MARGIN, 0);
          this.currentRightOffset_ += MARGIN;
          break;
        default:
          break;
      }
    }
    this.legend_.renderLegend(painter, this.dataSeriesVector_);
    painter.end();
    this.legendTexture_ = this.createTexture();
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, this.legendTexture_);
    this.pixelStorei(WGLWidget.GLenum.UNPACK_FLIP_Y_WEBGL, 1);
    this.texImage2D(
        WGLWidget.GLenum.TEXTURE_2D,
        0,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.RGBA,
        WGLWidget.GLenum.UNSIGNED_BYTE,
        legendPaintDev);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D, WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.LINEAR);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_S,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
    this.texParameteri(
        WGLWidget.GLenum.TEXTURE_2D,
        WGLWidget.GLenum.TEXTURE_WRAP_T,
        WGLWidget.GLenum.CLAMP_TO_EDGE);
  }

  private void paintPeripheralTexture(
      final WGLWidget.Buffer pos, final WGLWidget.Buffer texCo, final WGLWidget.Texture texture) {
    this.useProgram(this.textureProgram_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, pos);
    this.vertexAttribPointer(
        this.texture_vertexPositionAttribute_, 3, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.texture_vertexPositionAttribute_);
    this.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, texCo);
    this.vertexAttribPointer(
        this.texture_vertexTextureCoAttribute_, 2, WGLWidget.GLenum.FLOAT, false, 0, 0);
    this.enableVertexAttribArray(this.texture_vertexTextureCoAttribute_);
    this.activeTexture(WGLWidget.GLenum.TEXTURE0);
    this.bindTexture(WGLWidget.GLenum.TEXTURE_2D, texture);
    this.uniform1i(this.texture_texSamplerUniform_, 0);
    this.drawArrays(WGLWidget.GLenum.TRIANGLE_STRIP, 0, 4);
    this.disableVertexAttribArray(this.texture_vertexPositionAttribute_);
    this.disableVertexAttribArray(this.texture_vertexTextureCoAttribute_);
  }

  private javax.vecmath.Matrix4f worldTransform_;
  private boolean isViewSet_;
  private List<WAbstractDataSeries3D> dataSeriesVector_;
  private WAxis xAxis_;
  private WAxis yAxis_;
  private WAxis zAxis_;
  private ChartType chartType_;
  private boolean[] XYGridEnabled_ = new boolean[2];
  private boolean[] XZGridEnabled_ = new boolean[2];
  private boolean[] YZGridEnabled_ = new boolean[2];
  private WPen cubeLinesPen_;
  private WPen gridLinesPen_;
  private WColor background_;
  private WChartPalette chartPalette_;
  private WString title_;
  private WFont titleFont_;
  private WLegend3D legend_;
  private WChart3DImplementation interface_;
  private int axisRenderWidth_;
  private int axisRenderHeight_;
  private int gridRenderWidth_;
  private int textureScaling_;
  private int seriesCounter_;
  private int currentTopOffset_;
  private int currentBottomOffset_;
  private int currentLeftOffset_;
  private int currentRightOffset_;
  private EnumSet<ChartUpdates> updates_;
  private boolean intersectionLinesEnabled_;
  private WColor intersectionLinesColor_;
  List<WCartesian3DChart.IntersectionPlane> intersectionPlanes_;
  private WGLWidget.Shader fragmentShader_;
  private WGLWidget.Shader vertexShader_;
  private WGLWidget.Shader fragmentShader2_;
  private WGLWidget.Shader vertexShader2_;
  private WGLWidget.Shader cubeLineFragShader_;
  private WGLWidget.Shader cubeLineVertShader_;
  private WGLWidget.Shader vertexShader2D_;
  private WGLWidget.Shader fragmentShader2D_;
  private WGLWidget.Shader intersectionLinesFragmentShader_;
  private WGLWidget.Shader clippingPlaneFragShader_;
  private WGLWidget.Shader clippingPlaneVertexShader_;
  private WGLWidget.Program cubeProgram_;
  private WGLWidget.Program cubeLineProgram_;
  private WGLWidget.Program axisProgram_;
  private WGLWidget.Program textureProgram_;
  private WGLWidget.Program intersectionLinesProgram_;
  private WGLWidget.Program clippingPlaneProgram_;
  private WGLWidget.AttribLocation cube_vertexPositionAttribute_;
  private WGLWidget.AttribLocation cube_planeNormalAttribute_;
  private WGLWidget.AttribLocation cube_textureCoordAttribute_;
  private WGLWidget.AttribLocation cubeLine_vertexPositionAttribute_;
  private WGLWidget.AttribLocation cubeLine_normalAttribute_;
  private WGLWidget.UniformLocation cube_pMatrixUniform_;
  private WGLWidget.UniformLocation cube_mvMatrixUniform_;
  private WGLWidget.UniformLocation cube_cMatrixUniform_;
  private WGLWidget.UniformLocation cube_texSampler1Uniform_;
  private WGLWidget.UniformLocation cube_texSampler2Uniform_;
  private WGLWidget.UniformLocation cube_texSampler3Uniform_;
  private WGLWidget.UniformLocation cubeLine_pMatrixUniform_;
  private WGLWidget.UniformLocation cubeLine_mvMatrixUniform_;
  private WGLWidget.UniformLocation cubeLine_cMatrixUniform_;
  private WGLWidget.UniformLocation cubeLine_nMatrixUniform_;
  private WGLWidget.UniformLocation cubeLine_colorUniform_;
  private WGLWidget.AttribLocation axis_vertexPositionAttribute_;
  private WGLWidget.AttribLocation axis_textureCoordAttribute_;
  private WGLWidget.AttribLocation axis_inPlaneAttribute_;
  private WGLWidget.AttribLocation axis_planeNormalAttribute_;
  private WGLWidget.AttribLocation axis_outOfPlaneNormalAttribute_;
  private WGLWidget.UniformLocation axis_pMatrixUniform_;
  private WGLWidget.UniformLocation axis_mvMatrixUniform_;
  private WGLWidget.UniformLocation axis_cMatrixUniform_;
  private WGLWidget.UniformLocation axis_nMatrixUniform_;
  private WGLWidget.UniformLocation axis_normalAngleTextureUniform_;
  private WGLWidget.UniformLocation axis_texSamplerUniform_;
  private WGLWidget.AttribLocation texture_vertexPositionAttribute_;
  private WGLWidget.AttribLocation texture_vertexTextureCoAttribute_;
  private WGLWidget.UniformLocation texture_texSamplerUniform_;
  private WGLWidget.AttribLocation intersectionLines_vertexPositionAttribute_;
  private WGLWidget.AttribLocation intersectionLines_vertexTextureCoAttribute_;
  private WGLWidget.UniformLocation intersectionLines_cameraUniform_;
  private WGLWidget.UniformLocation intersectionLines_viewportWidthUniform_;
  private WGLWidget.UniformLocation intersectionLines_viewportHeightUniform_;
  private WGLWidget.UniformLocation intersectionLines_positionSamplerUniform_;
  private WGLWidget.UniformLocation intersectionLines_colorUniform_;
  private WGLWidget.UniformLocation intersectionLines_meshIndexSamplerUniform_;
  private WGLWidget.AttribLocation clippingPlane_vertexPositionAttribute_;
  private WGLWidget.UniformLocation clippingPlane_mvMatrixUniform_;
  private WGLWidget.UniformLocation clippingPlane_pMatrixUniform_;
  private WGLWidget.UniformLocation clippingPlane_cMatrixUniform_;
  private WGLWidget.UniformLocation clippingPlane_clipPtUniform_;
  private WGLWidget.UniformLocation clippingPlane_dataMinPtUniform_;
  private WGLWidget.UniformLocation clippingPlane_dataMaxPtUniform_;
  private WGLWidget.UniformLocation clippingPlane_clippingAxis_;
  private WGLWidget.UniformLocation clippingPlane_drawPositionUniform_;
  private javax.vecmath.Matrix4f pMatrix_;
  private WGLWidget.JavaScriptMatrix4x4 jsMatrix_;
  private java.nio.ByteBuffer cubeData_;
  private java.nio.ByteBuffer cubeNormalsData_;
  private java.nio.IntBuffer cubeIndices_;
  private java.nio.ByteBuffer cubeTexCo_;
  private java.nio.IntBuffer cubeLineIndices_;
  private java.nio.ByteBuffer axisSlabData_;
  private java.nio.IntBuffer axisSlabIndices_;
  private java.nio.ByteBuffer axisInPlaneBools_;
  private java.nio.ByteBuffer axisPlaneNormal_;
  private java.nio.ByteBuffer axisOutOfPlaneNormal_;
  private java.nio.ByteBuffer axisTexCo_;
  private java.nio.ByteBuffer axisSlabDataVert_;
  private java.nio.IntBuffer axisSlabIndicesVert_;
  private java.nio.ByteBuffer axisInPlaneBoolsVert_;
  private java.nio.ByteBuffer axisPlaneNormalVert_;
  private java.nio.ByteBuffer axisOutOfPlaneNormalVert_;
  private java.nio.ByteBuffer axisTexCoVert_;
  private WGLWidget.Buffer cubeBuffer_;
  private WGLWidget.Buffer cubeNormalsBuffer_;
  private WGLWidget.Buffer cubeIndicesBuffer_;
  private WGLWidget.Buffer cubeLineNormalsBuffer_;
  private WGLWidget.Buffer cubeLineIndicesBuffer_;
  private WGLWidget.Buffer cubeTexCoords_;
  private WGLWidget.Buffer axisBuffer_;
  private WGLWidget.Buffer axisIndicesBuffer_;
  private WGLWidget.Buffer axisInPlaneBuffer_;
  private WGLWidget.Buffer axisPlaneNormalBuffer_;
  private WGLWidget.Buffer axisOutOfPlaneNormalBuffer_;
  private WGLWidget.Buffer axisVertBuffer_;
  private WGLWidget.Buffer axisIndicesVertBuffer_;
  private WGLWidget.Buffer axisInPlaneVertBuffer_;
  private WGLWidget.Buffer axisPlaneNormalVertBuffer_;
  private WGLWidget.Buffer axisOutOfPlaneNormalVertBuffer_;
  private WGLWidget.Buffer axisTexCoordsHoriz_;
  private WGLWidget.Buffer axisTexCoordsVert_;
  private WGLWidget.Buffer overlayPosBuffer_;
  private WGLWidget.Buffer overlayTexCoBuffer_;
  private WGLWidget.Buffer clippingPlaneVertBuffer_;
  private WGLWidget.Texture horizAxisTexture_;
  private WGLWidget.Texture horizAxisTexture2_;
  private WGLWidget.Texture vertAxisTexture_;
  private WGLWidget.Texture cubeTextureXY_;
  private WGLWidget.Texture cubeTextureXZ_;
  private WGLWidget.Texture cubeTextureYZ_;
  private WGLWidget.Texture titleTexture_;
  private WGLWidget.Texture legendTexture_;
  private WGLWidget.Texture colorMapTexture_;
  private WGLWidget.Texture meshIndexTexture_;
  private WGLWidget.Texture positionTexture_;
  private WGLWidget.Texture intersectionLinesTexture_;
  private WGLWidget.Framebuffer meshIndexFramebuffer_;
  private WGLWidget.Framebuffer positionFramebuffer_;
  private WGLWidget.Framebuffer intersectionLinesFramebuffer_;
  private WGLWidget.Renderbuffer offscreenDepthbuffer_;
  private List<Object> objectsToDelete;
  private static float[] cubeData = {
    0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 1, 1,
    1, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1,
    1, 1, 1, 0, 1, 0, 0, 1
  };
  private static float[] cubePlaneNormals = {
    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
    0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0,
    0, 1, 0, 0, 1, 0, 0, 1
  };
  private static int[] cubeIndices = {
    0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11, 12, 13, 14, 12, 14, 15, 16, 17, 18, 16,
    18, 19, 20, 21, 22, 20, 22, 23
  };
  private static int[] cubeLineIndices = {
    0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4, 8, 9, 9, 10, 10, 11, 11, 8, 12, 13, 13, 14, 14,
    15, 15, 12, 16, 17, 17, 18, 18, 19, 19, 16, 20, 21, 21, 22, 22, 23, 23, 20
  };
  private static float[] cubeLineNormals = {
    0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 1, 0, 0, 1, 0,
    0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1
  };
  private static float[] cubeTexCo = {
    0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0,
    0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1
  };
  private static float[] axisSlabDataVertical = {
    -0.4f, 1.3f, 0, -0.4f, -0.3f, 0, 0, -0.3f, 0, 0, 1.3f, 0, 0, 1.3f, -0.4f, 0, -0.3f, -0.4f, 0,
    -0.3f, 0, 0, 1.3f, 0, 1, 1.3f, 0, 1, -0.3f, 0, 1.4f, -0.3f, 0, 1.4f, 1.3f, 0, 1, 1.3f, 0, 1,
    -0.3f, 0, 1, -0.3f, -0.4f, 1, 1.3f, -0.4f, 0, 1.3f, 1.4f, 0, -0.3f, 1.4f, 0, -0.3f, 1, 0, 1.3f,
    1, -0.4f, 1.3f, 1, -0.4f, -0.3f, 1, 0, -0.3f, 1, 0, 1.3f, 1, 0, 1.3f, 0, 0, -0.3f, 0, 0, -0.3f,
    -0.4f, 0, 1.3f, -0.4f, 0, 1.3f, 0, 0, -0.3f, 0, -0.4f, -0.3f, 0, -0.4f, 1.3f, 0, 1.4f, 1.3f, 1,
    1.4f, -0.3f, 1, 1, -0.3f, 1, 1, 1.3f, 1, 1, 1.3f, 1.4f, 1, -0.3f, 1.4f, 1, -0.3f, 1, 1, 1.3f, 1,
    0, 1.3f, 1, 0, -0.3f, 1, -0.4f, -0.3f, 1, -0.4f, 1.3f, 1, 0, 1.3f, 1, 0, -0.3f, 1, 0, -0.3f,
    1.4f, 0, 1.3f, 1.4f, 1, 1.3f, -0.4f, 1, -0.3f, -0.4f, 1, -0.3f, 0, 1, 1.3f, 0, 1.4f, 1.3f, 0,
    1.4f, -0.3f, 0, 1, -0.3f, 0, 1, 1.3f, 0, 1, 1.3f, 1, 1, -0.3f, 1, 1, -0.3f, 1.4f, 1, 1.3f, 1.4f,
    1, 1.3f, 1, 1, -0.3f, 1, 1.4f, -0.3f, 1, 1.4f, 1.3f, 1
  };
  private static float[] axisPlaneNormalVertical = {
    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
    1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
    0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
    -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, -1, 0, 0, -1, 0, 0, -1, 0, 0,
    -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
    0, -1, 0, 0, -1, 0, 0, -1, 0, 0
  };
  private static float[] axisOutOfPlaneNormalVertical = {
    -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 1, 0, 0, 1, 0,
    0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
    0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
    0, -1, 0, 0, -1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, -1, 0,
    0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, -1, 0,
    0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
    0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1
  };
  private static float[] axisInPlaneBoolsVertical = {
    1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0,
    1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0
  };
  private static int[] axisSlabIndicesVertical = {
    0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11, 12, 13, 14, 12, 14, 15, 16, 17, 18, 16,
    18, 19, 20, 21, 22, 20, 22, 23, 24, 25, 26, 24, 26, 27, 28, 29, 30, 28, 30, 31, 32, 33, 34, 32,
    34, 35, 36, 37, 38, 36, 38, 39, 40, 41, 42, 40, 42, 43, 44, 45, 46, 44, 46, 47, 48, 49, 50, 48,
    50, 51, 52, 53, 54, 52, 54, 55, 56, 57, 58, 56, 58, 59, 60, 61, 62, 60, 62, 63
  };
  private static float[] axisTexCoVertical = {
    0, 1, 0, 0, 0.5f, 0, 0.5f, 1, 0, 1, 0, 0, 0.5f, 0, 0.5f, 1, 0.5f, 1, 0.5f, 0, 1, 0, 1, 1, 0.5f,
    1, 0.5f, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0.5f, 0, 0.5f, 1, 0, 1, 0, 0, 0.5f, 0, 0.5f, 1, 0.5f, 1,
    0.5f, 0, 1, 0, 1, 1, 0.5f, 1, 0.5f, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0.5f, 0, 0.5f, 1, 0, 1, 0, 0,
    0.5f, 0, 0.5f, 1, 0.5f, 1, 0.5f, 0, 1, 0, 1, 1, 0.5f, 1, 0.5f, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0.5f,
    0, 0.5f, 1, 0, 1, 0, 0, 0.5f, 0, 0.5f, 1, 0.5f, 1, 0.5f, 0, 1, 0, 1, 1, 0.5f, 1, 0.5f, 0, 1, 0,
    1, 1
  };
  private static float[] axisSlabData = {
    1.3f, 0, 0, 1.3f, 0, -0.4f, -0.3f, 0, -0.4f, -0.3f, 0, 0, 0, 0, -0.3f, -0.4f, 0, -0.3f, -0.4f,
    0, 1.3f, 0, 0, 1.3f, -0.3f, 0, 1, -0.3f, 0, 1.4f, 1.3f, 0, 1.4f, 1.3f, 0, 1, 1, 0, 1.3f, 1.4f,
    0, 1.3f, 1.4f, 0, -0.3f, 1, 0, -0.3f, 1.3f, 0, 0, 1.3f, -0.4f, 0, -0.3f, -0.4f, 0, -0.3f, 0, 0,
    0, 0, -0.3f, 0, -0.4f, -0.3f, 0, -0.4f, 1.3f, 0, 0, 1.3f, -0.3f, 0, 1, -0.3f, -0.4f, 1, 1.3f,
    -0.4f, 1, 1.3f, 0, 1, 1, 0, 1.3f, 1, -0.4f, 1.3f, 1, -0.4f, -0.3f, 1, 0, -0.3f, 1.3f, 1, -0.4f,
    1.3f, 1, 0, -0.3f, 1, 0, -0.3f, 1, -0.4f, -0.4f, 1, -0.3f, 0, 1, -0.3f, 0, 1, 1.3f, -0.4f, 1,
    1.3f, -0.3f, 1, 1.4f, -0.3f, 1, 1, 1.3f, 1, 1, 1.3f, 1, 1.4f, 1.4f, 1, 1.3f, 1, 1, 1.3f, 1, 1,
    -0.3f, 1.4f, 1, -0.3f, 1.3f, 1.4f, 0, 1.3f, 1, 0, -0.3f, 1, 0, -0.3f, 1.4f, 0, 0, 1.4f, -0.3f,
    0, 1, -0.3f, 0, 1, 1.3f, 0, 1.4f, 1.3f, -0.3f, 1.4f, 1, -0.3f, 1, 1, 1.3f, 1, 1, 1.3f, 1.4f, 1,
    1, 1.4f, 1.3f, 1, 1, 1.3f, 1, 1, -0.3f, 1, 1.4f, -0.3f, -0.3f, 0, 0, -0.3f, -0.4f, 0, 1.3f,
    -0.4f, 0, 1.3f, 0, 0, -0.3f, 0, 0, -0.3f, 0, -0.4f, 1.3f, 0, -0.4f, 1.3f, 0, 0, -0.3f, 1.4f, 0,
    -0.3f, 1, 0, 1.3f, 1, 0, 1.3f, 1.4f, 0, -0.3f, 1, -0.4f, -0.3f, 1, 0, 1.3f, 1, 0, 1.3f, 1,
    -0.4f, 0, 0, 1.3f, 0, -0.4f, 1.3f, 0, -0.4f, -0.3f, 0, 0, -0.3f, 0, 0, 1.3f, -0.4f, 0, 1.3f,
    -0.4f, 0, -0.3f, 0, 0, -0.3f, 0, 1.4f, 1.3f, 0, 1, 1.3f, 0, 1, -0.3f, 0, 1.4f, -0.3f, -0.4f, 1,
    1.3f, 0, 1, 1.3f, 0, 1, -0.3f, -0.4f, 1, -0.3f, 1.3f, 0, 1, 1.3f, -0.4f, 1, -0.3f, -0.4f, 1,
    -0.3f, 0, 1, 1.3f, 0, 1, 1.3f, 0, 1.4f, -0.3f, 0, 1.4f, -0.3f, 0, 1, 1.3f, 1.4f, 1, 1.3f, 1, 1,
    -0.3f, 1, 1, -0.3f, 1.4f, 1, 1.3f, 1, 1.4f, 1.3f, 1, 1, -0.3f, 1, 1, -0.3f, 1, 1.4f, 1, 0,
    -0.3f, 1, -0.4f, -0.3f, 1, -0.4f, 1.3f, 1, 0, 1.3f, 1, 0, -0.3f, 1.4f, 0, -0.3f, 1.4f, 0, 1.3f,
    1, 0, 1.3f, 1, 1.4f, -0.3f, 1, 1, -0.3f, 1, 1, 1.3f, 1, 1.4f, 1.3f, 1.4f, 1, -0.3f, 1, 1, -0.3f,
    1, 1, 1.3f, 1.4f, 1, 1.3f
  };
  private static float[] axisPlaneNormal = {
    0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
    1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
    0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
    0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
    -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
    0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
    0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0,
    1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
    0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
    -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
    -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
    0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0
  };
  private static float[] axisOutOfPlaneNormal = {
    0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0,
    1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
    -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1,
    0, 0, 1, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, -1, 0,
    0, -1, 0, 0, -1, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1,
    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
    -1, 0, 0, -1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, -1,
    0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
    0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
    0, 1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 1,
    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0
  };
  private static float[] axisInPlaneBools = {
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0,
    1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0
  };
  private static int[] axisSlabIndices = {
    0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11, 12, 13, 14, 12, 14, 15, 16, 17, 18, 16,
    18, 19, 20, 21, 22, 20, 22, 23, 24, 25, 26, 24, 26, 27, 28, 29, 30, 28, 30, 31, 32, 33, 34, 32,
    34, 35, 36, 37, 38, 36, 38, 39, 40, 41, 42, 40, 42, 43, 44, 45, 46, 44, 46, 47, 48, 49, 50, 48,
    50, 51, 52, 53, 54, 52, 54, 55, 56, 57, 58, 56, 58, 59, 60, 61, 62, 60, 62, 63, 64, 65, 66, 64,
    66, 67, 68, 69, 70, 68, 70, 71, 72, 73, 74, 72, 74, 75, 76, 77, 78, 76, 78, 79, 80, 81, 82, 80,
    82, 83, 84, 85, 86, 84, 86, 87, 88, 89, 90, 88, 90, 91, 92, 93, 94, 92, 94, 95, 96, 97, 98, 96,
    98, 99, 100, 101, 102, 100, 102, 103, 104, 105, 106, 104, 106, 107, 108, 109, 110, 108, 110,
    111, 112, 113, 114, 112, 114, 115, 116, 117, 118, 116, 118, 119, 120, 121, 122, 120, 122, 123,
    124, 125, 126, 124, 126, 127
  };
  private static float[] axisTexCo = {
    0, 0.875f, 0, 0.75f, 1, 0.75f, 1, 0.875f, 0, 0.5f, 0, 0.376f, 1, 0.376f, 1, 0.5f, 0, 1, 0,
    0.876f, 1, 0.876f, 1, 1, 0, 0.375f, 0, 0.25f, 1, 0.25f, 1, 0.375f, 0, 0.875f, 0, 0.75f, 1,
    0.75f, 1, 0.875f, 0, 0.5f, 0, 0.376f, 1, 0.376f, 1, 0.5f, 0, 1, 0, 0.876f, 1, 0.876f, 1, 1, 0,
    0.375f, 0, 0.25f, 1, 0.25f, 1, 0.375f, 0, 0.624f, 0, 0.5f, 1, 0.5f, 1, 0.624f, 0, 0.25f, 0,
    0.125f, 1, 0.125f, 1, 0.25f, 0, 0.75f, 0, 0.625f, 1, 0.625f, 1, 0.75f, 0, 0.124f, 0, 0, 1, 0, 1,
    0.124f, 0, 0.624f, 0, 0.5f, 1, 0.5f, 1, 0.624f, 0, 0.25f, 0, 0.125f, 1, 0.125f, 1, 0.25f, 0,
    0.75f, 0, 0.625f, 1, 0.625f, 1, 0.75f, 0, 0.124f, 0, 0, 1, 0, 1, 0.124f, 0, 1, 0, 0.876f, 1,
    0.876f, 1, 1, 0, 1, 0, 0.876f, 1, 0.876f, 1, 1, 0, 0.75f, 0, 0.625f, 1, 0.625f, 1, 0.75f, 0,
    0.75f, 0, 0.625f, 1, 0.625f, 1, 0.75f, 0, 0.375f, 0, 0.25f, 1, 0.25f, 1, 0.375f, 0, 0.375f, 0,
    0.25f, 1, 0.25f, 1, 0.375f, 0, 0.124f, 0, 0, 1, 0, 1, 0.124f, 0, 0.124f, 0, 0, 1, 0, 1, 0.124f,
    0, 0.875f, 0, 0.75f, 1, 0.75f, 1, 0.875f, 0, 0.875f, 0, 0.75f, 1, 0.75f, 1, 0.875f, 0, 0.624f,
    0, 0.5f, 1, 0.5f, 1, 0.624f, 0, 0.624f, 0, 0.5f, 1, 0.5f, 1, 0.624f, 0, 0.5f, 0, 0.376f, 1,
    0.376f, 1, 0.5f, 0, 0.5f, 0, 0.376f, 1, 0.376f, 1, 0.5f, 0, 0.25f, 0, 0.125f, 1, 0.125f, 1,
    0.25f, 0, 0.25f, 0, 0.125f, 1, 0.125f, 1, 0.25f
  };
  private static final String cubeFragmentShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec2 vTextureCo;\nvarying float vWhichTexture;\n\nuniform sampler2D uSampler1;\nuniform sampler2D uSampler2;\nuniform sampler2D uSampler3;\n\nvoid main(void) {\n  if (vWhichTexture - 1.5 < 0.0) {    gl_FragColor = texture2D(uSampler3, vec2(vTextureCo.s, vTextureCo.t));\n  } else if (vWhichTexture - 2.5 < 0.0) {    gl_FragColor = texture2D(uSampler1, vec2(vTextureCo.s, vTextureCo.t));\n  } else if (vWhichTexture - 3.5 < 0.0) {    gl_FragColor = texture2D(uSampler2, vec2(vTextureCo.s, vTextureCo.t));\n  } else {    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n  }}\n";
  private static final String cubeVertexShaderSrc =
      "attribute vec3 aVertexPosition;\nattribute vec3 aPlaneNormal;\nattribute vec2 aTextureCo;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec2 vTextureCo;\nvarying float vWhichTexture;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vTextureCo = aTextureCo;\n  vWhichTexture = dot(aPlaneNormal, vec3(1.0, 2.0, 3.0));}\n";
  private static final String cubeLineFragmentShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nuniform vec4 uColor;\n\nvoid main(void) {\n  gl_FragColor = uColor/255.0;\n}\n";
  private static final String cubeLineVertexShaderSrc =
      "attribute vec3 aVertexPosition;\nattribute vec3 aNormal;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\nuniform mat4 uNMatrix;\n\nvoid main(void) {\n  vec3 transformedNormal = normalize((uNMatrix * vec4(normalize(aNormal), 0)).xyz);\n  if (transformedNormal.z > 0.0) {\n    gl_Position = vec4(5.0, 0.0, 0.0, 1.0);\n  } else {    gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  }}\n";
  private static final String axisVertexShaderSrc =
      "attribute vec3 aVertexPosition;\nattribute vec2 aTextureCo;\nattribute float aInPlane;\nattribute vec3 aPlaneNormal;\nattribute vec3 aOutOfPlaneNormal;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\nuniform mat4 uNMatrix;\nuniform bool uNormalAngleTexture;\n\nvarying vec2 vTextureCo;\nvarying float vShowTexture;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vTextureCo = aTextureCo;\n    vec3 transformedPlaneNormal = normalize((uNMatrix * vec4(normalize(aPlaneNormal), 0)).xyz);\n  vec3 transformedOutOfPlaneNormal = normalize((uNMatrix * vec4(normalize(aOutOfPlaneNormal), 0)).xyz);\n    float zProjInPlane = transformedPlaneNormal.z;\n  float zProjOutOfPlane = transformedOutOfPlaneNormal.z;\n  bool showInPlane = (zProjInPlane > zProjOutOfPlane);\n  bool inPlane = (aInPlane > 0.5);\n    float xOrientation = (transformedOutOfPlaneNormal + transformedPlaneNormal).x;\n  bool correctTexture = (xOrientation > 0.0) == uNormalAngleTexture;\n  bool cull = (zProjInPlane <= 0.0 || zProjOutOfPlane < 0.0 || correctTexture );\n  if ( showInPlane != inPlane || cull ) {\n    gl_Position = vec4(0.0, 0.0, 0.0, 1.0);\n  }\n    vec3 cp = cross(transformedPlaneNormal, transformedOutOfPlaneNormal);  bool showTexture = abs(cp.z) < 0.85;  vShowTexture = 1.0;\n  if (!showTexture){\n    vShowTexture = 0.0;\n  }\n}\n";
  private static final String axisFragmentShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec2 vTextureCo;\nvarying float vShowTexture;\n\nuniform sampler2D uSampler;\n\nvoid main(void) {\n  gl_FragColor = texture2D(uSampler, vec2(vTextureCo.s, vTextureCo.t));\n}\n";
  private static final String fragmentShaderSrc2D =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec2 vTextureCo;\n\nuniform sampler2D uSampler;\n\nvoid main(void) {\n  gl_FragColor = texture2D(uSampler, vec2(vTextureCo.s, vTextureCo.t));\n}";
  private static final String vertexShaderSrc2D =
      "attribute vec3 aVertexPosition;\nattribute vec2 aTextureCo;\n\nvarying vec2 vTextureCo;\n\nvoid main(void) {\n  gl_Position = vec4(aVertexPosition, 1.0);\n  vTextureCo = aTextureCo;\n}";
  private static final String intersectionLinesFragmentShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec2 vTextureCo;\n\nuniform mat4 uCamera;\nuniform vec4 uColor;\nuniform float uVPwidth;\nuniform float uVPheight;\nuniform sampler2D uPositionSampler;\nuniform sampler2D uMeshIndexSampler;\n\nvoid main(void) {\n  float dx = 1.0/uVPwidth;\n  float dy = 1.0/uVPheight;\n  vec3 pt  = texture2D(uPositionSampler, vTextureCo+vec2(-dx,0.0)).xyz;\n  vec3 pl  = texture2D(uPositionSampler, vTextureCo+vec2(0.0,dy)).xyz;\n  vec3 pr  = texture2D(uPositionSampler, vTextureCo+vec2(0.0,-dy)).xyz;\n  vec3 pb  = texture2D(uPositionSampler, vTextureCo+vec2(dx,0.0)).xyz;\n  vec3 it  = texture2D(uMeshIndexSampler, vTextureCo+vec2(-dx,0.0)).xyz;\n  vec3 il  = texture2D(uMeshIndexSampler, vTextureCo+vec2(0.0,dy)).xyz;\n  vec3 ir  = texture2D(uMeshIndexSampler, vTextureCo+vec2(0.0,-dy)).xyz;\n  vec3 ib  = texture2D(uMeshIndexSampler, vTextureCo+vec2(dx,0.0)).xyz;\n  float scale = length(vec3(uCamera[0][0], uCamera[1][0], uCamera[2][0]));\n  scale = scale > 5.0 ? 5.0 : scale;\n  float totalDistance = 0.0;\n  int count = 0;\n  vec3 white = vec3(1.0);\n  if (il != ir && il != white && ir != white) {\n    count ++;\n    totalDistance += length(pl - pr) * scale;\n  }\n  if (it != ib && it != white && ib != white) {\n    count ++;\n    totalDistance += length(pt - pb) * scale;\n  }\n  float factor = count == 0 ? 0.0 : 1.0 - totalDistance / float(count);\n  factor = smoothstep(0.9, 1.0, factor);\n  gl_FragColor = vec4(uColor.rgb, factor);\n}";
  private static final String clippingPlaneFragShaderSrc =
      "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec3 vPos;\n\nuniform bool uDrawPosition;\n#ifdef GL_ES\nuniform lowp int uClippingAxis;\n#else\nuniform int uClippingAxis;\n#endif\n\nvoid main(void) {\n  if (uClippingAxis == 0 && (vPos.x <= 0.0 || vPos.x >= 1.0) ||      uClippingAxis == 1 && (vPos.y <= 0.0 || vPos.y >= 1.0) ||      uClippingAxis == 2 && (vPos.z <= 0.0 || vPos.z >= 1.0)) {\n    discard;\n  }\n  if (uDrawPosition) {\n    gl_FragColor = vec4(vPos, 1.0);\n  } else {\n    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n  }\n}\n";
  private static final String clippingPlaneVertexShaderSrc =
      "attribute vec2 aVertexPosition;\n\nvarying vec3 vPos;\n\nuniform vec3 uClipPt;\nuniform vec3 uDataMinPt;\nuniform vec3 uDataMaxPt;\n#ifdef GL_ES\nuniform lowp int uClippingAxis;\n#else\nuniform int uClippingAxis;\n#endif\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvoid main(void) {\n  vec3 pos;\n  vec3 clipPt = clamp((uClipPt - uDataMinPt) / (uDataMaxPt - uDataMinPt), 0.0, 1.0);\n  if (uClippingAxis == 0) {\n    pos = vec3(clipPt.x, aVertexPosition);\n  } else if (uClippingAxis == 1) {\n    pos = vec3(aVertexPosition.x, clipPt.y, aVertexPosition.y);\n  } else if (uClippingAxis == 2) {\n    pos = vec3(aVertexPosition, clipPt.z);\n  }\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(pos, 1.0);\n  vPos = pos;\n}\n";
  private static final double TICKLENGTH = 5.0;
  private static final double TITLEOFFSET = 30;
  private static final double ANGLE1 = 15;
  private static final double ANGLE2 = 80;
}
