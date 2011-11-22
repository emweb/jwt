package eu.webtoolkit.jwt.examples.features.serverpush;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class ServerPush extends WtServlet {
	@Override
	public WApplication createApplication(WEnvironment env) {
		WApplication app = new WApplication(env);
		new BigWorkWidget(app.getRoot());
		
		return app;
	}
}
