package eu.webtoolkit.jwt.examples.simplechat;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLength.Unit;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;

public class ChatApplication extends WApplication {

	public ChatApplication(WEnvironment env) {
		super(env);
		
		setCssTheme("polished");
		
		setTitle("Wt Chat");
		
        WXmlLocalizedStrings resourceBundle = new WXmlLocalizedStrings();
        resourceBundle.use("/eu/webtoolkit/jwt/examples/simplechat/simplechat");
        setLocalizedStrings(resourceBundle);

		getRoot().addWidget(new WText(WString.tr("introduction")));

		SimpleChatWidget chatWidget = new SimpleChatWidget(theServer, getRoot());
		chatWidget.setStyleClass("chat");

		final WPushButton b = new WPushButton("I'm schizophrenic ...",
				getRoot());
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent arg) {
				b.hide();
				addChatWidget();
			}
		});

		WPushButton about = new WPushButton("About...", getRoot());
		about.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent arg) {
				  final WDialog dialog = new WDialog("About");
				  
				  dialog.resize(new WLength(70, Unit.Percentage), WLength.Auto);

				  new WText(tr("about"), dialog.getContents());
				  new WBreak(dialog.getContents());
				  WPushButton ok = new WPushButton("Ok", dialog.getContents());
				  ok.clicked().addListener(dialog, new Signal1.Listener<WMouseEvent>(){
					public void trigger(WMouseEvent arg) {
						dialog.accept();
					}
				  });

				  dialog.exec();
			}
		});
		
		useStyleSheet("style/simplechat.css");
	}

	private void addChatWidget() {
		SimpleChatWidget chatWidget2 = new SimpleChatWidget(theServer,
				WApplication.getInstance().getRoot());
		chatWidget2.setStyleClass("chat");
	}

	private static SimpleChatServer theServer = new SimpleChatServer();
}
