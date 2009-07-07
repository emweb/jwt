/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.Writer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * A widget that holds and manages child widgets
 * <p>
 * 
 * A WContainerWidget acts as a container for child widgets. Child widgets may
 * be added directly to the container or using a layout manager.
 * <p>
 * Use {@link WContainerWidget#addWidget(WWidget widget)} or pass the container
 * as constructor argument to a widget to directly add children to the
 * container, without using a layout manager. In that case, CSS-based layout is
 * used, which is governed by properties of the children, and properties of the
 * container. By default, a WContainerWidget is
 * {@link WWidget#setInline(boolean inlined) stacked} and manages its children
 * within a rectangle. Inline child widgets are layed out in lines, wrapping
 * around as needed, while stacked widgets are stacked vertically. The container
 * may add padding at the container edges using
 * {@link WContainerWidget#setPadding(WLength length, EnumSet sides)}, and
 * provide alignment of contents using
 * {@link WContainerWidget#setContentAlignment(EnumSet alignment)}. A container
 * is rendered by default using a HTML <code>div</code> tag, but this may be
 * changed to an HTML <code>ul</code> or <code>ol</code> tag to make use of
 * other CSS layout techniques, using
 * {@link WContainerWidget#setList(boolean list, boolean ordered)}. In addition,
 * specializations of this class as implemented by {@link WAnchor},
 * {@link WGroupBox}, {@link WStackedWidget} and {@link WTableCell} provide
 * other alternative rendering of the container.
 * <p>
 * When setting the WContainerWidget {@link WWidget#setInline(boolean inlined)
 * inline} the container only acts as a conceptual container, offering a common
 * style to its children. Inline children are still layed out inline within the
 * flow of the parent container of this container, as if they were inserted
 * directly into that parent container.
 * <p>
 * To use a layout manager instead of CSS-based layout, use
 * {@link WContainerWidget#setLayout(WLayout layout)} or pass the container as
 * constructor argument to a layout manager. In that case you should not define
 * any padding for the container, and widgets and nested layout managers must be
 * added to the layout manager, instead of to the container directly.
 * <p>
 * Usage example:
 * <p>
 * <code>
 // Example 1: <br> 
 // Instantiate a container widget and add some children whose layout  <br> 
 // is governed based on HTML/CSS rules. <br> 
 WContainerWidget container1 = new WContainerWidget(); <br> 
 container1.addWidget(new WText(&quot;Some text&quot;)); <br> 
 container1.addWidget(new WImage(&quot;images/img.png&quot;)); <br> 
 WContainerWidget child3 = new WContainerWidget(container1); <br> 
		  <br> 
 // Example 2: <br> 
 // Instantiate a container widget which uses a layout manager <br> 
 WContainerWidget container2 = new WContainerWidget(); <br> 
 // give the container a fixed height <br> 
 container2.resize(WLength.Auto, new WLength(600));  <br> 
 <br> 
 WVBoxLayout layout = new WVBoxLayout(); <br> 
 layout.addWidget(new WText(&quot;Some text&quot;)); <br> 
 layout.addWidget(new WImage(&quot;images/img.png&quot;)); <br> 
		  <br> 
 container2.setLayout(layout);      // set the layout to the container.
</code>
 * <p>
 * Depending on its configuration and usage, the widget corresponds to the
 * following HTML tags:
 * <ul>
 * <li>By default, the widget corresponds to a <code>&lt;div&gt;</code> tag.</li>
 * <li>When configured with setInline(true), the widget corresponds to a
 * <code>&lt;span&gt;</code>.</li>
 * <li>When configured with setList(true), the widget corresponds to a
 * <code>&lt;ul&gt;</code>.</li>
 * <li>When configured with setList(true, true), the widget corresponds to a
 * <code>&lt;ol&gt;</code>.</li>
 * <li>When inserted into a container widget that
 * {@link WContainerWidget#isList()}, the widget corresponds to a
 * <code>&lt;li&gt;</code>.</li>
 * </ul>
 */
public class WContainerWidget extends WInteractWidget {
	/**
	 * How to handle overflow of inner content.
	 */
	public enum Overflow {
		/**
		 * Show content that overflows.
		 */
		OverflowVisible,
		/**
		 * Show scrollbars when needed.
		 */
		OverflowAuto,
		/**
		 * Hide content that overflows.
		 */
		OverflowHidden,
		/**
		 * Always show scroll bars.
		 */
		OverflowScroll;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Create a container with optional parent.
	 */
	public WContainerWidget(WContainerWidget parent) {
		super(parent);
		this.flags_ = new BitSet();
		this.contentAlignment_ = EnumSet.of(AlignmentFlag.AlignLeft);
		this.overflow_ = null;
		this.padding_ = null;
		this.layout_ = null;
		this.setInline(false);
		this.setLoadLaterWhenInvisible(false);
		this.children_ = new ArrayList<WWidget>();
	}

	/**
	 * Create a container with optional parent.
	 * <p>
	 * Calls {@link #WContainerWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WContainerWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Destruct a WContainerWidget.
	 */
	public void remove() {
		/* delete this.layout_ */;
		/* delete this.padding_ */;
		/* delete this.overflow_ */;
		super.remove();
	}

	/**
	 * Set a layout manager for the container.
	 * <p>
	 * Only a single layout manager may be set. Note that you can nest layout
	 * managers inside each other, to create a complex layout hierarchy.
	 * <p>
	 * The layout manager arranges children in the entire width and height of
	 * the container. This is equivalent to
	 * {@link WContainerWidget#setLayout(WLayout layout, EnumSet alignment)
	 * setLayout(<i>layout</i>, AlignJustify)}
	 * <p>
	 * 
	 * @see WContainerWidget#getLayout()
	 * @see WContainerWidget#setLayout(WLayout layout, EnumSet alignment)
	 */
	public void setLayout(WLayout layout) {
		this.setLayout(layout, EnumSet.of(AlignmentFlag.AlignJustify));
	}

	/**
	 * Set a layout manager for the container.
	 * <p>
	 * The <i>alignment</i> argument determines how the layout is aligned inside
	 * the container. By default, the layout manager arranges children over the
	 * entire width and height of the container, corresponding to a value of
	 * AlignJustify. This requires that the container has a specified height
	 * (either because it is managed by another layout manager, is the root
	 * container widget, or has a height set).
	 * <p>
	 * In general, <i>alignment</i> is the logical OR of a horizontal and a
	 * vertical flag:
	 * <ul>
	 * <li>The horizontal alignment option may be one of
	 * {@link AlignmentFlag#AlignLeft}, {@link AlignmentFlag#AlignCenter},
	 * {@link AlignmentFlag#AlignRight}, or {@link AlignmentFlag#AlignJustify}.</li>
	 * <li>The vertical alignment option may be &apos;0&apos; (corresponding to
	 * vertical justification to the full height), or
	 * {@link AlignmentFlag#AlignTop}.</li>
	 * </ul>
	 * <p>
	 * When using a horizontal alingment different from
	 * {@link AlignmentFlag#AlignJustify}, and a vertical alignment different
	 * from &apos;0&apos;, the widget is sized in that direction to fit the
	 * contents, instead of the contents being adjusted to the widget size. This
	 * is useful when the container does not have a specific size in that
	 * direction and when the layout manager does not contain any widgets that
	 * wish to consume all remaining space in that direction.
	 * <p>
	 * Only a single layout manager may be set. If you want to replace the
	 * current layout manager, you have to erase all contents first using
	 * {@link WContainerWidget#clear()}, which also deletes the layout manager.
	 * <p>
	 * Note that you can nest layout managers inside each other, to create a
	 * complex layout hierarchy.
	 * <p>
	 * The widget will take ownership of <i>layout</i>.
	 * <p>
	 * 
	 * @see WContainerWidget#getLayout()
	 */
	public void setLayout(WLayout layout, EnumSet<AlignmentFlag> alignment) {
		if (this.layout_ != null && layout != this.layout_) {
			WApplication.getInstance().log("error").append(
					"WContainerWidget::setLayout: already have a layout.");
			return;
		}
		this.contentAlignment_ = EnumSet.copyOf(alignment);
		if (!(this.layout_ != null)) {
			this.layout_ = layout;
			this.flags_.set(BIT_LAYOUT_CHANGED);
			super.setLayout(layout);
			this.getLayoutImpl().setContainer(this);
		}
	}

	/**
	 * Set a layout manager for the container.
	 * <p>
	 * Calls {@link #setLayout(WLayout layout, EnumSet alignment)
	 * setLayout(layout, EnumSet.of(alignmen, alignment))}
	 */
	public final void setLayout(WLayout layout, AlignmentFlag alignmen,
			AlignmentFlag... alignment) {
		setLayout(layout, EnumSet.of(alignmen, alignment));
	}

	/**
	 * Get the layout manager that was set for the container.
	 * <p>
	 * If no layout manager was previously set using setLayout({@link WLayout}
	 * ), 0 is returned.
	 * <p>
	 * 
	 * @see WContainerWidget#setLayout(WLayout layout)
	 */
	public WLayout getLayout() {
		return this.layout_;
	}

	/**
	 * Add a child widget to this container.
	 * <p>
	 * This is equivalent to passing this container as the parent when
	 * constructing the child. The widget is appended to the list of children,
	 * and thus also layed-out at the end.
	 */
	public void addWidget(WWidget widget) {
		if (widget.getParent() != null) {
			if (widget.getParent() != this) {
				WApplication.getInstance().log("warn").append(
						"WContainerWidget::addWidget(): reparenting widget");
				widget.setParent((WWidget) null);
			} else {
				return;
			}
		}
		if (!(this.transientImpl_ != null)) {
			this.transientImpl_ = new WWebWidget.TransientImpl();
			if (this.getDomElementType() != DomElementType.DomElement_TD
					|| !WApplication.getInstance().getEnvironment().agentIsIE()) {
				this.setLoadLaterWhenInvisible(true);
			}
		}
		this.transientImpl_.addedChildren_.add(widget);
		this.flags_.set(BIT_ADJUST_CHILDREN_ALIGN);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		widget.setParent(this);
	}

	/**
	 * insert a child widget in this container, before another widget.
	 * <p>
	 * The <i>widget</i> is inserted at the place of the <i>before</i> widget,
	 * and subsequent widgets are shifted.
	 * <p>
	 * 
	 * @see WContainerWidget#insertWidget(int index, WWidget widget)
	 */
	public void insertBefore(WWidget widget, WWidget before) {
		if (before.getParent() != this) {
			WApplication
					.getInstance()
					.log("error")
					.append(
							"WContainerWidget::insertBefore(): 'before' not in this container");
			return;
		}
		if (widget.getParent() != null) {
			if (widget.getParent() != this) {
				WApplication.getInstance().log("warn").append(
						"WContainerWidget::insertWidget(): reparenting widget");
				widget.setParent((WWidget) null);
			} else {
				return;
			}
		}
		int i = this.children_.indexOf(before);
		if (i == -1) {
			i = this.children_.size();
		}
		this.children_.add(0 + i, widget);
		this.flags_.set(BIT_ADJUST_CHILDREN_ALIGN);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		if (!(this.transientImpl_ != null)) {
			this.transientImpl_ = new WWebWidget.TransientImpl();
		}
		this.transientImpl_.addedChildren_.add(widget);
		widget.setParent((WObject) this);
		if (this.isLoaded()) {
			this.doLoad(widget);
		}
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(this, false);
	}

	/**
	 * insert a child widget in this container at given index.
	 * <p>
	 * The <i>widget</i> is inserted at the given <i>index</i>, and subsequent
	 * widgets are shifted.
	 * <p>
	 * 
	 * @see WContainerWidget#insertBefore(WWidget widget, WWidget before)
	 */
	public void insertWidget(int index, WWidget widget) {
		if (index == (int) this.children_.size()) {
			this.addWidget(widget);
		} else {
			this.insertBefore(widget, this.getChildren().get(index));
		}
	}

	/**
	 * Remove a child widget from this container.
	 * <p>
	 * This removes the widget from this container, but does not delete the
	 * widget !
	 */
	public void removeWidget(WWidget widget) {
		widget.setParent((WWidget) null);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Remove and delete all child widgets.
	 * <p>
	 * This deletes all children that have been added to this container.
	 * <p>
	 * If a layout was set, also the layout manager is deleted.
	 */
	public void clear() {
		while (!this.getChildren().isEmpty()) {
			WWidget w = this.getChildren().get(this.getChildren().size() - 1);
			this.removeWidget(w);
			if (w != null)
				w.remove();
		}
		/* delete this.layout_ */;
	}

	/**
	 * Return the index of a widget.
	 */
	public int getIndexOf(WWidget widget) {
		return this.getChildren().indexOf(widget);
	}

	/**
	 * Return the widget at <i>index</i>.
	 */
	public WWidget getWidget(int index) {
		return this.getChildren().get(index);
	}

	/**
	 * Get the number of widgets in this container.
	 */
	public int getCount() {
		return this.getChildren().size();
	}

	/**
	 * Specify how child widgets must be aligned within the container.
	 * <p>
	 * For a {@link WContainerWidget}, only specifes the horizontal alignment of
	 * child widgets. Note that there is no way to specify vertical alignment:
	 * children are always pushed to the top of the container.
	 * <p>
	 * For a {@link WTableCell}, this may also specify the vertical alignment.
	 * The default alignment is ({@link AlignmentFlag#AlignTop AlignTop} |
	 * {@link AlignmentFlag#AlignLeft AlignLeft}).
	 */
	public void setContentAlignment(EnumSet<AlignmentFlag> alignment) {
		this.contentAlignment_ = EnumSet.copyOf(alignment);
		AlignmentFlag vAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				this.contentAlignment_, AlignmentFlag.AlignVerticalMask));
		if (vAlign == null) {
			this.contentAlignment_.add(AlignmentFlag.AlignTop);
		}
		this.flags_.set(BIT_CONTENT_ALIGNMENT_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Specify how child widgets must be aligned within the container.
	 * <p>
	 * Calls {@link #setContentAlignment(EnumSet alignment)
	 * setContentAlignment(EnumSet.of(alignmen, alignment))}
	 */
	public final void setContentAlignment(AlignmentFlag alignmen,
			AlignmentFlag... alignment) {
		setContentAlignment(EnumSet.of(alignmen, alignment));
	}

	/**
	 * Set padding inside the widget.
	 * <p>
	 * Setting padding has the effect of adding distance between the widget
	 * children and the border.
	 */
	public void setPadding(WLength length, EnumSet<Side> sides) {
		if (!(this.padding_ != null)) {
			this.padding_ = new WLength[4];
			this.padding_[0] = this.padding_[1] = this.padding_[2] = this.padding_[3] = WLength.Auto;
		}
		if (sides.contains(Side.Top)) {
			this.padding_[0] = length;
		}
		if (sides.contains(Side.Right)) {
			this.padding_[1] = length;
		}
		if (sides.contains(Side.Bottom)) {
			this.padding_[2] = length;
		}
		if (sides.contains(Side.Left)) {
			this.padding_[3] = length;
		}
		this.flags_.set(BIT_PADDINGS_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Set padding inside the widget.
	 * <p>
	 * Calls {@link #setPadding(WLength length, EnumSet sides)
	 * setPadding(length, EnumSet.of(side, sides))}
	 */
	public final void setPadding(WLength length, Side side, Side... sides) {
		setPadding(length, EnumSet.of(side, sides));
	}

	/**
	 * Set padding inside the widget.
	 * <p>
	 * Calls {@link #setPadding(WLength length, EnumSet sides)
	 * setPadding(length, Side.All)}
	 */
	public final void setPadding(WLength length) {
		setPadding(length, Side.All);
	}

	/**
	 * Get the padding set for the widget.
	 * <p>
	 * 
	 * @see WContainerWidget#setPadding(WLength length, EnumSet sides)
	 */
	public WLength getPadding(Side side) {
		if (!(this.padding_ != null)) {
			return WLength.Auto;
		}
		switch (side) {
		case Top:
			return this.padding_[0];
		case Right:
			return this.padding_[1];
		case Bottom:
			return this.padding_[2];
		case Left:
			return this.padding_[3];
		default:
			throw new WtException(
					"WContainerWidget::padding(Side) with invalid side.");
		}
	}

	/**
	 * Return the alignment of children.
	 * <p>
	 * 
	 * @see WContainerWidget#setContentAlignment(EnumSet alignment)
	 */
	public EnumSet<AlignmentFlag> getContentAlignment() {
		return this.contentAlignment_;
	}

	/**
	 * Set how overflow of contained children must be handled.
	 * <p>
	 * This is an alternative (CSS-ish) way to provide scroll bars on a
	 * container widget, compared to wrapping inside a {@link WScrollArea}.
	 * <p>
	 * Note that currently, you cannot separately specify vertical and
	 * horizontal scroll behaviour, since this is not supported on Opera.
	 * Therefore, settings will apply automatically to both orientations.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>For Internet Explorer, setting overflow to OverflowAuto or
	 * OverflowScroll may cause problems with content that uses absolutely
	 * positioned DOM elements (such as {@link WPaintedWidget} or several Ext
	 * widgets): due to a bug in IE, these elements will not scroll along, but
	 * stay &apos;fixed&apos; at their initial position. This problem can
	 * (usually) be circumvented by using a relative position scheme relative
	 * (using {@link WWebWidget#setPositionScheme(PositionScheme scheme)
	 * setPositionScheme(Relative)}) for the same container widget which
	 * provides the scroll bars.</i>
	 * </p>
	 * 
	 * @see WScrollArea
	 */
	public void setOverflow(WContainerWidget.Overflow value,
			EnumSet<Orientation> orientation) {
		if (!(this.overflow_ != null)) {
			this.overflow_ = new WContainerWidget.Overflow[2];
			this.overflow_[0] = this.overflow_[1] = WContainerWidget.Overflow.OverflowVisible;
		}
		if (!EnumUtils.mask(orientation, Orientation.Horizontal).isEmpty()) {
			this.overflow_[0] = value;
		}
		if (!EnumUtils.mask(orientation, Orientation.Vertical).isEmpty()) {
			this.overflow_[1] = value;
		}
		this.flags_.set(BIT_OVERFLOW_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Set how overflow of contained children must be handled.
	 * <p>
	 * Calls
	 * {@link #setOverflow(WContainerWidget.Overflow value, EnumSet orientation)
	 * setOverflow(value, EnumSet.of(orientatio, orientation))}
	 */
	public final void setOverflow(WContainerWidget.Overflow value,
			Orientation orientatio, Orientation... orientation) {
		setOverflow(value, EnumSet.of(orientatio, orientation));
	}

	/**
	 * Set how overflow of contained children must be handled.
	 * <p>
	 * Calls
	 * {@link #setOverflow(WContainerWidget.Overflow value, EnumSet orientation)
	 * setOverflow(value, EnumSet.of (Orientation.Horizontal,
	 * Orientation.Vertical))}
	 */
	public final void setOverflow(WContainerWidget.Overflow value) {
		setOverflow(value, EnumSet.of(Orientation.Horizontal,
				Orientation.Vertical));
	}

	/**
	 * Render the container as an HTML list.
	 * <p>
	 * Setting <i>renderList</i> to true will cause the container to be using an
	 * HTML <code>&lt;ul&gt;</code> or <code>&lt;ol&gt;</code> type, depending
	 * on the value of <i>orderedList</i>. This must be set before the initial
	 * render of the container. When set, any contained {@link WContainerWidget}
	 * will be rendered as an HTML <code>&lt;li&gt;</code>. Adding
	 * non-WContainerWidget children results in unspecified behaviour.
	 * <p>
	 * Note that CSS default layout rules for <code>&lt;ul&gt;</code> and
	 * <code>&lt;ol&gt;</code> add margin and padding to the container, which
	 * may look odd if you do not use bullets.
	 * <p>
	 * By default, a container is rendered using a <code>&lt;div&gt;</code>
	 * element.
	 * <p>
	 * 
	 * @see WContainerWidget#isList()
	 * @see WContainerWidget#isOrderedList()
	 * @see WContainerWidget#isUnorderedList()
	 */
	public void setList(boolean list, boolean ordered) {
		this.flags_.set(BIT_LIST, list);
		this.flags_.set(BIT_ORDERED_LIST, ordered);
	}

	/**
	 * Render the container as an HTML list.
	 * <p>
	 * Calls {@link #setList(boolean list, boolean ordered) setList(list,
	 * false)}
	 */
	public final void setList(boolean list) {
		setList(list, false);
	}

	/**
	 * Return if this container is rendered as a List.
	 * <p>
	 * 
	 * @see WContainerWidget#setList(boolean list, boolean ordered)
	 * @see WContainerWidget#isOrderedList()
	 * @see WContainerWidget#isUnorderedList()
	 */
	public boolean isList() {
		return this.flags_.get(BIT_LIST);
	}

	/**
	 * Return if this container is rendered as an Unordered List.
	 * <p>
	 * 
	 * @see WContainerWidget#setList(boolean list, boolean ordered)
	 * @see WContainerWidget#isList()
	 * @see WContainerWidget#isOrderedList()
	 */
	public boolean isUnorderedList() {
		return this.flags_.get(BIT_LIST) && !this.flags_.get(BIT_ORDERED_LIST);
	}

	/**
	 * Return if this container is rendered as an Ordered List.
	 * <p>
	 * 
	 * @see WContainerWidget#setList(boolean list, boolean ordered)
	 * @see WContainerWidget#isList()
	 * @see WContainerWidget#isUnorderedList()
	 */
	public boolean isOrderedList() {
		return this.flags_.get(BIT_LIST) && this.flags_.get(BIT_ORDERED_LIST);
	}

	/**
	 * Event signal emitted when scrolling in the widget.
	 * <p>
	 * This event is emitted when the user scrolls in the widget (for setting
	 * the scroll bar policy, see
	 * {@link WContainerWidget#setOverflow(WContainerWidget.Overflow value, EnumSet orientation)}
	 * ). The event conveys details such as the new scroll bar position, the
	 * total contents height and the current widget height.
	 * <p>
	 * 
	 * @see WContainerWidget#setOverflow(WContainerWidget.Overflow value,
	 *      EnumSet orientation)
	 */
	public EventSignal1<WScrollEvent> scrolled() {
		return this.scrollEventSignal(SCROLL_SIGNAL, true);
	}

	private static final int BIT_CONTENT_ALIGNMENT_CHANGED = 0;
	private static final int BIT_PADDINGS_CHANGED = 1;
	private static final int BIT_OVERFLOW_CHANGED = 2;
	private static final int BIT_ADJUST_CHILDREN_ALIGN = 3;
	private static final int BIT_LIST = 4;
	private static final int BIT_ORDERED_LIST = 5;
	private static final int BIT_LAYOUT_CHANGED = 6;
	private BitSet flags_;
	EnumSet<AlignmentFlag> contentAlignment_;
	private WContainerWidget.Overflow[] overflow_;
	private WLength[] padding_;
	private WLayout layout_;

	private boolean isWasEmpty() {
		if (this.isPopup() || this.getFirstChildIndex() > 0) {
			return false;
		} else {
			return (this.transientImpl_ != null ? this.transientImpl_.addedChildren_
					.size()
					: 0) == this.children_.size();
		}
	}

	void rootAsJavaScript(WApplication app, Writer out, boolean all) {
		List<WWidget> toAdd = all ? this.children_
				: this.transientImpl_ != null ? this.transientImpl_.addedChildren_
						: null;
		if (toAdd != null) {
			for (int i = 0; i < toAdd.size(); ++i) {
				DomElement c = toAdd.get(i).createSDomElement(app);
				c
						.callMethod("omousemove=function(e) {if (!e) e = window.event;return "
								+ app.getJavaScriptClass()
								+ "._p_.dragDrag(event); }");
				c
						.callMethod("mouseup=function(e) {if (!e) e = window.event;return "
								+ app.getJavaScriptClass()
								+ "._p_.dragEnd(event);}");
				c.callMethod("dragstart=function(){return false;}");
				c.asJavaScript(out);
				/* delete c */;
			}
		}
		if (this.transientImpl_ != null) {
			this.transientImpl_.addedChildren_.clear();
		}
		if (!all) {
			if (false && this.transientImpl_ != null
					&& !this.transientImpl_.childRemoveChanges_.isEmpty()) {
				EscapeOStream sout = new EscapeOStream(out);
				for (int i = 0; i < this.transientImpl_.childRemoveChanges_
						.size(); ++i) {
					DomElement c = this.transientImpl_.childRemoveChanges_
							.get(i);
					c.asJavaScript(sout, DomElement.Priority.Delete);
					/* delete c */;
				}
				this.transientImpl_.childRemoveChanges_.clear();
			}
		}
		this.propagateRenderOk(false);
	}

	protected void removeChild(WWidget child) {
		boolean ignoreThisChildRemove = false;
		if (this.transientImpl_ != null) {
			if (this.transientImpl_.addedChildren_.remove(child)) {
				ignoreThisChildRemove = true;
			}
		}
		if (ignoreThisChildRemove) {
			if (this.flags_.get(BIT_IGNORE_CHILD_REMOVES)) {
				ignoreThisChildRemove = false;
			}
		}
		if (ignoreThisChildRemove) {
			this.setIgnoreChildRemoves(true);
		}
		super.removeChild(child);
		if (ignoreThisChildRemove) {
			this.setIgnoreChildRemoves(false);
		}
	}

	protected int getFirstChildIndex() {
		return 0;
	}

	protected void getDomChanges(List<DomElement> result, WApplication app) {
		DomElement e = DomElement.getForUpdate(this, this.getDomElementType());
		if (!app.getSession().getRenderer().isPreLearning()) {
			if (this.flags_.get(BIT_LAYOUT_CHANGED)) {
				if (this.layout_ != null) {
					this.createDomChildren(e, app);
				} else {
					this.flags_.clear(BIT_LAYOUT_CHANGED);
				}
			}
		}
		this.updateDom(e, false);
		result.add(e);
	}

	protected void createDomChildren(DomElement parent, WApplication app) {
		if (this.layout_ != null) {
			boolean fitWidth = !EnumUtils.mask(this.contentAlignment_,
					AlignmentFlag.AlignJustify).isEmpty();
			boolean fitHeight = !!EnumUtils.mask(this.contentAlignment_,
					AlignmentFlag.AlignVerticalMask).isEmpty();
			DomElement c = this.getLayoutImpl().createDomElement(fitWidth,
					fitHeight, app);
			if (this.getPositionScheme() == PositionScheme.Relative) {
				c.setProperty(Property.PropertyStylePosition, "absolute");
				c.setProperty(Property.PropertyStyleLeft, "0");
				c.setProperty(Property.PropertyStyleRight, "0");
			}
			switch (EnumUtils.enumFromSet(EnumUtils.mask(
					this.contentAlignment_, AlignmentFlag.AlignHorizontalMask))) {
			case AlignCenter: {
				DomElement itable = DomElement
						.createNew(DomElementType.DomElement_TABLE);
				itable.setAttribute("class", "Wt-hcenter");
				if (fitHeight) {
					itable.setAttribute("style", "height:100%;");
				}
				DomElement irow = DomElement
						.createNew(DomElementType.DomElement_TR);
				DomElement itd = DomElement
						.createNew(DomElementType.DomElement_TD);
				if (fitHeight) {
					itd.setAttribute("style", "height:100%;");
				}
				itd.addChild(c);
				irow.addChild(itd);
				itable.addChild(irow);
				itable.setId(this.getFormName() + "l");
				c = itable;
				break;
			}
			case AlignLeft:
				break;
			case AlignRight:
				c.setProperty(Property.PropertyStyleFloat, "right");
				break;
			default:
				break;
			}
			parent.addChild(c);
			this.flags_.clear(BIT_LAYOUT_CHANGED);
		} else {
			for (int i = 0; i < this.children_.size(); ++i) {
				parent.addChild(this.children_.get(i).createSDomElement(app));
			}
			if (this.transientImpl_ != null) {
				this.transientImpl_.addedChildren_.clear();
			}
		}
	}

	protected DomElementType getDomElementType() {
		DomElementType type = this.isInline() ? DomElementType.DomElement_SPAN
				: DomElementType.DomElement_DIV;
		WContainerWidget p = ((this.getParent()) instanceof WContainerWidget ? (WContainerWidget) (this
				.getParent())
				: null);
		if (p != null && p.isList()) {
			type = DomElementType.DomElement_LI;
		}
		if (this.isList()) {
			type = this.isOrderedList() ? DomElementType.DomElement_OL
					: DomElementType.DomElement_UL;
		}
		return type;
	}

	protected void updateDom(DomElement element, boolean all) {
		if (all && element.getType() == DomElementType.DomElement_LI
				&& this.isInline()) {
			element.setProperty(Property.PropertyStyleDisplay, "inline");
		}
		if (this.flags_.get(BIT_CONTENT_ALIGNMENT_CHANGED) || all) {
			AlignmentFlag hAlign = EnumUtils.enumFromSet(EnumUtils.mask(
					this.contentAlignment_, AlignmentFlag.AlignHorizontalMask));
			switch (hAlign) {
			case AlignLeft:
				if (this.flags_.get(BIT_CONTENT_ALIGNMENT_CHANGED)) {
					element
							.setProperty(Property.PropertyStyleTextAlign,
									"left");
				}
				break;
			case AlignRight:
				element.setProperty(Property.PropertyStyleTextAlign, "right");
				break;
			case AlignCenter:
				element.setProperty(Property.PropertyStyleTextAlign, "center");
				break;
			case AlignJustify:
				if (!(this.layout_ != null)) {
					element.setProperty(Property.PropertyStyleTextAlign,
							"justify");
				}
				break;
			default:
				break;
			}
			if (this.getDomElementType() == DomElementType.DomElement_TD) {
				AlignmentFlag vAlign = EnumUtils.enumFromSet(EnumUtils
						.mask(this.contentAlignment_,
								AlignmentFlag.AlignVerticalMask));
				switch (vAlign) {
				case AlignTop:
					if (this.flags_.get(BIT_CONTENT_ALIGNMENT_CHANGED)) {
						element.setProperty(
								Property.PropertyStyleVerticalAlign, "top");
					}
					break;
				case AlignMiddle:
					element.setProperty(Property.PropertyStyleVerticalAlign,
							"middle");
					break;
				case AlignBottom:
					element.setProperty(Property.PropertyStyleVerticalAlign,
							"bottom");
				default:
					break;
				}
			}
		}
		if (this.flags_.get(BIT_ADJUST_CHILDREN_ALIGN)
				|| this.flags_.get(BIT_CONTENT_ALIGNMENT_CHANGED) || all) {
			for (int i = 0; i < this.children_.size(); ++i) {
				WWidget child = this.children_.get(i);
				if (!child.isInline()) {
					AlignmentFlag ha = EnumUtils.enumFromSet(EnumUtils.mask(
							this.contentAlignment_,
							AlignmentFlag.AlignHorizontalMask));
					if (ha == AlignmentFlag.AlignCenter) {
						if (!child.getMargin(Side.Left).isAuto()) {
							child
									.setMargin(WLength.Auto, EnumSet
											.of(Side.Left));
						}
						if (!child.getMargin(Side.Right).isAuto()) {
							child.setMargin(WLength.Auto, EnumSet
									.of(Side.Right));
						}
					} else {
						if (ha == AlignmentFlag.AlignRight) {
							if (!child.getMargin(Side.Left).isAuto()) {
								child.setMargin(WLength.Auto, EnumSet
										.of(Side.Left));
							}
						}
					}
				}
			}
			this.flags_.clear(BIT_CONTENT_ALIGNMENT_CHANGED);
			this.flags_.clear(BIT_ADJUST_CHILDREN_ALIGN);
		}
		if (this.flags_.get(BIT_PADDINGS_CHANGED)
				|| all
				&& this.padding_ != null
				&& !(this.padding_[0].isAuto() && this.padding_[1].isAuto()
						&& this.padding_[2].isAuto() && this.padding_[3]
						.isAuto())) {
			if (this.padding_[0].equals(this.padding_[1])
					&& this.padding_[0].equals(this.padding_[2])
					&& this.padding_[0].equals(this.padding_[3])) {
				element.setProperty(Property.PropertyStylePadding,
						this.padding_[0].getCssText());
			} else {
				if (this.padding_ != null) {
					element.setProperty(Property.PropertyStylePadding,
							this.padding_[0].getCssText() + " "
									+ this.padding_[1].getCssText() + " "
									+ this.padding_[2].getCssText() + " "
									+ this.padding_[3].getCssText());
				}
			}
			this.flags_.clear(BIT_PADDINGS_CHANGED);
		}
		if (!WApplication.getInstance().getSession().getRenderer()
				.isPreLearning()
				&& !(this.layout_ != null)) {
			element.setWasEmpty(all || this.isWasEmpty());
			if (this.transientImpl_ != null) {
				WApplication app = WApplication.getInstance();
				List<Integer> orderedInserts = new ArrayList<Integer>();
				List<WWidget> ac = this.transientImpl_.addedChildren_;
				for (int i = 0; i < ac.size(); ++i) {
					orderedInserts.add(this.children_.indexOf(ac.get(i)));
				}
				Collections.sort(orderedInserts);
				int addedCount = this.transientImpl_.addedChildren_.size();
				int totalCount = this.children_.size();
				int insertCount = 0;
				for (int i = 0; i < orderedInserts.size(); ++i) {
					int pos = orderedInserts.get(i);
					DomElement c = this.children_.get(pos).createSDomElement(
							app);
					if (pos + (addedCount - insertCount) == totalCount) {
						element.addChild(c);
					} else {
						element.insertChildAt(c, pos
								+ this.getFirstChildIndex());
					}
					++insertCount;
				}
				this.transientImpl_.addedChildren_.clear();
			}
		}
		super.updateDom(element, all);
		if (this.flags_.get(BIT_OVERFLOW_CHANGED)
				|| all
				&& this.overflow_ != null
				&& !(this.overflow_[0] == WContainerWidget.Overflow.OverflowVisible && this.overflow_[1] == WContainerWidget.Overflow.OverflowVisible)) {
			element.setProperty(Property.PropertyStyleOverflowX,
					cssText[this.overflow_[0].getValue()]);
			this.flags_.clear(BIT_OVERFLOW_CHANGED);
		}
	}

	protected void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_CONTENT_ALIGNMENT_CHANGED);
		this.flags_.clear(BIT_PADDINGS_CHANGED);
		this.flags_.clear(BIT_OVERFLOW_CHANGED);
		this.flags_.clear(BIT_LAYOUT_CHANGED);
		if (this.layout_ != null && deep) {
			this.propagateLayoutItemsOk(this.getLayout());
		} else {
			if (this.transientImpl_ != null) {
				this.transientImpl_.addedChildren_.clear();
			}
		}
		super.propagateRenderOk(deep);
	}

	protected DomElement createDomElement(WApplication app) {
		if (this.transientImpl_ != null) {
			this.transientImpl_.addedChildren_.clear();
		}
		DomElement result = super.createDomElement(app);
		this.createDomChildren(result, app);
		return result;
	}

	protected WLayoutItemImpl createLayoutItemImpl(WLayoutItem item) {
		{
			WWidgetItem wi = ((item) instanceof WWidgetItem ? (WWidgetItem) (item)
					: null);
			if (wi != null) {
				return new StdWidgetItemImpl(wi);
			}
		}
		{
			WBorderLayout l = ((item) instanceof WBorderLayout ? (WBorderLayout) (item)
					: null);
			if (l != null) {
				return new StdGridLayoutImpl(l, l.getGrid());
			}
		}
		{
			WBoxLayout l = ((item) instanceof WBoxLayout ? (WBoxLayout) (item)
					: null);
			if (l != null) {
				return new StdGridLayoutImpl(l, l.getGrid());
			}
		}
		{
			WGridLayout l = ((item) instanceof WGridLayout ? (WGridLayout) (item)
					: null);
			if (l != null) {
				return new StdGridLayoutImpl(l, l.getGrid());
			}
		}
		assert false;
		return null;
	}

	protected StdLayoutImpl getLayoutImpl() {
		return ((this.layout_.getImpl()) instanceof StdLayoutImpl ? (StdLayoutImpl) (this.layout_
				.getImpl())
				: null);
	}

	protected void layoutChanged(boolean deleted) {
		if (!this.flags_.get(BIT_LAYOUT_CHANGED)) {
			if (!(this.transientImpl_ != null)) {
				this.transientImpl_ = new WWebWidget.TransientImpl();
			}
			String fn = EnumUtils.mask(this.contentAlignment_,
					AlignmentFlag.AlignHorizontalMask).equals(
					AlignmentFlag.AlignCenter) ? this.getFormName() + "l"
					: this.getLayoutImpl().getFormName();
			DomElement e = DomElement.getForUpdate(fn,
					DomElementType.DomElement_TABLE);
			e.removeFromParent();
			this.transientImpl_.childRemoveChanges_.add(e);
			this.flags_.set(BIT_LAYOUT_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		if (deleted) {
			this.layout_ = null;
		}
	}

	protected final void layoutChanged() {
		layoutChanged(false);
	}

	private void propagateLayoutItemsOk(WLayoutItem item) {
		if (item.getLayout() != null) {
			WLayout layout = item.getLayout();
			final int c = layout.getCount();
			for (int i = 0; i < c; ++i) {
				this.propagateLayoutItemsOk(layout.getItemAt(i));
			}
		} else {
			if (item.getWidget() != null) {
				WWidget w = item.getWidget();
				w.getWebWidget().propagateRenderOk(true);
			}
		}
	}

	private static String[] cssText = { "visible", "auto", "hidden", "scroll" };
	private static String SCROLL_SIGNAL = "scroll";
}
