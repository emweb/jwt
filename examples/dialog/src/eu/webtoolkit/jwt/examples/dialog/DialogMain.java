package eu.webtoolkit.jwt.examples.dialog;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class DialogMain extends WtServlet {
	public DialogMain() {
		super();
	}

	public WApplication createApplication(WEnvironment env) {
		return new DialogApplication(env);
	}
}
