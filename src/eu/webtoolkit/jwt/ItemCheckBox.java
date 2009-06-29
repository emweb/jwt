package eu.webtoolkit.jwt;


class ItemCheckBox extends WCheckBox {
	public ItemCheckBox(WModelIndex index) {
		super();
		this.index_ = index;
	}

	public void setIndex(WModelIndex index) {
		this.index_ = index;
	}

	public WModelIndex getIndex() {
		return this.index_;
	}

	private WModelIndex index_;
}
