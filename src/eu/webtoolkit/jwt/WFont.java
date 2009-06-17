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
		 * e.g. Times
		 */
		Serif,
		/**
		 * e.g. Helvetica
		 */
		SansSerif,
		/**
		 * e.g. Zapf-Chancery
		 */
		Cursive,
		/**
		 * e.g. Western
		 */
		Fantasy,
		/**
		 * e.g. Courier
		 */
		Monospace;

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
	 * Set the font family.
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
	 * Set the font family.
	 * <p>
	 * Calls
	 * {@link #setFamily(WFont.GenericFamily genericFamily, CharSequence specificFamilies)
	 * setFamily(genericFamily, new WString())}
	 */
	public final void setFamily(WFont.GenericFamily genericFamily) {
		setFamily(genericFamily, new WString());
	}

	/**
	 * Get the font generic family.
	 */
	public WFont.GenericFamily getGenericFamily() {
		return this.genericFamily_;
	}

	/**
	 * Get the font specific family names.
	 */
	public WString getSpecificFamilies() {
		return this.specificFamilies_;
	}

	/**
	 * Set the font style.
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
	 * Get the font style.
	 */
	public WFont.Style getStyle() {
		return this.style_;
	}

	/**
	 * Set the font variant.
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
	 * Get the font variant.
	 */
	public WFont.Variant getVariant() {
		return this.variant_;
	}

	/**
	 * Set the font weight.
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
	 * Set the font weight.
	 * <p>
	 * Calls {@link #setWeight(WFont.Weight weight, int value) setWeight(weight,
	 * 400)}
	 */
	public final void setWeight(WFont.Weight weight) {
		setWeight(weight, 400);
	}

	/**
	 * Get the font weight.
	 */
	public WFont.Weight getWeight() {
		return this.weight_;
	}

	/**
	 * Get the font weight value.
	 */
	public int getWeightValue() {
		return this.weightValue_;
	}

	/**
	 * Set the font size.
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
	 * Set the font size.
	 * <p>
	 * Calls {@link #setSize(WFont.Size size, WLength fixedSize) setSize(size,
	 * WLength.Auto)}
	 */
	public final void setSize(WFont.Size size) {
		setSize(size, WLength.Auto);
	}

	/**
	 * Get the font size.
	 */
	public WFont.Size getSize() {
		return this.size_;
	}

	/**
	 * Get the fixed font size for {@link WFont.Size#FixedSize FixedSize}.
	 */
	public WLength getFixedSize() {
		return this.fixedSize_;
	}

	public String getCssText() {
		DomElement d = DomElement.createNew(DomElementType.DomElement_DIV);
		WFont f = this;
		f.updateDomElement(d, false, true);
		String result = d.getCssStyle();
		/* delete d */;
		return result;
	}

	public void updateDomElement(DomElement element, boolean fontall,
			boolean all) {
		if (this.familyChanged_ || fontall || all) {
			String family = this.specificFamilies_.toString();
			if (family.length() != 0) {
				family += ',';
			}
			switch (this.genericFamily_) {
			case Default:
				if (this.familyChanged_ || fontall) {
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
			if (family.length() != 0) {
				element.setProperty(Property.PropertyStyleFontFamily, family);
			}
			this.familyChanged_ = false;
		}
		if (this.styleChanged_ || fontall || all) {
			switch (this.style_) {
			case NormalStyle:
				if (this.styleChanged_ || fontall) {
					element.setProperty(Property.PropertyStyleFontStyle,
							"normal");
				}
				break;
			case Italic:
				element.setProperty(Property.PropertyStyleFontStyle, "italic");
				break;
			case Oblique:
				element.setProperty(Property.PropertyStyleFontStyle, "oblique");
				break;
			}
			this.styleChanged_ = false;
		}
		if (this.variantChanged_ || fontall || all) {
			switch (this.variant_) {
			case NormalVariant:
				if (this.variantChanged_ || fontall) {
					element.setProperty(Property.PropertyStyleFontVariant,
							"normal");
				}
				break;
			case SmallCaps:
				element.setProperty(Property.PropertyStyleFontVariant,
						"small-caps");
				break;
			}
			this.variantChanged_ = false;
		}
		if (this.weightChanged_ || fontall || all) {
			switch (this.weight_) {
			case NormalWeight:
				if (this.weightChanged_ || fontall) {
					element.setProperty(Property.PropertyStyleFontWeight,
							"normal");
				}
				break;
			case Bold:
				element.setProperty(Property.PropertyStyleFontWeight, "bold");
				break;
			case Bolder:
				element.setProperty(Property.PropertyStyleFontWeight, "bolder");
				break;
			case Lighter:
				element
						.setProperty(Property.PropertyStyleFontWeight,
								"lighter");
				break;
			case Value: {
				int v = Math.min(900, Math.max(100,
						this.weightValue_ / 100 * 100));
				element.setProperty(Property.PropertyStyleFontWeight, String
						.valueOf(v));
				break;
			}
			}
			this.weightChanged_ = false;
		}
		if (this.sizeChanged_ || fontall || all) {
			switch (this.size_) {
			case Medium:
				if (this.sizeChanged_ || fontall) {
					element.setProperty(Property.PropertyStyleFontSize,
							"medium");
				}
				break;
			case XXSmall:
				element.setProperty(Property.PropertyStyleFontSize, "xx-small");
				break;
			case XSmall:
				element.setProperty(Property.PropertyStyleFontSize, "x-small");
				break;
			case Small:
				element.setProperty(Property.PropertyStyleFontSize, "small");
				break;
			case Large:
				element.setProperty(Property.PropertyStyleFontSize, "large");
				break;
			case XLarge:
				element.setProperty(Property.PropertyStyleFontSize, "x-large");
				break;
			case XXLarge:
				element.setProperty(Property.PropertyStyleFontSize, "xx-large");
				break;
			case Smaller:
				element.setProperty(Property.PropertyStyleFontSize, "smaller");
				break;
			case Larger:
				element.setProperty(Property.PropertyStyleFontSize, "larger");
				break;
			case FixedSize:
				element.setProperty(Property.PropertyStyleFontSize,
						this.fixedSize_.getCssText());
				break;
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

	void setWebWidget(WWebWidget w) {
		this.widget_ = w;
	}
}
