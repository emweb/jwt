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
import java.util.SortedMap;
import java.util.TreeMap;
import eu.webtoolkit.jwt.utils.CollectionUtils;
import eu.webtoolkit.jwt.utils.EnumUtils;
import eu.webtoolkit.jwt.utils.ObjectUtils;

/**
 * An item in a {@link WStandardItemModel}.
 * <p>
 * 
 * The item provides access to various data properties:
 * {@link WStandardItem#setText(CharSequence text) text},
 * {@link WStandardItem#setIcon(String uri) icon},
 * {@link WStandardItem#setStyleClass(CharSequence styleClass) CSS style class},
 * {@link WStandardItem#setToolTip(CharSequence toolTip) tool tip}, and
 * {@link WStandardItem#setChecked(boolean checked) check state}, and data flags
 * ({@link WStandardItem#setFlags(EnumSet flags) setFlags()} and
 * {@link WStandardItem#setCheckable(boolean checkable) setCheckable()}).
 * <p>
 * An item may contain a table of children items: the initial geometry may be
 * specified in the constructor, or using the methods
 * {@link WStandardItem#setRowCount(int rows) setRowCount()} and
 * setModelCount(). Unspecified items are 0. You can set or inspect children
 * items using the
 * {@link WStandardItem#setChild(int row, int column, WStandardItem item)
 * setChild()} and {@link WStandardItem#getChild(int row, int column)
 * getChild()} methods.
 * <p>
 * It is possible to reimplement this class and specialize the methods for data
 * acess ({@link WStandardItem#setData(Object d, int role) setData()} and
 * {@link WStandardItem#getData(int role) getData()}), or provide custom sorting
 * functionality by reimplementing
 * <p>
 * {@link WStandardItem#compare(WStandardItem other) compare()}.
 */
public class WStandardItem {
	/**
	 * Creates an empty standard item.
	 */
	public WStandardItem() {
		this.model_ = null;
		this.parent_ = null;
		this.row_ = -1;
		this.column_ = -1;
		this.data_ = new TreeMap<Integer, Object>();
		this.flags_ = EnumSet.of(ItemFlag.ItemIsSelectable);
		this.columns_ = null;
	}

	/**
	 * Creates an item with a text.
	 * <p>
	 * 
	 * @see WStandardItem#setText(CharSequence text)
	 */
	public WStandardItem(CharSequence text) {
		this.model_ = null;
		this.parent_ = null;
		this.row_ = -1;
		this.column_ = -1;
		this.data_ = new TreeMap<Integer, Object>();
		this.flags_ = EnumSet.of(ItemFlag.ItemIsSelectable);
		this.columns_ = null;
		this.setText(text);
	}

	/**
	 * Creates an item with an icon and a text.
	 * <p>
	 * 
	 * @see WStandardItem#setText(CharSequence text)
	 * @see WStandardItem#setIcon(String uri)
	 */
	public WStandardItem(String iconUri, CharSequence text) {
		this.model_ = null;
		this.parent_ = null;
		this.row_ = -1;
		this.column_ = -1;
		this.data_ = new TreeMap<Integer, Object>();
		this.flags_ = EnumSet.of(ItemFlag.ItemIsSelectable);
		this.columns_ = null;
		this.setText(text);
		this.setIcon(iconUri);
	}

	/**
	 * Creates an item with an initial geometry.
	 * <p>
	 * 
	 * @see WStandardItem#setRowCount(int rows)
	 * @see WStandardItem#setColumnCount(int columns)
	 */
	public WStandardItem(int rows, int columns) {
		this.model_ = null;
		this.parent_ = null;
		this.row_ = -1;
		this.column_ = -1;
		this.data_ = new TreeMap<Integer, Object>();
		this.flags_ = EnumSet.of(ItemFlag.ItemIsSelectable);
		this.columns_ = null;
		if (rows > 0) {
			columns = Math.max(columns, 1);
		}
		if (columns > 0) {
			this.columns_ = new ArrayList<List<WStandardItem>>();
			for (int i = 0; i < columns; ++i) {
				List<WStandardItem> c = new ArrayList<WStandardItem>();
				{
					int insertPos = c.size();
					for (int ii = 0; ii < rows; ++ii)
						c.add(insertPos + ii, (WStandardItem) null);
				}
				;
				this.columns_.add(c);
			}
		}
	}

	/**
	 * Creates an item with an initial geometry.
	 * <p>
	 * Calls {@link #WStandardItem(int rows, int columns) this(rows, 1)}
	 */
	public WStandardItem(int rows) {
		this(rows, 1);
	}

	/**
	 * Sets the text.
	 * <p>
	 * The text is stored as {@link ItemDataRole#DisplayRole DisplayRole} data.
	 * <p>
	 * The default text is empty (&quot;&quot;).
	 * <p>
	 * 
	 * @see WStandardItem#getText()
	 * @see WStandardItem#setData(Object d, int role)
	 */
	public void setText(CharSequence text) {
		this.setData(text, ItemDataRole.DisplayRole);
	}

	/**
	 * Returns the text.
	 * <p>
	 * 
	 * @see WStandardItem#setText(CharSequence text)
	 */
	public WString getText() {
		Object d = this.getData(ItemDataRole.DisplayRole);
		return StringUtils.asString(d);
	}

	/**
	 * Sets the icon url.
	 * <p>
	 * The icon is stored as {@link ItemDataRole#DecorationRole DecorationRole}
	 * data.
	 * <p>
	 * The default icon url is empty (&quot;&quot;).
	 * <p>
	 * 
	 * @see WStandardItem#getIcon()
	 * @see WStandardItem#setData(Object d, int role)
	 */
	public void setIcon(String uri) {
		this.setData(uri, ItemDataRole.DecorationRole);
	}

	/**
	 * Returns the icon url.
	 * <p>
	 * 
	 * @see WStandardItem#setIcon(String uri)
	 */
	public String getIcon() {
		Object d = this.getData(ItemDataRole.DisplayRole);
		if (!(d == null) && d.getClass().equals(String.class)) {
			return (String) d;
		} else {
			return "";
		}
	}

	/**
	 * Sets the CSS style class.
	 * <p>
	 * The style class is stored as {@link ItemDataRole#StyleClassRole
	 * StyleClassRole} data.
	 * <p>
	 * The default style class is empty (&quot;&quot;).
	 * <p>
	 * 
	 * @see WStandardItem#getStyleClass()
	 * @see WStandardItem#setData(Object d, int role)
	 */
	public void setStyleClass(CharSequence styleClass) {
		this.setData(styleClass, ItemDataRole.StyleClassRole);
	}

	/**
	 * Returns the item style class.
	 * <p>
	 * 
	 * @see WStandardItem#setStyleClass(CharSequence styleClass)
	 */
	public WString getStyleClass() {
		Object d = this.getData(ItemDataRole.StyleClassRole);
		if (!(d == null) && d.getClass().equals(WString.class)) {
			return (WString) d;
		} else {
			return new WString();
		}
	}

	/**
	 * Sets a tool tip.
	 * <p>
	 * The tool tip is stored as {@link ItemDataRole#ToolTipRole ToolTipRole}
	 * data.
	 * <p>
	 * The default tool tip is empty (&quot;&quot;).
	 * <p>
	 * 
	 * @see WStandardItem#getToolTip()
	 * @see WStandardItem#setData(Object d, int role)
	 */
	public void setToolTip(CharSequence toolTip) {
		this.setData(toolTip, ItemDataRole.ToolTipRole);
	}

	/**
	 * Returns the tool tip.
	 * <p>
	 * 
	 * @see WStandardItem#setToolTip(CharSequence toolTip)
	 */
	public WString getToolTip() {
		Object d = this.getData(ItemDataRole.ToolTipRole);
		if (!(d == null) && d.getClass().equals(WString.class)) {
			return (WString) d;
		} else {
			return new WString();
		}
	}

	/**
	 * Sets an anchor to an internal path.
	 * <p>
	 * The internal path is stored as {@link ItemDataRole#InternalPathRole
	 * InternalPathRole} data.
	 * <p>
	 * 
	 * @see WStandardItem#getInternalPath()
	 * @see WStandardItem#setData(Object d, int role)
	 */
	public void setInternalPath(String internalpath) {
		this.setData(internalpath, ItemDataRole.InternalPathRole);
	}

	/**
	 * Returns the anchor to an internal path.
	 * <p>
	 * 
	 * @see WStandardItem#setInternalPath(String internalpath)
	 * @see WStandardItem#setData(Object d, int role)
	 */
	public String getInternalPath() {
		Object d = this.getData(ItemDataRole.InternalPathRole);
		if (!(d == null) && d.getClass().equals(String.class)) {
			return (String) d;
		} else {
			return "";
		}
	}

	/**
	 * Sets an anchor to an external URL.
	 * <p>
	 * The anchor Url is stored as {@link ItemDataRole#UrlRole UrlRole} data.
	 * <p>
	 * 
	 * @see WStandardItem#setInternalPath(String internalpath)
	 * @see WStandardItem#setData(Object d, int role)
	 */
	public void setUrl(String url) {
		this.setData(url, ItemDataRole.UrlRole);
	}

	/**
	 * Returns the url referenced by this item.
	 * <p>
	 * 
	 * @see WStandardItem#setUrl(String url)
	 * @see WStandardItem#setData(Object d, int role)
	 */
	public String getUrl() {
		Object d = this.getData(ItemDataRole.UrlRole);
		if (!(d == null) && d.getClass().equals(String.class)) {
			return (String) d;
		} else {
			return "";
		}
	}

	/**
	 * Checks or unchecks the item.
	 * <p>
	 * The value is stored as {@link ItemDataRole#CheckStateRole CheckStateRole}
	 * data.
	 * <p>
	 * By default, an item is not checked.
	 * <p>
	 * Note: this requires that the item is checkable (see
	 * {@link WStandardItem#setCheckable(boolean checkable) setCheckable()}).
	 * <p>
	 * If the item is tri-state, you may consider using
	 * {@link WStandardItem#setCheckState(CheckState state) setCheckState()}
	 * instead which supports also setting the third
	 * {@link CheckState#PartiallyChecked} state.
	 * <p>
	 * 
	 * @see WStandardItem#setCheckable(boolean checkable)
	 * @see WStandardItem#setCheckState(CheckState state)
	 */
	public void setChecked(boolean checked) {
		if (this.isChecked() != checked) {
			this.setCheckState(checked ? CheckState.Checked
					: CheckState.Unchecked);
		}
	}

	/**
	 * Returns whether the item is checked.
	 * <p>
	 * 
	 * @see WStandardItem#setChecked(boolean checked)
	 */
	public boolean isChecked() {
		return this.getCheckState() == CheckState.Checked;
	}

	/**
	 * Sets the check state.
	 * <p>
	 * Like {@link WStandardItem#setChecked(boolean checked) setChecked()}, this
	 * sets the check state, but allows also setting the
	 * {@link CheckState#PartiallyChecked} state when the item is tri-state
	 * checkable.
	 * <p>
	 * The value is stored as {@link ItemDataRole#CheckStateRole CheckStateRole}
	 * data.
	 * <p>
	 * 
	 * @see WStandardItem#setCheckable(boolean checkable)
	 * @see WStandardItem#setData(Object d, int role)
	 */
	public void setCheckState(CheckState state) {
		if (this.getCheckState() != state) {
			if (this.isTristate()) {
				this.setData(state, ItemDataRole.CheckStateRole);
			} else {
				this.setData(state == CheckState.Checked,
						ItemDataRole.CheckStateRole);
			}
		}
	}

	/**
	 * Returns the item&apos;s check state.
	 * <p>
	 * 
	 * @see WStandardItem#setCheckState(CheckState state)
	 */
	public CheckState getCheckState() {
		Object d = this.getData(ItemDataRole.CheckStateRole);
		if ((d == null)) {
			return CheckState.Unchecked;
		} else {
			if (d.getClass().equals(Boolean.class)) {
				return (Boolean) d ? CheckState.Checked : CheckState.Unchecked;
			} else {
				if (d.getClass().equals(CheckState.class)) {
					return (CheckState) d;
				} else {
					return CheckState.Unchecked;
				}
			}
		}
	}

	/**
	 * Sets the flags.
	 * <p>
	 * The default flag value is {@link ItemFlag#ItemIsSelectable
	 * ItemIsSelectable}.
	 * <p>
	 * 
	 * @see ItemFlag
	 * @see WStandardItem#getFlags()
	 * @see WStandardItem#setCheckable(boolean checkable)
	 */
	public void setFlags(EnumSet<ItemFlag> flags) {
		if (!this.flags_.equals(flags)) {
			this.flags_ = EnumSet.copyOf(flags);
			this.signalModelDataChange();
		}
	}

	/**
	 * Sets the flags.
	 * <p>
	 * Calls {@link #setFlags(EnumSet flags) setFlags(EnumSet.of(flag, flags))}
	 */
	public final void setFlags(ItemFlag flag, ItemFlag... flags) {
		setFlags(EnumSet.of(flag, flags));
	}

	/**
	 * Returns the flags.
	 * <p>
	 * 
	 * @see WStandardItem#setFlags(EnumSet flags)
	 */
	public EnumSet<ItemFlag> getFlags() {
		return this.flags_;
	}

	/**
	 * Makes the item checkable.
	 * <p>
	 * Adds {@link ItemFlag#ItemIsUserCheckable ItemIsUserCheckable} to the
	 * item&apos;s flags.
	 * <p>
	 * 
	 * @see WStandardItem#setFlags(EnumSet flags)
	 * @see WStandardItem#setChecked(boolean checked)
	 */
	public void setCheckable(boolean checkable) {
		if (!this.isCheckable() && checkable) {
			this.flags_.add(ItemFlag.ItemIsUserCheckable);
			this.signalModelDataChange();
		}
		if (this.isCheckable() && !checkable) {
			this.flags_.remove(ItemFlag.ItemIsUserCheckable);
			this.signalModelDataChange();
		}
	}

	/**
	 * Returns whether the item is checkable.
	 * <p>
	 * 
	 * @see WStandardItem#setCheckable(boolean checkable)
	 */
	public boolean isCheckable() {
		return !EnumUtils.mask(this.flags_, ItemFlag.ItemIsUserCheckable)
				.isEmpty();
	}

	/**
	 * Makes the item tri-state checkable.
	 * <p>
	 * When <code>tristate</code> is <code>true</code>, the item is checkable
	 * with three states: {@link CheckState#Unchecked},
	 * {@link CheckState#Checked}, and {@link CheckState#PartiallyChecked}.
	 * <p>
	 * This requires that the item is also checkable (see
	 * {@link WStandardItem#setCheckable(boolean checkable) setCheckable()})
	 * <p>
	 * 
	 * @see WStandardItem#setCheckable(boolean checkable)
	 */
	public void setTristate(boolean tristate) {
		this.flags_.add(ItemFlag.ItemIsTristate);
	}

	/**
	 * Returns whether the item is tri-state checkable.
	 * <p>
	 * 
	 * @see WStandardItem#setTristate(boolean tristate)
	 */
	public boolean isTristate() {
		return !EnumUtils.mask(this.flags_, ItemFlag.ItemIsTristate).isEmpty();
	}

	void setEditable(boolean editable) {
		if (!this.isEditable()) {
			this.flags_.add(ItemFlag.ItemIsEditable);
			this.signalModelDataChange();
		}
	}

	boolean isEditable() {
		return !EnumUtils.mask(this.flags_, ItemFlag.ItemIsEditable).isEmpty();
	}

	/**
	 * Sets item data.
	 * <p>
	 * Sets item data for the given role.
	 * <p>
	 * 
	 * @see WStandardItem#getData(int role)
	 */
	public void setData(Object d, int role) {
		if (role == ItemDataRole.EditRole) {
			if ((d == null)) {
				return;
			} else {
				role = ItemDataRole.DisplayRole;
			}
		}
		this.data_.put(role, d);
		if (this.model_ != null) {
			WModelIndex self = this.getIndex();
			this.model_.dataChanged().trigger(self, self);
			this.model_.itemChanged().trigger(this);
		}
	}

	/**
	 * Sets item data.
	 * <p>
	 * Calls {@link #setData(Object d, int role) setData(d,
	 * ItemDataRole.UserRole)}
	 */
	public final void setData(Object d) {
		setData(d, ItemDataRole.UserRole);
	}

	/**
	 * Returns item data.
	 * <p>
	 * Returns item data for the given role.
	 * <p>
	 * 
	 * @see WStandardItem#getData(int role)
	 */
	public Object getData(int role) {
		Object i = this.data_.get(role);
		if (i != null) {
			return i;
		} else {
			return null;
		}
	}

	/**
	 * Returns item data.
	 * <p>
	 * Returns {@link #getData(int role) getData(ItemDataRole.UserRole)}
	 */
	public final Object getData() {
		return getData(ItemDataRole.UserRole);
	}

	/**
	 * Returns whether the item has any children.
	 * <p>
	 * This is a convenience method and checks whether
	 * {@link WStandardItem#getRowCount() getRowCount()} and
	 * {@link WStandardItem#getColumnCount() getColumnCount()} differ both from
	 * 0.
	 * <p>
	 * 
	 * @see WStandardItem#getRowCount()
	 * @see WStandardItem#getColumnCount()
	 */
	public boolean hasChildren() {
		return this.columns_ != null;
	}

	/**
	 * Sets the row count.
	 * <p>
	 * If <code>rows</code> is bigger than the current row count, empty rows are
	 * appended.
	 * <p>
	 * If <code>rows</code> is smaller than the current row count, rows are
	 * deleted at the end.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>If <code>rows</code> &gt; 0, and
	 * {@link WStandardItem#getColumnCount() getColumnCount()} == 0, columnCount
	 * is first increased to 1 using setColumnCount(1).</i>
	 * </p>
	 * 
	 * @see WStandardItem#setColumnCount(int columns)
	 * @see WStandardItem#getRowCount()
	 */
	public void setRowCount(int rows) {
		if (rows > this.getRowCount()) {
			this.insertRows(this.getRowCount(), rows - this.getRowCount());
		} else {
			if (rows < this.getRowCount()) {
				this.removeRows(rows, this.getRowCount() - rows);
			}
		}
	}

	/**
	 * Returns the row count.
	 * <p>
	 * 
	 * @see WStandardItem#setRowCount(int rows)
	 */
	public int getRowCount() {
		return this.columns_ != null ? this.columns_.get(0).size() : 0;
	}

	/**
	 * Sets the column count.
	 * <p>
	 * If <code>columns</code> is bigger than the current column count, empty
	 * columns are appended.
	 * <p>
	 * If <code>columns</code> is smaller than the current column count, columns
	 * are deleted at the end.
	 * <p>
	 * 
	 * @see WStandardItem#setRowCount(int rows)
	 * @see WStandardItem#getColumnCount()
	 */
	public void setColumnCount(int columns) {
		if (columns > this.getColumnCount()) {
			this.insertColumns(this.getColumnCount(), columns
					- this.getColumnCount());
		} else {
			if (columns < this.getColumnCount()) {
				this.removeColumns(columns, this.getColumnCount() - columns);
			}
		}
	}

	/**
	 * Returns the column count.
	 * <p>
	 * 
	 * @see WStandardItem#setRowCount(int rows)
	 */
	public int getColumnCount() {
		return this.columns_ != null ? this.columns_.size() : 0;
	}

	/**
	 * Add a single column of items.
	 * <p>
	 * Appends a single column of <code>items</code>. If necessary, the row
	 * count is increased.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * insertColumn(columnCount(), items);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#insertColumn(int column, List items)
	 * @see WStandardItem#appendRow(List items)
	 */
	public void appendColumn(List<WStandardItem> items) {
		this.insertColumn(this.getColumnCount(), items);
	}

	/**
	 * Inserts a single column of items.
	 * <p>
	 * Inserts a single column of <code>items</code> at column
	 * <code>column</code>. If necessary, the row count is increased.
	 * <p>
	 * 
	 * @see WStandardItem#insertRow(int row, List items)
	 */
	public void insertColumn(int column, List<WStandardItem> items) {
		int rc = this.getRowCount();
		if (!(this.columns_ != null)) {
			this.columns_ = new ArrayList<List<WStandardItem>>();
		} else {
			if (rc < items.size()) {
				this.setRowCount(items.size());
			}
		}
		if (this.model_ != null) {
			this.model_.beginInsertColumns(this.getIndex(), column, column);
		}
		this.columns_.add(0 + column, items);
		for (int i = 0; i < items.size(); ++i) {
			if (items.get(i) != null) {
				this.adoptChild(i, column, items.get(i));
			}
		}
		if (items.size() < rc) {
			List<WStandardItem> inserted = this.columns_.get(column);
			CollectionUtils.resize(inserted, items.size());
		}
		this.renumberColumns(column + 1);
		if (this.model_ != null) {
			this.model_.endInsertColumns();
		}
	}

	/**
	 * Add a single row of items.
	 * <p>
	 * Appends a single row of <code>items</code>. If necessary, the column
	 * count is increased.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * insertRow(rowCount(), items);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#insertRow(int row, List items)
	 * @see WStandardItem#appendColumn(List items)
	 */
	public void appendRow(List<WStandardItem> items) {
		this.insertRow(this.getRowCount(), items);
	}

	/**
	 * Inserts a single row of items.
	 * <p>
	 * Inserts a single row of <i>items</i> at row <code>row</code>. If
	 * necessary, the column count is increased.
	 * <p>
	 * 
	 * @see WStandardItem#insertColumn(int column, List items)
	 */
	public void insertRow(int row, List<WStandardItem> items) {
		if (!(this.columns_ != null)) {
			this.setColumnCount(1);
		}
		int cc = this.getColumnCount();
		if (cc < items.size()) {
			this.setColumnCount(items.size());
			cc = items.size();
		}
		if (this.model_ != null) {
			this.model_.beginInsertRows(this.getIndex(), row, row);
		}
		for (int i = 0; i < cc; ++i) {
			List<WStandardItem> c = this.columns_.get(i);
			WStandardItem item = i < items.size() ? items.get(i) : null;
			c.add(0 + row, item);
			this.adoptChild(row, i, item);
		}
		this.renumberRows(row + 1);
		if (this.model_ != null) {
			this.model_.endInsertRows();
		}
	}

	/**
	 * Inserts a number of empty columns.
	 * <p>
	 * Inserts <i>count</i> empty columns at position <code>column</code>.
	 * <p>
	 * 
	 * @see WStandardItem#insertRows(int row, int count)
	 */
	public void insertColumns(int column, int count) {
		if (count > 0) {
			if (this.model_ != null) {
				this.model_.beginInsertColumns(this.getIndex(), column, column
						+ count - 1);
			}
			int rc = this.getRowCount();
			if (!(this.columns_ != null)) {
				this.columns_ = new ArrayList<List<WStandardItem>>();
			}
			for (int i = 0; i < count; ++i) {
				List<WStandardItem> c = new ArrayList<WStandardItem>();
				{
					int insertPos = c.size();
					for (int ii = 0; ii < rc; ++ii)
						c.add(insertPos + ii, (WStandardItem) null);
				}
				;
				this.columns_.add(0 + column + i, c);
			}
			this.renumberColumns(column + count);
			if (this.model_ != null) {
				this.model_.endInsertColumns();
			}
		}
	}

	/**
	 * Inserts a number of empty rows.
	 * <p>
	 * Inserts <i>count</i> empty rows at position <code>row</code>.
	 * <p>
	 * 
	 * @see WStandardItem#insertColumns(int column, int count)
	 */
	public void insertRows(int row, int count) {
		if (count > 0) {
			if (this.model_ != null) {
				this.model_.beginInsertRows(this.getIndex(), row, row + count
						- 1);
			}
			if (!(this.columns_ != null)) {
				this.setColumnCount(1);
			}
			int cc = this.getColumnCount();
			for (int i = 0; i < cc; ++i) {
				List<WStandardItem> c = this.columns_.get(i);
				{
					int insertPos = 0 + row;
					for (int ii = 0; ii < count; ++ii)
						c.add(insertPos + ii, (WStandardItem) null);
				}
				;
			}
			this.renumberRows(row + count);
			if (this.model_ != null) {
				this.model_.endInsertRows();
			}
		}
	}

	/**
	 * Appends a row containing one item.
	 * <p>
	 * This is a convenience method for nodes with a single column (for example
	 * for tree nodes). This adds a row with a single item, and is equivalent
	 * to:
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * insertRow(rowCount(), item);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#insertRow(int row, WStandardItem item)
	 */
	public void appendRow(WStandardItem item) {
		this.insertRow(this.getRowCount(), item);
	}

	/**
	 * Inserts a row containing one item.
	 * <p>
	 * This is a convenience method for nodes with a single column (for example
	 * for tree nodes). This inserts a row with a single item, and is equivalent
	 * to:
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * List&lt;WStandardItem&gt; r;
	 * r.add(item);
	 * insertRow(row, r);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#insertRow(int row, List items)
	 */
	public void insertRow(int row, WStandardItem item) {
		List<WStandardItem> r = new ArrayList<WStandardItem>();
		r.add(item);
		this.insertRow(row, r);
	}

	/**
	 * Appends multiple rows containing one item.
	 * <p>
	 * This is a convenience method for nodes with a single column (for example
	 * for tree nodes). This adds a number of rows, each containing a single
	 * item, and is equivalent to:
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * insertRows(rowCount(), items);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#insertRows(int row, List items)
	 */
	public void appendRows(List<WStandardItem> items) {
		this.insertRows(this.getRowCount(), items);
	}

	/**
	 * Inserts multiple rows containing one item.
	 * <p>
	 * This is a convenience method for nodes with a single column (for example
	 * for tree nodes). This inserts a number of rows at row <code>row</code>,
	 * each containing a single item, and is equivalent to:
	 * <p>
	 * 
	 * @see WStandardItem#insertRow(int row, WStandardItem item)
	 */
	public void insertRows(int row, List<WStandardItem> items) {
		List<WStandardItem> r = new ArrayList<WStandardItem>();
		r.add((WStandardItem) null);
		for (int i = 0; i < items.size(); ++i) {
			r.set(0, items.get(i));
			this.insertRow(row + i, r);
		}
	}

	/**
	 * Sets a child item.
	 * <p>
	 * Sets a child item <i>item</i> at position (<code>row</code>,
	 * <code>column</code>). If an item was previously set, it is deleted first.
	 * <p>
	 * If necessary, the {@link WStandardItem#getRowCount() getRowCount()}
	 * and/or the {@link WStandardItem#getColumnCount() getColumnCount()} is
	 * increased.
	 * <p>
	 * 
	 * @see WStandardItem#getChild(int row, int column)
	 */
	public void setChild(int row, int column, WStandardItem item) {
		if (column >= this.getColumnCount()) {
			this.setColumnCount(column + 1);
		}
		if (row >= this.getRowCount()) {
			this.setRowCount(row + 1);
		}
		;
		this.columns_.get(column).set(row, item);
		this.adoptChild(row, column, item);
		if (this.model_ != null) {
			WModelIndex self = item.getIndex();
			this.model_.dataChanged().trigger(self, self);
			this.model_.itemChanged().trigger(this);
		}
	}

	/**
	 * Sets a child item.
	 * <p>
	 * This is a convenience method for nodes with a single column (e.g. tree
	 * nodes), and is equivalent to: <blockquote>
	 * 
	 * <pre>
	 * setChild(row, 0, item);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#setChild(int row, int column, WStandardItem item)
	 */
	public void setChild(int row, WStandardItem item) {
		this.setChild(row, 0, item);
	}

	/**
	 * Returns a child item.
	 * <p>
	 * Returns the child item at position (<i>row</i>, <code>column</code>).
	 * This may be <code>null</code> if an item was not previously set, or if
	 * the position is out of bounds.
	 * <p>
	 * 
	 * @see WStandardItem#setChild(int row, int column, WStandardItem item)
	 */
	public WStandardItem getChild(int row, int column) {
		if (row < this.getRowCount() && column < this.getColumnCount()) {
			return this.columns_.get(column).get(row);
		} else {
			return null;
		}
	}

	/**
	 * Returns a child item.
	 * <p>
	 * Returns {@link #getChild(int row, int column) getChild(row, 0)}
	 */
	public final WStandardItem getChild(int row) {
		return getChild(row, 0);
	}

	/**
	 * Takes a child out of the item.
	 * <p>
	 * Returns the child item at position (<i>row</i>, <code>column</code>), and
	 * removes it (by setting <code>null</code> instead). Ownership of the item
	 * is transferred to the caller.
	 * <p>
	 * 
	 * @see WStandardItem#getChild(int row, int column)
	 * @see WStandardItem#setChild(int row, int column, WStandardItem item)
	 */
	public WStandardItem takeChild(int row, int column) {
		WStandardItem result = this.getChild(row, column);
		if (result != null) {
			this.orphanChild(result);
			this.columns_.get(column).set(row, null);
		}
		return result;
	}

	/**
	 * Takes a column of children out of the item.
	 * <p>
	 * Returns the column <code>column</code>, and removes the column from the
	 * model (reducing the column count by one). Ownership of all items is
	 * transferred to the caller.
	 * <p>
	 * 
	 * @see WStandardItem#takeRow(int row)
	 * @see WStandardItem#removeColumn(int column)
	 */
	public List<WStandardItem> takeColumn(int column) {
		if (this.model_ != null) {
			this.model_.beginRemoveColumns(this.getIndex(), column, column);
		}
		List<WStandardItem> result = this.columns_.get(column);
		this.columns_.remove(0 + column);
		if (this.columns_.isEmpty()) {
			;
			this.columns_ = null;
		}
		for (int i = 0; i < result.size(); ++i) {
			this.orphanChild(result.get(i));
		}
		this.renumberColumns(column);
		if (this.model_ != null) {
			this.model_.endRemoveColumns();
		}
		return result;
	}

	/**
	 * Takes a row of children out of the item.
	 * <p>
	 * Returns the row <code>row</code>, and removes the row from the model
	 * (reducing the row count by one). Ownership of all items is transferred to
	 * the caller.
	 * <p>
	 * 
	 * @see WStandardItem#takeColumn(int column)
	 * @see WStandardItem#removeRow(int row)
	 */
	public List<WStandardItem> takeRow(int row) {
		if (this.model_ != null) {
			this.model_.beginRemoveRows(this.getIndex(), row, row);
		}
		List<WStandardItem> result = new ArrayList<WStandardItem>();
		{
			int insertPos = result.size();
			for (int ii = 0; ii < this.getColumnCount(); ++ii)
				result.add(insertPos + ii, (WStandardItem) null);
		}
		;
		for (int i = 0; i < result.size(); ++i) {
			List<WStandardItem> c = this.columns_.get(i);
			result.set(i, c.get(row));
			this.orphanChild(result.get(i));
			c.remove(0 + row);
		}
		this.renumberRows(row);
		if (this.model_ != null) {
			this.model_.endRemoveRows();
		}
		return result;
	}

	/**
	 * Removes a single column.
	 * <p>
	 * Removes the column <code>column</code> from the model (reducing the
	 * column count by one). Is equivalent to: <blockquote>
	 * 
	 * <pre>
	 * removeColumns(column, 1);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#removeColumns(int column, int count)
	 * @see WStandardItem#takeColumn(int column)
	 */
	public void removeColumn(int column) {
		this.removeColumns(column, 1);
	}

	/**
	 * Removes a number of columns.
	 * <p>
	 * Removes <code>count</code> columns from the model (reducing the column
	 * count by <code>count</code>).
	 * <p>
	 * 
	 * @see WStandardItem#removeColumn(int column)
	 * @see WStandardItem#removeRows(int row, int count)
	 */
	public void removeColumns(int column, int count) {
		if (this.model_ != null) {
			this.model_.beginRemoveColumns(this.getIndex(), column, column
					+ count - 1);
		}
		for (int i = 0; i < count; ++i) {
			for (int j = 0; j < this.getRowCount(); ++j) {
				;
			}
		}
		for (int ii = 0; ii < (0 + column + count) - (0 + column); ++ii)
			this.columns_.remove(0 + column);
		;
		if (this.columns_.isEmpty()) {
			;
			this.columns_ = null;
		}
		this.renumberColumns(column);
		if (this.model_ != null) {
			this.model_.endRemoveColumns();
		}
	}

	/**
	 * Removes a single row.
	 * <p>
	 * Removes the row <code>row</code> from the model (reducing the row count
	 * by one). Is equivalent to: <blockquote>
	 * 
	 * <pre>
	 * removeRows(row, 1);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#removeRows(int row, int count)
	 * @see WStandardItem#takeRow(int row)
	 */
	public void removeRow(int row) {
		this.removeRows(row, 1);
	}

	/**
	 * Removes a number of rows.
	 * <p>
	 * Removes <code>count</code> rows from the model (reducing the row count by
	 * <code>count</code>).
	 * <p>
	 * 
	 * @see WStandardItem#removeRow(int row)
	 * @see WStandardItem#removeColumns(int column, int count)
	 */
	public void removeRows(int row, int count) {
		if (this.model_ != null) {
			this.model_.beginRemoveRows(this.getIndex(), row, row + count - 1);
		}
		for (int i = 0; i < this.getColumnCount(); ++i) {
			List<WStandardItem> c = this.columns_.get(i);
			for (int j = 0; j < count; ++j) {
				;
			}
			for (int ii = 0; ii < (0 + row + count) - (0 + row); ++ii)
				c.remove(0 + row);
			;
		}
		this.renumberRows(row);
		if (this.model_ != null) {
			this.model_.endRemoveRows();
		}
	}

	/**
	 * Returns the model index for this item.
	 * <p>
	 * 
	 * @see WStandardItemModel#indexFromItem(WStandardItem item)
	 */
	public WModelIndex getIndex() {
		if (this.model_ != null) {
			return this.model_.indexFromItem(this);
		} else {
			return null;
		}
	}

	/**
	 * Returns the model.
	 * <p>
	 * This is the model that this item belongs to, or 0 if the item is not
	 * associated with a model.
	 */
	public WStandardItemModel getModel() {
		return this.model_;
	}

	/**
	 * Returns the parent item.
	 * <p>
	 * Returns the parent item.
	 * <p>
	 * 
	 * @see WStandardItem#setChild(int row, int column, WStandardItem item)
	 */
	public WStandardItem getParent() {
		return this.parent_;
	}

	/**
	 * Returns the row index.
	 * <p>
	 * Returns the row index of this item in the parent.
	 * <p>
	 * 
	 * @see WStandardItem#getColumn()
	 */
	public int getRow() {
		return this.row_;
	}

	/**
	 * Returns the column index.
	 * <p>
	 * Returns the column index of this item in the parent.
	 * <p>
	 * 
	 * @see WStandardItem#getColumn()
	 */
	public int getColumn() {
		return this.column_;
	}

	/**
	 * Returns a clone of this item.
	 * <p>
	 * 
	 * @see WStandardItemModel#setItemPrototype(WStandardItem item)
	 */
	public WStandardItem clone() {
		WStandardItem result = new WStandardItem();
		result.data_ = new TreeMap<Integer, Object>(this.data_);
		result.flags_ = EnumSet.copyOf(this.flags_);
		return result;
	}

	/**
	 * Sorts the children according to a given column and sort order.
	 * <p>
	 * Children of this item, and all children items are sorted recursively.
	 * Existing model indexes will be invalidated by the operation (will point
	 * to other items).
	 * <p>
	 * The {@link WAbstractItemModel#layoutAboutToBeChanged()
	 * WAbstractItemModel#layoutAboutToBeChanged()} and
	 * {@link WAbstractItemModel#layoutChanged()
	 * WAbstractItemModel#layoutChanged()} signals are emitted before and after
	 * the operation so that you get a chance to invalidate or update model
	 * indexes.
	 * <p>
	 * 
	 * @see WStandardItem#compare(WStandardItem other)
	 * @see WStandardItemModel#setSortRole(int role)
	 */
	public void sortChildren(int column, SortOrder order) {
		if (this.model_ != null) {
			this.model_.layoutAboutToBeChanged().trigger();
		}
		this.recursiveSortChildren(column, order);
		if (this.model_ != null) {
			this.model_.layoutChanged().trigger();
		}
	}

	/**
	 * Compares the item with another item.
	 * <p>
	 * This is used during sorting (from
	 * {@link WStandardItem#sortChildren(int column, SortOrder order)
	 * sortChildren()}), and returns which of the two items is the lesser, based
	 * on their data.
	 * <p>
	 * The default implementation compares the data based on the value
	 * corresponding to the {@link WStandardItemModel#getSortRole()
	 * WStandardItemModel#getSortRole()}.
	 * <p>
	 * 
	 * @see WStandardItem#sortChildren(int column, SortOrder order)
	 * @see WStandardItemModel#setSortRole(int role)
	 */
	int compare(WStandardItem other) {
		int role = this.model_ != null ? this.model_.getSortRole()
				: ItemDataRole.DisplayRole;
		Object d1 = this.getData(role);
		Object d2 = other.getData(role);
		return ObjectUtils.compare(d1, d2);
	}

	WStandardItemModel model_;
	private WStandardItem parent_;
	private int row_;
	private int column_;
	private SortedMap<Integer, Object> data_;
	private EnumSet<ItemFlag> flags_;
	private List<List<WStandardItem>> columns_;

	private void signalModelDataChange() {
		if (this.model_ != null) {
			WModelIndex self = this.getIndex();
			this.model_.dataChanged().trigger(self, self);
		}
	}

	private void adoptChild(int row, int column, WStandardItem item) {
		if (item != null) {
			item.parent_ = this;
			item.row_ = row;
			item.column_ = column;
			item.setModel(this.model_);
		}
	}

	private void orphanChild(WStandardItem item) {
		if (item != null) {
			item.parent_ = null;
			item.row_ = -1;
			item.column_ = -1;
			item.setModel((WStandardItemModel) null);
		}
	}

	private void setModel(WStandardItemModel model) {
		this.model_ = model;
		for (int i = 0; i < this.getColumnCount(); ++i) {
			for (int j = 0; j < this.getRowCount(); ++j) {
				WStandardItem c = this.columns_.get(i).get(j);
				if (c != null) {
					c.setModel(model);
				}
			}
		}
	}

	private void recursiveSortChildren(int column, SortOrder order) {
		if (column < this.getColumnCount()) {
			List<Integer> permutation = new ArrayList<Integer>();
			for (int i = 0; i < this.getRowCount(); ++i) {
				permutation.add(i);
			}
			Collections.sort(permutation, new WStandardItemCompare(this,
					column, order));
			for (int c = 0; c < this.getColumnCount(); ++c) {
				List<WStandardItem> temp = new ArrayList<WStandardItem>();
				List<WStandardItem> cc = this.columns_.get(c);
				for (int r = 0; r < this.getRowCount(); ++r) {
					temp.add(cc.get(permutation.get(r)));
					if (temp.get(r) != null) {
						temp.get(r).row_ = r;
					}
				}
				this.columns_.set(c, temp);
			}
		}
		for (int c = 0; c < this.getColumnCount(); ++c) {
			for (int r = 0; r < this.getRowCount(); ++r) {
				WStandardItem ch = this.getChild(r, c);
				if (ch != null) {
					ch.recursiveSortChildren(column, order);
				}
			}
		}
	}

	private void renumberColumns(int column) {
		for (int c = column; c < this.getColumnCount(); ++c) {
			for (int r = 0; r < this.getRowCount(); ++r) {
				WStandardItem item = this.getChild(r, c);
				if (item != null) {
					item.column_ = c;
				}
			}
		}
	}

	private void renumberRows(int row) {
		for (int c = 0; c < this.getColumnCount(); ++c) {
			for (int r = row; r < this.getRowCount(); ++r) {
				WStandardItem item = this.getChild(r, c);
				if (item != null) {
					item.row_ = r;
				}
			}
		}
	}
}
