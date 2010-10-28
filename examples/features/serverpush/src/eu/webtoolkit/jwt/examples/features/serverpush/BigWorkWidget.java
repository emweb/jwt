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
		startButton_ = new WPushButton("Start", this);
		startButton_.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					@Override
					public void trigger(WMouseEvent me) {
						startButton_.disable();
						startBigWork();
					}
				});

		resultText_ = new WText(this);
		resultText_.setInline(false);
	}

	private WPushButton startButton_;
	private WText resultText_;

	private Thread workThread_;

	private void startBigWork() {
		final WApplication app = WApplication.getInstance();

		// Enable server push
		app.enableUpdates(true);

		workThread_ = new Thread(new Runnable() {
			@Override
			public void run() {
				doBigWork(app);
			}
		});
		workThread_.start();

		resultText_.setText("");
		startButton_.setText("Busy...");
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

			resultText_.setText(resultText_.getText() + ".");

			app.triggerUpdate();
			
			uiLock.release();
		}

		WApplication.UpdateLock uiLock = app.getUpdateLock();

		resultText_.setText("That was hefty!");
		startButton_.enable();
		startButton_.setText("Again!");

		app.triggerUpdate();

		// Disable server push
		app.enableUpdates(false);
		
		uiLock.release();
	}
}
