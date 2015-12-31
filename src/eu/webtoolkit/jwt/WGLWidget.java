/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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
 * GL support class.
 * <p>
 * 
 * The {@link WGLWidget} class is an interface to the HTML5 WebGL infrastructure
 * for client-side rendering, and OpenGL for server-side rendering. Its API is
 * based on the WebGL API. To fully understand WebGL, it is recommended to read
 * the WebGL standard in addition to this documentation.
 * <p>
 * The most recent version of the WebGL specification can be found here: <a
 * href= "http://www.khronos.org/registry/webgl/specs/latest/1.0/">http://www.
 * khronos .org/registry/webgl/specs/latest/1.0/</a>
 * <p>
 * The goal of the {@link WGLWidget} class is to provide a method to render 3D
 * structures in the browser, where rendering and rerendering is normally done
 * at the client side in JavaScript without interaction from the server, in
 * order to obtain a smooth user interaction. Unless the scene requires
 * server-side updates, there is no communication with the server.
 * <p>
 * The rendering interface resembles to OpenGL ES, the same standard as WebGL is
 * based on. This is a stripped down version of the normal OpenGL as we usually
 * find them on desktops. Many stateful OpenGL features are not present in
 * OpenGL ES: no modelview and camera transformation stacks, no default lighting
 * models, no support for other rendering methods than through VBOs, ...
 * Therefore much existing example code for OpenGL applications and shaders will
 * not work on WebGL without modifications. The &apos;learning webgl&apos; web
 * site at <a href="http://learningwebgl.com/">http://learningwebgl.com/</a> is
 * a good starting point to get familiar with WebGL.
 * <p>
 * To use a WGLWidget, you must derive from it and reimplement the painter
 * methods. Usually, you will always need to implement
 * {@link WGLWidget#initializeGL() initializeGL()} and
 * {@link WGLWidget#paintGL() paintGL()}. Optionally, you may choose to
 * implement {@link WGLWidget#resizeGL(int width, int height) resizeGL()} (if
 * your widget does not have a fixed size), and {@link WGLWidget#updateGL()
 * updateGL()}. If you need to modify the painting methods, a repaint is
 * triggered by calling the {@link WGLWidget#repaintGL(EnumSet which)
 * repaintGL()} method. The default behaviour for any of these four painting
 * functions is to do nothing.
 * <p>
 * The four painter methods ({@link WGLWidget#initializeGL() initializeGL()},
 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()},
 * {@link WGLWidget#paintGL() paintGL()} and {@link WGLWidget#updateGL()
 * updateGL()}) all record JavaScript which is sent to the browser. The
 * JavaScript code of {@link WGLWidget#paintGL() paintGL()} is cached
 * client-side, and may be executed many times, e.g. to repaint a scene from
 * different viewpoints. The JavaScript code of {@link WGLWidget#initializeGL()
 * initializeGL()}, {@link WGLWidget#resizeGL(int width, int height) resizeGL()}
 * and {@link WGLWidget#updateGL() updateGL()} are intended for OpenGL state
 * updates, and is therefore only executed once on the client and is then
 * discarded.
 * <p>
 * There are four painting methods that you may implement in a specialization of
 * this class. The purpose of these functions is to register what JavaScript
 * code has to be executed to render a scene. Through invocations of the WebGL
 * functions documented below, JWt records the JavaScript calls that have to be
 * invoked in the browser.
 * <ul>
 * <li>
 * <b>{@link WGLWidget#initializeGL() initializeGL()}</b>: this function is
 * executed after the GL context has been initialized. It is also executed when
 * the <code>webglcontextrestored</code> signal is fired. You can distinguish
 * between the first initialization and restoration of context using
 * {@link WGLWidget#isRestoringContext() isRestoringContext()}. This is the
 * ideal location to compose shader programs, send VBO&apos;s to the client,
 * extract uniform and attribute locations, ... Due to the presence of
 * VBO&apos;s, this function may generate a large amount of data to the client.</li>
 * <li>
 * <b>{@link WGLWidget#resizeGL(int width, int height) resizeGL()}</b>: this
 * function is executed whenever the canvas dimensions change. A change in
 * canvas size will require you to invoke the
 * {@link WGLWidget#viewport(int x, int y, int width, int height) viewport()}
 * function again, as well as recalculate the projection matrices (especially
 * when the aspect ratio has changed). The
 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()} function is
 * therefore the ideal location to set those properties. The
 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()} function is
 * invoked automatically on every resize, and after the first
 * {@link WGLWidget#initializeGL() initializeGL()} invocation. Additional
 * invocations may be triggered by calling repaint() with the RESIZE_GL flag.</li>
 * <li>
 * <b>{@link WGLWidget#paintGL() paintGL()}</b>: this is the main scene drawing
 * function. Through its execution, JWt records what has to be done to render a
 * scene, and it is executed every time that the scene is to be redrawn. You can
 * use the VBO&apos;s and shaders prepared in the
 * {@link WGLWidget#initializeGL() initializeGL()} phase. Usually, this function
 * sets uniforms and attributes, links attributes to VBO&apos;s, applies
 * textures, and draws primitives. You may also create local programs, buffers,
 * ... Remember that this function is executed a lot of times, so every
 * buffer/program created in this function should also be destroyed to avoid
 * memory leaks. This function is transmitted once to the client, and is
 * executed when the scene needs to be redrawn. Redraws may be triggered from
 * mouse events, timer triggers, events on e.g. a video element, or whatever
 * other event. The {@link WGLWidget#paintGL() paintGL()} function can be
 * updated through invoking {@link WGLWidget#repaintGL(EnumSet which)
 * repaintGL()} with the PAINT_GL flag.</li>
 * <li>
 * <b>{@link WGLWidget#updateGL() updateGL()}</b>: VBO&apos;s, programs,
 * uniforms, GL properties, or anything else set during intializeGL() are not
 * necessarily immutable. If you want to change, add, remove or reconfigure
 * those properties, the execution of an {@link WGLWidget#updateGL() updateGL()}
 * function can be triggered by invoking
 * {@link WGLWidget#repaintGL(EnumSet which) repaintGL()} with the UPDATE_GL
 * flag. This signals that {@link WGLWidget#updateGL() updateGL()} needs to be
 * evaluated - just once. It is possible that the {@link WGLWidget#paintGL()
 * paintGL()} function also requires updates as consequence of the changes in
 * the {@link WGLWidget#updateGL() updateGL()} function; in this case, you
 * should also set the PAINT_GL flag of
 * {@link WGLWidget#repaintGL(EnumSet which) repaintGL()}.</li>
 * </ul>
 * <p>
 * The GL functions are intended to be used exclusively from within the
 * invocation of the four callback functions mentioned above. In order to
 * manually trigger the execution of these function, use the
 * {@link WGLWidget#repaintGL(EnumSet which) repaintGL()}.
 * <p>
 * A {@link WGLWidget} must be given a size explicitly, or must be put inside a
 * layout manager that manages its width and height. The behaviour of a
 * {@link WGLWidget} that was not given a size is undefined.
 * <p>
 * <h3>Binary buffer transfers</h3>
 * <p>
 * In {@link WGLWidget#activeTexture(WGLWidget.GLenum texture) activeTexture()},
 * there is an additional boolean argument where you can indicate that you want
 * the data to be transferred to the client in binary form. A
 * {@link WMemoryResource} is created for each of these buffers. If you know all
 * previous resources are not required in the client anymore, you can free
 * memory with the method
 * {@link WGLWidget#activeTexture(WGLWidget.GLenum texture) activeTexture()}
 * (the memory is also managed, so this is not neccesary). If you want to manage
 * these resources entirely by yourself, the following method can be used.
 * <p>
 * Using {@link WGLWidget#createAndLoadArrayBuffer(String url)
 * createAndLoadArrayBuffer()}, you can load an array buffer in binary format
 * from an URL. This will cause the client to fetch the given URL, and make the
 * contents of the file available in an {@link ArrayBuffer}, which can then be
 * used by BufferData() to bind them to an OpenGL buffer. This is ideal to load
 * VBO buffers in a faster way, as it avoids converting floats to text strings
 * on the server and then back to floats on the client. You can combine this
 * with the use of {@link WResource} (e.g. {@link WMemoryResource}) to send an
 * std::vector of vertices to the client. Note that using {@link ArrayBuffer} is
 * not possible when you want a fall-back in the form of server-side rendering.
 * <p>
 * <h3>Client side matrices and vectors.</h3>
 * <p>
 * The {@link WGLWidget} provides the {@link JavaScriptMatrix4x4} class as a
 * mechanism to use client-side modifiable matrices in the render functions.
 * These matrices can be used identically to the &apos;constant&apos;, with the
 * advantage that there is no need to have a roundtrip to the server to redraw
 * the scene when they are changed. As such, they are ideal for mouse-based
 * camera manipulations, timer triggered animations, or object manipulations.
 * <p>
 * There&apos;s also support for client-side modifiable vectors, with
 * {@link JavaScriptVector}.
 */
public class WGLWidget extends WInteractWidget {
	private static Logger logger = LoggerFactory.getLogger(WGLWidget.class);

	/**
	 * Abstract base class for all GL objects.
	 */
	public static abstract class GlObject {
		private static Logger logger = LoggerFactory.getLogger(GlObject.class);

		public GlObject() {
			this.id_ = -1;
		}

		public GlObject(int id) {
			this.id_ = id;
		}

		public abstract String getJsRef();

		public int getId() {
			return this.id_;
		}

		public void clear() {
			this.id_ = -1;
		}

		public boolean isNull() {
			return this.id_ == -1;
		}

		private int id_;
	}

	/**
	 * Reference to a WebGLShader class.
	 */
	public static class Shader extends WGLWidget.GlObject {
		private static Logger logger = LoggerFactory.getLogger(Shader.class);

		public Shader() {
			super();
		}

		public Shader(int i) {
			super(i);
		}

		public String getJsRef() {
			if (this.isNull()) {
				throw new WException("Shader: is null");
			}
			return "ctx.WtShader" + String.valueOf(this.getId());
		}
	}

	/**
	 * Reference to a WebGLProgram class.
	 */
	public static class Program extends WGLWidget.GlObject {
		private static Logger logger = LoggerFactory.getLogger(Program.class);

		public Program() {
			super();
		}

		public Program(int i) {
			super(i);
		}

		public String getJsRef() {
			if (this.isNull()) {
				throw new WException("Program: is null");
			}
			return "ctx.WtProgram" + String.valueOf(this.getId());
		}
	}

	/**
	 * Reference to a shader attribute location.
	 */
	public static class AttribLocation extends WGLWidget.GlObject {
		private static Logger logger = LoggerFactory
				.getLogger(AttribLocation.class);

		public AttribLocation() {
			super();
		}

		public AttribLocation(int i) {
			super(i);
		}

		public String getJsRef() {
			if (this.isNull()) {
				throw new WException("AttribLocation: is null");
			}
			return "ctx.WtAttrib" + String.valueOf(this.getId());
		}
	}

	/**
	 * Reference to a WebGLBuffer class.
	 */
	public static class Buffer extends WGLWidget.GlObject {
		private static Logger logger = LoggerFactory.getLogger(Buffer.class);

		public Buffer() {
			super();
		}

		public Buffer(int i) {
			super(i);
		}

		public String getJsRef() {
			if (this.isNull()) {
				throw new WException("Buffer: is null");
			}
			return "ctx.WtBuffer" + String.valueOf(this.getId());
		}
	}

	/**
	 * Reference to a WebGLUniformLocation class.
	 */
	public static class UniformLocation extends WGLWidget.GlObject {
		private static Logger logger = LoggerFactory
				.getLogger(UniformLocation.class);

		public UniformLocation() {
			super();
		}

		public UniformLocation(int i) {
			super(i);
		}

		public String getJsRef() {
			if (this.isNull()) {
				throw new WException("UniformLocation: is null");
			}
			return "ctx.WtUniform" + String.valueOf(this.getId());
		}
	}

	/**
	 * Reference to a WebGLTexture class.
	 */
	public static class Texture extends WGLWidget.GlObject {
		private static Logger logger = LoggerFactory.getLogger(Texture.class);

		public Texture() {
			super();
			this.url_ = "";
		}

		public Texture(int i) {
			super(i);
			this.url_ = "";
		}

		public String getJsRef() {
			if (this.isNull()) {
				return "null";
			}
			return "ctx.WtTexture" + String.valueOf(this.getId());
		}

		public void setUrl(String url) {
			this.url_ = url;
		}

		public String getUrl() {
			return this.url_;
		}

		private String url_;
	}

	/**
	 * Reference to a WebGLFramebuffer class.
	 */
	public static class Framebuffer extends WGLWidget.GlObject {
		private static Logger logger = LoggerFactory
				.getLogger(Framebuffer.class);

		public Framebuffer() {
			super();
		}

		public Framebuffer(int i) {
			super(i);
		}

		public String getJsRef() {
			if (this.isNull()) {
				return "null";
			}
			return "ctx.WtFramebuffer" + String.valueOf(this.getId());
		}
	}

	/**
	 * Reference to a WebGLRenderbuffer class.
	 */
	public static class Renderbuffer extends WGLWidget.GlObject {
		private static Logger logger = LoggerFactory
				.getLogger(Renderbuffer.class);

		public Renderbuffer() {
			super();
		}

		public Renderbuffer(int i) {
			super(i);
		}

		public String getJsRef() {
			if (this.isNull()) {
				return "null";
			}
			return "ctx.WtRenderbuffer" + String.valueOf(this.getId());
		}
	}

	/**
	 * Reference to a javascript ArrayBuffer class.
	 */
	public static class ArrayBuffer extends WGLWidget.GlObject {
		private static Logger logger = LoggerFactory
				.getLogger(ArrayBuffer.class);

		public ArrayBuffer() {
			super();
		}

		public ArrayBuffer(int i) {
			super(i);
		}

		public String getJsRef() {
			if (this.isNull()) {
				throw new WException("ArrayBuffer: is null");
			}
			return "ctx.WtBufferResource" + String.valueOf(this.getId());
		}
	}

	/**
	 * A client-side JavaScript vector.
	 * <p>
	 * 
	 * Using a {@link JavaScriptVector}, GL parameters can be modified without
	 * communication with the server. The value of the
	 * {@link JavaScriptMatrix4x4} is updated server-side whenever an event is
	 * sent to the server.
	 * <p>
	 * The {@link JavaScriptVector} is represented in JavaScript as an array,
	 * either as a Float32Array or as a plain JavaScript array.
	 */
	public static class JavaScriptVector {
		private static Logger logger = LoggerFactory
				.getLogger(JavaScriptVector.class);

		/**
		 * Create a temporarily invalid {@link JavaScriptVector}.
		 * <p>
		 * Should be added to a WGLWidget with
		 * {@link WGLWidget#addJavaScriptVector(WGLWidget.JavaScriptVector vec)
		 * WGLWidget#addJavaScriptVector()}, and initialized with
		 * {@link WGLWidget#initJavaScriptVector(WGLWidget.JavaScriptVector vec)
		 * WGLWidget#initJavaScriptVector()}.
		 */
		public JavaScriptVector(int length) {
			this.id_ = -1;
			this.length_ = length;
			this.jsRef_ = "";
			this.context_ = null;
			this.initialized_ = false;
		}

		public int getId() {
			return this.id_;
		}

		/**
		 * Returns whether this JavaScriptVector has been initialized.
		 */
		public boolean isInitialized() {
			return this.initialized_;
		}

		/**
		 * Returns whether this JavaScriptVector has been assigned to a
		 * {@link WGLWidget}.
		 */
		public boolean hasContext() {
			return this.context_ != null;
		}

		/**
		 * Returns the length (number of items) of this {@link JavaScriptVector}
		 * .
		 */
		public int getLength() {
			return this.length_;
		}

		/**
		 * Returns the JavaScript reference to this {@link JavaScriptVector}.
		 * <p>
		 * In order to get a valid JavaScript reference, this vector should have
		 * been added to a {@link WGLWidget}.
		 */
		public String getJsRef() {
			if (!this.hasContext()) {
				throw new WException(
						"JavaScriptVector: does not belong to a WGLWidget yet");
			}
			return this.jsRef_;
		}

		/**
		 * Returns the current server-side value.
		 * <p>
		 * Client-side changes to the {@link JavaScriptVector} are automatically
		 * synchronized.
		 */
		public List<Float> getValue() {
			if (!this.hasContext()) {
				throw new WException(
						"JavaScriptVector: vector not assigned to a WGLWidget");
			}
			for (int i = 0; i < this.context_.jsVectorList_.size(); i++) {
				if (this.context_.jsVectorList_.get(i).id == this.id_) {
					return this.context_.jsVectorList_.get(i).serverSideCopy;
				}
			}
			List<Float> result = new ArrayList<Float>();
			for (int i = 0; i < this.getLength(); ++i) {
				result.add(0.0f);
			}
			return result;
		}

		// public WGLWidget.JavaScriptVector clone() ;
		private void assignToContext(int id, WGLWidget context) {
			this.id_ = id;
			this.jsRef_ = context.getGlObjJsRef() + ".jsValues["
					+ String.valueOf(this.id_) + "]";
			this.context_ = context;
		}

		void initialize() {
			this.initialized_ = true;
		}

		private int id_;
		private int length_;
		private String jsRef_;
		WGLWidget context_;
		private boolean initialized_;
	}

	/**
	 * A client-side JavaScript matrix.
	 * <p>
	 * 
	 * A JavaScriptMatrix has methods that make it possible to do client-side
	 * calculations on matrices.
	 * <p>
	 * Using a {@link JavaScriptMatrix4x4}, GL parameters can be modified
	 * without communication with the server. The value of the
	 * {@link JavaScriptMatrix4x4} is updated server-side whenever an event is
	 * sent to the server.
	 * <p>
	 * Important: only the {@link WGLWidget.JavaScriptMatrix4x4#getJsRef()
	 * getJsRef()} of the return value from a call to
	 * WGLWidget::createJavaScriptMatrix() is a variable name that can be used
	 * in custom JavaScript to modify a matrix from external scripts. The
	 * {@link WGLWidget.JavaScriptMatrix4x4#getJsRef() getJsRef()} of return
	 * values of operations refer to unnamed temporary objects - rvalues in
	 * C++-lingo.
	 * <p>
	 * The {@link JavaScriptMatrix4x4} is represented in JavaScript as an array
	 * of 16 elements. This array represents the values of the matrix in
	 * column-major order. It is either a Float32Array or a plain JavaScript
	 * array.
	 */
	public static class JavaScriptMatrix4x4 {
		private static Logger logger = LoggerFactory
				.getLogger(JavaScriptMatrix4x4.class);

		/**
		 * Creates a temporarily invalid {@link JavaScriptMatrix4x4}.
		 * <p>
		 * Should be added to a WGLWidget with
		 * {@link WGLWidget#addJavaScriptMatrix4(WGLWidget.JavaScriptMatrix4x4 mat)
		 * WGLWidget#addJavaScriptMatrix4()}, and initialized with
		 * {@link WGLWidget#initJavaScriptMatrix4(WGLWidget.JavaScriptMatrix4x4 mat)
		 * WGLWidget#initJavaScriptMatrix4()}.
		 */
		public JavaScriptMatrix4x4() {
			this.id_ = -1;
			this.jsRef_ = "";
			this.context_ = null;
			this.operations_ = new ArrayList<WGLWidget.JavaScriptMatrix4x4.op>();
			this.matrices_ = new ArrayList<javax.vecmath.Matrix4f>();
			this.initialized_ = false;
		}

		public int getId() {
			return this.id_;
		}

		/**
		 * Returns whether this {@link JavaScriptMatrix4x4} has been
		 * initialized.
		 */
		public boolean isInitialized() {
			return this.initialized_;
		}

		/**
		 * Retuns whether this {@link JavaScriptMatrix4x4} has been assigned to
		 * a {@link WGLWidget}.
		 */
		public boolean hasContext() {
			return this.context_ != null;
		}

		/**
		 * Returns the JavaScript reference to this {@link JavaScriptMatrix4x4}.
		 * <p>
		 * In order to get a valid JavaScript reference, this matrix should have
		 * been added to a {@link WGLWidget}.
		 */
		public String getJsRef() {
			if (!this.hasContext()) {
				throw new WException(
						"JavaScriptMatrix4x4: does not belong to a WGLWidget yet");
			}
			return this.jsRef_;
		}

		/**
		 * Returns the current server-side value.
		 * <p>
		 * Client-side changes to the {@link JavaScriptMatrix4x4} are
		 * automatically synchronized.
		 */
		public javax.vecmath.Matrix4f getValue() {
			if (!this.hasContext()) {
				throw new WException(
						"JavaScriptMatrix4x4: matrix not assigned to a WGLWidget");
			}
			javax.vecmath.Matrix4f originalCpy = new javax.vecmath.Matrix4f(
					1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
					1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
			for (int i = 0; i < this.context_.jsMatrixList_.size(); i++) {
				if (this.context_.jsMatrixList_.get(i).id == this.id_) {
					originalCpy = this.context_.jsMatrixList_.get(i).serverSideCopy;
				}
			}
			int nbMult = 0;
			for (int i = 0; i < this.operations_.size(); i++) {
				switch (this.operations_.get(i)) {
				case TRANSPOSE:
					originalCpy = WebGLUtils.transpose(originalCpy);
					break;
				case INVERT:
					originalCpy.invert();
					break;
				case MULTIPLY:
					originalCpy.mul(this.matrices_.get(nbMult));
					nbMult++;
					break;
				}
			}
			return originalCpy;
		}

		public WGLWidget.JavaScriptMatrix4x4 inverted() {
			if (!this.isInitialized()) {
				throw new WException(
						"JavaScriptMatrix4x4: matrix not initialized");
			}
			WGLWidget.JavaScriptMatrix4x4 copy = this.clone();
			copy.jsRef_ = "Wt3_3_5.glMatrix.mat4.inverse(" + this.jsRef_
					+ ", Wt3_3_5.glMatrix.mat4.create())";
			copy.operations_.add(WGLWidget.JavaScriptMatrix4x4.op.INVERT);
			return copy;
		}

		public WGLWidget.JavaScriptMatrix4x4 transposed() {
			if (!this.isInitialized()) {
				throw new WException(
						"JavaScriptMatrix4x4: matrix not initialized");
			}
			WGLWidget.JavaScriptMatrix4x4 copy = this.clone();
			copy.jsRef_ = "Wt3_3_5.glMatrix.mat4.transpose(" + this.jsRef_
					+ ", Wt3_3_5.glMatrix.mat4.create())";
			copy.operations_.add(WGLWidget.JavaScriptMatrix4x4.op.TRANSPOSE);
			return copy;
		}

		public WGLWidget.JavaScriptMatrix4x4 multiply(
				final javax.vecmath.Matrix4f m) {
			if (!this.isInitialized()) {
				throw new WException(
						"JavaScriptMatrix4x4: matrix not initialized");
			}
			WGLWidget.JavaScriptMatrix4x4 copy = this.clone();
			StringWriter ss = new StringWriter();
			ss.append("Wt3_3_5.glMatrix.mat4.multiply(").append(this.jsRef_)
					.append(",");
			javax.vecmath.Matrix4f t = WebGLUtils.transpose(m);
			WebGLUtils.renderfv(ss, t, this.context_.pImpl_.getArrayType());
			ss.append(", Wt3_3_5.glMatrix.mat4.create())");
			copy.jsRef_ = ss.toString();
			copy.operations_.add(WGLWidget.JavaScriptMatrix4x4.op.MULTIPLY);
			copy.matrices_.add(m);
			return copy;
		}

		public WGLWidget.JavaScriptMatrix4x4 clone() {
			WGLWidget.JavaScriptMatrix4x4 copy = new WGLWidget.JavaScriptMatrix4x4();
			copy.id_ = this.id_;
			copy.jsRef_ = this.jsRef_;
			copy.context_ = this.context_;
			copy.initialized_ = this.initialized_;
			Utils.copyList(this.operations_, copy.operations_);
			Utils.copyList(this.matrices_, copy.matrices_);
			return copy;
		}

		private void assignToContext(int id, WGLWidget context) {
			this.id_ = id;
			this.jsRef_ = context.getGlObjJsRef() + ".jsValues["
					+ String.valueOf(this.id_) + "]";
			this.context_ = context;
		}

		void initialize() {
			this.initialized_ = true;
		}

		private boolean hasOperations() {
			return this.operations_.size() > 0;
		}

		private int id_;
		private String jsRef_;
		WGLWidget context_;

		enum op {
			TRANSPOSE, INVERT, MULTIPLY;

			/**
			 * Returns the numerical representation of this enum.
			 */
			public int getValue() {
				return ordinal();
			}
		}

		List<WGLWidget.JavaScriptMatrix4x4.op> operations_;
		List<javax.vecmath.Matrix4f> matrices_;
		private boolean initialized_;
	}

	/**
	 * Enumeration for render options.
	 * <p>
	 * 
	 * @see WGLWidget#setRenderOptions(EnumSet options)
	 */
	public enum RenderOption {
		/**
		 * Enables client-side rendering.
		 */
		ClientSideRendering,
		/**
		 * Enables server-side rendering.
		 */
		ServerSideRendering,
		/**
		 * Enables anti-aliasing.
		 */
		AntiAliasing;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Construct a GL widget.
	 * <p>
	 * Before the first rendering, you must apply a size to the
	 * {@link WGLWidget}.
	 */
	public WGLWidget(WContainerWidget parent) {
		super(parent);
		this.renderOptions_ = EnumSet.of(
				WGLWidget.RenderOption.ClientSideRendering,
				WGLWidget.RenderOption.ServerSideRendering,
				WGLWidget.RenderOption.AntiAliasing);
		this.pImpl_ = null;
		this.jsMatrixList_ = new ArrayList<WGLWidget.jsMatrixMap>();
		this.jsVectorList_ = new ArrayList<WGLWidget.jsVectorMap>();
		this.jsValues_ = 0;
		this.js_ = new StringWriter();
		this.repaintSignal_ = new JSignal(this, "repaintSignal");
		this.alternative_ = null;
		this.webglNotAvailable_ = new JSignal(this, "webglNotAvailable");
		this.webGlNotAvailable_ = false;
		this.contextRestored_ = new JSignal(this, "contextRestored");
		this.restoringContext_ = false;
		this.valueChanged_ = false;
		this.mouseWentDownSlot_ = new JSlot("function(o, e){"
				+ this.getGlObjJsRef() + ".mouseDown(o, e);}", this);
		this.mouseWentUpSlot_ = new JSlot("function(o, e){"
				+ this.getGlObjJsRef() + ".mouseUp(o, e);}", this);
		this.mouseDraggedSlot_ = new JSlot("function(o, e){"
				+ this.getGlObjJsRef() + ".mouseDrag(o, e);}", this);
		this.mouseMovedSlot_ = new JSlot("function(o, e){"
				+ this.getGlObjJsRef() + ".mouseMove(o, e);}", this);
		this.mouseWheelSlot_ = new JSlot("function(o, e){"
				+ this.getGlObjJsRef() + ".mouseWheel(o, e);}", this);
		this.touchStarted_ = new JSlot("function(o, e){" + this.getGlObjJsRef()
				+ ".touchStart(o, e);}", this);
		this.touchEnded_ = new JSlot("function(o, e){" + this.getGlObjJsRef()
				+ ".touchEnd(o, e);}", this);
		this.touchMoved_ = new JSlot("function(o, e){" + this.getGlObjJsRef()
				+ ".touchMoved(o, e);}", this);
		this.repaintSlot_ = new JSlot("function() {var o = "
				+ this.getGlObjJsRef() + ";if(o.ctx) o.paintGL();}", this);
		this.setInline(false);
		this.setLayoutSizeAware(true);
		this.webglNotAvailable_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WGLWidget.this.webglNotAvailable();
			}
		});
		this.repaintSignal_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WGLWidget.this.repaintGL(WGLWidget.ClientSideRenderer.PAINT_GL);
			}
		});
		this.contextRestored_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WGLWidget.this.contextRestored();
			}
		});
		this.mouseWentDown().addListener(this.mouseWentDownSlot_);
		this.mouseWentUp().addListener(this.mouseWentUpSlot_);
		this.mouseDragged().addListener(this.mouseDraggedSlot_);
		this.mouseMoved().addListener(this.mouseMovedSlot_);
		this.mouseWheel().addListener(this.mouseWheelSlot_);
		this.touchStarted().addListener(this.touchStarted_);
		this.touchEnded().addListener(this.touchEnded_);
		this.touchMoved().addListener(this.touchMoved_);
		this.setAlternativeContent(new WText(
				"Your browser does not support WebGL"));
		this.setFormObject(true);
	}

	/**
	 * Construct a GL widget.
	 * <p>
	 * Calls {@link #WGLWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WGLWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		;
		super.remove();
	}

	/**
	 * Sets the rendering option.
	 * <p>
	 * Use this method to configure whether client-side and/or server-side
	 * rendering can be used, and whether anti-aliasing should be enabled. The
	 * actual choice is also based on availability (respectively client-side or
	 * server-side).
	 * <p>
	 * The default value is to try both ClientSide or ServerSide rendering, and
	 * to enable anti-aliasing if available.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Options must be set before the widget is being rendered.
	 * </i>
	 * </p>
	 */
	public void setRenderOptions(EnumSet<WGLWidget.RenderOption> options) {
		this.renderOptions_ = EnumSet.copyOf(options);
	}

	/**
	 * Sets the rendering option.
	 * <p>
	 * Calls {@link #setRenderOptions(EnumSet options)
	 * setRenderOptions(EnumSet.of(option, options))}
	 */
	public final void setRenderOptions(WGLWidget.RenderOption option,
			WGLWidget.RenderOption... options) {
		setRenderOptions(EnumSet.of(option, options));
	}

	/**
	 * Initialize the GL state when the widget is first shown.
	 * <p>
	 * {@link WGLWidget#initializeGL() initializeGL()} is called when the widget
	 * is first rendered, and when the webglcontextrestored signal is fired. You
	 * can distinguish between the first initialization and context restoration
	 * using {@link WGLWidget#isRestoringContext() isRestoringContext()}. It
	 * usually creates most of the GL related state: shaders, VBOs, uniform
	 * locations, ...
	 * <p>
	 * If this state is to be updated during the lifetime of the widget, you
	 * should specialize the {@link WGLWidget#updateGL() updateGL()} to
	 * accomodate for this.
	 */
	protected void initializeGL() {
	}

	/**
	 * Act on resize events.
	 * <p>
	 * Usually, this method only contains functions to set the viewport and the
	 * projection matrix (as this is aspect ration dependent).
	 * <p>
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()} is rendered
	 * after initializeGL, and whenever widget is resized. After this method
	 * finishes, the widget is repainted with the cached client-side paint
	 * function.
	 */
	protected void resizeGL(int width, int height) {
	}

	/**
	 * Update the client-side painting function.
	 * <p>
	 * This method is invoked client-side when a repaint is required, i.e. when
	 * the {@link WGLWidget#getRepaintSlot() getRepaintSlot()} (a
	 * JavaScript-side {@link JSlot}) is triggered. Typical examples are: after
	 * mouse-based camera movements, after a timed update of a camera or an
	 * object&apos;s position, after a resize event (
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()} will also be
	 * called then), after an animation event, ... In many cases, this function
	 * will be executed client-side many many times.
	 * <p>
	 * Using the GL functions from this class, you construct a scene. The
	 * implementation tracks all JavaScript calls that need to be performed to
	 * draw the scenes, and will replay them verbatim on every trigger of the
	 * {@link WGLWidget#getRepaintSlot() getRepaintSlot()}. There are a few
	 * mechanisms that may be employed to change what is rendered without
	 * updating the {@link WGLWidget#paintGL() paintGL()} cache:
	 * <ul>
	 * <li>
	 * Client-side matrices may be used to change camera viewpoints, manipilate
	 * separate object&apos;s model transformation matrices, ...</li>
	 * <li>
	 * {@link Shader} sources can be updated without requiring the paint
	 * function to be renewed</li>
	 * </ul>
	 * <p>
	 * Updating the {@link WGLWidget#paintGL() paintGL()} cache is usually not
	 * too expensive; the VBOs, which are large in many cases, are already at
	 * the client side, while the {@link WGLWidget#paintGL() paintGL()} code
	 * only draws the VBOs. Of course, if you have to draw many separate
	 * objects, the {@link WGLWidget#paintGL() paintGL()} JS code may become
	 * large and updating is more expensive.
	 * <p>
	 * In order to update the {@link WGLWidget#paintGL() paintGL()} cache, call
	 * {@link WGLWidget#repaintGL(EnumSet which) repaintGL()} with the PAINT_GL
	 * parameter, which will cause the invocation of this method.
	 */
	protected void paintGL() {
	}

	/**
	 * Update state set in {@link WGLWidget#initializeGL() initializeGL()}.
	 * <p>
	 * Invoked when repaint is called with the UPDATE_GL call.
	 * <p>
	 * This is intended to be executed when you want to change programs,
	 * &apos;constant&apos; uniforms, or even VBO&apos;s, ... without resending
	 * already initialized data. It is a mechanism to make changes to what
	 * you&apos;ve set in intializeGL(). For every server-side invocation of
	 * this method, the result will be rendered client-side exactly once.
	 */
	protected void updateGL() {
	}

	/**
	 * Specifies what GL function needs to be updated.
	 */
	public enum ClientSideRenderer {
		/**
		 * refresh {@link WGLWidget#paintGL() paintGL()}
		 */
		PAINT_GL,
		/**
		 * refresh {@link WGLWidget#resizeGL(int width, int height) resizeGL()}
		 */
		RESIZE_GL,
		/**
		 * refresh {@link WGLWidget#updateGL() updateGL()}
		 */
		UPDATE_GL;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Request invocation of resizeGL, paintGL and/or updateGL.
	 * <p>
	 * If invoked with PAINT_GL, the client-side cached paint function is
	 * updated. If invoked with RESIZE_GL or UPDATE_GL, the code will be
	 * executed once.
	 * <p>
	 * If invoked with multiple flags set, the order of execution will be
	 * {@link WGLWidget#updateGL() updateGL()},
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()},
	 * {@link WGLWidget#paintGL() paintGL()}.
	 */
	public void repaintGL(EnumSet<WGLWidget.ClientSideRenderer> which) {
		if (!(this.pImpl_ != null)) {
			return;
		}
		this.pImpl_.repaintGL(which);
		if (!which.equals(0)) {
			this.repaint();
		}
	}

	/**
	 * Request invocation of resizeGL, paintGL and/or updateGL.
	 * <p>
	 * Calls {@link #repaintGL(EnumSet which) repaintGL(EnumSet.of(whic,
	 * which))}
	 */
	public final void repaintGL(WGLWidget.ClientSideRenderer whic,
			WGLWidget.ClientSideRenderer... which) {
		repaintGL(EnumSet.of(whic, which));
	}

	/**
	 * Returns whether a lost context is in the process of being restored.
	 * <p>
	 * You can check for this in {@link WGLWidget#initializeGL() initializeGL()}
	 * , to handle the first initialization and restoration of context
	 * differently.
	 */
	public boolean isRestoringContext() {
		return this.restoringContext_;
	}

	public void resize(final WLength width, final WLength height) {
		super.resize(width, height);
		if (this.pImpl_ != null) {
			this.layoutSizeChanged((int) width.getValue(),
					(int) height.getValue());
		}
	}

	/**
	 * The enormous GLenum.
	 * <p>
	 * This enum contains all numeric constants defined by the WebGL standard,
	 * see: <a href=
	 * "http://www.khronos.org/registry/webgl/specs/latest/1.0/#WEBGLRENDERINGCONTEXT"
	 * >http://www.khronos.org/registry/webgl/specs/latest/1.0/#
	 * WEBGLRENDERINGCONTEXT</a>
	 */
	public enum GLenum {
		DEPTH_BUFFER_BIT, STENCIL_BUFFER_BIT, COLOR_BUFFER_BIT, POINTS, LINES, LINE_LOOP, LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN, ZERO, ONE, SRC_COLOR, ONE_MINUS_SRC_COLOR, SRC_ALPHA, ONE_MINUS_SRC_ALPHA, DST_ALPHA, ONE_MINUS_DST_ALPHA, DST_COLOR, ONE_MINUS_DST_COLOR, SRC_ALPHA_SATURATE, FUNC_ADD, BLEND_EQUATION, BLEND_EQUATION_RGB, BLEND_EQUATION_ALPHA, FUNC_SUBTRACT, FUNC_REVERSE_SUBTRACT, BLEND_DST_RGB, BLEND_SRC_RGB, BLEND_DST_ALPHA, BLEND_SRC_ALPHA, CONSTANT_COLOR, ONE_MINUS_CONSTANT_COLOR, CONSTANT_ALPHA, ONE_MINUS_CONSTANT_ALPHA, BLEND_COLOR, ARRAY_BUFFER, ELEMENT_ARRAY_BUFFER, ARRAY_BUFFER_BINDING, ELEMENT_ARRAY_BUFFER_BINDING, STREAM_DRAW, STATIC_DRAW, DYNAMIC_DRAW, BUFFER_SIZE, BUFFER_USAGE, CURRENT_VERTEX_ATTRIB, FRONT, BACK, FRONT_AND_BACK, CULL_FACE, BLEND, DITHER, STENCIL_TEST, DEPTH_TEST, SCISSOR_TEST, POLYGON_OFFSET_FILL, SAMPLE_ALPHA_TO_COVERAGE, SAMPLE_COVERAGE, NO_ERROR, INVALID_ENUM, INVALID_VALUE, INVALID_OPERATION, OUT_OF_MEMORY, CW, CCW, LINE_WIDTH, ALIASED_POINT_SIZE_RANGE, ALIASED_LINE_WIDTH_RANGE, CULL_FACE_MODE, FRONT_FACE, DEPTH_RANGE, DEPTH_WRITEMASK, DEPTH_CLEAR_VALUE, DEPTH_FUNC, STENCIL_CLEAR_VALUE, STENCIL_FUNC, STENCIL_FAIL, STENCIL_PASS_DEPTH_FAIL, STENCIL_PASS_DEPTH_PASS, STENCIL_REF, STENCIL_VALUE_MASK, STENCIL_WRITEMASK, STENCIL_BACK_FUNC, STENCIL_BACK_FAIL, STENCIL_BACK_PASS_DEPTH_FAIL, STENCIL_BACK_PASS_DEPTH_PASS, STENCIL_BACK_REF, STENCIL_BACK_VALUE_MASK, STENCIL_BACK_WRITEMASK, VIEWPORT, SCISSOR_BOX, COLOR_CLEAR_VALUE, COLOR_WRITEMASK, UNPACK_ALIGNMENT, PACK_ALIGNMENT, MAX_TEXTURE_SIZE, MAX_VIEWPORT_DIMS, SUBPIXEL_BITS, RED_BITS, GREEN_BITS, BLUE_BITS, ALPHA_BITS, DEPTH_BITS, STENCIL_BITS, POLYGON_OFFSET_UNITS, POLYGON_OFFSET_FACTOR, TEXTURE_BINDING_2D, SAMPLE_BUFFERS, SAMPLES, SAMPLE_COVERAGE_VALUE, SAMPLE_COVERAGE_INVERT, NUM_COMPRESSED_TEXTURE_FORMATS, COMPRESSED_TEXTURE_FORMATS, DONT_CARE, FASTEST, NICEST, GENERATE_MIPMAP_HINT, BYTE, UNSIGNED_BYTE, SHORT, UNSIGNED_SHORT, INT, UNSIGNED_INT, FLOAT, DEPTH_COMPONENT, ALPHA, RGB, RGBA, LUMINANCE, LUMINANCE_ALPHA, UNSIGNED_SHORT_4_4_4_4, UNSIGNED_SHORT_5_5_5_1, UNSIGNED_SHORT_5_6_5, FRAGMENT_SHADER, VERTEX_SHADER, MAX_VERTEX_ATTRIBS, MAX_VERTEX_UNIFORM_VECTORS, MAX_VARYING_VECTORS, MAX_COMBINED_TEXTURE_IMAGE_UNITS, MAX_VERTEX_TEXTURE_IMAGE_UNITS, MAX_TEXTURE_IMAGE_UNITS, MAX_FRAGMENT_UNIFORM_VECTORS, SHADER_TYPE, DELETE_STATUS, LINK_STATUS, VALIDATE_STATUS, ATTACHED_SHADERS, ACTIVE_UNIFORMS, ACTIVE_UNIFORM_MAX_LENGTH, ACTIVE_ATTRIBUTES, ACTIVE_ATTRIBUTE_MAX_LENGTH, SHADING_LANGUAGE_VERSION, CURRENT_PROGRAM, NEVER, LESS, EQUAL, LEQUAL, GREATER, NOTEQUAL, GEQUAL, ALWAYS, KEEP, REPLACE, INCR, DECR, INVERT, INCR_WRAP, DECR_WRAP, VENDOR, RENDERER, VERSION, NEAREST, LINEAR, NEAREST_MIPMAP_NEAREST, LINEAR_MIPMAP_NEAREST, NEAREST_MIPMAP_LINEAR, LINEAR_MIPMAP_LINEAR, TEXTURE_MAG_FILTER, TEXTURE_MIN_FILTER, TEXTURE_WRAP_S, TEXTURE_WRAP_T, TEXTURE_2D, TEXTURE, TEXTURE_CUBE_MAP, TEXTURE_BINDING_CUBE_MAP, TEXTURE_CUBE_MAP_POSITIVE_X, TEXTURE_CUBE_MAP_NEGATIVE_X, TEXTURE_CUBE_MAP_POSITIVE_Y, TEXTURE_CUBE_MAP_NEGATIVE_Y, TEXTURE_CUBE_MAP_POSITIVE_Z, TEXTURE_CUBE_MAP_NEGATIVE_Z, MAX_CUBE_MAP_TEXTURE_SIZE, TEXTURE0, TEXTURE1, TEXTURE2, TEXTURE3, TEXTURE4, TEXTURE5, TEXTURE6, TEXTURE7, TEXTURE8, TEXTURE9, TEXTURE10, TEXTURE11, TEXTURE12, TEXTURE13, TEXTURE14, TEXTURE15, TEXTURE16, TEXTURE17, TEXTURE18, TEXTURE19, TEXTURE20, TEXTURE21, TEXTURE22, TEXTURE23, TEXTURE24, TEXTURE25, TEXTURE26, TEXTURE27, TEXTURE28, TEXTURE29, TEXTURE30, TEXTURE31, ACTIVE_TEXTURE, REPEAT, CLAMP_TO_EDGE, MIRRORED_REPEAT, FLOAT_VEC2, FLOAT_VEC3, FLOAT_VEC4, INT_VEC2, INT_VEC3, INT_VEC4, BOOL, BOOL_VEC2, BOOL_VEC3, BOOL_VEC4, FLOAT_MAT2, FLOAT_MAT3, FLOAT_MAT4, SAMPLER_2D, SAMPLER_CUBE, VERTEX_ATTRIB_ARRAY_ENABLED, VERTEX_ATTRIB_ARRAY_SIZE, VERTEX_ATTRIB_ARRAY_STRIDE, VERTEX_ATTRIB_ARRAY_TYPE, VERTEX_ATTRIB_ARRAY_NORMALIZED, VERTEX_ATTRIB_ARRAY_POINTER, VERTEX_ATTRIB_ARRAY_BUFFER_BINDING, COMPILE_STATUS, INFO_LOG_LENGTH, SHADER_SOURCE_LENGTH, LOW_FLOAT, MEDIUM_FLOAT, HIGH_FLOAT, LOW_INT, MEDIUM_INT, HIGH_INT, FRAMEBUFFER, RENDERBUFFER, RGBA4, RGB5_A1, RGB565, DEPTH_COMPONENT16, STENCIL_INDEX, STENCIL_INDEX8, DEPTH_STENCIL, RENDERBUFFER_WIDTH, RENDERBUFFER_HEIGHT, RENDERBUFFER_INTERNAL_FORMAT, RENDERBUFFER_RED_SIZE, RENDERBUFFER_GREEN_SIZE, RENDERBUFFER_BLUE_SIZE, RENDERBUFFER_ALPHA_SIZE, RENDERBUFFER_DEPTH_SIZE, RENDERBUFFER_STENCIL_SIZE, FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE, FRAMEBUFFER_ATTACHMENT_OBJECT_NAME, FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL, FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE, COLOR_ATTACHMENT0, DEPTH_ATTACHMENT, STENCIL_ATTACHMENT, DEPTH_STENCIL_ATTACHMENT, NONE, FRAMEBUFFER_COMPLETE, FRAMEBUFFER_INCOMPLETE_ATTACHMENT, FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT, FRAMEBUFFER_INCOMPLETE_DIMENSIONS, FRAMEBUFFER_UNSUPPORTED, FRAMEBUFFER_BINDING, RENDERBUFFER_BINDING, MAX_RENDERBUFFER_SIZE, INVALID_FRAMEBUFFER_OPERATION, UNPACK_FLIP_Y_WEBGL, UNPACK_PREMULTIPLY_ALPHA_WEBGL, CONTEXT_LOST_WEBGL, UNPACK_COLORSPACE_CONVERSION_WEBGL, BROWSER_DEFAULT_WEBGL;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	public void debugger() {
		this.pImpl_.debugger();
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void activeTexture(WGLWidget.GLenum texture) {
		this.pImpl_.activeTexture(texture);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void attachShader(WGLWidget.Program program, WGLWidget.Shader shader) {
		this.pImpl_.attachShader(program, shader);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bindAttribLocation(WGLWidget.Program program, int index,
			final String name) {
		this.pImpl_.bindAttribLocation(program, index, name);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bindBuffer(WGLWidget.GLenum target, WGLWidget.Buffer buffer) {
		this.pImpl_.bindBuffer(target, buffer);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bindFramebuffer(WGLWidget.GLenum target,
			WGLWidget.Framebuffer buffer) {
		this.pImpl_.bindFramebuffer(target, buffer);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bindRenderbuffer(WGLWidget.GLenum target,
			WGLWidget.Renderbuffer buffer) {
		this.pImpl_.bindRenderbuffer(target, buffer);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bindTexture(WGLWidget.GLenum target, WGLWidget.Texture texture) {
		this.pImpl_.bindTexture(target, texture);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void blendColor(double red, double green, double blue, double alpha) {
		this.pImpl_.blendColor(red, green, blue, alpha);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void blendEquation(WGLWidget.GLenum mode) {
		this.pImpl_.blendEquation(mode);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void blendEquationSeparate(WGLWidget.GLenum modeRGB,
			WGLWidget.GLenum modeAlpha) {
		this.pImpl_.blendEquationSeparate(modeRGB, modeAlpha);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void blendFunc(WGLWidget.GLenum sfactor, WGLWidget.GLenum dfactor) {
		this.pImpl_.blendFunc(sfactor, dfactor);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void blendFuncSeparate(WGLWidget.GLenum srcRGB,
			WGLWidget.GLenum dstRGB, WGLWidget.GLenum srcAlpha,
			WGLWidget.GLenum dstAlpha) {
		this.pImpl_.blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bufferData(WGLWidget.GLenum target, int size,
			WGLWidget.GLenum usage) {
		this.pImpl_.bufferData(target, size, usage);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bufferData(WGLWidget.GLenum target, WGLWidget.ArrayBuffer res,
			WGLWidget.GLenum usage) {
		this.pImpl_.bufferData(target, res, usage);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bufferData(WGLWidget.GLenum target, WGLWidget.ArrayBuffer res,
			int bufferResourceOffset, int bufferResourceSize,
			WGLWidget.GLenum usage) {
		this.pImpl_.bufferData(target, res, bufferResourceOffset,
				bufferResourceSize, usage);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bufferSubData(WGLWidget.GLenum target, int offset,
			WGLWidget.ArrayBuffer res) {
		this.pImpl_.bufferSubData(target, offset, res);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bufferSubData(WGLWidget.GLenum target, int offset,
			WGLWidget.ArrayBuffer res, int bufferResourceOffset,
			int bufferResourceSize) {
		this.pImpl_.bufferSubData(target, offset, res, bufferResourceOffset,
				bufferResourceSize);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bufferDatafv(WGLWidget.GLenum target,
			final java.nio.ByteBuffer buffer, WGLWidget.GLenum usage,
			boolean binary) {
		this.pImpl_.bufferDatafv(target, buffer, usage, binary);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * Calls
	 * {@link #bufferDatafv(WGLWidget.GLenum target, java.nio.ByteBuffer buffer, WGLWidget.GLenum usage, boolean binary)
	 * bufferDatafv(target, buffer, usage, false)}
	 */
	public final void bufferDatafv(WGLWidget.GLenum target,
			final java.nio.ByteBuffer buffer, WGLWidget.GLenum usage) {
		bufferDatafv(target, buffer, usage, false);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bufferDatafv(WGLWidget.GLenum target,
			final java.nio.FloatBuffer buffer, WGLWidget.GLenum usage) {
		this.pImpl_.bufferDatafv(target, buffer, usage);
	}

	/**
	 * remove all binary buffer resources
	 * <p>
	 * Removes all WMemoryResources that were allocated when calling
	 * bufferDatafv with binary=true. This is not required, since the resources
	 * are also managed, but if you are sure they will not be used anymore in
	 * the client, this can help free some memory.
	 */
	public void clearBinaryResources() {
		this.pImpl_.clearBinaryResources();
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bufferDataiv(WGLWidget.GLenum target,
			final java.nio.IntBuffer buffer, WGLWidget.GLenum usage,
			WGLWidget.GLenum type) {
		this.pImpl_.bufferDataiv(target, buffer, usage, type);
	}

	/**
	 * GL function that updates an existing VBO with new float data.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/glBufferSubData.xml"
	 * >glBufferSubData() OpenGL ES manpage</a>
	 */
	public void bufferSubDatafv(WGLWidget.GLenum target, int offset,
			final java.nio.ByteBuffer buffer, boolean binary) {
		this.pImpl_.bufferSubDatafv(target, offset, buffer, binary);
	}

	/**
	 * GL function that updates an existing VBO with new float data.
	 * <p>
	 * Calls
	 * {@link #bufferSubDatafv(WGLWidget.GLenum target, int offset, java.nio.ByteBuffer buffer, boolean binary)
	 * bufferSubDatafv(target, offset, buffer, false)}
	 */
	public final void bufferSubDatafv(WGLWidget.GLenum target, int offset,
			final java.nio.ByteBuffer buffer) {
		bufferSubDatafv(target, offset, buffer, false);
	}

	/**
	 * GL function to activate an existing texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml"
	 * >glActiveTexture() OpenGL ES manpage</a>
	 */
	public void bufferSubDatafv(WGLWidget.GLenum target, int offset,
			final java.nio.FloatBuffer buffer) {
		this.pImpl_.bufferSubDatafv(target, offset, buffer);
	}

	/**
	 * GL function that loads integer data in a VBO.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/glBufferSubData.xml"
	 * >glBufferSubData() OpenGL ES manpage</a>
	 */
	public void bufferSubDataiv(WGLWidget.GLenum target, int offset,
			final java.nio.IntBuffer buffer, WGLWidget.GLenum type) {
		this.pImpl_.bufferSubDataiv(target, offset, buffer, type);
	}

	/**
	 * GL function that clears the given buffers.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glClear.xml"
	 * >glClear() OpenGL ES manpage</a>
	 */
	public void clear(EnumSet<WGLWidget.GLenum> mask) {
		this.pImpl_.clear(mask);
	}

	/**
	 * GL function that sets the clear color of the color buffer.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glClearColor.xml"
	 * >glClearColor() OpenGL ES manpage</a>
	 */
	public void clearColor(double r, double g, double b, double a) {
		this.pImpl_.clearColor(r, g, b, a);
	}

	/**
	 * GL function that configures the depth to be set when the depth buffer is
	 * cleared.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glClearDepthf.xml"
	 * >glClearDepthf() OpenGL ES manpage</a>
	 */
	public void clearDepth(double depth) {
		this.pImpl_.clearDepth(depth);
	}

	/**
	 * GL function.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glClearStencil.xml"
	 * >glClearStencil() OpenGL ES manpage</a>
	 */
	public void clearStencil(int s) {
		this.pImpl_.clearStencil(s);
	}

	/**
	 * GL function.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glColorMask.xml"
	 * >glColorMask() OpenGL ES manpage</a>
	 */
	public void colorMask(boolean red, boolean green, boolean blue,
			boolean alpha) {
		this.pImpl_.colorMask(red, green, blue, alpha);
	}

	/**
	 * GL function to compile a shader.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCompileShader.xml"
	 * >glCompileShader() OpenGL ES manpage</a>
	 */
	public void compileShader(WGLWidget.Shader shader) {
		this.pImpl_.compileShader(shader);
	}

	/**
	 * GL function to copy a texture image.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCopyTexImage2D.xml"
	 * >glCopyTexImage2D() OpenGL ES manpage</a>
	 */
	public void copyTexImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalFormat, int x, int y, int width,
			int height, int border) {
		this.pImpl_.copyTexImage2D(target, level, internalFormat, x, y, width,
				height, border);
	}

	/**
	 * GL function that copies a part of a texture image.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCopyTexSubImage2D.xml"
	 * >glCopyTexSubImage2D() OpenGL ES manpage</a>
	 */
	public void copyTexSubImage2D(WGLWidget.GLenum target, int level,
			int xoffset, int yoffset, int x, int y, int width, int height) {
		this.pImpl_.copyTexSubImage2D(target, level, xoffset, yoffset, x, y,
				width, height);
	}

	/**
	 * GL function that creates an empty VBO.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenBuffers.xml"
	 * >glGenBuffers() OpenGL ES manpage</a>
	 */
	public WGLWidget.Buffer createBuffer() {
		return this.pImpl_.getCreateBuffer();
	}

	/**
	 * Function that creates an {@link ArrayBuffer} by loading data from a given
	 * URL. (<b>deprecated</b>).
	 * <p>
	 * The {@link ArrayBuffer} is loaded before executing initGL. Use it to load
	 * binary data for use in
	 * {@link WGLWidget#activeTexture(WGLWidget.GLenum texture) activeTexture()}
	 * or {@link WGLWidget#activeTexture(WGLWidget.GLenum texture)
	 * activeTexture()}.
	 * <p>
	 * 
	 * @deprecated An {@link ArrayBuffer} refers to a javascript object, which
	 *             cannot be used for server-side rendering. The new way to
	 *             accomplish a binary transfer of buffers is with the added
	 *             boolean argument in
	 *             {@link WGLWidget#activeTexture(WGLWidget.GLenum texture)
	 *             activeTexture()}
	 */
	public WGLWidget.ArrayBuffer createAndLoadArrayBuffer(final String url) {
		return this.pImpl_.createAndLoadArrayBuffer(url);
	}

	/**
	 * GL function that creates a frame buffer object.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenFramebuffers.xml"
	 * >glGenFramebuffers() OpenGL ES manpage</a>
	 */
	public WGLWidget.Framebuffer getCreateFramebuffer() {
		return this.pImpl_.getCreateFramebuffer();
	}

	/**
	 * GL function that creates an empty program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCreateProgram.xml"
	 * >glCreateProgram() OpenGL ES manpage</a>
	 */
	public WGLWidget.Program createProgram() {
		return this.pImpl_.getCreateProgram();
	}

	/**
	 * GL function that creates a render buffer object.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenRenderbuffers.xml"
	 * >glGenRenderbuffers() OpenGL ES manpage</a>
	 */
	public WGLWidget.Renderbuffer getCreateRenderbuffer() {
		return this.pImpl_.getCreateRenderbuffer();
	}

	/**
	 * GL function that creates an empty shader.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCreateShader.xml"
	 * >glCreateShader() OpenGL ES manpage</a>
	 */
	public WGLWidget.Shader createShader(WGLWidget.GLenum shader) {
		return this.pImpl_.createShader(shader);
	}

	/**
	 * GL function that creates an empty texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenTextures.xml"
	 * >glGenTextures() OpenGL ES manpage</a>
	 */
	public WGLWidget.Texture createTexture() {
		return this.pImpl_.getCreateTexture();
	}

	/**
	 * GL function that creates an image texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenTextures.xml"
	 * >glGenTextures() OpenGL ES manpage</a>
	 */
	public WGLWidget.Texture createTextureAndLoad(final String url) {
		return this.pImpl_.createTextureAndLoad(url);
	}

	/**
	 * returns an paintdevice that can be used to paint a GL texture
	 * <p>
	 * If the client has a webGL enabled browser this function returns a
	 * {@link WCanvasPaintDevice}.
	 * <p>
	 * If server-side rendering is used as fallback then this function returns a
	 * {@link WRasterPaintDevice}.
	 */
	public WPaintDevice createPaintDevice(final WLength width,
			final WLength height) {
		return this.pImpl_.createPaintDevice(width, height);
	}

	/**
	 * GL function that configures the backface culling mode.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCullFace.xml"
	 * >glCullFace() OpenGL ES manpage</a>
	 */
	public void cullFace(WGLWidget.GLenum mode) {
		this.pImpl_.cullFace(mode);
	}

	/**
	 * GL function that deletes a VBO.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteBuffers.xml"
	 * >glDeleteBuffers() OpenGL ES manpage</a>
	 */
	public void deleteBuffer(WGLWidget.Buffer buffer) {
		this.pImpl_.deleteBuffer(buffer);
	}

	/**
	 * GL function that deletes a frame buffer.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteFramebuffers.xml"
	 * >glDeleteFramebuffers() OpenGL ES manpage</a>
	 */
	public void deleteFramebuffer(WGLWidget.Framebuffer buffer) {
		this.pImpl_.deleteFramebuffer(buffer);
	}

	/**
	 * GL function that deletes a program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteProgram.xml"
	 * >glDeleteProgram() OpenGL ES manpage</a>
	 */
	public void deleteProgram(WGLWidget.Program program) {
		this.pImpl_.deleteProgram(program);
	}

	/**
	 * GL function that deletes a render buffer.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteRenderbuffers.xml"
	 * >glDeleteRenderbuffers() OpenGL ES manpage</a>
	 */
	public void deleteRenderbuffer(WGLWidget.Renderbuffer buffer) {
		this.pImpl_.deleteRenderbuffer(buffer);
	}

	/**
	 * GL function that depetes a shader.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteShader.xml"
	 * >glDeleteShader() OpenGL ES manpage</a>
	 */
	public void deleteShader(WGLWidget.Shader shader) {
		this.pImpl_.deleteShader(shader);
	}

	/**
	 * GL function that deletes a texture.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteTextures.xml"
	 * >glDeleteTextures() OpenGL ES manpage</a>
	 */
	public void deleteTexture(WGLWidget.Texture texture) {
		this.pImpl_.deleteTexture(texture);
	}

	/**
	 * GL function to set the depth test function.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDepthFunc.xml"
	 * >glDepthFunc() OpenGL ES manpage</a>
	 */
	public void depthFunc(WGLWidget.GLenum func) {
		this.pImpl_.depthFunc(func);
	}

	/**
	 * GL function that enables or disables writing to the depth buffer.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDepthMask.xml"
	 * >glDepthMask() OpenGL ES manpage</a>
	 */
	public void depthMask(boolean flag) {
		this.pImpl_.depthMask(flag);
	}

	/**
	 * GL function that specifies to what range the normalized [-1,1] z values
	 * should match.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDepthRangef.xml"
	 * >glDepthRangef() OpenGL ES manpage</a>
	 */
	public void depthRange(double zNear, double zFar) {
		this.pImpl_.depthRange(zNear, zFar);
	}

	/**
	 * GL function that detaches a shader from a program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDetachShader.xml"
	 * >glDetachShader() OpenGL ES manpage</a>
	 */
	public void detachShader(WGLWidget.Program program, WGLWidget.Shader shader) {
		this.pImpl_.detachShader(program, shader);
	}

	/**
	 * GL function to disable features.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDisable.xml"
	 * >glDisable() OpenGL ES manpage</a>
	 */
	public void disable(WGLWidget.GLenum cap) {
		this.pImpl_.disable(cap);
	}

	/**
	 * GL function to disable the vertex attribute array.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDisableVertexAttribArray.xml"
	 * >glDisableVertexAttribArray() OpenGL ES manpage</a>
	 */
	public void disableVertexAttribArray(WGLWidget.AttribLocation index) {
		this.pImpl_.disableVertexAttribArray(index);
	}

	/**
	 * GL function to draw a VBO.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDrawArrays.xml"
	 * >glDrawArrays() OpenGL ES manpage</a>
	 */
	public void drawArrays(WGLWidget.GLenum mode, int first, int count) {
		this.pImpl_.drawArrays(mode, first, count);
	}

	/**
	 * GL function to draw indexed VBOs.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDrawElements.xml"
	 * >glDrawElements() OpenGL ES manpage</a>
	 */
	public void drawElements(WGLWidget.GLenum mode, int count,
			WGLWidget.GLenum type, int offset) {
		this.pImpl_.drawElements(mode, count, type, offset);
	}

	/**
	 * GL function to enable features.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glEnable.xml"
	 * >glEnable() OpenGL ES manpage</a>
	 */
	public void enable(WGLWidget.GLenum cap) {
		this.pImpl_.enable(cap);
	}

	/**
	 * GL function to enable the vertex attribute array.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glEnableVertexAttribArray.xml"
	 * >glEnableVertexAttribArray() OpenGL ES manpage</a>
	 */
	public void enableVertexAttribArray(WGLWidget.AttribLocation index) {
		this.pImpl_.enableVertexAttribArray(index);
	}

	/**
	 * GL function to wait until given commands are executed.
	 * <p>
	 * This call is transfered to JS, but the server will never wait on this
	 * call.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFinish.xml"
	 * >glFinish() OpenGL ES manpage</a>
	 */
	public void finish() {
		this.pImpl_.finish();
	}

	/**
	 * GL function to force execution of GL commands in finite time.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFlush.xml"
	 * >glFlush() OpenGL ES manpage</a>
	 */
	public void flush() {
		this.pImpl_.flush();
	}

	/**
	 * GL function to attach the given renderbuffer to the currently bound frame
	 * buffer.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFramebufferRenderbuffer.xml"
	 * >glFramebufferRenderbuffer() OpenGL ES manpage</a>
	 */
	public void framebufferRenderbuffer(WGLWidget.GLenum target,
			WGLWidget.GLenum attachment, WGLWidget.GLenum renderbuffertarget,
			WGLWidget.Renderbuffer renderbuffer) {
		this.pImpl_.framebufferRenderbuffer(target, attachment,
				renderbuffertarget, renderbuffer);
	}

	/**
	 * GL function to render directly into a texture image.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFramebufferTexture2D.xml"
	 * >glFramebufferTexture2D() OpenGL ES manpage</a>
	 */
	public void framebufferTexture2D(WGLWidget.GLenum target,
			WGLWidget.GLenum attachment, WGLWidget.GLenum textarget,
			WGLWidget.Texture texture, int level) {
		this.pImpl_.framebufferTexture2D(target, attachment, textarget,
				texture, level);
	}

	/**
	 * GL function that specifies which side of a triangle is the front side.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFrontFace.xml"
	 * >glFrontFace() OpenGL ES manpage</a>
	 */
	public void frontFace(WGLWidget.GLenum mode) {
		this.pImpl_.frontFace(mode);
	}

	/**
	 * GL function that generates a set of mipmaps for a texture object.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenerateMipmap.xml"
	 * >glGenerateMipmap() OpenGL ES manpage</a>
	 */
	public void generateMipmap(WGLWidget.GLenum target) {
		this.pImpl_.generateMipmap(target);
	}

	/**
	 * GL function to retrieve an attribute&apos;s location in a {@link Program}
	 * .
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGetAttribLocation.xml"
	 * >glGetAttribLocation() OpenGL ES manpage</a>
	 */
	public WGLWidget.AttribLocation getAttribLocation(
			WGLWidget.Program program, final String attrib) {
		return this.pImpl_.getAttribLocation(program, attrib);
	}

	/**
	 * GL function to retrieve a Uniform&apos;s location in a {@link Program}.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGetUniformLocation.xml"
	 * >glGetUniformLocation() OpenGL ES manpage</a>
	 */
	public WGLWidget.UniformLocation getUniformLocation(
			WGLWidget.Program program, final String location) {
		return this.pImpl_.getUniformLocation(program, location);
	}

	/**
	 * GL function to give hints to the render pipeline.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glHint.xml"
	 * >glHint() OpenGL ES manpage</a>
	 */
	public void hint(WGLWidget.GLenum target, WGLWidget.GLenum mode) {
		this.pImpl_.hint(target, mode);
	}

	/**
	 * GL function to set the line width.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glLineWidth.xml"
	 * >glLineWidth() OpenGL ES manpage</a>
	 */
	public void lineWidth(double width) {
		this.pImpl_.lineWidth(width);
	}

	/**
	 * GL function to link a program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glLinkProgram.xml"
	 * >glLinkProgram() OpenGL ES manpage</a>
	 */
	public void linkProgram(WGLWidget.Program program) {
		this.pImpl_.linkProgram(program);
	}

	/**
	 * GL function to set the pixel storage mode.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glPixelStorei.xml"
	 * >glPixelStorei() OpenGL ES manpage</a>
	 */
	public void pixelStorei(WGLWidget.GLenum pname, int param) {
		this.pImpl_.pixelStorei(pname, param);
	}

	/**
	 * GL function to apply modifications to Z values.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glPolygonOffset.xml"
	 * >glPolygonOffset() OpenGL ES manpage</a>
	 */
	public void polygonOffset(double factor, double units) {
		this.pImpl_.polygonOffset(factor, units);
	}

	/**
	 * GL function to allocate the appropriate amount of memory for a render
	 * buffer.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glRenderbufferStorage.xml"
	 * >glSampleCoverage() OpenGL ES manpage</a>
	 */
	public void renderbufferStorage(WGLWidget.GLenum target,
			WGLWidget.GLenum internalformat, int width, int height) {
		this.pImpl_.renderbufferStorage(target, internalformat, width, height);
	}

	/**
	 * GL function to set multisample parameters.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glSampleCoverage.xml"
	 * >glSampleCoverage() OpenGL ES manpage</a>
	 */
	public void sampleCoverage(double value, boolean invert) {
		this.pImpl_.sampleCoverage(value, invert);
	}

	/**
	 * GL function to define the scissor box.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glScissor.xml"
	 * >glScissor() OpenGL ES manpage</a>
	 */
	public void scissor(int x, int y, int width, int height) {
		this.pImpl_.scissor(x, y, width, height);
	}

	/**
	 * GL function to set a shader&apos;s source code.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glShaderSource.xml"
	 * >glShaderSource() OpenGL ES manpage</a>
	 */
	public void shaderSource(WGLWidget.Shader shader, final String src) {
		this.pImpl_.shaderSource(shader, src);
	}

	/**
	 * GL function to set stencil test parameters.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilFunc.xml"
	 * >glStencilFunc() OpenGL ES manpage</a>
	 */
	public void stencilFunc(WGLWidget.GLenum func, int ref, int mask) {
		this.pImpl_.stencilFunc(func, ref, mask);
	}

	/**
	 * GL function to set stencil test parameters for front and/or back
	 * stencils.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilFuncSeparate.xml"
	 * >glStencilFuncSeparate() OpenGL ES manpage</a>
	 */
	public void stencilFuncSeparate(WGLWidget.GLenum face,
			WGLWidget.GLenum func, int ref, int mask) {
		this.pImpl_.stencilFuncSeparate(face, func, ref, mask);
	}

	/**
	 * GL function to control which bits are to be written in the stencil
	 * buffer.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilMask.xml"
	 * >glStencilMask() OpenGL ES manpage</a>
	 */
	public void stencilMask(int mask) {
		this.pImpl_.stencilMask(mask);
	}

	/**
	 * GL function to control which bits are written to the front and/or back
	 * stencil buffers.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilMaskSeparate.xml"
	 * >glStencilMaskSeparate() OpenGL ES manpage</a>
	 */
	public void stencilMaskSeparate(WGLWidget.GLenum face, int mask) {
		this.pImpl_.stencilMaskSeparate(face, mask);
	}

	/**
	 * GL function to set stencil test actions.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilOp.xml"
	 * >glStencilOp() OpenGL ES manpage</a>
	 */
	public void stencilOp(WGLWidget.GLenum fail, WGLWidget.GLenum zfail,
			WGLWidget.GLenum zpass) {
		this.pImpl_.stencilOp(fail, zfail, zpass);
	}

	/**
	 * GL function to set front and/or back stencil test actions separately.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilOpSeparate.xml"
	 * >glStencilOpSeparate() OpenGL ES manpage</a>
	 */
	public void stencilOpSeparate(WGLWidget.GLenum face, WGLWidget.GLenum fail,
			WGLWidget.GLenum zfail, WGLWidget.GLenum zpass) {
		this.pImpl_.stencilOpSeparate(face, fail, zfail, zpass);
	}

	/**
	 * GL function to reserve space for a 2D texture, without specifying its
	 * contents.
	 * <p>
	 * This corresponds to calling the WebGL function void texImage2D(GLenum
	 * target, GLint level, GLenum internalformat, GLsizei width, GLsizei
	 * height, GLint border, GLenum format, GLenum type, ArrayBufferView pixels)
	 * with null as last parameters. The value of &apos;type&apos; is then of no
	 * importance and is therefore omitted from this function.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml"
	 * >glTexImage2D() OpenGL ES manpage</a>
	 */
	public void texImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalformat, int width, int height, int border,
			WGLWidget.GLenum format) {
		this.pImpl_.texImage2D(target, level, internalformat, width, height,
				border, format);
	}

	/**
	 * GL function to load a 2D texture from a {@link WImage}.
	 * <p>
	 * Note: {@link WImage} must be loaded before this function is executed.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml"
	 * >glTexImage2D() OpenGL ES manpage</a>
	 */
	public void texImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalformat, WGLWidget.GLenum format,
			WGLWidget.GLenum type, WImage image) {
		this.pImpl_.texImage2D(target, level, internalformat, format, type,
				image);
	}

	/**
	 * GL function to load a 2D texture from a {@link WVideo}.
	 * <p>
	 * Note: the video must be loaded prior to calling this function. The
	 * current frame is used as texture image.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml"
	 * >glTexImage2D() OpenGL ES manpage</a>
	 */
	public void texImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalformat, WGLWidget.GLenum format,
			WGLWidget.GLenum type, WVideo video) {
		this.pImpl_.texImage2D(target, level, internalformat, format, type,
				video);
	}

	/**
	 * GL function to load a 2D texture from a file.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml"
	 * >glTexImage2D() OpenGL ES manpage</a>
	 */
	public void texImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalformat, WGLWidget.GLenum format,
			WGLWidget.GLenum type, String imgFilename) {
		this.pImpl_.texImage2D(target, level, internalformat, format, type,
				imgFilename);
	}

	/**
	 * GL function to load a 2D texture from a {@link WPaintDevice}.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml"
	 * >glTexImage2D() OpenGL ES manpage</a>
	 * <p>
	 * 
	 * @see WGLWidget#createPaintDevice(WLength width, WLength height)
	 */
	public void texImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalformat, WGLWidget.GLenum format,
			WGLWidget.GLenum type, WPaintDevice paintdevice) {
		this.pImpl_.texImage2D(target, level, internalformat, format, type,
				paintdevice);
	}

	/**
	 * GL function to load a 2D texture loaded with
	 * {@link WGLWidget#createTextureAndLoad(String url) createTextureAndLoad()}
	 * .
	 * <p>
	 * This function must only be used for textures created with
	 * {@link WGLWidget#createTextureAndLoad(String url) createTextureAndLoad()}
	 * <p>
	 * Note: the {@link WGLWidget} implementation will delay rendering until all
	 * textures created with {@link WGLWidget#createTextureAndLoad(String url)
	 * createTextureAndLoad()} are loaded in the browser.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml"
	 * >glTexImage2D() OpenGL ES manpage</a>
	 */
	public void texImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalformat, WGLWidget.GLenum format,
			WGLWidget.GLenum type, WGLWidget.Texture texture) {
		this.pImpl_.texImage2D(target, level, internalformat, format, type,
				texture);
	}

	/**
	 * GL function to set texture parameters.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexParameter.xml"
	 * >glTexParameter() OpenGL ES manpage</a>
	 */
	public void texParameteri(WGLWidget.GLenum target, WGLWidget.GLenum pname,
			WGLWidget.GLenum param) {
		this.pImpl_.texParameteri(target, pname, param);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform1f(final WGLWidget.UniformLocation location, double x) {
		this.pImpl_.uniform1f(location, x);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform1fv(final WGLWidget.UniformLocation location,
			float[] value) {
		this.pImpl_.uniform1fv(location, value);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform1fv(final WGLWidget.UniformLocation location,
			final WGLWidget.JavaScriptVector v) {
		this.pImpl_.uniform1fv(location, v);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform1i(final WGLWidget.UniformLocation location, int x) {
		this.pImpl_.uniform1i(location, x);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform1iv(final WGLWidget.UniformLocation location, int[] value) {
		this.pImpl_.uniform1iv(location, value);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform2f(final WGLWidget.UniformLocation location, double x,
			double y) {
		this.pImpl_.uniform2f(location, x, y);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform2fv(final WGLWidget.UniformLocation location,
			float[] value) {
		this.pImpl_.uniform2fv(location, value);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform2fv(final WGLWidget.UniformLocation location,
			final WGLWidget.JavaScriptVector v) {
		this.pImpl_.uniform2fv(location, v);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform2i(final WGLWidget.UniformLocation location, int x, int y) {
		this.pImpl_.uniform2i(location, x, y);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform2iv(final WGLWidget.UniformLocation location, int[] value) {
		this.pImpl_.uniform2iv(location, value);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform3f(final WGLWidget.UniformLocation location, double x,
			double y, double z) {
		this.pImpl_.uniform3f(location, x, y, z);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform3fv(final WGLWidget.UniformLocation location,
			float[] value) {
		this.pImpl_.uniform3fv(location, value);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform3fv(final WGLWidget.UniformLocation location,
			final WGLWidget.JavaScriptVector v) {
		this.pImpl_.uniform3fv(location, v);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform3i(final WGLWidget.UniformLocation location, int x,
			int y, int z) {
		this.pImpl_.uniform3i(location, x, y, z);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform3iv(final WGLWidget.UniformLocation location, int[] value) {
		this.pImpl_.uniform3iv(location, value);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform4f(final WGLWidget.UniformLocation location, double x,
			double y, double z, double w) {
		this.pImpl_.uniform4f(location, x, y, z, w);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform4fv(final WGLWidget.UniformLocation location,
			float[] value) {
		this.pImpl_.uniform4fv(location, value);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform4fv(final WGLWidget.UniformLocation location,
			final WGLWidget.JavaScriptVector v) {
		this.pImpl_.uniform4fv(location, v);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform4i(final WGLWidget.UniformLocation location, int x,
			int y, int z, int w) {
		this.pImpl_.uniform4i(location, x, y, z, w);
	}

	/**
	 * GL function to set the value of a uniform variable of the current
	 * program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniform4iv(final WGLWidget.UniformLocation location, int[] value) {
		this.pImpl_.uniform4iv(location, value);
	}

	/**
	 * GL function to set the value of a uniform matrix of the current program.
	 * <p>
	 * Attention: The OpenGL ES specification states that transpose MUST be
	 * false.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniformMatrix2fv(final WGLWidget.UniformLocation location,
			boolean transpose, double[] value) {
		this.pImpl_.uniformMatrix2fv(location, transpose, value);
	}

	/**
	 * GL function to set the value of a uniform matrix of the current program.
	 * <p>
	 * This function renders the matrix in the proper row/column order.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniformMatrix2(final WGLWidget.UniformLocation location,
			final Matrix2f m) {
		this.pImpl_.uniformMatrix2(location, m);
	}

	/**
	 * GL function to set the value of a uniform matrix of the current program.
	 * <p>
	 * Attention: The OpenGL ES specification states that transpose MUST be
	 * false.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniformMatrix3fv(final WGLWidget.UniformLocation location,
			boolean transpose, double[] value) {
		this.pImpl_.uniformMatrix3fv(location, transpose, value);
	}

	/**
	 * GL function to set the value of a uniform matrix of the current program.
	 * <p>
	 * This function renders the matrix in the proper row/column order.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniformMatrix3(final WGLWidget.UniformLocation location,
			final javax.vecmath.Matrix3f m) {
		this.pImpl_.uniformMatrix3(location, m);
	}

	/**
	 * GL function to set the value of a uniform matrix of the current program.
	 * <p>
	 * Attention: The OpenGL ES specification states that transpose MUST be
	 * false.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniformMatrix4fv(final WGLWidget.UniformLocation location,
			boolean transpose, double[] value) {
		this.pImpl_.uniformMatrix4fv(location, transpose, value);
	}

	/**
	 * GL function to set the value of a uniform matrix of the current program.
	 * <p>
	 * This function renders the matrix in the proper row/column order.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniformMatrix4(final WGLWidget.UniformLocation location,
			final javax.vecmath.Matrix4f m) {
		this.pImpl_.uniformMatrix4(location, m);
	}

	/**
	 * GL function to set the value of a uniform matrix of the current program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml"
	 * >glUniform() OpenGL ES manpage</a>
	 */
	public void uniformMatrix4(final WGLWidget.UniformLocation location,
			final WGLWidget.JavaScriptMatrix4x4 jsm) {
		if (!jsm.isInitialized()) {
			throw new WException("JavaScriptMatrix4x4: matrix not initialized");
		}
		this.pImpl_.uniformMatrix4(location, jsm);
	}

	/**
	 * GL function to set the current active shader program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUseProgram.xml"
	 * >glUseProgram() OpenGL ES manpage</a>
	 */
	public void useProgram(WGLWidget.Program program) {
		this.pImpl_.useProgram(program);
	}

	/**
	 * GL function to validate a program.
	 * <p>
	 * implementation note: there is currently not yet a method to read out the
	 * validation result.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glValidateProgram.xml"
	 * >glValidateProgram() OpenGL ES manpage</a>
	 */
	public void validateProgram(WGLWidget.Program program) {
		this.pImpl_.validateProgram(program);
	}

	/**
	 * GL function to set the value of an attribute of the current program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml"
	 * >glVertexAttrib() OpenGL ES manpage</a>
	 */
	public void vertexAttrib1f(WGLWidget.AttribLocation location, double x) {
		this.pImpl_.vertexAttrib1f(location, x);
	}

	/**
	 * GL function to set the value of an attribute of the current program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml"
	 * >glVertexAttrib() OpenGL ES manpage</a>
	 */
	public void vertexAttrib2f(WGLWidget.AttribLocation location, double x,
			double y) {
		this.pImpl_.vertexAttrib2f(location, x, y);
	}

	/**
	 * GL function to set the value of an attribute of the current program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml"
	 * >glVertexAttrib() OpenGL ES manpage</a>
	 */
	public void vertexAttrib2fv(WGLWidget.AttribLocation location,
			float[] values) {
		this.vertexAttrib2f(location, values[0], values[1]);
	}

	/**
	 * GL function to set the value of an attribute of the current program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml"
	 * >glVertexAttrib() OpenGL ES manpage</a>
	 */
	public void vertexAttrib3f(WGLWidget.AttribLocation location, double x,
			double y, double z) {
		this.pImpl_.vertexAttrib3f(location, x, y, z);
	}

	/**
	 * GL function to set the value of an attribute of the current program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml"
	 * >glVertexAttrib() OpenGL ES manpage</a>
	 */
	public void vertexAttrib3fv(WGLWidget.AttribLocation location,
			float[] values) {
		this.vertexAttrib3f(location, values[0], values[1], values[2]);
	}

	/**
	 * GL function to set the value of an attribute of the current program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml"
	 * >glVertexAttrib() OpenGL ES manpage</a>
	 */
	public void vertexAttrib4f(WGLWidget.AttribLocation location, double x,
			double y, double z, double w) {
		this.pImpl_.vertexAttrib4f(location, x, y, z, w);
	}

	/**
	 * GL function to set the value of an attribute of the current program.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml"
	 * >glVertexAttrib() OpenGL ES manpage</a>
	 */
	public void vertexAttrib4fv(WGLWidget.AttribLocation location,
			float[] values) {
		this.vertexAttrib4f(location, values[0], values[1], values[2],
				values[3]);
	}

	/**
	 * GL function to bind a VBO to an attribute.
	 * <p>
	 * This function links the given attribute to the VBO currently bound to the
	 * ARRAY_BUFFER target.
	 * <p>
	 * The size parameter specifies the number of components per attribute (1 to
	 * 4). The type parameter is also used to determine the size of each
	 * component.
	 * <p>
	 * The size of a float is 8 bytes.
	 * <p>
	 * In {@link WGLWidget}, the size of an int is 4 bytes.
	 * <p>
	 * The stride is in bytes.
	 * <p>
	 * The maximum stride is 255.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttribPointer.xml"
	 * >glVertexAttribPointer() OpenGL ES manpage</a>
	 */
	public void vertexAttribPointer(WGLWidget.AttribLocation location,
			int size, WGLWidget.GLenum type, boolean normalized, int stride,
			int offset) {
		this.pImpl_.vertexAttribPointer(location, size, type, normalized,
				stride, offset);
	}

	/**
	 * GL function to set the viewport.
	 * <p>
	 * <a href=
	 * "http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glViewport.xml"
	 * >glViewport() OpenGL ES manpage</a>
	 */
	public void viewport(int x, int y, int width, int height) {
		this.pImpl_.viewport(x, y, width, height);
	}

	/**
	 * Create a matrix that can be manipulated in client-side JavaScript.
	 * <p>
	 * This is a shorthand for creating a {@link JavaScriptMatrix4x4}, then
	 * adding it to a {@link WGLWidget} with addJavaScriptMatrix4, and
	 * initializing it with initJavaScriptMatrix4.
	 * <p>
	 * This method should only be called in {@link WGLWidget#initializeGL()
	 * initializeGL()}, {@link WGLWidget#updateGL() updateGL()} or
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()}.
	 */
	public WGLWidget.JavaScriptMatrix4x4 createJavaScriptMatrix4() {
		WGLWidget.JavaScriptMatrix4x4 mat = new WGLWidget.JavaScriptMatrix4x4();
		this.addJavaScriptMatrix4(mat);
		this.initJavaScriptMatrix4(mat);
		return mat;
	}

	/**
	 * Register a matrix with this {@link WGLWidget}.
	 * <p>
	 * You can call this outside of
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()},
	 * {@link WGLWidget#paintGL() paintGL()}, {@link WGLWidget#updateGL()
	 * updateGL()} or {@link WGLWidget#initializeGL() initializeGL()} methods.
	 * After a {@link JavaScriptMatrix4x4} is added to a {@link WGLWidget}, its
	 * {@link WWidget#getJsRef() WWidget#getJsRef()} becomes valid, and can be
	 * used in a {@link JSlot}, for example.
	 */
	public void addJavaScriptMatrix4(final WGLWidget.JavaScriptMatrix4x4 mat) {
		if (mat.hasContext()) {
			throw new WException(
					"The given matrix is already associated with a WGLWidget!");
		}
		mat.assignToContext(this.jsValues_++, this);
		this.jsMatrixList_.add(new WGLWidget.jsMatrixMap(mat.getId(),
				new javax.vecmath.Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
						1.0f)));
	}

	/**
	 * Initialize the client-side JavaScript for the given
	 * {@link JavaScriptMatrix4x4}.
	 * <p>
	 * If the given matrix is not associated with a widget yet, it will be added
	 * to this widget.
	 * <p>
	 * If the given matrix has already been added to a {@link WGLWidget}, then
	 * this {@link WGLWidget} should be the same as the one you call
	 * initJavaScriptMatrix4 on.
	 * <p>
	 * This method should only be called in {@link WGLWidget#initializeGL()
	 * initializeGL()}, {@link WGLWidget#updateGL() updateGL()} or
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()}.
	 */
	public void initJavaScriptMatrix4(final WGLWidget.JavaScriptMatrix4x4 mat) {
		this.pImpl_.initJavaScriptMatrix4(mat);
	}

	/**
	 * Set the value of a client-side JavaScript matrix created by
	 * createJavaScriptMatrix4x4().
	 * <p>
	 * This method should only be called in {@link WGLWidget#initializeGL()
	 * initializeGL()}, {@link WGLWidget#updateGL() updateGL()} or
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()}.
	 */
	public void setJavaScriptMatrix4(final WGLWidget.JavaScriptMatrix4x4 jsm,
			final javax.vecmath.Matrix4f m) {
		if (!jsm.isInitialized()) {
			throw new WException("JavaScriptMatrix4x4: matrix not initialized");
		}
		if (jsm.hasOperations()) {
			throw new WException(
					"JavaScriptMatrix4x4: matrix was already operated on");
		}
		this.valueChanged_ = true;
		for (int i = 0; i < this.jsMatrixList_.size(); i++) {
			if (this.jsMatrixList_.get(i).id == jsm.getId()) {
				this.jsMatrixList_.get(i).serverSideCopy = m;
			}
		}
		this.pImpl_.setJavaScriptMatrix4(jsm, m);
	}

	/**
	 * Create a vector of a certain length that can be manipulated in
	 * client-side JavaScript.
	 * <p>
	 * This is a shorthand for creating a {@link JavaScriptVector}, then adding
	 * it to a {@link WGLWidget} with addJavaScriptVector, and initializing it
	 * with initJavaScriptVector.
	 * <p>
	 * This method should only be called in {@link WGLWidget#initializeGL()
	 * initializeGL()}, {@link WGLWidget#updateGL() updateGL()} or
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()}.
	 */
	public WGLWidget.JavaScriptVector createJavaScriptVector(int length) {
		WGLWidget.JavaScriptVector vec = new WGLWidget.JavaScriptVector(length);
		this.addJavaScriptVector(vec);
		this.initJavaScriptVector(vec);
		return vec;
	}

	/**
	 * Register a vector with this {@link WGLWidget}.
	 * <p>
	 * You can call this outside of
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()},
	 * {@link WGLWidget#paintGL() paintGL()}, {@link WGLWidget#updateGL()
	 * updateGL()} or {@link WGLWidget#initializeGL() initializeGL()} methods.
	 * After a {@link JavaScriptVector} is added to a {@link WGLWidget}, its
	 * {@link WWidget#getJsRef() WWidget#getJsRef()} becomes valid, and can be
	 * used in a {@link JSlot}, for example.
	 */
	public void addJavaScriptVector(final WGLWidget.JavaScriptVector vec) {
		if (vec.hasContext()) {
			throw new WException(
					"The given matrix is already associated with a WGLWidget!");
		}
		vec.assignToContext(this.jsValues_++, this);
		List<Float> values = new ArrayList<Float>();
		for (int i = 0; i < vec.getLength(); ++i) {
			values.add(0.0f);
		}
		this.jsVectorList_.add(new WGLWidget.jsVectorMap(vec.getId(), values));
	}

	/**
	 * Initialize the client-side JavaScript for the given
	 * {@link JavaScriptVector}.
	 * <p>
	 * If the given vector is not associated with a widget yet, it will be added
	 * to this widget.
	 * <p>
	 * If the given vector has already been added to a {@link WGLWidget}, then
	 * this {@link WGLWidget} should be the same as the one you call
	 * initJavaScriptVector on.
	 * <p>
	 * This method should only be called in {@link WGLWidget#initializeGL()
	 * initializeGL()}, {@link WGLWidget#updateGL() updateGL()} or
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()}.
	 */
	public void initJavaScriptVector(final WGLWidget.JavaScriptVector vec) {
		this.pImpl_.initJavaScriptVector(vec);
	}

	/**
	 * Set the value of a client-side JavaScript vector created by
	 * {@link WGLWidget#createJavaScriptVector(int length)
	 * createJavaScriptVector()}.
	 * <p>
	 * This method should only be called in {@link WGLWidget#initializeGL()
	 * initializeGL()}, {@link WGLWidget#updateGL() updateGL()} or
	 * {@link WGLWidget#resizeGL(int width, int height) resizeGL()}.
	 */
	public void setJavaScriptVector(final WGLWidget.JavaScriptVector jsv,
			final List<Float> v) {
		if (!jsv.isInitialized()) {
			throw new WException("JavaScriptVector: vector not initialized");
		}
		this.valueChanged_ = true;
		for (int i = 0; i < this.jsVectorList_.size(); i++) {
			if (this.jsVectorList_.get(i).id == jsv.getId()) {
				Utils.copyList(v, this.jsVectorList_.get(i).serverSideCopy);
			}
		}
		this.pImpl_.setJavaScriptVector(jsv, v);
	}

	/**
	 * Set a custom mouse handler based on the given JavaScript code.
	 * <p>
	 * The handler code should be JavaScript code that produces an object when
	 * evaluated.
	 * <p>
	 * A mouse handler is an object that can implement one or more of the
	 * following functions:
	 * <p>
	 * <ul>
	 * <li><b>setTarget(target)</b>: This is called immediately when the mouse
	 * handler is added with an object that uniquely identifies the
	 * {@link WGLWidget}, and a {@link WGLWidget#paintGL() paintGL()} method.</li>
	 * <li><b>mouseDown(o, event)</b>: To handle the <code>mousedown</code>
	 * event. <code>o</code> is the <code>&lt;canvas&gt;</code> (client-side
	 * rendering) or <code>&lt;img&gt;</code> (server-side rendering) element
	 * corresponding to this {@link WGLWidget}. <code>event</code> is the
	 * <code>MouseEvent</code>.</li>
	 * <li><b>mouseUp(o, event)</b>: To handle the <code>mouseup</code> event.
	 * <code>o</code> is the <code>&lt;canvas&gt;</code> (client-side rendering)
	 * or <code>&lt;img&gt;</code> (server-side rendering) element corresponding
	 * to this {@link WGLWidget}. <code>event</code> is the
	 * <code>MouseEvent</code>.</li>
	 * <li><b>mouseDrag(o, event)</b>: Called when the mouse is dragged.
	 * <code>o</code> is the <code>&lt;canvas&gt;</code> (client-side rendering)
	 * or <code>&lt;img&gt;</code> (server-side rendering) element corresponding
	 * to this {@link WGLWidget}. <code>event</code> is the
	 * <code>MouseEvent</code>.</li>
	 * <li><b>mouseMove(o, event)</b>: Called when the mouse is moved.
	 * <code>o</code> is the <code>&lt;canvas&gt;</code> (client-side rendering)
	 * or <code>&lt;img&gt;</code> (server-side rendering) element corresponding
	 * to this {@link WGLWidget}. <code>event</code> is the
	 * <code>MouseEvent</code>.</li>
	 * <li><b>mouseWheel(o, event)</b>: Called when the mouse wheel is used.
	 * <code>o</code> is the <code>&lt;canvas&gt;</code> (client-side rendering)
	 * or <code>&lt;img&gt;</code> (server-side rendering) element corresponding
	 * to this {@link WGLWidget}. <code>event</code> is the
	 * <code>MouseEvent</code>.</li>
	 * <li><b>touchStart(o, event)</b>: To handle the <code>touchstart</code>
	 * event. <code>o</code> is the <code>&lt;canvas&gt;</code> (client-side
	 * rendering) or <code>&lt;img&gt;</code> (server-side rendering) element
	 * corresponding to this {@link WGLWidget}. <code>event</code> is the
	 * <code>TouchEvent</code>.</li>
	 * <li><b>touchEnd(o, event)</b>: To handle the <code>touchend</code> event.
	 * <code>o</code> is this <code>&lt;canvas&gt;</code> (client-side
	 * rendering) or <code>&lt;img&gt;</code> (server-side rendering) element
	 * corresponding to this {@link WGLWidget}. <code>event</code> is the
	 * <code>TouchEvent</code>.</li>
	 * <li><b>touchMoved(o, event)</b>: To handle the <code>touchmove</code>
	 * event. <code>o</code> is this <code>&lt;canvas&gt;</code> (client-side
	 * rendering) or <code>&lt;img&gt;</code> (server-side rendering) element
	 * corresponding to this {@link WGLWidget}. <code>event</code> is the
	 * <code>TouchEvent</code>.</li>
	 * </ul>
	 * <p>
	 * Example: If the variable <code>mouseHandler</code> contains the following
	 * JavaScript, and uses the <a href="glmatrix.net">glMatrix</a> library:
	 * 
	 * <pre>
	 *   {@code
	 *   function(cameraMatrix) {
	 *     var target = null;
	 *   
	 *     this.setTarget = function(newTarget) {
	 *       target = newTarget;
	 *     };
	 *   
	 *     this.mouseWheel = function(o, event) {
	 *       var fix = jQuery.event.fix(event);
	 *       fix.preventDefault();
	 *       fix.stopPropagation();
	 *       var d = wheelDelta(event);
	 *       var s = Math.pow(1.2, delta);
	 *       mat4.scale(cameraMatrix, cameraMatrix, [s, s, s]);
	 *       target.paintGL();
	 *     };
	 *   
	 *     function wheelDelta(e) {
	 *       var delta = 0;
	 *       if (e.wheelDelta) {
	 *         delta = e.wheelDelta > 0 ? 1 : -1;
	 *       } else if (e.detail) {
	 *         delta = e.detail < 0 ? 1 : -1;
	 *       }
	 *       return delta;
	 *     }
	 *   }
	 *   }
	 * </pre>
	 * <p>
	 * This mouse handler can be set as such:
	 * 
	 * <pre>
	 *   {@code
	 *   setClientSideMouseHandler("new " + mouseHandler + "(" + cameraMatrix.jsRef() + ")");
	 *   }
	 * </pre>
	 * 
	 * where <code>mouseHandler</code> is the code of the mouse handler, and
	 * <code>cameraMatrix</code> is a {@link JavaScriptMatrix4x4}.
	 */
	public void setClientSideMouseHandler(final String handlerCode) {
		this.pImpl_.setClientSideMouseHandler(handlerCode);
	}

	/**
	 * Add a mouse handler to the widget that looks at a given point.
	 * <p>
	 * This will allow a user to change client-side matrix m with the mouse. M
	 * is a model transformation matrix, representing the viewpoint of the
	 * camera.
	 * <p>
	 * Through mouse operations, the camera can be changed by the user, but (lX,
	 * lY, lZ) will always be at the center of the display, (uX, uY, uZ) is
	 * considered to be the up direction, and the distance of the camera to (lX,
	 * lY, lZ) will never change.
	 * <p>
	 * Pressing the left mouse button and moving the mouse left/right will
	 * rotate the camera around the up (uX, uY, uZ) direction. Moving up/down
	 * will tilt the camera (causing it to move up/down to keep the lookpoint
	 * centered). The scroll wheel simulates zooming by scaling the scene.
	 * <p>
	 * pitchRate and yawRate control how much the camera will move per mouse
	 * pixel.
	 * <p>
	 * Usually this method is called after setting a camera transformation with
	 * a client-side matrix in {@link WGLWidget#initializeGL() initializeGL()}.
	 * However, this function may also be called from outside the
	 * intializeGL()/paintGL()/updateGL() methods (but not before m was
	 * initialized).
	 */
	public void setClientSideLookAtHandler(
			final WGLWidget.JavaScriptMatrix4x4 m, double centerX,
			double centerY, double centerZ, double uX, double uY, double uZ,
			double pitchRate, double yawRate) {
		this.pImpl_.setClientSideLookAtHandler(m, centerX, centerY, centerZ,
				uX, uY, uZ, pitchRate, yawRate);
	}

	/**
	 * Add a mouse handler to the widget that allows &apos;walking&apos; in the
	 * scene.
	 * <p>
	 * This will allow a user to change client-side matrix m with the mouse. M
	 * is a model transformation matrix, representing the viewpoint of the
	 * camera.
	 * <p>
	 * Through mouse operations, the camera can be changed by the user, as if he
	 * is walking around on a plane.
	 * <p>
	 * Pressing the left mouse button and moving the mouse left/right will
	 * rotate the camera around Y axis. Moving the mouse up/down will move the
	 * camera in the Z direction (walking forward/backward). centered).
	 * <p>
	 * frontStep and rotStep control how much the camera will move per mouse
	 * pixel.
	 */
	public void setClientSideWalkHandler(final WGLWidget.JavaScriptMatrix4x4 m,
			double frontStep, double rotStep) {
		this.pImpl_.setClientSideWalkHandler(m, frontStep, rotStep);
	}

	/**
	 * Sets the content to be displayed when WebGL is not available.
	 * <p>
	 * If JWt cannot create a working WebGL context, this content will be shown
	 * to the user. This may be a text explanation, or a pre-rendered image, or
	 * a video, a flash movie, ...
	 * <p>
	 * The default is a widget that explains to the user that he has no WebGL
	 * support.
	 */
	public void setAlternativeContent(WWidget alternative) {
		if (this.alternative_ != null) {
			if (this.alternative_ != null)
				this.alternative_.remove();
		}
		this.alternative_ = alternative;
		if (this.alternative_ != null) {
			this.addChild(this.alternative_);
		}
	}

	/**
	 * A JavaScript slot that repaints the widget when triggered.
	 * <p>
	 * This is useful for client-side initiated repaints. You may e.g. use this
	 * if you write your own client-side mouse handler, or if you updated a
	 * texture, or if you&apos;re playing a video texture.
	 */
	public JSlot getRepaintSlot() {
		return this.repaintSlot_;
	}

	/**
	 * enable client-side error messages (read detailed doc!)
	 * <p>
	 * This option will add client-side code to check the result of every WebGL
	 * call, and will popup an error dialog if a WebGL call returned an error.
	 * The JavaScript then invokes the client-side debugger. This code is
	 * intended to test your application, and should not be used in production.
	 */
	public void enableClientErrorChecks(boolean enable) {
		this.pImpl_.enableClientErrorChecks(enable);
	}

	/**
	 * enable client-side error messages (read detailed doc!)
	 * <p>
	 * Calls {@link #enableClientErrorChecks(boolean enable)
	 * enableClientErrorChecks(true)}
	 */
	public final void enableClientErrorChecks() {
		enableClientErrorChecks(true);
	}

	/**
	 * Inject JavaScript into the current js-stream.
	 * <p>
	 * Careful: this method directly puts the given jsString into the JavaScript
	 * stream, whatever state it current has. For example, if called in
	 * initGL(), it will put the jsString into the client-side initGL() code.
	 */
	public void injectJS(final String jsString) {
		this.pImpl_.injectJS(jsString);
	}

	public void webglNotAvailable() {
		System.out.append("WebGL Not available in client!\n");
		this.webGlNotAvailable_ = true;
	}

	DomElementType getDomElementType() {
		if (((this.pImpl_) instanceof WClientGLWidget ? (WClientGLWidget) (this.pImpl_)
				: null) != null) {
			return DomElementType.DomElement_CANVAS;
		} else {
			return DomElementType.DomElement_IMG;
		}
	}

	protected DomElement createDomElement(WApplication app) {
		DomElement result = null;
		if (!(this.pImpl_ != null)) {
			result = DomElement.createNew(DomElementType.DomElement_DIV);
			result.addChild(this.alternative_.createSDomElement(app));
			this.webGlNotAvailable_ = true;
		} else {
			result = DomElement.createNew(this.getDomElementType());
			this.repaintGL(EnumSet.of(WGLWidget.ClientSideRenderer.PAINT_GL,
					WGLWidget.ClientSideRenderer.RESIZE_GL));
		}
		this.setId(result, app);
		this.updateDom(result, true);
		return result;
	}

	protected void getDomChanges(final List<DomElement> result, WApplication app) {
		super.getDomChanges(result, app);
	}

	void updateDom(final DomElement element, boolean all) {
		if (all || this.valueChanged_) {
			this.valueChanged_ = false;
		}
		if (this.webGlNotAvailable_) {
			return;
		}
		DomElement el = element;
		this.pImpl_.updateDom(el, all);
		super.updateDom(element, all);
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			if (!(this.pImpl_ != null)) {
				if (!EnumUtils.mask(this.renderOptions_,
						WGLWidget.RenderOption.ClientSideRendering).isEmpty()
						&& WApplication.getInstance().getEnvironment()
								.hasWebGL()) {
					this.pImpl_ = new WClientGLWidget(this);
				} else {
					if (!EnumUtils.mask(this.renderOptions_,
							WGLWidget.RenderOption.ServerSideRendering)
							.isEmpty()) {
						this.pImpl_ = new WServerGLWidget(this);
					} else {
						this.pImpl_ = null;
					}
				}
			}
			if (this.pImpl_ != null && !this.getWidth().isAuto()
					&& !this.getHeight().isAuto()) {
				this.layoutSizeChanged((int) this.getWidth().toPixels(),
						(int) this.getHeight().toPixels());
			}
			this.defineJavaScript();
		}
		if (this.pImpl_ != null) {
			this.pImpl_.render(this.getJsRef(), flags);
		}
		super.render(flags);
	}

	String renderRemoveJs(boolean recursive) {
		if (this.webGlNotAvailable_) {
			return this.alternative_.getWebWidget().renderRemoveJs(recursive);
		} else {
			return super.renderRemoveJs(recursive);
		}
	}

	protected void layoutSizeChanged(int width, int height) {
		this.pImpl_.layoutSizeChanged(width, height);
		this.repaintGL(EnumSet.of(WGLWidget.ClientSideRenderer.RESIZE_GL));
	}

	protected void setFormData(final WObject.FormData formData) {
		if (this.valueChanged_) {
			return;
		}
		String[] parVals = formData.values;
		if ((parVals.length == 0)) {
			return;
		}
		if (parVals[0].equals("undefined")) {
			return;
		}
		List<String> matrices = new ArrayList<String>();
		matrices = new ArrayList<String>(Arrays.asList(parVals[0].split(";")));
		for (int i = 0; i < matrices.size(); i++) {
			if (matrices.get(i).equals("")) {
				break;
			}
			List<String> idAndData = new ArrayList<String>();
			idAndData = new ArrayList<String>(Arrays.asList(matrices.get(i)
					.split(":")));
			int id = (int) StringUtils.asNumber(idAndData.get(0));
			int j = 0;
			for (j = 0; j < this.jsMatrixList_.size(); j++) {
				if (this.jsMatrixList_.get(j).id == id) {
					break;
				}
			}
			if (j == this.jsMatrixList_.size()) {
				for (j = 0; j < this.jsVectorList_.size(); j++) {
					if (this.jsVectorList_.get(j).id == id) {
						break;
					}
				}
				final List<Float> vec = this.jsVectorList_.get(j).serverSideCopy;
				List<String> mData = new ArrayList<String>();
				mData = new ArrayList<String>(Arrays.asList(idAndData.get(1)
						.split(",")));
				for (int i1 = 0; i1 < vec.size(); i1++) {
					if (mData.get(i1).equals("Infinity")) {
						vec.set(i1, Float.POSITIVE_INFINITY);
					} else {
						if (mData.get(i1).equals("-Infinity")) {
							vec.set(i1, -Float.POSITIVE_INFINITY);
						} else {
							vec.set(i1, Float.parseFloat(mData.get(i1)));
						}
					}
				}
			} else {
				final javax.vecmath.Matrix4f mat = this.jsMatrixList_.get(j).serverSideCopy;
				List<String> mData = new ArrayList<String>();
				mData = new ArrayList<String>(Arrays.asList(idAndData.get(1)
						.split(",")));
				for (int i1 = 0; i1 < 4; i1++) {
					for (int i2 = 0; i2 < 4; i2++) {
						mat.setElement(i2, i1,
								Float.parseFloat(mData.get(i1 * 4 + i2)));
					}
				}
			}
		}
	}

	protected void contextRestored() {
		this.restoringContext_ = true;
		this.pImpl_.restoreContext(this.getJsRef());
		this.repaintGL(EnumSet.of(WGLWidget.ClientSideRenderer.UPDATE_GL,
				WGLWidget.ClientSideRenderer.RESIZE_GL,
				WGLWidget.ClientSideRenderer.PAINT_GL));
		this.restoringContext_ = false;
	}

	EnumSet<WGLWidget.RenderOption> renderOptions_;
	private WAbstractGLImplementation pImpl_;

	static class jsMatrixMap {
		private static Logger logger = LoggerFactory
				.getLogger(jsMatrixMap.class);

		public int id;
		public javax.vecmath.Matrix4f serverSideCopy;

		public jsMatrixMap(int matId, final javax.vecmath.Matrix4f ssCopy) {
			this.id = matId;
			this.serverSideCopy = ssCopy;
		}
	}

	static class jsVectorMap {
		private static Logger logger = LoggerFactory
				.getLogger(jsVectorMap.class);

		public int id;
		public List<Float> serverSideCopy;

		public jsVectorMap(int vecId, final List<Float> ssCopy) {
			this.id = vecId;
			this.serverSideCopy = ssCopy;
		}
	}

	private List<WGLWidget.jsMatrixMap> jsMatrixList_;
	private List<WGLWidget.jsVectorMap> jsVectorList_;
	private int jsValues_;
	private StringWriter js_;
	private JSignal repaintSignal_;

	private String getGlObjJsRef() {
		return "(function(){var r = "
				+ this.getJsRef()
				+ ";var o = r ? jQuery.data(r,'obj') : null;return o ? o : {ctx: null};})()";
	}

	private WWidget alternative_;
	private JSignal webglNotAvailable_;
	private boolean webGlNotAvailable_;
	private JSignal contextRestored_;
	private boolean restoringContext_;
	private boolean valueChanged_;
	private JSlot mouseWentDownSlot_;
	private JSlot mouseWentUpSlot_;
	private JSlot mouseDraggedSlot_;
	private JSlot mouseMovedSlot_;
	private JSlot mouseWheelSlot_;
	private JSlot touchStarted_;
	private JSlot touchEnded_;
	private JSlot touchMoved_;
	private JSlot repaintSlot_;

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtGlMatrix.js", wtjs2());
		app.loadJavaScript("js/WGLWidget.js", wtjs1());
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WGLWidget",
				"function(E,h){function F(){var a=jQuery.data(h,\"obj\"),d=\"\";for(var l in a.jsValues)if(a.jsValues.hasOwnProperty(l)){d+=l+\":\";for(var o=0;o<a.jsValues[l].length;o++){d+=a.jsValues[l][o];d+=o!==a.jsValues[l].length-1?\",\":\";\"}}return d}jQuery.data(h,\"obj\",this);var w=this,g=E.WT,q=g.glMatrix.vec3,j=g.glMatrix.mat4;this.ctx=null;this.initializeGL=function(){};this.paintGL=function(){};this.resizeGL=function(){};this.updates=[];this.initialized= false;this.preloadingBuffers=this.preloadingTextures=0;this.jsValues={};this.discoverContext=function(a,d){if(h.getContext){try{this.ctx=h.getContext(\"webgl\",{antialias:d})}catch(l){}if(this.ctx===null)try{this.ctx=h.getContext(\"experimental-webgl\",{antialias:d})}catch(o){}if(this.ctx===null){h.parentNode.insertBefore(h.firstChild,h);h.style.display=\"none\";a()}}return this.ctx};if(h.addEventListener){h.addEventListener(\"webglcontextlost\",function(a){a.preventDefault();w.initialized=false},false); h.addEventListener(\"webglcontextrestored\",function(){Wt.emit(h,\"contextRestored\")},false)}var e=null;this.setMouseHandler=function(a){e=a;e.setTarget&&e.setTarget(this)};this.LookAtMouseHandler=function(a,d,l,o,x){function y(b){b=Math.pow(1.2,b);j.translate(f,i);j.scale(f,[b,b,b]);q.negate(i);j.translate(f,i);q.negate(i);w.paintGL()}function z(b){var c=f[5]/q.length([f[1],f[5],f[9]]),m=f[6]/q.length([f[2],f[6],f[10]]);c=Math.atan2(m,c);m=b.x-p.x;var v=b.y-p.y,A=q.create();A[0]=f[0];A[1]=f[4];A[2]= f[8];var r=j.create();j.identity(r);j.translate(r,i);v=v*s;if(Math.abs(c+v)>=Math.PI/2)v=(c>0?1:-1)*Math.PI/2-c;j.rotate(r,v,A);j.rotate(r,m*G,k);q.negate(i);j.translate(r,i);q.negate(i);j.multiply(f,r,f);w.paintGL();p=b}var f=a,i=d,k=l,s=o,G=x,C=null,t=null,u=null,p=null,D=this;this.mouseDown=function(b,c){g.capture(null);g.capture(h);p=g.pageCoordinates(c)};this.mouseUp=function(){if(p!==null)p=null};this.mouseDrag=function(b,c){if(p!==null){b=g.pageCoordinates(c);g.buttons===1&&z(b)}};this.mouseWheel= function(b,c){g.cancelEvent(c);b=g.wheelDelta(c);y(b)};this.touchStart=function(b,c){t=c.touches.length===1?true:false;u=c.touches.length===2?true:false;if(t){g.capture(null);g.capture(h);p=g.pageCoordinates(c.touches[0])}else if(u){b=g.pageCoordinates(c.touches[0]);var m=g.pageCoordinates(c.touches[1]);C=Math.sqrt((b.x-m.x)*(b.x-m.x)+(b.y-m.y)*(b.y-m.y))}else return;c.preventDefault()};this.touchEnd=function(b,c){var m=c.touches.length===0?true:false;t=c.touches.length===1?true:false;u=c.touches.length=== 2?true:false;m&&D.mouseUp(null,null);if(t||u)D.touchStart(b,c)};this.touchMoved=function(b,c){if(t||u){c.preventDefault();if(t){if(p===null)return;b=g.pageCoordinates(c);z(b)}if(u){b=g.pageCoordinates(c.touches[0]);c=g.pageCoordinates(c.touches[1]);c=Math.sqrt((b.x-c.x)*(b.x-c.x)+(b.y-c.y)*(b.y-c.y));b=c/C;if(!(Math.abs(b-1)<0.05)){b=b>1?1:-1;C=c;y(b)}}}}};this.WalkMouseHandler=function(a,d,l){function o(i){var k=i.x-f.x;i=i.y-f.y;var s=j.create();j.identity(s);j.rotateY(s,k*z);k=q.create();k[0]= 0;k[1]=0;k[2]=-y*i;j.translate(s,k);j.multiply(s,x,x);w.paintGL();f=g.pageCoordinates(event)}var x=a,y=d,z=l,f=null;this.mouseDown=function(i,k){g.capture(null);g.capture(h);f=g.pageCoordinates(k)};this.mouseUp=function(){if(f!==null)f=null};this.mouseDrag=function(i,k){if(f!==null){i=g.pageCoordinates(k);o(i)}}};this.mouseDrag=function(a,d){if((this.initialized||!this.ctx)&&e&&e.mouseDrag)e.mouseDrag(a,d)};this.mouseMove=function(a,d){if((this.initialized||!this.ctx)&&e&&e.mouseMove)e.mouseMove(a, d)};this.mouseDown=function(a,d){if((this.initialized||!this.ctx)&&e&&e.mouseDown)e.mouseDown(a,d)};this.mouseUp=function(a,d){if((this.initialized||!this.ctx)&&e&&e.mouseUp)e.mouseUp(a,d)};this.mouseWheel=function(a,d){if((this.initialized||!this.ctx)&&e&&e.mouseWheel)e.mouseWheel(a,d)};this.touchStart=function(a,d){if((this.initialized||!this.ctx)&&e&&e.touchStart)e.touchStart(a,d)};this.touchEnd=function(a,d){if((this.initialized||!this.ctx)&&e&&e.touchEnd)e.touchEnd(a,d)};this.touchMoved=function(a, d){if((this.initialized||!this.ctx)&&e&&e.touchMoved)e.touchMoved(a,d)};this.handlePreload=function(){if(this.preloadingTextures===0&&this.preloadingBuffers===0){if(this.initialized){var a;for(a in this.updates)this.updates[a]();this.updates=[]}else this.initializeGL();this.resizeGL();this.paintGL()}};h.wtEncodeValue=F;var B=null,n=new Image;n.busy=false;n.onload=function(){h.src=n.src;if(B!=null)n.src=B;else n.busy=false;B=null};n.onerror=n.onload;this.loadImage=function(a){if(n.busy)B=a;else{n.src= a;n.busy=true}}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptObject,
				"glMatrix",
				"function(){glMatrixArrayType=typeof Float32Array!=\"undefined\"?Float32Array:typeof WebGLFloatArray!=\"undefined\"?WebGLFloatArray:Array;var D={};D.create=function(a){var b=new glMatrixArrayType(3);if(a){b[0]=a[0];b[1]=a[1];b[2]=a[2]}return b};D.set=function(a,b){b[0]=a[0];b[1]=a[1];b[2]=a[2];return b};D.add=function(a,b,c){if(!c||a==c){a[0]+=b[0];a[1]+=b[1];a[2]+=b[2];return a}c[0]=a[0]+b[0];c[1]=a[1]+b[1];c[2]=a[2]+b[2];return c};D.subtract=function(a, b,c){if(!c||a==c){a[0]-=b[0];a[1]-=b[1];a[2]-=b[2];return a}c[0]=a[0]-b[0];c[1]=a[1]-b[1];c[2]=a[2]-b[2];return c};D.negate=function(a,b){b||(b=a);b[0]=-a[0];b[1]=-a[1];b[2]=-a[2];return b};D.scale=function(a,b,c){if(!c||a==c){a[0]*=b;a[1]*=b;a[2]*=b;return a}c[0]=a[0]*b;c[1]=a[1]*b;c[2]=a[2]*b;return c};D.normalize=function(a,b){b||(b=a);var c=a[0],d=a[1];a=a[2];var e=Math.sqrt(c*c+d*d+a*a);if(e){if(e==1){b[0]=c;b[1]=d;b[2]=a;return b}}else{b[0]=0;b[1]=0;b[2]=0;return b}e=1/e;b[0]=c*e;b[1]=d*e;b[2]= a*e;return b};D.cross=function(a,b,c){c||(c=a);var d=a[0],e=a[1];a=a[2];var g=b[0],f=b[1];b=b[2];c[0]=e*b-a*f;c[1]=a*g-d*b;c[2]=d*f-e*g;return c};D.length=function(a){var b=a[0],c=a[1];a=a[2];return Math.sqrt(b*b+c*c+a*a)};D.dot=function(a,b){return a[0]*b[0]+a[1]*b[1]+a[2]*b[2]};D.direction=function(a,b,c){c||(c=a);var d=a[0]-b[0],e=a[1]-b[1];a=a[2]-b[2];b=Math.sqrt(d*d+e*e+a*a);if(!b){c[0]=0;c[1]=0;c[2]=0;return c}b=1/b;c[0]=d*b;c[1]=e*b;c[2]=a*b;return c};D.str=function(a){return\"[\"+a[0]+\", \"+ a[1]+\", \"+a[2]+\"]\"};var H={};H.create=function(a){var b=new glMatrixArrayType(9);if(a){b[0]=a[0];b[1]=a[1];b[2]=a[2];b[3]=a[3];b[4]=a[4];b[5]=a[5];b[6]=a[6];b[7]=a[7];b[8]=a[8];b[9]=a[9]}return b};H.set=function(a,b){b[0]=a[0];b[1]=a[1];b[2]=a[2];b[3]=a[3];b[4]=a[4];b[5]=a[5];b[6]=a[6];b[7]=a[7];b[8]=a[8];return b};H.identity=function(a){a[0]=1;a[1]=0;a[2]=0;a[3]=0;a[4]=1;a[5]=0;a[6]=0;a[7]=0;a[8]=1;return a};H.toMat4=function(a,b){b||(b=o.create());b[0]=a[0];b[1]=a[1];b[2]=a[2];b[3]=0;b[4]=a[3]; b[5]=a[4];b[6]=a[5];b[7]=0;b[8]=a[6];b[9]=a[7];b[10]=a[8];b[11]=0;b[12]=0;b[13]=0;b[14]=0;b[15]=1;return b};H.str=function(a){return\"[\"+a[0]+\", \"+a[1]+\", \"+a[2]+\", \"+a[3]+\", \"+a[4]+\", \"+a[5]+\", \"+a[6]+\", \"+a[7]+\", \"+a[8]+\"]\"};var o={};o.create=function(a){var b=new glMatrixArrayType(16);if(a){b[0]=a[0];b[1]=a[1];b[2]=a[2];b[3]=a[3];b[4]=a[4];b[5]=a[5];b[6]=a[6];b[7]=a[7];b[8]=a[8];b[9]=a[9];b[10]=a[10];b[11]=a[11];b[12]=a[12];b[13]=a[13];b[14]=a[14];b[15]=a[15]}return b};o.set=function(a,b){b[0]= a[0];b[1]=a[1];b[2]=a[2];b[3]=a[3];b[4]=a[4];b[5]=a[5];b[6]=a[6];b[7]=a[7];b[8]=a[8];b[9]=a[9];b[10]=a[10];b[11]=a[11];b[12]=a[12];b[13]=a[13];b[14]=a[14];b[15]=a[15];return b};o.identity=function(a){a[0]=1;a[1]=0;a[2]=0;a[3]=0;a[4]=0;a[5]=1;a[6]=0;a[7]=0;a[8]=0;a[9]=0;a[10]=1;a[11]=0;a[12]=0;a[13]=0;a[14]=0;a[15]=1;return a};o.transpose=function(a,b){if(!b||a==b){b=a[1];var c=a[2],d=a[3],e=a[6],g=a[7],f=a[11];a[1]=a[4];a[2]=a[8];a[3]=a[12];a[4]=b;a[6]=a[9];a[7]=a[13];a[8]=c;a[9]=e;a[11]=a[14];a[12]= d;a[13]=g;a[14]=f;return a}b[0]=a[0];b[1]=a[4];b[2]=a[8];b[3]=a[12];b[4]=a[1];b[5]=a[5];b[6]=a[9];b[7]=a[13];b[8]=a[2];b[9]=a[6];b[10]=a[10];b[11]=a[14];b[12]=a[3];b[13]=a[7];b[14]=a[11];b[15]=a[15];return b};o.determinant=function(a){var b=a[0],c=a[1],d=a[2],e=a[3],g=a[4],f=a[5],h=a[6],i=a[7],j=a[8],k=a[9],m=a[10],n=a[11],l=a[12],p=a[13],q=a[14];a=a[15];return l*k*h*e-j*p*h*e-l*f*m*e+g*p*m*e+j*f*q*e-g*k*q*e-l*k*d*i+j*p*d*i+l*c*m*i-b*p*m*i-j*c*q*i+b*k*q*i+l*f*d*n-g*p*d*n-l*c*h*n+b*p*h*n+g*c*q*n-b* f*q*n-j*f*d*a+g*k*d*a+j*c*h*a-b*k*h*a-g*c*m*a+b*f*m*a};o.inverse=function(a,b){b||(b=a);var c=a[0],d=a[1],e=a[2],g=a[3],f=a[4],h=a[5],i=a[6],j=a[7],k=a[8],m=a[9],n=a[10],l=a[11],p=a[12],q=a[13],s=a[14];a=a[15];var A=c*h-d*f,B=c*i-e*f,C=c*j-g*f,t=d*i-e*h,u=d*j-g*h,v=e*j-g*i,w=k*q-m*p,x=k*s-n*p,y=k*a-l*p,z=m*s-n*q,F=m*a-l*q,G=n*a-l*s,r=1/(A*G-B*F+C*z+t*y-u*x+v*w);b[0]=(h*G-i*F+j*z)*r;b[1]=(-d*G+e*F-g*z)*r;b[2]=(q*v-s*u+a*t)*r;b[3]=(-m*v+n*u-l*t)*r;b[4]=(-f*G+i*y-j*x)*r;b[5]=(c*G-e*y+g*x)*r;b[6]=(-p* v+s*C-a*B)*r;b[7]=(k*v-n*C+l*B)*r;b[8]=(f*F-h*y+j*w)*r;b[9]=(-c*F+d*y-g*w)*r;b[10]=(p*u-q*C+a*A)*r;b[11]=(-k*u+m*C-l*A)*r;b[12]=(-f*z+h*x-i*w)*r;b[13]=(c*z-d*x+e*w)*r;b[14]=(-p*t+q*B-s*A)*r;b[15]=(k*t-m*B+n*A)*r;return b};o.toRotationMat=function(a,b){b||(b=o.create());b[0]=a[0];b[1]=a[1];b[2]=a[2];b[3]=a[3];b[4]=a[4];b[5]=a[5];b[6]=a[6];b[7]=a[7];b[8]=a[8];b[9]=a[9];b[10]=a[10];b[11]=a[11];b[12]=0;b[13]=0;b[14]=0;b[15]=1;return b};o.toMat3=function(a,b){b||(b=H.create());b[0]=a[0];b[1]=a[1];b[2]= a[2];b[3]=a[4];b[4]=a[5];b[5]=a[6];b[6]=a[8];b[7]=a[9];b[8]=a[10];return b};o.toInverseMat3=function(a,b){var c=a[0],d=a[1],e=a[2],g=a[4],f=a[5],h=a[6],i=a[8],j=a[9];a=a[10];var k=a*f-h*j,m=-a*g+h*i,n=j*g-f*i,l=c*k+d*m+e*n;if(!l)return null;l=1/l;b||(b=H.create());b[0]=k*l;b[1]=(-a*d+e*j)*l;b[2]=(h*d-e*f)*l;b[3]=m*l;b[4]=(a*c-e*i)*l;b[5]=(-h*c+e*g)*l;b[6]=n*l;b[7]=(-j*c+d*i)*l;b[8]=(f*c-d*g)*l;return b};o.multiply=function(a,b,c){c||(c=a);var d=a[0],e=a[1],g=a[2],f=a[3],h=a[4],i=a[5],j=a[6],k=a[7], m=a[8],n=a[9],l=a[10],p=a[11],q=a[12],s=a[13],A=a[14];a=a[15];var B=b[0],C=b[1],t=b[2],u=b[3],v=b[4],w=b[5],x=b[6],y=b[7],z=b[8],F=b[9],G=b[10],r=b[11],I=b[12],J=b[13],K=b[14];b=b[15];c[0]=B*d+C*h+t*m+u*q;c[1]=B*e+C*i+t*n+u*s;c[2]=B*g+C*j+t*l+u*A;c[3]=B*f+C*k+t*p+u*a;c[4]=v*d+w*h+x*m+y*q;c[5]=v*e+w*i+x*n+y*s;c[6]=v*g+w*j+x*l+y*A;c[7]=v*f+w*k+x*p+y*a;c[8]=z*d+F*h+G*m+r*q;c[9]=z*e+F*i+G*n+r*s;c[10]=z*g+F*j+G*l+r*A;c[11]=z*f+F*k+G*p+r*a;c[12]=I*d+J*h+K*m+b*q;c[13]=I*e+J*i+K*n+b*s;c[14]=I*g+J*j+K*l+b* A;c[15]=I*f+J*k+K*p+b*a;return c};o.multiplyVec3=function(a,b,c){c||(c=b);var d=b[0],e=b[1];b=b[2];c[0]=a[0]*d+a[4]*e+a[8]*b+a[12];c[1]=a[1]*d+a[5]*e+a[9]*b+a[13];c[2]=a[2]*d+a[6]*e+a[10]*b+a[14];return c};o.multiplyVec4=function(a,b,c){c||(c=b);var d=b[0],e=b[1],g=b[2];b=b[3];c[0]=a[0]*d+a[4]*e+a[8]*g+a[12]*b;c[1]=a[1]*d+a[5]*e+a[9]*g+a[13]*b;c[2]=a[2]*d+a[6]*e+a[10]*g+a[14]*b;c[3]=a[3]*d+a[7]*e+a[11]*g+a[15]*b;return c};o.translate=function(a,b,c){var d=b[0],e=b[1];b=b[2];if(!c||a==c){a[12]=a[0]* d+a[4]*e+a[8]*b+a[12];a[13]=a[1]*d+a[5]*e+a[9]*b+a[13];a[14]=a[2]*d+a[6]*e+a[10]*b+a[14];a[15]=a[3]*d+a[7]*e+a[11]*b+a[15];return a}var g=a[0],f=a[1],h=a[2],i=a[3],j=a[4],k=a[5],m=a[6],n=a[7],l=a[8],p=a[9],q=a[10],s=a[11];c[0]=g;c[1]=f;c[2]=h;c[3]=i;c[4]=j;c[5]=k;c[6]=m;c[7]=n;c[8]=l;c[9]=p;c[10]=q;c[11]=s;c[12]=g*d+j*e+l*b+a[12];c[13]=f*d+k*e+p*b+a[13];c[14]=h*d+m*e+q*b+a[14];c[15]=i*d+n*e+s*b+a[15];return c};o.scale=function(a,b,c){var d=b[0],e=b[1];b=b[2];if(!c||a==c){a[0]*=d;a[1]*=d;a[2]*=d;a[3]*= d;a[4]*=e;a[5]*=e;a[6]*=e;a[7]*=e;a[8]*=b;a[9]*=b;a[10]*=b;a[11]*=b;return a}c[0]=a[0]*d;c[1]=a[1]*d;c[2]=a[2]*d;c[3]=a[3]*d;c[4]=a[4]*e;c[5]=a[5]*e;c[6]=a[6]*e;c[7]=a[7]*e;c[8]=a[8]*b;c[9]=a[9]*b;c[10]=a[10]*b;c[11]=a[11]*b;c[12]=a[12];c[13]=a[13];c[14]=a[14];c[15]=a[15];return c};o.rotate=function(a,b,c,d){var e=c[0],g=c[1];c=c[2];var f=Math.sqrt(e*e+g*g+c*c);if(!f)return null;if(f!=1){f=1/f;e*=f;g*=f;c*=f}var h=Math.sin(b),i=Math.cos(b),j=1-i;b=a[0];f=a[1];var k=a[2],m=a[3],n=a[4],l=a[5],p=a[6], q=a[7],s=a[8],A=a[9],B=a[10],C=a[11],t=e*e*j+i,u=g*e*j+c*h,v=c*e*j-g*h,w=e*g*j-c*h,x=g*g*j+i,y=c*g*j+e*h,z=e*c*j+g*h;e=g*c*j-e*h;g=c*c*j+i;if(d){if(a!=d){d[12]=a[12];d[13]=a[13];d[14]=a[14];d[15]=a[15]}}else d=a;d[0]=b*t+n*u+s*v;d[1]=f*t+l*u+A*v;d[2]=k*t+p*u+B*v;d[3]=m*t+q*u+C*v;d[4]=b*w+n*x+s*y;d[5]=f*w+l*x+A*y;d[6]=k*w+p*x+B*y;d[7]=m*w+q*x+C*y;d[8]=b*z+n*e+s*g;d[9]=f*z+l*e+A*g;d[10]=k*z+p*e+B*g;d[11]=m*z+q*e+C*g;return d};o.rotateX=function(a,b,c){var d=Math.sin(b);b=Math.cos(b);var e=a[4],g=a[5], f=a[6],h=a[7],i=a[8],j=a[9],k=a[10],m=a[11];if(c){if(a!=c){c[0]=a[0];c[1]=a[1];c[2]=a[2];c[3]=a[3];c[12]=a[12];c[13]=a[13];c[14]=a[14];c[15]=a[15]}}else c=a;c[4]=e*b+i*d;c[5]=g*b+j*d;c[6]=f*b+k*d;c[7]=h*b+m*d;c[8]=e*-d+i*b;c[9]=g*-d+j*b;c[10]=f*-d+k*b;c[11]=h*-d+m*b;return c};o.rotateY=function(a,b,c){var d=Math.sin(b);b=Math.cos(b);var e=a[0],g=a[1],f=a[2],h=a[3],i=a[8],j=a[9],k=a[10],m=a[11];if(c){if(a!=c){c[4]=a[4];c[5]=a[5];c[6]=a[6];c[7]=a[7];c[12]=a[12];c[13]=a[13];c[14]=a[14];c[15]=a[15]}}else c= a;c[0]=e*b+i*-d;c[1]=g*b+j*-d;c[2]=f*b+k*-d;c[3]=h*b+m*-d;c[8]=e*d+i*b;c[9]=g*d+j*b;c[10]=f*d+k*b;c[11]=h*d+m*b;return c};o.rotateZ=function(a,b,c){var d=Math.sin(b);b=Math.cos(b);var e=a[0],g=a[1],f=a[2],h=a[3],i=a[4],j=a[5],k=a[6],m=a[7];if(c){if(a!=c){c[8]=a[8];c[9]=a[9];c[10]=a[10];c[11]=a[11];c[12]=a[12];c[13]=a[13];c[14]=a[14];c[15]=a[15]}}else c=a;c[0]=e*b+i*d;c[1]=g*b+j*d;c[2]=f*b+k*d;c[3]=h*b+m*d;c[4]=e*-d+i*b;c[5]=g*-d+j*b;c[6]=f*-d+k*b;c[7]=h*-d+m*b;return c};o.frustum=function(a,b,c,d, e,g,f){f||(f=o.create());var h=b-a,i=d-c,j=g-e;f[0]=e*2/h;f[1]=0;f[2]=0;f[3]=0;f[4]=0;f[5]=e*2/i;f[6]=0;f[7]=0;f[8]=(b+a)/h;f[9]=(d+c)/i;f[10]=-(g+e)/j;f[11]=-1;f[12]=0;f[13]=0;f[14]=-(g*e*2)/j;f[15]=0;return f};o.perspective=function(a,b,c,d,e){a=c*Math.tan(a*Math.PI/360);b=a*b;return o.frustum(-b,b,-a,a,c,d,e)};o.ortho=function(a,b,c,d,e,g,f){f||(f=o.create());var h=b-a,i=d-c,j=g-e;f[0]=2/h;f[1]=0;f[2]=0;f[3]=0;f[4]=0;f[5]=2/i;f[6]=0;f[7]=0;f[8]=0;f[9]=0;f[10]=-2/j;f[11]=0;f[12]=-(a+b)/h;f[13]= -(d+c)/i;f[14]=-(g+e)/j;f[15]=1;return f};o.lookAt=function(a,b,c,d){d||(d=o.create());var e=a[0],g=a[1];a=a[2];var f=c[0],h=c[1],i=c[2];c=b[1];var j=b[2];if(e==b[0]&&g==c&&a==j)return o.identity(d);var k,m,n,l;c=e-b[0];j=g-b[1];b=a-b[2];l=1/Math.sqrt(c*c+j*j+b*b);c*=l;j*=l;b*=l;k=h*b-i*j;i=i*c-f*b;f=f*j-h*c;if(l=Math.sqrt(k*k+i*i+f*f)){l=1/l;k*=l;i*=l;f*=l}else f=i=k=0;h=j*f-b*i;m=b*k-c*f;n=c*i-j*k;if(l=Math.sqrt(h*h+m*m+n*n)){l=1/l;h*=l;m*=l;n*=l}else n=m=h=0;d[0]=k;d[1]=h;d[2]=c;d[3]=0;d[4]=i; d[5]=m;d[6]=j;d[7]=0;d[8]=f;d[9]=n;d[10]=b;d[11]=0;d[12]=-(k*e+i*g+f*a);d[13]=-(h*e+m*g+n*a);d[14]=-(c*e+j*g+b*a);d[15]=1;return d};o.str=function(a){return\"[\"+a[0]+\", \"+a[1]+\", \"+a[2]+\", \"+a[3]+\", \"+a[4]+\", \"+a[5]+\", \"+a[6]+\", \"+a[7]+\", \"+a[8]+\", \"+a[9]+\", \"+a[10]+\", \"+a[11]+\", \"+a[12]+\", \"+a[13]+\", \"+a[14]+\", \"+a[15]+\"]\"};var E={};E.create=function(a){var b=new glMatrixArrayType(4);if(a){b[0]=a[0];b[1]=a[1];b[2]=a[2];b[3]=a[3]}return b};E.set=function(a,b){b[0]=a[0];b[1]=a[1];b[2]=a[2];b[3]=a[3]; return b};E.calculateW=function(a,b){var c=a[0],d=a[1],e=a[2];if(!b||a==b){a[3]=-Math.sqrt(Math.abs(1-c*c-d*d-e*e));return a}b[0]=c;b[1]=d;b[2]=e;b[3]=-Math.sqrt(Math.abs(1-c*c-d*d-e*e));return b};E.inverse=function(a,b){if(!b||a==b){a[0]*=1;a[1]*=1;a[2]*=1;return a}b[0]=-a[0];b[1]=-a[1];b[2]=-a[2];b[3]=a[3];return b};E.length=function(a){var b=a[0],c=a[1],d=a[2];a=a[3];return Math.sqrt(b*b+c*c+d*d+a*a)};E.normalize=function(a,b){b||(b=a);var c=a[0],d=a[1],e=a[2];a=a[3];var g=Math.sqrt(c*c+d*d+e* e+a*a);if(g==0){b[0]=0;b[1]=0;b[2]=0;b[3]=0;return b}g=1/g;b[0]=c*g;b[1]=d*g;b[2]=e*g;b[3]=a*g;return b};E.multiply=function(a,b,c){c||(c=a);var d=a[0],e=a[1],g=a[2];a=a[3];var f=b[0],h=b[1],i=b[2];b=b[3];c[0]=d*b+a*f+e*i-g*h;c[1]=e*b+a*h+g*f-d*i;c[2]=g*b+a*i+d*h-e*f;c[3]=a*b-d*f-e*h-g*i;return c};E.multiplyVec3=function(a,b,c){c||(c=b);var d=b[0],e=b[1],g=b[2];b=a[0];var f=a[1],h=a[2];a=a[3];var i=a*d+f*g-h*e,j=a*e+h*d-b*g,k=a*g+b*e-f*d;d=-b*d-f*e-h*g;c[0]=i*a+d*-b+j*-h-k*-f;c[1]=j*a+d*-f+k*-b-i* -h;c[2]=k*a+d*-h+i*-f-j*-b;return c};E.toMat3=function(a,b){b||(b=H.create());var c=a[0],d=a[1],e=a[2],g=a[3],f=c+c,h=d+d,i=e+e;a=c*f;var j=c*h;c=c*i;var k=d*h;d=d*i;e=e*i;f=g*f;h=g*h;g=g*i;b[0]=1-(k+e);b[1]=j-g;b[2]=c+h;b[3]=j+g;b[4]=1-(a+e);b[5]=d-f;b[6]=c-h;b[7]=d+f;b[8]=1-(a+k);return b};E.toMat4=function(a,b){b||(b=o.create());var c=a[0],d=a[1],e=a[2],g=a[3],f=c+c,h=d+d,i=e+e;a=c*f;var j=c*h;c=c*i;var k=d*h;d=d*i;e=e*i;f=g*f;h=g*h;g=g*i;b[0]=1-(k+e);b[1]=j-g;b[2]=c+h;b[3]=0;b[4]=j+g;b[5]=1-(a+ e);b[6]=d-f;b[7]=0;b[8]=c-h;b[9]=d+f;b[10]=1-(a+k);b[11]=0;b[12]=0;b[13]=0;b[14]=0;b[15]=1;return b};E.str=function(a){return\"[\"+a[0]+\", \"+a[1]+\", \"+a[2]+\", \"+a[3]+\"]\"};return{vec3:D,mat3:H,mat4:o}}()");
	}
}
