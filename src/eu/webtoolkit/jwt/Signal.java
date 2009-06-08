/*
 * Copyright (C) 2006 Pieter Libin, Leuven, Belgium.
 *
 * Licensed under the terms of the GNU General Public License,
 * see the LICENSE file for more details.
 */

package eu.webtoolkit.jwt;

import java.util.ArrayList;

/**
 * A signal.
 * 
 * A signal implements the Observable pattern, allowing one or more listeners to listen for
 * events generated on the signal.
 */
public class Signal extends AbstractSignal {
	/**
	 * The listener interface.
	 * 
	 * This listener may be added to any signal, and its {@link #trigger()} method will
	 * be invoked whenever the signal is triggered.
	 * 
	 * @see AbstractSignal#addListener(WObject, Listener)
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

	Signal(WObject sender) {
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
	 * 
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