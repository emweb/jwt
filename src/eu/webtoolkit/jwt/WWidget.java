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
 * A WWidget is the abstract base class for any Wt widget
 * 
 * 
 * The user-interface is organized in a tree structure, in which all nodes are
 * widgets. When a widget is deleted, it is also visually removed from the
 * user-interface and all children are deleted recursively. All widgets, except
 * for the application&apos;s root widget have a parent, which is usually a
 * {@link WContainerWidget}.
 * <p>
 * WWidget is abstract and cannot be instantiated. Implementations either from
 * {@link WWebWidget} (for basic widgets with a direct HTML counter-part) or
 * {@link WCompositeWidget} (for anything else). To add a {@link WWebWidget} to
 * a parent {@link WContainerWidget}, either specify the parent in the
 * constructor (which is conventionally the last constructor argument), or add
 * the widget to the parent using
 * {@link WContainerWidget#addWidget(WWidget widget)}.
 * <p>
 * A widget provides methods to manage its decorative style. It also provides
 * access to CSS-based layout. Alternatively, you may use layout managers (see
 * {@link WContainerWidget#setLayout(WLayout layout)}) to manage layout of
 * widgets, in which case you should not use methods that are marked as being
 * involved in CSS-based layout only.
 */
public abstract class WWidget extends WObject {
	/**
	 * Delete a widget.
	 * 
	 * Deletes a widget and all children (recursively). If the widget is
	 * contained in another widget, it is removed first.
	 * <p>
	 * 
	 * @see WContainerWidget#removeWidget(WWidget widget)
	 */
	public void remove() {
		while (!this.eventSignals_.isEmpty()) {
			AbstractEventSignal s = this.eventSignals_.peek();
			this.eventSignals_.removeFirst();
			s.destroy();
		}
		if (this.needRerender_) {
			WApplication.instance().getSession().getRenderer().doneUpdate(this);
		}
	}

	/**
	 * Return the parent widget.
	 * 
	 * With few exceptions, the parent is a {@link WContainerWidget}, and has
	 * been set implicitly when adding the widget to a container using
	 * {@link WContainerWidget#addWidget(WWidget widget)} or by passing a
	 * container as a parent to the constructor.
	 */
	public WWidget getParent() {
		return (WWidget) super.getParent();
	}

	/**
	 * Set the widget position scheme.
	 * 
	 * Establishes how the widget must be layed-out relative to its siblings.
	 * The default position scheme is Static.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see PositionScheme#PositionScheme
	 * @see WWidget#getPositionScheme()
	 */
	public abstract void setPositionScheme(PositionScheme scheme);

	/**
	 * Returns the widget position scheme.
	 * 
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see PositionScheme#PositionScheme
	 * @see WWidget#setPositionScheme(PositionScheme scheme)
	 */
	public abstract PositionScheme getPositionScheme();

	/**
	 * Apply offsets to a widget.
	 * 
	 * The argument <i>sides</i> may be a logical concatenation of
	 * {@link Side#Left Left}, {@link Side#Right Right}, {@link Side#Top Top},
	 * and {@link Side#Bottom Bottom}.
	 * <p>
	 * This applies only to widgets that have a position scheme that is
	 * {@link PositionScheme#Relative Relative}, {@link PositionScheme#Absolute
	 * Absolute}, or {@link PositionScheme#Fixed Fixed}, and has a slightly
	 * different meaning for these three cases.
	 * <p>
	 * For a relatively positioned widget, an offset applies relative to the
	 * position the widget would have when layed-out using a
	 * {@link PositionScheme#Static Static} position scheme. The widget may be
	 * shifted to the left or right by specifying an offset to the
	 * {@link Side#Left Left} or {@link Side#Right Right}). The widget may be
	 * shifted vertically, by specifying an offset for the
	 * {@link AlignmentFlag#AlignTop Top} or {@link Side#Bottom Bottom}.
	 * <p>
	 * For an absolutely positioned widget, an offset specifies a distance of
	 * the corresponding side of the widget with respect to the corresponding
	 * side of the reference parent widget. Thus, setting all offsets to 0
	 * result in a widget that spans the entire reference widget. The reference
	 * parent widget is the first ancestor widget that is a table cell, or a
	 * widget with a relative, absolute or fixed position scheme.
	 * <p>
	 * For an fixed positioned widget, an offset specifies a distance of the
	 * corresponding side of the widget with respect to the browser window,
	 * regardless of scrolling. Thus, setting all offsets to 0 result in a
	 * widget that spans the entire browser window.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#getOffset(Side side)
	 */
	public abstract void setOffsets(WLength offset, EnumSet<Side> sides);

	public final void setOffsets(WLength offset, Side side, Side... sides) {
		setOffsets(offset, EnumSet.of(side, sides));
	}

	public final void setOffsets(WLength offset) {
		setOffsets(offset, Side.All);
	}

	public void setOffsets(int pixels, EnumSet<Side> sides) {
		this.setOffsets(new WLength(pixels), sides);
	}

	public final void setOffsets(int pixels, Side side, Side... sides) {
		setOffsets(pixels, EnumSet.of(side, sides));
	}

	public final void setOffsets(int pixels) {
		setOffsets(pixels, Side.All);
	}

	/**
	 * Retrieve the offset of the widget.
	 * 
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setOffsets(WLength offset, EnumSet sides)
	 */
	public abstract WLength getOffset(Side side);

	/**
	 * Resize the widget.
	 * 
	 * Specify a new size for this widget, by specifying width and height. By
	 * default a widget has automatic width and height, see
	 * {@link WLength#isAuto()}.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#getWidth()
	 * @see WWidget#getHeight()
	 */
	public abstract void resize(WLength width, WLength height);

	public void resize(int widthPixels, int heightPixels) {
		this.resize(new WLength(widthPixels), new WLength(heightPixels));
	}

	/**
	 * Returns the widget width.
	 * 
	 * Return the width set for this widget. This is not a calculated width,
	 * based on layout, but the width as specified with
	 * {@link WWidget#resize(WLength width, WLength height)}.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#resize(WLength width, WLength height)
	 * @see WWidget#getHeight()
	 */
	public abstract WLength getWidth();

	/**
	 * Returns the widget height.
	 * 
	 * Return the height set for this widget. This is not a calculated height,
	 * based on layout, but the height as specified previously with
	 * {@link WWidget#resize(WLength width, WLength height)}.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#resize(WLength width, WLength height)
	 * @see WWidget#getWidth()
	 */
	public abstract WLength getHeight();

	/**
	 * Set a minimum size.
	 * 
	 * Specify a minimum size for this widget. When the widget is managed using
	 * a layout manager, these sizes are also taken into account.
	 * <p>
	 * 
	 * @see WWidget#resize(WLength width, WLength height)
	 * @see WWidget#getMinimumWidth()
	 * @see WWidget#getMinimumHeight()
	 */
	public abstract void setMinimumSize(WLength width, WLength height);

	/**
	 * Returns the minimum width.
	 * 
	 * Return the minimum width set for this widget with
	 * {@link WWidget#setMinimumSize(WLength width, WLength height)}.
	 * <p>
	 * 
	 * @see WWidget#setMinimumSize(WLength width, WLength height)
	 * @see WWidget#getMinimumHeight()
	 */
	public abstract WLength getMinimumWidth();

	/**
	 * Returns the minimum height.
	 * 
	 * Return the minmum height set for this widget with
	 * {@link WWidget#setMinimumSize(WLength width, WLength height)}.
	 * <p>
	 * 
	 * @see WWidget#setMinimumSize(WLength width, WLength height)
	 * @see WWidget#getMinimumWidth()
	 */
	public abstract WLength getMinimumHeight();

	/**
	 * Set a maximum size.
	 * 
	 * Specify a minimum size for this widget.
	 * <p>
	 * 
	 * @see WWidget#resize(WLength width, WLength height)
	 * @see WWidget#getMaximumWidth()
	 * @see WWidget#getMaximumHeight()
	 */
	public abstract void setMaximumSize(WLength width, WLength height);

	/**
	 * Returns the maximum width.
	 * 
	 * Return the maximum width set for this widget with
	 * {@link WWidget#setMaximumSize(WLength width, WLength height)}.
	 * <p>
	 * 
	 * @see WWidget#setMaximumSize(WLength width, WLength height)
	 * @see WWidget#getMaximumHeight()
	 */
	public abstract WLength getMaximumWidth();

	/**
	 * Returns the maximum height.
	 * 
	 * Return the minmum height set for this widget with
	 * {@link WWidget#setMaximumSize(WLength width, WLength height)}.
	 * <p>
	 * 
	 * @see WWidget#setMaximumSize(WLength width, WLength height)
	 * @see WWidget#getMaximumWidth()
	 */
	public abstract WLength getMaximumHeight();

	/**
	 * Set the line height for contained text.
	 */
	public abstract void setLineHeight(WLength height);

	/**
	 * Return the line height for contained text.
	 * 
	 * sa {@link WWidget#setLineHeight(WLength height)}
	 */
	public abstract WLength getLineHeight();

	/**
	 * Specify a side to which the {@link WWidget} must float.
	 * 
	 * This only applies to widgets with a {@link PositionScheme#Static Static}
	 * {@link WWidget#getPositionScheme()}.
	 * <p>
	 * It specifies if the widget must be positioned on one of the sides of the
	 * parent widget, at the current line. A typical use is to position images
	 * within text. Valid values for Side or , {@link Side#Left Left} or
	 * {@link Side#Right Right}.
	 * <p>
	 * This applies to CSS-based layout.
	 */
	public abstract void setFloatSide(Side s);

	/**
	 * Return the float side.
	 * 
	 * @see WWidget#setFloatSide(Side s)
	 */
	public abstract Side getFloatSide();

	/**
	 * Set the sides that should be cleared of floats.
	 * 
	 * This pushes the widget down until it is not surrounded by floats at the
	 * <i>sides</i> (which may be a logical OR of {@link Side#Left Left} and
	 * {@link Side#Right Right}.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setFloatSide(Side s)
	 */
	public abstract void setClearSides(EnumSet<Side> sides);

	public final void setClearSides(Side side, Side... sides) {
		setClearSides(EnumSet.of(side, sides));
	}

	/**
	 * Returns the sides that should remain empty.
	 * 
	 * @see WWidget#setClearSides(EnumSet sides)
	 */
	public abstract EnumSet<Side> getClearSides();

	/**
	 * Set margins around the widget.
	 * 
	 * Setting margin has the effect of adding a distance between the widget and
	 * surrounding widgets. The default margin (with an automatic length) is
	 * zero.
	 * <p>
	 * Use any combination of {@link Side#Left Left}, {@link Side#Right Right},
	 * {@link Side#Bottom Bottom}, or {@link Side#Top Top}.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#getMargin(Side side)
	 */
	public abstract void setMargin(WLength margin, EnumSet<Side> sides);

	public final void setMargin(WLength margin, Side side, Side... sides) {
		setMargin(margin, EnumSet.of(side, sides));
	}

	public final void setMargin(WLength margin) {
		setMargin(margin, Side.All);
	}

	public void setMargin(int pixels, EnumSet<Side> sides) {
		this.setMargin(new WLength(pixels), sides);
	}

	public final void setMargin(int pixels, Side side, Side... sides) {
		setMargin(pixels, EnumSet.of(side, sides));
	}

	public final void setMargin(int pixels) {
		setMargin(pixels, Side.All);
	}

	/**
	 * Returns the margin set for that side.
	 * 
	 * This applies to CSS-based layout.
	 * <p>
	 */
	public abstract WLength getMargin(Side side);

	/**
	 * Set whether the widget must be hidden.
	 * 
	 * Hide or show the widget (including all its descendant widgets).
	 * setHidden(false) will show this widget and all child widgets that are not
	 * hidden.
	 * <p>
	 * 
	 * @see WWidget#hide()
	 * @see WWidget#show()
	 */
	public abstract void setHidden(boolean hidden);

	/**
	 * Return whether this widget is set hidden.
	 * 
	 * A widget that is not hidden may still be not visible when one of its
	 * ancestor widgets are hidden.
	 * <p>
	 * 
	 * @see WWidget#setHidden(boolean hidden)
	 */
	public abstract boolean isHidden();

	/**
	 * Let the widget overlay other sibling widgets.
	 * 
	 * A widget that {@link WWidget#isPopup()} will be rendered on top of any
	 * other sibling widget contained within the same parent (including other
	 * popup widgets previously added to the container).
	 * <p>
	 * This will only have an effect when the widgetis either
	 * {@link PositionScheme#Absolute Absolute} or {@link PositionScheme#Fixed
	 * Fixed} {@link WWidget#getPositionScheme()}.
	 * <p>
	 * This applies to CSS-based layout.
	 */
	public abstract void setPopup(boolean popup);

	/**
	 * Returns whether this widget is overlayed.
	 * 
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setPopup(boolean popup)
	 */
	public abstract boolean isPopup();

	/**
	 * Set whether this widget is inline or stacked.
	 * 
	 * This option changes whether this widget must be rendered in-line with
	 * sibling widgets wrapping at the right edge of the parent container (like
	 * text), or whether this widget must be stacked vertically with sibling
	 * widgets. Depending on the widget type, the default value is inline (such
	 * as for example for {@link WText}, or {@link WPushButton}), or stacked
	 * (such as for example for {@link WTable}).
	 * <p>
	 * This applies to CSS-based layout.
	 */
	public abstract void setInline(boolean inlined);

	/**
	 * Returns whether this widget is inline or stacked.
	 * 
	 * @see WWidget#setInline(boolean inlined)
	 */
	public abstract boolean isInline();

	public abstract void setDecorationStyle(WCssDecorationStyle style);

	/**
	 * Returns the decoration style of this widget.
	 * 
	 * This groups all decorative aspects of the widget, which do not affect the
	 * widget layout (except for the border properties which may behave like
	 * extra margin around the widget).
	 */
	public abstract WCssDecorationStyle getDecorationStyle();

	/**
	 * Sets a style class.
	 * 
	 * The CSS style class works in conjunction with style sheet, and provides a
	 * flexible way to provide many widgets the same markup.
	 * <p>
	 * Setting an empty string removes the style class.
	 * <p>
	 * 
	 * @see WApplication#getStyleSheet()
	 */
	public abstract void setStyleClass(String styleClass);

	/**
	 * Returns the style class.
	 * 
	 * @see WWidget#setStyleClass(String styleClass)
	 */
	public abstract String getStyleClass();

	/**
	 * Set the vertical alignment of this (inline) widget.
	 * 
	 * This only applies to inline widgets, and determines how to position
	 * itself on the current line, with respect to sibling inline widgets.
	 * <p>
	 * This applies to CSS-based layout.
	 */
	public abstract void setVerticalAlignment(AlignmentFlag alignment,
			WLength length);

	public final void setVerticalAlignment(AlignmentFlag alignment) {
		setVerticalAlignment(alignment, WLength.Auto);
	}

	/**
	 * Returns the vertical alignment.
	 * 
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setVerticalAlignment(AlignmentFlag alignment, WLength
	 *      length)
	 */
	public abstract AlignmentFlag getVerticalAlignment();

	/**
	 * Returns the fixed vertical alignment that was set.
	 * 
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setVerticalAlignment(AlignmentFlag alignment, WLength
	 *      length)
	 */
	public abstract WLength getVerticalAlignmentLength();

	/**
	 * Sets a tooltip.
	 * 
	 * The tooltip is displayed when the cursor hovers over the widget.
	 */
	public abstract void setToolTip(CharSequence text);

	/**
	 * Returns the tooltip text.
	 */
	public abstract WString getToolTip();

	/**
	 * Refresh the widget.
	 * 
	 * The refresh method is invoked when the locale is changed using
	 * {@link WApplication#setLocale(String locale)} or when the user hit the
	 * refresh button.
	 * <p>
	 * The widget must actualize its contents in response.
	 */
	public abstract void refresh();

	/**
	 * A JavaScript expression that returns the corresponding DOM node.
	 * 
	 * You may want to use this in conjunction with {@link JSlot} or
	 * {@link WApplication#doJavaScript(String javascript, boolean afterLoaded)}
	 * in custom JavaScript code.
	 */
	public String getJsRef() {
		return "Wt2_99_2.getElement('" + this.getFormName() + "')";
	}

	/**
	 * Set an attribute value.
	 * 
	 * Associate an extra attribute with this widget, with the given value. This
	 * is only useful when processing dom nodes associated with widgets in
	 * custom JavaScript code.
	 * <p>
	 * 
	 * @see JSlot
	 * @see WApplication#doJavaScript(String javascript, boolean afterLoaded)
	 */
	public abstract void setAttributeValue(String name, String value);

	/**
	 * Short hand for {@link WString#tr()}.
	 * 
	 * Create a message with the given key.
	 */
	public static WString tr(String key) {
		return WString.tr(key);
	}

	/**
	 * Load content just before the widget&apos;s content is rendered.
	 * 
	 * As soon as a widget is inserted into the widget hierarchy, it is
	 * rendered. Visible widgets are rendered immediately, and invisible widgets
	 * in the back-ground. This method is called when the widget is directly or
	 * indirectly inserted into the widget tree.
	 * <p>
	 * The default implementation simply propagates the load signal to its
	 * children. You may want to override this method to load resource-intensive
	 * content only when the widget is loaded into the browser.
	 */
	public abstract void load();

	/**
	 * Return if this widget has been loaded.
	 * 
	 * @see WWidget#load()
	 */
	public abstract boolean isLoaded();

	/**
	 * Set a mime type to be accepted for dropping.
	 * 
	 * You may specify a style class that is applied to the widget when the
	 * specified mimetype hovers on top of it.
	 * <p>
	 * 
	 * @see WWidget#dropEvent(WDropEvent event)
	 * @see WInteractWidget#setDraggable(String mimeType, WWidget dragWidget,
	 *      boolean isDragWidgetOnly, WObject sourceObject)
	 * @see WWidget#stopAcceptDrops(String mimeType)
	 */
	public void acceptDrops(String mimeType, String hoverStyleClass) {
		WWebWidget thisWebWidget = this.getWebWidget();
		if (thisWebWidget.setAcceptDropsImpl(mimeType, true, hoverStyleClass)) {
			thisWebWidget.otherImpl_.dropSignal_.addListener(this,
					new Signal3.Listener<String, String, WMouseEvent>() {
						public void trigger(String e1, String e2, WMouseEvent e3) {
							WWidget.this.getDrop(e1, e2, e3);
						}
					});
		}
	}

	public final void acceptDrops(String mimeType) {
		acceptDrops(mimeType, "");
	}

	/**
	 * No longer accept a mime type for dropping.
	 * 
	 * @see WWidget#acceptDrops(String mimeType, String hoverStyleClass)
	 */
	public void stopAcceptDrops(String mimeType) {
		WWebWidget thisWebWidget = this.getWebWidget();
		thisWebWidget.setAcceptDropsImpl(mimeType, false, "");
	}

	/**
	 * Set the CSS Id.
	 * 
	 * Sets a custom Id. Note that the Id must be unique across the whole widget
	 * tree, can only be set right after construction and cannot be changed.
	 * <p>
	 * 
	 * @see WObject#getId()
	 */
	public abstract void setId(String id);

	/**
	 * Stream the (X)HTML representation.
	 * 
	 * Streams the widget as UTF8-encoded (HTML-compatible) XHTML.
	 * <p>
	 * This may be useful as a debugging tool for the web-savvy, or in other
	 * rare situations. Usually, you will not deal directly with HTML, and
	 * calling this method on a widget that is rendered may interfere with the
	 * library keeping track of changes to the widget.
	 */
	public void htmlText(Writer out) {
		DomElement element = this.getWebWidget().createSDomElement(
				WApplication.instance());
		List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
		EscapeOStream sout = new EscapeOStream(out);
		element.asHTML(sout, timeouts);
		/* delete element */;
	}

	/**
	 * Set as selectable.
	 * 
	 * When a widget is made unselectable, a selection of text (or images) will
	 * not be visible (but may still be possible).
	 * <p>
	 * By default, the widget inherits this property from its parent, and this
	 * property propagates to all children. The top level container (
	 * {@link WApplication#getRoot()}) selectable by default.
	 */
	public abstract void setSelectable(boolean selectable);

	public String getInlineCssStyle() {
		WWebWidget ww = this.getWebWidget();
		DomElement e = DomElement.getForUpdate(ww, ww.getDomElementType());
		ww.updateDom(e, true);
		String result = e.getCssStyle();
		/* delete e */;
		return result;
	}

	public String createJavaScript(StringWriter js, String insertJS) {
		DomElement de = this.getWebWidget().createSDomElement(
				WApplication.instance());
		String var = de.asJavaScript(js, true);
		if (insertJS.length() != 0) {
			js.append(insertJS).append(var).append(");");
		}
		de.asJavaScript(js, false);
		/* delete de */;
		return var;
	}

	public Signal1<WWidget> destroyed;

	/**
	 * Hide this {@link WWidget}.
	 * 
	 * @see WWidget#setHidden(boolean hidden)
	 */
	public void hide() {
		this.wasHidden_ = this.isHidden();
		this.setHidden(true);
	}

	/**
	 * Show this {@link WWidget}.
	 * 
	 * @see WWidget#setHidden(boolean hidden)
	 */
	public void show() {
		this.wasHidden_ = this.isHidden();
		this.setHidden(false);
	}

	abstract DomElement createSDomElement(WApplication app);

	/**
	 * Create a widget with a given parent.
	 * 
	 * If a parent container is specified, the widget is added to the container,
	 * using {@link WContainerWidget#addWidget(WWidget widget)}.
	 */
	protected WWidget(WContainerWidget parent) {
		super((WObject) null);
		this.destroyed = new Signal1<WWidget>();
		this.eventSignals_ = new LinkedList<AbstractEventSignal>();
		this.needRerender_ = true;
	}

	protected WWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Handle a drop event.
	 * 
	 * Reimplement this method to handle a drop events for mime types you
	 * declared to accept using acceptDrops.
	 * <p>
	 * The default implementation simply completes the drag and drop operation
	 * as if nothing happened.
	 * <p>
	 * 
	 * @see WWidget#acceptDrops(String mimeType, String hoverStyleClass)
	 * @see WInteractWidget#setDraggable(String mimeType, WWidget dragWidget,
	 *      boolean isDragWidgetOnly, WObject sourceObject)
	 */
	protected void dropEvent(WDropEvent event) {
	}

	protected void getDrop(String sourceId, String mimeType, WMouseEvent event) {
		WDropEvent e = new WDropEvent(WApplication.instance().decodeObject(
				sourceId), mimeType, event);
		this.dropEvent(e);
	}

	protected abstract void addChild(WWidget child);

	protected abstract void removeChild(WWidget child);

	protected abstract void setHideWithOffsets(boolean how);

	protected final void setHideWithOffsets() {
		setHideWithOffsets(true);
	}

	protected void setParent(WWidget p) {
		if (this.getParent() != null) {
			this.getParent().removeChild(this);
		}
		if (p != null) {
			p.addChild(this);
		}
	}

	protected abstract boolean isVisible();

	protected abstract boolean isStubbed();

	protected void render() {
		this.renderOk();
	}

	protected WWidget getAdam() {
		WWidget p = this.getParent();
		return p != null ? p.getAdam() : this;
	}

	void setLayout(WLayout layout) {
		layout.setParent(this);
	}

	protected LinkedList<AbstractEventSignal> EventSignalList;

	protected void addEventSignal(AbstractEventSignal s) {
		this.eventSignals_.offer(s);
	}

	protected AbstractEventSignal getEventSignal(String name) {
		for (Iterator<AbstractEventSignal> i_it = this.eventSignals_.iterator(); i_it
				.hasNext();) {
			AbstractEventSignal i = i_it.next();
			AbstractEventSignal s = i;
			if (s.getName() == name) {
				return s;
			}
		}
		return null;
	}

	protected LinkedList<AbstractEventSignal> eventSignals() {
		return this.eventSignals_;
	}

	// protected AbstractEventSignal.LearningListener
	// getStateless(<pointertomember or dependentsizedarray>
	// methodpointertomember or dependentsizedarray>) ;
	protected void renderOk() {
		if (this.needRerender_) {
			this.needRerender_ = false;
			WApplication.instance().getSession().getRenderer().doneUpdate(this);
		}
	}

	protected void askRerender(boolean laterOnly) {
		if (!this.needRerender_) {
			this.needRerender_ = true;
			WApplication.instance().getSession().getRenderer().needUpdate(this,
					laterOnly);
		}
	}

	protected final void askRerender() {
		askRerender(false);
	}

	protected boolean needsRerender() {
		return this.needRerender_;
	}

	protected abstract void getSDomChanges(List<DomElement> result,
			WApplication app);

	protected abstract boolean needsToBeRendered();

	private LinkedList<AbstractEventSignal> eventSignals_;
	private boolean wasHidden_;
	private boolean needRerender_;

	private void undoHideShow() {
		this.setHidden(this.wasHidden_);
	}

	abstract WWebWidget getWebWidget();

	WLayoutItemImpl createLayoutItemImpl(WLayoutItem layoutItem) {
		throw new WtException(
				"WWidget::setLayout(): widget does not support layout managers");
	}

	WLayout getLayout() {
		return null;
	}
}
