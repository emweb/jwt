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
 * A widget that provides in-place-editable text.
 *
 * <p>The WInPlaceEdit provides a text that may be edited in place by the user by clicking on it.
 * When clicked, the text turns into a line edit, with optionally a save and cancel button (see
 * {@link WInPlaceEdit#setButtonsEnabled(boolean enabled) setButtonsEnabled()}).
 *
 * <p>When the user saves the edit, the {@link WInPlaceEdit#valueChanged() valueChanged()} signal is
 * emitted.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * WContainerWidget w = new WContainerWidget();
 * new WText("Name: ", w);
 * WInPlaceEdit edit = new WInPlaceEdit("Bob Smith", w);
 * edit.setStyleClass("inplace");
 *
 * }</pre>
 *
 * <p>This code will produce an edit that looks like: <div align="center"> <img
 * src="doc-files/WInPlaceEdit-1.png">
 *
 * <p><strong>WInPlaceEdit text mode</strong> </div>
 *
 * <p>When the text is clicked, the edit will expand to become: <div align="center"> <img
 * src="doc-files/WInPlaceEdit-2.png">
 *
 * <p><strong>WInPlaceEdit edit mode</strong> </div>
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>A {@link WInPlaceEdit} widget renders as a <code>&lt;span&gt;</code> containing a {@link
 * WText}, a {@link WLineEdit} and optional buttons ({@link WPushButton}). All these widgets may be
 * styled as such. It does not provide style information.
 *
 * <p>In particular, you may want to provide a visual indication that the text is editable e.g.
 * using a hover effect:
 *
 * <p>CSS stylesheet:
 *
 * <pre>{@code
 * .inplace span:hover {
 * background-color: gray;
 * }
 *
 * }</pre>
 */
public class WInPlaceEdit extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WInPlaceEdit.class);

  /** Creates an in-place edit. */
  public WInPlaceEdit(WContainerWidget parentContainer) {
    super();
    this.valueChanged_ = new Signal1<WString>();
    this.placeholderText_ = new WString();
    this.c2_ = new AbstractSignal.Connection();
    this.create();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an in-place edit.
   *
   * <p>Calls {@link #WInPlaceEdit(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WInPlaceEdit() {
    this((WContainerWidget) null);
  }
  /** Creates an in-place edit with the given text. */
  public WInPlaceEdit(final CharSequence text, WContainerWidget parentContainer) {
    super();
    this.valueChanged_ = new Signal1<WString>();
    this.placeholderText_ = new WString();
    this.c2_ = new AbstractSignal.Connection();
    this.create();
    this.setText(text);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an in-place edit with the given text.
   *
   * <p>Calls {@link #WInPlaceEdit(CharSequence text, WContainerWidget parentContainer) this(text,
   * (WContainerWidget)null)}
   */
  public WInPlaceEdit(final CharSequence text) {
    this(text, (WContainerWidget) null);
  }
  /**
   * Creates an in-place edit with the given text.
   *
   * <p>The first parameter configures whether buttons are available in edit mode.
   *
   * <p>
   *
   * @see WInPlaceEdit#setButtonsEnabled(boolean enabled)
   */
  public WInPlaceEdit(boolean buttons, final CharSequence text, WContainerWidget parentContainer) {
    super();
    this.valueChanged_ = new Signal1<WString>();
    this.placeholderText_ = new WString();
    this.c2_ = new AbstractSignal.Connection();
    this.create();
    this.setText(text);
    this.setButtonsEnabled(buttons);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an in-place edit with the given text.
   *
   * <p>Calls {@link #WInPlaceEdit(boolean buttons, CharSequence text, WContainerWidget
   * parentContainer) this(buttons, text, (WContainerWidget)null)}
   */
  public WInPlaceEdit(boolean buttons, final CharSequence text) {
    this(buttons, text, (WContainerWidget) null);
  }
  /**
   * Returns the current value.
   *
   * <p>
   *
   * @see WInPlaceEdit#setText(CharSequence text)
   */
  public WString getText() {
    return new WString(this.edit_.getText());
  }
  /**
   * Sets the current value.
   *
   * <p>
   *
   * @see WInPlaceEdit#getText()
   */
  public void setText(final CharSequence text) {
    this.empty_ = (text.length() == 0);
    if (!this.empty_) {
      this.text_.setText(text);
    } else {
      this.text_.setText(this.getPlaceholderText());
    }
    this.edit_.setText(text.toString());
  }
  /**
   * Sets the placeholder text.
   *
   * <p>This sets the text that is shown when the field is empty.
   */
  public void setPlaceholderText(final CharSequence text) {
    this.placeholderText_ = WString.toWString(text);
    this.edit_.setPlaceholderText(text);
    if (this.empty_) {
      this.text_.setText(text);
    }
  }
  /**
   * Returns the placeholder text.
   *
   * <p>
   *
   * @see WInPlaceEdit#setPlaceholderText(CharSequence text)
   */
  public WString getPlaceholderText() {
    return this.placeholderText_;
  }
  /**
   * Returns the line edit.
   *
   * <p>You may use this for example to set a validator on the line edit.
   */
  public WLineEdit getLineEdit() {
    return this.edit_;
  }
  /**
   * Returns the {@link WText} widget that renders the current string.
   *
   * <p>You may use this for example to set the text format of the displayed string.
   */
  public WText getTextWidget() {
    return this.text_;
  }
  /**
   * Returns the save button.
   *
   * <p>This method returns <code>null</code> if the buttons were disabled.
   *
   * <p>
   *
   * @see WInPlaceEdit#getCancelButton()
   * @see WInPlaceEdit#setButtonsEnabled(boolean enabled)
   */
  public WPushButton getSaveButton() {
    return this.save_;
  }
  /**
   * Returns the cancel button.
   *
   * <p>This method returns <code>null</code> if the buttons were disabled.
   *
   * <p>
   *
   * @see WInPlaceEdit#getSaveButton()
   * @see WInPlaceEdit#setButtonsEnabled(boolean enabled)
   */
  public WPushButton getCancelButton() {
    return this.cancel_;
  }
  /**
   * Signal emitted when the value has been changed.
   *
   * <p>The signal argument provides the new value.
   */
  public Signal1<WString> valueChanged() {
    return this.valueChanged_;
  }
  /**
   * Displays the Save and &apos;Cancel&apos; button during editing.
   *
   * <p>By default, the Save and Cancel buttons are shown. Call this function with <code>enabled
   * </code> = <code>false</code> to only show a line edit.
   *
   * <p>In this mode, the enter key or any event that causes focus to be lost saves the value while
   * the escape key cancels the editing.
   */
  public void setButtonsEnabled(boolean enabled) {
    if (enabled && !(this.save_ != null)) {
      this.c2_.disconnect();
      if (this.buttons_ != null) {
        this.buttons_.addWidget(
            this.save_ = new WPushButton(tr("Wt.WInPlaceEdit.Save"), (WContainerWidget) null));
        this.buttons_.addWidget(
            this.cancel_ = new WPushButton(tr("Wt.WInPlaceEdit.Cancel"), (WContainerWidget) null));
      } else {
        this.editing_.addWidget(
            this.save_ = new WPushButton(tr("Wt.WInPlaceEdit.Save"), (WContainerWidget) null));
        this.editing_.addWidget(
            this.cancel_ = new WPushButton(tr("Wt.WInPlaceEdit.Cancel"), (WContainerWidget) null));
      }
      this.save_
          .clicked()
          .addListener(
              this.edit_,
              (WMouseEvent e1) -> {
                WInPlaceEdit.this.edit_.disable();
              });
      this.save_
          .clicked()
          .addListener(
              this.save_,
              (WMouseEvent e1) -> {
                WInPlaceEdit.this.save_.disable();
              });
      this.save_
          .clicked()
          .addListener(
              this.cancel_,
              (WMouseEvent e1) -> {
                WInPlaceEdit.this.cancel_.disable();
              });
      this.save_
          .clicked()
          .addListener(
              this,
              (WMouseEvent e1) -> {
                WInPlaceEdit.this.save();
              });
      this.cancel_
          .clicked()
          .addListener(
              this.editing_,
              (WMouseEvent e1) -> {
                WInPlaceEdit.this.editing_.hide();
              });
      this.cancel_
          .clicked()
          .addListener(
              this.text_,
              (WMouseEvent e1) -> {
                WInPlaceEdit.this.text_.show();
              });
      this.cancel_
          .clicked()
          .addListener(
              this,
              (WMouseEvent e1) -> {
                WInPlaceEdit.this.cancel();
              });
    } else {
      if (!enabled && this.save_ != null) {
        {
          WWidget toRemove = WidgetUtils.remove(this.save_.getParent(), this.save_);
          if (toRemove != null) toRemove.remove();
        }

        {
          WWidget toRemove = WidgetUtils.remove(this.cancel_.getParent(), this.cancel_);
          if (toRemove != null) toRemove.remove();
        }

        this.save_ = null;
        this.cancel_ = null;
        this.c2_ =
            this.edit_
                .blurred()
                .addListener(
                    this,
                    () -> {
                      WInPlaceEdit.this.save();
                    });
      }
    }
  }
  /**
   * Displays the Save and &apos;Cancel&apos; button during editing.
   *
   * <p>Calls {@link #setButtonsEnabled(boolean enabled) setButtonsEnabled(true)}
   */
  public final void setButtonsEnabled() {
    setButtonsEnabled(true);
  }

  protected void render(EnumSet<RenderFlag> flags) {
    WApplication app = WApplication.getInstance();
    WBootstrap5Theme bs5Theme = ObjectUtils.cast(app.getTheme(), WBootstrap5Theme.class);
    if (!(this.buttons_ != null) && (!(bs5Theme != null) || !this.isThemeStyleEnabled())) {
      this.editing_.addWidget(this.buttons_ = new WContainerWidget());
      this.buttons_.setInline(true);
      this.buttons_.setThemeStyleEnabled(this.isThemeStyleEnabled());
      if (this.save_ != null) {
        this.buttons_.addWidget(WidgetUtils.remove(this.editing_, this.save_));
        this.buttons_.addWidget(WidgetUtils.remove(this.editing_, this.cancel_));
      }
    }
    if (this.save_ != null && flags.contains(RenderFlag.Full)) {
      app.getTheme().apply(this, this.editing_, WidgetThemeRole.InPlaceEditing);
      app.getTheme().apply(this, this.save_, WidgetThemeRole.InPlaceEditingButton);
      app.getTheme().apply(this, this.cancel_, WidgetThemeRole.InPlaceEditingButton);
      if (this.buttons_ != null) {
        app.getTheme().apply(this, this.buttons_, WidgetThemeRole.InPlaceEditingButtonsContainer);
      }
    }
    super.render(flags);
  }

  private void create() {
    this.buttons_ = null;
    this.setImplementation(this.impl_ = new WContainerWidget());
    this.setInline(true);
    this.impl_.addWidget(
        this.text_ = new WText(WString.Empty, TextFormat.Plain, (WContainerWidget) null));
    this.text_.getDecorationStyle().setCursor(Cursor.Arrow);
    this.impl_.addWidget(this.editing_ = new WContainerWidget());
    this.editing_.setInline(true);
    this.editing_.hide();
    this.editing_.addWidget(this.edit_ = new WLineEdit());
    this.edit_.setTextSize(20);
    this.save_ = null;
    this.cancel_ = null;
    this.text_
        .clicked()
        .addListener(
            this.text_,
            (WMouseEvent e1) -> {
              WInPlaceEdit.this.text_.hide();
            });
    this.text_
        .clicked()
        .addListener(
            this.editing_,
            (WMouseEvent e1) -> {
              WInPlaceEdit.this.editing_.show();
            });
    this.text_
        .clicked()
        .addListener(
            this.edit_,
            (WMouseEvent e1) -> {
              WInPlaceEdit.this.edit_.setFocus();
            });
    this.edit_
        .enterPressed()
        .addListener(
            this.edit_,
            () -> {
              WInPlaceEdit.this.edit_.disable();
            });
    this.edit_
        .enterPressed()
        .addListener(
            this,
            () -> {
              WInPlaceEdit.this.save();
            });
    this.edit_.enterPressed().preventPropagation();
    this.edit_
        .escapePressed()
        .addListener(
            this.editing_,
            () -> {
              WInPlaceEdit.this.editing_.hide();
            });
    this.edit_
        .escapePressed()
        .addListener(
            this.text_,
            () -> {
              WInPlaceEdit.this.text_.show();
            });
    this.edit_
        .escapePressed()
        .addListener(
            this,
            () -> {
              WInPlaceEdit.this.cancel();
            });
    this.edit_.escapePressed().preventPropagation();
    this.setButtonsEnabled();
  }

  private void save() {
    this.editing_.hide();
    this.text_.show();
    this.edit_.enable();
    if (this.save_ != null) {
      this.save_.enable();
    }
    if (this.cancel_ != null) {
      this.cancel_.enable();
    }
    boolean changed =
        this.empty_
            ? this.edit_.getText().length() != 0
            : !this.edit_.getText().equals(this.text_.getText().toString());
    if (changed) {
      this.setText(this.edit_.getText());
      this.valueChanged().trigger(new WString(this.edit_.getText()));
    }
  }

  private void cancel() {
    this.edit_.setText((this.empty_ ? WString.Empty : this.text_.getText()).toString());
  }

  private Signal1<WString> valueChanged_;
  private WContainerWidget impl_;
  private WContainerWidget editing_;
  private WContainerWidget buttons_;
  private WText text_;
  private WLineEdit edit_;
  private WPushButton save_;
  private WPushButton cancel_;
  private WString placeholderText_;
  private AbstractSignal.Connection c2_;
  private boolean empty_;
}
