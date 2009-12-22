/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The abstract base class for a user-interface component
 * <p>
 * 
 * The user-interface is organized in a tree structure, in which each nodes is a
 * widgets. All widgets, except for the application&apos;s root widget and
 * dialogs, have a parent which is usually a {@link WContainerWidget}.
 * <p>
 * This is an abstract base class. Implementations derive either from the
 * abstract {@link WWebWidget} (for basic widgets with a direct HTML
 * counter-part) or from the abstract {@link WCompositeWidget} (for anything
 * else). To add a WWebWidget directly to a parent container, either specify the
 * parent in the constructor (which is conventionally the last constructor
 * argument), or add the widget to the parent using
 * {@link WContainerWidget#addWidget(WWidget widget)
 * WContainerWidget#addWidget()}. Alternatively, you may add the widget to a
 * layout manager set for a WContainerWidget.
 * <p>
 * A widget provides methods to manage its decorative style base on CSS. It also
 * provides access to CSS-based layout, which you may not use when the widget is
 * not inserted into a layout manager.
 */
public abstract class WWidget extends WObject {
	/**
	 * Destructor.
	 * <p>
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
			;
		}
		this.renderOk();
	}

	/**
	 * Returns the parent widget.
	 * <p>
	 * With a few exceptions, the parent is a {@link WContainerWidget}, and has
	 * been set implicitly when adding the widget to a container using
	 * {@link WContainerWidget#addWidget(WWidget widget)
	 * WContainerWidget#addWidget()}, by passing a container as a parent to the
	 * constructor, or by inserting the widget into a layout manager.
	 */
	public WWidget getParent() {
		return ((super.getParent()) instanceof WWidget ? (WWidget) (super
				.getParent()) : null);
	}

	/**
	 * Sets the CSS position scheme.
	 * <p>
	 * Establishes how the widget must be layed-out relative to its siblings.
	 * The default position scheme is Static.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see PositionScheme
	 * @see WWidget#getPositionScheme()
	 */
	public abstract void setPositionScheme(PositionScheme scheme);

	/**
	 * Returns the CSS position scheme.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see PositionScheme
	 * @see WWidget#setPositionScheme(PositionScheme scheme)
	 */
	public abstract PositionScheme getPositionScheme();

	/**
	 * Sets CSS offsets for a non-statically positioned widget.
	 * <p>
	 * The argument <code>sides</code> may be a combination of {@link Side#Left
	 * Left}, {@link Side#Right Right}, {@link Side#Top Top}, and
	 * {@link Side#Bottom Bottom}.
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

	/**
	 * Sets CSS offsets for a non-statically positioned widget.
	 * <p>
	 * Calls {@link #setOffsets(WLength offset, EnumSet sides)
	 * setOffsets(offset, EnumSet.of(side, sides))}
	 */
	public final void setOffsets(WLength offset, Side side, Side... sides) {
		setOffsets(offset, EnumSet.of(side, sides));
	}

	/**
	 * Sets CSS offsets for a non-statically positioned widget.
	 * <p>
	 * Calls {@link #setOffsets(WLength offset, EnumSet sides)
	 * setOffsets(offset, Side.All)}
	 */
	public final void setOffsets(WLength offset) {
		setOffsets(offset, Side.All);
	}

	/**
	 * Sets CSS offsets for a non-statically positioned widget.
	 * <p>
	 * This is a convenience method for applying offsets in pixel units.
	 * <p>
	 * 
	 * @see WWidget#setOffsets(WLength offset, EnumSet sides)
	 */
	public void setOffsets(int pixels, EnumSet<Side> sides) {
		this.setOffsets(new WLength(pixels), sides);
	}

	/**
	 * Sets CSS offsets for a non-statically positioned widget.
	 * <p>
	 * Calls {@link #setOffsets(int pixels, EnumSet sides) setOffsets(pixels,
	 * EnumSet.of(side, sides))}
	 */
	public final void setOffsets(int pixels, Side side, Side... sides) {
		setOffsets(pixels, EnumSet.of(side, sides));
	}

	/**
	 * Sets CSS offsets for a non-statically positioned widget.
	 * <p>
	 * Calls {@link #setOffsets(int pixels, EnumSet sides) setOffsets(pixels,
	 * Side.All)}
	 */
	public final void setOffsets(int pixels) {
		setOffsets(pixels, Side.All);
	}

	/**
	 * Returns a CSS offset.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setOffsets(WLength offset, EnumSet sides)
	 */
	public abstract WLength getOffset(Side side);

	/**
	 * Resizes the widget.
	 * <p>
	 * Specify a new size for this widget, by specifying width and height. By
	 * default a widget has automatic width and height, see
	 * {@link WLength#isAuto() WLength#isAuto()}.
	 * <p>
	 * This applies to CSS-based layout, and only
	 * {@link WWidget#setInline(boolean inlined) block} widgets can be given a
	 * size reliably.
	 * <p>
	 * 
	 * @see WWidget#getWidth()
	 * @see WWidget#getHeight()
	 */
	public abstract void resize(WLength width, WLength height);

	/**
	 * Resizes the widget.
	 * <p>
	 * This is a convenience method for resizing a widget using pixel units.
	 * <p>
	 * 
	 * @see WWidget#resize(WLength width, WLength height)
	 */
	public void resize(int widthPixels, int heightPixels) {
		this.resize(new WLength(widthPixels), new WLength(heightPixels));
	}

	/**
	 * Returns the width.
	 * <p>
	 * Returns the width set for this widget. This is not a calculated width,
	 * based on layout, but the width as specified with
	 * {@link WWidget#resize(WLength width, WLength height) resize()}.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#resize(WLength width, WLength height)
	 * @see WWidget#getHeight()
	 */
	public abstract WLength getWidth();

	/**
	 * Returns the height.
	 * <p>
	 * Returns the height set for this widget. This is not a calculated height,
	 * based on layout, but the height as specified previously with
	 * {@link WWidget#resize(WLength width, WLength height) resize()}.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#resize(WLength width, WLength height)
	 * @see WWidget#getWidth()
	 */
	public abstract WLength getHeight();

	/**
	 * Sets a minimum size.
	 * <p>
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
	 * <p>
	 * Returns the minimum width set for this widget with
	 * {@link WWidget#setMinimumSize(WLength width, WLength height)
	 * setMinimumSize()}.
	 * <p>
	 * 
	 * @see WWidget#setMinimumSize(WLength width, WLength height)
	 * @see WWidget#getMinimumHeight()
	 */
	public abstract WLength getMinimumWidth();

	/**
	 * Returns the minimum height.
	 * <p>
	 * Returns the minmum height set for this widget with
	 * {@link WWidget#setMinimumSize(WLength width, WLength height)
	 * setMinimumSize()}.
	 * <p>
	 * 
	 * @see WWidget#setMinimumSize(WLength width, WLength height)
	 * @see WWidget#getMinimumWidth()
	 */
	public abstract WLength getMinimumHeight();

	/**
	 * Sets a maximum size.
	 * <p>
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
	 * <p>
	 * Returns the maximum width set for this widget with
	 * {@link WWidget#setMaximumSize(WLength width, WLength height)
	 * setMaximumSize()}.
	 * <p>
	 * 
	 * @see WWidget#setMaximumSize(WLength width, WLength height)
	 * @see WWidget#getMaximumHeight()
	 */
	public abstract WLength getMaximumWidth();

	/**
	 * Returns the maximum height.
	 * <p>
	 * Returns the minmum height set for this widget with
	 * {@link WWidget#setMaximumSize(WLength width, WLength height)
	 * setMaximumSize()}.
	 * <p>
	 * 
	 * @see WWidget#setMaximumSize(WLength width, WLength height)
	 * @see WWidget#getMaximumWidth()
	 */
	public abstract WLength getMaximumHeight();

	/**
	 * Sets the CSS line height for contained text.
	 */
	public abstract void setLineHeight(WLength height);

	/**
	 * Returns the CSS line height for contained text.
	 * <p>
	 * sa {@link WWidget#setLineHeight(WLength height) setLineHeight()}
	 */
	public abstract WLength getLineHeight();

	/**
	 * Specifies a CSS float side.
	 * <p>
	 * This only applies to widgets with a {@link PositionScheme#Static Static}
	 * {@link WWidget#getPositionScheme() getPositionScheme()}.
	 * <p>
	 * This lets the widget float to one of the sides of the parent widget, at
	 * the current line. A typical use is to position images within text. Valid
	 * values for Side or {@link Side#None None} , {@link Side#Left Left} or
	 * {@link Side#Right Right}.
	 * <p>
	 * This applies to CSS-based layout.
	 */
	public abstract void setFloatSide(Side s);

	/**
	 * Returns the CSS float side.
	 * <p>
	 * 
	 * @see WWidget#setFloatSide(Side s)
	 */
	public abstract Side getFloatSide();

	/**
	 * Sets the sides that should be cleared of floats.
	 * <p>
	 * This pushes the widget down until it is not surrounded by floats at the
	 * <code>sides</code> (which may be a combination of {@link Side#Left Left}
	 * and {@link Side#Right Right}.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setFloatSide(Side s)
	 */
	public abstract void setClearSides(EnumSet<Side> sides);

	/**
	 * Sets the sides that should be cleared of floats.
	 * <p>
	 * Calls {@link #setClearSides(EnumSet sides) setClearSides(EnumSet.of(side,
	 * sides))}
	 */
	public final void setClearSides(Side side, Side... sides) {
		setClearSides(EnumSet.of(side, sides));
	}

	/**
	 * Returns the sides that should remain empty.
	 * <p>
	 * 
	 * @see WWidget#setClearSides(EnumSet sides)
	 */
	public abstract EnumSet<Side> getClearSides();

	/**
	 * Sets CSS margins around the widget.
	 * <p>
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

	/**
	 * Sets CSS margins around the widget.
	 * <p>
	 * Calls {@link #setMargin(WLength margin, EnumSet sides) setMargin(margin,
	 * EnumSet.of(side, sides))}
	 */
	public final void setMargin(WLength margin, Side side, Side... sides) {
		setMargin(margin, EnumSet.of(side, sides));
	}

	/**
	 * Sets CSS margins around the widget.
	 * <p>
	 * Calls {@link #setMargin(WLength margin, EnumSet sides) setMargin(margin,
	 * Side.All)}
	 */
	public final void setMargin(WLength margin) {
		setMargin(margin, Side.All);
	}

	/**
	 * Sets CSS margins around the widget.
	 * <p>
	 * This is a convenience method for setting margins in pixel units.
	 * <p>
	 * 
	 * @see WWidget#setMargin(WLength margin, EnumSet sides)
	 */
	public void setMargin(int pixels, EnumSet<Side> sides) {
		this.setMargin(new WLength(pixels), sides);
	}

	/**
	 * Sets CSS margins around the widget.
	 * <p>
	 * Calls {@link #setMargin(int pixels, EnumSet sides) setMargin(pixels,
	 * EnumSet.of(side, sides))}
	 */
	public final void setMargin(int pixels, Side side, Side... sides) {
		setMargin(pixels, EnumSet.of(side, sides));
	}

	/**
	 * Sets CSS margins around the widget.
	 * <p>
	 * Calls {@link #setMargin(int pixels, EnumSet sides) setMargin(pixels,
	 * Side.All)}
	 */
	public final void setMargin(int pixels) {
		setMargin(pixels, Side.All);
	}

	/**
	 * Returns a CSS margin set.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setMargin(WLength margin, EnumSet sides)
	 */
	public abstract WLength getMargin(Side side);

	/**
	 * Sets whether the widget keeps its geometry when hidden.
	 * <p>
	 * Normally, a widget that is hidden will no longer occupy space, causing a
	 * reflow of sibling widgets. Using this method you may change this behavior
	 * to keep an (open) space when hidden.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Currently you can only set this before initial
	 * rendering.</i>
	 * </p>
	 * 
	 * @see WWidget#setHidden(boolean hidden)
	 */
	public abstract void setHiddenKeepsGeometry(boolean enabled);

	/**
	 * Returns whether the widget keeps its geometry when hidden.
	 * <p>
	 * 
	 * @see WWidget#setHiddenKeepsGeometry(boolean enabled)
	 */
	public abstract boolean isHiddenKeepsGeometry();

	/**
	 * Sets whether the widget is hidden.
	 * <p>
	 * Hides or show the widget (including all its descendant widgets).
	 * setHidden(false) will show this widget and all descendant widgets that
	 * are not hidden. A widget is only visible if it and all its ancestors in
	 * the widget tree are visible, which may be checked using
	 * {@link WWidget#isVisible() isVisible()}.
	 * <p>
	 * 
	 * @see WWidget#hide()
	 * @see WWidget#show()
	 */
	public abstract void setHidden(boolean hidden);

	/**
	 * Returns whether the widget is set hidden.
	 * <p>
	 * A widget that is not hidden may still be not visible when one of its
	 * ancestor widgets is hidden. Use {@link WWidget#isVisible() isVisible()}
	 * to check the visibility of a widget.
	 * <p>
	 * 
	 * @see WWidget#setHidden(boolean hidden)
	 * @see WWidget#isVisible()
	 */
	public abstract boolean isHidden();

	/**
	 * Returns whether the widget is visible.
	 * <p>
	 * A widget is visible if it is not hidden, and none of its ancestors are
	 * hidden. This method returns the true visibility, while
	 * {@link WWidget#isHidden() isHidden()} returns whether a widget has been
	 * explicitly hidden.
	 * <p>
	 * Note that a widget may be at the same time not hidden, and not visible,
	 * in case one of its ancestors was hidden.
	 * <p>
	 * 
	 * @see WWidget#isHidden()
	 */
	abstract boolean isVisible();

	/**
	 * Sets whether the widget is disabled.
	 * <p>
	 * Enables or disables the widget (including all its descendant widgets).
	 * setDisabled(false) will enable this widget and all descendant widgets
	 * that are not disabled. A widget is only enabled if it and all its
	 * ancestors in the widget tree are disabled.
	 * <p>
	 * 
	 * @see WWidget#disable()
	 * @see WWidget#enable()
	 */
	public abstract void setDisabled(boolean disabled);

	/**
	 * Returns whether the widget is set disabled.
	 * <p>
	 * A widget that is not set disabled may still be disabled when one of its
	 * ancestor widgets is set disabled. Use {@link WWidget#isEnabled()
	 * isEnabled()} to find out whether a widget is enabled.
	 * <p>
	 * 
	 * @see WWidget#setDisabled(boolean disabled)
	 * @see WWidget#isEnabled()
	 */
	public abstract boolean isDisabled();

	/**
	 * Returns whether the widget is enabled.
	 * <p>
	 * A widget is enabled if it is not disabled, and none of its ancestors are
	 * disabled. This method returns whether the widget is rendered as enabled,
	 * while {@link WWidget#isDisabled() isDisabled()} returns whether a widget
	 * has been explicitly disabled.
	 * <p>
	 * Note that a widget may be at the same time not enabled, and not disabled,
	 * in case one of its ancestors was disabled.
	 * <p>
	 * 
	 * @see WWidget#isDisabled()
	 */
	public abstract boolean isEnabled();

	/**
	 * Lets the widget overlay over other sibling widgets.
	 * <p>
	 * A widget that {@link WWidget#isPopup() isPopup()} will be rendered on top
	 * of any other sibling widget contained within the same parent (including
	 * other popup widgets previously added to the container).
	 * <p>
	 * This will only have an effect when the widgetis either
	 * {@link PositionScheme#Absolute Absolute} or {@link PositionScheme#Fixed
	 * Fixed} {@link WWidget#getPositionScheme() getPositionScheme()}.
	 * <p>
	 * This applies to CSS-based layout, and configures the z-index property.
	 */
	public abstract void setPopup(boolean popup);

	/**
	 * Returns whether the widget is overlayed.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setPopup(boolean popup)
	 */
	public abstract boolean isPopup();

	/**
	 * Sets whether the widget is displayed inline or as a block.
	 * <p>
	 * This option changes whether this widget must be rendered in line with
	 * sibling widgets wrapping at the right edge of the parent container (like
	 * text), or whether this widget must be rendered as a rectangular block
	 * that stacks vertically with sibling widgets (unless a CSS float property
	 * is applied). Depending on the widget type, the default value is inline
	 * (such as for example for {@link WText}, or {@link WPushButton}), or block
	 * (such as for example for a {@link WContainerWidget}).
	 * <p>
	 * This applies to CSS-based layout.
	 */
	public abstract void setInline(boolean inlined);

	/**
	 * Returns whether the widget is displayed inline or as block.
	 * <p>
	 * 
	 * @see WWidget#setInline(boolean inlined)
	 */
	public abstract boolean isInline();

	/**
	 * Sets a CSS decoration style.
	 * <p>
	 * This copies the style over its current
	 * {@link WWidget#getDecorationStyle() getDecorationStyle()}
	 */
	public abstract void setDecorationStyle(WCssDecorationStyle style);

	/**
	 * Returns the decoration style of this widget.
	 * <p>
	 * This groups all decorative aspects of the widget, which do not affect the
	 * widget layout (except for the border properties which may behave like
	 * extra margin around the widget).
	 * <p>
	 * When a decoration style has not been previously set, it returns a default
	 * decoration style object.
	 * <p>
	 * 
	 * @see WWidget#setDecorationStyle(WCssDecorationStyle style)
	 */
	public abstract WCssDecorationStyle getDecorationStyle();

	/**
	 * Sets (one or more) CSS style classes.
	 * <p>
	 * You may set one or more space separated style classes. CSS style class
	 * works in conjunction with style sheet, and provides a flexible way to
	 * provide many widgets the same markup.
	 * <p>
	 * Setting an empty string removes the style class(es).
	 * <p>
	 * 
	 * @see WApplication#getStyleSheet()
	 */
	public abstract void setStyleClass(String styleClass);

	/**
	 * Returns the CSS style class.
	 * <p>
	 * 
	 * @see WWidget#setStyleClass(String styleClass)
	 */
	public abstract String getStyleClass();

	/**
	 * Sets the vertical alignment.
	 * <p>
	 * This only applies to inline widgets, and determines how to position
	 * itself on the current line, with respect to sibling inline widgets.
	 * <p>
	 * This applies to CSS-based layout.
	 */
	public abstract void setVerticalAlignment(AlignmentFlag alignment,
			WLength length);

	/**
	 * Sets the vertical alignment.
	 * <p>
	 * Calls
	 * {@link #setVerticalAlignment(AlignmentFlag alignment, WLength length)
	 * setVerticalAlignment(alignment, WLength.Auto)}
	 */
	public final void setVerticalAlignment(AlignmentFlag alignment) {
		setVerticalAlignment(alignment, WLength.Auto);
	}

	/**
	 * Returns the vertical alignment.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setVerticalAlignment(AlignmentFlag alignment, WLength
	 *      length)
	 */
	public abstract AlignmentFlag getVerticalAlignment();

	/**
	 * Returns the fixed vertical alignment that was set.
	 * <p>
	 * This applies to CSS-based layout.
	 * <p>
	 * 
	 * @see WWidget#setVerticalAlignment(AlignmentFlag alignment, WLength
	 *      length)
	 */
	public abstract WLength getVerticalAlignmentLength();

	/**
	 * Sets a tooltip.
	 * <p>
	 * The tooltip is displayed when the cursor hovers over the widget.
	 */
	public abstract void setToolTip(CharSequence text);

	/**
	 * Returns the tooltip.
	 */
	public abstract WString getToolTip();

	/**
	 * Refresh the widget.
	 * <p>
	 * The refresh method is invoked when the locale is changed using
	 * {@link WApplication#setLocale(Locale locale) WApplication#setLocale()} or
	 * when the user hit the refresh button.
	 * <p>
	 * The widget must actualize its contents in response.
	 */
	public abstract void refresh();

	/**
	 * Returns a JavaScript expression to the corresponding DOM node.
	 * <p>
	 * You may want to use this in conjunction with {@link JSlot} or
	 * {@link WApplication#doJavaScript(String javascript, boolean afterLoaded)
	 * WApplication#doJavaScript()} in custom JavaScript code.
	 * <p>
	 * 
	 * @see WWidget#isRendered()
	 */
	public String getJsRef() {
		return "Wt3_1_0.getElement('" + this.getId() + "')";
	}

	/**
	 * Sets an attribute value.
	 * <p>
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
	 * Returns an attribute value.
	 * <p>
	 * 
	 * @see WWidget#setAttributeValue(String name, String value)
	 */
	public abstract String getAttributeValue(String name);

	/**
	 * Short hand for {@link WString#tr(String key) WString#tr()}.
	 * <p>
	 * Creates a localized string with the given key.
	 */
	public static WString tr(String key) {
		return WString.tr(key);
	}

	/**
	 * Loads content just before the widget is used.
	 * <p>
	 * When the widget is inserted in the widget hierarchy, this method is
	 * called. Widgets that get inserted in the widget hierarchy will be
	 * rendered. Visible widgets are rendered immediately, and invisible widgets
	 * in the back-ground (or not for a plain HTML session). This method is
	 * called when the widget is directly or indirectly inserted into the widget
	 * tree.
	 * <p>
	 * The default implementation simply propagates the load signal to its
	 * children. You may want to override this method to delay loading of
	 * resource-intensive contents.
	 */
	public abstract void load();

	/**
	 * Returns whether this widget has been loaded.
	 * <p>
	 * 
	 * @see WWidget#load()
	 */
	public abstract boolean isLoaded();

	/**
	 * Sets a mime type to be accepted for dropping.
	 * <p>
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

	/**
	 * Sets a mime type to be accepted for dropping.
	 * <p>
	 * Calls {@link #acceptDrops(String mimeType, String hoverStyleClass)
	 * acceptDrops(mimeType, "")}
	 */
	public final void acceptDrops(String mimeType) {
		acceptDrops(mimeType, "");
	}

	/**
	 * Indicates that a mime type is no longer accepted for dropping.
	 * <p>
	 * 
	 * @see WWidget#acceptDrops(String mimeType, String hoverStyleClass)
	 */
	public void stopAcceptDrops(String mimeType) {
		WWebWidget thisWebWidget = this.getWebWidget();
		thisWebWidget.setAcceptDropsImpl(mimeType, false, "");
	}

	/**
	 * Sets the CSS Id.
	 * <p>
	 * Sets a custom Id. Note that the Id must be unique across the whole widget
	 * tree, can only be set right after construction and cannot be changed.
	 * This is mostly useful for in tests using a test plan that manipulates DOM
	 * elements by Id.
	 * <p>
	 * By default, auto-generated id&apos;s are used.
	 * <p>
	 * 
	 * @see WObject#getId()
	 */
	public abstract void setId(String id);

	/**
	 * Finds a descendend widget by name.
	 * <p>
	 * 
	 * @see WObject#setObjectName(String name)
	 */
	public abstract WWidget find(String name);

	/**
	 * Streams the (X)HTML representation.
	 * <p>
	 * Streams the widget as UTF8-encoded (HTML-compatible) XHTML.
	 * <p>
	 * This may be useful as a debugging tool for the web-savvy, or in other
	 * rare situations. Usually, you will not deal directly with HTML, and
	 * calling this method on a widget that is rendered may interfere with the
	 * library keeping track of changes to the widget.
	 */
	public void htmlText(Writer out) {
		DomElement element = this.getWebWidget().createSDomElement(
				WApplication.getInstance());
		List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
		EscapeOStream sout = new EscapeOStream(out);
		element.asHTML(sout, timeouts);
		;
	}

	/**
	 * Sets as selectable.
	 * <p>
	 * When a widget is made unselectable, a selection of text (or images) will
	 * not be visible (but may still be possible).
	 * <p>
	 * By default, the widget inherits this property from its parent, and this
	 * property propagates to all children. The top level container (
	 * {@link WApplication#getRoot() WApplication#getRoot()}) selectable by
	 * default.
	 */
	public abstract void setSelectable(boolean selectable);

	/**
	 * Returns whether the widget is rendered.
	 * <p>
	 * 
	 * @see WWidget#getJsRef()
	 */
	public boolean isRendered() {
		WWidget self = this;
		return self.getWebWidget().isRendered();
	}

	String getInlineCssStyle() {
		WWebWidget ww = this.getWebWidget();
		DomElement e = DomElement.getForUpdate(ww, ww.getDomElementType());
		ww.updateDom(e, true);
		String result = e.getCssStyle();
		;
		return result;
	}

	String createJavaScript(StringWriter js, String insertJS) {
		WApplication app = WApplication.getInstance();
		DomElement de = this.getWebWidget().createSDomElement(app);
		String var = de.getCreateVar();
		if (insertJS.length() != 0) {
			insertJS += var + ");";
		}
		de.createElement(js, app, insertJS);
		;
		return var;
	}

	/**
	 * Hides the widget.
	 * <p>
	 * This calls {@link WWidget#setHidden(boolean hidden) setHidden(true)}.
	 */
	public void hide() {
		this.flags_.set(BIT_WAS_HIDDEN, this.isHidden());
		this.setHidden(true);
	}

	/**
	 * Shows the widget.
	 * <p>
	 * This calls {@link WWidget#setHidden(boolean hidden) setHidden(false)}.
	 */
	public void show() {
		this.flags_.set(BIT_WAS_HIDDEN, this.isHidden());
		this.setHidden(false);
	}

	/**
	 * Enables the widget.
	 * <p>
	 * This calls {@link WWidget#setDisabled(boolean disabled)
	 * setDisabled(false)}.
	 */
	public void enable() {
		this.flags_.set(BIT_WAS_DISABLED, this.isDisabled());
		this.setDisabled(false);
	}

	/**
	 * Disable thes widget.
	 * <p>
	 * This calls {@link WWidget#setDisabled(boolean disabled)
	 * setDisabled(true)}.
	 */
	public void disable() {
		this.flags_.set(BIT_WAS_DISABLED, this.isDisabled());
		this.setDisabled(true);
	}

	abstract DomElement createSDomElement(WApplication app);

	/**
	 * Creates a widget.
	 * <p>
	 * When a parent container is specified, the widget is added to the
	 * container, using {@link WContainerWidget#addWidget(WWidget widget)
	 * WContainerWidget#addWidget()}.
	 */
	protected WWidget(WContainerWidget parent) {
		super((WObject) null);
		this.flags_ = new BitSet();
		this.eventSignals_ = new LinkedList<AbstractEventSignal>();
		this.flags_.set(BIT_NEED_RERENDER);
	}

	/**
	 * Creates a widget.
	 * <p>
	 * Calls {@link #WWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	protected WWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Handles a drop event.
	 * <p>
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

	/**
	 * Progresses to an Ajax-enabled widget.
	 * <p>
	 * This method is called when the progressive bootstrap method is used, and
	 * support for AJAX has been detected. The default behavior will upgrade the
	 * widget&apos;s event handling to use AJAX instead of full page reloads,
	 * and propagate the call to its children.
	 * <p>
	 * You may want to reimplement this method if you want to make changes to
	 * widget when AJAX is enabled. You should always call the base
	 * implementation.
	 * <p>
	 * 
	 * @see WApplication#enableAjax()
	 */
	protected abstract void enableAjax();

	/**
	 * Returns the widget&apos;s built-in padding.
	 * <p>
	 * This is used by the layout managers to correct for a built-in padding
	 * which interferes with setting a widget&apos;s width (or height) to 100%.
	 * <p>
	 * A layout manager needs to set the width to 100% only for form widgets (
	 * {@link WTextArea}, {@link WLineEdit}, {@link WComboBox}, etc...).
	 * Therefore, only for those widgets this needs to return the padding (the
	 * default implementation returns 0).
	 * <p>
	 * For form widgets, the padding depends on the specific browser/platform
	 * combination, unless an explicit padding is set for the widget.
	 * <p>
	 * When setting an explicit padding for the widget using a style class, you
	 * will want to reimplement this method to return this padding in case you
	 * want to set the widget inside a layout manager.
	 * <p>
	 * 
	 * @see WWidget#boxBorder(Orientation orientation)
	 */
	protected int boxPadding(Orientation orientation) {
		return 0;
	}

	/**
	 * Returns the widget&apos;s built-in border width.
	 * <p>
	 * This is used by the layout managers to correct for a built-in border
	 * which interferes with setting a widget&apos;s width (or height) to 100%.
	 * <p>
	 * A layout manager needs to set the width to 100% only for form widgets (
	 * {@link WTextArea}, {@link WLineEdit}, {@link WComboBox}, etc...).
	 * Therefore, only for those widgets this needs to return the border width
	 * (the default implementation returns 0).
	 * <p>
	 * For form widgets, the border width depends on the specific
	 * browser/platform combination, unless an explicit border is set for the
	 * widget.
	 * <p>
	 * When setting an explicit border for the widget using a style class, you
	 * will want to reimplement this method to return this border width, in case
	 * you want to set the widget inside a layout manager.
	 * <p>
	 * 
	 * @see WWidget#boxPadding(Orientation orientation)
	 */
	protected int boxBorder(Orientation orientation) {
		return 0;
	}

	void getDrop(String sourceId, String mimeType, WMouseEvent event) {
		WDropEvent e = new WDropEvent(WApplication.getInstance().decodeObject(
				sourceId), mimeType, event);
		this.dropEvent(e);
	}

	abstract void addChild(WWidget child);

	abstract void removeChild(WWidget child);

	abstract void setHideWithOffsets(boolean how);

	final void setHideWithOffsets() {
		setHideWithOffsets(true);
	}

	void setParent(WWidget p) {
		if (this.getParent() != null) {
			this.getParent().removeChild(this);
		}
		if (p != null) {
			p.addChild(this);
		}
	}

	abstract boolean isStubbed();

	void render() {
		this.renderOk();
	}

	WWidget getAdam() {
		WWidget p = this.getParent();
		return p != null ? p.getAdam() : this;
	}

	void setLayout(WLayout layout) {
		layout.setParent(this);
	}

	void addEventSignal(AbstractEventSignal s) {
		this.eventSignals_.offer(s);
	}

	AbstractEventSignal getEventSignal(String name) {
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

	LinkedList<AbstractEventSignal> eventSignals() {
		return this.eventSignals_;
	}

	// protected AbstractEventSignal.LearningListener
	// getStateless(<pointertomember or dependentsizedarray>
	// methodpointertomember or dependentsizedarray>) ;
	void renderOk() {
		if (this.flags_.get(BIT_NEED_RERENDER)) {
			this.flags_.clear(BIT_NEED_RERENDER);
			WApplication.getInstance().getSession().getRenderer().doneUpdate(
					this);
		}
	}

	void askRerender(boolean laterOnly) {
		if (!this.flags_.get(BIT_NEED_RERENDER)) {
			this.flags_.set(BIT_NEED_RERENDER);
			WApplication.getInstance().getSession().getRenderer().needUpdate(
					this, laterOnly);
		}
	}

	final void askRerender() {
		askRerender(false);
	}

	boolean needsRerender() {
		return this.flags_.get(BIT_NEED_RERENDER);
	}

	abstract void getSDomChanges(List<DomElement> result, WApplication app);

	abstract boolean needsToBeRendered();

	private static final int BIT_WAS_HIDDEN = 0;
	private static final int BIT_WAS_DISABLED = 1;
	private static final int BIT_NEED_RERENDER = 2;
	private BitSet flags_;
	private LinkedList<AbstractEventSignal> eventSignals_;

	private void undoHideShow() {
		this.setHidden(this.flags_.get(BIT_WAS_HIDDEN));
	}

	private void undoDisableEnable() {
		this.setDisabled(this.flags_.get(BIT_WAS_DISABLED));
	}

	abstract WWebWidget getWebWidget();

	WLayoutItemImpl createLayoutItemImpl(WLayoutItem item) {
		throw new WtException(
				"WWidget::setLayout(): widget does not support layout managers");
	}

	WLayout getLayout() {
		return null;
	}
}
