/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget that is painted using vector graphics.
 *
 * <p>A painted widget is rendered from basic drawing primitives. Rendering is done not on the
 * server but on the browser, using different rendering methods:
 *
 * <p>
 *
 * <table border="1" cellspacing="3" cellpadding="3">
 * <tr><td><b>Browser</b>
 * </td><td><b>Methods</b>
 * </td><td><b>Default method</b>
 * </td></tr>
 * <tr><td>Firefox 1.5+
 * </td><td>HtmlCanvas, InlineSVG, PngImage
 * </td><td>HtmlCanvas
 * </td></tr>
 * <tr><td>Internet Explorer 6.0+
 * </td><td>InlineVML, PngImage
 * </td><td>InlineVML
 * </td></tr>
 * <tr><td>Internet Explorer 9+
 * </td><td>HtmlCanvas, InlineSVG, PngImage
 * </td><td>HtmlCanvas
 * </td></tr>
 * <tr><td>Safari
 * </td><td>HtmlCanvas, InlineSVG, PngImage
 * </td><td>HtmlCanvas
 * </td></tr>
 * <tr><td>Opera
 * </td><td>InlineSVG, HtmlCanvas*, PngImage
 * </td><td>InlineSVG
 * </td></tr>
 * <tr><td>other
 * </td><td>?
 * </td><td>HtmlCanvas, PngImage
 * </td></tr>
 * </table>
 *
 * <p><i>* HtmlCanvas occasionally suffers from rendering artefacts in Opera.</i>
 *
 * <p>The different rendering methods correspond to different {@link WPaintDevice} implementations,
 * from which this widget choses a suitable one depending on the browser capabilities and
 * configuration.
 *
 * <p>If no JavaScript is available, the JavaScript-based HtmlCanvas will not be used, and InlineSVG
 * will be used instead. The method used may be changed by using {@link
 * WPaintedWidget#setPreferredMethod(RenderMethod method) setPreferredMethod()}.
 *
 * <p>In some browsers, InlineSVG requires that the document is rendered as XHTML. This must be
 * enabled in the configuration file using the <code>&lt;send-xhtml-mime-type&gt;</code> option. By
 * default, this option is off. Firefox 4 and Chrome do support svg in normal html mode.
 *
 * <p>The PngImage is the most portable rendering method, and may be the fastest if the painting is
 * of high complexity and/or the image is fairly small.
 *
 * <p>To use a WPaintedWidget, you must derive from it and reimplement {@link
 * WPaintedWidget#paintEvent(WPaintDevice paintDevice) paintEvent()}. To paint on a {@link
 * WPaintDevice}, you will need to use a {@link WPainter}. Repainting is triggered by calling the
 * {@link WPaintedWidget#update(EnumSet flags) update()} method.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>Styling through CSS is not applicable.
 *
 * <p>
 *
 * <p><i><b>Note: </b>A WPaintedWidget requires that it is given a size using {@link
 * WPaintedWidget#resize(WLength width, WLength height) resize()} or by a layout manager. </i>
 *
 * <h3>Client side interaction and repainting</h3>
 *
 * <p>If the widget is drawn as an HTML canvas element, i.e. the {@link WPaintedWidget#getMethod()
 * method} is HtmlCanvas, a WPaintedWidget can expose certain objects to be modified client side.
 *
 * <p>
 *
 * @see WJavaScriptHandle
 * @see WJavaScriptExposableObject
 * @see WImage
 */
public abstract class WPaintedWidget extends WInteractWidget {
  private static Logger logger = LoggerFactory.getLogger(WPaintedWidget.class);

  /** Create a new painted widget. */
  public WPaintedWidget(WContainerWidget parentContainer) {
    super();
    this.preferredMethod_ = RenderMethod.HtmlCanvas;
    this.painter_ = null;
    this.needRepaint_ = false;
    this.sizeChanged_ = false;
    this.areaImageAdded_ = false;
    this.repaintFlags_ = EnumSet.noneOf(PaintFlag.class);
    this.areaImage_ = null;
    this.renderWidth_ = 0;
    this.renderHeight_ = 0;
    this.repaintSlot_ =
        new JSlot("function() {var o=" + this.getObjJsRef() + ";if(o){o.repaint();}}", this);
    this.jsObjects_ = new WJavaScriptObjectStorage(this);
    this.jsDefined_ = false;
    if (WApplication.getInstance() != null) {
      final WEnvironment env = WApplication.getInstance().getEnvironment();
      if (env.agentIsOpera() && env.getUserAgent().indexOf("Mac OS X") == -1) {
        this.preferredMethod_ = RenderMethod.InlineSvgVml;
      }
    }
    this.setInline(false);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Create a new painted widget.
   *
   * <p>Calls {@link #WPaintedWidget(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WPaintedWidget() {
    this((WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {
    {
      WWidget oldWidget = this.areaImage_;
      this.areaImage_ = null;
      {
        WWidget toRemove = this.manageWidget(oldWidget, this.areaImage_);
        if (toRemove != null) toRemove.remove();
      }
    }
    super.remove();
  }
  /**
   * Sets the preferred rendering method.
   *
   * <p>When <code>method</code> is supported by the browser, then it is chosen for rendering.
   */
  public void setPreferredMethod(RenderMethod method) {
    if (this.preferredMethod_ != method) {
      this.painter_ = null;
      this.preferredMethod_ = method;
    }
  }
  /** Returns the preferred rendering method. */
  public RenderMethod getPreferredMethod() {
    return this.preferredMethod_;
  }
  /**
   * Lets the widget repaint itself.
   *
   * <p>Repainting is not immediate, but happens after when the event loop is exited.
   *
   * <p>Unless a {@link PaintFlag#Update} paint flag is set, the widget is first cleared.
   */
  public void update(EnumSet<PaintFlag> flags) {
    this.needRepaint_ = true;
    this.repaintFlags_.addAll(flags);
    this.repaint();
  }
  /**
   * Lets the widget repaint itself.
   *
   * <p>Calls {@link #update(EnumSet flags) update(EnumSet.of(flag, flags))}
   */
  public final void update(PaintFlag flag, PaintFlag... flags) {
    update(EnumSet.of(flag, flags));
  }
  /**
   * Lets the widget repaint itself.
   *
   * <p>Calls {@link #update(EnumSet flags) update(EnumSet.noneOf(PaintFlag.class))}
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
   *
   * <p>Adds the <code>area</code> which listens to events in a specific region of the widget. Areas
   * are organized in a list, to which the given <code>area</code> is appended. When areas overlap,
   * the area with the lowest index receives the event.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When defining at least one area, no more events will propagate to the widget
   * itself. As a work-around, you can emulate this by listening for events on a {@link WRectArea}
   * that corresponds to the whole widget, and which is added as the last area (catching all events
   * that were not caught by preceding areas). </i>
   */
  public void addArea(WAbstractArea area) {
    this.createAreaImage();
    this.areaImage_.addArea(area);
  }
  /**
   * Inserts an interactive area.
   *
   * <p>Inserts the <code>area</code> which listens to events in the coresponding area of the
   * widget. Areas are organized in a list, and the <i>area</i> is inserted at index <code>index
   * </code>. When areas overlap, the area with the lowest index receives the event.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When defining at least one area, no more events will propagate to the widget
   * itself. As a work-around, you can emulate this by listening for events on a {@link WRectArea}
   * that corresponds to the whole widget, and which is added as the last area (catching all events
   * that were not caught by preceding areas). </i>
   */
  public void insertArea(int index, WAbstractArea area) {
    this.createAreaImage();
    this.areaImage_.insertArea(index, area);
  }
  /**
   * Removes an interactive area.
   *
   * <p>Removes the <code>area</code> from this widget.
   *
   * <p>
   */
  public WAbstractArea removeArea(WAbstractArea area) {
    this.createAreaImage();
    return this.areaImage_.removeArea(area);
  }
  /**
   * Returns the interactive area at the given index.
   *
   * <p>Returns <code>null</code> if <code>index</code> was invalid.
   *
   * <p>
   */
  public WAbstractArea getArea(int index) {
    return this.areaImage_ != null ? this.areaImage_.getArea(index) : null;
  }
  /**
   * Returns the interactive areas set for this widget.
   *
   * <p>
   *
   * @see WPaintedWidget#addArea(WAbstractArea area)
   */
  public List<WAbstractArea> getAreas() {
    return this.areaImage_ != null
        ? this.areaImage_.getAreas()
        : (List<WAbstractArea>) new ArrayList<WAbstractArea>();
  }
  /**
   * A JavaScript slot that repaints the widget when triggered.
   *
   * <p>This is useful for client-side initiated repaints. You may want to use this if you want to
   * add interaction or animation to your {@link WPaintedWidget}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This feature is currently only supported if the {@link
   * WPaintedWidget#getMethod() method} is HtmlCanvas. This will not cause a server roundtrip.
   * Instead, the resulting JavaScript of {@link WPaintedWidget#paintEvent(WPaintDevice paintDevice)
   * paintEvent()} will be re-executed on the client side. </i>
   *
   * @see WPaintedWidget#getObjJsRef()
   */
  public JSlot getRepaintSlot() {
    return this.repaintSlot_;
  }
  /**
   * Create a {@link WTransform} that is accessible from JavaScript, associated with this
   * WPaintedWidget.
   */
  protected WJavaScriptHandle<WTransform> createJSTransform() {
    return this.jsObjects_.addObject(new WTransform());
  }
  /**
   * Create a {@link WBrush} that is accessible from JavaScript, associated with this
   * WPaintedWidget.
   */
  protected WJavaScriptHandle<WBrush> createJSBrush() {
    return this.jsObjects_.addObject(new WBrush());
  }
  /**
   * Create a {@link WPen} that is accessible from JavaScript, associated with this WPaintedWidget.
   */
  protected WJavaScriptHandle<WPen> createJSPen() {
    return this.jsObjects_.addObject(new WPen());
  }
  /**
   * Create a {@link WPainterPath} that is accessible from JavaScript, associated with this
   * WPaintedWidget.
   */
  protected WJavaScriptHandle<WPainterPath> createJSPainterPath() {
    return this.jsObjects_.addObject(new WPainterPath());
  }
  /**
   * Create a {@link WRectF} that is accessible from JavaScript, associated with this
   * WPaintedWidget.
   */
  protected WJavaScriptHandle<WRectF> createJSRect() {
    return this.jsObjects_.addObject(new WRectF(0, 0, 0, 0));
  }
  /**
   * Create a {@link WPointF} that is accessible from JavaScript, associated with this
   * WPaintedWidget.
   */
  protected WJavaScriptHandle<WPointF> createJSPoint() {
    return this.jsObjects_.addObject(new WPointF(0, 0));
  }

  protected void layoutSizeChanged(int width, int height) {
    this.resizeCanvas(width, height);
  }
  /**
   * Returns the actual method used for rendering.
   *
   * <p>The default method considers browser capabilites and the preferred method to make an actual
   * choice for the implementation.
   *
   * <p>You may want to reimplement this method to override this choice.
   */
  protected RenderMethod getMethod() {
    final WEnvironment env = WApplication.getInstance().getEnvironment();
    RenderMethod method;
    if (this.preferredMethod_ == RenderMethod.PngImage) {
      return RenderMethod.PngImage;
    }
    if (env.agentIsIElt(9)) {
      method =
          this.preferredMethod_ == RenderMethod.InlineSvgVml
              ? RenderMethod.InlineSvgVml
              : RenderMethod.PngImage;
    } else {
      if (!(env.agentIsChrome()
              && (int) env.getAgent().getValue() >= (int) UserAgent.Chrome5.getValue()
          || env.agentIsGecko()
              && (int) env.getAgent().getValue() >= (int) UserAgent.Firefox4_0.getValue())) {
        method = env.hasJavaScript() ? RenderMethod.HtmlCanvas : RenderMethod.PngImage;
      } else {
        if (!env.hasJavaScript()) {
          method = RenderMethod.InlineSvgVml;
        } else {
          boolean oldFirefoxMac =
              (env.getUserAgent().indexOf("Firefox/1.5") != -1
                      || env.getUserAgent().indexOf("Firefox/2.0") != -1)
                  && env.getUserAgent().indexOf("Macintosh") != -1;
          if (oldFirefoxMac) {
            method = RenderMethod.HtmlCanvas;
          } else {
            method =
                this.preferredMethod_ == RenderMethod.PngImage
                    ? RenderMethod.HtmlCanvas
                    : this.preferredMethod_;
          }
          boolean nokia810 =
              env.getUserAgent().indexOf("Linux arm") != -1
                  && env.getUserAgent().indexOf("Tablet browser") != -1
                  && env.getUserAgent().indexOf("Gecko") != -1;
          if (nokia810) {
            method = RenderMethod.HtmlCanvas;
          } else {
            method =
                this.preferredMethod_ == RenderMethod.PngImage
                    ? RenderMethod.HtmlCanvas
                    : this.preferredMethod_;
          }
        }
      }
    }
    return method;
  }
  /**
   * Paints the widget.
   *
   * <p>You should reimplement this method to paint the contents of the widget, using the given
   * paintDevice.
   */
  protected abstract void paintEvent(WPaintDevice paintDevice);
  /**
   * Creates a paint device.
   *
   * <p>Although it&apos;s usually not necessary to call this function, you may want to reimplement
   * this function to customize or specialize the device used for painting the widget.
   */
  protected WPaintDevice getCreatePaintDevice() {
    (this).createPainter();
    if (this.painter_ != null) {
      return this.painter_.createPaintDevice(true);
    } else {
      return null;
    }
  }

  DomElementType getDomElementType() {
    if (this.isInline() && WApplication.getInstance().getEnvironment().agentIsIElt(9)) {
      return DomElementType.SPAN;
    } else {
      return DomElementType.DIV;
    }
  }

  void updateDom(final DomElement element, boolean all) {
    if (all && this.areaImage_ != null || this.areaImageAdded_) {
      element.addChild(this.areaImage_.createSDomElement(WApplication.getInstance()));
      this.areaImageAdded_ = false;
    }
    super.updateDom(element, all);
  }

  protected DomElement createDomElement(WApplication app) {
    if (this.isInLayout()) {
      this.setLayoutSizeAware(true);
      this.setJavaScriptMember(
          WT_RESIZE_JS,
          "function(self, w, h) {let u = self.querySelector('canvas, img');if (u === null) return;if (w >= 0) u.style.width = `${w}px`;else u.style.width = 'auto';if (h >= 0) u.style.height = `${h}px`;else u.style.height = 'auto';}");
    }
    this.createPainter();
    DomElement result = DomElement.createNew(this.getDomElementType());
    this.setId(result, app);
    DomElement wrap = result;
    if (this.getWidth().isAuto() && this.getHeight().isAuto()) {
      result.setProperty(Property.StylePosition, "relative");
      wrap = DomElement.createNew(DomElementType.DIV);
      wrap.setProperty(Property.StylePosition, "absolute");
      wrap.setProperty(Property.StyleLeft, "0");
      wrap.setProperty(Property.StyleRight, "0");
    }
    DomElement canvas = DomElement.createNew(DomElementType.DIV);
    if (!app.getEnvironment().agentIsSpiderBot()) {
      canvas.setId('p' + this.getId());
    }
    WPaintDevice device = this.painter_.getPaintDevice(false);
    if (this.painter_.getRenderType() == WWidgetPainter.RenderType.InlineVml && this.isInline()) {
      result.setProperty(Property.Style, "zoom: 1;");
      canvas.setProperty(Property.StyleDisplay, "inline");
      canvas.setProperty(Property.Style, "zoom: 1;");
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
    DomElement e = DomElement.getForUpdate(this, DomElementType.DIV);
    this.updateDom(e, false);
    result.add(e);
    boolean createdNew = this.createPainter();
    if (this.needRepaint_) {
      WPaintDevice device =
          this.painter_.getPaintDevice(
              this.repaintFlags_.contains(PaintFlag.Update) && !createdNew);
      if (this.renderWidth_ != 0 && this.renderHeight_ != 0) {
        this.paintEvent(device);
        if (device.getPainter() != null) {
          device.getPainter().end();
        }
      }
      if (createdNew) {
        DomElement canvas = DomElement.getForUpdate('p' + this.getId(), DomElementType.DIV);
        canvas.removeAllChildren();
        this.painter_.createContents(canvas, device);
        result.add(canvas);
      } else {
        this.painter_.updateContents(result, device);
      }
      this.needRepaint_ = false;
      this.repaintFlags_ = EnumSet.noneOf(PaintFlag.class);
    }
  }

  void propagateRenderOk(boolean deep) {
    this.needRepaint_ = false;
    super.propagateRenderOk(deep);
  }

  protected void enableAjax() {
    if (ObjectUtils.cast(this.painter_, WWidgetCanvasPainter.class) != null
        && this.renderWidth_ != 0
        && this.renderHeight_ != 0) {
      this.update();
    }
    super.enableAjax();
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (flags.contains(RenderFlag.Full) || !this.jsDefined_) {
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
   * Returns a JavaScript reference to the client side representation of the {@link WPaintedWidget}.
   *
   * <p>The client side representation exposes the following interface:
   *
   * <pre>{@code
   * {
   * canvas: exposes the underlying HTML canvas element
   * repaint: a function that, when called, will repaint the widget without a server roundtrip
   * }
   *
   * }</pre>
   *
   * <p>
   *
   * <p><i><b>Note: </b>The {@link WPaintedWidget#getMethod() method} should be HtmlCanvas and there
   * has to be at least one {@link WJavaScriptHandle} associated with this {@link WPaintedWidget} in
   * order for this reference to be valid. </i>
   */
  protected String getObjJsRef() {
    return this.getJsRef() + ".wtObj";
  }

  private void defineJavaScript() {
    WApplication app = WApplication.getInstance();
    if (this.getMethod() == RenderMethod.HtmlCanvas) {
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

  private RenderMethod preferredMethod_;
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
      this.areaImage_.resize(new WLength(this.renderWidth_), new WLength(this.renderHeight_));
    }
    this.sizeChanged_ = true;
    this.update();
  }

  private boolean createPainter() {
    if (this.painter_ != null) {
      return false;
    }
    final WEnvironment env = WApplication.getInstance().getEnvironment();
    RenderMethod method = this.getMethod();
    if (method == RenderMethod.InlineSvgVml) {
      if (env.agentIsIElt(9)) {
        this.painter_ = new WWidgetVectorPainter(this, WWidgetPainter.RenderType.InlineVml);
      } else {
        this.painter_ = new WWidgetVectorPainter(this, WWidgetPainter.RenderType.InlineSvg);
      }
    } else {
      if (method == RenderMethod.PngImage) {
        this.painter_ = new WWidgetRasterPainter(this);
      } else {
        this.painter_ = new WWidgetCanvasPainter(this);
      }
    }
    return true;
  }

  private void createAreaImage() {
    if (!(this.areaImage_ != null)) {
      this.areaImage_ =
          new WImage(
              new WLink(WApplication.getInstance().getOnePixelGifUrl()), (WContainerWidget) null);
      this.widgetAdded(this.areaImage_);
      if (this.getPositionScheme() == PositionScheme.Static) {
        this.setPositionScheme(PositionScheme.Relative);
      }
      this.areaImage_.setPositionScheme(PositionScheme.Absolute);
      this.areaImage_.setOffsets(new WLength(0), EnumSet.of(Side.Left, Side.Top));
      this.areaImage_.setMargin(new WLength(0), EnumSet.of(Side.Top));
      this.areaImage_.resize(new WLength(this.renderWidth_), new WLength(this.renderHeight_));
      this.areaImageAdded_ = true;
    }
  }

  static WJavaScriptPreamble wtjs10() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WPaintedWidget",
        "(function(t,n){n.wtObj=this;const e=this;this.imagePreloaders=[];this.images=[];this.canvas=document.getElementById(\"c\"+n.id);this.repaint=function(){};this.widget=n;this.cancelPreloaders=function(){for(const t of e.imagePreloaders)t.cancel();e.imagePreloaders=[]}})");
  }

  static WJavaScriptPreamble wtjs11() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptObject,
        "gfxUtils",
        "function(){const t=1024,n=4080;return new function(){const e=this;this.path_crisp=function(t){return t.map((function(t){return[Math.floor(t[0])+.5,Math.floor(t[1])+.5,t[2]]}))};this.transform_mult=function(t,n){if(2===n.length){const e=n[0],r=n[1];return[t[0]*e+t[2]*r+t[4],t[1]*e+t[3]*r+t[5]]}if(3===n.length){if(8===n[2]||9===n[2])return n.slice(0);const e=n[0],r=n[1];return[t[0]*e+t[2]*r+t[4],t[1]*e+t[3]*r+t[5],n[2]]}if(4===n.length){let r,i,o,s,a;const c=e.transform_mult(t,[n[0],n[1]]);r=c[0];o=c[0];i=c[1];s=c[1];for(let c=0;c<3;++c){a=e.transform_mult(t,0===c?[e.rect_left(n),e.rect_bottom(n)]:1===c?[e.rect_right(n),e.rect_top(n)]:[e.rect_right(n),e.rect_bottom(n)]);r=Math.min(r,a[0]);o=Math.max(o,a[0]);i=Math.min(i,a[1]);s=Math.max(s,a[1])}return[r,i,o-r,s-i]}return 6===n.length?[t[0]*n[0]+t[2]*n[1],t[1]*n[0]+t[3]*n[1],t[0]*n[2]+t[2]*n[3],t[1]*n[2]+t[3]*n[3],t[0]*n[4]+t[2]*n[5]+t[4],t[1]*n[4]+t[3]*n[5]+t[5]]:[]};this.transform_apply=function(t,n){const r=e.transform_mult;return n.map((function(n){return r(t,n)}))};this.transform_det=function(t){const n=t[0],e=t[2],r=t[1];return n*t[3]-r*e};this.transform_adjoint=function(t){const n=t[0],e=t[2],r=t[1],i=t[3],o=t[4],s=t[5];return[i,-e,-r,n,s*r-o*i,-(s*n-o*e)]};this.transform_inverted=function(t){const n=e.transform_det(t);if(0!==n){const r=e.transform_adjoint(t);return[r[0]/n,r[2]/n,r[1]/n,r[3]/n,r[4]/n,r[5]/n]}console.log(\"inverted(): oops, determinant == 0\");return t};this.transform_assign=function(t,n){t[0]=n[0];t[1]=n[1];t[2]=n[2];t[3]=n[3];t[4]=n[4];t[5]=n[5]};this.transform_equal=function(t,n){return t[0]===n[0]&&t[1]===n[1]&&t[2]===n[2]&&t[3]===n[3]&&t[4]===n[4]&&t[5]===n[5]};this.css_text=function(t){return\"rgba(\"+t[0]+\",\"+t[1]+\",\"+t[2]+\",\"+t[3]+\")\"};this.arcPosition=function(t,n,e,r,i){const o=-i/180*Math.PI;return[t+e*Math.cos(o),n+r*Math.sin(o)]};this.pnpoly=function(t,n){let r=!1,i=0,o=0;const s=t[0],a=t[1];let c,l;for(let t=0;t<n.length;++t){c=i;l=o;if(7===n[t][2]){const r=e.arcPosition(n[t][0],n[t][1],n[t+1][0],n[t+1][1],n[t+2][0]);c=r[0];l=r[1]}else if(9===n[t][2]){const r=e.arcPosition(n[t-2][0],n[t-2][1],n[t-1][0],n[t-1][1],n[t][0]+n[t][1]);c=r[0];l=r[1]}else if(8!==n[t][2]){c=n[t][0];l=n[t][1]}0!==n[t][2]&&o>a!=l>a&&s<(c-i)*(a-o)/(l-o)+i&&(r=!r);i=c;o=l}return r};this.rect_intersection=function(t,n){t=e.rect_normalized(t);n=e.rect_normalized(n);const r=e.rect_top,i=e.rect_bottom,o=e.rect_left,s=e.rect_right,a=Math.max(o(t),o(n)),c=Math.min(s(t),s(n)),l=Math.max(r(t),r(n));return[a,l,c-a,Math.min(i(t),i(n))-l]};this.drawRect=function(t,n,r,i){n=e.rect_normalized(n);const o=e.rect_top(n),s=e.rect_bottom(n),a=e.rect_left(n),c=e.rect_right(n),l=[[a,o,0],[c,o,1],[c,s,1],[a,s,1],[a,o,1]];e.drawPath(t,l,r,i,!1)};this.drawPath=function(t,n,r,i,o){let s=0,a=[],c=[],l=[];const f=1048576;function u(t){return t[0]}function h(t){return t[1]}function m(t){return t[2]}t.beginPath();n.length>0&&0!==m(n[0])&&t.moveTo(0,0);for(s=0;s<n.length;s++){const _=n[s];switch(m(_)){case 0:Math.abs(u(_))<=f&&Math.abs(h(_))<=f&&t.moveTo(u(_),h(_));break;case 1:!function(){const a=0===s?[0,0]:n[s-1];if(!r&&!o&&i&&(Math.abs(u(a))>f||Math.abs(h(a))>f||Math.abs(u(_))>f||Math.abs(h(_))>f)){!function(){const n=t.wtTransform?t.wtTransform:[1,0,0,1,0,0],r=e.transform_inverted(n),i=e.transform_mult(n,a),o=e.transform_mult(n,_),s=u(o)-u(i),c=h(o)-h(i),l=-50,f=t.canvas.width+50,m=-50,p=t.canvas.height+50;function g(t,n,e){return(e-t)/n}function d(t,n,e){return t+e*n}let b,w,P,M=null,T=null,y=null,x=null;if(u(i)<l&&u(o)>l){b=g(u(i),s,l);M=[l,d(h(i),c,b),b]}else if(u(i)>f&&u(o)<f){b=g(u(i),s,f);M=[f,d(h(i),c,b),b]}else{if(!(u(i)>l&&u(i)<f))return;M=[u(i),h(i),0]}if(h(i)<m&&h(o)>m){b=g(h(i),c,m);T=[d(u(i),s,b),m,b]}else if(h(i)>p&&h(o)<p){b=g(h(i),c,p);T=[d(u(i),s,b),p,b]}else{if(!(h(i)>m&&h(i)<p))return;T=[u(i),h(i),0]}w=M[2]>T[2]?[M[0],M[1]]:[T[0],T[1]];if(!(u(w)<l||u(w)>f||h(w)<m||h(w)>p)){if(u(i)<f&&u(o)>f){b=g(u(i),s,f);y=[f,d(h(i),c,b),b]}else if(u(i)>l&&u(o)<l){b=g(u(i),s,l);y=[l,d(h(i),c,b),b]}else{if(!(u(o)>l&&u(o)<f))return;y=[u(o),h(o),1]}if(h(i)<p&&h(o)>p){b=g(h(i),c,p);x=[d(u(i),s,b),p,b]}else if(h(i)>m&&h(o)<m){b=g(h(i),c,m);x=[d(u(i),s,b),m,b]}else{if(!(u(o)>m&&h(o)<p))return;x=[u(o),h(o),1]}P=y[2]<x[2]?[y[0],y[1]]:[x[0],x[1]];if(!(u(P)<l||u(P)>f||h(P)<m||h(P)>p)){w=e.transform_mult(r,w);P=e.transform_mult(r,P);t.moveTo(w[0],w[1]);t.lineTo(P[0],P[1])}}}();Math.abs(u(_))<=f&&Math.abs(h(_))<=f&&t.moveTo(u(_),h(_))}else t.lineTo(u(_),h(_))}();break;case 2:case 3:a.push(u(_),h(_));break;case 4:a.push(u(_),h(_));t.bezierCurveTo.apply(t,a);a=[];break;case 7:c.push(u(_),h(_));break;case 8:c.push(u(_));break;case 9:!function(){function n(t){const n=t%360;return n<0?n+360:n}function e(t){return t*Math.PI/180}const r=u(_),i=h(_),o=e(n(-r));let s;s=i>=360||i<=-360?o-2*Math.PI*(i>0?1:-1):e(n(-r-((a=i)>360?360:a<-360?-360:a)));var a;const l=i>0;c.push(o,s,l);t.arc.apply(t,c);c=[]}();break;case 5:l.push(u(_),h(_));break;case 6:l.push(u(_),h(_));t.quadraticCurveTo.apply(t,l);l=[]}}r&&t.fill();i&&t.stroke();o&&t.clip()};this.drawStencilAlongPath=function(t,n,r,i,o,s){function a(t){return t[1]}function c(t){return t[2]}for(let f=0;f<r.length;f++){const u=r[f];if((!s||!t.wtClipPath||e.pnpoly(u,e.transform_apply(t.wtClipPathTransform,t.wtClipPath)))&&(0===c(u)||1===c(u)||6===c(u)||4===c(u))){const r=e.transform_apply([1,0,0,1,(l=u,l[0]),a(u)],n);e.drawPath(t,r,i,o,!1)}}var l};this.drawText=function(r,i,o,s,a){if(a&&r.wtClipPath&&!e.pnpoly(a,e.transform_apply(r.wtClipPathTransform,r.wtClipPath)))return;const c=o&n;let l=null,f=null;switch(15&o){case 1:r.textAlign=\"left\";l=e.rect_left(i);break;case 2:r.textAlign=\"right\";l=e.rect_right(i);break;case 4:r.textAlign=\"center\";l=e.rect_center(i).x}switch(c){case 128:r.textBaseline=\"top\";f=e.rect_top(i);break;case t:r.textBaseline=\"bottom\";f=e.rect_bottom(i);break;case 512:r.textBaseline=\"middle\";f=e.rect_center(i).y}if(null===l||null===f)return;const u=r.fillStyle;r.fillStyle=r.strokeStyle;r.fillText(s,l,f);r.fillStyle=u};this.calcYOffset=function(n,e,r,i){return 512===i?-(e-1)*r/2+n*r:128===i?n*r:i===t?-(e-1-n)*r:0};this.drawTextOnPath=function(t,r,i,o,s,a,c,l,f){function u(t){return t[0]}function h(t){return t[1]}function m(t){return t[2]}const _=e.transform_apply(o,s);for(let o=0;o<s.length&&!(o>=r.length);o++){const p=s[o],g=_[o],d=r[o].split(\"\\n\");if(0===m(p)||1===m(p)||6===m(p)||4===m(p))if(0===a)for(let r=0;r<d.length;r++){const o=e.calcYOffset(r,d.length,c,l&n);e.drawText(t,[i[0]+u(g),i[1]+h(g)+o,i[2],i[3]],l,d[r],f?[u(g),h(g)]:null)}else{const r=a*Math.PI/180,o=Math.cos(-r),s=-Math.sin(-r),m=-s,_=o;t.save();t.transform(o,m,s,_,u(g),h(g));for(let r=0;r<d.length;r++){const o=e.calcYOffset(r,d.length,c,l&n);e.drawText(t,[i[0],i[1]+o,i[2],i[3]],l,d[r],f?[u(g),h(g)]:null)}t.restore()}}};this.setClipPath=function(t,n,r,i){if(i){t.setTransform.apply(t,r);e.drawPath(t,n,!1,!1,!0);t.setTransform(1,0,0,1,0,0)}t.wtClipPath=n;t.wtClipPathTransform=r};this.removeClipPath=function(t){delete t.wtClipPath;delete t.wtClipPathTransform};this.rect_top=function(t){return t[1]};this.rect_bottom=function(t){return t[1]+t[3]};this.rect_right=function(t){return t[0]+t[2]};this.rect_left=function(t){return t[0]};this.rect_topleft=function(t){return[t[0],t[1]]};this.rect_topright=function(t){return[t[0]+t[2],t[1]]};this.rect_bottomleft=function(t){return[t[0],t[1]+t[3]]};this.rect_bottomright=function(t){return[t[0]+t[2],t[1]+t[3]]};this.rect_center=function(t){return{x:(2*t[0]+t[2])/2,y:(2*t[1]+t[3])/2}};this.rect_normalized=function(t){let n,e,r,i;if(t[2]>0){n=t[0];r=t[2]}else{n=t[0]+t[2];r=-t[2]}if(t[3]>0){e=t[1];i=t[3]}else{e=t[1]+t[3];i=-t[3]}return[n,e,r,i]};this.drawImage=function(t,n,e,r,i){try{t.drawImage(n,r[0],r[1],r[2],r[3],i[0],i[1],i[2],i[3])}catch(t){let n=\"Error while drawing image: '\"+e+\"': \"+t.name;t.message&&(n+=\": \"+t.message);console.error(n)}}}}()");
  }

  static WJavaScriptPreamble wtjs20() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WJavaScriptObjectStorage",
        "(function(t,r){r.wtJSObj=this;const e=this;function n(t){return\"[object Object]\"===Object.prototype.toString.call(t)}function o(t){if(Array.isArray(t)){const r=[];for(let e=0;e<t.length;++e)r.push(o(t[e]));return r}if(n(t)){const r={};for(const[e,n]of Object.entries(t))r[e]=o(n);return r}return t}function s(t,r){if(t===r)return!0;if(Array.isArray(t)&&Array.isArray(r)){if(t.length!==r.length)return!1;for(let e=0;e<t.length;++e)if(!s(t[e],r[e]))return!1;return!0}if(n(t)&&n(r)){for(const[e,n]of Object.entries(t)){if(!Object.prototype.hasOwnProperty.call(r,e))return!1;if(!s(n,r[e]))return!1}for(const e of Object.keys(r))if(!Object.prototype.hasOwnProperty.call(t,e))return!1;return!0}return!1}function c(t){return Array.isArray(t)&&t.length>6}const i={};this.jsValues=[];this.setJsValue=function(t,r){c(r)||(i[t]=o(r));e.jsValues[t]=r};r.wtEncodeValue=function(){const t={};for(let r=0;r<e.jsValues.length;++r){const n=e.jsValues[r];c(n)||s(n,i[r])||(t[r]=n)}return JSON.stringify(t)}})");
  }
}
