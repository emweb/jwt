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
 * A progress bar.
 *
 * <p>The progress bar can be used to indicate the progress of a certain operation. The text
 * displayed in the progress bar can be customized by specializing {@link WProgressBar#getText()
 * getText()}.
 *
 * <p>To use the progress bar, you need to give it a range (minimum and maximum value), and update
 * the progress using {@link WProgressBar#setValue(double value) setValue()}.
 *
 * <p>WProgressBar is an {@link WWidget#setInline(boolean inlined) inline } widget.
 *
 * <p>
 *
 * <p><i><b>Note: </b>With the advent of HTML5, this widget will be implemented using the native
 * HTML5 control when available. </i>
 */
public class WProgressBar extends WInteractWidget {
  private static Logger logger = LoggerFactory.getLogger(WProgressBar.class);

  /** Creates a progress bar. */
  public WProgressBar(WContainerWidget parentContainer) {
    super();
    this.min_ = 0;
    this.max_ = 100;
    this.value_ = 0;
    this.format_ = new WString();
    this.changed_ = false;
    this.valueStyleClassChanged_ = false;
    this.valueStyleClass_ = "";
    this.valueChanged_ = new Signal1<Double>();
    this.progressCompleted_ = new Signal();
    this.format_ = new WString("%.0f %%");
    this.setFlexBox(true);
    this.setInline(true);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a progress bar.
   *
   * <p>Calls {@link #WProgressBar(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WProgressBar() {
    this((WContainerWidget) null);
  }
  /**
   * Sets the minimum value.
   *
   * <p>The minimum value is the value that corresponds to 0%.
   *
   * <p>The default value is 0.
   */
  public void setMinimum(double minimum) {
    this.min_ = minimum;
    this.changed_ = true;
    this.repaint();
  }
  /**
   * Returns the minimum value.
   *
   * <p>
   *
   * @see WProgressBar#setMinimum(double minimum)
   */
  public double getMinimum() {
    return this.min_;
  }
  /**
   * Sets the maximum value.
   *
   * <p>The maximum value is the value that corresponds to 100%.
   *
   * <p>The default value is 100.
   */
  public void setMaximum(double maximum) {
    this.max_ = maximum;
    this.changed_ = true;
    this.repaint();
  }
  /**
   * Returns the maximum value.
   *
   * <p>
   *
   * @see WProgressBar#setMaximum(double maximum)
   */
  public double getMaximum() {
    return this.max_;
  }
  /**
   * Sets the range.
   *
   * <p>
   *
   * @see WProgressBar#setMinimum(double minimum)
   * @see WProgressBar#setMaximum(double maximum)
   */
  public void setRange(double minimum, double maximum) {
    this.min_ = minimum;
    this.max_ = maximum;
    this.changed_ = true;
    this.repaint();
  }
  /**
   * Sets the current progress.
   *
   * <p><code>value</code> must be a value between {@link WProgressBar#getMinimum() getMinimum()}
   * and {@link WProgressBar#getMaximum() getMaximum()}.
   */
  public void setValue(double value) {
    this.value_ = value;
    this.valueChanged_.trigger(this.value_);
    if (this.value_ == this.max_) {
      this.progressCompleted_.trigger();
    }
    this.changed_ = true;
    this.repaint();
  }
  /** Returns the current progress. */
  public double getValue() {
    return this.value_;
  }
  /**
   * Sets the progress format string.
   *
   * <p>The format is used by {@link WProgressBar#getText() getText()} to indicate the progress
   * value.
   *
   * <p>The default value is &quot;%.0f %%&quot;
   */
  public void setFormat(final CharSequence format) {
    this.format_ = WString.toWString(format);
  }
  /**
   * Returns the progress format string.
   *
   * <p>
   *
   * @see WProgressBar#setFormat(CharSequence format)
   */
  public WString getFormat() {
    return this.format_;
  }
  /**
   * Returns the text displayed inside the progress bar.
   *
   * <p>This text must be an XHTML formatted text fragment. The default text prints the current
   * progress using {@link WProgressBar#getFormat() getFormat()}. You may want to reimplement this
   * method to display a different text corresponding to the current {@link WProgressBar#getValue()
   * getValue()}.
   */
  public WString getText() {
    return StringUtils.formatFloat(this.format_, this.getPercentage());
  }
  /**
   * A signal that indicates when the value has changed.
   *
   * <p>This signal is emitted when {@link WProgressBar#setValue(double value) setValue()} is
   * called.
   *
   * <p>
   *
   * @see WProgressBar#setValue(double value)
   */
  public Signal1<Double> valueChanged() {
    return this.valueChanged_;
  }
  /**
   * A signal that indicates when 100% is reached.
   *
   * <p>This signal is emitted when setValue(maximum()) is called.
   *
   * <p>
   *
   * @see WProgressBar#setValue(double value)
   */
  public Signal progressCompleted() {
    return this.progressCompleted_;
  }

  public void resize(final WLength width, final WLength height) {
    super.resize(width, height);
    if (!height.isAuto()) {
      this.setAttributeValue("style", "line-height: " + height.getCssText());
    }
  }
  /**
   * Updates the style class of the bar part of the {@link WProgressBar}.
   *
   * <p>This can be used to style the bar part of the {@link WProgressBar}, e.g.
   * &quot;progress-bar-success&quot; or &quot;progress-bar-danger&quot;.
   */
  public void setValueStyleClass(final String valueStyleClass) {
    this.valueStyleClass_ = valueStyleClass;
    this.valueStyleClassChanged_ = true;
    this.repaint();
  }

  public void setState(double minimum, double maximum, double value) {
    this.min_ = minimum;
    this.max_ = maximum;
    if (this.value_ != value) {
      this.value_ = value;
      if (this.value_ == this.max_) {
        this.progressCompleted_.trigger();
      }
    }
  }
  /**
   * Update the progress bar itself.
   *
   * <p>Will be called whenever the value changes, and changes the width of the progress bar
   * accordingly.
   *
   * <p>You can reimplement this method to apply certain style changes to the progress bar according
   * to the value. Don&apos;t forget to call {@link WProgressBar#updateBar(DomElement bar)
   * updateBar()} if you still want the width to change.
   */
  protected void updateBar(final DomElement bar) {
    bar.setProperty(Property.StyleWidth, String.valueOf(this.getPercentage()) + "%");
  }

  void updateDom(final DomElement element, boolean all) {
    DomElement bar = null;
    DomElement label = null;
    WApplication app = WApplication.getInstance();
    WBootstrap5Theme bs5Theme = ObjectUtils.cast(app.getTheme(), WBootstrap5Theme.class);
    if (all) {
      bar = DomElement.createNew(DomElementType.DIV);
      bar.setId("bar" + this.getId());
      bar.setProperty(Property.Class, this.valueStyleClass_);
      app.getTheme().apply(this, bar, ElementThemeRole.ProgressBarBar);
      if (bs5Theme != null) {
        label = bar;
      } else {
        label = DomElement.createNew(DomElementType.DIV);
        label.setId("lbl" + this.getId());
        app.getTheme().apply(this, label, ElementThemeRole.ProgressBarLabel);
      }
    }
    if (this.changed_ || all) {
      if (!(bar != null)) {
        bar = DomElement.getForUpdate("bar" + this.getId(), DomElementType.DIV);
      }
      if (!(label != null)) {
        if (bs5Theme != null) {
          label = bar;
        } else {
          label = DomElement.getForUpdate("lbl" + this.getId(), DomElementType.DIV);
        }
      }
      this.updateBar(bar);
      WString s = this.getText();
      removeScript(s);
      label.setProperty(Property.InnerHTML, s.toString());
      this.changed_ = false;
    }
    if (this.valueStyleClassChanged_) {
      if (!(bar != null)) {
        bar = DomElement.getForUpdate("bar" + this.getId(), DomElementType.DIV);
      }
      bar.setProperty(Property.Class, this.valueStyleClass_);
      WApplication.getInstance().getTheme().apply(this, bar, ElementThemeRole.ProgressBarBar);
      this.valueStyleClassChanged_ = false;
    }
    if (bar != null) {
      element.addChild(bar);
    }
    if (label != null && !(bs5Theme != null)) {
      element.addChild(label);
    }
    super.updateDom(element, all);
  }

  DomElementType getDomElementType() {
    return DomElementType.DIV;
  }

  void propagateRenderOk(boolean deep) {
    this.changed_ = false;
    super.propagateRenderOk(deep);
  }

  private double min_;
  private double max_;
  private double value_;
  private WString format_;
  private boolean changed_;
  private boolean valueStyleClassChanged_;
  private String valueStyleClass_;
  // private void onChange() ;
  private Signal1<Double> valueChanged_;
  private Signal progressCompleted_;

  private double getPercentage() {
    double max = this.getMaximum();
    double min = this.getMinimum();
    if (max - min != 0) {
      return (this.getValue() - min) * 100 / (max - min);
    } else {
      return 0;
    }
  }
}
