package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;

public class WModelIndex implements Comparable<WModelIndex> {

	private int row;
	private int column;
	private WAbstractItemModel model;
	private Object internalPointer;

	public WModelIndex(int row, int column, WAbstractItemModel model, Object ptr) {
		this.row = row;
		this.column = column;
		this.model = model;
		this.internalPointer = ptr;
	}

	public WModelIndex getParent() {
		return model.getParent(this);
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	public Object getInternalPointer() {
		return internalPointer;
	}

	public WAbstractItemModel getModel() {
		return model;
	}

	public Object getData(int role) {
		return model.getData(this, role);
	}

	public Object getData() {
		return getData(ItemDataRole.DisplayRole);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof WModelIndex))
			return false;

		WModelIndex i1 = this;
		WModelIndex i2 = (WModelIndex) other;

		return i1.getModel() == i2.getModel() && i1.getRow() == i2.getRow()
				&& i1.getColumn() == i2.getColumn()
				&& i1.getInternalPointer() == i2.getInternalPointer();
	}

	@Override
	public int hashCode() {
		return model.hashCode() + (internalPointer == null ? 0 : internalPointer.hashCode()) + row * 1000 + column;
	}

	public int compareTo(WModelIndex i2) {
		WModelIndex i1 = this;

		if (i1.equals(i2))
			return 0;
		else {
			ArrayList<WModelIndex> ancestors1 = i1.getAncestors();
			ArrayList<WModelIndex> ancestors2 = i2.getAncestors();

			int e = Math.min(ancestors1.size(), ancestors2.size());

			for (int i = 0; i < e; ++i) {
				WModelIndex a1 = ancestors1.get(i);
				WModelIndex a2 = ancestors2.get(i);

				if (!a1.equals(a2)) {
					if (a1.getRow() < a2.getRow())
						return -1;
					else if (a1.getRow() > a2.getRow())
						return 1;
					else if (a1.getColumn() < a2.getColumn())
						return -1;
					else
						return 1;
				}
			}

			return ancestors1.size() - ancestors2.size();
		}
	}

	private ArrayList<WModelIndex> getAncestors() {
		ArrayList<WModelIndex> result;

		WModelIndex parent = getParent();
		if (parent != null)
			result = parent.getAncestors();
		else
			result = new ArrayList<WModelIndex>();

		result.add(this);

		return result;
	}

	public EnumSet<ItemFlag> getFlags() {
		return model.getFlags(this);
	}
}
