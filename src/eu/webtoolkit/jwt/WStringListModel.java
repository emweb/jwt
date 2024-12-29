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
 * An model that manages a list of strings.
 *
 * <p>This model only manages a unidimensional list of items and is optimized for usage by view
 * widgets such as combo-boxes.
 *
 * <p>It supports all features of a typical item model, including data for multiple roles, editing
 * and addition and removal of data rows.
 *
 * <p>You can populate the model by passing a list of strings to its consructor, or by using the
 * {@link WStringListModel#setStringList(List strings) setStringList()} method. You can set or
 * retrieve data using the {@link WStringListModel#setData(WModelIndex index, Object value,
 * ItemDataRole role) setData()} and {@link WStringListModel#getData(WModelIndex index, ItemDataRole
 * role) getData()} methods, and add or remove data using the {@link WStringListModel#insertRows(int
 * row, int count, WModelIndex parent) insertRows()} and {@link WStringListModel#removeRows(int row,
 * int count, WModelIndex parent) removeRows()} methods.
 *
 * <p>
 *
 * @see WComboBox
 * @see WSelectionBox
 */
public class WStringListModel extends WAbstractListModel {
  private static Logger logger = LoggerFactory.getLogger(WStringListModel.class);

  /** Creates a new empty string list model. */
  public WStringListModel() {
    super();
    this.displayData_ = new ArrayList<WString>();
    this.otherData_ = null;
    this.flags_ = new ArrayList<EnumSet<ItemFlag>>();
  }
  /** Creates a new string list model. */
  public WStringListModel(final List<WString> strings) {
    super();
    this.displayData_ = strings;
    this.otherData_ = null;
    this.flags_ = new ArrayList<EnumSet<ItemFlag>>();
  }
  /**
   * Sets a new string list.
   *
   * <p>Replaces the current string list with a new list.
   *
   * <p>
   *
   * @see WAbstractItemModel#dataChanged()
   * @see WStringListModel#addString(CharSequence string)
   */
  public void setStringList(final List<WString> strings) {
    int currentSize = this.displayData_.size();
    int newSize = strings.size();
    if (newSize > currentSize) {
      this.beginInsertRows(null, currentSize, newSize - 1);
    } else {
      if (newSize < currentSize) {
        this.beginRemoveRows(null, newSize, currentSize - 1);
      }
    }
    Utils.copyList(strings, this.displayData_);
    this.flags_.clear();

    this.otherData_ = null;
    if (newSize > currentSize) {
      this.endInsertRows();
    } else {
      if (newSize < currentSize) {
        this.endRemoveRows();
      }
    }
    int numChanged = Math.min(currentSize, newSize);
    if (numChanged != 0) {
      this.dataChanged().trigger(this.getIndex(0, 0), this.getIndex(numChanged - 1, 0));
    }
  }
  /**
   * Inserts a string.
   *
   * <p>
   *
   * @see WStringListModel#setStringList(List strings)
   */
  public void insertString(int row, final CharSequence string) {
    this.insertRows(row, 1);
    this.setData(row, 0, string);
  }
  /**
   * Adds a string.
   *
   * <p>
   *
   * @see WStringListModel#setStringList(List strings)
   */
  public void addString(final CharSequence string) {
    this.insertString(this.getRowCount(), string);
  }
  /**
   * Returns the string list.
   *
   * <p>
   *
   * @see WStringListModel#setStringList(List strings)
   */
  public List<WString> getStringList() {
    return this.displayData_;
  }
  /**
   * Sets model flags for an item.
   *
   * <p>The default item flags are {@link ItemFlag#Selectable} | {@link ItemFlag#Editable}.
   */
  public void setFlags(int row, EnumSet<ItemFlag> flags) {
    if (this.flags_.isEmpty()) {
      {
        int insertPos = 0;
        for (int ii = 0; ii < (this.getRowCount()); ++ii)
          this.flags_.add(insertPos + ii, EnumSet.of(ItemFlag.Selectable, ItemFlag.Editable));
      }
      ;
    }
    this.flags_.set(row, flags);
    this.dataChanged().trigger(this.getIndex(row, 0), this.getIndex(row, 0));
  }
  /**
   * Sets model flags for an item.
   *
   * <p>Calls {@link #setFlags(int row, EnumSet flags) setFlags(row, EnumSet.of(flag, flags))}
   */
  public final void setFlags(int row, ItemFlag flag, ItemFlag... flags) {
    setFlags(row, EnumSet.of(flag, flags));
  }
  /**
   * Returns the flags for an item.
   *
   * <p>This method is reimplemented to return flags set in {@link WStringListModel#setFlags(int
   * row, EnumSet flags) setFlags()}.
   *
   * <p>
   *
   * @see WStringListModel#setFlags(int row, EnumSet flags)
   */
  public EnumSet<ItemFlag> getFlags(final WModelIndex index) {
    if (this.flags_.isEmpty()) {
      return EnumSet.of(ItemFlag.Selectable, ItemFlag.Editable);
    } else {
      return this.flags_.get(index.getRow());
    }
  }

  public boolean setData(final WModelIndex index, final Object value, ItemDataRole role) {
    if (role.equals(ItemDataRole.Edit)) {
      role = ItemDataRole.Display;
    }
    if (role.equals(ItemDataRole.Display)) {
      this.displayData_.set(index.getRow(), StringUtils.asString(value));
      if (!(this.otherData_ != null)) {
        this.createOtherDataMap();
      }
      this.otherData_.get(index.getRow()).put(ItemDataRole.Edit, value);
    } else {
      if (!(this.otherData_ != null)) {
        this.createOtherDataMap();
      }
      this.otherData_.get(index.getRow()).put(role, value);
    }
    this.dataChanged().trigger(index, index);
    return true;
  }

  public Object getData(final WModelIndex index, ItemDataRole role) {
    if (role.equals(ItemDataRole.Display)) {
      return this.displayData_.get(index.getRow());
    } else {
      if (this.otherData_ != null) {
        return this.otherData_.get(index.getRow()).get(role);
      } else {
        return null;
      }
    }
  }

  public int getRowCount(final WModelIndex parent) {
    return (parent != null) ? 0 : this.displayData_.size();
  }

  public boolean insertRows(int row, int count, final WModelIndex parent) {
    if (!(parent != null)) {
      this.beginInsertRows(parent, row, row + count - 1);
      {
        int insertPos = 0 + row;
        for (int ii = 0; ii < (count); ++ii) this.displayData_.add(insertPos + ii, new WString());
      }
      ;
      if (!this.flags_.isEmpty()) {
        {
          int insertPos = 0 + row;
          for (int ii = 0; ii < (count); ++ii)
            this.flags_.add(insertPos + ii, EnumSet.of(ItemFlag.Selectable, ItemFlag.Editable));
        }
        ;
      }
      if (this.otherData_ != null) {
        {
          int insertPos = 0 + row;
          for (int ii = 0; ii < (count); ++ii)
            this.otherData_.add(insertPos + ii, new TreeMap<ItemDataRole, Object>());
        }
        ;
      }
      this.endInsertRows();
      return true;
    } else {
      return false;
    }
  }

  public boolean removeRows(int row, int count, final WModelIndex parent) {
    if (!(parent != null)) {
      this.beginRemoveRows(parent, row, row + count - 1);
      for (int ii = 0; ii < (0 + row + count) - (0 + row); ++ii) this.displayData_.remove(0 + row);
      ;
      if (!this.flags_.isEmpty()) {
        for (int ii = 0; ii < (0 + row + count) - (0 + row); ++ii) this.flags_.remove(0 + row);
        ;
      }
      if (this.otherData_ != null) {
        for (int ii = 0; ii < (0 + row + count) - (0 + row); ++ii) this.otherData_.remove(0 + row);
        ;
      }
      this.endRemoveRows();
      return true;
    } else {
      return false;
    }
  }

  public void sort(int column, SortOrder order) {
    this.layoutAboutToBeChanged().trigger();
    if (!(this.otherData_ != null) && this.flags_.isEmpty()) {
      if (order == SortOrder.Ascending) {
        Collections.sort(this.displayData_);
      } else {
        Collections.sort(this.displayData_, new ReverseOrder<WString>());
      }
    } else {
      List<Integer> permutation = new ArrayList<Integer>();
      for (int i = 0; i < this.getRowCount(); ++i) {
        permutation.add(i);
      }
      Collections.sort(permutation, new StringListModelCompare(this, order));
      List<WString> displayData = new ArrayList<WString>();
      CollectionUtils.resize(displayData, this.getRowCount());
      List<EnumSet<ItemFlag>> flags = new ArrayList<EnumSet<ItemFlag>>();
      if (!this.flags_.isEmpty()) {
        CollectionUtils.resize(flags, this.getRowCount());
      }
      List<SortedMap<ItemDataRole, Object>> otherData = null;
      if (this.otherData_ != null) {
        otherData = new ArrayList<SortedMap<ItemDataRole, Object>>();
        CollectionUtils.resize(otherData, this.getRowCount());
      }
      for (int i = 0; i < permutation.size(); ++i) {
        displayData.set(i, this.displayData_.get(permutation.get(i)));
        if (otherData != null) {
          otherData.set(i, this.otherData_.get(permutation.get(i)));
        }
        if (!flags.isEmpty()) {
          flags.set(i, this.flags_.get(permutation.get(i)));
        }
      }
      Utils.copyList(displayData, this.displayData_);

      this.otherData_ = otherData;
      Utils.copyList(flags, this.flags_);
    }
    this.layoutChanged().trigger();
  }

  private List<WString> displayData_;
  private List<SortedMap<ItemDataRole, Object>> otherData_;
  private List<EnumSet<ItemFlag>> flags_;

  private void createOtherDataMap() {
    this.otherData_ = new ArrayList<SortedMap<ItemDataRole, Object>>();
    for (int i = 0; i < this.displayData_.size(); ++i) {
      this.otherData_.add(new TreeMap<ItemDataRole, Object>());
    }
  }
}
