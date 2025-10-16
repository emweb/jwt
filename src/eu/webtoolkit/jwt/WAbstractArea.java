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
 * Abstract base class for interactive areas in a widget.
 *
 * <p>Use an WAbstractArea (or rather, one of its concrete implementations), to define interactivity
 * that applies on a part of a {@link WImage} or {@link WPaintedWidget}. The area may be defined
 * using different shapes through {@link WRectArea}, {@link WCircleArea} or {@link WPolygonArea}.
 *
 * <p>
 *
 * @see WImage#addArea(WAbstractArea area)
 * @see WPaintedWidget#addArea(WAbstractArea area)
 */
public abstract class WAbstractArea extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WAbstractArea.class);

  /**
   * Specifies that this area specifies a hole for another area.
   *
   * <p>When set to <code>true</code>, this area will define an area that does not provide
   * interactivity. When it preceeds other, overlapping, areas, it acts as if it cuts a hole in
   * those areas.
   *
   * <p>The default value is <code>false</code>.
   *
   * <p>
   *
   * @see WAbstractArea#isHole()
   */
  public void setHole(boolean hole) {
    this.hole_ = hole;
    this.repaint();
  }
  /**
   * Returns whether this area specifies a hole.
   *
   * <p>
   *
   * @see WAbstractArea#setHole(boolean hole)
   */
  public boolean isHole() {
    return this.hole_;
  }

  public void setTransformable(boolean transformable) {
    this.transformable_ = transformable;
    this.repaint();
  }

  public boolean isTransformable() {
    return this.transformable_;
  }
  /**
   * Sets a link.
   *
   * <p>By setting a link, the area behaves like a {@link WAnchor}.
   *
   * <p>By default, no destination link is set.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Even when no destination link is set, in some circumstances, an identity URL
   * (&apos;#&apos;) will be linked to on the underlying HTML &lt;area&gt; element (see also {@link
   * WAbstractArea#setCursor(Cursor cursor) setCursor()}). </i>
   */
  public void setLink(final WLink link) {
    this.createAnchorImpl();
    this.anchor_.linkState.link = link;
    if (this.anchor_.linkState.link.getType() == LinkType.Resource) {
      this.anchor_
          .linkState
          .link
          .getResource()
          .dataChanged()
          .addListener(
              this,
              () -> {
                WAbstractArea.this.resourceChanged();
              });
    }
    this.repaint();
  }
  /**
   * Returns the link.
   *
   * <p>
   *
   * @see WAbstractArea#setLink(WLink link)
   */
  public WLink getLink() {
    if (!(this.anchor_ != null)) {
      return new WLink();
    } else {
      return this.anchor_.linkState.link;
    }
  }
  /**
   * Sets an alternate text.
   *
   * <p>The alternate text should provide a fallback for browsers that do not display an image. If
   * no sensible fallback text can be provided, an empty text is preferred over nonsense.
   *
   * <p>This should not be confused with {@link WAbstractArea#getToolTip() getToolTip()} text, which
   * provides additional information that is displayed when the mouse hovers over the area.
   *
   * <p>The default alternate text is an empty text (&quot;&quot;).
   *
   * <p>
   *
   * @see WAbstractArea#getAlternateText()
   */
  public void setAlternateText(final CharSequence text) {
    this.createAnchorImpl();
    this.anchor_.altText = WString.toWString(text);
    this.repaint();
  }
  /**
   * Returns the alternate text.
   *
   * <p>
   *
   * @see WAbstractArea#setAlternateText(CharSequence text)
   */
  public WString getAlternateText() {
    if (this.anchor_ != null) {
      return this.anchor_.altText;
    } else {
      return new WString();
    }
  }
  /**
   * Sets the tooltip.
   *
   * <p>The tooltip is displayed when the cursor hovers over the area.
   */
  public void setToolTip(final CharSequence text) {
    this.widget_.setToolTip(text);
  }
  /**
   * Returns the tooltip text.
   *
   * <p>
   *
   * @see WAbstractArea#setToolTip(CharSequence text)
   */
  public WString getToolTip() {
    return this.widget_.getToolTip();
  }
  /**
   * Defines a style class.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Only few CSS declarations are known to affect the look of a image area, the
   * most notable one being the &apos;cursor&apos;. Other things will simply be ignored. </i>
   */
  public void setStyleClass(final String styleClass) {
    this.widget_.setStyleClass(styleClass);
  }
  /**
   * Returns the style class.
   *
   * <p>
   *
   * @see WAbstractArea#setStyleClass(String styleClass)
   */
  public String getStyleClass() {
    return this.widget_.getStyleClass();
  }
  /**
   * Adds a style class.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Only few CSS declarations are known to affect the look of a image area, the
   * most notable one being the &apos;cursor&apos;. Other things will simply be ignored. </i>
   */
  public void addStyleClass(final String styleClass, boolean force) {
    this.widget_.addStyleClass(styleClass, force);
  }
  /**
   * Adds a style class.
   *
   * <p>Calls {@link #addStyleClass(String styleClass, boolean force) addStyleClass(styleClass,
   * false)}
   */
  public final void addStyleClass(final String styleClass) {
    addStyleClass(styleClass, false);
  }
  /** Removes a style class. */
  public void removeStyleClass(final String styleClass, boolean force) {
    this.widget_.removeStyleClass(styleClass, force);
  }
  /**
   * Removes a style class.
   *
   * <p>Calls {@link #removeStyleClass(String styleClass, boolean force)
   * removeStyleClass(styleClass, false)}
   */
  public final void removeStyleClass(final String styleClass) {
    removeStyleClass(styleClass, false);
  }
  /**
   * Sets the cursor.
   *
   * <p>This sets the mouse cursor that is shown when the mouse pointer is over the area. Most
   * browsers only support {@link Cursor#PointingHand}, which is activated by a non-empty ref.
   *
   * <p>
   */
  public void setCursor(Cursor cursor) {
    this.widget_.getDecorationStyle().setCursor(cursor);
  }
  /**
   * Sets a custom cursor image URL.
   *
   * <p>The URL should point to a .cur file. For optimal portability, make sure that the .cur file
   * is proparly constructed. A renamed .ico file will not work on Internet Explorer.
   */
  public void setCursor(String cursorImage, Cursor fallback) {
    this.widget_.getDecorationStyle().setCursor(cursorImage, fallback);
  }
  /**
   * Sets a custom cursor image URL.
   *
   * <p>Calls {@link #setCursor(String cursorImage, Cursor fallback) setCursor(cursorImage,
   * Cursor.Arrow)}
   */
  public final void setCursor(String cursorImage) {
    setCursor(cursorImage, Cursor.Arrow);
  }
  /**
   * Returns the cursor.
   *
   * <p>
   *
   * @see WAbstractArea#setCursor(Cursor cursor)
   */
  public Cursor getCursor() {
    return this.widget_.getDecorationStyle().getCursor();
  }

  WImage getImage() {
    return this.image_;
  }
  /**
   * Event signal emitted when a keyboard key is pushed down.
   *
   * <p>The keyWentDown signal is the first signal emitted when a key is pressed (before the {@link
   * WAbstractArea#keyPressed() keyPressed()} signal). Unlike {@link WAbstractArea#keyPressed()
   * keyPressed()} however it is also emitted for modifier keys (such as &quot;shift&quot;,
   * &quot;control&quot;, ...) or keyboard navigation keys that do not have a corresponding
   * character.
   *
   * <p>
   *
   * @see WAbstractArea#keyPressed()
   * @see WAbstractArea#keyWentUp()
   */
  public EventSignal1<WKeyEvent> keyWentDown() {
    return this.widget_.keyWentDown();
  }
  /**
   * Event signal emitted when a &quot;character&quot; was entered.
   *
   * <p>The keyPressed signal is emitted when a key is pressed, and a character is entered. Unlike
   * {@link WAbstractArea#keyWentDown() keyWentDown()}, it is emitted only for key presses that
   * result in a character being entered, and thus not for modifier keys or keyboard navigation
   * keys.
   *
   * <p>
   *
   * @see WAbstractArea#keyWentDown()
   */
  public EventSignal1<WKeyEvent> keyPressed() {
    return this.widget_.keyPressed();
  }
  /**
   * Event signal emitted when a keyboard key is released.
   *
   * <p>This is the counter-part of the {@link WAbstractArea#keyWentDown() keyWentDown()} event.
   * Every key-down has its corresponding key-up.
   *
   * <p>
   *
   * @see WAbstractArea#keyWentDown()
   */
  public EventSignal1<WKeyEvent> keyWentUp() {
    return this.widget_.keyWentUp();
  }
  /**
   * Event signal emitted when enter was pressed.
   *
   * <p>This signal is emitted when the Enter or Return key was pressed.
   *
   * <p>
   *
   * @see WAbstractArea#keyPressed()
   * @see Key#Enter
   */
  public EventSignal enterPressed() {
    return this.widget_.enterPressed();
  }
  /**
   * Event signal emitted when escape was pressed.
   *
   * <p>This signal is emitted when the Escape key was pressed.
   *
   * <p>
   *
   * @see WAbstractArea#keyPressed()
   * @see Key#Escape
   */
  public EventSignal escapePressed() {
    return this.widget_.escapePressed();
  }
  /**
   * Event signal emitted when a mouse key was clicked on this widget.
   *
   * <p>The event details contains information such as the {@link WMouseEvent#getButton()}, optional
   * {@link WMouseEvent#getModifiers() keyboard modifiers}, and mouse coordinates relative to the
   * {@link WMouseEvent#getWidget()}, the window {@link WMouseEvent#getWindow()}, or the {@link
   * WMouseEvent#getDocument()}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When JavaScript is disabled, the event details contain invalid information.
   * </i>
   */
  public EventSignal1<WMouseEvent> clicked() {
    return this.widget_.clicked();
  }
  /**
   * Event signal emitted when a mouse key was double clicked on this widget.
   *
   * <p>The event details contains information such as the {@link WMouseEvent#getButton()}, optional
   * {@link WMouseEvent#getModifiers() keyboard modifiers}, and mouse coordinates relative to the
   * {@link WMouseEvent#getWidget()}, the window {@link WMouseEvent#getWindow()}, or the {@link
   * WMouseEvent#getDocument()}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When JavaScript is disabled, the signal will never fire. </i>
   */
  public EventSignal1<WMouseEvent> doubleClicked() {
    return this.widget_.doubleClicked();
  }
  /**
   * Event signal emitted when a mouse key was pushed down on this widget.
   *
   * <p>The event details contains information such as the {@link WMouseEvent#getButton()}, optional
   * {@link WMouseEvent#getModifiers() keyboard modifiers}, and mouse coordinates relative to the
   * {@link WMouseEvent#getWidget()}, the window {@link WMouseEvent#getWindow()}, or the {@link
   * WMouseEvent#getDocument()}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When JavaScript is disabled, the signal will never fire. </i>
   */
  public EventSignal1<WMouseEvent> mouseWentDown() {
    return this.widget_.mouseWentDown();
  }
  /**
   * Event signal emitted when a mouse key was released on this widget.
   *
   * <p>The event details contains information such as the {@link WMouseEvent#getButton()}, optional
   * {@link WMouseEvent#getModifiers() keyboard modifiers}, and mouse coordinates relative to the
   * {@link WMouseEvent#getWidget()}, the window {@link WMouseEvent#getWindow()}, or the {@link
   * WMouseEvent#getDocument()}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When JavaScript is disabled, the signal will never fire. </i>
   */
  public EventSignal1<WMouseEvent> mouseWentUp() {
    return this.widget_.mouseWentUp();
  }
  /**
   * Event signal emitted when the mouse went out of this widget.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When JavaScript is disabled, the signal will never fire. </i>
   */
  public EventSignal1<WMouseEvent> mouseWentOut() {
    return this.widget_.mouseWentOut();
  }
  /**
   * Event signal emitted when the mouse entered this widget.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When JavaScript is disabled, the signal will never fire. </i>
   */
  public EventSignal1<WMouseEvent> mouseWentOver() {
    return this.widget_.mouseWentOver();
  }
  /**
   * Event signal emitted when the mouse moved over this widget.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When JavaScript is disabled, the signal will never fire. </i>
   */
  public EventSignal1<WMouseEvent> mouseMoved() {
    return this.widget_.mouseMoved();
  }
  /**
   * Event signal emitted when the mouse is dragged over this widget.
   *
   * <p>The mouse event contains information on the button(s) currently pressed. If multiple buttons
   * are currently pressed, only the button with smallest enum value is returned.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When JavaScript is disabled, the signal will never fire. </i>
   */
  public EventSignal1<WMouseEvent> mouseDragged() {
    return this.widget_.mouseDragged();
  }
  /**
   * Event signal emitted when the mouse scroll wheel was used.
   *
   * <p>The event details contains information such as the {@link WMouseEvent#getWheelDelta() wheel
   * delta}, optional {@link WMouseEvent#getModifiers() keyboard modifiers}, and mouse coordinates
   * relative to the {@link WMouseEvent#getWidget()}, the window {@link WMouseEvent#getWindow()}, or
   * the {@link WMouseEvent#getDocument()}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When JavaScript is disabled, the signal will never fire. </i>
   */
  public EventSignal1<WMouseEvent> mouseWheel() {
    return this.widget_.mouseWheel();
  }

  public void setObjectName(final String name) {
    this.widget_.setObjectName(name);
    super.setObjectName(name);
  }

  WAbstractArea() {
    super();
    this.uWidget_ = new AreaWidget(this);
    this.widget_ = this.uWidget_;
    this.hole_ = false;
    this.transformable_ = true;
    this.anchor_ = null;
    this.image_ = null;
  }

  protected boolean updateDom(final DomElement element, boolean all) {
    boolean needsUrlResolution = false;
    if (!this.hole_ && this.anchor_ != null) {
      needsUrlResolution = WAnchor.renderHRef(this.widget_, this.anchor_.linkState, element);
      WAnchor.renderHTarget(this.anchor_.linkState, element, all);
      element.setAttribute("alt", this.anchor_.altText.toString());
    } else {
      element.setAttribute("alt", "");
      if (this.hole_) {
        element.setAttribute("nohref", "nohref");
      }
    }
    return needsUrlResolution;
  }

  protected abstract String getUpdateAreaCoordsJS();

  void repaint() {
    this.widget_.repaint();
  }

  protected String getJsRef() {
    return this.widget_.getJsRef();
  }

  static class AnchorImpl {
    private static Logger logger = LoggerFactory.getLogger(AnchorImpl.class);

    public AnchorImpl() {
      this.linkState = new WAnchor.LinkState();
      this.altText = new WString();
    }

    public WAnchor.LinkState linkState;
    public WString altText;
  }

  private AreaWidget uWidget_;
  private AreaWidget widget_;
  private boolean hole_;
  private boolean transformable_;
  private WAbstractArea.AnchorImpl anchor_;
  private WImage image_;

  private void createAnchorImpl() {
    if (!(this.anchor_ != null)) {
      this.anchor_ = new WAbstractArea.AnchorImpl();
    }
  }

  private void resourceChanged() {
    this.repaint();
  }

  WInteractWidget getWidget() {
    return this.widget_;
  }

  private void setImage(WImage image) {
    this.image_ = image;
  }

  WWidget takeWidget() {
    return this.uWidget_;
  }

  void returnWidget(WWidget w) {
    this.uWidget_ = ObjectUtils.cast(w, AreaWidget.class);
  }
}
