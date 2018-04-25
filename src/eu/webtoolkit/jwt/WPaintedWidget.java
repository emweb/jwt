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
 * A widget that is painted using vector graphics.
 * <p>
 * 
 * A painted widget is rendered from basic drawing primitives. Rendering is done
 * not on the server but on the browser, using different rendering methods:
 * <p>
 * <table border="1" cellspacing="3" cellpadding="3">
 * <tr>
 * <td><b>Browser</b></td>
 * <td><b>Methods</b></td>
 * <td><b>Default method</b></td>
 * </tr>
 * <tr>
 * <td>Firefox 1.5+</td>
 * <td>HtmlCanvas, InlineSVG, PngImage</td>
 * <td>HtmlCanvas</td>
 * </tr>
 * <tr>
 * <td>Internet Explorer 6.0+</td>
 * <td>InlineVML, PngImage</td>
 * <td>InlineVML</td>
 * </tr>
 * <tr>
 * <td>Internet Explorer 9+</td>
 * <td>HtmlCanvas, InlineSVG, PngImage</td>
 * <td>HtmlCanvas</td>
 * </tr>
 * <tr>
 * <td>Safari</td>
 * <td>HtmlCanvas, InlineSVG, PngImage</td>
 * <td>HtmlCanvas</td>
 * </tr>
 * <tr>
 * <td>Opera</td>
 * <td>InlineSVG, HtmlCanvas*, PngImage</td>
 * <td>InlineSVG</td>
 * </tr>
 * <tr>
 * <td>other</td>
 * <td>?</td>
 * <td>HtmlCanvas, PngImage</td>
 * </tr>
 * </table>
 * <p>
 * <i>* HtmlCanvas occasionally suffers from rendering artefacts in Opera.</i>
 * <p>
 * The different rendering methods correspond to different {@link WPaintDevice}
 * implementations, from which this widget choses a suitable one depending on
 * the browser capabilities and configuration.
 * <p>
 * If no JavaScript is available, the JavaScript-based HtmlCanvas will not be
 * used, and InlineSVG will be used instead. The method used may be changed by
 * using {@link WPaintedWidget#setPreferredMethod(WPaintedWidget.Method method)
 * setPreferredMethod()}.
 * <p>
 * In some browsers, InlineSVG requires that the document is rendered as XHTML.
 * This must be enabled in the configuration file using the
 * <code>&lt;send-xhtml-mime-type&gt;</code> option. By default, this option is
 * off. Firefox 4 and Chrome do support svg in normal html mode.
 * <p>
 * The PngImage is the most portable rendering method, and may be the fastest if
 * the painting is of high complexity and/or the image is fairly small.
 * <p>
 * To use a WPaintedWidget, you must derive from it and reimplement
 * {@link WPaintedWidget#paintEvent(WPaintDevice paintDevice) paintEvent()}. To
 * paint on a {@link WPaintDevice}, you will need to use a {@link WPainter}.
 * Repainting is triggered by calling the
 * {@link WPaintedWidget#update(EnumSet flags) update()} method.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * Styling through CSS is not applicable.
 * <p>
 * <p>
 * <i><b>Note: </b>A WPaintedWidget requires that it is given a size using
 * {@link WPaintedWidget#resize(WLength width, WLength height) resize()} or by a
 * layout manager.</i>
 * </p>
 * <h3>Client side interaction and repainting</h3>
 * <p>
 * If the widget is drawn as an HTML canvas element, i.e. the
 * {@link WPaintedWidget#getMethod() method} is HtmlCanvas, a WPaintedWidget can
 * expose certain objects to be modified client side.
 * <p>
 * 
 * @see WJavaScriptHandle
 * @see WJavaScriptExposableObject
 * @see WImage
 */
public abstract class WPaintedWidget extends WInteractWidget {
	private static Logger logger = LoggerFactory
			.getLogger(WPaintedWidget.class);

	/**
	 * Enumeration that indicates a rendering method.
	 */
	public enum Method {
		/**
		 * SVG (Most browsers) or VML (Internet Explorer &lt; 9) embedded in the
		 * page.
		 */
		InlineSvgVml,
		/**
		 * The WHATWG HTML 5 canvas element.
		 */
		HtmlCanvas,
		/**
		 * Using a PNG image resource.
		 */
		PngImage;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Create a new painted widget.
	 */
	public WPaintedWidget(WContainerWidget parent) {
		super(parent);
		this.preferredMethod_ = WPaintedWidget.Method.HtmlCanvas;
		this.painter_ = null;
		this.needRepaint_ = false;
		this.sizeChanged_ = false;
		this.areaImageAdded_ = false;
		this.repaintFlags_ = EnumSet.noneOf(PaintFlag.class);
		this.areaImage_ = null;
		this.renderWidth_ = 0;
		this.renderHeight_ = 0;
		this.repaintSlot_ = new JSlot("function() {var o=" + this.getObjJsRef()
				+ ";if(o){o.repaint();}}", this);
		this.jsObjects_ = new WJavaScriptObjectStorage(this);
		this.jsDefined_ = false;
		if (WApplication.getInstance() != null) {
			final WEnvironment env = WApplication.getInstance()
					.getEnvironment();
			if (env.agentIsOpera()
					&& env.getUserAgent().indexOf("Mac OS X") == -1) {
				this.preferredMethod_ = WPaintedWidget.Method.InlineSvgVml;
			}
		}
		this.setInline(false);
	}

	/**
	 * Create a new painted widget.
	 * <p>
	 * Calls {@link #WPaintedWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WPaintedWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		;
		if (this.areaImage_ != null)
			this.areaImage_.remove();
		super.remove();
	}

	/**
	 * Sets the preferred rendering method.
	 * <p>
	 * When <code>method</code> is supported by the browser, then it is chosen
	 * for rendering.
	 */
	public void setPreferredMethod(WPaintedWidget.Method method) {
		if (this.preferredMethod_ != method) {
			;
			this.painter_ = null;
			this.preferredMethod_ = method;
		}
	}

	/**
	 * Returns the preferred rendering method.
	 * <p>
	 * 
	 * @see WPaintedWidget#setPreferredMethod(WPaintedWidget.Method method)
	 */
	public WPaintedWidget.Method getPreferredMethod() {
		return this.preferredMethod_;
	}

	/**
	 * Lets the widget repaint itself.
	 * <p>
	 * Repainting is not immediate, but happens after when the event loop is
	 * exited.
	 * <p>
	 * Unless a {@link PaintFlag#PaintUpdate} paint flag is set, the widget is
	 * first cleared.
	 */
	public void update(EnumSet<PaintFlag> flags) {
		this.needRepaint_ = true;
		this.repaintFlags_.addAll(flags);
		this.repaint();
	}

	/**
	 * Lets the widget repaint itself.
	 * <p>
	 * Calls {@link #update(EnumSet flags) update(EnumSet.of(flag, flags))}
	 */
	public final void update(PaintFlag flag, PaintFlag... flags) {
		update(EnumSet.of(flag, flags));
	}

	/**
	 * Lets the widget repaint itself.
	 * <p>
	 * Calls {@link #update(EnumSet flags)
	 * update(EnumSet.noneOf(PaintFlag.class))}
	 */
	public final void update() {
		update(EnumSet.noneOf(PaintFlag.class));
	}

	public void resize(final WLength width, final WLength height) {
		if (!width.isAuto() && !height.isAuto()) {
			this.setLayoutSizeAware(false);
			this.resizeCanvas((int) width.toPixels(), (int) height.toPixels());
		}
		super.resize(width, height);
	}

	/**
	 * Adds an interactive area.
	 * <p>
	 * Adds the <code>area</code> which listens to events in a specific region
	 * of the widget. Areas are organized in a list, to which the given
	 * <code>area</code> is appended. When areas overlap, the area with the
	 * lowest index receives the event.
	 * <p>
	 * Ownership of the <code>area</code> is transferred to the widget.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>When defining at least one area, no more events will
	 * propagate to the widget itself. As a work-around, you can emulate this by
	 * listening for events on a {@link WRectArea} that corresponds to the whole
	 * widget, and which is added as the last area (catching all events that
	 * were not caught by preceding areas).</i>
	 * </p>
	 * 
	 * @see WPaintedWidget#insertArea(int index, WAbstractArea area)
	 */
	public void addArea(WAbstractArea area) {
		this.createAreaImage();
		this.areaImage_.addArea(area);
	}

	/**
	 * Inserts an interactive area.
	 * <p>
	 * Inserts the <code>area</code> which listens to events in the coresponding
	 * area of the widget. Areas are organized in a list, and the <i>area</i> is
	 * inserted at index <code>index</code>. When areas overlap, the area with
	 * the lowest index receives the event.
	 * <p>
	 * Ownership of the <code>Area</code> is transferred to the widget.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>When defining at least one area, no more events will
	 * propagate to the widget itself. As a work-around, you can emulate this by
	 * listening for events on a {@link WRectArea} that corresponds to the whole
	 * widget, and which is added as the last area (catching all events that
	 * were not caught by preceding areas).</i>
	 * </p>
	 * 
	 * @see WPaintedWidget#addArea(WAbstractArea area)
	 */
	public void insertArea(int index, WAbstractArea area) {
		this.createAreaImage();
		this.areaImage_.insertArea(index, area);
	}

	/**
	 * Removes an interactive area.
	 * <p>
	 * Removes the <code>area</code> from this widget, returning the ownership.
	 * <p>
	 * 
	 * @see WPaintedWidget#addArea(WAbstractArea area)
	 */
	public void removeArea(WAbstractArea area) {
		this.createAreaImage();
		this.areaImage_.removeArea(area);
	}

	/**
	 * Returns the interactive area at the given index.
	 * <p>
	 * Returns <code>null</code> if <code>index</code> was invalid.
	 * <p>
	 * 
	 * @see WPaintedWidget#insertArea(int index, WAbstractArea area)
	 */
	public WAbstractArea getArea(int index) {
		return this.areaImage_ != null ? this.areaImage_.getArea(index) : null;
	}

	/**
	 * Returns the interactive areas set for this widget.
	 * <p>
	 * 
	 * @see WPaintedWidget#addArea(WAbstractArea area)
	 */
	public List<WAbstractArea> getAreas() {
		return this.areaImage_ != null ? this.areaImage_.getAreas()
				: (List<WAbstractArea>) new ArrayList<WAbstractArea>();
	}

	/**
	 * A JavaScript slot that repaints the widget when triggered.
	 * <p>
	 * This is useful for client-side initiated repaints. You may want to use
	 * this if you want to add interaction or animation to your
	 * {@link WPaintedWidget}.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>This feature is currently only supported if the
	 * {@link WPaintedWidget#getMethod() method} is HtmlCanvas. This will not
	 * cause a server roundtrip. Instead, the resulting JavaScript of
	 * {@link WPaintedWidget#paintEvent(WPaintDevice paintDevice) paintEvent()}
	 * will be re-executed on the client side.</i>
	 * </p>
	 * 
	 * @see WPaintedWidget#getObjJsRef()
	 */
	public JSlot getRepaintSlot() {
		return this.repaintSlot_;
	}

	/**
	 * Create a {@link WTransform} that is accessible from JavaScript,
	 * associated with this WPaintedWidget.
	 */
	protected WJavaScriptHandle<WTransform> createJSTransform() {
		return this.jsObjects_.addObject(new WTransform());
	}

	/**
	 * Create a {@link WBrush} that is accessible from JavaScript, associated
	 * with this WPaintedWidget.
	 */
	protected WJavaScriptHandle<WBrush> createJSBrush() {
		return this.jsObjects_.addObject(new WBrush());
	}

	/**
	 * Create a {@link WPen} that is accessible from JavaScript, associated with
	 * this WPaintedWidget.
	 */
	protected WJavaScriptHandle<WPen> createJSPen() {
		return this.jsObjects_.addObject(new WPen());
	}

	/**
	 * Create a {@link WPainterPath} that is accessible from JavaScript,
	 * associated with this WPaintedWidget.
	 */
	protected WJavaScriptHandle<WPainterPath> createJSPainterPath() {
		return this.jsObjects_.addObject(new WPainterPath());
	}

	/**
	 * Create a {@link WRectF} that is accessible from JavaScript, associated
	 * with this WPaintedWidget.
	 */
	protected WJavaScriptHandle<WRectF> createJSRect() {
		return this.jsObjects_.addObject(new WRectF(0, 0, 0, 0));
	}

	/**
	 * Create a {@link WPointF} that is accessible from JavaScript, associated
	 * with this WPaintedWidget.
	 */
	protected WJavaScriptHandle<WPointF> createJSPoint() {
		return this.jsObjects_.addObject(new WPointF(0, 0));
	}

	protected void layoutSizeChanged(int width, int height) {
		this.resizeCanvas(width, height);
	}

	/**
	 * Returns the actual method used for rendering.
	 * <p>
	 * The default method considers browser capabilites and the preferred method
	 * to make an actual choice for the implementation.
	 * <p>
	 * You may want to reimplement this method to override this choice.
	 */
	protected WPaintedWidget.Method getMethod() {
		final WEnvironment env = WApplication.getInstance().getEnvironment();
		WPaintedWidget.Method method;
		if (this.preferredMethod_ == WPaintedWidget.Method.PngImage) {
			return WPaintedWidget.Method.PngImage;
		}
		if (env.agentIsIElt(9)) {
			method = this.preferredMethod_ == WPaintedWidget.Method.InlineSvgVml ? WPaintedWidget.Method.InlineSvgVml
					: WPaintedWidget.Method.PngImage;
		} else {
			if (!(env.agentIsChrome()
					&& env.getAgent().getValue() >= WEnvironment.UserAgent.Chrome5
							.getValue() || env.agentIsGecko()
					&& env.getAgent().getValue() >= WEnvironment.UserAgent.Firefox4_0
							.getValue())) {
				method = env.hasJavaScript() ? WPaintedWidget.Method.HtmlCanvas
						: WPaintedWidget.Method.PngImage;
			} else {
				if (!env.hasJavaScript()) {
					method = WPaintedWidget.Method.InlineSvgVml;
				} else {
					boolean oldFirefoxMac = (env.getUserAgent().indexOf(
							"Firefox/1.5") != -1 || env.getUserAgent().indexOf(
							"Firefox/2.0") != -1)
							&& env.getUserAgent().indexOf("Macintosh") != -1;
					if (oldFirefoxMac) {
						method = WPaintedWidget.Method.HtmlCanvas;
					} else {
						method = this.preferredMethod_ == WPaintedWidget.Method.PngImage ? WPaintedWidget.Method.HtmlCanvas
								: this.preferredMethod_;
					}
					boolean nokia810 = env.getUserAgent().indexOf("Linux arm") != -1
							&& env.getUserAgent().indexOf("Tablet browser") != -1
							&& env.getUserAgent().indexOf("Gecko") != -1;
					if (nokia810) {
						method = WPaintedWidget.Method.HtmlCanvas;
					} else {
						method = this.preferredMethod_ == WPaintedWidget.Method.PngImage ? WPaintedWidget.Method.HtmlCanvas
								: this.preferredMethod_;
					}
				}
			}
		}
		return method;
	}

	/**
	 * Paints the widget.
	 * <p>
	 * You should reimplement this method to paint the contents of the widget,
	 * using the given paintDevice.
	 */
	protected abstract void paintEvent(WPaintDevice paintDevice);

	/**
	 * Creates a paint device.
	 * <p>
	 * Although it&apos;s usually not necessary to call this function, you may
	 * want to reimplement this function to customize or specialize the device
	 * used for painting the widget.
	 */
	protected WPaintDevice getCreatePaintDevice() {
		(this).isCreatePainter();
		if (this.painter_ != null) {
			return this.painter_.createPaintDevice(true);
		} else {
			return null;
		}
	}

	DomElementType getDomElementType() {
		if (this.isInline()
				&& WApplication.getInstance().getEnvironment().agentIsIElt(9)) {
			return DomElementType.DomElement_SPAN;
		} else {
			return DomElementType.DomElement_DIV;
		}
	}

	void updateDom(final DomElement element, boolean all) {
		if (all && this.areaImage_ != null || this.areaImageAdded_) {
			element.addChild(this.areaImage_.createSDomElement(WApplication
					.getInstance()));
			this.areaImageAdded_ = false;
		}
		super.updateDom(element, all);
	}

	protected DomElement createDomElement(WApplication app) {
		if (this.isInLayout()) {
			this.setLayoutSizeAware(true);
			this.setJavaScriptMember(
					WT_RESIZE_JS,
					"function(self, w, h) {var u = $(self).find('canvas, img');if (w >= 0) u.width(w);if (h >= 0) u.height(h);}");
		}
		this.isCreatePainter();
		DomElement result = DomElement.createNew(this.getDomElementType());
		this.setId(result, app);
		DomElement wrap = result;
		if (this.getWidth().isAuto() && this.getHeight().isAuto()) {
			result.setProperty(Property.PropertyStylePosition, "relative");
			wrap = DomElement.createNew(DomElementType.DomElement_DIV);
			wrap.setProperty(Property.PropertyStylePosition, "absolute");
			wrap.setProperty(Property.PropertyStyleLeft, "0");
			wrap.setProperty(Property.PropertyStyleRight, "0");
		}
		DomElement canvas = DomElement.createNew(DomElementType.DomElement_DIV);
		if (!app.getEnvironment().agentIsSpiderBot()) {
			canvas.setId('p' + this.getId());
		}
		WPaintDevice device = this.painter_.getPaintDevice(false);
		if (this.painter_.getRenderType() == WWidgetPainter.RenderType.InlineVml
				&& this.isInline()) {
			result.setProperty(Property.PropertyStyle, "zoom: 1;");
			canvas.setProperty(Property.PropertyStyleDisplay, "inline");
			canvas.setProperty(Property.PropertyStyle, "zoom: 1;");
		}
		if (this.renderWidth_ != 0 && this.renderHeight_ != 0) {
			this.paintEvent(device);
			if (device.getPainter() != null) {
				device.getPainter().end();
			}
		}
		this.painter_.createContents(canvas, device);
		this.needRepaint_ = false;
		wrap.addChild(canvas);
		if (wrap != result) {
			result.addChild(wrap);
		}
		this.updateDom(result, true);
		return result;
	}

	protected void getDomChanges(final List<DomElement> result, WApplication app) {
		DomElement e = DomElement.getForUpdate(this,
				DomElementType.DomElement_DIV);
		this.updateDom(e, false);
		result.add(e);
		boolean createdNew = this.isCreatePainter();
		if (this.needRepaint_) {
			WPaintDevice device = this.painter_.getPaintDevice(!EnumUtils.mask(
					this.repaintFlags_, PaintFlag.PaintUpdate).isEmpty()
					&& !createdNew);
			if (this.renderWidth_ != 0 && this.renderHeight_ != 0) {
				this.paintEvent(device);
				if (device.getPainter() != null) {
					device.getPainter().end();
				}
			}
			if (createdNew) {
				DomElement canvas = DomElement.getForUpdate('p' + this.getId(),
						DomElementType.DomElement_DIV);
				canvas.removeAllChildren();
				this.painter_.createContents(canvas, device);
				result.add(canvas);
			} else {
				this.painter_.updateContents(result, device);
			}
			this.needRepaint_ = false;
			this.repaintFlags_.clear();
		}
	}

	void propagateRenderOk(boolean deep) {
		this.needRepaint_ = false;
		super.propagateRenderOk(deep);
	}

	protected void enableAjax() {
		if (((this.painter_) instanceof WWidgetCanvasPainter ? (WWidgetCanvasPainter) (this.painter_)
				: null) != null
				&& this.renderWidth_ != 0 && this.renderHeight_ != 0) {
			this.update();
		}
		super.enableAjax();
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()
				|| !this.jsDefined_) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	protected void setFormData(final WObject.FormData formData) {
		String[] parVals = formData.values;
		if ((parVals.length == 0)) {
			return;
		}
		if (parVals[0].equals("undefined")) {
			return;
		}
		this.jsObjects_.assignFromJSON(parVals[0]);
	}

	/**
	 * Returns a JavaScript reference to the client side representation of the
	 * {@link WPaintedWidget}.
	 * <p>
	 * The client side representation exposes the following interface:
	 * 
	 * <pre>
	 *   {@code
	 *    {
	 *      canvas: exposes the underlying HTML canvas element
	 *      repaint: a function that, when called, will repaint the widget without a server roundtrip
	 *    }
	 *   }
	 * </pre>
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The {@link WPaintedWidget#getMethod() method} should be
	 * HtmlCanvas and there has to be at least one {@link WJavaScriptHandle}
	 * associated with this {@link WPaintedWidget} in order for this reference
	 * to be valid. </i>
	 * </p>
	 */
	protected String getObjJsRef() {
		return "jQuery.data(" + this.getJsRef() + ",'obj')";
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		if (this.getMethod() == WPaintedWidget.Method.HtmlCanvas) {
			app.loadJavaScript("js/WPaintedWidget.js", wtjs10());
			app.loadJavaScript("js/WPaintedWidget.js", wtjs11());
			if (this.jsObjects_.size() > 0) {
				this.setFormObject(true);
				app.loadJavaScript("js/WJavaScriptObjectStorage.js", wtjs20());
				this.jsDefined_ = true;
			} else {
				this.jsDefined_ = false;
			}
		}
	}

	private WPaintedWidget.Method preferredMethod_;
	private WWidgetPainter painter_;
	private boolean needRepaint_;
	boolean sizeChanged_;
	private boolean areaImageAdded_;
	EnumSet<PaintFlag> repaintFlags_;
	WImage areaImage_;
	int renderWidth_;
	int renderHeight_;
	private JSlot repaintSlot_;
	WJavaScriptObjectStorage jsObjects_;
	private boolean jsDefined_;

	private void resizeCanvas(int width, int height) {
		if (this.renderWidth_ == width && this.renderHeight_ == height) {
			return;
		}
		this.renderWidth_ = width;
		this.renderHeight_ = height;
		if (this.areaImage_ != null) {
			this.areaImage_.resize(new WLength(this.renderWidth_), new WLength(
					this.renderHeight_));
		}
		this.sizeChanged_ = true;
		this.update();
	}

	private boolean isCreatePainter() {
		if (this.painter_ != null) {
			return false;
		}
		final WEnvironment env = WApplication.getInstance().getEnvironment();
		WPaintedWidget.Method method = this.getMethod();
		if (method == WPaintedWidget.Method.InlineSvgVml) {
			if (env.agentIsIElt(9)) {
				this.painter_ = new WWidgetVectorPainter(this,
						WWidgetPainter.RenderType.InlineVml);
			} else {
				this.painter_ = new WWidgetVectorPainter(this,
						WWidgetPainter.RenderType.InlineSvg);
			}
		} else {
			if (method == WPaintedWidget.Method.PngImage) {
				this.painter_ = new WWidgetRasterPainter(this);
			} else {
				this.painter_ = new WWidgetCanvasPainter(this);
			}
		}
		return true;
	}

	private void createAreaImage() {
		if (!(this.areaImage_ != null)) {
			this.areaImage_ = new WImage(WApplication.getInstance()
					.getOnePixelGifUrl());
			this.areaImage_.setParentWidget(this);
			if (this.getPositionScheme() == PositionScheme.Static) {
				this.setPositionScheme(PositionScheme.Relative);
			}
			this.areaImage_.setPositionScheme(PositionScheme.Absolute);
			this.areaImage_.setOffsets(new WLength(0),
					EnumSet.of(Side.Left, Side.Top));
			this.areaImage_.setMargin(new WLength(0), EnumSet.of(Side.Top));
			this.areaImage_.resize(new WLength(this.renderWidth_), new WLength(
					this.renderHeight_));
			this.areaImageAdded_ = true;
		}
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WPaintedWidget",
				"function(J,p){jQuery.data(p,\"obj\",this);var q=this;this.imagePreloaders=[];this.images=[];this.canvas=document.getElementById(\"c\"+p.id);this.repaint=function(){};this.widget=p;this.cancelPreloaders=function(){for(var r=0;r<q.imagePreloaders.length;++r)q.imagePreloaders[r].cancel();q.imagePreloaders=[]}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptObject,
				"gfxUtils",
				"function(){function J(){var g=this;this.path_crisp=function(a){return a.map(function(b){return[Math.floor(b[0])+0.5,Math.floor(b[1])+0.5,b[2]]})};this.transform_mult=function(a,b){if(b.length===2){var c=b[0],e=b[1];return[a[p]*c+a[q]*e+a[v],a[r]*c+a[s]*e+a[w]]}if(b.length===3){if(b[2]===B||b[2]===C)return b.slice(0);c=b[0];e=b[1];return[a[p]*c+a[q]*e+a[v],a[r]*c+a[s]*e+a[w],b[2]]}if(b.length===4){var f,h,d,l;h=g.transform_mult(a,[b[0],b[1]]);c=h[0]; f=h[0];e=h[1];h=h[1];for(d=0;d<3;++d){l=g.transform_mult(a,d==0?[g.rect_left(b),g.rect_bottom(b)]:d==1?[g.rect_right(b),g.rect_top(b)]:[g.rect_right(b),g.rect_bottom(b)]);c=Math.min(c,l[0]);f=Math.max(f,l[0]);e=Math.min(e,l[1]);h=Math.max(h,l[1])}return[c,e,f-c,h-e]}if(b.length===6)return[a[p]*b[p]+a[q]*b[r],a[p]*b[q]+a[q]*b[s],a[r]*b[p]+a[s]*b[r],a[r]*b[q]+a[s]*b[s],a[p]*b[v]+a[q]*b[w]+a[v],a[r]*b[v]+a[s]*b[w]+a[w]];return[]};this.transform_apply=function(a,b){var c=g.transform_mult;return b.map(function(e){return c(a, e)})};this.transform_det=function(a){return a[p]*a[s]-a[q]*a[r]};this.transform_adjoint=function(a){var b=a[p],c=a[r],e=a[q],f=a[s],h=a[v];a=a[w];return[f,-c,-e,b,a*e-h*f,-(a*b-h*c)]};this.transform_inverted=function(a){var b=g.transform_det(a);if(b!=0){a=g.transform_adjoint(a);return[a[p]/b,a[r]/b,a[q]/b,a[s]/b,a[v]/b,a[w]/b]}else{console.log(\"inverted(): oops, determinant == 0\");return a}};this.transform_assign=function(a,b){a[0]=b[0];a[1]=b[1];a[2]=b[2];a[3]=b[3];a[4]=b[4];a[5]=b[5]};this.transform_equal= function(a,b){return a[0]==b[0]&&a[1]==b[1]&&a[2]==b[2]&&a[3]==b[3]&&a[4]==b[4]&&a[5]==b[5]};this.css_text=function(a){return\"rgba(\"+a[0]+\",\"+a[1]+\",\"+a[2]+\",\"+a[3]+\")\"};this.arcPosition=function(a,b,c,e,f){f=-f/180*Math.PI;return[a+c*Math.cos(f),b+e*Math.sin(f)]};this.pnpoly=function(a,b){var c=false,e=0,f=0,h=a[0];a=a[1];var d,l,j;for(d=0;d<b.length;++d){l=e;j=f;if(b[d][2]===K){j=g.arcPosition(b[d][0],b[d][1],b[d+1][0],b[d+1][1],b[d+2][0]);l=j[0];j=j[1]}else if(b[d][2]===C){j=g.arcPosition(b[d- 2][0],b[d-2][1],b[d-1][0],b[d-1][1],b[d][0]+b[d][1]);l=j[0];j=j[1]}else if(b[d][2]!==B){l=b[d][0];j=b[d][1]}if(b[d][2]!==y)if(f>a!==j>a&&h<(l-e)*(a-f)/(j-f)+e)c=!c;e=l;f=j}return c};this.rect_intersection=function(a,b){a=g.rect_normalized(a);b=g.rect_normalized(b);var c=g.rect_top,e=g.rect_bottom,f=g.rect_left,h=g.rect_right;f=Math.max(f(a),f(b));h=Math.min(h(a),h(b));c=Math.max(c(a),c(b));a=Math.min(e(a),e(b));return[f,c,h-f,a-c]};this.drawRect=function(a,b,c,e){b=g.rect_normalized(b);var f=g.rect_top(b), h=g.rect_bottom(b),d=g.rect_left(b);b=g.rect_right(b);path=[[d,f,y],[b,f,x],[b,h,x],[d,h,x],[d,f,x]];g.drawPath(a,path,c,e,false)};this.drawPath=function(a,b,c,e,f){function h(k){return k[0]}function d(k){return k[1]}function l(k){return k[2]}var j=0,n=[],m=[],o=[];a.beginPath();b.length>0&&l(b[0])!==y&&a.moveTo(0,0);for(j=0;j<b.length;j++){var i=b[j];switch(l(i)){case y:a.moveTo(h(i),d(i));break;case x:a.lineTo(h(i),d(i));break;case P:n.push(h(i),d(i));break;case Q:n.push(h(i),d(i));break;case D:n.push(h(i), d(i));a.bezierCurveTo.apply(a,n);n=[];break;case K:m.push(h(i),d(i));break;case B:m.push(h(i));break;case C:m.push(h(i)*Math.PI/180,d(i)*Math.PI/180,d(i)>0);a.arc.apply(a,m);m=[];break;case S:o.push(h(i));break;case E:o.push(h(i),d(i));a.quadraticCurveTo.apply(a,o);o=[];break}}c&&a.fill();e&&a.stroke();f&&a.clip()};this.drawStencilAlongPath=function(a,b,c,e,f,h){function d(o){return o[0]}function l(o){return o[1]}function j(o){return o[2]}var n=0;for(n=0;n<c.length;n++){var m=c[n];if(!(h&&a.wtClipPath&& !g.pnpoly(m,g.transform_apply(a.wtClipPathTransform,a.wtClipPath))))if(j(m)==y||j(m)==x||j(m)==E||j(m)==D){m=g.transform_apply([1,0,0,1,d(m),l(m)],b);g.drawPath(a,m,e,f,false)}}};this.drawText=function(a,b,c,e,f){if(!(f&&a.wtClipPath&&!g.pnpoly(f,g.transform_apply(a.wtClipPathTransform,a.wtClipPath)))){var h=c&F;var d=f=null;switch(c&T){case M:a.textAlign=\"left\";f=g.rect_left(b);break;case N:a.textAlign=\"right\";f=g.rect_right(b);break;case O:a.textAlign=\"center\";f=g.rect_center(b).x;break}switch(h){case G:a.textBaseline= \"top\";d=g.rect_top(b);break;case H:a.textBaseline=\"bottom\";d=g.rect_bottom(b);break;case I:a.textBaseline=\"middle\";d=g.rect_center(b).y;break}if(!(f==null||d==null)){b=a.fillStyle;a.fillStyle=a.strokeStyle;a.fillText(e,f,d);a.fillStyle=b}}};this.calcYOffset=function(a,b,c,e){return e===I?-((b-1)*c/2)+a*c:e===G?a*c:e===H?-(b-1-a)*c:0};this.drawTextOnPath=function(a,b,c,e,f,h,d,l,j){function n(A){return A[0]}function m(A){return A[1]}function o(A){return A[2]}var i=0,k=0;e=g.transform_apply(e,f);for(i= 0;i<f.length;i++){if(i>=b.length)break;k=f[i];var u=e[i],z=b[i].split(\"\\n\");if(o(k)==y||o(k)==x||o(k)==E||o(k)==D)if(h==0)for(k=0;k<z.length;k++){var t=g.calcYOffset(k,z.length,d,l&F);g.drawText(a,[c[0]+n(u),c[1]+m(u)+t,c[2],c[3]],l,z[k],j?[n(u),m(u)]:null)}else{t=h*Math.PI/180;k=Math.cos(-t);t=-Math.sin(-t);var U=-t;a.save();a.transform(k,U,t,k,n(u),m(u));for(k=0;k<z.length;k++){t=g.calcYOffset(k,z.length,d,l&F);g.drawText(a,[c[0],c[1]+t,c[2],c[3]],l,z[k],j?[n(u),m(u)]:null)}a.restore()}}};this.setClipPath= function(a,b,c,e){if(e){a.setTransform.apply(a,c);g.drawPath(a,b,false,false,true);a.setTransform(1,0,0,1,0,0)}a.wtClipPath=b;a.wtClipPathTransform=c};this.removeClipPath=function(a){delete a.wtClipPath;delete a.wtClipPathTransform};this.rect_top=function(a){return a[1]};this.rect_bottom=function(a){return a[1]+a[3]};this.rect_right=function(a){return a[0]+a[2]};this.rect_left=function(a){return a[0]};this.rect_topleft=function(a){return[a[0],a[1]]};this.rect_topright=function(a){return[a[0]+a[2], a[1]]};this.rect_bottomleft=function(a){return[a[0],a[1]+a[3]]};this.rect_bottomright=function(a){return[a[0]+a[2],a[1]+a[3]]};this.rect_center=function(a){return{x:(2*a[0]+a[2])/2,y:(2*a[1]+a[3])/2}};this.rect_normalized=function(a){var b,c,e;if(a[2]>0){b=a[0];e=a[2]}else{b=a[0]+a[2];e=-a[2]}if(a[3]>0){c=a[1];a=a[3]}else{c=a[1]+a[3];a=-a[3]}return[b,c,e,a]}}var p=0,q=1,r=2,s=3,v=4,w=5,y=0,x=1,P=2,Q=3,D=4,S=5,E=6,K=7,B=8,C=9,M=1,N=2,O=4,G=128,I=512,H=1024,F=112|G|256|I|H|2048,T=M|N|O|8;return new J}()");
	}

	static WJavaScriptPreamble wtjs20() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WJavaScriptObjectStorage",
				"function(k,g){function d(b){if(jQuery.isArray(b)){var c=[],a;for(a=0;a<b.length;++a)c.push(d(b[a]));return c}else if(jQuery.isPlainObject(b)){c={};for(a in b)if(b.hasOwnProperty(a))c[a]=d(b[a]);return c}else return b}function e(b,c){if(b===c)return true;if(jQuery.isArray(b)&&jQuery.isArray(c)){if(b.length!==c.length)return false;var a;for(a=0;a<b.length;++a)if(!e(b[a],c[a]))return false;return true}else if(jQuery.isPlainObject(b)&& jQuery.isPlainObject(c)){for(a in b)if(b.hasOwnProperty(a)){if(!c.hasOwnProperty(a))return false;if(!e(b[a],c[a]))return false}for(a in c)if(c.hasOwnProperty(a))if(!b.hasOwnProperty(a))return false;return true}else return false}function h(b){return jQuery.isArray(b)&&b.length>6}function j(){var b={},c,a;for(a=0;a<f.jsValues.length;++a){c=f.jsValues[a];if(!h(c)&&!e(c,i[a]))b[a]=c}return JSON.stringify(b)}jQuery.data(g,\"jsobj\",this);var f=this,i={};this.jsValues=[];this.setJsValue=function(b,c){h(c)|| (i[b]=d(c));f.jsValues[b]=c};g.wtEncodeValue=j}");
	}
}
