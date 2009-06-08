package eu.webtoolkit.jwt;

import java.util.ArrayList;
import eu.webtoolkit.jwt.WObject;

/**
 * Abstract base class for signals.
 * 
 * Signals are used to relay events to event listeners, implementing in this way
 * the Observer pattern.
 */
public abstract class AbstractSignal {
	private SignalImpl impl;

	/**
	 * A signal connection.
	 * 
	 * A connection is returned when an event listener is connected to a signal.
	 * It may be used to disconnect the listener at a later point from the
	 * signal.
	 */
	public static class Connection {
		private SignalImpl signalImpl;
		private SignalImpl.Listener listener;

		Connection(SignalImpl signalImpl, SignalImpl.Listener listener) {
			this.signalImpl = signalImpl;
			this.listener = listener;
		}

		Connection() {
			signalImpl = null;
			listener = null;
		}

		/**
		 * Disconnect.
		 * 
		 * If the connection was not connected, this method does nothing.
		 */
		public void disconnect() {
			if (signalImpl != null)
				signalImpl.removeListener(listener);
		}

		/**
		 * Returns whether the connection is connected.
		 * 
		 * @return whether the connection is connected.
		 */
		public boolean isConnected() {
			return signalImpl != null && signalImpl.hasListener(listener);
		}
	}

	private static ArrayList<SignalImpl.Listener> emptyList = new ArrayList<SignalImpl.Listener>();

	protected AbstractSignal() { }

	/**
	 * Returns whether at least one event listener has been connected to this
	 * signal.
	 * 
	 * @return whether at least one event listener has been connected to this
	 *         signal.
	 */
	public boolean isConnected() {
		return getListenerCount() != 0;
	}

	protected SignalImpl getImpl(boolean create) {
		if (impl == null && create)
			impl = new SignalImpl();

		return impl;
	}

	protected int getListenerCount() {
		if (impl == null)
			return 0;
		else
			return impl.getListenerCount();
	}

	protected ArrayList<SignalImpl.Listener> getListeners() {
		if (impl == null)
			return emptyList;
		else
			return impl.getListeners();
	}

	/**
	 * Returns whether this signal is blocked.
	 * 
	 * @return whether this signal is blocked.
	 * 
	 * @see #setBlocked(boolean)
	 */
	public boolean isBlocked() {
		if (impl == null)
			return false;
		else
			return impl.isBlocked();
	}

	/**
	 * Blocks or unblocks this signal.
	 * 
	 * While a signal is blocked, it will not trigger any of its connected event
	 * listeners.
	 * 
	 * @param blocked
	 */
	public void setBlocked(boolean blocked) {
		getImpl(true).setBlocked(blocked);
	}

	/**
	 * Adds a listener for this signal.
	 * 
	 * Each listener will be notified when the signal is triggered.
	 * 
	 * An owner object may be passed when the listener is implemented using an
	 * anonymous inner class. In that case the owner object should be the
	 * enclosing object of the listener object, and this is used to bind the
	 * lifetime of the listener. To avoid the owner object from not being
	 * garbage collected when it is no longer used, only the owner object will
	 * add a reference to the listener, while the signal will use a weak
	 * reference.
	 * 
	 * This avoids the most common reason for memory leaks in Java
	 * implementations of the Observer pattern: the owner object will not get
	 * garbage collected because of the (anonymous) listener object having a
	 * reference to it, even if the receiver object is no longer referenced from
	 * anywhere. When the owner object is not <code>null</code>, the listener is
	 * stored using a strong reference in the owner object, and using a weak
	 * reference in the signal.
	 * 
	 * @param listenerOwner
	 *            if not <code>null</code>, the enclosing object for an
	 *            anonymous listener
	 * @param listener
	 *            the listener
	 * @return a connection object that may be used to control the connection
	 */
	public abstract Connection addListener(WObject listenerOwner,
			Signal.Listener listener);
	
	/**
	 * Removes a listener.
	 * 
	 * @param listener a listener that was previously added.
	 */
	public abstract void removeListener(Signal.Listener listener);
}
