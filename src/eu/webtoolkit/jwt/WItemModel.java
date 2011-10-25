package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;

/**
 * An item model for use with Wt&apos;s view classes.
 * <p>
 * 
 * An item model specializes {@link WAbstractItemModel} for
 * hierarchical data (i.e. a model with a tree-like data structure and one or more columns).
 * <p>
 * It cannot be used directly but must be subclassed. Subclassed models must at
 * least implement {@link WItemModel#getChildItems(Object, int, int) getChildItems() } 
 * to return a list of items for a parent,
 * {@link WAbstractItemModel#getData(WModelIndex index, int role)
 * getData()} to return data and 
 * {@link WAbstractItemModel#getColumnCount(WModelIndex parent)
 * getColumnCount()}
 * to return the number of columns.
 * 
 * When providing a value for fetchSize, 
 * the model will attempt to fetch a node's children in blocks minimally the size of the provided value, 
 * if not provided or the value is -1, all the node's children are fetched at once.
 */
public abstract class WItemModel<Item extends Object> extends WAbstractItemModel {
	private class Node {
		private List<Item> childItems;
		private WModelIndex index;
		private List<Node> childNodes;
		
		private int childCount;

		public Node(WModelIndex index) {
			this.childItems = new ArrayList<Item>();
			this.index = index;
			this.childNodes = new ArrayList<Node>();
		}

		public Item getItem(WModelIndex index) {
			int row = index.getRow();
			if (row >= childItems.size()) {
				Item parent = null;
				if (index.getParent() != null)
					parent = getParentItem(index);
				
				List<Item> children;
				if (fetchSize > 0) {
					int from = childItems.size();
					int count = ((row - from) / fetchSize + 1) * fetchSize;
					count = Math.min(count, childCount - from);
					
					children = getChildItems((Item)parent, from, count);
				} else {
					children = getChildItems((Item)parent, 0, -1);
				}
				childItems.addAll(children);
			}
			
			return childItems.get(index.getRow());
		}
		
		public WModelIndex getIndex(int row, int column) {
			if (childNodes.get(row) != null)
				return childNodes.get(row).index;
			else
				return createIndex(row, column, this);
		}
		
		public void setChildCount(int childCount) {
			this.childCount = childCount;
			for (int i = 0; i < childCount; i++)
				this.childNodes.add(null);
		}
	};

	private Node rootNode = null;
	private int fetchSize;
	
	public WItemModel() {
		this(-1);
	}
	
	public WItemModel(int fetchSize) {
		this.fetchSize = fetchSize;
		this.rootNode = null;
	}

	@Override
	public int getRowCount(WModelIndex index) {
		Item item = null;
		if (index != null) 
			item = getItem(index);

		return getChildCount(item);
	}

	/**
	 * Returns the number of children for <code>item</code>.
	 * 
	 * <p>
	 * This returns the number of children for <code>item</code>.
	 * When the <code>item</code> is <code>null</code>,
	 * this method should return the number of top level items.
	 * 
	 * The default implementation returns the size of the list returned,
	 * by invoking <code>getChildItems(item, 0, -1)</code>.
	 * When providing a fetchSize to the constructor,
	 * you need to override this method with a proper implementation.
	 * <p>
	 */
	public int getChildCount(Item item) {
		return getChildItems(item, 0, -1).size();
	}

	@Override
	public WModelIndex getParent(WModelIndex index) {
		@SuppressWarnings("unchecked")
		Node parentNode = (Node)index.getInternalPointer();
		return parentNode.index;
	}

	/**
	 * Returns the Item for the {@link WModelIndex} <code>index</code>.
	 * 
	 * <p>
	 * This returns the Item for the {@link WModelIndex} <code>index</code>.
	 * </p>
	 * */
	@SuppressWarnings("unchecked")
	public Item getItem(WModelIndex index) {
		if (index == null) {
			return null;
		} else {
			Node parentNode = (Node) index.getInternalPointer();
			return (Item) parentNode.getItem(index);
		}
	}
	
	@Override
	public WModelIndex getIndex(int row, int column, WModelIndex parent) {
		Node parentNode = null;
		
		if (parent == null) {
			if (rootNode == null) { 
				rootNode = new Node(null);
				
				int childCount = getChildCount(null);
				rootNode.setChildCount(childCount);
			}
			parentNode = rootNode;
		} else {
			@SuppressWarnings("unchecked")
			Node parentParentNode = (Node)parent.getInternalPointer();

			if (parent.getRow() < parentParentNode.childNodes.size()) 
				parentNode = parentParentNode.childNodes.get(parent.getRow());
			
			if (parentNode == null) {
				parentNode = new Node(null);
				parentNode.index = parent;
				
				int childCount = getChildCount(parentParentNode.childItems.get(parent.getRow()));
				parentNode.setChildCount(childCount);
				
				parentParentNode.childNodes.set(parent.getRow(), parentNode);
			}
		}
		
		return parentNode.getIndex(row, column);
	}

	/**
	 * Returns the list of child items for a parent item.
	 * 
	 * <p>
	 * This returns the list of child items for a <code>parent</code> item.
	 * When the <code>parent</code> item is <code>null</code>,
	 * this method should return all top level items.
	 * When you provided a fetchSize, 
	 * this method should only return a slice specified 
	 * by <code>from</code> and <code>count</code> of the total list of items,
	 * if not <code>count</code> will be -1 and all items should be returned at once.
	 * 
	 * When the item has no children, you should return an empty List<Item>.
	 * <p>
	 */
	public abstract List<Item> getChildItems(Item parent, int from, int count);
	
	private Item getParentItem(WModelIndex index) {
		@SuppressWarnings("unchecked")
		Node parentParentNode = (Node)index.getParent().getInternalPointer();
		return parentParentNode.childItems.get(index.getParent().getRow());
	}
}
