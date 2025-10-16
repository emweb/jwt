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
 * A split button.
 *
 * <p>A split button combines a button and a drop down menu. Typically, the button represents an
 * action, with related alternative actions accessible from the drop down menu.
 */
public class WSplitButton extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WSplitButton.class);

  /** Constructor. */
  public WSplitButton(WContainerWidget parentContainer) {
    super();
    this.init(WString.Empty);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #WSplitButton(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WSplitButton() {
    this((WContainerWidget) null);
  }
  /** Constructor passing the label. */
  public WSplitButton(final CharSequence label, WContainerWidget parentContainer) {
    super();
    this.init(label);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor passing the label.
   *
   * <p>Calls {@link #WSplitButton(CharSequence label, WContainerWidget parentContainer) this(label,
   * (WContainerWidget)null)}
   */
  public WSplitButton(final CharSequence label) {
    this(label, (WContainerWidget) null);
  }
  /**
   * Returns the action button.
   *
   * <p>This is the button that represents the main action.
   */
  public WPushButton getActionButton() {
    return ObjectUtils.cast(this.impl_.widget(0), WPushButton.class);
  }
  /**
   * Returns the drop down button.
   *
   * <p>This represents the button that represents the drop-down action.
   */
  public WPushButton getDropDownButton() {
    return ObjectUtils.cast(this.impl_.widget(1), WPushButton.class);
  }
  /** Sets the menu for the drop-down button. */
  public void setMenu(WPopupMenu popupMenu) {
    this.getDropDownButton().setMenu(popupMenu);
  }
  /** Returns the menu for the drop-down button. */
  public WPopupMenu getMenu() {
    return this.getDropDownButton().getMenu();
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (this.getDropDownButton().isThemeStyleEnabled()) {
      this.getDropDownButton().addStyleClass("dropdown-toggle");
    }
    super.render(flags);
  }

  private WToolBar impl_;

  private void init(final CharSequence label) {
    this.setImplementation(this.impl_ = new WToolBar());
    this.impl_.setInline(true);
    this.impl_.addButton(new WPushButton(label, (WContainerWidget) null));
    this.impl_.addButton(new WPushButton());
  }
}
