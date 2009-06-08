package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * An abstract proxy model for Wt&apos;s item models.
 * 
 * 
 * A proxy model does not store data, but presents data from a source model in
 * another way. It may provide filtering, sorting, or other computed changes to
 * the source model. A proxy model may be a fully functional model, that also
 * allows modification of the underlying model.
 * <p>
 * This abstract proxy model may be used as a starting point for implementing a
 * custom proxy model, when {@link WSortFilterProxyModel} is not adequate. It
 * implements data access and manipulation using the a virtual mapping method (
 * {@link WAbstractProxyModel#mapToSource(WModelIndex proxyIndex)}) to access
 * and manipulate the underlying {@link WAbstractProxyModel#getSourceModel()}.
 */
public abstract class WAbstractProxyModel extends WAbstractItemModel {
	/**
	 * Constructor.
	 */
	public WAbstractProxyModel(WObject parent) {
		super(parent);
		this.sourceModel_ = null;
	}

	public WAbstractProxyModel() {
		this((WObject) null);
	}

	/**
	 * Map a source model index to the proxy model.
	 * 
	 * This method returns a model index in the proxy model that corresponds to
	 * the model index <i>sourceIndex</i> in the source model. This method must
	 * only be implemented for source model indexes that are mapped and thus are
	 * the result of
	 * {@link WAbstractProxyModel#mapToSource(WModelIndex proxyIndex)}.
	 * <p>
	 * 
	 * @see WAbstractProxyModel#mapToSource(WModelIndex proxyIndex)
	 */
	public abstract WModelIndex mapFromSource(WModelIndex sourceIndex);

	/**
	 * Map a proxy model index to the source model.
	 * 
	 * This method returns a model index in the source model that corresponds to
	 * the proxy model index <i>proxyIndex</i>.
	 * <p>
	 * 
	 * @see WAbstractProxyModel#mapFromSource(WModelIndex sourceIndex)
	 */
	public abstract WModelIndex mapToSource(WModelIndex proxyIndex);

	/**
	 * Set a source model.
	 * 
	 * The source model provides the actual data for the proxy model.
	 * <p>
	 * Ownership of the source model is <i>not</i> transferred.
	 */
	public void setSourceModel(WAbstractItemModel sourceModel) {
		this.sourceModel_ = sourceModel;
	}

	/**
	 * Returns the source model.
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

	public boolean setHeaderData(int section, Orientation orientation,
			Object value, int role) {
		if (this.getRowCount() > 0) {
			if (orientation == Orientation.Vertical) {
				section = this.mapToSource(this.getIndex(section, 0)).getRow();
			} else {
				section = this.mapToSource(this.getIndex(0, section))
						.getColumn();
			}
		}
		return this.sourceModel_.setHeaderData(section, orientation, value,
				role);
	}

	public Object getHeaderData(int section, Orientation orientation, int role) {
		if (this.getRowCount() > 0) {
			if (orientation == Orientation.Vertical) {
				section = this.mapToSource(this.getIndex(section, 0)).getRow();
			} else {
				section = this.mapToSource(this.getIndex(0, section))
						.getColumn();
			}
		}
		return this.sourceModel_.getHeaderData(section, orientation, role);
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

	private WAbstractItemModel sourceModel_;
}
