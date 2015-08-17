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
 * <p>
 * <i><b>Note: </b>Changes to client side objects are currently not synced back
 * to the server, whereas changes to the handle on the server side <i>will</i>
 * be synced to the client.</i>
 * </p>
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
		this.jsObjects_ = new WJavaScriptObjectStorage(this.getObjJsRef());
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
			this
					.setJavaScriptMember(
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
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	/**
	 * Returns a JavaScript reference to the client side representation of the
	 * {@link WPaintedWidget}.
	 * <p>
	 * The client side representation exposes the following interface:
	 * 
	 * <pre>
	 * {@code
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
		app.loadJavaScript("js/WPaintedWidget.js", wtjs2());
		app.loadJavaScript("js/WPaintedWidget.js", wtjs1());
	}

	private WPaintedWidget.Method preferredMethod_;
	private WWidgetPainter painter_;
	private boolean needRepaint_;
	boolean sizeChanged_;
	private boolean areaImageAdded_;
	EnumSet<PaintFlag> repaintFlags_;
	private WImage areaImage_;
	int renderWidth_;
	int renderHeight_;
	private JSlot repaintSlot_;
	WJavaScriptObjectStorage jsObjects_;

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
			this.areaImage_.setOffsets(new WLength(0), EnumSet.of(Side.Left,
					Side.Top));
			this.areaImage_.setMargin(new WLength(0), EnumSet.of(Side.Top));
			this.areaImage_.resize(new WLength(this.renderWidth_), new WLength(
					this.renderHeight_));
			this.areaImageAdded_ = true;
		}
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WPaintedWidget",
				"function(z,l){this.canvas=document.getElementById(\"c\"+l.id);this.canvas.getContext(\"2d\");jQuery.data(l,\"obj\",this);this.jsValues=[];this.repaint=function(){}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptObject,
				"gfxUtils",
				"function(){function z(){var i=this;this.path_crisp=function(a){return a.map(function(b){return[Math.floor(b[0])+0.5,Math.floor(b[1])+0.5,b[2]]})};this.transform_mult=function(a,b){if(b.length===2){var e=b[0],d=b[1];return[a[l]*e+a[m]*d+a[p],a[n]*e+a[o]*d+a[q]]}if(b.length===3){if(b[2]===x||b[2]===y)return b.slice(0);e=b[0];d=b[1];return[a[l]*e+a[m]*d+a[p],a[n]*e+a[o]*d+a[q],b[2]]}if(b.length===4){var g,f,c,k;f=i.transform_mult(a,[b[0],b[1]]);e=f[0]; g=f[0];d=f[1];f=f[1];for(c=0;c<3;++c){k=i.transform_mult(a,c==0?[i.rect_left(b),i.rect_bottom(b)]:c==1?[i.rect_right(b),i.rect_top(b)]:[i.rect_right(b),i.rect_bottom(b)]);e=Math.min(e,k[0]);g=Math.max(g,k[0]);d=Math.min(d,k[1]);f=Math.max(f,k[1])}return[e,d,g-e,f-d]}if(b.length===6)return[a[l]*b[l]+a[m]*b[n],a[l]*b[m]+a[m]*b[o],a[n]*b[l]+a[o]*b[n],a[n]*b[m]+a[o]*b[o],a[l]*b[p]+a[m]*b[q]+a[p],a[n]*b[p]+a[o]*b[q]+a[q]];return[]};this.transform_apply=function(a,b){var e=i.transform_mult;return b.map(function(d){return e(a, d)})};this.transform_det=function(a){return a[l]*a[o]-a[m]*a[n]};this.transform_adjoint=function(a){var b=a[l],e=a[n],d=a[m],g=a[o],f=a[p];a=a[q];return[g,-e,-d,b,a*d-f*g,-(a*b-f*e)]};this.transform_inverted=function(a){var b=i.transform_det(a);if(b!=0){a=i.transform_adjoint(a);return[a[l]/b,a[n]/b,a[m]/b,a[o]/b,a[p]/b,a[q]/b]}else{console.log(\"inverted(): oops, determinant == 0\");return a}};this.transform_assign=function(a,b){a[0]=b[0];a[1]=b[1];a[2]=b[2];a[3]=b[3];a[4]=b[4];a[5]=b[5]};this.css_text= function(a){return\"rgba(\"+a[0]+\",\"+a[1]+\",\"+a[2]+\",\"+a[3]+\")\"};this.arcPosition=function(a,b,e,d,g){g=-g/180*Math.PI;return[a+e*Math.cos(g),b+d*Math.sin(g)]};this.pnpoly=function(a,b){var e=false,d=0,g=0,f=a[0];a=a[1];var c,k,j;for(c=0;c<b.length;++c){k=d;j=g;if(b[c][2]===A){j=i.arcPosition(b[c][0],b[c][1],b[c+1][0],b[c+1][1],b[c+2][0]);k=j[0];j=j[1]}else if(b[c][2]===y){j=i.arcPosition(b[c-2][0],b[c-2][1],b[c-1][0],b[c-1][1],b[c][0]+b[c][1]);k=j[0];j=j[1]}else if(b[c][2]!==x){k=b[c][0];j=b[c][1]}if(b[c][2]!== v)if(g>a!==j>a&&f<(k-d)*(a-g)/(j-g)+d)e=!e;d=k;g=j}return e};this.drawRect=function(a,b,e,d){b=i.rect_normalized(b);var g=i.rect_top(b),f=i.rect_bottom(b),c=i.rect_left(b);b=i.rect_right(b);path=[[c,g,v],[b,g,r],[b,f,r],[c,f,r],[c,g,r]];i.drawPath(a,path,e,d,false)};this.drawPath=function(a,b,e,d,g){function f(s){return s[0]}function c(s){return s[1]}function k(s){return s[2]}var j=0,t=[],u=[],w=[];a.beginPath();b.length>0&&k(b[0])!==v&&a.moveTo(0,0);for(j=0;j<b.length;j++){var h=b[j];switch(k(h)){case v:a.moveTo(f(h), c(h));break;case r:a.lineTo(f(h),c(h));break;case B:t.push(f(h),c(h));break;case C:t.push(f(h),c(h));break;case D:t.push(f(h),c(h));a.bezierCurveTo.apply(a,t);t=[];break;case A:u.push(f(h),c(h));break;case x:u.push(f(h));break;case y:u.push(f(h)*Math.PI/180,c(h)*Math.PI/180,c(h)>0);a.arc.apply(a,u);u=[];break;case E:w.push(f(h));break;case F:w.push(f(h),c(h));a.quadraticCurveTo.apply(a,w);w=[];break}}e&&a.fill();d&&a.stroke();g&&a.clip()};this.rect_top=function(a){return a[1]};this.rect_bottom=function(a){return a[1]+ a[3]};this.rect_right=function(a){return a[0]+a[2]};this.rect_left=function(a){return a[0]};this.rect_topleft=function(a){return[a[0],a[1]]};this.rect_topright=function(a){return[a[0]+a[2],a[1]]};this.rect_bottomleft=function(a){return[a[0],a[1]+a[3]]};this.rect_bottomright=function(a){return[a[0]+a[2],a[1]+a[3]]};this.rect_center=function(a){return{x:(2*a[0]+a[2])/2,y:(2*a[1]+a[3])/2}};this.rect_normalized=function(a){var b,e,d;if(a[2]>0){b=a[0];d=a[2]}else{b=a[0]+a[2];d=-a[2]}if(a[3]>0){e=a[1]; a=a[3]}else{e=a[1]+a[3];a=-a[3]}return[b,e,d,a]}}var l=0,m=1,n=2,o=3,p=4,q=5,v=0,r=1,B=2,C=3,D=4,E=5,F=6,A=7,x=8,y=9;return new z}()");
	}
}
