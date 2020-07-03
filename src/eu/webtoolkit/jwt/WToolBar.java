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
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A toolbar.
 *
 * <p>By default, a toolbar is rendered as &quot;compact&quot; leaving no margin between buttons. By
 * adding a separator or a split button, the toolbar also supports separation between buttons.
 */
public class WToolBar extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WToolBar.class);

  /** Constructor. */
  public WToolBar(WContainerWidget parentContainer) {
    super();
    this.compact_ = true;
    this.lastGroup_ = null;
    this.setImplementation(this.impl_ = new WContainerWidget());
    this.setStyleClass("btn-group");
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #WToolBar(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WToolBar() {
    this((WContainerWidget) null);
  }
  /**
   * Set vertical or horizontal orientation.
   *
   * <p>Use bootstrap btn-group-vertical style for vertical orientation.
   */
  public void setOrientation(Orientation orientation) {
    if (orientation == Orientation.Vertical) {
      this.addStyleClass("btn-group-vertical");
    } else {
      this.removeStyleClass("btn-group-vertical");
    }
  }
  /** Adds a button. */
  public void addButton(WPushButton button, AlignmentFlag alignmentFlag) {
    if (this.compact_) {
      if (alignmentFlag == AlignmentFlag.Right) {
        button.setAttributeValue("style", "float:right;");
      }
      this.impl_.addWidget(button);
    } else {
      if (alignmentFlag == AlignmentFlag.Right) {
        this.getLastGroup().setAttributeValue("style", "float:right;");
      }
      this.getLastGroup().addWidget(button);
    }
  }
  /**
   * Adds a button.
   *
   * <p>Calls {@link #addButton(WPushButton button, AlignmentFlag alignmentFlag) addButton(button,
   * AlignmentFlag.Left)}
   */
  public final void addButton(WPushButton button) {
    addButton(button, AlignmentFlag.Left);
  }
  /**
   * Adds a split button.
   *
   * <p>When adding a split button, the toolbar automatically becomes non-compact, since otherwise
   * the split button functionality cannot be distinguished from other buttons.
   *
   * <p>
   *
   * @see WToolBar#setCompact(boolean compact)
   */
  public void addButton(WSplitButton button, AlignmentFlag alignmentFlag) {
    this.setCompact(false);
    this.lastGroup_ = null;
    if (alignmentFlag == AlignmentFlag.Right) {
      button.setAttributeValue("style", "float:right;");
    }
    this.impl_.addWidget(button);
  }
  /**
   * Adds a split button.
   *
   * <p>Calls {@link #addButton(WSplitButton button, AlignmentFlag alignmentFlag) addButton(button,
   * AlignmentFlag.Left)}
   */
  public final void addButton(WSplitButton button) {
    addButton(button, AlignmentFlag.Left);
  }
  /**
   * Adds a widget.
   *
   * <p>The toolbar automatically becomes non-compact.
   */
  public void addWidget(WWidget widget, AlignmentFlag alignmentFlag) {
    this.setCompact(false);
    this.lastGroup_ = null;
    if (alignmentFlag == AlignmentFlag.Right) {
      widget.setAttributeValue("style", "float:right;");
    }
    this.impl_.addWidget(widget);
  }
  /**
   * Adds a widget.
   *
   * <p>Calls {@link #addWidget(WWidget widget, AlignmentFlag alignmentFlag) addWidget(widget,
   * AlignmentFlag.Left)}
   */
  public final void addWidget(WWidget widget) {
    addWidget(widget, AlignmentFlag.Left);
  }

  public WWidget removeWidget(WWidget widget) {
    WWidget p = widget.getParent();
    if (p == this.impl_) {
      return this.impl_.removeWidget(widget);
    } else {
      int i = this.impl_.getIndexOf(p);
      if (i >= 0) {
        WContainerWidget cw = ((p) instanceof WContainerWidget ? (WContainerWidget) (p) : null);
        if (cw != null) {
          WWidget result = cw.removeWidget(widget);
          if (cw.getCount() == 0) {
            {
              WWidget toRemove = WidgetUtils.remove(cw.getParent(), cw);
              if (toRemove != null) toRemove.remove();
            }
          }
          return result;
        }
      }
    }
    return null;
  }
  /**
   * Adds a separator.
   *
   * <p>The toolbar automatically becomes non-compact.
   *
   * <p>
   *
   * @see WToolBar#setCompact(boolean compact)
   */
  public void addSeparator() {
    this.setCompact(false);
    this.lastGroup_ = null;
  }
  /**
   * Returns the number of buttons.
   *
   * <p>
   *
   * @see WToolBar#widget(int index)
   */
  public int getCount() {
    if (this.compact_) {
      return this.impl_.getCount();
    } else {
      int result = 0;
      for (int i = 0; i < this.impl_.getCount(); ++i) {
        WWidget w = this.impl_.getWidget(i);
        if (((w) instanceof WSplitButton ? (WSplitButton) (w) : null) != null) {
          ++result;
        } else {
          WContainerWidget group =
              ((w) instanceof WContainerWidget ? (WContainerWidget) (w) : null);
          result += group.getCount();
        }
      }
      return result;
    }
  }
  /**
   * Returns a button.
   *
   * <p>The returned widget is a {@link WPushButton} or {@link WSplitButton} added by {@link
   * WToolBar#addButton(WPushButton button, AlignmentFlag alignmentFlag) addButton()} or a widget
   * added by {@link WToolBar#addWidget(WWidget widget, AlignmentFlag alignmentFlag) addWidget()}.
   */
  public WWidget widget(int index) {
    if (this.compact_) {
      return this.impl_.getWidget(index);
    } else {
      int current = 0;
      for (int i = 0; i < this.impl_.getCount(); ++i) {
        WWidget w = this.impl_.getWidget(i);
        if (((w) instanceof WSplitButton ? (WSplitButton) (w) : null) != null) {
          if (index == current) {
            return w;
          }
          ++current;
        } else {
          WContainerWidget group =
              ((w) instanceof WContainerWidget ? (WContainerWidget) (w) : null);
          if (index < current + group.getCount()) {
            return group.getWidget(index - current);
          }
          current += group.getCount();
        }
      }
      return null;
    }
  }
  /**
   * Sets the toolbar to be rendered compact.
   *
   * <p>The default value is <code>true</code>, but <code>setCompact(true)</code> is called
   * automatically when calling addButton(WSplitButton *) or {@link WToolBar#addSeparator()
   * addSeparator()}.
   */
  public void setCompact(boolean compact) {
    if (compact != this.compact_) {
      this.compact_ = compact;
      if (compact) {
        if (this.impl_.getCount() > 0) {
          logger.info(new StringWriter().append("setCompact(true): not implemented").toString());
        }
        this.setStyleClass("btn-group");
      } else {
        this.setStyleClass("btn-toolbar");
        if (this.impl_.getCount() > 0) {
          WContainerWidget group = new WContainerWidget();
          group.setStyleClass("btn-group");
          while (this.impl_.getCount() > 0) {
            WWidget w = this.impl_.removeWidget(this.impl_.getWidget(0));
            group.addWidget(w);
          }
          this.lastGroup_ = group;
          this.impl_.addWidget(group);
        }
      }
    }
  }
  /**
   * Returns whether the toolbar was rendered compact.
   *
   * <p>
   *
   * @see WToolBar#setCompact(boolean compact)
   */
  public boolean isCompact() {
    return this.compact_;
  }

  private boolean compact_;
  private WContainerWidget impl_;
  private WContainerWidget lastGroup_;

  private WContainerWidget getLastGroup() {
    if (!(this.lastGroup_ != null)) {
      this.lastGroup_ = new WContainerWidget();
      this.impl_.addWidget(this.lastGroup_);
      this.lastGroup_.addStyleClass("btn-group");
    }
    return this.lastGroup_;
  }
}
