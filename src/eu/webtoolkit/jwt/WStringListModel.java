/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.utils.ReverseOrder;

/**
 * An model that manages a list of strings.
 */
public class WStringListModel extends WAbstractListModel {
	/**
	 * Create a new empty string list model.
	 */
	public WStringListModel(WObject parent) {
		super(parent);
		this.strings_ = new ArrayList<WString>();
	}

	/**
	 * Create a new empty string list model.
	 * <p>
	 * Calls {@link #WStringListModel(WObject parent) this((WObject)null)}
	 */
	public WStringListModel() {
		this((WObject) null);
	}

	/**
	 * Create a new string list model.
	 */
	public WStringListModel(List<WString> strings, WObject parent) {
		super(parent);
		this.strings_ = strings;
	}

	/**
	 * Create a new string list model.
	 * <p>
	 * Calls {@link #WStringListModel(List strings, WObject parent)
	 * this(strings, (WObject)null)}
	 */
	public WStringListModel(List<WString> strings) {
		this(strings, (WObject) null);
	}

	/**
	 * Set a new string list.
	 * <p>
	 * Replaces the current string list with a new list.
	 * <p>
	 * 
	 * @see WAbstractItemModel#dataChanged()
	 * @see WStringListModel#addString(CharSequence string)
	 */
	public void setStringList(List<WString> strings) {
		int currentSize = this.strings_.size();
		int newSize = strings.size();
		if (newSize > currentSize) {
			this.beginInsertRows(null, currentSize, newSize - 1);
		} else {
			if (newSize < currentSize) {
				this.beginRemoveRows(null, newSize, currentSize - 1);
			}
		}
		this.strings_ = strings;
		if (newSize > currentSize) {
			this.endInsertRows();
		} else {
			if (newSize < currentSize) {
				this.endRemoveRows();
			}
		}
		int numChanged = Math.min(currentSize, newSize);
		if (numChanged != 0) {
			this.dataChanged().trigger(this.getIndex(0, 0),
					this.getIndex(numChanged - 1, 0));
		}
	}

	/**
	 * Inserts a string.
	 * <p>
	 * 
	 * @see WStringListModel#setStringList(List strings)
	 */
	public void insertString(int row, CharSequence string) {
		this.insertRows(row, 1);
		this.setData(row, 0, string);
	}

	/**
	 * Adds a string.
	 * <p>
	 * 
	 * @see WStringListModel#setStringList(List strings)
	 */
	public void addString(CharSequence string) {
		this.insertString(this.getRowCount(), string);
	}

	/**
	 * Returns the string list.
	 * <p>
	 * 
	 * @see WStringListModel#setStringList(List strings)
	 */
	public List<WString> getStringList() {
		return this.strings_;
	}

	/**
	 * Returns the flags for an item.
	 * <p>
	 * This method is reimplemented to return {@link ItemFlag#ItemIsSelectable
	 * ItemIsSelectable} | {@link ItemFlag#ItemIsEditable ItemIsEditable}.
	 * <p>
	 * 
	 * @see ItemFlag
	 */
	public EnumSet<ItemFlag> getFlags(WModelIndex index) {
		return EnumSet.of(ItemFlag.ItemIsSelectable, ItemFlag.ItemIsEditable);
	}

	public boolean setData(WModelIndex index, Object value, int role) {
		if (role == ItemDataRole.EditRole) {
			role = ItemDataRole.DisplayRole;
		}
		if (role == ItemDataRole.DisplayRole) {
			this.strings_.set(index.getRow(), StringUtils.asString(value));
			this.dataChanged().trigger(index, index);
			return true;
		} else {
			return false;
		}
	}

	public Object getData(WModelIndex index, int role) {
		return role == ItemDataRole.DisplayRole ? this.strings_.get(index
				.getRow()) : null;
	}

	public int getRowCount(WModelIndex parent) {
		return (parent != null) ? 0 : this.strings_.size();
	}

	public boolean insertRows(int row, int count, WModelIndex parent) {
		if (!(parent != null)) {
			this.beginInsertRows(parent, row, row + count - 1);
			{
				int insertPos = 0 + row;
				for (int ii = 0; ii < count; ++ii)
					this.strings_.add(insertPos + ii, new WString());
			}
			;
			this.endInsertRows();
			return true;
		} else {
			return false;
		}
	}

	public boolean removeRows(int row, int count, WModelIndex parent) {
		if (!(parent != null)) {
			this.beginRemoveRows(parent, row, row + count - 1);
			for (int ii = 0; ii < (0 + row + count) - (0 + row); ++ii)
				this.strings_.remove(0 + row);
			;
			this.endRemoveRows();
			return true;
		} else {
			return false;
		}
	}

	public void sort(int column, SortOrder order) {
		this.layoutAboutToBeChanged().trigger();
		if (order == SortOrder.AscendingOrder) {
			Collections.sort(this.strings_);
		} else {
			Collections.sort(this.strings_, new ReverseOrder<WString>());
		}
		this.layoutChanged().trigger();
	}

	private List<WString> strings_;
}
