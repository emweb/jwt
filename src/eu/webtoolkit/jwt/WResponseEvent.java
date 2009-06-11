package eu.webtoolkit.jwt;


class WResponseEvent implements WAbstractEvent {
	public WResponseEvent() {
		super();
		this.jsEvent_ = new JavaScriptEvent();
	}

	public String getResponse() {
		return this.jsEvent_.response;
	}

	public WAbstractEvent createFromJSEvent(JavaScriptEvent jsEvent) {
		return new WResponseEvent(jsEvent);
	}

	private JavaScriptEvent jsEvent_;

	private WResponseEvent(JavaScriptEvent jsEvent) {
		super();
		this.jsEvent_ = jsEvent;
	}
}
