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
 * A widget that displays an image.
 * <p>
 * 
 * The image may be specified either as a URL, or may be dynamically generated
 * by a {@link WResource}.
 * <p>
 * You may listen to events by attaching event listeners to signals such as
 * {@link WInteractWidget#clicked() WInteractWidget#clicked()}. Since mouse
 * events pass the coordinates through a {@link WMouseEvent} object, it is
 * possible to react to clicks in specific parts of the image. An alternative is
 * to define interactive areas on the image using
 * {@link WImage#addArea(WAbstractArea area) addArea()}, which in addition
 * allows to have customized tool tips for certain image areas (using
 * {@link WAbstractArea#setToolTip(CharSequence text)
 * WAbstractArea#setToolTip()}).
 * <p>
 * WImage is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The widget corresponds to the HTML <code>&lt;img&gt;</code> tag and does not
 * provide styling. It can be styled using inline or external CSS as
 * appropriate.
 * <p>
 * 
 * @see WResource
 * @see WPaintedWidget
 */
public class WImage extends WInteractWidget {
	private static Logger logger = LoggerFactory.getLogger(WImage.class);

	/**
	 * Creates an empty image widget.
	 */
	public WImage(WContainerWidget parent) {
		super(parent);
		this.altText_ = new WString();
		this.imageLink_ = new WLink();
		this.map_ = null;
		this.flags_ = new BitSet();
		this.targetJS_ = "";
		this.setLoadLaterWhenInvisible(false);
	}

	/**
	 * Creates an empty image widget.
	 * <p>
	 * Calls {@link #WImage(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WImage() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates an image widget with a given image link.
	 * <p>
	 * The <code>imageLink</code> may link to a URL or resource.
	 */
	public WImage(final WLink link, WContainerWidget parent) {
		super(parent);
		this.altText_ = new WString();
		this.imageLink_ = new WLink();
		this.map_ = null;
		this.flags_ = new BitSet();
		this.targetJS_ = "";
		this.setLoadLaterWhenInvisible(false);
		this.setImageLink(link);
	}

	/**
	 * Creates an image widget with a given image link.
	 * <p>
	 * Calls {@link #WImage(WLink link, WContainerWidget parent) this(link,
	 * (WContainerWidget)null)}
	 */
	public WImage(final WLink link) {
		this(link, (WContainerWidget) null);
	}

	/**
	 * Creates an image widget with a given image link and alternate text.
	 * <p>
	 * The <code>imageLink</code> may link to a URL or resource.
	 */
	public WImage(final WLink link, final CharSequence altText,
			WContainerWidget parent) {
		super(parent);
		this.altText_ = WString.toWString(altText);
		this.imageLink_ = new WLink();
		this.map_ = null;
		this.flags_ = new BitSet();
		this.targetJS_ = "";
		this.setLoadLaterWhenInvisible(false);
		this.setImageLink(link);
	}

	/**
	 * Creates an image widget with a given image link and alternate text.
	 * <p>
	 * Calls
	 * {@link #WImage(WLink link, CharSequence altText, WContainerWidget parent)
	 * this(link, altText, (WContainerWidget)null)}
	 */
	public WImage(final WLink link, final CharSequence altText) {
		this(link, altText, (WContainerWidget) null);
	}

	/**
	 * Creates an image widget with given image URL (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use {@link WImage#WImage(WLink link, WContainerWidget parent)
	 *             WImage()} instead.
	 */
	public WImage(final String imageRef, WContainerWidget parent) {
		super(parent);
		this.altText_ = new WString();
		this.imageLink_ = new WLink(WLink.Type.Url, imageRef);
		this.map_ = null;
		this.flags_ = new BitSet();
		this.targetJS_ = "";
		this.setLoadLaterWhenInvisible(false);
	}

	/**
	 * Creates an image widget with given image URL (<b>deprecated</b>).
	 * <p>
	 * Calls {@link #WImage(String imageRef, WContainerWidget parent)
	 * this(imageRef, (WContainerWidget)null)}
	 */
	public WImage(final String imageRef) {
		this(imageRef, (WContainerWidget) null);
	}

	/**
	 * Creates an image widget with given image URL and alternate text
	 * (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use
	 *             {@link WImage#WImage(WLink link, CharSequence altText, WContainerWidget parent)
	 *             WImage()} instead.
	 */
	public WImage(final String imageRef, final CharSequence altText,
			WContainerWidget parent) {
		super(parent);
		this.altText_ = WString.toWString(altText);
		this.imageLink_ = new WLink(WLink.Type.Url, imageRef);
		this.map_ = null;
		this.flags_ = new BitSet();
		this.targetJS_ = "";
		this.setLoadLaterWhenInvisible(false);
	}

	/**
	 * Creates an image widget with given image URL and alternate text
	 * (<b>deprecated</b>).
	 * <p>
	 * Calls
	 * {@link #WImage(String imageRef, CharSequence altText, WContainerWidget parent)
	 * this(imageRef, altText, (WContainerWidget)null)}
	 */
	public WImage(final String imageRef, final CharSequence altText) {
		this(imageRef, altText, (WContainerWidget) null);
	}

	/**
	 * Creates an image widget with given image resource and alternate text
	 * (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use
	 *             {@link WImage#WImage(WLink link, CharSequence altText, WContainerWidget parent)
	 *             WImage()} instead.
	 */
	public WImage(WResource resource, final CharSequence altText,
			WContainerWidget parent) {
		super(parent);
		this.altText_ = WString.toWString(altText);
		this.imageLink_ = new WLink();
		this.map_ = null;
		this.flags_ = new BitSet();
		this.targetJS_ = "";
		this.setLoadLaterWhenInvisible(false);
		this.setImageLink(new WLink(resource));
	}

	/**
	 * Creates an image widget with given image resource and alternate text
	 * (<b>deprecated</b>).
	 * <p>
	 * Calls
	 * {@link #WImage(WResource resource, CharSequence altText, WContainerWidget parent)
	 * this(resource, altText, (WContainerWidget)null)}
	 */
	public WImage(WResource resource, final CharSequence altText) {
		this(resource, altText, (WContainerWidget) null);
	}

	public void remove() {
		if (this.map_ != null)
			this.map_.remove();
		super.remove();
	}

	/**
	 * Sets an alternate text.
	 * <p>
	 * The alternate text should provide a fallback for browsers that do not
	 * display an image. If no sensible fallback text can be provided, an empty
	 * text is preferred over nonsense.
	 * <p>
	 * This should not be confused with {@link WWebWidget#getToolTip()
	 * WWebWidget#getToolTip()} text, which provides additional information that
	 * is displayed when the mouse hovers over the image.
	 * <p>
	 * The default alternate text is an empty text (&quot;&quot;).
	 * <p>
	 * 
	 * @see WImage#getAlternateText()
	 */
	public void setAlternateText(final CharSequence text) {
		if (canOptimizeUpdates() && text.equals(this.altText_)) {
			return;
		}
		this.altText_ = WString.toWString(text);
		this.flags_.set(BIT_ALT_TEXT_CHANGED);
		this.repaint();
	}

	/**
	 * Returns the alternate text.
	 * <p>
	 * 
	 * @see WImage#setAlternateText(CharSequence text)
	 */
	public WString getAlternateText() {
		return this.altText_;
	}

	/**
	 * Sets the image link.
	 * <p>
	 * The image may be specified as a URL or as a resource. A resource
	 * specifies application-dependent content, which may be used to generate an
	 * image on demand.
	 */
	public void setImageLink(final WLink link) {
		if (link.getType() != WLink.Type.Resource && canOptimizeUpdates()
				&& link.equals(this.imageLink_)) {
			return;
		}
		this.imageLink_ = link;
		if (link.getType() == WLink.Type.Resource) {
			link.getResource().dataChanged()
					.addListener(this, new Signal.Listener() {
						public void trigger() {
							WImage.this.resourceChanged();
						}
					});
		}
		this.flags_.set(BIT_IMAGE_LINK_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	/**
	 * Returns the image link.
	 * <p>
	 */
	public WLink getImageLink() {
		return this.imageLink_;
	}

	/**
	 * Sets the image URL (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use {@link WImage#setImageLink(WLink link) setImageLink()}
	 *             instead.
	 */
	public void setImageRef(final String ref) {
		this.setImageLink(new WLink(ref));
	}

	/**
	 * Returns the image URL (<b>deprecated</b>).
	 * <p>
	 * When the image is specified as a resource, this returns the current
	 * resource URL.
	 * <p>
	 * 
	 * @deprecated Use {@link WImage#getImageLink() getImageLink()} instead.
	 */
	public String getImageRef() {
		return this.imageLink_.getUrl();
	}

	/**
	 * Sets the image resource (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use {@link WImage#setImageLink(WLink link) setImageLink()}
	 *             instead.
	 */
	public void setResource(WResource resource) {
		this.setImageLink(new WLink(resource));
	}

	/**
	 * Returns the image resource (<b>deprecated</b>.
	 * <p>
	 * Returns <code>null</code> if no image resource was set.
	 * <p>
	 * 
	 * @deprecated Use {@link WImage#setImageLink(WLink link) setImageLink()}
	 *             instead.
	 */
	public WResource getResource() {
		return this.imageLink_.getResource();
	}

	/**
	 * Adds an interactive area.
	 * <p>
	 * Adds the <code>area</code> which listens to events in a specific region
	 * of the image. Areas are organized in an indexed list, to which the given
	 * <code>area</code> is appended. When areas overlap, the area with the
	 * lowest index receives the event.
	 * <p>
	 * Ownership of the <code>area</code> is transferred to the image.
	 * <p>
	 * 
	 * @see WImage#insertArea(int index, WAbstractArea area) <p>
	 *      <i><b>Note: </b>Currently it is not possible to add a first area
	 *      after the image has been rendered. If you want to use interactive
	 *      areas you need to add one immediately. </i>
	 *      </p>
	 */
	public void addArea(WAbstractArea area) {
		this.insertArea(this.map_ != null ? this.map_.getCount() : 0, area);
	}

	/**
	 * Inserts an interactive area.
	 * <p>
	 * Inserts the <code>area</code> which listens to events in the coresponding
	 * area of the image. Areas are organized in a list, and the <i>area</i> is
	 * inserted at index <code>index</code>. When areas overlap, the area with
	 * the lowest index receives the event.
	 * <p>
	 * Ownership of the <code>area</code> is transferred to the image.
	 * <p>
	 * 
	 * @see WImage#addArea(WAbstractArea area) <p>
	 *      <i><b>Note: </b>Currently it is not possible to add a first area
	 *      after the image has been rendered. If you want to use interactive
	 *      areas you need to add one immediately. </i>
	 *      </p>
	 */
	public void insertArea(int index, WAbstractArea area) {
		if (!(this.map_ != null)) {
			this.addChild(this.map_ = new MapWidget());
			this.flags_.set(BIT_MAP_CREATED);
			this.repaint();
		}
		this.map_.insertWidget(index, area.getImpl());
	}

	/**
	 * Removes an interactive area.
	 * <p>
	 * Removes the <code>area</code> from this widget, and also returns the
	 * ownership.
	 * <p>
	 * 
	 * @see WImage#addArea(WAbstractArea area)
	 */
	public void removeArea(WAbstractArea area) {
		if (!(this.map_ != null)) {
			logger.error(new StringWriter()
					.append("removeArea(): no such area").toString());
			return;
		}
		this.map_.removeWidget(area.getImpl());
	}

	/**
	 * Returns the interactive area at the given index.
	 * <p>
	 * Returns <code>null</code> if <code>index</code> was invalid.
	 * <p>
	 * 
	 * @see WImage#insertArea(int index, WAbstractArea area)
	 */
	public WAbstractArea getArea(int index) {
		if (this.map_ != null && index < this.map_.getCount()) {
			return WAbstractArea.areaForImpl(this.map_.getWidget(index));
		} else {
			return null;
		}
	}

	/**
	 * Returns the interactive areas set for this widget.
	 * <p>
	 * 
	 * @see WImage#addArea(WAbstractArea area)
	 */
	public List<WAbstractArea> getAreas() {
		List<WAbstractArea> result = new ArrayList<WAbstractArea>();
		if (this.map_ != null) {
			for (int i = 0; i < this.map_.getCount(); ++i) {
				result.add(WAbstractArea.areaForImpl(this.map_.getWidget(i)));
			}
		}
		return result;
	}

	/**
	 * Event emitted when the image was loaded.
	 */
	public EventSignal imageLoaded() {
		return this.voidEventSignal(LOAD_SIGNAL, true);
	}

	public void setTargetJS(String targetJS) {
		this.targetJS_ = targetJS;
	}

	public String getUpdateAreasJS() {
		StringBuilder ss = new StringBuilder();
		if (this.targetJS_.length() != 0) {
			ss.append("(function(){var w = ").append(this.getJsRef())
					.append(";if (w) {var o = jQuery.data(")
					.append(this.getJsRef())
					.append(", 'obj');if (o) { o.updateAreas(); }}})();");
		}
		return ss.toString();
	}

	public String getSetAreaCoordsJS() {
		StringBuilder ss = new StringBuilder();
		if (this.targetJS_.length() != 0) {
			ss.append("jQuery.data(").append(this.getJsRef())
					.append(", 'obj').setAreaCoordsJSON(")
					.append(this.getUpdateAreaCoordsJSON()).append(");");
		}
		return ss.toString();
	}

	private static String LOAD_SIGNAL = "load";
	private static final int BIT_ALT_TEXT_CHANGED = 0;
	private static final int BIT_IMAGE_LINK_CHANGED = 1;
	private static final int BIT_MAP_CREATED = 2;
	private WString altText_;
	private WLink imageLink_;
	private MapWidget map_;
	BitSet flags_;
	String targetJS_;

	private void resourceChanged() {
		this.flags_.set(BIT_IMAGE_LINK_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	protected void getDomChanges(final List<DomElement> result, WApplication app) {
		if (this.map_ != null) {
			DomElement e = DomElement.getForUpdate("i" + this.getId(),
					DomElementType.DomElement_IMG);
			this.updateDom(e, false);
			result.add(e);
		} else {
			super.getDomChanges(result, app);
		}
	}

	void updateDom(final DomElement element, boolean all) {
		DomElement img = element;
		if (all && element.getType() == DomElementType.DomElement_SPAN) {
			DomElement map = this.map_.createSDomElement(WApplication
					.getInstance());
			element.addChild(map);
			img = DomElement.createNew(DomElementType.DomElement_IMG);
			img.setId("i" + this.getId());
		}
		if (this.flags_.get(BIT_IMAGE_LINK_CHANGED) || all) {
			String url = "";
			WApplication app = WApplication.getInstance();
			if (!this.imageLink_.isNull()) {
				url = resolveRelativeUrl(this.imageLink_.getUrl());
				url = app.encodeUntrustedUrl(url);
			} else {
				url = app.getOnePixelGifUrl();
			}
			img.setProperty(Property.PropertySrc, url);
			this.flags_.clear(BIT_IMAGE_LINK_CHANGED);
		}
		if (this.flags_.get(BIT_ALT_TEXT_CHANGED) || all) {
			img.setAttribute("alt", this.altText_.toString());
			this.flags_.clear(BIT_ALT_TEXT_CHANGED);
		}
		if (this.flags_.get(BIT_MAP_CREATED) || all && this.map_ != null) {
			img.setAttribute("usemap", '#' + this.map_.getId());
			this.flags_.clear(BIT_MAP_CREATED);
		}
		super.updateDom(img, all);
		if (element != img) {
			element.addChild(img);
		}
	}

	protected void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WImage.js", wtjs1());
		StringBuilder ss = new StringBuilder();
		ss.append("new Wt3_3_7.WImage(").append(app.getJavaScriptClass())
				.append(",").append(this.getJsRef()).append(",")
				.append(this.targetJS_).append(");");
		this.doJavaScript(ss.toString());
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			if (this.targetJS_.length() != 0) {
				this.defineJavaScript();
			}
		}
		super.render(flags);
	}

	DomElementType getDomElementType() {
		return this.map_ != null ? DomElementType.DomElement_SPAN
				: DomElementType.DomElement_IMG;
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_IMAGE_LINK_CHANGED);
		this.flags_.clear(BIT_ALT_TEXT_CHANGED);
		super.propagateRenderOk(deep);
	}

	protected String getUpdateAreaCoordsJSON() {
		StringBuilder js = new StringBuilder();
		final List<WAbstractArea> areas = this.getAreas();
		if (!areas.isEmpty()) {
			for (int i = 0; i < areas.size(); ++i) {
				if (areas.get(i).isTransformable()) {
					if ((js.length() == 0)) {
						js.append("[");
					} else {
						js.append(",");
					}
					js.append(areas.get(i).getUpdateAreaCoordsJS());
				}
			}
			js.append("]");
		} else {
			js.append("null");
		}
		return js.toString();
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WImage",
				"function(o,p,q){jQuery.data(p,\"obj\",this);var k=this,g=null,e=0,h=\"\",b=null,d=0;this.setAreaCoordsJSON=function(a){g=a;h=\"\";this.updateAreas()};this.updateAreas=function(){var a=q.combinedTransform;if(!(a===undefined||g===null)){a=a();var i=a.toString()!==h.toString();if(e===0&&d===0){if(i){h=a;e=(new Date).getTime();b=setTimeout(this.updateAreas,100)}}else{var c=(new Date).getTime();if(i){h=a;e=c;d=0;b&&clearTimeout(b);b=setTimeout(this.updateAreas, 100)}else if(d>0||e&&c-e>100)if(k.updateAreaCoords(50)){d=e=0;b&&clearTimeout(b);b=null}else{b&&clearTimeout(b);b=setTimeout(k.updateAreas,0)}else{a=c-e;if(a>100)a=100;b&&clearTimeout(b);b=setTimeout(k.updateAreas,a)}}}};this.updateAreaCoords=function(a){var i=o.WT.gfxUtils.transform_mult;a=d+a;if(a>g.length)a=g.length;for(;d<a;){var c=g[d],l=c[0];c=c[1];for(var m=c.length,j=\"\",f=0;f+1<m;f+=2){if(f>0)j+=\",\";var n=i(h,c.slice(f,f+2));j+=Math.round(n[0]).toString()+\",\"+Math.round(n[1]).toString()}if(f< m)j+=\",\"+c[f].toString();if(l)l.coords=j;d++}return a===g.length}}");
	}
}
