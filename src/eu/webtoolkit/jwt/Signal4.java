/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;

/**
 * A signal that propagates events to listeners, and is capable of passing 4 argument.
 * <p>
 * A signal implements the Observer pattern, allowing one or more listeners to listen for
 * events generated on the signal. The event may propagate 4 arguments to the listeners.
 * <p>
 * For a usage example take a look the documentation of {@link Signal2}.
 */
public class Signal4<A1, A2, A3, A4> extends AbstractSignal {

	/**
	 * The listener interface.
	 * <p>
	 * This listener may be added to a {@link Signal4} or {@link JSignal4}, and
	 * its {@link #trigger(Object, Object, Object, Object)} method will be invoked whenever
	 * the signal is triggered.
	 */
	public static interface Listener<A1, A2, A3, A4> extends SignalImpl.Listener {
		/**
		 * Triggers the listener.
		 * 
		 * @param arg1 Argument 1
		 * @param arg2 Argument 2
		 * @param arg3 Argument 3
		 * @param arg4 Argument 4
		 */
		public void trigger(A1 arg1, A2 arg2, A3 arg3, A4 arg4);
	}

	/**
	 * Creates a new signal.
	 */	
	public Signal4() {
	}

	Signal4(WObject sender) {
		this();
	}

	/**
	 * Adds a listener for this signal.
	 * <p>
	 * Each listener will be triggered whenever the signal is triggered.
	 * 
	 * @param listenerOwner
	 *            the enclosing object for a listener implemented using an (anonymous) inner class
	 * @param listener
	 *            the listener
	 * @return a connection object that may be used to control the connection
	 * 
	 * @see AbstractSignal#addListener(WObject, eu.webtoolkit.jwt.Signal.Listener)
	 */
	public Connection addListener(WObject listenerOwner, Listener<A1, A2, A3, A4> listener) {
		return getImpl(true).addListener(listenerOwner, listener);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener a listener that was previously added
	 */
	public void removeListener(Listener<A1, A2, A3, A4> listener) {
		getImpl(false).removeListener(listener);
	}

	/**
	 * Triggers the signal.
	 * <p>
	 * The arguments are passed to the listeners.
	 * 
	 * @param arg1 Argument 1.
	 * @param arg2 Argument 2.
	 * @param arg3 Argument 3.
	 * @param arg4 Argument 4.
	 */
	@SuppressWarnings("unchecked")
	public void trigger(A1 arg1, A2 arg2, A3 arg3, A4 arg4) {
		SignalImpl impl = getImpl(false);
		if (impl == null)
			return;

		ArrayList<SignalImpl.Listener> listeners = impl.getListeners();

		for (SignalImpl.Listener listener : listeners)
			((Listener) (listener)).trigger(arg1, arg2, arg3, arg4);
	}

	@Override
	public Connection addListener(WObject listenerOwner, final Signal.Listener listener) {
		Signal4.Listener<A1, A2, A3, A4> l = new Signal4.Listener<A1, A2, A3, A4>() {
			public void trigger(A1 a1, A2 a2, A3 a3, A4 a4) {
				listener.trigger();
			}
		};

		return getImpl(true).addWrappedListener(listenerOwner, l, listener);
	}

	@Override
	public void removeListener(Signal.Listener listener) {
		getImpl(true).removeWrappedListener(listener);
	}
	
	@Override
	protected int getArgumentCount() {
		return 4;
	}
}