/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A style class for a single widget or style sheet rule.
 *
 * <p>You can manipulate the decoration style of a single widget using {@link
 * WWidget#getDecorationStyle()} or you can use a {@link WCssDecorationStyle} to add a rule to the
 * inline style sheet using {@link WCssStyleSheet#addRule(String selector, WCssDecorationStyle
 * style, String ruleName) WCssStyleSheet#addRule()}.
 */
public class WCssDecorationStyle extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WCssDecorationStyle.class);

  /** Creates a default style. */
  public WCssDecorationStyle() {
    super();
    this.widget_ = null;
    this.cursor_ = Cursor.Auto;
    this.cursorImage_ = "";
    this.backgroundColor_ = new WColor();
    this.foregroundColor_ = new WColor();
    this.backgroundImage_ = new WLink();
    this.backgroundImageRepeat_ = EnumSet.of(Orientation.Horizontal, Orientation.Vertical);
    this.backgroundImageLocation_ = EnumSet.noneOf(Side.class);
    this.font_ = new WFont();
    this.textDecoration_ = EnumSet.noneOf(TextDecoration.class);
    this.cursorChanged_ = false;
    this.borderChanged_ = false;
    this.foregroundColorChanged_ = false;
    this.backgroundColorChanged_ = false;
    this.backgroundImageChanged_ = false;
    this.fontChanged_ = false;
    this.textDecorationChanged_ = false;
  }
  /** Copy constructor. */
  public WCssDecorationStyle(final WCssDecorationStyle other) {
    super();
    this.widget_ = null;
    this.cursor_ = Cursor.Auto;
    this.cursorImage_ = "";
    this.backgroundColor_ = new WColor();
    this.foregroundColor_ = new WColor();
    this.backgroundImage_ = new WLink();
    this.backgroundImageRepeat_ = EnumSet.of(Orientation.Horizontal, Orientation.Vertical);
    this.backgroundImageLocation_ = EnumSet.noneOf(Side.class);
    this.font_ = new WFont();
    this.textDecoration_ = EnumSet.noneOf(TextDecoration.class);
    this.cursorChanged_ = false;
    this.borderChanged_ = false;
    this.foregroundColorChanged_ = false;
    this.backgroundColorChanged_ = false;
    this.backgroundImageChanged_ = false;
    this.fontChanged_ = false;
    this.textDecorationChanged_ = false;
    this.copy(other);
  }
  /** Sets the cursor style. */
  public void setCursor(Cursor c) {
    if (!WWebWidget.canOptimizeUpdates() || this.cursorImage_.length() != 0 || this.cursor_ != c) {
      this.cursorImage_ = "";
      this.cursor_ = c;
      this.cursorChanged_ = true;
      this.changed();
    }
  }
  /**
   * Returns the cursor style.
   *
   * <p>
   *
   * @see WCssDecorationStyle#setCursor(Cursor c)
   */
  public Cursor getCursor() {
    return this.cursor_;
  }
  /**
   * Sets a custom cursor image Url.
   *
   * <p>The Url should point to a .cur file (this shoul be a real .cur file, renaming an .ico is not
   * enough for Internet Explorer).
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
   * Sets a custom cursor image Url.
   *
   * <p>Calls {@link #setCursor(String cursorImage, Cursor fallback) setCursor(cursorImage,
   * Cursor.Arrow)}
   */
  public final void setCursor(String cursorImage) {
    setCursor(cursorImage, Cursor.Arrow);
  }
  /**
   * Returns the cursor image.
   *
   * <p>
   *
   * @see WCssDecorationStyle#setCursor(Cursor c)
   */
  public String getCursorImage() {
    return this.cursorImage_;
  }
  /** Sets the background color. */
  public void setBackgroundColor(WColor color) {
    if (!WWebWidget.canOptimizeUpdates() || !this.backgroundColor_.equals(color)) {
      this.backgroundColorChanged_ = true;
      this.backgroundColor_ = color;
      this.changed();
    }
  }
  /**
   * Returns the background color.
   *
   * <p>
   *
   * @see WCssDecorationStyle#setBackgroundColor(WColor color)
   */
  public WColor getBackgroundColor() {
    return this.backgroundColor_;
  }
  /**
   * Sets a background image.
   *
   * <p>The <code>link</code> may be a URL or a resource.
   *
   * <p>The image may be placed in a particular location by specifying sides by OR&apos;ing {@link
   * Side} values together, e.g. ({@link Side#Right} | {@link Side#Top}).
   */
  public void setBackgroundImage(
      final WLink image, EnumSet<Orientation> repeat, EnumSet<Side> sides) {
    if (image.getType() == LinkType.Resource) {
      image
          .getResource()
          .dataChanged()
          .addListener(
              this,
              () -> {
                WCssDecorationStyle.this.backgroundImageResourceChanged();
              });
    }
    if (!WWebWidget.canOptimizeUpdates()
        || !this.backgroundImage_.equals(image)
        || !this.backgroundImageRepeat_.equals(repeat)
        || !this.backgroundImageLocation_.equals(sides)) {
      this.backgroundImage_ = image;
      this.backgroundImageRepeat_ = EnumSet.copyOf(repeat);
      this.backgroundImageLocation_ = EnumSet.copyOf(sides);
      this.backgroundImageChanged_ = true;
      this.changed();
    }
  }
  /**
   * Sets a background image.
   *
   * <p>Calls {@link #setBackgroundImage(WLink image, EnumSet repeat, EnumSet sides)
   * setBackgroundImage(image, repeat, EnumSet.of(side, sides))}
   */
  public final void setBackgroundImage(
      final WLink image, EnumSet<Orientation> repeat, Side side, Side... sides) {
    setBackgroundImage(image, repeat, EnumSet.of(side, sides));
  }
  /**
   * Sets a background image.
   *
   * <p>Calls {@link #setBackgroundImage(WLink image, EnumSet repeat, EnumSet sides)
   * setBackgroundImage(image, EnumSet.of (Orientation.Horizontal, Orientation.Vertical),
   * EnumSet.noneOf(Side.class))}
   */
  public final void setBackgroundImage(final WLink image) {
    setBackgroundImage(
        image,
        EnumSet.of(Orientation.Horizontal, Orientation.Vertical),
        EnumSet.noneOf(Side.class));
  }
  /**
   * Sets a background image.
   *
   * <p>Calls {@link #setBackgroundImage(WLink image, EnumSet repeat, EnumSet sides)
   * setBackgroundImage(image, repeat, EnumSet.noneOf(Side.class))}
   */
  public final void setBackgroundImage(final WLink image, EnumSet<Orientation> repeat) {
    setBackgroundImage(image, repeat, EnumSet.noneOf(Side.class));
  }
  /**
   * Sets a background image.
   *
   * <p>The image may be placed in a particular location by specifying sides by OR&apos;ing {@link
   * Side} values together, e.g. ({@link Side#Right} | {@link Side#Top}).
   */
  public void setBackgroundImage(
      final String url, EnumSet<Orientation> repeat, EnumSet<Side> sides) {
    this.setBackgroundImage(new WLink(url), repeat, sides);
  }
  /**
   * Sets a background image.
   *
   * <p>Calls {@link #setBackgroundImage(String url, EnumSet repeat, EnumSet sides)
   * setBackgroundImage(url, repeat, EnumSet.of(side, sides))}
   */
  public final void setBackgroundImage(
      final String url, EnumSet<Orientation> repeat, Side side, Side... sides) {
    setBackgroundImage(url, repeat, EnumSet.of(side, sides));
  }
  /**
   * Sets a background image.
   *
   * <p>Calls {@link #setBackgroundImage(String url, EnumSet repeat, EnumSet sides)
   * setBackgroundImage(url, EnumSet.of (Orientation.Horizontal, Orientation.Vertical),
   * EnumSet.noneOf(Side.class))}
   */
  public final void setBackgroundImage(final String url) {
    setBackgroundImage(
        url, EnumSet.of(Orientation.Horizontal, Orientation.Vertical), EnumSet.noneOf(Side.class));
  }
  /**
   * Sets a background image.
   *
   * <p>Calls {@link #setBackgroundImage(String url, EnumSet repeat, EnumSet sides)
   * setBackgroundImage(url, repeat, EnumSet.noneOf(Side.class))}
   */
  public final void setBackgroundImage(final String url, EnumSet<Orientation> repeat) {
    setBackgroundImage(url, repeat, EnumSet.noneOf(Side.class));
  }
  /**
   * Returns the background image URL.
   *
   * <p>
   *
   * @see WCssDecorationStyle#setBackgroundImage(WLink image, EnumSet repeat, EnumSet sides)
   */
  public String getBackgroundImage() {
    return this.backgroundImage_.getUrl();
  }
  /**
   * Returns the background image repeat.
   *
   * <p>
   *
   * @see WCssDecorationStyle#setBackgroundImage(WLink image, EnumSet repeat, EnumSet sides)
   */
  public EnumSet<Orientation> getBackgroundImageRepeat() {
    return this.backgroundImageRepeat_;
  }
  /** Sets the text color. */
  public void setForegroundColor(WColor color) {
    if (!WWebWidget.canOptimizeUpdates() || !this.foregroundColor_.equals(color)) {
      this.foregroundColor_ = color;
      this.foregroundColorChanged_ = true;
      this.changed();
    }
  }
  /**
   * Returns the text color.
   *
   * <p>
   *
   * @see WCssDecorationStyle#setForegroundColor(WColor color)
   */
  public WColor getForegroundColor() {
    return this.foregroundColor_;
  }
  /**
   * Sets the border style.
   *
   * <p>The given <code>border</code> will be set for the specified <code>sides</code>.
   *
   * <p>A different border style may be specified for each of the four sides.
   */
  public void setBorder(WBorder border, EnumSet<Side> sides) {
    Side[] theSides = {Side.Top, Side.Right, Side.Bottom, Side.Left};
    for (int i = 0; i < 4; ++i) {
      if (sides.contains(theSides[i])) {
        this.border_[i] = border.clone();
      }
      this.borderChanged_ = true;
    }
    if (this.borderChanged_) {
      this.changed(EnumSet.of(RepaintFlag.SizeAffected));
    }
  }
  /**
   * Sets the border style.
   *
   * <p>Calls {@link #setBorder(WBorder border, EnumSet sides) setBorder(border, EnumSet.of(side,
   * sides))}
   */
  public final void setBorder(WBorder border, Side side, Side... sides) {
    setBorder(border, EnumSet.of(side, sides));
  }
  /**
   * Sets the border style.
   *
   * <p>Calls {@link #setBorder(WBorder border, EnumSet sides) setBorder(border, Side.AllSides)}
   */
  public final void setBorder(WBorder border) {
    setBorder(border, Side.AllSides);
  }
  /**
   * Returns the border style.
   *
   * <p>Returns the border style set using {@link WCssDecorationStyle#setBorder(WBorder border,
   * EnumSet sides) setBorder()} for the given <code>side</code>.
   *
   * <p>
   *
   * @see WCssDecorationStyle#setBorder(WBorder border, EnumSet sides)
   *     <p><i><b>Note: </b>Prior to version 3.1.9 it was not possible to pass a side and only one
   *     border could be configured. </i>
   */
  public WBorder getBorder(Side side) {
    switch (side) {
      case Top:
        return this.borderI(0);
      case Right:
        return this.borderI(1);
      case Bottom:
        return this.borderI(2);
      case Left:
        return this.borderI(3);
      default:
        break;
    }
    return new WBorder();
  }
  /**
   * Returns the border style.
   *
   * <p>Returns {@link #getBorder(Side side) getBorder(Side.Top)}
   */
  public final WBorder getBorder() {
    return getBorder(Side.Top);
  }
  /** Sets the text font. */
  public void setFont(final WFont font) {
    if (!WWebWidget.canOptimizeUpdates() || !this.font_.equals(font)) {
      this.font_ = font;
      this.fontChanged_ = true;
      this.changed(EnumSet.of(RepaintFlag.SizeAffected));
    }
  }
  /**
   * Returns the font.
   *
   * <p>
   *
   * @see WCssDecorationStyle#setFont(WFont font)
   */
  public WFont getFont() {
    return this.font_;
  }
  /**
   * Sets text decoration options.
   *
   * <p>You may logically or together any of the options of the TextDecoration enumeration.
   *
   * <p>The default is 0.
   */
  public void setTextDecoration(EnumSet<TextDecoration> options) {
    if (!WWebWidget.canOptimizeUpdates() || !this.textDecoration_.equals(options)) {
      this.textDecoration_ = EnumSet.copyOf(options);
      this.textDecorationChanged_ = true;
      this.changed();
    }
  }
  /**
   * Sets text decoration options.
   *
   * <p>Calls {@link #setTextDecoration(EnumSet options) setTextDecoration(EnumSet.of(option,
   * options))}
   */
  public final void setTextDecoration(TextDecoration option, TextDecoration... options) {
    setTextDecoration(EnumSet.of(option, options));
  }
  /**
   * Returns the text decoration options.
   *
   * <p>
   *
   * @see WCssDecorationStyle#setTextDecoration(EnumSet options)
   */
  public EnumSet<TextDecoration> getTextDecoration() {
    return this.textDecoration_;
  }

  String getCssText() {
    DomElement e = new DomElement(DomElement.Mode.Create, DomElementType.A);
    this.updateDomElement(e, true);
    return e.getCssStyle();
  }

  void updateDomElement(final DomElement element, boolean all) {
    if (this.cursorChanged_ || all) {
      switch (this.cursor_) {
        case Auto:
          if (this.cursorChanged_) {
            element.setProperty(Property.StyleCursor, "auto");
          }
          break;
        case Arrow:
          element.setProperty(Property.StyleCursor, "default");
          break;
        case Cross:
          element.setProperty(Property.StyleCursor, "crosshair");
          break;
        case PointingHand:
          element.setProperty(Property.StyleCursor, "pointer");
          break;
        case OpenHand:
          element.setProperty(Property.StyleCursor, "move");
          break;
        case Wait:
          element.setProperty(Property.StyleCursor, "wait");
          break;
        case IBeam:
          element.setProperty(Property.StyleCursor, "text");
          break;
        case WhatsThis:
          element.setProperty(Property.StyleCursor, "help");
          break;
      }
      if (this.cursorImage_.length() != 0) {
        element.setProperty(
            Property.StyleCursor,
            "url(" + this.cursorImage_ + ")," + element.getProperty(Property.StyleCursor));
      }
      this.cursorChanged_ = false;
    }
    this.font_.updateDomElement(element, this.fontChanged_, all);
    this.fontChanged_ = false;
    Property[] properties = {
      Property.StyleBorderTop,
      Property.StyleBorderRight,
      Property.StyleBorderBottom,
      Property.StyleBorderLeft
    };
    if (this.borderChanged_ || all) {
      for (int i = 0; i < 4; ++i) {
        if (this.border_[i] != null) {
          element.setProperty(properties[i], this.border_[i].getCssText());
        } else {
          if (this.borderChanged_) {
            element.setProperty(properties[i], "");
          }
        }
      }
      this.borderChanged_ = false;
    }
    if (this.foregroundColorChanged_ || all) {
      if (all && !this.foregroundColor_.isDefault() || this.foregroundColorChanged_) {
        element.setProperty(Property.StyleColor, this.foregroundColor_.getCssText());
      }
      this.foregroundColorChanged_ = false;
    }
    if (this.backgroundColorChanged_ || all) {
      if (all && !this.backgroundColor_.isDefault() || this.backgroundColorChanged_) {
        element.setProperty(Property.StyleBackgroundColor, this.backgroundColor_.getCssText());
      }
      this.backgroundColorChanged_ = false;
    }
    if (this.backgroundImageChanged_ || all) {
      if (!this.backgroundImage_.isNull() || this.backgroundImageChanged_) {
        if (this.backgroundImage_.isNull()) {
          element.setProperty(Property.StyleBackgroundImage, "none");
        } else {
          WApplication app = WApplication.getInstance();
          String url =
              app.encodeUntrustedUrl(app.resolveRelativeUrl(this.backgroundImage_.getUrl()));
          element.setProperty(
              Property.StyleBackgroundImage, "url(" + WWebWidget.jsStringLiteral(url, '"') + ")");
        }
        if (!this.backgroundImageRepeat_.equals(
                EnumSet.of(Orientation.Horizontal, Orientation.Vertical))
            || !this.backgroundImageLocation_.isEmpty()) {
          if (this.backgroundImageRepeat_.equals(
              EnumSet.of(Orientation.Horizontal, Orientation.Vertical))) {
            element.setProperty(Property.StyleBackgroundRepeat, "repeat");
          } else {
            if (this.backgroundImageRepeat_.equals(Orientation.Horizontal)) {
              element.setProperty(Property.StyleBackgroundRepeat, "repeat-x");
            } else {
              if (this.backgroundImageRepeat_.equals(Orientation.Vertical)) {
                element.setProperty(Property.StyleBackgroundRepeat, "repeat-y");
              } else {
                element.setProperty(Property.StyleBackgroundRepeat, "no-repeat");
              }
            }
          }
          if (!this.backgroundImageLocation_.isEmpty()) {
            String location = "";
            if (this.backgroundImageLocation_.contains(Side.CenterY)) {
              location += " center";
            } else {
              if (this.backgroundImageLocation_.contains(Side.Bottom)) {
                location += " bottom";
              } else {
                location += " top";
              }
            }
            if (this.backgroundImageLocation_.contains(Side.CenterX)) {
              location += " center";
            } else {
              if (this.backgroundImageLocation_.contains(Side.Right)) {
                location += " right";
              } else {
                location += " left";
              }
            }
            element.setProperty(Property.StyleBackgroundPosition, location);
          }
        }
      }
      this.backgroundImageChanged_ = false;
    }
    if (this.textDecorationChanged_ || all) {
      String options = "";
      if (!this.textDecoration_.isEmpty()) {
        if (this.textDecoration_.contains(TextDecoration.Underline)) {
          options += " underline";
        }
        if (this.textDecoration_.contains(TextDecoration.Overline)) {
          options += " overline";
        }
        if (this.textDecoration_.contains(TextDecoration.LineThrough)) {
          options += " line-through";
        }
        if (this.textDecoration_.contains(TextDecoration.Blink)) {
          options += " blink";
        }
        if (this.textDecoration_.contains(TextDecoration.None)) {
          options += " none";
        }
      }
      if (options.length() != 0 || this.textDecorationChanged_) {
        element.setProperty(Property.StyleTextDecoration, options);
      }
      this.textDecorationChanged_ = false;
    }
  }

  private WWebWidget widget_;
  private Cursor cursor_;
  private String cursorImage_;
  private WBorder[] border_ = new WBorder[4];
  private WColor backgroundColor_;
  private WColor foregroundColor_;
  private WLink backgroundImage_;
  private EnumSet<Orientation> backgroundImageRepeat_;
  private EnumSet<Side> backgroundImageLocation_;
  private WFont font_;
  private EnumSet<TextDecoration> textDecoration_;
  private boolean cursorChanged_;
  private boolean borderChanged_;
  private boolean foregroundColorChanged_;
  private boolean backgroundColorChanged_;
  private boolean backgroundImageChanged_;
  private boolean fontChanged_;
  private boolean textDecorationChanged_;

  private void changed(EnumSet<RepaintFlag> flags) {
    if (this.widget_ != null) {
      this.widget_.repaint(flags);
    }
  }

  private final void changed(RepaintFlag flag, RepaintFlag... flags) {
    changed(EnumSet.of(flag, flags));
  }

  private final void changed() {
    changed(EnumSet.noneOf(RepaintFlag.class));
  }

  private void backgroundImageResourceChanged() {
    if (this.backgroundImage_.getType() == LinkType.Resource) {
      this.backgroundImageChanged_ = true;
      this.changed();
    }
  }

  private WBorder borderI(int i) {
    if (this.border_[i] != null) {
      return this.border_[i];
    } else {
      return new WBorder();
    }
  }

  private void copy(final WCssDecorationStyle other) {
    if (this == other) {
      return;
    }
    this.setCursor(other.cursor_);
    this.setBackgroundColor(other.getBackgroundColor());
    this.setBackgroundImage(
        other.getBackgroundImage(),
        other.getBackgroundImageRepeat(),
        other.backgroundImageLocation_);
    this.setForegroundColor(other.getForegroundColor());
    for (int i = 0; i < 4; ++i) {
      if (other.border_[i] != null) {
        this.border_[i] = other.border_[i].clone();
      } else {
        this.border_[i] = (WBorder) null;
      }
    }
    this.borderChanged_ = true;
    this.setFont(other.font_);
    this.setTextDecoration(other.getTextDecoration());
  }

  void setWebWidget(WWebWidget w) {
    this.widget_ = w;
    this.font_.setWebWidget(w);
  }
}
