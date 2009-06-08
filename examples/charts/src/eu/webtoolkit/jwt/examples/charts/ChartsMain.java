package eu.webtoolkit.jwt.examples.charts;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;
import eu.webtoolkit.jwt.WtServlet;


public class ChartsMain extends WtServlet {
	public ChartsMain() {
		super();
		
		getConfiguration().setSendXHTMLMimeType(false);
	}

	public WApplication createApplication(WEnvironment env) {
		WApplication app = new WApplication(env);
		app.setTitle("Charts example");

		WXmlLocalizedStrings resourceBundle = new WXmlLocalizedStrings();
		resourceBundle.use("/eu/webtoolkit/jwt/examples/charts/charts");
		app.setLocalizedStrings(resourceBundle);

		app.getRoot().setPadding(new WLength(10));

		new ChartsExample(app.getRoot());

		app.useStyleSheet("style/charts/charts.css");

		return app;
	}
}
