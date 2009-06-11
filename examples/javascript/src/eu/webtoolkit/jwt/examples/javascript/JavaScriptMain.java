package eu.webtoolkit.jwt.examples.javascript;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class JavaScriptMain extends WtServlet {
	public JavaScriptMain() {
		super();
	}

	public WApplication createApplication(WEnvironment env) {
		return new JavaScriptExample(env);
	}
}
