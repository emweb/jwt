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
 * A specialized tree node which allows additional data to be associated with
 * each node
 * 
 * 
 * Additional data for each column can be set using
 * {@link WTreeTableNode#setColumnWidget(int column, WWidget widget)}.
 * <p>
 * 
 * @see WTreeNode
 * @see WTreeTable
 */
public class WTreeTableNode extends WTreeNode {
	/**
	 * Create a new {@link WTreeTableNode}.
	 * 
	 * @see WTreeNode#WTreeNode(CharSequence labelText, WIconPair labelIcon,
	 *      WTreeNode parent)
	 */
	public WTreeTableNode(CharSequence labelText, WIconPair labelIcon,
			WTreeTableNode parentNode) {
		super(labelText, labelIcon);
		this.table_ = null;
		this.row_ = null;
		this.columnWidgets_ = new ArrayList<WTreeTableNode.ColumnWidget>();
		if (parentNode != null) {
			parentNode.addChildNode(this);
		}
	}

	public WTreeTableNode(CharSequence labelText) {
		this(labelText, (WIconPair) null, (WTreeTableNode) null);
	}

	public WTreeTableNode(CharSequence labelText, WIconPair labelIcon) {
		this(labelText, labelIcon, (WTreeTableNode) null);
	}

	/**
	 * Set a widget to be displayed in the given column for this node.
	 * 
	 * Columns are counted starting from 0 for the tree list itself, and 1 for
	 * the first additional column.
	 */
	public void setColumnWidget(int column, WWidget widget) {
		--column;
		this.createExtraColumns(column);
		if (column < (int) this.columnWidgets_.size()) {
			if (this.columnWidgets_.get(column).widget != null)
				this.columnWidgets_.get(column).widget.remove();
			this.columnWidgets_.set(column, new WTreeTableNode.ColumnWidget(
					widget, true));
		} else {
			this.columnWidgets_.add(new WTreeTableNode.ColumnWidget(widget,
					true));
		}
		widget.setInline(false);
		widget.setFloatSide(Side.Left);
		widget.resize(this.columnWidth(column + 1), WLength.Auto);
		if (column == (int) this.columnWidgets_.size() - 1) {
			this.row_.addWidget(widget);
		} else {
			this.row_.insertBefore(widget,
					this.columnWidgets_.get(column + 1).widget);
		}
	}

	/**
	 * Returns the widget set for a column.
	 * 
	 * Returns the widget set previously using
	 * {@link WTreeTableNode#setColumnWidget(int column, WWidget widget)}, or 0
	 * if no widget was previously set.
	 */
	public WWidget getColumnWidget(int column) {
		--column;
		return column < (int) this.columnWidgets_.size()
				&& this.columnWidgets_.get(column).isSet ? this.columnWidgets_
				.get(column).widget : null;
	}

	public WTreeTable getTable() {
		return this.table_;
	}

	public void addChildNode(WTreeNode node) {
		super.addChildNode(node);
		if (this.table_ != null) {
			(((node) instanceof WTreeTableNode ? (WTreeTableNode) (node) : null))
					.setTable(this.table_);
		}
	}

	/**
	 * Sets the table for this node.
	 * 
	 * This method is called when the node is inserted, directly, or indirectly
	 * into a table.
	 * <p>
	 * You may want to reimplement this method if you wish to customize the
	 * behaviour of the node depending on table properties. For example to only
	 * associate data with the node when the tree list is actually used inside a
	 * table.
	 */
	protected void setTable(WTreeTable table) {
		if (this.table_ != table) {
			this.table_ = table;
			for (int i = 0; i < this.getChildNodes().size(); ++i) {
				(((this.getChildNodes().get(i)) instanceof WTreeTableNode ? (WTreeTableNode) (this
						.getChildNodes().get(i))
						: null)).setTable(table);
			}
			this.createExtraColumns(table.getColumnCount() - 1);
			for (int i = 0; i < this.columnWidgets_.size(); ++i) {
				WWidget w = this.columnWidgets_.get(i).widget;
				w.resize(this.columnWidth(i + 1), w.getHeight());
			}
		}
	}

	private WTreeTable table_;
	private WContainerWidget row_;

	private static class ColumnWidget {
		public WWidget widget;
		public boolean isSet;

		public ColumnWidget(WWidget aWidget, boolean set) {
			this.widget = aWidget;
			this.isSet = set;
		}
	}

	private List<WTreeTableNode.ColumnWidget> columnWidgets_;

	private void createExtraColumns(int numColumns) {
		if (!(this.row_ != null)) {
			this.row_ = new WContainerWidget();
			this.getLabelArea().insertBefore(this.row_,
					this.getLabelArea().getChildren().get(0));
			this.row_.setFloatSide(Side.Right);
			this.getLabelArea().resize(
					new WLength(100, WLength.Unit.Percentage), WLength.Auto);
			this.getLabelArea().getTable().resize(
					new WLength(100, WLength.Unit.Percentage), WLength.Auto);
		}
		while ((int) this.columnWidgets_.size() < numColumns) {
			WText w = new WText(new WString(" "), this.row_);
			w.setInline(false);
			this.columnWidgets_.add(new WTreeTableNode.ColumnWidget(w, false));
			w.setFloatSide(Side.Left);
			w.resize(this.columnWidth(this.columnWidgets_.size()), new WLength(
					1));
		}
	}

	private WLength columnWidth(int column) {
		if (this.table_ != null) {
			return this.table_.columnWidth(column);
		} else {
			return WLength.Auto;
		}
	}
}
