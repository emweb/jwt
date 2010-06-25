package eu.webtoolkit.jwt.examples.planner.captcha;

import eu.webtoolkit.jwt.WBrush;
import eu.webtoolkit.jwt.WBrushStyle;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPointF;
import eu.webtoolkit.jwt.WString;

public class Circle extends Shape {
	public Circle(WPointF center, ShapeColor color, double size) {
		super(center, color, size);
	}

	@Override
	public boolean contains(WPointF point) {
		return distanceTo(getCenter().getX(), getCenter().getY(), point.getX(), point.getY()) <= getSize();
	}
	
	private double distanceTo(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));   
	}
	
	@Override
	public WString getShapeName() {
		return WString.tr("captcha.circle");
	}

	@Override
	public void paint(WPainter painter) {
		//create a brush to define the circle's color and style
		WBrush b = new WBrush();
		b.setStyle(WBrushStyle.SolidPattern);
		b.setColor(getColor());
		
		//create a painterpath, and add an ellipse to it
		WPainterPath pp = new WPainterPath();
		pp.addEllipse(getCenter().getX() - getSize(), getCenter().getY() - getSize(), getSize() * 2, getSize() * 2);
		 
		//draw and fill the painterpath on the painter
		painter.fillPath(pp, b);
	}
}
