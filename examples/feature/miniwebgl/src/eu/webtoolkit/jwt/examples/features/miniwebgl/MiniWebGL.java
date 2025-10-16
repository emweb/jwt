package eu.webtoolkit.jwt.examples.features.miniwebgl;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WtServlet;

public class MiniWebGL extends WtServlet {
	private static final long serialVersionUID = 1L;

  public MiniWebGL() {
    super();

    getConfiguration().setUseScriptNonce(true);
  }

	@Override
	public WApplication createApplication(WEnvironment env) {
		WApplication app = new WApplication(env);

		app.setTitle("Minimalistic WebGL Demo");

		new WText(
				"This is a minimalistic demonstration "
						+ "application for WebGL. If your browser supports WebGL, you will "
						+ "see a black square with a triangle inside.",
				app.getRoot());

		new WBreak(app.getRoot());

		PaintWidget gl = new PaintWidget(app.getRoot());
		gl.resize(640, 640);

		return app;
	}
}
