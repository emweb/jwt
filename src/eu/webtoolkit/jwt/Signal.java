/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;

/**
 * A signal that propagates events to listeners.
 * <p>
 * A signal implements the Observer pattern, allowing one or more listeners to listen for
 * events generated on the signal.
 * <p>
 * For a usage example take a look the documentation of {@link Signal2}.
 */
public class Signal extends AbstractSignal {
	/**
	 * The listener interface.
	 * <p>
	 * This listener may be added to any signal, and its {@link #trigger()} method will
	 * be invoked whenever the signal is triggered.
	 * 
	 * @see AbstractSignal#addListener(WObject, Signal.Listener)
	 */
	public static interface Listener extends SignalImpl.Listener {
		/**
		 * Triggers the listener.
		 */
		public void trigger();
	}

	/**
	 * Creates a new signal.
	 */
	public Signal() {
	}

	public Signal(WObject sender) {
		this();
	}

	@Override
	public Connection addListener(WObject listenerOwner, Listener listener) {
		return getImpl(true).addListener(listenerOwner, listener);
	}

	@Override
	public void removeListener(Listener listener) {
		getImpl(true).removeListener(listener);
	}

	/**
	 * Triggers the signal.
	 * <p>
	 * The {@link Listener#trigger()} method of all listeners added to this signal is invoked,
	 * unless the signal is blocked (see {@link #setBlocked(boolean)}.
	 */
	public void trigger() {
		SignalImpl impl = getImpl(false);
		if (impl == null)
			return;

		ArrayList<SignalImpl.Listener> listeners = impl.getListeners();

		for (SignalImpl.Listener listener : listeners)
			((Listener) (listener)).trigger();
	}
}