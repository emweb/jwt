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
    this.flags_ = new BitSet();
    this.compact_ = true;
    this.orientation_ = Orientation.Horizontal;
    this.lastGroup_ = null;
    this.nextUnrenderedGroup_ = 0;
    this.widgets_ = new ArrayList<WWidget>();
    this.setImplementation(this.impl_ = new WContainerWidget());
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
    if (this.orientation_ != orientation) {
      this.orientation_ = orientation;
      this.flags_.set(BIT_ORIENTATION_CHANGED);
      this.scheduleRender();
    }
  }
  /** Adds a button. */
  public WPushButton addButton(WPushButton button, AlignmentFlag alignmentFlag) {
    WPushButton result = button;
    this.widgets_.add(button);
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
    return result;
  }
  /**
   * Adds a button.
   *
   * <p>Returns {@link #addButton(WPushButton button, AlignmentFlag alignmentFlag) addButton(button,
   * AlignmentFlag.Left)}
   */
  public final WPushButton addButton(WPushButton button) {
    return addButton(button, AlignmentFlag.Left);
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
  public WSplitButton addButton(WSplitButton button, AlignmentFlag alignmentFlag) {
    WSplitButton result = button;
    this.widgets_.add(button);
    this.setCompact(false);
    this.lastGroup_ = null;
    if (alignmentFlag == AlignmentFlag.Right) {
      button.setAttributeValue("style", "float:right;");
    }
    this.impl_.addWidget(button);
    return result;
  }
  /**
   * Adds a split button.
   *
   * <p>Returns {@link #addButton(WSplitButton button, AlignmentFlag alignmentFlag)
   * addButton(button, AlignmentFlag.Left)}
   */
  public final WSplitButton addButton(WSplitButton button) {
    return addButton(button, AlignmentFlag.Left);
  }
  /**
   * Adds a widget.
   *
   * <p>The toolbar automatically becomes non-compact.
   */
  public void addWidget(WWidget widget, AlignmentFlag alignmentFlag) {
    this.widgets_.add(widget);
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
  // public Widget  addWidget(<Woow... some pseudoinstantiation type!> widget, AlignmentFlag
  // alignmentFlag) ;
  public WWidget removeWidget(WWidget widget) {
    int idx = this.widgets_.indexOf(widget);
    if (idx != -1) {
      WContainerWidget parent = (WContainerWidget) widget.getParent();
      WWidget retval = parent.removeWidget(widget);
      if (parent != this.impl_ && parent.getCount() == 0) {
        {
          WWidget toRemove = WidgetUtils.remove(this.impl_, parent);
          if (toRemove != null) toRemove.remove();
        }

        if (this.lastGroup_ == parent) {
          this.lastGroup_ = null;
        }
      }
      this.widgets_.remove(0 + idx);
      return retval;
    } else {
      return null;
    }
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
   * Returns the number of widgets.
   *
   * <p>The counted widgets are either a {@link WPushButton} or {@link WSplitButton} added by {@link
   * WToolBar#addButton(WPushButton button, AlignmentFlag alignmentFlag) addButton()} or a widget
   * added by {@link WToolBar#addWidget(WWidget widget, AlignmentFlag alignmentFlag) addWidget()}.
   *
   * <p>
   *
   * @see WToolBar#widget(int index)
   */
  public int getCount() {
    return this.widgets_.size();
  }
  /**
   * Returns a button.
   *
   * <p>The returned widget is a {@link WPushButton} or {@link WSplitButton} added by {@link
   * WToolBar#addButton(WPushButton button, AlignmentFlag alignmentFlag) addButton()} or a widget
   * added by {@link WToolBar#addWidget(WWidget widget, AlignmentFlag alignmentFlag) addWidget()}.
   */
  public WWidget widget(int index) {
    if (index < (int) this.widgets_.size()) {
      return this.widgets_.get(index);
    } else {
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
      } else {
        if (this.impl_.getCount() > 0) {
          WContainerWidget group = new WContainerWidget();
          while (this.impl_.getCount() > 0) {
            WWidget w = this.impl_.removeWidget(this.impl_.getWidget(0));
            group.addWidget(w);
          }
          this.lastGroup_ = group;
          this.impl_.addWidget(group);
          this.flags_.set(BIT_MULTIPLE_GROUPS);
        }
      }
      this.flags_.set(BIT_COMPACT_CHANGED);
      this.scheduleRender();
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

  protected void render(EnumSet<RenderFlag> flags) {
    boolean all = flags.contains(RenderFlag.Full);
    if (this.isThemeStyleEnabled()) {
      if (this.flags_.get(BIT_COMPACT_CHANGED) || all) {
        if (this.compact_) {
          this.removeStyleClass("btn-toolbar");
          this.addStyleClass("btn-group");
        } else {
          this.removeStyleClass("btn-group");
          this.addStyleClass("btn-toolbar");
        }
        this.flags_.clear(BIT_COMPACT_CHANGED);
      }
      if (this.flags_.get(BIT_MULTIPLE_GROUPS)) {
        for (int i = this.nextUnrenderedGroup_; i < this.impl_.getCount(); ++i) {
          this.impl_.getChildren().get(i).addStyleClass("btn-group me-2");
        }
        this.nextUnrenderedGroup_ = this.impl_.getCount();
      }
      if (this.flags_.get(BIT_ORIENTATION_CHANGED) || all) {
        if (this.orientation_ == Orientation.Vertical) {
          this.addStyleClass("btn-group-vertical");
        } else {
          this.removeStyleClass("btn-group-vertical");
        }
        this.flags_.clear(BIT_ORIENTATION_CHANGED);
      }
    }
  }

  private static final int BIT_COMPACT_CHANGED = 0;
  private static final int BIT_ORIENTATION_CHANGED = 1;
  private static final int BIT_MULTIPLE_GROUPS = 2;
  private BitSet flags_;
  private boolean compact_;
  private Orientation orientation_;
  private WContainerWidget impl_;
  private WContainerWidget lastGroup_;
  private int nextUnrenderedGroup_;
  private List<WWidget> widgets_;

  private WContainerWidget getLastGroup() {
    if (!(this.lastGroup_ != null)) {
      this.lastGroup_ = new WContainerWidget();
      this.impl_.addWidget(this.lastGroup_);
      this.scheduleRender();
    }
    return this.lastGroup_;
  }
}
