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
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GL support class.
 *
 * <p>The {@link WGLWidget} class is an interface to the HTML5 WebGL infrastructure for client-side
 * rendering, and OpenGL for server-side rendering. Its API is based on the WebGL API. To fully
 * understand WebGL, it is recommended to read the WebGL standard in addition to this documentation.
 *
 * <p>The most recent version of the WebGL specification can be found here: <a
 * href="http://www.khronos.org/registry/webgl/specs/latest/1.0/">http://www.khronos.org/registry/webgl/specs/latest/1.0/</a>
 *
 * <p>The goal of the {@link WGLWidget} class is to provide a method to render 3D structures in the
 * browser, where rendering and rerendering is normally done at the client side in JavaScript
 * without interaction from the server, in order to obtain a smooth user interaction. Unless the
 * scene requires server-side updates, there is no communication with the server.
 *
 * <p>The rendering interface resembles to OpenGL ES, the same standard as WebGL is based on. This
 * is a stripped down version of the normal OpenGL as we usually find them on desktops. Many
 * stateful OpenGL features are not present in OpenGL ES: no modelview and camera transformation
 * stacks, no default lighting models, no support for other rendering methods than through VBOs, ...
 * Therefore much existing example code for OpenGL applications and shaders will not work on WebGL
 * without modifications. The &apos;learning webgl&apos; web site at <a
 * href="http://learningwebgl.com/">http://learningwebgl.com/</a> is a good starting point to get
 * familiar with WebGL.
 *
 * <p>To use a WGLWidget, you must derive from it and reimplement the painter methods. Usually, you
 * will always need to implement {@link WGLWidget#initializeGL() initializeGL()} and {@link
 * WGLWidget#paintGL() paintGL()}. Optionally, you may choose to implement {@link
 * WGLWidget#resizeGL(int width, int height) resizeGL()} (if your widget does not have a fixed
 * size), and {@link WGLWidget#updateGL() updateGL()}. If you need to modify the painting methods, a
 * repaint is triggered by calling the {@link WGLWidget#repaintGL(EnumSet which) repaintGL()}
 * method. The default behaviour for any of these four painting functions is to do nothing.
 *
 * <p>The four painter methods ({@link WGLWidget#initializeGL() initializeGL()}, {@link
 * WGLWidget#resizeGL(int width, int height) resizeGL()}, {@link WGLWidget#paintGL() paintGL()} and
 * {@link WGLWidget#updateGL() updateGL()}) all record JavaScript which is sent to the browser. The
 * JavaScript code of {@link WGLWidget#paintGL() paintGL()} is cached client-side, and may be
 * executed many times, e.g. to repaint a scene from different viewpoints. The JavaScript code of
 * {@link WGLWidget#initializeGL() initializeGL()}, {@link WGLWidget#resizeGL(int width, int height)
 * resizeGL()} and {@link WGLWidget#updateGL() updateGL()} are intended for OpenGL state updates,
 * and is therefore only executed once on the client and is then discarded.
 *
 * <p>There are four painting methods that you may implement in a specialization of this class. The
 * purpose of these functions is to register what JavaScript code has to be executed to render a
 * scene. Through invocations of the WebGL functions documented below, JWt records the JavaScript
 * calls that have to be invoked in the browser.
 *
 * <ul>
 *   <li><b>{@link WGLWidget#initializeGL() initializeGL()}</b>: this function is executed after the
 *       GL context has been initialized. It is also executed when the <code>webglcontextrestored
 *       </code> signal is fired. You can distinguish between the first initialization and
 *       restoration of context using {@link WGLWidget#isRestoringContext() isRestoringContext()}.
 *       This is the ideal location to compose shader programs, send VBO&apos;s to the client,
 *       extract uniform and attribute locations, ... Due to the presence of VBO&apos;s, this
 *       function may generate a large amount of data to the client.
 *   <li><b>{@link WGLWidget#resizeGL(int width, int height) resizeGL()}</b>: this function is
 *       executed whenever the canvas dimensions change. A change in canvas size will require you to
 *       invoke the {@link WGLWidget#viewport(int x, int y, int width, int height) viewport()}
 *       function again, as well as recalculate the projection matrices (especially when the aspect
 *       ratio has changed). The {@link WGLWidget#resizeGL(int width, int height) resizeGL()}
 *       function is therefore the ideal location to set those properties. The {@link
 *       WGLWidget#resizeGL(int width, int height) resizeGL()} function is invoked automatically on
 *       every resize, and after the first {@link WGLWidget#initializeGL() initializeGL()}
 *       invocation. Additional invocations may be triggered by calling repaint() with the RESIZE_GL
 *       flag.
 *   <li><b>{@link WGLWidget#paintGL() paintGL()}</b>: this is the main scene drawing function.
 *       Through its execution, JWt records what has to be done to render a scene, and it is
 *       executed every time that the scene is to be redrawn. You can use the VBO&apos;s and shaders
 *       prepared in the {@link WGLWidget#initializeGL() initializeGL()} phase. Usually, this
 *       function sets uniforms and attributes, links attributes to VBO&apos;s, applies textures,
 *       and draws primitives. You may also create local programs, buffers, ... Remember that this
 *       function is executed a lot of times, so every buffer/program created in this function
 *       should also be destroyed to avoid memory leaks. This function is transmitted once to the
 *       client, and is executed when the scene needs to be redrawn. Redraws may be triggered from
 *       mouse events, timer triggers, events on e.g. a video element, or whatever other event. The
 *       {@link WGLWidget#paintGL() paintGL()} function can be updated through invoking {@link
 *       WGLWidget#repaintGL(EnumSet which) repaintGL()} with the PAINT_GL flag.
 *   <li><b>{@link WGLWidget#updateGL() updateGL()}</b>: VBO&apos;s, programs, uniforms, GL
 *       properties, or anything else set during intializeGL() are not necessarily immutable. If you
 *       want to change, add, remove or reconfigure those properties, the execution of an {@link
 *       WGLWidget#updateGL() updateGL()} function can be triggered by invoking {@link
 *       WGLWidget#repaintGL(EnumSet which) repaintGL()} with the UPDATE_GL flag. This signals that
 *       {@link WGLWidget#updateGL() updateGL()} needs to be evaluated - just once. It is possible
 *       that the {@link WGLWidget#paintGL() paintGL()} function also requires updates as
 *       consequence of the changes in the {@link WGLWidget#updateGL() updateGL()} function; in this
 *       case, you should also set the PAINT_GL flag of {@link WGLWidget#repaintGL(EnumSet which)
 *       repaintGL()}.
 * </ul>
 *
 * <p>The GL functions are intended to be used exclusively from within the invocation of the four
 * callback functions mentioned above. In order to manually trigger the execution of these function,
 * use the {@link WGLWidget#repaintGL(EnumSet which) repaintGL()}.
 *
 * <p>A {@link WGLWidget} must be given a size explicitly, or must be put inside a layout manager
 * that manages its width and height. The behaviour of a {@link WGLWidget} that was not given a size
 * is undefined.
 *
 * <p>
 *
 * <h3>Binary buffer transfers</h3>
 *
 * <p>In {@link WGLWidget#bufferDatafv(WGLWidget.GLenum target, java.nio.ByteBuffer buffer,
 * WGLWidget.GLenum usage, boolean binary) bufferDatafv()}, there is an additional boolean argument
 * where you can indicate that you want the data to be transferred to the client in binary form. A
 * {@link WMemoryResource} is created for each of these buffers. If you know all previous resources
 * are not required in the client anymore, you can free memory with the method {@link
 * WGLWidget#clearBinaryResources() clearBinaryResources()} (the memory is also managed, so this is
 * not neccesary). If you want to manage these resources entirely by yourself, the following method
 * can be used.
 *
 * <p>Using createAndLoadArrayBuffer(), you can load an array buffer in binary format from an URL.
 * This will cause the client to fetch the given URL, and make the contents of the file available in
 * an {@link ArrayBuffer}, which can then be used by BufferData() to bind them to an OpenGL buffer.
 * This is ideal to load VBO buffers in a faster way, as it avoids converting floats to text strings
 * on the server and then back to floats on the client. You can combine this with the use of {@link
 * WResource} (e.g. {@link WMemoryResource}) to send an std::vector of vertices to the client. Note
 * that using {@link ArrayBuffer} is not possible when you want a fall-back in the form of
 * server-side rendering.
 *
 * <p>
 *
 * <h3>Client side matrices and vectors.</h3>
 *
 * <p>The {@link WGLWidget} provides the {@link JavaScriptMatrix4x4} class as a mechanism to use
 * client-side modifiable matrices in the render functions. These matrices can be used identically
 * to the &apos;constant&apos;, with the advantage that there is no need to have a roundtrip to the
 * server to redraw the scene when they are changed. As such, they are ideal for mouse-based camera
 * manipulations, timer triggered animations, or object manipulations.
 *
 * <p>There&apos;s also support for client-side modifiable vectors, with {@link JavaScriptVector}.
 */
public class WGLWidget extends WInteractWidget {
  private static Logger logger = LoggerFactory.getLogger(WGLWidget.class);

  /** Abstract base class for all GL objects. */
  public abstract static class GlObject {
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
  /** Reference to a WebGLShader class. */
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
  /** Reference to a WebGLProgram class. */
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
  /** Reference to a shader attribute location. */
  public static class AttribLocation extends WGLWidget.GlObject {
    private static Logger logger = LoggerFactory.getLogger(AttribLocation.class);

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
  /** Reference to a WebGLBuffer class. */
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
  /** Reference to a WebGLUniformLocation class. */
  public static class UniformLocation extends WGLWidget.GlObject {
    private static Logger logger = LoggerFactory.getLogger(UniformLocation.class);

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
  /** Reference to a WebGLTexture class. */
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
  /** Reference to a WebGLFramebuffer class. */
  public static class Framebuffer extends WGLWidget.GlObject {
    private static Logger logger = LoggerFactory.getLogger(Framebuffer.class);

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
  /** Reference to a WebGLRenderbuffer class. */
  public static class Renderbuffer extends WGLWidget.GlObject {
    private static Logger logger = LoggerFactory.getLogger(Renderbuffer.class);

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
  /** Reference to a javascript ArrayBuffer class. */
  public static class ArrayBuffer extends WGLWidget.GlObject {
    private static Logger logger = LoggerFactory.getLogger(ArrayBuffer.class);

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
   *
   * <p>Using a {@link JavaScriptVector}, GL parameters can be modified without communication with
   * the server. The value of the {@link JavaScriptMatrix4x4} is updated server-side whenever an
   * event is sent to the server.
   *
   * <p>The {@link JavaScriptVector} is represented in JavaScript as an array, either as a
   * Float32Array or as a plain JavaScript array.
   */
  public static class JavaScriptVector {
    private static Logger logger = LoggerFactory.getLogger(JavaScriptVector.class);

    /**
     * Create a temporarily invalid {@link JavaScriptVector}.
     *
     * <p>Should be added to a WGLWidget with {@link
     * WGLWidget#addJavaScriptVector(WGLWidget.JavaScriptVector vec)
     * WGLWidget#addJavaScriptVector()}, and initialized with {@link
     * WGLWidget#initJavaScriptVector(WGLWidget.JavaScriptVector vec)
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
    /** Returns whether this JavaScriptVector has been initialized. */
    public boolean isInitialized() {
      return this.initialized_;
    }
    /** Returns whether this JavaScriptVector has been assigned to a {@link WGLWidget}. */
    public boolean hasContext() {
      return this.context_ != null;
    }
    /** Returns the length (number of items) of this {@link JavaScriptVector}. */
    public int getLength() {
      return this.length_;
    }
    /**
     * Returns the JavaScript reference to this {@link JavaScriptVector}.
     *
     * <p>In order to get a valid JavaScript reference, this vector should have been added to a
     * {@link WGLWidget}.
     */
    public String getJsRef() {
      if (!this.hasContext()) {
        throw new WException("JavaScriptVector: does not belong to a WGLWidget yet");
      }
      return this.jsRef_;
    }
    /**
     * Returns the current server-side value.
     *
     * <p>Client-side changes to the {@link JavaScriptVector} are automatically synchronized.
     */
    public List<Float> getValue() {
      if (!this.hasContext()) {
        throw new WException("JavaScriptVector: vector not assigned to a WGLWidget");
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
      this.jsRef_ = context.getGlObjJsRef() + ".jsValues[" + String.valueOf(this.id_) + "]";
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
   *
   * <p>A JavaScriptMatrix has methods that make it possible to do client-side calculations on
   * matrices.
   *
   * <p>Using a {@link JavaScriptMatrix4x4}, GL parameters can be modified without communication
   * with the server. The value of the {@link JavaScriptMatrix4x4} is updated server-side whenever
   * an event is sent to the server.
   *
   * <p>Important: only the {@link WGLWidget.JavaScriptMatrix4x4#getJsRef() getJsRef()} of the
   * return value from a call to WGLWidget::createJavaScriptMatrix() is a variable name that can be
   * used in custom JavaScript to modify a matrix from external scripts. The {@link
   * WGLWidget.JavaScriptMatrix4x4#getJsRef() getJsRef()} of return values of operations refer to
   * unnamed temporary objects - rvalues in C++-lingo.
   *
   * <p>The {@link JavaScriptMatrix4x4} is represented in JavaScript as an array of 16 elements.
   * This array represents the values of the matrix in column-major order. It is either a
   * Float32Array or a plain JavaScript array.
   */
  public static class JavaScriptMatrix4x4 {
    private static Logger logger = LoggerFactory.getLogger(JavaScriptMatrix4x4.class);

    /**
     * Creates a temporarily invalid {@link JavaScriptMatrix4x4}.
     *
     * <p>Should be added to a WGLWidget with {@link
     * WGLWidget#addJavaScriptMatrix4(WGLWidget.JavaScriptMatrix4x4 mat)
     * WGLWidget#addJavaScriptMatrix4()}, and initialized with {@link
     * WGLWidget#initJavaScriptMatrix4(WGLWidget.JavaScriptMatrix4x4 mat)
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
    /** Returns whether this {@link JavaScriptMatrix4x4} has been initialized. */
    public boolean isInitialized() {
      return this.initialized_;
    }
    /**
     * Returns whether this {@link JavaScriptMatrix4x4} has been assigned to a {@link WGLWidget}.
     */
    public boolean hasContext() {
      return this.context_ != null;
    }
    /**
     * Returns the JavaScript reference to this {@link JavaScriptMatrix4x4}.
     *
     * <p>In order to get a valid JavaScript reference, this matrix should have been added to a
     * {@link WGLWidget}.
     */
    public String getJsRef() {
      if (!this.hasContext()) {
        throw new WException("JavaScriptMatrix4x4: does not belong to a WGLWidget yet");
      }
      return this.jsRef_;
    }
    /**
     * Returns the current server-side value.
     *
     * <p>Client-side changes to the {@link JavaScriptMatrix4x4} are automatically synchronized.
     */
    public javax.vecmath.Matrix4f getValue() {
      if (!this.hasContext()) {
        throw new WException("JavaScriptMatrix4x4: matrix not assigned to a WGLWidget");
      }
      javax.vecmath.Matrix4f originalCpy =
          new javax.vecmath.Matrix4f(
              1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
              0.0f, 1.0f);
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
        throw new WException("JavaScriptMatrix4x4: matrix not initialized");
      }
      WGLWidget.JavaScriptMatrix4x4 copy = this.clone();
      copy.jsRef_ =
          "Wt4_8_1.glMatrix.mat4.inverse(" + this.jsRef_ + ", Wt4_8_1.glMatrix.mat4.create())";
      copy.operations_.add(WGLWidget.JavaScriptMatrix4x4.op.INVERT);
      return copy;
    }

    public WGLWidget.JavaScriptMatrix4x4 transposed() {
      if (!this.isInitialized()) {
        throw new WException("JavaScriptMatrix4x4: matrix not initialized");
      }
      WGLWidget.JavaScriptMatrix4x4 copy = this.clone();
      copy.jsRef_ =
          "Wt4_8_1.glMatrix.mat4.transpose(" + this.jsRef_ + ", Wt4_8_1.glMatrix.mat4.create())";
      copy.operations_.add(WGLWidget.JavaScriptMatrix4x4.op.TRANSPOSE);
      return copy;
    }

    public WGLWidget.JavaScriptMatrix4x4 multiply(final javax.vecmath.Matrix4f m) {
      if (!this.isInitialized()) {
        throw new WException("JavaScriptMatrix4x4: matrix not initialized");
      }
      WGLWidget.JavaScriptMatrix4x4 copy = this.clone();
      StringWriter ss = new StringWriter();
      ss.append("Wt4_8_1.glMatrix.mat4.multiply(").append(this.jsRef_).append(",");
      javax.vecmath.Matrix4f t = WebGLUtils.transpose(m);
      WebGLUtils.renderfv(ss, t, this.context_.pImpl_.getArrayType());
      ss.append(", Wt4_8_1.glMatrix.mat4.create())");
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
      this.jsRef_ = context.getGlObjJsRef() + ".jsValues[" + String.valueOf(this.id_) + "]";
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
      TRANSPOSE,
      INVERT,
      MULTIPLY;

      /** Returns the numerical representation of this enum. */
      public int getValue() {
        return ordinal();
      }
    }

    List<WGLWidget.JavaScriptMatrix4x4.op> operations_;
    List<javax.vecmath.Matrix4f> matrices_;
    private boolean initialized_;
  }
  /**
   * Construct a GL widget.
   *
   * <p>Before the first rendering, you must apply a size to the {@link WGLWidget}.
   */
  public WGLWidget(WContainerWidget parentContainer) {
    super();
    this.renderOptions_ =
        EnumSet.of(
            GLRenderOption.ClientSide, GLRenderOption.ServerSide, GLRenderOption.AntiAliasing);
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
    this.mouseWentDownSlot_ =
        new JSlot("function(o, e){" + this.getGlObjJsRef() + ".mouseDown(o, e);}", this);
    this.mouseWentUpSlot_ =
        new JSlot("function(o, e){" + this.getGlObjJsRef() + ".mouseUp(o, e);}", this);
    this.mouseDraggedSlot_ =
        new JSlot("function(o, e){" + this.getGlObjJsRef() + ".mouseDrag(o, e);}", this);
    this.mouseMovedSlot_ =
        new JSlot("function(o, e){" + this.getGlObjJsRef() + ".mouseMove(o, e);}", this);
    this.mouseWheelSlot_ =
        new JSlot("function(o, e){" + this.getGlObjJsRef() + ".mouseWheel(o, e);}", this);
    this.touchStarted_ =
        new JSlot("function(o, e){" + this.getGlObjJsRef() + ".touchStart(o, e);}", this);
    this.touchEnded_ =
        new JSlot("function(o, e){" + this.getGlObjJsRef() + ".touchEnd(o, e);}", this);
    this.touchMoved_ =
        new JSlot("function(o, e){" + this.getGlObjJsRef() + ".touchMoved(o, e);}", this);
    this.repaintSlot_ =
        new JSlot("function() {var o = " + this.getGlObjJsRef() + ";if(o.ctx) o.paintGL();}", this);
    this.setInline(false);
    this.setLayoutSizeAware(true);
    this.webglNotAvailable_.addListener(
        this,
        () -> {
          WGLWidget.this.webglNotAvailable();
        });
    this.repaintSignal_.addListener(
        this,
        () -> {
          WGLWidget.this.repaintGL(GLClientSideRenderer.PAINT_GL);
        });
    this.contextRestored_.addListener(
        this,
        () -> {
          WGLWidget.this.contextRestored();
        });
    this.mouseWentDown().addListener(this.mouseWentDownSlot_);
    this.mouseWentUp().addListener(this.mouseWentUpSlot_);
    this.mouseDragged().addListener(this.mouseDraggedSlot_);
    this.mouseMoved().addListener(this.mouseMovedSlot_);
    this.mouseWheel().addListener(this.mouseWheelSlot_);
    this.touchStarted().addListener(this.touchStarted_);
    this.touchEnded().addListener(this.touchEnded_);
    this.touchMoved().addListener(this.touchMoved_);
    this.setAlternativeContent(
        new WText("Your browser does not support WebGL", (WContainerWidget) null));
    this.setFormObject(true);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Construct a GL widget.
   *
   * <p>Calls {@link #WGLWidget(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WGLWidget() {
    this((WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {
    {
      WWidget oldWidget = this.alternative_;
      this.alternative_ = null;
      {
        WWidget toRemove = this.manageWidget(oldWidget, this.alternative_);
        if (toRemove != null) toRemove.remove();
      }
    }
    super.remove();
  }
  /**
   * Sets the rendering option.
   *
   * <p>Use this method to configure whether client-side and/or server-side rendering can be used,
   * and whether anti-aliasing should be enabled. The actual choice is also based on availability
   * (respectively client-side or server-side).
   *
   * <p>The default value is to try both ClientSide or ServerSide rendering, and to enable
   * anti-aliasing if available.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Options must be set before the widget is being rendered. </i>
   */
  public void setRenderOptions(EnumSet<GLRenderOption> options) {
    this.renderOptions_ = EnumSet.copyOf(options);
  }
  /**
   * Sets the rendering option.
   *
   * <p>Calls {@link #setRenderOptions(EnumSet options) setRenderOptions(EnumSet.of(option,
   * options))}
   */
  public final void setRenderOptions(GLRenderOption option, GLRenderOption... options) {
    setRenderOptions(EnumSet.of(option, options));
  }
  /**
   * Initialize the GL state when the widget is first shown.
   *
   * <p>{@link WGLWidget#initializeGL() initializeGL()} is called when the widget is first rendered,
   * and when the webglcontextrestored signal is fired. You can distinguish between the first
   * initialization and context restoration using {@link WGLWidget#isRestoringContext()
   * isRestoringContext()}. It usually creates most of the GL related state: shaders, VBOs, uniform
   * locations, ...
   *
   * <p>If this state is to be updated during the lifetime of the widget, you should specialize the
   * {@link WGLWidget#updateGL() updateGL()} to accomodate for this.
   */
  protected void initializeGL() {}
  /**
   * Act on resize events.
   *
   * <p>Usually, this method only contains functions to set the viewport and the projection matrix
   * (as this is aspect ration dependent).
   *
   * <p>{@link WGLWidget#resizeGL(int width, int height) resizeGL()} is rendered after initializeGL,
   * and whenever widget is resized. After this method finishes, the widget is repainted with the
   * cached client-side paint function.
   */
  protected void resizeGL(int width, int height) {}
  /**
   * Update the client-side painting function.
   *
   * <p>This method is invoked client-side when a repaint is required, i.e. when the {@link
   * WGLWidget#getRepaintSlot() getRepaintSlot()} (a JavaScript-side {@link JSlot}) is triggered.
   * Typical examples are: after mouse-based camera movements, after a timed update of a camera or
   * an object&apos;s position, after a resize event ({@link WGLWidget#resizeGL(int width, int
   * height) resizeGL()} will also be called then), after an animation event, ... In many cases,
   * this function will be executed client-side many many times.
   *
   * <p>Using the GL functions from this class, you construct a scene. The implementation tracks all
   * JavaScript calls that need to be performed to draw the scenes, and will replay them verbatim on
   * every trigger of the {@link WGLWidget#getRepaintSlot() getRepaintSlot()}. There are a few
   * mechanisms that may be employed to change what is rendered without updating the {@link
   * WGLWidget#paintGL() paintGL()} cache:
   *
   * <ul>
   *   <li>Client-side matrices may be used to change camera viewpoints, manipilate separate
   *       object&apos;s model transformation matrices, ...
   *   <li>{@link Shader} sources can be updated without requiring the paint function to be renewed
   * </ul>
   *
   * <p>Updating the {@link WGLWidget#paintGL() paintGL()} cache is usually not too expensive; the
   * VBOs, which are large in many cases, are already at the client side, while the {@link
   * WGLWidget#paintGL() paintGL()} code only draws the VBOs. Of course, if you have to draw many
   * separate objects, the {@link WGLWidget#paintGL() paintGL()} JS code may become large and
   * updating is more expensive.
   *
   * <p>In order to update the {@link WGLWidget#paintGL() paintGL()} cache, call {@link
   * WGLWidget#repaintGL(EnumSet which) repaintGL()} with the PAINT_GL parameter, which will cause
   * the invocation of this method.
   */
  protected void paintGL() {}
  /**
   * Update state set in {@link WGLWidget#initializeGL() initializeGL()}
   *
   * <p>Invoked when repaint is called with the UPDATE_GL call.
   *
   * <p>This is intended to be executed when you want to change programs, &apos;constant&apos;
   * uniforms, or even VBO&apos;s, ... without resending already initialized data. It is a mechanism
   * to make changes to what you&apos;ve set in intializeGL(). For every server-side invocation of
   * this method, the result will be rendered client-side exactly once.
   */
  protected void updateGL() {}
  /**
   * Request invocation of resizeGL, paintGL and/or updateGL.
   *
   * <p>If invoked with PAINT_GL, the client-side cached paint function is updated. If invoked with
   * RESIZE_GL or UPDATE_GL, the code will be executed once.
   *
   * <p>If invoked with multiple flags set, the order of execution will be {@link
   * WGLWidget#updateGL() updateGL()}, {@link WGLWidget#resizeGL(int width, int height) resizeGL()},
   * {@link WGLWidget#paintGL() paintGL()}.
   */
  public void repaintGL(EnumSet<GLClientSideRenderer> which) {
    if (!(this.pImpl_ != null)) {
      return;
    }
    this.pImpl_.repaintGL(which);
    if (!which.isEmpty()) {
      this.repaint();
    }
  }
  /**
   * Request invocation of resizeGL, paintGL and/or updateGL.
   *
   * <p>Calls {@link #repaintGL(EnumSet which) repaintGL(EnumSet.of(whic, which))}
   */
  public final void repaintGL(GLClientSideRenderer whic, GLClientSideRenderer... which) {
    repaintGL(EnumSet.of(whic, which));
  }
  /**
   * Returns whether a lost context is in the process of being restored.
   *
   * <p>You can check for this in {@link WGLWidget#initializeGL() initializeGL()}, to handle the
   * first initialization and restoration of context differently.
   */
  public boolean isRestoringContext() {
    return this.restoringContext_;
  }

  public void resize(final WLength width, final WLength height) {
    super.resize(width, height);
    if (this.pImpl_ != null) {
      this.layoutSizeChanged((int) width.getValue(), (int) height.getValue());
    }
  }
  /**
   * The enormous GLenum.
   *
   * <p>This enum contains all numeric constants defined by the WebGL standard, see: <a
   * href="http://www.khronos.org/registry/webgl/specs/latest/1.0/#WEBGLRENDERINGCONTEXT">http://www.khronos.org/registry/webgl/specs/latest/1.0/#WEBGLRENDERINGCONTEXT</a>
   */
  public enum GLenum {
    DEPTH_BUFFER_BIT,
    STENCIL_BUFFER_BIT,
    COLOR_BUFFER_BIT,
    POINTS,
    LINES,
    LINE_LOOP,
    LINE_STRIP,
    TRIANGLES,
    TRIANGLE_STRIP,
    TRIANGLE_FAN,
    ZERO,
    ONE,
    SRC_COLOR,
    ONE_MINUS_SRC_COLOR,
    SRC_ALPHA,
    ONE_MINUS_SRC_ALPHA,
    DST_ALPHA,
    ONE_MINUS_DST_ALPHA,
    DST_COLOR,
    ONE_MINUS_DST_COLOR,
    SRC_ALPHA_SATURATE,
    FUNC_ADD,
    BLEND_EQUATION,
    BLEND_EQUATION_RGB,
    BLEND_EQUATION_ALPHA,
    FUNC_SUBTRACT,
    FUNC_REVERSE_SUBTRACT,
    BLEND_DST_RGB,
    BLEND_SRC_RGB,
    BLEND_DST_ALPHA,
    BLEND_SRC_ALPHA,
    CONSTANT_COLOR,
    ONE_MINUS_CONSTANT_COLOR,
    CONSTANT_ALPHA,
    ONE_MINUS_CONSTANT_ALPHA,
    BLEND_COLOR,
    ARRAY_BUFFER,
    ELEMENT_ARRAY_BUFFER,
    ARRAY_BUFFER_BINDING,
    ELEMENT_ARRAY_BUFFER_BINDING,
    STREAM_DRAW,
    STATIC_DRAW,
    DYNAMIC_DRAW,
    BUFFER_SIZE,
    BUFFER_USAGE,
    CURRENT_VERTEX_ATTRIB,
    FRONT,
    BACK,
    FRONT_AND_BACK,
    CULL_FACE,
    BLEND,
    DITHER,
    STENCIL_TEST,
    DEPTH_TEST,
    SCISSOR_TEST,
    POLYGON_OFFSET_FILL,
    SAMPLE_ALPHA_TO_COVERAGE,
    SAMPLE_COVERAGE,
    NO_ERROR,
    INVALID_ENUM,
    INVALID_VALUE,
    INVALID_OPERATION,
    OUT_OF_MEMORY,
    CW,
    CCW,
    LINE_WIDTH,
    ALIASED_POINT_SIZE_RANGE,
    ALIASED_LINE_WIDTH_RANGE,
    CULL_FACE_MODE,
    FRONT_FACE,
    DEPTH_RANGE,
    DEPTH_WRITEMASK,
    DEPTH_CLEAR_VALUE,
    DEPTH_FUNC,
    STENCIL_CLEAR_VALUE,
    STENCIL_FUNC,
    STENCIL_FAIL,
    STENCIL_PASS_DEPTH_FAIL,
    STENCIL_PASS_DEPTH_PASS,
    STENCIL_REF,
    STENCIL_VALUE_MASK,
    STENCIL_WRITEMASK,
    STENCIL_BACK_FUNC,
    STENCIL_BACK_FAIL,
    STENCIL_BACK_PASS_DEPTH_FAIL,
    STENCIL_BACK_PASS_DEPTH_PASS,
    STENCIL_BACK_REF,
    STENCIL_BACK_VALUE_MASK,
    STENCIL_BACK_WRITEMASK,
    VIEWPORT,
    SCISSOR_BOX,
    COLOR_CLEAR_VALUE,
    COLOR_WRITEMASK,
    UNPACK_ALIGNMENT,
    PACK_ALIGNMENT,
    MAX_TEXTURE_SIZE,
    MAX_VIEWPORT_DIMS,
    SUBPIXEL_BITS,
    RED_BITS,
    GREEN_BITS,
    BLUE_BITS,
    ALPHA_BITS,
    DEPTH_BITS,
    STENCIL_BITS,
    POLYGON_OFFSET_UNITS,
    POLYGON_OFFSET_FACTOR,
    TEXTURE_BINDING_2D,
    SAMPLE_BUFFERS,
    SAMPLES,
    SAMPLE_COVERAGE_VALUE,
    SAMPLE_COVERAGE_INVERT,
    NUM_COMPRESSED_TEXTURE_FORMATS,
    COMPRESSED_TEXTURE_FORMATS,
    DONT_CARE,
    FASTEST,
    NICEST,
    GENERATE_MIPMAP_HINT,
    BYTE,
    UNSIGNED_BYTE,
    SHORT,
    UNSIGNED_SHORT,
    INT,
    UNSIGNED_INT,
    FLOAT,
    DEPTH_COMPONENT,
    ALPHA,
    RGB,
    RGBA,
    LUMINANCE,
    LUMINANCE_ALPHA,
    UNSIGNED_SHORT_4_4_4_4,
    UNSIGNED_SHORT_5_5_5_1,
    UNSIGNED_SHORT_5_6_5,
    FRAGMENT_SHADER,
    VERTEX_SHADER,
    MAX_VERTEX_ATTRIBS,
    MAX_VERTEX_UNIFORM_VECTORS,
    MAX_VARYING_VECTORS,
    MAX_COMBINED_TEXTURE_IMAGE_UNITS,
    MAX_VERTEX_TEXTURE_IMAGE_UNITS,
    MAX_TEXTURE_IMAGE_UNITS,
    MAX_FRAGMENT_UNIFORM_VECTORS,
    SHADER_TYPE,
    DELETE_STATUS,
    LINK_STATUS,
    VALIDATE_STATUS,
    ATTACHED_SHADERS,
    ACTIVE_UNIFORMS,
    ACTIVE_UNIFORM_MAX_LENGTH,
    ACTIVE_ATTRIBUTES,
    ACTIVE_ATTRIBUTE_MAX_LENGTH,
    SHADING_LANGUAGE_VERSION,
    CURRENT_PROGRAM,
    NEVER,
    LESS,
    EQUAL,
    LEQUAL,
    GREATER,
    NOTEQUAL,
    GEQUAL,
    ALWAYS,
    KEEP,
    REPLACE,
    INCR,
    DECR,
    INVERT,
    INCR_WRAP,
    DECR_WRAP,
    VENDOR,
    RENDERER,
    VERSION,
    NEAREST,
    LINEAR,
    NEAREST_MIPMAP_NEAREST,
    LINEAR_MIPMAP_NEAREST,
    NEAREST_MIPMAP_LINEAR,
    LINEAR_MIPMAP_LINEAR,
    TEXTURE_MAG_FILTER,
    TEXTURE_MIN_FILTER,
    TEXTURE_WRAP_S,
    TEXTURE_WRAP_T,
    TEXTURE_2D,
    TEXTURE,
    TEXTURE_CUBE_MAP,
    TEXTURE_BINDING_CUBE_MAP,
    TEXTURE_CUBE_MAP_POSITIVE_X,
    TEXTURE_CUBE_MAP_NEGATIVE_X,
    TEXTURE_CUBE_MAP_POSITIVE_Y,
    TEXTURE_CUBE_MAP_NEGATIVE_Y,
    TEXTURE_CUBE_MAP_POSITIVE_Z,
    TEXTURE_CUBE_MAP_NEGATIVE_Z,
    MAX_CUBE_MAP_TEXTURE_SIZE,
    TEXTURE0,
    TEXTURE1,
    TEXTURE2,
    TEXTURE3,
    TEXTURE4,
    TEXTURE5,
    TEXTURE6,
    TEXTURE7,
    TEXTURE8,
    TEXTURE9,
    TEXTURE10,
    TEXTURE11,
    TEXTURE12,
    TEXTURE13,
    TEXTURE14,
    TEXTURE15,
    TEXTURE16,
    TEXTURE17,
    TEXTURE18,
    TEXTURE19,
    TEXTURE20,
    TEXTURE21,
    TEXTURE22,
    TEXTURE23,
    TEXTURE24,
    TEXTURE25,
    TEXTURE26,
    TEXTURE27,
    TEXTURE28,
    TEXTURE29,
    TEXTURE30,
    TEXTURE31,
    ACTIVE_TEXTURE,
    REPEAT,
    CLAMP_TO_EDGE,
    MIRRORED_REPEAT,
    FLOAT_VEC2,
    FLOAT_VEC3,
    FLOAT_VEC4,
    INT_VEC2,
    INT_VEC3,
    INT_VEC4,
    BOOL,
    BOOL_VEC2,
    BOOL_VEC3,
    BOOL_VEC4,
    FLOAT_MAT2,
    FLOAT_MAT3,
    FLOAT_MAT4,
    SAMPLER_2D,
    SAMPLER_CUBE,
    VERTEX_ATTRIB_ARRAY_ENABLED,
    VERTEX_ATTRIB_ARRAY_SIZE,
    VERTEX_ATTRIB_ARRAY_STRIDE,
    VERTEX_ATTRIB_ARRAY_TYPE,
    VERTEX_ATTRIB_ARRAY_NORMALIZED,
    VERTEX_ATTRIB_ARRAY_POINTER,
    VERTEX_ATTRIB_ARRAY_BUFFER_BINDING,
    COMPILE_STATUS,
    INFO_LOG_LENGTH,
    SHADER_SOURCE_LENGTH,
    LOW_FLOAT,
    MEDIUM_FLOAT,
    HIGH_FLOAT,
    LOW_INT,
    MEDIUM_INT,
    HIGH_INT,
    FRAMEBUFFER,
    RENDERBUFFER,
    RGBA4,
    RGB5_A1,
    RGB565,
    DEPTH_COMPONENT16,
    STENCIL_INDEX,
    STENCIL_INDEX8,
    DEPTH_STENCIL,
    RENDERBUFFER_WIDTH,
    RENDERBUFFER_HEIGHT,
    RENDERBUFFER_INTERNAL_FORMAT,
    RENDERBUFFER_RED_SIZE,
    RENDERBUFFER_GREEN_SIZE,
    RENDERBUFFER_BLUE_SIZE,
    RENDERBUFFER_ALPHA_SIZE,
    RENDERBUFFER_DEPTH_SIZE,
    RENDERBUFFER_STENCIL_SIZE,
    FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE,
    FRAMEBUFFER_ATTACHMENT_OBJECT_NAME,
    FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL,
    FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE,
    COLOR_ATTACHMENT0,
    DEPTH_ATTACHMENT,
    STENCIL_ATTACHMENT,
    DEPTH_STENCIL_ATTACHMENT,
    NONE,
    FRAMEBUFFER_COMPLETE,
    FRAMEBUFFER_INCOMPLETE_ATTACHMENT,
    FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT,
    FRAMEBUFFER_INCOMPLETE_DIMENSIONS,
    FRAMEBUFFER_UNSUPPORTED,
    FRAMEBUFFER_BINDING,
    RENDERBUFFER_BINDING,
    MAX_RENDERBUFFER_SIZE,
    INVALID_FRAMEBUFFER_OPERATION,
    UNPACK_FLIP_Y_WEBGL,
    UNPACK_PREMULTIPLY_ALPHA_WEBGL,
    CONTEXT_LOST_WEBGL,
    UNPACK_COLORSPACE_CONVERSION_WEBGL,
    BROWSER_DEFAULT_WEBGL;

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return ordinal();
    }
  }

  public void debugger() {
    this.pImpl_.debugger();
  }
  /**
   * GL function to activate an existing texture.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glActiveTexture.xml">glActiveTexture()
   * OpenGL ES manpage</a>
   */
  public void activeTexture(WGLWidget.GLenum texture) {
    this.pImpl_.activeTexture(texture);
  }
  /**
   * GL function to attach a shader to a program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glAttachShader.xml">glAttachShader()
   * OpenGL ES manpage</a>
   */
  public void attachShader(WGLWidget.Program program, WGLWidget.Shader shader) {
    this.pImpl_.attachShader(program, shader);
  }
  /**
   * GL function to bind an attribute to a given location.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBindAttribLocation.xml">glBindAttribLocation()
   * OpenGL ES manpage</a>
   */
  public void bindAttribLocation(WGLWidget.Program program, int index, final String name) {
    this.pImpl_.bindAttribLocation(program, index, name);
  }
  /**
   * GL function to bind a buffer to a target.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBindBuffer.xml">glBindBuffer()
   * OpenGL ES manpage</a>
   */
  public void bindBuffer(WGLWidget.GLenum target, WGLWidget.Buffer buffer) {
    this.pImpl_.bindBuffer(target, buffer);
  }
  /**
   * GL function to bind a frame buffer to a target.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBindFramebuffer.xml">glBindFramebuffer()
   * OpenGL ES manpage</a>
   */
  public void bindFramebuffer(WGLWidget.GLenum target, WGLWidget.Framebuffer buffer) {
    this.pImpl_.bindFramebuffer(target, buffer);
  }
  /**
   * GL function to bind a render buffer to a target.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBindRenderbuffer.xml">glBindRenderbuffer()
   * OpenGL ES manpage</a>
   */
  public void bindRenderbuffer(WGLWidget.GLenum target, WGLWidget.Renderbuffer buffer) {
    this.pImpl_.bindRenderbuffer(target, buffer);
  }
  /**
   * GL function to bind a texture to a target.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBindTexture.xml">glBindTexture()
   * OpenGL ES manpage</a>
   */
  public void bindTexture(WGLWidget.GLenum target, WGLWidget.Texture texture) {
    this.pImpl_.bindTexture(target, texture);
  }
  /**
   * GL function to set the blending color.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBlendColor.xml">glBlendColor()
   * OpenGL ES manpage</a>
   */
  public void blendColor(double red, double green, double blue, double alpha) {
    this.pImpl_.blendColor(red, green, blue, alpha);
  }
  /**
   * GL function to set the blending equation.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBlendEquation.xml">glBlendEquation()
   * OpenGL ES manpage</a>
   */
  public void blendEquation(WGLWidget.GLenum mode) {
    this.pImpl_.blendEquation(mode);
  }
  /**
   * GL function that sets separate blending functions for RGB and alpha.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBlendEquationSeparate.xml">glBlendEquationSeparate()
   * OpenGL ES manpage</a>
   */
  public void blendEquationSeparate(WGLWidget.GLenum modeRGB, WGLWidget.GLenum modeAlpha) {
    this.pImpl_.blendEquationSeparate(modeRGB, modeAlpha);
  }
  /**
   * GL function to configure the blending function.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBlendFunc.xml">glBlendFunc()
   * OpenGL ES manpage</a>
   */
  public void blendFunc(WGLWidget.GLenum sfactor, WGLWidget.GLenum dfactor) {
    this.pImpl_.blendFunc(sfactor, dfactor);
  }
  /**
   * GL function that configures the blending function.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBlendFuncSeparate.xml">glBlendFuncSeparate()
   * OpenGL ES manpage</a>
   */
  public void blendFuncSeparate(
      WGLWidget.GLenum srcRGB,
      WGLWidget.GLenum dstRGB,
      WGLWidget.GLenum srcAlpha,
      WGLWidget.GLenum dstAlpha) {
    this.pImpl_.blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
  }
  /**
   * glBufferData - create and initialize a buffer object&apos;s data store
   *
   * <p>Set the size of the currently bound WebGLBuffer object for the passed target. The buffer is
   * initialized to 0.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBufferData.xml">glBufferData()
   * OpenGL ES manpage</a>
   */
  public void bufferData(WGLWidget.GLenum target, int size, WGLWidget.GLenum usage) {
    this.pImpl_.bufferData(target, size, usage);
  }
  /**
   * glBufferData - create and initialize a buffer object&apos;s data store from an {@link
   * ArrayBuffer}
   *
   * <p>Set the size and contents of the currently bound WebGLBuffer object to be a copy of the
   * given {@link ArrayBuffer}.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBufferData.xml">glBufferData()
   * OpenGL ES manpage</a>
   *
   * <p>Note: an {@link ArrayBuffer} refers to a javascript object, which cannot be used for
   * server-side rendering. If a server-side fallback will be used, then {@link
   * WGLWidget#bufferDatafv(WGLWidget.GLenum target, java.nio.ByteBuffer buffer, WGLWidget.GLenum
   * usage, boolean binary) bufferDatafv()} should be used with the additional boolean argument to
   * indicate binary transfer of the data in case of client-side rendering.
   *
   * <p>
   */
  public void bufferData(
      WGLWidget.GLenum target, WGLWidget.ArrayBuffer res, WGLWidget.GLenum usage) {
    this.pImpl_.bufferData(target, res, usage);
  }
  /**
   * glBufferData - create and initialize a buffer object&apos;s data store from an {@link
   * ArrayBuffer}
   *
   * <p>Set the size of the currently bound WebGLBuffer object to arrayBufferSize, and copy the
   * contents of the {@link ArrayBuffer} to the buffer, starting at the given offset.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBufferData.xml">glBufferData()
   * OpenGL ES manpage</a>
   *
   * <p>Note: not functional for a server-side fall-back (see {@link
   * WGLWidget#bufferData(WGLWidget.GLenum target, WGLWidget.ArrayBuffer res, WGLWidget.GLenum
   * usage) bufferData()} for more info)
   *
   * <p>
   */
  public void bufferData(
      WGLWidget.GLenum target,
      WGLWidget.ArrayBuffer res,
      int bufferResourceOffset,
      int bufferResourceSize,
      WGLWidget.GLenum usage) {
    this.pImpl_.bufferData(target, res, bufferResourceOffset, bufferResourceSize, usage);
  }
  /**
   * Initialize a buffer object&apos;s data store from an {@link ArrayBuffer}.
   *
   * <p>Load the data of the currently bound WebGLBuffer object from the given {@link ArrayBuffer}.
   * The first byte of the resource data will be written at the given offset of the currently bound
   * buffer.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBufferSubData.xml">glBufferSubData()
   * OpenGL ES manpage</a>
   *
   * <p>Note: not functional for a server-side fall-back (see {@link
   * WGLWidget#bufferData(WGLWidget.GLenum target, WGLWidget.ArrayBuffer res, WGLWidget.GLenum
   * usage) bufferData()} for more info)
   *
   * <p>
   */
  public void bufferSubData(WGLWidget.GLenum target, int offset, WGLWidget.ArrayBuffer res) {
    this.pImpl_.bufferSubData(target, offset, res);
  }
  /**
   * Initialize a buffer object&apos;s data store from an {@link ArrayBuffer}.
   *
   * <p>Load the data of the currently bound WebGLBuffer object from the given {@link ArrayBuffer}.
   * The byte at position arrayBufferOffset will be written to the currently bound buffer at
   * position offset.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glBufferSubData.xml">glBufferSubData()
   * OpenGL ES manpage</a>
   *
   * <p>Note: not functional for a server-side fall-back (see {@link
   * WGLWidget#bufferData(WGLWidget.GLenum target, WGLWidget.ArrayBuffer res, WGLWidget.GLenum
   * usage) bufferData()} for more info)
   *
   * <p>
   */
  public void bufferSubData(
      WGLWidget.GLenum target,
      int offset,
      WGLWidget.ArrayBuffer res,
      int bufferResourceOffset,
      int bufferResourceSize) {
    this.pImpl_.bufferSubData(target, offset, res, bufferResourceOffset, bufferResourceSize);
  }
  /**
   * GL function that loads float or double data in a VBO.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/glBufferData.xml">glBufferData()
   * OpenGL ES manpage</a>
   */
  public void bufferDatafv(
      WGLWidget.GLenum target,
      final java.nio.ByteBuffer buffer,
      WGLWidget.GLenum usage,
      boolean binary) {
    this.pImpl_.bufferDatafv(target, buffer, usage, binary);
  }
  /**
   * GL function that loads float or double data in a VBO.
   *
   * <p>Calls {@link #bufferDatafv(WGLWidget.GLenum target, java.nio.ByteBuffer buffer,
   * WGLWidget.GLenum usage, boolean binary) bufferDatafv(target, buffer, usage, false)}
   */
  public final void bufferDatafv(
      WGLWidget.GLenum target, final java.nio.ByteBuffer buffer, WGLWidget.GLenum usage) {
    bufferDatafv(target, buffer, usage, false);
  }

  public void bufferDatafv(
      WGLWidget.GLenum target, final java.nio.FloatBuffer buffer, WGLWidget.GLenum usage) {
    this.pImpl_.bufferDatafv(target, buffer, usage);
  }
  /**
   * remove all binary buffer resources
   *
   * <p>Removes all WMemoryResources that were allocated when calling bufferDatafv with binary=true.
   * This is not required, since the resources are also managed, but if you are sure they will not
   * be used anymore in the client, this can help free some memory.
   */
  public void clearBinaryResources() {
    this.pImpl_.clearBinaryResources();
  }
  /**
   * GL function that updates an existing VBO with new integer data.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/glBufferData.xml">glBufferData()
   * OpenGL ES manpage</a>
   */
  public void bufferDataiv(
      WGLWidget.GLenum target,
      final java.nio.IntBuffer buffer,
      WGLWidget.GLenum usage,
      WGLWidget.GLenum type) {
    this.pImpl_.bufferDataiv(target, buffer, usage, type);
  }
  /**
   * GL function that updates an existing VBO with new float data.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/glBufferSubData.xml">glBufferSubData()
   * OpenGL ES manpage</a>
   */
  public void bufferSubDatafv(
      WGLWidget.GLenum target, int offset, final java.nio.ByteBuffer buffer, boolean binary) {
    this.pImpl_.bufferSubDatafv(target, offset, buffer, binary);
  }
  /**
   * GL function that updates an existing VBO with new float data.
   *
   * <p>Calls {@link #bufferSubDatafv(WGLWidget.GLenum target, int offset, java.nio.ByteBuffer
   * buffer, boolean binary) bufferSubDatafv(target, offset, buffer, false)}
   */
  public final void bufferSubDatafv(
      WGLWidget.GLenum target, int offset, final java.nio.ByteBuffer buffer) {
    bufferSubDatafv(target, offset, buffer, false);
  }

  public void bufferSubDatafv(
      WGLWidget.GLenum target, int offset, final java.nio.FloatBuffer buffer) {
    this.pImpl_.bufferSubDatafv(target, offset, buffer);
  }
  /**
   * GL function that loads integer data in a VBO.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/glBufferSubData.xml">glBufferSubData()
   * OpenGL ES manpage</a>
   */
  public void bufferSubDataiv(
      WGLWidget.GLenum target, int offset, final java.nio.IntBuffer buffer, WGLWidget.GLenum type) {
    this.pImpl_.bufferSubDataiv(target, offset, buffer, type);
  }
  /**
   * GL function that clears the given buffers.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glClear.xml">glClear()
   * OpenGL ES manpage</a>
   */
  public void clear(EnumSet<WGLWidget.GLenum> mask) {
    this.pImpl_.clear(mask);
  }
  /**
   * GL function that sets the clear color of the color buffer.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glClearColor.xml">glClearColor()
   * OpenGL ES manpage</a>
   */
  public void clearColor(double r, double g, double b, double a) {
    this.pImpl_.clearColor(r, g, b, a);
  }
  /**
   * GL function that configures the depth to be set when the depth buffer is cleared.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glClearDepthf.xml">glClearDepthf()
   * OpenGL ES manpage</a>
   */
  public void clearDepth(double depth) {
    this.pImpl_.clearDepth(depth);
  }
  /**
   * GL function.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glClearStencil.xml">glClearStencil()
   * OpenGL ES manpage</a>
   */
  public void clearStencil(int s) {
    this.pImpl_.clearStencil(s);
  }
  /**
   * GL function.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glColorMask.xml">glColorMask()
   * OpenGL ES manpage</a>
   */
  public void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
    this.pImpl_.colorMask(red, green, blue, alpha);
  }
  /**
   * GL function to compile a shader.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCompileShader.xml">glCompileShader()
   * OpenGL ES manpage</a>
   */
  public void compileShader(WGLWidget.Shader shader) {
    this.pImpl_.compileShader(shader);
  }
  /**
   * GL function to copy a texture image.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCopyTexImage2D.xml">glCopyTexImage2D()
   * OpenGL ES manpage</a>
   */
  public void copyTexImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalFormat,
      int x,
      int y,
      int width,
      int height,
      int border) {
    this.pImpl_.copyTexImage2D(target, level, internalFormat, x, y, width, height, border);
  }
  /**
   * GL function that copies a part of a texture image.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCopyTexSubImage2D.xml">glCopyTexSubImage2D()
   * OpenGL ES manpage</a>
   */
  public void copyTexSubImage2D(
      WGLWidget.GLenum target,
      int level,
      int xoffset,
      int yoffset,
      int x,
      int y,
      int width,
      int height) {
    this.pImpl_.copyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
  }
  /**
   * GL function that creates an empty VBO.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenBuffers.xml">glGenBuffers()
   * OpenGL ES manpage</a>
   */
  public WGLWidget.Buffer createBuffer() {
    return this.pImpl_.getCreateBuffer();
  }
  /**
   * GL function that creates a frame buffer object.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenFramebuffers.xml">glGenFramebuffers()
   * OpenGL ES manpage</a>
   */
  public WGLWidget.Framebuffer getCreateFramebuffer() {
    return this.pImpl_.getCreateFramebuffer();
  }
  /**
   * GL function that creates an empty program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCreateProgram.xml">glCreateProgram()
   * OpenGL ES manpage</a>
   */
  public WGLWidget.Program createProgram() {
    return this.pImpl_.getCreateProgram();
  }
  /**
   * GL function that creates a render buffer object.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenRenderbuffers.xml">glGenRenderbuffers()
   * OpenGL ES manpage</a>
   */
  public WGLWidget.Renderbuffer getCreateRenderbuffer() {
    return this.pImpl_.getCreateRenderbuffer();
  }
  /**
   * GL function that creates an empty shader.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCreateShader.xml">glCreateShader()
   * OpenGL ES manpage</a>
   */
  public WGLWidget.Shader createShader(WGLWidget.GLenum shader) {
    return this.pImpl_.createShader(shader);
  }
  /**
   * GL function that creates an empty texture.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenTextures.xml">glGenTextures()
   * OpenGL ES manpage</a>
   */
  public WGLWidget.Texture createTexture() {
    return this.pImpl_.getCreateTexture();
  }
  /**
   * GL function that creates an image texture.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenTextures.xml">glGenTextures()
   * OpenGL ES manpage</a>
   */
  public WGLWidget.Texture createTextureAndLoad(final String url) {
    return this.pImpl_.createTextureAndLoad(url);
  }
  /**
   * returns an paintdevice that can be used to paint a GL texture
   *
   * <p>If the client has a webGL enabled browser this function returns a {@link
   * WCanvasPaintDevice}.
   *
   * <p>If server-side rendering is used as fallback then this function returns a
   * WRasterPaintDevice.
   */
  public WPaintDevice createPaintDevice(final WLength width, final WLength height) {
    return this.pImpl_.createPaintDevice(width, height);
  }
  /**
   * GL function that configures the backface culling mode.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glCullFace.xml">glCullFace()
   * OpenGL ES manpage</a>
   */
  public void cullFace(WGLWidget.GLenum mode) {
    this.pImpl_.cullFace(mode);
  }
  /**
   * GL function that deletes a VBO.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteBuffers.xml">glDeleteBuffers()
   * OpenGL ES manpage</a>
   */
  public void deleteBuffer(WGLWidget.Buffer buffer) {
    this.pImpl_.deleteBuffer(buffer);
  }
  /**
   * GL function that deletes a frame buffer.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteFramebuffers.xml">glDeleteFramebuffers()
   * OpenGL ES manpage</a>
   */
  public void deleteFramebuffer(WGLWidget.Framebuffer buffer) {
    this.pImpl_.deleteFramebuffer(buffer);
  }
  /**
   * GL function that deletes a program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteProgram.xml">glDeleteProgram()
   * OpenGL ES manpage</a>
   */
  public void deleteProgram(WGLWidget.Program program) {
    this.pImpl_.deleteProgram(program);
  }
  /**
   * GL function that deletes a render buffer.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteRenderbuffers.xml">glDeleteRenderbuffers()
   * OpenGL ES manpage</a>
   */
  public void deleteRenderbuffer(WGLWidget.Renderbuffer buffer) {
    this.pImpl_.deleteRenderbuffer(buffer);
  }
  /**
   * GL function that depetes a shader.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteShader.xml">glDeleteShader()
   * OpenGL ES manpage</a>
   */
  public void deleteShader(WGLWidget.Shader shader) {
    this.pImpl_.deleteShader(shader);
  }
  /**
   * GL function that deletes a texture.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDeleteTextures.xml">glDeleteTextures()
   * OpenGL ES manpage</a>
   */
  public void deleteTexture(WGLWidget.Texture texture) {
    this.pImpl_.deleteTexture(texture);
  }
  /**
   * GL function to set the depth test function.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDepthFunc.xml">glDepthFunc()
   * OpenGL ES manpage</a>
   */
  public void depthFunc(WGLWidget.GLenum func) {
    this.pImpl_.depthFunc(func);
  }
  /**
   * GL function that enables or disables writing to the depth buffer.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDepthMask.xml">glDepthMask()
   * OpenGL ES manpage</a>
   */
  public void depthMask(boolean flag) {
    this.pImpl_.depthMask(flag);
  }
  /**
   * GL function that specifies to what range the normalized [-1,1] z values should match.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDepthRangef.xml">glDepthRangef()
   * OpenGL ES manpage</a>
   */
  public void depthRange(double zNear, double zFar) {
    this.pImpl_.depthRange(zNear, zFar);
  }
  /**
   * GL function that detaches a shader from a program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDetachShader.xml">glDetachShader()
   * OpenGL ES manpage</a>
   */
  public void detachShader(WGLWidget.Program program, WGLWidget.Shader shader) {
    this.pImpl_.detachShader(program, shader);
  }
  /**
   * GL function to disable features.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDisable.xml">glDisable()
   * OpenGL ES manpage</a>
   */
  public void disable(WGLWidget.GLenum cap) {
    this.pImpl_.disable(cap);
  }
  /**
   * GL function to disable the vertex attribute array.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDisableVertexAttribArray.xml">glDisableVertexAttribArray()
   * OpenGL ES manpage</a>
   */
  public void disableVertexAttribArray(WGLWidget.AttribLocation index) {
    this.pImpl_.disableVertexAttribArray(index);
  }
  /**
   * GL function to draw a VBO.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDrawArrays.xml">glDrawArrays()
   * OpenGL ES manpage</a>
   */
  public void drawArrays(WGLWidget.GLenum mode, int first, int count) {
    this.pImpl_.drawArrays(mode, first, count);
  }
  /**
   * GL function to draw indexed VBOs.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glDrawElements.xml">glDrawElements()
   * OpenGL ES manpage</a>
   */
  public void drawElements(WGLWidget.GLenum mode, int count, WGLWidget.GLenum type, int offset) {
    this.pImpl_.drawElements(mode, count, type, offset);
  }
  /**
   * GL function to enable features.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glEnable.xml">glEnable()
   * OpenGL ES manpage</a>
   */
  public void enable(WGLWidget.GLenum cap) {
    this.pImpl_.enable(cap);
  }
  /**
   * GL function to enable the vertex attribute array.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glEnableVertexAttribArray.xml">glEnableVertexAttribArray()
   * OpenGL ES manpage</a>
   */
  public void enableVertexAttribArray(WGLWidget.AttribLocation index) {
    this.pImpl_.enableVertexAttribArray(index);
  }
  /**
   * GL function to wait until given commands are executed.
   *
   * <p>This call is transfered to JS, but the server will never wait on this call.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFinish.xml">glFinish()
   * OpenGL ES manpage</a>
   */
  public void finish() {
    this.pImpl_.finish();
  }
  /**
   * GL function to force execution of GL commands in finite time.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFlush.xml">glFlush()
   * OpenGL ES manpage</a>
   */
  public void flush() {
    this.pImpl_.flush();
  }
  /**
   * GL function to attach the given renderbuffer to the currently bound frame buffer.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFramebufferRenderbuffer.xml">glFramebufferRenderbuffer()
   * OpenGL ES manpage</a>
   */
  public void framebufferRenderbuffer(
      WGLWidget.GLenum target,
      WGLWidget.GLenum attachment,
      WGLWidget.GLenum renderbuffertarget,
      WGLWidget.Renderbuffer renderbuffer) {
    this.pImpl_.framebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
  }
  /**
   * GL function to render directly into a texture image.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFramebufferTexture2D.xml">glFramebufferTexture2D()
   * OpenGL ES manpage</a>
   */
  public void framebufferTexture2D(
      WGLWidget.GLenum target,
      WGLWidget.GLenum attachment,
      WGLWidget.GLenum textarget,
      WGLWidget.Texture texture,
      int level) {
    this.pImpl_.framebufferTexture2D(target, attachment, textarget, texture, level);
  }
  /**
   * GL function that specifies which side of a triangle is the front side.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glFrontFace.xml">glFrontFace()
   * OpenGL ES manpage</a>
   */
  public void frontFace(WGLWidget.GLenum mode) {
    this.pImpl_.frontFace(mode);
  }
  /**
   * GL function that generates a set of mipmaps for a texture object.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGenerateMipmap.xml">glGenerateMipmap()
   * OpenGL ES manpage</a>
   */
  public void generateMipmap(WGLWidget.GLenum target) {
    this.pImpl_.generateMipmap(target);
  }
  /**
   * GL function to retrieve an attribute&apos;s location in a {@link Program}.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGetAttribLocation.xml">glGetAttribLocation()
   * OpenGL ES manpage</a>
   */
  public WGLWidget.AttribLocation getAttribLocation(
      WGLWidget.Program program, final String attrib) {
    return this.pImpl_.getAttribLocation(program, attrib);
  }
  /**
   * GL function to retrieve a Uniform&apos;s location in a {@link Program}.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glGetUniformLocation.xml">glGetUniformLocation()
   * OpenGL ES manpage</a>
   */
  public WGLWidget.UniformLocation getUniformLocation(
      WGLWidget.Program program, final String location) {
    return this.pImpl_.getUniformLocation(program, location);
  }
  /**
   * GL function to give hints to the render pipeline.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glHint.xml">glHint() OpenGL
   * ES manpage</a>
   */
  public void hint(WGLWidget.GLenum target, WGLWidget.GLenum mode) {
    this.pImpl_.hint(target, mode);
  }
  /**
   * GL function to set the line width.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glLineWidth.xml">glLineWidth()
   * OpenGL ES manpage</a>
   */
  public void lineWidth(double width) {
    this.pImpl_.lineWidth(width);
  }
  /**
   * GL function to link a program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glLinkProgram.xml">glLinkProgram()
   * OpenGL ES manpage</a>
   */
  public void linkProgram(WGLWidget.Program program) {
    this.pImpl_.linkProgram(program);
  }
  /**
   * GL function to set the pixel storage mode.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glPixelStorei.xml">glPixelStorei()
   * OpenGL ES manpage</a>
   */
  public void pixelStorei(WGLWidget.GLenum pname, int param) {
    this.pImpl_.pixelStorei(pname, param);
  }
  /**
   * GL function to apply modifications to Z values.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glPolygonOffset.xml">glPolygonOffset()
   * OpenGL ES manpage</a>
   */
  public void polygonOffset(double factor, double units) {
    this.pImpl_.polygonOffset(factor, units);
  }
  /**
   * GL function to allocate the appropriate amount of memory for a render buffer.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glRenderbufferStorage.xml">glSampleCoverage()
   * OpenGL ES manpage</a>
   */
  public void renderbufferStorage(
      WGLWidget.GLenum target, WGLWidget.GLenum internalformat, int width, int height) {
    this.pImpl_.renderbufferStorage(target, internalformat, width, height);
  }
  /**
   * GL function to set multisample parameters.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glSampleCoverage.xml">glSampleCoverage()
   * OpenGL ES manpage</a>
   */
  public void sampleCoverage(double value, boolean invert) {
    this.pImpl_.sampleCoverage(value, invert);
  }
  /**
   * GL function to define the scissor box.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glScissor.xml">glScissor()
   * OpenGL ES manpage</a>
   */
  public void scissor(int x, int y, int width, int height) {
    this.pImpl_.scissor(x, y, width, height);
  }
  /**
   * GL function to set a shader&apos;s source code.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glShaderSource.xml">glShaderSource()
   * OpenGL ES manpage</a>
   */
  public void shaderSource(WGLWidget.Shader shader, final String src) {
    this.pImpl_.shaderSource(shader, src);
  }
  /**
   * GL function to set stencil test parameters.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilFunc.xml">glStencilFunc()
   * OpenGL ES manpage</a>
   */
  public void stencilFunc(WGLWidget.GLenum func, int ref, int mask) {
    this.pImpl_.stencilFunc(func, ref, mask);
  }
  /**
   * GL function to set stencil test parameters for front and/or back stencils.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilFuncSeparate.xml">glStencilFuncSeparate()
   * OpenGL ES manpage</a>
   */
  public void stencilFuncSeparate(WGLWidget.GLenum face, WGLWidget.GLenum func, int ref, int mask) {
    this.pImpl_.stencilFuncSeparate(face, func, ref, mask);
  }
  /**
   * GL function to control which bits are to be written in the stencil buffer.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilMask.xml">glStencilMask()
   * OpenGL ES manpage</a>
   */
  public void stencilMask(int mask) {
    this.pImpl_.stencilMask(mask);
  }
  /**
   * GL function to control which bits are written to the front and/or back stencil buffers.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilMaskSeparate.xml">glStencilMaskSeparate()
   * OpenGL ES manpage</a>
   */
  public void stencilMaskSeparate(WGLWidget.GLenum face, int mask) {
    this.pImpl_.stencilMaskSeparate(face, mask);
  }
  /**
   * GL function to set stencil test actions.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilOp.xml">glStencilOp()
   * OpenGL ES manpage</a>
   */
  public void stencilOp(WGLWidget.GLenum fail, WGLWidget.GLenum zfail, WGLWidget.GLenum zpass) {
    this.pImpl_.stencilOp(fail, zfail, zpass);
  }
  /**
   * GL function to set front and/or back stencil test actions separately.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glStencilOpSeparate.xml">glStencilOpSeparate()
   * OpenGL ES manpage</a>
   */
  public void stencilOpSeparate(
      WGLWidget.GLenum face,
      WGLWidget.GLenum fail,
      WGLWidget.GLenum zfail,
      WGLWidget.GLenum zpass) {
    this.pImpl_.stencilOpSeparate(face, fail, zfail, zpass);
  }
  /**
   * GL function to reserve space for a 2D texture, without specifying its contents.
   *
   * <p>This corresponds to calling the WebGL function void texImage2D(GLenum target, GLint level,
   * GLenum internalformat, GLsizei width, GLsizei height, GLint border, GLenum format, GLenum type,
   * ArrayBufferView pixels) with null as last parameters. The value of &apos;type&apos; is then of
   * no importance and is therefore omitted from this function.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml">glTexImage2D()
   * OpenGL ES manpage</a>
   */
  public void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      int width,
      int height,
      int border,
      WGLWidget.GLenum format) {
    this.pImpl_.texImage2D(target, level, internalformat, width, height, border, format);
  }
  /**
   * GL function to load a 2D texture from a {@link WImage}.
   *
   * <p>Note: {@link WImage} must be loaded before this function is executed.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml">glTexImage2D()
   * OpenGL ES manpage</a>
   */
  public void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      WGLWidget.GLenum format,
      WGLWidget.GLenum type,
      WImage image) {
    this.pImpl_.texImage2D(target, level, internalformat, format, type, image);
  }
  /**
   * GL function to load a 2D texture from a {@link WVideo}.
   *
   * <p>Note: the video must be loaded prior to calling this function. The current frame is used as
   * texture image.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml">glTexImage2D()
   * OpenGL ES manpage</a>
   */
  public void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      WGLWidget.GLenum format,
      WGLWidget.GLenum type,
      WVideo video) {
    this.pImpl_.texImage2D(target, level, internalformat, format, type, video);
  }
  /**
   * GL function to load a 2D texture from a file.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml">glTexImage2D()
   * OpenGL ES manpage</a>
   */
  public void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      WGLWidget.GLenum format,
      WGLWidget.GLenum type,
      String imgFilename) {
    this.pImpl_.texImage2D(target, level, internalformat, format, type, imgFilename);
  }
  /**
   * GL function to load a 2D texture from a {@link WPaintDevice}.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml">glTexImage2D()
   * OpenGL ES manpage</a>
   *
   * <p>
   *
   * @see WGLWidget#createPaintDevice(WLength width, WLength height)
   */
  public void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      WGLWidget.GLenum format,
      WGLWidget.GLenum type,
      WPaintDevice paintdevice) {
    this.pImpl_.texImage2D(target, level, internalformat, format, type, paintdevice);
  }
  /**
   * GL function to load a 2D texture loaded with {@link WGLWidget#createTextureAndLoad(String url)
   * createTextureAndLoad()}
   *
   * <p>This function must only be used for textures created with {@link
   * WGLWidget#createTextureAndLoad(String url) createTextureAndLoad()}
   *
   * <p>Note: the {@link WGLWidget} implementation will delay rendering until all textures created
   * with {@link WGLWidget#createTextureAndLoad(String url) createTextureAndLoad()} are loaded in
   * the browser.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexImage2D.xml">glTexImage2D()
   * OpenGL ES manpage</a>
   */
  public void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      WGLWidget.GLenum format,
      WGLWidget.GLenum type,
      WGLWidget.Texture texture) {
    this.pImpl_.texImage2D(target, level, internalformat, format, type, texture);
  }
  /**
   * GL function to set texture parameters.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glTexParameter.xml">glTexParameter()
   * OpenGL ES manpage</a>
   */
  public void texParameteri(
      WGLWidget.GLenum target, WGLWidget.GLenum pname, WGLWidget.GLenum param) {
    this.pImpl_.texParameteri(target, pname, param);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform1f(final WGLWidget.UniformLocation location, double x) {
    this.pImpl_.uniform1f(location, x);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform1fv(final WGLWidget.UniformLocation location, float[] value) {
    this.pImpl_.uniform1fv(location, value);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform1fv(
      final WGLWidget.UniformLocation location, final WGLWidget.JavaScriptVector v) {
    this.pImpl_.uniform1fv(location, v);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform1i(final WGLWidget.UniformLocation location, int x) {
    this.pImpl_.uniform1i(location, x);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform1iv(final WGLWidget.UniformLocation location, int[] value) {
    this.pImpl_.uniform1iv(location, value);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform2f(final WGLWidget.UniformLocation location, double x, double y) {
    this.pImpl_.uniform2f(location, x, y);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform2fv(final WGLWidget.UniformLocation location, float[] value) {
    this.pImpl_.uniform2fv(location, value);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform2fv(
      final WGLWidget.UniformLocation location, final WGLWidget.JavaScriptVector v) {
    this.pImpl_.uniform2fv(location, v);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform2i(final WGLWidget.UniformLocation location, int x, int y) {
    this.pImpl_.uniform2i(location, x, y);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform2iv(final WGLWidget.UniformLocation location, int[] value) {
    this.pImpl_.uniform2iv(location, value);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform3f(final WGLWidget.UniformLocation location, double x, double y, double z) {
    this.pImpl_.uniform3f(location, x, y, z);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform3fv(final WGLWidget.UniformLocation location, float[] value) {
    this.pImpl_.uniform3fv(location, value);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform3fv(
      final WGLWidget.UniformLocation location, final WGLWidget.JavaScriptVector v) {
    this.pImpl_.uniform3fv(location, v);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform3i(final WGLWidget.UniformLocation location, int x, int y, int z) {
    this.pImpl_.uniform3i(location, x, y, z);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform3iv(final WGLWidget.UniformLocation location, int[] value) {
    this.pImpl_.uniform3iv(location, value);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform4f(
      final WGLWidget.UniformLocation location, double x, double y, double z, double w) {
    this.pImpl_.uniform4f(location, x, y, z, w);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform4fv(final WGLWidget.UniformLocation location, float[] value) {
    this.pImpl_.uniform4fv(location, value);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform4fv(
      final WGLWidget.UniformLocation location, final WGLWidget.JavaScriptVector v) {
    this.pImpl_.uniform4fv(location, v);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform4i(final WGLWidget.UniformLocation location, int x, int y, int z, int w) {
    this.pImpl_.uniform4i(location, x, y, z, w);
  }
  /**
   * GL function to set the value of a uniform variable of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniform4iv(final WGLWidget.UniformLocation location, int[] value) {
    this.pImpl_.uniform4iv(location, value);
  }
  /**
   * GL function to set the value of a uniform matrix of the current program.
   *
   * <p>Attention: The OpenGL ES specification states that transpose MUST be false.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniformMatrix2fv(
      final WGLWidget.UniformLocation location, boolean transpose, double[] value) {
    this.pImpl_.uniformMatrix2fv(location, transpose, value);
  }
  /**
   * GL function to set the value of a uniform matrix of the current program.
   *
   * <p>This function renders the matrix in the proper row/column order.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniformMatrix2(final WGLWidget.UniformLocation location, final Matrix2f m) {
    this.pImpl_.uniformMatrix2(location, m);
  }
  /**
   * GL function to set the value of a uniform matrix of the current program.
   *
   * <p>Attention: The OpenGL ES specification states that transpose MUST be false.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniformMatrix3fv(
      final WGLWidget.UniformLocation location, boolean transpose, double[] value) {
    this.pImpl_.uniformMatrix3fv(location, transpose, value);
  }
  /**
   * GL function to set the value of a uniform matrix of the current program.
   *
   * <p>This function renders the matrix in the proper row/column order.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniformMatrix3(
      final WGLWidget.UniformLocation location, final javax.vecmath.Matrix3f m) {
    this.pImpl_.uniformMatrix3(location, m);
  }
  /**
   * GL function to set the value of a uniform matrix of the current program.
   *
   * <p>Attention: The OpenGL ES specification states that transpose MUST be false.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniformMatrix4fv(
      final WGLWidget.UniformLocation location, boolean transpose, double[] value) {
    this.pImpl_.uniformMatrix4fv(location, transpose, value);
  }
  /**
   * GL function to set the value of a uniform matrix of the current program.
   *
   * <p>This function renders the matrix in the proper row/column order.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniformMatrix4(
      final WGLWidget.UniformLocation location, final javax.vecmath.Matrix4f m) {
    this.pImpl_.uniformMatrix4(location, m);
  }
  /**
   * GL function to set the value of a uniform matrix of the current program.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUniform.xml">glUniform()
   * OpenGL ES manpage</a>
   */
  public void uniformMatrix4(
      final WGLWidget.UniformLocation location, final WGLWidget.JavaScriptMatrix4x4 jsm) {
    if (!jsm.isInitialized()) {
      throw new WException("JavaScriptMatrix4x4: matrix not initialized");
    }
    this.pImpl_.uniformMatrix4(location, jsm);
  }
  /**
   * GL function to set the current active shader program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glUseProgram.xml">glUseProgram()
   * OpenGL ES manpage</a>
   */
  public void useProgram(WGLWidget.Program program) {
    this.pImpl_.useProgram(program);
  }
  /**
   * GL function to validate a program.
   *
   * <p>implementation note: there is currently not yet a method to read out the validation result.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glValidateProgram.xml">glValidateProgram()
   * OpenGL ES manpage</a>
   */
  public void validateProgram(WGLWidget.Program program) {
    this.pImpl_.validateProgram(program);
  }
  /**
   * GL function to set the value of an attribute of the current program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml">glVertexAttrib()
   * OpenGL ES manpage</a>
   */
  public void vertexAttrib1f(WGLWidget.AttribLocation location, double x) {
    this.pImpl_.vertexAttrib1f(location, x);
  }
  /**
   * GL function to set the value of an attribute of the current program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml">glVertexAttrib()
   * OpenGL ES manpage</a>
   */
  public void vertexAttrib2f(WGLWidget.AttribLocation location, double x, double y) {
    this.pImpl_.vertexAttrib2f(location, x, y);
  }
  /**
   * GL function to set the value of an attribute of the current program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml">glVertexAttrib()
   * OpenGL ES manpage</a>
   */
  public void vertexAttrib2fv(WGLWidget.AttribLocation location, float[] values) {
    this.vertexAttrib2f(location, values[0], values[1]);
  }
  /**
   * GL function to set the value of an attribute of the current program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml">glVertexAttrib()
   * OpenGL ES manpage</a>
   */
  public void vertexAttrib3f(WGLWidget.AttribLocation location, double x, double y, double z) {
    this.pImpl_.vertexAttrib3f(location, x, y, z);
  }
  /**
   * GL function to set the value of an attribute of the current program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml">glVertexAttrib()
   * OpenGL ES manpage</a>
   */
  public void vertexAttrib3fv(WGLWidget.AttribLocation location, float[] values) {
    this.vertexAttrib3f(location, values[0], values[1], values[2]);
  }
  /**
   * GL function to set the value of an attribute of the current program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml">glVertexAttrib()
   * OpenGL ES manpage</a>
   */
  public void vertexAttrib4f(
      WGLWidget.AttribLocation location, double x, double y, double z, double w) {
    this.pImpl_.vertexAttrib4f(location, x, y, z, w);
  }
  /**
   * GL function to set the value of an attribute of the current program.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttrib.xml">glVertexAttrib()
   * OpenGL ES manpage</a>
   */
  public void vertexAttrib4fv(WGLWidget.AttribLocation location, float[] values) {
    this.vertexAttrib4f(location, values[0], values[1], values[2], values[3]);
  }
  /**
   * GL function to bind a VBO to an attribute.
   *
   * <p>This function links the given attribute to the VBO currently bound to the ARRAY_BUFFER
   * target.
   *
   * <p>The size parameter specifies the number of components per attribute (1 to 4). The type
   * parameter is also used to determine the size of each component.
   *
   * <p>The size of a float is 8 bytes.
   *
   * <p>In {@link WGLWidget}, the size of an int is 4 bytes.
   *
   * <p>The stride is in bytes.
   *
   * <p>The maximum stride is 255.
   *
   * <p><a
   * href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glVertexAttribPointer.xml">glVertexAttribPointer()
   * OpenGL ES manpage</a>
   */
  public void vertexAttribPointer(
      WGLWidget.AttribLocation location,
      int size,
      WGLWidget.GLenum type,
      boolean normalized,
      int stride,
      int offset) {
    this.pImpl_.vertexAttribPointer(location, size, type, normalized, stride, offset);
  }
  /**
   * GL function to set the viewport.
   *
   * <p><a href="http://www.khronos.org/opengles/sdk/2.0/docs/man/xhtml/glViewport.xml">glViewport()
   * OpenGL ES manpage</a>
   */
  public void viewport(int x, int y, int width, int height) {
    this.pImpl_.viewport(x, y, width, height);
  }
  /**
   * Create a matrix that can be manipulated in client-side JavaScript.
   *
   * <p>This is a shorthand for creating a {@link JavaScriptMatrix4x4}, then adding it to a {@link
   * WGLWidget} with addJavaScriptMatrix4, and initializing it with initJavaScriptMatrix4.
   *
   * <p>This method should only be called in {@link WGLWidget#initializeGL() initializeGL()}, {@link
   * WGLWidget#updateGL() updateGL()} or {@link WGLWidget#resizeGL(int width, int height)
   * resizeGL()}.
   */
  public WGLWidget.JavaScriptMatrix4x4 createJavaScriptMatrix4() {
    WGLWidget.JavaScriptMatrix4x4 mat = new WGLWidget.JavaScriptMatrix4x4();
    this.addJavaScriptMatrix4(mat);
    this.initJavaScriptMatrix4(mat);
    return mat;
  }
  /**
   * Register a matrix with this {@link WGLWidget}.
   *
   * <p>You can call this outside of {@link WGLWidget#resizeGL(int width, int height) resizeGL()},
   * {@link WGLWidget#paintGL() paintGL()}, {@link WGLWidget#updateGL() updateGL()} or {@link
   * WGLWidget#initializeGL() initializeGL()} methods. After a {@link JavaScriptMatrix4x4} is added
   * to a {@link WGLWidget}, its {@link WWidget#getJsRef()} becomes valid, and can be used in a
   * {@link JSlot}, for example.
   */
  public void addJavaScriptMatrix4(final WGLWidget.JavaScriptMatrix4x4 mat) {
    if (mat.hasContext()) {
      throw new WException("The given matrix is already associated with a WGLWidget!");
    }
    mat.assignToContext(this.jsValues_++, this);
    this.jsMatrixList_.add(
        new WGLWidget.jsMatrixMap(
            mat.getId(),
            new javax.vecmath.Matrix4f(
                1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f)));
  }
  /**
   * Initialize the client-side JavaScript for the given {@link JavaScriptMatrix4x4}.
   *
   * <p>If the given matrix is not associated with a widget yet, it will be added to this widget.
   *
   * <p>If the given matrix has already been added to a {@link WGLWidget}, then this {@link
   * WGLWidget} should be the same as the one you call initJavaScriptMatrix4 on.
   *
   * <p>This method should only be called in {@link WGLWidget#initializeGL() initializeGL()}, {@link
   * WGLWidget#updateGL() updateGL()} or {@link WGLWidget#resizeGL(int width, int height)
   * resizeGL()}.
   */
  public void initJavaScriptMatrix4(final WGLWidget.JavaScriptMatrix4x4 mat) {
    this.pImpl_.initJavaScriptMatrix4(mat);
  }
  /**
   * Set the value of a client-side JavaScript matrix created by createJavaScriptMatrix4x4()
   *
   * <p>This method should only be called in {@link WGLWidget#initializeGL() initializeGL()}, {@link
   * WGLWidget#updateGL() updateGL()} or {@link WGLWidget#resizeGL(int width, int height)
   * resizeGL()}.
   */
  public void setJavaScriptMatrix4(
      final WGLWidget.JavaScriptMatrix4x4 jsm, final javax.vecmath.Matrix4f m) {
    if (!jsm.isInitialized()) {
      throw new WException("JavaScriptMatrix4x4: matrix not initialized");
    }
    if (jsm.hasOperations()) {
      throw new WException("JavaScriptMatrix4x4: matrix was already operated on");
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
   * Create a vector of a certain length that can be manipulated in client-side JavaScript.
   *
   * <p>This is a shorthand for creating a {@link JavaScriptVector}, then adding it to a {@link
   * WGLWidget} with addJavaScriptVector, and initializing it with initJavaScriptVector.
   *
   * <p>This method should only be called in {@link WGLWidget#initializeGL() initializeGL()}, {@link
   * WGLWidget#updateGL() updateGL()} or {@link WGLWidget#resizeGL(int width, int height)
   * resizeGL()}.
   */
  public WGLWidget.JavaScriptVector createJavaScriptVector(int length) {
    WGLWidget.JavaScriptVector vec = new WGLWidget.JavaScriptVector(length);
    this.addJavaScriptVector(vec);
    this.initJavaScriptVector(vec);
    return vec;
  }
  /**
   * Register a vector with this {@link WGLWidget}.
   *
   * <p>You can call this outside of {@link WGLWidget#resizeGL(int width, int height) resizeGL()},
   * {@link WGLWidget#paintGL() paintGL()}, {@link WGLWidget#updateGL() updateGL()} or {@link
   * WGLWidget#initializeGL() initializeGL()} methods. After a {@link JavaScriptVector} is added to
   * a {@link WGLWidget}, its {@link WWidget#getJsRef()} becomes valid, and can be used in a {@link
   * JSlot}, for example.
   */
  public void addJavaScriptVector(final WGLWidget.JavaScriptVector vec) {
    if (vec.hasContext()) {
      throw new WException("The given matrix is already associated with a WGLWidget!");
    }
    vec.assignToContext(this.jsValues_++, this);
    List<Float> values = new ArrayList<Float>();
    for (int i = 0; i < vec.getLength(); ++i) {
      values.add(0.0f);
    }
    this.jsVectorList_.add(new WGLWidget.jsVectorMap(vec.getId(), values));
  }
  /**
   * Initialize the client-side JavaScript for the given {@link JavaScriptVector}.
   *
   * <p>If the given vector is not associated with a widget yet, it will be added to this widget.
   *
   * <p>If the given vector has already been added to a {@link WGLWidget}, then this {@link
   * WGLWidget} should be the same as the one you call initJavaScriptVector on.
   *
   * <p>This method should only be called in {@link WGLWidget#initializeGL() initializeGL()}, {@link
   * WGLWidget#updateGL() updateGL()} or {@link WGLWidget#resizeGL(int width, int height)
   * resizeGL()}.
   */
  public void initJavaScriptVector(final WGLWidget.JavaScriptVector vec) {
    this.pImpl_.initJavaScriptVector(vec);
  }
  /**
   * Set the value of a client-side JavaScript vector created by {@link
   * WGLWidget#createJavaScriptVector(int length) createJavaScriptVector()}
   *
   * <p>This method should only be called in {@link WGLWidget#initializeGL() initializeGL()}, {@link
   * WGLWidget#updateGL() updateGL()} or {@link WGLWidget#resizeGL(int width, int height)
   * resizeGL()}.
   */
  public void setJavaScriptVector(final WGLWidget.JavaScriptVector jsv, final List<Float> v) {
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
   *
   * <p>The handler code should be JavaScript code that produces an object when evaluated.
   *
   * <p>A mouse handler is an object that can implement one or more of the following functions:
   *
   * <p>
   *
   * <ul>
   *   <li><b>setTarget(target)</b>: This is called immediately when the mouse handler is added with
   *       an object that uniquely identifies the {@link WGLWidget}, and a {@link
   *       WGLWidget#paintGL() paintGL()} method.
   *   <li><b>mouseDown(o, event)</b>: To handle the <code>mousedown</code> event. <code>o</code> is
   *       the <code>&lt;canvas&gt;</code> (client-side rendering) or <code>&lt;img&gt;</code>
   *       (server-side rendering) element corresponding to this {@link WGLWidget}. <code>event
   *       </code> is the <code>MouseEvent</code>.
   *   <li><b>mouseUp(o, event)</b>: To handle the <code>mouseup</code> event. <code>o</code> is the
   *       <code>&lt;canvas&gt;</code> (client-side rendering) or <code>&lt;img&gt;</code>
   *       (server-side rendering) element corresponding to this {@link WGLWidget}. <code>event
   *       </code> is the <code>MouseEvent</code>.
   *   <li><b>mouseDrag(o, event)</b>: Called when the mouse is dragged. <code>o</code> is the
   *       <code>&lt;canvas&gt;</code> (client-side rendering) or <code>&lt;img&gt;</code>
   *       (server-side rendering) element corresponding to this {@link WGLWidget}. <code>event
   *       </code> is the <code>MouseEvent</code>.
   *   <li><b>mouseMove(o, event)</b>: Called when the mouse is moved. <code>o</code> is the <code>
   *       &lt;canvas&gt;</code> (client-side rendering) or <code>&lt;img&gt;</code> (server-side
   *       rendering) element corresponding to this {@link WGLWidget}. <code>event</code> is the
   *       <code>MouseEvent</code>.
   *   <li><b>mouseWheel(o, event)</b>: Called when the mouse wheel is used. <code>o</code> is the
   *       <code>&lt;canvas&gt;</code> (client-side rendering) or <code>&lt;img&gt;</code>
   *       (server-side rendering) element corresponding to this {@link WGLWidget}. <code>event
   *       </code> is the <code>MouseEvent</code>.
   *   <li><b>touchStart(o, event)</b>: To handle the <code>touchstart</code> event. <code>o</code>
   *       is the <code>&lt;canvas&gt;</code> (client-side rendering) or <code>&lt;img&gt;</code>
   *       (server-side rendering) element corresponding to this {@link WGLWidget}. <code>event
   *       </code> is the <code>TouchEvent</code>.
   *   <li><b>touchEnd(o, event)</b>: To handle the <code>touchend</code> event. <code>o</code> is
   *       this <code>&lt;canvas&gt;</code> (client-side rendering) or <code>&lt;img&gt;</code>
   *       (server-side rendering) element corresponding to this {@link WGLWidget}. <code>event
   *       </code> is the <code>TouchEvent</code>.
   *   <li><b>touchMoved(o, event)</b>: To handle the <code>touchmove</code> event. <code>o</code>
   *       is this <code>&lt;canvas&gt;</code> (client-side rendering) or <code>&lt;img&gt;</code>
   *       (server-side rendering) element corresponding to this {@link WGLWidget}. <code>event
   *       </code> is the <code>TouchEvent</code>.
   * </ul>
   *
   * <p>For example, if we wanted to scale some object when we scroll, we could create a
   * JavaScriptMatrix4x4 called \p transform_:
   *
   * <p>
   *
   * <pre>{@code
   * private JavaScriptMatrix4x4 transform_;
   *
   * }</pre>
   *
   * <p>We can add this in the constructor:
   *
   * <p>
   *
   * <pre>{@code
   * transform_ = new JavaScriptMatrix4x4();
   * addJavaScriptMatrix4(transform_);
   *
   * }</pre>
   *
   * <p>Then, in {@link WGLWidget#initializeGL() initializeGL()}, we can initialize it and set the
   * value:
   *
   * <p>
   *
   * <pre>{@code
   * initJavaScriptMatrix4(transform_);
   * setJavaScriptMatrix4(transform_, new WMatrix4x4()); // Set to identity matrix
   *
   * }</pre>
   *
   * <p>Then, still in {@link WGLWidget#initializeGL() initializeGL()}, we can set a mouse handler
   * as such:
   *
   * <p>
   *
   * <pre>{@code
   * setClientSideMouseHandler("(function(){") +
   * "var MouseHandler = function(transform) {" +
   * "var target = null;" +
   * "this.setTarget = function(newTarget) {" +
   * "target = newTarget;" +
   * "};" +
   * "this.mouseWheel = function(o, event) {" +
   * "var fix = jQuery.event.fix(event);" +
   * "fix.preventDefault();" +
   * "fix.stopPropagation();" +
   * "var d = wheelDelta(event);" +
   * "var s = Math.pow(1.2, d);" +
   * "transform[0] *= s;" + // Scale X
   * "transform[5] *= s;" + // Scale Y
   * "transform[10] *= s;" + // Scale Z
   * "target.paintGL();" + // Repaint
   * "};" +
   * "function wheelDelta(e) {" +
   * "var delta = 0;" +
   * "if (e.wheelDelta) {" +
   * "delta = e.wheelDelta > 0 ? 1 : -1;" +
   * "} else if (e.detail) {" +
   * "delta = e.detail < 0 ? 1 : -1;" +
   * "}" +
   * "return delta;" +
   * "}" +
   * "};" +
   * "return new MouseHandler(" + transform_.jsRef() + ");" +
   * "})()");
   *
   * }</pre>
   *
   * <p>All that&apos;s left to do then is to use this transform somewhere as a uniform variable,
   * see {@link WGLWidget#getUniformLocation(WGLWidget.Program program, String location)
   * getUniformLocation()} and {@link WGLWidget#uniformMatrix4(WGLWidget.UniformLocation location,
   * javax.vecmath.Matrix4f m) uniformMatrix4()}.
   */
  public void setClientSideMouseHandler(final String handlerCode) {
    this.pImpl_.setClientSideMouseHandler(handlerCode);
  }
  /**
   * Add a mouse handler to the widget that looks at a given point.
   *
   * <p>This will allow a user to change client-side matrix m with the mouse. M is a model
   * transformation matrix, representing the viewpoint of the camera.
   *
   * <p>Through mouse operations, the camera can be changed by the user, but (lX, lY, lZ) will
   * always be at the center of the display, (uX, uY, uZ) is considered to be the up direction, and
   * the distance of the camera to (lX, lY, lZ) will never change.
   *
   * <p>Pressing the left mouse button and moving the mouse left/right will rotate the camera around
   * the up (uX, uY, uZ) direction. Moving up/down will tilt the camera (causing it to move up/down
   * to keep the lookpoint centered). The scroll wheel simulates zooming by scaling the scene.
   *
   * <p>pitchRate and yawRate control how much the camera will move per mouse pixel.
   *
   * <p>Usually this method is called after setting a camera transformation with a client-side
   * matrix in {@link WGLWidget#initializeGL() initializeGL()}. However, this function may also be
   * called from outside the intializeGL()/paintGL()/updateGL() methods (but not before m was
   * initialized).
   */
  public void setClientSideLookAtHandler(
      final WGLWidget.JavaScriptMatrix4x4 m,
      double centerX,
      double centerY,
      double centerZ,
      double uX,
      double uY,
      double uZ,
      double pitchRate,
      double yawRate) {
    this.pImpl_.setClientSideLookAtHandler(
        m, centerX, centerY, centerZ, uX, uY, uZ, pitchRate, yawRate);
  }
  /**
   * Add a mouse handler to the widget that allows &apos;walking&apos; in the scene.
   *
   * <p>This will allow a user to change client-side matrix m with the mouse. M is a model
   * transformation matrix, representing the viewpoint of the camera.
   *
   * <p>Through mouse operations, the camera can be changed by the user, as if he is walking around
   * on a plane.
   *
   * <p>Pressing the left mouse button and moving the mouse left/right will rotate the camera around
   * Y axis. Moving the mouse up/down will move the camera in the Z direction (walking
   * forward/backward). centered).
   *
   * <p>frontStep and rotStep control how much the camera will move per mouse pixel.
   */
  public void setClientSideWalkHandler(
      final WGLWidget.JavaScriptMatrix4x4 m, double frontStep, double rotStep) {
    this.pImpl_.setClientSideWalkHandler(m, frontStep, rotStep);
  }
  /**
   * Sets the content to be displayed when WebGL is not available.
   *
   * <p>If JWt cannot create a working WebGL context, this content will be shown to the user. This
   * may be a text explanation, or a pre-rendered image, or a video, a flash movie, ...
   *
   * <p>The default is a widget that explains to the user that he has no WebGL support.
   */
  public void setAlternativeContent(WWidget alternative) {
    {
      WWidget oldWidget = this.alternative_;
      this.alternative_ = alternative;
      {
        WWidget toRemove = this.manageWidget(oldWidget, this.alternative_);
        if (toRemove != null) toRemove.remove();
      }
    }
  }
  /**
   * A JavaScript slot that repaints the widget when triggered.
   *
   * <p>This is useful for client-side initiated repaints. You may e.g. use this if you write your
   * own client-side mouse handler, or if you updated a texture, or if you&apos;re playing a video
   * texture.
   */
  public JSlot getRepaintSlot() {
    return this.repaintSlot_;
  }
  /**
   * enable client-side error messages (read detailed doc!)
   *
   * <p>This option will add client-side code to check the result of every WebGL call, and will
   * popup an error dialog if a WebGL call returned an error. The JavaScript then invokes the
   * client-side debugger. This code is intended to test your application, and should not be used in
   * production.
   */
  public void enableClientErrorChecks(boolean enable) {
    this.pImpl_.enableClientErrorChecks(enable);
  }
  /**
   * enable client-side error messages (read detailed doc!)
   *
   * <p>Calls {@link #enableClientErrorChecks(boolean enable) enableClientErrorChecks(true)}
   */
  public final void enableClientErrorChecks() {
    enableClientErrorChecks(true);
  }
  /**
   * Inject JavaScript into the current js-stream.
   *
   * <p>Careful: this method directly puts the given jsString into the JavaScript stream, whatever
   * state it current has. For example, if called in initGL(), it will put the jsString into the
   * client-side initGL() code.
   */
  public void injectJS(final String jsString) {
    this.pImpl_.injectJS(jsString);
  }

  public void webglNotAvailable() {
    System.out.append("WebGL Not available in client!\n");
    this.webGlNotAvailable_ = true;
  }

  DomElementType getDomElementType() {
    if (ObjectUtils.cast(this.pImpl_, WClientGLWidget.class) != null) {
      return DomElementType.CANVAS;
    } else {
      return DomElementType.IMG;
    }
  }

  protected DomElement createDomElement(WApplication app) {
    DomElement result = null;
    if (!(this.pImpl_ != null)) {
      result = DomElement.createNew(DomElementType.DIV);
      result.addChild(this.alternative_.createSDomElement(app));
      this.webGlNotAvailable_ = true;
    } else {
      result = DomElement.createNew(this.getDomElementType());
      this.repaintGL(EnumSet.of(GLClientSideRenderer.PAINT_GL, GLClientSideRenderer.RESIZE_GL));
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
    if (flags.contains(RenderFlag.Full)) {
      if (!(this.pImpl_ != null)) {
        if (this.renderOptions_.contains(GLRenderOption.ClientSide)
            && WApplication.getInstance().getEnvironment().hasWebGL()) {
          this.pImpl_ = new WClientGLWidget(this);
        } else {
          if (!EnumUtils.mask(this.renderOptions_, GLRenderOption.ServerSide).isEmpty()) {
            this.pImpl_ = new WServerGLWidget(this);
          } else {
            this.pImpl_ = null;
          }
        }
      }
      if (this.pImpl_ != null && !this.getWidth().isAuto() && !this.getHeight().isAuto()) {
        this.layoutSizeChanged((int) this.getWidth().toPixels(), (int) this.getHeight().toPixels());
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
      return this.alternative_.renderRemoveJs(recursive);
    } else {
      return super.renderRemoveJs(recursive);
    }
  }

  protected void layoutSizeChanged(int width, int height) {
    this.pImpl_.layoutSizeChanged(width, height);
    this.repaintGL(EnumSet.of(GLClientSideRenderer.RESIZE_GL));
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
      idAndData = new ArrayList<String>(Arrays.asList(matrices.get(i).split(":")));
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
        mData = new ArrayList<String>(Arrays.asList(idAndData.get(1).split(",")));
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
        mData = new ArrayList<String>(Arrays.asList(idAndData.get(1).split(",")));
        for (int i1 = 0; i1 < 4; i1++) {
          for (int i2 = 0; i2 < 4; i2++) {
            mat.setElement(i2, i1, Float.parseFloat(mData.get(i1 * 4 + i2)));
          }
        }
      }
    }
  }

  protected void contextRestored() {
    this.restoringContext_ = true;
    this.pImpl_.restoreContext(this.getJsRef());
    this.repaintGL(
        EnumSet.of(
            GLClientSideRenderer.UPDATE_GL,
            GLClientSideRenderer.RESIZE_GL,
            GLClientSideRenderer.PAINT_GL));
    this.restoringContext_ = false;
  }

  EnumSet<GLRenderOption> renderOptions_;
  private WAbstractGLImplementation pImpl_;

  static class jsMatrixMap {
    private static Logger logger = LoggerFactory.getLogger(jsMatrixMap.class);

    public int id;
    public javax.vecmath.Matrix4f serverSideCopy;

    public jsMatrixMap(int matId, final javax.vecmath.Matrix4f ssCopy) {
      this.id = matId;
      this.serverSideCopy = ssCopy;
    }
  }

  static class jsVectorMap {
    private static Logger logger = LoggerFactory.getLogger(jsVectorMap.class);

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
        + ";var o = r ? r.wtObj : null;return o ? o : {ctx: null};})()";
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
    if (this.renderOptions_.contains(GLRenderOption.ClientSide)
        && WApplication.getInstance().getEnvironment().hasWebGL()) {
      app.loadJavaScript("js/WPaintedWidget.js", wtjs11());
    }
    app.loadJavaScript("js/WtGlMatrix.js", wtjs2());
    app.loadJavaScript("js/WGLWidget.js", wtjs1());
  }

  static WJavaScriptPreamble wtjs10() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WPaintedWidget",
        "(function(t,r){r.wtObj=this;var e=this;this.imagePreloaders=[];this.images=[];this.canvas=document.getElementById(\"c\"+r.id);this.repaint=function(){};this.widget=r;this.cancelPreloaders=function(){for(var t=0;t<e.imagePreloaders.length;++t)e.imagePreloaders[t].cancel();e.imagePreloaders=[]}})");
  }

  static WJavaScriptPreamble wtjs11() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptObject,
        "gfxUtils",
        "function(){var t=1024,r=4080;return new function(){var e=this;this.path_crisp=function(t){return t.map((function(t){return[Math.floor(t[0])+.5,Math.floor(t[1])+.5,t[2]]}))};this.transform_mult=function(t,r){if(2===r.length){var n=r[0],a=r[1];return[t[0]*n+t[2]*a+t[4],t[1]*n+t[3]*a+t[5]]}if(3===r.length){if(8===r[2]||9===r[2])return r.slice(0);n=r[0],a=r[1];return[t[0]*n+t[2]*a+t[4],t[1]*n+t[3]*a+t[5],r[2]]}if(4===r.length){var i,o,s,c,f,l,u=e.transform_mult(t,[r[0],r[1]]);i=u[0];s=u[0];o=u[1];c=u[1];for(f=0;f<3;++f){l=e.transform_mult(t,0==f?[e.rect_left(r),e.rect_bottom(r)]:1==f?[e.rect_right(r),e.rect_top(r)]:[e.rect_right(r),e.rect_bottom(r)]);i=Math.min(i,l[0]);s=Math.max(s,l[0]);o=Math.min(o,l[1]);c=Math.max(c,l[1])}return[i,o,s-i,c-o]}return 6===r.length?[t[0]*r[0]+t[2]*r[1],t[1]*r[0]+t[3]*r[1],t[0]*r[2]+t[2]*r[3],t[1]*r[2]+t[3]*r[3],t[0]*r[4]+t[2]*r[5]+t[4],t[1]*r[4]+t[3]*r[5]+t[5]]:[]};this.transform_apply=function(t,r){var n=e.transform_mult;return r.map((function(r){return n(t,r)}))};this.transform_det=function(t){var r=t[0],e=t[2],n=t[1];return r*t[3]-n*e};this.transform_adjoint=function(t){var r=t[0],e=t[2],n=t[1],a=t[3],i=t[4],o=t[5];return[a,-e,-n,r,o*n-i*a,-(o*r-i*e)]};this.transform_inverted=function(t){var r=e.transform_det(t);if(0!=r){var n=e.transform_adjoint(t);return[n[0]/r,n[2]/r,n[1]/r,n[3]/r,n[4]/r,n[5]/r]}console.log(\"inverted(): oops, determinant == 0\");return t};this.transform_assign=function(t,r){t[0]=r[0];t[1]=r[1];t[2]=r[2];t[3]=r[3];t[4]=r[4];t[5]=r[5]};this.transform_equal=function(t,r){return t[0]==r[0]&&t[1]==r[1]&&t[2]==r[2]&&t[3]==r[3]&&t[4]==r[4]&&t[5]==r[5]};this.css_text=function(t){return\"rgba(\"+t[0]+\",\"+t[1]+\",\"+t[2]+\",\"+t[3]+\")\"};this.arcPosition=function(t,r,e,n,a){var i=-a/180*Math.PI;return[t+e*Math.cos(i),r+n*Math.sin(i)]};this.pnpoly=function(t,r){var n,a,i,o=!1,s=0,c=0,f=t[0],l=t[1];for(n=0;n<r.length;++n){a=s;i=c;if(7===r[n][2]){a=(u=e.arcPosition(r[n][0],r[n][1],r[n+1][0],r[n+1][1],r[n+2][0]))[0];i=u[1]}else if(9===r[n][2]){var u;a=(u=e.arcPosition(r[n-2][0],r[n-2][1],r[n-1][0],r[n-1][1],r[n][0]+r[n][1]))[0];i=u[1]}else if(8!==r[n][2]){a=r[n][0];i=r[n][1]}0!==r[n][2]&&c>l!=i>l&&f<(a-s)*(l-c)/(i-c)+s&&(o=!o);s=a;c=i}return o};this.rect_intersection=function(t,r){t=e.rect_normalized(t);r=e.rect_normalized(r);var n=e.rect_top,a=e.rect_bottom,i=e.rect_left,o=e.rect_right,s=Math.max(i(t),i(r)),c=Math.min(o(t),o(r)),f=Math.max(n(t),n(r));return[s,f,c-s,Math.min(a(t),a(r))-f]};this.drawRect=function(t,r,n,a){r=e.rect_normalized(r);var i=e.rect_top(r),o=e.rect_bottom(r),s=e.rect_left(r),c=e.rect_right(r);path=[[s,i,0],[c,i,1],[c,o,1],[s,o,1],[s,i,1]];e.drawPath(t,path,n,a,!1)};this.drawPath=function(t,r,n,a,i){var o=0,s=[],c=[],f=[],l=1048576;function u(t){return t[0]}function h(t){return t[1]}function m(t){return t[2]}t.beginPath();r.length>0&&0!==m(r[0])&&t.moveTo(0,0);for(o=0;o<r.length;o++){var p=r[o];switch(m(p)){case 0:Math.abs(u(p))<=l&&Math.abs(h(p))<=l&&t.moveTo(u(p),h(p));break;case 1:!function(){var s=0===o?[0,0]:r[o-1];if(!n&&!i&&a&&(Math.abs(u(s))>l||Math.abs(h(s))>l||Math.abs(u(p))>l||Math.abs(h(p))>l)){!function(){var r,n=t.wtTransform?t.wtTransform:[1,0,0,1,0,0],a=e.transform_inverted(n),i=e.transform_mult(n,s),o=e.transform_mult(n,p),c=u(o)-u(i),f=h(o)-h(i),l=-50,m=t.canvas.width+50,_=-50,v=t.canvas.height+50;function g(t,r,e){return(e-t)/r}function d(t,r,e){return t+e*r}var b,w,P=null,M=null,T=null,y=null;if(u(i)<l&&u(o)>l){r=g(u(i),c,l);P=[l,d(h(i),f,r),r]}else if(u(i)>m&&u(o)<m){r=g(u(i),c,m);P=[m,d(h(i),f,r),r]}else{if(!(u(i)>l&&u(i)<m))return;P=[u(i),h(i),0]}if(h(i)<_&&h(o)>_){r=g(h(i),f,_);M=[d(u(i),c,r),_,r]}else if(h(i)>v&&h(o)<v){r=g(h(i),f,v);M=[d(u(i),c,r),v,r]}else{if(!(h(i)>_&&h(i)<v))return;M=[u(i),h(i),0]}if(!(u(b=P[2]>M[2]?[P[0],P[1]]:[M[0],M[1]])<l||u(b)>m||h(b)<_||h(b)>v)){if(u(i)<m&&u(o)>m){r=g(u(i),c,m);T=[m,d(h(i),f,r),r]}else if(u(i)>l&&u(o)<l){r=g(u(i),c,l);T=[l,d(h(i),f,r),r]}else{if(!(u(o)>l&&u(o)<m))return;T=[u(o),h(o),1]}if(h(i)<v&&h(o)>v){r=g(h(i),f,v);y=[d(u(i),c,r),v,r]}else if(h(i)>_&&h(o)<_){r=g(h(i),f,_);y=[d(u(i),c,r),_,r]}else{if(!(u(o)>_&&h(o)<v))return;y=[u(o),h(o),1]}if(!(u(w=T[2]<y[2]?[T[0],T[1]]:[y[0],y[1]])<l||u(w)>m||h(w)<_||h(w)>v)){b=e.transform_mult(a,b);w=e.transform_mult(a,w);t.moveTo(b[0],b[1]);t.lineTo(w[0],w[1])}}}();Math.abs(u(p))<=l&&Math.abs(h(p))<=l&&t.moveTo(u(p),h(p))}else t.lineTo(u(p),h(p))}();break;case 2:case 3:s.push(u(p),h(p));break;case 4:s.push(u(p),h(p));t.bezierCurveTo.apply(t,s);s=[];break;case 7:c.push(u(p),h(p));break;case 8:c.push(u(p));break;case 9:!function(){function r(t){var r=t%360;return r<0?r+360:r}function e(t){return t*Math.PI/180}var n,a,i=u(p),o=h(p),s=e(r(-i));n=o>=360||o<=-360?s-2*Math.PI*(o>0?1:-1):e(r(-i-((a=o)>360?360:a<-360?-360:a)));var f=o>0;c.push(s,n,f);t.arc.apply(t,c);c=[]}();break;case 5:f.push(u(p),h(p));break;case 6:f.push(u(p),h(p));t.quadraticCurveTo.apply(t,f);f=[]}}n&&t.fill();a&&t.stroke();i&&t.clip()};this.drawStencilAlongPath=function(t,r,n,a,i,o){var s,c=0;function f(t){return t[1]}function l(t){return t[2]}for(c=0;c<n.length;c++){var u=n[c];if((!o||!t.wtClipPath||e.pnpoly(u,e.transform_apply(t.wtClipPathTransform,t.wtClipPath)))&&(0==l(u)||1==l(u)||6==l(u)||4==l(u))){var h=e.transform_apply([1,0,0,1,(s=u,s[0]),f(u)],r);e.drawPath(t,h,a,i,!1)}}};this.drawText=function(n,a,i,o,s){if(!s||!n.wtClipPath||e.pnpoly(s,e.transform_apply(n.wtClipPathTransform,n.wtClipPath))){var c=i&r,f=null,l=null;switch(15&i){case 1:n.textAlign=\"left\";f=e.rect_left(a);break;case 2:n.textAlign=\"right\";f=e.rect_right(a);break;case 4:n.textAlign=\"center\";f=e.rect_center(a).x}switch(c){case 128:n.textBaseline=\"top\";l=e.rect_top(a);break;case t:n.textBaseline=\"bottom\";l=e.rect_bottom(a);break;case 512:n.textBaseline=\"middle\";l=e.rect_center(a).y}if(null!=f&&null!=l){var u=n.fillStyle;n.fillStyle=n.strokeStyle;n.fillText(o,f,l);n.fillStyle=u}}};this.calcYOffset=function(r,e,n,a){return 512===a?-(e-1)*n/2+r*n:128===a?r*n:a===t?-(e-1-r)*n:0};this.drawTextOnPath=function(t,n,a,i,o,s,c,f,l){var u=0,h=0;function m(t){return t[0]}function p(t){return t[1]}function _(t){return t[2]}var v=e.transform_apply(i,o);for(u=0;u<o.length&&!(u>=n.length);u++){var g=o[u],d=v[u],b=n[u].split(\"\\n\");if(0==_(g)||1==_(g)||6==_(g)||4==_(g))if(0==s)for(h=0;h<b.length;h++){var w=e.calcYOffset(h,b.length,c,f&r);e.drawText(t,[a[0]+m(d),a[1]+p(d)+w,a[2],a[3]],f,b[h],l?[m(d),p(d)]:null)}else{var P=s*Math.PI/180,M=Math.cos(-P),T=-Math.sin(-P),y=-T,x=M;t.save();t.transform(M,y,T,x,m(d),p(d));for(h=0;h<b.length;h++){w=e.calcYOffset(h,b.length,c,f&r);e.drawText(t,[a[0],a[1]+w,a[2],a[3]],f,b[h],l?[m(d),p(d)]:null)}t.restore()}}};this.setClipPath=function(t,r,n,a){if(a){t.setTransform.apply(t,n);e.drawPath(t,r,!1,!1,!0);t.setTransform(1,0,0,1,0,0)}t.wtClipPath=r;t.wtClipPathTransform=n};this.removeClipPath=function(t){delete t.wtClipPath;delete t.wtClipPathTransform};this.rect_top=function(t){return t[1]};this.rect_bottom=function(t){return t[1]+t[3]};this.rect_right=function(t){return t[0]+t[2]};this.rect_left=function(t){return t[0]};this.rect_topleft=function(t){return[t[0],t[1]]};this.rect_topright=function(t){return[t[0]+t[2],t[1]]};this.rect_bottomleft=function(t){return[t[0],t[1]+t[3]]};this.rect_bottomright=function(t){return[t[0]+t[2],t[1]+t[3]]};this.rect_center=function(t){return{x:(2*t[0]+t[2])/2,y:(2*t[1]+t[3])/2}};this.rect_normalized=function(t){var r,e,n,a;if(t[2]>0){r=t[0];n=t[2]}else{r=t[0]+t[2];n=-t[2]}if(t[3]>0){e=t[1];a=t[3]}else{e=t[1]+t[3];a=-t[3]}return[r,e,n,a]};this.drawImage=function(t,r,e,n,a){try{t.drawImage(r,n[0],n[1],n[2],n[3],a[0],a[1],a[2],a[3])}catch(t){var i=\"Error while drawing image: '\"+e+\"': \"+t.name;t.message&&(i+=\": \"+t.message);console.error(i)}}}}()");
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WGLWidget",
        "(function(t,e){e.wtObj=this;var i=this,n=t.WT,s=n.glMatrix.vec3,a=(n.glMatrix.mat3,n.glMatrix.mat4);this.ctx=null;this.initializeGL=function(){};this.paintGL=function(){};this.resizeGL=function(){};this.updates=new Array;this.initialized=!1;this.preloadingTextures=0;this.preloadingBuffers=0;this.jsValues={};this.discoverContext=function(t,i){if(e.getContext){try{this.ctx=e.getContext(\"webgl\",{antialias:i})}catch(t){}if(null===this.ctx)try{this.ctx=e.getContext(\"experimental-webgl\",{antialias:i})}catch(t){}if(null===this.ctx){var n=e.firstChild;e.parentNode.insertBefore(n,e);e.style.display=\"none\";t()}}return this.ctx};if(e.addEventListener){e.addEventListener(\"webglcontextlost\",(function(t){t.preventDefault();i.initialized=!1}),!1);e.addEventListener(\"webglcontextrestored\",(function(i){t.emit(e,\"contextRestored\")}),!1)}var o=null;this.setMouseHandler=function(t){(o=t).setTarget&&o.setTarget(this)};this.LookAtMouseHandler=function(t,o,u,l,r){var h=t,c=o,f=u,d=l,g=r,p=null,x=null,v=null,m=null,y=this;this.mouseDown=function(t,i){n.capture(null);n.capture(e);m=n.pageCoordinates(i)};this.mouseUp=function(t,e){null!==m&&(m=null)};this.mouseDrag=function(t,e){if(null!==m){var i=n.pageCoordinates(e);1===n.buttons&&w(i)}};this.mouseWheel=function(t,e){n.cancelEvent(e);M(n.wheelDelta(e))};function M(t){var e=Math.pow(1.2,t);a.translate(h,c);a.scale(h,[e,e,e]);s.negate(c);a.translate(h,c);s.negate(c);i.paintGL()}function w(t){var e=h[5]/s.length([h[1],h[5],h[9]]),n=h[6]/s.length([h[2],h[6],h[10]]),o=Math.atan2(n,e),u=t.x-m.x,l=t.y-m.y,r=s.create();r[0]=h[0];r[1]=h[4];r[2]=h[8];var p=a.create();a.identity(p);a.translate(p,c);var x=l*d;if(Math.abs(o+x)>=Math.PI/2){x=(o>0?1:-1)*Math.PI/2-o}a.rotate(p,x,r);a.rotate(p,u*g,f);s.negate(c);a.translate(p,c);s.negate(c);a.multiply(h,p,h);i.paintGL();m=t}this.touchStart=function(t,i){x=1===i.touches.length;v=2===i.touches.length;if(x){n.capture(null);n.capture(e);m=n.pageCoordinates(i.touches[0])}else{if(!v)return;var s=n.pageCoordinates(i.touches[0]),a=n.pageCoordinates(i.touches[1]);p=Math.sqrt((s.x-a.x)*(s.x-a.x)+(s.y-a.y)*(s.y-a.y))}i.preventDefault()};this.touchEnd=function(t,e){var i=0===e.touches.length;x=1===e.touches.length;v=2===e.touches.length;i&&y.mouseUp(null,null);(x||v)&&y.touchStart(t,e)};this.touchMoved=function(t,e){if(x||v){e.preventDefault();if(x){if(null===m)return;w(n.pageCoordinates(e))}if(v){var i=n.pageCoordinates(e.touches[0]),s=n.pageCoordinates(e.touches[1]),a=Math.sqrt((i.x-s.x)*(i.x-s.x)+(i.y-s.y)*(i.y-s.y)),o=a/p;if(Math.abs(o-1)<.05)return;p=a;M(o=o>1?1:-1)}}}};this.WalkMouseHandler=function(t,o,u){var l=t,r=o,h=u,c=null;this.mouseDown=function(t,i){n.capture(null);n.capture(e);c=n.pageCoordinates(i)};this.mouseUp=function(t,e){null!==c&&(c=null)};this.mouseDrag=function(t,e){if(null!==c){f(n.pageCoordinates(e))}};function f(t){var e=t.x-c.x,o=t.y-c.y,u=a.create();a.identity(u);a.rotateY(u,e*h);var f=s.create();f[0]=0;f[1]=0;f[2]=-r*o;a.translate(u,f);a.multiply(u,l,l);i.paintGL();c=n.pageCoordinates(event)}};this.mouseDrag=function(t,e){(this.initialized||!this.ctx)&&o&&o.mouseDrag&&o.mouseDrag(t,e)};this.mouseMove=function(t,e){(this.initialized||!this.ctx)&&o&&o.mouseMove&&o.mouseMove(t,e)};this.mouseDown=function(t,e){(this.initialized||!this.ctx)&&o&&o.mouseDown&&o.mouseDown(t,e)};this.mouseUp=function(t,e){(this.initialized||!this.ctx)&&o&&o.mouseUp&&o.mouseUp(t,e)};this.mouseWheel=function(t,e){(this.initialized||!this.ctx)&&o&&o.mouseWheel&&o.mouseWheel(t,e)};this.touchStart=function(t,e){(this.initialized||!this.ctx)&&o&&o.touchStart&&o.touchStart(t,e)};this.touchEnd=function(t,e){(this.initialized||!this.ctx)&&o&&o.touchEnd&&o.touchEnd(t,e)};this.touchMoved=function(t,e){(this.initialized||!this.ctx)&&o&&o.touchMoved&&o.touchMoved(t,e)};this.handlePreload=function(){if(0===this.preloadingTextures&&0===this.preloadingBuffers)if(this.initialized){var t;for(t in this.updates)this.updates[t]();this.updates=new Array;this.resizeGL();this.paintGL()}else{this.initializeGL();this.resizeGL();this.paintGL()}};e.wtEncodeValue=function(){var t=e.wtObj,i=\"\";for(var n in t.jsValues)if(t.jsValues.hasOwnProperty(n)){i+=n+\":\";for(var s=0;s<t.jsValues[n].length;s++){i+=t.jsValues[n][s];s!==t.jsValues[n].length-1?i+=\",\":i+=\";\"}}return i};var u=null,l=new Image;l.busy=!1;l.onload=function(){e.src=l.src;null!=u?l.src=u:l.busy=!1;u=null};l.onerror=l.onload;this.loadImage=function(t){if(l.busy)u=t;else{l.src=t;l.busy=!0}}})");
  }

  static WJavaScriptPreamble wtjs2() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptObject,
        "glMatrix",
        "function(){\"undefined\"!=typeof Float32Array?glMatrixArrayType=Float32Array:\"undefined\"!=typeof WebGLFloatArray?glMatrixArrayType=WebGLFloatArray:glMatrixArrayType=Array;var r={create:function(r){var t=new glMatrixArrayType(3);if(r){t[0]=r[0];t[1]=r[1];t[2]=r[2]}return t},set:function(r,t){t[0]=r[0];t[1]=r[1];t[2]=r[2];return t},add:function(r,t,n){if(!n||r==n){r[0]+=t[0];r[1]+=t[1];r[2]+=t[2];return r}n[0]=r[0]+t[0];n[1]=r[1]+t[1];n[2]=r[2]+t[2];return n},subtract:function(r,t,n){if(!n||r==n){r[0]-=t[0];r[1]-=t[1];r[2]-=t[2];return r}n[0]=r[0]-t[0];n[1]=r[1]-t[1];n[2]=r[2]-t[2];return n},negate:function(r,t){t||(t=r);t[0]=-r[0];t[1]=-r[1];t[2]=-r[2];return t},scale:function(r,t,n){if(!n||r==n){r[0]*=t;r[1]*=t;r[2]*=t;return r}n[0]=r[0]*t;n[1]=r[1]*t;n[2]=r[2]*t;return n},normalize:function(r,t){t||(t=r);var n=r[0],e=r[1],u=r[2],a=Math.sqrt(n*n+e*e+u*u);if(!a){t[0]=0;t[1]=0;t[2]=0;return t}if(1==a){t[0]=n;t[1]=e;t[2]=u;return t}a=1/a;t[0]=n*a;t[1]=e*a;t[2]=u*a;return t},cross:function(r,t,n){n||(n=r);var e=r[0],u=r[1],a=r[2],i=t[0],f=t[1],o=t[2];n[0]=u*o-a*f;n[1]=a*i-e*o;n[2]=e*f-u*i;return n},length:function(r){var t=r[0],n=r[1],e=r[2];return Math.sqrt(t*t+n*n+e*e)},dot:function(r,t){return r[0]*t[0]+r[1]*t[1]+r[2]*t[2]},direction:function(r,t,n){n||(n=r);var e=r[0]-t[0],u=r[1]-t[1],a=r[2]-t[2],i=Math.sqrt(e*e+u*u+a*a);if(!i){n[0]=0;n[1]=0;n[2]=0;return n}i=1/i;n[0]=e*i;n[1]=u*i;n[2]=a*i;return n},str:function(r){return\"[\"+r[0]+\", \"+r[1]+\", \"+r[2]+\"]\"}},t={create:function(r){var t=new glMatrixArrayType(9);if(r){t[0]=r[0];t[1]=r[1];t[2]=r[2];t[3]=r[3];t[4]=r[4];t[5]=r[5];t[6]=r[6];t[7]=r[7];t[8]=r[8];t[9]=r[9]}return t},set:function(r,t){t[0]=r[0];t[1]=r[1];t[2]=r[2];t[3]=r[3];t[4]=r[4];t[5]=r[5];t[6]=r[6];t[7]=r[7];t[8]=r[8];return t},identity:function(r){r[0]=1;r[1]=0;r[2]=0;r[3]=0;r[4]=1;r[5]=0;r[6]=0;r[7]=0;r[8]=1;return r},toMat4:function(r,t){t||(t=n.create());t[0]=r[0];t[1]=r[1];t[2]=r[2];t[3]=0;t[4]=r[3];t[5]=r[4];t[6]=r[5];t[7]=0;t[8]=r[6];t[9]=r[7];t[10]=r[8];t[11]=0;t[12]=0;t[13]=0;t[14]=0;t[15]=1;return t},str:function(r){return\"[\"+r[0]+\", \"+r[1]+\", \"+r[2]+\", \"+r[3]+\", \"+r[4]+\", \"+r[5]+\", \"+r[6]+\", \"+r[7]+\", \"+r[8]+\"]\"}},n={create:function(r){var t=new glMatrixArrayType(16);if(r){t[0]=r[0];t[1]=r[1];t[2]=r[2];t[3]=r[3];t[4]=r[4];t[5]=r[5];t[6]=r[6];t[7]=r[7];t[8]=r[8];t[9]=r[9];t[10]=r[10];t[11]=r[11];t[12]=r[12];t[13]=r[13];t[14]=r[14];t[15]=r[15]}return t},set:function(r,t){t[0]=r[0];t[1]=r[1];t[2]=r[2];t[3]=r[3];t[4]=r[4];t[5]=r[5];t[6]=r[6];t[7]=r[7];t[8]=r[8];t[9]=r[9];t[10]=r[10];t[11]=r[11];t[12]=r[12];t[13]=r[13];t[14]=r[14];t[15]=r[15];return t},identity:function(r){r[0]=1;r[1]=0;r[2]=0;r[3]=0;r[4]=0;r[5]=1;r[6]=0;r[7]=0;r[8]=0;r[9]=0;r[10]=1;r[11]=0;r[12]=0;r[13]=0;r[14]=0;r[15]=1;return r},transpose:function(r,t){if(!t||r==t){var n=r[1],e=r[2],u=r[3],a=r[6],i=r[7],f=r[11];r[1]=r[4];r[2]=r[8];r[3]=r[12];r[4]=n;r[6]=r[9];r[7]=r[13];r[8]=e;r[9]=a;r[11]=r[14];r[12]=u;r[13]=i;r[14]=f;return r}t[0]=r[0];t[1]=r[4];t[2]=r[8];t[3]=r[12];t[4]=r[1];t[5]=r[5];t[6]=r[9];t[7]=r[13];t[8]=r[2];t[9]=r[6];t[10]=r[10];t[11]=r[14];t[12]=r[3];t[13]=r[7];t[14]=r[11];t[15]=r[15];return t},determinant:function(r){var t=r[0],n=r[1],e=r[2],u=r[3],a=r[4],i=r[5],f=r[6],o=r[7],c=r[8],s=r[9],l=r[10],v=r[11],M=r[12],y=r[13],h=r[14],p=r[15];return M*s*f*u-c*y*f*u-M*i*l*u+a*y*l*u+c*i*h*u-a*s*h*u-M*s*e*o+c*y*e*o+M*n*l*o-t*y*l*o-c*n*h*o+t*s*h*o+M*i*e*v-a*y*e*v-M*n*f*v+t*y*f*v+a*n*h*v-t*i*h*v-c*i*e*p+a*s*e*p+c*n*f*p-t*s*f*p-a*n*l*p+t*i*l*p},inverse:function(r,t){t||(t=r);var n=r[0],e=r[1],u=r[2],a=r[3],i=r[4],f=r[5],o=r[6],c=r[7],s=r[8],l=r[9],v=r[10],M=r[11],y=r[12],h=r[13],p=r[14],A=r[15],d=n*f-e*i,g=n*o-u*i,m=n*c-a*i,T=e*o-u*f,q=e*c-a*f,x=u*c-a*o,b=s*h-l*y,E=s*p-v*y,F=s*A-M*y,W=l*p-v*h,w=l*A-M*h,L=v*A-M*p,R=1/(d*L-g*w+m*W+T*F-q*E+x*b);t[0]=(f*L-o*w+c*W)*R;t[1]=(-e*L+u*w-a*W)*R;t[2]=(h*x-p*q+A*T)*R;t[3]=(-l*x+v*q-M*T)*R;t[4]=(-i*L+o*F-c*E)*R;t[5]=(n*L-u*F+a*E)*R;t[6]=(-y*x+p*m-A*g)*R;t[7]=(s*x-v*m+M*g)*R;t[8]=(i*w-f*F+c*b)*R;t[9]=(-n*w+e*F-a*b)*R;t[10]=(y*q-h*m+A*d)*R;t[11]=(-s*q+l*m-M*d)*R;t[12]=(-i*W+f*E-o*b)*R;t[13]=(n*W-e*E+u*b)*R;t[14]=(-y*T+h*g-p*d)*R;t[15]=(s*T-l*g+v*d)*R;return t},toRotationMat:function(r,t){t||(t=n.create());t[0]=r[0];t[1]=r[1];t[2]=r[2];t[3]=r[3];t[4]=r[4];t[5]=r[5];t[6]=r[6];t[7]=r[7];t[8]=r[8];t[9]=r[9];t[10]=r[10];t[11]=r[11];t[12]=0;t[13]=0;t[14]=0;t[15]=1;return t},toMat3:function(r,n){n||(n=t.create());n[0]=r[0];n[1]=r[1];n[2]=r[2];n[3]=r[4];n[4]=r[5];n[5]=r[6];n[6]=r[8];n[7]=r[9];n[8]=r[10];return n},toInverseMat3:function(r,n){var e=r[0],u=r[1],a=r[2],i=r[4],f=r[5],o=r[6],c=r[8],s=r[9],l=r[10],v=l*f-o*s,M=-l*i+o*c,y=s*i-f*c,h=e*v+u*M+a*y;if(!h)return null;var p=1/h;n||(n=t.create());n[0]=v*p;n[1]=(-l*u+a*s)*p;n[2]=(o*u-a*f)*p;n[3]=M*p;n[4]=(l*e-a*c)*p;n[5]=(-o*e+a*i)*p;n[6]=y*p;n[7]=(-s*e+u*c)*p;n[8]=(f*e-u*i)*p;return n},multiply:function(r,t,n){n||(n=r);var e=r[0],u=r[1],a=r[2],i=r[3],f=r[4],o=r[5],c=r[6],s=r[7],l=r[8],v=r[9],M=r[10],y=r[11],h=r[12],p=r[13],A=r[14],d=r[15],g=t[0],m=t[1],T=t[2],q=t[3],x=t[4],b=t[5],E=t[6],F=t[7],W=t[8],w=t[9],L=t[10],R=t[11],_=t[12],G=t[13],I=t[14],V=t[15];n[0]=g*e+m*f+T*l+q*h;n[1]=g*u+m*o+T*v+q*p;n[2]=g*a+m*c+T*M+q*A;n[3]=g*i+m*s+T*y+q*d;n[4]=x*e+b*f+E*l+F*h;n[5]=x*u+b*o+E*v+F*p;n[6]=x*a+b*c+E*M+F*A;n[7]=x*i+b*s+E*y+F*d;n[8]=W*e+w*f+L*l+R*h;n[9]=W*u+w*o+L*v+R*p;n[10]=W*a+w*c+L*M+R*A;n[11]=W*i+w*s+L*y+R*d;n[12]=_*e+G*f+I*l+V*h;n[13]=_*u+G*o+I*v+V*p;n[14]=_*a+G*c+I*M+V*A;n[15]=_*i+G*s+I*y+V*d;return n},multiplyVec3:function(r,t,n){n||(n=t);var e=t[0],u=t[1],a=t[2];n[0]=r[0]*e+r[4]*u+r[8]*a+r[12];n[1]=r[1]*e+r[5]*u+r[9]*a+r[13];n[2]=r[2]*e+r[6]*u+r[10]*a+r[14];return n},multiplyVec4:function(r,t,n){n||(n=t);var e=t[0],u=t[1],a=t[2],i=t[3];n[0]=r[0]*e+r[4]*u+r[8]*a+r[12]*i;n[1]=r[1]*e+r[5]*u+r[9]*a+r[13]*i;n[2]=r[2]*e+r[6]*u+r[10]*a+r[14]*i;n[3]=r[3]*e+r[7]*u+r[11]*a+r[15]*i;return n},translate:function(r,t,n){var e=t[0],u=t[1],a=t[2];if(!n||r==n){r[12]=r[0]*e+r[4]*u+r[8]*a+r[12];r[13]=r[1]*e+r[5]*u+r[9]*a+r[13];r[14]=r[2]*e+r[6]*u+r[10]*a+r[14];r[15]=r[3]*e+r[7]*u+r[11]*a+r[15];return r}var i=r[0],f=r[1],o=r[2],c=r[3],s=r[4],l=r[5],v=r[6],M=r[7],y=r[8],h=r[9],p=r[10],A=r[11];n[0]=i;n[1]=f;n[2]=o;n[3]=c;n[4]=s;n[5]=l;n[6]=v;n[7]=M;n[8]=y;n[9]=h;n[10]=p;n[11]=A;n[12]=i*e+s*u+y*a+r[12];n[13]=f*e+l*u+h*a+r[13];n[14]=o*e+v*u+p*a+r[14];n[15]=c*e+M*u+A*a+r[15];return n},scale:function(r,t,n){var e=t[0],u=t[1],a=t[2];if(!n||r==n){r[0]*=e;r[1]*=e;r[2]*=e;r[3]*=e;r[4]*=u;r[5]*=u;r[6]*=u;r[7]*=u;r[8]*=a;r[9]*=a;r[10]*=a;r[11]*=a;return r}n[0]=r[0]*e;n[1]=r[1]*e;n[2]=r[2]*e;n[3]=r[3]*e;n[4]=r[4]*u;n[5]=r[5]*u;n[6]=r[6]*u;n[7]=r[7]*u;n[8]=r[8]*a;n[9]=r[9]*a;n[10]=r[10]*a;n[11]=r[11]*a;n[12]=r[12];n[13]=r[13];n[14]=r[14];n[15]=r[15];return n},rotate:function(r,t,n,e){var u=n[0],a=n[1],i=n[2],f=Math.sqrt(u*u+a*a+i*i);if(!f)return null;if(1!=f){u*=f=1/f;a*=f;i*=f}var o=Math.sin(t),c=Math.cos(t),s=1-c,l=r[0],v=r[1],M=r[2],y=r[3],h=r[4],p=r[5],A=r[6],d=r[7],g=r[8],m=r[9],T=r[10],q=r[11],x=u*u*s+c,b=a*u*s+i*o,E=i*u*s-a*o,F=u*a*s-i*o,W=a*a*s+c,w=i*a*s+u*o,L=u*i*s+a*o,R=a*i*s-u*o,_=i*i*s+c;if(e){if(r!=e){e[12]=r[12];e[13]=r[13];e[14]=r[14];e[15]=r[15]}}else e=r;e[0]=l*x+h*b+g*E;e[1]=v*x+p*b+m*E;e[2]=M*x+A*b+T*E;e[3]=y*x+d*b+q*E;e[4]=l*F+h*W+g*w;e[5]=v*F+p*W+m*w;e[6]=M*F+A*W+T*w;e[7]=y*F+d*W+q*w;e[8]=l*L+h*R+g*_;e[9]=v*L+p*R+m*_;e[10]=M*L+A*R+T*_;e[11]=y*L+d*R+q*_;return e},rotateX:function(r,t,n){var e=Math.sin(t),u=Math.cos(t),a=r[4],i=r[5],f=r[6],o=r[7],c=r[8],s=r[9],l=r[10],v=r[11];if(n){if(r!=n){n[0]=r[0];n[1]=r[1];n[2]=r[2];n[3]=r[3];n[12]=r[12];n[13]=r[13];n[14]=r[14];n[15]=r[15]}}else n=r;n[4]=a*u+c*e;n[5]=i*u+s*e;n[6]=f*u+l*e;n[7]=o*u+v*e;n[8]=a*-e+c*u;n[9]=i*-e+s*u;n[10]=f*-e+l*u;n[11]=o*-e+v*u;return n},rotateY:function(r,t,n){var e=Math.sin(t),u=Math.cos(t),a=r[0],i=r[1],f=r[2],o=r[3],c=r[8],s=r[9],l=r[10],v=r[11];if(n){if(r!=n){n[4]=r[4];n[5]=r[5];n[6]=r[6];n[7]=r[7];n[12]=r[12];n[13]=r[13];n[14]=r[14];n[15]=r[15]}}else n=r;n[0]=a*u+c*-e;n[1]=i*u+s*-e;n[2]=f*u+l*-e;n[3]=o*u+v*-e;n[8]=a*e+c*u;n[9]=i*e+s*u;n[10]=f*e+l*u;n[11]=o*e+v*u;return n},rotateZ:function(r,t,n){var e=Math.sin(t),u=Math.cos(t),a=r[0],i=r[1],f=r[2],o=r[3],c=r[4],s=r[5],l=r[6],v=r[7];if(n){if(r!=n){n[8]=r[8];n[9]=r[9];n[10]=r[10];n[11]=r[11];n[12]=r[12];n[13]=r[13];n[14]=r[14];n[15]=r[15]}}else n=r;n[0]=a*u+c*e;n[1]=i*u+s*e;n[2]=f*u+l*e;n[3]=o*u+v*e;n[4]=a*-e+c*u;n[5]=i*-e+s*u;n[6]=f*-e+l*u;n[7]=o*-e+v*u;return n},frustum:function(r,t,e,u,a,i,f){f||(f=n.create());var o=t-r,c=u-e,s=i-a;f[0]=2*a/o;f[1]=0;f[2]=0;f[3]=0;f[4]=0;f[5]=2*a/c;f[6]=0;f[7]=0;f[8]=(t+r)/o;f[9]=(u+e)/c;f[10]=-(i+a)/s;f[11]=-1;f[12]=0;f[13]=0;f[14]=-i*a*2/s;f[15]=0;return f},perspective:function(r,t,e,u,a){var i=e*Math.tan(r*Math.PI/360),f=i*t;return n.frustum(-f,f,-i,i,e,u,a)},ortho:function(r,t,e,u,a,i,f){f||(f=n.create());var o=t-r,c=u-e,s=i-a;f[0]=2/o;f[1]=0;f[2]=0;f[3]=0;f[4]=0;f[5]=2/c;f[6]=0;f[7]=0;f[8]=0;f[9]=0;f[10]=-2/s;f[11]=0;f[12]=-(r+t)/o;f[13]=-(u+e)/c;f[14]=-(i+a)/s;f[15]=1;return f},lookAt:function(r,t,e,u){u||(u=n.create());var a,i,f,o,c,s,l,v,M,y,h=r[0],p=r[1],A=r[2],d=e[0],g=e[1],m=e[2],T=t[0],q=t[1],x=t[2];if(h==T&&p==q&&A==x)return n.identity(u);a=h-t[0];i=p-t[1];f=A-t[2];o=g*(f*=y=1/Math.sqrt(a*a+i*i+f*f))-m*(i*=y);c=m*(a*=y)-d*f;s=d*i-g*a;if(y=Math.sqrt(o*o+c*c+s*s)){o*=y=1/y;c*=y;s*=y}else{o=0;c=0;s=0}l=i*s-f*c;v=f*o-a*s;M=a*c-i*o;if(y=Math.sqrt(l*l+v*v+M*M)){l*=y=1/y;v*=y;M*=y}else{l=0;v=0;M=0}u[0]=o;u[1]=l;u[2]=a;u[3]=0;u[4]=c;u[5]=v;u[6]=i;u[7]=0;u[8]=s;u[9]=M;u[10]=f;u[11]=0;u[12]=-(o*h+c*p+s*A);u[13]=-(l*h+v*p+M*A);u[14]=-(a*h+i*p+f*A);u[15]=1;return u},str:function(r){return\"[\"+r[0]+\", \"+r[1]+\", \"+r[2]+\", \"+r[3]+\", \"+r[4]+\", \"+r[5]+\", \"+r[6]+\", \"+r[7]+\", \"+r[8]+\", \"+r[9]+\", \"+r[10]+\", \"+r[11]+\", \"+r[12]+\", \"+r[13]+\", \"+r[14]+\", \"+r[15]+\"]\"}};return{vec3:r,mat3:t,mat4:n}}()");
  }
}
