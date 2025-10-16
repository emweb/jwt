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
 * A widget that hides the implementation of composite widgets.
 *
 * <p>Composite widgets, built on top of the WebWidgets, should derive from this class, and use
 * {@link WCompositeWidget#setImplementation(WWidget widget) setImplementation()} to set the widget
 * that implements the composite widget (which is typically a {@link WContainerWidget} or a {@link
 * WTable}, or another widget that allows composition, including perhaps another WCompositeWidget).
 *
 * <p>Using this class you can completely hide the implementation of your composite widget, and
 * provide access to only the standard {@link WWidget} methods.
 */
public class WCompositeWidget extends WWidget {
  private static Logger logger = LoggerFactory.getLogger(WCompositeWidget.class);

  /**
   * Creates a WCompositeWidget.
   *
   * <p>You need to set an implemetation using {@link WCompositeWidget#setImplementation(WWidget
   * widget) setImplementation()} directly after construction.
   */
  public WCompositeWidget(WContainerWidget parentContainer) {
    super();
    this.impl_ = null;
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a WCompositeWidget.
   *
   * <p>Calls {@link #WCompositeWidget(WContainerWidget parentContainer)
   * this((WContainerWidget)null)}
   */
  public WCompositeWidget() {
    this((WContainerWidget) null);
  }
  /**
   * Creates a WCompositeWidget with given implementation.
   *
   * <p>
   *
   * @see WCompositeWidget#setImplementation(WWidget widget)
   */
  public WCompositeWidget(WWidget implementation, WContainerWidget parentContainer) {
    super();
    this.impl_ = null;
    this.setImplementation(implementation);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a WCompositeWidget with given implementation.
   *
   * <p>Calls {@link #WCompositeWidget(WWidget implementation, WContainerWidget parentContainer)
   * this(implementation, (WContainerWidget)null)}
   */
  public WCompositeWidget(WWidget implementation) {
    this(implementation, (WContainerWidget) null);
  }

  public void remove() {
    if (this.impl_ != null) {
      {
        WWidget toRemove = this.impl_.removeFromParent();
        if (toRemove != null) toRemove.remove();
      }
    }
    {
      WWidget toRemove = this.removeFromParent();
      if (toRemove != null) toRemove.remove();
    }

    super.remove();
  }

  public List<WWidget> getChildren() {
    List<WWidget> result = new ArrayList<WWidget>();
    result.add(this.impl_);
    return result;
  }

  public WWidget removeWidget(WWidget child) {
    child.setParentWidget((WWidget) null);
    if (this.impl_ != null) {
      {
        WWidget toRemove = this.impl_.removeFromParent();
        if (toRemove != null) toRemove.remove();
      }
    }
    return null;
  }

  public void setObjectName(final String name) {
    this.impl_.setObjectName(name);
  }

  public String getObjectName() {
    return this.impl_.getObjectName();
  }

  public String getId() {
    return this.impl_.getId();
  }

  public void setPositionScheme(PositionScheme scheme) {
    this.impl_.setPositionScheme(scheme);
  }

  public PositionScheme getPositionScheme() {
    if (this.impl_ != null) {
      return this.impl_.getPositionScheme();
    } else {
      return PositionScheme.Static;
    }
  }

  public void setOffsets(final WLength offset, EnumSet<Side> sides) {
    this.impl_.setOffsets(offset, sides);
  }

  public WLength getOffset(Side s) {
    return this.impl_.getOffset(s);
  }

  public void resize(final WLength width, final WLength height) {
    this.impl_.resize(width, height);
    super.resize(width, height);
  }

  public WLength getWidth() {
    return this.impl_.getWidth();
  }

  public WLength getHeight() {
    return this.impl_.getHeight();
  }

  public void setMinimumSize(final WLength width, final WLength height) {
    this.impl_.setMinimumSize(width, height);
  }

  public WLength getMinimumWidth() {
    return this.impl_.getMinimumWidth();
  }

  public WLength getMinimumHeight() {
    return this.impl_.getMinimumHeight();
  }

  public void setMaximumSize(final WLength width, final WLength height) {
    this.impl_.setMaximumSize(width, height);
  }

  public WLength getMaximumWidth() {
    return this.impl_.getMaximumWidth();
  }

  public WLength getMaximumHeight() {
    return this.impl_.getMaximumHeight();
  }

  public void setLineHeight(final WLength height) {
    this.impl_.setLineHeight(height);
  }

  public WLength getLineHeight() {
    return this.impl_.getLineHeight();
  }

  public void setFloatSide(Side s) {
    this.impl_.setFloatSide(s);
  }

  public Side getFloatSide() {
    return this.impl_.getFloatSide();
  }

  public void setClearSides(EnumSet<Side> sides) {
    this.impl_.setClearSides(sides);
  }

  public EnumSet<Side> getClearSides() {
    return this.impl_.getClearSides();
  }

  public void setMargin(final WLength margin, EnumSet<Side> sides) {
    this.impl_.setMargin(margin, sides);
  }

  public WLength getMargin(Side side) {
    return this.impl_.getMargin(side);
  }

  public void setHiddenKeepsGeometry(boolean enabled) {
    this.impl_.setHiddenKeepsGeometry(enabled);
  }

  public boolean isHiddenKeepsGeometry() {
    return this.impl_.isHiddenKeepsGeometry();
  }

  public void setHidden(boolean hidden, final WAnimation animation) {
    this.impl_.setHidden(hidden, animation);
  }

  public boolean isHidden() {
    return this.impl_.isHidden();
  }

  public boolean isVisible() {
    if (this.isHidden()) {
      return false;
    } else {
      if (this.getParent() != null) {
        return this.getParent().isVisible();
      } else {
        return false;
      }
    }
  }

  public void setDisabled(boolean disabled) {
    this.impl_.setDisabled(disabled);
    this.propagateSetEnabled(!disabled);
  }

  public boolean isDisabled() {
    if (!(this.impl_ != null)) {
      return false;
    }
    return this.impl_.isDisabled();
  }

  public boolean isEnabled() {
    if (this.isDisabled()) {
      return false;
    } else {
      if (this.getParent() != null) {
        return this.getParent().isEnabled();
      } else {
        return true;
      }
    }
  }

  public void setPopup(boolean popup) {
    this.impl_.setPopup(popup);
  }

  public boolean isPopup() {
    return this.impl_.isPopup();
  }

  public void setInline(boolean isInline) {
    // this.resetLearnedSlot(WWidget.show);
    this.impl_.setInline(isInline);
  }

  public boolean isInline() {
    return this.impl_.isInline();
  }

  public void setDecorationStyle(final WCssDecorationStyle style) {
    this.impl_.setDecorationStyle(style);
  }

  public WCssDecorationStyle getDecorationStyle() {
    return this.impl_.getDecorationStyle();
  }

  public void setStyleClass(final String styleClass) {
    this.impl_.setStyleClass(styleClass);
  }

  public String getStyleClass() {
    return this.impl_.getStyleClass();
  }

  public void addStyleClass(final String styleClass, boolean force) {
    this.impl_.addStyleClass(styleClass, force);
  }

  public void removeStyleClass(final String styleClass, boolean force) {
    this.impl_.removeStyleClass(styleClass, force);
  }

  public boolean hasStyleClass(final String styleClass) {
    return this.impl_.hasStyleClass(styleClass);
  }

  public void setVerticalAlignment(AlignmentFlag alignment, final WLength length) {
    if (AlignmentFlag.AlignHorizontalMask.contains(alignment)) {
      logger.error(
          new StringWriter()
              .append("setVerticalAlignment(): alignment ")
              .append(String.valueOf(alignment.getValue()))
              .append("is not vertical")
              .toString());
    }
    this.impl_.setVerticalAlignment(alignment, length);
  }

  public AlignmentFlag getVerticalAlignment() {
    return this.impl_.getVerticalAlignment();
  }

  public WLength getVerticalAlignmentLength() {
    return this.impl_.getVerticalAlignmentLength();
  }

  WWebWidget getWebWidget() {
    return this.impl_ != null ? this.impl_.getWebWidget() : null;
  }

  public void setToolTip(final CharSequence text, TextFormat textFormat) {
    this.impl_.setToolTip(text, textFormat);
  }

  public WString getToolTip() {
    return this.impl_.getToolTip();
  }

  public void setDeferredToolTip(boolean enable, TextFormat textFormat) {
    this.impl_.setDeferredToolTip(enable, textFormat);
  }

  public void refresh() {
    this.impl_.refresh();
    super.refresh();
  }

  public void setAttributeValue(final String name, final String value) {
    this.impl_.setAttributeValue(name, value);
  }

  public String getAttributeValue(final String name) {
    return this.impl_.getAttributeValue(name);
  }

  public void setJavaScriptMember(final String name, final String value) {
    this.impl_.setJavaScriptMember(name, value);
  }

  public String getJavaScriptMember(final String name) {
    return this.impl_.getJavaScriptMember(name);
  }

  public void callJavaScriptMember(final String name, final String args) {
    this.impl_.callJavaScriptMember(name, args);
  }

  public void load() {
    if (this.impl_ != null) {
      this.impl_.load();
    }
  }

  public boolean isLoaded() {
    return this.impl_ != null ? this.impl_.isLoaded() : true;
  }

  public void setCanReceiveFocus(boolean enabled) {
    this.impl_.setCanReceiveFocus(enabled);
  }

  public boolean isCanReceiveFocus() {
    return this.impl_.isCanReceiveFocus();
  }

  public void setFocus(boolean focus) {
    this.impl_.setFocus(true);
  }

  public boolean isSetFirstFocus() {
    return this.impl_.getWebWidget().isSetFirstFocus();
  }

  public boolean hasFocus() {
    return this.impl_.hasFocus();
  }

  public void setTabIndex(int index) {
    this.impl_.setTabIndex(index);
  }

  public int getTabIndex() {
    return this.impl_.getTabIndex();
  }

  int getZIndex() {
    return this.impl_.getZIndex();
  }

  public void setId(final String id) {
    this.impl_.setId(id);
  }

  public WWidget find(final String name) {
    if (this.getObjectName().equals(name)) {
      return this;
    } else {
      return this.impl_.find(name);
    }
  }

  public WWidget findById(final String id) {
    if (this.getId().equals(id)) {
      return this;
    } else {
      return this.impl_.findById(id);
    }
  }

  public void setSelectable(boolean selectable) {
    this.impl_.setSelectable(selectable);
  }

  public void doJavaScript(final String js) {
    this.impl_.doJavaScript(js);
  }

  public void propagateSetEnabled(boolean enabled) {
    this.impl_.getWebWidget().propagateSetEnabled(enabled);
  }

  public void propagateSetVisible(boolean visible) {
    this.impl_.getWebWidget().propagateSetVisible(visible);
  }

  public boolean isScrollVisibilityEnabled() {
    return this.impl_.getWebWidget().isScrollVisibilityEnabled();
  }

  public void setScrollVisibilityEnabled(boolean enabled) {
    this.impl_.getWebWidget().setScrollVisibilityEnabled(enabled);
  }

  public int getScrollVisibilityMargin() {
    return this.impl_.getWebWidget().getScrollVisibilityMargin();
  }

  public void setScrollVisibilityMargin(int margin) {
    this.impl_.getWebWidget().setScrollVisibilityMargin(margin);
  }

  public Signal1<Boolean> scrollVisibilityChanged() {
    return this.impl_.getWebWidget().scrollVisibilityChanged();
  }

  public boolean isScrollVisible() {
    return this.impl_.getWebWidget().isScrollVisible();
  }

  public void setThemeStyleEnabled(boolean enabled) {
    this.impl_.setThemeStyleEnabled(enabled);
  }

  public boolean isThemeStyleEnabled() {
    return this.impl_.isThemeStyleEnabled();
  }

  public int getBaseZIndex() {
    return this.impl_.getBaseZIndex();
  }

  void setHideWithOffsets(boolean hideWithOffsets) {
    this.impl_.setHideWithOffsets(hideWithOffsets);
  }

  boolean isStubbed() {
    if (this.getParent() != null) {
      return this.getParent().isStubbed();
    } else {
      return false;
    }
  }

  protected void enableAjax() {
    this.impl_.enableAjax();
  }
  /**
   * Set the implementation widget.
   *
   * <p>This sets the widget that implements this compositeWidget. Ownership of the widget is
   * completely transferred (including deletion).
   *
   * <p>
   *
   * <p><i><b>Note: </b>You cannot change the implementation of a composite widget after it has been
   * rendered. </i>
   */
  protected void setImplementation(WWidget widget) {
    this.impl_ = widget;
    this.impl_.setParentWidget(this);
    WWidget p = this.getParent();
    if (p != null && p.isLoaded()) {
      this.impl_.load();
    }
  }
  // protected Widget  setImplementation(<Woow... some pseudoinstantiation type!> widget) ;
  // protected W  getSetNewImplementation() ;
  // protected W  setNewImplementation(Arg1) ;
  // protected W  setNewImplementation(Arg1, Arg2) ;
  // protected W  setNewImplementation(Arg1, Arg2, Arg3) ;
  /**
   * Get the implementation widget.
   *
   * <p>This returns the widget that implements this compositeWidget.
   */
  protected WWidget getImplementation() {
    return this.impl_;
  }

  protected WWidget getTakeImplementation() {
    return this.impl_;
  }

  void getSDomChanges(final List<DomElement> result, WApplication app) {
    if (this.needsToBeRendered()) {
      this.render(
          this.impl_.isRendered() || !WWebWidget.canOptimizeUpdates()
              ? RenderFlag.Update
              : RenderFlag.Full);
    }
    this.impl_.getSDomChanges(result, app);
  }

  boolean needsToBeRendered() {
    return this.impl_.needsToBeRendered();
  }

  protected int boxPadding(Orientation orientation) {
    return this.impl_.boxPadding(orientation);
  }

  protected int boxBorder(Orientation orientation) {
    return this.impl_.boxBorder(orientation);
  }

  protected void render(EnumSet<RenderFlag> flags) {
    this.impl_.render(flags);
    super.render(flags);
    this.renderOk();
  }

  String renderRemoveJs(boolean recursive) {
    return this.impl_.renderRemoveJs(recursive);
  }

  protected void setParentWidget(WWidget parent) {
    if (parent != null && !this.isDisabled()) {
      this.propagateSetEnabled(parent.isEnabled());
    }
    super.setParentWidget(parent);
  }

  private WWidget impl_;
}
