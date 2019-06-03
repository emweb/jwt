package eu.webtoolkit.jwt.examples.webgl;

import eu.webtoolkit.jwt.Signal1.Listener;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTextArea;
import eu.webtoolkit.jwt.WtServlet;

public class Teapot extends WtServlet {
	private static final long serialVersionUID = 1L;

	class WebGLDemo extends WApplication
	{
		public WebGLDemo(WEnvironment env) {
			  super(env);
			    setTitle("WebGL Demo");

			    getRoot().addWidget(new WText("If your browser supports WebGL, you'll " +
			      "see a teapot below.<br/>Use your mouse to move around the teapot.<br/>" +
			      "Edit the shaders below the teapot to change how the teapot is rendered."));
			    getRoot().addWidget(new WBreak());

			    paintWidget_ = null;

			    glContainer_ = new WContainerWidget(getRoot());
			    glContainer_.resize(500, 500);
			    glContainer_.setInline(false);

			    WPushButton updateButton = new WPushButton("Update shaders", getRoot());
			    updateButton.clicked().addListener(this, new Listener<WMouseEvent>() {
					public void trigger(WMouseEvent arg) {
						updateShaders();
					}
				});
			    WPushButton resetButton = new WPushButton("Reset shaders", getRoot());
			    resetButton.clicked().addListener(this, new Listener<WMouseEvent>() {
					public void trigger(WMouseEvent arg) {
						resetShaders();
					}
				});

			    WTabWidget tabs = new WTabWidget(getRoot());

			    fragmentShaderText_ = new WTextArea();
			    fragmentShaderText_.resize(750, 250);
			    tabs.addTab(fragmentShaderText_, "Fragment Shader");
			    vertexShaderText_ = new WTextArea();
			    vertexShaderText_.resize(750, 250);
			    tabs.addTab(vertexShaderText_, "Vertex Shader");

			    resetShaders();
		}

		private void updateShaders() {
			  paintWidget_ = new PaintWidget(glContainer_);
			  paintWidget_.resize(500, 500);
			  paintWidget_.setShaders(vertexShaderText_.getText(),
			    fragmentShaderText_.getText());
			  /*paintWidget_.setAlternativeContent(new WImage("pics/nowebgl.png"));*/
		}
		
		private void resetShaders() {
			  fragmentShaderText_.setText(fragmentShaderSrc);
			  vertexShaderText_.setText(vertexShaderSrc);
			  updateShaders();
		}

		private WContainerWidget glContainer_;
		private PaintWidget paintWidget_;
		private WTextArea fragmentShaderText_;
		private WTextArea vertexShaderText_;
	}
	
	@Override
	public WApplication createApplication(WEnvironment env) {
		return new WebGLDemo(env);
	}
	
	static String fragmentShaderSrc =
		"#ifdef GL_ES\n" + 
		"precision highp float;\n" + 
		"#endif\n" + 
		"\n" + 
		"varying vec3 vLightWeighting;\n" +
		"\n" + 
		"void main(void) {\n" +
		"  vec4 matColor = vec4(0.278, 0.768, 0.353, 1.0);\n" +
		"  gl_FragColor = vec4(matColor.rgb * vLightWeighting, matColor.a);\n" + 
		"}\n"; 

	static String vertexShaderSrc =
		"attribute vec3 aVertexPosition;\n" + 
		"attribute vec3 aVertexNormal;\n" + 
		"\n" + 
		"uniform mat4 uMVMatrix; // [M]odel[V]iew matrix\n" + 
		"uniform mat4 uCMatrix;  // Client-side manipulated [C]amera matrix\n" + 
		"uniform mat4 uPMatrix;  // Perspective [P]rojection matrix\n" + 
		"uniform mat4 uNMatrix;  // [N]ormal transformation\n" + 
		"// uNMatrix is the transpose of the inverse of uCMatrix * uMVMatrix\n" + 
		"\n" + 
		"varying vec3 vLightWeighting;\n" + 
		"\n" + 
		"void main(void) {\n" +
		"  // Calculate the position of this vertex\n" + 
		"  gl_Position = uPMatrix * uCMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n" + 
		"\n" + 
		"  // Phong shading\n" + 
		"  vec3 transformedNormal = normalize((uNMatrix * vec4(normalize(aVertexNormal), 0)).xyz);\n" +
		"  vec3 lightingDirection = normalize(vec3(1, 1, 1));\n" + 
		"  float directionalLightWeighting = max(dot(transformedNormal, lightingDirection), 0.0);\n" +
		"  vec3 uAmbientLightColor = vec3(0.2, 0.2, 0.2);\n" + 
		"  vec3 uDirectionalColor = vec3(0.8, 0.8, 0.8);\n" + 
		"  vLightWeighting = uAmbientLightColor + uDirectionalColor * directionalLightWeighting;\n" + 
		"}\n";
}
