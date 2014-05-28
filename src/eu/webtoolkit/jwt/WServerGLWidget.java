package eu.webtoolkit.jwt;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;
import javax.vecmath.Matrix4f;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;

import eu.webtoolkit.jwt.WGLWidget.AttribLocation;
import eu.webtoolkit.jwt.WGLWidget.Buffer;
import eu.webtoolkit.jwt.WGLWidget.Framebuffer;
import eu.webtoolkit.jwt.WGLWidget.GLenum;
import eu.webtoolkit.jwt.WGLWidget.JavaScriptMatrix4x4;
import eu.webtoolkit.jwt.WGLWidget.JavaScriptVector;
import eu.webtoolkit.jwt.WGLWidget.Program;
import eu.webtoolkit.jwt.WGLWidget.Renderbuffer;
import eu.webtoolkit.jwt.WGLWidget.Shader;
import eu.webtoolkit.jwt.WGLWidget.Texture;
import eu.webtoolkit.jwt.WGLWidget.UniformLocation;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;
import eu.webtoolkit.jwt.utils.EnumUtils;

public class WServerGLWidget extends WAbstractGLImplementation {

	private class WGLImageResource extends WMemoryResource {
		public WGLImageResource(final String mimeType) {
			super(mimeType);
		}

		@Override
		public void handleRequest(WebRequest request, WebResponse response) throws IOException {
			response.addHeader("Cache-Control", "max-age=60");
			super.handleRequest(request, response);
		}
	}


	public WServerGLWidget(WGLWidget glInterface) {
		super(glInterface);
		
		mr_ = new WGLImageResource("image/png");
	    link_ = new WLink(mr_);
	    
	    js_ = new StringWriter();
	    
	    if (serverWindow_) {
	    	GLProfile glp = GLProfile.getDefault();
	        GLCapabilities caps = new GLCapabilities(glp);
	        caps.setSampleBuffers(true);
	        caps.setNumSamples(2);
	        caps.setDoubleBuffered(true);
	        caps.setAlphaBits(8);
	        window_ = GLWindow.create(caps);
	        window_.setSize(500,  500);
	        window_.setVisible(true);

	        window_.addWindowListener(new WindowAdapter() {
	                public void windowDestroyNotify(WindowEvent arg0) {
	                	System.exit(0);
	                };
	        });
	    } else {
	    	GLProfile glp = GLProfile.getDefault();
	    	GLDrawableFactory fact = GLDrawableFactory.getFactory(glp);
	    	GLCapabilities caps = new GLCapabilities(glp);
	    	caps.setAlphaBits(8);
	    	caps.setFBO(true);
	    	offscreenDrawable_ =
	    			fact.createOffscreenAutoDrawable(null, caps, null, 100,
	    					100, null); // correct size is set later on, not available yet
	    }
	}

	@Override
	public void debugger() { // does nothing (only for WClientGLWidget)
	}
	
	@Override
	public void activeTexture(WGLWidget.GLenum texture) {
		glCtx_.glActiveTexture(serverGLenum(texture));
	}
	
	@Override
	public void attachShader(Program program, Shader shader) {

		glCtx_.glAttachShader(program.getId(), shader.getId());
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void bindAttribLocation(Program program, int index, String name) {
		glCtx_.glBindAttribLocation(program.getId(), index, name);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void bindBuffer(GLenum target, Buffer buffer) {

		glCtx_.glBindBuffer(serverGLenum(target), buffer.getId());
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void bindFramebuffer(GLenum target, Framebuffer buffer) {
		if (buffer == null || buffer.isNull()) {
		    glCtx_.glBindFramebuffer(serverGLenum(target), 0);
		} else {
		    glCtx_.glBindFramebuffer(serverGLenum(target), buffer.getId());
		}
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void bindRenderbuffer(GLenum target, Renderbuffer buffer) {
		if (buffer == null || buffer.isNull()) {
		    glCtx_.glBindRenderbuffer(serverGLenum(target), 0);
		} else {
		    glCtx_.glBindRenderbuffer(serverGLenum(target), buffer.getId());
		}
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void bindTexture(WGLWidget.GLenum target,
			WGLWidget.Texture texture) {
		glCtx_.glBindTexture(serverGLenum(target), texture.getId());
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void blendColor(double red, double green, double blue, double alpha) {
		glCtx_.glBlendColor((float)red, (float)green, (float)blue, (float)alpha);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void blendEquation(GLenum mode) {
		glCtx_.glBlendEquation(serverGLenum(mode));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void blendEquationSeparate(GLenum modeRGB, GLenum modeAlpha) {
		glCtx_.glBlendEquationSeparate(serverGLenum(modeRGB), serverGLenum(modeAlpha));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void blendFunc(WGLWidget.GLenum sfactor,
			WGLWidget.GLenum dfactor) {
		glCtx_.glBlendFunc(serverGLenum(sfactor), serverGLenum(dfactor));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void blendFuncSeparate(GLenum srcRGB, GLenum dstRGB,
			GLenum srcAlpha, GLenum dstAlpha) {
		glCtx_.glBlendFuncSeparate(serverGLenum(srcRGB), serverGLenum(dstRGB),
					serverGLenum(srcAlpha), serverGLenum(dstAlpha));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void bufferData(WGLWidget.GLenum target, int size,
			WGLWidget.GLenum usage)
	{
		float[] zeros = new float[size];
		bufferDatafv(target, FloatBuffer.wrap(zeros), usage);
	}

	@Override
	public void bufferData(WGLWidget.GLenum target,
			WGLWidget.ArrayBuffer res, WGLWidget.GLenum usage) {
		throw new WException("WServerGLWidget: this operation is not supported in server-side rendering");
	}

	@Override
	public void bufferData(WGLWidget.GLenum target,
			WGLWidget.ArrayBuffer res, int arrayBufferOffset,
			int arrayBufferSize, WGLWidget.GLenum usage) {
		throw new WException("WServerGLWidget: this operation is not supported in server-side rendering");
	}

	@Override
	public void bufferSubData(WGLWidget.GLenum target, int offset,
			WGLWidget.ArrayBuffer res) {
		throw new WException("WServerGLWidget: this operation is not supported in server-side rendering");
	}

	@Override
	public void bufferSubData(WGLWidget.GLenum target, int offset,
			WGLWidget.ArrayBuffer res, int arrayBufferOffset, int size) {
		throw new WException("WServerGLWidget: this operation is not supported in server-side rendering");
	}

	@Override
	public void bufferDatafv(WGLWidget.GLenum target,
			java.nio.ByteBuffer v, WGLWidget.GLenum usage, boolean binary)
	{
		v.rewind();
		glCtx_.glBufferData(serverGLenum(target), v.capacity(), v, serverGLenum(usage));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void bufferDatafv(GLenum target, FloatBuffer v, GLenum usage) {
		v.rewind();
		glCtx_.glBufferData(serverGLenum(target), v.capacity() * 4, v, serverGLenum(usage));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void bufferSubDatafv(WGLWidget.GLenum target, int offset,
			java.nio.ByteBuffer buffer, boolean binary)
	{
		glCtx_.glBufferSubData(serverGLenum(target), offset, buffer.capacity(), buffer);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void bufferSubDatafv(WGLWidget.GLenum target, int offset,
			java.nio.FloatBuffer buffer)
	{
		glCtx_.glBufferSubData(serverGLenum(target), offset, buffer.capacity() * 4, buffer);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void bufferDataiv(WGLWidget.GLenum target,
			java.nio.IntBuffer buffer, WGLWidget.GLenum usage,
			WGLWidget.GLenum type)
	{
		buffer.rewind();
		ShortBuffer sb = ShortBuffer.allocate(buffer.capacity());
		for (int i=0; i < buffer.capacity(); i++) {
			sb.put((short)buffer.get(i));
		}
		sb.rewind();
		glCtx_.glBufferData(serverGLenum(target), sb.capacity() * 2, sb, serverGLenum(usage));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void bufferSubDataiv(WGLWidget.GLenum target, int offset,
			java.nio.IntBuffer buffer, WGLWidget.GLenum type)
	{
		glCtx_.glBufferSubData(serverGLenum(target), offset, buffer.capacity() * 4, buffer);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void clear(EnumSet<GLenum> mask) {
		Iterator<GLenum> it = mask.iterator();
		while (it.hasNext()) {
			glCtx_.glClear(serverGLenum(it.next()));
			if (debug_)
				System.out.println(glCtx_.glGetError());
		}
	}

	@Override
	public void clearColor(double r, double g, double b, double a) {

		glCtx_.glClearColor((float)r, (float)g, (float)b, (float)a);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void clearStencil(int s) {
		glCtx_.glClearStencil(s);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void colorMask(boolean red, boolean green, boolean blue,
			boolean alpha) {
		glCtx_.glColorMask(red, green, blue, alpha);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void clearDepth(double depth) {

		glCtx_.glClearDepth(depth);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void compileShader(Shader shader) {
		glCtx_.glCompileShader(shader.getId());
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void copyTexImage2D(GLenum target, int level, GLenum internalFormat,
			int x, int y, int width, int height, int border) {
		glCtx_.glCopyTexImage2D(serverGLenum(target), level, serverGLenum(internalFormat), x, y, width, height, border);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void copyTexSubImage2D(GLenum target, int level, int xoffset,
			int yoffset, int x, int y, int width, int height) {
		glCtx_.glCopyTexSubImage2D(serverGLenum(target), level, xoffset, yoffset, x, y, width, height);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public Buffer getCreateBuffer() {
		int[] bufferId = new int[1];
		glCtx_.glGenBuffers(1, bufferId, 0);
		if (debug_)
			System.out.println(glCtx_.glGetError());
		return new Buffer(bufferId[0]);
	}
	
	@Override
	public WGLWidget.ArrayBuffer createAndLoadArrayBuffer(String url) {
		throw new WException("WServerGLWidget: this operation is not supported in server-side rendering");
	}
	
	@Override
	public Framebuffer getCreateFramebuffer() {
		int[] bufferId = new int[1];
		glCtx_.glGenFramebuffers(1, bufferId, 0);
		if (debug_)
			System.out.println(glCtx_.glGetError());
		return new Framebuffer(bufferId[0]);
	}

	@Override
	public Program getCreateProgram() {
		int programId = glCtx_.glCreateProgram();
		if (debug_)
			System.out.println(glCtx_.glGetError());
		return new Program(programId);
	}

	@Override
	public Renderbuffer getCreateRenderbuffer() {
		int[] bufferId = new int[1];
		glCtx_.glGenRenderbuffers(1, bufferId, 0);
		if (debug_)
			System.out.println(glCtx_.glGetError());
		return new Renderbuffer(bufferId[0]);
	}

	@Override
	public Shader createShader(GLenum shader) {
		int shaderId = glCtx_.glCreateShader(serverGLenum(shader));
		if (debug_)
			System.out.println(glCtx_.glGetError());
		return new Shader(shaderId);
	}

	@Override
	public WGLWidget.Texture getCreateTexture()
	{
		int[] textureId = new int[1];
		glCtx_.glGenTextures(1, textureId, 0);
		if (debug_)
			System.out.println(glCtx_.glGetError());
		return new Texture(textureId[0]);
	}

	@Override
	public WGLWidget.Texture createTextureAndLoad(String url) {
		int[] textureId = new int[1];
		glCtx_.glGenTextures(1, textureId, 0);
		if (debug_)
			System.out.println(glCtx_.glGetError());
		Texture tex = new Texture(textureId[0]);
		tex.setUrl(url);
		return tex;
	}

	@Override
	public WPaintDevice createPaintDevice(WLength width,
			WLength height) {
		return new WRasterPaintDevice("png", width, height);
	}

	@Override
	public void cullFace(WGLWidget.GLenum mode) {
		glCtx_.glCullFace(serverGLenum(mode));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	

	@Override
	public void deleteBuffer(Buffer buffer) {
		int[] buffers = new int[1];
		buffers[0] = buffer.getId();
		glCtx_.glDeleteBuffers(1, buffers, 0);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void deleteFramebuffer(Framebuffer buffer) {
		int[] buffers = new int[1];
		buffers[0] = buffer.getId();
		glCtx_.glDeleteFramebuffers(1, buffers, 0);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void deleteProgram(Program program) {
		glCtx_.glDeleteProgram(program.getId());
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void deleteRenderbuffer(Renderbuffer buffer) {
		int[] buffers = new int[1];
		buffers[0] = buffer.getId();
		glCtx_.glDeleteRenderbuffers(1, buffers, 0);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void deleteShader(Shader shader) {
		glCtx_.glDeleteShader(shader.getId());
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void deleteTexture(Texture texture) {
		int[] textures = new int[1];
		textures[0] = texture.getId();
		glCtx_.glDeleteTextures(1, textures, 0);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void depthFunc(GLenum func) {
		glCtx_.glDepthFunc(serverGLenum(func));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void depthMask(boolean flag) {
		glCtx_.glDepthMask(flag);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void depthRange(double zNear, double zFar) {
		glCtx_.glDepthRange(zNear, zFar);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void detachShader(Program program, Shader shader) {
		glCtx_.glDetachShader(program.getId(), shader.getId());
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void disable(GLenum cap) {
		glCtx_.glDisable(serverGLenum(cap));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void disableVertexAttribArray(WGLWidget.AttribLocation index) {
		glCtx_.glDisableVertexAttribArray(index.getId());
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void drawArrays(GLenum mode, int first, int count) {
		glCtx_.glDrawArrays(serverGLenum(mode), first, count);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void drawElements(WGLWidget.GLenum mode, int count,
			WGLWidget.GLenum type, int offset)
	{
		glCtx_.glDrawElements(serverGLenum(mode), count, serverGLenum(type), offset);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void enable(WGLWidget.GLenum cap)
	{
		glCtx_.glEnable(serverGLenum(cap));
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void enableVertexAttribArray(AttribLocation index) {
		glCtx_.glEnableVertexAttribArray(index.getId());
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void finish() {
		glCtx_.glFinish();
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void flush() {
		glCtx_.glFlush();
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void framebufferRenderbuffer(GLenum target, GLenum attachment,
			GLenum renderbuffertarget, Renderbuffer renderbuffer) {
		glCtx_.glFramebufferRenderbuffer(serverGLenum(target), serverGLenum(attachment), serverGLenum(renderbuffertarget), renderbuffer.getId());
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void framebufferTexture2D(GLenum target, GLenum attachment,
			GLenum textarget, Texture texture, int level) {
		glCtx_.glFramebufferTexture2D(serverGLenum(target), serverGLenum(attachment), serverGLenum(textarget), texture.getId(), level);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void frontFace(GLenum mode) {
		glCtx_.glFrontFace(serverGLenum(mode));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void generateMipmap(WGLWidget.GLenum target) {
		glCtx_.glGenerateMipmap(serverGLenum(target));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public AttribLocation getAttribLocation(Program program, String attrib) {
		int attribId = glCtx_.glGetAttribLocation(program.getId(), attrib);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
		return new AttribLocation(attribId);
	}

	@Override
	public UniformLocation getUniformLocation(Program program, String location) {
		int uniformId = glCtx_.glGetUniformLocation(program.getId(), location);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
		return new UniformLocation(uniformId);
	}

	@Override
	public void hint(GLenum target, GLenum mode) {
		glCtx_.glHint(serverGLenum(target), serverGLenum(mode));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void lineWidth(double width) {
		glCtx_.glLineWidth((float)width);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void linkProgram(Program program) {
		glCtx_.glLinkProgram(program.getId());
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void pixelStorei(WGLWidget.GLenum pname, int param) {
		if (pname == GLenum.UNPACK_FLIP_Y_WEBGL || pname == GLenum.UNPACK_PREMULTIPLY_ALPHA_WEBGL || pname == GLenum.UNPACK_COLORSPACE_CONVERSION_WEBGL)
			return;
		glCtx_.glPixelStorei(serverGLenum(pname), param);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void polygonOffset(double factor, double units) {
		glCtx_.glPolygonOffset((float)factor, (float)units);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void renderbufferStorage(GLenum target, GLenum internalformat,
			int width, int height) {
		glCtx_.glRenderbufferStorage(serverGLenum(target), serverGLenum(internalformat), width, height);
		if (debug_)
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void sampleCoverage(double value, boolean invert) {
		glCtx_.glSampleCoverage((float)value, invert);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void scissor(int x, int y, int width, int height) {
		glCtx_.glScissor(x, y, width, height);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void shaderSource(Shader shader, String src) {
		String[] shaderSource = new String[1];
	    int[] sourceLength = new int[1];
	    shaderSource[0] = src;
	    sourceLength[0] = shaderSource[0].length();
		glCtx_.glShaderSource(shader.getId(), 1, shaderSource, sourceLength, 0);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void stencilFunc(GLenum func, int ref, int mask) {
		glCtx_.glStencilFunc(serverGLenum(func), ref, mask);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void stencilFuncSeparate(GLenum face, GLenum func, int ref, int mask) {
		glCtx_.glStencilFuncSeparate(serverGLenum(face), serverGLenum(func), ref, mask);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void stencilMask(int mask) {
		glCtx_.glStencilMask(mask);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void stencilMaskSeparate(GLenum face, int mask) {
		glCtx_.glStencilMaskSeparate(serverGLenum(face), mask);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void stencilOp(GLenum fail, GLenum zfail, GLenum zpass) {
		glCtx_.glStencilOp(serverGLenum(fail), serverGLenum(zfail), serverGLenum(zpass));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void stencilOpSeparate(GLenum face, GLenum fail, GLenum zfail,
			GLenum zpass) {
		glCtx_.glStencilOpSeparate(serverGLenum(face), serverGLenum(fail), serverGLenum(zfail), serverGLenum(zpass));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void texImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalformat, int width, int height, int border,
			WGLWidget.GLenum format) {
		glCtx_.glTexImage2D(serverGLenum(target), level, serverGLenum(internalformat), width, height, border, serverGLenum(format),
				GL2.GL_UNSIGNED_BYTE, null);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void texImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalformat, WGLWidget.GLenum format,
			WGLWidget.GLenum type, WImage image) {
		throw new WException("WServerGLWidget: this operation is not supported in server-side rendering");
	}

	@Override
	public void texImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalformat, WGLWidget.GLenum format,
			WGLWidget.GLenum type, WVideo video) {
		throw new WException("WServerGLWidget: this operation is not supported in server-side rendering");
	}

	@Override
	public void texImage2D(WGLWidget.GLenum target, int level,
			WGLWidget.GLenum internalformat, WGLWidget.GLenum format,
			WGLWidget.GLenum type, WGLWidget.Texture texture) {
		BufferedImage initialImage = null;
		try {
			initialImage = ImageIO.read(new File(texture.getUrl()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int openGlInternalFormat = serverGLenum(internalformat);
        int openGlImageFormat = serverGLenum(format);
        WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, initialImage.getWidth(), initialImage.getHeight(), 4, null);
        ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] { 8, 8, 8, 8 }, 
                true, false, 
                ComponentColorModel.TRANSLUCENT, 
                DataBuffer.TYPE_BYTE);
        BufferedImage bufImg = new BufferedImage(colorModel,
                raster, false,
                null);

        Graphics2D g = bufImg.createGraphics();
        g.translate(0, initialImage.getHeight());
        g.scale(1, -1);
        g.drawImage(initialImage, null, null);

        DataBufferByte imgBuf = (DataBufferByte) raster.getDataBuffer();
        byte[] bytes = imgBuf.getData();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        g.dispose();
        
        glCtx_.glTexImage2D(GL2.GL_TEXTURE_2D, 0, openGlInternalFormat, initialImage.getWidth(), initialImage.getHeight(), 0, openGlImageFormat, GL2.GL_UNSIGNED_BYTE, buffer);
	}

	@Override
	public void texParameteri(WGLWidget.GLenum target,
			WGLWidget.GLenum pname, WGLWidget.GLenum param) {
		glCtx_.glTexParameteri(serverGLenum(target), serverGLenum(pname), serverGLenum(param));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform1f(WGLWidget.UniformLocation location, double x) {
		glCtx_.glUniform1f(location.getId(), (float)x);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void uniform1fv(WGLWidget.UniformLocation location,
			float[] value) {
		glCtx_.glUniform1fv(location.getId(), 1, FloatBuffer.wrap(value));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform1fv(WGLWidget.UniformLocation location,
			JavaScriptVector value) {
		FloatBuffer buffer = FloatBuffer.allocate(1);
		buffer.put(0,value.getValue().get(0));
		glCtx_.glUniform1fv(location.getId(), 1, buffer);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform1i(WGLWidget.UniformLocation location, int x) {
		glCtx_.glUniform1i(location.getId(), x);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform1iv(WGLWidget.UniformLocation location,
			int[] value) {
		glCtx_.glUniform1iv(location.getId(), 1, IntBuffer.wrap(value));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform2f(WGLWidget.UniformLocation location,
			double x, double y) {
		glCtx_.glUniform2f(location.getId(), (float)x, (float)y);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform2fv(WGLWidget.UniformLocation location,
			float[] value) {
		glCtx_.glUniform2fv(location.getId(), 1, FloatBuffer.wrap(value));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform2fv(WGLWidget.UniformLocation location,
			JavaScriptVector value) {
		FloatBuffer buffer = FloatBuffer.allocate(2);
		buffer.put(0,value.getValue().get(0));
		buffer.put(1,value.getValue().get(1));
		glCtx_.glUniform2fv(location.getId(), 1, buffer);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform2i(WGLWidget.UniformLocation location, int x,
			int y) {
		glCtx_.glUniform2i(location.getId(), x, y);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform2iv(WGLWidget.UniformLocation location,
			int[] value) {
		glCtx_.glUniform2iv(location.getId(), 1, IntBuffer.wrap(value));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform3f(WGLWidget.UniformLocation location,
			double x, double y, double z) {
		glCtx_.glUniform3f(location.getId(), (float)x, (float)y, (float)z);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform3fv(WGLWidget.UniformLocation location,
			float[] value) {
		glCtx_.glUniform3fv(location.getId(), 1, FloatBuffer.wrap(value));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform3fv(WGLWidget.UniformLocation location,
			JavaScriptVector value) {
		FloatBuffer buffer = FloatBuffer.allocate(3);
		buffer.put(0,value.getValue().get(0));
		buffer.put(1,value.getValue().get(1));
		buffer.put(2,value.getValue().get(2));
		glCtx_.glUniform3fv(location.getId(), 1, buffer);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform3i(WGLWidget.UniformLocation location, int x,
			int y, int z) {
		glCtx_.glUniform3i(location.getId(), x, y, z);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform3iv(WGLWidget.UniformLocation location,
			int[] value) {
		glCtx_.glUniform3iv(location.getId(), 1, IntBuffer.wrap(value));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform4f(WGLWidget.UniformLocation location,
			double x, double y, double z, double w) {
		glCtx_.glUniform4f(location.getId(), (float)x, (float)y, (float)z, (float)w);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform4fv(WGLWidget.UniformLocation location,
			float[] value) {
		glCtx_.glUniform4fv(location.getId(), 1, FloatBuffer.wrap(value));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform4fv(WGLWidget.UniformLocation location,
			JavaScriptVector value) {
		FloatBuffer buffer = FloatBuffer.allocate(4);
		buffer.put(0,value.getValue().get(0));
		buffer.put(1,value.getValue().get(1));
		buffer.put(2,value.getValue().get(2));
		buffer.put(3,value.getValue().get(3));
		glCtx_.glUniform4fv(location.getId(), 1, buffer);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform4i(WGLWidget.UniformLocation location, int x,
			int y, int z, int w) {
		glCtx_.glUniform4i(location.getId(), x, y, z, w);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniform4iv(WGLWidget.UniformLocation location,
			int[] value) {
		glCtx_.glUniform4iv(location.getId(), 1, IntBuffer.wrap(value));
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniformMatrix2fv(WGLWidget.UniformLocation location,
			boolean transpose, double[] value) {
		float[] mat = new float[4];
		for (int i=0; i<4; i++){
			mat[i] = (float)value[i];
		}
		glCtx_.glUniformMatrix2fv(location.getId(), 1, transpose, mat, 0);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniformMatrix2(WGLWidget.UniformLocation location,
			Matrix2f m) {
		float[] mat = new float[4];
	    for (int i=0; i<2; i++){
	    	for (int j=0; j<2; j++){
	    		mat[i*2+j] = m.getElement(j, i);
	    	}
	    }
	    glCtx_.glUniformMatrix2fv(location.getId(), 1, false, mat, 0);
	    if (debug_) 
	    	System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniformMatrix3fv(WGLWidget.UniformLocation location,
			boolean transpose, double[] value) {
		float[] mat = new float[9];
		for (int i=0; i<9; i++){
			mat[i] = (float)value[i];
		}
		glCtx_.glUniformMatrix3fv(location.getId(), 1, transpose, mat, 0);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniformMatrix3(WGLWidget.UniformLocation location,
			javax.vecmath.Matrix3f m) {
		float[] mat = new float[9];
	    for (int i=0; i<3; i++){
	    	for (int j=0; j<3; j++){
	    		mat[i*3+j] = m.getElement(j, i);
	    	}
	    }
	    glCtx_.glUniformMatrix3fv(location.getId(), 1, false, mat, 0);
	    if (debug_) 
	    	System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniformMatrix4fv(WGLWidget.UniformLocation location,
			boolean transpose, double[] value) {
		float[] mat = new float[16];
		for (int i=0; i<16; i++){
			mat[i] = (float)value[i];
		}
		glCtx_.glUniformMatrix4fv(location.getId(), 1, transpose, mat, 0);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void uniformMatrix4(UniformLocation location, Matrix4f m) {
		float[] mat = new float[16];
	    for (int i=0; i<4; i++) {
	    	for (int j=0; j<4; j++) {
	    		mat[i*4+j] = m.getElement(j, i);
	    	}
	    }
	    glCtx_.glUniformMatrix4fv(location.getId(), 1, false, mat, 0);
	    if (debug_) 
	    	System.out.println(glCtx_.glGetError());
	}

	@Override
	public void useProgram(Program program) {
		glCtx_.glUseProgram(program.getId());
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void validateProgram(Program program) {
		glCtx_.glValidateProgram(program.getId());
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}
	
	@Override
	public void vertexAttrib1f(AttribLocation location, double x) {
		glCtx_.glVertexAttrib1f(location.getId(), (float)x);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void vertexAttrib2f(AttribLocation location, double x, double y) {
		glCtx_.glVertexAttrib2f(location.getId(), (float)x, (float)y);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void vertexAttrib3f(AttribLocation location, double x, double y,
			double z) {
		glCtx_.glVertexAttrib3f(location.getId(), (float)x, (float)y, (float)z);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void vertexAttrib4f(AttribLocation location, double x, double y,
			double z, double w) {
		glCtx_.glVertexAttrib4f(location.getId(), (float)x, (float)y, (float)z, (float)w);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void vertexAttribPointer(AttribLocation location, int size,
			GLenum type, boolean normalized, int stride, int offset) {
		glCtx_.glVertexAttribPointer(location.getId(), size, serverGLenum(type), normalized, stride, offset);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	@Override
	public void viewport(int x, int y, int width, int height) {
		if (serverWindow_)
			ctx_ = window_.getContext();
		else
			ctx_ = offscreenDrawable_.getContext();
		glCtx_ = ctx_.getGL().getGL2();
		glCtx_.glViewport(x, y, width, height);
		if (debug_) 
			System.out.println(glCtx_.glGetError());
	}

	private String glObjJsRef(String jsRef) {
		return "(function(){var r = "
				+ jsRef
				+ ";var o = r ? jQuery.data(r,'obj') : null;return o ? o : {ctx: null};})()";
	}
	
	@Override
	public void restoreContext(String jsRef) {
	}

	@Override
	public void render(String jsRef, EnumSet<RenderFlag> flags) {
		if (serverWindow_)
			ctx_ = window_.getContext();
		else
			ctx_ = offscreenDrawable_.getContext();
		ctx_.makeCurrent();
		

		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			if (serverWindow_) {
				window_.setSize((int)glInterface_.getWidth().getValue(), (int)glInterface_.getHeight().getValue());
			} else {
				offscreenDrawable_.setSize((int)glInterface_.getWidth().getValue(), (int)glInterface_.getHeight().getValue());
			}
			// build GLWidget object in browser
			StringWriter tmp = new StringWriter();
			
			tmp.append("{\nvar obj = new ");
			tmp.append(WEnvironment.getJavaScriptWtScope());
			tmp.append(".WGLWidget(")
				.append(WApplication.getInstance().getJavaScriptClass())
				.append(",").append(jsRef)
				.append(");\n");
			
			js_.getBuffer().setLength(0);
			init();
			tmp.append(js_.toString());
			tmp.append("obj.paintGL = function(){\n")
				.append("Wt.emit(").append(jsRef).append(", ").append(WWebWidget.jsStringLiteral("repaintSignal")).append(");")
				.append("}");
			tmp.append("}");
			
			glInterface_.doJavaScript(tmp.toString());

			this.updatePaintGL_ = true;
		}
		if (this.updateGL_) {
			js_.getBuffer().setLength(0);
			js_.append("var obj=").append(glObjJsRef(jsRef)).append(";\n");
			update();
			glInterface_.doJavaScript(js_.toString());
			this.updateGL_ = false;
		}
		if (this.updateResizeGL_) {
			if (sizeChanged_) {
				if (serverWindow_)
					window_.setSize(renderWidth_, renderHeight_);
				else
					offscreenDrawable_.setSize(renderWidth_, renderHeight_);
			}
			this.reshape(0, 0, renderWidth_, renderHeight_);
			this.updateResizeGL_ = false;
		}
		if (this.updatePaintGL_) {
			display();
			this.updatePaintGL_ = false;
		}
		
		// schrijf framebuffer naar een image
		ByteBuffer glBB = ByteBuffer.allocate(4 * renderWidth_ * renderHeight_); 
		glBB.order(ByteOrder.nativeOrder());
	    
	    glCtx_.glReadPixels(0, 0, renderWidth_, renderHeight_, GL2.GL_RGBA, GL2.GL_BYTE, glBB);
	    BufferedImage bi = new BufferedImage(renderWidth_, renderHeight_, BufferedImage.TYPE_INT_ARGB);
	    int[] bd = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();

	    for (int y = 0; y < renderHeight_; y++) {
	        for (int x = 0; x < renderWidth_; x++) {
	            int r = 2 * glBB.get();
	            int g = 2 * glBB.get();
	            int b = 2 * glBB.get();
	            int a = 2 * glBB.get();

	            bd[(renderHeight_ - y - 1) * renderWidth_ + x] = (r << 16) | (g << 8) | b | (a << 24);
	        }
	    }

	    
	    ctx_.release();
	    
	    ByteArrayOutputStream pngData = new ByteArrayOutputStream();
	    try {
			ImageIO.write(bi, "png", pngData);
		} catch (IOException e1) {
			System.out.println("writing of image data failed");
		}
	    
	    mr_.setData(pngData.toByteArray());
	    
	    StringWriter tmp = new StringWriter();
	    tmp.append("jQuery.data(")
	       .append(jsRef)
	       .append(",'obj').loadImage(")
	       .append(WWebWidget.jsStringLiteral(link_.getUrl()))
	       .append(");");
	    glInterface_.doJavaScript(tmp.toString());
	}
	
	private static int serverGLenum(GLenum type) {
		switch(type) {
		case DEPTH_BUFFER_BIT:
		  return GL2.GL_DEPTH_BUFFER_BIT;
		case STENCIL_BUFFER_BIT:
		  return GL2.GL_STENCIL_BUFFER_BIT;
		case COLOR_BUFFER_BIT:
		  return GL2.GL_COLOR_BUFFER_BIT;
		case POINTS:
		  return GL2.GL_POINTS;
		case LINES:
		  return GL2.GL_LINES;
		case LINE_LOOP:
		  return GL2.GL_LINE_LOOP;
		case LINE_STRIP:
		  return GL2.GL_LINE_STRIP;
		case TRIANGLES:
		  return GL2.GL_TRIANGLES;
		case TRIANGLE_STRIP:
		  return GL2.GL_TRIANGLE_STRIP;
		case TRIANGLE_FAN:
		  return GL2.GL_TRIANGLE_FAN;
		case ZERO:
		  return GL2.GL_ZERO;
		case ONE:
		  return GL2.GL_ONE;
		case SRC_COLOR:
		  return GL2.GL_SRC_COLOR;
		case ONE_MINUS_SRC_COLOR:
		  return GL2.GL_ONE_MINUS_SRC_COLOR;
		case SRC_ALPHA:
		  return GL2.GL_SRC_ALPHA;
		case ONE_MINUS_SRC_ALPHA:
		  return GL2.GL_ONE_MINUS_SRC_ALPHA;
		case DST_ALPHA:
		  return GL2.GL_DST_ALPHA;
		case ONE_MINUS_DST_ALPHA:
		  return GL2.GL_ONE_MINUS_DST_ALPHA;
		case DST_COLOR:
		  return GL2.GL_DST_COLOR;
		case ONE_MINUS_DST_COLOR:
		  return GL2.GL_ONE_MINUS_DST_COLOR;
		case SRC_ALPHA_SATURATE:
		  return GL2.GL_SRC_ALPHA_SATURATE;
		case FUNC_ADD:
		  return GL2.GL_FUNC_ADD;
		case BLEND_EQUATION:
		  return GL2.GL_BLEND_EQUATION;
		case BLEND_EQUATION_RGB:
		  return GL2.GL_BLEND_EQUATION_RGB;
		case BLEND_EQUATION_ALPHA:
		  return GL2.GL_BLEND_EQUATION_ALPHA;
		case FUNC_SUBTRACT:
		  return GL2.GL_FUNC_SUBTRACT;
		case FUNC_REVERSE_SUBTRACT:
		  return GL2.GL_FUNC_REVERSE_SUBTRACT;
		case BLEND_DST_RGB:
		  return GL2.GL_BLEND_DST_RGB;
		case BLEND_SRC_RGB:
		  return GL2.GL_BLEND_SRC_RGB;
		case BLEND_DST_ALPHA:
		  return GL2.GL_BLEND_DST_ALPHA;
		case BLEND_SRC_ALPHA:
		  return GL2.GL_BLEND_SRC_ALPHA;
		case CONSTANT_COLOR:
		  return GL2.GL_CONSTANT_COLOR;
		case ONE_MINUS_CONSTANT_COLOR:
		  return GL2.GL_ONE_MINUS_CONSTANT_COLOR;
		case CONSTANT_ALPHA:
		  return GL2.GL_CONSTANT_ALPHA;
		case ONE_MINUS_CONSTANT_ALPHA:
		  return GL2.GL_ONE_MINUS_CONSTANT_ALPHA;
		case BLEND_COLOR:
		  return GL2.GL_BLEND_COLOR;
		case ARRAY_BUFFER:
		  return GL2.GL_ARRAY_BUFFER;
		case ELEMENT_ARRAY_BUFFER:
		  return GL2.GL_ELEMENT_ARRAY_BUFFER;
		case ARRAY_BUFFER_BINDING:
		  return GL2.GL_ARRAY_BUFFER_BINDING;
		case ELEMENT_ARRAY_BUFFER_BINDING:
		  return GL2.GL_ELEMENT_ARRAY_BUFFER_BINDING;
		case STREAM_DRAW:
		  return GL2.GL_STREAM_DRAW;
		case STATIC_DRAW:
		  return GL2.GL_STATIC_DRAW;
		case DYNAMIC_DRAW:
		  return GL2.GL_DYNAMIC_DRAW;
		case BUFFER_SIZE:
		  return GL2.GL_BUFFER_SIZE;
		case BUFFER_USAGE:
		  return GL2.GL_BUFFER_USAGE;
		case CURRENT_VERTEX_ATTRIB:
		  return GL2.GL_CURRENT_VERTEX_ATTRIB;
		case FRONT:
		  return GL2.GL_FRONT;
		case BACK:
		  return GL2.GL_BACK;
		case FRONT_AND_BACK:
		  return GL2.GL_FRONT_AND_BACK;
		case CULL_FACE:
		  return GL2.GL_CULL_FACE;
		case BLEND:
		  return GL2.GL_BLEND;
		case DITHER:
		  return GL2.GL_DITHER;
		case STENCIL_TEST:
		  return GL2.GL_STENCIL_TEST;
		case DEPTH_TEST:
		  return GL2.GL_DEPTH_TEST;
		case SCISSOR_TEST:
		  return GL2.GL_SCISSOR_TEST;
		case POLYGON_OFFSET_FILL:
		  return GL2.GL_POLYGON_OFFSET_FILL;
		case SAMPLE_ALPHA_TO_COVERAGE:
		  return GL2.GL_SAMPLE_ALPHA_TO_COVERAGE;
		case SAMPLE_COVERAGE:
		  return GL2.GL_SAMPLE_COVERAGE;
		case NO_ERROR:
		  return GL2.GL_NO_ERROR;
		case INVALID_ENUM:
		  return GL2.GL_INVALID_ENUM;
		case INVALID_VALUE:
		  return GL2.GL_INVALID_VALUE;
		case INVALID_OPERATION:
		  return GL2.GL_INVALID_OPERATION;
		case OUT_OF_MEMORY:
		  return GL2.GL_OUT_OF_MEMORY;
		case CW:
		  return GL2.GL_CW;
		case CCW:
		  return GL2.GL_CCW;
		case LINE_WIDTH:
		  return GL2.GL_LINE_WIDTH;
		case ALIASED_POINT_SIZE_RANGE:
		  return GL2.GL_ALIASED_POINT_SIZE_RANGE;
		case ALIASED_LINE_WIDTH_RANGE:
		  return GL2.GL_ALIASED_LINE_WIDTH_RANGE;
		case CULL_FACE_MODE:
		  return GL2.GL_CULL_FACE_MODE;
		case FRONT_FACE:
		  return GL2.GL_FRONT_FACE;
		case DEPTH_RANGE:
		  return GL2.GL_DEPTH_RANGE;
		case DEPTH_WRITEMASK:
		  return GL2.GL_DEPTH_WRITEMASK;
		case DEPTH_CLEAR_VALUE:
		  return GL2.GL_DEPTH_CLEAR_VALUE;
		case DEPTH_FUNC:
		  return GL2.GL_DEPTH_FUNC;
		case STENCIL_CLEAR_VALUE:
		  return GL2.GL_STENCIL_CLEAR_VALUE;
		case STENCIL_FUNC:
		  return GL2.GL_STENCIL_FUNC;
		case STENCIL_FAIL:
		  return GL2.GL_STENCIL_FAIL;
		case STENCIL_PASS_DEPTH_FAIL:
		  return GL2.GL_STENCIL_PASS_DEPTH_FAIL;
		case STENCIL_PASS_DEPTH_PASS:
		  return GL2.GL_STENCIL_PASS_DEPTH_PASS;
		case STENCIL_REF:
		  return GL2.GL_STENCIL_REF;
		case STENCIL_VALUE_MASK:
		  return GL2.GL_STENCIL_VALUE_MASK;
		case STENCIL_WRITEMASK:
		  return GL2.GL_STENCIL_WRITEMASK;
		case STENCIL_BACK_FUNC:
		  return GL2.GL_STENCIL_BACK_FUNC;
		case STENCIL_BACK_FAIL:
		  return GL2.GL_STENCIL_BACK_FAIL;
		case STENCIL_BACK_PASS_DEPTH_FAIL:
		  return GL2.GL_STENCIL_BACK_PASS_DEPTH_FAIL;
		case STENCIL_BACK_PASS_DEPTH_PASS:
		  return GL2.GL_STENCIL_BACK_PASS_DEPTH_PASS;
		case STENCIL_BACK_REF:
		  return GL2.GL_STENCIL_BACK_REF;
		case STENCIL_BACK_VALUE_MASK:
		  return GL2.GL_STENCIL_BACK_VALUE_MASK;
		case STENCIL_BACK_WRITEMASK:
		  return GL2.GL_STENCIL_BACK_WRITEMASK;
		case VIEWPORT:
		  return GL2.GL_VIEWPORT;
		case SCISSOR_BOX:
		  return GL2.GL_SCISSOR_BOX;
		case COLOR_CLEAR_VALUE:
		  return GL2.GL_COLOR_CLEAR_VALUE;
		case COLOR_WRITEMASK:
		  return GL2.GL_COLOR_WRITEMASK;
		case UNPACK_ALIGNMENT:
		  return GL2.GL_UNPACK_ALIGNMENT;
		case PACK_ALIGNMENT:
		  return GL2.GL_PACK_ALIGNMENT;
		case MAX_TEXTURE_SIZE:
		  return GL2.GL_MAX_TEXTURE_SIZE;
		case MAX_VIEWPORT_DIMS:
		  return GL2.GL_MAX_VIEWPORT_DIMS;
		case SUBPIXEL_BITS:
		  return GL2.GL_SUBPIXEL_BITS;
		case RED_BITS:
		  return GL2.GL_RED_BITS;
		case GREEN_BITS:
		  return GL2.GL_GREEN_BITS;
		case BLUE_BITS:
		  return GL2.GL_BLUE_BITS;
		case ALPHA_BITS:
		  return GL2.GL_ALPHA_BITS;
		case DEPTH_BITS:
		  return GL2.GL_DEPTH_BITS;
		case STENCIL_BITS:
		  return GL2.GL_STENCIL_BITS;
		case POLYGON_OFFSET_UNITS:
		  return GL2.GL_POLYGON_OFFSET_UNITS;
		case POLYGON_OFFSET_FACTOR:
		  return GL2.GL_POLYGON_OFFSET_FACTOR;
		case TEXTURE_BINDING_2D:
		  return GL2.GL_TEXTURE_BINDING_2D;
		case SAMPLE_BUFFERS:
		  return GL2.GL_SAMPLE_BUFFERS;
		case SAMPLES:
		  return GL2.GL_SAMPLES;
		case SAMPLE_COVERAGE_VALUE:
		  return GL2.GL_SAMPLE_COVERAGE_VALUE;
		case SAMPLE_COVERAGE_INVERT:
		  return GL2.GL_SAMPLE_COVERAGE_INVERT;
		case NUM_COMPRESSED_TEXTURE_FORMATS:
		  return GL2.GL_NUM_COMPRESSED_TEXTURE_FORMATS;
		case COMPRESSED_TEXTURE_FORMATS:
		  return GL2.GL_COMPRESSED_TEXTURE_FORMATS;
		case DONT_CARE:
		  return GL2.GL_DONT_CARE;
		case FASTEST:
		  return GL2.GL_FASTEST;
		case NICEST:
		  return GL2.GL_NICEST;
		case GENERATE_MIPMAP_HINT:
		  return GL2.GL_GENERATE_MIPMAP_HINT;
		case BYTE:
		  return GL2.GL_BYTE;
		case UNSIGNED_BYTE:
		  return GL2.GL_UNSIGNED_BYTE;
		case SHORT:
		  return GL2.GL_SHORT;
		case UNSIGNED_SHORT:
		  return GL2.GL_UNSIGNED_SHORT;
		case INT:
		  return GL2.GL_INT;
		case UNSIGNED_INT:
		  return GL2.GL_UNSIGNED_INT;
		case FLOAT:
		  return GL2.GL_FLOAT;
		case DEPTH_COMPONENT:
		  return GL2.GL_DEPTH_COMPONENT;
		case ALPHA:
		  return GL2.GL_ALPHA;
		case RGB:
		  return GL2.GL_RGB;
		case RGBA:
		  return GL2.GL_RGBA;
		case LUMINANCE:
		  return GL2.GL_LUMINANCE;
		case LUMINANCE_ALPHA:
		  return GL2.GL_LUMINANCE_ALPHA;
		case UNSIGNED_SHORT_4_4_4_4:
		  return GL2.GL_UNSIGNED_SHORT_4_4_4_4;
		case UNSIGNED_SHORT_5_5_5_1:
		  return GL2.GL_UNSIGNED_SHORT_5_5_5_1;
		case UNSIGNED_SHORT_5_6_5:
		  return GL2.GL_UNSIGNED_SHORT_5_6_5;
		case FRAGMENT_SHADER:
		  return GL2.GL_FRAGMENT_SHADER;
		case VERTEX_SHADER:
		  return GL2.GL_VERTEX_SHADER;
		case MAX_VERTEX_ATTRIBS:
		  return GL2.GL_MAX_VERTEX_ATTRIBS;
		case MAX_VERTEX_UNIFORM_VECTORS:
		  return GL2.GL_MAX_VERTEX_UNIFORM_VECTORS;
		case MAX_VARYING_VECTORS:
		  return GL2.GL_MAX_VARYING_VECTORS;
		case MAX_COMBINED_TEXTURE_IMAGE_UNITS:
		  return GL2.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;
		case MAX_VERTEX_TEXTURE_IMAGE_UNITS:
		  return GL2.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS;
		case MAX_TEXTURE_IMAGE_UNITS:
		  return GL2.GL_MAX_TEXTURE_IMAGE_UNITS;
		case MAX_FRAGMENT_UNIFORM_VECTORS:
		  return GL2.GL_MAX_FRAGMENT_UNIFORM_VECTORS;
		case SHADER_TYPE:
		  return GL2.GL_SHADER_TYPE;
		case DELETE_STATUS:
		  return GL2.GL_DELETE_STATUS;
		case LINK_STATUS:
		  return GL2.GL_LINK_STATUS;
		case VALIDATE_STATUS:
		  return GL2.GL_VALIDATE_STATUS;
		case ATTACHED_SHADERS:
		  return GL2.GL_ATTACHED_SHADERS;
		case ACTIVE_UNIFORMS:
		  return GL2.GL_ACTIVE_UNIFORMS;
		case ACTIVE_UNIFORM_MAX_LENGTH:
		  return GL2.GL_ACTIVE_UNIFORM_MAX_LENGTH;
		case ACTIVE_ATTRIBUTES:
		  return GL2.GL_ACTIVE_ATTRIBUTES;
		case ACTIVE_ATTRIBUTE_MAX_LENGTH:
		  return GL2.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH;
		case SHADING_LANGUAGE_VERSION:
		  return GL2.GL_SHADING_LANGUAGE_VERSION;
		case CURRENT_PROGRAM:
		  return GL2.GL_CURRENT_PROGRAM;
		case NEVER:
		  return GL2.GL_NEVER;
		case LESS:
		  return GL2.GL_LESS;
		case EQUAL:
		  return GL2.GL_EQUAL;
		case LEQUAL:
		  return GL2.GL_LEQUAL;
		case GREATER:
		  return GL2.GL_GREATER;
		case NOTEQUAL:
		  return GL2.GL_NOTEQUAL;
		case GEQUAL:
		  return GL2.GL_GEQUAL;
		case ALWAYS:
		  return GL2.GL_ALWAYS;
		case KEEP:
		  return GL2.GL_KEEP;
		case REPLACE:
		  return GL2.GL_REPLACE;
		case INCR:
		  return GL2.GL_INCR;
		case DECR:
		  return GL2.GL_DECR;
		case INVERT:
		  return GL2.GL_INVERT;
		case INCR_WRAP:
		  return GL2.GL_INCR_WRAP;
		case DECR_WRAP:
		  return GL2.GL_DECR_WRAP;
		case VENDOR:
		  return GL2.GL_VENDOR;
		case RENDERER:
		  return GL2.GL_RENDERER;
		case VERSION:
		  return GL2.GL_VERSION;
		case NEAREST:
		  return GL2.GL_NEAREST;
		case LINEAR:
		  return GL2.GL_LINEAR;
		case NEAREST_MIPMAP_NEAREST:
		  return GL2.GL_NEAREST_MIPMAP_NEAREST;
		case LINEAR_MIPMAP_NEAREST:
		  return GL2.GL_LINEAR_MIPMAP_NEAREST;
		case NEAREST_MIPMAP_LINEAR:
		  return GL2.GL_NEAREST_MIPMAP_LINEAR;
		case LINEAR_MIPMAP_LINEAR:
		  return GL2.GL_LINEAR_MIPMAP_LINEAR;
		case TEXTURE_MAG_FILTER:
		  return GL2.GL_TEXTURE_MAG_FILTER;
		case TEXTURE_MIN_FILTER:
		  return GL2.GL_TEXTURE_MIN_FILTER;
		case TEXTURE_WRAP_S:
		  return GL2.GL_TEXTURE_WRAP_S;
		case TEXTURE_WRAP_T:
		  return GL2.GL_TEXTURE_WRAP_T;
		case TEXTURE_2D:
		  return GL2.GL_TEXTURE_2D;
		case TEXTURE:
		  return GL2.GL_TEXTURE;
		case TEXTURE_CUBE_MAP:
		  return GL2.GL_TEXTURE_CUBE_MAP;
		case TEXTURE_BINDING_CUBE_MAP:
		  return GL2.GL_TEXTURE_BINDING_CUBE_MAP;
		case TEXTURE_CUBE_MAP_POSITIVE_X:
		  return GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
		case TEXTURE_CUBE_MAP_NEGATIVE_X:
		  return GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
		case TEXTURE_CUBE_MAP_POSITIVE_Y:
		  return GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
		case TEXTURE_CUBE_MAP_NEGATIVE_Y:
		  return GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
		case TEXTURE_CUBE_MAP_POSITIVE_Z:
		  return GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
		case TEXTURE_CUBE_MAP_NEGATIVE_Z:
		  return GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;
		case MAX_CUBE_MAP_TEXTURE_SIZE:
		  return GL2.GL_MAX_CUBE_MAP_TEXTURE_SIZE;
		case TEXTURE0:
		  return GL2.GL_TEXTURE0;
		case TEXTURE1:
		  return GL2.GL_TEXTURE1;
		case TEXTURE2:
		  return GL2.GL_TEXTURE2;
		case TEXTURE3:
		  return GL2.GL_TEXTURE3;
		case TEXTURE4:
		  return GL2.GL_TEXTURE4;
		case TEXTURE5:
		  return GL2.GL_TEXTURE5;
		case TEXTURE6:
		  return GL2.GL_TEXTURE6;
		case TEXTURE7:
		  return GL2.GL_TEXTURE7;
		case TEXTURE8:
		  return GL2.GL_TEXTURE8;
		case TEXTURE9:
		  return GL2.GL_TEXTURE9;
		case TEXTURE10:
		  return GL2.GL_TEXTURE10;
		case TEXTURE11:
		  return GL2.GL_TEXTURE11;
		case TEXTURE12:
		  return GL2.GL_TEXTURE12;
		case TEXTURE13:
		  return GL2.GL_TEXTURE13;
		case TEXTURE14:
		  return GL2.GL_TEXTURE14;
		case TEXTURE15:
		  return GL2.GL_TEXTURE15;
		case TEXTURE16:
		  return GL2.GL_TEXTURE16;
		case TEXTURE17:
		  return GL2.GL_TEXTURE17;
		case TEXTURE18:
		  return GL2.GL_TEXTURE18;
		case TEXTURE19:
		  return GL2.GL_TEXTURE19;
		case TEXTURE20:
		  return GL2.GL_TEXTURE20;
		case TEXTURE21:
		  return GL2.GL_TEXTURE21;
		case TEXTURE22:
		  return GL2.GL_TEXTURE22;
		case TEXTURE23:
		  return GL2.GL_TEXTURE23;
		case TEXTURE24:
		  return GL2.GL_TEXTURE24;
		case TEXTURE25:
		  return GL2.GL_TEXTURE25;
		case TEXTURE26:
		  return GL2.GL_TEXTURE26;
		case TEXTURE27:
		  return GL2.GL_TEXTURE27;
		case TEXTURE28:
		  return GL2.GL_TEXTURE28;
		case TEXTURE29:
		  return GL2.GL_TEXTURE29;
		case TEXTURE30:
		  return GL2.GL_TEXTURE30;
		case TEXTURE31:
		  return GL2.GL_TEXTURE31;
		case ACTIVE_TEXTURE:
		  return GL2.GL_ACTIVE_TEXTURE;
		case REPEAT:
		  return GL2.GL_REPEAT;
		case CLAMP_TO_EDGE:
		  return GL2.GL_CLAMP_TO_EDGE;
		case MIRRORED_REPEAT:
		  return GL2.GL_MIRRORED_REPEAT;
		case FLOAT_VEC2:
		  return GL2.GL_FLOAT_VEC2;
		case FLOAT_VEC3:
		  return GL2.GL_FLOAT_VEC3;
		case FLOAT_VEC4:
		  return GL2.GL_FLOAT_VEC4;
		case INT_VEC2:
		  return GL2.GL_INT_VEC2;
		case INT_VEC3:
		  return GL2.GL_INT_VEC3;
		case INT_VEC4:
		  return GL2.GL_INT_VEC4;
		case BOOL:
		  return GL2.GL_BOOL;
		case BOOL_VEC2:
		  return GL2.GL_BOOL_VEC2;
		case BOOL_VEC3:
		  return GL2.GL_BOOL_VEC3;
		case BOOL_VEC4:
		  return GL2.GL_BOOL_VEC4;
		case FLOAT_MAT2:
		  return GL2.GL_FLOAT_MAT2;
		case FLOAT_MAT3:
		  return GL2.GL_FLOAT_MAT3;
		case FLOAT_MAT4:
		  return GL2.GL_FLOAT_MAT4;
		case SAMPLER_2D:
		  return GL2.GL_SAMPLER_2D;
		case SAMPLER_CUBE:
		  return GL2.GL_SAMPLER_CUBE;
		case VERTEX_ATTRIB_ARRAY_ENABLED:
		  return GL2.GL_VERTEX_ATTRIB_ARRAY_ENABLED;
		case VERTEX_ATTRIB_ARRAY_SIZE:
		  return GL2.GL_VERTEX_ATTRIB_ARRAY_SIZE;
		case VERTEX_ATTRIB_ARRAY_STRIDE:
		  return GL2.GL_VERTEX_ATTRIB_ARRAY_STRIDE;
		case VERTEX_ATTRIB_ARRAY_TYPE:
		  return GL2.GL_VERTEX_ATTRIB_ARRAY_TYPE;
		case VERTEX_ATTRIB_ARRAY_NORMALIZED:
		  return GL2.GL_VERTEX_ATTRIB_ARRAY_NORMALIZED;
		case VERTEX_ATTRIB_ARRAY_POINTER:
		  return GL2.GL_VERTEX_ATTRIB_ARRAY_POINTER;
		case VERTEX_ATTRIB_ARRAY_BUFFER_BINDING:
		  return GL2.GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING;
		case COMPILE_STATUS:
		  return GL2.GL_COMPILE_STATUS;
		case INFO_LOG_LENGTH:
		  return GL2.GL_INFO_LOG_LENGTH;
		case SHADER_SOURCE_LENGTH:
		  return GL2.GL_SHADER_SOURCE_LENGTH;
		case LOW_FLOAT:
		  return GL2.GL_LOW_FLOAT;
		case MEDIUM_FLOAT:
		  return GL2.GL_MEDIUM_FLOAT;
		case HIGH_FLOAT:
		  return GL2.GL_HIGH_FLOAT;
		case LOW_INT:
		  return GL2.GL_LOW_INT;
		case MEDIUM_INT:
		  return GL2.GL_MEDIUM_INT;
		case HIGH_INT:
		  return GL2.GL_HIGH_INT;
		case FRAMEBUFFER:
		  return GL2.GL_FRAMEBUFFER;
		case RENDERBUFFER:
		  return GL2.GL_RENDERBUFFER;
		case RGBA4:
		  return GL2.GL_RGBA4;
		case RGB5_A1:
		  return GL2.GL_RGB5_A1;
		case RGB565:
		  return GL2.GL_RGB565;
		case DEPTH_COMPONENT16:
		  return GL2.GL_DEPTH_COMPONENT16;
		case STENCIL_INDEX:
		  return GL2.GL_STENCIL_INDEX;
		case STENCIL_INDEX8:
		  return GL2.GL_STENCIL_INDEX8;
		case DEPTH_STENCIL:
		  return GL2.GL_DEPTH_STENCIL;
		case RENDERBUFFER_WIDTH:
		  return GL2.GL_RENDERBUFFER_WIDTH;
		case RENDERBUFFER_HEIGHT:
		  return GL2.GL_RENDERBUFFER_HEIGHT;
		case RENDERBUFFER_INTERNAL_FORMAT:
		  return GL2.GL_RENDERBUFFER_INTERNAL_FORMAT;
		case RENDERBUFFER_RED_SIZE:
		  return GL2.GL_RENDERBUFFER_RED_SIZE;
		case RENDERBUFFER_GREEN_SIZE:
		  return GL2.GL_RENDERBUFFER_GREEN_SIZE;
		case RENDERBUFFER_BLUE_SIZE:
		  return GL2.GL_RENDERBUFFER_BLUE_SIZE;
		case RENDERBUFFER_ALPHA_SIZE:
		  return GL2.GL_RENDERBUFFER_ALPHA_SIZE;
		case RENDERBUFFER_DEPTH_SIZE:
		  return GL2.GL_RENDERBUFFER_DEPTH_SIZE;
		case RENDERBUFFER_STENCIL_SIZE:
		  return GL2.GL_RENDERBUFFER_STENCIL_SIZE;
		case FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE:
		  return GL2.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE;
		case FRAMEBUFFER_ATTACHMENT_OBJECT_NAME:
		  return GL2.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME;
		case FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL:
		  return GL2.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL;
		case FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE:
		  return GL2.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE;
		case COLOR_ATTACHMENT0:
		  return GL2.GL_COLOR_ATTACHMENT0;
		case DEPTH_ATTACHMENT:
		  return GL2.GL_DEPTH_ATTACHMENT;
		case STENCIL_ATTACHMENT:
		  return GL2.GL_STENCIL_ATTACHMENT;
		case DEPTH_STENCIL_ATTACHMENT:
		  return GL2.GL_DEPTH_STENCIL_ATTACHMENT;
		case NONE:
		  return GL2.GL_NONE;
		case FRAMEBUFFER_COMPLETE:
		  return GL2.GL_FRAMEBUFFER_COMPLETE;
		case FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
		  return GL2.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
		case FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
		  return GL2.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
		case FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
		  return GL2.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS;
		case FRAMEBUFFER_UNSUPPORTED:
		  return GL2.GL_FRAMEBUFFER_UNSUPPORTED;
		case FRAMEBUFFER_BINDING:
		  return GL2.GL_FRAMEBUFFER_BINDING;
		case RENDERBUFFER_BINDING:
		  return GL2.GL_RENDERBUFFER_BINDING;
		case MAX_RENDERBUFFER_SIZE:
		  return GL2.GL_MAX_RENDERBUFFER_SIZE;
		case INVALID_FRAMEBUFFER_OPERATION:
		  return GL2.GL_INVALID_FRAMEBUFFER_OPERATION;
		default:
		  return -1;
		}
	}

	public void init() {
		if (serverWindow_)
			ctx_ = window_.getContext();
		else
			ctx_ = offscreenDrawable_.getContext();
		glCtx_ = ctx_.getGL().getGL2();
		glInterface_.initializeGL();

		if (serverWindow_)
			window_.swapBuffers();
	}
	
	public void update() {
		if (serverWindow_)
			ctx_ = window_.getContext();
		else
			ctx_ = offscreenDrawable_.getContext();
		glCtx_ = ctx_.getGL().getGL2();
		glInterface_.updateGL();

		if (serverWindow_)
			window_.swapBuffers();
	}

	public void display() {
		if (serverWindow_)
			ctx_ = window_.getContext();
		else
			ctx_ = offscreenDrawable_.getContext();
		glCtx_ = ctx_.getGL().getGL2();
		glCtx_.glEnable(GL2.GL_VERTEX_PROGRAM_POINT_SIZE);
		glCtx_.glEnable(GL2.GL_POINT_SPRITE);
		glInterface_.paintGL();

		if (serverWindow_)
			window_.swapBuffers();
	}

	public void reshape(int x, int y, int width,
			int height) {
		glInterface_.resizeGL(width, height);
	}
	
	
	private GLWindow window_;
	private GLOffscreenAutoDrawable offscreenDrawable_;
	private GLContext ctx_;
	
	private WMemoryResource mr_;
	private WLink link_;
	
	private GL2 glCtx_;
	
	private StringWriter js_;
	private int jsValues_;
	
	private static final boolean debug_ = false;

	

	@Override
	public void uniformMatrix4(UniformLocation location, JavaScriptMatrix4x4 jsm) {
		Matrix4f matrix = jsm.getValue();
		
		float[] mat = new float[16];
	    for (int i=0; i<4; i++){
	    	for (int j=0; j<4; j++){
	    		mat[i*4+j] = matrix.getElement(j, i);
	    	}
	    }
	    glCtx_.glUniformMatrix4fv(location.getId(), 1, false, mat, 0);
	    if (debug_) 
	    	System.out.println(glCtx_.glGetError());
	}

	@Override
	public void initJavaScriptMatrix4(final WGLWidget.JavaScriptMatrix4x4 mat) {
	    if (!mat.hasContext())
	      glInterface_.addJavaScriptMatrix4(mat);
	    else if (mat.context_ != glInterface_)
	      throw new WException("JavaScriptMatrix4x4: associated WGLWidget is not equal to the WGLWidget it's being initialized in");
	    if (mat.isInitialized())
	      throw new WException("JavaScriptMatrix4x4: matrix already initialized");

	    javax.vecmath.Matrix4f m = mat.getValue();
	    this.js_.append(mat.getJsRef()).append("=");
	    WebGLUtils.renderfv(this.js_, m, JsArrayType.Array);
	    this.js_.append(";");

	    mat.initialize();
	}

	@Override
	public void setJavaScriptMatrix4(JavaScriptMatrix4x4 jsm, Matrix4f m) {
		javax.vecmath.Matrix4f t = WebGLUtils.transpose(m);
		this.js_.append(WEnvironment.getJavaScriptWtScope());
		this.js_.append(".glMatrix.mat4.set(");
		WebGLUtils.renderfv(this.js_, t, JsArrayType.Array);
		this.js_.append(", ").append("obj.jsValues[");
		this.js_.append(String.valueOf(jsm.getId())).append("]);");
	}

	@Override
	public void initJavaScriptVector(final WGLWidget.JavaScriptVector vec) {
	    if (!vec.hasContext())
	      glInterface_.addJavaScriptVector(vec);
	    else if (vec.context_ != glInterface_)
	      throw new WException("JavaScriptMatrix4x4: associated WGLWidget is not equal to the WGLWidget it's being initialized in");
	    if (vec.isInitialized())
	      throw new WException("JavaScriptVector: vector already initialized");

	    List<Float> v = vec.getValue();
	    js_.append(vec.getJsRef()).append("= new Float32Array([");
	    for (int i = 0; i < vec.getLength(); i++) {
	      if (i != 0)
		js_.append(",");
	      js_.append(String.valueOf(v.get(i)));
	    }
	    js_.append("]);");

	    vec.initialize();
	}

	@Override
	public void setJavaScriptVector(JavaScriptVector jsv, List<Float> v) {
		if (jsv.getLength() != v.size())
		  throw new WException("Trying to set a JavaScriptVector with incompatible length!");
		for (int i = 0; i < jsv.getLength(); ++i) {
		  this.js_.append("obj.jsValues[").append(String.valueOf(jsv.getId()))
		    .append("][").append(String.valueOf(i)).append("] = ")
		    .append(String.valueOf(v.get(i))).append(";");
		}
	}

	@Override
	public void setClientSideMouseHandler(String handlerCode) {
	  this.js_.append("obj.setMouseHandler(").append(handlerCode).append(");");
	}

	@Override
	public void setClientSideLookAtHandler(JavaScriptMatrix4x4 m, double centerX,
			double centerY, double centerZ, double uX, double uY, double uZ,
			double pitchRate, double yawRate) {
		this.js_.append("obj.setMouseHandler(new obj.LookAtMouseHandler(")
		.append("obj.jsValues[").append(String.valueOf(m.getId())).append("]")
		.append(",[").append(String.valueOf(centerX)).append(",")
		.append(String.valueOf(centerY)).append(",")
		.append(String.valueOf(centerZ)).append("],").append("[")
		.append(String.valueOf(uX)).append(",")
		.append(String.valueOf(uY)).append(",")
		.append(String.valueOf(uZ)).append("],")
		.append(String.valueOf(pitchRate)).append(",")
		.append(String.valueOf(yawRate)).append("));");
	}

	@Override
	public void setClientSideWalkHandler(JavaScriptMatrix4x4 m,
			double frontStep, double rotStep) {
		this.js_.append("obj.setMouseHandler(new obj.WalkMouseHandler(")
		.append("obj.jsValues[").append(String.valueOf(m.getId())).append("]").append(",")
		.append(String.valueOf(frontStep)).append(",")
		.append(String.valueOf(rotStep)).append("));");
	}

	@Override
	public void clearBinaryResources() { // only functional for WClientGLWidget
	}

	@Override
	public void texImage2D(GLenum target, int level, GLenum internalformat,
			GLenum format, GLenum type, String image) {
		BufferedImage initialImage = null;
		try {
			initialImage = ImageIO.read(new File(image));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int openGlInternalFormat = serverGLenum(internalformat);
        int openGlImageFormat = serverGLenum(format);
        WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, initialImage.getWidth(), initialImage.getHeight(), 4, null);
        ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] { 8, 8, 8, 8 }, 
                true, false, 
                ComponentColorModel.TRANSLUCENT, 
                DataBuffer.TYPE_BYTE);
        BufferedImage bufImg = new BufferedImage(colorModel,
                raster, false,
                null);

        Graphics2D g = bufImg.createGraphics();
        g.translate(0, initialImage.getHeight());
        g.scale(1, -1);
        g.drawImage(initialImage, null, null);

        DataBufferByte imgBuf = (DataBufferByte) raster.getDataBuffer();
        byte[] bytes = imgBuf.getData();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        g.dispose();
        
        glCtx_.glTexImage2D(GL2.GL_TEXTURE_2D, 0, openGlInternalFormat, initialImage.getWidth(), initialImage.getHeight(),
        					0, openGlImageFormat, GL2.GL_UNSIGNED_BYTE, buffer);
        if (debug_)
        	System.out.println(glCtx_.glGetError());
	}

	@Override
	public void texImage2D(GLenum target, int level, GLenum internalformat,
			GLenum format, GLenum type, WPaintDevice paintdevice) {
		ByteArrayInputStream dataIn = null;
		if (((paintdevice) instanceof WRasterPaintDevice ? (WRasterPaintDevice) (paintdevice)
				: null) != null) {
			WRasterPaintDevice rpd = ((paintdevice) instanceof WRasterPaintDevice ? (WRasterPaintDevice) (paintdevice)
					: null);
			ByteArrayOutputStream data = new ByteArrayOutputStream();
	    	try {
	    		rpd.write(data);
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    	dataIn = new ByteArrayInputStream(data.toByteArray());
	    	
		} else {
			throw new WException("WServerGLWidget: invalid WPaintDevice");
		}
		
		BufferedImage initialImage = null;
		try {
			initialImage = ImageIO.read(dataIn);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int openGlInternalFormat = serverGLenum(internalformat);
        int openGlImageFormat = serverGLenum(format);
        WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, initialImage.getWidth(), initialImage.getHeight(), 4, null);
        ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] { 8, 8, 8, 8 }, 
                true, false, 
                ComponentColorModel.TRANSLUCENT, 
                DataBuffer.TYPE_BYTE);
        BufferedImage bufImg = new BufferedImage(colorModel,
                raster, false,
                null);

        Graphics2D g = bufImg.createGraphics();
        g.translate(0, initialImage.getHeight());
        g.scale(1, -1);
        g.drawImage(initialImage, null, null);

        DataBufferByte imgBuf = (DataBufferByte) raster.getDataBuffer();
        byte[] bytes = imgBuf.getData();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        g.dispose();
        
        glCtx_.glTexImage2D(GL2.GL_TEXTURE_2D, 0, openGlInternalFormat, initialImage.getWidth(), initialImage.getHeight(),
        					0, openGlImageFormat, GL2.GL_UNSIGNED_BYTE, buffer);
        if (debug_)
        	System.out.println(glCtx_.glGetError());
	}


	@Override
	public void injectJS(String jsString) { // only functional for WClientGLWidget
	}

	private static final boolean serverWindow_ = false;

}
