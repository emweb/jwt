/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.utils.EnumUtils;

class WSliderBackground extends WPaintedWidget {
	public WSliderBackground(WSlider slider) {
		super();
		this.slider_ = slider;
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		WPen pen = new WPen();
		pen.setCapStyle(PenCapStyle.FlatCap);
		int w;
		int h;
		if (this.slider_.getOrientation() == Orientation.Horizontal) {
			w = (int) this.getWidth().toPixels();
			h = (int) this.getHeight().toPixels();
		} else {
			w = (int) this.getHeight().toPixels();
			h = (int) this.getWidth().toPixels();
			painter.translate(0, w);
			painter.rotate(-90);
		}
		pen.setColor(new WColor(0x89, 0x89, 0x89));
		painter.setPen(pen);
		painter.drawLine(WSlider.HANDLE_WIDTH / 2, h / 2 - 2 + 0.5, w
				- WSlider.HANDLE_WIDTH / 2, h / 2 - 2 + 0.5);
		pen.setColor(new WColor(0xb7, 0xb7, 0xb7));
		painter.setPen(pen);
		painter.drawLine(WSlider.HANDLE_WIDTH / 2, h / 2 + 1 + 0.5, w
				- WSlider.HANDLE_WIDTH / 2, h / 2 + 1 + 0.5);
		pen.setColor(new WColor(0xd7, 0xd7, 0xd7));
		pen.setWidth(new WLength(2));
		painter.setPen(pen);
		painter.drawLine(WSlider.HANDLE_WIDTH / 2, h / 2, w
				- WSlider.HANDLE_WIDTH / 2, h / 2);
		if (!this.slider_.getTickPosition().isEmpty()) {
			int tickInterval = this.slider_.getTickInterval();
			int range = this.slider_.getMaximum() - this.slider_.getMinimum();
			if (tickInterval == 0) {
				tickInterval = range / 2;
			}
			double tickStep = ((double) w - WSlider.HANDLE_WIDTH)
					/ (range / tickInterval);
			pen.setWidth(new WLength(1));
			painter.setPen(pen);
			int y1 = h / 4;
			int y2 = h / 2 - 4;
			int y3 = h / 2 + 4;
			int y4 = h - h / 4;
			for (int i = 0;; ++i) {
				int x = WSlider.HANDLE_WIDTH / 2 + (int) (i * tickStep);
				if (x > w - WSlider.HANDLE_WIDTH / 2) {
					break;
				}
				if (!EnumUtils.mask(this.slider_.getTickPosition(),
						WSlider.TickPosition.TicksAbove).isEmpty()) {
					painter.drawLine(x + 0.5, y1, x + 0.5, y2);
				}
				if (!EnumUtils.mask(this.slider_.getTickPosition(),
						WSlider.TickPosition.TicksBelow).isEmpty()) {
					painter.drawLine(x + 0.5, y3, x + 0.5, y4);
				}
			}
		}
	}

	private WSlider slider_;
}
