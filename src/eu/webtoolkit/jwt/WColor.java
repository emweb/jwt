/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;

import eu.webtoolkit.jwt.utils.MathUtils;

/**
 * A class that specifies a color.
 * 
 * A color corresponds to a CSS color. You can specify a color either using its
 * red/green/blue components, or from a valid CSS name.
 * 
 * The color supports an alpha channel, which determines the degree of
 * transparency. An alpha value of 0 is completely transparent (and thus
 * invisible), while a value of 255 is completely opaque.
 */
public class WColor {
	/**
	 * White WColor object.
	 */
	public static WColor white= new WColor(0xff, 0xff, 0xff);
	/**
	 * Black WColor object.
	 */
	public static WColor black= new WColor(0x00, 0x00, 0x00);
	/**
	 * Red WColor object.
	 */
	public static WColor red= new WColor(0xff, 0x00, 0x00);
	/**
	 * Dark red WColor object.
	 */
	public static WColor darkRed= new WColor(0x80, 0x00, 0x00);
	/**
	 * Green WColor object.
	 */
	public static WColor green= new WColor(0x00, 0xff, 0x00);
	/**
	 * Dark green WColor object.
	 */
	public static WColor darkGreen= new WColor(0x00, 0x80, 0x00);
	/**
	 * Blue WColor object.
	 */
	public static WColor blue= new WColor(0x00, 0x00, 0xff);
	/**
	 * Dark blue WColor object.
	 */
	public static WColor darkBlue= new WColor(0x00, 0x00, 0x80);
	/**
	 * Cyan WColor object.
	 */
	public static WColor cyan= new WColor(0x00, 0xff, 0xff);
	/**
	 * Dark cyan WColor object.
	 */
	public static WColor darkCyan= new WColor(0x00, 0x80, 0x80);
	/**
	 * White Magenta object.
	 */
	public static WColor magenta= new WColor(0xff, 0x00, 0xff);
	/**
	 * Dark magenta WColor object.
	 */
	public static WColor darkMagenta= new WColor(0x80, 0x00, 0x80);
	/**
	 * Yellow WColor object.
	 */
	public static WColor yellow= new WColor(0xff, 0xff, 0x00);
	/**
	 * Dark yellow WColor object.
	 */
	public static WColor darkYellow= new WColor(0x80, 0x80, 0x00);
	/**
	 * Gray WColor object.
	 */
	public static WColor gray= new WColor(0xa0, 0xa0, 0xa4);
	/**
	 * Dark gray WColor object.
	 */
	public static WColor darkGray= new WColor(0x80, 0x80, 0x80);
	/**
	 * Light gray WColor object.
	 */
	public static WColor lightGray= new WColor(0xc0, 0xc0, 0xc0);
	/**
	 * Transparant WColor object.
	 */
	public static WColor transparent= new WColor(0x00, 0x00, 0x00, 0x00);
	
	/**
	 * Construct a default color.
	 * 
	 * The default color is depending on the context, another color (for example
	 * from a hierarchical parent in a widget tree), or a completely transparent
	 * color.
	 */
	public WColor() {
		this.default_ = true;
		this.red_ = 0;
		this.green_ = 0;
		this.blue_ = 0;
		this.alpha_ = 255;
		this.name_ = new WString();
	}

	/**
	 * Construct a color from a StandardColor.
	 */
	public WColor(StandardColor c) {
		this.default_ = true;
		this.red_ = 0;
		this.green_ = 0;
		this.blue_ = 0;
		this.alpha_ = 255;
		this.name_ = new WString();

		WColor otherColor = null;
		switch (c) {
			case White:
				otherColor = white;
				break;
			case Black:
				otherColor = black;
				break;
			case Red:
				otherColor = red;
				break;
			case DarkRed:
				otherColor = darkRed;
				break;
			case Green:
				otherColor = green;
				break;
			case DarkGreen:
				otherColor = darkGreen;
				break;
			case Blue:
				otherColor = blue;
				break;
			case DarkBlue:
				otherColor = darkBlue;
				break;
			case Cyan:
				otherColor = cyan;
				break;
			case DarkCyan:
				otherColor = darkCyan;
				break;
			case Magenta:
				otherColor = magenta;
				break;
			case DarkMagenta:
				otherColor = darkMagenta;
				break;
			case Yellow:
				otherColor = yellow;
				break;
			case DarkYellow:
				otherColor = darkYellow;
				break;
			case Gray:
				otherColor = gray;
				break;
			case DarkGray:
				otherColor = darkGray;
				break;
			case LightGray:
				otherColor = lightGray;
				break;
			case Transparent:
				otherColor = transparent;
				break;
		}
		setRgb(otherColor.getRed(), otherColor.getGreen(), otherColor.getBlue(), otherColor.getAlpha());
	}

	/**
	 * Construct a color with given red/green/blue/alpha components.
	 * 
	 * All four components must be specified with a value in the range (0 -
	 * 255). The alpha channel determines the degree of transparency. An alpha
	 * value of 0 is completely transparent (and thus invisible), while a value
	 * of 255 is completely opaque.
	 * 
	 * @see WColor#setRgb(int red, int green, int blue, int alpha)
	 */
	public WColor(int r, int g, int b, int a) {
		this.default_ = false;
		this.red_ = r;
		this.green_ = g;
		this.blue_ = b;
		this.alpha_ = a;
		this.name_ = new WString();
	}

	/**
	 * Construct a color with given red/green/blue components.
	 * 
	 * All three components must be specified with a value in the range (0 -
	 * 255). The alpha channel is defaulted to completely opaque (255).
	 * 
	 * @see WColor#setRgb(int, int, int)
	 */
	public WColor(int r, int g, int b) {
		this(r, g, b, 255);
	}

	/**
	 * Construct a color from a CSS name.
	 * 
	 * The <i>name</i> may be any valid CSS color name, including names colors
	 * such as &quot;aqua&quot;, or colors defined as RGB components.
	 * <p>
	 * In either case, the result of the methods {@link WColor#getRed()},
	 * {@link WColor#getGreen()} and {@link WColor#getBlue()} is undefined.
	 * <p>
	 * See also <a
	 * href="http://www.w3.org/TR/REC-CSS2/syndata.html#value-def-color"
	 * >http://www.w3.org/TR/REC-CSS2/syndata.html#value-def-color</a>
	 */
	public WColor(CharSequence name) {
		this.default_ = false;
		this.name_ = WString.toWString(name);
		
		WColor c = ColorUtils.parseCssColor(name.toString());
		this.setRgb(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}

	/**
	 * Set the red/green/blue/alpha components.
	 * 
	 * All four components must be specified with a value in the range (0 -
	 * 255). The alpha channel determines the degree of transparency. An alpha
	 * value of 0 is completely transparent (and thus invisible), while a value
	 * of 255 is completely opaque.
	 */
	public void setRgb(int red, int green, int blue, int alpha) {
		this.default_ = false;
		this.name_ = new WString();
		this.red_ = red;
		this.green_ = green;
		this.blue_ = blue;
		this.alpha_ = alpha;
	}

	/**
	 * Set the red/green/blue components and a 255 for the alpha component.
	 * 
	 * @see WColor#setRgb(int, int, int, int)
	 */
	public final void setRgb(int red, int green, int blue) {
		setRgb(red, green, blue, 255);
	}

	/**
	 * Set the CSS name.
	 * 
	 * The <i>name</i> may be any valid CSS color name, including names colors
	 * such as &quot;aqua&quot;, or colors defined as RGB components.
	 * <p>
	 * In either case, the result of the methods {@link WColor#getRed()},
	 * {@link WColor#getGreen()} and {@link WColor#getBlue()} is undefined.
	 * <p>
	 * See also <a
	 * href="http://www.w3.org/TR/REC-CSS2/syndata.html#value-def-color"
	 * >http://www.w3.org/TR/REC-CSS2/syndata.html#value-def-color</a>
	 */
	public void setName(CharSequence name) {
		this.default_ = false;
		this.red_ = this.green_ = this.blue_ = -1;
		this.alpha_ = 255;
	}

	/**
	 * Returns if the color is the default color.
	 * 
	 * @see WColor#WColor()
	 */
	public boolean isDefault() {
		return this.default_;
	}

	/**
	 * Returns the red component.
	 * 
	 * Only available when the color was specified in terms of the RGB
	 * components using
	 * {@link WColor#setRgb(int, int, int, int)} or
	 * {@link WColor}(int, int, int, int).
	 */
	public int getRed() {
		return this.red_;
	}

	/**
	 * Returns the green component.
	 * 
	 * Only available when the color was specified in terms of the RGB
	 * components using
	 * {@link WColor#setRgb(int, int, int, int)} or
	 * {@link WColor}(int, int, int, int).
	 */
	public int getGreen() {
		return this.green_;
	}

	/**
	 * Returns the blue component.
	 * 
	 * Only available when the color was specified in terms of the RGB
	 * components using
	 * {@link WColor#setRgb(int, int, int, int)} or
	 * {@link WColor}(int, int, int, int).
	 */
	public int getBlue() {
		return this.blue_;
	}

	/**
	 * Returns the alpha component.
	 * 
	 * Only available when the color was specified in terms of the RGB
	 * components using
	 * {@link WColor#setRgb(int, int, int, int)} or
	 * {@link WColor}(int, int, int, int).
	 */
	public int getAlpha() {
		return this.alpha_;
	}

	/**
	 * Get CSS name.
	 * 
	 * Only available when it was set with
	 * {@link WColor#setName(CharSequence name)} or
	 * {@link WColor#WColor(CharSequence name)}.
	 */
	public WString getName() {
		return this.name_;
	}

	/**
	 * Indicates whether some other object is "equal to" this one. 
	 * 
	 * Returns true if the two colors were defined in exactly the same way. It
	 * may return false although they actually represent the same color.
	 */
	public boolean equals(WColor other) {
		return this.default_ == other.default_ && this.red_ == other.red_
				&& this.green_ == other.green_ && this.blue_ == other.blue_
				&& this.alpha_ == other.alpha_
				&& this.name_.equals(other.name_);
	}

	public final String getCssText(boolean withAlpha) {
		if (this.default_) {
			return "";
		} else {
			if (!this.name_.isEmpty()) {
				return this.name_.toString();
			} else {
				StringWriter tmp = new StringWriter();
				if (this.alpha_ != 255 && withAlpha) {
					tmp.append("rgba(").append(String.valueOf(this.red_));
					tmp.append(',').append(String.valueOf(this.green_));
					tmp.append(',').append(String.valueOf(this.blue_));
					tmp.append(',').append(
							MathUtils.roundCss(this.alpha_ / 255., 2)).append(')');
				} else {
					tmp.append("rgb(").append(String.valueOf(this.red_));
					tmp.append(',').append(String.valueOf(this.green_));
					tmp.append(',').append(String.valueOf(this.blue_))
							.append(')');
				}
				return tmp.toString();
			}
		}
	}

	public final String getCssText() {
		return getCssText(false);
	}

	public final void toHSL(double[] hsl) {
		assert hsl.length == 3;
		double h = 0.0, s = 0.0, l = 0.0;
		double r = getRed() / 255.0;
		double g = getGreen() / 255.0;
		double b = getBlue() / 255.0;
		double cmax = Math.max(r, Math.max(g, b));
		double cmin = Math.min(r, Math.min(g, b));
		double delta = cmax - cmin;
		l = (cmax + cmin) / 2.0;
		s = delta == 0 ? 0 : delta / (1.0 - Math.abs(2.0 * l - 1.0));
		if (delta == 0) {
			h = 0;
		} else if (cmax == r) {
			if (g >= b) {
				h = 60.0 * (g - b) / delta;
			} else {
				h = 60.0 * ((g - b) / delta + 6.0);
			}
		} else if (cmax == g) {
			h = 60.0 * ((b - r) / delta + 2.0);
		} else if (cmax == b) {
			h = 60.0 * ((r - g) / delta + 4.0);
		}
		hsl[0] = h; hsl[1] = s; hsl[2] = l;
	}

	public static WColor fromHSL(double h, double s, double l, int alpha) {
		double c = (1.0 - Math.abs(2.0 * l - 1.0)) * s;
		double x = c * (1.0 - Math.abs((h / 60.0) % 2.0 - 1.0));
		double m = l - c/2.0;
		double r, g, b;
		if (0.0 <= h && h < 60.0) {
			r = c;
			g = x;
			b = 0;
		} else if (60.0 <= h && h < 120.0) {
			r = x;
			g = c;
			b = 0;
		} else if (120.0 <= h && h < 180.0) {
			r = 0;
			g = c;
			b = x;
		} else if (180.0 <= h && h < 240.0) {
			r = 0;
			g = x;
			b = c;
		} else if (240.0 <= h && h < 300.0) {
			r = x;
			g = 0;
			b = c;
		} else /* 300 <= h < 360 */ {
			r = c;
			g = 0;
			b = x;
		}
		return new WColor((int)((r + m) * 255), (int)((g + m) * 255), (int)((b + m) * 255), alpha);
	}

	private boolean default_;
	private int red_;
	private int green_;
	private int blue_;
	private int alpha_;
	private WString name_;
}
