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
 * A selection box allows selection from a list of options.
 *
 * <p>By default, a selection box may be used to let the user select one item from a list. This may
 * be changed to multiple selection mode using {@link WSelectionBox#setSelectionMode(SelectionMode
 * mode) setSelectionMode()}.
 *
 * <p>The current selection may be set and read using {@link WComboBox#setCurrentIndex(int index)
 * WComboBox#setCurrentIndex()} and {@link WComboBox#getCurrentIndex()}, for {@link
 * SelectionMode#Single} mode, or {@link WSelectionBox#setSelectedIndexes(Set selection)
 * setSelectedIndexes()} and {@link WSelectionBox#getSelectedIndexes() getSelectedIndexes()} for
 * {@link SelectionMode#Extended} mode. The {@link WComboBox#activated()} and {@link
 * WComboBox#sactivated()} signals are not emited in the {@link SelectionMode#Extended} mode, use
 * the {@link WFormWidget#changed()} signal.
 *
 * <p>WSelectionBox is an MVC view class, using a simple string list model by default. The model may
 * be populated using {@link WComboBox#addItem(CharSequence text) WComboBox#addItem()} or {@link
 * WComboBox#insertItem(int index, CharSequence text) WComboBox#insertItem()} and the contents can
 * be cleared through {@link WComboBox#clear()}. These methods manipulate the underlying {@link
 * WComboBox#getModel()}.
 *
 * <p>To use the selectionbox with a custom model instead of the default {@link WStringListModel},
 * use {@link WComboBox#setModel(WAbstractItemModel model) WComboBox#setModel()}.
 *
 * <p>WSelectionBox is an {@link WWidget#setInline(boolean inlined) inline } widget.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>The widget corresponds to the HTML <code>&lt;select&gt;</code> tag and does not provide
 * styling. It can be styled using inline or external CSS as appropriate.
 */
public class WSelectionBox extends WComboBox {
  private static Logger logger = LoggerFactory.getLogger(WSelectionBox.class);

  /** Constructor. */
  public WSelectionBox(WContainerWidget parentContainer) {
    super();
    this.verticalSize_ = 5;
    this.selectionMode_ = SelectionMode.Single;
    this.selection_ = new HashSet<Integer>();
    this.configChanged_ = false;
    this.noSelectionEnabled_ = true;
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #WSelectionBox(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WSelectionBox() {
    this((WContainerWidget) null);
  }
  /** Returns the number of items that are visible. */
  public int getVerticalSize() {
    return this.verticalSize_;
  }
  /**
   * Sets the number of items that are visible.
   *
   * <p>If more items are available, a scroll-bar is provided.
   */
  public void setVerticalSize(int items) {
    this.verticalSize_ = items;
    this.configChanged_ = true;
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
  }
  /**
   * Sets the selection mode.
   *
   * <p>The default selection mode is {@link SelectionMode#Single}. You can change to {@link
   * SelectionMode#Extended} to allow selection of multiple items.
   */
  public void setSelectionMode(SelectionMode mode) {
    if (mode != this.selectionMode_) {
      this.selectionMode_ = mode;
      this.configChanged_ = true;
      this.repaint();
      if (mode == SelectionMode.Extended) {
        this.selection_.clear();
        if (this.getCurrentIndex() != -1) {
          this.selection_.add(this.getCurrentIndex());
        }
      } else {
        if (this.selection_.size() == 1) {
          this.setCurrentIndex(this.selection_.iterator().next());
        } else {
          this.setCurrentIndex(-1);
        }
        this.selection_.clear();
      }
    }
  }
  /**
   * Returns the selection mode.
   *
   * <p>
   *
   * @see WSelectionBox#setSelectionMode(SelectionMode mode)
   */
  public SelectionMode getSelectionMode() {
    return this.selectionMode_;
  }
  /**
   * Returns the current selection (in {@link SelectionMode#Extended} mode).
   *
   * <p>Get the list of currently selected items. This method is only defined when {@link
   * WSelectionBox#getSelectionMode() getSelectionMode()} is {@link SelectionMode#Extended}.
   * Otherwise, you should use {@link WComboBox#getCurrentIndex()} to get item currently selected.
   *
   * <p>
   *
   * @see WComboBox#getCurrentIndex()
   */
  public Set<Integer> getSelectedIndexes() {
    if (this.selectionMode_ != SelectionMode.Extended) {
      throw new WException(
          "WSelectionBox::setSelectedIndexes() can only be used for an SelectionMode::Extended mode");
    }
    return this.selection_;
  }
  /**
   * Sets the selection (in {@link SelectionMode#Extended} mode).
   *
   * <p>For an {@link SelectionMode#Extended} mode, set the list of currently selected items.
   *
   * <p>
   *
   * @see WSelectionBox#getSelectedIndexes()
   */
  public void setSelectedIndexes(final Set<Integer> selection) {
    if (this.selectionMode_ != SelectionMode.Extended) {
      throw new WException(
          "WSelectionBox::setSelectedIndexes() can only be used for an SelectionMode::Extended mode");
    }
    this.selection_ = selection;
    this.selectionChanged_ = true;
    this.repaint();
  }
  /**
   * Clears the current selection.
   *
   * <p>Clears the current selection.
   *
   * <p>
   *
   * @see WComboBox#setCurrentIndex(int index)
   * @see WSelectionBox#setSelectedIndexes(Set selection)
   */
  public void clearSelection() {
    if (this.selectionMode_ == SelectionMode.Extended) {
      this.setSelectedIndexes(new HashSet<Integer>());
    } else {
      this.setCurrentIndex(-1);
    }
  }

  private int verticalSize_;
  private SelectionMode selectionMode_;
  private Set<Integer> selection_;
  private boolean configChanged_;

  private boolean isSupportsNoSelection() {
    return true;
  }

  void updateDom(final DomElement element, boolean all) {
    if (this.configChanged_ || all) {
      element.setAttribute("size", String.valueOf(this.verticalSize_));
      if (!all || this.selectionMode_ == SelectionMode.Extended) {
        element.setProperty(
            Property.Multiple, this.selectionMode_ == SelectionMode.Extended ? "true" : "false");
        if (!all) {
          this.selectionChanged_ = true;
        }
      }
      this.configChanged_ = false;
    }
    if (this.selectionMode_ == SelectionMode.Extended) {
      if (this.selectionChanged_ && !all) {
        for (int i = 0; i < this.getCount(); ++i) {
          element.callMethod(
              "options["
                  + String.valueOf(i)
                  + "].selected="
                  + (this.isSelected(i) ? "true" : "false"));
        }
      }
      this.selectionChanged_ = false;
    }
    super.updateDom(element, all);
  }

  protected void setFormData(final WObject.FormData formData) {
    if (this.selectionChanged_) {
      return;
    }
    if (this.selectionMode_ == SelectionMode.Single) {
      super.setFormData(formData);
    } else {
      this.selection_.clear();
      for (int j = 0; j < formData.values.length; ++j) {
        final String v = formData.values[j];
        if (v.length() != 0) {
          try {
            int i = Integer.parseInt(v);
            this.selection_.add(i);
          } catch (final RuntimeException e) {
            logger.error(
                new StringWriter()
                    .append("received illegal form value: '")
                    .append(v)
                    .append("'")
                    .toString());
          }
        }
      }
    }
  }

  void propagateRenderOk(boolean deep) {
    this.configChanged_ = false;
    this.selectionChanged_ = false;
    super.propagateRenderOk(deep);
  }

  boolean isSelected(int index) {
    if (this.selectionMode_ == SelectionMode.Extended) {
      boolean i = this.selection_.contains(index);
      return i != false;
    } else {
      return super.isSelected(index);
    }
  }
}
