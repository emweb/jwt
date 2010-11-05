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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import eu.webtoolkit.jwt.utils.EnumUtils;

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
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
			throw new WtException("WWebWidget::offset(Side) with invalid side.");
		}
	}

	public void resize(WLength width, WLength height) {
		if (!(this.width_ != null) && !width.isAuto()) {
			this.width_ = new WLength();
		}
		if (this.width_ != null && !this.width_.equals(width)) {
			this.width_ = width;
			this.flags_.set(BIT_WIDTH_CHANGED);
		}
		if (!(this.height_ != null) && !height.isAuto()) {
			this.height_ = new WLength();
		}
		if (this.height_ != null && !this.height_.equals(height)) {
			this.height_ = height;
			this.flags_.set(BIT_HEIGHT_CHANGED);
		}
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		super.resize(width, height);
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
		this.layoutImpl_.minimumWidth_ = width;
		this.layoutImpl_.minimumHeight_ = height;
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public WLength getMinimumWidth() {
		return this.layoutImpl_ != null ? this.layoutImpl_.minimumWidth_
				: WLength.Auto;
	}

	public WLength getMinimumHeight() {
		return this.layoutImpl_ != null ? this.layoutImpl_.minimumHeight_
				: WLength.Auto;
	}

	public void setMaximumSize(WLength width, WLength height) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.maximumWidth_ = width;
		this.layoutImpl_.maximumHeight_ = height;
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
			throw new WtException("WWebWidget::margin(Side) with invalid side");
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

	public void setHidden(boolean hidden) {
		if (canOptimizeUpdates() && hidden == this.isHidden()) {
			return;
		}
		this.flags_.set(BIT_HIDDEN, hidden);
		this.flags_.set(BIT_HIDDEN_CHANGED);
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(this, true);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public boolean isHidden() {
		return this.flags_.get(BIT_HIDDEN);
	}

	boolean isVisible() {
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
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public boolean isPopup() {
		return this.layoutImpl_ != null ? this.layoutImpl_.zIndex_ != 0 : false;
	}

	public void setInline(boolean inl) {
		this.flags_.set(BIT_INLINE, inl);
		// this.resetLearnedSlot(WWidget.show);
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
				this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
			}
		}
		if (force && this.isRendered()) {
			if (!(this.transientImpl_ != null)) {
				this.transientImpl_ = new WWebWidget.TransientImpl();
			}
			this.transientImpl_.addedStyleClasses_.add(styleClass);
			this.transientImpl_.removedStyleClasses_.remove(styleClass);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	public void removeStyleClass(String styleClass, boolean force) {
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl();
		}
		String currentClass = this.lookImpl_.styleClass_;
		Set<String> classes = new HashSet<String>();
		StringUtils.split(classes, currentClass, " ", true);
		if (classes.contains(styleClass) != false) {
			this.lookImpl_.styleClass_ = StringUtils.eraseWord(
					this.lookImpl_.styleClass_, styleClass);
			if (!force) {
				this.flags_.set(BIT_STYLECLASS_CHANGED);
				this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
			}
		}
		if (force && this.isRendered()) {
			if (!(this.transientImpl_ != null)) {
				this.transientImpl_ = new WWebWidget.TransientImpl();
			}
			this.transientImpl_.removedStyleClasses_.add(styleClass);
			this.transientImpl_.addedStyleClasses_.remove(styleClass);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	public void setVerticalAlignment(AlignmentFlag alignment, WLength length) {
		if (!EnumUtils.mask(AlignmentFlag.AlignHorizontalMask, alignment)
				.isEmpty()) {
			WApplication.getInstance().log("warning").append(
					"WWebWidget::setVerticalAlignment(): alignment (").append(
					alignment.toString()).append(
					") is horizontal, expected vertical");
		}
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.verticalAlignment_ = alignment;
		this.layoutImpl_.verticalAlignmentLength_ = length;
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public AlignmentFlag getVerticalAlignment() {
		return this.layoutImpl_ != null ? this.layoutImpl_.verticalAlignment_
				: AlignmentFlag.AlignBaseline;
	}

	public WLength getVerticalAlignmentLength() {
		return this.layoutImpl_ != null ? this.layoutImpl_.verticalAlignmentLength_
				: WLength.Auto;
	}

	public void setToolTip(CharSequence message) {
		if (canOptimizeUpdates() && this.getToolTip().equals(message)) {
			return;
		}
		if (!(this.lookImpl_ != null)) {
			this.lookImpl_ = new WWebWidget.LookImpl();
		}
		if (!(this.lookImpl_.toolTip_ != null)) {
			this.lookImpl_.toolTip_ = new WString();
		}
		this.lookImpl_.toolTip_ = WString.toWString(message);
		this.flags_.set(BIT_TOOLTIP_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public WString getToolTip() {
		return this.lookImpl_ != null ? this.lookImpl_.toolTip_ != null ? this.lookImpl_.toolTip_
				: new WString()
				: new WString();
	}

	public void refresh() {
		if (this.lookImpl_ != null && this.lookImpl_.toolTip_ != null) {
			if (this.lookImpl_.toolTip_.refresh()) {
				this.flags_.set(BIT_TOOLTIP_CHANGED);
				this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
			this.otherImpl_ = new WWebWidget.OtherImpl();
		}
		if (!(this.otherImpl_.attributes_ != null)) {
			this.otherImpl_.attributes_ = new HashMap<String, String>();
		}
		String i = this.otherImpl_.attributes_.get(name);
		if (i != null && i.equals(value)) {
			return;
		}
		this.otherImpl_.attributes_.put(name, value);
		if (!(this.otherImpl_.attributesSet_ != null)) {
			this.otherImpl_.attributesSet_ = new ArrayList<String>();
		}
		this.otherImpl_.attributesSet_.add(name);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
			this.otherImpl_ = new WWebWidget.OtherImpl();
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
			WWebWidget.OtherImpl.Member m = new WWebWidget.OtherImpl.Member();
			m.name = name;
			m.value = value;
			members.add(m);
		}
		if (!(this.otherImpl_.jsMembersSet_ != null)) {
			this.otherImpl_.jsMembersSet_ = new ArrayList<String>();
		}
		this.otherImpl_.jsMembersSet_.add(name);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl();
		}
		if (!(this.otherImpl_.jsMemberCalls_ != null)) {
			this.otherImpl_.jsMemberCalls_ = new ArrayList<String>();
		}
		this.otherImpl_.jsMemberCalls_.add(name + "(" + args + ");");
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public void load() {
		this.flags_.set(BIT_LOADED);
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.doLoad(this.children_.get(i));
			}
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

	public void setId(String id) {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl();
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

	public void setSelectable(boolean selectable) {
		this.flags_.set(BIT_SET_SELECTABLE, selectable);
		this.flags_.set(BIT_SET_UNSELECTABLE, !selectable);
		this.flags_.set(BIT_SELECTABLE_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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

	DomElement createActualElement(WApplication app) {
		this.flags_.clear(BIT_STUBBED);
		return this.createDomElement(app);
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
	 * Escape HTML control characters in the text, to display literally.
	 */
	public static WString escapeText(CharSequence text, boolean newlinestoo) {
		String result = text.toString();
		result = escapeText(result, newlinestoo);
		return new WString(result);
	}

	/**
	 * Escape HTML control characters in the text, to display literally.
	 * <p>
	 * Returns {@link #escapeText(CharSequence text, boolean newlinestoo)
	 * escapeText(text, false)}
	 */
	public static final WString escapeText(CharSequence text) {
		return escapeText(text, false);
	}

	/**
	 * Escape HTML control characters in the text, to display literally.
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
	 * Escape HTML control characters in the text, to display literally.
	 * <p>
	 * Returns {@link #escapeText(String text, boolean newlinestoo)
	 * escapeText(text, false)}
	 */
	public static final String escapeText(String text) {
		return escapeText(text, false);
	}

	/**
	 * Remove tags/attributes from text that are not passive.
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
		StringWriter result = new StringWriter();
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

	static String fixRelativeUrl(String url) {
		return WApplication.getInstance().fixRelativeUrl(url);
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

	int getZIndex() {
		if (this.layoutImpl_ != null) {
			return this.layoutImpl_.zIndex_;
		} else {
			return 0;
		}
	}

	void setZIndex(int zIndex) {
		if (!(this.layoutImpl_ != null)) {
			this.layoutImpl_ = new WWebWidget.LayoutImpl();
		}
		this.layoutImpl_.zIndex_ = zIndex;
		this.flags_.set(BIT_GEOMETRY_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public boolean isRendered() {
		return this.flags_.get(WWebWidget.BIT_RENDERED);
	}

	void repaint(EnumSet<RepaintFlag> flags) {
		if (!this.flags_.get(BIT_STUBBED) && this.isStubbed()) {
			WebRenderer renderer = WApplication.getInstance().getSession()
					.getRenderer();
			if (renderer.isPreLearning()) {
				renderer.learningIncomplete();
			}
		}
		if (!this.flags_.get(BIT_RENDERED)) {
			return;
		}
		super.askRerender();
		if (!EnumUtils.mask(flags, RepaintFlag.RepaintPropertyIEMobile)
				.isEmpty()) {
			this.flags_.set(BIT_REPAINT_PROPERTY_IEMOBILE);
		}
		if (!EnumUtils.mask(flags, RepaintFlag.RepaintPropertyAttribute)
				.isEmpty()) {
			this.flags_.set(BIT_REPAINT_PROPERTY_ATTRIBUTE);
		}
		if (!EnumUtils.mask(flags, RepaintFlag.RepaintInnerHtml).isEmpty()) {
			this.flags_.set(BIT_REPAINT_INNER_HTML);
		}
		if (!EnumUtils.mask(flags, RepaintFlag.RepaintToAjax).isEmpty()) {
			this.flags_.set(BIT_REPAINT_TO_AJAX);
		}
	}

	final void repaint(RepaintFlag flag, RepaintFlag... flags) {
		repaint(EnumSet.of(flag, flags));
	}

	final void repaint() {
		repaint(RepaintFlag.RepaintAll);
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
								WApplication app = WApplication.getInstance();
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
			if (!this.flags_.get(BIT_HIDE_WITH_VISIBILITY)) {
				this.flags_.clear(BIT_HIDDEN_CHANGED);
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
					WApplication app = WApplication.getInstance();
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
										+ ";if (w && !Wt3_1_7.isHidden(w)) {var i = Wt3_1_7.getElement('"
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
				if (!this.layoutImpl_.minimumWidth_.isAuto()
						&& this.layoutImpl_.minimumWidth_.getValue() != 0) {
					element.setProperty(Property.PropertyStyleMinWidth,
							this.layoutImpl_.minimumWidth_.getCssText());
				}
				if (!this.layoutImpl_.minimumHeight_.isAuto()
						&& this.layoutImpl_.minimumHeight_.getValue() != 0) {
					element.setProperty(Property.PropertyStyleMinHeight,
							this.layoutImpl_.minimumHeight_.getCssText());
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
					for (int i = 0; i < 4; ++i) {
						if (!this.layoutImpl_.offsets_[i].isAuto()) {
							element.setProperty(properties[i],
									this.layoutImpl_.offsets_[i].getCssText());
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
				case AlignLength:
					element.setProperty(Property.PropertyStyleVerticalAlign,
							this.layoutImpl_.verticalAlignmentLength_
									.getCssText());
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
					switch (this.layoutImpl_.floatSide_) {
					case Left:
						element
								.setProperty(Property.PropertyStyleFloat,
										"left");
						break;
					case Right:
						element.setProperty(Property.PropertyStyleFloat,
								"right");
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
					element.setAttribute("title", this.lookImpl_.toolTip_
							.toString());
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
		if (this.transientImpl_ != null) {
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
							element.callJavaScript("Wt3_1_7.remove('"
									+ js.substring(1) + "');", true);
						} else {
							element.callJavaScript(js);
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
					if (this.otherImpl_.attributesSet_ != null) {
						for (int i = 0; i < this.otherImpl_.attributesSet_
								.size(); ++i) {
							String attr = this.otherImpl_.attributesSet_.get(i);
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
				;
				this.otherImpl_.attributesSet_ = null;
			}
			if (this.otherImpl_.jsMembers_ != null) {
				if (all) {
					for (int i = 0; i < this.otherImpl_.jsMembers_.size(); i++) {
						WWebWidget.OtherImpl.Member member = this.otherImpl_.jsMembers_
								.get(i);
						element.callMethod(member.name + "=" + member.value);
					}
				} else {
					if (this.otherImpl_.jsMembersSet_ != null) {
						for (int i = 0; i < this.otherImpl_.jsMembersSet_
								.size(); ++i) {
							String m = this.otherImpl_.jsMembersSet_.get(i);
							String value = this.getJavaScriptMember(m);
							if (value.length() != 0) {
								element.callMethod(m + "=" + value);
							} else {
								element.callMethod(m + "= null");
							}
						}
					}
				}
				;
				this.otherImpl_.jsMembersSet_ = null;
			}
			if (this.otherImpl_.jsMemberCalls_ != null) {
				for (int i = 0; i < this.otherImpl_.jsMemberCalls_.size(); ++i) {
					String m = this.otherImpl_.jsMemberCalls_.get(i);
					element.callMethod(m);
				}
				;
				this.otherImpl_.jsMemberCalls_ = null;
			}
		}
		if (this.flags_.get(BIT_HIDE_WITH_VISIBILITY)) {
			if (this.flags_.get(BIT_HIDDEN_CHANGED) || all
					&& this.flags_.get(BIT_HIDDEN)) {
				if (this.flags_.get(BIT_HIDDEN)) {
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
							element.setProperty(Property.PropertyStyleTop,
									this.layoutImpl_.offsets_[0].getCssText());
							element.setProperty(Property.PropertyStyleLeft,
									this.layoutImpl_.offsets_[3].getCssText());
						} else {
							element.setProperty(Property.PropertyStylePosition,
									"static");
						}
						element.setProperty(Property.PropertyStyleTop, "0px");
						element.setProperty(Property.PropertyStyleLeft, "0px");
					}
					element.setProperty(Property.PropertyStyleVisibility,
							"visible");
					element.setProperty(Property.PropertyStyleDisplay, "");
				}
				this.flags_.clear(BIT_HIDDEN_CHANGED);
			}
		}
		this.renderOk();
		;
		this.transientImpl_ = null;
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
				if (s.getName() == WInteractWidget.CLICK_SIGNAL) {
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
			WApplication.getInstance().log("warn").append(
					"WWebWidget::addChild(): reparenting child");
		}
		if (!(this.children_ != null)) {
			this.children_ = new ArrayList<WWidget>();
		}
		this.children_.add(child);
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
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		child.setParent((WObject) null);
		if (!child.getWebWidget().flags_.get(BIT_BEING_DELETED)) {
			child.getWebWidget().setRendered(false);
		}
		this.children_.remove(0 + i);
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(child.getWebWidget(), true);
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

	void doLoad(WWidget w) {
		w.load();
		if (!w.isLoaded()) {
			System.err
					.append(
							"Improper load() implementation: base implementation not called?")
					.append('\n');
		}
	}

	void render(EnumSet<RenderFlag> flags) {
		super.render(flags);
		if (this.otherImpl_ != null
				&& this.otherImpl_.delayedDoJavaScript_ != null) {
			WApplication.getInstance().doJavaScript(
					this.otherImpl_.delayedDoJavaScript_.toString());
			;
			this.otherImpl_.delayedDoJavaScript_ = null;
		}
	}

	/**
	 * Executes the given JavaScript statements, possibly delayed until after
	 * the widget is rendered.
	 * <p>
	 * Calling
	 * {@link WApplication#doJavaScript(String javascript, boolean afterLoaded)
	 * WApplication#doJavaScript()} with JavaScript code that refers to a widget
	 * that is still to be rendered causes JavaScript errors. This happens for
	 * example when an object is created, but not yet inserted in the widget
	 * tree.
	 * <p>
	 * This method offers an alternative: it queues up all doJavaScript calls
	 * for widgets that were not yet rendered until they are rendered.
	 */
	protected void doJavaScript(String javascript) {
		if (this.isRendered()) {
			WApplication.getInstance().doJavaScript(javascript);
		} else {
			if (!(this.otherImpl_ != null)) {
				this.otherImpl_ = new WWebWidget.OtherImpl();
			}
			if (!(this.otherImpl_.delayedDoJavaScript_ != null)) {
				this.otherImpl_.delayedDoJavaScript_ = new StringBuilder();
			}
			this.otherImpl_.delayedDoJavaScript_.append(javascript);
		}
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
	private static final int BIT_REPAINT_PROPERTY_IEMOBILE = 12;
	private static final int BIT_REPAINT_PROPERTY_ATTRIBUTE = 13;
	private static final int BIT_REPAINT_INNER_HTML = 14;
	static final int BIT_REPAINT_TO_AJAX = 15;
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
	private static final int BIT_HIDE_WITH_VISIBILITY = 26;
	private static final int BIT_HIDDEN_CHANGED = 27;
	static final int BIT_ENABLED = 28;
	BitSet flags_;
	private WLength width_;
	private WLength height_;

	static class TransientImpl {
		public List<String> childRemoveChanges_;
		public List<WWidget> addedChildren_;
		public List<String> addedStyleClasses_;
		public List<String> removedStyleClasses_;
		public boolean specialChildRemove_;

		public TransientImpl() {
			this.childRemoveChanges_ = new ArrayList<String>();
			this.addedChildren_ = new ArrayList<WWidget>();
			this.addedStyleClasses_ = new ArrayList<String>();
			this.removedStyleClasses_ = new ArrayList<String>();
			this.specialChildRemove_ = false;
		}
	}

	WWebWidget.TransientImpl transientImpl_;

	static class LayoutImpl {
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
			this.minimumWidth_ = new WLength();
			this.minimumHeight_ = new WLength();
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
		public WCssDecorationStyle decorationStyle_;
		public String styleClass_;
		public WString toolTip_;

		public LookImpl() {
			this.decorationStyle_ = null;
			this.styleClass_ = "";
			this.toolTip_ = null;
		}
	}

	private WWebWidget.LookImpl lookImpl_;

	static class DropMimeType {
		public String hoverStyleClass;

		public DropMimeType() {
			this.hoverStyleClass = "";
		}

		public DropMimeType(String aHoverStyleClass) {
			this.hoverStyleClass = aHoverStyleClass;
		}
	}

	static class OtherImpl {
		static class Member {
			public String name;
			public String value;
		}

		public String id_;
		public Map<String, String> attributes_;
		public List<String> attributesSet_;
		public List<WWebWidget.OtherImpl.Member> jsMembers_;
		public List<String> jsMembersSet_;
		public List<String> jsMemberCalls_;
		public JSignal3<String, String, WMouseEvent> dropSignal_;
		public Map<String, WWebWidget.DropMimeType> acceptedDropMimeTypes_;
		public StringBuilder delayedDoJavaScript_;

		public OtherImpl() {
			this.id_ = null;
			this.attributes_ = null;
			this.attributesSet_ = null;
			this.jsMembers_ = null;
			this.jsMembersSet_ = null;
			this.jsMemberCalls_ = null;
			this.dropSignal_ = null;
			this.acceptedDropMimeTypes_ = null;
			this.delayedDoJavaScript_ = null;
		}
	}

	WWebWidget.OtherImpl otherImpl_;
	List<WWidget> children_;

	void signalConnectionsChanged() {
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	void renderOk() {
		super.renderOk();
		this.flags_.clear(BIT_REPAINT_PROPERTY_IEMOBILE);
		this.flags_.clear(BIT_REPAINT_PROPERTY_ATTRIBUTE);
		this.flags_.clear(BIT_REPAINT_INNER_HTML);
		this.flags_.clear(BIT_REPAINT_TO_AJAX);
	}

	private void calcZIndex() {
		this.layoutImpl_.zIndex_ = -1;
		WWidget p = this;
		do {
			p = p.getParent();
		} while (p != null
				&& ((p) instanceof WCompositeWidget ? (WCompositeWidget) (p)
						: null) != null);
		if (p == null) {
			return;
		}
		WWebWidget ww = p.getWebWidget();
		if (ww != null) {
			List<WWidget> children = ww.getChildren();
			int maxZ = 0;
			for (int i = 0; i < children.size(); ++i) {
				WWebWidget wi = children.get(i).getWebWidget();
				maxZ = Math.max(maxZ, wi.getZIndex());
			}
			this.layoutImpl_.zIndex_ = maxZ + 5;
		}
	}

	boolean needsToBeRendered() {
		return this.flags_.get(BIT_DONOT_STUB)
				|| !this.flags_.get(BIT_HIDDEN)
				|| !WApplication.getInstance().getSession().getRenderer()
						.isVisibleOnly();
	}

	void getSDomChanges(List<DomElement> result, WApplication app) {
		boolean isIEMobile = app.getEnvironment().agentIsIEMobile();
		if (this.flags_.get(BIT_STUBBED)) {
			if (app.getSession().getRenderer().isPreLearning()) {
				this.getDomChanges(result, app);
				this.askRerender(true);
			} else {
				if (!app.getSession().getRenderer().isVisibleOnly()) {
					this.flags_.clear(BIT_STUBBED);
					if (!isIEMobile) {
						DomElement stub = DomElement.getForUpdate(this,
								DomElementType.DomElement_SPAN);
						this.setRendered(true);
						this.render(EnumSet.of(RenderFlag.RenderFull));
						DomElement realElement = this.createDomElement(app);
						stub.unstubWith(realElement, !this.flags_
								.get(BIT_HIDE_WITH_OFFSETS));
						result.add(stub);
					} else {
						this.propagateRenderOk();
					}
				}
			}
		} else {
			this.render(EnumSet.of(RenderFlag.RenderUpdate));
			if (isIEMobile) {
				if (this.flags_.get(BIT_REPAINT_PROPERTY_ATTRIBUTE)) {
					WWidget p = this;
					WWebWidget w = this;
					do {
						p = p.getParent();
						if (p != null) {
							w = p.getWebWidget();
						}
					} while (p != null && w == this);
					if (w != this) {
						w.getSDomChanges(result, app);
					}
				} else {
					if (this.flags_.get(BIT_REPAINT_INNER_HTML)
							|| !this.flags_.get(BIT_REPAINT_PROPERTY_IEMOBILE)) {
						DomElement e = this.createDomElement(app);
						e.updateInnerHtmlOnly();
						result.add(e);
					} else {
						this.getDomChanges(result, app);
					}
				}
				return;
			}
			this.getDomChanges(result, app);
		}
	}

	private void getSFormObjects(Map<String, WObject> result) {
		if (!this.flags_.get(BIT_STUBBED) && !this.flags_.get(BIT_HIDDEN)) {
			this.getFormObjects(result);
		}
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
			this.otherImpl_ = new WWebWidget.OtherImpl();
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

	protected EventSignal1<WTouchEvent> touchEventSignal(String name,
			boolean create) {
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

	protected EventSignal1<WGestureEvent> gestureEventSignal(String name,
			boolean create) {
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

	static Property[] properties = { Property.PropertyStyleTop,
			Property.PropertyStyleRight, Property.PropertyStyleBottom,
			Property.PropertyStyleLeft };
	private static List<WWidget> emptyWidgetList_ = new ArrayList<WWidget>();
}
