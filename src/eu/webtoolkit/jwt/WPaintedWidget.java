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
				"function(P,t){jQuery.data(t,\"obj\",this);var u=this;this.imagePreloaders=[];this.images=[];this.canvas=document.getElementById(\"c\"+t.id);this.repaint=function(){};this.widget=t;this.cancelPreloaders=function(){for(var v=0;v<u.imagePreloaders.length;++v)u.imagePreloaders[v].cancel();u.imagePreloaders=[]}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptObject,
				"gfxUtils",
				"function(){function P(){var h=this;this.path_crisp=function(a){return a.map(function(b){return[Math.floor(b[0])+0.5,Math.floor(b[1])+0.5,b[2]]})};this.transform_mult=function(a,b){if(b.length===2){var d=b[0],f=b[1];return[a[t]*d+a[v]*f+a[A],a[u]*d+a[x]*f+a[B]]}if(b.length===3){if(b[2]===G||b[2]===H)return b.slice(0);d=b[0];f=b[1];return[a[t]*d+a[v]*f+a[A],a[u]*d+a[x]*f+a[B],b[2]]}if(b.length===4){var g,e,c,l;e=h.transform_mult(a,[b[0],b[1]]);d=e[0]; g=e[0];f=e[1];e=e[1];for(c=0;c<3;++c){l=h.transform_mult(a,c==0?[h.rect_left(b),h.rect_bottom(b)]:c==1?[h.rect_right(b),h.rect_top(b)]:[h.rect_right(b),h.rect_bottom(b)]);d=Math.min(d,l[0]);g=Math.max(g,l[0]);f=Math.min(f,l[1]);e=Math.max(e,l[1])}return[d,f,g-d,e-f]}if(b.length===6)return[a[t]*b[t]+a[v]*b[u],a[u]*b[t]+a[x]*b[u],a[t]*b[v]+a[v]*b[x],a[u]*b[v]+a[x]*b[x],a[t]*b[A]+a[v]*b[B]+a[A],a[u]*b[A]+a[x]*b[B]+a[B]];return[]};this.transform_apply=function(a,b){var d=h.transform_mult;return b.map(function(f){return d(a, f)})};this.transform_det=function(a){return a[t]*a[x]-a[u]*a[v]};this.transform_adjoint=function(a){var b=a[t],d=a[v],f=a[u],g=a[x],e=a[A];a=a[B];return[g,-d,-f,b,a*f-e*g,-(a*b-e*d)]};this.transform_inverted=function(a){var b=h.transform_det(a);if(b!=0){a=h.transform_adjoint(a);return[a[t]/b,a[v]/b,a[u]/b,a[x]/b,a[A]/b,a[B]/b]}else{console.log(\"inverted(): oops, determinant == 0\");return a}};this.transform_assign=function(a,b){a[0]=b[0];a[1]=b[1];a[2]=b[2];a[3]=b[3];a[4]=b[4];a[5]=b[5]};this.transform_equal= function(a,b){return a[0]==b[0]&&a[1]==b[1]&&a[2]==b[2]&&a[3]==b[3]&&a[4]==b[4]&&a[5]==b[5]};this.css_text=function(a){return\"rgba(\"+a[0]+\",\"+a[1]+\",\"+a[2]+\",\"+a[3]+\")\"};this.arcPosition=function(a,b,d,f,g){g=-g/180*Math.PI;return[a+d*Math.cos(g),b+f*Math.sin(g)]};this.pnpoly=function(a,b){var d=false,f=0,g=0,e=a[0];a=a[1];var c,l,k;for(c=0;c<b.length;++c){l=f;k=g;if(b[c][2]===Q){k=h.arcPosition(b[c][0],b[c][1],b[c+1][0],b[c+1][1],b[c+2][0]);l=k[0];k=k[1]}else if(b[c][2]===H){k=h.arcPosition(b[c- 2][0],b[c-2][1],b[c-1][0],b[c-1][1],b[c][0]+b[c][1]);l=k[0];k=k[1]}else if(b[c][2]!==G){l=b[c][0];k=b[c][1]}if(b[c][2]!==D)if(g>a!==k>a&&e<(l-f)*(a-g)/(k-g)+f)d=!d;f=l;g=k}return d};this.rect_intersection=function(a,b){a=h.rect_normalized(a);b=h.rect_normalized(b);var d=h.rect_top,f=h.rect_bottom,g=h.rect_left,e=h.rect_right;g=Math.max(g(a),g(b));e=Math.min(e(a),e(b));d=Math.max(d(a),d(b));a=Math.min(f(a),f(b));return[g,d,e-g,a-d]};this.drawRect=function(a,b,d,f){b=h.rect_normalized(b);var g=h.rect_top(b), e=h.rect_bottom(b),c=h.rect_left(b);b=h.rect_right(b);path=[[c,g,D],[b,g,C],[b,e,C],[c,e,C],[c,g,C]];h.drawPath(a,path,d,f,false)};this.drawPath=function(a,b,d,f,g){function e(j){return j[0]}function c(j){return j[1]}function l(j){return j[2]}var k=0,q=[],m=[],r=[];a.beginPath();b.length>0&&l(b[0])!==D&&a.moveTo(0,0);for(k=0;k<b.length;k++){var i=b[k];switch(l(i)){case D:a.moveTo(e(i),c(i));break;case C:(function(){var j=k===0?[0,0]:b[k-1];if(!d&&!g&&f&&(e(i)-e(j)>16777216||c(i)-c(j)>16777216)){var p= a.wtTransform?a.wtTransform:[1,0,0,1,0,0],s=h.transform_mult(p,j),o=h.transform_mult(p,i);p=e(o)-e(s);o=c(o)-c(s);var F=a.canvas.width+50,z=a.canvas.height+50,E=[],n;if(p!==0){n=(-50-e(s))/p;E.push(n);n=(F-e(s))/p;E.push(n)}if(o!==0){n=(-50-c(s))/o;E.push(n);n=(z-c(s))/o;E.push(n)}var w=[],y=0;for(y=0;y<E.length;++y){n=E[y];if(n<0)n=0;if(n>1)n=1;var S=e(s)+n*p,T=c(s)+n*o;S>=-50&&S<=F&&T>=-50&&T<=z&&w.push([n,e(j)+n*(e(i)-e(j)),c(j)+n*(c(i)-c(j))])}w.sort(function(X,Y){return X[0]-Y[0]});for(y=1;y< w.length;)if(w[y][0]===w[y-1][0])w.splice(y,1);else++y;if(w.length===2){a.moveTo(w[0][1],w[0][2]);a.lineTo(w[1][1],w[1][2]);a.moveTo(e(i),c(i))}}else a.lineTo(e(i),c(i))})();break;case Z:q.push(e(i),c(i));break;case $:q.push(e(i),c(i));break;case I:q.push(e(i),c(i));a.bezierCurveTo.apply(a,q);q=[];break;case Q:m.push(e(i),c(i));break;case G:m.push(e(i));break;case H:m.push(e(i)*Math.PI/180,c(i)*Math.PI/180,c(i)>0);a.arc.apply(a,m);m=[];break;case aa:r.push(e(i));break;case J:r.push(e(i),c(i));a.quadraticCurveTo.apply(a, r);r=[];break}}d&&a.fill();f&&a.stroke();g&&a.clip()};this.drawStencilAlongPath=function(a,b,d,f,g,e){function c(r){return r[0]}function l(r){return r[1]}function k(r){return r[2]}var q=0;for(q=0;q<d.length;q++){var m=d[q];if(!(e&&a.wtClipPath&&!h.pnpoly(m,h.transform_apply(a.wtClipPathTransform,a.wtClipPath))))if(k(m)==D||k(m)==C||k(m)==J||k(m)==I){m=h.transform_apply([1,0,0,1,c(m),l(m)],b);h.drawPath(a,m,f,g,false)}}};this.drawText=function(a,b,d,f,g){if(!(g&&a.wtClipPath&&!h.pnpoly(g,h.transform_apply(a.wtClipPathTransform, a.wtClipPath)))){var e=d&K;var c=g=null;switch(d&ba){case U:a.textAlign=\"left\";g=h.rect_left(b);break;case V:a.textAlign=\"right\";g=h.rect_right(b);break;case W:a.textAlign=\"center\";g=h.rect_center(b).x;break}switch(e){case M:a.textBaseline=\"top\";c=h.rect_top(b);break;case N:a.textBaseline=\"bottom\";c=h.rect_bottom(b);break;case O:a.textBaseline=\"middle\";c=h.rect_center(b).y;break}if(!(g==null||c==null)){b=a.fillStyle;a.fillStyle=a.strokeStyle;a.fillText(f,g,c);a.fillStyle=b}}};this.calcYOffset=function(a, b,d,f){return f===O?-((b-1)*d/2)+a*d:f===M?a*d:f===N?-(b-1-a)*d:0};this.drawTextOnPath=function(a,b,d,f,g,e,c,l,k){function q(z){return z[0]}function m(z){return z[1]}function r(z){return z[2]}var i=0,j=0;f=h.transform_apply(f,g);for(i=0;i<g.length;i++){if(i>=b.length)break;j=g[i];var p=f[i],s=b[i].split(\"\\n\");if(r(j)==D||r(j)==C||r(j)==J||r(j)==I)if(e==0)for(j=0;j<s.length;j++){var o=h.calcYOffset(j,s.length,c,l&K);h.drawText(a,[d[0]+q(p),d[1]+m(p)+o,d[2],d[3]],l,s[j],k?[q(p),m(p)]:null)}else{o= e*Math.PI/180;j=Math.cos(-o);o=-Math.sin(-o);var F=-o;a.save();a.transform(j,F,o,j,q(p),m(p));for(j=0;j<s.length;j++){o=h.calcYOffset(j,s.length,c,l&K);h.drawText(a,[d[0],d[1]+o,d[2],d[3]],l,s[j],k?[q(p),m(p)]:null)}a.restore()}}};this.setClipPath=function(a,b,d,f){if(f){a.setTransform.apply(a,d);h.drawPath(a,b,false,false,true);a.setTransform(1,0,0,1,0,0)}a.wtClipPath=b;a.wtClipPathTransform=d};this.removeClipPath=function(a){delete a.wtClipPath;delete a.wtClipPathTransform};this.rect_top=function(a){return a[1]}; this.rect_bottom=function(a){return a[1]+a[3]};this.rect_right=function(a){return a[0]+a[2]};this.rect_left=function(a){return a[0]};this.rect_topleft=function(a){return[a[0],a[1]]};this.rect_topright=function(a){return[a[0]+a[2],a[1]]};this.rect_bottomleft=function(a){return[a[0],a[1]+a[3]]};this.rect_bottomright=function(a){return[a[0]+a[2],a[1]+a[3]]};this.rect_center=function(a){return{x:(2*a[0]+a[2])/2,y:(2*a[1]+a[3])/2}};this.rect_normalized=function(a){var b,d,f;if(a[2]>0){b=a[0];f=a[2]}else{b= a[0]+a[2];f=-a[2]}if(a[3]>0){d=a[1];a=a[3]}else{d=a[1]+a[3];a=-a[3]}return[b,d,f,a]}}var t=0,u=1,v=2,x=3,A=4,B=5,D=0,C=1,Z=2,$=3,I=4,aa=5,J=6,Q=7,G=8,H=9,U=1,V=2,W=4,M=128,O=512,N=1024,K=112|M|256|O|N|2048,ba=U|V|W|8;return new P}()");
	}

	static WJavaScriptPreamble wtjs20() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WJavaScriptObjectStorage",
				"function(k,g){function d(b){if(jQuery.isArray(b)){var c=[],a;for(a=0;a<b.length;++a)c.push(d(b[a]));return c}else if(jQuery.isPlainObject(b)){c={};for(a in b)if(b.hasOwnProperty(a))c[a]=d(b[a]);return c}else return b}function e(b,c){if(b===c)return true;if(jQuery.isArray(b)&&jQuery.isArray(c)){if(b.length!==c.length)return false;var a;for(a=0;a<b.length;++a)if(!e(b[a],c[a]))return false;return true}else if(jQuery.isPlainObject(b)&& jQuery.isPlainObject(c)){for(a in b)if(b.hasOwnProperty(a)){if(!c.hasOwnProperty(a))return false;if(!e(b[a],c[a]))return false}for(a in c)if(c.hasOwnProperty(a))if(!b.hasOwnProperty(a))return false;return true}else return false}function h(b){return jQuery.isArray(b)&&b.length>6}function j(){var b={},c,a;for(a=0;a<f.jsValues.length;++a){c=f.jsValues[a];if(!h(c)&&!e(c,i[a]))b[a]=c}return JSON.stringify(b)}jQuery.data(g,\"jsobj\",this);var f=this,i={};this.jsValues=[];this.setJsValue=function(b,c){h(c)|| (i[b]=d(c));f.jsValues[b]=c};g.wtEncodeValue=j}");
	}
}
