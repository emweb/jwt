/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing grid-based data for on a 3D chart.
 * <p>
 * 
 * General information can be found at {@link WAbstractDataSeries3D}.
 * Information on how the model is structured is provided in the subclasses.
 * GridData can be represented in three ways. This is indicated by DOCREF<a
 * class="el" href="namespaceWt_1_1Chart.html#cca4e123c9e4b212050bcfac488fceee">
 * Series3DType</a> and can be either PointSeries3D, SurfaceSeries3D or
 * BarSeries3D. Note that points and surfaces can only be added to charts of
 * type DOCREF<a class="el" href="group__charts.html#gg8d63464f873580c77508e1c0c26cbfea6ddab43d32242eb28831938a1e469a1f"
 * >ScatterPlot</a>, while bars can only be added to charts of type DOCREF<a
 * class="el" href="group__charts.html#gg8d63464f873580c77508e1c0c26cbfeaebfd9bd11d1126f2db7ff891c04c29f9"
 * >CategoryChart</a>.
 * <p>
 * When the data is shown as a surface, a mesh can be added to the surface. This
 * draws lines over the surface at the positions of the x- and y-values. For
 * bar-series data, it is possible to adjust the width of the bars in both
 * directions.
 * <p>
 * The three types of data-representation are illustrated below.
 * <p>
 * <div align="center"> <img src="doc-files//gridDataTypes.png"
 * alt="The three representation types of grid-based data">
 * <p>
 * <strong>The three representation types of grid-based data</strong>
 * </p>
 * </div>
 */
public abstract class WAbstractGridData extends WAbstractDataSeries3D {
	private static Logger logger = LoggerFactory
			.getLogger(WAbstractGridData.class);

	/**
	 * Constructor.
	 */
	public WAbstractGridData(WAbstractItemModel model) {
		super(model);
		this.seriesType_ = Series3DType.PointSeries3D;
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
		this.binaryResources_ = new ArrayList<WMemoryResource>();
		this.fragShader_ = new WGLWidget.Shader();
		this.colFragShader_ = new WGLWidget.Shader();
		this.meshFragShader_ = new WGLWidget.Shader();
		this.vertShader_ = new WGLWidget.Shader();
		this.colVertShader_ = new WGLWidget.Shader();
		this.meshVertShader_ = new WGLWidget.Shader();
		this.seriesProgram_ = new WGLWidget.Program();
		this.colSeriesProgram_ = new WGLWidget.Program();
		this.meshProgram_ = new WGLWidget.Program();
		this.vertexPosAttr_ = new WGLWidget.AttribLocation();
		this.vertexPosAttr2_ = new WGLWidget.AttribLocation();
		this.meshVertexPosAttr_ = new WGLWidget.AttribLocation();
		this.vertexSizeAttr_ = new WGLWidget.AttribLocation();
		this.vertexSizeAttr2_ = new WGLWidget.AttribLocation();
		this.vertexColAttr2_ = new WGLWidget.AttribLocation();
		this.barTexCoordAttr_ = new WGLWidget.AttribLocation();
		this.mvMatrixUniform_ = new WGLWidget.UniformLocation();
		this.mvMatrixUniform2_ = new WGLWidget.UniformLocation();
		this.mesh_mvMatrixUniform_ = new WGLWidget.UniformLocation();
		this.pMatrix_ = new WGLWidget.UniformLocation();
		this.pMatrix2_ = new WGLWidget.UniformLocation();
		this.mesh_pMatrix_ = new WGLWidget.UniformLocation();
		this.cMatrix_ = new WGLWidget.UniformLocation();
		this.cMatrix2_ = new WGLWidget.UniformLocation();
		this.mesh_cMatrix_ = new WGLWidget.UniformLocation();
		this.TexSampler_ = new WGLWidget.UniformLocation();
		this.mesh_colorUniform_ = new WGLWidget.UniformLocation();
		this.offset_ = new WGLWidget.UniformLocation();
		this.scaleFactor_ = new WGLWidget.UniformLocation();
		this.colormapTexture_ = new WGLWidget.Texture();
	}

	public abstract double minimum(Axis axis);

	public abstract double maximum(Axis axis);

	/**
	 * Sets the type of representation that will be used for the data.
	 * <p>
	 * All representations in DOCREF<a class="el"
	 * href="namespaceWt_1_1Chart.html#cca4e123c9e4b212050bcfac488fceee"
	 * >Series3DType</a> are possible for the data. Note that DOCREF<a
	 * class="el" href="namespaceWt_1_1Chart.html#cca4e123c9e4b212050bcfac488fceee8a491a3e05d49b93cb2b22c3fd06881c"
	 * >PointSeries3D</a> and DOCREF<a class="el" href="namespaceWt_1_1Chart.html#cca4e123c9e4b212050bcfac488fceeed47a9568df4bd41a9ac06562ac31ecc2"
	 * >SurfaceSeries3D</a> can only be used on a chart that is configured as a
	 * DOCREF<a class="el" href="group__charts.html#gg8d63464f873580c77508e1c0c26cbfea6ddab43d32242eb28831938a1e469a1f"
	 * >ScatterPlot</a> and DOCREF<a class="el" href="namespaceWt_1_1Chart.html#cca4e123c9e4b212050bcfac488fceee3c5fa7e4aed74f126086c1877c4acbfa"
	 * >BarSeries3D</a> can only be used on a chart that is configured to be a
	 * DOCREF<a class="el" href="group__charts.html#gg8d63464f873580c77508e1c0c26cbfeaebfd9bd11d1126f2db7ff891c04c29f9"
	 * >CategoryChart</a>.
	 * <p>
	 * The default value is PointSeries3D.
	 */
	public void setType(Series3DType type) {
		if (this.seriesType_ != type) {
			this.seriesType_ = type;
			if (this.chart_ != null) {
				this.chart_.updateChart(EnumSet
						.of(WCartesian3DChart.ChartUpdates.GLContext));
			}
		}
	}

	/**
	 * Returns the type of representation that will be used for the data.
	 * <p>
	 * 
	 * @see WAbstractGridData#setType(Series3DType type)
	 */
	public Series3DType getType() {
		return this.seriesType_;
	}

	/**
	 * Enables or disables a mesh for when a surface is drawn.
	 * <p>
	 * The default value is false. This option only takes effect when the type
	 * of this {@link WGridData} is DOCREF<a class="el" href="namespaceWt_1_1Chart.html#cca4e123c9e4b212050bcfac488fceeed47a9568df4bd41a9ac06562ac31ecc2"
	 * >SurfaceSeries3D</a>. The mesh is drawn at the position of the x-axis and
	 * y-axis values.
	 */
	public void setSurfaceMeshEnabled(boolean enabled) {
		if (enabled != this.surfaceMeshEnabled_) {
			this.surfaceMeshEnabled_ = enabled;
			if (this.seriesType_ == Series3DType.SurfaceSeries3D) {
				if (this.chart_ != null) {
					this.chart_.updateChart(EnumSet
							.of(WCartesian3DChart.ChartUpdates.GLContext));
				}
			}
		}
	}

	/**
	 * Enables or disables a mesh for when a surface is drawn.
	 * <p>
	 * Calls {@link #setSurfaceMeshEnabled(boolean enabled)
	 * setSurfaceMeshEnabled(true)}
	 */
	public final void setSurfaceMeshEnabled() {
		setSurfaceMeshEnabled(true);
	}

	/**
	 * Returns whether the surface-mesh is enabled for this dataseries.
	 * <p>
	 * 
	 * @see WAbstractGridData#setSurfaceMeshEnabled(boolean enabled)
	 */
	public boolean isSurfaceMeshEnabled() {
		return this.surfaceMeshEnabled_;
	}

	/**
	 * Sets the bar-width.
	 * <p>
	 * This option only takes effect when the type of this {@link WGridData} is
	 * BarSeries3D. The values provided should be between 0 and 1, where 1 lets
	 * the bars each take up 1/(nb of x/y-values) of the axis.
	 * <p>
	 * The default bar-width is 0.5 in both directions.
	 */
	public void setBarWidth(double xWidth, double yWidth) {
		if (xWidth != this.barWidthX_ || yWidth != this.barWidthY_) {
			this.barWidthX_ = xWidth;
			this.barWidthY_ = yWidth;
			if (this.chart_ != null) {
				this.chart_.updateChart(EnumSet
						.of(WCartesian3DChart.ChartUpdates.GLContext));
			}
		}
	}

	/**
	 * Returns the bar-width in the X-axis direction.
	 * <p>
	 * 
	 * @see WAbstractGridData#setBarWidth(double xWidth, double yWidth)
	 */
	public double getBarWidthX() {
		return this.barWidthX_;
	}

	/**
	 * Returns the bar-width in the Y-axis direction.
	 * <p>
	 * 
	 * @see WAbstractGridData#setBarWidth(double xWidth, double yWidth)
	 */
	public double getBarWidthY() {
		return this.barWidthY_;
	}

	/**
	 * Sets the {@link WPen} that is used for drawing the mesh.
	 * <p>
	 * Used when drawing the mesh on a surface or the lines around bars. The
	 * default is a default constructed {@link WPen} (black and one pixel wide).
	 * <p>
	 * Note: only the width and color of this {@link WPen} are used.
	 * <p>
	 * 
	 * @see WAbstractGridData#setSurfaceMeshEnabled(boolean enabled)
	 */
	public void setPen(final WPen pen) {
		this.meshPen_ = pen;
		if (this.chart_ != null) {
			this.chart_.updateChart(EnumSet
					.of(WCartesian3DChart.ChartUpdates.GLContext));
		}
	}

	/**
	 * Returns the pen that is used for drawing the mesh.
	 * <p>
	 * 
	 * @see WAbstractGridData#setPen(WPen pen)
	 */
	public WPen getPen() {
		return this.meshPen_;
	}

	public static final int SURFACE_SIDE_LIMIT = 256;
	public static final int BAR_BUFFER_LIMIT = 8190;

	public abstract int getNbXPoints();

	public abstract int getNbYPoints();

	public abstract WString axisLabel(int u, Axis axis);

	abstract Object data(int i, int j);

	public void initializeGL() {
	}

	public void paintGL() {
		if (this.hidden_) {
			return;
		}
		switch (this.seriesType_) {
		case PointSeries3D:
			if (this.chart_.getType() != ChartType.ScatterPlot) {
				return;
			}
			break;
		case SurfaceSeries3D:
			if (this.chart_.getType() != ChartType.ScatterPlot) {
				return;
			}
			break;
		case BarSeries3D:
			if (this.chart_.getType() != ChartType.CategoryChart) {
				return;
			}
			break;
		}
		this.chart_.disable(WGLWidget.GLenum.CULL_FACE);
		this.chart_.enable(WGLWidget.GLenum.DEPTH_TEST);
		for (int i = 0; i < this.vertexPosBuffers_.size(); i++) {
			this.chart_.useProgram(this.seriesProgram_);
			this.chart_
					.uniformMatrix4(this.cMatrix_, this.chart_.getJsMatrix());
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexPosBuffers_.get(i));
			this.chart_.vertexAttribPointer(this.vertexPosAttr_, 3,
					WGLWidget.GLenum.FLOAT, false, 0, 0);
			this.chart_.enableVertexAttribArray(this.vertexPosAttr_);
			if (this.seriesType_ == Series3DType.BarSeries3D) {
				this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
						this.colormapTexBuffers_.get(i));
				this.chart_.vertexAttribPointer(this.barTexCoordAttr_, 2,
						WGLWidget.GLenum.FLOAT, false, 0, 0);
				this.chart_.enableVertexAttribArray(this.barTexCoordAttr_);
			}
			this.chart_.activeTexture(WGLWidget.GLenum.TEXTURE0);
			this.chart_.bindTexture(WGLWidget.GLenum.TEXTURE_2D,
					this.colormapTexture_);
			this.chart_.uniform1i(this.TexSampler_, 0);
			if (this.seriesType_ == Series3DType.BarSeries3D) {
				this.chart_.enable(WGLWidget.GLenum.POLYGON_OFFSET_FILL);
				float unitOffset = (float) (Math.pow(5, this.meshPen_
						.getWidth().getValue()) < 10000 ? Math.pow(5,
						this.meshPen_.getWidth().getValue()) : 10000);
				this.chart_.polygonOffset(1, unitOffset);
				this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
						this.indexBuffers_.get(i));
				this.chart_.drawElements(WGLWidget.GLenum.TRIANGLES,
						this.indexBufferSizes_.get(i),
						WGLWidget.GLenum.UNSIGNED_SHORT, 0);
				this.chart_.disableVertexAttribArray(this.vertexPosAttr_);
				this.chart_.disableVertexAttribArray(this.barTexCoordAttr_);
			} else {
				if (this.seriesType_ == Series3DType.SurfaceSeries3D) {
					this.chart_.enable(WGLWidget.GLenum.POLYGON_OFFSET_FILL);
					float unitOffset = (float) (Math.pow(5, this.meshPen_
							.getWidth().getValue()) < 10000 ? Math.pow(5,
							this.meshPen_.getWidth().getValue()) : 10000);
					this.chart_.polygonOffset(1, unitOffset);
					this.chart_.bindBuffer(
							WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
							this.indexBuffers_.get(i));
					this.chart_.drawElements(WGLWidget.GLenum.TRIANGLE_STRIP,
							this.indexBufferSizes_.get(i),
							WGLWidget.GLenum.UNSIGNED_SHORT, 0);
					this.chart_.disable(WGLWidget.GLenum.POLYGON_OFFSET_FILL);
					this.chart_.disableVertexAttribArray(this.vertexPosAttr_);
				} else {
					if (!WApplication.getInstance().getEnvironment()
							.agentIsIE()) {
						this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
								this.vertexSizeBuffers_.get(i));
						this.chart_.vertexAttribPointer(this.vertexSizeAttr_,
								1, WGLWidget.GLenum.FLOAT, false, 0, 0);
						this.chart_
								.enableVertexAttribArray(this.vertexSizeAttr_);
					}
					this.chart_.drawArrays(WGLWidget.GLenum.POINTS, 0,
							this.vertexPosBufferSizes_.get(i) / 3);
					this.chart_.disableVertexAttribArray(this.vertexPosAttr_);
					if (!WApplication.getInstance().getEnvironment()
							.agentIsIE()) {
						this.chart_
								.disableVertexAttribArray(this.vertexSizeAttr_);
					}
				}
			}
			if (this.seriesType_ == Series3DType.BarSeries3D
					|| this.seriesType_ == Series3DType.SurfaceSeries3D
					&& this.surfaceMeshEnabled_) {
				this.chart_.useProgram(this.meshProgram_);
				this.chart_.depthFunc(WGLWidget.GLenum.LEQUAL);
				this.chart_.uniformMatrix4(this.mesh_cMatrix_, this.chart_
						.getJsMatrix());
				this.chart_.uniform4f(this.mesh_colorUniform_,
						(float) this.meshPen_.getColor().getRed(),
						(float) this.meshPen_.getColor().getGreen(),
						(float) this.meshPen_.getColor().getBlue(),
						(float) this.meshPen_.getColor().getAlpha());
				this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
						this.vertexPosBuffers_.get(i));
				this.chart_.vertexAttribPointer(this.meshVertexPosAttr_, 3,
						WGLWidget.GLenum.FLOAT, false, 0, 0);
				this.chart_.enableVertexAttribArray(this.meshVertexPosAttr_);
				this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
						this.overlayLinesBuffers_.get(i));
				if (this.seriesType_ == Series3DType.SurfaceSeries3D) {
					this.chart_
							.lineWidth(this.meshPen_.getWidth().getValue() == 0 ? 1.0
									: this.meshPen_.getWidth().getValue());
					this.chart_.drawElements(WGLWidget.GLenum.LINE_STRIP,
							this.lineBufferSizes_.get(i),
							WGLWidget.GLenum.UNSIGNED_SHORT, 0);
				} else {
					if (this.seriesType_ == Series3DType.BarSeries3D) {
						this.chart_.lineWidth(this.meshPen_.getWidth()
								.getValue() == 0 ? 1.0 : this.meshPen_
								.getWidth().getValue());
						this.chart_.drawElements(WGLWidget.GLenum.LINES,
								this.lineBufferSizes_.get(i),
								WGLWidget.GLenum.UNSIGNED_SHORT, 0);
					}
				}
				this.chart_.disableVertexAttribArray(this.meshVertexPosAttr_);
			}
		}
		for (int i = 0; i < this.vertexPosBuffers2_.size(); i++) {
			this.chart_.useProgram(this.colSeriesProgram_);
			this.chart_.uniformMatrix4(this.cMatrix2_, this.chart_
					.getJsMatrix());
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexPosBuffers2_.get(i));
			this.chart_.vertexAttribPointer(this.vertexPosAttr2_, 3,
					WGLWidget.GLenum.FLOAT, false, 0, 0);
			this.chart_.enableVertexAttribArray(this.vertexPosAttr2_);
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.vertexColorBuffers2_.get(i));
			this.chart_.vertexAttribPointer(this.vertexColAttr2_, 4,
					WGLWidget.GLenum.FLOAT, false, 0, 0);
			this.chart_.enableVertexAttribArray(this.vertexColAttr2_);
			if (this.seriesType_ == Series3DType.PointSeries3D) {
				if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
					this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
							this.vertexSizeBuffers2_.get(i));
					this.chart_.vertexAttribPointer(this.vertexSizeAttr2_, 1,
							WGLWidget.GLenum.FLOAT, false, 0, 0);
					this.chart_.enableVertexAttribArray(this.vertexSizeAttr2_);
				}
				this.chart_.drawArrays(WGLWidget.GLenum.POINTS, 0,
						this.vertexPosBuffer2Sizes_.get(i) / 3);
				if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
					this.chart_.disableVertexAttribArray(this.vertexSizeAttr2_);
				}
			} else {
				if (this.seriesType_ == Series3DType.BarSeries3D) {
					this.chart_.bindBuffer(
							WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
							this.indexBuffers2_.get(i));
					this.chart_.drawElements(WGLWidget.GLenum.TRIANGLES,
							this.indexBufferSizes2_.get(i),
							WGLWidget.GLenum.UNSIGNED_SHORT, 0);
				}
			}
			this.chart_.disableVertexAttribArray(this.vertexPosAttr2_);
			this.chart_.disableVertexAttribArray(this.vertexColAttr2_);
			if (this.seriesType_ == Series3DType.BarSeries3D) {
				this.chart_.useProgram(this.meshProgram_);
				this.chart_.depthFunc(WGLWidget.GLenum.LEQUAL);
				this.chart_.enable(WGLWidget.GLenum.POLYGON_OFFSET_FILL);
				this.chart_.polygonOffset(1, 0.001);
				this.chart_.uniformMatrix4(this.mesh_cMatrix_, this.chart_
						.getJsMatrix());
				this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
						this.vertexPosBuffers2_.get(i));
				this.chart_.vertexAttribPointer(this.meshVertexPosAttr_, 3,
						WGLWidget.GLenum.FLOAT, false, 0, 0);
				this.chart_.enableVertexAttribArray(this.meshVertexPosAttr_);
				this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
						this.overlayLinesBuffers2_.get(i));
				this.chart_.drawElements(WGLWidget.GLenum.LINES,
						this.lineBufferSizes2_.get(i),
						WGLWidget.GLenum.UNSIGNED_SHORT, 0);
				this.chart_.disableVertexAttribArray(this.meshVertexPosAttr_);
			}
		}
		this.chart_.enable(WGLWidget.GLenum.CULL_FACE);
		this.chart_.disable(WGLWidget.GLenum.DEPTH_TEST);
	}

	public void updateGL() {
		switch (this.seriesType_) {
		case PointSeries3D:
			if (this.chart_.getType() != ChartType.ScatterPlot) {
				return;
			}
			this.initializePointSeriesBuffers();
			break;
		case SurfaceSeries3D:
			if (this.chart_.getType() != ChartType.ScatterPlot) {
				return;
			}
			this.initializeSurfaceSeriesBuffers();
			break;
		case BarSeries3D:
			if (this.chart_.getType() != ChartType.CategoryChart) {
				return;
			}
			this.initializeBarSeriesBuffers();
			break;
		}
		this.colormapTexture_ = this.getColorTexture();
		this.chart_.texParameteri(WGLWidget.GLenum.TEXTURE_2D,
				WGLWidget.GLenum.TEXTURE_MAG_FILTER, WGLWidget.GLenum.NEAREST);
		this.chart_.texParameteri(WGLWidget.GLenum.TEXTURE_2D,
				WGLWidget.GLenum.TEXTURE_MIN_FILTER, WGLWidget.GLenum.NEAREST);
		this.chart_
				.texParameteri(WGLWidget.GLenum.TEXTURE_2D,
						WGLWidget.GLenum.TEXTURE_WRAP_S,
						WGLWidget.GLenum.CLAMP_TO_EDGE);
		this.chart_
				.texParameteri(WGLWidget.GLenum.TEXTURE_2D,
						WGLWidget.GLenum.TEXTURE_WRAP_T,
						WGLWidget.GLenum.CLAMP_TO_EDGE);
		this.initShaders();
		double min;
		double max;
		switch (this.seriesType_) {
		case BarSeries3D:
			break;
		case PointSeries3D:
		case SurfaceSeries3D:
			this.chart_.useProgram(this.seriesProgram_);
			if (this.colormap_ != null) {
				min = this.chart_.toPlotCubeCoords(this.colormap_.getMinimum(),
						Axis.ZAxis_3D);
				max = this.chart_.toPlotCubeCoords(this.colormap_.getMaximum(),
						Axis.ZAxis_3D);
				this.chart_.uniform1f(this.offset_, min);
				this.chart_.uniform1f(this.scaleFactor_, 1.0 / (max - min));
			} else {
				this.chart_.uniform1f(this.offset_, 0.0);
				this.chart_.uniform1f(this.scaleFactor_, 1.0);
			}
			break;
		}
		;
		this.chart_.useProgram(this.seriesProgram_);
		this.chart_.uniformMatrix4(this.mvMatrixUniform_, this.mvMatrix_);
		this.chart_.uniformMatrix4(this.pMatrix_, this.chart_.getPMatrix());
		switch (this.seriesType_) {
		case BarSeries3D:
			this.chart_.useProgram(this.meshProgram_);
			this.chart_.uniformMatrix4(this.mesh_mvMatrixUniform_,
					this.mvMatrix_);
			this.chart_.uniformMatrix4(this.mesh_pMatrix_, this.chart_
					.getPMatrix());
		case PointSeries3D:
			this.chart_.useProgram(this.colSeriesProgram_);
			this.chart_.uniformMatrix4(this.mvMatrixUniform2_, this.mvMatrix_);
			this.chart_
					.uniformMatrix4(this.pMatrix2_, this.chart_.getPMatrix());
			break;
		case SurfaceSeries3D:
			if (this.surfaceMeshEnabled_) {
				this.chart_.useProgram(this.meshProgram_);
				this.chart_.uniformMatrix4(this.mesh_mvMatrixUniform_,
						this.mvMatrix_);
				this.chart_.uniformMatrix4(this.mesh_pMatrix_, this.chart_
						.getPMatrix());
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
			this.chart_
					.uniformMatrix4(this.pMatrix2_, this.chart_.getPMatrix());
		}
		if (!this.meshProgram_.isNull()) {
			this.chart_.useProgram(this.meshProgram_);
			this.chart_.uniformMatrix4(this.mesh_pMatrix_, this.chart_
					.getPMatrix());
		}
	}

	public void deleteAllGLResources() {
		if (this.seriesProgram_.isNull()) {
			return;
		}
		this.chart_.detachShader(this.seriesProgram_, this.fragShader_);
		this.chart_.detachShader(this.seriesProgram_, this.vertShader_);
		this.chart_.deleteShader(this.fragShader_);
		this.chart_.deleteShader(this.vertShader_);
		this.chart_.deleteProgram(this.seriesProgram_);
		this.seriesProgram_.clear();
		if (!this.colSeriesProgram_.isNull()) {
			this.chart_.detachShader(this.colSeriesProgram_,
					this.colFragShader_);
			this.chart_.detachShader(this.colSeriesProgram_,
					this.colVertShader_);
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
		for (int i = 0; i < this.vertexPosBuffers_.size(); i++) {
			if (!this.vertexPosBuffers_.get(i).getJsRef().equals("")) {
				this.chart_.deleteBuffer(this.vertexPosBuffers_.get(i));
				this.vertexPosBuffers_.get(i).clear();
			}
		}
		for (int i = 0; i < this.vertexSizeBuffers_.size(); i++) {
			if (this.vertexSizeBuffers_.get(i).isNull()) {
				this.chart_.deleteBuffer(this.vertexSizeBuffers_.get(i));
				this.vertexSizeBuffers_.get(i).clear();
			}
		}
		for (int i = 0; i < this.vertexPosBuffers2_.size(); i++) {
			if (this.vertexPosBuffers2_.get(i).isNull()) {
				this.chart_.deleteBuffer(this.vertexPosBuffers2_.get(i));
				this.vertexPosBuffers2_.get(i).clear();
				this.chart_.deleteBuffer(this.vertexColorBuffers2_.get(i));
				this.vertexColorBuffers2_.get(i).clear();
			}
		}
		for (int i = 0; i < this.vertexSizeBuffers2_.size(); i++) {
			if (this.vertexSizeBuffers2_.get(i).isNull()) {
				this.chart_.deleteBuffer(this.vertexSizeBuffers2_.get(i));
				this.vertexSizeBuffers2_.get(i).clear();
			}
		}
		for (int i = 0; i < this.indexBuffers_.size(); i++) {
			if (this.indexBuffers_.get(i).isNull()) {
				this.chart_.deleteBuffer(this.indexBuffers_.get(i));
				this.indexBuffers_.get(i).clear();
			}
		}
		for (int i = 0; i < this.overlayLinesBuffers_.size(); i++) {
			if (this.overlayLinesBuffers_.get(i).isNull()) {
				this.chart_.deleteBuffer(this.overlayLinesBuffers_.get(i));
				this.overlayLinesBuffers_.get(i).clear();
			}
		}
		for (int i = 0; i < this.colormapTexBuffers_.size(); i++) {
			if (this.colormapTexBuffers_.get(i).isNull()) {
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
		this.indexBufferSizes_.clear();
		this.overlayLinesBuffers_.clear();
		this.lineBufferSizes_.clear();
		this.colormapTexBuffers_.clear();
		if (this.colormapTexture_.isNull()) {
			this.chart_.deleteTexture(this.colormapTexture_);
			this.colormapTexture_.clear();
		}
	}

	abstract void pointDataFromModel(final java.nio.ByteBuffer simplePtsArray,
			final java.nio.ByteBuffer simplePtsSize,
			final java.nio.ByteBuffer coloredPtsArray,
			final java.nio.ByteBuffer coloredPtsSize,
			final java.nio.ByteBuffer coloredPtsColor);

	abstract void surfaceDataFromModel(
			final List<java.nio.ByteBuffer> simplePtsArrays);

	protected abstract void barDataFromModel(
			final List<java.nio.ByteBuffer> simplePtsArrays,
			final List<java.nio.ByteBuffer> coloredPtsArrays,
			final List<java.nio.ByteBuffer> coloredPtsColors);

	abstract int getCountSimpleData();

	float stackAllValues(List<WAbstractGridData> dataseries, int i, int j) {
		float value = 0;
		for (int k = 0; k < dataseries.size(); k++) {
			float modelVal = (float) StringUtils.asNumber(dataseries.get(k)
					.data(i, j));
			if (modelVal <= 0) {
				modelVal = zeroBarCompensation;
			}
			value += modelVal;
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
		case BarSeries3D:
			this.fragShader_ = this.chart_
					.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
			this.chart_.shaderSource(this.fragShader_, barFragShaderSrc);
			this.chart_.compileShader(this.fragShader_);
			this.vertShader_ = this.chart_
					.createShader(WGLWidget.GLenum.VERTEX_SHADER);
			this.chart_.shaderSource(this.vertShader_, barVertexShaderSrc);
			this.chart_.compileShader(this.vertShader_);
			this.seriesProgram_ = this.chart_.createProgram();
			this.chart_.attachShader(this.seriesProgram_, this.vertShader_);
			this.chart_.attachShader(this.seriesProgram_, this.fragShader_);
			this.chart_.linkProgram(this.seriesProgram_);
			this.vertexPosAttr_ = this.chart_.getAttribLocation(
					this.seriesProgram_, "aVertexPosition");
			this.barTexCoordAttr_ = this.chart_.getAttribLocation(
					this.seriesProgram_, "aTextureCoord");
			this.mvMatrixUniform_ = this.chart_.getUniformLocation(
					this.seriesProgram_, "uMVMatrix");
			this.pMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_,
					"uPMatrix");
			this.cMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_,
					"uCMatrix");
			this.TexSampler_ = this.chart_.getUniformLocation(
					this.seriesProgram_, "uSampler");
			this.colFragShader_ = this.chart_
					.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
			this.chart_.shaderSource(this.colFragShader_, colBarFragShaderSrc);
			this.chart_.compileShader(this.colFragShader_);
			this.colVertShader_ = this.chart_
					.createShader(WGLWidget.GLenum.VERTEX_SHADER);
			this.chart_
					.shaderSource(this.colVertShader_, colBarVertexShaderSrc);
			this.chart_.compileShader(this.colVertShader_);
			this.colSeriesProgram_ = this.chart_.createProgram();
			this.chart_.attachShader(this.colSeriesProgram_,
					this.colFragShader_);
			this.chart_.attachShader(this.colSeriesProgram_,
					this.colVertShader_);
			this.chart_.linkProgram(this.colSeriesProgram_);
			this.vertexPosAttr2_ = this.chart_.getAttribLocation(
					this.colSeriesProgram_, "aVertexPosition");
			this.vertexColAttr2_ = this.chart_.getAttribLocation(
					this.colSeriesProgram_, "aVertexColor");
			this.mvMatrixUniform2_ = this.chart_.getUniformLocation(
					this.colSeriesProgram_, "uMVMatrix");
			this.pMatrix2_ = this.chart_.getUniformLocation(
					this.colSeriesProgram_, "uPMatrix");
			this.cMatrix2_ = this.chart_.getUniformLocation(
					this.colSeriesProgram_, "uCMatrix");
			break;
		case PointSeries3D:
			this.fragShader_ = this.chart_
					.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
			this.chart_.shaderSource(this.fragShader_, ptFragShaderSrc);
			this.chart_.compileShader(this.fragShader_);
			this.vertShader_ = this.chart_
					.createShader(WGLWidget.GLenum.VERTEX_SHADER);
			this.chart_.shaderSource(this.vertShader_, ptVertexShaderSrc);
			this.chart_.compileShader(this.vertShader_);
			this.seriesProgram_ = this.chart_.createProgram();
			this.chart_.attachShader(this.seriesProgram_, this.vertShader_);
			this.chart_.attachShader(this.seriesProgram_, this.fragShader_);
			this.chart_.linkProgram(this.seriesProgram_);
			this.vertexPosAttr_ = this.chart_.getAttribLocation(
					this.seriesProgram_, "aVertexPosition");
			if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
				this.vertexSizeAttr_ = this.chart_.getAttribLocation(
						this.seriesProgram_, "aPointSize");
			}
			this.mvMatrixUniform_ = this.chart_.getUniformLocation(
					this.seriesProgram_, "uMVMatrix");
			this.pMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_,
					"uPMatrix");
			this.cMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_,
					"uCMatrix");
			this.TexSampler_ = this.chart_.getUniformLocation(
					this.seriesProgram_, "uSampler");
			this.offset_ = this.chart_.getUniformLocation(this.seriesProgram_,
					"uOffset");
			this.scaleFactor_ = this.chart_.getUniformLocation(
					this.seriesProgram_, "uScaleFactor");
			this.colFragShader_ = this.chart_
					.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
			this.chart_.shaderSource(this.colFragShader_, colPtFragShaderSrc);
			this.chart_.compileShader(this.colFragShader_);
			this.colVertShader_ = this.chart_
					.createShader(WGLWidget.GLenum.VERTEX_SHADER);
			this.chart_.shaderSource(this.colVertShader_, colPtVertexShaderSrc);
			this.chart_.compileShader(this.colVertShader_);
			this.colSeriesProgram_ = this.chart_.createProgram();
			this.chart_.attachShader(this.colSeriesProgram_,
					this.colVertShader_);
			this.chart_.attachShader(this.colSeriesProgram_,
					this.colFragShader_);
			this.chart_.linkProgram(this.colSeriesProgram_);
			this.vertexPosAttr2_ = this.chart_.getAttribLocation(
					this.colSeriesProgram_, "aVertexPosition");
			if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
				this.vertexSizeAttr2_ = this.chart_.getAttribLocation(
						this.colSeriesProgram_, "aPointSize");
			}
			this.vertexColAttr2_ = this.chart_.getAttribLocation(
					this.colSeriesProgram_, "aColor");
			this.mvMatrixUniform2_ = this.chart_.getUniformLocation(
					this.colSeriesProgram_, "uMVMatrix");
			this.pMatrix2_ = this.chart_.getUniformLocation(
					this.colSeriesProgram_, "uPMatrix");
			this.cMatrix2_ = this.chart_.getUniformLocation(
					this.colSeriesProgram_, "uCMatrix");
			break;
		case SurfaceSeries3D:
			this.fragShader_ = this.chart_
					.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
			this.chart_.shaderSource(this.fragShader_, surfFragShaderSrc);
			this.chart_.compileShader(this.fragShader_);
			this.vertShader_ = this.chart_
					.createShader(WGLWidget.GLenum.VERTEX_SHADER);
			this.chart_.shaderSource(this.vertShader_, surfVertexShaderSrc);
			this.chart_.compileShader(this.vertShader_);
			this.seriesProgram_ = this.chart_.createProgram();
			this.chart_.attachShader(this.seriesProgram_, this.vertShader_);
			this.chart_.attachShader(this.seriesProgram_, this.fragShader_);
			this.chart_.linkProgram(this.seriesProgram_);
			this.vertexPosAttr_ = this.chart_.getAttribLocation(
					this.seriesProgram_, "aVertexPosition");
			this.mvMatrixUniform_ = this.chart_.getUniformLocation(
					this.seriesProgram_, "uMVMatrix");
			this.pMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_,
					"uPMatrix");
			this.cMatrix_ = this.chart_.getUniformLocation(this.seriesProgram_,
					"uCMatrix");
			this.TexSampler_ = this.chart_.getUniformLocation(
					this.seriesProgram_, "uSampler");
			this.offset_ = this.chart_.getUniformLocation(this.seriesProgram_,
					"uOffset");
			this.scaleFactor_ = this.chart_.getUniformLocation(
					this.seriesProgram_, "uScaleFactor");
			break;
		}
		;
		if (this.seriesType_ == Series3DType.BarSeries3D
				|| this.seriesType_ == Series3DType.SurfaceSeries3D
				&& this.surfaceMeshEnabled_) {
			this.meshFragShader_ = this.chart_
					.createShader(WGLWidget.GLenum.FRAGMENT_SHADER);
			this.chart_.shaderSource(this.meshFragShader_, meshFragShaderSrc);
			this.chart_.compileShader(this.meshFragShader_);
			this.meshVertShader_ = this.chart_
					.createShader(WGLWidget.GLenum.VERTEX_SHADER);
			this.chart_.shaderSource(this.meshVertShader_, meshVertexShaderSrc);
			this.chart_.compileShader(this.meshVertShader_);
			this.meshProgram_ = this.chart_.createProgram();
			this.chart_.attachShader(this.meshProgram_, this.meshVertShader_);
			this.chart_.attachShader(this.meshProgram_, this.meshFragShader_);
			this.chart_.linkProgram(this.meshProgram_);
			this.meshVertexPosAttr_ = this.chart_.getAttribLocation(
					this.meshProgram_, "aVertexPosition");
			this.mesh_mvMatrixUniform_ = this.chart_.getUniformLocation(
					this.meshProgram_, "uMVMatrix");
			this.mesh_pMatrix_ = this.chart_.getUniformLocation(
					this.meshProgram_, "uPMatrix");
			this.mesh_cMatrix_ = this.chart_.getUniformLocation(
					this.meshProgram_, "uCMatrix");
			this.mesh_colorUniform_ = this.chart_.getUniformLocation(
					this.meshProgram_, "uColor");
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
		coloredPtsArrays.add(WebGLUtils
				.newByteBuffer(4 * (3 * (Nx * Ny - cnt))));
		coloredPtsSizes.add(WebGLUtils.newByteBuffer(4 * (Nx * Ny - cnt)));
		coloredPtsColors.add(WebGLUtils
				.newByteBuffer(4 * (4 * (Nx * Ny - cnt))));
		this.pointDataFromModel(simplePtsArrays.get(0), simplePtsSizes.get(0),
				coloredPtsArrays.get(0), coloredPtsSizes.get(0),
				coloredPtsColors.get(0));
		for (int i = 0; i < simplePtsArrays.size(); i++) {
			if (simplePtsArrays.get(i).capacity() / 4 != 0) {
				this.loadBinaryResource(simplePtsArrays.get(i),
						this.vertexPosBuffers_);
				this.vertexPosBufferSizes_.add(simplePtsArrays.get(i)
						.capacity() / 4);
				this.loadBinaryResource(simplePtsSizes.get(i),
						this.vertexSizeBuffers_);
			}
			if (coloredPtsArrays.get(i).capacity() / 4 != 0) {
				this.loadBinaryResource(coloredPtsArrays.get(i),
						this.vertexPosBuffers2_);
				this.vertexPosBuffer2Sizes_.add(coloredPtsArrays.get(i)
						.capacity() / 4);
				this.loadBinaryResource(coloredPtsSizes.get(i),
						this.vertexSizeBuffers2_);
				this.loadBinaryResource(coloredPtsColors.get(i),
						this.vertexColorBuffers2_);
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
				simplePtsArrays
						.add(WebGLUtils
								.newByteBuffer(4 * (3 * SURFACE_SIDE_LIMIT * SURFACE_SIDE_LIMIT)));
			}
			simplePtsArrays
					.add(WebGLUtils
							.newByteBuffer(4 * (3 * SURFACE_SIDE_LIMIT * (Ny - (nbYaxisBuffers - 1)
									* (SURFACE_SIDE_LIMIT - 1)))));
		}
		for (int j = 0; j < nbYaxisBuffers - 1; j++) {
			simplePtsArrays.add(WebGLUtils
					.newByteBuffer(4 * (3 * (Nx - (nbXaxisBuffers - 1)
							* (SURFACE_SIDE_LIMIT - 1)) * SURFACE_SIDE_LIMIT)));
		}
		simplePtsArrays
				.add(WebGLUtils
						.newByteBuffer(4 * (3 * (Nx - (nbXaxisBuffers - 1)
								* (SURFACE_SIDE_LIMIT - 1)) * (Ny - (nbYaxisBuffers - 1)
								* (SURFACE_SIDE_LIMIT - 1)))));
		this.surfaceDataFromModel(simplePtsArrays);
		for (int i = 0; i < simplePtsArrays.size(); i++) {
			this.loadBinaryResource(simplePtsArrays.get(i),
					this.vertexPosBuffers_);
			this.vertexPosBufferSizes_
					.add(simplePtsArrays.get(i).capacity() / 4);
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
			java.nio.IntBuffer vertexIndices = java.nio.IntBuffer
					.allocate((Nx_patch - 1) * (Ny_patch + 1) * 2);
			this.generateVertexIndices(vertexIndices, Nx_patch, Ny_patch);
			this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					this.indexBuffers_.get(i));
			this.chart_.bufferDataiv(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					vertexIndices, WGLWidget.GLenum.STATIC_DRAW,
					WGLWidget.GLenum.UNSIGNED_SHORT);
			this.indexBufferSizes_.add(vertexIndices.capacity());
			this.overlayLinesBuffers_.add(this.chart_.createBuffer());
			java.nio.IntBuffer lineIndices = java.nio.IntBuffer.allocate(2
					* Nx_patch * Ny_patch);
			this.generateMeshIndices(lineIndices, Nx_patch, Ny_patch);
			this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					this.overlayLinesBuffers_.get(i));
			this.chart_.bufferDataiv(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					lineIndices, WGLWidget.GLenum.STATIC_DRAW,
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
			simplePtsArrays.add(WebGLUtils
					.newByteBuffer(4 * (PT_INFO_SIZE * BAR_BUFFER_LIMIT)));
			barVertexArrays.add(WebGLUtils
					.newByteBuffer(4 * (PTS_PER_BAR * 3 * BAR_BUFFER_LIMIT)));
		}
		simplePtsArrays.add(WebGLUtils
				.newByteBuffer(4 * (PT_INFO_SIZE * (cnt - BAR_BUFFER_LIMIT
						* (nbSimpleBarBuffers - 1)))));
		barVertexArrays.add(WebGLUtils
				.newByteBuffer(4 * (PTS_PER_BAR * 3 * (cnt - BAR_BUFFER_LIMIT
						* (nbSimpleBarBuffers - 1)))));
		for (int i = 0; i < nbColoredBarBuffers - 1; i++) {
			coloredPtsArrays.add(WebGLUtils
					.newByteBuffer(4 * (PT_INFO_SIZE * BAR_BUFFER_LIMIT)));
			coloredPtsColors.add(WebGLUtils.newByteBuffer(4 * (PTS_PER_BAR
					* PT_INFO_SIZE * BAR_BUFFER_LIMIT)));
			coloredBarVertexArrays.add(WebGLUtils
					.newByteBuffer(4 * (PTS_PER_BAR * 3 * BAR_BUFFER_LIMIT)));
		}
		coloredPtsArrays.add(WebGLUtils.newByteBuffer(4 * (PT_INFO_SIZE * (Nx
				* Ny - cnt - BAR_BUFFER_LIMIT * (nbColoredBarBuffers - 1)))));
		coloredPtsColors.add(WebGLUtils.newByteBuffer(4 * (PTS_PER_BAR
				* PT_INFO_SIZE * (Nx * Ny - cnt - BAR_BUFFER_LIMIT
				* (nbColoredBarBuffers - 1)))));
		coloredBarVertexArrays
				.add(WebGLUtils.newByteBuffer(4 * (PTS_PER_BAR * 3 * (Nx * Ny
						- cnt - BAR_BUFFER_LIMIT * (nbColoredBarBuffers - 1)))));
		this.barDataFromModel(simplePtsArrays, coloredPtsArrays,
				coloredPtsColors);
		for (int i = 0; i < simplePtsArrays.size(); i++) {
			this.barSeriesVertexData(simplePtsArrays.get(i), barVertexArrays
					.get(i));
		}
		for (int i = 0; i < coloredPtsArrays.size(); i++) {
			this.barSeriesVertexData(coloredPtsArrays.get(i),
					coloredBarVertexArrays.get(i));
		}
		for (int i = 0; i < barVertexArrays.size(); i++) {
			this.loadBinaryResource(barVertexArrays.get(i),
					this.vertexPosBuffers_);
			this.vertexPosBufferSizes_
					.add(barVertexArrays.get(i).capacity() / 4);
		}
		for (int i = 0; i < coloredBarVertexArrays.size(); i++) {
			this.loadBinaryResource(coloredBarVertexArrays.get(i),
					this.vertexPosBuffers2_);
			this.vertexPosBuffer2Sizes_.add(coloredBarVertexArrays.get(i)
					.capacity() / 4);
			this.loadBinaryResource(coloredPtsColors.get(i),
					this.vertexColorBuffers2_);
		}
		for (int i = 0; i < simplePtsArrays.size(); i++) {
			this.indexBuffers_.add(this.chart_.createBuffer());
			java.nio.IntBuffer vertexIndices = java.nio.IntBuffer
					.allocate(12 * 3 * (simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE));
			this.generateVertexIndices(vertexIndices, 0, 0, simplePtsArrays
					.get(i).capacity()
					/ 4 / PT_INFO_SIZE);
			this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					this.indexBuffers_.get(i));
			this.chart_.bufferDataiv(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					vertexIndices, WGLWidget.GLenum.STATIC_DRAW,
					WGLWidget.GLenum.UNSIGNED_SHORT);
			this.indexBufferSizes_.add(vertexIndices.capacity());
			this.overlayLinesBuffers_.add(this.chart_.createBuffer());
			java.nio.IntBuffer lineIndices = java.nio.IntBuffer
					.allocate(24 * (simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE));
			this.generateMeshIndices(lineIndices, 0, 0, simplePtsArrays.get(i)
					.capacity()
					/ 4 / PT_INFO_SIZE);
			this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					this.overlayLinesBuffers_.get(i));
			this.chart_.bufferDataiv(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					lineIndices, WGLWidget.GLenum.STATIC_DRAW,
					WGLWidget.GLenum.UNSIGNED_SHORT);
			this.lineBufferSizes_.add(lineIndices.capacity());
			java.nio.ByteBuffer texCoordArray = WebGLUtils
					.newByteBuffer(4 * (PTS_PER_BAR * 2 * (simplePtsArrays.get(
							i).capacity() / 4 / PT_INFO_SIZE)));
			this.generateTextureCoords(texCoordArray, simplePtsArrays.get(i),
					simplePtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE);
			this.colormapTexBuffers_.add(this.chart_.createBuffer());
			this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER,
					this.colormapTexBuffers_.get(i));
			this.chart_.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER,
					texCoordArray, WGLWidget.GLenum.STATIC_DRAW);
		}
		for (int i = 0; i < coloredPtsArrays.size(); i++) {
			this.indexBuffers2_.add(this.chart_.createBuffer());
			java.nio.IntBuffer vertexIndices = java.nio.IntBuffer
					.allocate(12 * 3 * (coloredPtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE));
			this.generateVertexIndices(vertexIndices, 0, 0, coloredPtsArrays
					.get(i).capacity()
					/ 4 / PT_INFO_SIZE);
			this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					this.indexBuffers2_.get(i));
			this.chart_.bufferDataiv(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					vertexIndices, WGLWidget.GLenum.STATIC_DRAW,
					WGLWidget.GLenum.UNSIGNED_SHORT);
			this.indexBufferSizes2_.add(vertexIndices.capacity());
			this.overlayLinesBuffers2_.add(this.chart_.createBuffer());
			java.nio.IntBuffer lineIndices = java.nio.IntBuffer
					.allocate(24 * (coloredPtsArrays.get(i).capacity() / 4 / PT_INFO_SIZE));
			this.generateMeshIndices(lineIndices, 0, 0, coloredPtsArrays.get(i)
					.capacity()
					/ 4 / PT_INFO_SIZE);
			this.chart_.bindBuffer(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					this.overlayLinesBuffers2_.get(i));
			this.chart_.bufferDataiv(WGLWidget.GLenum.ELEMENT_ARRAY_BUFFER,
					lineIndices, WGLWidget.GLenum.STATIC_DRAW,
					WGLWidget.GLenum.UNSIGNED_SHORT);
			this.lineBufferSizes2_.add(lineIndices.capacity());
		}
	}

	private void loadBinaryResource(final java.nio.ByteBuffer data,
			final List<WGLWidget.Buffer> buffers) {
		buffers.add(this.chart_.createBuffer());
		this.chart_.bindBuffer(WGLWidget.GLenum.ARRAY_BUFFER, buffers
				.get(buffers.size() - 1));
		this.chart_.bufferDatafv(WGLWidget.GLenum.ARRAY_BUFFER, data,
				WGLWidget.GLenum.STATIC_DRAW, true);
	}

	private void barSeriesVertexData(final java.nio.ByteBuffer verticesIN,
			final java.nio.ByteBuffer verticesOUT) {
		float x;
		float y;
		float z0;
		float z;
		for (int i = 0; i < verticesIN.capacity() / 4 / 4; i++) {
			int index = i * 4;
			x = verticesIN.getFloat(4 * (index));
			y = verticesIN.getFloat(4 * (index + 1));
			z0 = verticesIN.getFloat(4 * (index + 2));
			z = verticesIN.getFloat(4 * (index + 2))
					+ verticesIN.getFloat(4 * (index + 3));
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

	private void generateVertexIndices(final java.nio.IntBuffer indicesOUT,
			int Nx, int Ny, int size) {
		boolean forward = true;
		switch (this.seriesType_) {
		case PointSeries3D:
			break;
		case SurfaceSeries3D:
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
		case BarSeries3D:
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

	private final void generateVertexIndices(
			final java.nio.IntBuffer indicesOUT, int Nx, int Ny) {
		generateVertexIndices(indicesOUT, Nx, Ny, 0);
	}

	private void generateMeshIndices(final java.nio.IntBuffer indicesOUT,
			int Nx, int Ny, int size) {
		boolean forward = true;
		switch (this.seriesType_) {
		case PointSeries3D:
			break;
		case SurfaceSeries3D:
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
		case BarSeries3D:
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

	private final void generateMeshIndices(final java.nio.IntBuffer indicesOUT,
			int Nx, int Ny) {
		generateMeshIndices(indicesOUT, Nx, Ny, 0);
	}

	private void generateTextureCoords(final java.nio.ByteBuffer coordsOUT,
			final java.nio.ByteBuffer dataArray, int size) {
		switch (this.seriesType_) {
		case PointSeries3D:
		case SurfaceSeries3D:
			break;
		case BarSeries3D:
			if (this.colormap_ == null) {
				for (int i = 0; i < size; i++) {
					for (int k = 0; k < 16; k++) {
						coordsOUT.putFloat(0.0f);
					}
				}
			} else {
				float min = (float) this.chart_.toPlotCubeCoords(this.colormap_
						.getMinimum(), Axis.ZAxis_3D);
				float max = (float) this.chart_.toPlotCubeCoords(this.colormap_
						.getMaximum(), Axis.ZAxis_3D);
				for (int i = 0; i < size; i++) {
					float zNorm = (dataArray.getFloat(4 * (i * 4 + 3)) - min)
							/ (max - min);
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

	private List<Integer> vertexPosBufferSizes_;
	private List<Integer> vertexPosBuffer2Sizes_;
	private List<Integer> indexBufferSizes_;
	private List<Integer> lineBufferSizes_;
	private List<Integer> indexBufferSizes2_;
	private List<Integer> lineBufferSizes2_;
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
	private List<WMemoryResource> binaryResources_;
	private WGLWidget.Shader fragShader_;
	private WGLWidget.Shader colFragShader_;
	private WGLWidget.Shader meshFragShader_;
	private WGLWidget.Shader vertShader_;
	private WGLWidget.Shader colVertShader_;
	private WGLWidget.Shader meshVertShader_;
	private WGLWidget.Program seriesProgram_;
	private WGLWidget.Program colSeriesProgram_;
	private WGLWidget.Program meshProgram_;
	private WGLWidget.AttribLocation vertexPosAttr_;
	private WGLWidget.AttribLocation vertexPosAttr2_;
	private WGLWidget.AttribLocation meshVertexPosAttr_;
	private WGLWidget.AttribLocation vertexSizeAttr_;
	private WGLWidget.AttribLocation vertexSizeAttr2_;
	private WGLWidget.AttribLocation vertexColAttr2_;
	private WGLWidget.AttribLocation barTexCoordAttr_;
	private WGLWidget.UniformLocation mvMatrixUniform_;
	private WGLWidget.UniformLocation mvMatrixUniform2_;
	private WGLWidget.UniformLocation mesh_mvMatrixUniform_;
	private WGLWidget.UniformLocation pMatrix_;
	private WGLWidget.UniformLocation pMatrix2_;
	private WGLWidget.UniformLocation mesh_pMatrix_;
	private WGLWidget.UniformLocation cMatrix_;
	private WGLWidget.UniformLocation cMatrix2_;
	private WGLWidget.UniformLocation mesh_cMatrix_;
	private WGLWidget.UniformLocation TexSampler_;
	private WGLWidget.UniformLocation mesh_colorUniform_;
	private WGLWidget.UniformLocation offset_;
	private WGLWidget.UniformLocation scaleFactor_;
	private WGLWidget.Texture colormapTexture_;
	private static final String barFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\nvarying vec2 vTextureCoord;\nvarying vec3 vPos;\n\nuniform sampler2D uSampler;\n\nvoid main(void) {\n  if (vPos.x < 0.0 || vPos.x > 1.0 ||      vPos.y < 0.0 || vPos.y > 1.0 ||      vPos.z < 0.0 || vPos.z > 1.0) {\n    discard;\n  }\n  gl_FragColor = texture2D(uSampler, vec2(vTextureCoord.s, vTextureCoord.t) );\n}\n";
	private static final String barVertexShaderSrc = "attribute vec3 aVertexPosition;\nattribute vec2 aTextureCoord;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec2 vTextureCoord;\nvarying vec3 vPos;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vTextureCoord = aTextureCoord;\n  vPos = aVertexPosition;\n}\n";
	private static final String colBarFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\nvarying vec3 vPos;\nvarying vec4 vColor;\n\nvoid main(void) {\n  if (vPos.x < 0.0 || vPos.x > 1.0 ||      vPos.y < 0.0 || vPos.y > 1.0 ||      vPos.z < 0.0 || vPos.z > 1.0) {\n    discard;\n  }\n  gl_FragColor = vColor;\n}\n";
	private static final String colBarVertexShaderSrc = "attribute vec3 aVertexPosition;\nattribute vec4 aVertexColor;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec4 vColor;\nvarying vec3 vPos;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vColor = aVertexColor/255.0;\n  vPos = aVertexPosition;\n}\n";
	private static final String ptFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying float x, y, z;\n\nuniform sampler2D uSampler;\nuniform float uOffset;\nuniform float uScaleFactor;\n\nvoid main(void) {\n  if (x < 0.0 || x > 1.0 ||      y < 0.0 || y > 1.0 ||      z < 0.0 || z > 1.0) {\n    discard;\n  }\n  gl_FragColor = texture2D(uSampler, vec2(0.0, uScaleFactor * (z - uOffset) ) );\n}\n";
	private static final String ptVertexShaderSrc = "attribute vec3 aVertexPosition;\nattribute float aPointSize;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying float x, y, z;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  x = aVertexPosition.x;\n  y = aVertexPosition.y;\n  z = aVertexPosition.z;\n  gl_PointSize = aPointSize;\n}\n";
	private static final String colPtFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\nvarying vec4 vColor;\nvarying float x, y, z;\n\nvoid main(void) {\n  if (x < 0.0 || x > 1.0 ||      y < 0.0 || y > 1.0 ||      z < 0.0 || z > 1.0) {\n    discard;\n  }\n  gl_FragColor = vColor;\n}\n";
	private static final String colPtVertexShaderSrc = "attribute vec3 aVertexPosition;\nattribute float aPointSize;\nattribute vec4 aColor;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying vec4 vColor;\nvarying float x, y, z;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vColor = aColor/255.0;\n  x = aVertexPosition.x;\n  y = aVertexPosition.y;\n  z = aVertexPosition.z;\n  gl_PointSize = aPointSize;\n}\n";
	private static final String surfFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying float x, y, z;\n\nuniform sampler2D uSampler;\nuniform float uOffset;\nuniform float uScaleFactor;\n\nvoid main(void) {\n  if (x < 0.0 || x > 1.0 ||      y < 0.0 || y > 1.0 ||      z < 0.0 || z > 1.0) {\n    discard;\n  }\n  gl_FragColor = texture2D(uSampler, vec2(0.0, uScaleFactor * (z - uOffset) ) );\n}\n";
	private static final String surfVertexShaderSrc = "attribute vec3 aVertexPosition;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvarying float x, y, z;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  x = aVertexPosition.x;\n  y = aVertexPosition.y;\n  z = aVertexPosition.z;\n}\n";
	private static final String meshFragShaderSrc = "#ifdef GL_ES\nprecision highp float;\n#endif\n\nvarying vec3 vPos;\n\nuniform vec4 uColor;\n\nvoid main(void) {\n  if (vPos.x < 0.0 || vPos.x > 1.0 ||      vPos.y < 0.0 || vPos.y > 1.0 ||      vPos.z < 0.0 || vPos.z > 1.0) {\n    discard;\n  }\n  gl_FragColor = uColor/255.0;\n}\n";
	private static final String meshVertexShaderSrc = "attribute vec3 aVertexPosition;\n\nvarying vec3 vPos;\n\nuniform mat4 uMVMatrix;\nuniform mat4 uPMatrix;\nuniform mat4 uCMatrix;\n\nvoid main(void) {\n  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n  vPos = aVertexPosition;\n}\n";

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
}
