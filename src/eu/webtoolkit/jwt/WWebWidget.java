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

	public void setOffsets(WLength offset, EnumSet<Side> sides) {
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
			logger.error(new StringWriter().append(
					"offset(Side) with invalid side: ").append(
					String.valueOf((int) s.getValue())).toString());
			return new WLength();
		}
	}

	public void resize(WLength width, WLength height) {
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

	public void setMinimumSize(WLength width, WLength height) {
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

	public void setMaximumSize(WLength width, WLength height) {
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

	public void setLineHeight(WLength height) {
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

	public void setMargin(WLength margin, EnumSet<Side> sides) {
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
			logger.error(new StringWriter().append(
					"margin(Side) with invalid side: ").append(
					String.valueOf((int) side.getValue())).toString());
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

	public void setHidden(boolean hidden, WAnimation animation) {
		if (canOptimizeUpdates()
				&& (animation.isEmpty() && hidden == this.isHidden())) {
			return;
		}
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
				return true;
			}
		}
	}

	public void setDisabled(boolean disabled) {
		if (canOptimizeUpdates() && disabled == this.flags_.get(BIT_DISABLED)) {
			return;
		}
		this.flags_.set(BIT_DISABLED, disabled);
		this.flags_.set(BIT_DISABLED_CHANGED);
		this.propagateSetEnabled(!disabled);
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
		this.flags_.set(BIT_GEOMETRY_CHANGED);
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

	public void setDecorationStyle(WCssDecorationStyle style) {
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl();
		}
		this.lookImpl_.decorationStyle_ = style;
	}

	public WCssDecorationStyle getDecorationStyle() {
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl();
		}
		if (!(this.lookImpl_.decorationStyle_ != null)) {
			this.lookImpl_.decorationStyle_ = new WCssDecorationStyle();
			this.lookImpl_.decorationStyle_.setWebWidget(this);
		}
		return this.lookImpl_.decorationStyle_;
	}

	public void setStyleClass(String styleClass) {
		if (canOptimizeUpdates() && styleClass.equals(this.getStyleClass())) {
			return;
		}
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl();
		}
		this.lookImpl_.styleClass_ = styleClass;
		this.flags_.set(BIT_STYLECLASS_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	public String getStyleClass() {
		return this.lookImpl_ != null ? this.lookImpl_.styleClass_ : "";
	}

	public void addStyleClass(String styleClass, boolean force) {
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl();
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

	public void removeStyleClass(String styleClass, boolean force) {
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl();
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

	public boolean hasStyleClass(String styleClass) {
		if (!(this.lookImpl_ != null)) {
			return false;
		}
		String currentClass = this.lookImpl_.styleClass_;
		Set<String> classes = new HashSet<String>();
		StringUtils.split(classes, currentClass, " ", true);
		return classes.contains(styleClass) != false;
	}

	public void setVerticalAlignment(AlignmentFlag alignment, WLength length) {
		if (!EnumUtils.mask(AlignmentFlag.AlignHorizontalMask, alignment)
				.isEmpty()) {
			logger.error(new StringWriter().append(
					"setVerticalAlignment(): alignment ").append(
					String.valueOf(alignment.getValue())).append(
					" is not vertical").toString());
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

	public void setToolTip(CharSequence text, TextFormat textFormat) {
		if (canOptimizeUpdates() && this.getToolTip().equals(text)) {
			return;
		}
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl();
		}
		if (!(this.lookImpl_.toolTip_ != null)) {
			this.lookImpl_.toolTip_ = new WString();
		}
		this.lookImpl_.toolTip_ = WString.toWString(text);
		this.lookImpl_.toolTipTextFormat_ = textFormat;
		this.flags_.set(BIT_TOOLTIP_CHANGED);
		this.repaint();
	}

	public WString getToolTip() {
		return this.lookImpl_ != null && this.lookImpl_.toolTip_ != null ? this.lookImpl_.toolTip_
				: WString.Empty;
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

	public void setAttributeValue(String name, String value) {
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

	public String getAttributeValue(String name) {
		if (this.otherImpl_ != null) {
			String i = this.otherImpl_.attributes_.get(name);
			if (i != null) {
				return i;
			}
		}
		return "";
	}

	public void setJavaScriptMember(String name, String value) {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		if (!(this.otherImpl_.jsMembers_ != null)) {
			this.otherImpl_.jsMembers_ = new ArrayList<WWebWidget.OtherImpl.Member>();
		}
		List<WWebWidget.OtherImpl.Member> members = this.otherImpl_.jsMembers_;
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

	public String getJavaScriptMember(String name) {
		int index = this.indexOfJavaScriptMember(name);
		if (index != -1) {
			return this.otherImpl_.jsMembers_.get(index).value;
		} else {
			return "";
		}
	}

	public void callJavaScriptMember(String name, String args) {
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

	public void setTabIndex(int index) {
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				WWidget c = this.children_.get(i);
				c.setTabIndex(index);
			}
		}
	}

	public int getTabIndex() {
		if (this.children_ != null) {
			int result = 0;
			for (int i = 0; i < this.children_.size(); ++i) {
				WWidget c = this.children_.get(i);
				result = Math.max(result, c.getTabIndex());
			}
			return result;
		} else {
			return 0;
		}
	}

	public int getZIndex() {
		if (this.layoutImpl_ != null) {
			return this.layoutImpl_.zIndex_;
		} else {
			return 0;
		}
	}

	public void setId(String id) {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		if (!(this.otherImpl_.id_ != null)) {
			this.otherImpl_.id_ = "";
		}
		this.otherImpl_.id_ = id;
	}

	public WWidget find(String name) {
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

	public WWidget findById(String id) {
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

	public void doJavaScript(String javascript) {
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

	DomElement createDomElement(WApplication app) {
		this.setRendered(true);
		DomElement result = DomElement.createNew(this.getDomElementType());
		this.setId(result, app);
		this.updateDom(result, true);
		return result;
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
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

	public DomElement createActualElement(WWidget self, WApplication app) {
		this.flags_.clear(BIT_STUBBED);
		DomElement result = this.createDomElement(app);
		app.getTheme().apply(self, result,
				ElementThemeRole.MainElementThemeRole);
		String styleClass = result.getProperty(Property.PropertyClass);
		if (styleClass.length() != 0) {
			if (!(this.lookImpl_ != null)) {
				this.lookImpl_ = new WWebWidget.LookImpl();
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
	 * Escape HTML control characters in the text, to display literally
	 * (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated use {@link Utils#htmlEncode(WString text, EnumSet flags)}
	 *             instead.
	 */
	public static WString escapeText(CharSequence text, boolean newlinestoo) {
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
	public static final WString escapeText(CharSequence text) {
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
	public static String escapeText(String text, boolean newlinestoo) {
		EscapeOStream sout = new EscapeOStream();
		if (newlinestoo) {
			sout.pushEscape(EscapeOStream.RuleSet.PlainTextNewLines);
		} else {
			sout.pushEscape(EscapeOStream.RuleSet.PlainText);
		}
		StringUtils.sanitizeUnicode(sout, text);
		text = sout.toString();
		return text;
	}

	/**
	 * Escape HTML control characters in the text, to display literally
	 * (<b>deprecated</b>).
	 * <p>
	 * Returns {@link #escapeText(String text, boolean newlinestoo)
	 * escapeText(text, false)}
	 */
	public static final String escapeText(String text) {
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
	public static boolean removeScript(CharSequence text) {
		return XSSFilter.removeScript(text);
	}

	/**
	 * Turn a UTF8 encoded string into a JavaScript string literal.
	 * <p>
	 * The <code>delimiter</code> may be a single or double quote.
	 */
	public static String jsStringLiteral(String value, char delimiter) {
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
	public static final String jsStringLiteral(String value) {
		return jsStringLiteral(value, '\'');
	}

	static String jsStringLiteral(CharSequence value, char delimiter) {
		return WString.toWString(value).getJsStringLiteral(delimiter);
	}

	static final String jsStringLiteral(CharSequence value) {
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

	static String resolveRelativeUrl(String url) {
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
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint();
	}

	public boolean isRendered() {
		return this.flags_.get(WWebWidget.BIT_RENDERED);
	}

	void repaint(EnumSet<RepaintFlag> flags) {
		if (this.isStubbed()) {
			WebRenderer renderer = WApplication.getInstance().getSession()
					.getRenderer();
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

	void getFormObjects(Map<String, WObject> formObjects) {
		if (this.flags_.get(BIT_FORM_OBJECT)) {
			formObjects.put(this.getId(), this);
		}
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.children_.get(i).getWebWidget().getSFormObjects(
						formObjects);
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

	void updateDom(DomElement element, boolean all) {
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
				if (this.layoutImpl_.zIndex_ > 0) {
					element.setProperty(Property.PropertyStyleZIndex, String
							.valueOf(this.layoutImpl_.zIndex_));
					element.setProperty(Property.PropertyClass, StringUtils
							.addWord(element
									.getProperty(Property.PropertyClass),
									"Wt-popup"));
					if (!all && !this.flags_.get(BIT_STYLECLASS_CHANGED)
							&& this.lookImpl_ != null
							&& this.lookImpl_.styleClass_.length() != 0) {
						element.setProperty(Property.PropertyClass, StringUtils
								.addWord(element
										.getProperty(Property.PropertyClass),
										this.lookImpl_.styleClass_));
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
						i
								.setProperty(Property.PropertySrc,
										"javascript:false;");
						i.setAttribute("title", "Popup Shim");
						i.setAttribute("tabindex", "-1");
						i.setAttribute("frameborder", "0");
						app
								.addAutoJavaScript("{var w = "
										+ this.getJsRef()
										+ ";if (w && !Wt3_3_0.isHidden(w)) {var i = Wt3_3_0.getElement('"
										+ i.getId()
										+ "');i.style.width=w.clientWidth + 'px';i.style.height=w.clientHeight + 'px';}}");
						element.addChild(i);
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
				element.setProperty(Property.PropertyStyleWidth, this.width_
						.getCssText());
			}
			this.flags_.clear(BIT_WIDTH_CHANGED);
		}
		if (this.height_ != null
				&& (this.flags_.get(BIT_HEIGHT_CHANGED) || all)) {
			if (!all || !this.height_.isAuto()) {
				element.setProperty(Property.PropertyStyleHeight, this.height_
						.getCssText());
			}
			this.flags_.clear(BIT_HEIGHT_CHANGED);
		}
		if (this.flags_.get(BIT_FLOAT_SIDE_CHANGED) || all) {
			if (this.layoutImpl_ != null) {
				if (this.layoutImpl_.floatSide_ == null) {
					if (this.flags_.get(BIT_FLOAT_SIDE_CHANGED)) {
						element
								.setProperty(Property.PropertyStyleFloat,
										"none");
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
			if (this.flags_.get(BIT_MARGINS_CHANGED) || all) {
				if (!all || this.layoutImpl_.margin_[0].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginTop,
							this.layoutImpl_.margin_[0].getCssText());
				}
				if (!all || this.layoutImpl_.margin_[1].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginRight,
							this.layoutImpl_.margin_[1].getCssText());
				}
				if (!all || this.layoutImpl_.margin_[2].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginBottom,
							this.layoutImpl_.margin_[2].getCssText());
				}
				if (!all || this.layoutImpl_.margin_[3].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginLeft,
							this.layoutImpl_.margin_[3].getCssText());
				}
				this.flags_.clear(BIT_MARGINS_CHANGED);
			}
		}
		if (this.lookImpl_ != null) {
			if (this.lookImpl_.toolTip_ != null
					&& (this.flags_.get(BIT_TOOLTIP_CHANGED) || all)) {
				if (!all || !(this.lookImpl_.toolTip_.length() == 0)) {
					if (!(app != null)) {
						app = WApplication.getInstance();
					}
					if (this.lookImpl_.toolTipTextFormat_ != TextFormat.PlainText
							&& app.getEnvironment().hasAjax()) {
						app.loadJavaScript("js/ToolTip.js", wtjs10());
						element.callJavaScript("Wt3_3_0.toolTip(Wt3_3_0,"
								+ jsStringLiteral(this.getId())
								+ ","
								+ WString.toWString(this.lookImpl_.toolTip_)
										.getJsStringLiteral() + ");");
					} else {
						element.setAttribute("title", this.lookImpl_.toolTip_
								.toString());
					}
				}
				this.flags_.clear(BIT_TOOLTIP_CHANGED);
			}
			if (this.lookImpl_.decorationStyle_ != null) {
				this.lookImpl_.decorationStyle_.updateDomElement(element, all);
			}
			if (all || this.flags_.get(BIT_STYLECLASS_CHANGED)) {
				if (!all || this.lookImpl_.styleClass_.length() != 0) {
					element.setProperty(Property.PropertyClass, StringUtils
							.addWord(element
									.getProperty(Property.PropertyClass),
									this.lookImpl_.styleClass_));
				}
			}
			this.flags_.clear(BIT_STYLECLASS_CHANGED);
		}
		if (!all && this.transientImpl_ != null) {
			for (int i = 0; i < this.transientImpl_.addedStyleClasses_.size(); ++i) {
				element
						.callJavaScript("$('#" + this.getId() + "').addClass('"
								+ this.transientImpl_.addedStyleClasses_.get(i)
								+ "');");
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
						String js = this.transientImpl_.childRemoveChanges_
								.get(i);
						if (js.charAt(0) == '_') {
							element.callJavaScript("Wt3_3_0.remove('"
									+ js.substring(1) + "');", true);
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
				element.setProperty(Property.PropertyClass, StringUtils
						.addWord(element.getProperty(Property.PropertyClass),
								"unselectable"));
				element.setAttribute("unselectable", "on");
				element.setAttribute("onselectstart", "return false;");
			} else {
				if (this.flags_.get(BIT_SET_SELECTABLE)) {
					element.setProperty(Property.PropertyClass, StringUtils
							.addWord(element
									.getProperty(Property.PropertyClass),
									"selectable"));
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
							element.setProperty(Property.PropertyStyle, i
									.getValue());
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
							WWebWidget.OtherImpl.JavaScriptStatement jss = this.otherImpl_.jsStatements_
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
					WWebWidget.OtherImpl.JavaScriptStatement jss = this.otherImpl_.jsStatements_
							.get(i);
					switch (jss.type) {
					case SetMember:
						this.declareJavaScriptMember(element, jss.data, this
								.getJavaScriptMember(jss.data));
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
								element
										.setProperty(
												Property.PropertyStylePosition,
												"fixed");
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
					ss
							.append("Wt3_3_0")
							.append(".animateDisplay('")
							.append(this.getId())
							.append("',")
							.append(
									EnumUtils
											.valueOf(this.transientImpl_.animation_
													.getEffects()))
							.append(",")
							.append(
									(int) this.transientImpl_.animation_
											.getTimingFunction().getValue())
							.append(",")
							.append(
									this.transientImpl_.animation_
											.getDuration())
							.append(",'")
							.append(
									element
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
					ss
							.append("Wt3_3_0")
							.append(".animateVisible('")
							.append(this.getId())
							.append("',")
							.append(
									EnumUtils
											.valueOf(this.transientImpl_.animation_
													.getEffects()))
							.append(",")
							.append(
									(int) this.transientImpl_.animation_
											.getTimingFunction().getValue())
							.append(",")
							.append(
									this.transientImpl_.animation_
											.getDuration())
							.append(",'")
							.append(
									element
											.getProperty(Property.PropertyStyleVisibility))
							.append("','")
							.append(
									element
											.getProperty(Property.PropertyStylePosition))
							.append("','")
							.append(
									element
											.getProperty(Property.PropertyStyleTop))
							.append("','")
							.append(
									element
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
						element
								.removeProperty(Property.PropertyStyleVisibility);
						element.removeProperty(Property.PropertyStylePosition);
						element.removeProperty(Property.PropertyStyleTop);
						element.removeProperty(Property.PropertyStyleLeft);
					}
				}
			}
		}
		this.flags_.clear(BIT_HIDDEN_CHANGED);
		this.renderOk();
		;
		this.transientImpl_ = null;
	}

	boolean domCanBeSaved() {
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

	String renderRemoveJs() {
		return "_" + this.getId();
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
				AbstractEventSignal s = i;
				if (s.getName() == WInteractWidget.M_CLICK_SIGNAL) {
					this.repaint(EnumSet.of(RepaintFlag.RepaintToAjax));
				}
				s.senderRepaint();
			}
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
			String js = child.getWebWidget().renderRemoveJs();
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

	protected WWidget getSelfWidget() {
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
			logger
					.error(new StringWriter()
							.append(
									"improper load() implementation: base implementation not called")
							.toString());
		}
	}

	void childAdded(WWidget child) {
		child.setParent(this);
		WWebWidget ww = child.getWebWidget();
		if (ww != null) {
			ww.gotParent();
		}
		if (this.flags_.get(BIT_LOADED)) {
			this.doLoad(child);
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

		public LookImpl() {
			this.decorationStyle_ = null;
			this.styleClass_ = "";
			this.toolTip_ = null;
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

		public DropMimeType(String aHoverStyleClass) {
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
					WWebWidget.JavaScriptStatementType aType, String aData) {
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
		public JSignal3<String, String, WMouseEvent> dropSignal_;
		public Map<String, WWebWidget.DropMimeType> acceptedDropMimeTypes_;
		public Signal childrenChanged_;

		public OtherImpl(WWebWidget self) {
			this.id_ = null;
			this.attributes_ = null;
			this.jsMembers_ = null;
			this.jsStatements_ = null;
			this.resized_ = null;
			this.dropSignal_ = null;
			this.acceptedDropMimeTypes_ = null;
			this.childrenChanged_ = new Signal(self);
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
			List<WWidget> children = ww.getChildren();
			int maxZ = 0;
			for (int i = 0; i < children.size(); ++i) {
				WWebWidget wi = children.get(i).getWebWidget();
				maxZ = Math.max(maxZ, wi.getZIndex());
			}
			this.layoutImpl_.zIndex_ = maxZ + 100;
		}
	}

	boolean needsToBeRendered() {
		return this.flags_.get(BIT_DONOT_STUB)
				|| !this.flags_.get(BIT_HIDDEN)
				|| !WApplication.getInstance().getSession().getRenderer()
						.isVisibleOnly();
	}

	void getSDomChanges(List<DomElement> result, WApplication app) {
		if (this.flags_.get(BIT_STUBBED)) {
			if (app.getSession().getRenderer().isPreLearning()) {
				this.getDomChanges(result, app);
				this.scheduleRerender(true);
			} else {
				if (!app.getSession().getRenderer().isVisibleOnly()) {
					this.flags_.clear(BIT_STUBBED);
					DomElement stub = DomElement.getForUpdate(this,
							DomElementType.DomElement_SPAN);
					this.setRendered(true);
					this.render(EnumSet.of(RenderFlag.RenderFull));
					DomElement realElement = this.createDomElement(app);
					app.getTheme().apply(this.getSelfWidget(), realElement, 0);
					stub.unstubWith(realElement, !this.flags_
							.get(BIT_HIDE_WITH_OFFSETS));
					result.add(stub);
				}
			}
		} else {
			this.render(EnumSet.of(RenderFlag.RenderUpdate));
			this.getDomChanges(result, app);
		}
	}

	private void getSFormObjects(Map<String, WObject> result) {
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

	boolean setAcceptDropsImpl(String mimeType, boolean accept,
			String hoverStyleClass) {
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
			WWebWidget.JavaScriptStatementType type, String data) {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl(this);
		}
		if (!(this.otherImpl_.jsStatements_ != null)) {
			this.otherImpl_.jsStatements_ = new ArrayList<WWebWidget.OtherImpl.JavaScriptStatement>();
		}
		List<WWebWidget.OtherImpl.JavaScriptStatement> v = this.otherImpl_.jsStatements_;
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

	private int indexOfJavaScriptMember(String name) {
		if (this.otherImpl_ != null && this.otherImpl_.jsMembers_ != null) {
			for (int i = 0; i < this.otherImpl_.jsMembers_.size(); i++) {
				if (this.otherImpl_.jsMembers_.get(i).name.equals(name)) {
					return i;
				}
			}
		}
		return -1;
	}

	private void declareJavaScriptMember(DomElement element, String name,
			String value) {
		if (name.charAt(0) != ' ') {
			if (name.equals(WT_RESIZE_JS) && this.otherImpl_.resized_ != null) {
				StringBuilder combined = new StringBuilder();
				if (value.length() > 1) {
					combined.append(name).append("=function(s,w,h) {").append(
							WApplication.getInstance().getJavaScriptClass())
							.append("._p_.propagateSize(s,w,h);").append("(")
							.append(value).append(")(s,w,h);").append("}");
				} else {
					combined.append(name).append("=").append(
							WApplication.getInstance().getJavaScriptClass())
							.append("._p_.propagateSize");
				}
				element.callMethod(combined.toString());
			} else {
				if (value.length() > 1) {
					element.callMethod(name + "=" + value);
				} else {
					element.callMethod(name + "=null");
				}
			}
		} else {
			element.callJavaScript(value);
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

	protected void updateSignalConnection(DomElement element,
			AbstractEventSignal signal, String name, boolean all) {
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

	protected void containsLayout() {
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
				"function(B,C,D,E,F){var o=this,A=function(v,l,q,n,m){var G=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],H=[0,1,3,2,4,5],f=$(\"#\"+v),a=f.get(0),r=o.cssPrefix(\"Transition\"),I=r==\"Moz\"?\"animationend\":\"webkitAnimationEnd\",s=r==\"Moz\"?\"transitionend\":\"webkitTransitionEnd\";if(f.css(\"display\")!==m){var p=a.parentNode;if(p.wtAnimateChild)p.wtAnimateChild(o,f.get(0),l,q,n,{display:m});else{function w(){b(a,{animationDuration:n+\"ms\"},h);var c= (k==5?\"pop \":\"\")+(g?\"out\":\"in\");if(l&256)c+=\" fade\";g||t();f.addClass(c);f.one(I,function(){f.removeClass(c);if(g)a.style.display=m;b(a,h)})}function J(){x(\"width\",k==1?\"left\":\"right\",k==1,\"X\")}function K(){x(\"height\",k==4?\"top\":\"bottom\",k==4,\"Y\")}function x(c,i,j,d){g||t();c=o.px(a,c);i=(o.px(a,i)+c)*(j?-1:1);var e;if(g){b(a,{transform:\"translate\"+d+\"(0px)\"},h);e=i}else{b(a,{transform:\"translate\"+d+\"(\"+i+\"px)\"},h);e=0}if(l&256)b(a,{opacity:g?1:0},h);setTimeout(function(){b(a,{transition:\"all \"+n+ \"ms \"+u,transform:\"translate\"+d+\"(\"+e+\"px)\"},h);if(l&256)b(a,{opacity:g?0:1});f.one(s,function(){if(g)a.style.display=m;b(a,h);y()})},0)}function L(){var c,i,j={},d;if(g){i=f.height()+\"px\";b(a,{height:i,overflow:\"hidden\"},h);if(k==4&&a.childNodes.length==1){d=a.firstChild;b(d,{transform:\"translateY(0)\"},j);o.hasTag(d,\"TABLE\")||b(d,{display:\"block\"},j)}c=\"0px\"}else{var e=$(p),z={};b(p,{height:e.height()+\"px\",overflow:\"hidden\"},z);t();if(f.height()==0)a.style.height=\"auto\";c=f.height()+\"px\";b(a,{height:\"0px\", overflow:\"hidden\"},h);b(p,z);if(k==4){b(a,{WebkitBackfaceVisibility:\"visible\"},h);a.scrollTop=1E3}}if(l&256)b(a,{opacity:g?1:0},h);setTimeout(function(){b(a,{transition:\"all \"+n+\"ms \"+u,height:c},h);if(l&256)b(a,{opacity:g?0:1});d&&b(d,{transition:\"all \"+n+\"ms \"+u,transform:\"translateY(-\"+i+\")\"},j);f.one(s,function(){if(g)a.style.display=m;b(a,h);if(k==4){a.scrollTop=0;d&&b(d,j)}y()})},0)}function t(){a.style.display=m;a.wtPosition&&a.wtPosition();window.onshow&&window.onshow()}function y(){a.wtAnimatedHidden&& a.wtAnimatedHidden(g);f.removeClass(\"animating\");Wt.layouts2&&Wt.layouts2.setElementDirty(a)}function b(c,i,j){var d;for(d in i){var e=d;if(e==\"transform\"||e==\"transition\"||e==\"animationDuration\")e=r+e.substring(0,1).toUpperCase()+e.substring(1);if(j&&typeof j[e]===\"undefined\")j[e]=c.style[e];c.style[e]=i[d]}}if(f.hasClass(\"animating\"))$(a).one(s,function(){A(v,l,q,n,m)});else{f.addClass(\"animating\");var k=l&255,g=m===\"none\",u=G[g?H[q]:q],h={};setTimeout(function(){var c=f.css(\"position\");c=c===\"absolute\"|| c===\"fixed\";switch(k){case 4:case 3:c?K():L();break;case 1:case 2:c?J():w();break;case 0:case 5:w();break}},0)}}}};A(B,C,D,E,F)}");
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
				"function(i,j,n){var b=$(\"#\"+j),e=b.get(0),o=e.toolTip;e.toolTip=n;o||new (function(){function p(){$(\"#\"+j+\":hover\").length||k()}function q(){a=document.createElement(\"div\");a.className=\"Wt-tooltip\";a.innerHTML=e.toolTip;document.body.appendChild(a);var c=f.x,l=f.y;i.fitToWindow(a,c+d,l+d,c-d,l-d);g=setInterval(function(){p()},200)}function k(){clearTimeout(h);if(a){$(a).remove();a=null;clearInterval(g);g=null}}function m(c){clearTimeout(h);f=i.pageCoordinates(c); a||(h=setTimeout(function(){q()},r))}var h=null,g=null,f=null,a=null,d=10,r=500;b.mouseenter(m);b.mousemove(m);b.mouseleave(k)})}");
	}

	static WLength nonNegative(WLength w) {
		if (w.isAuto()) {
			return w;
		} else {
			return new WLength(Math.abs(w.getValue()), w.getUnit());
		}
	}
}
