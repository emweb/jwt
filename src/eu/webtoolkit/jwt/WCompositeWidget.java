/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * A widget that hides the implementation of composite widgets
 * <p>
 * 
 * Composite widgets, built on top of the WebWidgets, should derive from this
 * class, and use {@link WCompositeWidget#setImplementation(WWidget widget)} to
 * set the widget that implements the composite widget (which is typically a
 * {@link WContainerWidget} or a {@link WTable}, or another widget that allows
 * composition, including perhaps another WCompositeWidget).
 * <p>
 * Using this class you can completely hide the implementation of your composite
 * widget, and provide access to only the standard {@link WWidget} methods.
 */
public class WCompositeWidget extends WWidget {
	/**
	 * Create a {@link WCompositeWidget}.
	 */
	public WCompositeWidget(WContainerWidget parent) {
		super(parent);
		this.impl_ = null;
		if (parent != null) {
			parent.addWidget(this);
		}
	}

	/**
	 * Create a {@link WCompositeWidget}.
	 * <p>
	 * Calls {@link #WCompositeWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WCompositeWidget() {
		this((WContainerWidget) null);
	}

	public void remove() {
		this.setParent((WWidget) null);
		if (this.impl_ != null)
			this.impl_.remove();
		super.remove();
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

	public void setOffsets(WLength offset, EnumSet<Side> sides) {
		this.impl_.setOffsets(offset, sides);
	}

	public WLength getOffset(Side s) {
		return this.impl_.getOffset(s);
	}

	public void resize(WLength width, WLength height) {
		this.impl_.resize(width, height);
	}

	public WLength getWidth() {
		return this.impl_.getWidth();
	}

	public WLength getHeight() {
		return this.impl_.getHeight();
	}

	public void setMinimumSize(WLength width, WLength height) {
		this.impl_.setMinimumSize(width, height);
	}

	public WLength getMinimumWidth() {
		return this.impl_.getMinimumWidth();
	}

	public WLength getMinimumHeight() {
		return this.impl_.getMinimumHeight();
	}

	public void setMaximumSize(WLength width, WLength height) {
		this.impl_.setMaximumSize(width, height);
	}

	public WLength getMaximumWidth() {
		return this.impl_.getMaximumWidth();
	}

	public WLength getMaximumHeight() {
		return this.impl_.getMaximumHeight();
	}

	public void setLineHeight(WLength height) {
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

	public void setMargin(WLength margin, EnumSet<Side> sides) {
		this.impl_.setMargin(margin, sides);
	}

	public WLength getMargin(Side side) {
		return this.impl_.getMargin(side);
	}

	public void setHidden(boolean how) {
		this.impl_.setHidden(how);
	}

	public boolean isHidden() {
		return this.impl_.isHidden();
	}

	public void setPopup(boolean how) {
		this.impl_.setPopup(how);
	}

	public boolean isPopup() {
		return this.impl_.isPopup();
	}

	public void setInline(boolean how) {
		// this.resetLearnedSlot(WWidget.show);
		this.impl_.setInline(how);
	}

	public boolean isInline() {
		return this.impl_.isInline();
	}

	public void setDecorationStyle(WCssDecorationStyle style) {
		this.impl_.setDecorationStyle(style);
	}

	public WCssDecorationStyle getDecorationStyle() {
		return this.impl_.getDecorationStyle();
	}

	public void setStyleClass(String styleClass) {
		this.impl_.setStyleClass(styleClass);
	}

	public String getStyleClass() {
		return this.impl_.getStyleClass();
	}

	public void setVerticalAlignment(AlignmentFlag alignment, WLength length) {
		if (!EnumUtils.mask(AlignmentFlag.AlignHorizontalMask, alignment)
				.isEmpty()) {
			WApplication.getInstance().log("warning").append(
					"WCompositeWidget::setVerticalAlignment: alignment ")
					.append(alignment.toString()).append(
							"is horizontal, expected vertical");
		}
		this.impl_.setVerticalAlignment(alignment, length);
	}

	public AlignmentFlag getVerticalAlignment() {
		return this.impl_.getVerticalAlignment();
	}

	public WLength getVerticalAlignmentLength() {
		return this.impl_.getVerticalAlignmentLength();
	}

	public WWebWidget getWebWidget() {
		return this.impl_ != null ? this.impl_.getWebWidget() : null;
	}

	public void setToolTip(CharSequence text) {
		this.impl_.setToolTip(text);
	}

	public WString getToolTip() {
		return this.impl_.getToolTip();
	}

	public void refresh() {
		this.impl_.refresh();
	}

	public void setAttributeValue(String name, String value) {
		this.impl_.setAttributeValue(name, value);
	}

	public String getAttributeValue(String name) {
		return this.impl_.getAttributeValue(name);
	}

	public void load() {
		if (this.impl_ != null) {
			this.impl_.load();
		}
	}

	public boolean isLoaded() {
		return this.impl_ != null ? this.impl_.isLoaded() : true;
	}

	public void setId(String id) {
		this.impl_.setId(id);
	}

	public void setSelectable(boolean selectable) {
		this.impl_.setSelectable(selectable);
	}

	protected void addChild(WWidget child) {
		if (child != this.impl_) {
			this.impl_.addChild(child);
		} else {
			this.impl_.setParent((WObject) this);
		}
	}

	protected void removeChild(WWidget child) {
		if (child != this.impl_) {
			this.impl_.removeChild(child);
		} else {
			this.impl_.setParent((WObject) null);
		}
	}

	protected void setHideWithOffsets(boolean how) {
		this.impl_.setHideWithOffsets(how);
	}

	protected boolean isVisible() {
		if (this.getParent() != null) {
			return this.getParent().isVisible();
		} else {
			return true;
		}
	}

	protected boolean isStubbed() {
		if (this.getParent() != null) {
			return this.getParent().isStubbed();
		} else {
			return false;
		}
	}

	/**
	 * Set the implementation widget.
	 * <p>
	 * This sets the widget that implements this compositeWidget. Ownership of
	 * the widget is completely transferred (including deletion).
	 */
	protected void setImplementation(WWidget widget) {
		if (widget.getParent() != null) {
			throw new WtException(
					"WCompositeWidget implemnation widget cannot have a parent");
		}
		if (this.impl_ != null) {
			if (this.impl_ != null)
				this.impl_.remove();
		}
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
		widget.setParent(this);
	}

	public DomElement createSDomElement(WApplication app) {
		if (this.needsToBeRendered()) {
			this.render();
		}
		return this.impl_.createSDomElement(app);
	}

	protected void getSDomChanges(List<DomElement> result, WApplication app) {
		if (this.needsToBeRendered()) {
			this.render();
		}
		this.impl_.getSDomChanges(result, app);
	}

	protected boolean needsToBeRendered() {
		return this.impl_.needsToBeRendered();
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
