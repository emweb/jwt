package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBootstrapTheme;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;
import eu.webtoolkit.jwt.WtServlet;

public class WidgetGalleryServlet extends WtServlet {
	public WidgetGalleryServlet() {
		this.getConfiguration().setDebug(true);
	}
	
	@Override
	public WApplication createApplication(WEnvironment env) {
		WApplication app = new WApplication(env);
		  
		app.setTheme(new WBootstrapTheme());

		WXmlLocalizedStrings resourceBundle = new WXmlLocalizedStrings();
		resourceBundle.use("/eu/webtoolkit/jwt/examples/widgetgallery/text");
		resourceBundle.use("/eu/webtoolkit/jwt/examples/widgetgallery/src");
		app.setLocalizedStrings(resourceBundle);

		WHBoxLayout layout = new WHBoxLayout(app.getRoot());
		layout.setContentsMargins(0, 0, 0, 0);
		layout.addWidget(new WidgetGallery());
		
		app.setTitle("JWt Widget Gallery");

		app.useStyleSheet(new WLink("style/everywidget.css"));
		app.useStyleSheet(new WLink("style/dragdrop.css"));
		app.useStyleSheet(new WLink("style/combostyle.css"));
		app.useStyleSheet(new WLink("style/pygments.css"));

		return app;
	}
}
