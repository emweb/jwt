/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;

/**
 * Abstract delegate class for rendering a view item.
 * <p>
 * 
 * Rendering of an item in a {@link WTreeView} is delegated to an implementation
 * of this delegate class. The default implementation used by {@link WTreeView}
 * is {@link WItemDelegate}. To provide specialized rendering support, you can
 * reimplement this class, and indicate to the treeview to use this delegate for
 * rendering items.
 * <p>
 * As a delegate is used for rendering multiple items, the class should not keep
 * state about one specific item.
 * <p>
 * An example of a delegate that always renders the text in a line-edit, and
 * saves the modified value back to the (editable) model.
 * <p>
 * 
 * @see WAbstractItemView#setItemDelegate(WAbstractItemDelegate delegate)
 * @see WAbstractItemView#setItemDelegateForColumn(int column,
 *      WAbstractItemDelegate delegate)
 */
public abstract class WAbstractItemDelegate extends WObject {
	/**
	 * Constructor.
	 */
	public WAbstractItemDelegate(WObject parent) {
		super(parent);
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WAbstractItemDelegate(WObject parent) this((WObject)null)}
	 */
	public WAbstractItemDelegate() {
		this((WObject) null);
	}

	/**
	 * Create or update a widget that renders an item.
	 * <p>
	 * The item is specified by its model <code>index</code>, which also
	 * indicates the model. If an existing widget already renders the item, but
	 * needs to be updated, it is passed as the <code>widget</code> parameter.
	 * You may decide to create a new widget, in which case you are responsible
	 * to delete the previous <code>widget</code> if it is not reused.
	 * <p>
	 * When <code>widget</code> is <code>null</code>, a new widget needs to be
	 * created.
	 * <p>
	 * The <code>flags</code> parameter indicates options for rendering the
	 * item.
	 */
	public abstract WWidget update(WWidget widget, WModelIndex index,
			EnumSet<ViewItemRenderFlag> flags);

	/**
	 * Create or update a widget that renders an item.
	 * <p>
	 * Returns {@link #update(WWidget widget, WModelIndex index, EnumSet flags)
	 * update(widget, index, EnumSet.of(flag, flags))}
	 */
	public final WWidget update(WWidget widget, WModelIndex index,
			ViewItemRenderFlag flag, ViewItemRenderFlag... flags) {
		return update(widget, index, EnumSet.of(flag, flags));
	}

	/**
	 * Update the model index of a widget.
	 * <p>
	 * This method is invoked by the view when due to row/column insertions or
	 * removals, an index was modified for a widget.
	 * <p>
	 * You should reimplement this method if you are storing the model index
	 * (e.g. for editing purposes) in the <code>widget</code>, which you should
	 * update to the new <code>index</code>.
	 * <p>
	 * The default implementation does nothing.
	 */
	public void updateModelIndex(WWidget widget, WModelIndex index) {
	}
}
