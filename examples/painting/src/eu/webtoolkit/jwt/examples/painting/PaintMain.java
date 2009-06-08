package eu.webtoolkit.jwt.examples.painting;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WtServlet;
import eu.webtoolkit.jwt.utils.StringUtils;

public class PaintMain extends WtServlet {
	public PaintMain() {
		super();
	}

	@Override
	public WApplication createApplication(WEnvironment env) {
		WApplication app = new WApplication(env);
		app.setTitle(new WString("Paint example"));
		
		//TODO contextPath
		String contextPath = "context";
			//WebSession.Handler.instance().request().getContextPath();
		app.useStyleSheet(StringUtils.terminate(contextPath, '/') + "style/painting/painting.css");
		
		new PaintExample(app.getRoot());

		return app;
	}
}
