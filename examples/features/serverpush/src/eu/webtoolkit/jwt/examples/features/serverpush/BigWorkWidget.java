package eu.webtoolkit.jwt.examples.features.serverpush;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;

/*
 * This is a minimal server push example, which is used to update the GUI
 * while a big work is computing in another thread.
 */
public class BigWorkWidget extends WContainerWidget {
	public BigWorkWidget(WContainerWidget parent) {
		super(parent);
		startButton = new WPushButton("Start", this);
		startButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					@Override
					public void trigger(WMouseEvent me) {
						startButton.disable();
						startBigWork();
					}
				});

		resultText = new WText(this);
		resultText.setInline(false);
	}

	private WPushButton startButton;
	private WText resultText;

	private void startBigWork() {
		final WApplication app = WApplication.getInstance();

		// Enable server push
		app.enableUpdates(true);

		(new Thread(new Runnable() {
			@Override
			public void run() {
				doBigWork(app);
			}
		})).start();

		resultText.setText("");
		startButton.setText("Busy...");
	}

	/*
	 * This function runs from another thread.
	 * 
	 * From within this thread, we cannot use WApplication::instance(), since
	 * that use thread-local storage. We can only access
	 * WApplication::instance() after we have grabbed its update-lock.
	 */
	private void doBigWork(WApplication app) {
		for (int i = 0; i < 20; ++i) {
			// Do 50 ms of hard work.
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Get the application update lock to update the user-interface
			// with a progress indication.
			WApplication.UpdateLock uiLock = app.getUpdateLock();

			try {
				resultText.setText(resultText.getText() + ".");
				app.triggerUpdate();
			} finally {
				uiLock.release();
			}
		}

		WApplication.UpdateLock uiLock = app.getUpdateLock();

		try {
			resultText.setText("That was hefty!");
			startButton.enable();
			startButton.setText("Again!");

			app.triggerUpdate();

			app.enableUpdates(false);
		} finally {
			uiLock.release();
		}
	}
}
