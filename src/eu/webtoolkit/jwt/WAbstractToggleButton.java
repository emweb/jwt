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
 * An abstract base class for radio buttons and check boxes.
 *
 * <p>A toggle button provides a button with a boolean state (checked or unchecked), and a text
 * label.
 *
 * <p>To act on a change of the state, either connect a slot to the {@link WFormWidget#changed()}
 * signal, or connect a slot to the {@link WAbstractToggleButton#checked() checked()} or {@link
 * WAbstractToggleButton#unChecked() unChecked()} signals.
 *
 * <p>The current state (checked or unchecked) may be inspected using the {@link
 * WAbstractToggleButton#isChecked() isChecked()} method.
 */
public abstract class WAbstractToggleButton extends WFormWidget {
  private static Logger logger = LoggerFactory.getLogger(WAbstractToggleButton.class);

  /** Creates an unchecked toggle button without label. */
  protected WAbstractToggleButton(WContainerWidget parentContainer) {
    super();
    this.state_ = CheckState.Unchecked;
    this.text_ = new WText.RichText();
    this.flags_ = new BitSet();
    this.flags_.set(BIT_NAKED);
    this.flags_.set(BIT_WORD_WRAP);
    this.text_.format = TextFormat.Plain;
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an unchecked toggle button without label.
   *
   * <p>Calls {@link #WAbstractToggleButton(WContainerWidget parentContainer)
   * this((WContainerWidget)null)}
   */
  protected WAbstractToggleButton() {
    this((WContainerWidget) null);
  }
  /**
   * Creates an unchecked toggle button with given text label.
   *
   * <p>The text label is rendered to the right side of the button.
   */
  protected WAbstractToggleButton(final CharSequence text, WContainerWidget parentContainer) {
    super();
    this.state_ = CheckState.Unchecked;
    this.text_ = new WText.RichText();
    this.flags_ = new BitSet();
    this.flags_.set(BIT_WORD_WRAP);
    this.text_.format = TextFormat.Plain;
    this.text_.text = WString.toWString(text);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an unchecked toggle button with given text label.
   *
   * <p>Calls {@link #WAbstractToggleButton(CharSequence text, WContainerWidget parentContainer)
   * this(text, (WContainerWidget)null)}
   */
  protected WAbstractToggleButton(final CharSequence text) {
    this(text, (WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {
    super.remove();
  }
  /**
   * Sets the label text.
   *
   * <p>The label is rendered to the right of the button.
   */
  public void setText(final CharSequence text) {
    if (canOptimizeUpdates() && (text.toString().equals(this.text_.text.toString()))) {
      return;
    }
    if (this.isRendered() && this.flags_.get(BIT_NAKED)) {
      logger.error(
          new StringWriter()
              .append(
                  "setText() has no effect when already rendered as a naked checkbox (without label)")
              .toString());
    }
    this.text_.setText(text);
    this.flags_.clear(BIT_NAKED);
    this.flags_.set(BIT_TEXT_CHANGED);
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
  }
  /**
   * Returns the label text.
   *
   * <p>
   *
   * @see WAbstractToggleButton#setText(CharSequence text)
   */
  public WString getText() {
    return this.text_.text;
  }
  /**
   * Sets the text format.
   *
   * <p>The textFormat controls how the string should be interpreted: either as plain text, which is
   * displayed literally, or as XHTML-markup.
   *
   * <p>When changing the textFormat to {@link TextFormat#XHTML}, and the current text is literal
   * (not created using {@link WString#tr(String key) WString#tr()}), the current text is parsed
   * using an XML parser which discards malicious tags and attributes silently. When the parser
   * encounters an XML parse error, the textFormat is left unchanged, and this method returns false.
   *
   * <p>Returns whether the textFormat could be set for the current text.
   *
   * <p>The default format is {@link TextFormat#Plain}.
   */
  public boolean setTextFormat(TextFormat format) {
    return this.text_.setFormat(format);
  }
  /**
   * Returns the text format.
   *
   * <p>
   *
   * @see WAbstractToggleButton#setTextFormat(TextFormat format)
   */
  public TextFormat getTextFormat() {
    return this.text_.format;
  }
  /**
   * Returns the button state.
   *
   * <p>
   *
   * @see WAbstractToggleButton#setChecked()
   */
  public boolean isChecked() {
    return this.state_ == CheckState.Checked;
  }
  /**
   * Sets the button state.
   *
   * <p>This method does not emit one of the {@link WAbstractToggleButton#checked() checked()} or
   * {@link WAbstractToggleButton#unChecked() unChecked()} signals.
   *
   * <p>
   *
   * @see WAbstractToggleButton#setChecked()
   * @see WAbstractToggleButton#setUnChecked()
   */
  public void setChecked(boolean how) {
    this.setCheckState(how ? CheckState.Checked : CheckState.Unchecked);
  }
  /**
   * Checks the button.
   *
   * <p>Does not emit the {@link WAbstractToggleButton#checked() checked()} signal.
   *
   * <p>
   *
   * @see WAbstractToggleButton#setChecked(boolean how)
   */
  public void setChecked() {
    this.prevState_ = this.state_;
    this.setChecked(true);
  }
  /**
   * Unchecks the button.
   *
   * <p>Does not emit the {@link WAbstractToggleButton#unChecked() unChecked()} signal.
   *
   * <p>
   *
   * @see WAbstractToggleButton#setChecked(boolean how)
   */
  public void setUnChecked() {
    this.prevState_ = this.state_;
    this.setChecked(false);
  }
  /**
   * Returns the current value.
   *
   * <p>Returns &quot;yes&quot; when checked, &quot;maybe&quot; when partially checked, and
   * &quot;no&quot; when unchecked.
   */
  public String getValueText() {
    switch (this.state_) {
      case Unchecked:
        return "no";
      case PartiallyChecked:
        return "maybe";
      default:
        return "yes";
    }
  }
  /**
   * Sets the current value.
   *
   * <p>This interprets text values of &quot;yes&quot;, &quot;maybe&quot; or &quot;no&quot;.
   */
  public void setValueText(final String text) {
    if (text.equals("yes")) {
      this.setCheckState(CheckState.Checked);
    } else {
      if (text.equals("no")) {
        this.setCheckState(CheckState.Unchecked);
      } else {
        if (text.equals("maybe")) {
          this.setCheckState(CheckState.PartiallyChecked);
        }
      }
    }
  }
  /**
   * Signal emitted when the button gets checked.
   *
   * <p>This signal is emitted when the user checks the button.
   *
   * <p>You can use the {@link WFormWidget#changed()} signal to react when the user makes any change
   * to the button state.
   */
  public EventSignal checked() {
    return this.voidEventSignal(CHECKED_SIGNAL, true);
  }
  /**
   * Signal emitted when the button gets un-checked.
   *
   * <p>This signal is emitted when the user unchecks the button.
   *
   * <p>You can use the {@link WFormWidget#changed()} signal to react when the user makes any change
   * to the button state.
   *
   * <p>
   *
   * <p><i><b>Remark: </b>This signal is not emitted for {@link WRadioButton}. In this case a button
   * can only be unchecked because another button is checked. The browser will only generate an
   * event for the button which was checked. Since {@link EventSignal} mirrors browser events, JWt
   * will also not emit this signal. The function {@link WButtonGroup#checkedChanged()} might be
   * easier to use in this case. </i>
   */
  public EventSignal unChecked() {
    return this.voidEventSignal(UNCHECKED_SIGNAL, true);
  }

  public void refresh() {
    if (this.text_.text.refresh()) {
      this.flags_.set(BIT_TEXT_CHANGED);
      this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    }
    super.refresh();
  }
  /**
   * Configures word wrapping.
   *
   * <p>When <code>wordWrap</code> is <code>true</code>, the widget may break lines, creating a
   * multi-line text. When <code>wordWrap</code> is <code>false</code>, the text will displayed on a
   * single line, unless the text contains end-of-lines (for {@link TextFormat#Plain}) or &lt;br
   * /&gt; tags or other block-level tags (for {@link TextFormat#XHTML}).
   *
   * <p>The default value is <code>false</code>.
   *
   * <p>
   *
   * @see WAbstractToggleButton#isWordWrap()
   */
  public void setWordWrap(boolean wordWrap) {
    if (this.flags_.get(BIT_WORD_WRAP) != wordWrap) {
      this.flags_.set(BIT_WORD_WRAP, wordWrap);
      this.flags_.set(BIT_WORD_WRAP_CHANGED);
      this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    }
  }
  /**
   * Returns whether word wrapping is on.
   *
   * <p>
   *
   * @see WAbstractToggleButton#setWordWrap(boolean wordWrap)
   */
  public boolean isWordWrap() {
    return this.flags_.get(BIT_WORD_WRAP);
  }

  CheckState state_;

  abstract void updateInput(final DomElement input, boolean all);

  void updateDom(final DomElement element, boolean all) {
    WApplication app = WApplication.getInstance();
    final WEnvironment env = app.getEnvironment();
    DomElement input = null;
    DomElement span = null;
    DomElement label = null;
    if (all) {
      app.getTheme().apply(this, element, ElementThemeRole.ToggleButtonRole);
    }
    if (element.getType() == DomElementType.INPUT) {
      input = element;
    } else {
      if (all) {
        input = DomElement.createNew(DomElementType.INPUT);
        input.setName("in" + this.getId());
        span = DomElement.createNew(DomElementType.SPAN);
        span.setName("t" + this.getId());
        if (element.getType() != DomElementType.LABEL) {
          label = DomElement.createNew(DomElementType.LABEL);
          label.setName("l" + this.getId());
        }
      } else {
        input = DomElement.getForUpdate("in" + this.getId(), DomElementType.INPUT);
        span = DomElement.getForUpdate("t" + this.getId(), DomElementType.SPAN);
      }
    }
    if (all) {
      this.updateInput(input, all);
    }
    EventSignal check = this.voidEventSignal(CHECKED_SIGNAL, false);
    EventSignal uncheck = this.voidEventSignal(UNCHECKED_SIGNAL, false);
    EventSignal change = this.voidEventSignal(CHANGE_SIGNAL, false);
    EventSignal1<WMouseEvent> click = this.mouseEventSignal(M_CLICK_SIGNAL, false);
    boolean piggyBackChangeOnClick = env.agentIsIE();
    boolean needUpdateChangeSignal =
        change != null && change.needsUpdate(all)
            || check != null && check.needsUpdate(all)
            || uncheck != null && uncheck.needsUpdate(all);
    boolean needUpdateClickedSignal =
        click != null && click.needsUpdate(all) || piggyBackChangeOnClick && needUpdateChangeSignal;
    super.updateDom(input, all);
    if (element != input) {
      if (element.getProperties().get(Property.Class) != null) {
        input.addPropertyWord(Property.Class, element.getProperty(Property.Class));
      }
      element.setProperties(input.getProperties());
      input.clearProperties();
      String v = element.getProperty(Property.Disabled);
      if (v.length() != 0) {
        input.setProperty(Property.Disabled, v);
        element.removeProperty(Property.Disabled);
      }
      v = element.getProperty(Property.ReadOnly);
      if (v.length() != 0) {
        input.setProperty(Property.ReadOnly, v);
        element.removeProperty(Property.ReadOnly);
      }
      v = element.getProperty(Property.TabIndex);
      if (v.length() != 0) {
        input.setProperty(Property.TabIndex, v);
        element.removeProperty(Property.TabIndex);
      }
      v = input.getAttribute("title");
      if (v.length() != 0) {
        element.setAttribute("title", v);
      }
    }
    app.getTheme().apply(this, input, ElementThemeRole.ToggleButtonInput);
    if (span != null) {
      app.getTheme().apply(this, span, ElementThemeRole.ToggleButtonSpan);
    }
    if (this.flags_.get(BIT_STATE_CHANGED) || all) {
      input.setProperty(Property.Checked, this.state_ == CheckState.Unchecked ? "false" : "true");
      if (this.supportsIndeterminate(env)) {
        input.setProperty(
            Property.Indeterminate, this.state_ == CheckState.PartiallyChecked ? "true" : "false");
      } else {
        input.setProperty(
            Property.StyleOpacity, this.state_ == CheckState.PartiallyChecked ? "0.5" : "");
      }
      this.flags_.clear(BIT_STATE_CHANGED);
    }
    List<DomElement.EventAction> changeActions = new ArrayList<DomElement.EventAction>();
    if (needUpdateChangeSignal || piggyBackChangeOnClick && needUpdateClickedSignal || all) {
      String dom = "o";
      if (check != null) {
        if (check.isConnected()) {
          changeActions.add(
              new DomElement.EventAction(
                  dom + ".checked",
                  check.getJavaScript(),
                  check.encodeCmd(),
                  check.isExposedSignal()));
        }
        check.updateOk();
      }
      if (uncheck != null) {
        if (uncheck.isConnected()) {
          changeActions.add(
              new DomElement.EventAction(
                  "!" + dom + ".checked",
                  uncheck.getJavaScript(),
                  uncheck.encodeCmd(),
                  uncheck.isExposedSignal()));
        }
        uncheck.updateOk();
      }
      if (change != null) {
        if (change.isConnected()) {
          changeActions.add(
              new DomElement.EventAction(
                  "", change.getJavaScript(), change.encodeCmd(), change.isExposedSignal()));
        }
        change.updateOk();
      }
      if (!piggyBackChangeOnClick) {
        if (!(all && changeActions.isEmpty())) {
          input.setEvent("change", changeActions);
        }
      }
    }
    if (needUpdateClickedSignal || all) {
      if (piggyBackChangeOnClick) {
        if (click != null) {
          changeActions.add(
              new DomElement.EventAction(
                  "", click.getJavaScript(), click.encodeCmd(), click.isExposedSignal()));
          click.updateOk();
        }
        if (!(all && changeActions.isEmpty())) {
          input.setEvent(CLICK_SIGNAL, changeActions);
        }
      } else {
        if (click != null) {
          this.updateSignalConnection(input, click, CLICK_SIGNAL, all);
        }
      }
    }
    if (span != null) {
      if (all || this.flags_.get(BIT_TEXT_CHANGED)) {
        span.setProperty(Property.InnerHTML, this.text_.getFormattedText());
        if (all || this.flags_.get(BIT_WORD_WRAP_CHANGED)) {
          span.setProperty(
              Property.StyleWhiteSpace, this.flags_.get(BIT_WORD_WRAP) ? "normal" : "nowrap");
          this.flags_.clear(BIT_WORD_WRAP_CHANGED);
        }
        this.flags_.clear(BIT_TEXT_CHANGED);
      }
    }
    if (element != input) {
      if (label != null) {
        label.addChild(input);
        label.addChild(span);
        element.addChild(label);
      } else {
        element.addChild(input);
        element.addChild(span);
      }
    }
    String currentClasses = element.getProperty(Property.Class);
    String previousClasses = this.getStyleClass();
    List<String> previousClassesVec = new ArrayList<String>();
    StringUtils.split(previousClassesVec, previousClasses, " ", false);
    List<String> currentClassesVec = new ArrayList<String>();
    StringUtils.split(currentClassesVec, currentClasses, " ", false);
    List<String> missingClasses = new ArrayList<String>();
    for (String styleClass : previousClassesVec) {
      boolean alreadyHasClass = false;
      for (String currentStyleClass : currentClassesVec) {
        if (currentStyleClass.equals(styleClass)) {
          alreadyHasClass = true;
          break;
        }
      }
      if (!alreadyHasClass) {
        missingClasses.add(styleClass);
      }
    }
    for (String styleClass : missingClasses) {
      element.addPropertyWord(Property.Class, styleClass);
    }
  }

  void getFormObjects(final Map<String, WObject> formObjects) {
    formObjects.put(this.getFormName(), this);
  }

  protected void setFormData(final WObject.FormData formData) {
    if (this.flags_.get(BIT_STATE_CHANGED) || this.isReadOnly()) {
      return;
    }
    if (!(formData.values.length == 0)) {
      if (formData.values[0].equals("i")) {
        this.state_ = CheckState.PartiallyChecked;
      } else {
        this.state_ = !formData.values[0].equals("0") ? CheckState.Checked : CheckState.Unchecked;
      }
    } else {
      if (this.isEnabled() && this.isVisible()) {
        this.state_ = CheckState.Unchecked;
      }
    }
  }

  void propagateRenderOk(boolean deep) {
    this.flags_.clear(BIT_STATE_CHANGED);
    EventSignal check = this.voidEventSignal(CHECKED_SIGNAL, false);
    if (check != null) {
      check.updateOk();
    }
    EventSignal uncheck = this.voidEventSignal(UNCHECKED_SIGNAL, false);
    if (uncheck != null) {
      uncheck.updateOk();
    }
    super.propagateRenderOk(deep);
  }

  DomElementType getDomElementType() {
    if (!this.flags_.get(BIT_NAKED)) {
      return DomElementType.LABEL;
    } else {
      return DomElementType.INPUT;
    }
  }

  boolean supportsIndeterminate(final WEnvironment env) {
    return env.hasJavaScript()
        && (env.agentIsIE()
            || env.agentIsSafari()
            || env.agentIsChrome()
            || env.agentIsGecko()
                && (int) env.getAgent().getValue() >= (int) UserAgent.Firefox3_6.getValue());
  }

  String getFormName() {
    if (this.getDomElementType() == DomElementType.LABEL) {
      return "in" + this.getId();
    } else {
      return super.getFormName();
    }
  }
  // protected AbstractEventSignal.LearningListener  getStateless(<pointertomember or
  // dependentsizedarray> methodpointertomember or dependentsizedarray>) ;
  private static String CHECKED_SIGNAL = "M_checked";
  private static String UNCHECKED_SIGNAL = "M_unchecked";
  private WText.RichText text_;
  private static final int BIT_NAKED = 0;
  static final int BIT_STATE_CHANGED = 1;
  private static final int BIT_TEXT_CHANGED = 2;
  private static final int BIT_WORD_WRAP_CHANGED = 3;
  private static final int BIT_WORD_WRAP = 4;
  BitSet flags_;
  private CheckState prevState_;

  private void undoSetChecked() {
    this.setCheckState(this.prevState_);
  }

  private void undoSetUnChecked() {
    this.undoSetChecked();
  }

  void setCheckState(CheckState state) {
    if (canOptimizeUpdates() && state == this.state_) {
      return;
    }
    this.state_ = state;
    this.flags_.set(BIT_STATE_CHANGED);
    this.repaint();
  }
}
