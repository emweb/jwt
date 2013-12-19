package eu.webtoolkit.jwt.examples.features.miniwebgl;

import java.nio.FloatBuffer;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WGLWidget;

public class PaintWidget extends WGLWidget {
	// This fragment shader simply paints white.
	private String fragmentShaderSrc = "#ifdef GL_ES\n"
			+ "precision highp float;\n" + "#endif\n" + "\n"
			+ "void main(void) {\n" + "  gl_FragColor = vec4(1, 1, 1, 1);\n"
			+ "}\n";

	// This vertex shader does not transform at all
	private String vertexShaderSrc = "attribute vec3 aVertexPosition;\n" + "\n"
			+ "void main(void) {\n"
			+ "  gl_Position = vec4(aVertexPosition, 1.0);\n" + "}\n";

	public PaintWidget(WContainerWidget root)
	{
		super(root);
	}

	public void initializeGL() {
		// Create a shader
		Shader fragmentShader = createShader(GLenum.FRAGMENT_SHADER);
		shaderSource(fragmentShader, fragmentShaderSrc);
		compileShader(fragmentShader);
		Shader vertexShader = createShader(GLenum.VERTEX_SHADER);
		shaderSource(vertexShader, vertexShaderSrc);
		compileShader(vertexShader);
		shaderProgram_ = createProgram();
		attachShader(shaderProgram_, vertexShader);
		attachShader(shaderProgram_, fragmentShader);
		linkProgram(shaderProgram_);
		useProgram(shaderProgram_);

		// Extract the attribute location
		vertexPositionAttribute_ = getAttribLocation(shaderProgram_,
				"aVertexPosition");
		enableVertexAttribArray(vertexPositionAttribute_);

		// Now, preload the vertex buffer
		triangleVertexPositionBuffer_ = createBuffer();
		bindBuffer(GLenum.ARRAY_BUFFER, triangleVertexPositionBuffer_);
		float trianglePosition[] = { 0.0f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f,
				0.5f, -0.5f, 0.0f };
		bufferDatafv(GLenum.ARRAY_BUFFER, FloatBuffer.wrap(trianglePosition),
				GLenum.STATIC_DRAW);
	}

	public void resizeGL(int width, int height) {
		viewport(0, 0, width, height);
	}

	public void paintGL() {
		// Drawing starts here!
		clearColor(0, 0, 0, 1);
		disable(GLenum.DEPTH_TEST);
		disable(GLenum.CULL_FACE);
		clear(GLenum.COLOR_BUFFER_BIT);

		useProgram(shaderProgram_);

		// Draw the scene
		bindBuffer(GLenum.ARRAY_BUFFER, triangleVertexPositionBuffer_);
		vertexAttribPointer(vertexPositionAttribute_, 3, GLenum.FLOAT, false,
				0, 0);
		drawArrays(GLenum.TRIANGLES, 0, 3);
	}

	private Program shaderProgram_;
	private AttribLocation vertexPositionAttribute_;

	private Buffer triangleVertexPositionBuffer_;
}