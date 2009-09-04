/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A layout manager which arranges widgets horizontally or vertically
 * <p>
 * 
 * This layout manager arranges widgets horizontally or vertically inside the
 * parent container. The space is divided so that each widgets is given its
 * minimum size, and remaining space is divided according to stretch factors
 * among the widgets. The widget minimum height or width is used for sizing each
 * widget, whose default values may be overridden using
 * {@link WWidget#setMinimumSize(WLength width, WLength height)
 * setMinimumSize()}.
 * <p>
 * You can use WContainerWidget::setOverflow(OverflowAuto) to automatically show
 * scrollbars on a container widget which you inserted into the layout. In some
 * cases, you will want to wrap another widget (like a {@link WTable}) into
 * {@link WContainerWidget} to have the scrollbars, since not all widgets allow
 * for scrollbars.
 * <p>
 * Each item is separated using a constant spacing, which defaults to 6 pixels,
 * and can be changed using {@link WBoxLayout#setSpacing(int size) setSpacing()}
 * . In addition, when this layout is a top-level layout (i.e. is not nested
 * inside another layout), a margin is set around the contents, which thus
 * replaces padding defined for the container. This margin defaults to 9 pixels,
 * and can be changed using
 * {@link WLayout#setContentsMargins(int left, int top, int right, int bottom)
 * setContentsMargins()}. It is not allowed to define padding for the container
 * widget using its CSS &apos;padding&apos; property or the
 * {@link WContainerWidget#setPadding(WLength length, EnumSet sides)
 * setPadding()}. You can add more space between two widgets using
 * {@link WBoxLayout#addSpacing(WLength size) addSpacing()}.
 * <p>
 * For each item a stretch factor may be defined, which controls how remaining
 * space is used. Each item is stretched using the stretch factor to fill the
 * remaining space.
 * <p>
 * <p>
 * <i><b>Note:</b>When JavaScript support is not available, only Safari and
 * Firefox properly implement this box layout. For other browsers, only the
 * horizontal layout is properly implemented, while vertically all widgets use
 * their minimum size.
 * <p>
 * When set on a {@link WContainerWidget}, this layout manager accepts the
 * following hints (see {@link WLayout#setLayoutHint(String name, String value)
 * setLayoutHint()}):
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
 * {@link WContainerWidget#setLayout(WLayout layout) setLayout()} if the
 * container does not have a height that is constrained somehow. Otherwise the
 * behavior is undefined (the parent container will continue to increase in
 * size) </i>
 * </p>
 */
public class WBoxLayout extends WLayout {
	/**
	 * Enumeration of the direction in which widgets are layed out.
	 */
	public enum Direction {
		/**
		 * Horizontal layout, widgets are arranged from left to right.
		 */
		LeftToRight,
		/**
		 * Horizontal layout, widgets are arranged from right to left.
		 */
		RightToLeft,
		/**
		 * Vertical layout, widgets are arranged from top to bottom.
		 */
		TopToBottom,
		/**
		 * Vertical layout, widgets are arranged from bottom to top.
		 */
		BottomToTop;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Create a new box layout.
	 * <p>
	 * This constructor is rarely used. Instead, use the convenient constructors
	 * of the specialized {@link WHBoxLayout} or {@link WVBoxLayout} classes.
	 * <p>
	 * Use <i>parent</i>=0 to created a layout manager that can be nested inside
	 * other layout managers.
	 */
	public WBoxLayout(WBoxLayout.Direction dir, WWidget parent) {
		super();
		this.direction_ = dir;
		this.grid_ = new Grid();
		if (parent != null) {
			this.setLayoutInParent(parent);
		}
	}

	/**
	 * Create a new box layout.
	 * <p>
	 * Calls {@link #WBoxLayout(WBoxLayout.Direction dir, WWidget parent)
	 * this(dir, (WWidget)null)}
	 */
	public WBoxLayout(WBoxLayout.Direction dir) {
		this(dir, (WWidget) null);
	}

	public void addItem(WLayoutItem item) {
		this.insertItem(this.getCount(), item, 0, EnumSet
				.noneOf(AlignmentFlag.class));
	}

	public void removeItem(WLayoutItem item) {
		int index = this.indexOf(item);
		if (index != -1) {
			this.updateRemoveItem(item);
			switch (this.direction_) {
			case RightToLeft:
				index = this.grid_.columns_.size() - 1 - index;
			case LeftToRight:
				this.grid_.columns_.remove(0 + index);
				this.grid_.items_.get(0).remove(0 + index);
				break;
			case BottomToTop:
				index = this.grid_.rows_.size() - 1 - index;
			case TopToBottom:
				this.grid_.rows_.remove(0 + index);
				this.grid_.items_.remove(0 + index);
			}
		}
	}

	public WLayoutItem getItemAt(int index) {
		switch (this.direction_) {
		case RightToLeft:
			index = this.grid_.columns_.size() - 1 - index;
		case LeftToRight:
			return this.grid_.items_.get(0).get(index).item_;
		case BottomToTop:
			index = this.grid_.rows_.size() - 1 - index;
		case TopToBottom:
			return this.grid_.items_.get(index).get(0).item_;
		}
		assert false;
		return null;
	}

	public int getCount() {
		return this.grid_.rows_.size() * this.grid_.columns_.size();
	}

	/**
	 * Set the layout direction.
	 * <p>
	 * 
	 * @see WBoxLayout#getDirection()
	 */
	public void setDirection(WBoxLayout.Direction direction) {
		if (this.direction_ != direction) {
			this.direction_ = direction;
		}
	}

	/**
	 * Returns the layout direction.
	 * <p>
	 * 
	 * @see WBoxLayout#setDirection(WBoxLayout.Direction direction)
	 */
	public WBoxLayout.Direction getDirection() {
		return this.direction_;
	}

	/**
	 * Set spacing between each item.
	 * <p>
	 * The default spacing is 6 pixels.
	 */
	public void setSpacing(int size) {
		this.grid_.horizontalSpacing_ = size;
		this.grid_.verticalSpacing_ = size;
	}

	/**
	 * Returns the spacing between each item.
	 * <p>
	 * 
	 * @see WBoxLayout#setSpacing(int size)
	 */
	public int getSpacing() {
		return this.grid_.horizontalSpacing_;
	}

	/**
	 * Adds a widget to the layout.
	 * <p>
	 * Adds a widget to the layout, with given <i>stretch</i> factor. The
	 * <i>alignemnt</i> parameter is a combination of a horizontal and/or a
	 * vertical AlignmentFlag OR&apos;ed together.
	 * <p>
	 * The <i>alignment</i> specifies the vertical and horizontal alignment of
	 * the item. The default value 0 indicates that the item is stretched to
	 * fill the entire column or row. The alignment can be specified as a
	 * logical combination of a horizontal alignment (
	 * {@link AlignmentFlag#AlignLeft}, {@link AlignmentFlag#AlignCenter}, or
	 * {@link AlignmentFlag#AlignRight}) and a vertical alignment (
	 * {@link AlignmentFlag#AlignTop}, {@link AlignmentFlag#AlignMiddle}, or
	 * {@link AlignmentFlag#AlignBottom}).
	 * <p>
	 * 
	 * @see WBoxLayout#addLayout(WLayout layout, int stretch, EnumSet alignment)
	 * @see WBoxLayout#insertWidget(int index, WWidget widget, int stretch,
	 *      EnumSet alignment)
	 */
	public void addWidget(WWidget widget, int stretch,
			EnumSet<AlignmentFlag> alignment) {
		this.insertWidget(this.getCount(), widget, stretch, alignment);
	}

	/**
	 * Adds a widget to the layout.
	 * <p>
	 * Calls {@link #addWidget(WWidget widget, int stretch, EnumSet alignment)
	 * addWidget(widget, stretch, EnumSet.of(alignmen, alignment))}
	 */
	public final void addWidget(WWidget widget, int stretch,
			AlignmentFlag alignmen, AlignmentFlag... alignment) {
		addWidget(widget, stretch, EnumSet.of(alignmen, alignment));
	}

	/**
	 * Adds a widget to the layout.
	 * <p>
	 * Calls {@link #addWidget(WWidget widget, int stretch, EnumSet alignment)
	 * addWidget(widget, 0, EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addWidget(WWidget widget) {
		addWidget(widget, 0, EnumSet.noneOf(AlignmentFlag.class));
	}

	/**
	 * Adds a widget to the layout.
	 * <p>
	 * Calls {@link #addWidget(WWidget widget, int stretch, EnumSet alignment)
	 * addWidget(widget, stretch, EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addWidget(WWidget widget, int stretch) {
		addWidget(widget, stretch, EnumSet.noneOf(AlignmentFlag.class));
	}

	/**
	 * Adds a nested layout to the layout.
	 * <p>
	 * Adds a nested layout, with given <i>stretch</i> factor.
	 * <p>
	 * 
	 * @see WBoxLayout#addWidget(WWidget widget, int stretch, EnumSet alignment)
	 * @see WBoxLayout#insertLayout(int index, WLayout layout, int stretch,
	 *      EnumSet alignment)
	 */
	public void addLayout(WLayout layout, int stretch,
			EnumSet<AlignmentFlag> alignment) {
		this.insertLayout(this.getCount(), layout, stretch, alignment);
	}

	/**
	 * Adds a nested layout to the layout.
	 * <p>
	 * Calls {@link #addLayout(WLayout layout, int stretch, EnumSet alignment)
	 * addLayout(layout, stretch, EnumSet.of(alignmen, alignment))}
	 */
	public final void addLayout(WLayout layout, int stretch,
			AlignmentFlag alignmen, AlignmentFlag... alignment) {
		addLayout(layout, stretch, EnumSet.of(alignmen, alignment));
	}

	/**
	 * Adds a nested layout to the layout.
	 * <p>
	 * Calls {@link #addLayout(WLayout layout, int stretch, EnumSet alignment)
	 * addLayout(layout, 0, EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addLayout(WLayout layout) {
		addLayout(layout, 0, EnumSet.noneOf(AlignmentFlag.class));
	}

	/**
	 * Adds a nested layout to the layout.
	 * <p>
	 * Calls {@link #addLayout(WLayout layout, int stretch, EnumSet alignment)
	 * addLayout(layout, stretch, EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addLayout(WLayout layout, int stretch) {
		addLayout(layout, stretch, EnumSet.noneOf(AlignmentFlag.class));
	}

	/**
	 * Adds extra spacing.
	 * <p>
	 * Adds extra spacing to the layout.
	 * <p>
	 * 
	 * @see WBoxLayout#addStretch(int stretch)
	 * @see WBoxLayout#insertStretch(int index, int stretch)
	 */
	public void addSpacing(WLength size) {
		this.insertSpacing(this.getCount(), size);
	}

	/**
	 * Adds a stretch element.
	 * <p>
	 * Adds a stretch element to the layout. This adds an empty space that
	 * stretches as needed.
	 * <p>
	 * 
	 * @see WBoxLayout#addSpacing(WLength size)
	 * @see WBoxLayout#insertStretch(int index, int stretch)
	 */
	public void addStretch(int stretch) {
		this.insertStretch(this.getCount(), stretch);
	}

	/**
	 * Adds a stretch element.
	 * <p>
	 * Calls {@link #addStretch(int stretch) addStretch(0)}
	 */
	public final void addStretch() {
		addStretch(0);
	}

	/**
	 * Inserts a widget in the layout.
	 * <p>
	 * Inserts a widget in the layout at position <i>index</i>, with given
	 * <i>stretch</i> factor.
	 * <p>
	 * The <i>alignment</i> specifies the vertical and horizontal alignment of
	 * the item. The default value 0 indicates that the item is stretched to
	 * fill the entire column or row. The alignment can be specified as a
	 * logical combination of a horizontal alignment (
	 * {@link AlignmentFlag#AlignLeft}, {@link AlignmentFlag#AlignCenter}, or
	 * {@link AlignmentFlag#AlignRight}) and a vertical alignment (
	 * {@link AlignmentFlag#AlignTop}, {@link AlignmentFlag#AlignMiddle}, or
	 * {@link AlignmentFlag#AlignBottom}).
	 * <p>
	 * 
	 * @see WBoxLayout#insertLayout(int index, WLayout layout, int stretch,
	 *      EnumSet alignment)
	 * @see WBoxLayout#addWidget(WWidget widget, int stretch, EnumSet alignment)
	 */
	public void insertWidget(int index, WWidget widget, int stretch,
			EnumSet<AlignmentFlag> alignment) {
		this.insertItem(index, new WWidgetItem(widget), stretch, alignment);
	}

	/**
	 * Inserts a widget in the layout.
	 * <p>
	 * Calls
	 * {@link #insertWidget(int index, WWidget widget, int stretch, EnumSet alignment)
	 * insertWidget(index, widget, stretch, EnumSet.of(alignmen, alignment))}
	 */
	public final void insertWidget(int index, WWidget widget, int stretch,
			AlignmentFlag alignmen, AlignmentFlag... alignment) {
		insertWidget(index, widget, stretch, EnumSet.of(alignmen, alignment));
	}

	/**
	 * Inserts a widget in the layout.
	 * <p>
	 * Calls
	 * {@link #insertWidget(int index, WWidget widget, int stretch, EnumSet alignment)
	 * insertWidget(index, widget, 0, EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void insertWidget(int index, WWidget widget) {
		insertWidget(index, widget, 0, EnumSet.noneOf(AlignmentFlag.class));
	}

	/**
	 * Inserts a widget in the layout.
	 * <p>
	 * Calls
	 * {@link #insertWidget(int index, WWidget widget, int stretch, EnumSet alignment)
	 * insertWidget(index, widget, stretch,
	 * EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void insertWidget(int index, WWidget widget, int stretch) {
		insertWidget(index, widget, stretch, EnumSet
				.noneOf(AlignmentFlag.class));
	}

	/**
	 * Inserts a nested layout in the layout.
	 * <p>
	 * Inserts a nested layout in the layout at position<i>index</i>, with given
	 * <i>stretch</i> factor.
	 * <p>
	 * 
	 * @see WBoxLayout#insertWidget(int index, WWidget widget, int stretch,
	 *      EnumSet alignment)
	 * @see WBoxLayout#addLayout(WLayout layout, int stretch, EnumSet alignment)
	 */
	public void insertLayout(int index, WLayout layout, int stretch,
			EnumSet<AlignmentFlag> alignment) {
		this.insertItem(index, layout, stretch, alignment);
	}

	/**
	 * Inserts a nested layout in the layout.
	 * <p>
	 * Calls
	 * {@link #insertLayout(int index, WLayout layout, int stretch, EnumSet alignment)
	 * insertLayout(index, layout, stretch, EnumSet.of(alignmen, alignment))}
	 */
	public final void insertLayout(int index, WLayout layout, int stretch,
			AlignmentFlag alignmen, AlignmentFlag... alignment) {
		insertLayout(index, layout, stretch, EnumSet.of(alignmen, alignment));
	}

	/**
	 * Inserts a nested layout in the layout.
	 * <p>
	 * Calls
	 * {@link #insertLayout(int index, WLayout layout, int stretch, EnumSet alignment)
	 * insertLayout(index, layout, 0, EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void insertLayout(int index, WLayout layout) {
		insertLayout(index, layout, 0, EnumSet.noneOf(AlignmentFlag.class));
	}

	/**
	 * Inserts a nested layout in the layout.
	 * <p>
	 * Calls
	 * {@link #insertLayout(int index, WLayout layout, int stretch, EnumSet alignment)
	 * insertLayout(index, layout, stretch,
	 * EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void insertLayout(int index, WLayout layout, int stretch) {
		insertLayout(index, layout, stretch, EnumSet
				.noneOf(AlignmentFlag.class));
	}

	/**
	 * Inserts extra spacing in the layout.
	 * <p>
	 * Inserts extra spacing in the layout at position <i>index</i>.
	 * <p>
	 * 
	 * @see WBoxLayout#insertStretch(int index, int stretch)
	 * @see WBoxLayout#addSpacing(WLength size)
	 */
	public void insertSpacing(int index, WLength size) {
		WWidget spacer = this.createSpacer(size);
		this.insertItem(index, new WWidgetItem(spacer), 0, EnumSet
				.noneOf(AlignmentFlag.class));
	}

	/**
	 * Inserts a stretch element in the layout.
	 * <p>
	 * Inserts a stretch element in the layout at position <i>index</i>. This
	 * adds an empty space that stretches as needed.
	 * <p>
	 * 
	 * @see WBoxLayout#insertSpacing(int index, WLength size)
	 * @see WBoxLayout#addStretch(int stretch)
	 */
	public void insertStretch(int index, int stretch) {
		WWidget spacer = this.createSpacer(new WLength(0));
		this.insertItem(index, new WWidgetItem(spacer), stretch, EnumSet
				.noneOf(AlignmentFlag.class));
	}

	/**
	 * Inserts a stretch element in the layout.
	 * <p>
	 * Calls {@link #insertStretch(int index, int stretch) insertStretch(index,
	 * 0)}
	 */
	public final void insertStretch(int index) {
		insertStretch(index, 0);
	}

	/**
	 * Set the stretch factor for a nested layout.
	 * <p>
	 * The <i>layout</i> must have previously been added to this layout using
	 * {@link WBoxLayout#insertLayout(int index, WLayout layout, int stretch, EnumSet alignment)
	 * insertLayout()} or
	 * {@link WBoxLayout#addLayout(WLayout layout, int stretch, EnumSet alignment)
	 * addLayout()}.
	 * <p>
	 * Returns whether the <i>stretch</i> could be set.
	 */
	public boolean setStretchFactor(WLayout layout, int stretch) {
		for (int i = 0; i < this.getCount(); ++i) {
			WLayoutItem item = this.getItemAt(i);
			if (item != null && item.getLayout() == layout) {
				this.setStretchFactor(i, stretch);
				return true;
			}
		}
		return false;
	}

	/**
	 * Set the stretch factor for a nested layout.
	 * <p>
	 * The <i>widget</i> must have previously been added to this layout using
	 * {@link WBoxLayout#insertWidget(int index, WWidget widget, int stretch, EnumSet alignment)
	 * insertWidget()} or
	 * {@link WBoxLayout#addWidget(WWidget widget, int stretch, EnumSet alignment)
	 * addWidget()}.
	 * <p>
	 * Returns whether the <i>stretch</i> could be set.
	 */
	public boolean setStretchFactor(WWidget widget, int stretch) {
		for (int i = 0; i < this.getCount(); ++i) {
			WLayoutItem item = this.getItemAt(i);
			if (item != null && item.getWidget() == widget) {
				this.setStretchFactor(i, stretch);
				return true;
			}
		}
		return false;
	}

	public Grid getGrid() {
		return this.grid_;
	}

	protected void insertItem(int index, WLayoutItem item, int stretch,
			EnumSet<AlignmentFlag> alignment) {
		switch (this.direction_) {
		case RightToLeft:
			index = this.grid_.columns_.size() - index;
		case LeftToRight:
			this.grid_.columns_.add(0 + index, new Grid.Column(stretch));
			if (this.grid_.items_.isEmpty()) {
				this.grid_.items_.add(new ArrayList<Grid.Item>());
				this.grid_.rows_.add(new Grid.Row());
			}
			this.grid_.items_.get(0).add(0 + index,
					new Grid.Item(item, alignment));
			break;
		case BottomToTop:
			index = this.grid_.rows_.size() - index;
		case TopToBottom:
			if (this.grid_.columns_.isEmpty()) {
				this.grid_.columns_.add(new Grid.Column());
			}
			this.grid_.rows_.add(0 + index, new Grid.Row(stretch));
			this.grid_.items_.add(0 + index, new ArrayList<Grid.Item>());
			this.grid_.items_.get(index).add(new Grid.Item(item, alignment));
			break;
		}
		this.updateAddItem(item);
	}

	protected final void insertItem(int index, WLayoutItem item, int stretch,
			AlignmentFlag alignmen, AlignmentFlag... alignment) {
		insertItem(index, item, stretch, EnumSet.of(alignmen, alignment));
	}

	private WBoxLayout.Direction direction_;
	private Grid grid_;

	private void setStretchFactor(int i, int stretch) {
		switch (this.direction_) {
		case RightToLeft:
			i = this.grid_.columns_.size() - 1 - i;
		case LeftToRight:
			this.grid_.columns_.get(i).stretch_ = stretch;
			break;
		case BottomToTop:
			i = this.grid_.rows_.size() - 1 - i;
		case TopToBottom:
			this.grid_.rows_.get(i).stretch_ = stretch;
		}
	}

	private WWidget createSpacer(WLength size) {
		Spacer spacer = new Spacer();
		if (this.direction_ == WBoxLayout.Direction.LeftToRight
				|| this.direction_ == WBoxLayout.Direction.RightToLeft) {
			spacer.setMinimumSize(size, WLength.Auto);
		} else {
			spacer.setMinimumSize(WLength.Auto, size);
		}
		return spacer;
	}
}
