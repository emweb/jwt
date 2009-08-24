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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import eu.webtoolkit.jwt.utils.EnumUtils;
import eu.webtoolkit.jwt.utils.StringUtils;

/**
 * A base class for widgets with an HTML counterpart
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
		this.flags_.set(BIT_BEING_DELETED);
		if (this.flags_.get(BIT_FORM_OBJECT)) {
			WApplication.getInstance().getSession().getRenderer()
					.updateFormObjects(this, false);
		}
		this.setParent((WWidget) null);
		/* delete this.width_ */;
		/* delete this.height_ */;
		if (this.children_ != null) {
			while (this.children_.size() != 0) {
				if (this.children_.get(0) != null)
					this.children_.get(0).remove();
			}
			/* delete this.children_ */;
		}
		/* delete this.transientImpl_ */;
		/* delete this.layoutImpl_ */;
		/* delete this.lookImpl_ */;
		/* delete this.otherImpl_ */;
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

	public void setHidden(boolean hidden) {
		if (canOptimizeUpdates() && hidden == this.isHidden()) {
			return;
		}
		if (hidden) {
			this.flags_.set(BIT_HIDDEN);
		} else {
			this.flags_.clear(BIT_HIDDEN);
		}
		this.flags_.set(BIT_HIDDEN_CHANGED);
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(this, true);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public boolean isHidden() {
		return this.flags_.get(BIT_HIDDEN);
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

	public void setId(String id) {
		if (!(this.otherImpl_ != null)) {
			this.otherImpl_ = new WWebWidget.OtherImpl();
		}
		if (!(this.otherImpl_.id_ != null)) {
			this.otherImpl_.id_ = "";
		}
		this.otherImpl_.id_ = id;
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

	public DomElement createSDomElement(WApplication app) {
		if (!this.needsToBeRendered()) {
			this.propagateRenderOk();
			this.flags_.set(BIT_STUBBED);
			DomElement stub = DomElement
					.createNew(DomElementType.DomElement_SPAN);
			if (!this.flags_.get(BIT_HIDE_WITH_OFFSETS)) {
				stub.setProperty(Property.PropertyStyleDisplay, "none");
			} else {
				stub.setProperty(Property.PropertyStylePosition, "absolute");
				stub.setProperty(Property.PropertyStyleLeft, "-10000px");
				stub.setProperty(Property.PropertyStyleTop, "-10000px");
				stub.setProperty(Property.PropertyStyleVisibility, "hidden");
			}
			if (WApplication.getInstance().getEnvironment().hasJavaScript()) {
				stub.setProperty(Property.PropertyInnerHTML, "...");
			}
			if (!app.getEnvironment().agentIsSpiderBot()
					|| this.otherImpl_ != null && this.otherImpl_.id_ != null) {
				stub.setId(this.getId());
			}
			super.askRerender(true);
			return stub;
		} else {
			this.flags_.clear(BIT_STUBBED);
			this.render();
			return this.createDomElement(app);
		}
	}

	abstract DomElementType getDomElementType();

	/**
	 * Change the way the widget is loaded when invisible.
	 * <p>
	 * By default, invisible widgets are loaded only after visible content. For
	 * tiny widgets this may lead to a performance loss, instead of the expected
	 * increase, because they require many more DOM manipulation to render,
	 * reducing the overall responsiveness of the application.
	 * <p>
	 * Therefore, this is disabled for some widgets like {@link WImage}, or
	 * empty WContainerWidgets.
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
		text = StringUtils.escapeText(text, newlinestoo);
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
	 * Modifies the <i>text</i> if needed. When the text is not proper XML,
	 * returns false.
	 */
	public static boolean removeScript(CharSequence text) {
		return XSSFilter.removeScript(text);
	}

	/**
	 * Turn a UTF8 encoded string into a JavaScript string literal.
	 * <p>
	 * The <i>delimiter</i> may be a single or double quote.
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

	public static String jsStringLiteral(CharSequence value, char delimiter) {
		return WString.toWString(value).getJsStringLiteral(delimiter);
	}

	public static final String jsStringLiteral(CharSequence value) {
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

	public static String fixRelativeUrl(String url) {
		return WApplication.getInstance().fixRelativeUrl(url);
	}

	public void setFormObject(boolean how) {
		this.flags_.set(BIT_FORM_OBJECT, how);
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(this, false);
	}

	public static boolean canOptimizeUpdates() {
		return !WApplication.getInstance().getSession().getRenderer()
				.isPreLearning();
	}

	public int getZIndex() {
		if (this.layoutImpl_ != null) {
			return this.layoutImpl_.zIndex_;
		} else {
			return 0;
		}
	}

	protected Map<String, WObject> FormObjectsMap;

	protected void repaint(EnumSet<RepaintFlag> flags) {
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

	protected final void repaint(RepaintFlag flag, RepaintFlag... flags) {
		repaint(EnumSet.of(flag, flags));
	}

	protected final void repaint() {
		repaint(RepaintFlag.RepaintAll);
	}

	protected void getFormObjects(Map<String, WObject> formObjects) {
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

	protected void doneRerender() {
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.children_.get(i).getWebWidget().doneRerender();
			}
		}
	}

	protected void updateDom(DomElement element, boolean all) {
		if (this.flags_.get(BIT_GEOMETRY_CHANGED)
				|| !this.flags_.get(BIT_HIDE_WITH_OFFSETS)
				&& this.flags_.get(BIT_HIDDEN_CHANGED) || all) {
			if (this.flags_.get(BIT_HIDE_WITH_OFFSETS)
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
								element.setProperty(
										Property.PropertyStyleDisplay,
										"inline-block");
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
			if (!this.flags_.get(BIT_HIDE_WITH_OFFSETS)) {
				this.flags_.clear(BIT_HIDDEN_CHANGED);
			}
		}
		if (this.flags_.get(BIT_GEOMETRY_CHANGED) || all) {
			if (this.layoutImpl_ != null) {
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
					element
							.setProperty(Property.PropertyStylePosition,
									"fixed");
					break;
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
						i.setAttribute("class", "Wt-shim");
						i.setAttribute("src", "javascript:false;");
						i.setAttribute("title", "Popup Shim");
						i.setAttribute("tabindex", "-1");
						i.setAttribute("frameborder", "0");
						app
								.addAutoJavaScript("{var w = "
										+ this.getJsRef()
										+ ";if (w && !Wt2_99_4.isHidden(w)) {var i = Wt2_99_4.getElement('"
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
			if (this.flags_.get(BIT_WIDTH_CHANGED) || !this.width_.isAuto()) {
				element.setProperty(Property.PropertyStyleWidth, this.width_
						.getCssText());
			}
			this.flags_.clear(BIT_WIDTH_CHANGED);
		}
		if (this.height_ != null
				&& (this.flags_.get(BIT_HEIGHT_CHANGED) || all)) {
			if (this.flags_.get(BIT_HEIGHT_CHANGED) || !this.height_.isAuto()) {
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
				if (this.flags_.get(BIT_MARGINS_CHANGED)
						|| this.layoutImpl_.margin_[0].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginTop,
							this.layoutImpl_.margin_[0].getCssText());
				}
				if (this.flags_.get(BIT_MARGINS_CHANGED)
						|| this.layoutImpl_.margin_[1].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginRight,
							this.layoutImpl_.margin_[1].getCssText());
				}
				if (this.flags_.get(BIT_MARGINS_CHANGED)
						|| this.layoutImpl_.margin_[2].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginBottom,
							this.layoutImpl_.margin_[2].getCssText());
				}
				if (this.flags_.get(BIT_MARGINS_CHANGED)
						|| this.layoutImpl_.margin_[3].getValue() != 0) {
					element.setProperty(Property.PropertyStyleMarginLeft,
							this.layoutImpl_.margin_[3].getCssText());
				}
				this.flags_.clear(BIT_MARGINS_CHANGED);
			}
		}
		if (this.lookImpl_ != null) {
			if (this.lookImpl_.toolTip_ != null
					&& (this.flags_.get(BIT_TOOLTIP_CHANGED) || all)) {
				if (this.lookImpl_.toolTip_.toString().length() > 0
						|| this.flags_.get(BIT_TOOLTIP_CHANGED)) {
					element.setAttribute("title", this.lookImpl_.toolTip_
							.toString());
				}
				this.flags_.clear(BIT_TOOLTIP_CHANGED);
			}
			if (this.lookImpl_.decorationStyle_ != null) {
				this.lookImpl_.decorationStyle_.updateDomElement(element, all);
			}
			if (!all && this.flags_.get(BIT_STYLECLASS_CHANGED) || all
					&& this.lookImpl_.styleClass_.length() != 0) {
				element.setAttribute("class", this.lookImpl_.styleClass_);
			}
			this.flags_.clear(BIT_STYLECLASS_CHANGED);
		}
		if (all || this.flags_.get(BIT_SELECTABLE_CHANGED)) {
			if (this.flags_.get(BIT_SET_UNSELECTABLE)) {
				element.setAttribute("class", StringUtils.addWord(element
						.getAttribute("class"), "unselectable"));
				element.setAttribute("unselectable", "on");
				element.setAttribute("onselectstart", "return false;");
			} else {
				if (this.flags_.get(BIT_SET_SELECTABLE)) {
					element.setAttribute("class", StringUtils.addWord(element
							.getAttribute("class"), "selectable"));
					element.setAttribute("unselectable", "off");
					element.setAttribute("onselectstart",
							"event.cancelBubble=true;return true;");
				}
			}
			this.flags_.clear(BIT_SELECTABLE_CHANGED);
		}
		if (this.otherImpl_ != null && this.otherImpl_.attributes_ != null) {
			if (all) {
				for (Iterator<Map.Entry<String, String>> i_it = this.otherImpl_.attributes_
						.entrySet().iterator(); i_it.hasNext();) {
					Map.Entry<String, String> i = i_it.next();
					element.setAttribute(i.getKey(), i.getValue());
				}
			} else {
				if (this.otherImpl_.attributesSet_ != null) {
					for (int i = 0; i < this.otherImpl_.attributesSet_.size(); ++i) {
						String attr = this.otherImpl_.attributesSet_.get(i);
						element.setAttribute(attr, this.otherImpl_.attributes_
								.get(attr));
					}
				}
			}
			/* delete this.otherImpl_.attributesSet_ */;
			this.otherImpl_.attributesSet_ = null;
		}
		if (this.flags_.get(BIT_HIDE_WITH_OFFSETS)) {
			if (this.flags_.get(BIT_HIDDEN_CHANGED) || all
					&& this.flags_.get(BIT_HIDDEN)) {
				if (this.flags_.get(BIT_HIDDEN)) {
					element.setProperty(Property.PropertyStylePosition,
							"absolute");
					element.setProperty(Property.PropertyStyleLeft, "-10000px");
					element.setProperty(Property.PropertyStyleTop, "-10000px");
					element.setProperty(Property.PropertyStyleVisibility,
							"hidden");
				} else {
					if (this.layoutImpl_ != null) {
						switch (this.layoutImpl_.positionScheme_) {
						case Static:
							element.setProperty(Property.PropertyStylePosition,
									"static");
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
						element.setProperty(Property.PropertyStyleTop,
								this.layoutImpl_.offsets_[0].getCssText());
						element.setProperty(Property.PropertyStyleLeft,
								this.layoutImpl_.offsets_[3].getCssText());
					} else {
						element.setProperty(Property.PropertyStylePosition,
								"static");
					}
					element.setProperty(Property.PropertyStyleVisibility,
							"visible");
					element.setProperty(Property.PropertyStyleTop, "0px");
					element.setProperty(Property.PropertyStyleLeft, "0px");
					element.setProperty(Property.PropertyStyleDisplay, "");
				}
				this.flags_.clear(BIT_HIDDEN_CHANGED);
			}
		}
		this.renderOk();
		/* delete this.transientImpl_ */;
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
		this.renderOk();
		if (deep && this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.children_.get(i).getWebWidget().propagateRenderOk();
			}
		}
		/* delete this.transientImpl_ */;
		this.transientImpl_ = null;
	}

	final void propagateRenderOk() {
		propagateRenderOk(true);
	}

	protected DomElement renderRemove() {
		DomElement e = DomElement.getForUpdate(this,
				DomElementType.DomElement_DIV);
		e.removeFromParent();
		return e;
	}

	protected boolean isVisible() {
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

	protected boolean isStubbed() {
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

	protected void addChild(WWidget child) {
		if (child.getParent() != null) {
			child.setParent((WWidget) null);
			WApplication.getInstance().log("warn").append(
					"WWebWidget::addChild(): reparenting child");
		}
		if (!(this.children_ != null)) {
			this.children_ = new ArrayList<WWidget>();
		}
		this.children_.add(child);
		child.setParent((WObject) this);
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

	protected void removeChild(WWidget w) {
		assert this.children_ != null;
		int i = this.children_.indexOf(w);
		assert i != -1;
		if (!this.flags_.get(BIT_IGNORE_CHILD_REMOVES)
				&& !this.flags_.get(BIT_BEING_DELETED)) {
			DomElement e = w.getWebWidget().renderRemove();
			if (e != null) {
				if (!(this.transientImpl_ != null)) {
					this.transientImpl_ = new WWebWidget.TransientImpl();
				}
				this.transientImpl_.childRemoveChanges_.add(e);
				this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
			}
		}
		w.setParent((WObject) null);
		if (!w.getWebWidget().flags_.get(BIT_BEING_DELETED)) {
			w.getWebWidget().quickPropagateRenderOk();
		}
		this.children_.remove(0 + i);
		WApplication.getInstance().getSession().getRenderer()
				.updateFormObjects(w.getWebWidget(), true);
	}

	protected void setHideWithOffsets(boolean how) {
		if (how) {
			if (!this.flags_.get(BIT_HIDE_WITH_OFFSETS)) {
				this.flags_.set(BIT_HIDE_WITH_OFFSETS);
				// this.resetLearnedSlot(WWidget.show);
				// this.resetLearnedSlot(WWidget.hide);
				if (this.getParent() != null) {
					this.getParent().setHideWithOffsets(true);
				}
			}
		}
	}

	protected void doLoad(WWidget w) {
		w.load();
		if (!w.isLoaded()) {
			System.err
					.append(
							"Improper load() implementation: base implementation not called?")
					.append('\n');
		}
	}

	private static final int BIT_INLINE = 0;
	private static final int BIT_HIDDEN = 1;
	private static final int BIT_LOADED = 2;
	private static final int BIT_HIDDEN_CHANGED = 3;
	private static final int BIT_STUBBED = 4;
	private static final int BIT_FORM_OBJECT = 5;
	static final int BIT_IGNORE_CHILD_REMOVES = 6;
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
	BitSet flags_;
	private WLength width_;
	private WLength height_;

	static class TransientImpl {
		public List<DomElement> childRemoveChanges_;
		public List<WWidget> addedChildren_;

		public TransientImpl() {
			this.childRemoveChanges_ = new ArrayList<DomElement>();
			this.addedChildren_ = new ArrayList<WWidget>();
		}

		public void destroy() {
			for (int i = 0; i < this.childRemoveChanges_.size(); ++i) {
				/* delete this.childRemoveChanges_.get(i) */;
			}
		}
	}

	WWebWidget.TransientImpl transientImpl_;

	private static class LayoutImpl {
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

	private static class LookImpl {
		public WCssDecorationStyle decorationStyle_;
		public String styleClass_;
		public WString toolTip_;

		public LookImpl() {
			this.decorationStyle_ = null;
			this.styleClass_ = "";
			this.toolTip_ = null;
		}

		public void destroy() {
			/* delete this.decorationStyle_ */;
			/* delete this.toolTip_ */;
		}
	}

	private WWebWidget.LookImpl lookImpl_;

	private static class DropMimeType {
		public String hoverStyleClass;

		public DropMimeType() {
			this.hoverStyleClass = "";
		}

		public DropMimeType(String aHoverStyleClass) {
			this.hoverStyleClass = aHoverStyleClass;
		}
	}

	static class OtherImpl {
		public Map<String, String> attributes_;
		public List<String> attributesSet_;
		public String id_;
		public JSignal3<String, String, WMouseEvent> dropSignal_;
		public Map<String, WWebWidget.DropMimeType> MimeTypesMap;
		public Map<String, WWebWidget.DropMimeType> acceptedDropMimeTypes_;

		public OtherImpl() {
			this.attributes_ = null;
			this.attributesSet_ = null;
			this.id_ = null;
			this.dropSignal_ = null;
			this.acceptedDropMimeTypes_ = null;
		}

		public void destroy() {
			/* delete this.attributes_ */;
			/* delete this.attributesSet_ */;
			/* delete this.dropSignal_ */;
			/* delete this.acceptedDropMimeTypes_ */;
			/* delete this.id_ */;
		}
	}

	WWebWidget.OtherImpl otherImpl_;
	List<WWidget> children_;

	void signalConnectionsChanged() {
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	protected void renderOk() {
		super.renderOk();
		this.flags_.clear(BIT_REPAINT_PROPERTY_IEMOBILE);
		this.flags_.clear(BIT_REPAINT_PROPERTY_ATTRIBUTE);
		this.flags_.clear(BIT_REPAINT_INNER_HTML);
		this.flags_.clear(BIT_REPAINT_TO_AJAX);
	}

	private void quickPropagateRenderOk() {
		this.renderOk();
		if (this.children_ != null) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.children_.get(i).getWebWidget().quickPropagateRenderOk();
			}
		}
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

	protected boolean needsToBeRendered() {
		return this.flags_.get(BIT_DONOT_STUB)
				|| !this.flags_.get(BIT_HIDDEN)
				|| !WApplication.getInstance().getSession().getRenderer()
						.isVisibleOnly();
	}

	protected void getSDomChanges(List<DomElement> result, WApplication app) {
		boolean isIEMobile = app.getEnvironment().agentIsIEMobile();
		if (this.flags_.get(BIT_STUBBED)) {
			if (app.getSession().getRenderer().isPreLearning()) {
				this.getDomChanges(result, app);
				this.repaint();
			} else {
				this.flags_.clear(BIT_STUBBED);
				if (!isIEMobile) {
					DomElement stub = DomElement.getForUpdate(this,
							DomElementType.DomElement_SPAN);
					this.render();
					DomElement realElement = this.createDomElement(app);
					stub.replaceWith(realElement, !this.flags_
							.get(BIT_HIDE_WITH_OFFSETS));
					result.add(stub);
				} else {
					this.propagateRenderOk();
				}
			}
		} else {
			this.render();
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
					w.getSDomChanges(result, app);
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
			if (this.transientImpl_ != null) {
				result.addAll(this.transientImpl_.childRemoveChanges_);
				this.transientImpl_.childRemoveChanges_.clear();
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
				this.otherImpl_.acceptedDropMimeTypes_.remove(i);
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

	protected void setId(DomElement element, WApplication app) {
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

	protected EventSignal voidEventSignal(String name, boolean create) {
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

	protected EventSignal1<WKeyEvent> keyEventSignal(String name, boolean create) {
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

	protected EventSignal1<WMouseEvent> mouseEventSignal(String name,
			boolean create) {
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

	protected EventSignal1<WScrollEvent> scrollEventSignal(String name,
			boolean create) {
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

	protected void updateSignalConnection(DomElement element,
			AbstractEventSignal signal, String name, boolean all) {
		if (name.charAt(0) != 'M' && (all || signal.needUpdate())) {
			if (signal.isConnected()) {
				element.setEventSignal(name, signal);
			} else {
				if (!all) {
					element.setEvent(name, "", "");
				}
			}
			signal.updateOk();
		}
	}

	protected static Property[] properties = { Property.PropertyStyleTop,
			Property.PropertyStyleRight, Property.PropertyStyleBottom,
			Property.PropertyStyleLeft };
	private static List<WWidget> emptyWidgetList_ = new ArrayList<WWidget>();
}
