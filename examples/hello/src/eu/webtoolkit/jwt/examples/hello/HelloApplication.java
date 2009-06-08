package eu.webtoolkit.jwt.examples.hello;

import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;

/*
 * A simple hello world application class which demonstrates how to react
 * to events, read input, and give feed-back.
 */
public class HelloApplication extends WApplication {
	private WLineEdit nameEdit_;
	private WText greeting_;

	public HelloApplication(WEnvironment env) {
		super(env);
		setTitle("Hello world"); // application title

		getRoot().addWidget(new WText("Your name, please ? ")); // show some text
		nameEdit_ = new WLineEdit(getRoot()); // allow text input
		nameEdit_.setFocus(); // give focus

		WPushButton b = new WPushButton("Greet me.", getRoot()); // create a button
		b.setMargin(5, Side.Left); // add 5 pixels margin

		getRoot().addWidget(new WBreak()); // insert a line break

		greeting_ = new WText(getRoot()); // empty text

		/*
		 * Add the necessary listeners.
		 */
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {

			public void trigger(WMouseEvent a1) {
				greet();
			}

		});
		nameEdit_.enterPressed().addListener(this, new Signal.Listener() {

			public void trigger() {
				greet();
			}
		});

	}

	private void greet() {
		/*
		 * Update the text, using text input into the nameEdit_ field.
		 */
		greeting_.setText("Hello there, " + nameEdit_.getText());
	}
}
