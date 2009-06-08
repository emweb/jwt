package eu.webtoolkit.jwt.examples.javascript;

import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;

/**
 * Javascript - Wt interaction example
 */

/**
 * An example showing how to interact custom JavaScript with Wt stuff
 */
public class JavascriptExample extends WApplication {
	/**
	 * Create the example application.
	 */
	public JavascriptExample(WEnvironment env) {
		super(env);
		setTitle("Javascript example");

		// Create a popup for prompting the amount of money, and connect the
		// okPressed button to the slot for setting the amount of money.
		//
		// Note that the input provided by the user in the prompt box is passed
		// as
		// an argument to the slot.
		promptAmount_ = Popup.createPrompt("How much do you want to pay?",
				"", this);
		promptAmount_.okPressed().addListener(this,
				new Signal1.Listener<String>() {
					public void trigger(String a1) {
						setAmount(a1);
					}
				});

		// Create a popup for confirming the payment.
		//
		// Since a confirm popup does not allow input, we ignore the
		// argument carrying the input (which will be empty anyway).
		confirmPay_ = Popup.createConfirm("", this);
		confirmPay_.okPressed().addListener(this,
				new Signal1.Listener<String>() {
					public void trigger(String a1) {
						confirmed();
					}
				});

		new WText(
				"<h2>Wt Javascript example</h2>"
						+ "<p>Wt makes abstraction of Javascript, and therefore allows you"
						+ " to develop web applications without any knowledge of Javascript,"
						+ " and which are not dependent on Javascript."
						+ " However, Wt does allow you to add custom Javascript code:</p>"
						+ " <ul>"
						+ "   <li>To call custom JavaScript code from an event handler, "
						+ "connect the Wt::EventSignal to a Wt::JSlot.</li>"
						+ "   <li>To call C++ code from custom JavaScript, use "
						+ "Wt.emit() to emit a Wt::JSignal.</li>"
						+ "   <li>To call custom JavaScript code from C++, use "
						+ "WApplication::doJavascript() or Wt::JSlot::exec().</li>"
						+ " </ul>"
						+ "<p>This simple application shows how to interact between C++ and"
						+ " JavaScript using the JSlot and JSignal classes.</p>",
				getRoot());

		currentAmount_ = new WText("Current amount: $"
				+ promptAmount_.defaultValue(), getRoot());

		WPushButton amountButton = new WPushButton("Change ...", getRoot());
		amountButton.setMargin(10, Side.Left, Side.Right);

		new WBreak(getRoot());

		WPushButton confirmButton = new WPushButton("Pay now.", getRoot());
		confirmButton.setMargin(10, Side.Top, Side.Bottom);

		// Connect the event handlers to a JSlot: this will execute the
		// JavaScript
		// immediately, without a server round trip.
		amountButton.clicked().addListener(promptAmount_.show);
		confirmButton.clicked().addListener(confirmPay_.show);

		// Set the initial amount
		setAmount("1000");
	}

	/**
	 * The user has confirmed the payment.
	 */
	private void confirmed() {
		new WText("<br/>Just payed $" + promptAmount_.defaultValue() + ".",
				getRoot());
	}

	/**
	 * Set the amount to be payed.
	 */
	private void setAmount(String amount) {
		// Change the confirmation message to include the amount.
		confirmPay_.setMessage("Are you sure you want to pay $" + amount
				+ " ?");

		// Change the default value for the prompt.
		promptAmount_.setDefaultValue(amount);

		// Change the text that shows the current amount.
		currentAmount_.setText("Current amount: $"
				+ promptAmount_.defaultValue());
	}

	/**
	 * Popup for changing the amount.
	 */
	private Popup promptAmount_;

	/**
	 * Popup for paying.
	 */
	private Popup confirmPay_;

	/**
	 * WText for showing the current amount.
	 */
	private WText currentAmount_;
}
