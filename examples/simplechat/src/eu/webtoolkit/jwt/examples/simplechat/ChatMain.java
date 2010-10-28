package eu.webtoolkit.jwt.examples.simplechat;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class ChatMain extends WtServlet {
	public WApplication createApplication(WEnvironment env) {
		return new ChatApplication(env);
	}
}
