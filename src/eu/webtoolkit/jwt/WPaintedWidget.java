/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;

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
 * <td>HtmlCanvas, InlineSVG</td>
 * <td>HtmlCanvas</td>
 * </tr>
 * <tr>
 * <td>Internet Explorer 6.0+</td>
 * <td>InlineVML</td>
 * <td>InlineVML</td>
 * </tr>
 * <tr>
 * <td>Safari</td>
 * <td>HtmlCanvas, InlineSVG</td>
 * <td>HtmlCanvas</td>
 * </tr>
 * <tr>
 * <td>Opera</td>
 * <td>InlineSVG, HtmlCanvas*</td>
 * <td>InlineSVG</td>
 * </tr>
 * <tr>
 * <td>other</td>
 * <td>?</td>
 * <td>HtmlCanvas</td>
 * </tr>
 * </table>
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
 * InlineSVG requires that the document is rendered as XHTML. This must be
 * enabled in the configuration file using the
 * <code>&lt;send-xhtml-mime-type&gt;</code> option. By default, this option is
 * off.
 * <p>
 * To use a WPaintedWidget, you must derive from it and reimplement
 * {@link WPaintedWidget#paintEvent(WPaintDevice paintDevice) paintEvent()}. To
 * paint on a {@link WPaintDevice}, you will need to use a {@link WPainter}.
 * Repainting is triggered by calling the
 * {@link WPaintedWidget#update(EnumSet flags) update()} method.
 * <p>
 * <p>
 * <i><b>Note: </b>A WPaintedWidget requires that its size is specified in pixel
 * units using {@link WPaintedWidget#resize(WLength width, WLength height)
 * resize()}.</i>
 * </p>
 * 
 * @see WImage
 */
public abstract class WPaintedWidget extends WInteractWidget {
	/**
	 * Enumeration that indicates a rendering method.
	 */
	public enum Method {
		/**
		 * SVG (Most browsers) or VML (Internet Explorer) embedded in the page.
		 */
		InlineSvgVml,
		/**
		 * The WHATWG HTML 5 canvas element.
		 */
		HtmlCanvas;

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
		this.repaintFlags_ = EnumSet.noneOf(PaintFlag.class);
		this.areaImage_ = null;
		this.renderWidth_ = 0;
		this.renderHeight_ = 0;
		this.resized_ = new JSignal2<Integer, Integer>(this, "resized") {
		};
		if (WApplication.getInstance() != null) {
			WEnvironment env = WApplication.getInstance().getEnvironment();
			if (env.getUserAgent().indexOf("Opera") != -1) {
				this.preferredMethod_ = WPaintedWidget.Method.InlineSvgVml;
			}
		}
		this.setInline(false);
		this.resized_.addListener(this,
				new Signal2.Listener<Integer, Integer>() {
					public void trigger(Integer e1, Integer e2) {
						WPaintedWidget.this.onResize(e1, e2);
					}
				});
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
	 * Set the preferred rendering method.
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
	 * Get the preferred rendering method.
	 * <p>
	 * 
	 * @see WPaintedWidget#setPreferredMethod(WPaintedWidget.Method method)
	 */
	public WPaintedWidget.Method getPreferredMethod() {
		return this.preferredMethod_;
	}

	/**
	 * Let the widget repaint itself.
	 * <p>
	 * Repainting is not immediate, but happens after when the event loop is
	 * exited.
	 */
	public void update(EnumSet<PaintFlag> flags) {
		this.needRepaint_ = true;
		this.repaintFlags_.addAll(flags);
		super.repaint();
	}

	/**
	 * Let the widget repaint itself.
	 * <p>
	 * Calls {@link #update(EnumSet flags) update(EnumSet.of(flag, flags))}
	 */
	public final void update(PaintFlag flag, PaintFlag... flags) {
		update(EnumSet.of(flag, flags));
	}

	/**
	 * Let the widget repaint itself.
	 * <p>
	 * Calls {@link #update(EnumSet flags)
	 * update(EnumSet.noneOf(PaintFlag.class))}
	 */
	public final void update() {
		update(EnumSet.noneOf(PaintFlag.class));
	}

	public void resize(WLength width, WLength height) {
		super.resize(width, height);
		if (!width.isAuto() && !height.isAuto()) {
			this.resizeCanvas((int) this.getWidth().toPixels(), (int) this
					.getHeight().toPixels());
		}
	}

	/**
	 * Add an interactive area.
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
	 * Insert an interactive area.
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
	 * Paint the widget.
	 * <p>
	 * You should reimplement this method to paint the contents of the widget,
	 * using the given paintDevice.
	 */
	protected abstract void paintEvent(WPaintDevice paintDevice);

	DomElementType getDomElementType() {
		return DomElementType.DomElement_DIV;
	}

	void updateDom(DomElement element, boolean all) {
		if (all && this.areaImage_ != null) {
			element.addChild(((WWebWidget) this.areaImage_)
					.createDomElement(WApplication.getInstance()));
		}
		super.updateDom(element, all);
	}

	DomElement createDomElement(WApplication app) {
		this.isCreatePainter();
		DomElement result = DomElement.createNew(DomElementType.DomElement_DIV);
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
		WPaintDevice device = this.painter_.getCreatePaintDevice();
		if (this.renderWidth_ != 0 && this.renderHeight_ != 0) {
			this.paintEvent(device);
			if (device.getPainter() != null) {
				device.getPainter().end();
			}
		}
		this.painter_.createContents(canvas, device);
		;
		this.needRepaint_ = false;
		wrap.addChild(canvas);
		if (wrap != result) {
			result.addChild(wrap);
		}
		this.updateDom(result, true);
		WApplication
				.getInstance()
				.doJavaScript(
						this.getJsRef()
								+ ".wtResize = function(self, w, h) {if (!self.wtWidth || self.wtWidth!=w || !self.wtHeight || self.wtHeight!=h) {self.wtWidth=w; self.wtHeight=h;"
								+ this.resized_.createCall("w", "h") + "}};");
		return result;
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		DomElement e = DomElement.getForUpdate(this,
				DomElementType.DomElement_DIV);
		this.updateDom(e, false);
		result.add(e);
		boolean createNew = this.isCreatePainter();
		if (this.needRepaint_) {
			WPaintDevice device = this.painter_.getCreatePaintDevice();
			if (!createNew) {
				device.setPaintFlags(EnumUtils.mask(this.repaintFlags_,
						PaintFlag.PaintUpdate));
			}
			this.paintEvent(device);
			if (device.getPainter() != null) {
				device.getPainter().end();
			}
			if (createNew) {
				DomElement canvas = DomElement.getForUpdate('p' + this.getId(),
						DomElementType.DomElement_DIV);
				canvas.removeAllChildren();
				this.painter_.createContents(canvas, device);
				result.add(canvas);
			} else {
				this.painter_.updateContents(result, device);
			}
			;
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
	private EnumSet<PaintFlag> repaintFlags_;
	private WImage areaImage_;
	int renderWidth_;
	int renderHeight_;
	private JSignal2<Integer, Integer> resized_;

	private void resizeCanvas(int width, int height) {
		this.renderWidth_ = width;
		this.renderHeight_ = height;
		if (this.areaImage_ != null) {
			this.areaImage_.resize(new WLength(this.renderWidth_), new WLength(
					this.renderHeight_));
		}
		this.sizeChanged_ = true;
		this.update();
	}

	private void onResize(int width, int height) {
		this.resize(WLength.Auto, WLength.Auto);
		this.resizeCanvas(width, height);
	}

	private boolean isCreatePainter() {
		if (this.painter_ != null) {
			return false;
		}
		WEnvironment env = WApplication.getInstance().getEnvironment();
		if (env.agentIsIE()) {
			this.painter_ = new WWidgetVectorPainter(this,
					VectorFormat.VmlFormat);
			return true;
		}
		WPaintedWidget.Method method;
		if (env.getContentType() != WEnvironment.ContentType.XHTML1) {
			method = WPaintedWidget.Method.HtmlCanvas;
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
		if (method == WPaintedWidget.Method.InlineSvgVml) {
			this.painter_ = new WWidgetVectorPainter(this,
					VectorFormat.SvgFormat);
		} else {
			this.painter_ = new WWidgetCanvasPainter(this);
		}
		return true;
	}

	private void createAreaImage() {
		if (!(this.areaImage_ != null)) {
			this.areaImage_ = new WImage(WApplication.getInstance()
					.getOnePixelGifUrl());
			this.areaImage_.setParent(this);
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
		}
	}
}
