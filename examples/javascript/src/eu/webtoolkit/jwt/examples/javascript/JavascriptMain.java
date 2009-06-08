package eu.webtoolkit.jwt.examples.javascript;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class JavascriptMain extends WtServlet {
	public JavascriptMain() {
		super();
	}

	public WApplication createApplication(WEnvironment env) {
		return new JavascriptExample(env);
	}
}
