/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget that provides a drop-down combo-box control.
 * <p>
 * 
 * A combo box provides the user with a set of options, from which one option
 * may be selected.
 * <p>
 * WComboBox is an MVC view class, using a simple string list model by default.
 * The model may be populated using addItem(const {@link WString}&amp;) or
 * {@link WComboBox#insertItem(int index, CharSequence text) insertItem()} and
 * the contents can be cleared through {@link WComboBox#clear() clear()}. These
 * methods manipulate the underlying {@link WComboBox#getModel() getModel()}.
 * <p>
 * To use the combo box with a custom model instead of the default
 * {@link WStringListModel}, use
 * {@link WComboBox#setModel(WAbstractItemModel model) setModel()}.
 * <p>
 * To react to selection events, connect to the {@link WFormWidget#changed()
 * WFormWidget#changed()}, {@link WComboBox#activated() activated()} or
 * {@link WComboBox#sactivated() sactivated()} signals.
 * <p>
 * At all times, the current selection index is available through
 * {@link WComboBox#getCurrentIndex() getCurrentIndex()} and the current
 * selection text using {@link WComboBox#getCurrentText() getCurrentText()}.
 * <p>
 * {@link WComboBox} does not have support for auto-completion, this behaviour
 * can be found in the {@link WSuggestionPopup}.
 * <p>
 * WComboBox is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The widget corresponds to the HTML <code>&lt;select&gt;</code> tag and does
 * not provide styling. It can be styled using inline or external CSS as
 * appropriate.
 */
public class WComboBox extends WFormWidget {
	private static Logger logger = LoggerFactory.getLogger(WComboBox.class);

	/**
	 * Creates an empty combo-box with optional <i>parent</i>.
	 */
	public WComboBox(WContainerWidget parent) {
		super(parent);
		this.model_ = null;
		this.modelColumn_ = 0;
		this.currentIndex_ = -1;
		this.itemsChanged_ = false;
		this.selectionChanged_ = false;
		this.currentlyConnected_ = false;
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.activated_ = new Signal1<Integer>(this);
		this.sactivated_ = new Signal1<WString>(this);
		this.setInline(true);
		this.setFormObject(true);
		this.setModel(new WStringListModel(this));
	}

	/**
	 * Creates an empty combo-box with optional <i>parent</i>.
	 * <p>
	 * Calls {@link #WComboBox(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WComboBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Adds an option item.
	 * <p>
	 * Equivalent to {@link WComboBox#insertItem(int index, CharSequence text)
	 * insertItem} ({@link WComboBox#getCount() getCount()}, <code>text</code>).
	 */
	public void addItem(CharSequence text) {
		this.insertItem(this.getCount(), text);
	}

	/**
	 * Returns the number of items.
	 */
	public int getCount() {
		return this.model_.getRowCount();
	}

	/**
	 * Returns the currently selected item.
	 * <p>
	 * If no item is currently selected, the method returns -1.
	 * <p>
	 * The default value is 0, unless the combo box is empty.
	 */
	public int getCurrentIndex() {
		return this.currentIndex_;
	}

	/**
	 * Inserts an item at the specified position.
	 * <p>
	 * The item is inserted in the underlying model at position
	 * <code>index</code>. This requires that the {@link WComboBox#getModel()
	 * getModel()} is editable.
	 * <p>
	 * 
	 * @see WComboBox#addItem(CharSequence text)
	 * @see WComboBox#removeItem(int index)
	 */
	public void insertItem(int index, CharSequence text) {
		if (this.model_.insertRow(index)) {
			this.setItemText(index, text);
		}
	}

	/**
	 * Removes the item at the specified position.
	 * <p>
	 * The item is removed from the underlying model. This requires that the
	 * {@link WComboBox#getModel() getModel()} is editable.
	 * <p>
	 * 
	 * @see WComboBox#insertItem(int index, CharSequence text)
	 * @see WComboBox#clear()
	 */
	public void removeItem(int index) {
		this.model_.removeRow(index);
		this.setCurrentIndex(this.currentIndex_);
	}

	/**
	 * Changes the current selection.
	 * <p>
	 * Specify a value of -1 for <code>index</code> to clear the selection.
	 */
	public void setCurrentIndex(int index) {
		int newIndex = Math.min(index, this.getCount() - 1);
		if (this.currentIndex_ != newIndex) {
			this.currentIndex_ = newIndex;
			this.selectionChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
		}
	}

	/**
	 * Changes the text for a specified option.
	 * <p>
	 * The text for the item at position <code>index</code> is changed. This
	 * requires that the {@link WComboBox#getModel() getModel()} is editable.
	 */
	public void setItemText(int index, CharSequence text) {
		this.model_.setData(index, this.modelColumn_, text);
	}

	/**
	 * Returns the text of the currently selected item.
	 * <p>
	 * 
	 * @see WComboBox#getCurrentIndex()
	 * @see WComboBox#getItemText(int index)
	 */
	public WString getCurrentText() {
		if (this.currentIndex_ != -1) {
			return StringUtils.asString(this.model_.getData(this.currentIndex_,
					this.modelColumn_));
		} else {
			return new WString();
		}
	}

	/**
	 * Returns the text of a particular item.
	 * <p>
	 * 
	 * @see WComboBox#setItemText(int index, CharSequence text)
	 * @see WComboBox#getCurrentText()
	 */
	public WString getItemText(int index) {
		return StringUtils.asString(this.model_.getData(index,
				this.modelColumn_));
	}

	/**
	 * Sets the model to be used for the items.
	 * <p>
	 * The <code>model</code> may not be 0, and ownership of the model is not
	 * transferred.
	 * <p>
	 * The default value is a {@link WStringListModel} that is owned by the
	 * combo box.
	 * <p>
	 * 
	 * @see WComboBox#setModelColumn(int index)
	 */
	public void setModel(WAbstractItemModel model) {
		if (this.model_ != null) {
			for (int i = 0; i < this.modelConnections_.size(); ++i) {
				this.modelConnections_.get(i).disconnect();
			}
			this.modelConnections_.clear();
		}
		this.model_ = model;
		this.modelConnections_.add(this.model_.columnsInserted().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.columnsRemoved().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.rowsInserted().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.rowsRemoved().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.dataChanged().addListener(this,
				new Signal2.Listener<WModelIndex, WModelIndex>() {
					public void trigger(WModelIndex e1, WModelIndex e2) {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.modelReset().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WComboBox.this.itemsChanged();
					}
				}));
		this.modelConnections_.add(this.model_.layoutChanged().addListener(
				this, new Signal.Listener() {
					public void trigger() {
						WComboBox.this.itemsChanged();
					}
				}));
		this.refresh();
	}

	/**
	 * Sets the column in the model to be used for the items.
	 * <p>
	 * The column <code>index</code> in the model will be used to retrieve data.
	 * <p>
	 * The default value is 0.
	 * <p>
	 * 
	 * @see WComboBox#setModel(WAbstractItemModel model)
	 */
	public void setModelColumn(int index) {
		this.modelColumn_ = index;
	}

	/**
	 * Returns the data model.
	 * <p>
	 * 
	 * @see WComboBox#setModel(WAbstractItemModel model)
	 */
	public WAbstractItemModel getModel() {
		return this.model_;
	}

	/**
	 * Returns the index of the first item that matches a text.
	 */
	public int findText(CharSequence text, MatchOptions flags) {
		List<WModelIndex> list = this.model_.match(this.model_.getIndex(0,
				this.modelColumn_), ItemDataRole.DisplayRole, text, 1, flags);
		if (list.isEmpty()) {
			return -1;
		} else {
			return list.get(0).getRow();
		}
	}

	/**
	 * Returns the current value.
	 * <p>
	 * Returns {@link WComboBox#getCurrentText() getCurrentText()}.
	 */
	public String getValueText() {
		return this.getCurrentText().toString();
	}

	/**
	 * Sets the current value.
	 * <p>
	 * Sets the current index to the item corresponding to <code>value</code>.
	 */
	public void setValueText(String value) {
	}

	public void refresh() {
		this.itemsChanged();
		super.refresh();
	}

	/**
	 * Clears all items.
	 * <p>
	 * Removes all items from the underlying model. This requires that the
	 * {@link WComboBox#getModel() getModel()} is editable.
	 */
	public void clear() {
		this.model_.removeRows(0, this.getCount());
		this.setCurrentIndex(this.currentIndex_);
	}

	/**
	 * Signal emitted when the selection changed.
	 * <p>
	 * The newly selected item is passed as an argument.
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
	 * <p>
	 * The newly selected text is passed as an argument.
	 * <p>
	 * 
	 * @see WComboBox#activated()
	 * @see WComboBox#getCurrentText()
	 */
	public Signal1<WString> sactivated() {
		return this.sactivated_;
	}

	private WAbstractItemModel model_;
	private int modelColumn_;
	private int currentIndex_;
	private boolean itemsChanged_;
	boolean selectionChanged_;
	private boolean currentlyConnected_;
	private List<AbstractSignal.Connection> modelConnections_;
	private Signal1<Integer> activated_;
	private Signal1<WString> sactivated_;

	private void itemsChanged() {
		this.itemsChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	private void propagateChange() {
		int myCurrentIndex = this.currentIndex_;
		WString myCurrentValue = new WString();
		if (this.currentIndex_ != -1) {
			myCurrentValue = this.getCurrentText();
		}
		this.activated_.trigger(this.currentIndex_);
		if (myCurrentIndex != -1) {
			this.sactivated_.trigger(myCurrentValue);
		}
	}

	private boolean isSupportsNoSelection() {
		return false;
	}

	void updateDom(DomElement element, boolean all) {
		if (this.itemsChanged_ || all) {
			if (all && this.getCount() > 0 && this.currentIndex_ == -1
					&& !this.isSupportsNoSelection()) {
				this.currentIndex_ = 0;
			}
			if (!all) {
				element.removeAllChildren();
			}
			for (int i = 0; i < this.getCount(); ++i) {
				DomElement item = DomElement
						.createNew(DomElementType.DomElement_OPTION);
				item.setProperty(Property.PropertyValue, String.valueOf(i));
				item.setProperty(Property.PropertyInnerHTML, escapeText(
						StringUtils.asString(this.model_.getData(i,
								this.modelColumn_))).toString());
				if (this.isSelected(i)) {
					item.setProperty(Property.PropertySelected, "true");
				}
				WString sc = StringUtils.asString(this.model_.getData(i,
						this.modelColumn_, ItemDataRole.StyleClassRole));
				if (!(sc.length() == 0)) {
					item.setProperty(Property.PropertyClass, sc.toString());
				}
				element.addChild(item);
			}
			this.itemsChanged_ = false;
		}
		if (this.selectionChanged_) {
			element.setProperty(Property.PropertySelectedIndex, String
					.valueOf(this.currentIndex_));
			this.selectionChanged_ = false;
		}
		if (!this.currentlyConnected_
				&& (this.activated_.isConnected() || this.sactivated_
						.isConnected())) {
			this.currentlyConnected_ = true;
			this.changed().addListener(this, new Signal.Listener() {
				public void trigger() {
					WComboBox.this.propagateChange();
				}
			});
		}
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_SELECT;
	}

	void propagateRenderOk(boolean deep) {
		this.itemsChanged_ = false;
		this.selectionChanged_ = false;
		super.propagateRenderOk(deep);
	}

	void setFormData(WObject.FormData formData) {
		if (this.selectionChanged_ || this.isReadOnly()) {
			return;
		}
		if (!(formData.values.length == 0)) {
			String value = formData.values[0];
			if (value.length() != 0) {
				try {
					this.currentIndex_ = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					logger.error(new StringWriter().append(
							"received illegal form value: '").append(value)
							.append("'").toString());
				}
			} else {
				this.currentIndex_ = -1;
			}
		}
	}

	boolean isSelected(int index) {
		return index == this.currentIndex_;
	}

	void dummy() {
	}
}
