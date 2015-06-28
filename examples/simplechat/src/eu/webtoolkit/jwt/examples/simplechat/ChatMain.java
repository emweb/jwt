package eu.webtoolkit.jwt.examples.simplechat;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class ChatMain extends WtServlet {
	private static SimpleChatServer chatServer = null;
	
	public ChatMain() {
		chatServer = new SimpleChatServer(this);
	}

	public WApplication createApplication(WEnvironment env) {
		return new ChatApplication(env, chatServer);
	}
}
