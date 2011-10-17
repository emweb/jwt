package eu.webtoolkit.jwt.examples.planner.captcha;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.webtoolkit.jwt.WBrush;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPaintDevice;
import eu.webtoolkit.jwt.WPaintedWidget;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPen;
import eu.webtoolkit.jwt.WPointF;
import eu.webtoolkit.jwt.WString;

public class ShapesWidget extends WPaintedWidget {
	/**
	 * Enum to generate shapes randomly.
	 * 
	 * @author pieter
	 */
	enum Shapes {
		Rectangle {
			public Shape createShape(WPointF center, ShapeColor color, double size) {
				return new Rectangle(center, color, size);
			}
		},
		Circle {
			public Shape createShape(WPointF center, ShapeColor color, double size) {
				return new Circle(center, color, size);
			}
		};
		
		public abstract Shape createShape(WPointF center, ShapeColor color, double size);
		
		public static Shape getRandomShape(WPointF center, ShapeColor color, double size) {
			Random r = new Random();
			int n = r.nextInt(Shapes.values().length);
			return Shapes.values()[n].createShape(center, color, size);
		}
	}
	
	//List of shapes to render
	private List<Shape> shapes = new ArrayList<Shape>();
	//Shape that should be clicked on by the user
	private Shape toSelect;
	
	public  ShapesWidget(WContainerWidget parent) {
		super(parent);
	}

	public void initShapes() {
		shapes.clear();

		final int numberOfShapes = 5;

		int i = 1;
		
		Random r = new Random();
		
		toSelect = createRandomShape(r);
		while (i < numberOfShapes) {
			Shape s = createRandomShape(r);
			if (!sameShapeAndColor(s, toSelect)) {
				shapes.add(s);
				i++;
			}
		}
		
		shapes.add(r.nextInt(shapes.size()), toSelect);
	}
	
	@Override
	protected void paintEvent(WPaintDevice paintDevice) {
		//create a WPainter
		WPainter painter = new WPainter(paintDevice);

		//draw each shape
		for (Shape s : shapes) {
			s.paint(painter);
		}
	}
	
	private boolean sameShapeAndColor(Shape s1, Shape s2) {
		return s1.getClass().equals(s2.getClass()) && s1.getColor().equals(s2.getColor());
	}
	
	private Shape createRandomShape(Random r) {
		double size = 6 + r.nextDouble() * 6;

		ShapeColor c = null;
		final int amountOfColors = 4;
		switch (r.nextInt(amountOfColors)) {
			case 0:
				c = new ShapeColor(WColor.red, tr("captcha.red")); 
				break;
			case 1:
				c = new ShapeColor(WColor.green, tr("captcha.green")); 
				break;
			case 2:
				c = new ShapeColor(WColor.blue, tr("captcha.blue")); 
				break;
			case 3:
				c = new ShapeColor(WColor.yellow, tr("captcha.yellow")); 
				break;
		}		
		
		double x = size + r.nextDouble() * (getWidth().getValue() - 2 * size);
		double y = size + r.nextDouble() * (getHeight().getValue() - 2 * size);
		
		return Shapes.getRandomShape(new WPointF(x,y), c, size);
	}
	
	public WString getSelectedColor() {
		return toSelect.getColor().getColorName();
	}
	
	public WString getSelectedShape() {
		return toSelect.getShapeName();
	}
	
	public boolean correctlyClicked(WMouseEvent me) {
		return toSelect.contains(new WPointF(me.getWidget().x, me.getWidget().y));
	}
}
