package eu.webtoolkit.jwt;

class WSignalMapper1<T> extends WObject {

	private Signal1<T> mapped_;

	WSignalMapper1(WObject parent) {
		super(parent);
		mapped_ = new Signal1<T>();
	}

	void mapConnect(AbstractSignal signal, final T value) {
		signal.addListener(this, new Signal.Listener() {
			public void trigger() {
				mapped_.trigger(value);
			}
		});
	}

	public Signal1<T> mapped() {
		return mapped_;
	}
}
