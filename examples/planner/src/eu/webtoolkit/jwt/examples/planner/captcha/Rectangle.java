package eu.webtoolkit.jwt.examples.planner.captcha;

import eu.webtoolkit.jwt.WBrush;
import eu.webtoolkit.jwt.WBrushStyle;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPointF;
import eu.webtoolkit.jwt.WRectF;
import eu.webtoolkit.jwt.WString;

public class Rectangle extends Shape {
	public Rectangle(WPointF center, ShapeColor color, double size) {
		super(center, color, size);
	}

	@Override
	public boolean contains(WPointF point) {
		return new WRectF(getCenter().getX(), getCenter().getY(), getSize(), getSize()).contains(point);
	}

	@Override
	public WString getShapeName() {
		return WString.tr("captcha.rectangle");
	}
	
	@Override
	public void paint(WPainter painter) {
		WBrush b = new WBrush();
		b.setStyle(WBrushStyle.SolidPattern);
		b.setColor(getColor());

		WPainterPath pp = new WPainterPath();
		pp.addRect(new WRectF(getCenter().getX(), getCenter().getY(), getSize(), getSize()));

		painter.fillPath(pp, b);
	}
}
