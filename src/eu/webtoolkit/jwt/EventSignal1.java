package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.Signal.Listener;

/**
 * A signal which relays a browser event that passes event details.
 * 
 * The signal also passes on an event object that provides details of the event.
 */
public final class EventSignal1<E extends WAbstractEvent> extends AbstractEventSignal {
	private Signal1<E> dynamic_;
	private E instance_;

	EventSignal1(String name, WObject sender, E instance) {
		super(name, sender);

		dynamic_ = null;
		instance_ = instance;
	}

	/**
	 * Adds a listener for this signal.
	 * 
	 * Each listener will be triggered whenever the signal is triggered.
	 * 
	 * @param listenerOwner
	 * 			  the enclosing object for a listener implemented by an anonymous inner class.
	 * @param listener
	 *            the listener
	 * @return a connection object that may be used to control the connection
	 * 
	 * @see #addListener(WObject, Signal.Listener)
	 */
	public Connection addListener(WObject listenerOwner, Signal1.Listener<E> listener) {
		if (dynamic_ == null)
			dynamic_ = new Signal1<E>(getSender());

		Connection c = dynamic_.addListener(listenerOwner, listener);
		super.listenerAdded();

		return c;
	}

	@Override
	protected int getListenerCount() {
		return super.getListenerCount() + (dynamic_ != null ? dynamic_.getListenerCount() : 0);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener a listener that was previously added
	 */
	public void removeListener(Signal1.Listener<E> listener) {
		dynamic_.removeListener(listener);

		super.listenerRemoved();
	}

	/**
	 * Triggers the signal.
	 * 
	 * Event details will be passed on to listeners.
	 * 
	 * @param event The event details.
	 */
	public void trigger(E event) {
		if (dynamic_ != null)
			dynamic_.trigger(event);

		super.trigger();
	}

	@SuppressWarnings("unchecked")
	protected void processDynamic(JavaScriptEvent jsEvent) {
		E event = (E) instance_.createFromJSEvent(jsEvent);
		if (dynamic_ != null)
			dynamic_.trigger(event);

		super.processDynamic(jsEvent);
	}

	@Override
	public Connection addListener(WObject listenerOwner, final Signal.Listener listener) {
		if (dynamic_ == null)
			dynamic_ = new Signal1<E>(getSender());

		Connection c = dynamic_.addListener(listenerOwner, listener);
		super.listenerAdded();

		return c;
	}

	@Override
	public void removeListener(Signal.Listener listener) {
		if (dynamic_ != null)
			dynamic_.removeListener(listener);
	}
}
