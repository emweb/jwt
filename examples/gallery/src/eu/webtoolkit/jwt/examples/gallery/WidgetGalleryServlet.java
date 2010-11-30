package eu.webtoolkit.jwt.examples.gallery;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;
import eu.webtoolkit.jwt.WtServlet;

public class WidgetGalleryServlet extends WtServlet {
	@Override
	public WApplication createApplication(WEnvironment env) {
		WApplication app = new WApplication(env);
		  
		WHBoxLayout layout = new WHBoxLayout(app.getRoot());
		layout.setContentsMargins(0, 0, 0, 0);
		layout.addWidget(new WidgetGallery());
		  
		WXmlLocalizedStrings resourceBundle = new WXmlLocalizedStrings();
		resourceBundle.use("/eu/webtoolkit/jwt/examples/gallery/text.jwt");
		resourceBundle.use("/eu/webtoolkit/jwt/examples/gallery/text");
		resourceBundle.use("/eu/webtoolkit/jwt/examples/gallery/charts");
		app.setLocalizedStrings(resourceBundle);
		
		app.setTitle("Wt widgets demo");
		app.setCssTheme("polished");
		
		app.addMetaHeader("viewport", "width=700, height=1200");

		app.useStyleSheet("style/everywidget.css");
		app.useStyleSheet("style/dragdrop.css");
		app.useStyleSheet("style/combostyle.css");

		return app;
	}
}
