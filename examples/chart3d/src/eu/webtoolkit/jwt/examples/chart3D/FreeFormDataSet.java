/*
 * Copyright (C) 2014 Emweb bvba, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.chart3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;

import eu.webtoolkit.jwt.AbstractSignal.Connection;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WGLWidget.AttribLocation;
import eu.webtoolkit.jwt.WGLWidget.Buffer;
import eu.webtoolkit.jwt.WGLWidget.ClientSideRenderer;
import eu.webtoolkit.jwt.WGLWidget.GLenum;
import eu.webtoolkit.jwt.WGLWidget.Program;
import eu.webtoolkit.jwt.WGLWidget.Shader;
import eu.webtoolkit.jwt.WGLWidget.UniformLocation;
import eu.webtoolkit.jwt.chart.Axis;
import eu.webtoolkit.jwt.chart.ChartUpdates;
import eu.webtoolkit.jwt.chart.WAbstractDataSeries3D;

public class FreeFormDataSet extends WAbstractDataSeries3D {

	private static final String VERTEX_SHADER = ""
			+ "attribute vec3 aVertexPosition;\n"
			+ "uniform mat4 uMVMatrix;\n"
			+ "uniform mat4 uPMatrix;\n"
			+ "uniform mat4 uCMatrix;\n"
			+ "void main(void) {\n"
			+ "  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n"
			+ "}\n";
	private static final String FRAGMENT_SHADER = ""
			+ "#ifdef GL_ES\n"
			+ "precision highp float;\n"
			+ "#endif\n"
			+ "uniform vec4 uColor;\n"
			+ "void main(void) {\n"
			+ "  gl_FragColor = uColor;\n"
			+ "}\n";
	private Program program;
	private Shader vertexShader;
	private Shader fragShader;
	private Buffer vertexPosBuffer;
	private Buffer indexBuffer;
	private Buffer lineBuffer;
	private Buffer lineIndexBuffer;
	
	private AttribLocation vertexPosAttr;
	private UniformLocation mvMatrixUniform;
	private UniformLocation pMatrixUniform;
	private UniformLocation cMatrixUniform;
	private UniformLocation colorUniform;
	
	private Matrix4f mvMatrix;
	private WAbstractItemModel connectivityModel;
	private boolean drawLines = false;
	private WColor lineColor = new WColor();
	private WColor surfaceColor = new WColor();
	private List<Connection> connections = new ArrayList<Connection>();
	private DataUpdateListener updateListener = new DataUpdateListener();

	/**
	 * Create a new free form dataset using the given model.
	 * 
	 * Every three rows in the model should describe the vertices of a triangle.
	 * 
	 * Column 0 is the x coordinate, 1 is the y coordinate, and 2 is the z coordinate
	 * of the vertex.
	 */
	public FreeFormDataSet(WAbstractItemModel model) {
		super(model);
		mvMatrix = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
	}

	/**
	 * Create a new free form dataset using a model of vertices,
	 * and a connectivity model describing how these vertices are connected.
	 * 
	 * @param vertices
	 * 		  Every row in this model describes a vertex.
	 *		  Column 0 is the x coordinate, 1 is the y coordinate, and 2 is the z coordinate
	 *		  of the vertex.
	 * @param connectivityModel
	 * 		  The connectivity model describes how the triangles are formed.
	 * 		  Every three rows in column 0 make up a triangle by listing its index in vertex model.
	 */
	public FreeFormDataSet(WAbstractItemModel vertices,
			WAbstractItemModel connectivityModel) {
		super(vertices);
		this.connectivityModel = connectivityModel;
		mvMatrix = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		connections.add(connectivityModel.modelReset().addListener(this, updateListener));
		connections.add(connectivityModel.dataChanged().addListener(this, updateListener));
		connections.add(connectivityModel.rowsInserted().addListener(this, updateListener));
		connections.add(connectivityModel.columnsInserted().addListener(this, updateListener));
		connections.add(connectivityModel.rowsRemoved().addListener(this, updateListener));
		connections.add(connectivityModel.columnsRemoved().addListener(this, updateListener));
	}

	/**
	 * Get the connectivity model for this freeform dataset.
	 */
	public WAbstractItemModel getConnectivityModel() {
		return connectivityModel;
	}
	
	/**
	 * Set the connectivity model for this freeform dataset.
	 * 
	 * Note that if this model goes from null to an actual model, or from
	 * an actual model to null, then the semantics of how the model is generated
	 * is changed.
	 */
	public void setConnectivityModel(WAbstractItemModel connectivityModel) {
		if (connectivityModel != this.connectivityModel) {
			for (Connection connection : connections) {
				connection.disconnect();
			}
			connections.clear();
			this.connectivityModel = connectivityModel;
			if (connectivityModel != null) {
            	connections.add(connectivityModel.modelReset().addListener(this, updateListener));
            	connections.add(connectivityModel.dataChanged().addListener(this, updateListener));
            	connections.add(connectivityModel.rowsInserted().addListener(this, updateListener));
            	connections.add(connectivityModel.columnsInserted().addListener(this, updateListener));
            	connections.add(connectivityModel.rowsRemoved().addListener(this, updateListener));
            	connections.add(connectivityModel.columnsRemoved().addListener(this, updateListener));
			}
			chart_.updateChart(ChartUpdates.GLContext, ChartUpdates.GLTextures);
		}
	}
	
	private class DataUpdateListener implements Signal.Listener {
		@Override
		public void trigger() {
		    if (chart_ != null) {
		    	chart_.updateChart(ChartUpdates.GLContext, ChartUpdates.GLTextures);
		    }
		}
	}

	@Override
	public double minimum(Axis axis) {
		double minimum = Double.POSITIVE_INFINITY;
		for (int i = 0; i < model_.getRowCount(); i++) {
			int colNb = 0;
			if (axis == Axis.XAxis_3D) {
				colNb = 0;
			} else if (axis == Axis.YAxis_3D) {
				colNb = 1;
			} else if (axis == Axis.ZAxis_3D) {
				colNb = 2;
			}
			double data = getAsDouble(model_.getData(i, colNb));
			if (data < minimum) {
				minimum = data;
			}
		}
		return minimum;
	}

	@Override
	public double maximum(Axis axis) {
		double maximum = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < model_.getRowCount(); i++) {
			int colNb = 0;
			if (axis == Axis.XAxis_3D) {
				colNb = 0;
			} else if (axis == Axis.YAxis_3D) {
				colNb = 1;
			} else if (axis == Axis.ZAxis_3D) {
				colNb = 2;
			}
			double data = getAsDouble(model_.getData(i, colNb));
			if (data > maximum) {
				maximum = data;
			}
		}
		return maximum;
	}

	@Override
	public void initializeGL() {
	}

	@Override
	public void paintGL() {
		if (isHidden())
			return;

		chart_.disable(GLenum.CULL_FACE);
		chart_.enable(GLenum.DEPTH_TEST);

		if (!vertexPosBuffer.isNull()) {
			chart_.useProgram(program);
            chart_.enable(GLenum.POLYGON_OFFSET_FILL);
			chart_.polygonOffset(1, 0.001);
			chart_.uniformMatrix4(cMatrixUniform, chart_.getJsMatrix());
			chart_.bindBuffer(GLenum.ARRAY_BUFFER, vertexPosBuffer);
			chart_.vertexAttribPointer(vertexPosAttr, 3, GLenum.FLOAT, false,
					0, 0);
			chart_.enableVertexAttribArray(vertexPosAttr);
			chart_.uniform4f(colorUniform, surfaceColor.getRed() / 255.0f,
					surfaceColor.getGreen() / 255.0f,
					surfaceColor.getBlue() / 255.0f,
					surfaceColor.getAlpha() / 255.0f);
			if (connectivityModel == null) {
				chart_.drawArrays(GLenum.TRIANGLES, 0, model_.getRowCount());
			} else {
				chart_.bindBuffer(GLenum.ELEMENT_ARRAY_BUFFER, indexBuffer);
				chart_.drawElements(GLenum.TRIANGLES,
						connectivityModel.getRowCount(), GLenum.UNSIGNED_SHORT,
						0);
			}
			chart_.disable(GLenum.POLYGON_OFFSET_FILL);

			if (drawLines) {
				chart_.uniform4f(colorUniform, lineColor.getRed() / 255.0f,
						lineColor.getGreen() / 255.0f,
						lineColor.getBlue() / 255.0f,
						surfaceColor.getAlpha() / 255.0f);
				if (connectivityModel == null) {
					chart_.bindBuffer(GLenum.ARRAY_BUFFER, lineBuffer);
					chart_.vertexAttribPointer(vertexPosAttr, 3, GLenum.FLOAT,
							false, 0, 0);
					chart_.enableVertexAttribArray(vertexPosAttr);
					chart_.drawArrays(GLenum.LINES, 0, model_.getRowCount() * 2);
				} else {
					chart_.bindBuffer(GLenum.ELEMENT_ARRAY_BUFFER,
							lineIndexBuffer);
					chart_.drawElements(GLenum.LINES,
							connectivityModel.getRowCount() * 2,
							GLenum.UNSIGNED_SHORT, 0);
				}
			}
		}

		chart_.enable(GLenum.CULL_FACE);
		chart_.disable(GLenum.DEPTH_TEST);
	}


	@Override
	public void updateGL() {
		vertexShader = chart_.createShader(GLenum.VERTEX_SHADER);
		chart_.shaderSource(vertexShader, VERTEX_SHADER);
		chart_.compileShader(vertexShader);

		fragShader = chart_.createShader(GLenum.FRAGMENT_SHADER);
		chart_.shaderSource(fragShader, FRAGMENT_SHADER);
		chart_.compileShader(fragShader);

		program = chart_.createProgram();
		chart_.attachShader(program, vertexShader);
		chart_.attachShader(program, fragShader);
		chart_.linkProgram(program);

		vertexPosAttr = chart_.getAttribLocation(program, "aVertexPosition");
		mvMatrixUniform = chart_.getUniformLocation(program, "uMVMatrix");
		pMatrixUniform = chart_.getUniformLocation(program, "uPMatrix");
		cMatrixUniform = chart_.getUniformLocation(program, "uCMatrix");
		colorUniform = chart_.getUniformLocation(program, "uColor");

		ByteBuffer buf = generatePointBuffer();
		vertexPosBuffer = chart_.createBuffer();
		chart_.bindBuffer(GLenum.ARRAY_BUFFER, vertexPosBuffer);
		chart_.bufferDatafv(GLenum.ARRAY_BUFFER, buf, GLenum.STATIC_DRAW, true);

		if (connectivityModel != null) {
			IntBuffer indexBuf = IntBuffer.allocate(connectivityModel
					.getRowCount());

			for (int i = 0; i < connectivityModel.getRowCount(); i++) {
				indexBuf.put(getAsInt(connectivityModel.getData(i, 0)));
			}

			indexBuffer = chart_.createBuffer();
			chart_.bindBuffer(GLenum.ELEMENT_ARRAY_BUFFER, indexBuffer);
			chart_.bufferDataiv(GLenum.ELEMENT_ARRAY_BUFFER, indexBuf,
					GLenum.STATIC_DRAW, GLenum.UNSIGNED_SHORT);
		}

		if (drawLines) {
			if (connectivityModel == null) {
				ByteBuffer lineBuf = generateLineBuffer();
				lineBuffer = chart_.createBuffer();
				chart_.bindBuffer(GLenum.ARRAY_BUFFER, lineBuffer);
				chart_.bufferDatafv(GLenum.ARRAY_BUFFER, lineBuf,
						GLenum.STATIC_DRAW, true);
			} else {
				IntBuffer lineIndexBuf = IntBuffer.allocate(connectivityModel
						.getRowCount() * 2);

				for (int i = 0; i < connectivityModel.getRowCount(); i += 3) {
					int i0 = getAsInt(connectivityModel.getData(i, 0));
					int i1 = getAsInt(connectivityModel.getData(i + 1, 0));
					int i2 = getAsInt(connectivityModel.getData(i + 2, 0));
					lineIndexBuf.put(i0).put(i1).put(i1).put(i2).put(i2)
							.put(i0);
				}
					
				lineIndexBuffer = chart_.createBuffer();
				chart_.bindBuffer(GLenum.ELEMENT_ARRAY_BUFFER, lineIndexBuffer);
				chart_.bufferDataiv(GLenum.ELEMENT_ARRAY_BUFFER, lineIndexBuf,
						GLenum.STATIC_DRAW, GLenum.UNSIGNED_SHORT);
			}
		}

		chart_.useProgram(program);
		chart_.uniformMatrix4(mvMatrixUniform, mvMatrix);
		chart_.uniformMatrix4(pMatrixUniform, chart_.getPMatrix());
	}

	private int getAsInt(Object o) {
		if (o instanceof Integer) {
			return (Integer) o;
		} else if (o instanceof Long) {
			return (int) ((Long) o & 0xFFFFFFFF);
		} else if (o instanceof Short) {
			return (Short) o;
		} else if (o instanceof Byte) {
			return (Byte) o;
		}
		return 0;
	}
	
	private static double getAsDouble(Object o) {
		if (o instanceof Double) {
			return (Double) o;
		} else if (o instanceof Float) {
			return (Float) o;
		} else if (o instanceof Integer) {
			return (Integer) o;
		} else if (o instanceof Long) {
			return (Long) o;
		}
		return 0;
	}

	@Override
	public void resizeGL() {
		if (program != null && !program.isNull()) {
			chart_.useProgram(program);
			chart_.uniformMatrix4(pMatrixUniform, chart_.getPMatrix());
		}
		ByteBuffer buf = generatePointBuffer();
		chart_.bindBuffer(GLenum.ARRAY_BUFFER, vertexPosBuffer);
		chart_.bufferDatafv(GLenum.ARRAY_BUFFER, buf, GLenum.STATIC_DRAW, true);
		if (drawLines) {
			if (connectivityModel == null) {
				ByteBuffer lineBuf = generateLineBuffer();
				chart_.bindBuffer(GLenum.ARRAY_BUFFER, lineBuffer);
				chart_.bufferDatafv(GLenum.ARRAY_BUFFER, lineBuf,
						GLenum.STATIC_DRAW, true);
			}
		}
	}
	
	private ByteBuffer generatePointBuffer() {
		int N = model_.getRowCount();
		ByteBuffer buf = ByteBuffer.allocate(4 * 3 * N);
		buf.order(ByteOrder.nativeOrder());

		double xMin = chart_.axis(Axis.XAxis_3D).getMinimum();
		double xMax = chart_.axis(Axis.XAxis_3D).getMaximum();
		double yMin = chart_.axis(Axis.YAxis_3D).getMinimum();
		double yMax = chart_.axis(Axis.YAxis_3D).getMaximum();
		double zMin = chart_.axis(Axis.ZAxis_3D).getMinimum();
		double zMax = chart_.axis(Axis.ZAxis_3D).getMaximum();

		for (int i = 0; i < N; i++) {
			buf.putFloat((float) ((getAsDouble(model_.getData(i, 0)) - xMin) / (xMax - xMin)));
			buf.putFloat((float) ((getAsDouble(model_.getData(i, 1)) - yMin) / (yMax - yMin)));
			buf.putFloat((float) ((getAsDouble(model_.getData(i, 2)) - zMin) / (zMax - zMin)));
		}
		return buf;
	}
	
	private ByteBuffer generateLineBuffer() {
		int N = model_.getRowCount();

		double xMin = chart_.axis(Axis.XAxis_3D).getMinimum();
		double xMax = chart_.axis(Axis.XAxis_3D).getMaximum();
		double yMin = chart_.axis(Axis.YAxis_3D).getMinimum();
		double yMax = chart_.axis(Axis.YAxis_3D).getMaximum();
		double zMin = chart_.axis(Axis.ZAxis_3D).getMinimum();
		double zMax = chart_.axis(Axis.ZAxis_3D).getMaximum();

        ByteBuffer lineBuf = ByteBuffer.allocate(4 * 3 * 2 * N);
        lineBuf.order(ByteOrder.nativeOrder());
        for (int i = 0; i < N; i += 3) {
            float x0 = (float) ((getAsDouble(model_.getData(i, 0)) - xMin) / (xMax - xMin));
            float y0 = (float) ((getAsDouble(model_.getData(i, 1)) - yMin) / (yMax - yMin));
            float z0 = (float) ((getAsDouble(model_.getData(i, 2)) - zMin) / (zMax - zMin));
            float x1 = (float) ((getAsDouble(model_.getData(i + 1, 0)) - xMin) / (xMax - xMin));
            float y1 = (float) ((getAsDouble(model_.getData(i + 1, 1)) - yMin) / (yMax - yMin));
            float z1 = (float) ((getAsDouble(model_.getData(i + 1, 2)) - zMin) / (zMax - zMin));
            float x2 = (float) ((getAsDouble(model_.getData(i + 2, 0)) - xMin) / (xMax - xMin));
            float y2 = (float) ((getAsDouble(model_.getData(i + 2, 1)) - yMin) / (yMax - yMin));
            float z2 = (float) ((getAsDouble(model_.getData(i + 2, 2)) - zMin) / (zMax - zMin));
            lineBuf.putFloat(x0).putFloat(y0).putFloat(z0);
            lineBuf.putFloat(x1).putFloat(y1).putFloat(z1);
            lineBuf.putFloat(x1).putFloat(y1).putFloat(z1);
            lineBuf.putFloat(x2).putFloat(y2).putFloat(z2);
            lineBuf.putFloat(x2).putFloat(y2).putFloat(z2);
            lineBuf.putFloat(x0).putFloat(y0).putFloat(z0);
        }
        
        return lineBuf;
	}

	@Override
	public void deleteAllGLResources() {
		if (program != null && !program.isNull()) {
			chart_.detachShader(program, vertexShader);
			chart_.detachShader(program, fragShader);
			chart_.deleteShader(vertexShader);
			chart_.deleteShader(fragShader);
			chart_.deleteProgram(program);
			program.clear();
		}
		if (vertexPosBuffer != null && !vertexPosBuffer.isNull()) {
			chart_.deleteBuffer(vertexPosBuffer);
			vertexPosBuffer.clear();
		}
		if (indexBuffer != null && !indexBuffer.isNull()) {
			chart_.deleteBuffer(indexBuffer);
			indexBuffer.clear();
		}
		if (lineBuffer != null && !lineBuffer.isNull()) {
			chart_.deleteBuffer(lineBuffer);
			lineBuffer.clear();
		}
		if (lineIndexBuffer != null && !lineIndexBuffer.isNull()) {
			chart_.deleteBuffer(lineIndexBuffer);
			lineIndexBuffer.clear();
		}
	}

	/**
	 * Get whether to draw outlines of triangles.
	 */
	public boolean getDrawLines() {
		return drawLines;
	}

	/**
	 * Set whether to draw outlines of triangles.
	 */
	public void setDrawLines(boolean drawLines) {
		if (drawLines != this.drawLines) {
			this.drawLines = drawLines;
			if (chart_ != null) {
				chart_.updateChart(ChartUpdates.GLContext);
			}
		}
	}

	/**
	 * Gets the color of the outline of triangles.
	 */
	public WColor getLineColor() {
		return lineColor;
	}

	/**
	 * Sets the color of the outline of triangles.
	 */
	public void setLineColor(WColor lineColor) {
		if (!lineColor.equals(this.lineColor)) {
			this.lineColor = lineColor;
			if (chart_ != null) {
				chart_.repaintGL(ClientSideRenderer.PAINT_GL);
			}
		}
	}

	/**
	 * Gets the surface color of the freeform surface.
	 */
	public WColor getSurfaceColor() {
		return surfaceColor;
	}

	/**
	 * Gets the surface color of the freeform surface.
	 */
	public void setSurfaceColor(WColor surfaceColor) {
		if (!surfaceColor.equals(this.surfaceColor)) {
			this.surfaceColor = surfaceColor;
			if (chart_ != null) {
				chart_.repaintGL(ClientSideRenderer.PAINT_GL);
			}
		}
	}
}
