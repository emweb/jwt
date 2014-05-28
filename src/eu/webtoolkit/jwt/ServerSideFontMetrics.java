/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

class ServerSideFontMetrics {
	public ServerSideFontMetrics() {
		this.img_ = new WRasterPaintDevice("png", new WLength(100), new WLength(100));
		this.painter_ = new WPainter(this.img_);
	}

	public WFontMetrics fontMetrics(final WFont font) {
		this.painter_.setFont(font);
		return this.painter_.getDevice().getFontMetrics();
	}

	public WTextItem measureText(final WFont font, final CharSequence text,
			double maxWidth, boolean wordWrap) {
		this.painter_.setFont(font);
		return this.painter_.getDevice().measureText(text, maxWidth, wordWrap);
	}

	public static boolean isAvailable() {
		return true;
	}

	private WRasterPaintDevice img_;
	private WPainter painter_;
}
