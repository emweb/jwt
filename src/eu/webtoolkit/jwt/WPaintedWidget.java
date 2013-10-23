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
 * 
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
		if (!(env.agentIsChrome()
				&& env.getAgent().getValue() >= WEnvironment.UserAgent.Chrome5
						.getValue()
				|| env.agentIsIE()
				&& env.getAgent().getValue() >= WEnvironment.UserAgent.IE9
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
					method = this.preferredMethod_;
				}
				boolean nokia810 = env.getUserAgent().indexOf("Linux arm") != -1
						&& env.getUserAgent().indexOf("Tablet browser") != -1
						&& env.getUserAgent().indexOf("Gecko") != -1;
				if (nokia810) {
					method = WPaintedWidget.Method.HtmlCanvas;
				} else {
					method = this.preferredMethod_;
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

	DomElement createDomElement(WApplication app) {
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

	void getDomChanges(final List<DomElement> result, WApplication app) {
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

	private WPaintedWidget.Method preferredMethod_;
	private WWidgetPainter painter_;
	private boolean needRepaint_;
	boolean sizeChanged_;
	private boolean areaImageAdded_;
	EnumSet<PaintFlag> repaintFlags_;
	private WImage areaImage_;
	int renderWidth_;
	int renderHeight_;

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
		if (this.preferredMethod_ == WPaintedWidget.Method.PngImage) {
			this.painter_ = new WWidgetRasterPainter(this);
			return true;
		}
		final WEnvironment env = WApplication.getInstance().getEnvironment();
		if (env.agentIsIElt(9)) {
			this.painter_ = new WWidgetVectorPainter(this,
					WWidgetPainter.RenderType.InlineVml);
			return true;
		}
		WPaintedWidget.Method method = this.getMethod();
		if (method == WPaintedWidget.Method.InlineSvgVml) {
			this.painter_ = new WWidgetVectorPainter(this,
					WWidgetPainter.RenderType.InlineSvg);
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
			this.areaImage_.setPopup(true);
			this.areaImageAdded_ = true;
		}
	}
}
