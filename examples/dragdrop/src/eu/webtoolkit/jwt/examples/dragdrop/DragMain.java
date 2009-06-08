package eu.webtoolkit.jwt.examples.dragdrop;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WtServlet;

public class DragMain extends WtServlet {
	public DragMain() {
		super();
	}

	@Override
	public WApplication createApplication(WEnvironment env) {
		WApplication app = new WApplication(env);
		app.setTitle("Drag & drop");

		new WText("<h1>Wt Drag &amp; drop example.</h1>", app
				.getRoot());

		new DragExample(app.getRoot());

		app.useStyleSheet("style/dragdrop/dragdrop.css");

		return app;
	}
}
