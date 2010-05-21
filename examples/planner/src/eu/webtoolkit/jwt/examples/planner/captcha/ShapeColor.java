package eu.webtoolkit.jwt.examples.planner.captcha;

import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WString;

public class ShapeColor extends WColor {
	private WString colorName;
	
	public ShapeColor(WColor color, WString colorName) {
		super(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		this.colorName = colorName;
	}
	
	public WString getColorName() {
		return colorName;
	}
}
