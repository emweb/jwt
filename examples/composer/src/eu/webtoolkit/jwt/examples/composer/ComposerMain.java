package eu.webtoolkit.jwt.examples.composer;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WStdLocalizedStrings;
import eu.webtoolkit.jwt.WtServlet;

public class ComposerMain extends WtServlet
{
	public ComposerMain()
	{
		super();
	}

	public WApplication createApplication(WEnvironment env)
	{
		WApplication app = new WApplication(env);
		WStdLocalizedStrings wsls = new WStdLocalizedStrings();
		wsls.use("eu.webtoolkit.jwt.examples.composer.composer");
		app.setLocalizedStrings(wsls);
		app.setTitle("Composer example");
		app.useStyleSheet("style/composer.css");

		app.getRoot().addWidget(new ComposeExample());

		return app;
	}
}
