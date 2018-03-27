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
 * A base class for widgets with an HTML counterpart.
 * <p>
 * 
 * All descendants of WWebWidget implement a widget which corresponds almost
 * one-on-one with an HTML element. These widgets provide most capabilities of
 * these HTML elements, but rarely make no attempt to do anything more.
 * <p>
 * 
 * @see WCompositeWidget
 */
public abstract class WWebWidget extends WWidget {
	private static Logger logger = LoggerFactory.getLogger(WWebWidget.class);

	/**
	 * Construct a WebWidget with a given parent.
	 * <p>
	 * 
	 * @see WWidget#WWidget(WContainerWidget parent)
	 */
	public WWebWidget(WContainerWidget parent) {
		super(parent);
		this.elementTagName_ = "";
		this.flags_ = new BitSet();
		this.width_ = null;
		this.height_ = null;
		this.transientImpl_ = null;
		this.layoutImpl_ = null;
		this.lookImpl_ = null;
		this.otherImpl_ = null;
		this.children_ = null;
		this.flags_.set(BIT_INLINE);
		this.flags_.set(BIT_ENABLED);
		if (parent != null) {
			parent.addWidget(this);
		}
	}

	/**
	 * Construct a WebWidget with a given parent.
	 * <p>
	 * Calls {@link #WWebWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WWebWidget() {
		this((WContainerWidget) null);
	}

	public void remove() {
		this.beingDeleted();
		this.setParentWidget((WWidget) null);
		;
		;
		if (this.children_ != null) {
			while (this.children_.size() != 0) {
				if (this.children_.get(0) != null)
					this.children_.get(0).remove();
			}
			;
		}
		;
		;
		;
		;
		super.remove();
	}

	public void setPositionScheme(PositionScheme scheme) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.positionScheme_ = scheme;
		if (scheme == PositionScheme.Absolute || scheme == PositionScheme.Fixed) {
			this.flags_.clear(BIT_INLINE);
		}
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	public PositionScheme getPositionScheme() {
		return this.layoutImpl_ != null ? this.layoutImpl_.positionScheme_
				: PositionScheme.Static;
	}

	public void setOffsets(final WLength offset, EnumSet<Side> sides) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		if (!EnumUtils.mask(sides, Side.Top).isEmpty()) {
			this.layoutImpl_.offsets_[0] = offset;
		}
		if (!EnumUtils.mask(sides, Side.Right).isEmpty()) {
			this.layoutImpl_.offsets_[1] = offset;
		}
		if (!EnumUtils.mask(sides, Side.Bottom).isEmpty()) {
			this.layoutImpl_.offsets_[2] = offset;
		}
		if (!EnumUtils.mask(sides, Side.Left).isEmpty()) {
			this.layoutImpl_.offsets_[3] = offset;
		}
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint();
	}

	public WLength getOffset(Side s) {
		if (!(this.layoutImpl_ != null)) {
			return WLength.Auto;
		}
		switch (s) {
		case Top:
			return this.layoutImpl_.offsets_[0];
		case Right:
			return this.layoutImpl_.offsets_[1];
		case Bottom:
			return this.layoutImpl_.offsets_[2];
		case Left:
			return this.layoutImpl_.offsets_[3];
		default:
			logger.error(new StringWriter()
					.append("offset(Side) with invalid side: ")
					.append(String.valueOf((int) s.getValue())).toString());
			return new WLength();
		}
	}

	public void resize(final WLength width, final WLength height) {
		boolean changed = false;
		if (!(this.width_ != null) && !width.isAuto()) {
			this.width_ = new WLength();
		}
		if (this.width_ != null && !this.width_.equals(width)) {
			changed = true;
			this.width_ = nonNegative(width);
			this.flags_.set(BIT_WIDTH_CHANGED);
		}
		if (!(this.height_ != null) && !height.isAuto()) {
			this.height_ = new WLength();
		}
		if (this.height_ != null && !this.height_.equals(height)) {
			changed = true;
			this.height_ = nonNegative(height);
			this.flags_.set(BIT_HEIGHT_CHANGED);
		}
		if (changed) {
			this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
			super.resize(width, height);
		}
	}

	public WLength getWidth() {
		return this.width_ != null ? this.width_ : WLength.Auto;
	}

	public WLength getHeight() {
		return this.height_ != null ? this.height_ : WLength.Auto;
	}

	public void setMinimumSize(final WLength width, final WLength height) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.minimumWidth_ = nonNegative(width);
		this.layoutImpl_.minimumHeight_ = nonNegative(height);
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	public WLength getMinimumWidth() {
		return this.layoutImpl_ != null ? this.layoutImpl_.minimumWidth_
				: new WLength(0);
	}

	public WLength getMinimumHeight() {
		return this.layoutImpl_ != null ? this.layoutImpl_.minimumHeight_
				: new WLength(0);
	}

	public void setMaximumSize(final WLength width, final WLength height) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.maximumWidth_ = nonNegative(width);
		this.layoutImpl_.maximumHeight_ = nonNegative(height);
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	public WLength getMaximumWidth() {
		return this.layoutImpl_ != null ? this.layoutImpl_.maximumWidth_
				: WLength.Auto;
	}

	public WLength getMaximumHeight() {
		return this.layoutImpl_ != null ? this.layoutImpl_.maximumHeight_
				: WLength.Auto;
	}

	public void setLineHeight(final WLength height) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.lineHeight_ = height;
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	public WLength getLineHeight() {
		return this.layoutImpl_ != null ? this.layoutImpl_.lineHeight_
				: WLength.Auto;
	}

	public void setFloatSide(Side s) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.floatSide_ = s;
		this.flags_.set(BIT_FLOAT_SIDE_CHANGED);
		this.repaint();
	}

	public Side getFloatSide() {
		if (this.layoutImpl_ != null) {
			return this.layoutImpl_.floatSide_;
		} else {
			return null;
		}
	}

	public void setClearSides(EnumSet<Side> sides) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.clearSides_ = EnumSet.copyOf(sides);
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint();
	}

	public EnumSet<Side> getClearSides() {
		if (this.layoutImpl_ != null) {
			return this.layoutImpl_.clearSides_;
		} else {
			return EnumSet.copyOf(Side.None);
		}
	}

	public void setMargin(final WLength margin, EnumSet<Side> sides) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		if (!EnumUtils.mask(sides, Side.Top).isEmpty()) {
			this.layoutImpl_.margin_[0] = margin;
		}
		if (!EnumUtils.mask(sides, Side.Right).isEmpty()) {
			this.layoutImpl_.margin_[1] = margin;
		}
		if (!EnumUtils.mask(sides, Side.Bottom).isEmpty()) {
			this.layoutImpl_.margin_[2] = margin;
		}
		if (!EnumUtils.mask(sides, Side.Left).isEmpty()) {
			this.layoutImpl_.margin_[3] = margin;
		}
		this.flags_.set(BIT_MARGINS_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	public WLength getMargin(Side side) {
		if (!(this.layoutImpl_ != null)) {
			return new WLength(0);
		}
		switch (side) {
		case Top:
			return this.layoutImpl_.margin_[0];
		case Right:
			return this.layoutImpl_.margin_[1];
		case Bottom:
			return this.layoutImpl_.margin_[2];
		case Left:
			return this.layoutImpl_.margin_[3];
		default:
			logger.error(new StringWriter()
					.append("margin(Side) with invalid side: ")
					.append(String.valueOf((int) side.getValue())).toString());
			return new WLength();
		}
	}

	public void setHiddenKeepsGeometry(boolean enabled) {
		this.flags_.set(BIT_DONOT_STUB);
		this.flags_.set(BIT_HIDE_WITH_VISIBILITY, enabled);
		this.flags_.set(BIT_HIDDEN_CHANGED);
	}

	public boolean isHiddenKeepsGeometry() {
		return this.flags_.get(BIT_HIDE_WITH_VISIBILITY)
				&& !this.flags_.get(BIT_HIDE_WITH_OFFSETS);
	}

	public void setHidden(boolean hidden, final WAnimation animation) {
		if (canOptimizeUpdates()
				&& (animation.isEmpty() && hidden == this.isHidden())) {
			return;
		}
		boolean wasVisible = this.isVisible();
		this.flags_.set(BIT_HIDDEN, hidden);
		this.flags_.set(BIT_HIDDEN_CHANGED);
		if (!animation.isEmpty()
				&& WApplication.getInstance().getEnvironment()
						.supportsCss3Animations()
				&& WApplication.getInstance().getEnvironment().hasAjax()) {
			if (!(this.transientImpl_ != null)) {
				this.transientImpl_ = new WWebWidget.TransientImpl();
			}
			this.transientImpl_.animation_ = animation;
		}
		boolean shouldBeVisible = !hidden;
		if (shouldBeVisible && this.getParent() != null) {
			shouldBeVisible = this.getParent().isVisible();
		}
		if (!canOptimizeUpdates() || shouldBeVisible != wasVisible) {
			this.propagateSetVisible(shouldBeVisible);
		}
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(this, true);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	public boolean isHidden() {
		return this.flags_.get(BIT_HIDDEN);
	}

	public boolean isVisible() {
		if (this.flags_.get(BIT_STUBBED) || this.flags_.get(BIT_HIDDEN)) {
			return false;
		} else {
			if (this.getParent() != null) {
				return this.getParent().isVisible();
			} else {
				return this == WApplication.getInstance().getDomRoot()
						|| this == WApplication.getInstance().getDomRoot2();
			}
		}
	}

	public void setDisabled(boolean disabled) {
		if (canOptimizeUpdates() && disabled == this.flags_.get(BIT_DISABLED)) {
			return;
		}
		boolean wasEnabled = this.isEnabled();
		this.flags_.set(BIT_DISABLED, disabled);
		this.flags_.set(BIT_DISABLED_CHANGED);
		boolean shouldBeEnabled = !disabled;
		if (shouldBeEnabled && this.getParent() != null) {
			shouldBeEnabled = this.getParent().isEnabled();
		}
		if (shouldBeEnabled != wasEnabled) {
			this.propagateSetEnabled(shouldBeEnabled);
		}
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(this, true);
		this.repaint();
	}

	public boolean isDisabled() {
		return this.flags_.get(BIT_DISABLED);
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
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.zIndex_ = popup ? -1 : 0;
		if (popup && this.getParent() != null) {
			this.calcZIndex();
		}
		this.flags_.set(BIT_ZINDEX_CHANGED);
		this.repaint();
	}

	public boolean isPopup() {
		return this.layoutImpl_ != null ? this.layoutImpl_.zIndex_ != 0 : false;
	}

	public void setInline(boolean inl) {
		this.flags_.set(BIT_INLINE, inl);
		// this.resetLearnedSlot(WWidget.show);
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint();
	}

	public boolean isInline() {
		return this.flags_.get(BIT_INLINE);
	}

	public void setDecorationStyle(final WCssDecorationStyle style) {
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl(this);
		}
		this.lookImpl_.decorationStyle_ = style;
	}

	public WCssDecorationStyle getDecorationStyle() {
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl(this);
		}
		if (!(this.lookImpl_.decorationStyle_ != null)) {
			this.lookImpl_.decorationStyle_ = new WCssDecorationStyle();
			this.lookImpl_.decorationStyle_.setWebWidget(this);
		}
		return this.lookImpl_.decorationStyle_;
	}

	public void setStyleClass(final String styleClass) {
		if (canOptimizeUpdates() && styleClass.equals(this.getStyleClass())) {
			return;
		}
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl(this);
		}
		this.lookImpl_.styleClass_ = styleClass;
		this.flags_.set(BIT_STYLECLASS_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	public String getStyleClass() {
		return this.lookImpl_ != null ? this.lookImpl_.styleClass_ : "";
	}

	public void addStyleClass(final String styleClass, boolean force) {
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl(this);
		}
		String currentClass = this.lookImpl_.styleClass_;
		Set<String> classes = new HashSet<String>();
		StringUtils.split(classes, currentClass, " ", true);
		if (classes.contains(styleClass) == false) {
			this.lookImpl_.styleClass_ = StringUtils.addWord(
					this.lookImpl_.styleClass_, styleClass);
			if (!force) {
				this.flags_.set(BIT_STYLECLASS_CHANGED);
				this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
			}
		}
		if (force && this.isRendered()) {
			if (!(this.transientImpl_ != null)) {
				this.transientImpl_ = new WWebWidget.TransientImpl();
			}
			CollectionUtils.add(this.transientImpl_.addedStyleClasses_,
					styleClass);
			this.transientImpl_.removedStyleClasses_.remove(styleClass);
			this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
		}
	}

	public void removeStyleClass(final String styleClass, boolean force) {
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl(this);
		}
		if (this.hasStyleClass(styleClass)) {
			this.lookImpl_.styleClass_ = StringUtils.eraseWord(
					this.lookImpl_.styleClass_, styleClass);
			if (!force) {
				this.flags_.set(BIT_STYLECLASS_CHANGED);
				this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
			}
		}
		if (force && this.isRendered()) {
			if (!(this.transientImpl_ != null)) {
				this.transientImpl_ = new WWebWidget.TransientImpl();
			}
			CollectionUtils.add(this.transientImpl_.removedStyleClasses_,
					styleClass);
			this.transientImpl_.addedStyleClasses_.remove(styleClass);
			this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
		}
	}

	public boolean hasStyleClass(final String styleClass) {
		if (!(this.lookImpl_ != null)) {
			return false;
		}
		String currentClass = this.lookImpl_.styleClass_;
		Set<String> classes = new HashSet<String>();
		StringUtils.split(classes, currentClass, " ", true);
		return classes.contains(styleClass) != false;
	}

	public void setVerticalAlignment(AlignmentFlag alignment,
			final WLength length) {
		if (!EnumUtils.mask(AlignmentFlag.AlignHorizontalMask, alignment)
				.isEmpty()) {
			logger.error(new StringWriter()
					.append("setVerticalAlignment(): alignment ")
					.append(String.valueOf(alignment.getValue()))
					.append(" is not vertical").toString());
		}
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.verticalAlignment_ = alignment;
		this.layoutImpl_.verticalAlignmentLength_ = length;
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint();
	}

	public AlignmentFlag getVerticalAlignment() {
		return this.layoutImpl_ != null ? this.layoutImpl_.verticalAlignment_
				: AlignmentFlag.AlignBaseline;
	}

	public WLength getVerticalAlignmentLength() {
		return this.layoutImpl_ != null ? this.layoutImpl_.verticalAlignmentLength_
				: WLength.Auto;
	}

	public void setToolTip(final CharSequence text, TextFormat textFormat) {
		this.flags_.clear(BIT_TOOLTIP_DEFERRED);
		if (canOptimizeUpdates() && text.equals(this.getStoredToolTip())) {
			return;
		}
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl(this);
		}
		if (!(this.lookImpl_.toolTip_ != null)) {
			this.lookImpl_.toolTip_ = new WString();
		}
		this.lookImpl_.toolTip_ = WString.toWString(text);
		this.lookImpl_.toolTipTextFormat_ = textFormat;
		this.flags_.set(BIT_TOOLTIP_CHANGED);
		this.repaint();
	}

	public void setDeferredToolTip(boolean enable, TextFormat textFormat) {
		this.flags_.set(BIT_TOOLTIP_DEFERRED, enable);
		if (!enable) {
			this.setToolTip("", textFormat);
		} else {
			if (!(this.lookImpl_ != null)) {
				this.lookImpl_ = new WWebWidget.LookImpl(this);
			}
			if (!(this.lookImpl_.toolTip_ != null)) {
				this.lookImpl_.toolTip_ = new WString();
			} else {
				this.lookImpl_.toolTip_ = new WString();
			}
			this.lookImpl_.toolTipTextFormat_ = textFormat;
			this.flags_.set(BIT_TOOLTIP_CHANGED);
			this.repaint();
		}
	}

	public WString getToolTip() {
		return this.getStoredToolTip();
	}

	public void refresh() {
		if (this.lookImpl_ != null && this.lookImpl_.toolTip_ != null) {
			if (this.lookImpl_.toolTip_.refresh()) {
				this.flags_.set(BIT_TOOLTIP_CHANGED);
				this.repaint();
			}
		}
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.children_.get(i).refresh();
			}
		}
		super.refresh();
	}

	public void setAttributeValue(final String name, final String value) {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		if (!(this.otherImpl_.attributes_ != null)) {
			this.otherImpl_.attributes_ = new HashMap<String, String>();
		}
		String i = this.otherImpl_.attributes_.get(name);
		if (i != null && i.equals(value)) {
			return;
		}
		this.otherImpl_.attributes_.put(name, value);
		if (!(this.transientImpl_ != null)) {
			this.transientImpl_ = new WWebWidget.TransientImpl();
		}
		this.transientImpl_.attributesSet_.add(name);
		this.repaint();
	}

	public String getAttributeValue(final String name) {
		if (this.otherImpl_ != null && this.otherImpl_.attributes_ != null) {
			String i = this.otherImpl_.attributes_.get(name);
			if (i != null) {
				return i;
			}
		}
		return "";
	}

	public void setJavaScriptMember(final String name, final String value) {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		if (!(this.otherImpl_.jsMembers_ != null)) {
			this.otherImpl_.jsMembers_ = new ArrayList<WWebWidget.OtherImpl.Member>();
		}
		final List<WWebWidget.OtherImpl.Member> members = this.otherImpl_.jsMembers_;
		int index = this.indexOfJavaScriptMember(name);
		if (index != -1 && members.get(index).value.equals(value)) {
			return;
		}
		if (value.length() == 0) {
			if (index != -1) {
				members.remove(0 + index);
			} else {
				return;
			}
		} else {
			if (index == -1) {
				WWebWidget.OtherImpl.Member m = new WWebWidget.OtherImpl.Member();
				m.name = name;
				m.value = value;
				members.add(m);
			} else {
				members.get(index).value = value;
			}
		}
		this.addJavaScriptStatement(
				WWebWidget.JavaScriptStatementType.SetMember, name);
		this.repaint();
	}

	public String getJavaScriptMember(final String name) {
		int index = this.indexOfJavaScriptMember(name);
		if (index != -1) {
			return this.otherImpl_.jsMembers_.get(index).value;
		} else {
			return "";
		}
	}

	public void callJavaScriptMember(final String name, final String args) {
		this.addJavaScriptStatement(
				WWebWidget.JavaScriptStatementType.CallMethod, name + "("
						+ args + ");");
		this.repaint();
	}

	public void load() {
		this.flags_.set(BIT_LOADED);
		for (int i = 0; this.children_ != null && i < this.children_.size(); ++i) {
			this.doLoad(this.children_.get(i));
		}
		if (this.flags_.get(BIT_HIDE_WITH_OFFSETS)) {
			this.getParent().setHideWithOffsets(true);
		}
	}

	public boolean isLoaded() {
		return this.flags_.get(BIT_LOADED);
	}

	int getZIndex() {
		if (this.layoutImpl_ != null) {
			return this.layoutImpl_.zIndex_;
		} else {
			return 0;
		}
	}

	public void setId(final String id) {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		WApplication app = WApplication.getInstance();
		for (int i = 0; i < this.jsignals_.size(); ++i) {
			AbstractEventSignal signal = this.jsignals_.get(i);
			if (signal.isExposedSignal()) {
				app.removeExposedSignal(signal);
			}
		}
		if (!(this.otherImpl_.id_ != null)) {
			this.otherImpl_.id_ = "";
		}
		this.otherImpl_.id_ = id;
		for (int i = 0; i < this.jsignals_.size(); ++i) {
			AbstractEventSignal signal = this.jsignals_.get(i);
			if (signal.isExposedSignal()) {
				app.addExposedSignal(signal);
			}
		}
	}

	public WWidget find(final String name) {
		if (this.getObjectName().equals(name)) {
			return this;
		} else {
			if (this.children_ != null) {
				for (int i = 0; i < this.children_.size(); ++i) {
					WWidget result = this.children_.get(i).find(name);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

	public WWidget findById(final String id) {
		if (this.getId().equals(id)) {
			return this;
		} else {
			if (this.children_ != null) {
				for (int i = 0; i < this.children_.size(); ++i) {
					WWidget result = this.children_.get(i).findById(id);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

	public void setSelectable(boolean selectable) {
		this.flags_.set(BIT_SET_SELECTABLE, selectable);
		this.flags_.set(BIT_SET_UNSELECTABLE, !selectable);
		this.flags_.set(BIT_SELECTABLE_CHANGED);
		this.repaint();
	}

	public void doJavaScript(final String javascript) {
		this.addJavaScriptStatement(
				WWebWidget.JavaScriptStatementType.Statement, javascript);
		this.repaint();
	}

	public String getId() {
		if (this.otherImpl_ != null && this.otherImpl_.id_ != null) {
			return this.otherImpl_.id_;
		} else {
			return super.getId();
		}
	}

	/**
	 * Create DOM element for widget.
	 * <p>
	 * This is an internal function, and should not be called directly, or be
	 * overridden!
	 */
	protected DomElement createDomElement(WApplication app) {
		this.setRendered(true);
		DomElement result;
		if (this.elementTagName_.length() > 0) {
			result = DomElement.createNew(DomElementType.DomElement_OTHER);
			result.setDomElementTagName(this.elementTagName_);
		} else {
			result = DomElement.createNew(this.getDomElementType());
		}
		this.setId(result, app);
		this.updateDom(result, true);
		return result;
	}

	/**
	 * Get DOM changes for this widget.
	 * <p>
	 * This is an internal function, and should not be called directly, or be
	 * overridden!
	 */
	protected void getDomChanges(final List<DomElement> result, WApplication app) {
		DomElement e = DomElement.getForUpdate(this, this.getDomElementType());
		this.updateDom(e, false);
		result.add(e);
	}

	abstract DomElementType getDomElementType();

	DomElement createStubElement(WApplication app) {
		this.propagateRenderOk();
		this.flags_.set(BIT_STUBBED);
		DomElement stub = DomElement.createNew(DomElementType.DomElement_SPAN);
		if (!this.flags_.get(BIT_HIDE_WITH_OFFSETS)) {
			stub.setProperty(Property.PropertyStyleDisplay, "none");
		} else {
			stub.setProperty(Property.PropertyStylePosition, "absolute");
			stub.setProperty(Property.PropertyStyleLeft, "-10000px");
			stub.setProperty(Property.PropertyStyleTop, "-10000px");
			stub.setProperty(Property.PropertyStyleVisibility, "hidden");
		}
		if (app.getEnvironment().hasJavaScript()) {
			stub.setProperty(Property.PropertyInnerHTML, "...");
		}
		if (!app.getEnvironment().agentIsSpiderBot() || this.otherImpl_ != null
				&& this.otherImpl_.id_ != null) {
			stub.setId(this.getId());
		}
		return stub;
	}

	DomElement createActualElement(WWidget self, WApplication app) {
		this.flags_.clear(BIT_STUBBED);
		DomElement result = this.createDomElement(app);
		app.getTheme().apply(self, result,
				ElementThemeRole.MainElementThemeRole);
		String styleClass = result.getProperty(Property.PropertyClass);
		if (styleClass.length() != 0) {
			if (!(this.lookImpl_ != null)) {
				this.lookImpl_ = new WWebWidget.LookImpl(this);
			}
			this.lookImpl_.styleClass_ = styleClass;
		}
		return result;
	}

	/**
	 * Change the way the widget is loaded when invisible.
	 * <p>
	 * By default, invisible widgets are loaded only after visible content. For
	 * tiny widgets this may lead to a performance loss, instead of the expected
	 * increase, because they require many more DOM manipulations to render,
	 * reducing the overall responsiveness of the application.
	 * <p>
	 * Therefore, this is disabled for some widgets like {@link WImage}, or
	 * empty WContainerWidgets.
	 * <p>
	 * You may also want to disable deferred loading when JavaScript event
	 * handling expects the widget to be loaded.
	 * <p>
	 * Usually the default settings are fine, but you may want to change the
	 * behaviour.
	 * <p>
	 * 
	 * @see WApplication#setTwoPhaseRenderingThreshold(int bytes)
	 */
	public void setLoadLaterWhenInvisible(boolean how) {
		this.flags_.set(BIT_DONOT_STUB, !how);
	}

	/**
	 * returns the current html tag name
	 * <p>
	 * 
	 * @see WWebWidget#setHtmlTagName(String tag)
	 */
	public String getHtmlTagName() {
		if (this.elementTagName_.length() > 0) {
			return this.elementTagName_;
		}
		DomElementType type = this.getDomElementType();
		return DomElement.tagName(type);
	}

	/**
	 * set the custom HTML tag name
	 * <p>
	 * The custom tag will replace the actual tag. The tag is not tested to see
	 * if it is a valid one and a closing tag will always be added.
	 * <p>
	 * 
	 * @see WWebWidget#getHtmlTagName()
	 */
	public void setHtmlTagName(final String tag) {
		this.elementTagName_ = tag;
	}

	/**
	 * Escape HTML control characters in the text, to display literally
	 * (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated use {@link Utils#htmlEncode(WString text, EnumSet flags)}
	 *             instead.
	 */
	public static WString escapeText(final CharSequence text,
			boolean newlinestoo) {
		String result = text.toString();
		result = escapeText(result, newlinestoo);
		return new WString(result);
	}

	/**
	 * Escape HTML control characters in the text, to display literally
	 * (<b>deprecated</b>).
	 * <p>
	 * Returns {@link #escapeText(CharSequence text, boolean newlinestoo)
	 * escapeText(text, false)}
	 */
	public static final WString escapeText(final CharSequence text) {
		return escapeText(text, false);
	}

	/**
	 * Escape HTML control characters in the text, to display literally
	 * (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated use {@link Utils#htmlEncode(String text, EnumSet flags)}
	 *             instead.
	 */
	public static String escapeText(final String text, boolean newlinestoo) {
		EscapeOStream sout = new EscapeOStream();
		if (newlinestoo) {
			sout.pushEscape(EscapeOStream.RuleSet.PlainTextNewLines);
		} else {
			sout.pushEscape(EscapeOStream.RuleSet.PlainText);
		}
		StringUtils.sanitizeUnicode(sout, text);
		return sout.toString();
	}

	/**
	 * Escape HTML control characters in the text, to display literally
	 * (<b>deprecated</b>).
	 * <p>
	 * Returns {@link #escapeText(String text, boolean newlinestoo)
	 * escapeText(text, false)}
	 */
	public static final String escapeText(final String text) {
		return escapeText(text, false);
	}

	/**
	 * Remove tags/attributes from text that are not passive
	 * (<b>deprecated</b>).
	 * <p>
	 * This removes tags and attributes from XHTML-formatted text that do not
	 * simply display something but may trigger scripting, and could have been
	 * injected by a malicious user for Cross-Site Scripting (XSS).
	 * <p>
	 * This method is used by the library to sanitize XHTML-formatted text set
	 * in {@link WText}, but it may also be useful outside the library to
	 * sanitize user content when direcly using JavaScript.
	 * <p>
	 * Modifies the <code>text</code> if needed. When the text is not proper
	 * XML, returns <code>false</code>.
	 * <p>
	 * 
	 * @deprecated use {@link Utils#removeScript(CharSequence text)} instead.
	 */
	public static boolean removeScript(final CharSequence text) {
		return XSSFilter.removeScript(text);
	}

	/**
	 * Turn a UTF8 encoded string into a JavaScript string literal.
	 * <p>
	 * The <code>delimiter</code> may be a single or double quote.
	 */
	public static String jsStringLiteral(final String value, char delimiter) {
		StringBuilder result = new StringBuilder();
		DomElement.jsStringLiteral(result, value, delimiter);
		return result.toString();
	}

	/**
	 * Turn a UTF8 encoded string into a JavaScript string literal.
	 * <p>
	 * Returns {@link #jsStringLiteral(String value, char delimiter)
	 * jsStringLiteral(value, '\'')}
	 */
	public static final String jsStringLiteral(final String value) {
		return jsStringLiteral(value, '\'');
	}

	static String jsStringLiteral(final CharSequence value, char delimiter) {
		return WString.toWString(value).getJsStringLiteral(delimiter);
	}

	static final String jsStringLiteral(final CharSequence value) {
		return jsStringLiteral(value, '\'');
	}

	/**
	 * Returns contained widgets.
	 * <p>
	 * 
	 * @see WContainerWidget#addWidget(WWidget widget)
	 */
	public List<WWidget> getChildren() {
		return this.children_ != null ? this.children_ : emptyWidgetList_;
	}

	/**
	 * Signal emitted when children have been added or removed.
	 * <p>
	 * 
	 * @see WWebWidget#getChildren()
	 */
	public Signal childrenChanged() {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		return this.otherImpl_.childrenChanged_;
	}

	static String resolveRelativeUrl(final String url) {
		return WApplication.getInstance().resolveRelativeUrl(url);
	}

	void setFormObject(boolean how) {
		this.flags_.set(BIT_FORM_OBJECT, how);
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(this, false);
	}

	static boolean canOptimizeUpdates() {
		return !WApplication.getInstance().getSession().getRenderer()
				.isPreLearning();
	}

	void setZIndex(int zIndex) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.zIndex_ = zIndex;
		this.flags_.set(BIT_ZINDEX_CHANGED);
		this.repaint();
	}

	public boolean isRendered() {
		return this.flags_.get(WWebWidget.BIT_RENDERED);
	}

	public void setCanReceiveFocus(boolean enabled) {
		this.setTabIndex(enabled ? 0 : Integer.MIN_VALUE);
	}

	public boolean isCanReceiveFocus() {
		if (this.otherImpl_ != null) {
			return this.otherImpl_.tabIndex_ != Integer.MIN_VALUE;
		} else {
			return false;
		}
	}

	public boolean isSetFirstFocus() {
		if (this.isVisible() && this.isEnabled()) {
			if (this.isCanReceiveFocus()) {
				this.setFocus(true);
				return true;
			}
			for (int i = 0; i < this.getChildren().size(); i++) {
				if (this.getChildren().get(i).isSetFirstFocus()) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	public void setFocus(boolean focus) {
		this.flags_.set(BIT_GOT_FOCUS, focus);
		this.repaint();
		WApplication app = WApplication.getInstance();
		if (focus) {
			app.setFocus(this.getId(), -1, -1);
		} else {
			if (app.getFocus().equals(this.getId())) {
				app.setFocus("", -1, -1);
			}
		}
	}

	public boolean hasFocus() {
		return WApplication.getInstance().getFocus().equals(this.getId());
	}

	public void setTabIndex(int index) {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		this.otherImpl_.tabIndex_ = index;
		this.flags_.set(BIT_TABINDEX_CHANGED);
		this.repaint();
	}

	public int getTabIndex() {
		if (!(this.otherImpl_ != null)) {
			return this.isCanReceiveFocus() ? 0 : Integer.MIN_VALUE;
		} else {
			return this.otherImpl_.tabIndex_;
		}
	}

	/**
	 * Signal emitted when the widget lost focus.
	 * <p>
	 * This signals is only emitted for a widget that
	 * {@link WWebWidget#isCanReceiveFocus() isCanReceiveFocus()}
	 */
	public EventSignal blurred() {
		return this.voidEventSignal(BLUR_SIGNAL, true);
	}

	/**
	 * Signal emitted when the widget recieved focus.
	 * <p>
	 * This signals is only emitted for a widget that
	 * {@link WWebWidget#isCanReceiveFocus() isCanReceiveFocus()}
	 */
	public EventSignal focussed() {
		return this.voidEventSignal(FOCUS_SIGNAL, true);
	}

	public boolean isScrollVisibilityEnabled() {
		return this.flags_.get(BIT_SCROLL_VISIBILITY_ENABLED);
	}

	public void setScrollVisibilityEnabled(boolean enabled) {
		if (enabled && !(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		if (this.isScrollVisibilityEnabled() != enabled) {
			this.flags_.set(BIT_SCROLL_VISIBILITY_ENABLED, enabled);
			this.flags_.set(BIT_SCROLL_VISIBILITY_CHANGED);
			this.repaint();
		}
	}

	public int getScrollVisibilityMargin() {
		if (!(this.otherImpl_ != null)) {
			return 0;
		} else {
			return this.otherImpl_.scrollVisibilityMargin_;
		}
	}

	public void setScrollVisibilityMargin(int margin) {
		if (this.getScrollVisibilityMargin() != margin) {
			if (!(this.otherImpl_ != null)) {
				this.otherImpl_ = new WWebWidget.OtherImpl(this);
			}
			this.otherImpl_.scrollVisibilityMargin_ = margin;
			if (this.isScrollVisibilityEnabled()) {
				this.flags_.set(BIT_SCROLL_VISIBILITY_CHANGED);
				this.repaint();
			}
		}
	}

	public Signal1<Boolean> scrollVisibilityChanged() {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		return this.otherImpl_.scrollVisibilityChanged_;
	}

	public boolean isScrollVisible() {
		return this.flags_.get(BIT_IS_SCROLL_VISIBLE);
	}

	public void setThemeStyleEnabled(boolean enabled) {
		this.flags_.set(BIT_THEME_STYLE_DISABLED, !enabled);
	}

	public boolean isThemeStyleEnabled() {
		return !this.flags_.get(BIT_THEME_STYLE_DISABLED);
	}

	public int getBaseZIndex() {
		if (!(this.layoutImpl_ != null)) {
			return DEFAULT_BASE_Z_INDEX;
		} else {
			return this.layoutImpl_.baseZIndex_;
		}
	}

	public void setBaseZIndex(int zIndex) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.baseZIndex_ = zIndex;
	}

	void repaint(EnumSet<RepaintFlag> flags) {
		if (this.isStubbed()) {
			final WebRenderer renderer = WApplication.getInstance()
					.getSession().getRenderer();
			if (renderer.isPreLearning()) {
				renderer.learningIncomplete();
			}
		}
		if (!this.flags_.get(BIT_RENDERED)) {
			return;
		}
		super.scheduleRerender(false, flags);
		if (!EnumUtils.mask(flags, RepaintFlag.RepaintToAjax).isEmpty()) {
			this.flags_.set(BIT_REPAINT_TO_AJAX);
		}
	}

	final void repaint(RepaintFlag flag, RepaintFlag... flags) {
		repaint(EnumSet.of(flag, flags));
	}

	final void repaint() {
		repaint(EnumSet.noneOf(RepaintFlag.class));
	}

	void getFormObjects(final Map<String, WObject> formObjects) {
		if (this.flags_.get(BIT_FORM_OBJECT)) {
			formObjects.put(this.getId(), this);
		}
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.children_.get(i).getWebWidget()
						.getSFormObjects(formObjects);
			}
		}
	}

	void doneRerender() {
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.children_.get(i).getWebWidget().doneRerender();
			}
		}
	}

	void updateDom(final DomElement element, boolean all) {
		WApplication app = null;
		if (this.flags_.get(BIT_GEOMETRY_CHANGED)
				|| !this.flags_.get(BIT_HIDE_WITH_VISIBILITY)
				&& this.flags_.get(BIT_HIDDEN_CHANGED) || all) {
			if (this.flags_.get(BIT_HIDE_WITH_VISIBILITY)
					|| !this.flags_.get(BIT_HIDDEN)) {
				if (element.isDefaultInline() != this.flags_.get(BIT_INLINE)) {
					if (this.flags_.get(BIT_INLINE)) {
						if (element.getType() == DomElementType.DomElement_TABLE) {
							element.setProperty(Property.PropertyStyleDisplay,
									"inline-table");
						}
						if (element.getType() == DomElementType.DomElement_LI) {
							element.setProperty(Property.PropertyStyleDisplay,
									"inline");
						} else {
							if (element.getType() != DomElementType.DomElement_TD) {
								if (!(app != null)) {
									app = WApplication.getInstance();
								}
								if (app.getEnvironment().agentIsIElt(9)) {
									element.setProperty(
											Property.PropertyStyleDisplay,
											"inline");
									element.setProperty(
											Property.PropertyStyleZoom, "1");
								} else {
									element.setProperty(
											Property.PropertyStyleDisplay,
											"inline-block");
								}
							}
						}
					} else {
						element.setProperty(Property.PropertyStyleDisplay,
								"block");
					}
				} else {
					if (!all && this.flags_.get(BIT_HIDDEN_CHANGED)) {
						if (element.isDefaultInline() == this.flags_
								.get(BIT_INLINE)) {
							element.setProperty(Property.PropertyStyleDisplay,
									"");
						} else {
							element.setProperty(Property.PropertyStyleDisplay,
									this.flags_.get(BIT_INLINE) ? "inline"
											: "block");
						}
					}
				}
			} else {
				element.setProperty(Property.PropertyStyleDisplay, "none");
			}
		}
		if (this.flags_.get(BIT_ZINDEX_CHANGED) || all) {
			if (this.layoutImpl_ != null) {
				if (this.layoutImpl_.zIndex_ > 0) {
					element.setProperty(Property.PropertyStyleZIndex,
							String.valueOf(this.layoutImpl_.zIndex_));
					element.addPropertyWord(Property.PropertyClass, "Wt-popup");
					if (!all && !this.flags_.get(BIT_STYLECLASS_CHANGED)
							&& this.lookImpl_ != null
							&& this.lookImpl_.styleClass_.length() != 0) {
						element.addPropertyWord(Property.PropertyClass,
								this.lookImpl_.styleClass_);
					}
					if (!(app != null)) {
						app = WApplication.getInstance();
					}
					if (all
							&& app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6
							&& element.getType() == DomElementType.DomElement_DIV) {
						DomElement i = DomElement
								.createNew(DomElementType.DomElement_IFRAME);
						i.setId("sh" + this.getId());
						i.setProperty(Property.PropertyClass, "Wt-shim");
						i.setProperty(Property.PropertySrc, "javascript:false;");
						i.setAttribute("title", "Popup Shim");
						i.setAttribute("tabindex", "-1");
						i.setAttribute("frameborder", "0");
						app.addAutoJavaScript("{var w = "
								+ this.getJsRef()
								+ ";if (w && !Wt3_3_10.isHidden(w)) {var i = Wt3_3_10.getElement('"
								+ i.getId()
								+ "');i.style.width=w.clientWidth + 'px';i.style.height=w.clientHeight + 'px';}}");
						element.addChild(i);
					}
				}
			}
			this.flags_.clear(BIT_ZINDEX_CHANGED);
		}
		if (this.flags_.get(BIT_GEOMETRY_CHANGED) || all) {
			if (this.layoutImpl_ != null) {
				if (!(this.flags_.get(BIT_HIDE_WITH_VISIBILITY) && this.flags_
						.get(BIT_HIDDEN))) {
					switch (this.layoutImpl_.positionScheme_) {
					case Static:
						break;
					case Relative:
						element.setProperty(Property.PropertyStylePosition,
								"relative");
						break;
					case Absolute:
						element.setProperty(Property.PropertyStylePosition,
								"absolute");
						break;
					case Fixed:
						element.setProperty(Property.PropertyStylePosition,
								"fixed");
						break;
					}
				}
				if (this.layoutImpl_.clearSides_.equals(Side.Left)) {
					element.setProperty(Property.PropertyStyleClear, "left");
				} else {
					if (this.layoutImpl_.clearSides_.equals(Side.Right)) {
						element.setProperty(Property.PropertyStyleClear,
								"right");
					} else {
						if (this.layoutImpl_.clearSides_
								.equals(Side.Horizontals)) {
							element.setProperty(Property.PropertyStyleClear,
									"both");
						}
					}
				}
				if (this.layoutImpl_.minimumWidth_.getValue() != 0) {
					String text = this.layoutImpl_.minimumWidth_.isAuto() ? "0px"
							: this.layoutImpl_.minimumWidth_.getCssText();
					element.setProperty(Property.PropertyStyleMinWidth, text);
				}
				if (this.layoutImpl_.minimumHeight_.getValue() != 0) {
					String text = this.layoutImpl_.minimumHeight_.isAuto() ? "0px"
							: this.layoutImpl_.minimumHeight_.getCssText();
					element.setProperty(Property.PropertyStyleMinHeight, text);
				}
				if (!this.layoutImpl_.maximumWidth_.isAuto()) {
					element.setProperty(Property.PropertyStyleMaxWidth,
							this.layoutImpl_.maximumWidth_.getCssText());
				}
				if (!this.layoutImpl_.maximumHeight_.isAuto()) {
					element.setProperty(Property.PropertyStyleMaxHeight,
							this.layoutImpl_.maximumHeight_.getCssText());
				}
				if (this.layoutImpl_.positionScheme_ != PositionScheme.Static) {
					if (!this.layoutImpl_.offsets_[0].isAuto()
							|| !this.layoutImpl_.offsets_[1].isAuto()
							|| !this.layoutImpl_.offsets_[2].isAuto()
							|| !this.layoutImpl_.offsets_[3].isAuto()) {
						for (int i = 0; i < 4; ++i) {
							Property property = properties[i];
							if (!(app != null)) {
								app = WApplication.getInstance();
							}
							if (app.getLayoutDirection() == LayoutDirection.RightToLeft) {
								if (i == 1) {
									property = properties[3];
								} else {
									if (i == 3) {
										property = properties[1];
									}
								}
							}
							if (app.getEnvironment().hasAjax()
									&& !app.getEnvironment().agentIsIElt(9)
									|| !this.layoutImpl_.offsets_[i].isAuto()) {
								element.setProperty(property,
										this.layoutImpl_.offsets_[i]
												.getCssText());
							}
						}
					}
				}
				switch (this.layoutImpl_.verticalAlignment_) {
				case AlignBaseline:
					break;
				case AlignSub:
					element.setProperty(Property.PropertyStyleVerticalAlign,
							"sub");
					break;
				case AlignSuper:
					element.setProperty(Property.PropertyStyleVerticalAlign,
							"super");
					break;
				case AlignTop:
					element.setProperty(Property.PropertyStyleVerticalAlign,
							"top");
					break;
				case AlignTextTop:
					element.setProperty(Property.PropertyStyleVerticalAlign,
							"text-top");
					break;
				case AlignMiddle:
					element.setProperty(Property.PropertyStyleVerticalAlign,
							"middle");
					break;
				case AlignBottom:
					element.setProperty(Property.PropertyStyleVerticalAlign,
							"bottom");
					break;
				case AlignTextBottom:
					element.setProperty(Property.PropertyStyleVerticalAlign,
							"text-bottom");
					break;
				default:
					break;
				}
				if (!this.layoutImpl_.lineHeight_.isAuto()) {
					element.setProperty(Property.PropertyStyleLineHeight,
							this.layoutImpl_.lineHeight_.getCssText());
				}
			}
			this.flags_.clear(BIT_GEOMETRY_CHANGED);
		}
		if (this.width_ != null && (this.flags_.get(BIT_WIDTH_CHANGED) || all)) {
			if (!all || !this.width_.isAuto()) {
				element.setProperty(Property.PropertyStyleWidth,
						this.width_.getCssText());
			}
			this.flags_.clear(BIT_WIDTH_CHANGED);
		}
		if (this.height_ != null
				&& (this.flags_.get(BIT_HEIGHT_CHANGED) || all)) {
			if (!all || !this.height_.isAuto()) {
				element.setProperty(Property.PropertyStyleHeight,
						this.height_.getCssText());
			}
			this.flags_.clear(BIT_HEIGHT_CHANGED);
		}
		if (this.flags_.get(BIT_FLOAT_SIDE_CHANGED) || all) {
			if (this.layoutImpl_ != null) {
				if (this.layoutImpl_.floatSide_ == null) {
					if (this.flags_.get(BIT_FLOAT_SIDE_CHANGED)) {
						element.setProperty(Property.PropertyStyleFloat, "none");
					}
				} else {
					if (!(app != null)) {
						app = WApplication.getInstance();
					}
					boolean ltr = app.getLayoutDirection() == LayoutDirection.LeftToRight;
					switch (this.layoutImpl_.floatSide_) {
					case Left:
						element.setProperty(Property.PropertyStyleFloat,
								ltr ? "left" : "right");
						break;
					case Right:
						element.setProperty(Property.PropertyStyleFloat,
								ltr ? "right" : "left");
						break;
					default:
						;
					}
				}
			}
			this.flags_.clear(BIT_FLOAT_SIDE_CHANGED);
		}
		if (this.layoutImpl_ != null) {
			boolean changed = this.flags_.get(BIT_MARGINS_CHANGED);
			if (changed || all) {
				if (changed || this.layoutImpl_.margin_[0].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginTop,
							this.layoutImpl_.margin_[0].getCssText());
				}
				if (changed || this.layoutImpl_.margin_[1].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginRight,
							this.layoutImpl_.margin_[1].getCssText());
				}
				if (changed || this.layoutImpl_.margin_[2].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginBottom,
							this.layoutImpl_.margin_[2].getCssText());
				}
				if (changed || this.layoutImpl_.margin_[3].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginLeft,
							this.layoutImpl_.margin_[3].getCssText());
				}
				this.flags_.clear(BIT_MARGINS_CHANGED);
			}
		}
		if (this.lookImpl_ != null) {
			if ((this.lookImpl_.toolTip_ != null || this.flags_
					.get(BIT_TOOLTIP_DEFERRED))
					&& (this.flags_.get(BIT_TOOLTIP_CHANGED) || all)) {
				if (!all
						|| (!(this.lookImpl_.toolTip_.length() == 0) || this.flags_
								.get(BIT_TOOLTIP_DEFERRED))) {
					if (!(app != null)) {
						app = WApplication.getInstance();
					}
					if ((this.lookImpl_.toolTipTextFormat_ != TextFormat.PlainText || this.flags_
							.get(BIT_TOOLTIP_DEFERRED))
							&& app.getEnvironment().hasAjax()) {
						app.loadJavaScript("js/ToolTip.js", wtjs10());
						WString tooltipText = this.lookImpl_.toolTip_;
						if (this.lookImpl_.toolTipTextFormat_ == TextFormat.PlainText) {
							tooltipText = escapeText(this.lookImpl_.toolTip_);
						} else {
							if (this.lookImpl_.toolTipTextFormat_ == TextFormat.XHTMLText) {
								boolean res = removeScript(tooltipText);
								if (!res) {
									tooltipText = escapeText(this.lookImpl_.toolTip_);
								}
							}
						}
						String deferred = this.flags_.get(BIT_TOOLTIP_DEFERRED) ? "true"
								: "false";
						element.callJavaScript("Wt3_3_10.toolTip("
								+ app.getJavaScriptClass()
								+ ","
								+ jsStringLiteral(this.getId())
								+ ","
								+ WString.toWString(tooltipText)
										.getJsStringLiteral()
								+ ", "
								+ deferred
								+ ", "
								+ jsStringLiteral(app
										.getTheme()
										.utilityCssClass(
												UtilityCssClassRole.ToolTipInner))
								+ ", "
								+ jsStringLiteral(app
										.getTheme()
										.utilityCssClass(
												UtilityCssClassRole.ToolTipOuter))
								+ ");");
						if (this.flags_.get(BIT_TOOLTIP_DEFERRED)
								&& !this.lookImpl_.loadToolTip_.isConnected()) {
							this.lookImpl_.loadToolTip_.addListener(this,
									new Signal.Listener() {
										public void trigger() {
											WWebWidget.this.loadToolTip();
										}
									});
						}
						element.removeAttribute("title");
					} else {
						element.setAttribute("title",
								this.lookImpl_.toolTip_.toString());
					}
				}
				this.flags_.clear(BIT_TOOLTIP_CHANGED);
			}
			if (this.lookImpl_.decorationStyle_ != null) {
				this.lookImpl_.decorationStyle_.updateDomElement(element, all);
			}
			if (all || this.flags_.get(BIT_STYLECLASS_CHANGED)) {
				if (!all || this.lookImpl_.styleClass_.length() != 0) {
					element.addPropertyWord(Property.PropertyClass,
							this.lookImpl_.styleClass_);
				}
			}
			this.flags_.clear(BIT_STYLECLASS_CHANGED);
		}
		if (!all && this.transientImpl_ != null) {
			for (int i = 0; i < this.transientImpl_.addedStyleClasses_.size(); ++i) {
				element.callJavaScript("$('#" + this.getId() + "').addClass('"
						+ this.transientImpl_.addedStyleClasses_.get(i) + "');");
			}
			for (int i = 0; i < this.transientImpl_.removedStyleClasses_.size(); ++i) {
				element.callJavaScript("$('#" + this.getId()
						+ "').removeClass('"
						+ this.transientImpl_.removedStyleClasses_.get(i)
						+ "');");
			}
			if (!this.transientImpl_.childRemoveChanges_.isEmpty()) {
				if (this.children_ != null
						&& this.children_.size() != this.transientImpl_.addedChildren_
								.size()
						|| this.transientImpl_.specialChildRemove_) {
					for (int i = 0; i < this.transientImpl_.childRemoveChanges_
							.size(); ++i) {
						final String js = this.transientImpl_.childRemoveChanges_
								.get(i);
						if (js.charAt(0) == '_') {
							element.callJavaScript(
									"Wt3_3_10.remove('" + js.substring(1)
											+ "');", true);
						} else {
							element.callJavaScript(js, true);
						}
					}
				} else {
					element.removeAllChildren();
				}
				this.transientImpl_.childRemoveChanges_.clear();
				this.transientImpl_.specialChildRemove_ = false;
			}
		}
		if (all || this.flags_.get(BIT_SELECTABLE_CHANGED)) {
			if (this.flags_.get(BIT_SET_UNSELECTABLE)) {
				element.addPropertyWord(Property.PropertyClass, "unselectable");
				element.setAttribute("unselectable", "on");
				element.setAttribute("onselectstart", "return false;");
			} else {
				if (this.flags_.get(BIT_SET_SELECTABLE)) {
					element.addPropertyWord(Property.PropertyClass,
							"selectable");
					element.setAttribute("unselectable", "off");
					element.setAttribute("onselectstart",
							"event.cancelBubble=true; return true;");
				}
			}
			this.flags_.clear(BIT_SELECTABLE_CHANGED);
		}
		if (this.otherImpl_ != null) {
			if (this.otherImpl_.attributes_ != null) {
				if (all) {
					for (Iterator<Map.Entry<String, String>> i_it = this.otherImpl_.attributes_
							.entrySet().iterator(); i_it.hasNext();) {
						Map.Entry<String, String> i = i_it.next();
						if (i.getKey().equals("style")) {
							element.setProperty(Property.PropertyStyle,
									i.getValue());
						} else {
							element.setAttribute(i.getKey(), i.getValue());
						}
					}
				} else {
					if (this.transientImpl_ != null) {
						for (int i = 0; i < this.transientImpl_.attributesSet_
								.size(); ++i) {
							String attr = this.transientImpl_.attributesSet_
									.get(i);
							if (attr.equals("style")) {
								element.setProperty(Property.PropertyStyle,
										this.otherImpl_.attributes_.get(attr));
							} else {
								element.setAttribute(attr,
										this.otherImpl_.attributes_.get(attr));
							}
						}
					}
				}
			}
			if (all && this.otherImpl_.jsMembers_ != null) {
				for (int i = 0; i < this.otherImpl_.jsMembers_.size(); i++) {
					WWebWidget.OtherImpl.Member member = this.otherImpl_.jsMembers_
							.get(i);
					boolean notHere = false;
					if (this.otherImpl_.jsStatements_ != null) {
						for (int j = 0; j < this.otherImpl_.jsStatements_
								.size(); ++j) {
							final WWebWidget.OtherImpl.JavaScriptStatement jss = this.otherImpl_.jsStatements_
									.get(j);
							if (jss.type == WWebWidget.JavaScriptStatementType.SetMember
									&& jss.data.equals(member.name)) {
								notHere = true;
								break;
							}
						}
					}
					if (notHere) {
						continue;
					}
					this.declareJavaScriptMember(element, member.name,
							member.value);
				}
			}
			if (this.otherImpl_.jsStatements_ != null) {
				for (int i = 0; i < this.otherImpl_.jsStatements_.size(); ++i) {
					final WWebWidget.OtherImpl.JavaScriptStatement jss = this.otherImpl_.jsStatements_
							.get(i);
					switch (jss.type) {
					case SetMember:
						this.declareJavaScriptMember(element, jss.data,
								this.getJavaScriptMember(jss.data));
						break;
					case CallMethod:
						element.callMethod(jss.data);
						break;
					case Statement:
						element.callJavaScript(jss.data);
						break;
					}
				}
				;
				this.otherImpl_.jsStatements_ = null;
			}
		}
		if (this.flags_.get(BIT_HIDE_WITH_VISIBILITY)) {
			if (this.flags_.get(BIT_HIDDEN_CHANGED) || all
					&& this.flags_.get(BIT_HIDDEN)) {
				if (this.flags_.get(BIT_HIDDEN)) {
					element.callJavaScript("$('#" + this.getId()
							+ "').addClass('Wt-hidden');");
					element.setProperty(Property.PropertyStyleVisibility,
							"hidden");
					if (this.flags_.get(BIT_HIDE_WITH_OFFSETS)) {
						element.setProperty(Property.PropertyStylePosition,
								"absolute");
						element.setProperty(Property.PropertyStyleTop,
								"-10000px");
						element.setProperty(Property.PropertyStyleLeft,
								"-10000px");
					}
				} else {
					if (this.flags_.get(BIT_HIDE_WITH_OFFSETS)) {
						if (this.layoutImpl_ != null) {
							switch (this.layoutImpl_.positionScheme_) {
							case Static:
								element.setProperty(
										Property.PropertyStylePosition,
										"static");
								break;
							case Relative:
								element.setProperty(
										Property.PropertyStylePosition,
										"relative");
								break;
							case Absolute:
								element.setProperty(
										Property.PropertyStylePosition,
										"absolute");
								break;
							case Fixed:
								element.setProperty(
										Property.PropertyStylePosition, "fixed");
								break;
							}
							if (!this.layoutImpl_.offsets_[0].isAuto()) {
								element.setProperty(Property.PropertyStyleTop,
										this.layoutImpl_.offsets_[0]
												.getCssText());
							} else {
								element.setProperty(Property.PropertyStyleTop,
										"");
							}
							if (!this.layoutImpl_.offsets_[3].isAuto()) {
								element.setProperty(Property.PropertyStyleLeft,
										this.layoutImpl_.offsets_[3]
												.getCssText());
							} else {
								element.setProperty(Property.PropertyStyleTop,
										"");
							}
						} else {
							element.setProperty(Property.PropertyStylePosition,
									"static");
							element.setProperty(Property.PropertyStyleTop, "");
							element.setProperty(Property.PropertyStyleLeft, "");
						}
					}
					element.callJavaScript("$('#" + this.getId()
							+ "').removeClass('Wt-hidden');");
					element.setProperty(Property.PropertyStyleVisibility,
							"visible");
					element.setProperty(Property.PropertyStyleDisplay, "");
				}
			}
		}
		if (!all && this.flags_.get(BIT_HIDDEN_CHANGED) || all
				&& !this.flags_.get(BIT_HIDDEN)) {
			if (this.transientImpl_ != null
					&& !this.transientImpl_.animation_.isEmpty()) {
				String THIS_JS = "js/WWebWidget.js";
				if (!(app != null)) {
					app = WApplication.getInstance();
				}
				app.loadJavaScript(THIS_JS, wtjs1());
				app.loadJavaScript(THIS_JS, wtjs2());
				if (!this.flags_.get(BIT_HIDE_WITH_VISIBILITY)) {
					StringBuilder ss = new StringBuilder();
					ss.append("Wt3_3_10")
							.append(".animateDisplay(")
							.append(app.getJavaScriptClass())
							.append(",'")
							.append(this.getId())
							.append("',")
							.append(EnumUtils
									.valueOf(this.transientImpl_.animation_
											.getEffects()))
							.append(",")
							.append((int) this.transientImpl_.animation_
									.getTimingFunction().getValue())
							.append(",")
							.append(this.transientImpl_.animation_
									.getDuration())
							.append(",'")
							.append(element
									.getProperty(Property.PropertyStyleDisplay))
							.append("');");
					element.callJavaScript(ss.toString());
					if (all) {
						element.setProperty(Property.PropertyStyleDisplay,
								"none");
					} else {
						element.removeProperty(Property.PropertyStyleDisplay);
					}
				} else {
					StringBuilder ss = new StringBuilder();
					ss.append("Wt3_3_10")
							.append(".animateVisible('")
							.append(this.getId())
							.append("',")
							.append(EnumUtils
									.valueOf(this.transientImpl_.animation_
											.getEffects()))
							.append(",")
							.append((int) this.transientImpl_.animation_
									.getTimingFunction().getValue())
							.append(",")
							.append(this.transientImpl_.animation_
									.getDuration())
							.append(",'")
							.append(element
									.getProperty(Property.PropertyStyleVisibility))
							.append("','")
							.append(element
									.getProperty(Property.PropertyStylePosition))
							.append("','")
							.append(element
									.getProperty(Property.PropertyStyleTop))
							.append("','")
							.append(element
									.getProperty(Property.PropertyStyleLeft))
							.append("');");
					element.callJavaScript(ss.toString());
					if (all) {
						element.setProperty(Property.PropertyStyleVisibility,
								"hidden");
						element.setProperty(Property.PropertyStylePosition,
								"absolute");
						element.setProperty(Property.PropertyStyleTop,
								"-10000px");
						element.setProperty(Property.PropertyStyleLeft,
								"-10000px");
					} else {
						element.removeProperty(Property.PropertyStyleVisibility);
						element.removeProperty(Property.PropertyStylePosition);
						element.removeProperty(Property.PropertyStyleTop);
						element.removeProperty(Property.PropertyStyleLeft);
					}
				}
			}
		}
		this.flags_.clear(BIT_HIDDEN_CHANGED);
		if (this.flags_.get(BIT_GOT_FOCUS)) {
			if (!(app != null)) {
				app = WApplication.getInstance();
			}
			final WEnvironment env = app.getEnvironment();
			element.callJavaScript("setTimeout(function() {var o = "
					+ this.getJsRef() + ";if (o) {if (!$(o).hasClass('"
					+ app.getTheme().getDisabledClass()
					+ "')) {try { o.focus();} catch (e) {}}}}, "
					+ (env.agentIsIElt(9) ? "500" : "10") + ");");
			this.flags_.clear(BIT_GOT_FOCUS);
		}
		if (this.flags_.get(BIT_TABINDEX_CHANGED) || all) {
			if (this.otherImpl_ != null
					&& this.otherImpl_.tabIndex_ != Integer.MIN_VALUE) {
				element.setProperty(Property.PropertyTabIndex,
						String.valueOf(this.otherImpl_.tabIndex_));
			} else {
				if (!all) {
					element.removeAttribute("tabindex");
				}
			}
			this.flags_.clear(BIT_TABINDEX_CHANGED);
		}
		if (all || this.flags_.get(BIT_SCROLL_VISIBILITY_CHANGED)) {
			String SCROLL_JS = "js/ScrollVisibility.js";
			if (!(app != null)) {
				app = WApplication.getInstance();
			}
			if (!app.isJavaScriptLoaded(SCROLL_JS)
					&& this.isScrollVisibilityEnabled()) {
				app.loadJavaScript(SCROLL_JS, wtjs3());
				StringBuilder ss = new StringBuilder();
				ss.append("if (!Wt3_3_10.scrollVisibility) {Wt3_3_10.scrollVisibility = new ");
				ss.append("Wt3_3_10.ScrollVisibility(").append(
						app.getJavaScriptClass() + "); }");
				element.callJavaScript(ss.toString());
			}
			if (this.isScrollVisibilityEnabled()) {
				StringBuilder ss = new StringBuilder();
				ss.append("Wt3_3_10.scrollVisibility.add({");
				ss.append("el:").append(this.getJsRef()).append(',');
				ss.append("margin:").append(this.getScrollVisibilityMargin())
						.append(',');
				ss.append("visible:").append(this.isScrollVisible());
				ss.append("});");
				element.callJavaScript(ss.toString());
				this.flags_.set(BIT_SCROLL_VISIBILITY_LOADED);
			} else {
				if (this.flags_.get(BIT_SCROLL_VISIBILITY_LOADED)) {
					element.callJavaScript("Wt3_3_10.scrollVisibility.remove("
							+ jsStringLiteral(this.getId()) + ");");
					this.flags_.clear(BIT_SCROLL_VISIBILITY_LOADED);
				}
			}
			this.flags_.clear(BIT_SCROLL_VISIBILITY_CHANGED);
		}
		this.renderOk();
		;
		this.transientImpl_ = null;
	}

	boolean domCanBeSaved() {
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				if (!this.children_.get(i).getWebWidget().domCanBeSaved()) {
					return false;
				}
			}
		}
		return true;
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_HIDDEN_CHANGED);
		this.flags_.clear(BIT_GEOMETRY_CHANGED);
		this.flags_.clear(BIT_FLOAT_SIDE_CHANGED);
		this.flags_.clear(BIT_TOOLTIP_CHANGED);
		this.flags_.clear(BIT_MARGINS_CHANGED);
		this.flags_.clear(BIT_STYLECLASS_CHANGED);
		this.flags_.clear(BIT_SELECTABLE_CHANGED);
		this.flags_.clear(BIT_WIDTH_CHANGED);
		this.flags_.clear(BIT_HEIGHT_CHANGED);
		this.flags_.clear(BIT_DISABLED_CHANGED);
		this.flags_.clear(BIT_ZINDEX_CHANGED);
		this.flags_.clear(BIT_TABINDEX_CHANGED);
		this.flags_.clear(BIT_SCROLL_VISIBILITY_CHANGED);
		this.renderOk();
		if (deep && this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.children_.get(i).getWebWidget().propagateRenderOk();
			}
		}
		;
		this.transientImpl_ = null;
	}

	final void propagateRenderOk() {
		propagateRenderOk(true);
	}

	String renderRemoveJs(boolean recursive) {
		String result = "";
		if (this.isRendered() && this.isScrollVisibilityEnabled()) {
			result += "Wt3_3_10.scrollVisibility.remove("
					+ jsStringLiteral(this.getId()) + ");";
			this.flags_.set(BIT_SCROLL_VISIBILITY_CHANGED);
			this.flags_.clear(BIT_SCROLL_VISIBILITY_LOADED);
		}
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				result += this.children_.get(i).getWebWidget()
						.renderRemoveJs(true);
			}
		}
		if (!recursive) {
			if (result.length() == 0) {
				result = "_" + this.getId();
			} else {
				result += "Wt3_3_10.remove('" + this.getId() + "');";
			}
		}
		return result;
	}

	protected void propagateSetEnabled(boolean enabled) {
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				WWidget c = this.children_.get(i);
				if (!c.isDisabled()) {
					c.getWebWidget().propagateSetEnabled(enabled);
				}
			}
		}
	}

	protected void propagateSetVisible(boolean visible) {
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				WWidget c = this.children_.get(i);
				if (!c.isHidden()) {
					c.getWebWidget().propagateSetVisible(visible);
				}
			}
		}
	}

	boolean isStubbed() {
		if (this.flags_.get(BIT_STUBBED)) {
			return true;
		} else {
			WWidget p = this.getParent();
			return p != null ? p.isStubbed() : false;
		}
	}

	protected void enableAjax() {
		if (!this.isStubbed()) {
			for (Iterator<AbstractEventSignal> i_it = this.eventSignals()
					.iterator(); i_it.hasNext();) {
				AbstractEventSignal i = i_it.next();
				final AbstractEventSignal s = i;
				if (s.getName() == WInteractWidget.M_CLICK_SIGNAL) {
					this.repaint(EnumSet.of(RepaintFlag.RepaintToAjax));
				}
				s.senderRepaint();
			}
		}
		if (this.flags_.get(BIT_TOOLTIP_DEFERRED) || this.lookImpl_ != null
				&& this.lookImpl_.toolTipTextFormat_ != TextFormat.PlainText) {
			this.flags_.set(BIT_TOOLTIP_CHANGED);
			this.repaint();
		}
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.children_.get(i).enableAjax();
			}
		}
	}

	void addChild(WWidget child) {
		if (child.getParent() == this) {
			return;
		}
		if (child.getParent() != null) {
			child.setParentWidget((WWidget) null);
			logger.warn(new StringWriter().append(
					"addChild(): reparenting child").toString());
		}
		if (!(this.children_ != null)) {
			this.children_ = new ArrayList<WWidget>();
		}
		this.children_.add(child);
		this.childAdded(child);
	}

	void removeChild(WWidget child) {
		assert this.children_ != null;
		int i = this.children_.indexOf(child);
		assert i != -1;
		if (!this.flags_.get(BIT_IGNORE_CHILD_REMOVES)) {
			String js = child.getWebWidget().renderRemoveJs(false);
			if (!(this.transientImpl_ != null)) {
				this.transientImpl_ = new WWebWidget.TransientImpl();
			}
			this.transientImpl_.childRemoveChanges_.add(js);
			if (js.charAt(0) != '_') {
				this.transientImpl_.specialChildRemove_ = true;
			}
			this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
		}
		child.setParent((WObject) null);
		if (!child.getWebWidget().flags_.get(BIT_BEING_DELETED)) {
			child.getWebWidget().setRendered(false);
		}
		this.children_.remove(0 + i);
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(child.getWebWidget(), true);
		if (this.otherImpl_ != null) {
			this.otherImpl_.childrenChanged_.trigger();
		}
	}

	void setHideWithOffsets(boolean how) {
		if (how) {
			if (!this.flags_.get(BIT_HIDE_WITH_OFFSETS)) {
				this.flags_.set(BIT_HIDE_WITH_VISIBILITY);
				this.flags_.set(BIT_HIDE_WITH_OFFSETS);
				// this.resetLearnedSlot(WWidget.show);
				// this.resetLearnedSlot(WWidget.hide);
				if (this.getParent() != null) {
					this.getParent().setHideWithOffsets(true);
				}
			}
		}
	}

	// protected AbstractEventSignal.LearningListener
	// getStateless(<pointertomember or dependentsizedarray>
	// methodpointertomember or dependentsizedarray>) ;
	WWidget getSelfWidget() {
		WWidget p = null;
		WWidget p_parent = this;
		do {
			p = p_parent;
			p_parent = p.getParent();
		} while (p_parent != null
				&& ((p_parent) instanceof WCompositeWidget ? (WCompositeWidget) (p_parent)
						: null) != null);
		return p;
	}

	void doLoad(WWidget w) {
		w.load();
		if (!w.isLoaded()) {
			logger.error(new StringWriter()
					.append("improper load() implementation: base implementation not called")
					.toString());
		}
	}

	void childAdded(WWidget child) {
		child.setParent(this);
		WWebWidget ww = child.getWebWidget();
		if (ww != null) {
			ww.gotParent();
		}
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(this, false);
		if (this.otherImpl_ != null) {
			this.otherImpl_.childrenChanged_.trigger();
		}
	}

	protected void render(EnumSet<RenderFlag> flags) {
		super.render(flags);
	}

	void signalConnectionsChanged() {
		this.repaint();
	}

	private static final int BIT_INLINE = 0;
	private static final int BIT_HIDDEN = 1;
	private static final int BIT_LOADED = 2;
	private static final int BIT_RENDERED = 3;
	private static final int BIT_STUBBED = 4;
	private static final int BIT_FORM_OBJECT = 5;
	private static final int BIT_IGNORE_CHILD_REMOVES = 6;
	private static final int BIT_GEOMETRY_CHANGED = 7;
	private static final int BIT_HIDE_WITH_OFFSETS = 8;
	private static final int BIT_BEING_DELETED = 9;
	private static final int BIT_DONOT_STUB = 10;
	private static final int BIT_FLOAT_SIDE_CHANGED = 11;
	static final int BIT_REPAINT_TO_AJAX = 12;
	private static final int BIT_HIDE_WITH_VISIBILITY = 13;
	private static final int BIT_HIDDEN_CHANGED = 14;
	static final int BIT_ENABLED = 15;
	private static final int BIT_TOOLTIP_CHANGED = 16;
	private static final int BIT_MARGINS_CHANGED = 17;
	private static final int BIT_STYLECLASS_CHANGED = 18;
	private static final int BIT_SET_UNSELECTABLE = 19;
	private static final int BIT_SET_SELECTABLE = 20;
	private static final int BIT_SELECTABLE_CHANGED = 21;
	private static final int BIT_WIDTH_CHANGED = 22;
	private static final int BIT_HEIGHT_CHANGED = 23;
	private static final int BIT_DISABLED = 24;
	private static final int BIT_DISABLED_CHANGED = 25;
	private static final int BIT_CONTAINS_LAYOUT = 26;
	private static final int BIT_ZINDEX_CHANGED = 27;
	private static final int BIT_TOOLTIP_DEFERRED = 28;
	private static final int BIT_GOT_FOCUS = 29;
	private static final int BIT_TABINDEX_CHANGED = 30;
	private static final int BIT_SCROLL_VISIBILITY_ENABLED = 31;
	private static final int BIT_SCROLL_VISIBILITY_LOADED = 32;
	private static final int BIT_IS_SCROLL_VISIBLE = 33;
	private static final int BIT_SCROLL_VISIBILITY_CHANGED = 34;
	private static final int BIT_THEME_STYLE_DISABLED = 35;
	private static String FOCUS_SIGNAL = "focus";
	private static String BLUR_SIGNAL = "blur";
	private static final int DEFAULT_BASE_Z_INDEX = 100;
	private String elementTagName_;

	private void loadToolTip() {
		if (!(this.lookImpl_.toolTip_ != null)) {
			this.lookImpl_.toolTip_ = new WString();
		}
		this.lookImpl_.toolTip_ = this.getToolTip();
		this.flags_.set(BIT_TOOLTIP_CHANGED);
		this.repaint();
	}

	BitSet flags_;
	private WLength width_;
	private WLength height_;

	static class TransientImpl {
		private static Logger logger = LoggerFactory
				.getLogger(TransientImpl.class);

		public List<String> childRemoveChanges_;
		public List<WWidget> addedChildren_;
		public List<String> addedStyleClasses_;
		public List<String> removedStyleClasses_;
		public List<String> attributesSet_;
		public boolean specialChildRemove_;
		public WAnimation animation_;

		public TransientImpl() {
			this.childRemoveChanges_ = new ArrayList<String>();
			this.addedChildren_ = new ArrayList<WWidget>();
			this.addedStyleClasses_ = new ArrayList<String>();
			this.removedStyleClasses_ = new ArrayList<String>();
			this.attributesSet_ = new ArrayList<String>();
			this.specialChildRemove_ = false;
			this.animation_ = new WAnimation();
		}
	}

	WWebWidget.TransientImpl transientImpl_;

	static class LayoutImpl {
		private static Logger logger = LoggerFactory
				.getLogger(LayoutImpl.class);

		public PositionScheme positionScheme_;
		public Side floatSide_;
		public EnumSet<Side> clearSides_;
		public WLength[] offsets_ = new WLength[4];
		public WLength minimumWidth_;
		public WLength minimumHeight_;
		public WLength maximumWidth_;
		public WLength maximumHeight_;
		public int baseZIndex_;
		public int zIndex_;
		public AlignmentFlag verticalAlignment_;
		public WLength verticalAlignmentLength_;
		public WLength[] margin_ = new WLength[4];
		public WLength lineHeight_;

		public LayoutImpl() {
			this.positionScheme_ = PositionScheme.Static;
			this.floatSide_ = null;
			this.clearSides_ = EnumSet.noneOf(Side.class);
			this.minimumWidth_ = new WLength(0);
			this.minimumHeight_ = new WLength(0);
			this.maximumWidth_ = new WLength();
			this.maximumHeight_ = new WLength();
			this.baseZIndex_ = DEFAULT_BASE_Z_INDEX;
			this.zIndex_ = 0;
			this.verticalAlignment_ = AlignmentFlag.AlignBaseline;
			this.verticalAlignmentLength_ = new WLength();
			this.lineHeight_ = new WLength();
			for (int i = 0; i < 4; ++i) {
				this.offsets_[i] = WLength.Auto;
				this.margin_[i] = new WLength(0);
			}
		}
	}

	private WWebWidget.LayoutImpl layoutImpl_;

	static class LookImpl {
		private static Logger logger = LoggerFactory.getLogger(LookImpl.class);

		public WCssDecorationStyle decorationStyle_;
		public String styleClass_;
		public WString toolTip_;
		public TextFormat toolTipTextFormat_;
		public JSignal loadToolTip_;

		public LookImpl(WWebWidget w) {
			this.decorationStyle_ = null;
			this.styleClass_ = "";
			this.toolTip_ = null;
			this.toolTipTextFormat_ = TextFormat.PlainText;
			this.loadToolTip_ = new JSignal(w, "Wt-loadToolTip");
		}
	}

	private WWebWidget.LookImpl lookImpl_;

	static class DropMimeType {
		private static Logger logger = LoggerFactory
				.getLogger(DropMimeType.class);

		public String hoverStyleClass;

		public DropMimeType() {
			this.hoverStyleClass = "";
		}

		public DropMimeType(final String aHoverStyleClass) {
			this.hoverStyleClass = aHoverStyleClass;
		}
	}

	enum JavaScriptStatementType {
		SetMember, CallMethod, Statement;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	static class OtherImpl {
		private static Logger logger = LoggerFactory.getLogger(OtherImpl.class);

		static class Member {
			private static Logger logger = LoggerFactory
					.getLogger(Member.class);

			public String name;
			public String value;
		}

		static class JavaScriptStatement {
			private static Logger logger = LoggerFactory
					.getLogger(JavaScriptStatement.class);

			public JavaScriptStatement(
					WWebWidget.JavaScriptStatementType aType, final String aData) {
				this.type = aType;
				this.data = aData;
			}

			public WWebWidget.JavaScriptStatementType type;
			public String data;
		}

		public String id_;
		public Map<String, String> attributes_;
		public List<WWebWidget.OtherImpl.Member> jsMembers_;
		public List<WWebWidget.OtherImpl.JavaScriptStatement> jsStatements_;
		public JSignal2<Integer, Integer> resized_;
		public int tabIndex_;
		public JSignal3<String, String, WMouseEvent> dropSignal_;
		public JSignal3<String, String, WTouchEvent> dropSignal2_;
		public Map<String, WWebWidget.DropMimeType> acceptedDropMimeTypes_;
		public Signal childrenChanged_;
		public int scrollVisibilityMargin_;
		public Signal1<Boolean> scrollVisibilityChanged_;
		public JSignal1<Boolean> jsScrollVisibilityChanged_;

		public OtherImpl(final WWebWidget self) {
			this.id_ = null;
			this.attributes_ = null;
			this.jsMembers_ = null;
			this.jsStatements_ = null;
			this.resized_ = null;
			this.tabIndex_ = Integer.MIN_VALUE;
			this.dropSignal_ = null;
			this.dropSignal2_ = null;
			this.acceptedDropMimeTypes_ = null;
			this.childrenChanged_ = new Signal(self);
			this.scrollVisibilityMargin_ = 0;
			this.scrollVisibilityChanged_ = new Signal1<Boolean>(self);
			this.jsScrollVisibilityChanged_ = new JSignal1<Boolean>(self,
					"scrollVisibilityChanged") {
			};
			this.jsScrollVisibilityChanged_.addListener(self,
					new Signal1.Listener<Boolean>() {
						public void trigger(Boolean e1) {
							self.jsScrollVisibilityChanged(e1);
						}
					});
		}
	}

	WWebWidget.OtherImpl otherImpl_;
	List<WWidget> children_;
	private static List<WWidget> emptyWidgetList_ = new ArrayList<WWidget>();

	void renderOk() {
		super.renderOk();
		this.flags_.clear(BIT_REPAINT_TO_AJAX);
	}

	private void calcZIndex() {
		this.layoutImpl_.zIndex_ = -1;
		WWebWidget ww = this.getParentWebWidget();
		if (ww != null) {
			final List<WWidget> children = ww.getChildren();
			int maxZ = 0;
			for (int i = 0; i < children.size(); ++i) {
				WWebWidget wi = children.get(i).getWebWidget();
				if (wi.getBaseZIndex() <= this.getBaseZIndex()) {
					maxZ = Math.max(maxZ, wi.getZIndex());
				}
			}
			this.layoutImpl_.zIndex_ = Math.max(this.getBaseZIndex(),
					maxZ + 100);
		}
	}

	boolean needsToBeRendered() {
		return this.flags_.get(BIT_DONOT_STUB)
				|| !this.flags_.get(BIT_HIDDEN)
				|| !WApplication.getInstance().getSession().getRenderer()
						.isVisibleOnly();
	}

	void getSDomChanges(final List<DomElement> result, WApplication app) {
		if (this.flags_.get(BIT_STUBBED)) {
			if (app.getSession().getRenderer().isPreLearning()) {
				this.getDomChanges(result, app);
				this.scheduleRerender(true);
			} else {
				if (!app.getSession().getRenderer().isVisibleOnly()) {
					this.flags_.clear(BIT_STUBBED);
					DomElement stub = DomElement.getForUpdate(this,
							DomElementType.DomElement_SPAN);
					WWidget self = this.getSelfWidget();
					this.setRendered(true);
					self.render(EnumSet.of(RenderFlag.RenderFull));
					DomElement realElement = this.createDomElement(app);
					app.getTheme().apply(self, realElement, 0);
					stub.unstubWith(realElement,
							!this.flags_.get(BIT_HIDE_WITH_OFFSETS));
					result.add(stub);
				}
			}
		} else {
			this.render(EnumSet.of(RenderFlag.RenderUpdate));
			this.getDomChanges(result, app);
		}
	}

	private void getSFormObjects(final Map<String, WObject> result) {
		if (!this.flags_.get(BIT_STUBBED) && !this.flags_.get(BIT_HIDDEN)
				&& this.flags_.get(BIT_RENDERED)) {
			this.getFormObjects(result);
		}
	}

	WWebWidget getParentWebWidget() {
		WWidget p = this.getParent();
		while (p != null
				&& ((p) instanceof WCompositeWidget ? (WCompositeWidget) (p)
						: null) != null) {
			p = p.getParent();
		}
		return p != null ? p.getWebWidget() : null;
	}

	void gotParent() {
		if (this.isPopup()) {
			this.calcZIndex();
		}
	}

	boolean setAcceptDropsImpl(final String mimeType, boolean accept,
			final String hoverStyleClass) {
		boolean result = false;
		boolean changed = false;
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		if (!(this.otherImpl_.acceptedDropMimeTypes_ != null)) {
			this.otherImpl_.acceptedDropMimeTypes_ = new HashMap<String, WWebWidget.DropMimeType>();
		}
		WWebWidget.DropMimeType i = this.otherImpl_.acceptedDropMimeTypes_
				.get(mimeType);
		if (i == null) {
			if (accept) {
				result = this.otherImpl_.acceptedDropMimeTypes_.isEmpty();
				this.otherImpl_.acceptedDropMimeTypes_.put(mimeType,
						new WWebWidget.DropMimeType(hoverStyleClass));
				changed = true;
			}
		} else {
			if (!accept) {
				this.otherImpl_.acceptedDropMimeTypes_.remove(mimeType);
				changed = true;
			}
		}
		if (changed) {
			String mimeTypes = "";
			for (Iterator<Map.Entry<String, WWebWidget.DropMimeType>> j_it = this.otherImpl_.acceptedDropMimeTypes_
					.entrySet().iterator(); j_it.hasNext();) {
				Map.Entry<String, WWebWidget.DropMimeType> j = j_it.next();
				mimeTypes += "{" + j.getKey() + ":"
						+ j.getValue().hoverStyleClass + "}";
			}
			this.setAttributeValue("amts", mimeTypes);
		}
		if (result && !(this.otherImpl_.dropSignal_ != null)) {
			this.otherImpl_.dropSignal_ = new JSignal3<String, String, WMouseEvent>(
					this, "_drop") {
			};
		}
		if (result && !(this.otherImpl_.dropSignal2_ != null)) {
			this.otherImpl_.dropSignal2_ = new JSignal3<String, String, WTouchEvent>(
					this, "_drop2") {
			};
		}
		return result;
	}

	void setIgnoreChildRemoves(boolean how) {
		if (how) {
			this.flags_.set(BIT_IGNORE_CHILD_REMOVES);
		} else {
			this.flags_.clear(BIT_IGNORE_CHILD_REMOVES);
		}
	}

	boolean isIgnoreChildRemoves() {
		return this.flags_.get(BIT_IGNORE_CHILD_REMOVES);
	}

	private void beingDeleted() {
		this.flags_.set(BIT_BEING_DELETED);
		this.flags_.set(BIT_IGNORE_CHILD_REMOVES);
	}

	void setImplementLayoutSizeAware(boolean aware) {
		if (!aware) {
			if (this.otherImpl_ != null) {
				if (this.otherImpl_.resized_ != null) {
					;
					this.otherImpl_.resized_ = null;
					String v = this.getJavaScriptMember(WT_RESIZE_JS);
					if (v.length() == 1) {
						this.setJavaScriptMember(WT_RESIZE_JS, "");
					} else {
						this.addJavaScriptStatement(
								WWebWidget.JavaScriptStatementType.SetMember,
								WT_RESIZE_JS);
					}
				}
			}
		}
	}

	JSignal2<Integer, Integer> resized() {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		if (!(this.otherImpl_.resized_ != null)) {
			this.otherImpl_.resized_ = new JSignal2<Integer, Integer>(this,
					"resized") {
			};
			this.otherImpl_.resized_.addListener(this,
					new Signal2.Listener<Integer, Integer>() {
						public void trigger(Integer e1, Integer e2) {
							WWebWidget.this.layoutSizeChanged(e1, e2);
						}
					});
			String v = this.getJavaScriptMember(WT_RESIZE_JS);
			if (v.length() == 0) {
				this.setJavaScriptMember(WT_RESIZE_JS, "0");
			} else {
				this.addJavaScriptStatement(
						WWebWidget.JavaScriptStatementType.SetMember,
						WT_RESIZE_JS);
			}
		}
		return this.otherImpl_.resized_;
	}

	private void addJavaScriptStatement(
			WWebWidget.JavaScriptStatementType type, final String data) {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		if (!(this.otherImpl_.jsStatements_ != null)) {
			this.otherImpl_.jsStatements_ = new ArrayList<WWebWidget.OtherImpl.JavaScriptStatement>();
		}
		final List<WWebWidget.OtherImpl.JavaScriptStatement> v = this.otherImpl_.jsStatements_;
		if (type == WWebWidget.JavaScriptStatementType.SetMember) {
			for (int i = 0; i < v.size(); ++i) {
				if (v.get(i).type == WWebWidget.JavaScriptStatementType.SetMember
						&& v.get(i).data.equals(data)) {
					return;
				}
			}
		}
		if (v.isEmpty() || v.get(v.size() - 1).type != type
				|| !v.get(v.size() - 1).data.equals(data)) {
			v.add(new WWebWidget.OtherImpl.JavaScriptStatement(type, data));
		}
	}

	private int indexOfJavaScriptMember(final String name) {
		if (this.otherImpl_ != null && this.otherImpl_.jsMembers_ != null) {
			for (int i = 0; i < this.otherImpl_.jsMembers_.size(); i++) {
				if (this.otherImpl_.jsMembers_.get(i).name.equals(name)) {
					return i;
				}
			}
		}
		return -1;
	}

	private void declareJavaScriptMember(final DomElement element,
			final String name, final String value) {
		if (name.charAt(0) != ' ') {
			if (name.equals(WT_RESIZE_JS) && this.otherImpl_.resized_ != null) {
				StringBuilder combined = new StringBuilder();
				if (value.length() > 1) {
					combined.append(name)
							.append("=function(s,w,h) {")
							.append(WApplication.getInstance()
									.getJavaScriptClass())
							.append("._p_.propagateSize(s,w,h);").append("(")
							.append(value).append(")(s,w,h);").append("}");
				} else {
					combined.append(name)
							.append("=")
							.append(WApplication.getInstance()
									.getJavaScriptClass())
							.append("._p_.propagateSize");
				}
				element.callMethod(combined.toString());
			} else {
				if (value.length() > 0) {
					element.callMethod(name + "=" + value);
				} else {
					element.callMethod(name + "=null");
				}
			}
		} else {
			element.callJavaScript(value);
		}
	}

	private WString getStoredToolTip() {
		return this.lookImpl_ != null && this.lookImpl_.toolTip_ != null ? this.lookImpl_.toolTip_
				: WString.Empty;
	}

	private void undoSetFocus() {
	}

	private void jsScrollVisibilityChanged(boolean visible) {
		this.flags_.set(BIT_IS_SCROLL_VISIBLE, visible);
		if (this.otherImpl_ != null) {
			this.otherImpl_.scrollVisibilityChanged_.trigger(visible);
		}
	}

	void setRendered(boolean rendered) {
		if (rendered) {
			this.flags_.set(BIT_RENDERED);
		} else {
			this.flags_.clear(BIT_RENDERED);
			this.renderOk();
			if (this.children_ != null) {
				for (int i = 0; i < this.children_.size(); ++i) {
					this.children_.get(i).getWebWidget().setRendered(false);
				}
			}
		}
	}

	void setId(DomElement element, WApplication app) {
		if (!app.getEnvironment().agentIsSpiderBot() || this.otherImpl_ != null
				&& this.otherImpl_.id_ != null) {
			if (!this.flags_.get(BIT_FORM_OBJECT)) {
				element.setId(this.getId());
			} else {
				element.setName(this.getId());
			}
		}
	}

	WWebWidget getWebWidget() {
		return this;
	}

	EventSignal voidEventSignal(String name, boolean create) {
		AbstractEventSignal b = this.getEventSignal(name);
		if (b != null) {
			return (EventSignal) b;
		} else {
			if (!create) {
				return null;
			} else {
				EventSignal result = new EventSignal(name, this);
				this.addEventSignal(result);
				return result;
			}
		}
	}

	EventSignal1<WKeyEvent> keyEventSignal(String name, boolean create) {
		AbstractEventSignal b = this.getEventSignal(name);
		if (b != null) {
			return (EventSignal1<WKeyEvent>) b;
		} else {
			if (!create) {
				return null;
			} else {
				EventSignal1<WKeyEvent> result = new EventSignal1<WKeyEvent>(
						name, this, WKeyEvent.templateEvent);
				this.addEventSignal(result);
				return result;
			}
		}
	}

	EventSignal1<WMouseEvent> mouseEventSignal(String name, boolean create) {
		AbstractEventSignal b = this.getEventSignal(name);
		if (b != null) {
			return (EventSignal1<WMouseEvent>) b;
		} else {
			if (!create) {
				return null;
			} else {
				EventSignal1<WMouseEvent> result = new EventSignal1<WMouseEvent>(
						name, this, WMouseEvent.templateEvent);
				this.addEventSignal(result);
				return result;
			}
		}
	}

	EventSignal1<WScrollEvent> scrollEventSignal(String name, boolean create) {
		AbstractEventSignal b = this.getEventSignal(name);
		if (b != null) {
			return (EventSignal1<WScrollEvent>) b;
		} else {
			if (!create) {
				return null;
			} else {
				EventSignal1<WScrollEvent> result = new EventSignal1<WScrollEvent>(
						name, this, WScrollEvent.templateEvent);
				this.addEventSignal(result);
				return result;
			}
		}
	}

	EventSignal1<WTouchEvent> touchEventSignal(String name, boolean create) {
		AbstractEventSignal b = this.getEventSignal(name);
		if (b != null) {
			return (EventSignal1<WTouchEvent>) b;
		} else {
			if (!create) {
				return null;
			} else {
				EventSignal1<WTouchEvent> result = new EventSignal1<WTouchEvent>(
						name, this, WTouchEvent.templateEvent);
				this.addEventSignal(result);
				return result;
			}
		}
	}

	EventSignal1<WGestureEvent> gestureEventSignal(String name, boolean create) {
		AbstractEventSignal b = this.getEventSignal(name);
		if (b != null) {
			return (EventSignal1<WGestureEvent>) b;
		} else {
			if (!create) {
				return null;
			} else {
				EventSignal1<WGestureEvent> result = new EventSignal1<WGestureEvent>(
						name, this, WGestureEvent.templateEvent);
				this.addEventSignal(result);
				return result;
			}
		}
	}

	protected void updateSignalConnection(final DomElement element,
			final AbstractEventSignal signal, String name, boolean all) {
		if (name.charAt(0) != 'M' && signal.needsUpdate(all)) {
			element.setEventSignal(name, signal);
			signal.updateOk();
		}
	}

	protected void parentResized(WWidget parent, EnumSet<Orientation> directions) {
		if (this.flags_.get(BIT_CONTAINS_LAYOUT)) {
			if (this.children_ != null) {
				for (int i = 0; i < this.children_.size(); ++i) {
					WWidget c = this.children_.get(i);
					if (!c.isHidden()) {
						c.getWebWidget().parentResized(parent, directions);
					}
				}
			}
		}
	}

	protected final void parentResized(WWidget parent, Orientation direction,
			Orientation... directions) {
		parentResized(parent, EnumSet.of(direction, directions));
	}

	void containsLayout() {
		if (!this.flags_.get(BIT_CONTAINS_LAYOUT)) {
			this.flags_.set(BIT_CONTAINS_LAYOUT);
			WWebWidget p = this.getParentWebWidget();
			if (p != null) {
				p.containsLayout();
			}
		}
	}

	static Property[] properties = { Property.PropertyStyleTop,
			Property.PropertyStyleRight, Property.PropertyStyleBottom,
			Property.PropertyStyleLeft };

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"animateDisplay",
				"function(r,E,F,G,H,I){var j=r.WT,D=function(y,m,q,o,n){var J=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],K=[0,1,3,2,4,5],s=j.vendorPrefix(j.styleAttribute(\"animation\")),t=j.vendorPrefix(j.styleAttribute(\"transition\")),z=j.vendorPrefix(j.styleAttribute(\"transform\")),f=$(\"#\"+y),a=f.get(0),M=s==\"Webkit\"?\"webkitAnimationEnd\":\"animationend\",u=t==\"Webkit\"?\"webkitTransitionEnd\":\"transitionend\";if(f.css(\"display\")!==n){var p=a.parentNode; if(p.wtAnimateChild)p.wtAnimateChild(j,f.get(0),m,q,o,{display:n});else{function A(){d(a,{animationDuration:o+\"ms\"},h);e&&f.removeClass(\"in\");var b;switch(l){case 5:b=\"pop\";break;case 1:b=e?\"slide\":\"slide reverse\";break;case 2:b=e?\"slide reverse\":\"slide\";break}b+=e?\" out\":\" in\";if(m&256)b+=\" fade\";e||v();f.addClass(b);f.one(M,function(){e||(b=b.replace(\" in\",\"\"));f.removeClass(b);if(e)a.style.display=n;d(a,h);w()})}function N(){B(\"width\",l==1?\"left\":\"right\",l==1,\"X\")}function O(){B(\"height\",l==4? \"top\":\"bottom\",l==4,\"Y\")}function B(b,i,k,g){e||v();b=j.px(a,b);i=(j.px(a,i)+b)*(k?-1:1);var c;if(e){d(a,{transform:\"translate\"+g+\"(0px)\"},h);c=i}else{d(a,{transform:\"translate\"+g+\"(\"+i+\"px)\"},h);c=0}if(m&256)d(a,{opacity:e?1:0},h);setTimeout(function(){d(a,{transition:\"all \"+o+\"ms \"+x,transform:\"translate\"+g+\"(\"+c+\"px)\"},h);if(m&256)d(a,{opacity:e?0:1});f.one(u,function(){if(e)a.style.display=n;d(a,h);w()})},0)}function P(){var b,i,k={},g;if(e){i=f.height()+\"px\";d(a,{height:i,overflow:\"hidden\"}, h);if(l==4&&a.childNodes.length==1){g=a.firstChild;d(g,{transform:\"translateY(0)\"},k);j.hasTag(g,\"TABLE\")||d(g,{display:\"block\"},k)}b=\"0px\"}else{var c=$(p),C={};d(p,{height:c.height()+\"px\",overflow:\"hidden\"},C);v();if(f.height()==0)a.style.height=\"auto\";b=f.height()+\"px\";d(a,{height:\"0px\",overflow:\"hidden\"},h);d(p,C);if(l==4){d(a,{WebkitBackfaceVisibility:\"visible\"},h);a.scrollTop=1E3}}if(m&256)d(a,{opacity:e?1:0},h);i=a.clientHeight;setTimeout(function(){d(a,{transition:\"all \"+o+\"ms \"+x,height:b}, h);if(m&256)d(a,{opacity:e?0:1});g&&d(g,{transition:\"all \"+o+\"ms \"+x,transform:\"translateY(-\"+i+\")\"},k);f.one(u,function(){if(e)a.style.display=n;d(a,h);if(l==4){a.scrollTop=0;g&&d(g,k)}w()})},0)}function v(){a.style.display=n;a.wtPosition&&a.wtPosition();window.onshow&&window.onshow()}function w(){a.wtAnimatedHidden&&a.wtAnimatedHidden(e);f.removeClass(\"animating\");r.layouts2&&r.layouts2.setElementDirty(a)}function d(b,i,k){var g;for(g in i){var c=g;if(c==\"animationDuration\"&&s!=\"\")c=s+c.substring(0, 1).toUpperCase()+c.substring(1);else if(c==\"transform\"&&z!=\"\")c=z+c.substring(0,1).toUpperCase()+c.substring(1);else if(c==\"transition\"&&t!=\"\")c=t+c.substring(0,1).toUpperCase()+c.substring(1);if(k&&typeof k[c]===\"undefined\")k[c]=b.style[c];b.style[c]=i[g]}}if(f.hasClass(\"animating\"))$(a).one(u,function(){D(y,m,q,o,n)});else{f.addClass(\"animating\");var l=m&255,e=n===\"none\",x=J[e?K[q]:q],h={};setTimeout(function(){var b=f.css(\"position\");b=b===\"absolute\"||b===\"fixed\";switch(l){case 4:case 3:b?O(): P();break;case 1:case 2:b?N():A();break;case 0:case 5:A();break}},0)}}}};D(E,F,G,H,I)}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "animateVisible",
				"function(){}");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"toolTip",
				"function(l,m,n,s,t,u){var d=$(\"#\"+m),e=d.get(0),o=l.WT,i=e.toolTip;if(!i)e.toolTip=new (function(){function v(){$(\"#\"+m+\":hover\").length||p()}function w(){f=true;l.emit(e,\"Wt-loadToolTip\")}function p(){clearTimeout(g);setTimeout(function(){if(!j)if(a){$(a).parent().remove();a=null;clearInterval(c);c=null}},x)}function q(b){clearTimeout(g);k=o.pageCoordinates(b);a||(g=setTimeout(function(){e.toolTip.showToolTip()},y))}var g=null,c=null,k=null,a= null,y=500,x=200,f=false,h=n,j=false;this.setToolTipText=function(b){h=b;if(f){this.showToolTip();clearTimeout(g);f=false}};this.showToolTip=function(){s&&!h&&!f&&w();if(h){a=document.createElement(\"div\");a.className=t;a.innerHTML=h;outerDiv=document.createElement(\"div\");outerDiv.className=u;document.body.appendChild(outerDiv);outerDiv.appendChild(a);var b=k.x,r=k.y;o.fitToWindow(outerDiv,b+10,r+10,b-10,r-10);$(a).mouseenter(function(){j=true});$(a).mouseleave(function(){j=false})}clearInterval(c); c=null;c=setInterval(function(){v()},200)};d.mouseenter(q);d.mousemove(q);d.mouseleave(p)});i&&i.setToolTipText(n)}");
	}

	static WJavaScriptPreamble wtjs3() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"ScrollVisibility",
				"function(g){function l(a){if(a.style.visibility==\"hidden\"||a.style.display==\"none\"||$(a).hasClass(\"out\"))return false;else return(a=a.parentNode)&&!n.hasTag(a,\"BODY\")?l(a):true}function o(a,c){if(!l(a))return false;var e=n.widgetPageCoordinates(a),d=e.x-document.body.scrollLeft-document.documentElement.scrollLeft;e=e.y-document.body.scrollTop-document.documentElement.scrollTop;var f=n.windowSize(),r=a.offsetHeight,p=-c,s=f.x+2*c,q=-c; c=f.y+2*c;return d+a.offsetWidth>=p&&p+s>=d&&e+r>=q&&q+c>=e}function h(){if(i)for(var a in b){if(b.hasOwnProperty(a)){var c=l(b[a].el);if(b[a].visibleIfNotHidden&&b[a].visible!==c){b[a].visible=c;g.emit(b[a].el,\"scrollVisibilityChanged\",c)}}}else for(a in b)if(b.hasOwnProperty(a)){c=o(b[a].el,b[a].margin);if(c!==b[a].visible){b[a].visible=c;g.emit(b[a].el,\"scrollVisibilityChanged\",c)}}}function t(a){for(var c=0;c<a.length;++c){var e=a[c],d=e.target,f=d.id;d=l(d);if(e.intersectionRatio>0||e.intersectionRect.top!== 0||e.intersectionRect.left!==0){b[f].visibleIfNotHidden=true;if(b[f].visible!==d){b[f].visible=d;g.emit(b[f].el,\"scrollVisibilityChanged\",d)}}else{b[f].visibleIfNotHidden=false;if(b[f].visible){b[f].visible=false;g.emit(b[f].el,\"scrollVisibilityChanged\",false)}}}}function u(){if(j){j.observe(document,{childList:true,attributes:true,subtree:true,characterData:true});if(!i){window.addEventListener(\"resize\",h,true);window.addEventListener(\"scroll\",h,true)}}else m=setInterval(h,100)}function v(){if(j){j.disconnect(); if(!i){window.removeEventListener(\"resize\",h,{capture:true});window.removeEventListener(\"scroll\",h,{capture:true})}}else if(m){clearInterval(m);m=null}}var n=g.WT,k=0,b={},i=false,j=null,m=null;if(window.hasOwnProperty(\"MutationObserver\"))j=new MutationObserver(h);this.add=function(a){k===0&&u();var c=a.el.id,e=c in b;i&&e&&b[c].observer&&b[c].observer.disconnect();var d=o(a.el,a.margin);if(a.visible!==d){a.visible=d;g.emit(a.el,\"scrollVisibilityChanged\",d)}b[c]=a;if(i){d=new IntersectionObserver(t, {rootMargin:\"\"+a.margin+\"px\"});d.observe(a.el);b[c].observer=d}e||++k};this.remove=function(a){if(k!==0){if(a in b){i&&b[a].observer&&b[a].observer.disconnect();delete b[a];--k}k===0&&v()}}}");
	}

	static WLength nonNegative(final WLength w) {
		if (w.isAuto()) {
			return w;
		} else {
			return new WLength(Math.abs(w.getValue()), w.getUnit());
		}
	}
}
