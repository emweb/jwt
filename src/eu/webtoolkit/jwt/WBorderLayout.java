/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;

/**
 * A layout manager which divides the container region in five regions
 * <p>
 * 
 * The five regions are composed of:
 * 
 * <pre>
 * ------------------------------------
 *      |              North               |
 *      ------------------------------------
 *      |      |                    |      |
 *      | West |       Center       | East |
 *      |      |                    |      |
 *      ------------------------------------
 *      |              South               |
 *      ------------------------------------
 * </pre>
 * <p>
 * Each region may hold no more than one widget, and for all but the Center
 * region, the widget is optional.
 * <p>
 * Widgets in the North and South regions need to have The Center widget takes
 * all available remaining space.
 * <p>
 * When used with a {@link WContainerWidget}, the widget minimum sizes are used
 * for sizing the border widgets, whose default values may be overridden using
 * {@link WWidget#setMinimumSize(WLength width, WLength height)
 * WWidget#setMinimumSize()}. You may not define any padding for the container
 * widget. Instead, use
 * {@link WLayout#setContentsMargins(int left, int top, int right, int bottom)
 * WLayout#setContentsMargins()}.
 * <p>
 * <p>
 * <i><b>Note:</b>When used on a {@link WContainerWidget}, this layout manager
 * accepts the following hints (see
 * {@link WLayout#setLayoutHint(String name, String value)
 * WLayout#setLayoutHint()}):
 * <ul>
 * <li>
 * &quot;table-layout&quot; with possible values &quot;auto&quot; (default) or
 * &quot;fixed&quot;.<br>
 * Use &quot;fixed&quot; to prevent nested tables from overflowing the layout.
 * In that case, you will need to specify a width (in CSS or otherwise) for at
 * least one item in every column that has no stretch factor.</li>
 * </ul>
 * </i>
 * </p>
 * <p>
 * <i><b>Warning:</b>You should specify AlignTop in the alignment flags of
 * {@link WContainerWidget#setLayout(WLayout layout)
 * WContainerWidget#setLayout()} if the container does not have a height that is
 * constrained somehow. Otherwise the behavior is undefined (the parent
 * container will continue to increase in size) </i>
 * </p>
 */
public class WBorderLayout extends WLayout {
	/**
	 * Enumeration of possible positions in the layout.
	 */
	public enum Position {
		/**
		 * North (top).
		 */
		North,
		/**
		 * East (left).
		 */
		East,
		/**
		 * South (bottom).
		 */
		South,
		/**
		 * West (right).
		 */
		West,
		/**
		 * Center.
		 */
		Center;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Create a new border layout.
	 */
	public WBorderLayout(WWidget parent) {
		super();
		this.grid_ = new Grid();
		{
			int insertPos = 0;
			for (int ii = 0; ii < 3; ++ii)
				this.grid_.columns_.add(insertPos + ii, new Grid.Column(0));
		}
		;
		this.grid_.columns_.get(1).stretch_ = 1;
		{
			int insertPos = 0;
			for (int ii = 0; ii < 3; ++ii)
				this.grid_.rows_.add(insertPos + ii, new Grid.Row(0));
		}
		;
		this.grid_.rows_.get(1).stretch_ = 1;
		{
			int insertPos = 0;
			for (int ii = 0; ii < 3; ++ii)
				this.grid_.items_.add(insertPos + ii,
						new ArrayList<Grid.Item>());
		}
		;
		for (int i = 0; i < 3; ++i) {
			List<Grid.Item> items = this.grid_.items_.get(i);
			{
				int insertPos = 0;
				for (int ii = 0; ii < 3; ++ii)
					items.add(insertPos + ii, new Grid.Item());
			}
			;
		}
		this.grid_.items_.get(0).get(0).colSpan_ = 3;
		this.grid_.items_.get(2).get(0).colSpan_ = 3;
		if (parent != null) {
			this.setLayoutInParent(parent);
		}
	}

	/**
	 * Create a new border layout.
	 * <p>
	 * Calls {@link #WBorderLayout(WWidget parent) this((WWidget)null)}
	 */
	public WBorderLayout() {
		this((WWidget) null);
	}

	public void addItem(WLayoutItem item) {
		this.add(item, WBorderLayout.Position.Center);
	}

	public void removeItem(WLayoutItem item) {
		for (int i = 0; i < 5; ++i) {
			Grid.Item gridItem = this.itemAtPosition(WBorderLayout.Position
					.values()[i]);
			if (gridItem.item_ == item) {
				this.updateRemoveItem(item);
				gridItem.item_ = null;
				break;
			}
		}
	}

	public WLayoutItem getItemAt(int index) {
		int j = 0;
		for (int i = 0; i < 5; ++i) {
			WLayoutItem it = this.itemAtPosition(WBorderLayout.Position
					.values()[i]).item_;
			if (it != null) {
				if (j == index) {
					return it;
				} else {
					++j;
				}
			}
		}
		return null;
	}

	public int getCount() {
		int j = 0;
		for (int i = 0; i < 5; ++i) {
			if (this.itemAtPosition(WBorderLayout.Position.values()[i]).item_ != null) {
				++j;
			}
		}
		return j;
	}

	/**
	 * Add a widget to the given position.
	 * <p>
	 * Only one widget per position is supported.
	 * <p>
	 * 
	 * @see WBorderLayout#add(WLayoutItem item, WBorderLayout.Position position)
	 */
	public void addWidget(WWidget w, WBorderLayout.Position position) {
		this.add(new WWidgetItem(w), position);
	}

	/**
	 * Add a layout item to the given position.
	 * <p>
	 * Only one widget per position is supported.
	 */
	public void add(WLayoutItem item, WBorderLayout.Position position) {
		if (this.itemAtPosition(position).item_ != null) {
			throw new WtException(
					"WBorderLayout supports only one widget per position");
		}
		this.itemAtPosition(position).item_ = item;
		this.updateAddItem(item);
	}

	/**
	 * Return the widget at a position.
	 * <p>
	 * Returns <code>0</code> if no widget was set for that position.
	 */
	public WWidget widgetAt(WBorderLayout.Position position) {
		WWidgetItem item = ((this.getItemAt(position)) instanceof WWidgetItem ? (WWidgetItem) (this
				.getItemAt(position))
				: null);
		if (item != null) {
			return item.getWidget();
		} else {
			return null;
		}
	}

	/**
	 * Return the item at a position.
	 * <p>
	 * Returns <code>0</code> if no item was set for that position.
	 */
	public WLayoutItem getItemAt(WBorderLayout.Position position) {
		Grid.Item gridItem = this.itemAtPosition(position);
		return gridItem.item_;
	}

	/**
	 * Return the position at which the given layout item is set.
	 */
	public WBorderLayout.Position getPosition(WLayoutItem item) {
		for (int i = 0; i < 5; ++i) {
			if (this.itemAtPosition(WBorderLayout.Position.values()[i]).item_ == item) {
				return WBorderLayout.Position.values()[i];
			}
		}
		throw new WtException("WBorderLayout::position(): invalid item");
	}

	Grid getGrid() {
		return this.grid_;
	}

	private Grid grid_;

	private Grid.Item itemAtPosition(WBorderLayout.Position position) {
		switch (position) {
		case North:
			return this.grid_.items_.get(0).get(0);
		case East:
			return this.grid_.items_.get(1).get(0);
		case South:
			return this.grid_.items_.get(2).get(0);
		case West:
			return this.grid_.items_.get(1).get(2);
		case Center:
			return this.grid_.items_.get(1).get(1);
		default:
			throw new WtException(
					"WBorderLayout::itemAtPosition(): invalid position");
		}
	}
}
