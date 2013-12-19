package eu.webtoolkit.jwt.examples.webgl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.vecmath.Matrix4f;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WGLWidget;
import eu.webtoolkit.jwt.WebGLUtils;

//You must inherit WGLWidget to draw a 3D scene
public class PaintWidget extends WGLWidget {
	public static List<Double> data = new ArrayList<Double>();
	static {
		try {
			InputStream is = PaintWidget.class.getResourceAsStream("/eu/webtoolkit/jwt/examples/webgl/teapot.obj");
			readObj(is, data);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PaintWidget(WContainerWidget root) {
		super(root);
	}

	private double[] centerpoint() {
		double minx, miny, minz;
		double maxx, maxy, maxz;
		minx = maxx = data.get(0);
		miny = maxy = data.get(1);
		minz = maxz = data.get(2);
		for (int i = 0; i < data.size() / 6; ++i) {
			if (data.get(i * 6) < minx)
				minx = data.get(i * 6);
			if (data.get(i * 6) > maxx)
				maxx = data.get(i * 6);
			if (data.get(i * 6 + 1) < miny)
				miny = data.get(i * 6 + 1);
			if (data.get(i * 6 + 1) > maxy)
				maxy = data.get(i * 6 + 1);
			if (data.get(i * 6 + 2) < minz)
				minz = data.get(i * 6 + 2);
			if (data.get(i * 6 + 2) > maxz)
				maxz = data.get(i * 6 + 2);
		}

		double[] cp = new double[3];
		cp[0] = (minx + maxx) / 2.;
		cp[1] = (miny + maxy) / 2.;
		cp[2] = (minz + maxz) / 2.;
		return cp;
	}

	// The initializeGL() captures all JS commands that are to be executed
	// before the scene is rendered for the first time. It is executed only
	// once. It is re-executed when the WebGL context is restored after it
	// was lost.
	// In general, it should be used to set up shaders, create VBOs, initialize
	// matrices, ...
	@Override
	public void initializeGL() {
		// In order to know where to look at, calculate the centerpoint of the
		// scene
		double cx, cy, cz;
		double[] cp = centerpoint();
		cx = cp[0];
		cy = cp[1];
		cz = cp[2];

		// Transform the world so that we look at the centerpoint of the scene
		Matrix4f worldTransform = new Matrix4f();
		worldTransform.setIdentity();
		WebGLUtils.lookAt(worldTransform, cx, cy, cz + 10, // camera position
				cx, cy, cz, // looking at
				0, 1, 0); // 'up' vector

		// We want to be able to change the camera position client-side. In
		// order to do so, the world transformation matrix must be stored in
		// a matrix that can be manipulated from JavaScript.
		jsMatrix_ = createJavaScriptMatrix4();
		setJavaScriptMatrix4(jsMatrix_, worldTransform);

		// This installs a client-side mouse handler that modifies the
		// world transformation matrix. Like WMatrix4x4::lookAt, this works
		// by specifying a center point and an up direction; mouse movements
		// will allow the camera to be moved around the center point.
		setClientSideLookAtHandler(jsMatrix_, // the name of the JS matrix
				cx, cy, cz, // the center point
				0, 1, 0, // the up direction
				0.005, 0.005); // 'speed' factors
		// Alternative: this installs a client-side mouse handler that allows
		// to 'walk' around: go forward, backward, turn left, turn right, ...
		// setClientSideWalkHandler(jsMatrix_, 0.05, 0.005);

		// First, load a simple shader
		Shader fragmentShader = createShader(GLenum.FRAGMENT_SHADER);
		shaderSource(fragmentShader, fragmentShader_);
		compileShader(fragmentShader);
		Shader vertexShader = createShader(GLenum.VERTEX_SHADER);
		shaderSource(vertexShader, vertexShader_);
		compileShader(vertexShader);
		shaderProgram_ = createProgram();
		attachShader(shaderProgram_, vertexShader);
		attachShader(shaderProgram_, fragmentShader);
		linkProgram(shaderProgram_);
		useProgram(shaderProgram_);

		// Extract the references to the attributes from the shader.
		vertexNormalAttribute_ = getAttribLocation(shaderProgram_,
				"aVertexNormal");
		vertexPositionAttribute_ = getAttribLocation(shaderProgram_,
				"aVertexPosition");
		enableVertexAttribArray(vertexPositionAttribute_);
		enableVertexAttribArray(vertexNormalAttribute_);

		// Extract the references the uniforms from the shader
		pMatrixUniform_ = getUniformLocation(shaderProgram_, "uPMatrix");
		cMatrixUniform_ = getUniformLocation(shaderProgram_, "uCMatrix");
		mvMatrixUniform_ = getUniformLocation(shaderProgram_, "uMVMatrix");
		nMatrixUniform_ = getUniformLocation(shaderProgram_, "uNMatrix");

		// Create a Vertex Buffer Object (VBO) and load all polygon's data
		// (points, normals) into it. In this case we use one VBO that contains
		// all data (6 per point: vx, vy, vz, nx, ny, nz); alternatively you
		// can use multiple VBO's (e.g. one VBO for normals, one for points,
		// one for texture coordinates).
		// Note that if you use indexed buffers, you cannot have indexes
		// larger than 65K, due to the limitations of WebGL.
		objBuffer_ = createBuffer();
		bindBuffer(GLenum.ARRAY_BUFFER, objBuffer_);
		float[] fData = new float[data.size()];
		java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(data.size()*4);
		for (int i = 0; i < data.size(); i++)
			buf.putFloat(data.get(i).floatValue());
		bufferDatafv(GLenum.ARRAY_BUFFER, buf,
				GLenum.STATIC_DRAW);

		// Set the clear color to a transparant background
		clearColor(0, 0, 0, 0);

		// Reset Z-buffer, enable Z-buffering
		clearDepth(1);
		enable(GLenum.DEPTH_TEST);
		depthFunc(GLenum.LEQUAL);
	}

	@Override
	public void paintGL() {
		// Clear color an depth buffers

		clear(EnumSet.of(GLenum.COLOR_BUFFER_BIT, GLenum.DEPTH_BUFFER_BIT));

		// Configure the shader: set the uniforms
		// Uniforms are 'configurable constants' in a shader: they are
		// identical for every point that has to be drawn.
		// Set the camera transformation to the value of a client-side JS matrix
		uniformMatrix4(cMatrixUniform_, jsMatrix_);
		// Often, a model matrix is used to move the model around. We're happy
		// with the location of the model, so we leave it as the unit matrix
		Matrix4f modelMatrix = new Matrix4f();
		modelMatrix.setIdentity();
		uniformMatrix4(mvMatrixUniform_, modelMatrix);
		// The next one is a bit complicated. In desktop OpenGL, a shader
		// has the gl_NormalMatrix matrix available in the shader language,
		// a matrix that is used to transform normals to e.g. implement proper
		// Phong shading (google will help you to find a detailed explanation
		// of why you need it). It is the transposed inverse of the model view
		// matrix. Unfortunately, this matrix is not available in WebGL, so if
		// you want to do phong shading, you must calculate it yourself.
		// Wt provides methods to calculate the transposed inverse of a matrix,
		// when client-side JS matrices are involved. Here, we inverse-transpose
		// the product of the client-side camera matrix and the model matrix.
		uniformMatrix4(nMatrixUniform_, jsMatrix_.multiply(modelMatrix)
				.inverted().transposed());

		// Configure the shaders: set the attributes.
		// Attributes are 'variables' within a shader: they vary for every point
		// that has to be drawn. All are stored in one VBO.
		bindBuffer(GLenum.ARRAY_BUFFER, objBuffer_);
		// Configure the vertex attributes:
		vertexAttribPointer(vertexPositionAttribute_, 3, // size: Every vertex
															// has an X, Y anc Z
															// component
				GLenum.FLOAT, // type: They are floats
				false, // normalized: Please, do NOT normalize the vertices
				2 * 3 * 4, // stride: The first byte of the next vertex is
							// located this
				// amount of bytes further. The format of the VBO is
				// vx, vy, vz, nx, ny, nz and every element is a
				// Float32, hence 4 bytes large
				0); // offset: The byte position of the first vertex in the
					// buffer
		// is 0.
		vertexAttribPointer(vertexNormalAttribute_, 3, GLenum.FLOAT, false,
				2 * 3 * 4, // stride: see above. We jump from normal to normal
							// now
				3 * 4); // offset: the first normal is located after the first
						// vertex
						// position, consisting of three four-byte floats

		// Now draw all the triangles.
		drawArrays(GLenum.TRIANGLES, 0, data.size() / 6);
	}

	@Override
	public void resizeGL(int width, int height) {
		// Set the viewport size.
		viewport(0, 0, width, height);

		// Set projection matrix to some fixed values
		Matrix4f proj = new Matrix4f();
		proj.setIdentity();
		WebGLUtils.perspective(proj, 45, ((double) width) / height, 1., 40.);
		uniformMatrix4(pMatrixUniform_, proj);
	}

	// Sets the shader source. Must be set before the widget is first rendered.
	public void setShaders(String vertexShader, String fragmentShader) {
		vertexShader_ = vertexShader;
		fragmentShader_ = fragmentShader;
	}

	public static void readObj(InputStream is, List<Double> data)
			throws NumberFormatException, IOException {
		List<Double> points = new ArrayList<Double>();
		List<Double> normals = new ArrayList<Double>();
		List<Double> textures = new ArrayList<Double>();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = in.readLine()) != null) {
			List<String> splitLine = new ArrayList<String>();
			StringTokenizer tk = new StringTokenizer(line, " ", false);
			while (tk.hasMoreTokens())
				splitLine.add(tk.nextToken());
			
			if (splitLine.get(0).equals("v")) {
				points.add(Double.parseDouble(splitLine.get(1)));
				points.add(Double.parseDouble(splitLine.get(2)));
				points.add(Double.parseDouble(splitLine.get(3)));
			} else {
				if (splitLine.get(0).equals("vn")) {
					normals.add(Double.parseDouble(splitLine.get(1)));
					normals.add(Double.parseDouble(splitLine.get(2)));
					normals.add(Double.parseDouble(splitLine.get(3)));
				} else {
					if (splitLine.get(0).equals("vt")) {
						textures.add(Double.parseDouble(splitLine.get(1)));
						textures.add(Double.parseDouble(splitLine.get(2)));
					} else {
						if (splitLine.get(0).equals("f")) {
							for (int i = 1; i < splitLine.size(); ++i) {
								String[] faceLine = splitLine.get(i).split("/");
								int v;
								int t;
								int n;
								v = Integer.parseInt(faceLine[0]);
								if (!faceLine[1].equals("")) {
									t = Integer.parseInt(faceLine[1]);
								} else {
									t = -1;
								}
								if (!faceLine[2].equals("")) {
									n = Integer.parseInt(faceLine[2]);
								} else {
									n = -1;
								}
								data.add(points.get((v - 1) * 3));
								data.add(points.get((v - 1) * 3 + 1));
								data.add(points.get((v - 1) * 3 + 2));
								data.add(normals.get((n - 1) * 3));
								data.add(normals.get((n - 1) * 3 + 1));
								data.add(normals.get((n - 1) * 3 + 2));
							}
						} else {
							System.err
									.append("ERROR in obj file: unknown line\n");
							return;
						}
					}
				}
			}
		}
	}

	// The shaders, in plain text format
	private String vertexShader_;
	private String fragmentShader_;

	// Program and related variables
	private Program shaderProgram_;
	private AttribLocation vertexPositionAttribute_;
	private AttribLocation vertexNormalAttribute_;
	private UniformLocation pMatrixUniform_;
	private UniformLocation cMatrixUniform_;
	private UniformLocation mvMatrixUniform_;
	private UniformLocation nMatrixUniform_;

	// A client-side JavaScript matrix variable
	private JavaScriptMatrix4x4 jsMatrix_;

	// The so-called VBOs, Vertex Buffer Objects
	// This one contains both vertex (xyz) and normal (xyz) data
	private Buffer objBuffer_;
}