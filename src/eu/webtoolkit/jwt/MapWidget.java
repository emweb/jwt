package eu.webtoolkit.jwt;


class MapWidget extends WContainerWidget {
	public MapWidget() {
		super();
	}

	protected void updateDom(DomElement element, boolean all) {
		if (all) {
			element.setAttribute("name", this.getFormName());
		}
		super.updateDom(element, all);
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_MAP;
	}
}
