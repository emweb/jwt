/*
 * Copyright (C) 2006 Pieter Libin, Leuven, Belgium.
 *
 * Licensed under the terms of the GNU General Public License,
 * see the LICENSE file for more details.
 */

package eu.webtoolkit.jwt;

import java.util.ArrayList;

/**
 * A signal passing 6 arguments.
 * 
 * A signal implements the Observable pattern, allowing one or more listeners to listen for
 * events generated on the signal. The event may propagate 6 arguments to the listeners.
 */
public class Signal6<A1, A2, A3, A4, A5, A6> extends AbstractSignal {

	/**
	 * The listener interface.
	 * 
	 * This listener may be added to a {@link Signal6} or {@link JSignal6}, and
	 * its {@link #trigger(Object, Object, Object, Object, Object, Object)} method will be
	 * invoked whenever the signal is triggered.
	 */
	public static interface Listener<A1, A2, A3, A4, A5, A6> extends SignalImpl.Listener {
		/**
		 * Triggers the listener.
		 * 
		 * @param arg1 Argument 1
		 * @param arg2 Argument 2
		 * @param arg3 Argument 3
		 * @param arg4 Argument 4
		 * @param arg5 Argument 5
		 * @param arg6 Argument 6
		 */		
		public void trigger(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6);
	}

	/**
	 * Creates a new signal.
	 */
	public Signal6() {		
	}

	Signal6(WObject sender) {
		this();
	}

	/**
	 * Adds a listener for this signal.
	 * 
	 * Each listener will be triggered whenever the signal is triggered.
	 * 
	 * @param listenerOwner
	 *            the enclosing object for a listener implemented using an anonymous inner class
	 * @param listener
	 *            the listener
	 * @return a connection object that may be used to control the connection
	 * 
	 * @see AbstractSignal#addListener(WObject, eu.webtoolkit.jwt.Signal.Listener)
	 */
	public Connection addListener(WObject listenerOwner, Listener<A1, A2, A3, A4, A5, A6> listener) {
		return getImpl(true).addListener(listenerOwner, listener);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener a listener that was previously added
	 */
	public void removeListener(Listener<A1, A2, A3, A4, A5, A6> listener) {
		getImpl(false).removeListener(listener);
	}

	/**
	 * Triggers the signal.
	 * 
	 * The arguments are passed to the listeners.
	 * 
	 * @param arg1 Argument 1.
	 * @param arg2 Argument 2.
	 * @param arg3 Argument 3.
	 * @param arg4 Argument 4.
	 * @param arg5 Argument 5.
	 * @param arg6 Argument 6.
	 */
	@SuppressWarnings("unchecked")
	public void trigger(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6) {
		SignalImpl impl = getImpl(false);
		if (impl == null)
			return;

		ArrayList<SignalImpl.Listener> listeners = impl.getListeners();

		for (SignalImpl.Listener listener : listeners)
			((Listener) (listener)).trigger(arg1, arg2, arg3, arg4, arg5, arg6);
	}

	@Override
	public Connection addListener(WObject listenerOwner, final Signal.Listener listener) {
		Signal6.Listener<A1, A2, A3, A4, A5, A6> l = new Signal6.Listener<A1, A2, A3, A4, A5, A6>() {
			public void trigger(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6) {
				listener.trigger();
			}
		};

		return getImpl(true).addWrappedListener(listenerOwner, l, listener);
	}

	@Override
	public void removeListener(Signal.Listener listener) {
		getImpl(true).removeWrappedListener(listener);
	}
}