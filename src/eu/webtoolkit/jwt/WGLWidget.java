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
 * functions documented below, {@link } records the JavaScript calls that have to
 * be invoked in the browser.
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
 * function. Through its execution, {@link } records what has to be done to
 * render a scene, and it is executed every time that the scene is to be
 * redrawn. You can use the VBO&apos;s and shaders prepared in the
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
			copy.jsRef_ = "Wt3_3_2.glMatrix.mat4.inverse(" + this.jsRef_
					+ ", Wt3_3_2.glMatrix.mat4.create())";
			copy.operations_.add(WGLWidget.JavaScriptMatrix4x4.op.INVERT);
			return copy;
		}

		public WGLWidget.JavaScriptMatrix4x4 transposed() {
			if (!this.isInitialized()) {
				throw new WException(
						"JavaScriptMatrix4x4: matrix not initialized");
			}
			WGLWidget.JavaScriptMatrix4x4 copy = this.clone();
			copy.jsRef_ = "Wt3_3_2.glMatrix.mat4.transpose(" + this.jsRef_
					+ ", Wt3_3_2.glMatrix.mat4.create())";
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
			ss.append("Wt3_3_2.glMatrix.mat4.multiply(").append(this.jsRef_)
					.append(",");
			javax.vecmath.Matrix4f t = WebGLUtils.transpose(m);
			WebGLUtils.renderfv(ss, t, this.arrayType_);
			ss.append(", Wt3_3_2.glMatrix.mat4.create())");
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
			copy.arrayType_ = this.arrayType_;
			copy.initialized_ = this.initialized_;
			Utils.copyList(this.operations_, copy.operations_);
			Utils.copyList(this.matrices_, copy.matrices_);
			return copy;
		}

		private void assignToContext(int id, JsArrayType type, WGLWidget context) {
			this.id_ = id;
			this.jsRef_ = context.getGlObjJsRef() + ".jsValues["
					+ String.valueOf(this.id_) + "]";
			this.context_ = context;
			this.arrayType_ = type;
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
		private JsArrayType arrayType_;

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
	public void initializeGL() {
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
	public void resizeGL(int width, int height) {
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
	public void paintGL() {
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
	public void updateGL() {
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
			this.layoutSizeChanged((int) width.getValue(), (int) height
					.getValue());
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
	 * {@link WCanvasPaintDevice}. If server-side rendering is used as fallback
	 * then this function returns a {@link WRasterImage}
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteFramebuffers.xml"
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteRenderbuffers.xml"
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDisableVertexAttribArray.xml"
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glEnableVertexAttribArray.xml"
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFramebufferRenderbuffer.xml"
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFramebufferTexture2D.xml"
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGetUniformLocation.xml"
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glRenderbufferStorage.xml"
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilFuncSeparate.xml"
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilMaskSeparate.xml"
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
	 * <a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttribPointer.xml"
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
		mat.assignToContext(this.jsValues_++, JsArrayType.Float32Array, this);
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
	 * {@code
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
	 * {@code
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
	 * If {@link } cannot create a working WebGL context, this content will be
	 * shown to the user. This may be a text explanation, or a pre-rendered
	 * image, or a video, a flash movie, ...
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

	DomElement createDomElement(WApplication app) {
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

	void getDomChanges(final List<DomElement> result, WApplication app) {
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
								.isWebGL()) {
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

	String renderRemoveJs() {
		if (this.webGlNotAvailable_) {
			return this.alternative_.getWebWidget().renderRemoveJs();
		} else {
			return super.renderRemoveJs();
		}
	}

	protected void layoutSizeChanged(int width, int height) {
		this.pImpl_.layoutSizeChanged(width, height);
		this.repaintGL(EnumSet.of(WGLWidget.ClientSideRenderer.RESIZE_GL));
	}

	void setFormData(final WObject.FormData formData) {
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
						mat.setElement(i2, i1, Float.parseFloat(mData.get(i1
								* 4 + i2)));
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
				"function(j,d){jQuery.data(d,\"obj\",this);var k=this;var c=j.WT;var f=c.glMatrix.vec3;var b=c.glMatrix.mat3;var a=c.glMatrix.mat4;this.ctx=null;this.initializeGL=function(){};this.paintGL=function(){};this.resizeGL=function(){};this.updates=new Array();this.initialized=false;this.preloadingTextures=0;this.preloadingBuffers=0;this.jsValues=[];this.discoverContext=function(o,n){if(d.getContext){try{this.ctx=d.getContext(\"webgl\",{antialias:n})}catch(p){}if(this.ctx===null){try{this.ctx=d.getContext(\"experimental-webgl\")}catch(p){}}if(this.ctx===null){var m=d.firstChild;var l=d.parentNode;l.insertBefore(m,d);d.style.display=\"none\";o()}}return this.ctx};if(d.addEventListener){d.addEventListener(\"webglcontextlost\",function(l){l.preventDefault();k.initialized=false},false);d.addEventListener(\"webglcontextrestored\",function(l){Wt.emit(d,\"contextRestored\")},false)}var h=null;this.setMouseHandler=function(l){h=l;if(h.setTarget){h.setTarget(this)}};this.LookAtMouseHandler=function(v,m,s,y,l){var x=v;var B=m;var u=s;var p=y;var w=l;var n=null;var A=null;var q=null;var o=null;var t=null;this.setTarget=function(C){t=C};this.mouseDown=function(D,C){c.capture(null);c.capture(d);o=c.pageCoordinates(C)};this.mouseUp=function(D,C){if(o!==null){o=null}};this.mouseDrag=function(D,C){if(o===null){return}var E=c.pageCoordinates(C);if(c.buttons===1){r(E)}};this.mouseWheel=function(E,C){c.cancelEvent(C);var D=c.wheelDelta(C);z(D)};function z(D){var C=Math.pow(1.2,D);a.translate(x,B);a.scale(x,[C,C,C]);f.negate(B);a.translate(x,B);f.negate(B);t.paintGL()}function r(H){var E=x[5]/f.length([x[1],x[5],x[9]]);var J=x[6]/f.length([x[2],x[6],x[10]]);var G=Math.atan2(J,E);var L=(H.x-o.x);var I=(H.y-o.y);var K=f.create();K[0]=x[0];K[1]=x[4];K[2]=x[8];var C=a.create();a.identity(C);a.translate(C,B);var F=I*p;if(Math.abs(G+F)>=Math.PI/2){var D=G>0?1:-1;F=D*Math.PI/2-G}a.rotate(C,F,K);a.rotate(C,L*w,u);f.negate(B);a.translate(C,B);f.negate(B);a.multiply(x,C,x);t.paintGL();o=H}this.touchStart=function(F,E){A=E.touches.length===1?true:false;q=E.touches.length===2?true:false;if(A){c.capture(null);c.capture(d);o=c.pageCoordinates(E.touches[0])}else{if(q){var D=c.pageCoordinates(E.touches[0]);var C=c.pageCoordinates(E.touches[1]);n=Math.sqrt((D.x-C.x)*(D.x-C.x)+(D.y-C.y)*(D.y-C.y))}else{return}}E.preventDefault()};this.touchEnd=function(E,C){var D=C.touches.length===0?true:false;A=C.touches.length===1?true:false;q=C.touches.length===2?true:false;if(D){this.mouseUp(null,null)}if(A||q){this.touchStart(E,C)}};this.touchMoved=function(H,E){if((!A)&&(!q)){return}E.preventDefault();if(A){this.mouseDrag(H,E.touches[0])}if(q){var D=c.pageCoordinates(E.touches[0]);var C=c.pageCoordinates(E.touches[1]);var G=Math.sqrt((D.x-C.x)*(D.x-C.x)+(D.y-C.y)*(D.y-C.y));var F=G/n;if(Math.abs(F-1)<0.05){return}else{if(F>1){F=1}else{F=-1}}n=G;z(F)}}};this.WalkMouseHandler=function(p,o,l){var r=p;var t=o;var s=l;var m=null;var n=null;this.setTarget=function(u){n=u};this.mouseDown=function(v,u){c.capture(null);c.capture(d);m=c.pageCoordinates(u)};this.mouseUp=function(v,u){if(m!==null){m=null}};this.mouseDrag=function(v,u){if(m===null){return}var w=c.pageCoordinates(u);q(w)};function q(y){var v=(y.x-m.x);var u=(y.y-m.y);var x=a.create();a.identity(x);a.rotateY(x,v*s);var w=f.create();w[0]=0;w[1]=0;w[2]=-t*u;a.translate(x,w);a.multiply(x,r,r);n.paintGL();m=c.pageCoordinates(event)}};this.mouseDrag=function(m,l){if((this.initialized||!this.ctx)&&h&&h.mouseDrag){h.mouseDrag(m,l)}};this.mouseDown=function(m,l){if((this.initialized||!this.ctx)&&h&&h.mouseDown){h.mouseDown(m,l)}};this.mouseUp=function(m,l){if((this.initialized||!this.ctx)&&h&&h.mouseUp){h.mouseUp(m,l)}};this.mouseWheel=function(m,l){if((this.initialized||!this.ctx)&&h.mouseWheel){h.mouseWheel(m,l)}};this.touchStart=function(m,l){if((this.initialized||!this.ctx)&&h.touchStart){h.touchStart(m,l)}};this.touchEnd=function(m,l){if((this.initialized||!this.ctx)&&mouseWheel.touchEnd){mouseWheel.touchEnd(m,l)}};this.touchMoved=function(m,l){if((this.initialized||!this.ctx)&&mouseWheel.touchMoved){mouseWheel.touchMoved(m,l)}};this.handlePreload=function(){if(this.preloadingTextures===0&&this.preloadingBuffers===0){if(this.initialized){var l;for(l in this.updates){this.updates[l]()}this.updates=new Array();this.resizeGL();this.paintGL()}else{this.initializeGL();this.resizeGL();this.paintGL()}}else{}};function g(){var n=jQuery.data(d,\"obj\");var o=\"\";for(var l=0;l<n.jsValues.length;l++){o+=l+\":\";for(var m=0;m<n.jsValues[l].length;m++){o+=n.jsValues[l][m];if(m!==n.jsValues[l].length-1){o+=\",\"}else{o+=\";\"}}}return o}d.wtEncodeValue=g;var e=null;var i=new Image();i.busy=false;i.onload=function(){d.src=i.src;if(e!=null){i.src=e}else{i.busy=false}e=null};i.onerror=i.onload;this.loadImage=function(l){if(i.busy){e=l}else{i.src=l;i.busy=true}}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptObject,
				"glMatrix",
				"(function(){if(typeof Float32Array!=\"undefined\"){glMatrixArrayType=Float32Array}else{if(typeof WebGLFloatArray!=\"undefined\"){glMatrixArrayType=WebGLFloatArray}else{glMatrixArrayType=Array}}var c={};c.create=function(f){var e=new glMatrixArrayType(3);if(f){e[0]=f[0];e[1]=f[1];e[2]=f[2]}return e};c.set=function(f,e){e[0]=f[0];e[1]=f[1];e[2]=f[2];return e};c.add=function(f,g,e){if(!e||f==e){f[0]+=g[0];f[1]+=g[1];f[2]+=g[2];return f}e[0]=f[0]+g[0];e[1]=f[1]+g[1];e[2]=f[2]+g[2];return e};c.subtract=function(f,g,e){if(!e||f==e){f[0]-=g[0];f[1]-=g[1];f[2]-=g[2];return f}e[0]=f[0]-g[0];e[1]=f[1]-g[1];e[2]=f[2]-g[2];return e};c.negate=function(f,e){if(!e){e=f}e[0]=-f[0];e[1]=-f[1];e[2]=-f[2];return e};c.scale=function(f,g,e){if(!e||f==e){f[0]*=g;f[1]*=g;f[2]*=g;return f}e[0]=f[0]*g;e[1]=f[1]*g;e[2]=f[2]*g;return e};c.normalize=function(h,g){if(!g){g=h}var f=h[0],j=h[1],i=h[2];var e=Math.sqrt(f*f+j*j+i*i);if(!e){g[0]=0;g[1]=0;g[2]=0;return g}else{if(e==1){g[0]=f;g[1]=j;g[2]=i;return g}}e=1/e;g[0]=f*e;g[1]=j*e;g[2]=i*e;return g};c.cross=function(f,h,m){if(!m){m=f}var l=f[0],j=f[1],i=f[2];var e=h[0],k=h[1],g=h[2];m[0]=j*g-i*k;m[1]=i*e-l*g;m[2]=l*k-j*e;return m};c.length=function(f){var e=f[0],h=f[1],g=f[2];return Math.sqrt(e*e+h*h+g*g)};c.dot=function(e,f){return e[0]*f[0]+e[1]*f[1]+e[2]*f[2]};c.direction=function(h,i,g){if(!g){g=h}var f=h[0]-i[0];var k=h[1]-i[1];var j=h[2]-i[2];var e=Math.sqrt(f*f+k*k+j*j);if(!e){g[0]=0;g[1]=0;g[2]=0;return g}e=1/e;g[0]=f*e;g[1]=k*e;g[2]=j*e;return g};c.str=function(e){return\"[\"+e[0]+\", \"+e[1]+\", \"+e[2]+\"]\"};var b={};b.create=function(f){var e=new glMatrixArrayType(9);if(f){e[0]=f[0];e[1]=f[1];e[2]=f[2];e[3]=f[3];e[4]=f[4];e[5]=f[5];e[6]=f[6];e[7]=f[7];e[8]=f[8];e[9]=f[9]}return e};b.set=function(f,e){e[0]=f[0];e[1]=f[1];e[2]=f[2];e[3]=f[3];e[4]=f[4];e[5]=f[5];e[6]=f[6];e[7]=f[7];e[8]=f[8];return e};b.identity=function(e){e[0]=1;e[1]=0;e[2]=0;e[3]=0;e[4]=1;e[5]=0;e[6]=0;e[7]=0;e[8]=1;return e};b.toMat4=function(f,e){if(!e){e=a.create()}e[0]=f[0];e[1]=f[1];e[2]=f[2];e[3]=0;e[4]=f[3];e[5]=f[4];e[6]=f[5];e[7]=0;e[8]=f[6];e[9]=f[7];e[10]=f[8];e[11]=0;e[12]=0;e[13]=0;e[14]=0;e[15]=1;return e};b.str=function(e){return\"[\"+e[0]+\", \"+e[1]+\", \"+e[2]+\", \"+e[3]+\", \"+e[4]+\", \"+e[5]+\", \"+e[6]+\", \"+e[7]+\", \"+e[8]+\"]\"};var a={};a.create=function(f){var e=new glMatrixArrayType(16);if(f){e[0]=f[0];e[1]=f[1];e[2]=f[2];e[3]=f[3];e[4]=f[4];e[5]=f[5];e[6]=f[6];e[7]=f[7];e[8]=f[8];e[9]=f[9];e[10]=f[10];e[11]=f[11];e[12]=f[12];e[13]=f[13];e[14]=f[14];e[15]=f[15]}return e};a.set=function(f,e){e[0]=f[0];e[1]=f[1];e[2]=f[2];e[3]=f[3];e[4]=f[4];e[5]=f[5];e[6]=f[6];e[7]=f[7];e[8]=f[8];e[9]=f[9];e[10]=f[10];e[11]=f[11];e[12]=f[12];e[13]=f[13];e[14]=f[14];e[15]=f[15];return e};a.identity=function(e){e[0]=1;e[1]=0;e[2]=0;e[3]=0;e[4]=0;e[5]=1;e[6]=0;e[7]=0;e[8]=0;e[9]=0;e[10]=1;e[11]=0;e[12]=0;e[13]=0;e[14]=0;e[15]=1;return e};a.transpose=function(h,g){if(!g||h==g){var l=h[1],j=h[2],i=h[3];var e=h[6],k=h[7];var f=h[11];h[1]=h[4];h[2]=h[8];h[3]=h[12];h[4]=l;h[6]=h[9];h[7]=h[13];h[8]=j;h[9]=e;h[11]=h[14];h[12]=i;h[13]=k;h[14]=f;return h}g[0]=h[0];g[1]=h[4];g[2]=h[8];g[3]=h[12];g[4]=h[1];g[5]=h[5];g[6]=h[9];g[7]=h[13];g[8]=h[2];g[9]=h[6];g[10]=h[10];g[11]=h[14];g[12]=h[3];g[13]=h[7];g[14]=h[11];g[15]=h[15];return g};a.determinant=function(s){var l=s[0],k=s[1],i=s[2],g=s[3];var u=s[4],t=s[5],r=s[6],q=s[7];var p=s[8],o=s[9],n=s[10],m=s[11];var j=s[12],h=s[13],f=s[14],e=s[15];return j*o*r*g-p*h*r*g-j*t*n*g+u*h*n*g+p*t*f*g-u*o*f*g-j*o*i*q+p*h*i*q+j*k*n*q-l*h*n*q-p*k*f*q+l*o*f*q+j*t*i*m-u*h*i*m-j*k*r*m+l*h*r*m+u*k*f*m-l*t*f*m-p*t*i*e+u*o*i*e+p*k*r*e-l*o*r*e-u*k*n*e+l*t*n*e};a.inverse=function(z,o){if(!o){o=z}var G=z[0],E=z[1],D=z[2],B=z[3];var h=z[4],g=z[5],f=z[6],e=z[7];var w=z[8],v=z[9],u=z[10],t=z[11];var I=z[12],H=z[13],F=z[14],C=z[15];var s=G*g-E*h;var r=G*f-D*h;var q=G*e-B*h;var p=E*f-D*g;var n=E*e-B*g;var m=D*e-B*f;var l=w*H-v*I;var k=w*F-u*I;var j=w*C-t*I;var i=v*F-u*H;var A=v*C-t*H;var y=u*C-t*F;var x=1/(s*y-r*A+q*i+p*j-n*k+m*l);o[0]=(g*y-f*A+e*i)*x;o[1]=(-E*y+D*A-B*i)*x;o[2]=(H*m-F*n+C*p)*x;o[3]=(-v*m+u*n-t*p)*x;o[4]=(-h*y+f*j-e*k)*x;o[5]=(G*y-D*j+B*k)*x;o[6]=(-I*m+F*q-C*r)*x;o[7]=(w*m-u*q+t*r)*x;o[8]=(h*A-g*j+e*l)*x;o[9]=(-G*A+E*j-B*l)*x;o[10]=(I*n-H*q+C*s)*x;o[11]=(-w*n+v*q-t*s)*x;o[12]=(-h*i+g*k-f*l)*x;o[13]=(G*i-E*k+D*l)*x;o[14]=(-I*p+H*r-F*s)*x;o[15]=(w*p-v*r+u*s)*x;return o};a.toRotationMat=function(f,e){if(!e){e=a.create()}e[0]=f[0];e[1]=f[1];e[2]=f[2];e[3]=f[3];e[4]=f[4];e[5]=f[5];e[6]=f[6];e[7]=f[7];e[8]=f[8];e[9]=f[9];e[10]=f[10];e[11]=f[11];e[12]=0;e[13]=0;e[14]=0;e[15]=1;return e};a.toMat3=function(f,e){if(!e){e=b.create()}e[0]=f[0];e[1]=f[1];e[2]=f[2];e[3]=f[4];e[4]=f[5];e[5]=f[6];e[6]=f[8];e[7]=f[9];e[8]=f[10];return e};a.toInverseMat3=function(r,p){var i=r[0],h=r[1],g=r[2];var t=r[4],s=r[5],q=r[6];var m=r[8],l=r[9],k=r[10];var j=k*s-q*l;var f=-k*t+q*m;var o=l*t-s*m;var n=i*j+h*f+g*o;if(!n){return null}var e=1/n;if(!p){p=b.create()}p[0]=j*e;p[1]=(-k*h+g*l)*e;p[2]=(q*h-g*s)*e;p[3]=f*e;p[4]=(k*i-g*m)*e;p[5]=(-q*i+g*t)*e;p[6]=o*e;p[7]=(-l*i+h*m)*e;p[8]=(s*i-h*t)*e;return p};a.multiply=function(D,m,n){if(!n){n=D}var K=D[0],J=D[1],H=D[2],F=D[3];var l=D[4],k=D[5],j=D[6],i=D[7];var z=D[8],y=D[9],x=D[10],w=D[11];var M=D[12],L=D[13],I=D[14],G=D[15];var u=m[0],s=m[1],q=m[2],o=m[3];var E=m[4],C=m[5],B=m[6],A=m[7];var h=m[8],g=m[9],f=m[10],e=m[11];var v=m[12],t=m[13],r=m[14],p=m[15];n[0]=u*K+s*l+q*z+o*M;n[1]=u*J+s*k+q*y+o*L;n[2]=u*H+s*j+q*x+o*I;n[3]=u*F+s*i+q*w+o*G;n[4]=E*K+C*l+B*z+A*M;n[5]=E*J+C*k+B*y+A*L;n[6]=E*H+C*j+B*x+A*I;n[7]=E*F+C*i+B*w+A*G;n[8]=h*K+g*l+f*z+e*M;n[9]=h*J+g*k+f*y+e*L;n[10]=h*H+g*j+f*x+e*I;n[11]=h*F+g*i+f*w+e*G;n[12]=v*K+t*l+r*z+p*M;n[13]=v*J+t*k+r*y+p*L;n[14]=v*H+t*j+r*x+p*I;n[15]=v*F+t*i+r*w+p*G;return n};a.multiplyVec3=function(h,g,f){if(!f){f=g}var e=g[0],j=g[1],i=g[2];f[0]=h[0]*e+h[4]*j+h[8]*i+h[12];f[1]=h[1]*e+h[5]*j+h[9]*i+h[13];f[2]=h[2]*e+h[6]*j+h[10]*i+h[14];return f};a.multiplyVec4=function(i,h,g){if(!g){g=h}var e=h[0],k=h[1],j=h[2],f=h[3];g[0]=i[0]*e+i[4]*k+i[8]*j+i[12]*f;g[1]=i[1]*e+i[5]*k+i[9]*j+i[13]*f;g[2]=i[2]*e+i[6]*k+i[10]*j+i[14]*f;g[3]=i[3]*e+i[7]*k+i[11]*j+i[15]*f;return g};a.translate=function(r,m,k){var l=m[0],j=m[1],i=m[2];if(!k||r==k){r[12]=r[0]*l+r[4]*j+r[8]*i+r[12];r[13]=r[1]*l+r[5]*j+r[9]*i+r[13];r[14]=r[2]*l+r[6]*j+r[10]*i+r[14];r[15]=r[3]*l+r[7]*j+r[11]*i+r[15];return r}var v=r[0],u=r[1],t=r[2],s=r[3];var h=r[4],g=r[5],f=r[6],e=r[7];var q=r[8],p=r[9],o=r[10],n=r[11];k[0]=v;k[1]=u;k[2]=t;k[3]=s;k[4]=h;k[5]=g;k[6]=f;k[7]=e;k[8]=q;k[9]=p;k[10]=o;k[11]=n;k[12]=v*l+h*j+q*i+r[12];k[13]=u*l+g*j+p*i+r[13];k[14]=t*l+f*j+o*i+r[14];k[15]=s*l+e*j+n*i+r[15];return k};a.scale=function(h,g,f){var e=g[0],j=g[1],i=g[2];if(!f||h==f){h[0]*=e;h[1]*=e;h[2]*=e;h[3]*=e;h[4]*=j;h[5]*=j;h[6]*=j;h[7]*=j;h[8]*=i;h[9]*=i;h[10]*=i;h[11]*=i;return h}f[0]=h[0]*e;f[1]=h[1]*e;f[2]=h[2]*e;f[3]=h[3]*e;f[4]=h[4]*j;f[5]=h[5]*j;f[6]=h[6]*j;f[7]=h[7]*j;f[8]=h[8]*i;f[9]=h[9]*i;f[10]=h[10]*i;f[11]=h[11]*i;f[12]=h[12];f[13]=h[13];f[14]=h[14];f[15]=h[15];return f};a.rotate=function(I,G,e,o){var p=e[0],n=e[1],m=e[2];var E=Math.sqrt(p*p+n*n+m*m);if(!E){return null}if(E!=1){E=1/E;p*=E;n*=E;m*=E}var w=Math.sin(G);var K=Math.cos(G);var v=1-K;var O=I[0],N=I[1],M=I[2],L=I[3];var l=I[4],k=I[5],j=I[6],i=I[7];var D=I[8],C=I[9],B=I[10],A=I[11];var u=p*p*v+K,r=n*p*v+m*w,q=m*p*v-n*w;var J=p*n*v-m*w,H=n*n*v+K,F=m*n*v+p*w;var h=p*m*v+n*w,g=n*m*v-p*w,f=m*m*v+K;if(!o){o=I}else{if(I!=o){o[12]=I[12];o[13]=I[13];o[14]=I[14];o[15]=I[15]}}o[0]=O*u+l*r+D*q;o[1]=N*u+k*r+C*q;o[2]=M*u+j*r+B*q;o[3]=L*u+i*r+A*q;o[4]=O*J+l*H+D*F;o[5]=N*J+k*H+C*F;o[6]=M*J+j*H+B*F;o[7]=L*J+i*H+A*F;o[8]=O*h+l*g+D*f;o[9]=N*h+k*g+C*f;o[10]=M*h+j*g+B*f;o[11]=L*h+i*g+A*f;return o};a.rotateX=function(n,e,l){var q=Math.sin(e);var j=Math.cos(e);var p=n[4],o=n[5],m=n[6],k=n[7];var i=n[8],h=n[9],g=n[10],f=n[11];if(!l){l=n}else{if(n!=l){l[0]=n[0];l[1]=n[1];l[2]=n[2];l[3]=n[3];l[12]=n[12];l[13]=n[13];l[14]=n[14];l[15]=n[15]}}l[4]=p*j+i*q;l[5]=o*j+h*q;l[6]=m*j+g*q;l[7]=k*j+f*q;l[8]=p*-q+i*j;l[9]=o*-q+h*j;l[10]=m*-q+g*j;l[11]=k*-q+f*j;return l};a.rotateY=function(p,h,o){var q=Math.sin(h);var n=Math.cos(h);var i=p[0],g=p[1],f=p[2],e=p[3];var m=p[8],l=p[9],k=p[10],j=p[11];if(!o){o=p}else{if(p!=o){o[4]=p[4];o[5]=p[5];o[6]=p[6];o[7]=p[7];o[12]=p[12];o[13]=p[13];o[14]=p[14];o[15]=p[15]}}o[0]=i*n+m*-q;o[1]=g*n+l*-q;o[2]=f*n+k*-q;o[3]=e*n+j*-q;o[8]=i*q+m*n;o[9]=g*q+l*n;o[10]=f*q+k*n;o[11]=e*q+j*n;return o};a.rotateZ=function(o,h,l){var q=Math.sin(h);var j=Math.cos(h);var i=o[0],g=o[1],f=o[2],e=o[3];var p=o[4],n=o[5],m=o[6],k=o[7];if(!l){l=o}else{if(o!=l){l[8]=o[8];l[9]=o[9];l[10]=o[10];l[11]=o[11];l[12]=o[12];l[13]=o[13];l[14]=o[14];l[15]=o[15]}}l[0]=i*j+p*q;l[1]=g*j+n*q;l[2]=f*j+m*q;l[3]=e*j+k*q;l[4]=i*-q+p*j;l[5]=g*-q+n*j;l[6]=f*-q+m*j;l[7]=e*-q+k*j;return l};a.frustum=function(f,n,e,l,i,h,m){if(!m){m=a.create()}var j=(n-f);var g=(l-e);var k=(h-i);m[0]=(i*2)/j;m[1]=0;m[2]=0;m[3]=0;m[4]=0;m[5]=(i*2)/g;m[6]=0;m[7]=0;m[8]=(n+f)/j;m[9]=(l+e)/g;m[10]=-(h+i)/k;m[11]=-1;m[12]=0;m[13]=0;m[14]=-(h*i*2)/k;m[15]=0;return m};a.perspective=function(g,f,j,e,h){var k=j*Math.tan(g*Math.PI/360);var i=k*f;return a.frustum(-i,i,-k,k,j,e,h)};a.ortho=function(f,n,e,l,i,h,m){if(!m){m=a.create()}var j=(n-f);var g=(l-e);var k=(h-i);m[0]=2/j;m[1]=0;m[2]=0;m[3]=0;m[4]=0;m[5]=2/g;m[6]=0;m[7]=0;m[8]=0;m[9]=0;m[10]=-2/k;m[11]=0;m[12]=-(f+n)/j;m[13]=-(l+e)/g;m[14]=-(h+i)/k;m[15]=1;return m};a.lookAt=function(z,A,l,k){if(!k){k=a.create()}var x=z[0],v=z[1],s=z[2],j=l[0],i=l[1],h=l[2],r=A[0],q=A[1],p=A[2];if(x==r&&v==q&&s==p){return a.identity(k)}var o,n,m,y,w,u,g,f,e,t;o=x-A[0];n=v-A[1];m=s-A[2];t=1/Math.sqrt(o*o+n*n+m*m);o*=t;n*=t;m*=t;y=i*m-h*n;w=h*o-j*m;u=j*n-i*o;t=Math.sqrt(y*y+w*w+u*u);if(!t){y=0;w=0;u=0}else{t=1/t;y*=t;w*=t;u*=t}g=n*u-m*w;f=m*y-o*u;e=o*w-n*y;t=Math.sqrt(g*g+f*f+e*e);if(!t){g=0;f=0;e=0}else{t=1/t;g*=t;f*=t;e*=t}k[0]=y;k[1]=g;k[2]=o;k[3]=0;k[4]=w;k[5]=f;k[6]=n;k[7]=0;k[8]=u;k[9]=e;k[10]=m;k[11]=0;k[12]=-(y*x+w*v+u*s);k[13]=-(g*x+f*v+e*s);k[14]=-(o*x+n*v+m*s);k[15]=1;return k};a.str=function(e){return\"[\"+e[0]+\", \"+e[1]+\", \"+e[2]+\", \"+e[3]+\", \"+e[4]+\", \"+e[5]+\", \"+e[6]+\", \"+e[7]+\", \"+e[8]+\", \"+e[9]+\", \"+e[10]+\", \"+e[11]+\", \"+e[12]+\", \"+e[13]+\", \"+e[14]+\", \"+e[15]+\"]\"};var d={};d.create=function(f){var e=new glMatrixArrayType(4);if(f){e[0]=f[0];e[1]=f[1];e[2]=f[2];e[3]=f[3]}return e};d.set=function(f,e){e[0]=f[0];e[1]=f[1];e[2]=f[2];e[3]=f[3];return e};d.calculateW=function(g,f){var e=g[0],i=g[1],h=g[2];if(!f||g==f){g[3]=-Math.sqrt(Math.abs(1-e*e-i*i-h*h));return g}f[0]=e;f[1]=i;f[2]=h;f[3]=-Math.sqrt(Math.abs(1-e*e-i*i-h*h));return f};d.inverse=function(f,e){if(!e||f==e){f[0]*=1;f[1]*=1;f[2]*=1;return f}e[0]=-f[0];e[1]=-f[1];e[2]=-f[2];e[3]=f[3];return e};d.length=function(g){var e=g[0],i=g[1],h=g[2],f=g[3];return Math.sqrt(e*e+i*i+h*h+f*f)};d.normalize=function(i,h){if(!h){h=i}var f=i[0],k=i[1],j=i[2],g=i[3];var e=Math.sqrt(f*f+k*k+j*j+g*g);if(e==0){h[0]=0;h[1]=0;h[2]=0;h[3]=0;return h}e=1/e;h[0]=f*e;h[1]=k*e;h[2]=j*e;h[3]=g*e;return h};d.multiply=function(f,h,o){if(!o){o=f}var m=f[0],l=f[1],k=f[2],n=f[3];var i=h[0],g=h[1],e=h[2],j=h[3];o[0]=m*j+n*i+l*e-k*g;o[1]=l*j+n*g+k*i-m*e;o[2]=k*j+n*e+m*g-l*i;o[3]=n*j-m*i-l*g-k*e;return o};d.multiplyVec3=function(f,h,r){if(!r){r=h}var q=h[0],p=h[1],o=h[2];var m=f[0],l=f[1],k=f[2],n=f[3];var i=n*q+l*o-k*p;var g=n*p+k*q-m*o;var e=n*o+m*p-l*q;var j=-m*q-l*p-k*o;r[0]=i*n+j*-m+g*-k-e*-l;r[1]=g*n+j*-l+e*-m-i*-k;r[2]=e*n+j*-k+i*-l-g*-m;return r};d.toMat3=function(e,l){if(!l){l=b.create()}var m=e[0],k=e[1],j=e[2],n=e[3];var r=m+m;var f=k+k;var o=j+j;var i=m*r;var h=m*f;var g=m*o;var q=k*f;var p=k*o;var u=j*o;var v=n*r;var t=n*f;var s=n*o;l[0]=1-(q+u);l[1]=h-s;l[2]=g+t;l[3]=h+s;l[4]=1-(i+u);l[5]=p-v;l[6]=g-t;l[7]=p+v;l[8]=1-(i+q);return l};d.toMat4=function(e,l){if(!l){l=a.create()}var m=e[0],k=e[1],j=e[2],n=e[3];var r=m+m;var f=k+k;var o=j+j;var i=m*r;var h=m*f;var g=m*o;var q=k*f;var p=k*o;var u=j*o;var v=n*r;var t=n*f;var s=n*o;l[0]=1-(q+u);l[1]=h-s;l[2]=g+t;l[3]=0;l[4]=h+s;l[5]=1-(i+u);l[6]=p-v;l[7]=0;l[8]=g-t;l[9]=p+v;l[10]=1-(i+q);l[11]=0;l[12]=0;l[13]=0;l[14]=0;l[15]=1;return l};d.str=function(e){return\"[\"+e[0]+\", \"+e[1]+\", \"+e[2]+\", \"+e[3]+\"]\"};return{vec3:c,mat3:b,mat4:a}})()");
	}
}
