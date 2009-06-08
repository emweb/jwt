package eu.webtoolkit.jwt.examples.painting;

import eu.webtoolkit.jwt.PenStyle;
import eu.webtoolkit.jwt.WBrush;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WPaintDevice;
import eu.webtoolkit.jwt.WPaintedWidget;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPen;

public class ShapesWidget extends WPaintedWidget {
	public ShapesWidget(WContainerWidget parent) {
		super(parent);
		this.angle_ = 0;
		this.size_ = 1;
		this.resize(new WLength(710), new WLength(400));
	}

	public ShapesWidget() {
		this((WContainerWidget) null);
	}

	public void setAngle(double angle) {
		angle = Math.max(-30.0, Math.min(30.0, angle));
		if (this.angle_ != angle) {
			this.angle_ = angle;
			this.update();
		}
	}

	public double angle() {
		return this.angle_;
	}

	public void setRelativeSize(double size) {
		size = Math.max(0.1, Math.min(1.0, size));
		if (this.size_ != size) {
			this.size_ = size;
			this.update();
		}
	}

	public double relativeSize() {
		return this.size_;
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		painter.setRenderHint(WPainter.RenderHint.Antialiasing);
		painter.translate(this.getWidth().getValue() / 2, this.getHeight().getValue() / 2);
		painter.rotate(this.angle_);
		painter.scale(this.size_, this.size_);
		painter.translate(-this.getWidth().getValue() / 2 + 50, -this.getHeight().getValue() / 2 + 150);
		this.drawEmwebLogo(painter);
	}

	private double angle_;
	private double size_;

	private void drawEmwebLogo(WPainter painter) {
		painter.save();
		painter.setPen(new WPen(PenStyle.NoPen));
		painter.setBrush(new WBrush(WColor.black));
		this.drawEmwebE(painter);
		painter.save();
		painter.translate(397, 0);
		this.drawEmwebE(painter);
		painter.translate(210, 0);
		painter.scale(-1, 1);
		this.drawEmwebE(painter);
		painter.restore();
		painter.setBrush(new WBrush(emweb));
		this.drawEmwebMW(painter);
		painter.restore();
	}

	private void drawEmwebE(WPainter painter) {
		WPainterPath p = new WPainterPath();
		p.moveTo(46.835084, 58.783624);
		p.cubicTo(45.700172, 58.783624, 35.350098, 58.911502, 24.656354, 63.283309);
		p.cubicTo(8.7595992, 69.78907, 0, 82.38499, 0, 98.809238);
		p.cubicTo(0, 115.20152, 08.7595992, 127.82141, 24.656354, 134.31119);
		p.cubicTo(35.350098, 138.69099, 45.700172, 138.81088, 46.835084, 138.81088);
		p.lineTo(94.509362, 138.81088);
		p.lineTo(94.509362, 117.58323);
		p.lineTo(46.835084, 117.58323);
		p.cubicTo(46.811106, 117.58323, 39.466151, 117.47134, 32.608727, 114.53815);
		p.cubicTo(25.095932, 111.34122, 21.747144, 106.47389, 21.747144, 98.809238);
		p.cubicTo(21.747144, 91.120612, 25.095932, 86.269265, 32.608727, 83.064338);
		p.cubicTo(39.466151, 80.123159, 46.811106, 80.027251, 46.89103, 80.027251);
		p.lineTo(94.509362, 80.027251);
		p.lineTo(94.509362, 58.783624);
		p.lineTo(46.835084, 58.783624);
		painter.drawPath(p);
		painter.save();
		painter.translate(0, -58.783624);
		painter.drawPath(p);
		painter.restore();
	}

	private void drawEmwebMW(WPainter painter) {
		WPainterPath p = new WPainterPath();
		p.moveTo(120.59634, 24.072913);
		p.cubicTo(116.12064, 34.518895, 115.98477, 44.605222, 115.98477, 45.732141);
		p.lineTo(115.98477, 138.81088);
		p.lineTo(137.7399, 138.81088);
		p.lineTo(137.7399, 45.732141);
		p.cubicTo(137.7399, 45.708164, 137.83581, 38.53904, 140.84892, 31.841463);
		p.cubicTo(144.14176, 24.512492, 149.113, 21.235634, 156.98545, 21.235634);
		p.cubicTo(164.8499, 21.235634, 169.81314, 24.512492, 173.10599, 31.841463);
		p.cubicTo(176.10311, 38.53904, 176.215, 45.708164, 176.215, 45.780095);
		p.lineTo(176.215, 80.41343);
		p.lineTo(197.97014, 80.41343);
		p.lineTo(197.97014, 45.732141);
		p.cubicTo(197.97014, 44.605222, 197.83427, 34.518895, 193.35057, 24.072913);
		p.cubicTo(186.70894, 8.5517985, 173.77734, 0, 156.99344, 0);
		p.cubicTo(140.17756, 0, 127.25396, 8.5517985, 120.59634, 24.072913);
		painter.drawPath(p);
		final double dx = 176. - 115.98477;
		painter.save();
		painter.translate(dx, 0);
		painter.drawPath(p);
		painter.translate(dx, 0);
		painter.scale(-1, -1);
		painter.translate(0, -138.81088);
		painter.translate(-115.98477 - 197.95 - dx, 0);
		painter.drawPath(p);
		painter.translate(dx, 0);
		painter.drawPath(p);
		painter.restore();
	}

	static WColor emweb = new WColor(0x04, 0x7c, 0x93);
}
