package eu.webtoolkit.jwt.examples.planner.captcha;

import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPointF;
import eu.webtoolkit.jwt.WString;

public abstract class Shape {
	private WPointF center;
	private ShapeColor color;
	private double size;

	public Shape(WPointF center, ShapeColor color, double size) {
		this.center = center;
		this.color = color;
		this.size = size;
	}

	public abstract boolean contains(WPointF point);
	
	public abstract WString getShapeName();
	
	public abstract void paint(WPainter painter);
	
	protected WPointF getCenter() {
		return center;
	}
	
	public ShapeColor getColor() {
		return color;
	}
	
	public double getSize() {
		return size;
	}
}
