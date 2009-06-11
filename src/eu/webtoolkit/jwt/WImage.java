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
 * A widget that displays an image
 * 
 * 
 * The image may be specified either as a URL, or may be dynamically generated
 * by a .
 * <p>
 * You may listen to events by attaching event listeners to signals such as
 * {@link WInteractWidget#clicked()}. Since mouse events pass the coordinates
 * through a {@link WMouseEvent} object, it is possible to react to clicks in
 * specific parts of the image. An alternative is to define interactive areas on
 * the image using {@link WImage#addArea(WAbstractArea area)}, which in addition
 * allows to have customized tool tips for certain image areas (using
 * {@link WAbstractArea#setToolTip(CharSequence text)}).
 * <p>
 * WImage is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * The widget corresponds to the HTML <code>&lt;img&gt;</code> tag.
 * <p>
 * 
 * @see WPaintedWidget
 */
public class WImage extends WInteractWidget {
	/**
	 * Create an empty image widget.
	 */
	public WImage(WContainerWidget parent) {
		super(parent);
		this.altText_ = new WString();
		this.imageRef_ = "";
		this.resource_ = null;
		this.map_ = null;
		this.flags_ = new BitSet();
		this.setLoadLaterWhenInvisible(false);
	}

	public WImage() {
		this((WContainerWidget) null);
	}

	/**
	 * Create an image widget with given image URL.
	 */
	public WImage(String imageRef, WContainerWidget parent) {
		super(parent);
		this.altText_ = new WString();
		this.imageRef_ = imageRef;
		this.resource_ = null;
		this.map_ = null;
		this.flags_ = new BitSet();
		this.setLoadLaterWhenInvisible(false);
	}

	public WImage(String imageRef) {
		this(imageRef, (WContainerWidget) null);
	}

	/**
	 * Create an image widget with given image URL and alternate text.
	 */
	public WImage(String imageRef, CharSequence altText, WContainerWidget parent) {
		super(parent);
		this.altText_ = new WString(altText);
		this.imageRef_ = imageRef;
		this.resource_ = null;
		this.map_ = null;
		this.flags_ = new BitSet();
		this.setLoadLaterWhenInvisible(false);
	}

	public WImage(String imageRef, CharSequence altText) {
		this(imageRef, altText, (WContainerWidget) null);
	}

	/**
	 * Create an image widget with given image resource and alternate text.
	 * 
	 * Use this constructor if you want to present a dynamically generated
	 * image.
	 */
	public WImage(WResource resource, CharSequence altText,
			WContainerWidget parent) {
		super(parent);
		this.altText_ = new WString(altText);
		this.imageRef_ = "";
		this.resource_ = resource;
		this.map_ = null;
		this.flags_ = new BitSet();
		this.resource_.dataChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				WImage.this.resourceChanged();
			}
		});
		this.imageRef_ = this.resource_.generateUrl();
		this.setLoadLaterWhenInvisible(false);
	}

	public WImage(WResource resource, CharSequence altText) {
		this(resource, altText, (WContainerWidget) null);
	}

	public void remove() {
		if (this.map_ != null)
			this.map_.remove();
		super.remove();
	}

	/**
	 * Set an alternate text.
	 * 
	 * The alternate text should provide a fallback for browsers that do not
	 * display an image. If no sensible fallback text can be provided, an empty
	 * text is preferred over nonsense.
	 * <p>
	 * This should not be confused with {@link WWebWidget#getToolTip()} text,
	 * which provides additional information that is displayed when the mouse
	 * hovers over the image.
	 * <p>
	 * The default alternate text is an empty text (&quot;&quot;).
	 * <p>
	 * 
	 * @see WImage#getAlternateText()
	 */
	public void setAlternateText(CharSequence text) {
		if (canOptimizeUpdates() && text.equals(this.altText_)) {
			return;
		}
		this.altText_ = WString.toWString(text);
		this.flags_.set(BIT_ALT_TEXT_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Returns the alternate text.
	 * 
	 * @see WImage#setAlternateText(CharSequence text)
	 */
	public WString getAlternateText() {
		return this.altText_;
	}

	/**
	 * Set the image URL.
	 * 
	 * This should not be used when the image is specified as a resource.
	 * <p>
	 * 
	 * @see WImage#setResource(WResource resource)
	 */
	public void setImageRef(String ref) {
		if (canOptimizeUpdates() && ref.equals(this.imageRef_)) {
			return;
		}
		this.imageRef_ = ref;
		this.flags_.set(BIT_IMAGE_REF_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
	}

	/**
	 * Returns the image URL.
	 * 
	 * When the image is specified as a resource, this returns the current
	 * resource URL.
	 */
	public String getImageRef() {
		return this.imageRef_;
	}

	/**
	 * Set the image resource.
	 * 
	 * A resource specifies application-dependent content, which may be used to
	 * generate an image on demand.
	 * <p>
	 * This sets <i>resource</i> as the contents for the image, as an
	 * alternative to {@link WImage#setImageRef(String ref)}. The resource may
	 * be cleared by passing <i>resource</i> = 0.
	 * <p>
	 * The image does not assume ownership of the resource.
	 * <p>
	 * 
	 * @see WImage#setImageRef(String ref)
	 */
	public void setResource(WResource resource) {
		this.resource_ = resource;
		this.resource_.dataChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				WImage.this.resourceChanged();
			}
		});
		this.setImageRef(this.resource_.generateUrl());
	}

	/**
	 * Returns the image resource.
	 * 
	 * Returns 0 if no image resource was set.
	 */
	public WResource getResource() {
		return this.resource_;
	}

	/**
	 * Add an interactive area.
	 * 
	 * Adds the <i>area</i> which listens to events in a specific region of the
	 * image. Areas are organized in an indexed list, to which the given
	 * <i>area</i> is appended. When areas overlap, the area with the lowest
	 * index receives the event.
	 * <p>
	 * Ownership of the <i>area</i> is transferred to the image.
	 * <p>
	 * 
	 * @see WImage#insertArea(int index, WAbstractArea area)
	 */
	public void addArea(WAbstractArea area) {
		this.insertArea(this.map_ != null ? this.map_.getCount() : 0, area);
	}

	/**
	 * Insert an interactive area.
	 * 
	 * Inserts the <i>area</i> which listens to events in the coresponding area
	 * of the image. Areas are organized in a list, and the <i>area</i> is
	 * inserted at index <i>index</i>. When areas overlap, the area with the
	 * lowest index receives the event.
	 * <p>
	 * Ownership of the <i>Area</i> is transferred to the image.
	 * <p>
	 * 
	 * @see WImage#addArea(WAbstractArea area)
	 */
	public void insertArea(int index, WAbstractArea area) {
		if (!(this.map_ != null)) {
			this.addChild(this.map_ = new MapWidget());
			this.flags_.set(BIT_MAP_CREATED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		}
		this.map_.insertWidget(index, area.getImpl());
	}

	/**
	 * Removes an interactive area.
	 * 
	 * Removes the <i>area</i> from this widget, and also returns the ownership.
	 * <p>
	 * 
	 * @see WImage#addArea(WAbstractArea area)
	 */
	public void removeArea(WAbstractArea area) {
		if (!(this.map_ != null)) {
			WApplication.getInstance().log("error").append(
					"WImage::removeArea(): no such area");
			return;
		}
		this.map_.removeWidget(area.getImpl());
	}

	/**
	 * Returns the interactive area at the given index.
	 * 
	 * Returns 0 if <i>index</i> was invalid.
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

	private static final int BIT_ALT_TEXT_CHANGED = 0;
	private static final int BIT_IMAGE_REF_CHANGED = 1;
	private static final int BIT_MAP_CREATED = 2;
	private WString altText_;
	private String imageRef_;
	private WResource resource_;
	private MapWidget map_;
	private BitSet flags_;

	private void resourceChanged() {
		this.setImageRef(this.resource_.generateUrl());
	}

	protected void getDomChanges(List<DomElement> result, WApplication app) {
		if (this.map_ != null) {
			DomElement e = DomElement.getForUpdate("i" + this.getFormName(),
					DomElementType.DomElement_IMG);
			this.updateDom(e, false);
			result.add(e);
		} else {
			super.getDomChanges(result, app);
		}
	}

	protected void updateDom(DomElement element, boolean all) {
		DomElement img = element;
		if (all && element.getType() == DomElementType.DomElement_SPAN) {
			DomElement map = this.map_.createDomElement(WApplication
					.getInstance());
			element.addChild(map);
			img = DomElement.createNew(DomElementType.DomElement_IMG);
			img.setId("i" + this.getFormName(), false);
		}
		if (this.flags_.get(BIT_IMAGE_REF_CHANGED) || all) {
			if (this.imageRef_.length() != 0) {
				img.setProperty(Property.PropertySrc,
						fixRelativeUrl(this.imageRef_));
			}
			this.flags_.clear(BIT_IMAGE_REF_CHANGED);
		}
		if (this.flags_.get(BIT_ALT_TEXT_CHANGED) || all) {
			img.setAttribute("alt", this.altText_.toString());
			this.flags_.clear(BIT_ALT_TEXT_CHANGED);
		}
		if (this.flags_.get(BIT_MAP_CREATED) || all && this.map_ != null) {
			img.setAttribute("usemap", '#' + this.map_.getFormName());
			this.flags_.clear(BIT_MAP_CREATED);
		}
		super.updateDom(img, all);
		if (element != img) {
			element.addChild(img);
		}
	}

	protected DomElementType getDomElementType() {
		return this.map_ != null ? DomElementType.DomElement_SPAN
				: DomElementType.DomElement_IMG;
	}

	protected void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_IMAGE_REF_CHANGED);
		this.flags_.clear(BIT_ALT_TEXT_CHANGED);
		super.propagateRenderOk(deep);
	}

	private static String LOAD_SIGNAL = "load";
}
