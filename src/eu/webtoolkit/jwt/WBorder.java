/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * A style class for the border of a widget.
 */
public class WBorder {
	/**
	 * Enumeration for border width.
	 */
	public enum Width {
		/**
		 * Browser-dependent &apos;thin&apos; border.
		 */
		Thin,
		/**
		 * Browser-dependent &apos;medium&apos; border, default.
		 */
		Medium,
		/**
		 * Browser-dependent &apos;thin&apos; border.
		 */
		Thick,
		/**
		 * Explicit width. See also {@link WBorder#getExplicitWidth()
		 * getExplicitWidth()}.
		 */
		Explicit;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Enumeration for border style.
	 */
	public enum Style {
		/**
		 * No border (width ignored), default.
		 */
		None,
		/**
		 * Invisible border (of specified width).
		 */
		Hidden,
		/**
		 * Dotted border.
		 */
		Dotted,
		/**
		 * Dashed border.
		 */
		Dashed,
		/**
		 * Solid border.
		 */
		Solid,
		/**
		 * Double lined border.
		 */
		Double,
		/**
		 * Relief border grooved into the canvas.
		 */
		Groove,
		/**
		 * Relief border coming out of the canvas.
		 */
		Ridge,
		/**
		 * Relief border lowering contents into the canvas.
		 */
		Inset,
		/**
		 * Relief border letting contents come out of the canvas.
		 */
		Outset;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Create a border indicating <i>no border</i>.
	 */
	public WBorder() {
		this.width_ = WBorder.Width.Medium;
		this.explicitWidth_ = new WLength();
		this.color_ = new WColor();
		this.style_ = WBorder.Style.None;
	}

	/**
	 * Create a border with given style, thickness and color.
	 */
	public WBorder(WBorder.Style style, WBorder.Width width, WColor color) {
		this.width_ = width;
		this.explicitWidth_ = new WLength();
		this.color_ = color;
		this.style_ = style;
	}

	/**
	 * Create a border with given style, thickness and color.
	 * <p>
	 * Calls
	 * {@link #WBorder(WBorder.Style style, WBorder.Width width, WColor color)
	 * this(style, WBorder.Width.Medium, new WColor())}
	 */
	public WBorder(WBorder.Style style) {
		this(style, WBorder.Width.Medium, new WColor());
	}

	/**
	 * Create a border with given style, thickness and color.
	 * <p>
	 * Calls
	 * {@link #WBorder(WBorder.Style style, WBorder.Width width, WColor color)
	 * this(style, width, new WColor())}
	 */
	public WBorder(WBorder.Style style, WBorder.Width width) {
		this(style, width, new WColor());
	}

	/**
	 * Create a border with an absolute width.
	 */
	public WBorder(WBorder.Style style, WLength width, WColor color) {
		this.width_ = WBorder.Width.Explicit;
		this.explicitWidth_ = width;
		this.color_ = color;
		this.style_ = style;
	}

	/**
	 * Create a border with an absolute width.
	 * <p>
	 * Calls {@link #WBorder(WBorder.Style style, WLength width, WColor color)
	 * this(style, width, new WColor())}
	 */
	public WBorder(WBorder.Style style, WLength width) {
		this(style, width, new WColor());
	}

	/**
	 * Comparison operator.
	 */
	public boolean equals(WBorder other) {
		return this.width_ == other.width_ && this.color_.equals(other.color_)
				&& this.style_ == other.style_;
	}

	/**
	 * Set the border width.
	 * <p>
	 * If width == Explicit, then the width specified in
	 * <code>explicitWidth</code> is used.
	 */
	public void setWidth(WBorder.Width width, WLength explicitWidth) {
		this.width_ = width;
		this.explicitWidth_ = explicitWidth;
	}

	/**
	 * Set the border width.
	 * <p>
	 * Calls {@link #setWidth(WBorder.Width width, WLength explicitWidth)
	 * setWidth(width, WLength.Auto)}
	 */
	public final void setWidth(WBorder.Width width) {
		setWidth(width, WLength.Auto);
	}

	/**
	 * Set the border color.
	 */
	public void setColor(WColor color) {
		this.color_ = color;
	}

	/**
	 * Set the border style.
	 */
	public void setStyle(WBorder.Style style) {
		this.style_ = style;
	}

	/**
	 * Returns the border width.
	 * <p>
	 * 
	 * @see WBorder#setWidth(WBorder.Width width, WLength explicitWidth)
	 */
	public WBorder.Width getWidth() {
		return this.width_;
	}

	/**
	 * Returns the border width when set explicitly.
	 * <p>
	 * 
	 * @see WBorder#setWidth(WBorder.Width width, WLength explicitWidth)
	 */
	public WLength getExplicitWidth() {
		return this.explicitWidth_;
	}

	/**
	 * Returns the border color.
	 * <p>
	 * 
	 * @see WBorder#setColor(WColor color)
	 */
	public WColor getColor() {
		return this.color_;
	}

	/**
	 * Returns the border style.
	 * <p>
	 * 
	 * @see WBorder#setStyle(WBorder.Style style)
	 */
	public WBorder.Style getStyle() {
		return this.style_;
	}

	/**
	 * Returns the CSS text for this border style.
	 */
	public String getCssText() {
		String style = "";
		switch (this.style_) {
		case None:
			return "none";
		case Hidden:
			style = "hidden";
			break;
		case Dotted:
			style = "dotted";
			break;
		case Dashed:
			style = "dashed";
			break;
		case Solid:
			style = "solid";
			break;
		case Double:
			style = "double";
			break;
		case Groove:
			style = "groove";
			break;
		case Ridge:
			style = "ridge";
			break;
		case Inset:
			style = "inset";
			break;
		case Outset:
			style = "outset";
			break;
		}
		String width = "";
		switch (this.width_) {
		case Thin:
			width = "thin";
			break;
		case Medium:
			width = "medium";
			break;
		case Thick:
			width = "thick";
			break;
		case Explicit:
			width = this.explicitWidth_.getCssText();
		}
		return width + " " + style + " " + this.color_.getCssText();
	}

	private WBorder.Width width_;
	private WLength explicitWidth_;
	private WColor color_;
	private WBorder.Style style_;
}
