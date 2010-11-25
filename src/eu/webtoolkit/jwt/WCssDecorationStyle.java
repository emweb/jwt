/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A style class for a single widget or style sheet rule.
 * <p>
 * 
 * You can manipulate the decoration style of a single widget using
 * {@link WWidget#getDecorationStyle() WWidget#getDecorationStyle()} or you can
 * use a {@link WCssDecorationStyle} to add a rule to the inline style sheet
 * using
 * {@link WCssStyleSheet#addRule(String selector, WCssDecorationStyle style, String ruleName)
 * WCssStyleSheet#addRule()}.
 */
public class WCssDecorationStyle extends WObject {
	/**
	 * How a background image must be repeated.
	 */
	public enum Repeat {
		/**
		 * Repeat horizontally and vertically, default.
		 */
		RepeatXY,
		/**
		 * Repeat horizontally.
		 */
		RepeatX,
		/**
		 * Repeat vertically.
		 */
		RepeatY,
		/**
		 * Do not repeat.
		 */
		NoRepeat;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Text decoration options.
	 */
	public enum TextDecoration {
		/**
		 * Underline.
		 */
		Underline,
		/**
		 * Overline.
		 */
		Overline,
		/**
		 * LineThrough.
		 */
		LineThrough,
		/**
		 * Blink.
		 */
		Blink;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Creates a default style.
	 */
	public WCssDecorationStyle() {
		super();
		this.widget_ = null;
		this.cursor_ = Cursor.AutoCursor;
		this.cursorImage_ = "";
		this.border_ = new WBorder();
		this.backgroundColor_ = new WColor();
		this.foregroundColor_ = new WColor();
		this.backgroundImage_ = "";
		this.backgroundImageRepeat_ = WCssDecorationStyle.Repeat.RepeatXY;
		this.backgroundImageLocation_ = EnumSet.noneOf(Side.class);
		this.font_ = new WFont();
		this.textDecoration_ = EnumSet
				.noneOf(WCssDecorationStyle.TextDecoration.class);
		this.borderPosition_ = EnumSet.noneOf(Side.class);
		this.cursorChanged_ = false;
		this.borderChanged_ = false;
		this.foregroundColorChanged_ = false;
		this.backgroundColorChanged_ = false;
		this.backgroundImageChanged_ = false;
		this.fontChanged_ = false;
		this.textDecorationChanged_ = false;
	}

	/**
	 * Sets the cursor style.
	 */
	public void setCursor(Cursor c) {
		if (!WWebWidget.canOptimizeUpdates() || this.cursor_ != c) {
			this.cursor_ = c;
			this.cursorChanged_ = true;
			this.changed();
		}
	}

	/**
	 * Returns the cursor style.
	 */
	public Cursor getCursor() {
		return this.cursor_;
	}

	/**
	 * Sets a custom cursor image URI, with optionally a fallback cursor.
	 * <p>
	 * The URI should point to a .cur file (this shoul be a real .cur file,
	 * renaming an .ico is not enough for Internet Explorer).
	 */
	public void setCursor(String cursorImage, Cursor fallback) {
		if (!WWebWidget.canOptimizeUpdates()
				|| !this.cursorImage_.equals(cursorImage)
				|| this.cursor_ != fallback) {
			this.cursorImage_ = cursorImage;
			this.cursor_ = fallback;
			this.cursorChanged_ = true;
			this.changed();
		}
	}

	/**
	 * Sets a custom cursor image URI, with optionally a fallback cursor.
	 * <p>
	 * Calls {@link #setCursor(String cursorImage, Cursor fallback)
	 * setCursor(cursorImage, Cursor.ArrowCursor)}
	 */
	public final void setCursor(String cursorImage) {
		setCursor(cursorImage, Cursor.ArrowCursor);
	}

	/**
	 * Returns the cursor image.
	 */
	public String getCursorImage() {
		return this.cursorImage_;
	}

	/**
	 * Sets the background color.
	 */
	public void setBackgroundColor(WColor color) {
		if (!WWebWidget.canOptimizeUpdates()
				|| !this.backgroundColor_.equals(color)) {
			this.backgroundColorChanged_ = true;
			this.backgroundColor_ = color;
			this.changed();
		}
	}

	/**
	 * Returns the background color.
	 */
	public WColor getBackgroundColor() {
		return this.backgroundColor_;
	}

	/**
	 * Sets a background image URL.
	 * <p>
	 * The image may be placed in a particular location by specifying sides by
	 * OR&apos;ing {@link Side} values together, e.g. (Right | Top).
	 * <p>
	 * 
	 * @see WCssDecorationStyle#setBackgroundImage(String image,
	 *      WCssDecorationStyle.Repeat repeat, EnumSet sides)
	 */
	public void setBackgroundImage(String image,
			WCssDecorationStyle.Repeat repeat, EnumSet<Side> sides) {
		if (!WWebWidget.canOptimizeUpdates()
				|| !this.backgroundImage_.equals(image)
				|| this.backgroundImageRepeat_ != repeat
				|| !this.backgroundImageLocation_.equals(sides)) {
			this.backgroundImage_ = image;
			this.backgroundImageResource_ = null;
			this.backgroundImageRepeat_ = repeat;
			this.backgroundImageLocation_ = EnumSet.copyOf(sides);
			this.backgroundImageChanged_ = true;
			this.changed();
		}
	}

	/**
	 * Sets a background image URL.
	 * <p>
	 * Calls
	 * {@link #setBackgroundImage(String image, WCssDecorationStyle.Repeat repeat, EnumSet sides)
	 * setBackgroundImage(image, repeat, EnumSet.of(side, sides))}
	 */
	public final void setBackgroundImage(String image,
			WCssDecorationStyle.Repeat repeat, Side side, Side... sides) {
		setBackgroundImage(image, repeat, EnumSet.of(side, sides));
	}

	/**
	 * Sets a background image URL.
	 * <p>
	 * Calls
	 * {@link #setBackgroundImage(String image, WCssDecorationStyle.Repeat repeat, EnumSet sides)
	 * setBackgroundImage(image, WCssDecorationStyle.Repeat.RepeatXY,
	 * EnumSet.noneOf(Side.class))}
	 */
	public final void setBackgroundImage(String image) {
		setBackgroundImage(image, WCssDecorationStyle.Repeat.RepeatXY, EnumSet
				.noneOf(Side.class));
	}

	/**
	 * Sets a background image URL.
	 * <p>
	 * Calls
	 * {@link #setBackgroundImage(String image, WCssDecorationStyle.Repeat repeat, EnumSet sides)
	 * setBackgroundImage(image, repeat, EnumSet.noneOf(Side.class))}
	 */
	public final void setBackgroundImage(String image,
			WCssDecorationStyle.Repeat repeat) {
		setBackgroundImage(image, repeat, EnumSet.noneOf(Side.class));
	}

	/**
	 * Sets a background image URL.
	 * <p>
	 * The image may be placed in a particular location by specifying sides by
	 * OR&apos;ing {@link Side} values together, e.g. (Right | Top).
	 * <p>
	 * 
	 * @see WCssDecorationStyle#setBackgroundImage(String image,
	 *      WCssDecorationStyle.Repeat repeat, EnumSet sides)
	 */
	public void setBackgroundImage(WResource resource,
			WCssDecorationStyle.Repeat repeat, EnumSet<Side> sides) {
		this.backgroundImageResource_ = resource;
		resource.dataChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				WCssDecorationStyle.this.backgroundImageResourceChanged();
			}
		});
		this.setBackgroundImage(resource.getUrl(), repeat, sides);
	}

	/**
	 * Sets a background image URL.
	 * <p>
	 * Calls
	 * {@link #setBackgroundImage(WResource resource, WCssDecorationStyle.Repeat repeat, EnumSet sides)
	 * setBackgroundImage(resource, repeat, EnumSet.of(side, sides))}
	 */
	public final void setBackgroundImage(WResource resource,
			WCssDecorationStyle.Repeat repeat, Side side, Side... sides) {
		setBackgroundImage(resource, repeat, EnumSet.of(side, sides));
	}

	/**
	 * Sets a background image URL.
	 * <p>
	 * Calls
	 * {@link #setBackgroundImage(WResource resource, WCssDecorationStyle.Repeat repeat, EnumSet sides)
	 * setBackgroundImage(resource, WCssDecorationStyle.Repeat.RepeatXY,
	 * EnumSet.noneOf(Side.class))}
	 */
	public final void setBackgroundImage(WResource resource) {
		setBackgroundImage(resource, WCssDecorationStyle.Repeat.RepeatXY,
				EnumSet.noneOf(Side.class));
	}

	/**
	 * Sets a background image URL.
	 * <p>
	 * Calls
	 * {@link #setBackgroundImage(WResource resource, WCssDecorationStyle.Repeat repeat, EnumSet sides)
	 * setBackgroundImage(resource, repeat, EnumSet.noneOf(Side.class))}
	 */
	public final void setBackgroundImage(WResource resource,
			WCssDecorationStyle.Repeat repeat) {
		setBackgroundImage(resource, repeat, EnumSet.noneOf(Side.class));
	}

	/**
	 * Returns the background image URL.
	 */
	public String getBackgroundImage() {
		return this.backgroundImage_;
	}

	/**
	 * Returns the background image repeat.
	 */
	public WCssDecorationStyle.Repeat getBackgroundImageRepeat() {
		return this.backgroundImageRepeat_;
	}

	/**
	 * Sets the foreground color.
	 */
	public void setForegroundColor(WColor color) {
		if (!WWebWidget.canOptimizeUpdates()
				|| !this.foregroundColor_.equals(color)) {
			this.foregroundColor_ = color;
			this.foregroundColorChanged_ = true;
			this.changed();
		}
	}

	/**
	 * Returns the foreground color.
	 */
	public WColor getForegroundColor() {
		return this.foregroundColor_;
	}

	/**
	 * Sets the border style.
	 * <p>
	 * A border may be placed in a particular location by specifying sides by
	 * OR&apos;ing WWidget::Side values together, e.g. (Right | Top).
	 */
	public void setBorder(WBorder border, EnumSet<Side> sides) {
		if (!WWebWidget.canOptimizeUpdates() || !this.border_.equals(border)
				|| !this.borderPosition_.equals(sides)) {
			this.border_ = border;
			this.borderPosition_ = EnumSet.copyOf(sides);
			this.borderChanged_ = true;
			this.changed();
		}
	}

	/**
	 * Sets the border style.
	 * <p>
	 * Calls {@link #setBorder(WBorder border, EnumSet sides) setBorder(border,
	 * EnumSet.of(side, sides))}
	 */
	public final void setBorder(WBorder border, Side side, Side... sides) {
		setBorder(border, EnumSet.of(side, sides));
	}

	/**
	 * Sets the border style.
	 * <p>
	 * Calls {@link #setBorder(WBorder border, EnumSet sides) setBorder(border,
	 * Side.All)}
	 */
	public final void setBorder(WBorder border) {
		setBorder(border, Side.All);
	}

	/**
	 * Returns the border style.
	 */
	public WBorder getBorder() {
		return this.border_;
	}

	/**
	 * Sets the font.
	 */
	public void setFont(WFont font) {
		if (!WWebWidget.canOptimizeUpdates() || !this.font_.equals(font)) {
			this.font_ = font;
			this.fontChanged_ = true;
			this.changed();
		}
	}

	/**
	 * Returns a reference to the font.
	 */
	public WFont getFont() {
		return this.font_;
	}

	/**
	 * Sets the text decoration options.
	 * <p>
	 * You may logically or together any of the options of the TextDecoration
	 * enumeration.
	 * <p>
	 * The default is 0.
	 */
	public void setTextDecoration(
			EnumSet<WCssDecorationStyle.TextDecoration> options) {
		if (!WWebWidget.canOptimizeUpdates()
				|| !this.textDecoration_.equals(options)) {
			this.textDecoration_ = EnumSet.copyOf(options);
			this.textDecorationChanged_ = true;
			this.changed();
		}
	}

	/**
	 * Sets the text decoration options.
	 * <p>
	 * Calls {@link #setTextDecoration(EnumSet options)
	 * setTextDecoration(EnumSet.of(option, options))}
	 */
	public final void setTextDecoration(
			WCssDecorationStyle.TextDecoration option,
			WCssDecorationStyle.TextDecoration... options) {
		setTextDecoration(EnumSet.of(option, options));
	}

	/**
	 * Returns the text decoration options.
	 */
	public EnumSet<WCssDecorationStyle.TextDecoration> getTextDecoration() {
		return this.textDecoration_;
	}

	String getCssText() {
		DomElement e = new DomElement(DomElement.Mode.ModeCreate,
				DomElementType.DomElement_A);
		this.updateDomElement(e, true);
		return e.getCssStyle();
	}

	void updateDomElement(DomElement element, boolean all) {
		if (this.cursorChanged_ || all) {
			switch (this.cursor_) {
			case AutoCursor:
				if (this.cursorChanged_) {
					element.setProperty(Property.PropertyStyleCursor, "auto");
				}
				break;
			case ArrowCursor:
				element.setProperty(Property.PropertyStyleCursor, "default");
				break;
			case CrossCursor:
				element.setProperty(Property.PropertyStyleCursor, "crosshair");
				break;
			case PointingHandCursor:
				element.setProperty(Property.PropertyStyleCursor, "pointer");
				break;
			case OpenHandCursor:
				element.setProperty(Property.PropertyStyleCursor, "move");
				break;
			case WaitCursor:
				element.setProperty(Property.PropertyStyleCursor, "wait");
				break;
			case IBeamCursor:
				element.setProperty(Property.PropertyStyleCursor, "text");
				break;
			case WhatsThisCursor:
				element.setProperty(Property.PropertyStyleCursor, "help");
				break;
			}
			if (this.cursorImage_.length() != 0) {
				element.setProperty(Property.PropertyStyleCursor, "url("
						+ this.cursorImage_ + "),"
						+ element.getProperty(Property.PropertyStyleCursor));
			}
			this.cursorChanged_ = false;
		}
		this.font_.updateDomElement(element, this.fontChanged_, all);
		this.fontChanged_ = false;
		if (this.borderChanged_ || all) {
			boolean elementHasDefaultBorder = element.getType() == DomElementType.DomElement_IFRAME
					|| element.getType() == DomElementType.DomElement_INPUT
					|| element.getType() == DomElementType.DomElement_SELECT
					|| element.getType() == DomElementType.DomElement_TEXTAREA;
			if (this.borderChanged_ || elementHasDefaultBorder
					|| this.border_.getStyle() != WBorder.Style.None) {
				if (!EnumUtils.mask(this.borderPosition_, Side.Top).isEmpty()) {
					element.setProperty(Property.PropertyStyleBorderTop,
							this.border_.getCssText());
				}
				if (!EnumUtils.mask(this.borderPosition_, Side.Left).isEmpty()) {
					element.setProperty(Property.PropertyStyleBorderLeft,
							this.border_.getCssText());
				}
				if (!EnumUtils.mask(this.borderPosition_, Side.Right).isEmpty()) {
					element.setProperty(Property.PropertyStyleBorderRight,
							this.border_.getCssText());
				}
				if (!EnumUtils.mask(this.borderPosition_, Side.Bottom)
						.isEmpty()) {
					element.setProperty(Property.PropertyStyleBorderBottom,
							this.border_.getCssText());
				}
			}
			this.borderChanged_ = false;
		}
		if (this.foregroundColorChanged_ || all) {
			if (all && !this.foregroundColor_.isDefault()
					|| this.foregroundColorChanged_) {
				element.setProperty(Property.PropertyStyleColor,
						this.foregroundColor_.getCssText());
			}
			this.foregroundColorChanged_ = false;
		}
		if (this.backgroundColorChanged_ || all) {
			if (all && !this.backgroundColor_.isDefault()
					|| this.backgroundColorChanged_) {
				element.setProperty(Property.PropertyStyleBackgroundColor,
						this.backgroundColor_.getCssText());
			}
			this.backgroundColorChanged_ = false;
		}
		if (this.backgroundImageChanged_ || all) {
			if (this.backgroundImage_.length() != 0
					|| this.backgroundImageChanged_) {
				element.setProperty(Property.PropertyStyleBackgroundImage,
						this.backgroundImage_.length() > 0 ? "url("
								+ WApplication.getInstance().fixRelativeUrl(
										this.backgroundImage_) + ")" : "none");
				switch (this.backgroundImageRepeat_) {
				case RepeatXY:
					element.setProperty(Property.PropertyStyleBackgroundRepeat,
							"repeat");
					break;
				case RepeatX:
					element.setProperty(Property.PropertyStyleBackgroundRepeat,
							"repeat-x");
					break;
				case RepeatY:
					element.setProperty(Property.PropertyStyleBackgroundRepeat,
							"repeat-y");
					break;
				case NoRepeat:
					element.setProperty(Property.PropertyStyleBackgroundRepeat,
							"no-repeat");
					break;
				}
				if (!this.backgroundImageLocation_.isEmpty()) {
					String location = "";
					if (!EnumUtils.mask(this.backgroundImageLocation_,
							Side.CenterY).isEmpty()) {
						location += " center";
					} else {
						if (!EnumUtils.mask(this.backgroundImageLocation_,
								Side.Bottom).isEmpty()) {
							location += " bottom";
						} else {
							location += " top";
						}
					}
					if (!EnumUtils.mask(this.backgroundImageLocation_,
							Side.CenterX).isEmpty()) {
						location += " center";
					} else {
						if (!EnumUtils.mask(this.backgroundImageLocation_,
								Side.Right).isEmpty()) {
							location += " right";
						} else {
							location += " left";
						}
					}
					element.setProperty(
							Property.PropertyStyleBackgroundPosition, location);
				}
			}
			this.backgroundImageChanged_ = false;
		}
		if (this.textDecorationChanged_ || all) {
			String options = "";
			if (!EnumUtils.mask(this.textDecoration_,
					WCssDecorationStyle.TextDecoration.Underline).isEmpty()) {
				options += " underline";
			}
			if (!EnumUtils.mask(this.textDecoration_,
					WCssDecorationStyle.TextDecoration.Overline).isEmpty()) {
				options += " overline";
			}
			if (!EnumUtils.mask(this.textDecoration_,
					WCssDecorationStyle.TextDecoration.LineThrough).isEmpty()) {
				options += " line-through";
			}
			if (!EnumUtils.mask(this.textDecoration_,
					WCssDecorationStyle.TextDecoration.Blink).isEmpty()) {
				options += " blink";
			}
			if (options.length() != 0 || this.textDecorationChanged_) {
				element.setProperty(Property.PropertyStyleTextDecoration,
						options);
			}
			this.textDecorationChanged_ = false;
		}
	}

	private WWebWidget widget_;
	private Cursor cursor_;
	private String cursorImage_;
	private WBorder border_;
	private WColor backgroundColor_;
	private WColor foregroundColor_;
	private String backgroundImage_;
	private WResource backgroundImageResource_;
	private WCssDecorationStyle.Repeat backgroundImageRepeat_;
	private EnumSet<Side> backgroundImageLocation_;
	private WFont font_;
	private EnumSet<WCssDecorationStyle.TextDecoration> textDecoration_;
	private EnumSet<Side> borderPosition_;
	private boolean cursorChanged_;
	private boolean borderChanged_;
	private boolean foregroundColorChanged_;
	private boolean backgroundColorChanged_;
	private boolean backgroundImageChanged_;
	private boolean fontChanged_;
	private boolean textDecorationChanged_;

	private void changed() {
		if (this.widget_ != null) {
			this.widget_.repaint(EnumSet
					.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	private void backgroundImageResourceChanged() {
		if (this.backgroundImageResource_ != null) {
			this.setBackgroundImage(this.backgroundImageResource_.getUrl(),
					this.backgroundImageRepeat_, this.backgroundImageLocation_);
		}
	}

	void setWebWidget(WWebWidget w) {
		this.widget_ = w;
		this.font_.setWebWidget(w);
	}
}
