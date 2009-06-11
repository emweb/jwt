package eu.webtoolkit.jwt.examples.mission;

import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WtServlet;

public class MissionImpossibleMain extends WtServlet {

	public MissionImpossibleMain() {
		super();
	}

	@Override
	public WApplication createApplication(WEnvironment env) {
		final WApplication appl = new WApplication(env);

		new WText("<h1>Your mission</h1>", appl.getRoot());
		final WText secret = new WText("Your mission, Jim, should you accept, is to create solid web applications.", appl.getRoot());

		new WBreak(appl.getRoot());
		new WBreak(appl.getRoot());

		new WText("This program will quit in ", appl.getRoot());
		final CountDownWidget countdown = new CountDownWidget(10, 0, 1000, appl.getRoot());
		new WText(" seconds.", appl.getRoot());

		new WBreak(appl.getRoot());
		new WBreak(appl.getRoot());

		final WPushButton cancelButton = new WPushButton("Cancel!", appl.getRoot());
		WPushButton quitButton = new WPushButton("Quit", appl.getRoot());
		quitButton.clicked().addListener(appl, new Signal1.Listener<WMouseEvent>() {
			
			public void trigger(WMouseEvent a1) {
				appl.quit();
			}
		});

		countdown.done().addListener(appl, new Signal.Listener() {
			public void trigger() {
				appl.quit();
			}
		});

		cancelButton.clicked().addListener(appl, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent a1) {
				countdown.cancel();
				cancelButton.disable();
				secret.hide();
			}
		});

		return appl;
	}
}
