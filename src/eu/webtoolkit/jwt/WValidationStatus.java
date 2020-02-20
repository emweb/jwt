/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget that keeps track of the validation status of a form widget.
 *
 * <p><i>Since JWt 2.1.3, all standard validators provide client-side validation and this is
 * reflected in the form widget using the style class &quot;Wt-invalid&quot; when the validator
 * returns not {@link WValidator.State#Valid State#Valid}. Therefore, it is unlikely you will need
 * this class anymore.</i>
 *
 * <p>Use a WValidationStatus widget to act to changes in validation of a {@link WFormWidget}. The
 * widget may show visual feed-back of the validation state of the input.
 *
 * <p>Visual feed-back may be given by showing an invalidStateWidget when input is invalid, an
 * invalidEmptyStateWidget when the input is invalid because mandatory and empty, or a
 * validStateWidget when input is valid. All of these widgets may be 0, indicating that no widget
 * will be shown for the corresponding state.
 *
 * <p>When validation state changes from invalid to valid, or from valid to invalid, the widget
 * emits the validated signal. This may be used to for example enable or disable a button.
 *
 * <p>
 *
 * @deprecated Since JWt 3.1.1, validation is handled directly on {@link WFormWidget} subclasses.
 */
public class WValidationStatus extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WValidationStatus.class);

  /**
   * Construct a {@link WValidationStatus} widget for another widget.
   *
   * <p>Constructs a validation status widget for the given field.
   *
   * <p>The validation stateWidgets (if not <code>null</code>) will be managed by this widget, and
   * shown and hidden to reflect the current validation state.
   */
  public WValidationStatus(
      WFormWidget field,
      WWidget validStateWidget,
      WWidget invalidStateWidget,
      WWidget invalidEmptyStateWidget,
      WContainerWidget parent) {
    super(parent);
    this.validated_ = new Signal1<Boolean>(this);
    this.field_ = field;
    this.validStateWidget_ = validStateWidget;
    this.invalidStateWidget_ = invalidStateWidget;
    this.invalidEmptyStateWidget_ = invalidEmptyStateWidget;
    this.setImplementation(this.impl_ = new WContainerWidget());
    this.impl_.setInline(true);
    this.state_ = this.field_.validate();
    if (this.validStateWidget_ != null) {
      this.impl_.addWidget(this.validStateWidget_);
      this.validStateWidget_.setHidden(this.state_ != WValidator.State.Valid);
    }
    if (this.invalidStateWidget_ != null) {
      this.impl_.addWidget(this.invalidStateWidget_);
      this.invalidStateWidget_.setHidden(this.state_ != WValidator.State.Invalid);
    }
    if (this.invalidEmptyStateWidget_ != null) {
      this.impl_.addWidget(this.invalidEmptyStateWidget_);
      this.invalidEmptyStateWidget_.setHidden(this.state_ != WValidator.State.InvalidEmpty);
    }
    field
        .changed()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                WValidationStatus.this.inputChanged();
              }
            });
    field
        .keyWentUp()
        .addListener(
            this,
            new Signal1.Listener<WKeyEvent>() {
              public void trigger(WKeyEvent e1) {
                WValidationStatus.this.inputChanged();
              }
            });
  }
  /**
   * Construct a {@link WValidationStatus} widget for another widget.
   *
   * <p>Calls {@link #WValidationStatus(WFormWidget field, WWidget validStateWidget, WWidget
   * invalidStateWidget, WWidget invalidEmptyStateWidget, WContainerWidget parent) this(field,
   * (WWidget)null, (WWidget)null, (WWidget)null, (WContainerWidget)null)}
   */
  public WValidationStatus(WFormWidget field) {
    this(field, (WWidget) null, (WWidget) null, (WWidget) null, (WContainerWidget) null);
  }
  /**
   * Construct a {@link WValidationStatus} widget for another widget.
   *
   * <p>Calls {@link #WValidationStatus(WFormWidget field, WWidget validStateWidget, WWidget
   * invalidStateWidget, WWidget invalidEmptyStateWidget, WContainerWidget parent) this(field,
   * validStateWidget, (WWidget)null, (WWidget)null, (WContainerWidget)null)}
   */
  public WValidationStatus(WFormWidget field, WWidget validStateWidget) {
    this(field, validStateWidget, (WWidget) null, (WWidget) null, (WContainerWidget) null);
  }
  /**
   * Construct a {@link WValidationStatus} widget for another widget.
   *
   * <p>Calls {@link #WValidationStatus(WFormWidget field, WWidget validStateWidget, WWidget
   * invalidStateWidget, WWidget invalidEmptyStateWidget, WContainerWidget parent) this(field,
   * validStateWidget, invalidStateWidget, (WWidget)null, (WContainerWidget)null)}
   */
  public WValidationStatus(
      WFormWidget field, WWidget validStateWidget, WWidget invalidStateWidget) {
    this(field, validStateWidget, invalidStateWidget, (WWidget) null, (WContainerWidget) null);
  }
  /**
   * Construct a {@link WValidationStatus} widget for another widget.
   *
   * <p>Calls {@link #WValidationStatus(WFormWidget field, WWidget validStateWidget, WWidget
   * invalidStateWidget, WWidget invalidEmptyStateWidget, WContainerWidget parent) this(field,
   * validStateWidget, invalidStateWidget, invalidEmptyStateWidget, (WContainerWidget)null)}
   */
  public WValidationStatus(
      WFormWidget field,
      WWidget validStateWidget,
      WWidget invalidStateWidget,
      WWidget invalidEmptyStateWidget) {
    this(
        field,
        validStateWidget,
        invalidStateWidget,
        invalidEmptyStateWidget,
        (WContainerWidget) null);
  }
  /** Is the field currently considered valid? */
  public boolean isValid() {
    return this.state_ == WValidator.State.Valid;
  }
  /**
   * {@link Signal} emitted when the validation state changed.
   *
   * <p>The new state of the validation (valid or invalid) is given as argument. This signal gets
   * emitted when the state changes from {@link WValidator.State#Valid State#Valid} to {@link
   * WValidator.State#Invalid State#Invalid}, or from {@link WValidator.State#Invalid State#Invalid}
   * to {@link WValidator.State#Valid State#Valid}.
   */
  public Signal1<Boolean> validated() {
    return this.validated_;
  }

  private Signal1<Boolean> validated_;
  private WContainerWidget impl_;
  private WFormWidget field_;
  private WWidget validStateWidget_;
  private WWidget invalidStateWidget_;
  private WWidget invalidEmptyStateWidget_;
  private WValidator.State state_;

  private void inputChanged() {
    WValidator.State state = this.field_.validate();
    if (state != this.state_) {
      if (this.validStateWidget_ != null) {
        this.validStateWidget_.setHidden(state != WValidator.State.Valid);
      }
      if (this.invalidStateWidget_ != null) {
        this.invalidStateWidget_.setHidden(state != WValidator.State.Invalid);
      }
      if (this.invalidEmptyStateWidget_ != null) {
        this.invalidEmptyStateWidget_.setHidden(state != WValidator.State.InvalidEmpty);
      }
      if (this.state_ == WValidator.State.Valid || state == WValidator.State.Valid) {
        this.state_ = state;
        this.validated_.trigger(this.state_ == WValidator.State.Valid);
      } else {
        this.state_ = state;
      }
    }
  }
}
