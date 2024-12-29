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

class WTimePicker extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WTimePicker.class);

  public WTimePicker(WTimeEdit timeEdit, WContainerWidget parentContainer) {
    super();
    this.format_ = "";
    this.timeEdit_ = timeEdit;
    this.selectionChanged_ = new Signal();
    this.toggleAmPm_ = new JSlot(2, this);
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public WTimePicker(WTimeEdit timeEdit) {
    this(timeEdit, (WContainerWidget) null);
  }

  public WTime getTime() {
    int hours = 0;
    int minutes = 0;
    int seconds = 0;
    int milliseconds = 0;
    try {
      hours = Integer.parseInt(this.sbhour_.getText());
      minutes = Integer.parseInt(this.sbminute_.getText());
      if (this.isFormatS()) {
        seconds = Integer.parseInt(this.sbsecond_.getText());
      }
      if (this.isFormatMs()) {
        milliseconds = Integer.parseInt(this.sbmillisecond_.getText());
      }
      if (this.isFormatAp()) {
        if (this.cbAP_.getCurrentIndex() == 1) {
          if (hours != 12) {
            hours += 12;
          }
        } else {
          if (hours == 12) {
            hours = 0;
          }
        }
      }
    } catch (final RuntimeException e) {
      logger.error(
          new StringWriter().append("stoi() std::exception in WTimePicker::time()").toString());
    }
    return new WTime(hours, minutes, seconds, milliseconds);
  }

  public void setTime(final WTime time) {
    if (!(time != null && time.isValid())) {
      logger.error(new StringWriter().append("Time is invalid!").toString());
      return;
    }
    int hours = 0;
    if (this.isFormatAp()) {
      hours = time.getPmHour();
      if (time.getHour() < 12) {
        this.cbAP_.setCurrentIndex(0);
      } else {
        this.cbAP_.setCurrentIndex(1);
      }
    } else {
      hours = time.getHour();
    }
    int minutes = time.getMinute();
    int seconds = time.getSecond();
    int millisecond = time.getMsec();
    this.sbhour_.setValue(hours);
    this.sbminute_.setValue(minutes);
    if (this.isFormatS()) {
      this.sbsecond_.setValue(seconds);
    }
    if (this.isFormatMs()) {
      this.sbmillisecond_.setValue(millisecond);
    }
  }

  public Signal selectionChanged() {
    return this.selectionChanged_;
  }

  public void setHourStep(int step) {
    this.sbhour_.setSingleStep(step);
  }

  public int getHourStep() {
    return this.sbhour_.getSingleStep();
  }

  public void setMinuteStep(int step) {
    this.sbminute_.setSingleStep(step);
  }

  public int getMinuteStep() {
    return this.sbminute_.getSingleStep();
  }

  public void setSecondStep(int step) {
    if (this.sbsecond_ != null) {
      this.sbsecond_.setSingleStep(step);
    }
  }

  public int getSecondStep() {
    if (this.sbsecond_ != null) {
      return this.sbsecond_.getSingleStep();
    } else {
      return 1;
    }
  }

  public void setMillisecondStep(int step) {
    if (this.sbmillisecond_ != null) {
      this.sbmillisecond_.setSingleStep(step);
    }
  }

  public int getMillisecondStep() {
    if (this.sbmillisecond_ != null) {
      return this.sbmillisecond_.getSingleStep();
    } else {
      return 1;
    }
  }

  public void setWrapAroundEnabled(boolean enabled) {
    this.sbhour_.setWrapAroundEnabled(enabled);
    this.sbminute_.setWrapAroundEnabled(enabled);
    if (this.sbsecond_ != null) {
      this.sbsecond_.setWrapAroundEnabled(enabled);
    }
    if (this.sbmillisecond_ != null) {
      this.sbmillisecond_.setWrapAroundEnabled(enabled);
    }
    if (enabled) {
      this.sbhour_.jsValueChanged().addListener(this.toggleAmPm_);
    } else {
      this.sbhour_.jsValueChanged().removeListener(this.toggleAmPm_);
    }
  }

  public boolean isWrapAroundEnabled() {
    return this.sbhour_.isWrapAroundEnabled();
  }

  public void configure() {
    WTemplate container = ObjectUtils.cast(this.getImplementation(), WTemplate.class);
    if (this.isFormatS()) {
      this.sbsecond_ = new WSpinBox();
      container.bindWidget("second", this.sbsecond_);
      this.sbsecond_.setWidth(new WLength(70));
      this.sbsecond_.setRange(0, 59);
      this.sbsecond_.setSingleStep(1);
      this.sbsecond_
          .changed()
          .addListener(
              this,
              () -> {
                WTimePicker.this.secondValueChanged();
              });
      this.sbsecond_.setWrapAroundEnabled(this.isWrapAroundEnabled());
    } else {
      if (this.sbsecond_ != null) {
        {
          WWidget toRemove = container.removeWidget("second");
          if (toRemove != null) toRemove.remove();
        }

        this.sbsecond_ = null;
        container.bindEmpty("second");
      }
    }
    if (this.isFormatMs()) {
      if (!(this.sbmillisecond_ != null)) {
        this.sbmillisecond_ = new WSpinBox();
        container.bindWidget("millisecond", this.sbmillisecond_);
        this.sbmillisecond_.setWidth(new WLength(70));
        this.sbmillisecond_.setRange(0, 999);
        this.sbmillisecond_.setSingleStep(1);
        this.sbmillisecond_
            .changed()
            .addListener(
                this,
                () -> {
                  WTimePicker.this.msecValueChanged();
                });
        this.sbmillisecond_.setWrapAroundEnabled(this.isWrapAroundEnabled());
      }
    } else {
      if (this.sbmillisecond_ != null) {
        {
          WWidget toRemove = container.removeWidget("millisecond");
          if (toRemove != null) toRemove.remove();
        }

        this.sbmillisecond_ = null;
        container.bindEmpty("millisecond");
      }
    }
    if (this.isFormatAp()) {
      if (!(this.cbAP_ != null)) {
        this.cbAP_ = new WComboBox();
        container.bindWidget("ampm", this.cbAP_);
        this.cbAP_.setWidth(new WLength(90));
        this.cbAP_.addItem("AM");
        this.cbAP_.addItem("PM");
        this.cbAP_
            .changed()
            .addListener(
                this,
                () -> {
                  WTimePicker.this.ampmValueChanged();
                });
      }
      this.sbhour_.setRange(1, 12);
    } else {
      if (this.cbAP_ != null) {
        {
          WWidget toRemove = container.removeWidget("ampm");
          if (toRemove != null) toRemove.remove();
        }

        this.cbAP_ = null;
        container.bindEmpty("ampm");
      }
      this.sbhour_.setRange(0, 23);
    }
    if (this.cbAP_ != null) {
      StringBuilder jsValueChanged = new StringBuilder();
      jsValueChanged
          .append("function(o,e,oldv,v){")
          .append("var obj = ")
          .append(this.cbAP_.getJsRef())
          .append(";")
          .append("if(obj){")
          .append("if (v==12 && oldv==11) {")
          .append("obj.selectedIndex = (obj.selectedIndex + 1) % 2;")
          .append("}")
          .append("if (v==11 && oldv==12) {")
          .append("obj.selectedIndex = (obj.selectedIndex + 1) % 2;")
          .append("}")
          .append("}")
          .append("}");
      this.toggleAmPm_.setJavaScript(jsValueChanged.toString());
    } else {
      this.toggleAmPm_.setJavaScript("function(){}");
    }
  }

  private void init(final WTime time) {
    WTemplate container = new WTemplate();
    this.setImplementation(container);
    WApplication.getInstance()
        .getTheme()
        .apply(this, container, WidgetThemeRole.TimePickerPopupContent);
    container.setTemplateText(tr("Wt.WTimePicker.template"));
    this.sbhour_ = new WSpinBox();
    container.bindWidget("hour", this.sbhour_);
    this.sbhour_.setWidth(new WLength(70));
    this.sbhour_.setSingleStep(1);
    this.sbhour_
        .changed()
        .addListener(
            this,
            () -> {
              WTimePicker.this.hourValueChanged();
            });
    this.sbminute_ = new WSpinBox();
    container.bindWidget("minute", this.sbminute_);
    this.sbminute_.setWidth(new WLength(70));
    this.sbminute_.setRange(0, 59);
    this.sbminute_.setSingleStep(1);
    this.sbminute_
        .changed()
        .addListener(
            this,
            () -> {
              WTimePicker.this.minuteValueChanged();
            });
    this.sbsecond_ = null;
    container.bindEmpty("second");
    this.sbmillisecond_ = null;
    container.bindEmpty("millisecond");
    this.cbAP_ = null;
    container.bindEmpty("ampm");
    this.configure();
  }

  private final void init() {
    init(null);
  }

  private String format_;
  private WSpinBox sbhour_;
  private WSpinBox sbminute_;
  private WSpinBox sbsecond_;
  private WSpinBox sbmillisecond_;
  private WComboBox cbAP_;
  private WTimeEdit timeEdit_;

  private void hourValueChanged() {
    if (this.sbhour_.validate() == ValidationState.Valid) {
      this.selectionChanged_.trigger();
    }
  }

  private void minuteValueChanged() {
    if (this.sbminute_.validate() == ValidationState.Valid) {
      this.selectionChanged_.trigger();
    }
  }

  private void secondValueChanged() {
    if (this.sbsecond_.validate() == ValidationState.Valid) {
      this.selectionChanged_.trigger();
    }
  }

  private void msecValueChanged() {
    if (this.sbmillisecond_.validate() == ValidationState.Valid) {
      this.selectionChanged_.trigger();
    }
  }

  private void ampmValueChanged() {
    if (this.cbAP_.validate() == ValidationState.Valid) {
      this.selectionChanged_.trigger();
    }
  }

  private boolean isFormatAp() {
    return WTime.usesAmPm(this.timeEdit_.getFormat());
  }

  private boolean isFormatMs() {
    String format = this.timeEdit_.getFormat();
    return WTime.fromString(new WTime(4, 5, 6, 123).toString(format), format).getMsec() == 123;
  }

  private boolean isFormatS() {
    String format = this.timeEdit_.getFormat();
    return WTime.fromString(new WTime(4, 5, 6, 123).toString(format), format).getSecond() == 6;
  }

  private Signal selectionChanged_;
  private JSlot toggleAmPm_;
}
