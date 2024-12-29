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
 * Abstract base class for dataseries that can be drawn on a {@link WCartesian3DChart}.
 *
 * <p>
 *
 * <h3>General</h3>
 *
 * <p>The model that is provided at construction or with {@link
 * WAbstractDataSeries3D#setModel(WAbstractItemModel model) setModel()} contains the data of a
 * dataseries. Implementations of this class format the data for representation on the chart and
 * perform all necessary drawing operations. Note that if a dataseries holds numerical data it
 * should be added to a chart of type {@link ChartType#Scatter}, if it holds categorical data it
 * should be added to a chart of type {@link ChartType#Category}.
 *
 * <p>
 *
 * <h3>Color</h3>
 *
 * <p>The color used to draw data on a chart can be specified in a number of ways. The priority of
 * this is as follows (1 being the highest):
 *
 * <p>
 *
 * <ol>
 *   <li>{@link ItemDataRole#MarkerBrushColor} set on a value in the model
 *   <li>{@link WAbstractColorMap} set on the dataseries
 *   <li>{@link WChartPalette} present in the chart
 * </ol>
 *
 * <p>A chart-palette will specify one color for the entire dataseries. Each new dataseries on a
 * chart will receive another color.<br>
 * A colormap assigns different colors to the data within one dataseries, based on the z-value of
 * the data. {@link WStandardColorMap} provides an easy way to create a colormap that is either
 * smooth or consists of a number of bands.
 *
 * <p>
 *
 * <h3>Data-roles</h3>
 *
 * <p>The roles on the model which are taken into account are:<br>
 *
 * <ul>
 *   <li>{@link ItemDataRole#MarkerBrushColor}: this determines the color of a datapoint and
 *       overrides the default
 *   <li>{@link ItemDataRole#MarkerScaleFactor}: this determines the size of a datapoint and
 *       overrides the default
 * </ul>
 *
 * <p>Some representations of the data ignore these roles. For example, when a surface is drawn, the
 * roles are ignored.
 *
 * <p>
 *
 * <h3>Implementing a new dataseries class</h3>
 *
 * <p>When the existing implementations of WAbstractDataSeries3D don&apos;t meet your needs, you
 * might want to make your own. When doing this there are some details of the chart that you should
 * know. The chart is made so that when a property of the chart changes, which affect any of the GL
 * resources, all GL resources are destroyed and re-initialized. This eliminates the need to
 * determine which chart-setting affect which GL-resources, which can be a complicated problem.
 *
 * <p>Therefore only unchanging GL resources are initialized in {@link
 * WAbstractDataSeries3D#initializeGL() initializeGL()}. The initializeGL function in the chart is
 * implemented to immediately request a call to {@link WAbstractDataSeries3D#updateGL() updateGL()},
 * which then initializes the rest of the GL resources. Every call to updateGL in the chart, will
 * first call {@link WAbstractDataSeries3D#deleteAllGLResources() deleteAllGLResources()} on all
 * dataseries and will then call {@link WAbstractDataSeries3D#updateGL() updateGL()} on all
 * dataseries. So, when implementing a dataseries: initialize unchanging GL resources in {@link
 * WAbstractDataSeries3D#initializeGL() initializeGL()}, initialize the rest of your GL resources in
 * {@link WAbstractDataSeries3D#updateGL() updateGL()} and make GL-delete calls to all resources
 * initialized in {@link WAbstractDataSeries3D#updateGL() updateGL()} in the function {@link
 * WAbstractDataSeries3D#deleteAllGLResources() deleteAllGLResources()}. It is also best to check
 * isNull() on each of your GL-resources when deleting them.
 */
public abstract class WAbstractDataSeries3D extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WAbstractDataSeries3D.class);

  /**
   * Constructor.
   *
   * <p>This constructor takes a {@link WAbstractItemModel} as an argument. The model contains the
   * data of this dataseries. How the model should be structured is dependent on the implementation.
   * Therefore this information is found in the documentation of classes deriving from this one.
   */
  public WAbstractDataSeries3D(WAbstractItemModel model) {
    super();
    this.name_ = new WString();
    this.model_ = null;
    this.chart_ = null;
    this.rangeCached_ = false;
    this.pointSize_ = 2.0;
    this.pointSprite_ = "";
    this.colormap_ = (WAbstractColorMap) null;
    this.showColorMap_ = false;
    this.colorMapSide_ = Side.Right;
    this.legendEnabled_ = true;
    this.hidden_ = false;
    this.mvMatrix_ =
        new javax.vecmath.Matrix4f(
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f);
    this.connections_ = new ArrayList<AbstractSignal.Connection>();
    this.model_ = model;
  }
  /**
   * Sets the title of this dataseries.
   *
   * <p>When a dataseries that did not have a title set, is added to a {@link WCartesian3DChart} it
   * automatically gets the default title &apos;dataset i&apos;, with i the count of how many
   * dataseries have been added to the chart.
   *
   * <p>
   */
  public void setTitle(final CharSequence name) {
    this.name_ = WString.toWString(name);
    if (this.chart_ != null) {
      this.chart_.updateChart(EnumSet.of(ChartUpdates.GLTextures));
    }
  }
  /** Returns the title of this dataseries. */
  public WString getTitle() {
    return this.name_;
  }
  /**
   * Sets a model from which the dataseries gets its data.
   *
   * <p>Every dataseries needs a model from which it gets the data. How the data is structured is
   * determined by the type of dataseries. Therefore more info on how to construct a proper model is
   * provided in classes that derive from this one.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#getModel()
   * @see WAbstractDataSeries3D#WAbstractDataSeries3D(WAbstractItemModel model)
   */
  public void setModel(final WAbstractItemModel model) {
    if (model != this.model_) {
      if (this.model_ != null && this.chart_ != null) {
        clearConnections(this.connections_);
      }
      this.rangeCached_ = false;
      this.model_ = model;
      if (this.model_ != null && this.chart_ != null) {
        this.chart_.updateChart(EnumSet.of(ChartUpdates.GLContext));
        this.connections_.add(
            this.model_
                .modelReset()
                .addListener(
                    this.chart_,
                    () -> {
                      WAbstractDataSeries3D.this.chart_.updateChart(
                          EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                    }));
        this.connections_.add(
            this.model_
                .dataChanged()
                .addListener(
                    this.chart_,
                    () -> {
                      WAbstractDataSeries3D.this.chart_.updateChart(
                          EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                    }));
        this.connections_.add(
            this.model_
                .rowsInserted()
                .addListener(
                    this.chart_,
                    () -> {
                      WAbstractDataSeries3D.this.chart_.updateChart(
                          EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                    }));
        this.connections_.add(
            this.model_
                .columnsInserted()
                .addListener(
                    this.chart_,
                    () -> {
                      WAbstractDataSeries3D.this.chart_.updateChart(
                          EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                    }));
        this.connections_.add(
            this.model_
                .rowsRemoved()
                .addListener(
                    this.chart_,
                    () -> {
                      WAbstractDataSeries3D.this.chart_.updateChart(
                          EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                    }));
        this.connections_.add(
            this.model_
                .columnsRemoved()
                .addListener(
                    this.chart_,
                    () -> {
                      WAbstractDataSeries3D.this.chart_.updateChart(
                          EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                    }));
      }
    }
  }
  /**
   * Returns a pointer to the model used by this dataseries.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setModel(WAbstractItemModel model)
   */
  public WAbstractItemModel getModel() {
    return this.model_;
  }
  /**
   * Returns the computed minimum value of this dataseries along the given axis.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#maximum(Axis axis)
   */
  public abstract double minimum(Axis axis);
  /**
   * Returns the computed maximum value of this dataseries along the given axis.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#minimum(Axis axis)
   */
  public abstract double maximum(Axis axis);
  /** Returns a const pointer to the {@link WCartesian3DChart} on which the dataseries is drawn. */
  public WCartesian3DChart getChart() {
    return this.chart_;
  }
  /**
   * Sets the pointsize for drawing this dataseries.
   *
   * <p>The default pointsize is 2 pixels.
   *
   * <p><i><b>Note: </b></i>Setting the point-size is currently not supported in IE.
   */
  public void setPointSize(double size) {
    if (size != this.pointSize_) {
      this.pointSize_ = size;
      if (this.chart_ != null) {
        this.chart_.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
      }
    }
  }
  /**
   * Returns the pointsize for drawing this dataseries.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setPointSize(double size)
   */
  public double getPointSize() {
    return this.pointSize_;
  }
  /**
   * Set the point sprite used for drawing this dataseries.
   *
   * <p>This should be a local (server side) path to an image, such as a PNG or GIF. Only the alpha
   * channel of this image is used: the sprite only decides if a pixel in the point appears or not.
   * If the alpha value is below 0.5, the pixel is discarded.
   *
   * <p>For best effect, the point sprite&apos;s width and height should be the same as the {@link
   * WAbstractDataSeries3D#getPointSize() getPointSize()}, and the chart&apos;s antialiasing should
   * be disabled.
   *
   * <p>Defaults to the empty string, meaning that every pixel of the point will be drawn, yielding
   * a square.
   */
  public void setPointSprite(final String image) {
    if (!image.equals(this.pointSprite_)) {
      this.pointSprite_ = image;
      if (this.chart_ != null) {
        this.chart_.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
      }
    }
  }
  /**
   * Returns the point sprite used for drawing this dataseries.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setPointSprite(String image)
   */
  public String getPointSprite() {
    return this.pointSprite_;
  }
  /**
   * Sets the colormap for this dataseries.
   *
   * <p>Ownership of the {@link WAbstractColorMap} is transferred to this class.
   *
   * <p>By default there is no colormap set. When a colormap is set on a dataseries, the color of
   * {@link WCartesian3DChart#getPalette()} is no longer used for this series. The colormap
   * associates a color to the data based on the z-value of the data. If the colormap is set to 0,
   * the value of the palette will be used again.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setColorMapVisible(boolean enabled)
   * @see WAbstractDataSeries3D#setColorMapSide(Side side)
   */
  public void setColorMap(final WAbstractColorMap colormap) {
    this.colormap_ = colormap;
    if (this.chart_ != null) {
      this.chart_.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
    }
  }
  /**
   * Returns the colormap used by this dataseries.
   *
   * <p>If this dataseries has no colormap set, 0 will be returned.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setColorMap(WAbstractColorMap colormap)
   * @see WAbstractDataSeries3D#setColorMapVisible(boolean enabled)
   * @see WAbstractDataSeries3D#setColorMapSide(Side side)
   */
  public WAbstractColorMap getColorMap() {
    return this.colormap_;
  }
  /**
   * Sets whether the colormap that is used should be shown alongside the chart in the form of a
   * legend.
   *
   * <p>The default value is false.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setColorMap(WAbstractColorMap colormap)
   * @see WAbstractDataSeries3D#setColorMapSide(Side side)
   */
  public void setColorMapVisible(boolean enabled) {
    if (this.showColorMap_ == enabled) {
      return;
    }
    this.showColorMap_ = enabled;
    if (this.chart_ != null) {
      this.chart_.updateChart(EnumSet.of(ChartUpdates.GLTextures));
    }
  }
  /**
   * Sets whether the colormap that is used should be shown alongside the chart in the form of a
   * legend.
   *
   * <p>Calls {@link #setColorMapVisible(boolean enabled) setColorMapVisible(true)}
   */
  public final void setColorMapVisible() {
    setColorMapVisible(true);
  }
  /**
   * Returns whether the colormap is shown alongside the chart in the form of a legend.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setColorMap(WAbstractColorMap colormap)
   * @see WAbstractDataSeries3D#setColorMapVisible(boolean enabled)
   * @see WAbstractDataSeries3D#setColorMapSide(Side side)
   */
  public boolean isColorMapVisible() {
    return this.showColorMap_;
  }
  /**
   * Sets whether the colormap is shown on the left or right.
   *
   * <p>The default side is {@link Side#Right}.
   *
   * <p>Note: only {@link Side#Left} and {@link Side#Right} are valid values for this function.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setColorMap(WAbstractColorMap colormap)
   * @see WAbstractDataSeries3D#setColorMapVisible(boolean enabled)
   */
  public void setColorMapSide(Side side) {
    if (this.colorMapSide_ == side) {
      return;
    }
    this.colorMapSide_ = side;
    if (this.chart_ != null) {
      this.chart_.updateChart(EnumSet.of(ChartUpdates.GLTextures));
    }
  }
  /**
   * Returns on which side the colormap is shown.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setColorMap(WAbstractColorMap colormap)
   * @see WAbstractDataSeries3D#setColorMapVisible(boolean enabled)
   * @see WAbstractDataSeries3D#setColorMapSide(Side side)
   */
  public Side getColorMapSide() {
    return this.colorMapSide_;
  }
  /**
   * Sets whether this dataseries is included in the chart-legend.
   *
   * <p>By default, dataseries are enabled in the legend.
   */
  public void setLegendEnabled(boolean enabled) {
    this.legendEnabled_ = enabled;
  }
  /**
   * Sets whether this dataseries is included in the chart-legend.
   *
   * <p>Calls {@link #setLegendEnabled(boolean enabled) setLegendEnabled(true)}
   */
  public final void setLegendEnabled() {
    setLegendEnabled(true);
  }
  /**
   * Returns whether this dataseries is included in the chart-legend.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setLegendEnabled(boolean enabled)
   */
  public boolean isLegendEnabled() {
    return this.legendEnabled_;
  }
  /**
   * Sets if this dataseries is hidden.
   *
   * <p>By default dataseries are visible.
   */
  public void setHidden(boolean enabled) {
    if (enabled != this.hidden_) {
      this.hidden_ = enabled;
      if (this.chart_ != null) {
        this.chart_.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
      }
    }
  }
  /**
   * Sets if this dataseries is hidden.
   *
   * <p>Calls {@link #setHidden(boolean enabled) setHidden(true)}
   */
  public final void setHidden() {
    setHidden(true);
  }
  /**
   * Returns if this dataseries is hidden.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#setHidden(boolean enabled)
   */
  public boolean isHidden() {
    return this.hidden_;
  }

  public void setDefaultTitle(int i) {
    String tmp = "dataset ";
    tmp.concat(String.valueOf(i));
    this.name_ = new WString(tmp);
  }

  public WColor getChartpaletteColor() {
    if (this.colormap_ != null) {
      return new WColor();
    }
    int index = 0;
    for (int i = 0; i < this.chart_.getDataSeries().size(); i++) {
      if (this.chart_.getDataSeries().get(i) == this) {
        break;
      } else {
        if (this.chart_.getDataSeries().get(i).getColorMap() == null) {
          index++;
        }
      }
    }
    return this.chart_.getPalette().getBrush(index).getColor();
  }

  public void setChart(WCartesian3DChart chart) {
    if (chart == this.chart_) {
      return;
    } else {
      if (this.chart_ != null) {
        this.chart_.removeDataSeries(this);
      }
    }
    if (this.chart_ != null && this.model_ != null) {
      clearConnections(this.connections_);
    }
    this.chart_ = chart;
    if (this.chart_ != null && this.model_ != null) {
      this.connections_.add(
          this.model_
              .modelReset()
              .addListener(
                  this.chart_,
                  () -> {
                    WAbstractDataSeries3D.this.chart_.updateChart(
                        EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                  }));
      this.connections_.add(
          this.model_
              .dataChanged()
              .addListener(
                  this.chart_,
                  () -> {
                    WAbstractDataSeries3D.this.chart_.updateChart(
                        EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                  }));
      this.connections_.add(
          this.model_
              .rowsInserted()
              .addListener(
                  this.chart_,
                  () -> {
                    WAbstractDataSeries3D.this.chart_.updateChart(
                        EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                  }));
      this.connections_.add(
          this.model_
              .columnsInserted()
              .addListener(
                  this.chart_,
                  () -> {
                    WAbstractDataSeries3D.this.chart_.updateChart(
                        EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                  }));
      this.connections_.add(
          this.model_
              .rowsRemoved()
              .addListener(
                  this.chart_,
                  () -> {
                    WAbstractDataSeries3D.this.chart_.updateChart(
                        EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                  }));
      this.connections_.add(
          this.model_
              .columnsRemoved()
              .addListener(
                  this.chart_,
                  () -> {
                    WAbstractDataSeries3D.this.chart_.updateChart(
                        EnumSet.of(ChartUpdates.GLTextures, ChartUpdates.GLContext));
                  }));
    }
  }

  public List<Object> getGlObjects() {
    return new ArrayList<Object>();
  }
  /**
   * Initialize GL resources.
   *
   * <p>This function is called by {@link WAbstractDataSeries3D#initializeGL() initializeGL()} in
   * the chart to which this dataseries was added.
   */
  public abstract void initializeGL();
  /**
   * Update the client-side painting function.
   *
   * <p>This function is called by {@link WAbstractDataSeries3D#paintGL() paintGL()} in the chart to
   * which this dataseries was added.
   */
  public abstract void paintGL();
  /**
   * Update GL resources.
   *
   * <p>This function is called by {@link WAbstractDataSeries3D#updateGL() updateGL()} in the chart
   * to which this dataseries was added. Before this function is called, {@link
   * WAbstractDataSeries3D#deleteAllGLResources() deleteAllGLResources()} is called.
   *
   * <p>
   *
   * @see WAbstractDataSeries3D#deleteAllGLResources()
   */
  public abstract void updateGL();
  /**
   * Act on resize events.
   *
   * <p>This function is called by {@link WAbstractDataSeries3D#resizeGL() resizeGL()} in the chart
   * to which this dataseries was added.
   */
  public abstract void resizeGL();
  /**
   * Delete GL resources.
   *
   * <p>This function is called by {@link WAbstractDataSeries3D#updateGL() updateGL()} in the chart
   * to which this dataseries was added.
   */
  public abstract void deleteAllGLResources();

  WGLWidget.Texture getColorTexture() {
    WPaintDevice cpd = null;
    if (!(this.colormap_ != null)) {
      cpd = this.chart_.createPaintDevice(new WLength(1), new WLength(1));
      WColor seriesColor = this.getChartpaletteColor();
      WPainter painter = new WPainter(cpd);
      painter.setPen(new WPen(seriesColor));
      painter.drawLine(0, 0.5, 1, 0.5);
      painter.end();
    } else {
      cpd = this.chart_.createPaintDevice(new WLength(1), new WLength(1024));
      WPainter painter = new WPainter(cpd);
      this.colormap_.createStrip(painter);
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

  protected WGLWidget.Texture getPointSpriteTexture() {
    WGLWidget.Texture tex = this.chart_.createTexture();
    this.chart_.bindTexture(WGLWidget.GLenum.TEXTURE_2D, tex);
    if (this.pointSprite_.length() == 0) {
      WPaintDevice cpd = this.chart_.createPaintDevice(new WLength(1), new WLength(1));
      WColor color = new WColor(255, 255, 255, 255);
      WPainter painter = new WPainter(cpd);
      painter.setPen(new WPen(color));
      painter.drawLine(0, 0.5, 1, 0.5);
      painter.end();
      this.chart_.texImage2D(
          WGLWidget.GLenum.TEXTURE_2D,
          0,
          WGLWidget.GLenum.RGBA,
          WGLWidget.GLenum.RGBA,
          WGLWidget.GLenum.UNSIGNED_BYTE,
          cpd);
    }
    return tex;
  }

  protected void loadPointSpriteTexture(final WGLWidget.Texture tex) {
    this.chart_.bindTexture(WGLWidget.GLenum.TEXTURE_2D, tex);
    if (this.pointSprite_.length() != 0) {
      this.chart_.texImage2D(
          WGLWidget.GLenum.TEXTURE_2D,
          0,
          WGLWidget.GLenum.RGBA,
          WGLWidget.GLenum.RGBA,
          WGLWidget.GLenum.UNSIGNED_BYTE,
          this.pointSprite_);
    }
  }

  WString name_;
  protected WAbstractItemModel model_;
  protected WCartesian3DChart chart_;
  double zMin_;
  double zMax_;
  boolean rangeCached_;
  double pointSize_;
  protected String pointSprite_;
  WAbstractColorMap colormap_;
  boolean showColorMap_;
  Side colorMapSide_;
  boolean legendEnabled_;
  boolean hidden_;
  javax.vecmath.Matrix4f mvMatrix_;
  private List<AbstractSignal.Connection> connections_;

  static void clearConnections(List<AbstractSignal.Connection> connections) {
    for (int i = 0; i < connections.size(); i++) {
      connections.get(i).disconnect();
    }
    connections.clear();
  }
}
