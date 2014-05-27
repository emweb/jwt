/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget that hides the implementation of composite widgets.
 * <p>
 * 
 * Composite widgets, built on top of the WebWidgets, should derive from this
 * class, and use {@link WCompositeWidget#setImplementation(WWidget widget)
 * setImplementation()} to set the widget that implements the composite widget
 * (which is typically a {@link WContainerWidget} or a {@link WTable}, or
 * another widget that allows composition, including perhaps another
 * WCompositeWidget).
 * <p>
 * Using this class you can completely hide the implementation of your composite
 * widget, and provide access to only the standard {@link WWidget} methods.
 */
public class WCompositeWidget extends WWidget {
	private static Logger logger = LoggerFactory
			.getLogger(WCompositeWidget.class);

	/**
	 * Creates a WCompositeWidget.
	 * <p>
	 * You need to set an implemetation using
	 * {@link WCompositeWidget#setImplementation(WWidget widget)
	 * setImplementation()} directly after construction.
	 */
	public WCompositeWidget(WContainerWidget parent) {
		super(parent);
		this.impl_ = null;
		if (parent != null) {
			parent.addWidget(this);
		}
	}

	/**
	 * Creates a WCompositeWidget.
	 * <p>
	 * Calls {@link #WCompositeWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WCompositeWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a WCompositeWidget with given implementation.
	 * <p>
	 * 
	 * @see WCompositeWidget#setImplementation(WWidget widget)
	 */
	public WCompositeWidget(WWidget implementation, WContainerWidget parent) {
		super(parent);
		this.impl_ = null;
		if (parent != null) {
			parent.addWidget(this);
		}
		this.setImplementation(implementation);
	}

	public void remove() {
		this.setParentWidget((WWidget) null);
		if (this.impl_ != null)
			this.impl_.remove();
		super.remove();
	}

	public void setObjectName(final String name) {
		this.impl_.setObjectName(name);
	}

	public String getObjectName() {
		return this.impl_.getObjectName();
	}

	public String getId() {
		return this.impl_.getId();
	}

	public void setPositionScheme(PositionScheme scheme) {
		this.impl_.setPositionScheme(scheme);
	}

	public PositionScheme getPositionScheme() {
		return this.impl_.getPositionScheme();
	}

	public void setOffsets(final WLength offset, EnumSet<Side> sides) {
		this.impl_.setOffsets(offset, sides);
	}

	public WLength getOffset(Side s) {
		return this.impl_.getOffset(s);
	}

	public void resize(final WLength width, final WLength height) {
		this.impl_.resize(width, height);
		super.resize(width, height);
	}

	public WLength getWidth() {
		return this.impl_.getWidth();
	}

	public WLength getHeight() {
		return this.impl_.getHeight();
	}

	public void setMinimumSize(final WLength width, final WLength height) {
		this.impl_.setMinimumSize(width, height);
	}

	public WLength getMinimumWidth() {
		return this.impl_.getMinimumWidth();
	}

	public WLength getMinimumHeight() {
		return this.impl_.getMinimumHeight();
	}

	public void setMaximumSize(final WLength width, final WLength height) {
		this.impl_.setMaximumSize(width, height);
	}

	public WLength getMaximumWidth() {
		return this.impl_.getMaximumWidth();
	}

	public WLength getMaximumHeight() {
		return this.impl_.getMaximumHeight();
	}

	public void setLineHeight(final WLength height) {
		this.impl_.setLineHeight(height);
	}

	public WLength getLineHeight() {
		return this.impl_.getLineHeight();
	}

	public void setFloatSide(Side s) {
		this.impl_.setFloatSide(s);
	}

	public Side getFloatSide() {
		return this.impl_.getFloatSide();
	}

	public void setClearSides(EnumSet<Side> sides) {
		this.impl_.setClearSides(sides);
	}

	public EnumSet<Side> getClearSides() {
		return this.impl_.getClearSides();
	}

	public void setMargin(final WLength margin, EnumSet<Side> sides) {
		this.impl_.setMargin(margin, sides);
	}

	public WLength getMargin(Side side) {
		return this.impl_.getMargin(side);
	}

	public void setHiddenKeepsGeometry(boolean enabled) {
		this.impl_.setHiddenKeepsGeometry(enabled);
	}

	public boolean isHiddenKeepsGeometry() {
		return this.impl_.isHiddenKeepsGeometry();
	}

	public void setHidden(boolean hidden, final WAnimation animation) {
		this.impl_.setHidden(hidden, animation);
	}

	public boolean isHidden() {
		return this.impl_.isHidden();
	}

	public boolean isVisible() {
		if (this.isHidden()) {
			return false;
		} else {
			if (this.getParent() != null) {
				return this.getParent().isVisible();
			} else {
				return this.impl_.isRendered();
			}
		}
	}

	public void setDisabled(boolean disabled) {
		this.impl_.setDisabled(disabled);
		this.propagateSetEnabled(!disabled);
	}

	public boolean isDisabled() {
		return this.impl_.isDisabled();
	}

	public boolean isEnabled() {
		if (this.isDisabled()) {
			return false;
		} else {
			if (this.getParent() != null) {
				return this.getParent().isEnabled();
			} else {
				return true;
			}
		}
	}

	public void setPopup(boolean popup) {
		this.impl_.setPopup(popup);
	}

	public boolean isPopup() {
		return this.impl_.isPopup();
	}

	public void setInline(boolean isInline) {
		// this.resetLearnedSlot(WWidget.show);
		this.impl_.setInline(isInline);
	}

	public boolean isInline() {
		return this.impl_.isInline();
	}

	public void setDecorationStyle(final WCssDecorationStyle style) {
		this.impl_.setDecorationStyle(style);
	}

	public WCssDecorationStyle getDecorationStyle() {
		return this.impl_.getDecorationStyle();
	}

	public void setStyleClass(final String styleClass) {
		this.impl_.setStyleClass(styleClass);
	}

	public String getStyleClass() {
		return this.impl_.getStyleClass();
	}

	public void addStyleClass(final String styleClass, boolean force) {
		this.impl_.addStyleClass(styleClass, force);
	}

	public void removeStyleClass(final String styleClass, boolean force) {
		this.impl_.removeStyleClass(styleClass, force);
	}

	public boolean hasStyleClass(final String styleClass) {
		return this.impl_.hasStyleClass(styleClass);
	}

	public void setVerticalAlignment(AlignmentFlag alignment,
			final WLength length) {
		if (!EnumUtils.mask(AlignmentFlag.AlignHorizontalMask, alignment)
				.isEmpty()) {
			logger.error(new StringWriter().append(
					"setVerticalAlignment(): alignment ").append(
					String.valueOf(alignment.getValue())).append(
					"is not vertical").toString());
		}
		this.impl_.setVerticalAlignment(alignment, length);
	}

	public AlignmentFlag getVerticalAlignment() {
		return this.impl_.getVerticalAlignment();
	}

	public WLength getVerticalAlignmentLength() {
		return this.impl_.getVerticalAlignmentLength();
	}

	WWebWidget getWebWidget() {
		return this.impl_ != null ? this.impl_.getWebWidget() : null;
	}

	public void setToolTip(final CharSequence text, TextFormat textFormat) {
		this.impl_.setToolTip(text, textFormat);
	}

	public WString getToolTip() {
		return this.impl_.getToolTip();
	}

	public void setDeferredToolTip(boolean enable, TextFormat textFormat) {
		this.impl_.setDeferredToolTip(enable, textFormat);
	}

	public void refresh() {
		this.impl_.refresh();
		super.refresh();
	}

	public void setAttributeValue(final String name, final String value) {
		this.impl_.setAttributeValue(name, value);
	}

	public String getAttributeValue(final String name) {
		return this.impl_.getAttributeValue(name);
	}

	public void setJavaScriptMember(final String name, final String value) {
		this.impl_.setJavaScriptMember(name, value);
	}

	public String getJavaScriptMember(final String name) {
		return this.impl_.getJavaScriptMember(name);
	}

	public void callJavaScriptMember(final String name, final String args) {
		this.impl_.callJavaScriptMember(name, args);
	}

	public void load() {
		if (this.impl_ != null) {
			this.impl_.load();
		}
	}

	public boolean isLoaded() {
		return this.impl_ != null ? this.impl_.isLoaded() : true;
	}

	public void setTabIndex(int index) {
		this.impl_.setTabIndex(index);
	}

	public int getTabIndex() {
		return this.impl_.getTabIndex();
	}

	int getZIndex() {
		return this.impl_.getZIndex();
	}

	public void setId(final String id) {
		this.impl_.setId(id);
	}

	public WWidget find(final String name) {
		if (this.getObjectName().equals(name)) {
			return this;
		} else {
			return this.impl_.find(name);
		}
	}

	public WWidget findById(final String id) {
		if (this.getId().equals(id)) {
			return this;
		} else {
			return this.impl_.findById(id);
		}
	}

	public void setSelectable(boolean selectable) {
		this.impl_.setSelectable(selectable);
	}

	public void doJavaScript(final String js) {
		this.impl_.doJavaScript(js);
	}

	public void propagateSetEnabled(boolean enabled) {
		this.impl_.getWebWidget().propagateSetEnabled(enabled);
	}

	void addChild(WWidget child) {
		if (child != this.impl_) {
			this.impl_.addChild(child);
		} else {
			this.impl_.setParent(this);
		}
	}

	void removeChild(WWidget child) {
		if (child != this.impl_) {
			this.impl_.removeChild(child);
		} else {
			this.impl_.setParent((WObject) null);
		}
	}

	void setHideWithOffsets(boolean hideWithOffsets) {
		this.impl_.setHideWithOffsets(hideWithOffsets);
	}

	boolean isStubbed() {
		if (this.getParent() != null) {
			return this.getParent().isStubbed();
		} else {
			return false;
		}
	}

	protected void enableAjax() {
		this.impl_.enableAjax();
	}

	/**
	 * Set the implementation widget.
	 * <p>
	 * This sets the widget that implements this compositeWidget. Ownership of
	 * the widget is completely transferred (including deletion).
	 * <p>
	 * <p>
	 * <i><b>Note: </b>You cannot change the implementation of a composite
	 * widget after it has been rendered. </i>
	 * </p>
	 */
	protected void setImplementation(WWidget widget) {
		if (widget.getParent() != null) {
			throw new WException(
					"WCompositeWidget implementation widget cannot have a parent");
		}
		if (this.impl_ != null)
			this.impl_.remove();
		this.impl_ = widget;
		if (this.getParent() != null) {
			WWebWidget ww = this.impl_.getWebWidget();
			if (ww != null) {
				ww.gotParent();
			}
			if (this.getParent().isLoaded()) {
				this.impl_.load();
			}
		}
		widget.setParentWidget(this);
	}

	/**
	 * Get the implementation widget.
	 * <p>
	 * This returns the widget that implements this compositeWidget.
	 */
	protected WWidget getImplementation() {
		return this.impl_;
	}

	protected WWidget getTakeImplementation() {
		WWidget result = this.impl_;
		if (result != null) {
			this.removeChild(result);
			this.impl_ = null;
		}
		return result;
	}

	void getSDomChanges(final List<DomElement> result, WApplication app) {
		if (this.needsToBeRendered()) {
			this.render(this.impl_.isRendered() ? RenderFlag.RenderUpdate
					: RenderFlag.RenderFull);
		}
		this.impl_.getSDomChanges(result, app);
	}

	boolean needsToBeRendered() {
		return this.impl_.needsToBeRendered();
	}

	protected int boxPadding(Orientation orientation) {
		return this.impl_.boxPadding(orientation);
	}

	protected int boxBorder(Orientation orientation) {
		return this.impl_.boxBorder(orientation);
	}

	protected void render(EnumSet<RenderFlag> flags) {
		this.impl_.render(flags);
		this.renderOk();
	}

	private WWidget impl_;

	void setLayout(WLayout layout) {
		this.impl_.setLayout(layout);
	}

	WLayout getLayout() {
		return this.impl_.getLayout();
	}

	WLayoutItemImpl createLayoutItemImpl(WLayoutItem item) {
		return this.impl_.createLayoutItemImpl(item);
	}
}
