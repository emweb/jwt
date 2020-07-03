package eu.webtoolkit.jwt;

class IndexCheckBox extends WCheckBox {
	private WModelIndex index;

	public IndexCheckBox(WModelIndex index) {
		this.index = index;
	}

	public void setIndex(WModelIndex index2) {
		this.index = index2;
	}

	public WModelIndex getIndex() {
		return index;
	}

	public WString getToolTip(){
		return StringUtils.asString(
			  index.getData(ItemDataRole.ToolTip));
	}
}
