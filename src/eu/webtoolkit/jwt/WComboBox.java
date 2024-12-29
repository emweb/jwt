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
 * A widget that provides a drop-down combo-box control.
 *
 * <p>A combo box provides the user with a set of options, from which one option may be selected.
 *
 * <p>WComboBox is an MVC view class, using a simple string list model by default. The model may be
 * populated using {@link WComboBox#addItem(CharSequence text) addItem()} or {@link
 * WComboBox#insertItem(int index, CharSequence text) insertItem()} and the contents can be cleared
 * through {@link WComboBox#clear() clear()}. These methods manipulate the underlying {@link
 * WComboBox#getModel() getModel()}.
 *
 * <p>To use the combo box with a custom model instead of the default {@link WStringListModel}, use
 * {@link WComboBox#setModel(WAbstractItemModel model) setModel()}.
 *
 * <p>To react to selection events, connect to the {@link WFormWidget#changed()}, {@link
 * WComboBox#activated() activated()} or {@link WComboBox#sactivated() sactivated()} signals.
 *
 * <p>At all times, the current selection index is available through {@link
 * WComboBox#getCurrentIndex() getCurrentIndex()} and the current selection text using {@link
 * WComboBox#getCurrentText() getCurrentText()}.
 *
 * <p>{@link WComboBox} does not have support for auto-completion, this behaviour can be found in
 * the {@link WSuggestionPopup}.
 *
 * <p>WComboBox is an {@link WWidget#setInline(boolean inlined) inline } widget.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>The widget corresponds to the HTML <code>&lt;select&gt;</code> tag and does not provide
 * styling. It can be styled using inline or external CSS as appropriate.
 */
public class WComboBox extends WFormWidget {
  private static Logger logger = LoggerFactory.getLogger(WComboBox.class);

  /** Creates an empty combo-box.. */
  public WComboBox(WContainerWidget parentContainer) {
    super();
    this.model_ = null;
    this.modelColumn_ = 0;
    this.currentIndex_ = -1;
    this.currentIndexRaw_ = null;
    this.itemsChanged_ = false;
    this.selectionChanged_ = true;
    this.currentlyConnected_ = false;
    this.noSelectionEnabled_ = false;
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
    this.activated_ = new Signal1<Integer>();
    this.sactivated_ = new Signal1<WString>();
    this.setInline(true);
    this.setFormObject(true);
    this.setModel(new WStringListModel());
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an empty combo-box..
   *
   * <p>Calls {@link #WComboBox(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WComboBox() {
    this((WContainerWidget) null);
  }
  /**
   * Adds an option item.
   *
   * <p>This adds an item to the underlying model. This requires that the {@link
   * WComboBox#getModel() getModel()} is editable.
   *
   * <p>Equivalent to {@link WComboBox#insertItem(int index, CharSequence text) insertItem()}
   * ({@link WComboBox#getCount() getCount()}, <code>text</code>).
   */
  public void addItem(final CharSequence text) {
    this.insertItem(this.getCount(), text);
  }
  /** Returns the number of items. */
  public int getCount() {
    return this.model_.getRowCount();
  }
  /**
   * Returns the currently selected item.
   *
   * <p>If no item is currently selected, the method returns -1.
   *
   * <p>
   *
   * @see WComboBox#setNoSelectionEnabled(boolean enabled)
   */
  public int getCurrentIndex() {
    return this.currentIndex_;
  }
  /**
   * Inserts an item at the specified position.
   *
   * <p>The item is inserted in the underlying model at position <code>index</code>. This requires
   * that the {@link WComboBox#getModel() getModel()} is editable.
   *
   * <p>
   *
   * @see WComboBox#addItem(CharSequence text)
   * @see WComboBox#removeItem(int index)
   */
  public void insertItem(int index, final CharSequence text) {
    if (this.model_.insertRow(index)) {
      this.setItemText(index, text);
      this.makeCurrentIndexValid();
    }
  }
  /**
   * Removes the item at the specified position.
   *
   * <p>The item is removed from the underlying model. This requires that the {@link
   * WComboBox#getModel() getModel()} is editable.
   *
   * <p>
   *
   * @see WComboBox#insertItem(int index, CharSequence text)
   * @see WComboBox#clear()
   */
  public void removeItem(int index) {
    this.model_.removeRow(index);
    this.makeCurrentIndexValid();
  }
  /**
   * Changes the current selection.
   *
   * <p>Specify a value of -1 for <code>index</code> to clear the selection.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Setting a value of -1 works only if JavaScript is available. </i>
   */
  public void setCurrentIndex(int index) {
    int newIndex = Math.min(index, this.getCount() - 1);
    if (this.currentIndex_ != newIndex) {
      this.currentIndex_ = newIndex;
      this.makeCurrentIndexValid();
      this.validate();
      this.selectionChanged_ = true;
      this.repaint();
    }
  }
  /**
   * Changes the text for a specified option.
   *
   * <p>The text for the item at position <code>index</code> is changed. This requires that the
   * {@link WComboBox#getModel() getModel()} is editable.
   */
  public void setItemText(int index, final CharSequence text) {
    this.model_.setData(index, this.modelColumn_, text);
  }
  /**
   * Returns the text of the currently selected item.
   *
   * <p>
   *
   * @see WComboBox#getCurrentIndex()
   * @see WComboBox#getItemText(int index)
   */
  public WString getCurrentText() {
    if (this.currentIndex_ != -1) {
      return StringUtils.asString(this.model_.getData(this.currentIndex_, this.modelColumn_));
    } else {
      return new WString();
    }
  }
  /**
   * Returns the text of a particular item.
   *
   * <p>
   *
   * @see WComboBox#setItemText(int index, CharSequence text)
   * @see WComboBox#getCurrentText()
   */
  public WString getItemText(int index) {
    return StringUtils.asString(this.model_.getData(index, this.modelColumn_));
  }
  /**
   * Sets the model to be used for the items.
   *
   * <p>The default model is a {@link WStringListModel}.
   *
   * <p>Items in the model can be grouped by setting the {@link ItemDataRole#Level}. The contents is
   * interpreted by Wt::asString, and subsequent items of the same group are rendered as children of
   * a HTML <code> &lt;optgroup&gt; </code>element.
   *
   * <p>
   *
   * @see WComboBox#setModelColumn(int index)
   */
  public void setModel(final WAbstractItemModel model) {
    if (this.model_ != null) {
      for (int i = 0; i < this.modelConnections_.size(); ++i) {
        this.modelConnections_.get(i).disconnect();
      }
      this.modelConnections_.clear();
    }
    this.model_ = model;
    this.modelConnections_.add(
        this.model_
            .columnsInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WComboBox.this.itemsChanged();
                }));
    this.modelConnections_.add(
        this.model_
            .columnsRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WComboBox.this.itemsChanged();
                }));
    this.modelConnections_.add(
        this.model_
            .rowsInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WComboBox.this.rowsInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.model_
            .rowsRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WComboBox.this.rowsRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.model_
            .dataChanged()
            .addListener(
                this,
                (WModelIndex e1, WModelIndex e2) -> {
                  WComboBox.this.itemsChanged();
                }));
    this.modelConnections_.add(
        this.model_
            .modelReset()
            .addListener(
                this,
                () -> {
                  WComboBox.this.itemsChanged();
                }));
    this.modelConnections_.add(
        this.model_
            .layoutAboutToBeChanged()
            .addListener(
                this,
                () -> {
                  WComboBox.this.saveSelection();
                }));
    this.modelConnections_.add(
        this.model_
            .layoutChanged()
            .addListener(
                this,
                () -> {
                  WComboBox.this.layoutChanged();
                }));
    this.refresh();
  }
  /**
   * Sets the column in the model to be used for the items.
   *
   * <p>The column <code>index</code> in the model will be used to retrieve data.
   *
   * <p>The default value is 0.
   *
   * <p>
   *
   * @see WComboBox#setModel(WAbstractItemModel model)
   */
  public void setModelColumn(int index) {
    this.modelColumn_ = index;
  }
  /**
   * Returns the data model.
   *
   * <p>
   *
   * @see WComboBox#setModel(WAbstractItemModel model)
   */
  public WAbstractItemModel getModel() {
    return this.model_;
  }
  /** Returns the index of the first item that matches a text. */
  public int findText(final CharSequence text, MatchOptions flags) {
    List<WModelIndex> list =
        this.model_.match(
            this.model_.getIndex(0, this.modelColumn_), ItemDataRole.Display, text, 1, flags);
    if (list.isEmpty()) {
      return -1;
    } else {
      return list.get(0).getRow();
    }
  }
  /**
   * Returns the selection mode.
   *
   * <p>Always returns {@link SelectionMode#Single} for a combo box, but may return {@link
   * SelectionMode#Extended} for a selection box
   *
   * <p>
   *
   * @see WSelectionBox#setSelectionMode(SelectionMode mode)
   */
  public SelectionMode getSelectionMode() {
    return SelectionMode.Single;
  }
  /**
   * Returns the current value.
   *
   * <p>Returns {@link WComboBox#getCurrentText() getCurrentText()} as a String.
   */
  public String getValueText() {
    return this.getCurrentText().toString();
  }
  /**
   * Sets the current value.
   *
   * <p>Sets the current index to the item corresponding to <code>value</code>.
   */
  public void setValueText(final String value) {
    int size = this.getCount();
    for (int i = 0; i < size; ++i) {
      if ((StringUtils.asString(
              this.model_.getIndex(i, this.modelColumn_).getData(ItemDataRole.Display))
          .toString()
          .equals(value.toString()))) {
        this.setCurrentIndex(i);
        return;
      }
    }
    this.setCurrentIndex(-1);
  }

  public void refresh() {
    this.itemsChanged();
    super.refresh();
  }
  /**
   * Clears all items.
   *
   * <p>Removes all items from the underlying model. This requires that the {@link
   * WComboBox#getModel() getModel()} is editable.
   */
  public void clear() {
    this.model_.removeRows(0, this.getCount());
    this.makeCurrentIndexValid();
  }
  /**
   * Signal emitted when the selection changed.
   *
   * <p>The newly selected item is passed as an argument.
   *
   * <p>
   *
   * @see WComboBox#sactivated()
   * @see WComboBox#getCurrentIndex()
   */
  public Signal1<Integer> activated() {
    return this.activated_;
  }
  /**
   * Signal emitted when the selection changed.
   *
   * <p>The newly selected text is passed as an argument.
   *
   * <p>
   *
   * @see WComboBox#activated()
   * @see WComboBox#getCurrentText()
   */
  public Signal1<WString> sactivated() {
    return this.sactivated_;
  }
  /**
   * Enables the ability to have &apos;no currently selected&apos; item.
   *
   * <p>The setting may only be changed for a combo box (and not for a selection box). When enabled,
   * the {@link WComboBox#getCurrentIndex() getCurrentIndex()} may be &apos;-1&apos; also when the
   * combo box contains values. The user can however not select this option, it is thus only useful
   * as a default value.
   *
   * <p>By default, no selection is <code>false</code> for a combo-box and <code>true</code> for a
   * selection box.
   */
  public void setNoSelectionEnabled(boolean enabled) {
    if (this.noSelectionEnabled_ != enabled) {
      this.noSelectionEnabled_ = enabled;
      this.makeCurrentIndexValid();
    }
  }
  /**
   * Returns whether &apos;no selection&apos; is a valid state.
   *
   * <p>
   *
   * @see WComboBox#setNoSelectionEnabled(boolean enabled)
   */
  public boolean isNoSelectionEnabled() {
    return this.noSelectionEnabled_;
  }

  private WAbstractItemModel model_;
  private int modelColumn_;
  private int currentIndex_;
  private Object currentIndexRaw_;
  private boolean itemsChanged_;
  boolean selectionChanged_;
  private boolean currentlyConnected_;
  boolean noSelectionEnabled_;
  private List<AbstractSignal.Connection> modelConnections_;
  private Signal1<Integer> activated_;
  private Signal1<WString> sactivated_;

  private void layoutChanged() {
    this.itemsChanged_ = true;
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    this.restoreSelection();
  }

  private void itemsChanged() {
    this.itemsChanged_ = true;
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    this.makeCurrentIndexValid();
  }

  private void propagateChange() {
    int myCurrentIndex = this.currentIndex_;
    WString myCurrentValue = new WString();
    if (this.currentIndex_ != -1) {
      myCurrentValue = this.getCurrentText();
    }
    WComboBox guard = this;
    this.activated_.trigger(this.currentIndex_);
    if (guard != null) {
      if (myCurrentIndex != -1) {
        this.sactivated_.trigger(myCurrentValue);
      }
    }
  }

  private void rowsInserted(final WModelIndex anon1, int from, int to) {
    this.itemsChanged_ = true;
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    int count = to - from + 1;
    if (this.currentIndex_ == -1) {
      this.makeCurrentIndexValid();
    } else {
      if (this.currentIndex_ >= from) {
        this.currentIndex_ += count;
      }
    }
  }

  private void rowsRemoved(final WModelIndex anon1, int from, int to) {
    this.itemsChanged_ = true;
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    if (this.currentIndex_ < from) {
      return;
    }
    int count = to - from + 1;
    if (this.currentIndex_ > to) {
      this.currentIndex_ -= count;
    } else {
      if (this.currentIndex_ >= from) {
        this.currentIndex_ = -1;
        this.makeCurrentIndexValid();
      }
    }
  }

  private void saveSelection() {
    if (this.currentIndex_ >= 0) {
      this.currentIndexRaw_ =
          this.model_.toRawIndex(this.model_.getIndex(this.currentIndex_, this.modelColumn_));
    } else {
      this.currentIndexRaw_ = null;
    }
  }

  private void restoreSelection() {
    if (this.currentIndexRaw_ != null) {
      WModelIndex m = this.model_.fromRawIndex(this.currentIndexRaw_);
      if ((m != null)) {
        this.currentIndex_ = m.getRow();
      } else {
        this.currentIndex_ = -1;
      }
    } else {
      this.currentIndex_ = -1;
    }
    this.makeCurrentIndexValid();
    this.currentIndexRaw_ = null;
  }

  private boolean isSupportsNoSelection() {
    return this.noSelectionEnabled_;
  }

  void updateDom(final DomElement element, boolean all) {
    if (this.itemsChanged_ || all) {
      if (!all) {
        element.removeAllChildren();
        if (this.currentIndex_ == -1) {
          this.selectionChanged_ = true;
        }
      }
      DomElement currentGroup = null;
      boolean groupDisabled = true;
      int size = this.getCount();
      for (int i = 0; i < size; ++i) {
        DomElement item = DomElement.createNew(DomElementType.OPTION);
        item.setProperty(Property.Value, String.valueOf(i));
        item.setProperty(
            Property.InnerHTML,
            escapeText(StringUtils.asString(this.model_.getData(i, this.modelColumn_))).toString());
        if (!!EnumUtils.mask(
                this.model_.getFlags(this.model_.getIndex(i, this.modelColumn_)),
                ItemFlag.Selectable)
            .isEmpty()) {
          item.setProperty(Property.Disabled, "true");
        }
        if (this.isSelected(i)) {
          item.setProperty(Property.Selected, "true");
        }
        WString sc =
            StringUtils.asString(
                this.model_.getData(i, this.modelColumn_, ItemDataRole.StyleClass));
        if (!(sc.length() == 0)) {
          item.setProperty(Property.Class, sc.toString());
        }
        WString groupname =
            StringUtils.asString(this.model_.getData(i, this.modelColumn_, ItemDataRole.Level));
        boolean isSoloItem = false;
        if ((groupname.length() == 0)) {
          isSoloItem = true;
          if (currentGroup != null) {
            if (groupDisabled) {
              currentGroup.setProperty(Property.Disabled, "true");
            }
            element.addChild(currentGroup);
            currentGroup = null;
          }
        } else {
          isSoloItem = false;
          if (!(currentGroup != null)
              || !currentGroup.getProperty(Property.Label).equals(groupname.toString())) {
            if (currentGroup != null) {
              if (groupDisabled) {
                currentGroup.setProperty(Property.Disabled, "true");
              }
              element.addChild(currentGroup);
              currentGroup = null;
            }
            currentGroup = DomElement.createNew(DomElementType.OPTGROUP);
            currentGroup.setProperty(Property.Label, groupname.toString());
            groupDisabled =
                !!EnumUtils.mask(
                        this.model_.getFlags(this.model_.getIndex(i, this.modelColumn_)),
                        ItemFlag.Selectable)
                    .isEmpty();
          } else {
            if (this.model_
                .getFlags(this.model_.getIndex(i, this.modelColumn_))
                .contains(ItemFlag.Selectable)) {
              groupDisabled = false;
            }
          }
        }
        if (isSoloItem) {
          element.addChild(item);
        } else {
          currentGroup.addChild(item);
        }
        if (i == size - 1 && currentGroup != null) {
          if (groupDisabled) {
            currentGroup.setProperty(Property.Disabled, "true");
          }
          element.addChild(currentGroup);
          currentGroup = null;
        }
      }
      this.itemsChanged_ = false;
    }
    if (this.selectionChanged_ || all && this.getSelectionMode() == SelectionMode.Single) {
      element.setProperty(Property.SelectedIndex, String.valueOf(this.currentIndex_));
      this.selectionChanged_ = false;
    }
    if (!this.currentlyConnected_
        && (this.activated_.isConnected() || this.sactivated_.isConnected())) {
      this.currentlyConnected_ = true;
      this.changed()
          .addListener(
              this,
              () -> {
                WComboBox.this.propagateChange();
              });
    }
    super.updateDom(element, all);
  }

  DomElementType getDomElementType() {
    return DomElementType.SELECT;
  }

  void propagateRenderOk(boolean deep) {
    this.itemsChanged_ = false;
    this.selectionChanged_ = false;
    super.propagateRenderOk(deep);
  }

  protected void setFormData(final WObject.FormData formData) {
    if (this.selectionChanged_ || this.isReadOnly()) {
      return;
    }
    if (!(formData.values.length == 0)) {
      final String value = formData.values[0];
      if (value.length() != 0) {
        try {
          this.currentIndex_ = Integer.parseInt(value);
        } catch (final RuntimeException e) {
          logger.error(
              new StringWriter()
                  .append("received illegal form value: '")
                  .append(value)
                  .append("'")
                  .toString());
        }
      } else {
        this.currentIndex_ = -1;
      }
      this.makeCurrentIndexValid();
    }
  }

  boolean isSelected(int index) {
    return index == this.currentIndex_;
  }

  private void makeCurrentIndexValid() {
    int c = this.getCount();
    if (this.currentIndex_ > c - 1) {
      this.setCurrentIndex(c - 1);
    } else {
      if (c > 0 && this.currentIndex_ == -1 && !this.isSupportsNoSelection()) {
        this.setCurrentIndex(0);
      }
    }
  }
}
