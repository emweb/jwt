/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * A utility class which provides timer signals and single-shot timers
 * <p>
 * 
 * To use a timer, create a WTimer instance, set the timer interval using
 * {@link WTimer#setInterval(int msec) setInterval()} and connect a slot to the
 * timeout signal. Then, start the timer using {@link WTimer#start() start()}.
 * An active timer may be cancelled at any time using {@link WTimer#stop()
 * stop()}.
 * <p>
 * By default, a timer will continue to generate events until you
 * {@link WTimer#stop() stop()} it. To create a timer that will fire only once,
 * use {@link WTimer#setSingleShot(boolean singleShot) setSingleShot()}.
 * <p>
 * When connecting stateless slot implementations to the timeout signal, these
 * stateless slot implementations will be used as for any other signal (when
 * Ajax is available).
 * <p>
 * In clients without (enabled) JavaScript support, the minimum resolution of
 * the timer is one second (1000 milli-seconds), and it is probably wise to use
 * timers sparingly.
 */
public class WTimer extends WObject {
	/**
	 * Construct a new timer with the given parent.
	 */
	public WTimer(WObject parent) {
		super(parent);
		this.timerWidget_ = new WTimerWidget(this);
		this.singleShot_ = false;
		this.selfDestruct_ = false;
		this.interval_ = 0;
		this.active_ = false;
		this.timeoutConnected_ = false;
		this.timeout_ = new Time();
	}

	/**
	 * Construct a new timer with the given parent.
	 * <p>
	 * Calls {@link #WTimer(WObject parent) this((WObject)null)}
	 */
	public WTimer() {
		this((WObject) null);
	}

	/**
	 * Get the interval (msec).
	 */
	public int getInterval() {
		return this.interval_;
	}

	/**
	 * Set the interval (msec).
	 */
	public void setInterval(int msec) {
		this.interval_ = msec;
	}

	/**
	 * Return if the timer is running.
	 */
	public boolean isActive() {
		return this.active_;
	}

	/**
	 * Is this timer set to fire only once.
	 */
	public boolean isSingleShot() {
		return this.singleShot_;
	}

	/**
	 * Configure this timer to fire only once.
	 * <p>
	 * A Timer is by default not single shot, and will fire continuously, until
	 * it is stopped.
	 */
	public void setSingleShot(boolean singleShot) {
		this.singleShot_ = singleShot;
	}

	/**
	 * This static function calls a slot after a given time interval.
	 * <p>
	 * For example, the following code will call this.doSome() after 2 seconds:
	 * <blockquote>
	 * 
	 * <pre>
	 * WTimer::singleShot(2000, SLOT(this, MyClass::doSome));
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * Start the timer. The timer will be {@link WTimer#isActive() isActive()},
	 * until either the interval has elapsed, after which the timeout signal is
	 * activated, or until {@link WTimer#stop() stop()} is called.
	 */
	public void start() {
		if (!this.active_) {
			WApplication app = WApplication.getInstance();
			if (app != null && app.getDomRoot() != null) {
				app.getTimerRoot().addWidget(this.timerWidget_);
			}
			this.active_ = true;
			this.timeout_ = new Time().add(this.interval_);
			boolean jsRepeat = !this.timeout().isExposedSignal()
					&& !this.singleShot_;
			this.timerWidget_.timerStart(jsRepeat);
			if (this.timeout().isExposedSignal() && !this.timeoutConnected_) {
				this.timeout().addListener(this,
						new Signal1.Listener<WMouseEvent>() {
							public void trigger(WMouseEvent e1) {
								WTimer.this.gotTimeout();
							}
						});
				this.timeoutConnected_ = true;
			}
		}
	}

	/**
	 * Stop the timer.
	 * <p>
	 * You may stop the timer during its {@link WTimer#timeout() timeout()}, or
	 * cancel a running timer at any other time.
	 * <p>
	 * 
	 * @see WTimer#start()
	 */
	public void stop() {
		if (this.active_) {
			WApplication app = WApplication.getInstance();
			if (app != null && app.getDomRoot() != null) {
				app.getTimerRoot().removeWidget(this.timerWidget_);
			}
			this.active_ = false;
		}
	}

	/**
	 * Signal emitted when the timer timeouts.
	 * <p>
	 * The WMouseEvent does not provide any meaningful information but is an
	 * implementation artefact.
	 */
	public EventSignal1<WMouseEvent> timeout() {
		return this.timerWidget_.clicked();
	}

	WTimerWidget timerWidget_;
	private boolean singleShot_;
	private boolean selfDestruct_;
	private int interval_;
	private boolean active_;
	private boolean timeoutConnected_;
	private Time timeout_;

	private void gotTimeout() {
		if (this.active_) {
			if (!this.singleShot_) {
				this.timeout_ = new Time().add(this.interval_);
				this.timerWidget_.timerStart(false);
			} else {
				this.stop();
			}
		}
		if (this.selfDestruct_) {
			;
		}
	}

	private void setSelfDestruct() {
		this.selfDestruct_ = true;
	}

	int getRemainingInterval() {
		int remaining = this.timeout_.subtract(new Time());
		return Math.max(0, remaining);
	}
}
