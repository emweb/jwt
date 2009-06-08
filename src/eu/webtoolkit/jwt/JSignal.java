/*
 * Copyright (C) 2006 Pieter Libin, Leuven, Belgium.
 *
 * Licensed under the terms of the GNU General Public License,
 * see the LICENSE file for more details.
 */

package eu.webtoolkit.jwt;

/**
 * A signal that may be triggered from the browser with a JavaScript call.
 */
public class JSignal extends AbstractJSignal {
	private Signal dynamic_;

	JSignal(WObject sender, String name, boolean collectSlotJavaScript) {
		super(sender, name);

		this.dynamic_ = null;
	}

	/**
	 * Creates a signal.
	 * 
	 * @param sender the object that will be identified as the sender for this signal.
	 * @param name the signal name (must be unique for each sender)
	 */
	public JSignal(WObject sender, String name) {
		this(sender, name, false);
	}

	@Override
	public Connection addListener(WObject receiver, Signal.Listener listener) {
		if (dynamic_ == null)
			dynamic_ = new Signal(getSender());

		Connection c = dynamic_.addListener(receiver, listener);

		super.listenerAdded();

		return c;
	}

	@Override
	protected int getListenerCount() {
		return super.getListenerCount() + (dynamic_ != null ? dynamic_.getListenerCount() : 0);
	}

	@Override
	public void removeListener(Signal.Listener listener) {
		dynamic_.removeListener(listener);

		super.listenerRemoved();
	}

	/**
	 * Triggers the signal.
	 */
	public void trigger() {
		if (dynamic_ != null)
			dynamic_.trigger();

		super.trigger();
	}

	/**
	 * Returns a JavaScript statement that triggers this signal.
	 * 
	 * You can use this to trigger the signal from within generated JavaScript code.
	 */
	public String createCall() {
		return createUserEventCall(null, null, null, null, null, null, null, null);
	}

	@Override
	protected void processDynamic(JavaScriptEvent jsEvent) {
		trigger();
	}
}