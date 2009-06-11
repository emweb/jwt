package eu.webtoolkit.jwt;


class Spacer extends WWebWidget {
	public Spacer() {
		super();
		this.setInline(false);
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_DIV;
	}
}
