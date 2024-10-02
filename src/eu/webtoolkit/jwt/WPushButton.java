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
 * A widget that represents a push button.
 *
 * <p>To act on a button click, connect a slot to the {@link WInteractWidget#clicked()} signal.
 *
 * <p>WPushButton is an {@link WWidget#setInline(boolean inlined) inline } widget.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>The widget corresponds to the HTML <code>&lt;button&gt;</code> tag (with some exceptions in
 * the bootstrap theme).
 */
public class WPushButton extends WFormWidget {
  private static Logger logger = LoggerFactory.getLogger(WPushButton.class);

  /** Creates a push button. */
  public WPushButton(WContainerWidget parentContainer) {
    super();
    this.linkState_ = new WAnchor.LinkState();
    this.text_ = new WText.RichText();
    this.icon_ = new WLink();
    this.flags_ = new BitSet();
    this.popupMenu_ = null;
    this.text_.format = TextFormat.Plain;
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a push button.
   *
   * <p>Calls {@link #WPushButton(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WPushButton() {
    this((WContainerWidget) null);
  }
  /**
   * Creates a push button with given label text.
   *
   * <p>The default text format is {@link TextFormat#Plain}.
   */
  public WPushButton(final CharSequence text, WContainerWidget parentContainer) {
    super();
    this.linkState_ = new WAnchor.LinkState();
    this.text_ = new WText.RichText();
    this.icon_ = new WLink();
    this.flags_ = new BitSet();
    this.popupMenu_ = null;
    this.text_.format = TextFormat.Plain;
    this.text_.text = WString.toWString(text);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a push button with given label text.
   *
   * <p>Calls {@link #WPushButton(CharSequence text, WContainerWidget parentContainer) this(text,
   * (WContainerWidget)null)}
   */
  public WPushButton(final CharSequence text) {
    this(text, (WContainerWidget) null);
  }
  /** Creates a push button with given label text. */
  public WPushButton(final CharSequence text, TextFormat format, WContainerWidget parentContainer) {
    super();
    this.linkState_ = new WAnchor.LinkState();
    this.text_ = new WText.RichText();
    this.icon_ = new WLink();
    this.flags_ = new BitSet();
    this.popupMenu_ = null;
    this.text_.format = TextFormat.Plain;
    this.text_.text = WString.toWString(text);
    this.setTextFormat(format);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a push button with given label text.
   *
   * <p>Calls {@link #WPushButton(CharSequence text, TextFormat format, WContainerWidget
   * parentContainer) this(text, format, (WContainerWidget)null)}
   */
  public WPushButton(final CharSequence text, TextFormat format) {
    this(text, format, (WContainerWidget) null);
  }

  public void remove() {
    if (this.popupMenu_ != null) {
      this.popupMenu_.setButton((WInteractWidget) null);
    }
    super.remove();
  }
  /**
   * Sets the default property.
   *
   * <p>This has only a functional meaning for a button in a dialog footer, as it becomes associated
   * with pressing &apos;enter&apos; in the dialog.
   *
   * <p>A default button may be rendered in a different style, depending on the theme.
   */
  public void setDefault(boolean enabled) {
    this.flags_.set(BIT_DEFAULT, enabled);
  }
  /**
   * Returns whether the button is a default button.
   *
   * <p>
   *
   * @see WPushButton#setDefault(boolean enabled)
   */
  public boolean isDefault() {
    return this.flags_.get(BIT_DEFAULT);
  }
  /**
   * Sets whether the button is checkable.
   *
   * <p>A checkable button can be checked and unchecked, and clicking will toggle between these two
   * states.
   *
   * <p>
   *
   * @see WPushButton#setChecked(boolean checked)
   */
  public void setCheckable(boolean checkable) {
    this.flags_.set(BIT_IS_CHECKABLE, checkable);
    if (checkable) {
      this.clicked().addListener("function(o,e) { o.classList.toggle('active'); }");
      this.clicked()
          .addListener(
              this,
              (WMouseEvent e1) -> {
                WPushButton.this.toggled();
              });
    }
  }
  /**
   * Returns whether a button is checkable.
   *
   * <p>
   *
   * @see WPushButton#setCheckable(boolean checkable)
   */
  public boolean isCheckable() {
    return this.flags_.get(BIT_IS_CHECKABLE);
  }
  /**
   * Sets the button state.
   *
   * <p>This is ignored for a button which is not checkable.
   *
   * <p>This method does not emit one of the {@link WPushButton#checked() checked()} or {@link
   * WPushButton#unChecked() unChecked()} signals.
   *
   * <p>
   *
   * @see WPushButton#setCheckable(boolean checkable)
   * @see WPushButton#setChecked()
   * @see WPushButton#setUnChecked()
   */
  public void setChecked(boolean checked) {
    if (this.isCheckable()) {
      this.flags_.set(BIT_IS_CHECKED, checked);
      this.flags_.set(BIT_CHECKED_CHANGED, true);
      this.repaint();
    }
  }
  /**
   * Checks the button.
   *
   * <p>Does not emit the {@link WPushButton#checked() checked()} signal.
   *
   * <p>
   *
   * @see WPushButton#setChecked(boolean checked)
   */
  public void setChecked() {
    this.setChecked(true);
  }
  /**
   * Unchecks the button.
   *
   * <p>Does not emit the {@link WPushButton#unChecked() unChecked()} signal.
   *
   * <p>
   *
   * @see WPushButton#setChecked(boolean checked)
   */
  public void setUnChecked() {
    this.setChecked(false);
  }
  /**
   * Returns the button state.
   *
   * <p>
   *
   * @see WPushButton#setChecked()
   */
  public boolean isChecked() {
    return this.flags_.get(BIT_IS_CHECKED);
  }
  /**
   * Sets the button text.
   *
   * <p>The default text format is {@link TextFormat#Plain}.
   *
   * <p>When the current text format is {@link TextFormat#XHTML}, and <code>text</code> is literal
   * (not created using {@link WString#tr(String key) WString#tr()}), it is parsed using an XML
   * parser which discards malicious tags and attributes silently. When the parser encounters an XML
   * parse error, the textFormat is changed to {@link TextFormat#Plain}. If <code>text</code> is not
   * a literal, the same parser is applied only when the text is resolved.
   *
   * <p>Returns whether the text could be set using the current textFormat. A return value of <code>
   * false</code> indicates that the text format was changed in order to be able to accept the new
   * text.
   *
   * <p>
   *
   * @see WPushButton#setTextFormat(TextFormat textFormat)
   */
  public boolean setText(final CharSequence text) {
    if (canOptimizeUpdates() && (text.toString().equals(this.text_.text.toString()))) {
      return true;
    }
    boolean ok = this.text_.setText(text);
    this.flags_.set(BIT_TEXT_CHANGED);
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    return ok;
  }
  /**
   * Returns the button text.
   *
   * <p>
   *
   * @see WPushButton#setText(CharSequence text)
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
  public boolean setTextFormat(TextFormat textFormat) {
    return this.text_.setFormat(textFormat);
  }
  /**
   * Returns the text format.
   *
   * <p>
   *
   * @see WPushButton#setTextFormat(TextFormat textFormat)
   */
  public TextFormat getTextFormat() {
    return this.text_.format;
  }
  /**
   * Sets an icon.
   *
   * <p>The icon is placed to the left of the text.
   */
  public void setIcon(final WLink link) {
    if (canOptimizeUpdates() && link.equals(this.icon_)) {
      return;
    }
    this.icon_ = link;
    this.flags_.set(BIT_ICON_CHANGED);
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
  }
  /**
   * Returns the icon.
   *
   * <p>
   *
   * @see WPushButton#setIcon(WLink link)
   */
  public WLink getIcon() {
    return this.icon_;
  }
  /**
   * Sets a destination link.
   *
   * <p>This method can be used to make the button behave like a {@link WAnchor} (or conversely, an
   * anchor look like a button) and redirect to another URL when clicked.
   *
   * <p>The <code>link</code> may be to a URL, a resource, or an internal path.
   *
   * <p>By default, a button does not link to an URL and you should listen to the {@link
   * WInteractWidget#clicked()} signal to react to a click event.
   *
   * <p>
   *
   * <p><i><b>Warning: </b>In Bootstrap theme, you should set a link before it&apos;s rendered since
   * it commit&apos;s the button to be rendered as an anchor. (see also <a
   * href="http://redmine.emweb.be/issues/1802">http://redmine.emweb.be/issues/1802</a>). </i>
   */
  public void setLink(final WLink link) {
    if (link.equals(this.linkState_.link)) {
      return;
    }
    this.linkState_.link = link;
    this.flags_.set(BIT_LINK_CHANGED);
    if (this.linkState_.link.getType() == LinkType.Resource) {
      this.linkState_
          .link
          .getResource()
          .dataChanged()
          .addListener(
              this,
              () -> {
                WPushButton.this.resourceChanged();
              });
    }
    this.repaint();
  }
  /**
   * Returns the destination link.
   *
   * <p>
   *
   * @see WPushButton#setLink(WLink link)
   */
  public WLink getLink() {
    return this.linkState_.link;
  }
  /**
   * Returns the current value.
   *
   * <p>Returns an empty string, since a button has no value.
   */
  public String getValueText() {
    return "";
  }
  /**
   * Sets the current value.
   *
   * <p>Has no effect, since a button has not value.
   */
  public void setValueText(final String value) {}
  /**
   * Links a popup menu to the button.
   *
   * <p>When the button is clicked, the linked popup menu is shown.
   */
  public void setMenu(WPopupMenu popupMenu) {
    this.popupMenu_ = popupMenu;
    if (this.popupMenu_ != null) {
      this.popupMenu_.setButton(this);
    }
  }
  /**
   * Returns an associated popup menu.
   *
   * <p>
   *
   * @see WPushButton#setMenu(WPopupMenu popupMenu)
   */
  public WPopupMenu getMenu() {
    return this.popupMenu_;
  }

  public void refresh() {
    if (this.text_.text.refresh()) {
      this.flags_.set(BIT_TEXT_CHANGED);
      this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    }
    super.refresh();
  }
  /**
   * Signal emitted when the button gets checked.
   *
   * <p>This signal is emitted when the user checks the button.
   *
   * <p>You can use the {@link WInteractWidget#clicked()} signal to react to any change of the
   * button state.
   *
   * <p>
   *
   * @see WPushButton#setCheckable(boolean checkable)
   */
  public EventSignal checked() {
    return this.voidEventSignal(CHECKED_SIGNAL, true);
  }
  /**
   * Signal emitted when the button gets unchecked.
   *
   * <p>This signal is emitted when the user unchecks the button.
   *
   * <p>You can use the {@link WInteractWidget#clicked()} signal to react to any change of the
   * button state.
   *
   * <p>
   *
   * @see WPushButton#setCheckable(boolean checkable)
   */
  public EventSignal unChecked() {
    return this.voidEventSignal(UNCHECKED_SIGNAL, true);
  }

  public boolean isSetFirstFocus() {
    return false;
  }

  private static String CHECKED_SIGNAL = "M_checked";
  private static String UNCHECKED_SIGNAL = "M_unchecked";
  private static final int BIT_TEXT_CHANGED = 0;
  private static final int BIT_ICON_CHANGED = 1;
  private static final int BIT_ICON_RENDERED = 2;
  private static final int BIT_LINK_CHANGED = 3;
  private static final int BIT_DEFAULT = 4;
  private static final int BIT_IS_CHECKABLE = 5;
  private static final int BIT_IS_CHECKED = 6;
  private static final int BIT_CHECKED_CHANGED = 7;
  private WAnchor.LinkState linkState_;
  private WText.RichText text_;
  private WLink icon_;
  BitSet flags_;
  private WPopupMenu popupMenu_;

  void updateDom(final DomElement element, boolean all) {
    if (all && element.getType() == DomElementType.BUTTON) {
      element.setAttribute("type", "button");
    }
    boolean updateInnerHtml = !this.icon_.isNull() && this.flags_.get(BIT_TEXT_CHANGED);
    if (updateInnerHtml || this.flags_.get(BIT_ICON_CHANGED) || all && !this.icon_.isNull()) {
      DomElement image = DomElement.createNew(DomElementType.IMG);
      image.setProperty(Property.Src, this.icon_.resolveUrl(WApplication.getInstance()));
      image.setId("im" + this.getFormName());
      element.insertChildAt(image, 0);
      this.flags_.set(BIT_ICON_RENDERED);
      this.flags_.clear(BIT_ICON_CHANGED);
    }
    if (this.flags_.get(BIT_TEXT_CHANGED) || all) {
      element.setProperty(Property.InnerHTML, this.text_.getFormattedText());
      this.flags_.clear(BIT_TEXT_CHANGED);
    }
    if (this.flags_.get(BIT_LINK_CHANGED) || all) {
      if (element.getType() == DomElementType.A) {
        WAnchor.renderHRef(this, this.linkState_, element);
        WAnchor.renderHTarget(this.linkState_, element, all);
      } else {
        this.renderHRef(element);
      }
      this.flags_.clear(BIT_LINK_CHANGED);
    }
    if (this.isCheckable()) {
      if (this.flags_.get(BIT_CHECKED_CHANGED) || all) {
        if (!all || this.flags_.get(BIT_IS_CHECKED)) {
          this.toggleStyleClass("active", this.flags_.get(BIT_IS_CHECKED), true);
        }
        this.flags_.clear(BIT_CHECKED_CHANGED);
      }
    }
    if (!all) {
      WApplication.getInstance().getTheme().apply(this, element, ElementThemeRole.MainElement);
    }
    super.updateDom(element, all);
  }

  DomElementType getDomElementType() {
    if (!this.linkState_.link.isNull()) {
      WApplication app = WApplication.getInstance();
      if (app.getTheme().isCanStyleAnchorAsButton()) {
        return DomElementType.A;
      }
    }
    return DomElementType.BUTTON;
  }

  void propagateRenderOk(boolean deep) {
    this.flags_.clear(BIT_TEXT_CHANGED);
    this.flags_.clear(BIT_ICON_CHANGED);
    this.flags_.clear(BIT_LINK_CHANGED);
    this.flags_.clear(BIT_CHECKED_CHANGED);
    super.propagateRenderOk(deep);
  }

  protected void getDomChanges(final List<DomElement> result, WApplication app) {
    if (this.flags_.get(BIT_ICON_CHANGED) && this.flags_.get(BIT_ICON_RENDERED)) {
      DomElement image = DomElement.getForUpdate("im" + this.getFormName(), DomElementType.IMG);
      if (this.icon_.isNull()) {
        image.removeFromParent();
        this.flags_.clear(BIT_ICON_RENDERED);
      } else {
        image.setProperty(Property.Src, this.icon_.resolveUrl(app));
      }
      result.add(image);
      this.flags_.clear(BIT_ICON_CHANGED);
    }
    super.getDomChanges(result, app);
  }

  protected void propagateSetEnabled(boolean enabled) {
    super.propagateSetEnabled(enabled);
    this.flags_.set(BIT_LINK_CHANGED);
    this.repaint();
  }

  protected void enableAjax() {
    if (!this.linkState_.link.isNull()) {
      WApplication app = WApplication.getInstance();
      if (app.getTheme().isCanStyleAnchorAsButton()) {
        this.flags_.set(BIT_LINK_CHANGED);
        this.repaint();
      }
    }
    super.enableAjax();
  }

  private void doRedirect() {
    WApplication app = WApplication.getInstance();
    if (!app.getEnvironment().hasAjax()) {
      if (this.linkState_.link.getType() == LinkType.InternalPath) {
        app.setInternalPath(this.linkState_.link.getInternalPath(), true);
      } else {
        app.redirect(this.linkState_.link.getUrl());
      }
    }
  }

  private void resourceChanged() {
    this.flags_.set(BIT_LINK_CHANGED);
    this.repaint();
  }

  private void renderHRef(final DomElement element) {
    if (!this.linkState_.link.isNull() && !this.isDisabled()) {
      WApplication app = WApplication.getInstance();
      if (!(this.linkState_.clickJS != null)) {
        this.linkState_.clickJS = new JSlot();
        this.clicked().addListener(this.linkState_.clickJS);
        if (!app.getEnvironment().hasAjax()) {
          this.clicked()
              .addListener(
                  this,
                  (WMouseEvent e1) -> {
                    WPushButton.this.doRedirect();
                  });
        }
      }
      if (this.linkState_.link.getType() == LinkType.InternalPath) {
        this.linkState_.clickJS.setJavaScript(
            "function(){"
                + app.getJavaScriptClass()
                + "._p_.setHash("
                + jsStringLiteral(this.linkState_.link.getInternalPath())
                + ",true);}");
      } else {
        String url = this.linkState_.link.resolveUrl(app);
        if (this.linkState_.link.getTarget() == LinkTarget.NewWindow) {
          this.linkState_.clickJS.setJavaScript(
              "function(){window.open(" + jsStringLiteral(url) + ");}");
        } else {
          if (this.linkState_.link.getTarget() == LinkTarget.Download) {
            this.linkState_.clickJS.setJavaScript(
                "function(){var ifr = document.getElementById('wt_iframe_dl_id');ifr.src = "
                    + jsStringLiteral(url)
                    + ";}");
          } else {
            this.linkState_.clickJS.setJavaScript(
                "function(){window.location=" + jsStringLiteral(url) + ";}");
          }
        }
      }
      this.clicked().ownerRepaint();
    } else {

      this.linkState_.clickJS = null;
    }
  }

  private void toggled() {
    this.flags_.set(BIT_IS_CHECKED, !this.isChecked());
    if (this.isChecked()) {
      this.checked().trigger();
    } else {
      this.unChecked().trigger();
    }
  }
}
