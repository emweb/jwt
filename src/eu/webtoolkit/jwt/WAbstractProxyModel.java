/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;
import java.util.List;

/**
 * An abstract proxy model for Wt&apos;s item models.
 * <p>
 * 
 * A proxy model does not store data, but presents data from a source model in
 * another way. It may provide filtering, sorting, or other computed changes to
 * the source model. A proxy model may be a fully functional model, that also
 * allows modification of the underlying model.
 * <p>
 * This abstract proxy model may be used as a starting point for implementing a
 * custom proxy model, when {@link WSortFilterProxyModel} is not adequate. It
 * implements data access and manipulation using the a virtual mapping method (
 * {@link WAbstractProxyModel#mapToSource(WModelIndex proxyIndex) mapToSource()}
 * ) to access and manipulate the underlying
 * {@link WAbstractProxyModel#getSourceModel() getSourceModel()}.
 */
public abstract class WAbstractProxyModel extends WAbstractItemModel {
	/**
	 * Constructor.
	 */
	public WAbstractProxyModel(WObject parent) {
		super(parent);
		this.sourceModel_ = null;
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WAbstractProxyModel(WObject parent) this((WObject)null)}
	 */
	public WAbstractProxyModel() {
		this((WObject) null);
	}

	/**
	 * Maps a source model index to the proxy model.
	 * <p>
	 * This method returns a model index in the proxy model that corresponds to
	 * the model index <code>sourceIndex</code> in the source model. This method
	 * must only be implemented for source model indexes that are mapped and
	 * thus are the result of
	 * {@link WAbstractProxyModel#mapToSource(WModelIndex proxyIndex)
	 * mapToSource()}.
	 * <p>
	 * 
	 * @see WAbstractProxyModel#mapToSource(WModelIndex proxyIndex)
	 */
	public abstract WModelIndex mapFromSource(WModelIndex sourceIndex);

	/**
	 * Maps a proxy model index to the source model.
	 * <p>
	 * This method returns a model index in the source model that corresponds to
	 * the proxy model index <code>proxyIndex</code>.
	 * <p>
	 * 
	 * @see WAbstractProxyModel#mapFromSource(WModelIndex sourceIndex)
	 */
	public abstract WModelIndex mapToSource(WModelIndex proxyIndex);

	/**
	 * Sets the source model.
	 * <p>
	 * The source model provides the actual data for the proxy model.
	 * <p>
	 * Ownership of the source model is <i>not</i> transferred.
	 */
	public void setSourceModel(WAbstractItemModel sourceModel) {
		this.sourceModel_ = sourceModel;
	}

	/**
	 * Returns the source model.
	 * <p>
	 * 
	 * @see WAbstractProxyModel#setSourceModel(WAbstractItemModel sourceModel)
	 */
	public WAbstractItemModel getSourceModel() {
		return this.sourceModel_;
	}

	public Object getData(WModelIndex index, int role) {
		return this.sourceModel_.getData(this.mapToSource(index), role);
	}

	public boolean setData(WModelIndex index, Object value, int role) {
		return this.sourceModel_.setData(this.mapToSource(index), value, role);
	}

	public EnumSet<ItemFlag> getFlags(WModelIndex index) {
		return this.sourceModel_.getFlags(this.mapToSource(index));
	}

	public boolean insertColumns(int column, int count, WModelIndex parent) {
		return this.sourceModel_.insertColumns(column, count, parent);
	}

	public boolean insertRows(int row, int count, WModelIndex parent) {
		int sourceRow = this.mapToSource(this.getIndex(row, 0, parent))
				.getRow();
		return this.sourceModel_.insertRows(sourceRow, count, this
				.mapToSource(parent));
	}

	public boolean removeColumns(int column, int count, WModelIndex parent) {
		return this.sourceModel_.removeColumns(column, count, parent);
	}

	public boolean removeRows(int row, int count, WModelIndex parent) {
		int sourceRow = this.mapToSource(this.getIndex(row, 0, parent))
				.getRow();
		return this.sourceModel_.removeRows(sourceRow, count, this
				.mapToSource(parent));
	}

	public String getMimeType() {
		return this.sourceModel_.getMimeType();
	}

	public List<String> getAcceptDropMimeTypes() {
		return this.sourceModel_.getAcceptDropMimeTypes();
	}

	public void dropEvent(WDropEvent e, DropAction action, int row, int column,
			WModelIndex parent) {
		WModelIndex sourceParent = this.mapToSource(parent);
		int sourceRow = row;
		int sourceColumn = column;
		if (sourceRow != -1) {
			sourceRow = this.mapToSource(this.getIndex(row, 0, parent))
					.getRow();
		}
		this.sourceModel_.dropEvent(e, action, sourceRow, sourceColumn,
				sourceParent);
	}

	public Object toRawIndex(WModelIndex index) {
		return this.sourceModel_.toRawIndex(this.mapToSource(index));
	}

	public WModelIndex fromRawIndex(Object rawIndex) {
		return this.mapFromSource(this.sourceModel_.fromRawIndex(rawIndex));
	}

	/**
	 * Create a source model index.
	 * <p>
	 * This is a utility function that allows you to create indexes in the
	 * source model. In this way, you can reuse the internal pointers of the
	 * source model in proxy model indexes, and convert a proxy model index back
	 * to the source model index using this method.
	 */
	protected WModelIndex createSourceIndex(int row, int column, Object ptr) {
		return this.sourceModel_.createIndex(row, column, ptr);
	}

	private WAbstractItemModel sourceModel_;
}
