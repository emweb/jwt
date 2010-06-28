/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;

/**
 * A style class describing a font.
 */
public class WFont {
	/**
	 * The generic font family.
	 */
	public enum GenericFamily {
		/**
		 * Browser-dependent default.
		 */
		Default,
		/**
		 * for example: Times
		 */
		Serif,
		/**
		 * for example: Helvetica
		 */
		SansSerif,
		/**
		 * for example: Zapf-Chancery
		 */
		Cursive,
		/**
		 * for example: Western
		 */
		Fantasy,
		/**
		 * for example: Courier
		 */
		Monospace;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * The font style.
	 */
	public enum Style {
		/**
		 * Normal (default).
		 */
		NormalStyle,
		/**
		 * Italic.
		 */
		Italic,
		/**
		 * Oblique.
		 */
		Oblique;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * The font variant.
	 */
	public enum Variant {
		/**
		 * Normal (default).
		 */
		NormalVariant,
		/**
		 * Small Capitals.
		 */
		SmallCaps;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * The font weight.
	 */
	public enum Weight {
		/**
		 * Normal (default) (Value == 400).
		 */
		NormalWeight,
		/**
		 * Bold (Value == 700).
		 */
		Bold,
		/**
		 * Bolder than the parent widget.
		 */
		Bolder,
		/**
		 * Lighter than the parent widget.
		 */
		Lighter,
		/**
		 * Specify a value (100 - 900).
		 */
		Value;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * The font size.
	 */
	public enum Size {
		/**
		 * Extra Extra small.
		 */
		XXSmall,
		/**
		 * Extra small.
		 */
		XSmall,
		/**
		 * Small.
		 */
		Small,
		/**
		 * Medium, default.
		 */
		Medium,
		/**
		 * Large.
		 */
		Large,
		/**
		 * Extra large.
		 */
		XLarge,
		/**
		 * Extra Extra large.
		 */
		XXLarge,
		/**
		 * Relatively smaller than the parent widget.
		 */
		Smaller,
		/**
		 * Relatively larger than the parent widget.
		 */
		Larger,
		/**
		 * Explicit size, See also fontFixedSize().
		 */
		FixedSize;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * A default font (dependent on the user agent).
	 */
	public WFont() {
		this.widget_ = null;
		this.genericFamily_ = WFont.GenericFamily.Default;
		this.specificFamilies_ = new WString();
		this.style_ = WFont.Style.NormalStyle;
		this.variant_ = WFont.Variant.NormalVariant;
		this.weight_ = WFont.Weight.NormalWeight;
		this.weightValue_ = 400;
		this.size_ = WFont.Size.Medium;
		this.fixedSize_ = new WLength();
		this.familyChanged_ = false;
		this.styleChanged_ = false;
		this.variantChanged_ = false;
		this.weightChanged_ = false;
		this.sizeChanged_ = false;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 */
	public boolean equals(WFont other) {
		return this.genericFamily_ == other.genericFamily_
				&& this.specificFamilies_.equals(other.specificFamilies_)
				&& this.style_ == other.style_
				&& this.variant_ == other.variant_
				&& this.weight_ == other.weight_
				&& this.weightValue_ == other.weightValue_
				&& this.size_ == other.size_
				&& this.fixedSize_.equals(other.fixedSize_);
	}

	/**
	 * Sets the font family.
	 * <p>
	 * The font family is specified using a generic family name, in addition to
	 * a comma-seperated list of specific font choices.
	 * <p>
	 * The first specific font that can be matched will be used, otherwise a
	 * generic font will be used.
	 */
	public void setFamily(WFont.GenericFamily genericFamily,
			CharSequence specificFamilies) {
		this.genericFamily_ = genericFamily;
		this.specificFamilies_ = WString.toWString(specificFamilies);
		this.familyChanged_ = true;
		if (this.widget_ != null) {
			this.widget_.repaint(EnumSet
					.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Sets the font family.
	 * <p>
	 * Calls
	 * {@link #setFamily(WFont.GenericFamily genericFamily, CharSequence specificFamilies)
	 * setFamily(genericFamily, new WString())}
	 */
	public final void setFamily(WFont.GenericFamily genericFamily) {
		setFamily(genericFamily, new WString());
	}

	/**
	 * Returns the font generic family.
	 */
	public WFont.GenericFamily getGenericFamily() {
		return this.genericFamily_;
	}

	/**
	 * Returns the font specific family names.
	 */
	public WString getSpecificFamilies() {
		return this.specificFamilies_;
	}

	/**
	 * Sets the font style.
	 */
	public void setStyle(WFont.Style style) {
		this.style_ = style;
		this.styleChanged_ = true;
		if (this.widget_ != null) {
			this.widget_.repaint(EnumSet
					.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Returns the font style.
	 */
	public WFont.Style getStyle() {
		return this.style_;
	}

	/**
	 * Sets the font variant.
	 */
	public void setVariant(WFont.Variant variant) {
		this.variant_ = variant;
		this.variantChanged_ = true;
		if (this.widget_ != null) {
			this.widget_.repaint(EnumSet
					.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Returns the font variant.
	 */
	public WFont.Variant getVariant() {
		return this.variant_;
	}

	/**
	 * Sets the font weight.
	 * <p>
	 * When setting weight == Value, you may specify a value.
	 * <p>
	 * Valid values are between 100 and 900, and are rounded to multiples of
	 * 100.
	 */
	public void setWeight(WFont.Weight weight, int value) {
		this.weight_ = weight;
		this.weightValue_ = value;
		this.weightChanged_ = true;
		if (this.widget_ != null) {
			this.widget_.repaint(EnumSet
					.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Sets the font weight.
	 * <p>
	 * Calls {@link #setWeight(WFont.Weight weight, int value) setWeight(weight,
	 * 400)}
	 */
	public final void setWeight(WFont.Weight weight) {
		setWeight(weight, 400);
	}

	/**
	 * Returns the font weight.
	 */
	public WFont.Weight getWeight() {
		return this.weight_;
	}

	/**
	 * Returns the font weight value.
	 */
	public int getWeightValue() {
		return this.weightValue_;
	}

	/**
	 * Sets the font size.
	 */
	public void setSize(WFont.Size size, WLength fixedSize) {
		this.size_ = size;
		if (this.size_ == WFont.Size.FixedSize) {
			this.fixedSize_ = fixedSize;
		} else {
			this.fixedSize_ = WLength.Auto;
		}
		this.sizeChanged_ = true;
		if (this.widget_ != null) {
			this.widget_.repaint(EnumSet
					.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Sets the font size.
	 * <p>
	 * Calls {@link #setSize(WFont.Size size, WLength fixedSize) setSize(size,
	 * WLength.Auto)}
	 */
	public final void setSize(WFont.Size size) {
		setSize(size, WLength.Auto);
	}

	/**
	 * Returns the font size.
	 */
	public WFont.Size getSize() {
		return this.size_;
	}

	/**
	 * Returns the fixed font size for {@link WFont.Size#FixedSize FixedSize}.
	 */
	public WLength getFixedSize() {
		return this.fixedSize_;
	}

	String getCssText(boolean combined) {
		StringBuilder result = new StringBuilder();
		if (combined) {
			result.append(this.cssStyle(false)).append(' ').append(
					this.cssVariant(false)).append(' ').append(
					this.cssWeight(false)).append(' ').append(
					this.cssSize(true)).append(' ')
					.append(this.cssFamily(true));
		} else {
			String s = "";
			s = this.cssFamily(false);
			if (s.length() != 0) {
				result.append("font-family: ").append(s).append(";");
			}
			s = this.cssSize(false);
			if (s.length() != 0) {
				result.append("font-size: ").append(s).append(";");
			}
			s = this.cssStyle(false);
			if (s.length() != 0) {
				result.append("font-style: ").append(s).append(";");
			}
			s = this.cssVariant(false);
			if (s.length() != 0) {
				result.append("font-variant: ").append(s).append(";");
			}
			s = this.cssWeight(false);
			if (s.length() != 0) {
				result.append("font-weight: ").append(s).append(";");
			}
		}
		return result.toString();
	}

	final String getCssText() {
		return getCssText(true);
	}

	public void updateDomElement(DomElement element, boolean fontall,
			boolean all) {
		if (this.familyChanged_ || fontall || all) {
			String family = this.cssFamily(fontall);
			if (family.length() != 0) {
				element.setProperty(Property.PropertyStyleFontFamily, family);
			}
			this.familyChanged_ = false;
		}
		if (this.styleChanged_ || fontall || all) {
			String style = this.cssStyle(fontall);
			if (style.length() != 0) {
				element.setProperty(Property.PropertyStyleFontStyle, style);
			}
			this.styleChanged_ = false;
		}
		if (this.variantChanged_ || fontall || all) {
			String variant = this.cssVariant(fontall);
			if (variant.length() != 0) {
				element.setProperty(Property.PropertyStyleFontVariant, variant);
			}
			this.variantChanged_ = false;
		}
		if (this.weightChanged_ || fontall || all) {
			String weight = this.cssWeight(fontall);
			if (weight.length() != 0) {
				element.setProperty(Property.PropertyStyleFontWeight, weight);
			}
			this.weightChanged_ = false;
		}
		if (this.sizeChanged_ || fontall || all) {
			String size = this.cssSize(fontall);
			if (size.length() != 0) {
				element.setProperty(Property.PropertyStyleFontSize, size);
			}
			this.sizeChanged_ = false;
		}
	}

	private WWebWidget widget_;
	private WFont.GenericFamily genericFamily_;
	private WString specificFamilies_;
	private WFont.Style style_;
	private WFont.Variant variant_;
	private WFont.Weight weight_;
	private int weightValue_;
	private WFont.Size size_;
	private WLength fixedSize_;
	private boolean familyChanged_;
	private boolean styleChanged_;
	private boolean variantChanged_;
	private boolean weightChanged_;
	private boolean sizeChanged_;

	private String cssStyle(boolean all) {
		switch (this.style_) {
		case NormalStyle:
			if (this.styleChanged_ || all) {
				return "normal";
			}
			break;
		case Italic:
			return "italic";
		case Oblique:
			return "oblique";
		}
		return "";
	}

	private String cssVariant(boolean all) {
		switch (this.variant_) {
		case NormalVariant:
			if (this.variantChanged_ || all) {
				return "normal";
			}
			break;
		case SmallCaps:
			return "small-caps";
		}
		return "";
	}

	private String cssWeight(boolean all) {
		switch (this.weight_) {
		case NormalWeight:
			if (this.weightChanged_ || all) {
				return "normal";
			}
			break;
		case Bold:
			return "bold";
		case Bolder:
			return "bolder";
		case Lighter:
			return "lighter";
		case Value: {
			int v = Math.min(900, Math.max(100, this.weightValue_ / 100 * 100));
			return String.valueOf(v);
		}
		}
		return "";
	}

	private String cssFamily(boolean all) {
		String family = this.specificFamilies_.toString();
		if (family.length() != 0) {
			family += ',';
		}
		switch (this.genericFamily_) {
		case Default:
			if (this.familyChanged_ || all) {
				family = "inherit";
			}
			break;
		case Serif:
			family += "serif";
			break;
		case SansSerif:
			family += "sans-serif";
			break;
		case Cursive:
			family += "cursive";
			break;
		case Fantasy:
			family += "fantasay";
			break;
		case Monospace:
			family += "monospace";
			break;
		}
		return family;
	}

	private String cssSize(boolean all) {
		switch (this.size_) {
		case Medium:
			if (this.sizeChanged_ || all) {
				return "medium";
			}
			break;
		case XXSmall:
			return "xx-small";
		case XSmall:
			return "x-small";
		case Small:
			return "small";
		case Large:
			return "large";
		case XLarge:
			return "x-large";
		case XXLarge:
			return "xx-large";
		case Smaller:
			return "smaller";
		case Larger:
			return "larger";
		case FixedSize:
			return this.fixedSize_.getCssText();
		}
		return "";
	}

	void setWebWidget(WWebWidget w) {
		this.widget_ = w;
	}
}
