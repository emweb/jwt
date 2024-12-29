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
 * A widget that provides a browser-native color picker.
 *
 * <p>To act upon color value changes, connect a slot to the {@link WFormWidget#changed()} signal.
 * This signal is emitted when the user changes the selected color, and subsequently closes the
 * color picker.
 *
 * <p>To act upon any color change, connect a slot to the {@link WColorPicker#colorInput()
 * colorInput()} signal. Note that this signal may fire very quickly depending on how the
 * browser&apos;s color picker works.
 *
 * <p>At all times, the currently selected color may be accessed with the value() method.
 *
 * <p>The widget corresponds to the HTML <code>&lt;input type=&quot;color&quot;&gt;</code> tag. Note
 * that this element does not support CSS color names. When manipulating this widget with {@link
 * WColor} values, ensure they have valid RGB values or the color picker will reset to #000000.
 *
 * <p>WColorPicker is an {@link WWidget#setInline(boolean inlined) inline } widget.
 *
 * <p>
 *
 * @see WColor
 */
public class WColorPicker extends WFormWidget {
  private static Logger logger = LoggerFactory.getLogger(WColorPicker.class);

  /** Creates a color picker with the default color of black (#000000). */
  public WColorPicker(WContainerWidget parentContainer) {
    super();
    this.color_ = new WColor(StandardColor.Black);
    this.colorChanged_ = false;
    this.setInline(true);
    this.setFormObject(true);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a color picker with the default color of black (#000000).
   *
   * <p>Calls {@link #WColorPicker(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WColorPicker() {
    this((WContainerWidget) null);
  }
  /**
   * Creates a color picker with the given color value. Ensure the color has valid RGB values, or
   * the color will be reset to #000000.
   */
  public WColorPicker(final WColor color, WContainerWidget parentContainer) {
    super();
    this.color_ = color;
    this.colorChanged_ = false;
    this.setInline(true);
    this.setFormObject(true);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a color picker with the given color value. Ensure the color has valid RGB values, or
   * the color will be reset to #000000.
   *
   * <p>Calls {@link #WColorPicker(WColor color, WContainerWidget parentContainer) this(color,
   * (WContainerWidget)null)}
   */
  public WColorPicker(final WColor color) {
    this(color, (WContainerWidget) null);
  }
  /**
   * Returns the current value of the color picker as a {@link WColor} object.
   *
   * <p>
   *
   * @see WColorPicker#setColor(WColor value)
   */
  public WColor getColor() {
    return this.color_;
  }
  /**
   * Sets the selected color.
   *
   * <p>The default value is #000000 (black).
   *
   * <p>Ensure the color has valid RGB values, or the color will be reset to #000000.
   *
   * <p>
   *
   * @see WColorPicker#getColor()
   */
  public void setColor(final WColor value) {
    if (!value.equals(this.color_)) {
      this.color_ = value;
      this.colorChanged_ = true;
      this.repaint();
    }
  }
  /**
   * Event signal emitted when the selected color is changed.
   *
   * <p>This signal is emitted whenever the selected color has changed. Unlike the {@link
   * WFormWidget#changed()} signal, this signal is fired on every change, not only when the color
   * picker is closed.
   *
   * <p>In particular, on browsers with a draggable color picker (i.e. most common browsers), this
   * signal fires every time the position changes. Use with caution.
   *
   * <p>
   *
   * @see WFormWidget#changed()
   */
  public EventSignal colorInput() {
    return this.voidEventSignal(INPUT_SIGNAL, true);
  }
  /**
   * Returns the current value of the color picker as a string.
   *
   * <p>This is implemented as
   *
   * <pre>{@code
   * return color().cssText();
   *
   * }</pre>
   */
  public String getValueText() {
    return this.getColor().getCssText(false);
  }
  /**
   * Sets the current value of the color picker as a string. The string must be in a format from
   * which {@link WColor} can determine RGB values (i.e. not a CSS color name), or the value will be
   * set to #000000.
   *
   * <p>This is implemented as
   *
   * <pre>{@code
   * setColor(WColor(value));
   *
   * }</pre>
   *
   * <p>
   */
  public void setValueText(final String value) {
    this.setColor(new WColor(value));
  }

  private static String INPUT_SIGNAL = "input";
  private WColor color_;
  private boolean colorChanged_;

  void updateDom(final DomElement element, boolean all) {
    if (all) {
      element.setAttribute("type", "color");
    }
    if (this.colorChanged_ || all) {
      element.setProperty(Property.Value, CssUtils.colorToHex(this.color_));
      this.colorChanged_ = false;
    }
    super.updateDom(element, all);
  }

  DomElementType getDomElementType() {
    return DomElementType.INPUT;
  }

  void propagateRenderOk(boolean deep) {
    this.colorChanged_ = false;
  }

  protected void setFormData(final WObject.FormData formData) {
    if (this.colorChanged_ || this.isReadOnly()) {
      return;
    }
    if (!(formData.values.length == 0)) {
      final String value = formData.values[0];
      this.color_ = new WColor(value);
    }
  }
}
