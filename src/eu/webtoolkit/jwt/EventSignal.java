package eu.webtoolkit.jwt;

/**
 * An signal which relays a browser event.
 * 
 * This event signal does not pass any event details.
 */
public class EventSignal extends AbstractEventSignal {
	private Signal dynamic_;

	EventSignal(String name, WObject sender) {
		super(name, sender);

		dynamic_ = null;
	}

	@Override
	public Connection addListener(WObject listenerOwner, Signal.Listener listener) {
		if (dynamic_ == null)
			dynamic_ = new Signal(getSender());

		Connection result = dynamic_.addListener(listenerOwner, listener);

		super.listenerAdded();

		return result;
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

	@Override
	public void trigger() {
		if (dynamic_ != null)
			dynamic_.trigger();

		super.trigger();
	}

	protected void processDynamic(JavaScriptEvent jsEvent) {
		if (dynamic_ != null)
			dynamic_.trigger();

		super.processDynamic(jsEvent);
	}

	@Override
	public void addListener(JSlot slot) {
		super.addListener(slot);
	}

	@Override
	public void addListener(WObject receiver, LearningListener listener) {
		super.addListener(receiver, listener);
	}
}
