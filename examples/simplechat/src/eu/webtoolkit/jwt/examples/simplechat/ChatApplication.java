package eu.webtoolkit.jwt.examples.simplechat;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLength.Unit;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;

public class ChatApplication extends WApplication {

	public ChatApplication(WEnvironment env, SimpleChatServer server) {
		super(env);
		this.server = server;
		
		setCssTheme("polished");
		
		setTitle("Wt Chat");
		
        WXmlLocalizedStrings resourceBundle = new WXmlLocalizedStrings();
        resourceBundle.use("/eu/webtoolkit/jwt/examples/simplechat/simplechat");
        setLocalizedStrings(resourceBundle);

		getRoot().addWidget(new WText(WString.tr("introduction")));

		chatWidget = new SimpleChatWidget(server, getRoot());
		chatWidget.setStyleClass("chat");

		getRoot().addWidget(new WText(WString.tr("details")));

		final WPushButton b = new WPushButton("I'm schizophrenic ...",
				getRoot());
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent arg) {
				b.hide();
				addChatWidget();
			}
		});
		
		useStyleSheet(new WLink("style/simplechat.css"));
	}

	@Override
	public void destroy() {
		chatWidget.destroy();
		super.destroy();
	}

	private void addChatWidget() {
		SimpleChatWidget chatWidget2 = new SimpleChatWidget(server,
				WApplication.getInstance().getRoot());
		chatWidget2.setStyleClass("chat");
	}

	private SimpleChatServer server;
	private SimpleChatWidget chatWidget;
}
