package eu.webtoolkit.jwt;


enum VectorFormat {
	SvgFormat, VmlFormat;

	public int getValue() {
		return ordinal();
	}
}
