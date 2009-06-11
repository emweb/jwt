package eu.webtoolkit.jwt;


class WTimerWidget extends WInteractWidget {
	public WTimerWidget(WTimer timer) {
		super();
		this.timer_ = timer;
		this.timerStarted_ = false;
	}

	public void remove() {
		this.timer_.timerWidget_ = null;
		super.remove();
	}

	public void timerStart(boolean jsRepeat) {
		this.timerStarted_ = true;
		this.jsRepeat_ = jsRepeat;
		this.repaint();
	}

	public boolean isTimerExpired() {
		return this.timer_.getRemainingInterval() == 0;
	}

	private WTimer timer_;
	private boolean timerStarted_;
	private boolean jsRepeat_;

	protected void updateDom(DomElement element, boolean all) {
		if (this.timerStarted_
				|| (!WApplication.getInstance().getEnvironment()
						.hasJavaScript() || all) && this.timer_.isActive()) {
			element.setTimeout(this.timer_.getRemainingInterval(),
					this.jsRepeat_);
			this.timerStarted_ = false;
		}
		super.updateDom(element, all);
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_SPAN;
	}

	protected DomElement renderRemove() {
		DomElement e = DomElement.getForUpdate(this,
				DomElementType.DomElement_DIV);
		e.removeFromParent();
		e
				.callJavaScript(
						"{var obj="
								+ this.getJsRef()
								+ ";if (obj.timer) {clearTimeout(obj.timer);obj.timer = null;}}",
						true);
		return e;
	}
}
