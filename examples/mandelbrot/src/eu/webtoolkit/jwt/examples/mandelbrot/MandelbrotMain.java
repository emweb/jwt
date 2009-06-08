package eu.webtoolkit.jwt.examples.mandelbrot;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WtServlet;

public class MandelbrotMain extends WtServlet {
	public MandelbrotMain() {
		super();
	}

	@Override
	public WApplication createApplication(WEnvironment env) {
		WApplication app = new WApplication(env);
		app.setTitle(new WString("Wt Mandelbrot example"));
		app.getRoot().addWidget(new MandelbrotExample());
		return app;
	}
}
