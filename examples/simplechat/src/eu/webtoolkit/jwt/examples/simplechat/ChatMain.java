package eu.webtoolkit.jwt.examples.simplechat;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class ChatMain extends WtServlet {
	private static final long serialVersionUID = 1L;
	
	private static SimpleChatServer chatServer = null;
	
	public ChatMain() {
		chatServer = new SimpleChatServer(this);
        // Enable websockets only if the servlet container has support for JSR-356 (Jetty 9, Tomcat 7, ...)
        //getConfiguration().setWebSocketsEnabled(true);

        getConfiguration().setUseScriptNonce(true);
	}

  @Override
	public WApplication createApplication(WEnvironment env) {
		return new ChatApplication(env, chatServer);
	}
}
