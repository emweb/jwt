package eu.webtoolkit.jwt;

public class IndexContainerWidget extends WContainerWidget {
	private WModelIndex index;

	public IndexContainerWidget(WModelIndex index) {
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
