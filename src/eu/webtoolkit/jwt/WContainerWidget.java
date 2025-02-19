/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget that holds and manages child widgets.
 *
 * <p>A WContainerWidget acts as a container for child widgets. Child widgets may be added directly
 * to the container or using a layout manager.
 *
 * <p>Use {@link WContainerWidget#addWidget(WWidget widget) addWidget()} or pass the container as
 * constructor argument to a widget to directly add children to the container, without using a
 * layout manager. In that case, CSS-based layout is used, and the resulting display is determined
 * by properties of the children and the container. By default, a WContainerWidget is displayed as a
 * {@link WWidget#setInline(boolean inlined) block } and manages its children within a rectangle.
 * Inline child widgets are layed out in lines, wrapping around as needed, while block child widgets
 * are stacked vertically. The container may add padding at the container edges using {@link
 * WContainerWidget#setPadding(WLength length, EnumSet sides) setPadding()}, and provide alignment
 * of contents using {@link WContainerWidget#setContentAlignment(EnumSet alignment)
 * setContentAlignment()}. A container is rendered by default using a HTML <code>div</code> tag, but
 * this may be changed to an HTML <code>ul</code> or <code>ol</code> tag to make use of other CSS
 * layout techniques, using {@link WContainerWidget#setList(boolean list, boolean ordered)
 * setList()}. In addition, specializations of this class as implemented by {@link WAnchor}, {@link
 * WGroupBox}, {@link WStackedWidget} and {@link WTableCell} provide other alternative rendering of
 * the container.
 *
 * <p>When setting the WContainerWidget {@link WWidget#setInline(boolean inlined) inline } the
 * container only acts as a conceptual container, offering a common style to its children. Inline
 * children are still layed out inline within the flow of the parent container of this container, as
 * if they were inserted directly into that parent container. Block children are then not allowed
 * (according to the HTML specification).
 *
 * <p>To use a layout manager instead of CSS-based layout, use {@link
 * WContainerWidget#setLayout(WLayout layout) setLayout()} or pass the container as constructor
 * argument to a layout manager. In that case you should not define any padding for the container,
 * and widgets and nested layout managers must be added to the layout manager, instead of to the
 * container directly.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * // Example 1:
 * // Instantiate a container widget and add some children whose layout
 * // is governed based on HTML/CSS rules.
 * WContainerWidget container1 = new WContainerWidget();
 * container1.addWidget(new WText("Some text"));
 * container1.addWidget(new WImage("images/img.png"));
 * WContainerWidget child3 = new WContainerWidget(container1);
 *
 * // Example 2:
 * // Instantiate a container widget which uses a layout manager
 * WContainerWidget container2 = new WContainerWidget();
 * // give the container a fixed height
 * container2.resize(WLength.Auto, new WLength(600));
 *
 * WVBoxLayout layout = new WVBoxLayout();
 * layout.addWidget(new WText("Some text"));
 * layout.addWidget(new WImage("images/img.png"));
 *
 * container2.setLayout(layout);      // set the layout to the container.
 *
 * }</pre>
 *
 * <p>When using a layout manager, you need to carefully consider the alignment of the layout
 * manager with respect to the container: when the container&apos;s height is unconstrained (not
 * specified explicitly using {@link WWebWidget#resize(WLength width, WLength height)
 * WWebWidget#resize()} or a style class, and the container is not included in a layout manager),
 * you should pass {@link AlignmentFlag#Top} to {@link WContainerWidget#setLayout(WLayout layout)
 * setLayout()}.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>Depending on its configuration and usage, the widget corresponds to the following HTML tags:
 *
 * <ul>
 *   <li>By default, the widget corresponds to a <code>&lt;div&gt;</code> tag.
 *   <li>When configured with setInline(true), the widget corresponds to a <code>&lt;span&gt;</code>
 *       .
 *   <li>When configured with setList(true), the widget corresponds to a <code>&lt;ul&gt;</code>.
 *   <li>When configured with setList(true, true), the widget corresponds to a <code>&lt;ol&gt;
 *       </code>.
 *   <li>When inserted into a container widget that {@link WContainerWidget#isList() isList()}, the
 *       widget corresponds to a <code>&lt;li&gt;</code>.
 * </ul>
 *
 * <p>This widget does not provide styling, and can be styled using inline or external CSS as
 * appropriate.
 */
public class WContainerWidget extends WInteractWidget {
  private static Logger logger = LoggerFactory.getLogger(WContainerWidget.class);

  /** Creates a container. */
  public WContainerWidget(WContainerWidget parentContainer) {
    super();
    this.flags_ = new BitSet();
    this.contentAlignment_ = EnumSet.of(AlignmentFlag.Left);
    this.overflow_ = null;
    this.padding_ = null;
    this.children_ = new ArrayList<WWidget>();
    this.layout_ = null;
    this.addedChildren_ = null;
    this.globalUnfocused_ = false;
    this.scrollTop_ = 0;
    this.scrollLeft_ = 0;
    this.setInline(false);
    this.setLoadLaterWhenInvisible(false);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a container.
   *
   * <p>Calls {@link #WContainerWidget(WContainerWidget parentContainer)
   * this((WContainerWidget)null)}
   */
  public WContainerWidget() {
    this((WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {
    this.beingDeleted();
    this.clear();

    super.remove();
  }
  /**
   * Sets a layout manager for the container.
   *
   * <p>Note that you can nest layout managers inside each other, to create a complex layout
   * hierarchy.
   *
   * <p>If a previous layout manager was already set, it is first deleted. In that case, you will
   * need to make sure that you either readd all widgets that were part of the previous layout to
   * the new layout, or delete them, to avoid memory leaks.
   *
   * <p>
   *
   * @see WContainerWidget#getLayout()
   */
  public void setLayout(WLayout layout) {
    this.clear();
    this.layout_ = layout;
    if (this.layout_ != null) {
      this.layout_.setParentWidget(this);
    }
    EnumUtils.setOnly(this.contentAlignment_, AlignmentFlag.Justify);
    this.flags_.set(BIT_LAYOUT_NEEDS_RERENDER);
    this.repaint();
  }
  // public Layout  setLayout(<Woow... some pseudoinstantiation type!> layout) ;
  /**
   * Returns the layout manager that was set for the container.
   *
   * <p>If no layout manager was previously set using setLayout({@link WLayout} *), 0 is returned.
   *
   * <p>
   */
  public WLayout getLayout() {
    return this.layout_;
  }
  /**
   * Adds a child widget to this container.
   *
   * <p>The widget is appended to the list of children, and thus also layed-out at the end.
   *
   * <p>If, for some reason, you want to be in control of the lifetime of the widget, you can
   * retrieve a unique_ptr with WObject::removeChild()
   */
  public void addWidget(WWidget widget) {
    this.insertWidget(this.children_.size(), widget);
  }
  // public Widget  addWidget(<Woow... some pseudoinstantiation type!> widget) ;
  // public Widget  () ;
  // public Widget  (Arg1 arg1) ;
  // public Widget  (Arg1 arg1, Arg2 arg2) ;
  // public Widget  (Arg1 arg1, Arg2 arg2, Arg3 arg3) ;
  // public Widget  (Arg1 arg1, Arg2 arg2, Arg3 arg3, Arg4 arg4) ;
  // public Widget  addNew(Arg1 arg1, Arg2 arg2, Arg3 arg3, Arg4 arg4, Arg5 arg5) ;
  /**
   * Inserts a child widget in this container, before another widget.
   *
   * <p>The <i>widget</i> is inserted at the place of the <code>before</code> widget, and subsequent
   * widgets are shifted.
   *
   * <p>If, for some reason, you want to be in control of the lifetime of the widget, you can regain
   * ownership of the widget (without any functional implication) using WObject::removeChild()
   *
   * <p>
   */
  public void insertBefore(WWidget widget, WWidget before) {
    int index = this.getIndexOf(before);
    if (index == -1) {
      logger.error(
          new StringWriter()
              .append("insertBefore(): before is not in container, appending at back")
              .toString());
      index = this.children_.size();
    }
    this.insertWidget(index, widget);
  }
  // public Widget  insertBefore(<Woow... some pseudoinstantiation type!> widget, WWidget  before) ;
  /**
   * Inserts a child widget in this container at given index.
   *
   * <p>The <i>widget</i> is inserted at the given <code>index</code>, and subsequent widgets are
   * shifted.
   *
   * <p>If, for some reason, you want to be in control of the lifetime of the widget, you can regain
   * ownership of the widget (without any functional implication) using WObject::removeChild()
   *
   * <p>
   */
  public void insertWidget(int index, WWidget widget) {
    WWidget w = widget;
    if (!(this.addedChildren_ != null)) {
      this.addedChildren_ = new ArrayList<WWidget>();
      if (this.getDomElementType() != DomElementType.TD
          && this.getDomElementType() != DomElementType.TH) {
        this.setLoadLaterWhenInvisible(true);
      }
    }
    this.addedChildren_.add(widget);
    this.children_.add(0 + index, widget);
    this.flags_.set(BIT_ADJUST_CHILDREN_ALIGN);
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    this.widgetAdded(w);
  }
  // public Widget  insertWidget(int index, <Woow... some pseudoinstantiation type!> widget) ;
  /**
   * Removes a child widget from this container.
   *
   * <p>If the {@link WContainerWidget} owns the given widget (i.e. if it was added with {@link
   * WContainerWidget#addWidget(WWidget widget) addWidget()} or {@link
   * WContainerWidget#insertWidget(int index, WWidget widget) insertWidget()} and not removed with
   * WObject::removeChild()), a unique_ptr to this widget is returned. Otherwise, this returns
   * nullptr.
   */
  public WWidget removeWidget(WWidget widget) {
    if (this.layout_ != null) {
      WWidget result = this.layout_.removeWidget(widget);
      if (result != null) {
        this.widgetRemoved(result, false);
      }
      return result;
    }
    int index = this.getIndexOf(widget);
    if (index != -1) {
      boolean renderRemove = true;
      if (this.addedChildren_ != null && this.addedChildren_.remove(widget)) {
        renderRemove = false;
      }
      this.children_.remove(0 + index);
      WWidget result = widget;
      this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
      this.widgetRemoved(widget, renderRemove);
      return result;
    } else {
      logger.error(new StringWriter().append("removeWidget(): widget not in container").toString());
      return null;
    }
  }
  /**
   * Removes all widgets.
   *
   * <p>This removes all children that have been added to this container. If a layout was set, also
   * the layout manager is cleared.
   */
  public void clear() {
    this.layout_ = null;
    this.flags_.set(BIT_LAYOUT_NEEDS_RERENDER);
    this.repaint();
    while (!this.children_.isEmpty()) {
      {
        WWidget toRemove = this.removeWidget(this.children_.get(this.children_.size() - 1));
        if (toRemove != null) toRemove.remove();
      }
    }
  }
  /** Returns the index of a widget. */
  public int getIndexOf(WWidget widget) {
    for (int i = 0; i < this.children_.size(); ++i) {
      if (this.children_.get(i) == widget) {
        return i;
      }
    }
    return -1;
  }
  /** Returns the widget at <i>index</i> */
  public WWidget getWidget(int index) {
    return this.children_.get(index);
  }
  /** Returns the number of widgets in this container. */
  public int getCount() {
    return this.children_.size();
  }
  /**
   * Specifies how child widgets must be aligned within the container.
   *
   * <p>For a {@link WContainerWidget}, only specifes the horizontal alignment of child widgets.
   * Note that there is no way to specify vertical alignment: children are always pushed to the top
   * of the container.
   *
   * <p>For a {@link WTableCell}, this may also specify the vertical alignment. The default
   * alignment is ({@link AlignmentFlag#Top} | {@link AlignmentFlag#Left}).
   */
  public void setContentAlignment(EnumSet<AlignmentFlag> alignment) {
    this.contentAlignment_ = EnumSet.copyOf(alignment);
    AlignmentFlag vAlign =
        EnumUtils.enumFromSet(
            EnumUtils.mask(this.contentAlignment_, AlignmentFlag.AlignVerticalMask));
    if (vAlign == null) {
      this.contentAlignment_.add(AlignmentFlag.Top);
    }
    this.flags_.set(BIT_CONTENT_ALIGNMENT_CHANGED);
    this.repaint();
  }
  /**
   * Specifies how child widgets must be aligned within the container.
   *
   * <p>Calls {@link #setContentAlignment(EnumSet alignment)
   * setContentAlignment(EnumSet.of(alignmen, alignment))}
   */
  public final void setContentAlignment(AlignmentFlag alignmen, AlignmentFlag... alignment) {
    setContentAlignment(EnumSet.of(alignmen, alignment));
  }
  /**
   * Sets padding inside the widget.
   *
   * <p>Setting padding has the effect of adding distance between the widget children and the
   * border.
   */
  public void setPadding(final WLength length, EnumSet<Side> sides) {
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
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
  }
  /**
   * Sets padding inside the widget.
   *
   * <p>Calls {@link #setPadding(WLength length, EnumSet sides) setPadding(length, EnumSet.of(side,
   * sides))}
   */
  public final void setPadding(final WLength length, Side side, Side... sides) {
    setPadding(length, EnumSet.of(side, sides));
  }
  /**
   * Sets padding inside the widget.
   *
   * <p>Calls {@link #setPadding(WLength length, EnumSet sides) setPadding(length, Side.AllSides)}
   */
  public final void setPadding(final WLength length) {
    setPadding(length, Side.AllSides);
  }
  /**
   * Returns the padding set for the widget.
   *
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
        logger.error(new StringWriter().append("padding(): improper side.").toString());
        return new WLength();
    }
  }
  /**
   * Returns the alignment of children.
   *
   * <p>
   *
   * @see WContainerWidget#setContentAlignment(EnumSet alignment)
   */
  public EnumSet<AlignmentFlag> getContentAlignment() {
    return this.contentAlignment_;
  }
  /** Sets how overflow of contained children must be handled. */
  public void setOverflow(Overflow value, EnumSet<Orientation> orientation) {
    if (!(this.overflow_ != null)) {
      this.overflow_ = new Overflow[2];
      this.overflow_[0] = this.overflow_[1] = Overflow.Visible;
    }
    if (orientation.contains(Orientation.Horizontal)) {
      this.overflow_[0] = value;
    }
    if (orientation.contains(Orientation.Vertical)) {
      this.overflow_[1] = value;
    }
    this.flags_.set(BIT_OVERFLOW_CHANGED);
    this.repaint();
  }
  /**
   * Sets how overflow of contained children must be handled.
   *
   * <p>Calls {@link #setOverflow(Overflow value, EnumSet orientation) setOverflow(value,
   * EnumSet.of(orientatio, orientation))}
   */
  public final void setOverflow(
      Overflow value, Orientation orientatio, Orientation... orientation) {
    setOverflow(value, EnumSet.of(orientatio, orientation));
  }
  /**
   * Sets how overflow of contained children must be handled.
   *
   * <p>Calls {@link #setOverflow(Overflow value, EnumSet orientation) setOverflow(value, EnumSet.of
   * (Orientation.Horizontal, Orientation.Vertical))}
   */
  public final void setOverflow(Overflow value) {
    setOverflow(value, EnumSet.of(Orientation.Horizontal, Orientation.Vertical));
  }
  /**
   * Renders the container as an HTML list.
   *
   * <p>Setting <code>renderList</code> to <code>true</code> will cause the container to be using an
   * HTML <code>&lt;ul&gt;</code> or <code>&lt;ol&gt;</code> type, depending on the value of <code>
   * orderedList</code>. This must be set before the initial render of the container. When set, any
   * contained {@link WContainerWidget} will be rendered as an HTML <code>&lt;li&gt;</code>. Adding
   * non-WContainerWidget children results in unspecified behaviour.
   *
   * <p>Note that CSS default layout rules for <code>&lt;ul&gt;</code> and <code>&lt;ol&gt;</code>
   * add margin and padding to the container, which may look odd if you do not use bullets.
   *
   * <p>By default, a container is rendered using a <code>&lt;div&gt;</code> element.
   *
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
   * Renders the container as an HTML list.
   *
   * <p>Calls {@link #setList(boolean list, boolean ordered) setList(list, false)}
   */
  public final void setList(boolean list) {
    setList(list, false);
  }
  /**
   * Returns if this container is rendered as a List.
   *
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
   * Returns if this container is rendered as an Unordered List.
   *
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
   * Returns if this container is rendered as an Ordered List.
   *
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
   *
   * <p>This event is emitted when the user scrolls in the widget (for setting the scroll bar
   * policy, see {@link WContainerWidget#setOverflow(Overflow value, EnumSet orientation)
   * setOverflow()}). The event conveys details such as the new scroll bar position, the total
   * contents height and the current widget height.
   *
   * <p>
   *
   * @see WContainerWidget#setOverflow(Overflow value, EnumSet orientation)
   */
  public EventSignal1<WScrollEvent> scrolled() {
    return this.scrollEventSignal(SCROLL_SIGNAL, true);
  }
  /**
   * return the number of pixels the container is scrolled horizontally
   *
   * <p>This value is only set if {@link WContainerWidget#setOverflow(Overflow value, EnumSet
   * orientation) setOverflow()} has been called
   *
   * <p>
   *
   * @see WContainerWidget#setOverflow(Overflow value, EnumSet orientation)
   * @see WContainerWidget#getScrollLeft()
   */
  public int getScrollTop() {
    return this.scrollTop_;
  }
  /**
   * return the number of pixels the container is scrolled vertically
   *
   * <p>This value is only set if {@link WContainerWidget#setOverflow(Overflow value, EnumSet
   * orientation) setOverflow()} has been called
   *
   * <p>
   *
   * @see WContainerWidget#setOverflow(Overflow value, EnumSet orientation)
   * @see WContainerWidget#getScrollTop()
   */
  public int getScrollLeft() {
    return this.scrollLeft_;
  }

  public void setGlobalUnfocused(boolean b) {
    this.globalUnfocused_ = b;
  }

  public boolean isGlobalUnfocussed() {
    return this.globalUnfocused_;
  }

  private static String SCROLL_SIGNAL = "scroll";
  private static final int BIT_CONTENT_ALIGNMENT_CHANGED = 0;
  private static final int BIT_PADDINGS_CHANGED = 1;
  private static final int BIT_OVERFLOW_CHANGED = 2;
  private static final int BIT_ADJUST_CHILDREN_ALIGN = 3;
  private static final int BIT_LIST = 4;
  private static final int BIT_ORDERED_LIST = 5;
  private static final int BIT_LAYOUT_NEEDS_RERENDER = 6;
  private static final int BIT_LAYOUT_NEEDS_UPDATE = 7;
  BitSet flags_;
  EnumSet<AlignmentFlag> contentAlignment_;
  private Overflow[] overflow_;
  private WLength[] padding_;
  List<WWidget> children_;
  private WLayout layout_;
  private List<WWidget> addedChildren_;
  private boolean globalUnfocused_;
  private int scrollTop_;
  private int scrollLeft_;

  private boolean isWasEmpty() {
    if (this.isPopup() || this.getFirstChildIndex() > 0) {
      return false;
    } else {
      return (this.addedChildren_ != null ? this.addedChildren_.size() : 0)
          == this.children_.size();
    }
  }

  void rootAsJavaScript(WApplication app, final StringBuilder out, boolean all) {
    List<WWidget> toAdd = all ? this.children_ : this.addedChildren_;
    if (toAdd != null) {
      for (int i = 0; i < toAdd.size(); ++i) {
        DomElement c = toAdd.get(i).createSDomElement(app);
        app.streamBeforeLoadJavaScript(out, false);
        c.callMethod(
            "omousemove=function(e) {if (!e) e = window.event;return "
                + app.getJavaScriptClass()
                + "._p_.dragDrag(event); }");
        c.callMethod(
            "mouseup=function(e) {if (!e) e = window.event;return "
                + app.getJavaScriptClass()
                + "._p_.dragEnd(event);}");
        c.callMethod("dragstart=function(){return false;}");
        c.asJavaScript(out);
      }
    }
    this.addedChildren_ = null;
    if (!all) {}
    this.propagateRenderOk(false);
  }

  int getFirstChildIndex() {
    return 0;
  }

  void childResized(WWidget child, EnumSet<Orientation> directions) {
    if (this.layout_ != null) {
      WWidgetItem item = this.layout_.findWidgetItem(child);
      if (item != null) {
        if ((ObjectUtils.cast(item.getParentLayout().getImpl(), StdLayoutImpl.class))
            .itemResized(item)) {
          this.flags_.set(BIT_LAYOUT_NEEDS_UPDATE);
          this.repaint();
        }
      }
    } else {
      super.childResized(child, directions);
    }
  }

  protected void parentResized(WWidget parent, EnumSet<Orientation> directions) {
    if (this.layout_ != null) {
      if ((ObjectUtils.cast(this.layout_.getImpl(), StdLayoutImpl.class)).isParentResized()) {
        this.flags_.set(BIT_LAYOUT_NEEDS_UPDATE);
        this.repaint();
      }
    } else {
      super.parentResized(parent, directions);
    }
  }

  protected void getDomChanges(final List<DomElement> result, WApplication app) {
    DomElement e = DomElement.getForUpdate(this, this.getDomElementType());
    if (!app.getSession().getRenderer().isPreLearning()) {
      if (this.flags_.get(BIT_LAYOUT_NEEDS_RERENDER)) {
        e.removeAllChildren(this.getFirstChildIndex());
        this.createDomChildren(e, app);
        this.flags_.clear(BIT_LAYOUT_NEEDS_RERENDER);
        this.flags_.clear(BIT_LAYOUT_NEEDS_UPDATE);
      }
    }
    this.updateDomChildren(e, app);
    this.updateDom(e, false);
    result.add(e);
  }

  protected void iterateChildren(final HandleWidgetMethod method) {
    for (int i = 0; i < this.children_.size(); ++i) {
      method.handle(this.children_.get(i));
    }
    if (this.layout_ != null) {
      this.layout_.iterateWidgets(method);
    }
  }

  DomElement createDomElement(WApplication app, boolean addChildren) {
    this.addedChildren_ = null;
    DomElement result = super.createDomElement(app);
    if (addChildren) {
      this.createDomChildren(result, app);
    }
    this.flags_.clear(BIT_LAYOUT_NEEDS_RERENDER);
    this.flags_.clear(BIT_LAYOUT_NEEDS_UPDATE);
    return result;
  }

  void createDomChildren(final DomElement parent, WApplication app) {
    if (this.layout_ != null) {
      this.containsLayout();
      boolean fitWidth = true;
      boolean fitHeight = true;
      DomElement c = this.getLayoutImpl().createDomElement(parent, fitWidth, fitHeight, app);
      if (c != parent) {
        parent.addChild(c);
      }
      this.flags_.clear(BIT_LAYOUT_NEEDS_RERENDER);
      this.flags_.clear(BIT_LAYOUT_NEEDS_UPDATE);
    } else {
      for (int i = 0; i < this.children_.size(); ++i) {
        parent.addChild(this.children_.get(i).createSDomElement(app));
      }
    }
    this.addedChildren_ = null;
  }

  void updateDomChildren(final DomElement parent, WApplication app) {
    if (!app.getSession().getRenderer().isPreLearning() && !(this.layout_ != null)) {
      if (parent.getMode() == DomElement.Mode.Update) {
        parent.setWasEmpty(this.isWasEmpty());
      }
      if (this.addedChildren_ != null) {
        for (; ; ) {
          List<Integer> orderedInserts = new ArrayList<Integer>();
          final List<WWidget> ac = this.addedChildren_;
          for (int i = 0; i < ac.size(); ++i) {
            orderedInserts.add(this.getIndexOf(ac.get(i)));
          }
          Collections.sort(orderedInserts);
          int addedCount = this.addedChildren_.size();
          int totalCount = this.children_.size();
          int insertCount = 0;
          this.addedChildren_ = null;
          for (int i = 0; i < orderedInserts.size(); ++i) {
            int pos = orderedInserts.get(i);
            DomElement c = this.children_.get(pos).createSDomElement(app);
            if (pos + (addedCount - insertCount) == totalCount) {
              parent.addChild(c);
            } else {
              parent.insertChildAt(c, pos + this.getFirstChildIndex());
            }
            ++insertCount;
          }
          if (!(this.addedChildren_ != null) || this.addedChildren_.isEmpty()) {
            break;
          }
        }
        this.addedChildren_ = null;
      }
    }
    if (this.flags_.get(BIT_LAYOUT_NEEDS_UPDATE)) {
      if (this.layout_ != null) {
        this.getLayoutImpl().updateDom(parent);
      }
      this.flags_.clear(BIT_LAYOUT_NEEDS_UPDATE);
    }
  }

  DomElementType getDomElementType() {
    DomElementType type = this.isInline() ? DomElementType.SPAN : DomElementType.DIV;
    WContainerWidget p = ObjectUtils.cast(this.getParentWebWidget(), WContainerWidget.class);
    if (p != null && p.isList()) {
      type = DomElementType.LI;
    }
    if (this.isList()) {
      type = this.isOrderedList() ? DomElementType.OL : DomElementType.UL;
    }
    return type;
  }

  void updateDom(final DomElement element, boolean all) {
    element.setGlobalUnfocused(this.globalUnfocused_);
    if (all && element.getType() == DomElementType.LI && this.isInline()) {
      element.setProperty(Property.StyleDisplay, "inline");
    }
    if (this.flags_.get(BIT_CONTENT_ALIGNMENT_CHANGED) || all) {
      AlignmentFlag hAlign =
          EnumUtils.enumFromSet(
              EnumUtils.mask(this.contentAlignment_, AlignmentFlag.AlignHorizontalMask));
      boolean ltr = WApplication.getInstance().getLayoutDirection() == LayoutDirection.LeftToRight;
      switch (hAlign) {
        case Left:
          if (this.flags_.get(BIT_CONTENT_ALIGNMENT_CHANGED)) {
            element.setProperty(Property.StyleTextAlign, ltr ? "left" : "right");
          }
          break;
        case Right:
          element.setProperty(Property.StyleTextAlign, ltr ? "right" : "left");
          break;
        case Center:
          element.setProperty(Property.StyleTextAlign, "center");
          break;
        case Justify:
          if (!(this.layout_ != null)) {
            element.setProperty(Property.StyleTextAlign, "justify");
          }
          break;
        default:
          break;
      }
      if (this.getDomElementType() == DomElementType.TD) {
        AlignmentFlag vAlign =
            EnumUtils.enumFromSet(
                EnumUtils.mask(this.contentAlignment_, AlignmentFlag.AlignVerticalMask));
        switch (vAlign) {
          case Top:
            if (this.flags_.get(BIT_CONTENT_ALIGNMENT_CHANGED)) {
              element.setProperty(Property.StyleVerticalAlign, "top");
            }
            break;
          case Middle:
            element.setProperty(Property.StyleVerticalAlign, "middle");
            break;
          case Bottom:
            element.setProperty(Property.StyleVerticalAlign, "bottom");
          default:
            break;
        }
      }
    }
    if (this.flags_.get(BIT_ADJUST_CHILDREN_ALIGN)
        || this.flags_.get(BIT_CONTENT_ALIGNMENT_CHANGED)
        || all) {
      for (int i = 0; i < this.children_.size(); ++i) {
        WWidget child = this.children_.get(i);
        if (!child.isInline()) {
          AlignmentFlag ha =
              EnumUtils.enumFromSet(
                  EnumUtils.mask(this.contentAlignment_, AlignmentFlag.AlignHorizontalMask));
          if (ha == AlignmentFlag.Center) {
            if (!child.getMargin(Side.Left).isAuto()) {
              child.setMargin(WLength.Auto, EnumSet.of(Side.Left));
            }
            if (!child.getMargin(Side.Right).isAuto()) {
              child.setMargin(WLength.Auto, EnumSet.of(Side.Right));
            }
          } else {
            if (ha == AlignmentFlag.Right) {
              if (!child.getMargin(Side.Left).isAuto()) {
                child.setMargin(WLength.Auto, EnumSet.of(Side.Left));
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
            && !(this.padding_[0].isAuto()
                && this.padding_[1].isAuto()
                && this.padding_[2].isAuto()
                && this.padding_[3].isAuto())) {
      if (this.padding_[0].equals(this.padding_[1])
          && this.padding_[0].equals(this.padding_[2])
          && this.padding_[0].equals(this.padding_[3])) {
        element.setProperty(Property.StylePadding, this.padding_[0].getCssText());
      } else {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 4; ++i) {
          if (i != 0) {
            s.append(' ');
          }
          s.append(this.padding_[i].isAuto() ? "0" : this.padding_[i].getCssText());
        }
        element.setProperty(Property.StylePadding, s.toString());
      }
      this.flags_.clear(BIT_PADDINGS_CHANGED);
    }
    if (this.flags_.get(BIT_OVERFLOW_CHANGED)
        || all
            && this.overflow_ != null
            && !(this.overflow_[0] == Overflow.Visible && this.overflow_[1] == Overflow.Visible)) {
      element.setProperty(Property.StyleOverflowX, cssText[(int) this.overflow_[0].getValue()]);
      element.setProperty(Property.StyleOverflowY, cssText[(int) this.overflow_[1].getValue()]);
      this.setFormObject(true);
      this.setJavaScriptMember(
          "wtEncodeValue", "(self) => {return `${self.scrollTop};${self.scrollLeft}`;}");
      this.flags_.clear(BIT_OVERFLOW_CHANGED);
      WApplication app = WApplication.getInstance();
      if (app.getEnvironment().agentIsIE()
          && (this.overflow_[0] == Overflow.Auto || this.overflow_[0] == Overflow.Scroll)) {
        if (this.getPositionScheme() == PositionScheme.Static) {
          element.setProperty(Property.StylePosition, "relative");
        }
      }
    }
    super.updateDom(element, all);
  }

  void propagateRenderOk(boolean deep) {
    this.flags_.clear(BIT_CONTENT_ALIGNMENT_CHANGED);
    this.flags_.clear(BIT_PADDINGS_CHANGED);
    this.flags_.clear(BIT_OVERFLOW_CHANGED);
    this.flags_.clear(BIT_LAYOUT_NEEDS_RERENDER);
    this.flags_.clear(BIT_LAYOUT_NEEDS_UPDATE);
    if (this.layout_ != null && deep) {
      this.propagateLayoutItemsOk(this.getLayout());
    } else {
      this.addedChildren_ = null;
    }
    super.propagateRenderOk(deep);
  }

  protected DomElement createDomElement(WApplication app) {
    return this.createDomElement(app, true);
  }

  StdLayoutImpl getLayoutImpl() {
    return ObjectUtils.cast(this.layout_.getImpl(), StdLayoutImpl.class);
  }

  protected void setFormData(final WObject.FormData formData) {
    if (!(formData.values.length == 0)) {
      List<String> attributes = new ArrayList<String>();
      StringUtils.split(attributes, formData.values[0], ";", false);
      if (attributes.size() == 2) {
        try {
          this.scrollTop_ = (int) Double.parseDouble(attributes.get(0));
          this.scrollLeft_ = (int) Double.parseDouble(attributes.get(1));
        } catch (final RuntimeException e) {
          logger.error(
              new StringWriter()
                  .append("WContainerWidget ")
                  .append(this.getId())
                  .append(": error parsing form data: '")
                  .append(formData.values[0])
                  .append("', ignoring value, details: ")
                  .append(e.toString())
                  .toString());
        }
      } else {
        logger.error(
            new StringWriter()
                .append("WContainerWidget ")
                .append(this.getId())
                .append(": error parsing form data: '")
                .append(formData.values[0])
                .append("', ignoring value")
                .toString());
      }
    }
  }

  private void propagateLayoutItemsOk(WLayoutItem item) {
    if (!(item != null)) {
      return;
    }
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

  void layoutChanged(boolean rerender) {
    if (rerender) {
      this.flags_.set(BIT_LAYOUT_NEEDS_RERENDER);
    } else {
      this.flags_.set(BIT_LAYOUT_NEEDS_UPDATE);
    }
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
  }

  private static String[] cssText = {"visible", "auto", "hidden", "scroll"};
}
