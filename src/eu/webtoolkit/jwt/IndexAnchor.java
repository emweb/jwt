package eu.webtoolkit.jwt;

public class IndexAnchor  extends WAnchor {
	private WModelIndex index;

	public IndexAnchor(WModelIndex index) {
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
