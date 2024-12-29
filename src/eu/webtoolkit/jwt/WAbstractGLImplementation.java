/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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

abstract class WAbstractGLImplementation extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WAbstractGLImplementation.class);

  public abstract void debugger();

  public abstract void activeTexture(WGLWidget.GLenum texture);

  public abstract void attachShader(WGLWidget.Program program, WGLWidget.Shader shader);

  public abstract void bindAttribLocation(WGLWidget.Program program, int index, final String name);

  public abstract void bindBuffer(WGLWidget.GLenum target, WGLWidget.Buffer buffer);

  public abstract void bindFramebuffer(WGLWidget.GLenum target, WGLWidget.Framebuffer buffer);

  public abstract void bindRenderbuffer(WGLWidget.GLenum target, WGLWidget.Renderbuffer buffer);

  public abstract void bindTexture(WGLWidget.GLenum target, WGLWidget.Texture texture);

  public abstract void blendColor(double red, double green, double blue, double alpha);

  public abstract void blendEquation(WGLWidget.GLenum mode);

  public abstract void blendEquationSeparate(WGLWidget.GLenum modeRGB, WGLWidget.GLenum modeAlpha);

  public abstract void blendFunc(WGLWidget.GLenum sfactor, WGLWidget.GLenum dfactor);

  public abstract void blendFuncSeparate(
      WGLWidget.GLenum srcRGB,
      WGLWidget.GLenum dstRGB,
      WGLWidget.GLenum srcAlpha,
      WGLWidget.GLenum dstAlpha);

  public abstract void bufferData(WGLWidget.GLenum target, int size, WGLWidget.GLenum usage);

  public abstract void bufferData(
      WGLWidget.GLenum target, WGLWidget.ArrayBuffer res, WGLWidget.GLenum usage);

  public abstract void bufferData(
      WGLWidget.GLenum target,
      WGLWidget.ArrayBuffer res,
      int arrayBufferOffset,
      int arrayBufferSize,
      WGLWidget.GLenum usage);

  public abstract void bufferSubData(
      WGLWidget.GLenum target, int offset, WGLWidget.ArrayBuffer res);

  public abstract void bufferSubData(
      WGLWidget.GLenum target,
      int offset,
      WGLWidget.ArrayBuffer res,
      int arrayBufferOffset,
      int size);

  public abstract void bufferDatafv(
      WGLWidget.GLenum target, final java.nio.ByteBuffer v, WGLWidget.GLenum usage, boolean binary);

  public abstract void bufferDatafv(
      WGLWidget.GLenum target, final java.nio.FloatBuffer buffer, WGLWidget.GLenum usage);

  public abstract void bufferSubDatafv(
      WGLWidget.GLenum target, int offset, final java.nio.ByteBuffer buffer, boolean binary);

  public abstract void bufferSubDatafv(
      WGLWidget.GLenum target, int offset, final java.nio.FloatBuffer buffer);

  public abstract void bufferDataiv(
      WGLWidget.GLenum target,
      final java.nio.IntBuffer buffer,
      WGLWidget.GLenum usage,
      WGLWidget.GLenum type);

  public abstract void bufferSubDataiv(
      WGLWidget.GLenum target, int offset, final java.nio.IntBuffer buffer, WGLWidget.GLenum type);

  public abstract void clearBinaryResources();

  public abstract void clear(EnumSet<WGLWidget.GLenum> mask);

  public final void clear(WGLWidget.GLenum mas, WGLWidget.GLenum... mask) {
    clear(EnumSet.of(mas, mask));
  }

  public abstract void clearColor(double r, double g, double b, double a);

  public abstract void clearDepth(double depth);

  public abstract void clearStencil(int s);

  public abstract void colorMask(boolean red, boolean green, boolean blue, boolean alpha);

  public abstract void compileShader(WGLWidget.Shader shader);

  public abstract void copyTexImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalFormat,
      int x,
      int y,
      int width,
      int height,
      int border);

  public abstract void copyTexSubImage2D(
      WGLWidget.GLenum target,
      int level,
      int xoffset,
      int yoffset,
      int x,
      int y,
      int width,
      int height);

  public abstract WGLWidget.Buffer getCreateBuffer();

  public abstract WGLWidget.ArrayBuffer createAndLoadArrayBuffer(final String url);

  public abstract WGLWidget.Framebuffer getCreateFramebuffer();

  public abstract WGLWidget.Program getCreateProgram();

  public abstract WGLWidget.Renderbuffer getCreateRenderbuffer();

  public abstract WGLWidget.Shader createShader(WGLWidget.GLenum shader);

  public abstract WGLWidget.Texture getCreateTexture();

  public abstract WGLWidget.Texture createTextureAndLoad(final String url);

  public abstract WPaintDevice createPaintDevice(final WLength width, final WLength height);

  public abstract void cullFace(WGLWidget.GLenum mode);

  public abstract void deleteBuffer(WGLWidget.Buffer buffer);

  public abstract void deleteFramebuffer(WGLWidget.Framebuffer buffer);

  public abstract void deleteProgram(WGLWidget.Program program);

  public abstract void deleteRenderbuffer(WGLWidget.Renderbuffer buffer);

  public abstract void deleteShader(WGLWidget.Shader shader);

  public abstract void deleteTexture(WGLWidget.Texture texture);

  public abstract void depthFunc(WGLWidget.GLenum func);

  public abstract void depthMask(boolean flag);

  public abstract void depthRange(double zNear, double zFar);

  public abstract void detachShader(WGLWidget.Program program, WGLWidget.Shader shader);

  public abstract void disable(WGLWidget.GLenum cap);

  public abstract void disableVertexAttribArray(WGLWidget.AttribLocation index);

  public abstract void drawArrays(WGLWidget.GLenum mode, int first, int count);

  public abstract void drawElements(
      WGLWidget.GLenum mode, int count, WGLWidget.GLenum type, int offset);

  public abstract void enable(WGLWidget.GLenum cap);

  public abstract void enableVertexAttribArray(WGLWidget.AttribLocation index);

  public abstract void finish();

  public abstract void flush();

  public abstract void framebufferRenderbuffer(
      WGLWidget.GLenum target,
      WGLWidget.GLenum attachment,
      WGLWidget.GLenum renderbuffertarget,
      WGLWidget.Renderbuffer renderbuffer);

  public abstract void framebufferTexture2D(
      WGLWidget.GLenum target,
      WGLWidget.GLenum attachment,
      WGLWidget.GLenum textarget,
      WGLWidget.Texture texture,
      int level);

  public abstract void frontFace(WGLWidget.GLenum mode);

  public abstract void generateMipmap(WGLWidget.GLenum target);

  public abstract WGLWidget.AttribLocation getAttribLocation(
      WGLWidget.Program program, final String attrib);

  public abstract WGLWidget.UniformLocation getUniformLocation(
      WGLWidget.Program program, final String location);

  public abstract void hint(WGLWidget.GLenum target, WGLWidget.GLenum mode);

  public abstract void lineWidth(double width);

  public abstract void linkProgram(WGLWidget.Program program);

  public abstract void pixelStorei(WGLWidget.GLenum pname, int param);

  public abstract void polygonOffset(double factor, double units);

  public abstract void renderbufferStorage(
      WGLWidget.GLenum target, WGLWidget.GLenum internalformat, int width, int height);

  public abstract void sampleCoverage(double value, boolean invert);

  public abstract void scissor(int x, int y, int width, int height);

  public abstract void shaderSource(WGLWidget.Shader shader, final String src);

  public abstract void stencilFunc(WGLWidget.GLenum func, int ref, int mask);

  public abstract void stencilFuncSeparate(
      WGLWidget.GLenum face, WGLWidget.GLenum func, int ref, int mask);

  public abstract void stencilMask(int mask);

  public abstract void stencilMaskSeparate(WGLWidget.GLenum face, int mask);

  public abstract void stencilOp(
      WGLWidget.GLenum fail, WGLWidget.GLenum zfail, WGLWidget.GLenum zpass);

  public abstract void stencilOpSeparate(
      WGLWidget.GLenum face, WGLWidget.GLenum fail, WGLWidget.GLenum zfail, WGLWidget.GLenum zpass);

  public abstract void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      int width,
      int height,
      int border,
      WGLWidget.GLenum format);

  public abstract void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      WGLWidget.GLenum format,
      WGLWidget.GLenum type,
      WImage image);

  public abstract void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      WGLWidget.GLenum format,
      WGLWidget.GLenum type,
      WVideo video);

  public abstract void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      WGLWidget.GLenum format,
      WGLWidget.GLenum type,
      String image);

  public abstract void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      WGLWidget.GLenum format,
      WGLWidget.GLenum type,
      WPaintDevice paintdevice);

  public abstract void texImage2D(
      WGLWidget.GLenum target,
      int level,
      WGLWidget.GLenum internalformat,
      WGLWidget.GLenum format,
      WGLWidget.GLenum type,
      WGLWidget.Texture texture);

  public abstract void texParameteri(
      WGLWidget.GLenum target, WGLWidget.GLenum pname, WGLWidget.GLenum param);

  public abstract void uniform1f(final WGLWidget.UniformLocation location, double x);

  public abstract void uniform1fv(final WGLWidget.UniformLocation location, float[] value);

  public abstract void uniform1fv(
      final WGLWidget.UniformLocation location, final WGLWidget.JavaScriptVector v);

  public abstract void uniform1i(final WGLWidget.UniformLocation location, int x);

  public abstract void uniform1iv(final WGLWidget.UniformLocation location, int[] value);

  public abstract void uniform2f(final WGLWidget.UniformLocation location, double x, double y);

  public abstract void uniform2fv(final WGLWidget.UniformLocation location, float[] value);

  public abstract void uniform2fv(
      final WGLWidget.UniformLocation location, final WGLWidget.JavaScriptVector v);

  public abstract void uniform2i(final WGLWidget.UniformLocation location, int x, int y);

  public abstract void uniform2iv(final WGLWidget.UniformLocation location, int[] value);

  public abstract void uniform3f(
      final WGLWidget.UniformLocation location, double x, double y, double z);

  public abstract void uniform3fv(final WGLWidget.UniformLocation location, float[] value);

  public abstract void uniform3fv(
      final WGLWidget.UniformLocation location, final WGLWidget.JavaScriptVector v);

  public abstract void uniform3i(final WGLWidget.UniformLocation location, int x, int y, int z);

  public abstract void uniform3iv(final WGLWidget.UniformLocation location, int[] value);

  public abstract void uniform4f(
      final WGLWidget.UniformLocation location, double x, double y, double z, double w);

  public abstract void uniform4fv(final WGLWidget.UniformLocation location, float[] value);

  public abstract void uniform4fv(
      final WGLWidget.UniformLocation location, final WGLWidget.JavaScriptVector v);

  public abstract void uniform4i(
      final WGLWidget.UniformLocation location, int x, int y, int z, int w);

  public abstract void uniform4iv(final WGLWidget.UniformLocation location, int[] value);

  public abstract void uniformMatrix2fv(
      final WGLWidget.UniformLocation location, boolean transpose, double[] value);

  public abstract void uniformMatrix2(final WGLWidget.UniformLocation location, final Matrix2f m);

  public abstract void uniformMatrix3fv(
      final WGLWidget.UniformLocation location, boolean transpose, double[] value);

  public abstract void uniformMatrix3(
      final WGLWidget.UniformLocation location, final javax.vecmath.Matrix3f m);

  public abstract void uniformMatrix4fv(
      final WGLWidget.UniformLocation location, boolean transpose, double[] value);

  public abstract void uniformMatrix4(
      final WGLWidget.UniformLocation location, final javax.vecmath.Matrix4f m);

  public abstract void uniformMatrix4(
      final WGLWidget.UniformLocation location, final WGLWidget.JavaScriptMatrix4x4 m);

  public abstract void useProgram(WGLWidget.Program program);

  public abstract void validateProgram(WGLWidget.Program program);

  public abstract void vertexAttrib1f(WGLWidget.AttribLocation location, double x);

  public abstract void vertexAttrib2f(WGLWidget.AttribLocation location, double x, double y);

  public abstract void vertexAttrib3f(
      WGLWidget.AttribLocation location, double x, double y, double z);

  public abstract void vertexAttrib4f(
      WGLWidget.AttribLocation location, double x, double y, double z, double w);

  public abstract void vertexAttribPointer(
      WGLWidget.AttribLocation location,
      int size,
      WGLWidget.GLenum type,
      boolean normalized,
      int stride,
      int offset);

  public abstract void viewport(int x, int y, int width, int height);

  public abstract void initJavaScriptMatrix4(final WGLWidget.JavaScriptMatrix4x4 jsm);

  public abstract void setJavaScriptMatrix4(
      final WGLWidget.JavaScriptMatrix4x4 jsm, final javax.vecmath.Matrix4f m);

  public abstract void initJavaScriptVector(final WGLWidget.JavaScriptVector jsv);

  public abstract void setJavaScriptVector(
      final WGLWidget.JavaScriptVector jsv, final List<Float> v);

  public abstract void setClientSideMouseHandler(final String handlerCode);

  public abstract void setClientSideLookAtHandler(
      final WGLWidget.JavaScriptMatrix4x4 m,
      double ctrX,
      double ctrY,
      double ctrZ,
      double uX,
      double uY,
      double uZ,
      double pitchRate,
      double yawRate);

  public abstract void setClientSideWalkHandler(
      final WGLWidget.JavaScriptMatrix4x4 m, double frontStep, double rotStep);

  public void layoutSizeChanged(int width, int height) {
    this.renderWidth_ = width;
    this.renderHeight_ = height;
    this.sizeChanged_ = true;
  }

  public void repaintGL(EnumSet<GLClientSideRenderer> which) {
    if (which.contains(GLClientSideRenderer.PAINT_GL)) {
      this.updatePaintGL_ = true;
    }
    if (which.contains(GLClientSideRenderer.RESIZE_GL)) {
      this.updateResizeGL_ = true;
    }
    if (which.contains(GLClientSideRenderer.UPDATE_GL)) {
      this.updateGL_ = true;
    }
  }

  public final void repaintGL(GLClientSideRenderer whic, GLClientSideRenderer... which) {
    repaintGL(EnumSet.of(whic, which));
  }

  public void enableClientErrorChecks(boolean enable) {
    this.debugging_ = enable;
  }

  public abstract JsArrayType getArrayType();

  public abstract void injectJS(final String jsString);

  public abstract void restoreContext(final String jsRef);

  public abstract void render(final String jsRef, EnumSet<RenderFlag> flags);

  public final void render(final String jsRef, RenderFlag flag, RenderFlag... flags) {
    render(jsRef, EnumSet.of(flag, flags));
  }

  public void updateDom(DomElement el, boolean all) {
    if (all || this.sizeChanged_) {
      el.setAttribute("width", String.valueOf(this.renderWidth_));
      el.setAttribute("height", String.valueOf(this.renderHeight_));
      this.sizeChanged_ = false;
    }
  }

  protected WAbstractGLImplementation(WGLWidget glInterface) {
    super();
    this.glInterface_ = glInterface;
    this.updateGL_ = false;
    this.updateResizeGL_ = false;
    this.updatePaintGL_ = false;
    this.renderWidth_ = 0;
    this.renderHeight_ = 0;
    this.debugging_ = false;
    this.webglNotAvailable_ = new JSignal(this, "webglNotAvailable");
    this.webglNotAvailable_.addListener(
        this.glInterface_,
        () -> {
          WAbstractGLImplementation.this.glInterface_.webglNotAvailable();
        });
  }

  protected WGLWidget glInterface_;
  protected boolean updateGL_;
  protected boolean updateResizeGL_;
  protected boolean updatePaintGL_;
  protected int renderWidth_;
  protected int renderHeight_;
  protected boolean sizeChanged_;
  protected boolean debugging_;
  protected JSignal webglNotAvailable_;
}
